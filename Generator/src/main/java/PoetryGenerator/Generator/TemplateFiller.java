package PoetryGenerator.Generator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bson.Document;
import org.languagetool.JLanguageTool;
import org.languagetool.language.BritishEnglish;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.spelling.SpellingCheckRule;

/**
 * Fill in a POS template using wordbank stored in database
 * @author Clare Buckley
 * @version 31/01/19
 *
 */

public class TemplateFiller {
	MongoInterface mongo = new MongoInterface("poetryDB");
	JLanguageTool langTool = new JLanguageTool(new BritishEnglish());
	ArrayList<ArrayList<String[]>> template;
	//If getLine encounters the listed POS tags, the original poem words for that tag will be used in the line
	ArrayList<String> retainOriginal = new ArrayList<String>(Arrays.asList("IN", "PRP", "VB", "DT","CC","PRP$","TO","WRB","-RRB-","-LRB-","VBG","VBP", "VBZ"));
	String punctuation = ".,:;``-'''!";
	int replacedCount = 0;

	/**
	 * Process the POS poem template to create meaningful poem
	 * @param template - POS poem template
	 * @param poemText - original poem text
	 * @return poem made of words from wordbank
	 */
	public ArrayList<ArrayList<String>> processTemplate(List<List<Document>> templateVerses, List<List<Document>> poemTextVerses) {
		ArrayList<ArrayList<String>> poemVerses = new ArrayList<ArrayList<String>>();
		String line = "";

		//Each verse
		for(int x = 0; x < templateVerses.size(); x++) {
			ArrayList<String> poemVerse = new ArrayList<String>();
			List<Document> template = templateVerses.get(x);
			List<Document> poemText = poemTextVerses.get(x);

			//Process line by line
			for(int i = 0; i < template.size(); i++) {
				List<String> templateLine = (List<String>) template.get(i);
				List<String> originalLine = (List<String>)poemText.get(i);


				while(!checkValidLine(line)) {
					line = processLine(templateLine,  originalLine);
				}
				poemVerse.add(line);

				//Reset for next line
				line = "";
				replacedCount = 0;
			}
			poemVerses.add(poemVerse);
		}
		return poemVerses;
	}

	/**
	 * Process each line in a verse
	 * @param templateLine
	 * @param originalLine
	 * @return String containing poem line
	 */
	private String processLine(List<String> templateLine, List<String> originalLine) {
		String line = "";
		//Each word in a line
		for(int j = 0; j < templateLine.size(); j++) {
			boolean wordValid = false;
			String word = "";
			while(!checkValidWord(word)) {
				word = getWord(templateLine.get(j), originalLine.get(j));
			}

			line += word;
			//Don't add space after word if it's the end of a line
			if(j < templateLine.size()) {
				line += " ";
			}
			//Remove space before punctuation
			line = line.replaceAll(" [.,:;``-]", word);
		}
		line = postProcessLine(line);
		return line;
	}

	/**
	 * Replace tags in the given line with words
	 * @param templateWord - POS tag
	 * @param originalWord - original word in the current place in the poem
	 * @return word - word to be used in this line
	 */
	private String getWord(String templateWord, String originalWord){
		Random random = new Random();
		String word = "";

		//Only replace some words, keep others same as in original text
		if (retainOriginal.contains(templateWord) || replacedCount > 5) {
			word = originalWord;
		}
		//Replace tags with words from wordbank
		else if(!punctuation.contains(templateWord)) {
			ArrayList<String> words = (ArrayList<String>) mongo.getTagWords("wordbank", templateWord);
			int numOfWords = words.size();
			int randomIndex = random.nextInt(numOfWords);	
			word = words.get(randomIndex);	
			System.out.println(originalWord + " --> " + word);
			replacedCount++;
		} else {
			word = templateWord;
		}

		return word;
	}

	/**
	 * Perform final grammatical changes to line before it's added to the verse
	 * @param line
	 * @return
	 */
	private String postProcessLine(String line) {
		//Set start of lines to have capital letters
		line = line.substring(0, 1).toUpperCase() + line.substring(1);

		//A apple --> an apple
		line = line.replaceAll("a a", "an a");
		line = line.replaceAll(" i ", " I ");
		line = line.replaceAll("::", ":");
		line = line.replaceAll("' s", "'s");

		String capitaliseResult = "";
		boolean capitalise = true;
		for(int i = 0; i < line.length(); i++) {
			//Current character
			char c = line.charAt(i);
			//If character is full stop, next character will be capitalised
			if(c == '.') {
				capitalise = true;
			}
			else if(capitalise && Character.isAlphabetic(c)) {
				c = Character.toUpperCase(c);
				//Don't capitalise next character
				capitalise = false;
			}
			capitaliseResult += c;
		}
		line = capitaliseResult.trim();
		return line;
	}


	/**
	 * Returns false if word is invalid
	 * @param line
	 * @return true if word is contained in dictionary
	 */
	public boolean checkValidWord(String word) {
		if(word.length() == 0 || word.contains("`")) {
			return false;
		} else {
			List<RuleMatch> matches;

			for (Rule rule : langTool.getAllRules()) {
				if (!rule.isDictionaryBasedSpellingRule()) {
					langTool.disableRule(rule.getId());
				}
			}
			try {
				matches = langTool.check(word);
				System.out.println(matches.size());
				if(matches.size() > 0) {
					return false;
				}
			} catch (IOException e) {
				System.out.println("Error checking valid word");
				e.printStackTrace();
			}
		}
		return true;
	}

	/**
	 * Checks that a poem line is valid before adding it to verse
	 * @param line
	 * @return true if line is grammatically valid
	 */
	public boolean checkValidLine(String line) {
		if(line.length() > 0) {
			List<RuleMatch> matches;

			for (Rule rule : langTool.getAllRules()) {
				langTool.enableRule(rule.getId());
			}
			try {
				ArrayList<String> ignoreRule = new ArrayList<String>(Arrays.asList("And", "READABILITY_RULE_SIMPLE", "E_PRIME_LOOSE"));
				System.out.println(line);
				matches = langTool.check(line);
				//Empty string is valid
				if(matches.size() > 0) {
					for (RuleMatch match : matches) {
						//Don't check for ignored rules
						String ruleId = match.getRule().getId();
						if(ignoreRule.contains(ruleId)){
							return true;
						} else {
							//Act on an invalid line
							int from = match.getFromPos();
							int to = match.getToPos();
							System.out.println(match.getFromPos() + " - " + match.getToPos() + "--> " + match.getRule().getId() + ": " + match.getMessage());
							List<String> suggestions = match.getSuggestedReplacements();
							if(suggestions.size() > 0) {
								fixGrammar(line, from, to, suggestions);
								return true;
							} else if(ruleId == "SENTENCE_FRAGMENT") {
								line += "?";
							}
							
							return false;
						}
					}
				} else {
					return true;
				}
			} catch (IOException e) {
				System.out.println("Error checking valid line");
				e.printStackTrace();
			}
		} else {
			return false;
		}

		return true;
	}
	
	/**
	 * Fixes grammar issues in a given line
	 * @param line - whole line containing error
	 * @param from - start index of error
	 * @param to - end index of error
	 * @param suggestions - possible ways to correct issue
	 */
	private void fixGrammar(String line, int from, int to, List<String> suggestions) {
		for(String suggestion : suggestions) {
			System.out.println(suggestion);
		}
		
		
	}
}