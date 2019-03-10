package PoetryGenerator.Generator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;

import org.bson.Document;

public class LanguageModelParser {
	private static File file = new File("./src/main/java/PoetryGenerator/Data/languageModel.arpa");
	private static final float inf = 999999999;

	public LanguageModelParser() throws IOException {
		FileInputStream inputStream = new FileInputStream(file);
		Reader reader = new InputStreamReader(inputStream,"Cp1252");
		BufferedReader br = new BufferedReader(reader);
		parseModel(br);
	}

	public static void main(String[] args) throws IOException {
		new LanguageModelParser();
	}

	private void parseModel(BufferedReader br) throws IOException {
		String line = br.readLine();
		String ngramType = "";

		while(line != null) {
			Document modelDocument;
			String[] lineParts = line.split("	");
			double probability = 0;
			double backoff = 0;
			String word;

			//Determine what type of n-gram is being built
			if(line.startsWith("\\")) {
				//ngram can be 1-gram, 2-gram, 3-gram or 4-gram, ignore all others
				ngramType = line.substring(1, line.length()-2);
			}
			if(lineParts.length > 1 && !line.contains("\\data\\")) {
				probability = getProbability(lineParts[0]);

				//For 1-grams
				if(ngramType.equals("1-gram") && !lineParts[0].contains("1-grams") && !line.contains("<unk>")) {
					word = lineParts[1];
					if(lineParts.length == 3) {
						backoff = new Double(lineParts[2]);
					}
					WordModel model = new WordModel(word, ngramType, probability, backoff);
					modelDocument = model.buildDocument();
				}

				//For bigrams
				if(ngramType.equals("2-gram") && !lineParts[0].contains("2-grams")) {
					String n1;
					if(lineParts.length == 2) {
						//no backoff
						String[] words = lineParts[lineParts.length-1].split(" ");
						word = words[1];
						n1 = words[0];
						WordModel model = new WordModel(word, ngramType, probability, n1);
						modelDocument = model.buildDocument();
						
					} else {
						//has backoff
						backoff = new Double(lineParts[lineParts.length-1]);
						String[] words = lineParts[1].split(" ");
						word = words[1];
						n1 = words[0];
						WordModel model = new WordModel(word, ngramType, probability, backoff, n1);
						modelDocument = model.buildDocument();
					}
				}
			}


			//Attempt to read next line
			try {
				line = br.readLine();
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Error reading input: " + e);
			}

		}
		br.close();
	}


	private double getProbability(String linePart) {
		double probability;
		if(linePart.equals("-inf") || linePart.length() == 0) {
			probability = inf;
		} else {
			probability = Math.pow(10, new Double(linePart));
		}
		return probability;
	}

}
