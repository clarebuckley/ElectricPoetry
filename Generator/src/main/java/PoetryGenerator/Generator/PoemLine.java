package PoetryGenerator.Generator;

import org.bson.Document;

public class PoemLine {
	private int id;
	//Original text from poem
	private String textLine;
	//Pattern of speech
	private String pos;
	//textLine tagged with POS 
	private String tagged;
	
	public PoemLine(int id, String textLine, String pos, String tagged) {
		this.id = id;
		this.textLine = textLine;
		this.pos = pos;
		this.tagged = tagged;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTextLine() {
		return textLine;
	}
	public void setTextLine(String textLine) {
		this.textLine = textLine;
	}
	public String getPos() {
		return pos;
	}
	public void setPos(String pos) {
		this.pos = pos;
	}
	public String getTagged() {
		return tagged;
	}
	public void setTagged(String tagged) {
		this.tagged = tagged;
	}
	
	public Document buildLineDocument() {
		Document document = new Document("id", id)
			.append("textLine", textLine)
			.append("POS", pos)
			.append("taggedLine", tagged);
		
		return document;
	}
}
