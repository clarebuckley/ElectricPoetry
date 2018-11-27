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
		this.templateMutator = new TemplateMutator(1);
		this.template = templateMutator.getPoemTemplate();
		this.templateFiller = new TemplateFiller();
		this.poem = templateFiller.processTemplate(template);
		//System.out.println(poem);
		////printPoem(poem);
		
	}
	
	public void printPoem(ArrayList<ArrayList<String[]>> poem) {
		for(int i = 0; i < poem.size(); i++) {
			System.out.println(poem.get(i));
		}
	}
	
	

}
