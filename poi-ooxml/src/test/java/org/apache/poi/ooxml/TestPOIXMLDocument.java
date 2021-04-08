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

package org.apache.poi.ooxml;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.poi.POIDataSamples;
import org.apache.poi.ooxml.POIXMLDocumentPart.RelationPart;
import org.apache.poi.ooxml.util.PackageHelper;
import org.apache.poi.openxml4j.exceptions.OpenXML4JRuntimeException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationshipTypes;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.NullOutputStream;
import org.apache.poi.util.TempFile;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xssf.usermodel.XSSFRelation;
import org.apache.poi.xwpf.usermodel.XWPFRelation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

/**
 * Test recursive read and write of OPC packages
 */
public final class TestPOIXMLDocument {

    private static class OPCParser extends POIXMLDocument {

        public OPCParser(OPCPackage pkg) {
            super(pkg);
        }

        public OPCParser(OPCPackage pkg, String coreDocumentRel) {
            super(pkg, coreDocumentRel);
        }

        @Override
        public List<PackagePart> getAllEmbeddedParts() {
            throw new RuntimeException("not supported");
        }

        void parse(POIXMLFactory factory) throws IOException{
            load(factory);
        }
    }

    private static final class TestFactory extends POIXMLFactory {

        public TestFactory() {
            //
        }

        @Override
        protected POIXMLRelation getDescriptor(String relationshipType) {
            return null;
        }
    }

    private static void traverse(POIXMLDocument doc) {
        HashMap<String,POIXMLDocumentPart> context = new HashMap<>();
        for (RelationPart p : doc.getRelationParts()){
            traverse(p, context);
        }
    }

    /**
     * Recursively traverse a OOXML document and assert that same logical parts have the same physical instances
     */
    private static void traverse(RelationPart rp, HashMap<String,POIXMLDocumentPart> context) {
        POIXMLDocumentPart dp = rp.getDocumentPart();
        assertEquals(rp.getRelationship().getTargetURI().toString(), dp.getPackagePart().getPartName().getName());

        context.put(dp.getPackagePart().getPartName().getName(), dp);
        for(RelationPart p : dp.getRelationParts()){
            assertNotNull(p.getRelationship().toString());

            String uri = p.getDocumentPart().getPackagePart().getPartName().getURI().toString();
            assertEquals(uri, p.getRelationship().getTargetURI().toString());
            if (!context.containsKey(uri)) {
                traverse(p, context);
            } else {
                POIXMLDocumentPart prev = context.get(uri);
                assertSame(prev, p.getDocumentPart(), "Duplicate POIXMLDocumentPart instance for targetURI=" + uri);
            }
        }
    }

    private void assertReadWrite(POIDataSamples samples, String filename) throws Exception {
        try (InputStream is = samples.openResourceAsStream(filename);
            OPCPackage pkg1 = PackageHelper.open(is)){
            File tmp = TempFile.createTempFile("poi-ooxml", ".tmp");
            try (FileOutputStream out = new FileOutputStream(tmp);
                OPCParser doc = new OPCParser(pkg1)) {
                doc.parse(new TestFactory());
                traverse(doc);
                doc.write(out);
                out.close();

                // Should not be able to write to an output stream that has been closed
                // FIXME: A better exception class (IOException?) and message should be raised
                // indicating that the document could not be written because the output stream is closed.
                // see {@link org.apache.poi.openxml4j.opc.ZipPackage#saveImpl(java.io.OutputStream)}
                OpenXML4JRuntimeException e = assertThrows(OpenXML4JRuntimeException.class, () -> doc.write(out),
                    "Should not be able to write to an output stream that has been closed.");
                assertTrue(e.getMessage().matches("Fail to save: an error occurs while saving the package : " +
                    "The part .+ failed to be saved in the stream with marshaller .+"));

                // Should not be able to write a document that has been closed
                doc.close();
                IOException e2 = assertThrows(IOException.class, () -> doc.write(new NullOutputStream()),
                    "Should not be able to write a document that has been closed.");
                assertEquals("Cannot write data, document seems to have been closed already", e2.getMessage());

                // Should be able to close a document multiple times, though subsequent closes will have no effect.
            }


            try (OPCPackage pkg2 = OPCPackage.open(tmp.getAbsolutePath());
                 OPCParser doc = new OPCParser(pkg1)) {
                doc.parse(new TestFactory());
                traverse(doc);

                assertEquals(pkg1.getRelationships().size(), pkg2.getRelationships().size());

                ArrayList<PackagePart> l1 = pkg1.getParts();
                ArrayList<PackagePart> l2 = pkg2.getParts();

                assertEquals(l1.size(), l2.size());
                for (int i = 0; i < l1.size(); i++) {
                    PackagePart p1 = l1.get(i);
                    PackagePart p2 = l2.get(i);

                    assertEquals(p1.getContentType(), p2.getContentType());
                    assertEquals(p1.hasRelationships(), p2.hasRelationships());
                    if (p1.hasRelationships()) {
                        assertEquals(p1.getRelationships().size(), p2.getRelationships().size());
                    }
                    assertEquals(p1.getPartName(), p2.getPartName());
                }
            }
        }
    }

    @Test
    void testPPTX() throws Exception {
        assertReadWrite(POIDataSamples.getSlideShowInstance(), "PPTWithAttachments.pptm");
    }

    @Test
    void testXLSX() throws Exception {
        assertReadWrite(POIDataSamples.getSpreadSheetInstance(), "ExcelWithAttachments.xlsm");
    }

    @Test
    void testDOCX() throws Exception {
        assertReadWrite(POIDataSamples.getDocumentInstance(), "WordWithAttachments.docx");
    }

    @Test
    void testRelationOrder() throws Exception {
        POIDataSamples pds = POIDataSamples.getDocumentInstance();
        try (InputStream is = pds.openResourceAsStream("WordWithAttachments.docx");
            OPCPackage pkg = PackageHelper.open(is);
            OPCParser doc = new OPCParser(pkg)) {
            doc.parse(new TestFactory());

            for (POIXMLDocumentPart rel : doc.getRelations()) {
                //TODO finish me
                assertNotNull(rel);
            }
        }
    }

    @Test
    void testGetNextPartNumber() throws Exception {
        POIDataSamples pds = POIDataSamples.getDocumentInstance();
        try (InputStream is = pds.openResourceAsStream("WordWithAttachments.docx");
            OPCPackage pkg = PackageHelper.open(is);
            OPCParser doc = new OPCParser(pkg)) {
            doc.parse(new TestFactory());

            // Non-indexed parts: Word is taken, Excel is not
            assertEquals(-1, doc.getNextPartNumber(XWPFRelation.DOCUMENT, 0));
            assertEquals(-1, doc.getNextPartNumber(XWPFRelation.DOCUMENT, -1));
            assertEquals(-1, doc.getNextPartNumber(XWPFRelation.DOCUMENT, 99));
            assertEquals(0, doc.getNextPartNumber(XSSFRelation.WORKBOOK, 0));
            assertEquals(0, doc.getNextPartNumber(XSSFRelation.WORKBOOK, -1));
            assertEquals(0, doc.getNextPartNumber(XSSFRelation.WORKBOOK, 99));

            // Indexed parts:
            // Has 2 headers
            assertEquals(0, doc.getNextPartNumber(XWPFRelation.HEADER, 0));
            assertEquals(3, doc.getNextPartNumber(XWPFRelation.HEADER, -1));
            assertEquals(3, doc.getNextPartNumber(XWPFRelation.HEADER, 1));
            assertEquals(8, doc.getNextPartNumber(XWPFRelation.HEADER, 8));

            // Has no Excel Sheets
            assertEquals(0, doc.getNextPartNumber(XSSFRelation.WORKSHEET, 0));
            assertEquals(1, doc.getNextPartNumber(XSSFRelation.WORKSHEET, -1));
            assertEquals(1, doc.getNextPartNumber(XSSFRelation.WORKSHEET, 1));
        }
    }

    @Test
    void testCommitNullPart() throws IOException {
        POIXMLDocumentPart part = new POIXMLDocumentPart();
        part.prepareForCommit();
        part.commit();
        part.onSave(new HashSet<>());

        assertNull(part.getRelationById(null));
        assertNull(part.getRelationId(null));
        assertFalse(part.removeRelation(null, true));
        part.removeRelation((POIXMLDocumentPart)null);
        assertEquals("",part.toString());
        part.onDocumentCreate();
        //part.getTargetPart(null);
    }

    @Test
    void testVSDX() throws Exception {
        POIDataSamples pds = POIDataSamples.getDiagramInstance();
        try (InputStream is = pds.openResourceAsStream("test.vsdx");
             OPCPackage open = PackageHelper.open(is);
             POIXMLDocument part = new OPCParser(open, PackageRelationshipTypes.VISIO_CORE_DOCUMENT)) {
            assertNotNull(part);
            assertEquals(0, part.getRelationCounter());
        }
    }

    @Test
    void testVSDXPart() throws IOException {
        POIDataSamples pds = POIDataSamples.getDiagramInstance();
        try (InputStream is = pds.openResourceAsStream("test.vsdx");
            OPCPackage open = PackageHelper.open(is)) {

            POIXMLDocumentPart part = new POIXMLDocumentPart(open, PackageRelationshipTypes.VISIO_CORE_DOCUMENT);

            assertNotNull(part);
            assertEquals(0, part.getRelationCounter());
        }
    }

    @Test
    void testInvalidCoreRel() throws IOException {
        POIDataSamples pds = POIDataSamples.getDiagramInstance();
        try (OPCPackage open = PackageHelper.open(pds.openResourceAsStream("test.vsdx"))) {
            assertThrows(POIXMLException.class, () -> new POIXMLDocumentPart(open, "somethingillegal"));
        }
    }

    @Test
    void dontParseEmbeddedDocuments() throws IOException {
        // bug #62513
        POIDataSamples pds = POIDataSamples.getSlideShowInstance();
        try (InputStream is = pds.openResourceAsStream("bug62513.pptx");
            XMLSlideShow ppt = new XMLSlideShow(is)) {
            POIXMLDocumentPart doc = ppt.getSlides().get(12).getRelationById("rId3");
            assertNotNull(doc);
            assertEquals(POIXMLDocumentPart.class, doc.getClass());
        }
    }

    @Test
    void testOSGIClassLoading() throws IOException {
        byte[] data;
        try (InputStream is = POIDataSamples.getSlideShowInstance().openResourceAsStream("table_test.pptx")) {
            data = IOUtils.toByteArray(is);
        }

        // the schema type loader is cached per thread in POIXMLTypeLoader.
        // So create a new Thread and change the context class loader (which would normally be used)
        // to not contain the OOXML classes
        Runnable run = () -> assertDoesNotThrow(() -> {
            try (XMLSlideShow ppt = new XMLSlideShow(new ByteArrayInputStream(data))) {
                assertNotNull(ppt.getSlides().get(0).getShapes());
            }}
        );

        ClassLoader cl = getClass().getClassLoader();
        UncaughtHandler uh = new UncaughtHandler();

        // check schema type loading and check if we could run in an OOM
        Thread[] ta = new Thread[30];
        for (int j=0; j<10; j++) {
            for (int i=0; i<ta.length; i++) {
                ta[i] = new Thread(run);
                ta[i].setContextClassLoader(cl.getParent());
                ta[i].setUncaughtExceptionHandler(uh);
                ta[i].start();
            }
            for (Thread thread : ta) {
                assertDoesNotThrow((Executable) thread::join, "failed to join thread");
            }
        }
        assertFalse(uh.hasException(), "Should not have an exception now, but had " + uh.e);
    }

    private static class UncaughtHandler implements UncaughtExceptionHandler {
        Throwable e;

        public synchronized void uncaughtException(Thread t, Throwable e) {
            this.e = e;
        }

        public synchronized boolean hasException() {
            return e != null;
        }
    }

}
