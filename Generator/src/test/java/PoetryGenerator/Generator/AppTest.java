package PoetryGenerator.Generator;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.math.BigDecimal;

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
				"And other grammer issues that are here";
		assertTrue(poemAfter.trim().equals(expectedAfter.trim()));
	}

	@Test
	public void testCostCalculator() {
		CostCalculator cost2 = new CostCalculator("2-gram");
		String sequence = "And the sunset kindles with me: Oh what beautiful company!";
		assertEquals(cost2.getSequenceProbability(sequence), 
				new BigDecimal(1.000000000000000083336420607585985350931336026868654502364509783548862515410206308619223136702203191816806793212890625E-30));	
	}

	@Test
	public void testCrossover() {
		String poem1 = "Glory and loveliness have passed away;\r\n" + 
				"  For if we wander out in early morn,\r\n" + 
				"  No wreathed incense do we see upborne\r\n" + 
				"Into the east, to meet the smiling day:\r\n" + 
				"No crowd of nymphs soft voic'd and young, and gay,\r\n" + 
				"  In woven baskets bringing ears of corn,\r\n" + 
				"  Roses, and pinks, and violets, to adorn\r\n" + 
				"The shrine of Flora in her early May.\r\n" + 
				"But there are left delights as high as these,\r\n" + 
				"  And I shall ever bless my destiny,\r\n" + 
				"That in a time, when under pleasant trees\r\n" + 
				"  Pan is no longer sought, I feel a free\r\n" + 
				"A leafy luxury, seeing I could please\r\n" + 
				"  With these poor offerings, a man like thee.";

		String poem2 = "When by my solitary hearth I sit,\r\n" + 
				"  And hateful thoughts enwrap my soul in gloom;\r\n" + 
				"When no fair dreams before my \"mind's eye\" flit,\r\n" + 
				"  And the bare heath of life presents no bloom;\r\n" + 
				"    Sweet Hope, ethereal balm upon me shed,\r\n" + 
				"    And wave thy silver pinions o'er my head.";

		PoemGeneratorEA ea = new PoemGeneratorEA();
		String crossover = ea.generateCrossover(poem1, poem2);
		
		String expectedCrossover = "Glory and loveliness have passed away;\r\n" + 
				"  For if we wander out in early morn,\r\n" + 
				"  No wreathed incense do we see upborne\r\n" + 
				"Into the east, to meet the smiling day:\r\n" + 
				"No crowd of nymphs soft voic'd and young, and gay,\r\n" + 
				"When by my solitary hearth I sit,\r\n" + 
				"  And hateful thoughts enwrap my soul in gloom;\r\n" + 
				"When no fair dreams before my \"mind's eye\" flit,\r\n" + 
				"  And the bare heath of life presents no bloom;\r\n" + 
				"    Sweet Hope, ethereal balm upon me shed,\r\n";
		String[] expectedLines = expectedCrossover.split("\\r?\\n");
		String[] actualLines = crossover.split("\\r?\\n");
		assertEquals(expectedLines.length, actualLines.length);
		for(int i = 0; i < expectedLines.length; i++) {
			assertTrue(expectedLines[i].trim().equals(actualLines[i].trim()));
		}
	}

}
