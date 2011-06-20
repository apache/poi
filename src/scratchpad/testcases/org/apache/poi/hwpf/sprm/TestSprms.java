/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.hwpf.sprm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;
import org.apache.poi.POIDataSamples;
import org.apache.poi.hwpf.HWPFDocument;

public class TestSprms extends TestCase {
    /**
     * Test correct processing of "sprmPJc" by uncompressor
     */
    public void testSprmPJc() throws IOException {
        InputStream resourceAsStream = POIDataSamples.getDocumentInstance()
                .openResourceAsStream("Bug49820.doc");
        HWPFDocument hwpfDocument = new HWPFDocument(resourceAsStream);
        assertEquals(1, hwpfDocument.getStyleSheet().getParagraphStyle(8)
                .getJustification());
        resourceAsStream.close();
    }

    /**
     * Test correct processing of "sprmPJc" by compressor and uncompressor
     */
    public void testSprmPJcResave() throws IOException {
        InputStream resourceAsStream = POIDataSamples.getDocumentInstance()
                .openResourceAsStream("Bug49820.doc");
        HWPFDocument hwpfDocument = new HWPFDocument(resourceAsStream);
        resourceAsStream.close();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        hwpfDocument.write(baos);
        hwpfDocument = new HWPFDocument(
                new ByteArrayInputStream(baos.toByteArray()));

        assertEquals(1, hwpfDocument.getStyleSheet().getParagraphStyle(8)
                .getJustification());
    }
}
