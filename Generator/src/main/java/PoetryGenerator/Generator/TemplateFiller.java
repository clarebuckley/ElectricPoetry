package PoetryGenerator.Generator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class TemplateFiller {
	MongoInterface mongo = new MongoInterface("poetryDB");
	ArrayList<ArrayList<String[]>> template;

	public TemplateFiller() {

	}


	public ArrayList<String> processTemplate(ArrayList<ArrayList<String[]>> template) {
		ArrayList<String> poem = new ArrayList<String>();
		for(int i = 0; i < template.size(); i++) {
			System.out.println("Verse " + i);
			ArrayList<String[]> line = template.get(i);
			for(String[] tags : line) {
				String newLine = getLine(tags);
				poem.add(newLine);
				System.out.println(newLine);
			}
		}
		return poem;
	}

	private String getLine(String[] tags){
		Random random = new Random();
		String line = "";
		for(int i = 0; i < tags.length; i++) {	
			//For characters that aren't punctuation
			if(Character.isLetter(tags[i].charAt(0))){
				ArrayList<String> words = (ArrayList<String>) mongo.getTagWords("wordBank", tags[i]);
				int numOfWords = words.size();
				int randomIndex = random.nextInt(numOfWords);
				line += words.get(randomIndex);	
			} 
			else {
				//Remove space before punctuation
				line = line.substring(0, line.length()-1);
				line = line + tags[i];
			}

			if(i != tags.length-1) {
				line += " ";
			}
			
		}
		return line;
	}


}
