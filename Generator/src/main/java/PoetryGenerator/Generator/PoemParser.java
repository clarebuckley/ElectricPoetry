package PoetryGenerator.Generator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import edu.stanford.nlp.coref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.BasicDependenciesAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedDependenciesAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.EnhancedDependenciesAnnotation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;


public class PoemParser {

	public static void main(String args[]) throws ClassNotFoundException, IOException {
		new PoemParser();
	}

	public PoemParser() throws ClassNotFoundException, IOException {
		InputStream file = this.getClass().getResourceAsStream("/PoetryGenerator/Data/testFile.txt");
		BufferedReader reader = new BufferedReader(new InputStreamReader(file));
		String line = reader.readLine();

		while (line != null) {
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

				//Syntactic parse tree of sentence 
				Tree tree = sentence.get(TreeAnnotation.class); 
				System.out.println("Tree:\n"+ tree); 

				//Dependency graph of the sentence 
				SemanticGraph dependencies = sentence.get(EnhancedDependenciesAnnotation.class); 
				System.out.println("Dependencies\n:"+ dependencies);
			}

			System.out.println("Words: " + words.toString());
			System.out.println("posTags: " + posTags.toString());
		
			//Map of the chain 
			Map<Integer, CorefChain> graph = document.get(CorefChainAnnotation.class); 
			System.out.println("Map of the chain:\n" + graph);


			// read next line
			line = reader.readLine();
		}

	}


}
