package PoetryGenerator.Generator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;

/**
 * Generate poem using TemplateMutator and TemplateFiller
 * Contains main method to print out poem
 * @author Clare Buckley
 * @version 29/01/2019
 *
 */

public class PoemGenerator {
	private TemplateFiller templateFiller;
	private TemplateMutator templateMutator;
	private ArrayList<ArrayList<String>> poem;
	//POS tag poem template
	private List<List<Document>> template;
	//Original poem content
	private List<List<Document>> poemText;
		

	public static void main(String[] args) {
		new PoemGenerator();
	}
	
	public PoemGenerator() {
		//Create template with 1 verse
		this.templateMutator = new TemplateMutator(3);
		this.template = templateMutator.getPoemTemplate();
		this.poemText = templateMutator.getPoemText();
		this.templateFiller = new TemplateFiller();
		this.poem = templateFiller.processTemplate(template, poemText);
		
		printPoem(poem);
		
	}
	
	/**
	 * Print out contents of poem input
	 * @param poem - lines of poem are split into elements inside ArrayList
	 */
	public void printPoem(ArrayList<ArrayList<String>> poem) {
		System.out.println("------------ Poem Generator ------------");
		for(int i = 0; i < poem.size(); i++) {
			ArrayList<String> lines = poem.get(i);
			for(String line : lines) {
				System.out.println(line);
			}
		}
	}
	
	

}
