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

package org.apache.poi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationshipTypes;
import org.apache.poi.util.PackageHelper;
import org.apache.poi.util.TempFile;
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
        public List<PackagePart> getAllEmbedds() {
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

    /**
     * Recursively traverse a OOXML document and assert that same logical parts have the same physical instances
     */
    private static void traverse(POIXMLDocumentPart part, HashMap<String,POIXMLDocumentPart> context) throws IOException{
        assertEquals(part.getPackageRelationship().getTargetURI().toString(), part.getPackagePart().getPartName().getName());
        
        context.put(part.getPackagePart().getPartName().getName(), part);
        for(POIXMLDocumentPart p : part.getRelations()){
            assertNotNull(p.toString());
            
            String uri = p.getPackagePart().getPartName().getURI().toString();
            assertEquals(uri, p.getPackageRelationship().getTargetURI().toString());
            if (!context.containsKey(uri)) {
                traverse(p, context);
            } else {
                POIXMLDocumentPart prev = context.get(uri);
                assertSame("Duplicate POIXMLDocumentPart instance for targetURI=" + uri, prev, p);
            }
        }
    }

    public void assertReadWrite(OPCPackage pkg1) throws Exception {

        OPCParser doc = new OPCParser(pkg1);
        doc.parse(new TestFactory());

        HashMap<String,POIXMLDocumentPart> context = new HashMap<String,POIXMLDocumentPart>();
        traverse(doc, context);
        context.clear();

        File tmp = TempFile.createTempFile("poi-ooxml", ".tmp");
        FileOutputStream out = new FileOutputStream(tmp);
        doc.write(out);
        out.close();
        doc.close();

        @SuppressWarnings("resource")
        OPCPackage pkg2 = OPCPackage.open(tmp.getAbsolutePath());
        try {
            doc = new OPCParser(pkg1);
            doc.parse(new TestFactory());
            context = new HashMap<String,POIXMLDocumentPart>();
            traverse(doc, context);
            context.clear();
    
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
    public void testCommitNullPart() throws IOException, InvalidFormatException {
        POIXMLDocumentPart part = new POIXMLDocumentPart();
        part.prepareForCommit();
        part.commit();
        part.onSave(new HashSet<PackagePart>());

        assertNull(part.getRelationById(null));
        assertNull(part.getRelationId(null));
        assertFalse(part.removeRelation(null, true));
        part.removeRelation(null);
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
}
