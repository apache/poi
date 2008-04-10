/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.poi.hwpf.extractor;

import java.io.FileInputStream;
import java.util.Iterator;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.model.TextPiece;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

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
	// A word doc embeded in an excel file
	private String filename3;
	
    protected void setUp() throws Exception {
		String dirname = System.getProperty("HWPF.testdata.path");
		String pdirname = System.getProperty("POIFS.testdata.path");
		
		String filename = dirname + "/test2.doc";
		String filename2 = dirname + "/test.doc";
		filename3 = pdirname + "/excel_with_embeded.xls";
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
    
    
    /**
     * Test that we can get data from an
     *  embeded word document
     * @throws Exception
     */
    public void testExtractFromEmbeded() throws Exception {
    	POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(filename3));
    	DirectoryNode dir = (DirectoryNode)
    		fs.getRoot().getEntry("MBD03F25D8D");
    	// Should have WordDocument and 1Table
    	assertNotNull(dir.getEntry("1Table"));
    	assertNotNull(dir.getEntry("WordDocument"));
    	
    	HWPFDocument doc = new HWPFDocument(dir, fs);
    	WordExtractor extractor3 = new WordExtractor(doc);
		
    	assertNotNull(extractor3.getText());
    	assertTrue(extractor3.getText().length() > 20);
    }
}
