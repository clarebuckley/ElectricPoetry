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
	private String poemTitle;

	public static void main(String[] args) {
		new PoemGenerator();
	}

	public PoemGenerator() {
		System.out.println("------------------------------------");
		System.out.println("Please enter the title of your poem: ");
		Scanner scanner = new Scanner(System.in);
		String poemTitle = scanner.nextLine();
		System.out.println("------------------------------------");
		System.out.println("Writing your poem...");
		scanner.close();
		
		ArrayList<ArrayList<String>> poem = generatePoem(poemTitle, 1);
		printPoem(poem);
	}


	private ArrayList<ArrayList<String>> generatePoem(String poemTitle, int poemVerses){
		ArrayList<ArrayList<String>> poem = new ArrayList<ArrayList<String>>();
		TemplateMutator templateMutator = new TemplateMutator(1);
		TemplateFiller templateFiller = new TemplateFiller();
		List<List<Document>> poemText = templateMutator.getPoemText();
		List<List<Document>> template = templateMutator.getPoemTemplate();
		poem = templateFiller.processTemplate(template, poemText);
		return poem;
	}



	/**
	 * Print out contents of poem input
	 * @param poem - lines of poem are split into elements inside ArrayList
	 */
	public void printPoem(ArrayList<ArrayList<String>> poem) {
		System.out.println("------------ " + poemTitle + " ------------");
		for(int i = 0; i < poem.size(); i++) {
			ArrayList<String> lines = poem.get(i);
			for(String line : lines) {
				System.out.println(line);
			}
		}
	}



}
