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

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.poi.xwpf.XWPFTestDataSamples;
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTText;

public final class TestXWPFHeader extends TestCase {

	public void testSimpleHeader() {
		XWPFDocument sampleDoc = XWPFTestDataSamples.openSampleDocument("headerFooter.docx");

		XWPFHeaderFooterPolicy policy = sampleDoc.getHeaderFooterPolicy();


		XWPFHeader header = policy.getDefaultHeader();
		XWPFFooter footer = policy.getDefaultFooter();
		assertNotNull(header);
		assertNotNull(footer);

		// TODO verify if the following is correct
		assertNull(header.toString());

	}

	public void testSetHeader() throws IOException {
		XWPFDocument sampleDoc = XWPFTestDataSamples.openSampleDocument("SampleDoc.docx");
		// no header is set (yet)
		XWPFHeaderFooterPolicy policy = sampleDoc.getHeaderFooterPolicy();
		assertNull(policy.getDefaultHeader());
		assertNull(policy.getFirstPageHeader());
		assertNull(policy.getDefaultFooter());

		CTP ctP1 = CTP.Factory.newInstance();
		CTR ctR1 = ctP1.addNewR();
		CTText t = ctR1.addNewT();
		t.setStringValue("Paragraph in header");

		// Commented MB 23 May 2010
		//CTP ctP2 = CTP.Factory.newInstance();
		//CTR ctR2 = ctP2.addNewR();
		//CTText t2 = ctR2.addNewT();
		//t2.setStringValue("Second paragraph.. for footer");
		
		// Create two paragraphs for insertion into the footer.
		// Previously only one was inserted MB 23 May 2010
		CTP ctP2 = CTP.Factory.newInstance();
		CTR ctR2 = ctP2.addNewR();
		CTText t2 = ctR2.addNewT();
		t2.setStringValue("First paragraph for the footer");
		
		CTP ctP3 = CTP.Factory.newInstance();
		CTR ctR3 = ctP3.addNewR();
		CTText t3 = ctR3.addNewT();
		t3.setStringValue("Second paragraph for the footer");

		XWPFParagraph p1 = new XWPFParagraph(ctP1);
		XWPFParagraph[] pars = new XWPFParagraph[1];
		pars[0] = p1;

		XWPFParagraph p2 = new XWPFParagraph(ctP2);
		XWPFParagraph p3 = new XWPFParagraph(ctP3, null);
		XWPFParagraph[] pars2 = new XWPFParagraph[2];
		pars2[0] = p2;
		pars2[1] = p3;

		// Set headers
		policy.createHeader(policy.DEFAULT, pars);
		policy.createHeader(policy.FIRST);
		// Set a default footer and capture the returned XWPFFooter object.
		XWPFFooter footer = policy.createFooter(policy.DEFAULT, pars2);

		// Ensure the headers and footer were set correctly....
		assertNotNull(policy.getDefaultHeader());
		assertNotNull(policy.getFirstPageHeader());
		assertNotNull(policy.getDefaultFooter());
		// ....and that the footer object captured above contains two
		// paragraphs of text.
		assertEquals(footer.getParagraphs().size(), 2);
		
		// As an additional check, recover the defauls footer and
		// make sure that it contains two paragraphs of text and that
		// both do hold what is expected.
		footer = policy.getDefaultFooter();

		XWPFParagraph[] paras = new XWPFParagraph[footer.getParagraphs().size()];
		int i=0;
		for(XWPFParagraph p : footer.getParagraphs()) {
		   paras[i++] = p;
		}
		
		assertEquals(paras.length, 2);
		assertEquals(paras[0].getText(), "First paragraph for the footer");
		assertEquals(paras[1].getText(), "Second paragraph for the footer");
	}

	public void testSetWatermark() {
		XWPFDocument sampleDoc = XWPFTestDataSamples.openSampleDocument("SampleDoc.docx");
		// no header is set (yet)
		XWPFHeaderFooterPolicy policy = sampleDoc.getHeaderFooterPolicy();
		assertNull(policy.getDefaultHeader());
		assertNull(policy.getFirstPageHeader());
		assertNull(policy.getDefaultFooter());

		policy.createWatermark("DRAFT");

		assertNotNull(policy.getDefaultHeader());
		assertNotNull(policy.getFirstPageHeader());
		assertNotNull(policy.getEvenPageHeader());
	}
}
