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

package org.apache.poi.xssf.streaming;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.util.ZipEntrySource;
import org.apache.poi.poifs.crypt.temp.AesZipFileZipEntrySource;
import org.apache.poi.poifs.crypt.temp.EncryptedTempData;
import org.apache.poi.poifs.crypt.temp.SXSSFWorkbookWithCustomZipEntrySource;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

/**
 * This class tests that an SXSSFWorkbook can be written and read where all temporary disk I/O
 * is encrypted, but the final saved workbook is not encrypted
 */
public final class TestSXSSFWorkbookWithCustomZipEntrySource {

    final String sheetName = "TestSheet1";
    final String cellValue = "customZipEntrySource";

    // write an unencrypted workbook to disk, but any temporary files are encrypted
    @Test
    void customZipEntrySource() throws IOException {
        SXSSFWorkbookWithCustomZipEntrySource workbook = new SXSSFWorkbookWithCustomZipEntrySource();
        SXSSFSheet sheet1 = workbook.createSheet(sheetName);
        SXSSFRow row1 = sheet1.createRow(1);
        SXSSFCell cell1 = row1.createCell(1);
        cell1.setCellValue(cellValue);
        ByteArrayOutputStream os = new ByteArrayOutputStream(8192);
        workbook.write(os);
        workbook.close();
        workbook.dispose();
        XSSFWorkbook xwb = new XSSFWorkbook(new ByteArrayInputStream(os.toByteArray()));
        XSSFSheet xs1 = xwb.getSheetAt(0);
        assertEquals(sheetName, xs1.getSheetName());
        XSSFRow xr1 = xs1.getRow(1);
        XSSFCell xc1 = xr1.getCell(1);
        assertEquals(cellValue, xc1.getStringCellValue());
        xwb.close();
    }

    // write an encrypted workbook to disk, and encrypt any temporary files as well
    @Test
    void customZipEntrySourceForWriteAndRead() throws IOException, InvalidFormatException {
        SXSSFWorkbookWithCustomZipEntrySource workbook = new SXSSFWorkbookWithCustomZipEntrySource();
        SXSSFSheet sheet1 = workbook.createSheet(sheetName);
        SXSSFRow row1 = sheet1.createRow(1);
        SXSSFCell cell1 = row1.createCell(1);
        cell1.setCellValue(cellValue);
        EncryptedTempData tempData = new EncryptedTempData();
        try (OutputStream os = tempData.getOutputStream()) {
            workbook.write(os);
        }
        workbook.close();
        workbook.dispose();
        try (InputStream is = tempData.getInputStream();
             ZipEntrySource zipEntrySource = AesZipFileZipEntrySource.createZipEntrySource(is)) {
            tempData.dispose();
            try (OPCPackage opc = OPCPackage.open(zipEntrySource);
                XSSFWorkbook xwb = new XSSFWorkbook(opc)) {
                XSSFSheet xs1 = xwb.getSheetAt(0);
                assertEquals(sheetName, xs1.getSheetName());
                XSSFRow xr1 = xs1.getRow(1);
                XSSFCell xc1 = xr1.getCell(1);
                assertEquals(cellValue, xc1.getStringCellValue());
            }
        }
    }

    @Test
    void validateTempFilesAreEncrypted() throws IOException {
        TempFileRecordingSXSSFWorkbookWithCustomZipEntrySource workbook = new TempFileRecordingSXSSFWorkbookWithCustomZipEntrySource();
        SXSSFSheet sheet1 = workbook.createSheet(sheetName);
        SXSSFRow row1 = sheet1.createRow(1);
        SXSSFCell cell1 = row1.createCell(1);
        cell1.setCellValue(cellValue);
        ByteArrayOutputStream os = new ByteArrayOutputStream(8192);
        workbook.write(os);
        workbook.close();
        List<File> tempFiles = workbook.getTempFiles();
        assertEquals(1, tempFiles.size());
        File tempFile = tempFiles.get(0);
        assertTrue(tempFile.exists(), "tempFile exists?");
        try (InputStream stream = new FileInputStream(tempFile)) {
            byte[] data = IOUtils.toByteArray(stream);
            String text = new String(data, UTF_8);
            assertFalse(text.contains(sheetName));
            assertFalse(text.contains(cellValue));
        }
        workbook.dispose();
        assertFalse(tempFile.exists(), "tempFile deleted after dispose?");
    }
}