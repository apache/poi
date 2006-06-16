package org.apache.poi.hwpf.extractor;

import java.io.FileInputStream;
import java.util.Iterator;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.model.TextPiece;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;

import junit.framework.TestCase;

/**
 * Test the different routes to extracting text
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public class TestWordExtractor extends TestCase {
	private String[] p_text1 = new String[] {
			"This is a simple word document\r\n",
			"\r\n",
			"It has a number of paragraphs in it\r\n",
			"\r\n",
			"Some of them even feature bold, italic and underlined text\r\n",
			"\r\n",
			"\r\n",
			"This bit is in a different font and size\r\n",
			"\r\n",
			"\r\n",
			"This bit features some red text.\r\n",
			"\r\n",
			"\r\n",
			"It is otherwise very very boring.\r\n"
	};
	private String p_text1_block = new String();
		
	// Well behaved document
	private WordExtractor extractor;
	// Corrupted document - can't do paragraph based stuff
	private WordExtractor extractor2;
	
    protected void setUp() throws Exception {
		String dirname = System.getProperty("HWPF.testdata.path");
		
		String filename = dirname + "/test2.doc";
		String filename2 = dirname + "/test.doc";
		extractor = new WordExtractor(new FileInputStream(filename));
		extractor2 = new WordExtractor(new FileInputStream(filename2));
		
		// Build splat'd out text version
		for(int i=0; i<p_text1.length; i++) {
			p_text1_block += p_text1[i];
		}
    }			
    
    /**
     * Test paragraph based extraction
     */
    public void testExtractFromParagraphs() {
    	String[] text = extractor.getParagraphText();
    	
    	assertEquals(p_text1.length, text.length);
    	for(int i=0; i<p_text1.length; i++) {
    		assertEquals(p_text1[i], text[i]);
    	}
    	
    	// On second one, should fall back
    	assertEquals(1, extractor2.getParagraphText().length);
    }
    
    /**
     * Test the paragraph -> flat extraction
     */
    public void testGetText() {
    	assertEquals(p_text1_block, extractor.getText());
    	
    	// On second one, should fall back to text piece
    	assertEquals(extractor2.getTextFromPieces(), extractor2.getText());
    }
    
    /**
     * Test textPieces based extraction
     */
    public void testExtractFromTextPieces() throws Exception {
    	String text = extractor.getTextFromPieces();
    	assertEquals(p_text1_block, text);
    }
}
