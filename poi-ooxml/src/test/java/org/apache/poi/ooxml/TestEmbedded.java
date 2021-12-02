
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


package org.apache.poi.ooxml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.poi.POIDataSamples;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xslf.usermodel.XSLFSlideShow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;

/**
 * Class to test that we handle embedded bits in OOXML files properly
 */
class TestEmbedded {
    @Test
    void testExcel() throws Exception {
        POIXMLDocument doc = new XSSFWorkbook(
                POIDataSamples.getSpreadSheetInstance().openResourceAsStream("ExcelWithAttachments.xlsm")
        );
        test(doc, 4);
    }

    @Test
    void testWord() throws Exception {
        POIXMLDocument doc = new XWPFDocument(
                POIDataSamples.getDocumentInstance().openResourceAsStream("WordWithAttachments.docx")
        );
        test(doc, 5);
    }

    @Test
    void testPowerPoint() throws Exception {
        POIXMLDocument doc = new XSLFSlideShow(OPCPackage.open(
                POIDataSamples.getSlideShowInstance().openResourceAsStream("PPTWithAttachments.pptm"))
        );
        test(doc, 4);
    }

    private void test(POIXMLDocument doc, int expectedCount) throws Exception {
        assertNotNull(doc.getAllEmbeddedParts());
        assertEquals(expectedCount, doc.getAllEmbeddedParts().size());

        for(int i=0; i<doc.getAllEmbeddedParts().size(); i++) {
            PackagePart pp = doc.getAllEmbeddedParts().get(i);
            assertNotNull(pp);

            byte[] b = IOUtils.toByteArray(pp.getInputStream());
            assertTrue(b.length > 0);
        }

        doc.close();
    }
}
