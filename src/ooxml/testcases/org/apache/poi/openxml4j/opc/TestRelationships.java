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

package org.apache.poi.openxml4j.opc;

import java.io.*;

import junit.framework.TestCase;

import org.apache.poi.openxml4j.OpenXML4JTestDataSamples;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.POILogFactory;


public class TestRelationships extends TestCase {
	private static final String HYPERLINK_REL_TYPE =
		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/hyperlink";
	private static final String COMMENTS_REL_TYPE =
		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/comments";
	private static final String SHEET_WITH_COMMENTS =
		"/xl/worksheets/sheet1.xml";

    private static final POILogger logger = POILogFactory.getLogger(TestPackageCoreProperties.class);

    /**
     * Test relationships are correctly loaded. This at the moment fails (as of r499)
     * whenever a document is loaded before its correspondig .rels file has been found.
     * The code in this case assumes there are no relationships defined, but it should
     * really look also for not yet loaded parts.
     */
    public void testLoadRelationships() throws Exception {
        InputStream is = OpenXML4JTestDataSamples.openSampleStream("sample.xlsx");
        OPCPackage pkg = OPCPackage.open(is);
        logger.log(POILogger.DEBUG, "1: " + pkg);
        PackageRelationshipCollection rels = pkg.getRelationshipsByType(PackageRelationshipTypes.CORE_DOCUMENT);
        PackageRelationship coreDocRelationship = rels.getRelationship(0);
        PackagePart corePart = pkg.getPart(coreDocRelationship);
        String relIds[] = { "rId1", "rId2", "rId3" };
        for (String relId : relIds) {
            PackageRelationship rel = corePart.getRelationship(relId);
            assertNotNull(rel);
            PackagePartName relName = PackagingURIHelper.createPartName(rel.getTargetURI());
            PackagePart sheetPart = pkg.getPart(relName);
            assertEquals("Number of relationships1 for " + sheetPart.getPartName(), 1, sheetPart.getRelationships().size());
        }
    }
    
    /**
     * Checks that we can fetch a collection of relations by
     *  type, then grab from within there by id
     */
    public void testFetchFromCollection() throws Exception {
        InputStream is = OpenXML4JTestDataSamples.openSampleStream("ExcelWithHyperlinks.xlsx");
        OPCPackage pkg = OPCPackage.open(is);
        PackagePart sheet = pkg.getPart(
        		PackagingURIHelper.createPartName(SHEET_WITH_COMMENTS));
        assertNotNull(sheet);
        
        assertTrue(sheet.hasRelationships());
        assertEquals(6, sheet.getRelationships().size());
        
        // Should have three hyperlinks, and one comment
        PackageRelationshipCollection hyperlinks =
        	sheet.getRelationshipsByType(HYPERLINK_REL_TYPE);
        PackageRelationshipCollection comments =
        	sheet.getRelationshipsByType(COMMENTS_REL_TYPE);
        assertEquals(3, hyperlinks.size());
        assertEquals(1, comments.size());
        
        // Check we can get bits out by id
        // Hyperlinks are rId1, rId2 and rId3
        // Comment is rId6
        assertNotNull(hyperlinks.getRelationshipByID("rId1"));
        assertNotNull(hyperlinks.getRelationshipByID("rId2"));
        assertNotNull(hyperlinks.getRelationshipByID("rId3"));
        assertNull(hyperlinks.getRelationshipByID("rId6"));
        
        assertNull(comments.getRelationshipByID("rId1"));
        assertNull(comments.getRelationshipByID("rId2"));
        assertNull(comments.getRelationshipByID("rId3"));
        assertNotNull(comments.getRelationshipByID("rId6"));
        
        assertNotNull(sheet.getRelationship("rId1"));
        assertNotNull(sheet.getRelationship("rId2"));
        assertNotNull(sheet.getRelationship("rId3"));
        assertNotNull(sheet.getRelationship("rId6"));
    }
    
    /**
     * Excel uses relations on sheets to store the details of 
     *  external hyperlinks. Check we can load these ok.
     */
    public void testLoadExcelHyperlinkRelations() throws Exception {
        InputStream is = OpenXML4JTestDataSamples.openSampleStream("ExcelWithHyperlinks.xlsx");
        OPCPackage pkg = OPCPackage.open(is);
	    PackagePart sheet = pkg.getPart(
	    		PackagingURIHelper.createPartName(SHEET_WITH_COMMENTS));
	    assertNotNull(sheet);

	    // rId1 is url
	    PackageRelationship url = sheet.getRelationship("rId1");
	    assertNotNull(url);
	    assertEquals("rId1", url.getId());
	    assertEquals("/xl/worksheets/sheet1.xml", url.getSourceURI().toString());
	    assertEquals("http://poi.apache.org/", url.getTargetURI().toString());
	    
	    // rId2 is file
	    PackageRelationship file = sheet.getRelationship("rId2");
	    assertNotNull(file);
	    assertEquals("rId2", file.getId());
	    assertEquals("/xl/worksheets/sheet1.xml", file.getSourceURI().toString());
	    assertEquals("WithVariousData.xlsx", file.getTargetURI().toString());
	    
	    // rId3 is mailto
	    PackageRelationship mailto = sheet.getRelationship("rId3");
	    assertNotNull(mailto);
	    assertEquals("rId3", mailto.getId());
	    assertEquals("/xl/worksheets/sheet1.xml", mailto.getSourceURI().toString());
	    assertEquals("mailto:dev@poi.apache.org?subject=XSSF%20Hyperlinks", mailto.getTargetURI().toString());
    }
    
    /*
     * Excel uses relations on sheets to store the details of 
     *  external hyperlinks. Check we can create these OK, 
     *  then still read them later
     */
    public void testCreateExcelHyperlinkRelations() throws Exception {
    	String filepath = OpenXML4JTestDataSamples.getSampleFileName("ExcelWithHyperlinks.xlsx");
	    OPCPackage pkg = OPCPackage.open(filepath, PackageAccess.READ_WRITE);
	    PackagePart sheet = pkg.getPart(
	    		PackagingURIHelper.createPartName(SHEET_WITH_COMMENTS));
	    assertNotNull(sheet);
	    
	    assertEquals(3, sheet.getRelationshipsByType(HYPERLINK_REL_TYPE).size());
	    
	    // Add three new ones
	    PackageRelationship openxml4j =
	    	sheet.addExternalRelationship("http://www.openxml4j.org/", HYPERLINK_REL_TYPE);
	    PackageRelationship sf =
	    	sheet.addExternalRelationship("http://openxml4j.sf.net/", HYPERLINK_REL_TYPE);
	    PackageRelationship file =
	    	sheet.addExternalRelationship("MyDocument.docx", HYPERLINK_REL_TYPE);
	    
	    // Check they were added properly
	    assertNotNull(openxml4j);
	    assertNotNull(sf);
	    assertNotNull(file);
	    
	    assertEquals(6, sheet.getRelationshipsByType(HYPERLINK_REL_TYPE).size());
	    
	    assertEquals("http://www.openxml4j.org/", openxml4j.getTargetURI().toString());
	    assertEquals("/xl/worksheets/sheet1.xml", openxml4j.getSourceURI().toString());
	    assertEquals(HYPERLINK_REL_TYPE, openxml4j.getRelationshipType());
	    
	    assertEquals("http://openxml4j.sf.net/", sf.getTargetURI().toString());
	    assertEquals("/xl/worksheets/sheet1.xml", sf.getSourceURI().toString());
	    assertEquals(HYPERLINK_REL_TYPE, sf.getRelationshipType());
	    
	    assertEquals("MyDocument.docx", file.getTargetURI().toString());
	    assertEquals("/xl/worksheets/sheet1.xml", file.getSourceURI().toString());
	    assertEquals(HYPERLINK_REL_TYPE, file.getRelationshipType());
	    
	    // Will get ids 7, 8 and 9, as we already have 1-6
	    assertEquals("rId7", openxml4j.getId());
	    assertEquals("rId8", sf.getId());
	    assertEquals("rId9", file.getId());
	    
	    
	    // Write out and re-load
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    pkg.save(baos);
	    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
	    pkg = OPCPackage.open(bais);
	    
	    // Check again
	    sheet = pkg.getPart(
	    		PackagingURIHelper.createPartName(SHEET_WITH_COMMENTS));
	    
	    assertEquals(6, sheet.getRelationshipsByType(HYPERLINK_REL_TYPE).size());
	    
	    assertEquals("http://poi.apache.org/",
	    		sheet.getRelationship("rId1").getTargetURI().toString());
	    assertEquals("mailto:dev@poi.apache.org?subject=XSSF%20Hyperlinks",
	    		sheet.getRelationship("rId3").getTargetURI().toString());
	    
	    assertEquals("http://www.openxml4j.org/",
	    		sheet.getRelationship("rId7").getTargetURI().toString());
	    assertEquals("http://openxml4j.sf.net/",
	    		sheet.getRelationship("rId8").getTargetURI().toString());
	    assertEquals("MyDocument.docx",
	    		sheet.getRelationship("rId9").getTargetURI().toString());
    }

    public void testCreateRelationsFromScratch() throws Exception {
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	OPCPackage pkg = OPCPackage.create(baos);
    	
    	PackagePart partA =
    		pkg.createPart(PackagingURIHelper.createPartName("/partA"), "text/plain");
    	PackagePart partB =
    		pkg.createPart(PackagingURIHelper.createPartName("/partB"), "image/png");
    	assertNotNull(partA);
    	assertNotNull(partB);
    	
    	// Internal
    	partA.addRelationship(partB.getPartName(), TargetMode.INTERNAL, "http://example/Rel");
    	
    	// External
    	partA.addExternalRelationship("http://poi.apache.org/", "http://example/poi");
    	partB.addExternalRelationship("http://poi.apache.org/ss/", "http://example/poi/ss");

    	// Check as expected currently
    	assertEquals("/partB", partA.getRelationship("rId1").getTargetURI().toString());
    	assertEquals("http://poi.apache.org/", 
    			partA.getRelationship("rId2").getTargetURI().toString());
    	assertEquals("http://poi.apache.org/ss/", 
    			partB.getRelationship("rId1").getTargetURI().toString());
    	
    	
    	// Save, and re-load
    	pkg.close();
    	ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    	pkg = OPCPackage.open(bais);
    	
    	partA = pkg.getPart(PackagingURIHelper.createPartName("/partA"));
    	partB = pkg.getPart(PackagingURIHelper.createPartName("/partB"));
    	
    	
    	// Check the relations
    	assertEquals(2, partA.getRelationships().size());
    	assertEquals(1, partB.getRelationships().size());
    	
    	assertEquals("/partB", partA.getRelationship("rId1").getTargetURI().toString());
    	assertEquals("http://poi.apache.org/", 
    			partA.getRelationship("rId2").getTargetURI().toString());
    	assertEquals("http://poi.apache.org/ss/", 
    			partB.getRelationship("rId1").getTargetURI().toString());
    	// Check core too
    	assertEquals("/docProps/core.xml",
    			pkg.getRelationshipsByType("http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties").getRelationship(0).getTargetURI().toString());
    }

}
