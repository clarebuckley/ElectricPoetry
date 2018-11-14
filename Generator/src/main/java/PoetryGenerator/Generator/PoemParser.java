package PoetryGenerator.Generator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;


public class PoemParser {

	public static void main(String args[]) throws ClassNotFoundException, IOException {
		new PoemParser();
	}

	public PoemParser() throws ClassNotFoundException, IOException {
		MaxentTagger tagger = new MaxentTagger("taggers/bidirectional-distsim-wsj-0-18.tagger");

		InputStream file = this.getClass().getResourceAsStream("/PoetryGenerator/Data/testFile.txt");
		BufferedReader reader = new BufferedReader(new InputStreamReader(file));
		System.out.println(tagger.process(tagger.tokenizeText(reader)));

		
		//TODO The Stanford CoreNLP suite https://cloudacademy.com/blog/natural-language-processing-stanford-corenlp-2/
		
	}
}
