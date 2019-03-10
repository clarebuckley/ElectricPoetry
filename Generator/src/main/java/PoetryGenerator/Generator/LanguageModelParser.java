package PoetryGenerator.Generator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class LanguageModelParser {
	private static File file = new File("./src/main/java/PoetryGenerator/Data/languageModel.arpa");
	private static final float inf = 999999;
	
	public static void main(String[] args) throws IOException {
		FileInputStream inputStream = new FileInputStream(file);
		Reader reader = new InputStreamReader(inputStream,"Cp1252");
		BufferedReader br = new BufferedReader(reader);
		parseModel(br);


	}
	
	private static void parseModel(BufferedReader br) throws IOException {
		String line = br.readLine();
		String ngram = "";

		while(line != null) {
			String[] lineParts = line.split("	");
			double probability ;
			String word = "";
			
			if(line.startsWith("\\")) {
				//ngram can be 1-gram, 2-gram, 3-gram or 4-gram, ignore all others
				ngram = line.substring(1, line.length()-2);
				System.out.println(ngram);
			}
			if(ngram.equals("1-gram") && !lineParts[0].contains("1-grams")) {
				if(lineParts[0].equals("-inf")) {
					probability = inf;
				} else {
					probability = Math.pow(10, new Double(lineParts[0]));
				}
				word = lineParts[1];
				System.out.println(word);
			}
			if(ngram == "2-gram") {
				
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

}
