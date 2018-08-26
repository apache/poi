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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;

import org.apache.poi.POIDataSamples;
import org.apache.poi.UnsupportedFileFormatException;
import org.apache.poi.extractor.POIOLE2TextExtractor;
import org.apache.poi.extractor.POITextExtractor;
import org.apache.poi.hdgf.extractor.VisioTextExtractor;
import org.apache.poi.hpbf.extractor.PublisherTextExtractor;
import org.apache.poi.hsmf.extractor.OutlookTextExtactor;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.OldExcelFormatException;
import org.apache.poi.hssf.extractor.EventBasedExcelExtractor;
import org.apache.poi.hssf.extractor.ExcelExtractor;
import org.apache.poi.hwpf.extractor.Word6Extractor;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.ooxml.extractor.ExtractorFactory;
import org.apache.poi.ooxml.extractor.POIXMLTextExtractor;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.sl.extractor.SlideShowExtractor;
import org.apache.poi.xdgf.extractor.XDGFVisioExtractor;
import org.apache.poi.xssf.extractor.XSSFBEventBasedExcelExtractor;
import org.apache.poi.xssf.extractor.XSSFEventBasedExcelExtractor;
import org.apache.poi.xssf.extractor.XSSFExcelExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.xmlbeans.XmlException;
import org.junit.Test;

/**
 * Test that the extractor factory plays nicely
 */
public class TestExtractorFactory {

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
    private static final File txt = getFileAndCheck(slTests, "SampleShow.txt");

    private static final POIDataSamples olTests = POIDataSamples.getHSMFInstance();
    private static final File msg = getFileAndCheck(olTests, "quick.msg");
    private static final File msgEmb = getFileAndCheck(olTests, "attachment_test_msg.msg");
    private static final File msgEmbMsg = getFileAndCheck(olTests, "attachment_msg_pdf.msg");

    private static final POIDataSamples dgTests = POIDataSamples.getDiagramInstance();
    private static final File vsd = getFileAndCheck(dgTests, "Test_Visio-Some_Random_Text.vsd");
    private static final File vsdx = getFileAndCheck(dgTests, "test.vsdx");

    private static POIDataSamples pubTests = POIDataSamples.getPublisherInstance();
    private static File pub = getFileAndCheck(pubTests, "Simple.pub");

    private static File getFileAndCheck(POIDataSamples samples, String name) {
        File file = samples.getFile(name);

        assertNotNull("Did not get a file for " + name, file);
        assertTrue("Did not get a type file for " + name, file.isFile());
        assertTrue("File did not exist: " + name, file.exists());

        return file;
    }

    private static final Object[] TEST_SET = {
        "Excel", xls, ExcelExtractor.class, 200,
        "Excel - xlsx", xlsx, XSSFExcelExtractor.class, 200,
        "Excel - xltx", xltx, XSSFExcelExtractor.class, -1,
        "Excel - xlsb", xlsb, XSSFBEventBasedExcelExtractor.class, -1,
        "Word", doc, WordExtractor.class, 120,
        "Word - docx", docx, XWPFWordExtractor.class, 120,
        "Word - dotx", dotx, XWPFWordExtractor.class, -1,
        "Word 6", doc6, Word6Extractor.class, 20,
        "Word 95", doc95, Word6Extractor.class, 120,
        "PowerPoint", ppt, SlideShowExtractor.class, 120,
        "PowerPoint - pptx", pptx, SlideShowExtractor.class, 120,
        "Visio", vsd, VisioTextExtractor.class, 50,
        "Visio - vsdx", vsdx, XDGFVisioExtractor.class, 20,
        "Publisher", pub, PublisherTextExtractor.class, 50,
        "Outlook msg", msg, OutlookTextExtactor.class, 50,

        // TODO Support OOXML-Strict, see bug #57699
        // xlsxStrict
    };

    @FunctionalInterface
    interface FunctionEx<T, R> {
        R apply(T t) throws IOException, OpenXML4JException, XmlException;
    }


    @Test
    public void testFile() throws Exception {
        for (int i = 0; i < TEST_SET.length; i += 4) {
            try (POITextExtractor ext = ExtractorFactory.createExtractor((File) TEST_SET[i + 1])) {
                testExtractor(ext, (String) TEST_SET[i], (Class) TEST_SET[i + 2], (Integer) TEST_SET[i + 3]);
            }
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFileInvalid() throws Exception {
        // Text
        try (POITextExtractor ignored = ExtractorFactory.createExtractor(txt)) {
            fail("extracting from invalid package");
        }
    }

    @Test
    public void testInputStream() throws Exception {
        testStream(ExtractorFactory::createExtractor, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInputStreamInvalid() throws Exception {
        testInvalid(ExtractorFactory::createExtractor);
    }

    @Test
    public void testPOIFS() throws Exception {
        testStream((f) -> ExtractorFactory.createExtractor(new POIFSFileSystem(f)), false);
    }

    @Test(expected = IOException.class)
    public void testPOIFSInvalid() throws Exception {
        testInvalid((f) -> ExtractorFactory.createExtractor(new POIFSFileSystem(f)));
    }

    private void testStream(final FunctionEx<FileInputStream, POITextExtractor> poifsIS, final boolean loadOOXML)
    throws IOException, OpenXML4JException, XmlException {
        for (int i = 0; i < TEST_SET.length; i += 4) {
            File testFile = (File) TEST_SET[i + 1];
            if (!loadOOXML && (testFile.getName().endsWith("x") || testFile.getName().endsWith("xlsb"))) {
                continue;
            }
            try (FileInputStream fis = new FileInputStream(testFile);
                 POITextExtractor ext = poifsIS.apply(fis)) {
                testExtractor(ext, (String) TEST_SET[i], (Class) TEST_SET[i + 2], (Integer) TEST_SET[i + 3]);
            } catch (IllegalArgumentException e) {
                fail("failed to process "+testFile);
            }
        }
    }

    private void testExtractor(final POITextExtractor ext, final String testcase, final Class extrClass, final Integer minLength) {
        assertTrue("invalid extractor for " + testcase, extrClass.isInstance(ext));
        final String actual = ext.getText();
        if (minLength == -1) {
            assertContains(actual.toLowerCase(Locale.ROOT), "test");
        } else {
            assertTrue("extracted content too short for " + testcase, actual.length() > minLength);
        }
    }

    private void testInvalid(FunctionEx<FileInputStream, POITextExtractor> poifs) throws IOException, OpenXML4JException, XmlException {
        // Text
        try (FileInputStream fis = new FileInputStream(txt);
             POITextExtractor ignored = poifs.apply(fis)) {
            fail("extracting from invalid package");
        }
    }

    @Test
    public void testPackage() throws Exception {
        for (int i = 0; i < TEST_SET.length; i += 4) {
            final File testFile = (File) TEST_SET[i + 1];
            if (!testFile.getName().endsWith("x")) {
                continue;
            }

            try (final OPCPackage pkg = OPCPackage.open(testFile, PackageAccess.READ);
                 final POITextExtractor ext = ExtractorFactory.createExtractor(pkg)) {
                testExtractor(ext, (String) TEST_SET[i], (Class) TEST_SET[i + 2], (Integer) TEST_SET[i + 3]);
                pkg.revert();
            }
        }
    }

    @Test(expected = UnsupportedFileFormatException.class)
    public void testPackageInvalid() throws Exception {
        // Text
        try (final OPCPackage pkg = OPCPackage.open(txt, PackageAccess.READ);
             final POITextExtractor ignored = ExtractorFactory.createExtractor(pkg)) {
            fail("extracting from invalid package");
        }
    }

    @Test
    public void testPreferEventBased() throws Exception {
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


        // Check we get the right extractors now
        POITextExtractor extractor = ExtractorFactory.createExtractor(new POIFSFileSystem(new FileInputStream(xls)));
        assertTrue(
                extractor
                instanceof EventBasedExcelExtractor
        );
        extractor.close();
        extractor = ExtractorFactory.createExtractor(new POIFSFileSystem(new FileInputStream(xls)));
        assertTrue(
                extractor.getText().length() > 200
        );
        extractor.close();

        extractor = ExtractorFactory.createExtractor(OPCPackage.open(xlsx.toString(), PackageAccess.READ));
        assertTrue(extractor instanceof XSSFEventBasedExcelExtractor);
        extractor.close();

        extractor = ExtractorFactory.createExtractor(OPCPackage.open(xlsx.toString(), PackageAccess.READ));
        assertTrue(
                extractor.getText().length() > 200
        );
        extractor.close();


        // Put back to normal
        ExtractorFactory.setThreadPrefersEventExtractors(false);
        assertFalse(ExtractorFactory.getPreferEventExtractor());
        assertFalse(ExtractorFactory.getThreadPrefersEventExtractors());
        assertNull(ExtractorFactory.getAllThreadsPreferEventExtractors());

        // And back
        extractor = ExtractorFactory.createExtractor(new POIFSFileSystem(new FileInputStream(xls)));
        assertTrue(
                extractor
                instanceof ExcelExtractor
        );
        extractor.close();
        extractor = ExtractorFactory.createExtractor(new POIFSFileSystem(new FileInputStream(xls)));
        assertTrue(
                extractor.getText().length() > 200
        );
        extractor.close();

        extractor = ExtractorFactory.createExtractor(OPCPackage.open(xlsx.toString(), PackageAccess.READ));
        assertTrue(
                extractor
                instanceof XSSFExcelExtractor
        );
        extractor.close();
        extractor = ExtractorFactory.createExtractor(OPCPackage.open(xlsx.toString()));
        assertTrue(
                extractor.getText().length() > 200
        );
        extractor.close();
    }

    /**
     * Test embedded docs text extraction. For now, only
     *  does poifs embedded, but will do ooxml ones
     *  at some point.
     */
    @Test
    public void testEmbedded() throws Exception {
        final Object[] testObj = {
            "No embeddings", xls, "0-0-0-0-0-0",
            "Excel", xlsEmb, "6-2-2-2-0-0",
            "Word", docEmb, "4-1-2-1-0-0",
            "Word which contains an OOXML file", docEmbOOXML, "3-0-1-1-0-1",
            "Outlook", msgEmb, "1-1-0-0-0-0",
            "Outlook with another outlook file in it", msgEmbMsg, "1-0-0-0-1-0",
        };

        for (int i=0; i<testObj.length; i+=3) {
            try (final POIOLE2TextExtractor ext = ExtractorFactory.createExtractor((File)testObj[i+1])) {
                final POITextExtractor[] embeds = ExtractorFactory.getEmbeddedDocsTextExtractors(ext);

                int numWord = 0, numXls = 0, numPpt = 0, numMsg = 0, numWordX = 0;
                for (POITextExtractor embed : embeds) {
                    assertTrue(embed.getText().length() > 20);
                    if (embed instanceof SlideShowExtractor) {
                        numPpt++;
                    } else if (embed instanceof ExcelExtractor) {
                        numXls++;
                    } else if (embed instanceof WordExtractor) {
                        numWord++;
                    } else if (embed instanceof OutlookTextExtactor) {
                        numMsg++;
                    } else if (embed instanceof XWPFWordExtractor) {
                        numWordX++;
                    }
                }

                final String actual = embeds.length+"-"+numWord+"-"+numXls+"-"+numPpt+"-"+numMsg+"-"+numWordX;
                final String expected = (String)testObj[i+2];
                assertEquals("invalid number of embeddings - "+testObj[i], expected, actual);
            }
        }

        // TODO - PowerPoint
        // TODO - Publisher
        // TODO - Visio
    }

    private static final String[] EXPECTED_FAILURES = {
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
        "spreadsheet/chart_sheet.xlsx",
    };
    
    @Test
    public void testFileLeak() {
        // run a number of files that might fail in order to catch 
        // leaked file resources when using file-leak-detector while
        // running the test
        
        for(String file : EXPECTED_FAILURES) {
            try {
                ExtractorFactory.createExtractor(POIDataSamples.getSpreadSheetInstance().getFile(file));
            } catch (Exception e) {
                // catch all exceptions here as we are only interested in file-handle leaks
            }
        }
    }
    
    /**
     *  #59074 - Excel 95 files should give a helpful message, not just 
     *   "No supported documents found in the OLE2 stream"
     */
    @Test(expected = OldExcelFormatException.class)
    public void bug59074() throws Exception {
        ExtractorFactory.createExtractor(
                POIDataSamples.getSpreadSheetInstance().getFile("59074.xls"));
    }

    @SuppressWarnings("deprecation")
    @Test(expected = IllegalStateException.class)
    public void testGetEmbedFromXMLExtractor() {
        // currently not implemented
        ExtractorFactory.getEmbededDocsTextExtractors((POIXMLTextExtractor) null);
    }

    @SuppressWarnings("deprecation")
    @Test(expected = IllegalStateException.class)
    public void testGetEmbeddedFromXMLExtractor() {
        // currently not implemented
        ExtractorFactory.getEmbeddedDocsTextExtractors((POIXMLTextExtractor)null);
    }

    // This bug is currently open. This test will fail with "expected error not thrown" when the bug has been fixed.
    // When this happens, change this from @Test(expected=...) to @Test
    // bug 45565: text within TextBoxes is extracted by ExcelExtractor and WordExtractor
    @Test(expected=AssertionError.class)
    public void test45565() throws Exception {
        try (POITextExtractor extractor = ExtractorFactory.createExtractor(HSSFTestDataSamples.getSampleFile("45565.xls"))) {
            String text = extractor.getText();
            assertContains(text, "testdoc");
            assertContains(text, "test phrase");
        }
    }
}
