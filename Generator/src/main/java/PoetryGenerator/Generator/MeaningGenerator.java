package PoetryGenerator.Generator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;

public class NovelParser {
	private final MongoInterface mongo = new MongoInterface("poetryDB");
	private static File file = new File("./src/main/java/PoetryGenerator/Data/Do Androids Dream of Electric Sheep.txt"); 

	public static void main(String args[]) throws ClassNotFoundException, IOException {
		new NovelParser(file);

	}


	public NovelParser(File file) throws IOException {
		Scanner scanner = new Scanner(file);
		scanner.useDelimiter("[.!?]");

		//Loop through each line in the input text
		while (scanner.hasNext()) {
			System.out.println(scanner.next());
		}

		scanner.close();

	}

	//what do 
	public static ArrayList<String> ngrams(int n, String str) {
		ArrayList<String> ngrams = new ArrayList<String>();
		for (int i = 0; i < str.length() - n + 1; i++)
			ngrams.add(str.substring(i, i + n));
		return ngrams;
	}
}
