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
package org.apache.poi.stress;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.hssf.record.crypto.Biff8EncryptionKey;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OLE2NotOfficeXmlFileException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.crypt.Decryptor;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.eventusermodel.XLSX2CSV;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.examples.FromHowTo;
import org.apache.poi.xssf.extractor.XSSFExportToXml;
import org.apache.poi.xssf.usermodel.XSSFMap;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;
import org.xml.sax.SAXException;

public class XSSFFileHandler extends SpreadsheetHandler {
    @Override
    public void handleFile(InputStream stream, String path) throws Exception {
        // ignore password protected files if password is unknown
        String pass = Biff8EncryptionKey.getCurrentUserPassword();
        assumeFalse(pass == null && POIXMLDocumentHandler.isEncrypted(stream));

        final XSSFWorkbook wb;

        // make sure the potentially large byte-array is freed up quickly again
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            IOUtils.copy(stream, out);
            ByteArrayInputStream bytes = new ByteArrayInputStream(out.toByteArray());

            if (pass != null) {
                POIFSFileSystem poifs = new POIFSFileSystem(bytes);
                EncryptionInfo ei = new EncryptionInfo(poifs);
                Decryptor dec = ei.getDecryptor();
                try {
                    boolean b = dec.verifyPassword(pass);
                    assertTrue("password mismatch", b);
                } catch (EncryptedDocumentException e) {
                    String msg = "Export Restrictions in place - please install JCE Unlimited Strength Jurisdiction Policy files";
                    assumeFalse(msg.equals(e.getMessage()));
                    throw e;
                }
                InputStream is = dec.getDataStream(poifs);
                out.reset();
                IOUtils.copy(is, out);
                is.close();
                poifs.close();
                bytes = new ByteArrayInputStream(out.toByteArray());
            }
            checkXSSFReader(OPCPackage.open(bytes));
            bytes.reset();
            wb = new XSSFWorkbook(bytes);
        }

        // use the combined handler for HSSF/XSSF
        handleWorkbook(wb);
        
        // TODO: some documents fail currently...
        //XSSFFormulaEvaluator evaluator = new XSSFFormulaEvaluator(wb);
        //evaluator.evaluateAll();

        // also verify general POIFS-stuff
        new POIXMLDocumentHandler().handlePOIXMLDocument(wb);
        
        // and finally ensure that exporting to XML works
        exportToXML(wb);

        // this allows to trigger a heap-dump at this point to see which memory is still allocated
        //HeapDump.dumpHeap("/tmp/poi.hprof", false);
        
        wb.close();
    }


    private void checkXSSFReader(OPCPackage p) throws IOException, OpenXML4JException {
        XSSFReader reader = new XSSFReader(p);
        
        // these can be null...
        InputStream sharedStringsData = reader.getSharedStringsData();
        if(sharedStringsData != null) {
            sharedStringsData.close();
        }
        reader.getSharedStringsTable();

        InputStream stylesData = reader.getStylesData();
        if(stylesData != null) {
            stylesData.close();
        }
        reader.getStylesTable();
        
        InputStream themesData = reader.getThemesData();
        if(themesData != null) {
            themesData.close();
        }

        assertNotNull(reader.getWorkbookData());
        
        Iterator<InputStream> sheetsData = reader.getSheetsData();
        while(sheetsData.hasNext()) {
            InputStream str = sheetsData.next();
            str.close();
        }
    }
    
    private void exportToXML(XSSFWorkbook wb) throws SAXException,
            ParserConfigurationException, TransformerException {
        for (XSSFMap map : wb.getCustomXMLMappings()) {
            XSSFExportToXml exporter = new XSSFExportToXml(map);

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            exporter.exportToXML(os, true);
        }
    }

    private static final Set<String> EXPECTED_ADDITIONAL_FAILURES = new HashSet<>();
    static {
        // expected sheet-id not found
        // EXPECTED_ADDITIONAL_FAILURES.add("spreadsheet/52348.xlsx");
        // EXPECTED_ADDITIONAL_FAILURES.add("spreadsheet/59021.xlsx");
        // zip-bomb
        EXPECTED_ADDITIONAL_FAILURES.add("spreadsheet/54764.xlsx");
        EXPECTED_ADDITIONAL_FAILURES.add("spreadsheet/54764-2.xlsx");
        EXPECTED_ADDITIONAL_FAILURES.add("spreadsheet/54764.xlsx");
        EXPECTED_ADDITIONAL_FAILURES.add("spreadsheet/poc-xmlbomb.xlsx");
        EXPECTED_ADDITIONAL_FAILURES.add("spreadsheet/poc-xmlbomb-empty.xlsx");
        // strict OOXML
        EXPECTED_ADDITIONAL_FAILURES.add("spreadsheet/57914.xlsx");
        EXPECTED_ADDITIONAL_FAILURES.add("spreadsheet/SampleSS.strict.xlsx");
        EXPECTED_ADDITIONAL_FAILURES.add("spreadsheet/SimpleStrict.xlsx");
        EXPECTED_ADDITIONAL_FAILURES.add("spreadsheet/sample.strict.xlsx");
        // TODO: good to ignore?
        EXPECTED_ADDITIONAL_FAILURES.add("spreadsheet/sample-beta.xlsx");

        // corrupt/invalid
        EXPECTED_ADDITIONAL_FAILURES.add("openxml4j/invalid.xlsx");
    }

    @SuppressWarnings("resource")
    @Override
    public void handleAdditional(File file) throws Exception {
        // redirect stdout as the examples often write lots of text
        PrintStream oldOut = System.out;
        try {
            System.setOut(new NullPrintStream());
            FromHowTo.main(new String[]{file.getAbsolutePath()});
            XLSX2CSV.main(new String[]{file.getAbsolutePath()});

            assertFalse("Expected Extraction to fail for file " + file + " and handler " + this + ", but did not fail!",
                    EXPECTED_ADDITIONAL_FAILURES.contains(file.getParentFile().getName() + "/" + file.getName()));

        } catch (OLE2NotOfficeXmlFileException e) {
            // we have some files that are not actually OOXML and thus cannot be tested here
        } catch (IllegalArgumentException | InvalidFormatException | POIXMLException | IOException e) {
            if(!EXPECTED_ADDITIONAL_FAILURES.contains(file.getParentFile().getName() + "/" + file.getName())) {
                throw e;
            }
        } finally {
            System.setOut(oldOut);
        }
    }

    // a test-case to test this locally without executing the full TestAllFiles
    @Test
    public void test() throws Exception {
        File file = new File("test-data/spreadsheet/ref-56737.xlsx");

        try (InputStream stream = new BufferedInputStream(new FileInputStream(file))) {
            handleFile(stream, file.getPath());
        }

        handleExtracting(file);
    }

    @Test
    public void testAdditional() throws Exception {
        handleAdditional(new File("test-data/spreadsheet/poc-xmlbomb.xlsx"));
    }
    
    // need to override all methods to omit calls to UTF-handling methods 
    static class NullPrintStream extends PrintStream {
        @SuppressWarnings("resource")
        NullPrintStream() {
            super(new OutputStream() {
                @Override
                public void write(int b) {}
                @Override
                public void write(byte[] b) {}
                @Override
                public void write(byte[] b, int off, int len) {}
            });
        }
        @Override
        public void write(int b) {}
        @Override
        public void write(byte[] buf, int off, int len) {}
        @Override
        public void print(boolean b) {}
        @Override
        public void print(char c) {}
        @Override
        public void print(int i) {}
        @Override
        public void print(long l) {}
        @Override
        public void print(float f) {}
        @Override
        public void print(double d) {}
        @Override
        public void print(char[] s) {}
        @Override
        public void print(String s) {}
        @Override
        public void print(Object obj) {}
        @Override
        public void println() {}
        @Override
        public void println(boolean x) {}
        @Override
        public void println(char x) {}
        @Override
        public void println(int x) {}
        @Override
        public void println(long x) {}
        @Override
        public void println(float x) {}
        @Override
        public void println(double x) {}
        @Override
        public void println(char[] x) {}
        @Override
        public void println(String x) {}
        @Override
        public void println(Object x) {}
        @Override
        public PrintStream printf(String format, Object... args) { return this; }
        @Override
        public PrintStream printf(Locale l, String format, Object... args) { return this; }
        @Override
        public PrintStream format(String format, Object... args) { return this; }
        @Override
        public PrintStream format(Locale l, String format, Object... args) { return this; }
        @Override
        public PrintStream append(CharSequence csq) { return this; }
        @Override
        public PrintStream append(CharSequence csq, int start, int end) { return this; }
        @Override
        public PrintStream append(char c) { return this; }
        @Override
        public void write(byte[] b) {}
    }
}
