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

import java.io.IOException;

import org.apache.poi.POIDataSamples;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xdgf.usermodel.XmlVisioDocument;

import junit.framework.TestCase;

public class TestXDGFVisioExtractor extends TestCase {

    private POIDataSamples diagrams;
    private OPCPackage pkg;
    private XmlVisioDocument xml;

    protected void setUp() throws Exception {
        diagrams = POIDataSamples.getDiagramInstance();
        
        pkg = OPCPackage.open(diagrams.openResourceAsStream("test_text_extraction.vsdx"));
        xml = new XmlVisioDocument(pkg);
    }

    public void testGetSimpleText() throws IOException {
        new XDGFVisioExtractor(xml).close();
        new XDGFVisioExtractor(pkg).close();
        
        XDGFVisioExtractor extractor = new XDGFVisioExtractor(xml);
        extractor.getText();
        
        String text = extractor.getText();
        assertTrue(text.length() > 0);
        
        assertEquals("Text here\nText there\nText, text, everywhere!\nRouter here\n",
                     text);
        
        extractor.close();
    }
}
