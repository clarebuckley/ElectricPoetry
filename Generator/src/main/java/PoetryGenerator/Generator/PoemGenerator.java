package PoetryGenerator.Generator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.bson.Document;

/**
 * Generate poem using TemplateMutator and TemplateFiller
 * Contains main method to print out poem
 * @author Clare Buckley
 * @version 15/02/2019
 *
 */

public class PoemGenerator {
	//Title of poem
	private String poemTitle = "Androids Dream of Electric Sheep";

	public static void main(String[] args) {
		new PoemGenerator();

	}

	public PoemGenerator() {
		//		System.out.println("------------------------------------");
		//		System.out.println("Writing your poem...");
		//		
		//		String poem = generatePoem(1);
		//		System.out.println(poem);
		//		printPoem(poem);
	}


	public String generatePoem(int poemVerses){
		ArrayList<ArrayList<String>> poem = new ArrayList<ArrayList<String>>();
		TemplateMutator templateMutator = new TemplateMutator(1);
		TemplateFiller templateFiller = new TemplateFiller();
		//	PoemGeneratorEA meaningGenerator = new PoemGeneratorEA(10, 0.70, 50);
		List<List<Document>> poemText = templateMutator.getPoemText();
		List<List<Document>> template = templateMutator.getPoemTemplate();
		poem = templateFiller.processTemplate(template, poemText);
		
		String poemContent = buildPoem(poem);
		NGramController ngram = new NGramController();
		poemContent = ngram.addNGrams(poemContent);
		return poemContent;
	}


	/**
	 * Print out contents of poem input
	 * @param poem - lines of poem are split into elements inside ArrayList
	 */
	private String buildPoem(ArrayList<ArrayList<String>> poem) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < poem.size(); i++) {
			ArrayList<String> lines = poem.get(i);
			for(String line : lines) {
				sb.append(line);
				sb.append(System.getProperty("line.separator"));
			}
			sb.append(System.getProperty("line.separator"));
		}
		return sb.toString();
	}



}
