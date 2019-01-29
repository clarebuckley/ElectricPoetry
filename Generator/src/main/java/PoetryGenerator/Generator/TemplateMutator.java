package PoetryGenerator.Generator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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


public class TemplateMutator {
	private final MongoInterface mongo = new MongoInterface("poetryDB-modern");
	private final String collection = "verses";
	private final int numVerses;
	//Templates to be returned
	List<Document> originalTemplate;
	List<Document> verseTemplate;

	public TemplateMutator(int numVerses) {
		this.numVerses = numVerses;

		//Get required number of verses from database
		for(int i = 0; i < numVerses; i++) {
			long docCount = mongo.getDocumentCount(collection);
			Random random = new Random();
			int randomIndex = random.nextInt((int)docCount);
			Document template = mongo.getDocument(collection, randomIndex);
			originalTemplate = getTemplate(template, "text");
			verseTemplate = getTemplate(template, "POS");	
		}
	}


	/**
	 * Get template from document
	 * @param template - Document retrieved from database
	 * @param textType - either 'POS' or 'text'
	 * @return template to be used for this verse
	 */
	private List<Document> getTemplate(Document template, String textType) {
		//Get random verse POS from database
		String templateString = template.get(textType).toString();
		List<Document> verse = (List<Document>) template.get(textType);
		return verse;
	}


	/**
	 * Retrieve poem POS template
	 * @return completeVerses
	 */
	public List<Document> getPoemTemplate() {
		return verseTemplate;
	}

	/**
	 * Retrieve original text from the poem
	 * @return originalText
	 */
	public  List<Document> getPoemText(){
		return originalTemplate;
	}

}
