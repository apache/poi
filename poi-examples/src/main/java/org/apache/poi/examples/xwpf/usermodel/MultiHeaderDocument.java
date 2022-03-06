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

package org.apache.poi.examples.xwpf.usermodel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STHdrFtr;

public class MultiHeaderDocument {
    public static void main(String[] args) throws IOException {
        try (XWPFDocument doc = new XWPFDocument()) {
            // First Body Paragraph
            XWPFParagraph p1 = doc.createParagraph();

            XWPFRun run1 = p1.createRun();
            run1.setText("This is the text of the first paragraph/page/section");

            CTP ctp1 = p1.getCTP();
            CTPPr ppr1 = null;
            if (!ctp1.isSetPPr()) {
                ctp1.addNewPPr();
            }
            ppr1 = ctp1.getPPr();

            CTSectPr sec1 = null;
            if (!ppr1.isSetSectPr()) {
                ppr1.addNewSectPr();
            }

            sec1 = ppr1.getSectPr();

            // Paragraph for Section 1 header.
            CTP parCTP = CTP.Factory.newInstance();
            parCTP.addNewR().addNewT().setStringValue("Header For Section 1");
            XWPFParagraph headerPar1 = new XWPFParagraph(parCTP, doc);

            XWPFParagraph[] headerPars1 = { headerPar1 };

            XWPFHeaderFooterPolicy pol1 = new XWPFHeaderFooterPolicy(doc, sec1);
            pol1.createHeader(STHdrFtr.DEFAULT, headerPars1);

            // Create second body paragraph with a different header

            XWPFParagraph p2 = doc.createParagraph();

            XWPFRun run2 = p2.createRun();
            run2.setText("This is the text of the second paragraph/page/section");

            CTP ctp2 = p2.getCTP();
            CTPPr ppr2 = null;
            if (!ctp2.isSetPPr()) {
                ctp2.addNewPPr();
            }
            ppr2 = ctp2.getPPr();

            CTSectPr sec2 = null;
            if (!ppr2.isSetSectPr()) {
                ppr2.addNewSectPr();
            }

            sec2 = ppr2.getSectPr();

            // Paragraph for Section 2 header.
            CTP parCTP2 = CTP.Factory.newInstance();
            parCTP2.addNewR().addNewT().setStringValue("Header For Section 2");
            XWPFParagraph headerPar2 = new XWPFParagraph(parCTP2, doc);

            XWPFParagraph[] headerPars2 = { headerPar2 };

            XWPFHeaderFooterPolicy pol2 = new XWPFHeaderFooterPolicy(doc, sec2);
            pol2.createHeader(STHdrFtr.DEFAULT, headerPars2);

            try (OutputStream os = new FileOutputStream(new File("multiheader.docx"))) {

                doc.write(os);

            }
        }
    }
}
