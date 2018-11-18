package PoetryGenerator.Generator;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;

public class PoemVerse {
	private int id;
	//Original text from poem
	private ArrayList<String> text;
	//Pattern of speech
	private ArrayList<List<String>> pos;
	//Number of lines in the verse
	private int numLines;

	public PoemVerse(int id, ArrayList<String> text, ArrayList<List<String>> pos, int numLines) {
		this.id = id;
		this.text = text;
		this.pos = pos;
		this.numLines = numLines;
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public ArrayList<String> getText() {
		return text;
	}
	public void setText(ArrayList<String> text) {
		this.text = text;
	}
	public ArrayList<List<String>> getPos() {
		return pos;
	}
	public void setPos(ArrayList<List<String>> pos) {
		this.pos = pos;
	}
	public int getNumLines() {
		return numLines;
	}
	public void setNumLines(int numLines) {
		this.numLines = numLines;
	}

	public Document buildDocument() {
		Document document = new Document("id", id)
				.append("text", text)
				.append("POS", pos)
				.append("numLines", numLines);

		return document;
	}

}
