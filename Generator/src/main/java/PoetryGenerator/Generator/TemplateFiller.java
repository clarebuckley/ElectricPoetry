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

/**
 * Fill in a POS template using wordbank stored in database
 * @author Clare Buckley
 * @version 30/01/19
 *
 */

public class TemplateFiller {
	MongoInterface mongo = new MongoInterface("poetryDB");
	JLanguageTool langTool = new JLanguageTool(new BritishEnglish());
	ArrayList<ArrayList<String[]>> template;
	//If getLine encounters the listed POS tags, the original poem words for that tag will be used in the line
	ArrayList<String> retainOriginal = new ArrayList<String>(Arrays.asList("IN", "PRP", "VB", "DT","CC","PRP$","TO","WRB","-RRB-","-LRB-","VBG","VBP", "VBZ"));
	String punctuation = ".,:;``-'''!";
	//Title to base poem on
	String title = "Do Androids Dream of Electric Sheep";
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

				//Each word in a line
				for(int j = 0; j < templateLine.size(); j++) {
					boolean wordValid = false;
					String word = "";
					while(!wordValid) {
						word = getWord(templateLine.get(j), originalLine.get(j));
						if(!punctuation.contains(word) && templateLine.get(j)!= "POS" ) {
							wordValid = checkValidWord(word);
							System.out.println(wordValid);
						}	else {
							wordValid = true;
						}
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

				boolean lineValid = false;
				String testLine = line;
				while(!lineValid) {
					lineValid = checkValidLine(testLine);
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
			System.out.println(templateWord);
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
	 * @return
	 */
	public boolean checkValidWord(String word) {
		List<RuleMatch> matches;
		
		try {
			System.out.println(word);
			matches = langTool.check(word);
			for (RuleMatch match : matches) {
				System.out.println(match.getMessage());
				if(match.getMessage() == "Possible spelling mistake found") {
					System.out.println("INVALID------------------------");
					return false;
				} 
			}
		} catch (IOException e) {
			System.out.println("Error checking valid word");
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * Checks that a poem line is valid before adding it to verse
	 * @param line
	 * @return
	 */
	public boolean checkValidLine(String line) {
		List<RuleMatch> matches;
		for (Rule rule : langTool.getAllRules()) {
			langTool.enableRule(rule.getId());
		}


//TODO: This part always errors?

		//		List<RuleMatch> matches;
		//		try {
		//			System.out.println(line);
		//			matches = langTool.check(line);
		//			for (RuleMatch match : matches) {
		//				//TODO Can you use this on the whole line to get grammar problems?
		//				System.out.println("     --> " + match.getMessage());
		//			}
		//		} catch (IOException e) {
		//			System.out.println("Error checking valid line");
		//			e.printStackTrace();
		//		}
		return true;
	}
}