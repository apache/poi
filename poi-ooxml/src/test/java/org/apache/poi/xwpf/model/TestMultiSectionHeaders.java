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

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFFooter;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STHdrFtr;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestMultiSectionHeaders {

    @Test
    void testAddHeadersForTwoDistinctSections() {

        String header1Text = "Header 1 Text";
        String header2Text = "Header 2 Text";

        XWPFDocument doc = new XWPFDocument();

        // Add first body/section paragraph
        XWPFParagraph par1 = doc.createParagraph();

        XWPFRun run1 = par1.createRun();
        run1.setText("Text for first body paragraph");

        CTPPr ppr1 = par1.getCTPPr();

        CTSectPr sec1 = null;
        if (!ppr1.isSetSectPr()) {
            ppr1.addNewSectPr();
        }

        sec1 = ppr1.getSectPr();

        // Create paragraph of the first header
        CTP parCTP = CTP.Factory.newInstance();
        parCTP.addNewR().addNewT().setStringValue(header1Text);
        XWPFParagraph headerPar1 = new XWPFParagraph(parCTP, doc);

        XWPFParagraph[] headerPars1 = { headerPar1 };

        XWPFHeaderFooterPolicy pol1 = new XWPFHeaderFooterPolicy(doc, sec1);
        XWPFHeader header1 = pol1.createHeader(STHdrFtr.DEFAULT, headerPars1);

        // Add second body/section paragraph
        XWPFParagraph par2 = doc.createParagraph();

        XWPFRun run2 = par2.createRun();
        run2.setText("Text for second body paragraph");

        CTPPr ppr2 = par2.getCTPPr();

        CTSectPr sec2 = null;
        if (!ppr2.isSetSectPr()) {
            ppr2.addNewSectPr();
        }

        sec2 = ppr2.getSectPr();

        // Create paragraph of the second header
        CTP parCTP2 = CTP.Factory.newInstance();
        parCTP2.addNewR().addNewT().setStringValue(header2Text);
        XWPFParagraph headerPar2 = new XWPFParagraph(parCTP2, doc);

        XWPFParagraph[] headerPars2 = { headerPar2 };

        XWPFHeaderFooterPolicy pol2 = new XWPFHeaderFooterPolicy(doc, sec2);
        XWPFHeader header2 = pol2.createHeader(STHdrFtr.DEFAULT, headerPars2);

        // Validate the headers are not null
        assertNotNull(header1.getListParagraph().get(0));
        assertNotNull(header2.getListParagraph().get(0));

        // Validate the headers are not equal
        assertNotEquals(header1, header2);

        String textFromHeader1 = header1.getListParagraph().get(0).getCTP().getRArray()[0].getTArray()[0]
                .getStringValue();
        // Validate the text is equal to the text we assigned
        assertEquals(header1Text, textFromHeader1);

        String textFromHeader2 = header2.getListParagraph().get(0).getCTP().getRArray()[0].getTArray()[0]
                .getStringValue();
        // Validate the text is equal to the text we assigned
        assertEquals(header2Text, textFromHeader2);

        // Validate the headers text are not equal
        assertNotEquals(header1Text, header2Text);
    }

    @Test
    void testAddFootersForTwoDistinctSections() throws IOException {

        String footer1Text = "Footer 1 Text";
        String footer2Text = "Footer 2 Text";

        XWPFDocument doc = new XWPFDocument();

        // Add first body/section paragraph
        XWPFParagraph par1 = doc.createParagraph();

        XWPFRun run1 = par1.createRun();
        run1.setText("Text for first body paragraph");

        CTPPr ppr1 = par1.getCTPPr();

        CTSectPr sec1 = null;
        if (!ppr1.isSetSectPr()) {
            ppr1.addNewSectPr();
        }

        sec1 = ppr1.getSectPr();

        // Create paragraph of the first footer
        CTP parCTP = CTP.Factory.newInstance();
        parCTP.addNewR().addNewT().setStringValue(footer1Text);
        XWPFParagraph footerPar1 = new XWPFParagraph(parCTP, doc);

        XWPFParagraph[] footerPars1 = { footerPar1 };

        XWPFHeaderFooterPolicy pol1 = new XWPFHeaderFooterPolicy(doc, sec1);
        XWPFFooter footer1 = pol1.createFooter(STHdrFtr.DEFAULT, footerPars1);

        // Add second body/section paragraph
        XWPFParagraph par2 = doc.createParagraph();

        XWPFRun run2 = par2.createRun();
        run2.setText("Text for second body paragraph");

        CTPPr ppr2 = par2.getCTPPr();

        CTSectPr sec2 = null;
        if (!ppr2.isSetSectPr()) {
            ppr2.addNewSectPr();
        }

        sec2 = ppr2.getSectPr();

        // Create paragraph of the second footer
        CTP parCTP2 = CTP.Factory.newInstance();
        parCTP2.addNewR().addNewT().setStringValue(footer2Text);
        XWPFParagraph footerPar2 = new XWPFParagraph(parCTP2, doc);

        XWPFParagraph[] footerPars2 = { footerPar2 };

        XWPFHeaderFooterPolicy pol2 = new XWPFHeaderFooterPolicy(doc, sec2);
        XWPFFooter footer2 = pol2.createFooter(STHdrFtr.DEFAULT, footerPars2);

        // Validate the footers are not null
        assertNotNull(footer1.getListParagraph().get(0));
        assertNotNull(footer2.getListParagraph().get(0));

        // Validate the footers are not equal, as objects
        assertNotEquals(footer1, footer2);

        String textFromHeader1 = footer1.getListParagraph().get(0).getCTP().getRArray()[0].getTArray()[0]
                .getStringValue();
        // Validate the text is equal to the text we assigned
        assertEquals(footer1Text, textFromHeader1);

        String textFromHeader2 = footer2.getListParagraph().get(0).getCTP().getRArray()[0].getTArray()[0]
                .getStringValue();
        // Validate the text is equal to the text we assigned
        assertEquals(footer2Text, textFromHeader2);

        // Validate the footers text are not equal
        assertNotEquals(footer1Text, footer2Text);

    }

}
