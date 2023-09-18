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

package org.apache.poi.openxml4j.opc;

import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.poi.ooxml.util.DocumentHelper;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.apache.poi.xssf.usermodel.XSSFRelation.NS_WORDPROCESSINGML;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestStreamHelper {
    @Test
    void testStandaloneFlag() throws IOException {
        Document doc = DocumentHelper.createDocument();
        Element elDocument = doc.createElementNS(NS_WORDPROCESSINGML, "w:document");
        doc.appendChild(elDocument);
        Element elBody = doc.createElementNS(NS_WORDPROCESSINGML, "w:body");
        elDocument.appendChild(elBody);
        Element elParagraph = doc.createElementNS(NS_WORDPROCESSINGML, "w:p");
        elBody.appendChild(elParagraph);
        Element elRun = doc.createElementNS(NS_WORDPROCESSINGML, "w:r");
        elParagraph.appendChild(elRun);
        Element elText = doc.createElementNS(NS_WORDPROCESSINGML, "w:t");
        elRun.appendChild(elText);
        elText.setTextContent("Hello Open XML !");

        try (UnsynchronizedByteArrayOutputStream bos = new UnsynchronizedByteArrayOutputStream()) {
            StreamHelper.saveXmlInStream(doc, bos);
            String xml = bos.toString(StandardCharsets.UTF_8);
            assertTrue(xml.contains("standalone=\"yes\""), "xml contains standalone=yes?");
            assertTrue(xml.contains("encoding=\"UTF-8\""), "xml contains encoding=UTF-8?");
        }
    }
}
