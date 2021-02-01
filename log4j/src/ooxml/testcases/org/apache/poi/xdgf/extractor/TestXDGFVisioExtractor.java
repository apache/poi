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
package org.apache.poi.xdgf.extractor;

import static org.apache.poi.POITestCase.assertContains;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.POIDataSamples;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xdgf.usermodel.XmlVisioDocument;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestXDGFVisioExtractor {

    private static final POIDataSamples SAMPLES = POIDataSamples.getDiagramInstance();
    private OPCPackage pkg;
    private XmlVisioDocument xml;

    @BeforeEach
    void setUp() throws Exception {
        pkg = OPCPackage.open(SAMPLES.openResourceAsStream("test_text_extraction.vsdx"));
        xml = new XmlVisioDocument(pkg);
    }

    @AfterEach
    void closeResources() throws IOException {
        if(xml != null) {
            xml.close();
        }
        pkg.close();
    }

    @Test
    void testGetSimpleText() throws IOException {
        new XDGFVisioExtractor(xml).close();
        new XDGFVisioExtractor(pkg).close();

        XDGFVisioExtractor extractor = new XDGFVisioExtractor(xml);
        extractor.getText();

        String text = extractor.getText();
        String expected = "Text here\nText there\nText, text, everywhere!\nRouter here\n";
        assertEquals(expected, text);

        extractor.close();
    }


    //the point of this is to trigger the addition of
    //some common visio classes -- ConnectsType
    @Test
    void testVisioConnects() throws IOException {
        InputStream is = SAMPLES.openResourceAsStream("60489.vsdx");
        XmlVisioDocument document = new XmlVisioDocument(is);
        is.close();
        XDGFVisioExtractor extractor = new XDGFVisioExtractor(document);
        String text = extractor.getText();
        assertContains(text, "Arrears");
        extractor.close();
    }

    /**
     * Some confusion on PolylineTo vs PolyLineTo, both should be handled.
     * Previously failed with:
     * org.apache.poi.ooxml.POIXMLException: Invalid 'Row_Type' name 'PolylineTo'
     *  at org.apache.poi.xdgf.util.ObjectFactory.load
     *  at org.apache.poi.xdgf.usermodel.section.geometry.GeometryRowFactory.load
     */
    @Test
    void testPolylineTo() throws IOException {
        InputStream is = SAMPLES.openResourceAsStream("60973.vsdx");
        XmlVisioDocument document = new XmlVisioDocument(is);
        is.close();
        XDGFVisioExtractor extractor = new XDGFVisioExtractor(document);
        String text = extractor.getText();
        assertContains(text, "42 U");
        assertContains(text, "Access VLANS");
        extractor.close();
    }
}
