/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.hslf.usermodel;

import static org.junit.Assert.*;

import java.awt.Color;
import java.io.*;
import java.util.List;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hslf.model.Table;
import org.apache.poi.hslf.model.textproperties.TextPropCollection;
import org.apache.poi.hslf.record.*;
import org.apache.poi.hslf.usermodel.*;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for TextRuns
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public final class TestTextRun {
    private static POIDataSamples _slTests = POIDataSamples.getSlideShowInstance();

	// SlideShow primed on the test data
	private HSLFSlideShow ss;
	private HSLFSlideShow ssRich;

	@Before
	public void setUp() throws IOException {

		// Basic (non rich) test file
		ss = new HSLFSlideShow(_slTests.openResourceAsStream("basic_test_ppt_file.ppt"));

		// Rich test file
		ssRich = new HSLFSlideShow(_slTests.openResourceAsStream("Single_Coloured_Page.ppt"));
	}

	/**
	 * Test to ensure that getting the text works correctly
	 */
	@Test
	public void testGetText() {
		HSLFSlide slideOne = ss.getSlides().get(0);
		List<HSLFTextParagraph> textParas = slideOne.getTextParagraphs();

		assertEquals(2, textParas.size());

		// Get text works with \n
		assertEquals("This is a test title", textParas.get(0).getTextRuns().get(0).getRawText());
		assertEquals("This is a test subtitle\nThis is on page 1", textParas.get(1).getTextRuns().get(0).getRawText());

		// Raw text has \r instead
		assertEquals("This is a test title", textParas.get(0).getTextRuns().get(0).getRawText());
		assertEquals("This is a test subtitle\rThis is on page 1", textParas.get(1).getTextRuns().get(0).getRawText());


		// Now check on a rich text run
		HSLFSlide slideOneR = ssRich.getSlides().get(0);
		List<HSLFTextParagraph> textRunsR = slideOneR.getTextParagraphs();

		assertEquals(2, textRunsR.size());
		assertEquals("This is a title, it\u2019s in black", textRunsR.get(0).getTextRuns().get(0).getRawText());
		assertEquals("This is the subtitle, in bold\nThis bit is blue and italic\nThis bit is red (normal)", textRunsR.get(1).getTextRuns().get(0).getRawText());
		assertEquals("This is a title, it\u2019s in black", textRunsR.get(0).getTextRuns().get(0).getRawText());
		assertEquals("This is the subtitle, in bold\rThis bit is blue and italic\rThis bit is red (normal)", textRunsR.get(1).getTextRuns().get(0).getRawText());
	}

	/**
	 * Test to ensure changing non rich text bytes->bytes works correctly
	 */
	@Test
	public void testSetText() {
		HSLFSlide slideOne = ss.getSlides().get(0);
		List<HSLFTextParagraph> textRuns = slideOne.getTextParagraphs();
		HSLFTextParagraph run = textRuns.get(0);
		HSLFTextRun tr =  run.getTextRuns().get(0);

		// Check current text
		assertEquals("This is a test title", tr.getRawText());

		// Change
		String changeTo = "New test title";
		tr.setText(changeTo);
		assertEquals(changeTo, tr.getRawText());

		// Ensure trailing \n's are NOT stripped, it is legal to set a text with a trailing '\r'
		tr.setText(changeTo + "\n");
		assertEquals(changeTo + "\n", tr.getRawText());
	}

	/**
	 * Test to ensure that changing non rich text between bytes and
	 *  chars works correctly
	 */
	@Test
	public void testAdvancedSetText() {
		HSLFSlide slideOne = ss.getSlides().get(0);
		List<HSLFTextParagraph> paras = slideOne.getTextParagraphs();
		HSLFTextParagraph para = paras.get(0);
		
        TextHeaderAtom tha = null;
        TextBytesAtom tba = null;
        TextCharsAtom tca = null;
		for (Record r : para.getRecords()) {
		    if (r instanceof TextHeaderAtom) tha = (TextHeaderAtom)r;
		    else if (r instanceof TextBytesAtom) tba = (TextBytesAtom)r;
		    else if (r instanceof TextCharsAtom) tca = (TextCharsAtom)r;
		}
		

		// Bytes -> Bytes
		assertNull(tca);
		assertNotNull(tba);
		// assertFalse(run._isUnicode);
		assertEquals("This is a test title", para.getTextRuns().get(0).getRawText());

		String changeBytesOnly = "New Test Title";
		HSLFTextParagraph.setText(paras, changeBytesOnly);
		para = paras.get(0);
		tha = null; tba = null; tca = null;
        for (Record r : para.getRecords()) {
            if (r instanceof TextHeaderAtom) tha = (TextHeaderAtom)r;
            else if (r instanceof TextBytesAtom) tba = (TextBytesAtom)r;
            else if (r instanceof TextCharsAtom) tca = (TextCharsAtom)r;
        }
		
		assertEquals(changeBytesOnly, HSLFTextParagraph.getRawText(paras));
		assertNull(tca);
		assertNotNull(tba);

		// Bytes -> Chars
        assertNull(tca);
        assertNotNull(tba);
		assertEquals(changeBytesOnly, HSLFTextParagraph.getRawText(paras));

		String changeByteChar = "This is a test title with a '\u0121' g with a dot";
		HSLFTextParagraph.setText(paras, changeByteChar);
		para = paras.get(0);
        tha = null; tba = null; tca = null;
        for (Record r : para.getRecords()) {
            if (r instanceof TextHeaderAtom) tha = (TextHeaderAtom)r;
            else if (r instanceof TextBytesAtom) tba = (TextBytesAtom)r;
            else if (r instanceof TextCharsAtom) tca = (TextCharsAtom)r;
        }		

		assertEquals(changeByteChar, HSLFTextParagraph.getRawText(paras));
		assertNotNull(tca);
		assertNull(tba);

		// Chars -> Chars
		assertNull(tba);
		assertNotNull(tca);
		assertEquals(changeByteChar, HSLFTextParagraph.getRawText(paras));

		String changeCharChar = "This is a test title with a '\u0147' N with a hat";
		HSLFTextParagraph.setText(paras, changeCharChar);
        para = paras.get(0);
        tha = null; tba = null; tca = null;
        for (Record r : para.getRecords()) {
            if (r instanceof TextHeaderAtom) tha = (TextHeaderAtom)r;
            else if (r instanceof TextBytesAtom) tba = (TextBytesAtom)r;
            else if (r instanceof TextCharsAtom) tca = (TextCharsAtom)r;
        }       

		assertEquals(changeCharChar, HSLFTextParagraph.getRawText(paras));
		assertNotNull(tca);
		assertNull(tba);
	}

	/**
	 * Tests to ensure that non rich text has the right default rich text run
	 *  set up for it
	 */
	@Test
	public void testGetRichTextNonRich() {
		HSLFSlide slideOne = ss.getSlides().get(0);
		List<HSLFTextParagraph> textRuns = slideOne.getTextParagraphs();

		assertEquals(2, textRuns.size());

		HSLFTextParagraph trA = textRuns.get(0);
		HSLFTextParagraph trB = textRuns.get(1);

		assertEquals(1, trA.getTextRuns().size());
		assertEquals(1, trB.getTextRuns().size());

		HSLFTextRun rtrA = trA.getTextRuns().get(0);
		HSLFTextRun rtrB = trB.getTextRuns().get(0);

		assertEquals(HSLFTextParagraph.getRawText(textRuns.subList(0, 0)), rtrA.getRawText());
		assertEquals(HSLFTextParagraph.getRawText(textRuns.subList(1, 1)), rtrB.getRawText());

//		assertNull(rtrA._getRawCharacterStyle());
//		assertNull(rtrA._getRawParagraphStyle());
//		assertNull(rtrB._getRawCharacterStyle());
//		assertNull(rtrB._getRawParagraphStyle());
	}

	/**
	 * Tests to ensure that the rich text runs are built up correctly
	 */
	@Test
	public void testGetRichText() {
		HSLFSlide slideOne = ssRich.getSlides().get(0);
		List<HSLFTextParagraph> textRuns = slideOne.getTextParagraphs();

		assertEquals(2, textRuns.size());

		HSLFTextParagraph trA = textRuns.get(0);
		HSLFTextParagraph trB = textRuns.get(1);

		assertEquals(1, trA.getTextRuns().size());
		assertEquals(3, trB.getTextRuns().size());

		HSLFTextRun rtrA = trA.getTextRuns().get(0);
		HSLFTextRun rtrB = trB.getTextRuns().get(0);
		HSLFTextRun rtrC = trB.getTextRuns().get(1);
		HSLFTextRun rtrD = trB.getTextRuns().get(2);

		assertEquals(HSLFTextParagraph.getRawText(textRuns.subList(0, 0)), rtrA.getRawText());

		String trBstr = HSLFTextParagraph.getRawText(textRuns.subList(1, 1));
		assertEquals(trBstr.substring(0, 30), rtrB.getRawText());
		assertEquals(trBstr.substring(30,58), rtrC.getRawText());
		assertEquals(trBstr.substring(58,82), rtrD.getRawText());

//		assertNull(rtrA._getRawCharacterStyle());
//		assertNull(rtrA._getRawParagraphStyle());
//		assertNotNull(rtrB._getRawCharacterStyle());
//		assertNotNull(rtrB._getRawParagraphStyle());
//		assertNotNull(rtrC._getRawCharacterStyle());
//		assertNotNull(rtrC._getRawParagraphStyle());
//		assertNotNull(rtrD._getRawCharacterStyle());
//		assertNotNull(rtrD._getRawParagraphStyle());

		// Same paragraph styles
//		assertEquals(rtrB._getRawParagraphStyle(), rtrC._getRawParagraphStyle());
//		assertEquals(rtrB._getRawParagraphStyle(), rtrD._getRawParagraphStyle());

		// Different char styles
//		assertFalse( rtrB._getRawCharacterStyle().equals( rtrC._getRawCharacterStyle() ));
//		assertFalse( rtrB._getRawCharacterStyle().equals( rtrD._getRawCharacterStyle() ));
//		assertFalse( rtrC._getRawCharacterStyle().equals( rtrD._getRawCharacterStyle() ));
	}

	/**
	 * Tests to ensure that setting the text where the text isn't rich,
	 *  ensuring that everything stays with the same default styling
	 */
	@Test
	public void testSetTextWhereNotRich() {
		HSLFSlide slideOne = ss.getSlides().get(0);
		List<HSLFTextParagraph> textRuns = slideOne.getTextParagraphs();
		HSLFTextParagraph trB = textRuns.get(1);
//		assertEquals(1, trB.getTextRuns().length);

		HSLFTextRun rtrB = trB.getTextRuns().get(0);
//		assertEquals(trB.getRawText(), rtrB.getRawText());
//		assertNull(rtrB._getRawCharacterStyle());
//		assertNull(rtrB._getRawParagraphStyle());

		// Change text via normal
//		trB.setText("Test Foo Test");
		rtrB = trB.getTextRuns().get(0);
//		assertEquals("Test Foo Test", trB.getRawText());
//		assertEquals("Test Foo Test", rtrB.getRawText());
//		assertNull(rtrB._getRawCharacterStyle());
//		assertNull(rtrB._getRawParagraphStyle());
	}

	/**
	 * Tests to ensure that setting the text where the text is rich
	 *  sets everything to the same styling
	 */
	@Test
	public void testSetTextWhereRich() {
		HSLFSlide slideOne = ssRich.getSlides().get(0);
		List<HSLFTextParagraph> textRuns = slideOne.getTextParagraphs();
		HSLFTextParagraph trB = textRuns.get(1);
		assertEquals(3, trB.getTextRuns().size());

		HSLFTextRun rtrB = trB.getTextRuns().get(0);
		HSLFTextRun rtrC = trB.getTextRuns().get(1);
		HSLFTextRun rtrD = trB.getTextRuns().get(2);
//		TextPropCollection tpBP = rtrB._getRawParagraphStyle();
//		TextPropCollection tpBC = rtrB._getRawCharacterStyle();
//		TextPropCollection tpCP = rtrC._getRawParagraphStyle();
//		TextPropCollection tpCC = rtrC._getRawCharacterStyle();
//		TextPropCollection tpDP = rtrD._getRawParagraphStyle();
//		TextPropCollection tpDC = rtrD._getRawCharacterStyle();

//		assertEquals(trB.getRawText().substring(0, 30), rtrB.getRawText());
//		assertNotNull(tpBP);
//		assertNotNull(tpBC);
//		assertNotNull(tpCP);
//		assertNotNull(tpCC);
//		assertNotNull(tpDP);
//		assertNotNull(tpDC);
//		assertTrue(tpBP.equals(tpCP));
//		assertTrue(tpBP.equals(tpDP));
//		assertTrue(tpCP.equals(tpDP));
//		assertFalse(tpBC.equals(tpCC));
//		assertFalse(tpBC.equals(tpDC));
//		assertFalse(tpCC.equals(tpDC));

		// Change text via normal
//		trB.setText("Test Foo Test");

		// Ensure now have first style
//		assertEquals(1, trB.getTextRuns().length);
//		rtrB = trB.getTextRuns().get(0);
//		assertEquals("Test Foo Test", trB.getRawText());
//		assertEquals("Test Foo Test", rtrB.getRawText());
//		assertNotNull(rtrB._getRawCharacterStyle());
//		assertNotNull(rtrB._getRawParagraphStyle());
//		assertEquals( tpBP, rtrB._getRawParagraphStyle() );
//		assertEquals( tpBC, rtrB._getRawCharacterStyle() );
	}

	/**
	 * Test to ensure the right stuff happens if we change the text
	 *  in a rich text run, that doesn't happen to actually be rich
	 */
	@Test
	public void testChangeTextInRichTextRunNonRich() {
		HSLFSlide slideOne = ss.getSlides().get(0);
		List<HSLFTextParagraph> textRuns = slideOne.getTextParagraphs();
		HSLFTextParagraph trB = textRuns.get(1);
//		assertEquals(1, trB.getTextRuns().length);
//
//		HSLFTextRun rtrB = trB.getTextRuns().get(0);
//		assertEquals(trB.getRawText(), rtrB.getRawText());
//		assertNull(rtrB._getRawCharacterStyle());
//		assertNull(rtrB._getRawParagraphStyle());

		// Change text via rich
//		rtrB.setText("Test Test Test");
//		assertEquals("Test Test Test", trB.getRawText());
//		assertEquals("Test Test Test", rtrB.getRawText());

		// Will now have dummy props
//		assertNotNull(rtrB._getRawCharacterStyle());
//		assertNotNull(rtrB._getRawParagraphStyle());
	}

	/**
	 * Tests to ensure changing the text within rich text runs works
	 *  correctly
	 */
	@Test
	public void testChangeTextInRichTextRun() {
		HSLFSlide slideOne = ssRich.getSlides().get(0);
		List<HSLFTextParagraph> textRuns = slideOne.getTextParagraphs();
		HSLFTextParagraph trB = textRuns.get(1);
		assertEquals(3, trB.getTextRuns().size());

		// We start with 3 text runs, each with their own set of styles,
		//  but all sharing the same paragraph styles
		HSLFTextRun rtrB = trB.getTextRuns().get(0);
		HSLFTextRun rtrC = trB.getTextRuns().get(1);
		HSLFTextRun rtrD = trB.getTextRuns().get(2);
//		TextPropCollection tpBP = rtrB._getRawParagraphStyle();
//		TextPropCollection tpBC = rtrB._getRawCharacterStyle();
//		TextPropCollection tpCP = rtrC._getRawParagraphStyle();
//		TextPropCollection tpCC = rtrC._getRawCharacterStyle();
//		TextPropCollection tpDP = rtrD._getRawParagraphStyle();
//		TextPropCollection tpDC = rtrD._getRawCharacterStyle();

		// Check text and stylings
//		assertEquals(trB.getRawText().substring(0, 30), rtrB.getRawText());
//		assertNotNull(tpBP);
//		assertNotNull(tpBC);
//		assertNotNull(tpCP);
//		assertNotNull(tpCC);
//		assertNotNull(tpDP);
//		assertNotNull(tpDC);
//		assertTrue(tpBP.equals(tpCP));
//		assertTrue(tpBP.equals(tpDP));
//		assertTrue(tpCP.equals(tpDP));
//		assertFalse(tpBC.equals(tpCC));
//		assertFalse(tpBC.equals(tpDC));
//		assertFalse(tpCC.equals(tpDC));

		// Check text in the rich runs
		assertEquals("This is the subtitle, in bold\n", rtrB.getRawText());
		assertEquals("This bit is blue and italic\n", rtrC.getRawText());
		assertEquals("This bit is red (normal)", rtrD.getRawText());

		String newBText = "New Subtitle, will still be bold\n";
		String newCText = "New blue and italic text\n";
		String newDText = "Funky new normal red text";
		rtrB.setText(newBText);
		rtrC.setText(newCText);
		rtrD.setText(newDText);
		assertEquals(newBText, rtrB.getRawText());
		assertEquals(newCText, rtrC.getRawText());
		assertEquals(newDText, rtrD.getRawText());

//		assertEquals(newBText + newCText + newDText, trB.getRawText());

		// The styles should have been updated for the new sizes
//		assertEquals(newBText.length(), tpBC.getCharactersCovered());
//		assertEquals(newCText.length(), tpCC.getCharactersCovered());
//		assertEquals(newDText.length()+1, tpDC.getCharactersCovered()); // Last one is always one larger

//		assertEquals(
//				newBText.length() + newCText.length() + newDText.length(),
//				tpBP.getCharactersCovered()
//		);

		// Paragraph style should be sum of text length
//		assertEquals(newBText.length() + newCText.length() + newDText.length(), tpBP.getCharactersCovered());

		// Check stylings still as expected
//		TextPropCollection ntpBC = rtrB._getRawCharacterStyle();
//		TextPropCollection ntpCC = rtrC._getRawCharacterStyle();
//		TextPropCollection ntpDC = rtrD._getRawCharacterStyle();
//		assertEquals(tpBC.getTextPropList(), ntpBC.getTextPropList());
//		assertEquals(tpCC.getTextPropList(), ntpCC.getTextPropList());
//		assertEquals(tpDC.getTextPropList(), ntpDC.getTextPropList());
	}


	/**
	 * Test case for Bug 41015.
	 *
	 * In some cases RichTextRun.getText() threw StringIndexOutOfBoundsException because
	 * of the wrong list of potential paragraph properties defined in StyleTextPropAtom.
	 *
	 */
	@Test
	public void testBug41015() throws IOException {
		List<HSLFTextRun> rt;

		HSLFSlideShow ppt = new HSLFSlideShow(_slTests.openResourceAsStream("bug-41015.ppt"));
		HSLFSlide sl = ppt.getSlides().get(0);
		List<HSLFTextParagraph> txt = sl.getTextParagraphs();
		assertEquals(2, txt.size());

		rt = txt.get(0).getTextRuns();
		assertEquals(1, rt.size());
		assertEquals(0, txt.get(0).getIndentLevel());
		assertEquals("sdfsdfsdf", rt.get(0).getRawText());

		rt = txt.get(1).getTextRuns();
		assertEquals(2, rt.size());
		assertEquals(0, txt.get(0).getIndentLevel());
		assertEquals("Sdfsdfsdf\n" +
				"Dfgdfg\n" +
				"Dfgdfgdfg\n", rt.get(0).getRawText());
		assertEquals(1, txt.get(1).getIndentLevel());
		assertEquals("Sdfsdfs\n" +
				"Sdfsdf\n", rt.get(1).getRawText());
	}

	/**
	 * Test creation of TextRun objects.
	 */
	@Test
	public void testAddTextRun() {
		HSLFSlideShow ppt = new HSLFSlideShow();
		HSLFSlide slide = ppt.createSlide();

		assertNull(slide.getTextParagraphs());

		HSLFTextBox shape1 = new HSLFTextBox();
//		HSLFTextParagraph run1 = shape1.getTextParagraphs();
//		assertSame(run1, shape1.createTextRun());
//		run1.setText("Text 1");
		slide.addShape(shape1);

		//The array of Slide's text runs must be updated when new text shapes are added.
//		HSLFTextParagraph[] runs = slide.getTextParagraphs();
//		assertNotNull(runs);
//		assertSame(run1, runs.get(0));
//
//		HSLFTextBox shape2 = new HSLFTextBox();
//		HSLFTextParagraph run2 = shape2.getTextParagraphs();
//		assertSame(run2, shape2.createTextRun());
//		run2.setText("Text 2");
//		slide.addShape(shape2);
//
//		runs = slide.getTextParagraphs();
//		assertEquals(2, runs.length);
//
//		assertSame(run1, runs.get(0));
//		assertSame(run2, runs.get(1));
//
//		//as getShapes()
//		HSLFShape[] sh = slide.getShapes();
//		assertEquals(2, sh.length);
//		assertTrue(sh.get(0) instanceof HSLFTextBox);
//		HSLFTextBox box1 = (HSLFTextBox)sh.get(0);
//		assertSame(run1, box1.getTextParagraphs());
//		HSLFTextBox box2 = (HSLFTextBox)sh.get(1);
//		assertSame(run2, box2.getTextParagraphs());
//
//		//test Table - a complex group of shapes containing text objects
//		HSLFSlide slide2 = ppt.createSlide();
//		assertNull(slide2.getTextParagraphs());
//		Table table = new Table(2, 2);
//		slide2.addShape(table);
//		runs = slide2.getTextParagraphs();
//		assertNotNull(runs);
//		assertEquals(4, runs.length);
	}

	@Test
	public void test48916() throws IOException {
//        HSLFSlideShow ppt = new HSLFSlideShow(_slTests.openResourceAsStream("SampleShow.ppt"));
//        for(HSLFSlide slide : ppt.getSlides()){
//            for(HSLFShape sh : slide.getShapes()){
//                if(sh instanceof HSLFTextShape){
//                    HSLFTextShape tx = (HSLFTextShape)sh;
//                    HSLFTextParagraph run = tx.getTextParagraphs();
//                    //verify that records cached in  TextRun and EscherTextboxWrapper are the same
//                    Record[] runChildren = run.getRecords();
//                    Record[] txboxChildren = tx.getEscherTextboxWrapper().getChildRecords();
//                    assertEquals(runChildren.length, txboxChildren.length);
//                    for(int i=0; i < txboxChildren.length; i++){
//                        assertSame(txboxChildren.get(i), runChildren.get(i));
//                    }
//                    //caused NPE prior to fix of Bugzilla #48916 
//                    run.getTextRuns().get(0).setBold(true);
//                    run.getTextRuns().get(0).setFontColor(Color.RED);
//                }
//            }
//        }
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        ppt.write(out);
//        ppt = new HSLFSlideShow(new ByteArrayInputStream(out.toByteArray()));
//        for(HSLFSlide slide : ppt.getSlides()){
//            for(HSLFShape sh : slide.getShapes()){
//                if(sh instanceof HSLFTextShape){
//                    HSLFTextShape tx = (HSLFTextShape)sh;
//                    HSLFTextParagraph run = tx.getTextParagraphs();
//                    HSLFTextRun rt = run.getTextRuns().get(0);
//                    assertTrue(rt.isBold());
//                    assertEquals(rt.getFontColor(), Color.RED);
//                }
//            }
//        }

    }

	@Test
	public void test52244() throws IOException {
        HSLFSlideShow ppt = new HSLFSlideShow(_slTests.openResourceAsStream("52244.ppt"));
        HSLFSlide slide = ppt.getSlides().get(0);
        List<HSLFTextParagraph> runs = slide.getTextParagraphs();

        assertEquals("Arial", runs.get(0).getTextRuns().get(0).getFontFamily());
        assertEquals(36, runs.get(0).getTextRuns().get(0).getFontSize(), 0);

        assertEquals("Arial", runs.get(1).getTextRuns().get(0).getFontFamily());
        assertEquals(24, runs.get(1).getTextRuns().get(0).getFontSize(), 0);

        assertEquals("Arial", runs.get(2).getTextRuns().get(0).getFontFamily());
        assertEquals(32, runs.get(2).getTextRuns().get(0).getFontSize(), 0);

    }

}
