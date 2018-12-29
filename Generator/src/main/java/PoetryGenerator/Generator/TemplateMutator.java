package PoetryGenerator.Generator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.Document;

public class TemplateMutator {
	private final MongoInterface mongo = new MongoInterface("poetryDB-modern");
	private final String collection = "verses";
	private final int numVerses;
	//Complete verses made of POS tags
	ArrayList<ArrayList<String[]>> completeVerses;
	//Complete verses made of original lines TODO: get these to be used
	ArrayList<ArrayList<String>> completeLines;

	public TemplateMutator(int numVerses) {
		this.completeVerses = new ArrayList<ArrayList<String[]>>();
		this.numVerses = numVerses;

		//Get required number of verses
		for(int i = 0; i < numVerses; i++) {
			ArrayList<String[]> verseTemplate = getTemplate();
			completeVerses.add(verseTemplate);
		}
	}


	/**
	 * Get template from database and translate to ArrayList<String[]>
	 * @return template to be used for this verse
	 */
	private ArrayList<String[]> getTemplate() {
		//Get random verse POS from database
		Random random = new Random();
		long docCount = mongo.getDocumentCount(collection);
		int randomIndex = random.nextInt((int)docCount);
		Document template = mongo.getDocument(collection, randomIndex);
		String posString = template.get("POS").toString();
		int numLines = (Integer) template.get("numLines");
		//Remove start [
		posString = posString.substring(1);

		//Get content for each line in verse
		String[] linesToProcess = new String[numLines];
		Pattern pattern = Pattern.compile("\\[(.*?)\\]");
		Matcher matcher = pattern.matcher(posString);
		int i = 0;
		while (matcher.find()) {
			linesToProcess[i] = matcher.group(1);
			i++;
		}

		ArrayList<String[]> verse = new ArrayList<String[]>();
		for(String line : linesToProcess) {
			line = line.toString();
			String[] tokens = line.split(", ");
			verse.add(tokens);
		}

		return verse;
	}
	
	
	public ArrayList<ArrayList<String[]>> getPoemTemplate() {
		return completeVerses;
	}
	
	public ArrayList<ArrayList<String>> getPoemLines(){
		return completeLines;
	}

}
