package PoetryGenerator.Generator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bson.Document;

/**
 * Fill in a POS template using wordbank stored in database
 * @author Clare Buckley
 * @version 29/01/19
 *
 */

public class TemplateFiller {
	MongoInterface mongo = new MongoInterface("poetryDB-modern");
	ArrayList<ArrayList<String[]>> template;
	//If getLine encounters the listed POS tags, the original poem words for that tag will be used in the line
	ArrayList<String> retainOriginal = new ArrayList<String>(Arrays.asList("IN", "PRP", "VB", "DT","CC","PRP$","TO","WRB","-RRB-","-LRB-","VBG"));

	/**
	 * Process the POS poem template to create meaningful poem
	 * @param template2 - POS poem template
	 * @param poemText - original poem text
	 * @return poem made of words from wordbank
	 */
	public ArrayList<String> processTemplate(List<Document> template2, List<Document> poemText) {
		ArrayList<String> poem = new ArrayList<String>();
		String line = "";
		String punctuation = ".,:;``-";		//TODO: fix punctuation

	
		//Process line by line
		for(int i = 0; i < template2.size(); i++) {
			List<String> templateLine = (List<String>) template2.get(i);
			List<String> originalLine = (List<String>)poemText.get(i);

			//Each word in a line
			for(int j = 0; j < templateLine.size(); j++) {
				line += getWord(templateLine.get(j), originalLine.get(j));
				line += " ";
				poem.add(line);
				poem.add(System.lineSeparator());
			}
			
			
			
			//TODO: add spacing/capitalization
//			//Don't add space after word if end of line
//			if(i != string.size()-1) {
//				line += " ";
//			}
//
//			//Set start of lines to have capital letters
//			line = line.substring(0, 1).toUpperCase() + line.substring(1);


			
			
		}
		return poem;
	}

	/**
	 * Replace tags in the given line with words
	 * @param string - POS tags
	 * @param string2 - original words in the poem
	 * @return line - a poem line made from English words
	 */
	private String getWord(String templateWord, String originalWord){
		Random random = new Random();
		String word = "";
		String punctuation = ".,:;``-''";


		//Only replace some words, keep others same as in original text
		 if (retainOriginal.contains(templateWord)) {
			//				System.out.println(i);
			//				System.out.println(Arrays.toString(originalWords));
			//				System.out.println(Arrays.toString(tags));
			//				System.out.println(tags[i]);
			//				System.out.println(originalWords[i]);
			//				System.out.println("---------");
			word = originalWord;
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