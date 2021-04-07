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

package org.apache.poi.hssf.extractor;

import static org.apache.poi.POITestCase.assertContains;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.security.Permission;

import org.apache.poi.EmptyFileException;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.POIDataSamples;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.NullPrintStream;
import org.apache.poi.util.RecordFormatException;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the Excel 5/95 and Excel 4 (and older) text
 *  extractor
 */
final class TestOldExcelExtractor {
    private static OldExcelExtractor createExtractor(String sampleFileName) throws IOException {
        File file = HSSFTestDataSamples.getSampleFile(sampleFileName);
        return new OldExcelExtractor(file);
    }

    @Test
    void testSimpleExcel3() throws IOException {
        try (OldExcelExtractor extractor = createExtractor("testEXCEL_3.xls")) {

            // Check we can call getText without error
            String text = extractor.getText();

            // Check we find a few words we expect in there
            assertContains(text, "Season beginning August");
            assertContains(text, "USDA");

            // Check we find a few numbers we expect in there
            assertContains(text, "347");
            assertContains(text, "228");

            // Check we find a few string-literal dates in there
            assertContains(text, "1981/82");

            // Check the type
            assertEquals(3, extractor.getBiffVersion());
            assertEquals(0x10, extractor.getFileType());

        }
    }


    @Test
    void testSimpleExcel3NoReading() throws IOException {
        try (OldExcelExtractor extractor = createExtractor("testEXCEL_3.xls")) {
            assertNotNull(extractor);
        }
    }

    @Test
    void testSimpleExcel4() throws IOException {
        try (OldExcelExtractor extractor = createExtractor("testEXCEL_4.xls")) {

            // Check we can call getText without error
            String text = extractor.getText();

            // Check we find a few words we expect in there
            assertContains(text, "Size");
            assertContains(text, "Returns");

            // Check we find a few numbers we expect in there
            assertContains(text, "11");
            assertContains(text, "784");

            // Check the type
            assertEquals(4, extractor.getBiffVersion());
            assertEquals(0x10, extractor.getFileType());

        }
    }

    @Test
    void testSimpleExcel5() throws IOException {
        for (String ver : new String[] {"5", "95"}) {
            try (OldExcelExtractor extractor = createExtractor("testEXCEL_"+ver+".xls")) {

                // Check we can call getText without error
                String text = extractor.getText();

                // Check we find a few words we expect in there
                assertContains(text, "Sample Excel");
                assertContains(text, "Written and saved");

                // Check we find a few numbers we expect in there
                assertContains(text, "15");
                assertContains(text, "169");

                // Check we got the sheet names (new formats only)
                assertContains(text, "Sheet: Feuil3");

                // Check the type
                assertEquals(5, extractor.getBiffVersion());
                assertEquals(0x05, extractor.getFileType());

            }
        }
    }

    @Test
    void testStrings() throws IOException {
        try (OldExcelExtractor extractor = createExtractor("testEXCEL_4.xls")) {
            String text = extractor.getText();

            // Simple strings
            assertContains(text, "Table 10 -- Examination Coverage:");
            assertContains(text, "Recommended and Average Recommended Additional Tax After");
            assertContains(text, "Individual income tax returns, total");

            // More complicated strings
            assertContains(text, "$100,000 or more");
            assertContains(text, "S corporation returns, Form 1120S [10,15]");
            assertContains(text, "individual income tax return \u201Cshort forms.\u201D");

            // Formula based strings
            // TODO Find some then test
        }
    }

    @Test
    void testFormattedNumbersExcel4() throws IOException {
        try (OldExcelExtractor extractor = createExtractor("testEXCEL_4.xls")) {
            String text = extractor.getText();

            // Simple numbers
            assertContains(text, "151");
            assertContains(text, "784");

            // Numbers which come from formulas
            assertContains(text, "0.398"); // TODO Rounding
            assertContains(text, "624");

            // Formatted numbers
            // TODO
            // assertContains(text, "55,624");
            // assertContains(text, "11,743,477");
        }
    }

    @Test
    void testFormattedNumbersExcel5() throws IOException {
        for (String ver : new String[] {"5", "95"}) {
            try (OldExcelExtractor extractor = createExtractor("testEXCEL_"+ver+".xls")) {
                String text = extractor.getText();

                // Simple numbers
                assertContains(text, "1");

                // Numbers which come from formulas
                assertContains(text, "13");
                assertContains(text, "169");

                // Formatted numbers
                // TODO
                // assertContains(text, "100.00%");
                // assertContains(text, "155.00%");
                // assertContains(text, "1,125");
                // assertContains(text, "189,945");
                // assertContains(text, "1,234,500");
                // assertContains(text, "$169.00");
                // assertContains(text, "$1,253.82");
            }
        }
    }

    @Test
    void testFromFile() throws IOException {
        for (String ver : new String[] {"4", "5", "95"}) {
            String filename = "testEXCEL_"+ver+".xls";
            File f = HSSFTestDataSamples.getSampleFile(filename);

            try (OldExcelExtractor extractor = new OldExcelExtractor(f)) {
                String text = extractor.getText();
                assertNotNull(text);
                assertTrue(text.length() > 100);
            }
        }
    }

    @Test
    void testFromInputStream() throws IOException {
        for (String ver : new String[] {"4", "5", "95"}) {
            String filename = "testEXCEL_"+ver+".xls";
            File f = HSSFTestDataSamples.getSampleFile(filename);

            try (InputStream stream = new FileInputStream(f);
                 OldExcelExtractor extractor = new OldExcelExtractor(stream)) {
                String text = extractor.getText();
                assertNotNull(text);
                assertTrue(text.length() > 100);
            }
        }
    }

    @Test
    void testOpenInvalidFile1() throws IOException {
        // a file that exists, but is a different format
        assertThrows(OfficeXmlFileException.class, () -> createExtractor("WithVariousData.xlsx").close());

        // a completely different type of file
        assertThrows(RecordFormatException.class, () -> createExtractor("48936-strings.txt").close());

        // a POIFS file which is not a Workbook
        try (InputStream is = POIDataSamples.getDocumentInstance().openResourceAsStream("47304.doc")) {
            assertThrows(FileNotFoundException.class, () -> new OldExcelExtractor(is).close());
        }
    }

    @Test
    void testOpenNonExistingFile() {
        // a file that exists, but is a different format
        assertThrows(EmptyFileException.class, () -> new OldExcelExtractor(new File("notexistingfile.xls")).close());
    }

    @Test
    void testInputStream() throws IOException {
        File file = HSSFTestDataSamples.getSampleFile("testEXCEL_3.xls");
        try (InputStream stream = new FileInputStream(file);
             OldExcelExtractor extractor = new OldExcelExtractor(stream)) {
            String text = extractor.getText();
            assertNotNull(text);
        }
    }

    @Test
    void testInputStreamNPOIHeader() throws IOException {
        //TODO: the worksheet names are currently mangled.  They're treated
        //as if UTF-16, but they're just ascii.  Need to fix this.
        //Is it possible that the leading 0 byte in the worksheet name is a signal
        //that these worksheet names should be interpreted as ascii/1252?
        File file = HSSFTestDataSamples.getSampleFile("FormulaRefs.xls");
        try (InputStream stream = new FileInputStream(file);
             OldExcelExtractor extractor = new OldExcelExtractor(stream)) {
            String text = extractor.getText();
            assertNotNull(text);
        }
    }

    @Test
    void testPOIFSFileSystem() throws IOException {
        File file = HSSFTestDataSamples.getSampleFile("FormulaRefs.xls");
        try (POIFSFileSystem fs = new POIFSFileSystem(file);
            OldExcelExtractor extractor = new OldExcelExtractor(fs)){
            String text = extractor.getText();
            assertNotNull(text);
        }
    }

    @Test
    void testDirectoryNode() throws IOException {
        File file = HSSFTestDataSamples.getSampleFile("FormulaRefs.xls");
        try (POIFSFileSystem fs = new POIFSFileSystem(file);
             OldExcelExtractor extractor = new OldExcelExtractor(fs.getRoot())) {
            String text = extractor.getText();
            assertNotNull(text);
        }
    }

    @Test
    void testDirectoryNodeInvalidFile() throws IOException {
        File file = POIDataSamples.getDocumentInstance().getFile("test.doc");
        try (POIFSFileSystem fs = new POIFSFileSystem(file)) {
             assertThrows(FileNotFoundException.class, () -> new OldExcelExtractor(fs.getRoot()));
        }
    }

    @Test
    void testMainUsage() {
        PrintStream save = System.err;
        SecurityManager sm = System.getSecurityManager();
        System.setSecurityManager(new NoExitSecurityManager());
        try {
            System.setErr(new NullPrintStream());
            // calls System.exit()
            assertThrows(ExitException.class, () -> OldExcelExtractor.main(new String[]{}));
        } finally {
            System.setSecurityManager(sm);
            System.setErr(save);
        }
    }

    @Test
    void testMain() throws IOException {
        File file = HSSFTestDataSamples.getSampleFile("testEXCEL_3.xls");
        PrintStream save = System.out;
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PrintStream str = new PrintStream(out, false, "UTF-8");
            System.setOut(str);
            OldExcelExtractor.main(new String[] {file.getAbsolutePath()});
            String string = out.toString("UTF-8");
            assertTrue(string.contains("Table C-13--Lemons"), "Had: " + string);
        } finally {
            System.setOut(save);
        }
    }

    @Test
    void testEncryptionException() throws IOException {
        //test file derives from Common Crawl
        File file = HSSFTestDataSamples.getSampleFile("60284.xls");

        try (OldExcelExtractor ex = new OldExcelExtractor(file)) {
            assertEquals(5, ex.getBiffVersion());
            assertEquals(5, ex.getFileType());
            assertThrows(EncryptedDocumentException.class, ex::getText);
        }
    }

    @Test
    void testSheetWithNoName() throws IOException {
        File file = HSSFTestDataSamples.getSampleFile("64130.xls");

        try (OldExcelExtractor ex = new OldExcelExtractor(file)) {
            assertEquals(5, ex.getBiffVersion());
            assertEquals(5, ex.getFileType());
            assertContains(ex.getText(), "Dawn");
        }
    }

    private static class NoExitSecurityManager extends SecurityManager {
        @Override
        public void checkPermission(Permission perm) {
            // allow anything.
        }
        @Override
        public void checkPermission(Permission perm, Object context) {
            // allow anything.
        }
        @Override
        public void checkExit(int status) {
            super.checkExit(status);
            throw new ExitException(status);
        }
    }

    private static class ExitException extends SecurityException {
        public final int status;
        public ExitException(int status) {
            super("There is no escape!");
            this.status = status;
        }
    }
}
