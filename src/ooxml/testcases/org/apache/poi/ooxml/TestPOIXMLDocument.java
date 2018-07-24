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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.poi.POIDataSamples;
import org.apache.poi.ooxml.POIXMLDocumentPart.RelationPart;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JRuntimeException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationshipTypes;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.NullOutputStream;
import org.apache.poi.ooxml.util.PackageHelper;
import org.apache.poi.util.TempFile;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xssf.usermodel.XSSFRelation;
import org.apache.poi.xwpf.usermodel.XWPFRelation;
import org.junit.Test;

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

        public void parse(POIXMLFactory factory) throws IOException{
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

        /**
         * @since POI 3.14-Beta1
         */
        @Override
        protected POIXMLDocumentPart createDocumentPart
            (Class<? extends POIXMLDocumentPart> cls, Class<?>[] classes, Object[] values)
        throws SecurityException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
            return null;
        }
    }

    private static void traverse(POIXMLDocument doc) throws IOException{
        HashMap<String,POIXMLDocumentPart> context = new HashMap<>();
        for (RelationPart p : doc.getRelationParts()){
            traverse(p, context);
        }
    }
    
    /**
     * Recursively traverse a OOXML document and assert that same logical parts have the same physical instances
     */
    private static void traverse(RelationPart rp, HashMap<String,POIXMLDocumentPart> context) throws IOException{
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
                assertSame("Duplicate POIXMLDocumentPart instance for targetURI=" + uri, prev, p.getDocumentPart());
            }
        }
    }

    public void assertReadWrite(OPCPackage pkg1) throws Exception {

        OPCParser doc = new OPCParser(pkg1);
        doc.parse(new TestFactory());

        traverse(doc);

        File tmp = TempFile.createTempFile("poi-ooxml", ".tmp");
        FileOutputStream out = new FileOutputStream(tmp);
        doc.write(out);
        out.close();
        
        // Should not be able to write to an output stream that has been closed
        try {
            doc.write(out);
            fail("Should not be able to write to an output stream that has been closed.");
        } catch (final OpenXML4JRuntimeException e) {
            // FIXME: A better exception class (IOException?) and message should be raised
            // indicating that the document could not be written because the output stream is closed.
            // see {@link org.apache.poi.openxml4j.opc.ZipPackage#saveImpl(java.io.OutputStream)}
            if (e.getMessage().matches("Fail to save: an error occurs while saving the package : The part .+ failed to be saved in the stream with marshaller .+")) {
                // expected
            } else {
                throw e;
            }
        }

        // Should not be able to write a document that has been closed
        doc.close();
        try {
            doc.write(new NullOutputStream());
            fail("Should not be able to write a document that has been closed.");
        } catch (final IOException e) {
            if (e.getMessage().equals("Cannot write data, document seems to have been closed already")) {
                // expected
            } else {
                throw e;
            }
        }
        
        // Should be able to close a document multiple times, though subsequent closes will have no effect.
        doc.close();


        @SuppressWarnings("resource")
        OPCPackage pkg2 = OPCPackage.open(tmp.getAbsolutePath());
        doc = new OPCParser(pkg1);
        try {
            doc.parse(new TestFactory());
            traverse(doc);
    
            assertEquals(pkg1.getRelationships().size(), pkg2.getRelationships().size());
    
            ArrayList<PackagePart> l1 = pkg1.getParts();
            ArrayList<PackagePart> l2 = pkg2.getParts();
    
            assertEquals(l1.size(), l2.size());
            for (int i=0; i < l1.size(); i++){
                PackagePart p1 = l1.get(i);
                PackagePart p2 = l2.get(i);
    
                assertEquals(p1.getContentType(), p2.getContentType());
                assertEquals(p1.hasRelationships(), p2.hasRelationships());
                if(p1.hasRelationships()){
                    assertEquals(p1.getRelationships().size(), p2.getRelationships().size());
                }
                assertEquals(p1.getPartName(), p2.getPartName());
            }
        } finally {
            doc.close();
            pkg1.close();
            pkg2.close();
        }
    }

    @Test
    public void testPPTX() throws Exception {
        POIDataSamples pds = POIDataSamples.getSlideShowInstance();
        assertReadWrite(PackageHelper.open(pds.openResourceAsStream("PPTWithAttachments.pptm")));
    }

    @Test
    public void testXLSX() throws Exception {
        POIDataSamples pds = POIDataSamples.getSpreadSheetInstance();
        assertReadWrite(PackageHelper.open(pds.openResourceAsStream("ExcelWithAttachments.xlsm")));
    }

    @Test
    public void testDOCX() throws Exception {
        POIDataSamples pds = POIDataSamples.getDocumentInstance();
        assertReadWrite(PackageHelper.open(pds.openResourceAsStream("WordWithAttachments.docx")));
    }

    @Test
    public void testRelationOrder() throws Exception {
        POIDataSamples pds = POIDataSamples.getDocumentInstance();
        @SuppressWarnings("resource")
        OPCPackage pkg = PackageHelper.open(pds.openResourceAsStream("WordWithAttachments.docx"));
        OPCParser doc = new OPCParser(pkg);
        try {
            doc.parse(new TestFactory());
    
            for(POIXMLDocumentPart rel : doc.getRelations()){
                //TODO finish me
                assertNotNull(rel);
            }
        } finally {
        	doc.close();
        }
    }
    
    @Test
    public void testGetNextPartNumber() throws Exception {
        POIDataSamples pds = POIDataSamples.getDocumentInstance();
        @SuppressWarnings("resource")
        OPCPackage pkg = PackageHelper.open(pds.openResourceAsStream("WordWithAttachments.docx"));
        OPCParser doc = new OPCParser(pkg);
        try {
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
        } finally {
            doc.close();
        }
    }

    @Test
    public void testCommitNullPart() throws IOException, InvalidFormatException {
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
    public void testVSDX() throws Exception {
        POIDataSamples pds = POIDataSamples.getDiagramInstance();
        @SuppressWarnings("resource")
        OPCPackage open = PackageHelper.open(pds.openResourceAsStream("test.vsdx"));
        POIXMLDocument part = new OPCParser(open, PackageRelationshipTypes.VISIO_CORE_DOCUMENT);
        
        assertNotNull(part);
        assertEquals(0, part.getRelationCounter());
        part.close();
    }
    
    @Test
    public void testVSDXPart() throws IOException {
        POIDataSamples pds = POIDataSamples.getDiagramInstance();
        OPCPackage open = PackageHelper.open(pds.openResourceAsStream("test.vsdx"));
        
        POIXMLDocumentPart part = new POIXMLDocumentPart(open, PackageRelationshipTypes.VISIO_CORE_DOCUMENT);
        
        assertNotNull(part);
        assertEquals(0, part.getRelationCounter());
        
        open.close();
    }
    
    @Test(expected=POIXMLException.class)
    public void testInvalidCoreRel() throws IOException {
        POIDataSamples pds = POIDataSamples.getDiagramInstance();
        OPCPackage open = PackageHelper.open(pds.openResourceAsStream("test.vsdx"));
        
        try {
            new POIXMLDocumentPart(open, "somethingillegal");
        } finally {
            open.close();
        }
    }

    @Test
    public void dontParseEmbeddedDocuments() throws IOException {
        // bug #62513
        POIDataSamples pds = POIDataSamples.getSlideShowInstance();
        try (InputStream is = pds.openResourceAsStream("bug62513.pptx");
            XMLSlideShow ppt = new XMLSlideShow(is)) {
            POIXMLDocumentPart doc = ppt.getSlides().get(12).getRelationById("rId3");
            assertEquals(POIXMLDocumentPart.class, doc.getClass());
        }
    }

    @Test
    public void testOSGIClassLoading() {
        // the schema type loader is cached per thread in POIXMLTypeLoader.
        // So create a new Thread and change the context class loader (which would normally be used)
        // to not contain the OOXML classes
        Runnable run = new Runnable() {
            public void run() {
                InputStream is = POIDataSamples.getSlideShowInstance().openResourceAsStream("table_test.pptx");
                XMLSlideShow ppt = null;
                try {
                    ppt = new XMLSlideShow(is);
                    ppt.getSlides().get(0).getShapes();
                } catch (IOException e) {
                    fail("failed to load XMLSlideShow");
                } finally {
                    IOUtils.closeQuietly(ppt);
                    IOUtils.closeQuietly(is);
                }
            }
        };

        ClassLoader cl = getClass().getClassLoader();
        UncaughtHandler uh = new UncaughtHandler();
        
        // check schema type loading and check if we could run in an OOM
        Thread ta[] = new Thread[30];
        for (int j=0; j<10; j++) {
            for (int i=0; i<ta.length; i++) {
                ta[i] = new Thread(run);
                ta[i].setContextClassLoader(cl.getParent());
                ta[i].setUncaughtExceptionHandler(uh);
                ta[i].start();
            }
            for (int i=0; i<ta.length; i++) {
                try {
                    ta[i].join();
                } catch (InterruptedException e) {
                    fail("failed to join thread");
                }
            }
        }
        assertFalse(uh.hasException());
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
