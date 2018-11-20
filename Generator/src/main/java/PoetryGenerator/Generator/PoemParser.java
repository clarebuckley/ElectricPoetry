package PoetryGenerator.Generator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.bson.Document;

import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.RelationExtractorAnnotator;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.StringUtils;

/**
 * Parses input texts and adds POS, dependencies, and text content to the database
 * @author Clare Buckley
 * @version 18/11/2018
 *
 */
public class PoemParser {

	private int docId;
	private InputStream file;
	private final MongoInterface mongo = new MongoInterface("poetryDB");

	public static void main(String args[]) throws ClassNotFoundException, IOException {
		//Testing
		new PoemParser("/PoetryGenerator/Data/testFile.txt");
	}

	public PoemParser(String filePath) throws ClassNotFoundException, IOException {
		this.file = this.getClass().getResourceAsStream(filePath);
		if(mongo.getLastEnteredId("verses") == -1) {
			this.docId = 1;
		} else {
			this.docId = mongo.getLastEnteredId("verses") + 1;
		}

		System.out.println("start docId: " + docId);

		//Test
		parseLinesInFile(file);
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
				// creates a StanfordCoreNLP object with POS tagging
				Properties props = new Properties();
				props.setProperty("annotators", "tokenize, ssplit, pos");
				StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

				// create an empty Annotation just with the given text
				Annotation document = new Annotation(line);

				// run all Annotators on this text
				pipeline.annotate(document);

				// these are all the sentences in this document
				List <CoreMap> sentences = document.get(SentencesAnnotation.class);
				List<String> posTags = new ArrayList<String>();

				for (CoreMap sentence : sentences) {
					// traversing the words in the current sentence
					List<CoreLabel> tokens = sentence.get(TokensAnnotation.class);

					Collection<String> nGrams = StringUtils.getNgramsFromTokens(tokens,3,5);
					//TODO FUTURE ITERATIONS: for each in the array, when you add that word to the array also add the n-gram [will be multiple
					//n-grams for each word (max/min vals)
					//					System.out.println(tokens);
					//					System.out.println(nGrams.toString());
					for (CoreLabel token : tokens) {
						//text of the token
						String word = token.get(TextAnnotation.class);
						word = word.toLowerCase();
						
						
						//POS tag of the token
						String pos = token.get(PartOfSpeechAnnotation.class);
						System.out.println(word + " --> " + pos);
						posTags.add(pos);
						
						//Add word to wordbank
						mongo.updateDocumentArray("wordBank","tag", pos, "words", word);
					}
				}

				versePosTags.add(posTags);
				verseText.add(line.trim());
				verseLines++;
			} 
			else {
				PoemVerse verse = new PoemVerse(docId, verseText, versePosTags, verseLines);
				Document verseDocument = verse.buildDocument();
				//				mongo.insertDocument("verses", verseDocument);

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
