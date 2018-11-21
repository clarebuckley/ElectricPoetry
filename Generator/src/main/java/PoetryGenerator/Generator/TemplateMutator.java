package PoetryGenerator.Generator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.Document;

public class TemplateMutator {
	private final MongoInterface mongo = new MongoInterface("poetryDB");
	private final String collection = "verses";

	public static void main(String[] args) {
		new TemplateMutator();
	}

	public TemplateMutator() {
		getTemplate();
	}


	public ArrayList<String[]> getTemplate() {
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
			System.out.println(line + "     | --> |      " + Arrays.toString(tokens));
			verse.add(tokens);
		}

		return verse;
	}

}
