package PoetryGenerator.Generator;

import java.util.ArrayList;
import java.util.Arrays;

public class PoemGenerator {
	private TemplateFiller templateFiller;
	private TemplateMutator templateMutator;
	private ArrayList<ArrayList<String[]>> template;
	private ArrayList<String> poem;
	

	public static void main(String[] args) {
		new PoemGenerator();
	}
	
	public PoemGenerator() {
		this.templateMutator = new TemplateMutator(2);
		this.template = templateMutator.getPoemTemplate();
		this.templateFiller = new TemplateFiller();
		this.poem = templateFiller.processTemplate(template);
		
		printPoem(poem);
		
	}
	
	public void printPoem(ArrayList<String> poem) {
		System.out.println("------------ Poem Generator ------------");
		for(int i = 0; i < poem.size(); i++) {
			System.out.println(poem.get(i));
		}
	}
	
	

}
