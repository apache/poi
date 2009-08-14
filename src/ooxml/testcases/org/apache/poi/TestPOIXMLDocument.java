
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

import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.poi.util.TempFile;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;

import junit.framework.TestCase;

/**
 * Test recursive read and write of OPC packages
 */
public class TestPOIXMLDocument extends TestCase
{
    private static class OPCParser extends POIXMLDocument {

        public OPCParser(OPCPackage pkg) throws IOException {
            super(pkg);
        }

        public List<PackagePart> getAllEmbedds() {
            throw new RuntimeException("not supported");
        }

        public void parse(POIXMLFactory factory) throws OpenXML4JException, IOException{
            load(factory);
        }
    }

    private static class TestFactory extends POIXMLFactory  {

        public POIXMLDocumentPart createDocumentPart(PackageRelationship rel, PackagePart part){
            return new POIXMLDocumentPart(part, rel);
        }

        public POIXMLDocumentPart newDocumentPart(POIXMLRelation descriptor){
            throw new RuntimeException("not supported");
        }

    }

    /**
     * Recursively traverse a OOXML document and assert that same logical parts have the same physical instances
     */
    private static void traverse(POIXMLDocumentPart part, HashMap<String,POIXMLDocumentPart> context) throws IOException{
    	context.put(part.getPackageRelationship().getTargetURI().toString(), part);
    	for(POIXMLDocumentPart p : part.getRelations()){
            String uri = p.getPackageRelationship().getTargetURI().toString();
            if (!context.containsKey(uri)) {
    			traverse(p, context);
    		} else {
                POIXMLDocumentPart prev = context.get(uri);
                assertSame("Duplicate POIXMLDocumentPart instance for targetURI=" + uri, prev, p);
            }
    	}
    }

    public void assertReadWrite(String path) throws Exception {

        OPCPackage pkg1 = OPCPackage.open(path);
        OPCParser doc = new OPCParser(pkg1);
        doc.parse(new TestFactory());

        HashMap<String,POIXMLDocumentPart> context = new HashMap<String,POIXMLDocumentPart>();
        traverse(doc, context);
        context.clear();

        File tmp = TempFile.createTempFile("poi-ooxml", ".tmp");
        FileOutputStream out = new FileOutputStream(tmp);
        doc.write(out);
        out.close();

        OPCPackage pkg2 = OPCPackage.open(tmp.getAbsolutePath());

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
    }

    public void testPPTX() throws Exception {
        File file = new File(System.getProperty("OOXML.testdata.path"), "PPTWithAttachments.pptm");
        assertReadWrite(file.getAbsolutePath());
    }

    public void testXLSX() throws Exception {
        File file = new File(System.getProperty("OOXML.testdata.path"), "ExcelWithAttachments.xlsm");
        assertReadWrite(file.getAbsolutePath());
    }

    public void testDOCX() throws Exception {
        File file = new File(System.getProperty("OOXML.testdata.path"), "WordWithAttachments.docx");
        assertReadWrite(file.getAbsolutePath());
    }
}