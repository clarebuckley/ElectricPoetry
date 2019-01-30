package PoetryGenerator.Generator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bson.Document;

/**
 * Fill in a POS template using wordbank stored in database
 * @author Clare Buckley
 * @version 30/01/19
 *
 */

public class TemplateFiller {
	MongoInterface mongo = new MongoInterface("poetryDB-modern");
	ArrayList<ArrayList<String[]>> template;
	//If getLine encounters the listed POS tags, the original poem words for that tag will be used in the line
	ArrayList<String> retainOriginal = new ArrayList<String>(Arrays.asList("IN", "PRP", "VB", "DT","CC","PRP$","TO","WRB","-RRB-","-LRB-","VBG"));
	String punctuation = ".,:;``-'";	

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
					String word = getWord(templateLine.get(j), originalLine.get(j));
					line += word;
					//Don't add space after word if it's the end of a line
					if(j < templateLine.size()) {
						line += " ";
					}
					//Remove space before punctuation
					line = line.replaceAll(" [.,:;``-]", word);
				}
				//Set start of lines to have capital letters
				line = line.substring(0, 1).toUpperCase() + line.substring(1);
				poemVerse.add(line);

				//Reset for next line
				line = "";
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
		if (retainOriginal.contains(templateWord)) {
			word = originalWord;
		}
		else if(templateWord == "''") {
			templateWord = "``";
		}
		else if(originalWord == "''") {
			originalWord = "``";
		}
		//Replace tags with words from wordbank
		else if(!punctuation.contains(templateWord)) {
			System.out.println(templateWord);
			ArrayList<String> words = (ArrayList<String>) mongo.getTagWords("wordbank", templateWord);
			int numOfWords = words.size();
			int randomIndex = random.nextInt(numOfWords);	
			word = words.get(randomIndex);	
		} else {
			word = templateWord;
		}

		return word;
	}
}