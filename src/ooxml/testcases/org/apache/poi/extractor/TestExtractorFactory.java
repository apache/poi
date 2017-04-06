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
package org.apache.poi.extractor;

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

import org.apache.poi.POIDataSamples;
import org.apache.poi.POIOLE2TextExtractor;
import org.apache.poi.POITextExtractor;
import org.apache.poi.POIXMLException;
import org.apache.poi.POIXMLTextExtractor;
import org.apache.poi.UnsupportedFileFormatException;
import org.apache.poi.hdgf.extractor.VisioTextExtractor;
import org.apache.poi.hpbf.extractor.PublisherTextExtractor;
import org.apache.poi.hslf.extractor.PowerPointExtractor;
import org.apache.poi.hsmf.extractor.OutlookTextExtactor;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.OldExcelFormatException;
import org.apache.poi.hssf.extractor.EventBasedExcelExtractor;
import org.apache.poi.hssf.extractor.ExcelExtractor;
import org.apache.poi.hwpf.extractor.Word6Extractor;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.poifs.filesystem.OPOIFSFileSystem;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xdgf.extractor.XDGFVisioExtractor;
import org.apache.poi.xslf.extractor.XSLFPowerPointExtractor;
import org.apache.poi.xssf.extractor.XSSFEventBasedExcelExtractor;
import org.apache.poi.xssf.extractor.XSSFExcelExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test that the extractor factory plays nicely
 */
public class TestExtractorFactory {
    private static File txt;

    private static File xls;
    private static File xlsx;
    private static File xlsxStrict;
    private static File xltx;
    private static File xlsEmb;
    private static File xlsb;

    private static File doc;
    private static File doc6;
    private static File doc95;
    private static File docx;
    private static File dotx;
    private static File docEmb;
    private static File docEmbOOXML;

    private static File ppt;
    private static File pptx;

    private static File msg;
    private static File msgEmb;
    private static File msgEmbMsg;

    private static File vsd;
    private static File vsdx;

    private static File pub;

    private static File getFileAndCheck(POIDataSamples samples, String name) {
        File file = samples.getFile(name);

        assertNotNull("Did not get a file for " + name, file);
        assertTrue("Did not get a type file for " + name, file.isFile());
        assertTrue("File did not exist: " + name, file.exists());

        return file;
    }

    @BeforeClass
    public static void setUp() throws Exception {

        POIDataSamples ssTests = POIDataSamples.getSpreadSheetInstance();
        xls = getFileAndCheck(ssTests, "SampleSS.xls");
        xlsx = getFileAndCheck(ssTests, "SampleSS.xlsx");
        xlsxStrict = getFileAndCheck(ssTests, "SampleSS.strict.xlsx");
        xltx = getFileAndCheck(ssTests, "test.xltx");
        xlsEmb = getFileAndCheck(ssTests, "excel_with_embeded.xls");
        xlsb = getFileAndCheck(ssTests, "testVarious.xlsb");

        POIDataSamples wpTests = POIDataSamples.getDocumentInstance();
        doc = getFileAndCheck(wpTests, "SampleDoc.doc");
        doc6 = getFileAndCheck(wpTests, "Word6.doc");
        doc95 = getFileAndCheck(wpTests, "Word95.doc");
        docx = getFileAndCheck(wpTests, "SampleDoc.docx");
        dotx = getFileAndCheck(wpTests, "test.dotx");
        docEmb = getFileAndCheck(wpTests, "word_with_embeded.doc");
        docEmbOOXML = getFileAndCheck(wpTests, "word_with_embeded_ooxml.doc");

        POIDataSamples slTests = POIDataSamples.getSlideShowInstance();
        ppt = getFileAndCheck(slTests, "SampleShow.ppt");
        pptx = getFileAndCheck(slTests, "SampleShow.pptx");
        txt = getFileAndCheck(slTests, "SampleShow.txt");

        POIDataSamples dgTests = POIDataSamples.getDiagramInstance();
        vsd = getFileAndCheck(dgTests, "Test_Visio-Some_Random_Text.vsd");
        vsdx = getFileAndCheck(dgTests, "test.vsdx");

        POIDataSamples pubTests = POIDataSamples.getPublisherInstance();
        pub = getFileAndCheck(pubTests, "Simple.pub");

        POIDataSamples olTests = POIDataSamples.getHSMFInstance();
        msg = getFileAndCheck(olTests, "quick.msg");
        msgEmb = getFileAndCheck(olTests, "attachment_test_msg.msg");
        msgEmbMsg = getFileAndCheck(olTests, "attachment_msg_pdf.msg");
    }

    @Test
    public void testFile() throws Exception {
        // Excel
        POITextExtractor xlsExtractor = ExtractorFactory.createExtractor(xls);
        assertNotNull("Had empty extractor for " + xls, xlsExtractor);
        assertTrue("Expected instanceof ExcelExtractor, but had: " + xlsExtractor.getClass(), 
                xlsExtractor
                instanceof ExcelExtractor
        );
        assertTrue(
                xlsExtractor.getText().length() > 200
        );
        xlsExtractor.close();

        POITextExtractor extractor = ExtractorFactory.createExtractor(xlsx);
        assertTrue(
                extractor.getClass().getName(),
                extractor
                instanceof XSSFExcelExtractor
        );
        extractor.close();

        extractor = ExtractorFactory.createExtractor(xlsx);
        assertTrue(
                extractor.getText().length() > 200
        );
        extractor.close();

        extractor = ExtractorFactory.createExtractor(xltx);
        assertTrue(
                extractor.getClass().getName(),
                extractor
                instanceof XSSFExcelExtractor
        );
        extractor.close();

        extractor = ExtractorFactory.createExtractor(xlsb);
        assertContains(extractor.getText(), "test");
        extractor.close();


        extractor = ExtractorFactory.createExtractor(xltx);
        assertContains(extractor.getText(), "test");
        extractor.close();

        // TODO Support OOXML-Strict, see bug #57699
        try {
            /*extractor =*/ ExtractorFactory.createExtractor(xlsxStrict);
            fail("OOXML-Strict isn't yet supported");
        } catch (POIXMLException e) {
            // Expected, for now
        }
//        extractor = ExtractorFactory.createExtractor(xlsxStrict);
//        assertTrue(
//                extractor
//                instanceof XSSFExcelExtractor
//        );
//        extractor.close();
//
//        extractor = ExtractorFactory.createExtractor(xlsxStrict);
//        assertTrue(
//                extractor.getText().contains("test")
//        );
//        extractor.close();


        // Word
        extractor = ExtractorFactory.createExtractor(doc);
        assertTrue(
                extractor
                instanceof WordExtractor
        );
        assertTrue(
                extractor.getText().length() > 120
        );
        extractor.close();

        extractor = ExtractorFactory.createExtractor(doc6);
        assertTrue(
                extractor
                instanceof Word6Extractor
        );
        assertTrue(
                extractor.getText().length() > 20
        );
        extractor.close();

        extractor = ExtractorFactory.createExtractor(doc95);
        assertTrue(
                extractor
                instanceof Word6Extractor
        );
        assertTrue(
                extractor.getText().length() > 120
        );
        extractor.close();

        extractor = ExtractorFactory.createExtractor(docx);
        assertTrue(
                extractor instanceof XWPFWordExtractor
        );
        extractor.close();

        extractor = ExtractorFactory.createExtractor(docx);
        assertTrue(
                extractor.getText().length() > 120
        );
        extractor.close();

        extractor = ExtractorFactory.createExtractor(dotx);
        assertTrue(
                extractor instanceof XWPFWordExtractor
        );
        extractor.close();

        extractor = ExtractorFactory.createExtractor(dotx);
        assertContains(extractor.getText(), "Test");
        extractor.close();

        // PowerPoint (PPT)
        extractor = ExtractorFactory.createExtractor(ppt);
        assertTrue(
                extractor
                instanceof PowerPointExtractor
        );
        assertTrue(
                extractor.getText().length() > 120
        );
        extractor.close();

        // PowerPoint (PPTX)
        extractor = ExtractorFactory.createExtractor(pptx);
        assertTrue(
                extractor
                instanceof XSLFPowerPointExtractor
        );
        assertTrue(
                extractor.getText().length() > 120
        );
        extractor.close();

        // Visio - binary
        extractor = ExtractorFactory.createExtractor(vsd);
        assertTrue(
                extractor
                instanceof VisioTextExtractor
        );
        assertTrue(
                extractor.getText().length() > 50
        );
        extractor.close();

        // Visio - vsdx
        extractor = ExtractorFactory.createExtractor(vsdx);
        assertTrue(
                extractor
                instanceof XDGFVisioExtractor
        );
        assertTrue(
                extractor.getText().length() > 20
        );
        extractor.close();

        // Publisher
        extractor = ExtractorFactory.createExtractor(pub);
        assertTrue(
                extractor
                instanceof PublisherTextExtractor
        );
        assertTrue(
                extractor.getText().length() > 50
        );
        extractor.close();

        // Outlook msg
        extractor = ExtractorFactory.createExtractor(msg);
        assertTrue(
                extractor
                instanceof OutlookTextExtactor
        );
        assertTrue(
                extractor.getText().length() > 50
        );
        extractor.close();

        // Text
        try {
            ExtractorFactory.createExtractor(txt);
            fail();
        } catch(IllegalArgumentException e) {
            // Good
        }
    }

    @Test
    public void testInputStream() throws Exception {
        // Excel
        POITextExtractor extractor = ExtractorFactory.createExtractor(new FileInputStream(xls));
        assertTrue(
                extractor
                instanceof ExcelExtractor
        );
        assertTrue(
                extractor.getText().length() > 200
        );
        extractor.close();

        extractor = ExtractorFactory.createExtractor(new FileInputStream(xlsx));
        assertTrue(
                extractor.getClass().getName(),
                extractor
                instanceof XSSFExcelExtractor
        );
        assertTrue(
                extractor.getText().length() > 200
        );
        // TODO Support OOXML-Strict, see bug #57699
//        assertTrue(
//                ExtractorFactory.createExtractor(new FileInputStream(xlsxStrict))
//                instanceof XSSFExcelExtractor
//        );
//        assertTrue(
//                ExtractorFactory.createExtractor(new FileInputStream(xlsxStrict)).getText().length() > 200
//        );
        extractor.close();

        // Word
        extractor = ExtractorFactory.createExtractor(new FileInputStream(doc));
        assertTrue(
                extractor.getClass().getName(),
                extractor
                instanceof WordExtractor
        );
        assertTrue(
                extractor.getText().length() > 120
        );
        extractor.close();

        extractor = ExtractorFactory.createExtractor(new FileInputStream(doc6));
        assertTrue(
                extractor.getClass().getName(),
                extractor
                instanceof Word6Extractor
        );
        assertTrue(
                extractor.getText().length() > 20
        );
        extractor.close();

        extractor = ExtractorFactory.createExtractor(new FileInputStream(doc95));
        assertTrue(
                extractor.getClass().getName(),
                extractor
                instanceof Word6Extractor
        );
        assertTrue(
                extractor.getText().length() > 120
        );
        extractor.close();

        extractor = ExtractorFactory.createExtractor(new FileInputStream(docx));
        assertTrue(
                extractor
                instanceof XWPFWordExtractor
        );
        assertTrue(
                extractor.getText().length() > 120
        );
        extractor.close();

        // PowerPoint
        extractor = ExtractorFactory.createExtractor(new FileInputStream(ppt));
        assertTrue(
                extractor
                instanceof PowerPointExtractor
        );
        assertTrue(
                extractor.getText().length() > 120
        );
        extractor.close();

        extractor = ExtractorFactory.createExtractor(new FileInputStream(pptx));
        assertTrue(
                extractor
                instanceof XSLFPowerPointExtractor
        );
        assertTrue(
                extractor.getText().length() > 120
        );
        extractor.close();

        // Visio
        extractor = ExtractorFactory.createExtractor(new FileInputStream(vsd));
        assertTrue(
                extractor
                instanceof VisioTextExtractor
        );
        assertTrue(
                extractor.getText().length() > 50
        );
        extractor.close();

        // Visio - vsdx
        extractor = ExtractorFactory.createExtractor(new FileInputStream(vsdx));
        assertTrue(
                extractor
                instanceof XDGFVisioExtractor
        );
        assertTrue(
                extractor.getText().length() > 20
        );
        extractor.close();
        
        // Publisher
        extractor = ExtractorFactory.createExtractor(new FileInputStream(pub));
        assertTrue(
                extractor
                instanceof PublisherTextExtractor
        );
        assertTrue(
                extractor.getText().length() > 50
        );
        extractor.close();

        // Outlook msg
        extractor = ExtractorFactory.createExtractor(new FileInputStream(msg));
        assertTrue(
                extractor
                instanceof OutlookTextExtactor
        );
        assertTrue(
                extractor.getText().length() > 50
        );
        extractor.close();

        // Text
        try {
            FileInputStream stream = new FileInputStream(txt);
            try {
                ExtractorFactory.createExtractor(stream);
                fail();
            } finally {
                IOUtils.closeQuietly(stream);
            }
        } catch(IllegalArgumentException e) {
            // Good
        }
    }

    @Test
    public void testPOIFS() throws Exception {
        // Excel
        assertTrue(
                ExtractorFactory.createExtractor(new POIFSFileSystem(new FileInputStream(xls)))
                instanceof ExcelExtractor
        );
        assertTrue(
                ExtractorFactory.createExtractor(new POIFSFileSystem(new FileInputStream(xls))).getText().length() > 200
        );

        // Word
        assertTrue(
                ExtractorFactory.createExtractor(new POIFSFileSystem(new FileInputStream(doc)))
                instanceof WordExtractor
        );
        assertTrue(
                ExtractorFactory.createExtractor(new POIFSFileSystem(new FileInputStream(doc))).getText().length() > 120
        );

        assertTrue(
                ExtractorFactory.createExtractor(new POIFSFileSystem(new FileInputStream(doc6)))
                instanceof Word6Extractor
        );
        assertTrue(
                ExtractorFactory.createExtractor(new POIFSFileSystem(new FileInputStream(doc6))).getText().length() > 20
        );

        assertTrue(
                ExtractorFactory.createExtractor(new POIFSFileSystem(new FileInputStream(doc95)))
                instanceof Word6Extractor
        );
        assertTrue(
                ExtractorFactory.createExtractor(new POIFSFileSystem(new FileInputStream(doc95))).getText().length() > 120
        );

        // PowerPoint
        assertTrue(
                ExtractorFactory.createExtractor(new POIFSFileSystem(new FileInputStream(ppt)))
                instanceof PowerPointExtractor
        );
        assertTrue(
                ExtractorFactory.createExtractor(new POIFSFileSystem(new FileInputStream(ppt))).getText().length() > 120
        );

        // Visio
        assertTrue(
                ExtractorFactory.createExtractor(new POIFSFileSystem(new FileInputStream(vsd)))
                instanceof VisioTextExtractor
        );
        assertTrue(
                ExtractorFactory.createExtractor(new POIFSFileSystem(new FileInputStream(vsd))).getText().length() > 50
        );

        // Publisher
        assertTrue(
                ExtractorFactory.createExtractor(new POIFSFileSystem(new FileInputStream(pub)))
                instanceof PublisherTextExtractor
        );
        assertTrue(
                ExtractorFactory.createExtractor(new POIFSFileSystem(new FileInputStream(pub))).getText().length() > 50
        );

        // Outlook msg
        assertTrue(
                ExtractorFactory.createExtractor(new POIFSFileSystem(new FileInputStream(msg)))
                instanceof OutlookTextExtactor
        );
        assertTrue(
                ExtractorFactory.createExtractor(new POIFSFileSystem(new FileInputStream(msg))).getText().length() > 50
        );

        // Text
        try {
            ExtractorFactory.createExtractor(new POIFSFileSystem(new FileInputStream(txt)));
            fail();
        } catch(IOException e) {
            // Good
        }
    }


    @Test
    public void testOPOIFS() throws Exception {
        // Excel
        assertTrue(
                ExtractorFactory.createExtractor(new OPOIFSFileSystem(new FileInputStream(xls)))
                        instanceof ExcelExtractor
        );
        assertTrue(
                ExtractorFactory.createExtractor(new OPOIFSFileSystem(new FileInputStream(xls))).getText().length() > 200
        );

        // Word
        assertTrue(
                ExtractorFactory.createExtractor(new OPOIFSFileSystem(new FileInputStream(doc)))
                        instanceof WordExtractor
        );
        assertTrue(
                ExtractorFactory.createExtractor(new OPOIFSFileSystem(new FileInputStream(doc))).getText().length() > 120
        );

        assertTrue(
                ExtractorFactory.createExtractor(new OPOIFSFileSystem(new FileInputStream(doc6)))
                        instanceof Word6Extractor
        );
        assertTrue(
                ExtractorFactory.createExtractor(new OPOIFSFileSystem(new FileInputStream(doc6))).getText().length() > 20
        );

        assertTrue(
                ExtractorFactory.createExtractor(new OPOIFSFileSystem(new FileInputStream(doc95)))
                        instanceof Word6Extractor
        );
        assertTrue(
                ExtractorFactory.createExtractor(new OPOIFSFileSystem(new FileInputStream(doc95))).getText().length() > 120
        );

        // PowerPoint
        assertTrue(
                ExtractorFactory.createExtractor(new OPOIFSFileSystem(new FileInputStream(ppt)))
                        instanceof PowerPointExtractor
        );
        assertTrue(
                ExtractorFactory.createExtractor(new OPOIFSFileSystem(new FileInputStream(ppt))).getText().length() > 120
        );

        // Visio
        assertTrue(
                ExtractorFactory.createExtractor(new OPOIFSFileSystem(new FileInputStream(vsd)))
                        instanceof VisioTextExtractor
        );
        assertTrue(
                ExtractorFactory.createExtractor(new OPOIFSFileSystem(new FileInputStream(vsd))).getText().length() > 50
        );

        // Publisher
        assertTrue(
                ExtractorFactory.createExtractor(new OPOIFSFileSystem(new FileInputStream(pub)))
                        instanceof PublisherTextExtractor
        );
        assertTrue(
                ExtractorFactory.createExtractor(new OPOIFSFileSystem(new FileInputStream(pub))).getText().length() > 50
        );

        // Outlook msg
        assertTrue(
                ExtractorFactory.createExtractor(new OPOIFSFileSystem(new FileInputStream(msg)))
                        instanceof OutlookTextExtactor
        );
        assertTrue(
                ExtractorFactory.createExtractor(new OPOIFSFileSystem(new FileInputStream(msg))).getText().length() > 50
        );

        // Text
        try {
            ExtractorFactory.createExtractor(new OPOIFSFileSystem(new FileInputStream(txt)));
            fail();
        } catch(IOException e) {
            // Good
        }
    }

    @Test
    public void testPackage() throws Exception {
        // Excel
        POIXMLTextExtractor extractor = ExtractorFactory.createExtractor(OPCPackage.open(xlsx.toString(), PackageAccess.READ));
        assertTrue(extractor instanceof XSSFExcelExtractor);
        extractor.close();
        extractor = ExtractorFactory.createExtractor(OPCPackage.open(xlsx.toString()));
        assertTrue(extractor.getText().length() > 200);
        extractor.close();

        // Word
        extractor = ExtractorFactory.createExtractor(OPCPackage.open(docx.toString()));
        assertTrue(extractor instanceof XWPFWordExtractor);
        extractor.close();

        extractor = ExtractorFactory.createExtractor(OPCPackage.open(docx.toString()));
        assertTrue(extractor.getText().length() > 120);
        extractor.close();

        // PowerPoint
        extractor = ExtractorFactory.createExtractor(OPCPackage.open(pptx.toString()));
        assertTrue(extractor instanceof XSLFPowerPointExtractor);
        extractor.close();

        extractor = ExtractorFactory.createExtractor(OPCPackage.open(pptx.toString()));
        assertTrue(extractor.getText().length() > 120);
        extractor.close();
        
        // Visio
        extractor = ExtractorFactory.createExtractor(OPCPackage.open(vsdx.toString()));
        assertTrue(extractor instanceof XDGFVisioExtractor);
        assertTrue(extractor.getText().length() > 20);
        extractor.close();

        // Text
        try {
            ExtractorFactory.createExtractor(OPCPackage.open(txt.toString()));
            fail("TestExtractorFactory.testPackage() failed on " + txt);
        } catch(UnsupportedFileFormatException e) {
            // Good
        } catch (Exception e) {
            System.out.println("TestExtractorFactory.testPackage() failed on " + txt);
            throw e;
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
     * Test embeded docs text extraction. For now, only
     *  does poifs embeded, but will do ooxml ones 
     *  at some point.
     */
    @Test
    public void testEmbeded() throws Exception {
        POIOLE2TextExtractor ext;
        POITextExtractor[] embeds;

        // No embedings
        ext = (POIOLE2TextExtractor)
                ExtractorFactory.createExtractor(xls);
        embeds = ExtractorFactory.getEmbededDocsTextExtractors(ext);
        assertEquals(0, embeds.length);
        ext.close();

        // Excel
        ext = (POIOLE2TextExtractor)
                ExtractorFactory.createExtractor(xlsEmb);
        embeds = ExtractorFactory.getEmbededDocsTextExtractors(ext);

        assertEquals(6, embeds.length);
        int numWord = 0, numXls = 0, numPpt = 0, numMsg = 0, numWordX;
        for (POITextExtractor embed : embeds) {
            assertTrue(embed.getText().length() > 20);

            if (embed instanceof PowerPointExtractor) numPpt++;
            else if (embed instanceof ExcelExtractor) numXls++;
            else if (embed instanceof WordExtractor) numWord++;
            else if (embed instanceof OutlookTextExtactor) numMsg++;
        }
        assertEquals(2, numPpt);
        assertEquals(2, numXls);
        assertEquals(2, numWord);
        assertEquals(0, numMsg);
        ext.close();

        // Word
        ext = (POIOLE2TextExtractor)
                ExtractorFactory.createExtractor(docEmb);
        embeds = ExtractorFactory.getEmbededDocsTextExtractors(ext);

        numWord = 0; numXls = 0; numPpt = 0; numMsg = 0;
        assertEquals(4, embeds.length);
        for (POITextExtractor embed : embeds) {
            assertTrue(embed.getText().length() > 20);
            if (embed instanceof PowerPointExtractor) numPpt++;
            else if (embed instanceof ExcelExtractor) numXls++;
            else if (embed instanceof WordExtractor) numWord++;
            else if (embed instanceof OutlookTextExtactor) numMsg++;
        }
        assertEquals(1, numPpt);
        assertEquals(2, numXls);
        assertEquals(1, numWord);
        assertEquals(0, numMsg);
        ext.close();

        // Word which contains an OOXML file
        ext = (POIOLE2TextExtractor)
                ExtractorFactory.createExtractor(docEmbOOXML);
        embeds = ExtractorFactory.getEmbededDocsTextExtractors(ext);

        numWord = 0; numXls = 0; numPpt = 0; numMsg = 0; numWordX = 0;
        assertEquals(3, embeds.length);
        for (POITextExtractor embed : embeds) {
            assertTrue(embed.getText().length() > 20);
            if (embed instanceof PowerPointExtractor) numPpt++;
            else if (embed instanceof ExcelExtractor) numXls++;
            else if (embed instanceof WordExtractor) numWord++;
            else if (embed instanceof OutlookTextExtactor) numMsg++;
            else if (embed instanceof XWPFWordExtractor) numWordX++;
        }
        assertEquals(1, numPpt);
        assertEquals(1, numXls);
        assertEquals(0, numWord);
        assertEquals(1, numWordX);
        assertEquals(0, numMsg);
        ext.close();

        // Outlook
        ext = (OutlookTextExtactor)
                ExtractorFactory.createExtractor(msgEmb);
        embeds = ExtractorFactory.getEmbededDocsTextExtractors(ext);

        numWord = 0; numXls = 0; numPpt = 0; numMsg = 0;
        assertEquals(1, embeds.length);
        for (POITextExtractor embed : embeds) {
            assertTrue(embed.getText().length() > 20);
            if (embed instanceof PowerPointExtractor) numPpt++;
            else if (embed instanceof ExcelExtractor) numXls++;
            else if (embed instanceof WordExtractor) numWord++;
            else if (embed instanceof OutlookTextExtactor) numMsg++;
        }
        assertEquals(0, numPpt);
        assertEquals(0, numXls);
        assertEquals(1, numWord);
        assertEquals(0, numMsg);
        ext.close();

        // Outlook with another outlook file in it
        ext = (OutlookTextExtactor)
                ExtractorFactory.createExtractor(msgEmbMsg);
        embeds = ExtractorFactory.getEmbededDocsTextExtractors(ext);

        numWord = 0; numXls = 0; numPpt = 0; numMsg = 0;
        assertEquals(1, embeds.length);
        for (POITextExtractor embed : embeds) {
            assertTrue(embed.getText().length() > 20);
            if (embed instanceof PowerPointExtractor) numPpt++;
            else if (embed instanceof ExcelExtractor) numXls++;
            else if (embed instanceof WordExtractor) numWord++;
            else if (embed instanceof OutlookTextExtactor) numMsg++;
        }
        assertEquals(0, numPpt);
        assertEquals(0, numXls);
        assertEquals(0, numWord);
        assertEquals(1, numMsg);
        ext.close();

        // TODO - PowerPoint
        // TODO - Publisher
        // TODO - Visio
    }

    private static final String[] EXPECTED_FAILURES = new String[] {
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
    public void testFileLeak() throws Exception {
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
    @Test
    public void bug59074() throws Exception {
        try {
            ExtractorFactory.createExtractor(
                    POIDataSamples.getSpreadSheetInstance().getFile("59074.xls"));
            fail("Old excel formats not supported via ExtractorFactory");
        } catch (OldExcelFormatException e) {
            // expected here
        }
    }

    @Test
    public void testGetEmbeddedFromXMLExtractor() {
        try {
            // currently not implemented
            ExtractorFactory.getEmbededDocsTextExtractors((POIXMLTextExtractor)null);
            fail("Unsupported currently");
        } catch (IllegalStateException e) {
            // expected here
        }
    }
    
    // This bug is currently open. This test will fail with "expected error not thrown" when the bug has been fixed.
    // When this happens, change this from @Test(expected=...) to @Test
    // bug 45565: text within TextBoxes is extracted by ExcelExtractor and WordExtractor
    @Test(expected=AssertionError.class)
    public void test45565() throws Exception {
        POITextExtractor extractor = ExtractorFactory.createExtractor(HSSFTestDataSamples.getSampleFile("45565.xls"));
        try {
            String text = extractor.getText();
            assertContains(text, "testdoc");
            assertContains(text, "test phrase");
        } finally {
            extractor.close();
        }
    }
}
