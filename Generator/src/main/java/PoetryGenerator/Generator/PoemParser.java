package PoetryGenerator.Generator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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
 * @version 18/11/2018
 *
 */
public class PoemParser {
	
	private int docId;
	private InputStream file;
	private final MongoInterface mongo = new MongoInterface("test");

	public static void main(String args[]) throws ClassNotFoundException, IOException {
		//Testing
		new PoemParser("/PoetryGenerator/Data/testFile.txt");
	}

	public PoemParser(String filePath) throws ClassNotFoundException, IOException {
		this.file = this.getClass().getResourceAsStream(filePath);
		this.docId = mongo.getLastEnteredId("testData") + 1;
		
		//Test
		parseLinesInFile(file);
	}

	private void parseLinesInFile(InputStream file) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(file));
		String line = reader.readLine();

		while (line != null) {
			System.out.println(docId);
			if(line.trim().isEmpty()) {
				System.out.println("-------------------------------------END OF VERSE");
			} else {

				// creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution
				Properties props = new Properties();
				props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
				StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

				// create an empty Annotation just with the given text
				Annotation document = new Annotation(line);

				// run all Annotators on this text
				pipeline.annotate(document);

				// these are all the sentences in this document
				List <CoreMap> sentences = document.get(SentencesAnnotation.class);
				List<String> words = new ArrayList<String>();
				List<String> posTags = new ArrayList<String>();

				for (CoreMap sentence : sentences) {
					// traversing the words in the current sentence
					for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
						//text of the token
						String word = token.get(TextAnnotation.class);
						words.add(word);

						//POS tag of the token
						String pos = token.get(PartOfSpeechAnnotation.class);
						posTags.add(pos);
					}
				}

				System.out.println("Words: " + words.toString());
				System.out.println("posTags: " + posTags.toString());

			}
			
			//Attempt to read next line
			try {
				docId++;
				line = reader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Error reading input: " + e);
			}
		}
	}
}
