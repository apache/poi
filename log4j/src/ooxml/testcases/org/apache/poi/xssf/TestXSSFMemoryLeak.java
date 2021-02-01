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

package org.apache.poi.xssf;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.util.MemoryLeakVerifier;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCell;

/**
 * A test which uses {@link MemoryLeakVerifier} to ensure that certain
 * objects are not left over in memory after the test.
 *
 * E.g. verifies that objects are freed when stuff is removed from sheets or rows
 */
public class TestXSSFMemoryLeak {
    private final MemoryLeakVerifier verifier = new MemoryLeakVerifier();

    // keep some items in memory, so checks in tearDown() actually
    // verify that they do not keep certain objects in memory,
    // e.g. nested CT... objects which should be released
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final List<Object> references = new ArrayList<>();

    @AfterEach
    void tearDown() {
        verifier.assertGarbageCollected();
    }

    @Test
    void testWriteRow() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            final XSSFSheet sheet1 = wb.createSheet("Sheet1");
            final XSSFRow row = sheet1.createRow(0);
            final XSSFCell cell = row.createCell(0);
            cell.setCellValue("hello");

            // Cannot check the CTCell here as it is reused now and thus
            // not freed until we free up the Cell itself
            //verifier.addObject(ctCell);

            try (OutputStream out = new ByteArrayOutputStream(8192)) {
                wb.write(out);
            }

            CTCell ctCell = cell.getCTCell();
            assertSame(cell.getCTCell(), ctCell, "The CTCell should not be replaced");
            assertSame(row.getCTRow().getCArray(0), ctCell, "The CTCell in the row should not be replaced");
        }
    }

    @Test
    void testRemoveCellFromRow() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            final XSSFSheet sheet1 = wb.createSheet("Sheet1");
            final XSSFRow rowToCheck = sheet1.createRow(0);
            references.add(rowToCheck);

            XSSFCell cell = rowToCheck.createCell(0);
            cell.setCellValue("hello");

            // previously the CTCell was still referenced in the CTRow, verify that it is freed
            verifier.addObject(cell);
            verifier.addObject(cell.getCTCell());

            rowToCheck.removeCell(cell);

            assertNull(rowToCheck.getCell(0));
        }
    }

    @Test
    void testRemove2CellsFromRow() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            final XSSFSheet sheet1 = wb.createSheet("Sheet1");
            final XSSFRow rowToCheck = sheet1.createRow(0);
            references.add(rowToCheck);

            XSSFCell cell1 = rowToCheck.createCell(0);
            cell1.setCellValue("hello");
            XSSFCell cell2 = rowToCheck.createCell(1);
            cell2.setCellValue("world");

            // previously the CTCell was still referenced in the CTRow, verify that it is freed
            verifier.addObject(cell1);
            verifier.addObject(cell1.getCTCell());
            verifier.addObject(cell2);
            verifier.addObject(cell2.getCTCell());

            rowToCheck.removeCell(cell2);
            rowToCheck.removeCell(cell1);

            assertNull(rowToCheck.getCell(0));
            assertNull(rowToCheck.getCell(1));
        }
    }

    @Test
    void testRemoveRowFromSheet() throws IOException {
        try (XSSFWorkbook wb1 = new XSSFWorkbook()) {
            final XSSFSheet sheetToCheck = wb1.createSheet("Sheet1");
            references.add(sheetToCheck);
            final XSSFRow row = sheetToCheck.createRow(0);
            final XSSFCell cell = row.createCell(0);
            cell.setCellValue(1);

            // ensure that the row-data is not kept somewhere in another member
            verifier.addObject(row.getCTRow());
            verifier.addObject(row);
            verifier.addObject(cell.getCTCell());
            verifier.addObject(cell);

            sheetToCheck.removeRow(row);

            assertNull(sheetToCheck.getRow(0));
        }
    }

    @Test
    void testFileLeak() {
        File file = XSSFTestDataSamples.getSampleFile("xlsx-corrupted.xlsx");
        verifier.addObject(file);
        assertThrows(POIXMLException.class, () -> new XSSFWorkbook(file), "Should catch exception as the file is corrupted");
    }

    @Test
    void testFileLeak2() throws IOException, InvalidFormatException {
        File file = XSSFTestDataSamples.getSampleFile("xlsx-corrupted.xlsx");
        verifier.addObject(file);
        try (OPCPackage pkg = OPCPackage.open(file)) {
            assertThrows(POIXMLException.class, () -> new XSSFWorkbook(pkg), "Should catch exception as the file is corrupted");
        }
    }
}
