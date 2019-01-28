package PoetryGenerator.Generator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class TemplateFiller {
	MongoInterface mongo = new MongoInterface("poetryDB-modern");
	ArrayList<ArrayList<String[]>> template;

	public TemplateFiller() {

	}


	public ArrayList<String> processTemplate(ArrayList<ArrayList<String[]>> template, ArrayList<ArrayList<String[]>> poemText) {
		ArrayList<String> poem = new ArrayList<String>();
		
		//Each verse
		for(int i = 0; i < template.size(); i++) {
			ArrayList<String[]> templateLine = template.get(i);
			ArrayList<String[]> originalLine = poemText.get(i);
			//Each line
			for(int j = 0; j < templateLine.size(); j++) {
				String newLine = getLine(templateLine.get(j), originalLine.get(i));
				poem.add(newLine);
			}
		
			poem.add(System.lineSeparator());
		}
		return poem;
	}

	private String getLine(String[] tags, String[] originalWords){
		Random random = new Random();
		String line = "";
		for(int i = 0; i < tags.length; i++) {	
			String punctuation = ".,:;``-";

			//Add punctuation
			if(punctuation.contains(tags[i])) {
				//Remove space before punctuation
				if(line.length() > 0) {
					line = line.substring(0, line.length()-1);
				}

				line += tags[i];
			} 
			else if (tags[i] == "DT" || tags[i] == "IN" || tags[i] == "CC" || tags[i] == "PRP" || tags[i] == "PRP$" || tags[i] == "TO" || tags[i] == "WRB" || tags[i] == "-RRB-" || tags[i] == "-LRB-") {
				line = originalWords[i];
			}
			else {
				//Replace tags with words	
				ArrayList<String> words = (ArrayList<String>) mongo.getTagWords("wordbank", tags[i]);
				int numOfWords = words.size();
				int randomIndex = random.nextInt(numOfWords);	
				line += words.get(randomIndex);	
			} 

			//Don't add space after word if end of line
			if(i != tags.length-1) {
				line += " ";
			}

			//Set start of lines to have capital letters
			line = line.substring(0, 1).toUpperCase() + line.substring(1);

		}
		return line;

	}
}