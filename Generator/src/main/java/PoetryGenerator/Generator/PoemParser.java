package PoetryGenerator.Generator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.util.CoreMap;


public class PoemParser {

	public static void main(String args[]) throws ClassNotFoundException, IOException {
		new PoemParser();
	}

	public PoemParser() throws ClassNotFoundException, IOException {
		MaxentTagger tagger = new MaxentTagger("taggers/english-bidirectional-distsim.tagger");

		InputStream file = this.getClass().getResourceAsStream("/PoetryGenerator/Data/testFile.txt");
		BufferedReader reader = new BufferedReader(new InputStreamReader(file));
		String line = reader.readLine();

		while (line != null) {
			// creates a StanfordCoreNLP object, with POS tagging, lemmatization,
			// NER, parsing, and coreference resolution
			Properties props = new Properties();
			props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
			StanfordCoreNLP pipeline = new StanfordCoreNLP(props);


			// create an empty Annotation just with the given text
			Annotation document = new Annotation(line);

			// run all Annotators on this text
			pipeline.annotate(document);

			// these are all the sentences in this document
			List<CoreMap> sentences = document.get(SentencesAnnotation.class);

			List<String> words = new ArrayList<String>();
			List<String> posTags = new ArrayList<String>();
			List<String> nerTags = new ArrayList<String>();
			for (CoreMap sentence : sentences) {
				// traversing the words in the current sentence
				for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
					// this is the text of the token
					String word = token.get(TextAnnotation.class);
					words.add(word);
					// this is the POS tag of the token
					String pos = token.get(PartOfSpeechAnnotation.class);
					posTags.add(pos);
					// this is the NER label of the token
					String ne = token.get(NamedEntityTagAnnotation.class);
					nerTags.add(ne);
				}
			}
			System.out.println(words.toString());
			System.out.println(posTags.toString());
			System.out.println(nerTags.toString());
			
			// read next line
			line = reader.readLine();
		}




	}


}
