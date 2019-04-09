package PoetryGenerator.Generator;


import static org.junit.Assert.assertTrue;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

public class AppTest{
	private static PoemGeneratorEA ea;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ea = new PoemGeneratorEA();
	}
	@Test
	public void testLanguageTool() throws IOException {
		String poem = "this  is an test poems\r\n" + 
				"that that should find any errors;\r\n" + 
				"suuch as capitals for friday and september.\r\n" + 
				"and othrr grammer issues that are here\r\n";
		String poemAfter = ea.fixGrammar(poem).trim();
		String expectedAfter = "This is a test poem\r\n" + 
				"that should find any errors;\r\n" + 
				"such as capitals for Friday and September.\r\n" + 
				"And other grammar issues that are here.";
		assertTrue(poemAfter.equals(expectedAfter));
	}
	
	@Test
	public void testCostCalculator() {
		CostCalculator cost2 = new CostCalculator("2-gram");
		CostCalculator cost3 = new CostCalculator("3-gram");
		CostCalculator cost4 = new CostCalculator("4-gram");
		String sequence = "And the sunset kindles with me: Oh what beautiful company!";
		System.out.println(cost2.getSequenceProbability(sequence));
		System.out.println(cost3.getSequenceProbability(sequence));
		System.out.println(cost4.getSequenceProbability(sequence));
		
	}
	
}
