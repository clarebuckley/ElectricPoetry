package PoetryGenerator.Generator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class TemplateFiller {
	MongoInterface mongo = new MongoInterface("poetryDB-modern");
	ArrayList<ArrayList<String[]>> template;

	public TemplateFiller() {

	}


	public ArrayList<String> processTemplate(ArrayList<ArrayList<String[]>> template) {
		ArrayList<String> poem = new ArrayList<String>();
		for(int i = 0; i < template.size(); i++) {
			ArrayList<String[]> line = template.get(i);
			for(String[] tags : line) {
				String newLine = getLine(tags);
				poem.add(newLine);
			}
			poem.add(System.lineSeparator());
		}
		return poem;
	}

	private String getLine(String[] tags){
		Random random = new Random();
		String line = "";
		for(int i = 0; i < tags.length; i++) {	
			String punctuation = ".,:;";

			if(punctuation.contains(tags[i])) {
				line = line.substring(0, line.length()-1);
				line += tags[i];
			} else {
				ArrayList<String> words = (ArrayList<String>) mongo.getTagWords("wordbank", tags[i]);
				int numOfWords = words.size();
				int randomIndex = random.nextInt(numOfWords);	
				line += words.get(randomIndex);	
			} 

			if(i != tags.length-1) {
				line += " ";
			}


			line = line.substring(0, 1).toUpperCase() + line.substring(1);

		}
		return line;
	}


}
