package PoetryGenerator.Generator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.Document;
/**
 * Retrieves a poem POS template from the database to be filled
 * @author Clare Buckley
 * @version 29/01/19
 *
 */

//TODO: retrieval of templates needs to be revisited - try using http://www.thejavageek.com/2015/08/24/retrieve-array-from-mongodb-using-java/ 


public class TemplateMutator {
	private final MongoInterface mongo = new MongoInterface("poetryDB-modern");
	private final String collection = "verses";
	private final int numVerses;
	//Complete verses made of POS tags
	ArrayList<ArrayList<String[]>> completeVerses;
	//Original poem text
	ArrayList<ArrayList<String[]>> originalText;
	//Templates to be returned
	ArrayList<String[]> originalTemplate;
	ArrayList<String[]> verseTemplate;

	public TemplateMutator(int numVerses) {
		this.completeVerses = new ArrayList<ArrayList<String[]>>();
		this.originalText = new ArrayList<ArrayList<String[]>>();
		this.numVerses = numVerses;

		//Get required number of verses from database
		for(int i = 0; i < numVerses; i++) {
			long docCount = mongo.getDocumentCount(collection);
			Random random = new Random();
			int randomIndex = random.nextInt((int)docCount);
			System.out.println(randomIndex);
			Document template = mongo.getDocument(collection, randomIndex);
			originalTemplate = getTemplate(template, "text");
			verseTemplate = getTemplate(template, "POS");
			completeVerses.add(verseTemplate);
			originalText.add(originalTemplate);
		}
	}


	/**
	 * 
	 * @param template - Document retrieved from database
	 * @param textType - either 'POS' or 'text'
	 * @return template to be used for this verse
	 */
	private ArrayList<String[]> getTemplate(Document template, String textType) {
		//Get random verse POS from database
		String templateString = template.get(textType).toString();
		int numLines = (Integer) template.get("numLines");
		//Remove start [ and end ]
		templateString = templateString.substring(1);
		templateString = templateString.substring(0, templateString.length());

		//Get content for each line in verse
		String[] linesToProcess = new String[numLines];
		Pattern pattern = Pattern.compile("\\[(.*?)\\]");
		Matcher matcher = pattern.matcher(templateString);
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


	/**
	 * Retrieve poem POS template
	 * @return completeVerses
	 */
	public ArrayList<ArrayList<String[]>> getPoemTemplate() {
		return completeVerses;
	}

	/**
	 * Retrieve original text from the poem
	 * @return originalText
	 */
	public  ArrayList<ArrayList<String[]>> getPoemText(){
		return originalText;
	}

}
