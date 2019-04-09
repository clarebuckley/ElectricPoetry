package PoetryGenerator.Generator;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

/**
 * Generate poem using TemplateMutator and TemplateFiller
 * Contains main method to print out poem
 * @author Clare Buckley
 * @version 05/04/19
 *
 */

public class PoemGenerator {
	private String generationGram;

	public PoemGenerator(String generationGram) {
		this.generationGram = generationGram;
	}


	/**
	 * Generate poem made of a set number of verses
	 * @param poemVerses
	 * @return poem
	 */
	public String generatePoem(int poemVerses){
		ArrayList<ArrayList<String>> poem = new ArrayList<ArrayList<String>>();
		TemplateController templateMutator = new TemplateController(1);
		TemplateFiller templateFiller = new TemplateFiller(generationGram);
		List<List<Document>> poemText = templateMutator.getPoemText();
		List<List<Document>> template = templateMutator.getPoemTemplate();
		poem = templateFiller.processTemplate(template, poemText);
		
		String poemContent = buildPoem(poem);
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
