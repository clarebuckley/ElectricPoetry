package PoetryGenerator.Generator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.Document;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.language.BritishEnglish;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

/**
 * Fill in a POS template using wordbank stored in database
 * @author Clare Buckley
 * @version 21/02/19
 *
 */

public class TemplateFiller {
	private MongoInterface mongo = new MongoInterface("poetryDB");
	private JLanguageTool langTool = new JLanguageTool(new BritishEnglish());
	//If getLine encounters the listed POS tags, the original poem words for that tag will be used in the line
	private ArrayList<String> retainOriginal = new ArrayList<String>(Arrays.asList("IN", "PRP", "VB", "DT","CC","PRP$","TO","WRB","-RRB-","-LRB-","-lrb-","-rrb-","VBG","VBP", "VBZ"));
	private String punctuation = ".,:;-'''`!";
	//List of grammar rules a line breaks
	private List<RuleMatch> matches;
	//Flag for validity of current word
	private boolean wordValid = false;

	private List<String> templateLine;
	private List<String> originalLine;

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
				templateLine = (List<String>) template.get(i);
				originalLine = (List<String>)poemText.get(i);
				line = processLine(templateLine,  originalLine);
//				while(!checkValidLine(line)) {
//					System.out.println("fixing grammar: " + line);
//					line = fixGrammar(line, matches);
//				} 
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
			while(!spellCheckWord(word) || !wordValid(word, line)) {
				System.out.println("replacing word " + word);
				word = getWord(templateLine.get(j), originalLine.get(j));
			}

			line += word;
			//Don't add space after word if it's the end of a line
			if(j < templateLine.size()) {
				line += " ";
			}
			//Remove space before punctuation
			line = line.replaceAll(" [.,:;`-]", word);
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
		if(templateWord.contains("`")) {
			word = "'";
		}
		//-lrb- and -rrb- should be translated to ( and ) respectively
		else if(templateWord == "-lrb-" || templateWord == "-LRB-" || originalWord == "-lrb-" || originalWord == "-LRB-") {
			word = "(";
		} else if(templateWord == "-rrb-" || templateWord == "-RRB-" || originalWord == "-rrb-" || originalWord == "-RRB-") {
			word = ")";
		}
		//Only replace some words, keep others same as in original text
		else if ((retainOriginal.contains(templateWord) && wordValid) || punctuation.contains(templateWord)) {
			word = originalWord;
		}
		//Replace tags with words from wordbank
		else if(!punctuation.contains(templateWord)) {
			System.out.println(templateWord + ", " + originalWord);
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
		line = line.replaceAll(" !", "!");
		line = line.replaceAll(" \\?", "?");

		//For consonants following 'an', change to 'a'
		Pattern pattern = Pattern.compile("an [b-df-hj-np-tv-z]");
		Matcher matcher = pattern.matcher(line);
		while(matcher.find()){
			String match = line.substring(matcher.start(), matcher.start()+1);
			line.replace(match, "a");
		}


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
	 * Returns false if word is spelled incorrectly
	 * @param line
	 * @return true if word is contained in dictionary
	 */
	public boolean spellCheckWord(String word) {
		if(word.length() == 0) {
			wordValid = false;
			return false;
		} 
		else {	
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
	 * 
	 * Checks whether word is valid in context of its containing line
	 * @param word
	 * @param line
	 * @return
	 */
	public boolean wordValid(String word, String line) {
		//Can't start line with 'because' --> ruleId SENTENCE_FRAGMENT
		if(line.split(" ")[0] == word && word == "because") {
			return false;
		}
		return true;
	}
	

	/**
	 * Checks that a poem line is valid before adding it to verse
	 * @param line
	 * @return true if line is grammatically valid
	 */
//	public boolean checkValidLine(String line) {
//		if(line.length() > 0) {
//			//Enable all grammar rules
//			for (Rule rule : langTool.getAllRules()) {
//				String id = rule.getId();
//				if(!id.equals("And") && !id.equals("But")) {
//					langTool.enableRule(rule.getId());	
//				}
//			}
//			try {
//				System.out.println(line);
//				matches = langTool.check(line);
//				System.out.println(matches.size());
//				if(matches.size() > 0) {
//					for(RuleMatch match : matches) {
//						System.out.println(match.getMessage());
//						return false;
//					}
//				} else {
//					return true;
//				}
//			} catch (IOException e) {
//				System.out.println("Error checking valid line");
//				e.printStackTrace();
//			}
//		} else {
//			return false;
//		}
//
//		return true;
//	}



	/**
	 * Fix grammar for given line
	 * @param line
	 * @param matches
	 */
//	private String fixGrammar(String line, List<RuleMatch> matches) {
//		for (RuleMatch match : matches) {
//			String ruleId = match.getRule().getId();
//			if(!ruleId.equals("And") && !ruleId.equals("But")){
//				int from = match.getFromPos();
//				int to = match.getToPos();
//				System.out.println(ruleId + ", " + line);
//				List<String> suggestions = match.getSuggestedReplacements();
//				if(suggestions.size() > 0) {
//					System.out.println("has suggestions");
//					line = replaceWithSuggestion(line, from, to, suggestions);
//				} 
//				else if(ruleId.equals("SENTENCE_FRAGMENT")) {
//					line += "?";
//				}
//				else if(ruleId.equals("EN_UNPAIRED_BRACKETS")) {
//					line = line.replaceAll("'", "");
//				}
//				else if(ruleId.equals("E_PRIME_STRICT") || ruleId.equals("MORFOLOGIK_RULE_EN_GB") || ruleId.equals("E_PRIME_LOOSE")) {
//					line = replaceWord(line, from, to);
//				}
//				else if(ruleId.contains("READABILITY_RULE") || ruleId .equals("SENTENCE_FRAGMENT")) {
//					line = fixReadability(line);
//				}
//				else if(ruleId.equals("USELESS_THAT") || ruleId.equals("TIRED_INTENSIFIERS")) {
//					System.out.println(match.getFromPos() + " - " + match.getToPos() + " in: " + line );
//					String toReplace = line.substring(match.getFromPos(), match.getToPos());
//					line = line.replace(toReplace, "");
//				}
//				//The most common meaning of the verb 'to respect' is 'to show deferential regard for'. It can also mean 'to avoid violation of' (rules, for example), but with this meaning it is over-used in EU texts and often in a grammatically awkward manner. Alternatives: comply with, adhere to, meet (a deadline), compliance with.
//				else if(ruleId.equals("EUPUB_RESPECT")) {
//					String alternatives = match.getMessage().split("Alternatives:")[1];
//					String altArray[] = alternatives.split(",");
//					int respectFrom = line.indexOf("respect");
//					int respectTo = respectFrom + "respect".length(); 
//					for(String alternative : altArray) {
//						suggestions.add(alternative.trim());
//					}
//					line = replaceWithSuggestion(line, respectFrom, respectTo, suggestions);
//				}
//				else {
//					System.out.println("other rule --> " + ruleId);
//					System.out.println(match.getRule().getDescription());
//					if(suggestions.size()>0) {
//						System.out.println(suggestions.get(0));
//					} else {
//						System.out.println("no suggestions");
//					}
//				}
//
//			}
//		}
//		matches.clear();
//		return line;
//	}

	/**
	 * Replace incorrect grammar with suggestions
	 * @param line - whole line containing error
	 * @param from - start index of error
	 * @param to - end index of error
	 * @param suggestions - possible ways to correct issue
	 */
//	private String replaceWithSuggestion(String line, int from, int to, List<String> suggestions) {
//		Random random = new Random();
//		int randomIndex = random.nextInt(suggestions.size());
//		int fromIndex;
//		int toIndex;
//		if(from > 0) {
//			fromIndex = from -1;
//		} else {
//			fromIndex = 0;
//		}
//		if(to == line.length()-1) {
//			toIndex = to;
//		} else {
//			toIndex = to - 1;
//		}
//		String toReplace = line.substring(fromIndex, toIndex);
//		System.out.println("from " + from + " - to " + to);
//		String replacement = suggestions.get(randomIndex);
//
//		//line.replace doesn't work: can't be sure that toReplace pattern won't be repeated throughout line
//		String contentBefore = line.substring(0, from);
//		String contentAfter = line.substring(to, line.length());
//		System.out.println("from " + from + " to " + to + " --> " + toReplace);
//
//		line = contentBefore + replacement + contentAfter;
//
//
//		System.out.println("Replaced '" + toReplace + "' with '" + replacement + "' --> " + line);
//
//		return line;
//	}

	/**
	 * Replace verb in the given line
	 * @param line
	 * @param from
	 * @param to
	 * @return
	 * TODO: check this works as expected
	 * WORK IN PROGRESS
	 */
//	private String replaceWord(String line, int from, int to) {
//		System.out.println("CORRECTION: REPLACING WORD");
//		String toReplace = line.substring(from,to);
//		//TODO: get POS for toReplace
//		List<AnalyzedSentence> templateWord;
//		try {
//			templateWord = langTool.analyzeText(toReplace);
//			for(AnalyzedSentence test : templateWord) {
//				System.out.println("---------------------------------------------------> " + test);
//				String newVerb = getWord(templateWord.toString(), toReplace);
//				line = line.replace(toReplace, newVerb);
//			}
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return line;
//	}


	/**
	 * TODO: will this be needed after n-grams?
	 * @param line
	 * @return
	 * WORK IN PROGRESS
	 */
//	public String fixReadability(String line) {
//		System.out.println("fixing readability");
//		line = processLine(templateLine,  originalLine);
//		return line;
//	}



}