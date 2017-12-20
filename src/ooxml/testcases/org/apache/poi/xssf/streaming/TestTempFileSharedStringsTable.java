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

package org.apache.poi.xssf.streaming;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.apache.poi.util.DocumentHelper;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class TestTempFileSharedStringsTable {
    @Test
    public void testWriteOut() throws Exception {
        try (TempFileSharedStringsTable sst = new TempFileSharedStringsTable(new SXSSFWorkbook.Builder())) {
            sst.addSharedStringItem(new XSSFRichTextString("First string"));
            sst.addSharedStringItem(new XSSFRichTextString("First string"));
            sst.addSharedStringItem(new XSSFRichTextString("First string"));
            sst.addSharedStringItem(new XSSFRichTextString("Second string"));
            sst.addSharedStringItem(new XSSFRichTextString("Second string"));
            sst.addSharedStringItem(new XSSFRichTextString("Second string"));
            XSSFRichTextString rts = new XSSFRichTextString("Second string");
            XSSFFont font = new XSSFFont();
            font.setFontName("Arial");
            font.setBold(true);
            rts.applyFont(font);
            sst.addSharedStringItem(rts);
            Assert.assertEquals(3, sst.getUniqueCount());
            Assert.assertEquals(7, sst.getCount());
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                sst.writeTo(bos);
                DocumentHelper.newDocumentBuilder().parse(new ByteArrayInputStream(bos.toByteArray()));
            }
        }
    }

    @Ignore("temporary test")
    @Test
    public void testLargeData() throws Exception {
        java.util.Random rnd = new java.util.Random();
        byte[] bytes = new byte[640];
        try (TempFileSharedStringsTable sst = new TempFileSharedStringsTable(new SXSSFWorkbook.Builder())) {
            for (int i = 0; i < 1000; i++) {
                rnd.nextBytes(bytes);
                String rndString = java.util.Base64.getEncoder().encodeToString(bytes);
                sst.addSharedStringItem(new XSSFRichTextString(rndString));
            }
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream("sst.txt")) {
                sst.writeTo(fos);
            }
        }
    }
}
