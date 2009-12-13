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
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSdtBlock;

/**
 * @author Paolo Mottadelli
 */
public final class TestXWPFHeadings extends TestCase{
	private static final String HEADING1 = "Heading1";

	public void testSetParagraphStyle() throws IOException, XmlException {
		//new clean instance of paragraph
		XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("heading123.docx");
		XWPFParagraph p = doc.createParagraph();
		XWPFRun run = p.createRun();
		run.setText("Heading 1");

		CTSdtBlock block = doc.getDocument().getBody().addNewSdt();

		assertNull(p.getStyle());
		p.setStyle(HEADING1);
		assertEquals(HEADING1, p.getCTP().getPPr().getPStyle().getVal());

		doc.createTOC();
        /*
		// TODO - finish this test
		if (false) {
			CTStyles styles = doc.getStyle();
			CTStyle style = styles.addNewStyle();
			style.setType(STStyleType.PARAGRAPH);
			style.setStyleId("Heading1");
		}

		if (false) {
			File file = TempFile.createTempFile("testHeaders", ".docx");
			OutputStream out = new FileOutputStream(file);
			doc.write(out);
			out.close();
		}
        */
    }
}
