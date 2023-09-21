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

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.poi.ooxml.util.DocumentHelper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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

        try (UnsynchronizedByteArrayOutputStream bos = UnsynchronizedByteArrayOutputStream.builder().get()) {
            StreamHelper.saveXmlInStream(doc, bos);
            String xml = bos.toString(StandardCharsets.UTF_8);
            assertTrue(xml.contains("standalone=\"yes\""), "xml contains standalone=yes?");
            assertTrue(xml.contains("encoding=\"UTF-8\""), "xml contains encoding=UTF-8?");
        }
    }

    @Test
    void testXSSF() throws IOException {
        try(
                XSSFWorkbook workbook = new XSSFWorkbook();
                UnsynchronizedByteArrayOutputStream bos = UnsynchronizedByteArrayOutputStream.builder().get()
        ) {
            XSSFSheet sheet = workbook.createSheet("testsheet");
            Cell cell = sheet.createRow(0).createCell(0);
            cell.setCellValue("test-value");
            CellStyle style = workbook.createCellStyle();
            style.setWrapText(true);
            cell.setCellStyle(style);
            workbook.write(bos);
            try(ZipArchiveInputStream zis = new ZipArchiveInputStream(bos.toInputStream())) {
                ArchiveEntry entry;
                final int maxSize = 1024 * 1024;
                while((entry = zis.getNextEntry()) != null) {
                    final int entrySize = (int) entry.getSize();
                    final byte[] data = (entrySize == -1) ? IOUtils.toByteArrayWithMaxLength(zis, maxSize) :
                            IOUtils.toByteArray(zis, entrySize, maxSize);
                    final String str = new String(data, StandardCharsets.UTF_8);
                    if (str.contains("standalone")) {
                        assertTrue(str.contains("standalone=\"yes\""), "unexpected XML standalone flag in " + entry.getName());
                    }
                }
            }
        }
    }

    @Test
    void testSXSSF() throws IOException {
        try(
                SXSSFWorkbook workbook = new SXSSFWorkbook();
                UnsynchronizedByteArrayOutputStream bos = UnsynchronizedByteArrayOutputStream.builder().get()
        ) {
            SXSSFSheet sheet = workbook.createSheet("testsheet");
            Cell cell = sheet.createRow(0).createCell(0);
            cell.setCellValue("test-value");
            CellStyle style = workbook.createCellStyle();
            style.setWrapText(true);
            cell.setCellStyle(style);
            workbook.write(bos);
            try(ZipArchiveInputStream zis = new ZipArchiveInputStream(bos.toInputStream())) {
                ArchiveEntry entry;
                final int maxSize = 1024 * 1024;
                while((entry = zis.getNextEntry()) != null) {
                    final int entrySize = (int) entry.getSize();
                    final byte[] data = (entrySize == -1) ? IOUtils.toByteArrayWithMaxLength(zis, maxSize) :
                            IOUtils.toByteArray(zis, entrySize, maxSize);
                    final String str = new String(data, StandardCharsets.UTF_8);
                    if (str.contains("standalone")) {
                        assertTrue(str.contains("standalone=\"yes\""), "unexpected XML standalone flag in " + entry.getName());
                    }
                }
            }
        }
    }
}
