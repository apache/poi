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
package org.apache.poi.xwpf.usermodel;

import java.math.BigInteger;

import junit.framework.TestCase;

import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STOnOff;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STUnderline;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STVerticalAlignRun;

/**
 * Tests for XWPF Run
 */
public class TestXWPFRun extends TestCase {

    public CTR ctRun;
    public XWPFParagraph p;

    protected void setUp() {
	XWPFDocument doc = new XWPFDocument();
	p = doc.createParagraph();

	this.ctRun = CTR.Factory.newInstance();

    }

    public void testSetGetBold() {
	CTRPr rpr = ctRun.addNewRPr();
	rpr.addNewB().setVal(STOnOff.TRUE);

	XWPFRun run = new XWPFRun(ctRun, p);
	assertEquals(true, run.isBold());

	run.setBold(false);
	assertEquals(STOnOff.FALSE, rpr.getB().getVal());
    }

    public void testSetGetItalic() {
	CTRPr rpr = ctRun.addNewRPr();
	rpr.addNewI().setVal(STOnOff.TRUE);

	XWPFRun run = new XWPFRun(ctRun, p);
	assertEquals(true, run.isItalic());

	run.setItalic(false);
	assertEquals(STOnOff.FALSE, rpr.getI().getVal());
    }

    public void testSetGetStrike() {
	CTRPr rpr = ctRun.addNewRPr();
	rpr.addNewStrike().setVal(STOnOff.TRUE);

	XWPFRun run = new XWPFRun(ctRun, p);
	assertEquals(true, run.isStrike());

	run.setStrike(false);
	assertEquals(STOnOff.FALSE, rpr.getStrike().getVal());
    }

    public void testSetGetUnderline() {
	CTRPr rpr = ctRun.addNewRPr();
	rpr.addNewU().setVal(STUnderline.DASH);

	XWPFRun run = new XWPFRun(ctRun, p);
	assertEquals(UnderlinePatterns.DASH.getValue(), run.getUnderline()
		.getValue());

	run.setUnderline(UnderlinePatterns.NONE);
	assertEquals(STUnderline.NONE.intValue(), rpr.getU().getVal()
		.intValue());
    }

    public void testSetGetVAlign() {
	CTRPr rpr = ctRun.addNewRPr();
	rpr.addNewVertAlign().setVal(STVerticalAlignRun.SUBSCRIPT);

	XWPFRun run = new XWPFRun(ctRun, p);
	assertEquals(VerticalAlign.SUBSCRIPT, run.getSubscript());

	run.setSubscript(VerticalAlign.BASELINE);
	assertEquals(STVerticalAlignRun.BASELINE, rpr.getVertAlign().getVal());
    }

    public void testSetGetFontFamily() {
	CTRPr rpr = ctRun.addNewRPr();
	rpr.addNewRFonts().setAscii("Times New Roman");

	XWPFRun run = new XWPFRun(ctRun, p);
	assertEquals("Times New Roman", run.getFontFamily());

	run.setFontFamily("Verdana");
	assertEquals("Verdana", rpr.getRFonts().getAscii());
    }

    public void testSetGetFontSize() {
	CTRPr rpr = ctRun.addNewRPr();
	rpr.addNewSz().setVal(new BigInteger("4000"));

	XWPFRun run = new XWPFRun(ctRun, p);
	assertEquals(4000, run.getFontSize().longValue());

	run.setFontSize(new BigInteger("2400"));
	assertEquals(2400, rpr.getSz().getVal().longValue());
    }

    public void testSetGetTextForegroundBackground() {
	CTRPr rpr = ctRun.addNewRPr();
	rpr.addNewPosition().setVal(new BigInteger("4000"));

	XWPFRun run = new XWPFRun(ctRun, p);
	assertEquals(4000, run.getTextPosition().longValue());

	run.setTextPosition(new BigInteger("2400"));
	assertEquals(2400, rpr.getPosition().getVal().longValue());
    }

}
