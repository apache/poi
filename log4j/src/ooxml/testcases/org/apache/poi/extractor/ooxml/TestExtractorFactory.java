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
package org.apache.poi.extractor.ooxml;

import static org.apache.poi.POITestCase.assertContains;
import static org.apache.poi.extractor.ExtractorFactory.createExtractor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.stream.Stream;

import org.apache.poi.POIDataSamples;
import org.apache.poi.extractor.ExtractorFactory;
import org.apache.poi.extractor.POIOLE2TextExtractor;
import org.apache.poi.extractor.POITextExtractor;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.extractor.EventBasedExcelExtractor;
import org.apache.poi.hssf.extractor.ExcelExtractor;
import org.apache.poi.ooxml.extractor.POIXMLExtractorFactory;
import org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xssf.extractor.XSSFEventBasedExcelExtractor;
import org.apache.poi.xssf.extractor.XSSFExcelExtractor;
import org.apache.xmlbeans.XmlException;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Test that the extractor factory plays nicely
 */
class TestExtractorFactory {

    private static final POIDataSamples ssTests = POIDataSamples.getSpreadSheetInstance();
    private static final File xls = getFileAndCheck(ssTests, "SampleSS.xls");
    private static final File xlsx = getFileAndCheck(ssTests, "SampleSS.xlsx");
    @SuppressWarnings("unused")
    private static final File xlsxStrict = getFileAndCheck(ssTests, "SampleSS.strict.xlsx");
    private static final File xltx = getFileAndCheck(ssTests, "test.xltx");
    private static final File xlsEmb = getFileAndCheck(ssTests, "excel_with_embeded.xls");
    private static final File xlsb = getFileAndCheck(ssTests, "testVarious.xlsb");

    private static final POIDataSamples wpTests = POIDataSamples.getDocumentInstance();
    private static final File doc = getFileAndCheck(wpTests, "SampleDoc.doc");
    private static final File doc6 = getFileAndCheck(wpTests, "Word6.doc");
    private static final File doc95 = getFileAndCheck(wpTests, "Word95.doc");
    private static final File docx = getFileAndCheck(wpTests, "SampleDoc.docx");
    private static final File dotx = getFileAndCheck(wpTests, "test.dotx");
    private static final File docEmb = getFileAndCheck(wpTests, "word_with_embeded.doc");
    private static final File docEmbOOXML = getFileAndCheck(wpTests, "word_with_embeded_ooxml.doc");

    private static final POIDataSamples slTests = POIDataSamples.getSlideShowInstance();
    private static final File ppt = getFileAndCheck(slTests, "SampleShow.ppt");
    private static final File pptx = getFileAndCheck(slTests, "SampleShow.pptx");
    private static final File ppt97 = getFileAndCheck(slTests, "bug56240.ppt");
    private static final File txt = getFileAndCheck(slTests, "SampleShow.txt");

    private static final POIDataSamples olTests = POIDataSamples.getHSMFInstance();
    private static final File msg = getFileAndCheck(olTests, "quick.msg");
    private static final File msgEmb = getFileAndCheck(olTests, "attachment_test_msg.msg");
    private static final File msgEmbMsg = getFileAndCheck(olTests, "attachment_msg_pdf.msg");

    private static final POIDataSamples dgTests = POIDataSamples.getDiagramInstance();
    private static final File vsd = getFileAndCheck(dgTests, "Test_Visio-Some_Random_Text.vsd");
    private static final File vsdx = getFileAndCheck(dgTests, "test.vsdx");

    private static final POIDataSamples pubTests = POIDataSamples.getPublisherInstance();
    private static final File pub = getFileAndCheck(pubTests, "Simple.pub");

    private static final POIXMLExtractorFactory xmlFactory = new POIXMLExtractorFactory();

    private static File getFileAndCheck(POIDataSamples samples, String name) {
        File file = samples.getFile(name);

        assertNotNull(file, "Did not get a file for " + name);
        assertTrue(file.isFile(), "Did not get a type file for " + name);
        assertTrue(file.exists(), "File did not exist: " + name);

        return file;
    }

    public static Stream<Arguments> testOOXMLData() {
        return Stream.of(
            Arguments.of("Excel - xlsx", xlsx, "XSSFExcelExtractor", 200),
            Arguments.of("Excel - xltx", xltx, "XSSFExcelExtractor", -1),
            Arguments.of("Excel - xlsb", xlsb, "XSSFBEventBasedExcelExtractor", -1),
            Arguments.of("Word - docx", docx, "XWPFWordExtractor", 120),
            Arguments.of("Word - dotx", dotx, "XWPFWordExtractor", -1),
            Arguments.of("PowerPoint - pptx", pptx, "XSLFExtractor", 120),
            Arguments.of("Visio - vsdx", vsdx, "XDGFVisioExtractor", 20)
        );
    };

    public static Stream<Arguments> testScratchData() {
        return Stream.of(
            Arguments.of("Excel", xls, "ExcelExtractor", 200),
            Arguments.of("Word", doc, "WordExtractor", 120),
            Arguments.of("Word 6", doc6, "Word6Extractor", 20),
            Arguments.of("Word 95", doc95, "Word6Extractor", 120),
            Arguments.of("PowerPoint", ppt, "SlideShowExtractor", 120),
            Arguments.of("PowerPoint 97 Dual", ppt97, "SlideShowExtractor", 120),
            Arguments.of("Visio", vsd, "VisioTextExtractor", 50),
            Arguments.of("Publisher", pub, "PublisherTextExtractor", 50),
            Arguments.of("Outlook msg", msg, "OutlookTextExtractor", 50)
        );
    };

    public static Stream<Arguments> testFileData() {
        return Stream.concat(testOOXMLData(), testScratchData());
        // TODO Support OOXML-Strict / xlsxStrict, see bug #57699
    };


    @ParameterizedTest
    @MethodSource("testFileData")
    void testFile(String testcase, File file, String extractor, int count) throws Exception {
        try (POITextExtractor ext = createExtractor(file)) {
            assertNotNull(ext);
            testExtractor(ext, testcase, extractor, count);
        }
    }

    @ParameterizedTest
    @MethodSource("testScratchData")
    void testPOIFS(String testcase, File testFile, String extractor, int count) throws Exception {
        // test processing of InputStream
        try (FileInputStream fis = new FileInputStream(testFile);
             POIFSFileSystem poifs = new POIFSFileSystem(fis);
             POITextExtractor ext = createExtractor(poifs)) {
            assertNotNull(ext);
            testExtractor(ext, testcase, extractor, count);
        }
    }

    @ParameterizedTest
    @MethodSource("testFileData")
    void testOOXML(String testcase, File testFile, String extractor, int count) throws Exception {
        // test processing of InputStream
        try (FileInputStream fis = new FileInputStream(testFile);
             POITextExtractor ext = createExtractor(fis)) {
            assertNotNull(ext);
            testExtractor(ext, testcase, extractor, count);
        }
    }

    @ParameterizedTest
    @MethodSource("testOOXMLData")
    void testPackage(String testcase, File testFile, String extractor, int count) throws Exception {
        try (final OPCPackage pkg = OPCPackage.open(testFile, PackageAccess.READ);
             final POITextExtractor ext = xmlFactory.create(pkg)) {
            assertNotNull(ext);
            testExtractor(ext, testcase, extractor, count);
            pkg.revert();
        }
    }

    @Test
    void testFileInvalid() {
        IOException ex = assertThrows(IOException.class, () -> createExtractor(txt));
        assertEquals("Can't create extractor - unsupported file type: UNKNOWN", ex.getMessage());
    }

    @Test
    void testInputStreamInvalid() throws IOException {
        try (FileInputStream fis = new FileInputStream(txt)) {
            IOException ex = assertThrows(IOException.class, () -> createExtractor(fis));
            assertTrue(ex.getMessage().contains(FileMagic.UNKNOWN.name()));
        }
    }

    @Test
    void testPOIFSInvalid() {
        // Not really an Extractor test, but we'll leave it to test POIFS reaction anyway ...
        IOException ex = assertThrows(IOException.class, () -> new POIFSFileSystem(txt));
        assertTrue(ex.getMessage().contains("Invalid header signature; read 0x3D20726F68747541, expected 0xE11AB1A1E011CFD0"));
    }

    private void testExtractor(final POITextExtractor ext, final String testcase, final String extrClass, final Integer minLength) {
        assertEquals(extrClass, ext.getClass().getSimpleName(), "invalid extractor for " + testcase);
        final String actual = ext.getText();
        if (minLength == -1) {
            assertContains(actual.toLowerCase(Locale.ROOT), "test");
        } else {
            assertTrue(actual.length() > minLength, "extracted content too short for " + testcase);
        }
    }
    @Test
    void testPackageInvalid() {
        // Text
        assertThrows(NotOfficeXmlFileException.class, () -> OPCPackage.open(txt, PackageAccess.READ));
    }

    @Test
    void testPreferEventBased() throws Exception {
        assertFalse(ExtractorFactory.getPreferEventExtractor());
        assertFalse(ExtractorFactory.getThreadPrefersEventExtractors());
        assertNull(ExtractorFactory.getAllThreadsPreferEventExtractors());

        ExtractorFactory.setThreadPrefersEventExtractors(true);

        assertTrue(ExtractorFactory.getPreferEventExtractor());
        assertTrue(ExtractorFactory.getThreadPrefersEventExtractors());
        assertNull(ExtractorFactory.getAllThreadsPreferEventExtractors());

        ExtractorFactory.setAllThreadsPreferEventExtractors(false);

        assertFalse(ExtractorFactory.getPreferEventExtractor());
        assertTrue(ExtractorFactory.getThreadPrefersEventExtractors());
        assertEquals(Boolean.FALSE, ExtractorFactory.getAllThreadsPreferEventExtractors());

        ExtractorFactory.setAllThreadsPreferEventExtractors(null);

        assertTrue(ExtractorFactory.getPreferEventExtractor());
        assertTrue(ExtractorFactory.getThreadPrefersEventExtractors());
        assertNull(ExtractorFactory.getAllThreadsPreferEventExtractors());

        try {
            // Check we get the right extractors now
            try (POITextExtractor extractor = createExtractor(new POIFSFileSystem(new FileInputStream(xls)))) {
                assertTrue(extractor instanceof EventBasedExcelExtractor);
                assertTrue(extractor.getText().length() > 200);
            }
            try (POITextExtractor extractor = xmlFactory.create(OPCPackage.open(xlsx.toString(), PackageAccess.READ))) {
                assertNotNull(extractor);
                assertTrue(extractor instanceof XSSFEventBasedExcelExtractor);
                assertTrue(extractor.getText().length() > 200);
            }
        } finally {
            // Put back to normal
            ExtractorFactory.setThreadPrefersEventExtractors(false);
        }

        assertFalse(ExtractorFactory.getPreferEventExtractor());
        assertFalse(ExtractorFactory.getThreadPrefersEventExtractors());
        assertNull(ExtractorFactory.getAllThreadsPreferEventExtractors());

        // And back
        try (POITextExtractor extractor = createExtractor(new POIFSFileSystem(new FileInputStream(xls)))) {
            assertTrue(extractor instanceof ExcelExtractor);
            assertTrue(extractor.getText().length() > 200);
        }

        try (POITextExtractor extractor = xmlFactory.create(OPCPackage.open(xlsx.toString(), PackageAccess.READ))) {
            assertTrue(extractor instanceof XSSFExcelExtractor);
        }

        try (POITextExtractor extractor = xmlFactory.create(OPCPackage.open(xlsx.toString()))) {
            assertNotNull(extractor);
            assertTrue(extractor.getText().length() > 200);
        }
    }

    public static Stream<Arguments> testEmbeddedData() {
        return Stream.of(
            Arguments.of("No embeddings", xls, "0-0-0-0-0-0"),
            Arguments.of("Excel", xlsEmb, "6-2-2-2-0-0"),
            Arguments.of("Word", docEmb, "4-1-2-1-0-0"),
            Arguments.of("Word which contains an OOXML file", docEmbOOXML, "3-0-1-1-0-1"),
            Arguments.of("Outlook", msgEmb, "1-1-0-0-0-0"),
            Arguments.of("Outlook with another outlook file in it", msgEmbMsg, "1-0-0-0-1-0")
            // TODO - PowerPoint
            // TODO - Publisher
            // TODO - Visio
        );
    }

    /**
     * Test embedded docs text extraction. For now, only
     *  does poifs embedded, but will do ooxml ones
     *  at some point.
     */
    @ParameterizedTest
    @MethodSource("testEmbeddedData")
    void testEmbedded(String format, File file, String expected) throws Exception {
        int numWord = 0, numXls = 0, numPpt = 0, numMsg = 0, numWordX = 0;

        try (final POIOLE2TextExtractor ext = (POIOLE2TextExtractor) createExtractor(file)) {
            final POITextExtractor[] embeds = ExtractorFactory.getEmbeddedDocsTextExtractors(ext);

            for (POITextExtractor embed : embeds) {
                assertTrue(embed.getText().length() > 20);
                switch (embed.getClass().getSimpleName()) {
                    case "SlideShowExtractor":
                        numPpt++;
                        break;
                    case "ExcelExtractor":
                        numXls++;
                        break;
                    case "WordExtractor":
                        numWord++;
                        break;
                    case "OutlookTextExtractor":
                        numMsg++;
                        break;
                    case "XWPFWordExtractor":
                        numWordX++;
                        break;
                }
            }

            final String actual = embeds.length+"-"+numWord+"-"+numXls+"-"+numPpt+"-"+numMsg+"-"+numWordX;
            assertEquals(expected, actual, "invalid number of embeddings - "+format);
        }


    }

    @ParameterizedTest
    @ValueSource(strings = {
        // password protected files
        "spreadsheet/password.xls",
        "spreadsheet/protected_passtika.xlsx",
        "spreadsheet/51832.xls",
        "document/PasswordProtected.doc",
        "slideshow/Password_Protected-hello.ppt",
        "slideshow/Password_Protected-56-hello.ppt",
        "slideshow/Password_Protected-np-hello.ppt",
        "slideshow/cryptoapi-proc2356.ppt",
        //"document/bug53475-password-is-pass.docx",
        //"document/bug53475-password-is-solrcell.docx",
        "spreadsheet/xor-encryption-abc.xls",
        "spreadsheet/35897-type4.xls",
        //"poifs/protect.xlsx",
        //"poifs/protected_sha512.xlsx",
        //"poifs/extenxls_pwd123.xlsx",
        //"poifs/protected_agile.docx",
        "spreadsheet/58616.xlsx",

        // TODO: fails XMLExportTest, is this ok?
        "spreadsheet/CustomXMLMapping-singleattributenamespace.xlsx",
        "spreadsheet/55864.xlsx",
        "spreadsheet/57890.xlsx",

        // TODO: these fail now with some NPE/file read error because we now try to compute every value via Cell.toString()!
        "spreadsheet/44958.xls",
        "spreadsheet/44958_1.xls",
        "spreadsheet/testArraysAndTables.xls",

        // TODO: good to ignore?
        "spreadsheet/sample-beta.xlsx",

        // This is actually a spreadsheet!
        "hpsf/TestRobert_Flaherty.doc",

        // some files that are broken, eg Word 95, ...
        "spreadsheet/43493.xls",
        "spreadsheet/46904.xls",
        "document/Bug50955.doc",
        "slideshow/PPT95.ppt",
        "openxml4j/OPCCompliance_CoreProperties_DCTermsNamespaceLimitedUseFAIL.docx",
        "openxml4j/OPCCompliance_CoreProperties_DoNotUseCompatibilityMarkupFAIL.docx",
        "openxml4j/OPCCompliance_CoreProperties_LimitedXSITypeAttribute_NotPresentFAIL.docx",
        "openxml4j/OPCCompliance_CoreProperties_LimitedXSITypeAttribute_PresentWithUnauthorizedValueFAIL.docx",
        "openxml4j/OPCCompliance_CoreProperties_OnlyOneCorePropertiesPartFAIL.docx",
        "openxml4j/OPCCompliance_CoreProperties_UnauthorizedXMLLangAttributeFAIL.docx",
        "openxml4j/OPCCompliance_DerivedPartNameFAIL.docx",
        "openxml4j/invalid.xlsx",
        "spreadsheet/54764-2.xlsx",   // see TestXSSFBugs.bug54764()
        "spreadsheet/54764.xlsx",     // see TestXSSFBugs.bug54764()
        "spreadsheet/Simple.xlsb",
        "poifs/unknown_properties.msg", // POIFS properties corrupted
        "poifs/only-zero-byte-streams.ole2", // No actual contents
        "spreadsheet/poc-xmlbomb.xlsx",  // contains xml-entity-expansion
        "spreadsheet/poc-xmlbomb-empty.xlsx",  // contains xml-entity-expansion
        "spreadsheet/poc-shared-strings.xlsx",  // contains shared-string-entity-expansion

        // old Excel files, which we only support simple text extraction of
        "spreadsheet/testEXCEL_2.xls",
        "spreadsheet/testEXCEL_3.xls",
        "spreadsheet/testEXCEL_4.xls",
        "spreadsheet/testEXCEL_5.xls",
        "spreadsheet/testEXCEL_95.xls",

        // OOXML Strict is not yet supported, see bug #57699
        "spreadsheet/SampleSS.strict.xlsx",
        "spreadsheet/SimpleStrict.xlsx",
        "spreadsheet/sample.strict.xlsx",

        // non-TNEF files
        "ddf/Container.dat",
        "ddf/47143.dat",

        // sheet cloning errors
        "spreadsheet/47813.xlsx",
        "spreadsheet/56450.xls",
        "spreadsheet/57231_MixedGasReport.xls",
        "spreadsheet/OddStyleRecord.xls",
        "spreadsheet/WithChartSheet.xlsx",
        "spreadsheet/chart_sheet.xlsx"
    })
    void testFileLeak(String file) {
        // run a number of files that might fail in order to catch
        // leaked file resources when using file-leak-detector while
        // running the test
        assertThrows(Exception.class, () -> ex(file));
    }

    /**
     *  #59074 - Excel 95 files should give a helpful message, not just
     *   "No supported documents found in the OLE2 stream"
     */
    @Test
    void bug59074() throws Exception {
        try (POITextExtractor extractor = ex("59074.xls")) {
            String text = extractor.getText();
            assertContains(text, "Exotic warrant");
        }
    }

    @Test
    void testGetEmbeddedFromXMLExtractor() {
        // currently not implemented
        assertThrows(IllegalStateException.class, () -> ExtractorFactory.getEmbeddedDocsTextExtractors(null));
    }

    // This bug is currently open. This test will fail with "expected error not thrown" when the bug has been fixed.
    // When this happens, change this from @Test(expected=...) to @Test
    // bug 45565: text within TextBoxes is extracted by ExcelExtractor and WordExtractor
    @Test
    void test45565() throws Exception {
        try (POITextExtractor extractor = ex("45565.xls")) {
            String text = extractor.getText();
            assertThrows(AssertionError.class, () -> {
                assertContains(text, "testdoc");
                assertContains(text, "test phrase");
            });
        }
    }

    private static POITextExtractor ex(String filename) throws IOException {
        return createExtractor(ssTests.getFile(filename));
    }
}
