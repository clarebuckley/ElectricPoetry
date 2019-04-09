package PoetryGenerator.Generator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.Document;

/**
 * Fill in a POS template using wordbank stored in database
 * @author Clare Buckley
 * @version 09/04/19
 *
 */

public class TemplateFiller {
	//If getLine encounters the listed POS tags, the original poem words for that tag will be used in the line
	private ArrayList<String> retainOriginal = new ArrayList<String>(Arrays.asList("IN"/*,"PRP", "VB", "DT","CC","PRP$","TO","WRB","-RRB-","-LRB-","-lrb-","-rrb-","VBG","VBD","VBP", "VBZ"*/));

	private String punctuation = ".,:;-'''`!";

	private List<String> templateLine;
	private List<String> originalLine;

	private NGramController ngram ;

	public TemplateFiller(String generationGram) {
		ngram  = new NGramController(generationGram);
	}


	/**
	 * Process the POS poem template to create meaningful poem
	 * @param template - POS poem template
	 * @param poemText - original poem text
	 * @return poem made of words found using n-grams
	 */
	@SuppressWarnings("unchecked")
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
				line = postProcessLine(line);
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
	 * @param templateLine - line made of POS tags
	 * @param originalLine - original poem that made the template
	 * @return String containing poem line
	 */
	private String processLine(List<String> templateLine, List<String> originalLine) {
		String line = "";
		//Each word in a line
		for(int i = 0; i < templateLine.size(); i++) {
			String[] completedWords = line.split(" ");
			String prevWord1 = "", prevWord2 = "", prevWord3 = "";
			prevWord1 = ((i>=1) ? completedWords[i-1].toLowerCase() : "");
			prevWord2 = ((i>=2) ? completedWords[i-2].toLowerCase() : "");
			prevWord3 = ((i>=3) ? completedWords[i-3].toLowerCase() : "");
			String prevWord1POS = "", prevWord2POS = "", prevWord3POS = "";
			prevWord1POS = ((i>=1) ? templateLine.get(i-1) : "");
			prevWord2POS = ((i>=2) ? templateLine.get(i-2) : "");
			prevWord3POS = ((i>=3) ? templateLine.get(i-3) : "");

			String word = "";
			word = getWord(templateLine.get(i), originalLine.get(i), prevWord1, prevWord2, prevWord3, prevWord1POS, prevWord2POS, prevWord3POS);


			line += word;
			//Don't add space after word if it's the end of a line
			if(i < templateLine.size()) {
				line += " ";
			}
		}

		return line;
	}

	/**
	 * Replace tags in the given line with words
	 * @param templateWord - POS tag
	 * @param originalWord - original word in the current place in the poem
	 * @param n1 - previous word in the poem
	 * @param n2 - n-2nd word in the poem
	 * @param n3 - n-3rd word in the poem
	 * @param line - the poem line so far
	 * @return word - word to be used in this line
	 */
	public String getWord(String templateWord, String originalWord, String n1, String n2, String n3, String n1POS, String n2POS, String n3POS){
		String word = "";
		if(templateWord.contains("`") || originalWord.contains("`")) {
			word = "'";
			templateWord= "'";
			originalWord= "'";
		}

		//Only replace some words, keep others same as in original text
		else if (retainOriginal.contains(templateWord) || punctuation.contains(templateWord)) {
			word = originalWord;
		}
		//Replace tags with words from wordbank
		else if(!punctuation.contains(templateWord)) {
			word = ngram.getWord(templateWord, originalWord, n1, n2, n3, n1POS, n2POS, n3POS);
			if(word == null) {
				word = originalWord;
			}

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

		line = line.replaceAll(" i ", " I ");
		line = line.replaceAll("::", ":");
		line = line.replaceAll(" 's", "'s");
		line = line.replaceAll(" 'll", "'ll");
		line = line.replaceAll(" 'd", "'d");
		line = line.replaceAll("!", "! ");
		line = line.replaceAll(" !", "!");
		line = line.replaceAll(" \\?", "?");
		line = line.replaceAll("'", "");
		line = line.replaceAll("_", ".");
		line = line.replaceAll("-lrb- ", " (");
		line = line.replaceAll("-rrb-", ") ");
		//Remove space before punctuation
		line = line.replaceAll("\\s+(?=\\p{Punct})", "");

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

			if(i == line.length() && line.charAt(i) == ',') {
				line = line.substring(0, line.length()-1) + ".";
			}
		}
		line = capitaliseResult.trim();
		return line;
	}

}