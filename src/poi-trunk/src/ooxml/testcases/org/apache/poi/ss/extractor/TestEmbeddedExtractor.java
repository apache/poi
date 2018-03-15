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

package org.apache.poi.ss.extractor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.POIDataSamples;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.Test;

public class TestEmbeddedExtractor {
    private static final POIDataSamples samples = POIDataSamples.getSpreadSheetInstance();

    @Test
    public void extractPDFfromEMF() throws Exception {
        InputStream fis = samples.openResourceAsStream("Basic_Expense_Template_2011.xls");
        Workbook wb = WorkbookFactory.create(fis);
        fis.close();

        EmbeddedExtractor ee = new EmbeddedExtractor();
        List<EmbeddedData> edList = new ArrayList<>();
        for (Sheet s : wb) {
            edList.addAll(ee.extractAll(s));
        }
        wb.close();

        assertEquals(2, edList.size());

        String filename1 = "Sample.pdf";
        EmbeddedData ed0 = edList.get(0);
        assertEquals(filename1, ed0.getFilename());
        assertEquals(filename1, ed0.getShape().getShapeName().trim());
        assertEquals("uNplB1QpYug+LWappiTh0w==", md5hash(ed0.getEmbeddedData()));

        String filename2 = "kalastuslupa_jiyjhnj_yuiyuiyuio_uyte_sldfsdfsdf_sfsdfsdf_sfsssfsf_sdfsdfsdfsdf_sdfsdfsdf.pdf";
        EmbeddedData ed1 = edList.get(1);
        assertEquals(filename2, ed1.getFilename());
        assertEquals(filename2, ed1.getShape().getShapeName().trim());
        assertEquals("QjLuAZ+cd7KbhVz4sj+QdA==", md5hash(ed1.getEmbeddedData()));
    }

    @Test
    public void extractFromXSSF() throws IOException, EncryptedDocumentException, InvalidFormatException {
        InputStream fis = samples.openResourceAsStream("58325_db.xlsx");
        Workbook wb = WorkbookFactory.create(fis);
        fis.close();

        EmbeddedExtractor ee = new EmbeddedExtractor();
        List<EmbeddedData> edList = new ArrayList<>();
        for (Sheet s : wb) {
            edList.addAll(ee.extractAll(s));
        }
        wb.close();

        assertEquals(4, edList.size());
        EmbeddedData ed0 = edList.get(0);
        assertEquals("Object 1.pdf", ed0.getFilename());
        assertEquals("Object 1", ed0.getShape().getShapeName().trim());
        assertEquals("Oyys6UtQU1gbHYBYqA4NFA==", md5hash(ed0.getEmbeddedData()));

        EmbeddedData ed1 = edList.get(1);
        assertEquals("Object 2.pdf", ed1.getFilename());
        assertEquals("Object 2", ed1.getShape().getShapeName().trim());
        assertEquals("xLScPUS0XH+5CTZ2A3neNw==", md5hash(ed1.getEmbeddedData()));

        EmbeddedData ed2 = edList.get(2);
        assertEquals("Object 3.pdf", ed2.getFilename());
        assertEquals("Object 3", ed2.getShape().getShapeName().trim());
        assertEquals("rX4klZqJAeM5npb54Gi2+Q==", md5hash(ed2.getEmbeddedData()));

        EmbeddedData ed3 = edList.get(3);
        assertEquals("Microsoft_Excel_Worksheet1.xlsx", ed3.getFilename());
        assertEquals("Object 1", ed3.getShape().getShapeName().trim());
        assertEquals("4m4N8ji2tjpEGPQuw2YwGA==", md5hash(ed3.getEmbeddedData()));
    }

    public static String md5hash(byte[] input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte hash[] = md.digest(input);
            return DatatypeConverter.printBase64Binary(hash);
        } catch (NoSuchAlgorithmException e) {
            // doesn't happen
            throw new RuntimeException(e);
        }
    }


    @Test
    public void testNPE() throws IOException {
        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("angelo.edu_content_files_19555-nsse-2011-multiyear-benchmark.xls");
        EmbeddedExtractor ee = new EmbeddedExtractor();

        for (Sheet s : wb) {
            for (EmbeddedData ed : ee.extractAll(s)) {
                assertNotNull(ed.getFilename());
                assertNotNull(ed.getEmbeddedData());
                assertNotNull(ed.getShape());
            }
        }

    }
}
