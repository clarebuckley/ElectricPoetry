package PoetryGenerator.Generator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.bson.Document;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

/**
 * Parses input texts and adds POS, dependencies, and text content to the database
 * @author Clare Buckley
 * @version 21/11/2018
 *
 */
public class PoemParser {

	private int docId;
	private InputStream file;
	private final MongoInterface mongo = new MongoInterface("poetryDB");

	public static void main(String args[]) throws ClassNotFoundException, IOException {
//		new PoemParser("/PoetryGenerator/Data/Poems 1817 by John Keats.txt");
	}

	public PoemParser(String filePath) throws ClassNotFoundException, IOException {
		this.file = this.getClass().getResourceAsStream(filePath);
		if(mongo.getLastEnteredId("verses") == -1) {
			this.docId = 1;
		} else {
			this.docId = mongo.getLastEnteredId("verses") + 1;
		}

		parseLinesInFile(file);	
	}

	public PoemParser()  {
		reAssignIds();
	}

	/**
	 * Used after documents have been deleted
	 */
	private void reAssignIds() {
		MongoCollection<Document> collection = mongo.getCollection("verses");
		// Getting the iterable object 
		FindIterable<Document> iterDoc = collection.find(); 
		Iterator<Document> it = iterDoc.iterator(); 
		
		int newId = 0;
		while (it.hasNext()) {  
			Document doc = it.next();
			Object id = doc.get("id");
			mongo.updateDocument("verses", "id", id, "id", newId);
			System.out.println("Changed " + id + " to " + newId);
			newId++;
		}
	}


	private void parseLinesInFile(InputStream file) throws IOException {
		int verseLines = 0;
		ArrayList<List<String>> versePosTags = new ArrayList<List<String>>();
		ArrayList<String> verseText = new ArrayList<String>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(file));
		String line = reader.readLine();

		//Loop through each line in the input text
		while (line != null) {
			if(!line.trim().isEmpty()) {
				//Create a StanfordCoreNLP object with POS tagging
				Properties props = new Properties();
				props.setProperty("annotators", "tokenize, ssplit, pos");
				StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

				//Create empty Annotation just with the given text
				Annotation document = new Annotation(line);

				//Run all Annotators on this text
				pipeline.annotate(document);

				//Sentences in the document
				List <CoreMap> sentences = document.get(SentencesAnnotation.class);
				List<String> posTags = new ArrayList<String>();

				for (CoreMap sentence : sentences) {
					//Get tokenized sentence
					List<CoreLabel> tokens = sentence.get(TokensAnnotation.class);

					for (CoreLabel token : tokens) {
						//Text of the token
						String word = token.get(TextAnnotation.class);
						word = word.toLowerCase();

						//POS tag of the token
						String pos = token.get(PartOfSpeechAnnotation.class);
						posTags.add(pos);

						//Add word to wordbank
						mongo.updateDocumentArray("wordBank","tag", pos, "words", word);
					}
				}
				//Get POS tags and plain text for this verse
				versePosTags.add(posTags);
				verseText.add(line.trim());
				verseLines++;
			} 
			else {
				//End of verse, add to db
				PoemVerse verse = new PoemVerse(docId, verseText, versePosTags, verseLines);
				Document verseDocument = verse.buildDocument();
				mongo.insertDocument("verses", verseDocument);
				System.out.println("Added verse to db");

				//Empty all data structures for next verse
				versePosTags.removeAll(versePosTags);
				verseText.removeAll(verseText);
				verseLines = 0;
				docId++;
			}

			//Attempt to read next line
			try {
				line = reader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Error reading input: " + e);
			}
		}
	}
}
