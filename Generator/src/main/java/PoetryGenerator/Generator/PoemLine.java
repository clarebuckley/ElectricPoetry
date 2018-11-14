package PoetryGenerator.Generator;
//Diagram including database structure
//Explain project/db setup, how data is stored
//Explain choices between main decisions - top 3, choice of 1 for example
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

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
	
	public DBObject buildLineDocument() {
		DBObject document = new BasicDBObject("_id", id)
				.append("textLine", textLine)
				.append("POS", pos)
				.append("taggedLine", tagged);
		
		return document;
	}
	
//	public PoemLine documentToObject(FindIterable<Document> document) {
//		return new PoemLine()
//	}
}
