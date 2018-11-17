package PoetryGenerator.Generator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;


public class PoemParser {

	public static void main(String args[]) throws ClassNotFoundException, IOException {
		new PoemParser();
	}

	public PoemParser() throws ClassNotFoundException, IOException {
		MaxentTagger tagger = new MaxentTagger("taggers/english-bidirectional-distsim.tagger");

		InputStream file = this.getClass().getResourceAsStream("/PoetryGenerator/Data/testFile.txt");
		BufferedReader reader = new BufferedReader(new InputStreamReader(file));
		System.out.println(tagger.process(tagger.tokenizeText(reader)));
		
		Sentence sent = new Sentence("Lucy is in the sky with diamonds.");
		List<String> nerTags = sent.nerTags();  // [PERSON, O, O, O, O, O, O, O]
		String firstPOSTag = sent.posTag(0);   // NNP

		
		//TODO The Stanford CoreNLP suite https://cloudacademy.com/blog/natural-language-processing-stanford-corenlp-2/
		
	}
}
