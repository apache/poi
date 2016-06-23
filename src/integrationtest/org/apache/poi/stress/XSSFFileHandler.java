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

import java.io.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.poi.POIXMLException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OLE2NotOfficeXmlFileException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
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
    public void handleFile(InputStream stream) throws Exception {
        // ignore password protected files
        if (POIXMLDocumentHandler.isEncrypted(stream)) return;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.copy(stream, out);

        final byte[] bytes = out.toByteArray();
        final XSSFWorkbook wb;
        wb = new XSSFWorkbook(new ByteArrayInputStream(bytes));

        // use the combined handler for HSSF/XSSF
        handleWorkbook(wb);
        
        // TODO: some documents fail currently...
        //XSSFFormulaEvaluator evaluator = new XSSFFormulaEvaluator(wb);
        //evaluator.evaluateAll();

        // also verify general POIFS-stuff
        new POIXMLDocumentHandler().handlePOIXMLDocument(wb);
        
        // and finally ensure that exporting to XML works
        exportToXML(wb);

        checkXSSFReader(OPCPackage.open(new ByteArrayInputStream(bytes)));
        
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

    private static final Set<String> EXPECTED_ADDITIONAL_FAILURES = new HashSet<String>();
    static {
        // expected sheet-id not found
        // EXPECTED_ADDITIONAL_FAILURES.add("spreadsheet/52348.xlsx");
        // EXPECTED_ADDITIONAL_FAILURES.add("spreadsheet/59021.xlsx");
        // zip-bomb
        EXPECTED_ADDITIONAL_FAILURES.add("spreadsheet/54764.xlsx");
        EXPECTED_ADDITIONAL_FAILURES.add("spreadsheet/54764-2.xlsx");
        EXPECTED_ADDITIONAL_FAILURES.add("spreadsheet/54764.xlsx");
        EXPECTED_ADDITIONAL_FAILURES.add("spreadsheet/poc-xmlbomb.xlsx");
        // strict OOXML
        EXPECTED_ADDITIONAL_FAILURES.add("spreadsheet/57914.xlsx");
        EXPECTED_ADDITIONAL_FAILURES.add("spreadsheet/SampleSS.strict.xlsx");
        EXPECTED_ADDITIONAL_FAILURES.add("spreadsheet/SimpleStrict.xlsx");
        EXPECTED_ADDITIONAL_FAILURES.add("spreadsheet/sample.strict.xlsx");
        // binary format
        EXPECTED_ADDITIONAL_FAILURES.add("spreadsheet/Simple.xlsb");
        // TODO: good to ignore?
        EXPECTED_ADDITIONAL_FAILURES.add("spreadsheet/sample-beta.xlsx");
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
        } catch (IllegalArgumentException e) {
            if(!EXPECTED_ADDITIONAL_FAILURES.contains(file.getParentFile().getName() + "/" + file.getName())) {
                throw e;
            }
        } catch (InvalidFormatException e) {
            if(!EXPECTED_ADDITIONAL_FAILURES.contains(file.getParentFile().getName() + "/" + file.getName())) {
                throw e;
            }
        } catch (IOException e) {
            if(!EXPECTED_ADDITIONAL_FAILURES.contains(file.getParentFile().getName() + "/" + file.getName())) {
                throw e;
            }
        } catch (POIXMLException e) {
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
        InputStream stream = new BufferedInputStream(new FileInputStream("test-data/spreadsheet/ref-56737.xlsx"));
        try {
            handleFile(stream);
        } finally {
            stream.close();
        }
    }

    // a test-case to test this locally without executing the full TestAllFiles
    @Test
    public void testExtractor() throws Exception {
        handleExtracting(new File("test-data/spreadsheet/ref-56737.xlsx"));
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
                public void write(int b) {}
                public void write(byte[] b) {}
                public void write(byte[] b, int off, int len) {}
            });
        }
        public void write(int b) {}
        public void write(byte[] buf, int off, int len) {}
        public void print(boolean b) {}
        public void print(char c) {}
        public void print(int i) {}
        public void print(long l) {}
        public void print(float f) {}
        public void print(double d) {}
        public void print(char[] s) {}
        public void print(String s) {}
        public void print(Object obj) {}
        public void println() {}
        public void println(boolean x) {}
        public void println(char x) {}
        public void println(int x) {}
        public void println(long x) {}
        public void println(float x) {}
        public void println(double x) {}
        public void println(char[] x) {}
        public void println(String x) {}
        public void println(Object x) {}
        public PrintStream printf(String format, Object... args) { return this; }
        public PrintStream printf(Locale l, String format, Object... args) { return this; }
        public PrintStream format(String format, Object... args) { return this; }
        public PrintStream format(Locale l, String format, Object... args) { return this; }
        public PrintStream append(CharSequence csq) { return this; }
        public PrintStream append(CharSequence csq, int start, int end) { return this; }
        public PrintStream append(char c) { return this; }
        public void write(byte[] b) {}
    }
}
