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

package org.apache.poi.hslf.model;


import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.poi.hslf.model.textproperties.TextPropCollection;
import org.apache.poi.hslf.record.Record;
import org.apache.poi.hslf.record.TextBytesAtom;
import org.apache.poi.hslf.record.TextCharsAtom;
import org.apache.poi.hslf.record.TextHeaderAtom;
import org.apache.poi.hslf.usermodel.RichTextRun;
import org.apache.poi.hslf.usermodel.SlideShow;
import org.apache.poi.POIDataSamples;

/**
 * Tests for TextRuns
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public final class TestTextRun extends TestCase {
    private static POIDataSamples _slTests = POIDataSamples.getSlideShowInstance();

	// SlideShow primed on the test data
	private SlideShow ss;
	private SlideShow ssRich;

	protected void setUp() throws IOException {

		// Basic (non rich) test file
		ss = new SlideShow(_slTests.openResourceAsStream("basic_test_ppt_file.ppt"));

		// Rich test file
		ssRich = new SlideShow(_slTests.openResourceAsStream("Single_Coloured_Page.ppt"));
	}

	/**
	 * Test to ensure that getting the text works correctly
	 */
	public void testGetText() {
		Slide slideOne = ss.getSlides()[0];
		TextRun[] textRuns = slideOne.getTextRuns();

		assertEquals(2, textRuns.length);

		// Get text works with \n
		assertEquals("This is a test title", textRuns[0].getText());
		assertEquals("This is a test subtitle\nThis is on page 1", textRuns[1].getText());

		// Raw text has \r instead
		assertEquals("This is a test title", textRuns[0].getRawText());
		assertEquals("This is a test subtitle\rThis is on page 1", textRuns[1].getRawText());


		// Now check on a rich text run
		Slide slideOneR = ssRich.getSlides()[0];
		TextRun[] textRunsR = slideOneR.getTextRuns();

		assertEquals(2, textRunsR.length);
		assertEquals("This is a title, it\u2019s in black", textRunsR[0].getText());
		assertEquals("This is the subtitle, in bold\nThis bit is blue and italic\nThis bit is red (normal)", textRunsR[1].getText());
		assertEquals("This is a title, it\u2019s in black", textRunsR[0].getRawText());
		assertEquals("This is the subtitle, in bold\rThis bit is blue and italic\rThis bit is red (normal)", textRunsR[1].getRawText());
	}

	/**
	 * Test to ensure changing non rich text bytes->bytes works correctly
	 */
	public void testSetText() {
		Slide slideOne = ss.getSlides()[0];
		TextRun[] textRuns = slideOne.getTextRuns();
		TextRun run = textRuns[0];

		// Check current text
		assertEquals("This is a test title", run.getText());

		// Change
		String changeTo = "New test title";
		run.setText(changeTo);
		assertEquals(changeTo, run.getText());

		// Ensure trailing \n's get stripped
		run.setText(changeTo + "\n");
		assertEquals(changeTo, run.getText());
	}

	/**
	 * Test to ensure that changing non rich text between bytes and
	 *  chars works correctly
	 */
	public void testAdvancedSetText() {
		Slide slideOne = ss.getSlides()[0];
		TextRun run = slideOne.getTextRuns()[0];

		TextHeaderAtom tha = run._headerAtom;
		TextBytesAtom tba = run._byteAtom;
		TextCharsAtom tca = run._charAtom;

		// Bytes -> Bytes
		assertNull(tca);
		assertNotNull(tba);
		assertFalse(run._isUnicode);
		assertEquals("This is a test title", run.getText());

		String changeBytesOnly = "New Test Title";
		run.setText(changeBytesOnly);
		tba = run._byteAtom;
		tca = run._charAtom;

		assertEquals(changeBytesOnly, run.getText());
		assertFalse(run._isUnicode);
		assertNull(tca);
		assertNotNull(tba);

		// Bytes -> Chars
		assertNull(tca);
		assertNotNull(tba);
		assertFalse(run._isUnicode);
		assertEquals(changeBytesOnly, run.getText());

		String changeByteChar = "This is a test title with a '\u0121' g with a dot";
		run.setText(changeByteChar);
		tba = run._byteAtom;
		tca = run._charAtom;

		assertEquals(changeByteChar, run.getText());
		assertTrue(run._isUnicode);
		assertNotNull(tca);
		assertNull(tba);

		// Chars -> Chars
		assertNull(tba);
		assertNotNull(tca);
		assertTrue(run._isUnicode);
		assertEquals(changeByteChar, run.getText());

		String changeCharChar = "This is a test title with a '\u0147' N with a hat";
		run.setText(changeCharChar);
		tba = run._byteAtom;
		tca = run._charAtom;

		assertEquals(changeCharChar, run.getText());
		assertTrue(run._isUnicode);
		assertNotNull(tca);
		assertNull(tba);
	}

	/**
	 * Tests to ensure that non rich text has the right default rich text run
	 *  set up for it
	 */
	public void testGetRichTextNonRich() {
		Slide slideOne = ss.getSlides()[0];
		TextRun[] textRuns = slideOne.getTextRuns();

		assertEquals(2, textRuns.length);

		TextRun trA = textRuns[0];
		TextRun trB = textRuns[1];

		assertEquals(1, trA.getRichTextRuns().length);
		assertEquals(1, trB.getRichTextRuns().length);

		RichTextRun rtrA = trA.getRichTextRuns()[0];
		RichTextRun rtrB = trB.getRichTextRuns()[0];

		assertEquals(trA.getText(), rtrA.getText());
		assertEquals(trB.getText(), rtrB.getText());

		assertNull(rtrA._getRawCharacterStyle());
		assertNull(rtrA._getRawParagraphStyle());
		assertNull(rtrB._getRawCharacterStyle());
		assertNull(rtrB._getRawParagraphStyle());
	}

	/**
	 * Tests to ensure that the rich text runs are built up correctly
	 */
	public void testGetRichText() {
		Slide slideOne = ssRich.getSlides()[0];
		TextRun[] textRuns = slideOne.getTextRuns();

		assertEquals(2, textRuns.length);

		TextRun trA = textRuns[0];
		TextRun trB = textRuns[1];

		assertEquals(1, trA.getRichTextRuns().length);
		assertEquals(3, trB.getRichTextRuns().length);

		RichTextRun rtrA = trA.getRichTextRuns()[0];
		RichTextRun rtrB = trB.getRichTextRuns()[0];
		RichTextRun rtrC = trB.getRichTextRuns()[1];
		RichTextRun rtrD = trB.getRichTextRuns()[2];

		assertEquals(trA.getText(), rtrA.getText());

		assertEquals(trB.getText().substring(0, 30), rtrB.getText());
		assertEquals(trB.getText().substring(30,58), rtrC.getText());
		assertEquals(trB.getText().substring(58,82), rtrD.getText());

		assertNull(rtrA._getRawCharacterStyle());
		assertNull(rtrA._getRawParagraphStyle());
		assertNotNull(rtrB._getRawCharacterStyle());
		assertNotNull(rtrB._getRawParagraphStyle());
		assertNotNull(rtrC._getRawCharacterStyle());
		assertNotNull(rtrC._getRawParagraphStyle());
		assertNotNull(rtrD._getRawCharacterStyle());
		assertNotNull(rtrD._getRawParagraphStyle());

		// Same paragraph styles
		assertEquals(rtrB._getRawParagraphStyle(), rtrC._getRawParagraphStyle());
		assertEquals(rtrB._getRawParagraphStyle(), rtrD._getRawParagraphStyle());

		// Different char styles
		assertFalse( rtrB._getRawCharacterStyle().equals( rtrC._getRawCharacterStyle() ));
		assertFalse( rtrB._getRawCharacterStyle().equals( rtrD._getRawCharacterStyle() ));
		assertFalse( rtrC._getRawCharacterStyle().equals( rtrD._getRawCharacterStyle() ));
	}

	/**
	 * Tests to ensure that setting the text where the text isn't rich,
	 *  ensuring that everything stays with the same default styling
	 */
	public void testSetTextWhereNotRich() {
		Slide slideOne = ss.getSlides()[0];
		TextRun[] textRuns = slideOne.getTextRuns();
		TextRun trB = textRuns[1];
		assertEquals(1, trB.getRichTextRuns().length);

		RichTextRun rtrB = trB.getRichTextRuns()[0];
		assertEquals(trB.getText(), rtrB.getText());
		assertNull(rtrB._getRawCharacterStyle());
		assertNull(rtrB._getRawParagraphStyle());

		// Change text via normal
		trB.setText("Test Foo Test");
		rtrB = trB.getRichTextRuns()[0];
		assertEquals("Test Foo Test", trB.getText());
		assertEquals("Test Foo Test", rtrB.getText());
		assertNull(rtrB._getRawCharacterStyle());
		assertNull(rtrB._getRawParagraphStyle());
	}

	/**
	 * Tests to ensure that setting the text where the text is rich
	 *  sets everything to the same styling
	 */
	public void testSetTextWhereRich() {
		Slide slideOne = ssRich.getSlides()[0];
		TextRun[] textRuns = slideOne.getTextRuns();
		TextRun trB = textRuns[1];
		assertEquals(3, trB.getRichTextRuns().length);

		RichTextRun rtrB = trB.getRichTextRuns()[0];
		RichTextRun rtrC = trB.getRichTextRuns()[1];
		RichTextRun rtrD = trB.getRichTextRuns()[2];
		TextPropCollection tpBP = rtrB._getRawParagraphStyle();
		TextPropCollection tpBC = rtrB._getRawCharacterStyle();
		TextPropCollection tpCP = rtrC._getRawParagraphStyle();
		TextPropCollection tpCC = rtrC._getRawCharacterStyle();
		TextPropCollection tpDP = rtrD._getRawParagraphStyle();
		TextPropCollection tpDC = rtrD._getRawCharacterStyle();

		assertEquals(trB.getText().substring(0, 30), rtrB.getText());
		assertNotNull(tpBP);
		assertNotNull(tpBC);
		assertNotNull(tpCP);
		assertNotNull(tpCC);
		assertNotNull(tpDP);
		assertNotNull(tpDC);
		assertTrue(tpBP.equals(tpCP));
		assertTrue(tpBP.equals(tpDP));
		assertTrue(tpCP.equals(tpDP));
		assertFalse(tpBC.equals(tpCC));
		assertFalse(tpBC.equals(tpDC));
		assertFalse(tpCC.equals(tpDC));

		// Change text via normal
		trB.setText("Test Foo Test");

		// Ensure now have first style
		assertEquals(1, trB.getRichTextRuns().length);
		rtrB = trB.getRichTextRuns()[0];
		assertEquals("Test Foo Test", trB.getText());
		assertEquals("Test Foo Test", rtrB.getText());
		assertNotNull(rtrB._getRawCharacterStyle());
		assertNotNull(rtrB._getRawParagraphStyle());
		assertEquals( tpBP, rtrB._getRawParagraphStyle() );
		assertEquals( tpBC, rtrB._getRawCharacterStyle() );
	}

	/**
	 * Test to ensure the right stuff happens if we change the text
	 *  in a rich text run, that doesn't happen to actually be rich
	 */
	public void testChangeTextInRichTextRunNonRich() {
		Slide slideOne = ss.getSlides()[0];
		TextRun[] textRuns = slideOne.getTextRuns();
		TextRun trB = textRuns[1];
		assertEquals(1, trB.getRichTextRuns().length);

		RichTextRun rtrB = trB.getRichTextRuns()[0];
		assertEquals(trB.getText(), rtrB.getText());
		assertNull(rtrB._getRawCharacterStyle());
		assertNull(rtrB._getRawParagraphStyle());

		// Change text via rich
		rtrB.setText("Test Test Test");
		assertEquals("Test Test Test", trB.getText());
		assertEquals("Test Test Test", rtrB.getText());

		// Will now have dummy props
		assertNotNull(rtrB._getRawCharacterStyle());
		assertNotNull(rtrB._getRawParagraphStyle());
	}

	/**
	 * Tests to ensure changing the text within rich text runs works
	 *  correctly
	 */
	public void testChangeTextInRichTextRun() {
		Slide slideOne = ssRich.getSlides()[0];
		TextRun[] textRuns = slideOne.getTextRuns();
		TextRun trB = textRuns[1];
		assertEquals(3, trB.getRichTextRuns().length);

		// We start with 3 text runs, each with their own set of styles,
		//  but all sharing the same paragraph styles
		RichTextRun rtrB = trB.getRichTextRuns()[0];
		RichTextRun rtrC = trB.getRichTextRuns()[1];
		RichTextRun rtrD = trB.getRichTextRuns()[2];
		TextPropCollection tpBP = rtrB._getRawParagraphStyle();
		TextPropCollection tpBC = rtrB._getRawCharacterStyle();
		TextPropCollection tpCP = rtrC._getRawParagraphStyle();
		TextPropCollection tpCC = rtrC._getRawCharacterStyle();
		TextPropCollection tpDP = rtrD._getRawParagraphStyle();
		TextPropCollection tpDC = rtrD._getRawCharacterStyle();

		// Check text and stylings
		assertEquals(trB.getText().substring(0, 30), rtrB.getText());
		assertNotNull(tpBP);
		assertNotNull(tpBC);
		assertNotNull(tpCP);
		assertNotNull(tpCC);
		assertNotNull(tpDP);
		assertNotNull(tpDC);
		assertTrue(tpBP.equals(tpCP));
		assertTrue(tpBP.equals(tpDP));
		assertTrue(tpCP.equals(tpDP));
		assertFalse(tpBC.equals(tpCC));
		assertFalse(tpBC.equals(tpDC));
		assertFalse(tpCC.equals(tpDC));

		// Check text in the rich runs
		assertEquals("This is the subtitle, in bold\n", rtrB.getText());
		assertEquals("This bit is blue and italic\n", rtrC.getText());
		assertEquals("This bit is red (normal)", rtrD.getText());

		String newBText = "New Subtitle, will still be bold\n";
		String newCText = "New blue and italic text\n";
		String newDText = "Funky new normal red text";
		rtrB.setText(newBText);
		rtrC.setText(newCText);
		rtrD.setText(newDText);
		assertEquals(newBText, rtrB.getText());
		assertEquals(newCText, rtrC.getText());
		assertEquals(newDText, rtrD.getText());

		assertEquals(newBText + newCText + newDText, trB.getText());

		// The styles should have been updated for the new sizes
		assertEquals(newBText.length(), tpBC.getCharactersCovered());
		assertEquals(newCText.length(), tpCC.getCharactersCovered());
		assertEquals(newDText.length()+1, tpDC.getCharactersCovered()); // Last one is always one larger

		assertEquals(
				newBText.length() + newCText.length() + newDText.length(),
				tpBP.getCharactersCovered()
		);

		// Paragraph style should be sum of text length
		assertEquals(newBText.length() + newCText.length() + newDText.length(), tpBP.getCharactersCovered());

		// Check stylings still as expected
		TextPropCollection ntpBC = rtrB._getRawCharacterStyle();
		TextPropCollection ntpCC = rtrC._getRawCharacterStyle();
		TextPropCollection ntpDC = rtrD._getRawCharacterStyle();
		assertEquals(tpBC.getTextPropList(), ntpBC.getTextPropList());
		assertEquals(tpCC.getTextPropList(), ntpCC.getTextPropList());
		assertEquals(tpDC.getTextPropList(), ntpDC.getTextPropList());
	}


	/**
	 * Test case for Bug 41015.
	 *
	 * In some cases RichTextRun.getText() threw StringIndexOutOfBoundsException because
	 * of the wrong list of potential paragraph properties defined in StyleTextPropAtom.
	 *
	 */
	public void testBug41015() throws IOException {
		RichTextRun[] rt;

		SlideShow ppt = new SlideShow(_slTests.openResourceAsStream("bug-41015.ppt"));
		Slide sl = ppt.getSlides()[0];
		TextRun[] txt = sl.getTextRuns();
		assertEquals(2, txt.length);

		rt = txt[0].getRichTextRuns();
		assertEquals(1, rt.length);
		assertEquals(0, rt[0].getIndentLevel());
		assertEquals("sdfsdfsdf", rt[0].getText());

		rt = txt[1].getRichTextRuns();
		assertEquals(2, rt.length);
		assertEquals(0, rt[0].getIndentLevel());
		assertEquals("Sdfsdfsdf\n" +
				"Dfgdfg\n" +
				"Dfgdfgdfg\n", rt[0].getText());
		assertEquals(1, rt[1].getIndentLevel());
		assertEquals("Sdfsdfs\n" +
				"Sdfsdf\n", rt[1].getText());
	}

	/**
	 * Test creation of TextRun objects.
	 */
	public void testAddTextRun() {
		SlideShow ppt = new SlideShow();
		Slide slide = ppt.createSlide();

		assertNull(slide.getTextRuns());

		TextBox shape1 = new TextBox();
		TextRun run1 = shape1.getTextRun();
		assertSame(run1, shape1.createTextRun());
		run1.setText("Text 1");
		slide.addShape(shape1);

		//The array of Slide's text runs must be updated when new text shapes are added.
		TextRun[] runs = slide.getTextRuns();
		assertNotNull(runs);
		assertSame(run1, runs[0]);

		TextBox shape2 = new TextBox();
		TextRun run2 = shape2.getTextRun();
		assertSame(run2, shape2.createTextRun());
		run2.setText("Text 2");
		slide.addShape(shape2);

		runs = slide.getTextRuns();
		assertEquals(2, runs.length);

		assertSame(run1, runs[0]);
		assertSame(run2, runs[1]);

		//as getShapes()
		Shape[] sh = slide.getShapes();
		assertEquals(2, sh.length);
		assertTrue(sh[0] instanceof TextBox);
		TextBox box1 = (TextBox)sh[0];
		assertSame(run1, box1.getTextRun());
		TextBox box2 = (TextBox)sh[1];
		assertSame(run2, box2.getTextRun());

		//test Table - a complex group of shapes containing text objects
		Slide slide2 = ppt.createSlide();
		assertNull(slide2.getTextRuns());
		Table table = new Table(2, 2);
		slide2.addShape(table);
		runs = slide2.getTextRuns();
		assertNotNull(runs);
		assertEquals(4, runs.length);
	}

    public void test48916() throws IOException {
        SlideShow ppt = new SlideShow(_slTests.openResourceAsStream("SampleShow.ppt"));
        for(Slide slide : ppt.getSlides()){
            for(Shape sh : slide.getShapes()){
                if(sh instanceof TextShape){
                    TextShape tx = (TextShape)sh;
                    TextRun run = tx.getTextRun();
                    //verify that records cached in  TextRun and EscherTextboxWrapper are the same
                    Record[] runChildren = run.getRecords();
                    Record[] txboxChildren = tx.getEscherTextboxWrapper().getChildRecords();
                    assertEquals(runChildren.length, txboxChildren.length);
                    for(int i=0; i < txboxChildren.length; i++){
                        assertSame(txboxChildren[i], runChildren[i]);
                    }
                    //caused NPE prior to fix of Bugzilla #48916 
                    run.getRichTextRuns()[0].setBold(true);
                    run.getRichTextRuns()[0].setFontColor(Color.RED);
                }
            }
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ppt.write(out);
        ppt = new SlideShow(new ByteArrayInputStream(out.toByteArray()));
        for(Slide slide : ppt.getSlides()){
            for(Shape sh : slide.getShapes()){
                if(sh instanceof TextShape){
                    TextShape tx = (TextShape)sh;
                    TextRun run = tx.getTextRun();
                    RichTextRun rt = run.getRichTextRuns()[0];
                    assertTrue(rt.isBold());
                    assertEquals(rt.getFontColor(), Color.RED);
                }
            }
        }

    }
}
