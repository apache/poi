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
package org.apache.poi.xwpf.usermodel.examples;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTText;

/**
 * 
 * @author Richard Ngo
 *
 */
public class SimpleDocumentWithHeader {

	public static void main(String[] args) throws IOException {
		try (XWPFDocument doc = new XWPFDocument()) {

			XWPFParagraph p = doc.createParagraph();

			XWPFRun r = p.createRun();
			r.setText("Some Text");
			r.setBold(true);
			r = p.createRun();
			r.setText("Goodbye");

			CTP ctP = CTP.Factory.newInstance();
			CTText t = ctP.addNewR().addNewT();
			t.setStringValue("header");
			XWPFParagraph[] pars = new XWPFParagraph[1];
			p = new XWPFParagraph(ctP, doc);
			pars[0] = p;

			XWPFHeaderFooterPolicy hfPolicy = doc.createHeaderFooterPolicy();
			hfPolicy.createHeader(XWPFHeaderFooterPolicy.DEFAULT, pars);

			ctP = CTP.Factory.newInstance();
			t = ctP.addNewR().addNewT();
			t.setStringValue("My Footer");
			pars[0] = new XWPFParagraph(ctP, doc);
			hfPolicy.createFooter(XWPFHeaderFooterPolicy.DEFAULT, pars);

			try (OutputStream os = new FileOutputStream(new File("header.docx"))) {
				doc.write(os);
			}
		}
	}
}