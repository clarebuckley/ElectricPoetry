package PoetryGenerator.Generator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bson.Document;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.language.BritishEnglish;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

/**
 * Fill in a POS template using wordbank stored in database
 * @author Clare Buckley
 * @version 13/02/19
 *
 */

public class TemplateFiller {
	private MongoInterface mongo = new MongoInterface("poetryDB");
	private JLanguageTool langTool = new JLanguageTool(new BritishEnglish());
	//If getLine encounters the listed POS tags, the original poem words for that tag will be used in the line
	private ArrayList<String> retainOriginal = new ArrayList<String>(Arrays.asList("IN", "PRP", "VB", "DT","CC","PRP$","TO","WRB","-RRB-","-LRB-","-lrb-","VBG","VBP", "VBZ"));
	private String punctuation = ".,:;-'''!";
	//List of grammar rules a line breaks
	private List<RuleMatch> matches;
	//Flag for validity of current word
	private boolean wordValid = false;

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
				line = processLine(templateLine,  originalLine);
				while(!checkValidLine(line)) {
					System.out.println("fixing grammar: " + line);
					line = fixGrammar(line, matches);
				} 
				poemVerse.add(line);
				//Reset for next line
				line = "";
			}
			//Add line break between verse
			poemVerse.add(System.lineSeparator());
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
			String word = "";
			wordValid = false;
			while(!checkValidWord(word)) {
				System.out.println("replacing word " + word);
//				if(templateLine.get(j) == "``") {
//					templateLine.set(j,  "''");
//				}
//				if(templateLine.get(j) == "`" ) {
//					templateLine.set(j,  "'");
//				}
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
		if ((retainOriginal.contains(templateWord) && wordValid) || templateWord == "-lrb-") {
			word = originalWord;
		}
		//Replace tags with words from wordbank
		else if(!punctuation.contains(templateWord) && !templateWord.equals("``")) {
			System.out.println(templateWord + ", " + originalWord);
			if(templateWord.equals("``")) {
				System.out.println("??????");
			}
			ArrayList<String> words = (ArrayList<String>) mongo.getTagWords("wordbank", templateWord);
			int numOfWords = words.size();
			int randomIndex = random.nextInt(numOfWords);
			word = words.get(randomIndex);	
			System.out.println(originalWord + " --> " + word);
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
		line = line.replaceAll(" i ", " I ");
		line = line.replaceAll("::", ":");
		line = line.replaceAll(" 's", "'s");
		line = line.replaceAll(" 'll", "'ll");
		line = line.replaceAll(" 'd", "'d");
		line = line.replaceAll("!", "! ");
		
		//match constant following 'an' --> "^an [b-df-hj-np-tv-z]"

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
			wordValid = false;
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
				if(matches.size() > 0) {
					System.out.println("word not valid: " + word);
					wordValid = false;
					return false;
				} else {
					wordValid = true;
					return true;
				}
			} catch (IOException e) {
				System.out.println("Error checking valid word");
				e.printStackTrace();
				return false;
			}
		}
	}

	/**
	 * Checks that a poem line is valid before adding it to verse
	 * @param line
	 * @return true if line is grammatically valid
	 */
	public boolean checkValidLine(String line) {
		if(line.length() > 0) {
			//Enable all grammar rules
			for (Rule rule : langTool.getAllRules()) {
				langTool.enableRule(rule.getId());	
			}
			try {
				matches = langTool.check(line);
				//Empty string is valid
				if(matches.size() > 0) {
					for(RuleMatch match : matches) {
						if(match.getRule().getId() != "And") {
							return true;
						} else {
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
	 * Fix grammar for given line
	 * @param line
	 * @param matches
	 */
	private String fixGrammar(String line, List<RuleMatch> matches) {
		for (RuleMatch match : matches) {
			//Don't check for ignored rules
			String ruleId = match.getRule().getId();
			if(ruleId != "And"){
				int from = match.getFromPos();
				int to = match.getToPos();
				System.out.println(line);
				System.out.println(line.substring(from,to) +  "--> " + match.getRule().getId() + ": " + match.getMessage());
				List<String> suggestions = match.getSuggestedReplacements();
				if(suggestions.size() > 0) {
					line = replaceWithSuggestion(line, from, to, suggestions);
				} 
				else if(ruleId == "SENTENCE_FRAGMENT") {
					line += "?";
				}
				else if(ruleId == "E_PRIME_STRICT") {
					line = replaceVerb(line, from, to);
				}
				else {
					System.out.println("other rule --> " + ruleId);
				}

			}
		}
		System.out.println(line);
		return line;
	}

	/**
	 * Replace incorrect grammar with suggestions
	 * @param line - whole line containing error
	 * @param from - start index of error
	 * @param to - end index of error
	 * @param suggestions - possible ways to correct issue
	 */
	private String replaceWithSuggestion(String line, int from, int to, List<String> suggestions) {
		Random random = new Random();
		String toReplace = line.substring(from,to);
		int randomIndex = random.nextInt(suggestions.size());
		String replacement = suggestions.get(randomIndex);
		line = line.replace(toReplace, replacement);
		System.out.println("Replaced '" + toReplace + "' with '" + replacement + "' --> " + line);

		return line;
	}

	/**
	 * Replace verb in the given line
	 * @param line
	 * @param from
	 * @param to
	 * @return
	 */
	private String replaceVerb(String line, int from, int to) {
		System.out.println("REPLACING VERB");
		String toReplace = line.substring(from,to);
		//TODO: get POS for toReplace
		List<AnalyzedSentence> templateWord;
		try {
			templateWord = langTool.analyzeText(toReplace);
			for(AnalyzedSentence test : templateWord) {
				System.out.println("---------------------------------------------------> " + test);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//String newVerb = getWord(templateWord, toReplace);
		return line;

	}
}