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

package org.apache.poi.xwpf.model;

import junit.framework.TestCase;

import org.apache.poi.xwpf.XWPFTestDataSamples;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

/**
 * Tests for XWPF Header Footer Stuff
 */
public class TestXWPFHeaderFooterPolicy extends TestCase {
	private XWPFDocument noHeader;
	private XWPFDocument header;
	private XWPFDocument headerFooter;
	private XWPFDocument footer;
	private XWPFDocument oddEven;
	private XWPFDocument diffFirst;

	protected void setUp() {

		noHeader = XWPFTestDataSamples.openSampleDocument("NoHeadFoot.docx");
		header = XWPFTestDataSamples.openSampleDocument("ThreeColHead.docx");
		headerFooter = XWPFTestDataSamples.openSampleDocument("SimpleHeadThreeColFoot.docx");
		footer = XWPFTestDataSamples.openSampleDocument("FancyFoot.docx");
		oddEven = XWPFTestDataSamples.openSampleDocument("PageSpecificHeadFoot.docx");
		diffFirst = XWPFTestDataSamples.openSampleDocument("DiffFirstPageHeadFoot.docx");
	}

	public void testPolicy() {
		XWPFHeaderFooterPolicy policy;

		policy = noHeader.getHeaderFooterPolicy();
		assertNull(policy.getDefaultHeader());
		assertNull(policy.getDefaultFooter());

		assertNull(policy.getHeader(1));
		assertNull(policy.getHeader(2));
		assertNull(policy.getHeader(3));
		assertNull(policy.getFooter(1));
		assertNull(policy.getFooter(2));
		assertNull(policy.getFooter(3));


		policy = header.getHeaderFooterPolicy();
		assertNotNull(policy.getDefaultHeader());
		assertNull(policy.getDefaultFooter());

		assertEquals(policy.getDefaultHeader(), policy.getHeader(1));
		assertEquals(policy.getDefaultHeader(), policy.getHeader(2));
		assertEquals(policy.getDefaultHeader(), policy.getHeader(3));
		assertNull(policy.getFooter(1));
		assertNull(policy.getFooter(2));
		assertNull(policy.getFooter(3));


		policy = footer.getHeaderFooterPolicy();
		assertNull(policy.getDefaultHeader());
		assertNotNull(policy.getDefaultFooter());

		assertNull(policy.getHeader(1));
		assertNull(policy.getHeader(2));
		assertNull(policy.getHeader(3));
		assertEquals(policy.getDefaultFooter(), policy.getFooter(1));
		assertEquals(policy.getDefaultFooter(), policy.getFooter(2));
		assertEquals(policy.getDefaultFooter(), policy.getFooter(3));


		policy = headerFooter.getHeaderFooterPolicy();
		assertNotNull(policy.getDefaultHeader());
		assertNotNull(policy.getDefaultFooter());

		assertEquals(policy.getDefaultHeader(), policy.getHeader(1));
		assertEquals(policy.getDefaultHeader(), policy.getHeader(2));
		assertEquals(policy.getDefaultHeader(), policy.getHeader(3));
		assertEquals(policy.getDefaultFooter(), policy.getFooter(1));
		assertEquals(policy.getDefaultFooter(), policy.getFooter(2));
		assertEquals(policy.getDefaultFooter(), policy.getFooter(3));


		policy = oddEven.getHeaderFooterPolicy();
		assertNotNull(policy.getDefaultHeader());
		assertNotNull(policy.getDefaultFooter());
		assertNotNull(policy.getEvenPageHeader());
		assertNotNull(policy.getEvenPageFooter());

		assertEquals(policy.getDefaultHeader(), policy.getHeader(1));
		assertEquals(policy.getEvenPageHeader(), policy.getHeader(2));
		assertEquals(policy.getDefaultHeader(), policy.getHeader(3));
		assertEquals(policy.getDefaultFooter(), policy.getFooter(1));
		assertEquals(policy.getEvenPageFooter(), policy.getFooter(2));
		assertEquals(policy.getDefaultFooter(), policy.getFooter(3));


		policy = diffFirst.getHeaderFooterPolicy();
		assertNotNull(policy.getDefaultHeader());
		assertNotNull(policy.getDefaultFooter());
		assertNotNull(policy.getFirstPageHeader());
		assertNotNull(policy.getFirstPageFooter());
		assertNull(policy.getEvenPageHeader());
		assertNull(policy.getEvenPageFooter());

		assertEquals(policy.getFirstPageHeader(), policy.getHeader(1));
		assertEquals(policy.getDefaultHeader(), policy.getHeader(2));
		assertEquals(policy.getDefaultHeader(), policy.getHeader(3));
		assertEquals(policy.getFirstPageFooter(), policy.getFooter(1));
		assertEquals(policy.getDefaultFooter(), policy.getFooter(2));
		assertEquals(policy.getDefaultFooter(), policy.getFooter(3));
	}

	public void testContents() {
		XWPFHeaderFooterPolicy policy;

		// Test a few simple bits off a simple header
		policy = diffFirst.getHeaderFooterPolicy();

		assertEquals(
			"I am the header on the first page, and I" + '\u2019' + "m nice and simple\n",
			policy.getFirstPageHeader().getText()
		);
		assertEquals(
				"First header column!\tMid header\tRight header!\n",
				policy.getDefaultHeader().getText()
		);


		// And a few bits off a more complex header
		policy = oddEven.getHeaderFooterPolicy();

		assertEquals(
			"[ODD Page Header text]\n\n",
			policy.getDefaultHeader().getText()
		);
		assertEquals(
			"[This is an Even Page, with a Header]\n\n",
			policy.getEvenPageHeader().getText()
		);
	}
}
