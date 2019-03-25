package PoetryGenerator.Generator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;

import org.bson.Document;

/**
 * Used to parse an arpa langauge model file and add ngram contents to the database
 * @author Clare Buckley
 * @version 11/03/19
 *
 */

public class LanguageModelParser {
	private static File file = new File("./src/main/java/PoetryGenerator/Data/languageModel.arpa");
	private static final float inf = 999999999;
	private final MongoInterface mongo = new MongoInterface("poetryDB");

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
		HashMap<String, Document> entries = new HashMap<String, Document>();
		String line = br.readLine();
		String ngramType = "";
		while(line != null) {
			WordModel model = null;
			String[] lineParts = line.split("	");
			double probability = 0, backoff = 0;
			String word = "";

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
					model = new WordModel(word, ngramType, probability, backoff);
					Document doc = model.buildDocument();

					//Update document in hashmap
					entries.put(word, doc);
				}

				//For bigrams
				if(ngramType.equals("2-gram") && !lineParts[0].contains("2-grams")) {
					String n1;
					if(lineParts.length == 2) {
						//no backoff
						String[] words = lineParts[lineParts.length-1].split(" ");
						word = words[1];
						n1 = words[0];
						model = new WordModel(word, ngramType, probability, n1);

					} else {
						//has backoff
						backoff = new Double(lineParts[lineParts.length-1]);
						String[] words = lineParts[1].split(" ");
						word = words[1];
						n1 = words[0];
						model = new WordModel(word, ngramType, probability, backoff, n1);
					}
					Document doc = model.buildDocument();

					//Update document in hashmap
					if(entries.containsKey(word)) {
						Document entry = entries.get(word); 
						WordModel wm = new WordModel(entry);
						Document updated = wm.addTwoGram(n1, word, probability, backoff);
						entries.put(word,updated);
					} else {
						entries.put(word, doc);
					}
				}

				//For trigrams
				if(ngramType.equals("3-gram") && !lineParts[0].contains("3-grams")) {
					String n1, n2;
					if(lineParts.length == 2) {
						//no backoff
						String[] words = lineParts[lineParts.length-1].split(" ");
						word = words[2];
						n1 = words[1];
						n2 = words[0];
						model = new WordModel(word, ngramType, probability, n1, n2);
					} else {
						//has backoff
						backoff = new Double(lineParts[lineParts.length-1]);
						String[] words = lineParts[1].split(" ");
						word = words[2];
						n1 = words[1];
						n2 = words[0];
						model = new WordModel(word, ngramType, probability, backoff, n1, n2);
					}

					Document doc = model.buildDocument();

					//Update document in hashmap
					if(entries.containsKey(word)) {
						Document entry = entries.get(word); 
						WordModel wm = new WordModel(entry);
						Document updated = wm.addThreeGram(n2, n1, word, probability, backoff);
						entries.put(word,updated);
					} else {
						entries.put(word, doc);
					}
				}

				//For 4-grams
				if(ngramType.equals("4-gram") && !lineParts[0].contains("4-grams")) {
					String n1, n2, n3;
					//no backoff for 4-grams
					String[] words = lineParts[lineParts.length-1].split(" ");
					word = words[3];
					n1 = words[2];
					n2 = words[1];
					n3 = words[0];
					model = new WordModel(word, ngramType, probability, n1, n2, n3);

					Document doc = model.buildDocument();

					//Update document in hashmap
					if(entries.containsKey(word)) {
						Document entry = entries.get(word); 
						WordModel wm = new WordModel(entry);
						Document updated = wm.addFourGram(n3, n2, n1, word, probability, backoff);
						entries.put(word,updated);
					} else {
						entries.put(word, doc);
					}
				}

			}
			System.out.println("Added one entry to data structure");

			//Attempt to read next line
			try {
				line = br.readLine();
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Error reading input: " + e);
			}

		}

		br.close();
		addToDatabase(entries);
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

	private void addToDatabase(HashMap<String, Document> entries) {
		for (Document entry : entries.values()) {
		    mongo.insertDocument("languageModelTest", entry);
		    System.out.println("Inserted one entry to database");
		}
		System.out.println(entries.size() + " documents added to the db");
		
	}

}
