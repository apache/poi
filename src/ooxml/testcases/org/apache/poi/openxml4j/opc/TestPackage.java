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

import org.apache.poi.*;
import org.apache.poi.extractor.ExtractorFactory;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.openxml4j.OpenXML4JTestDataSamples;
import org.apache.poi.openxml4j.exceptions.*;
import org.apache.poi.openxml4j.opc.internal.ContentTypeManager;
import org.apache.poi.openxml4j.opc.internal.FileHelper;
import org.apache.poi.openxml4j.opc.internal.PackagePropertiesPart;
import org.apache.poi.openxml4j.opc.internal.ZipHelper;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.util.*;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.xmlbeans.XmlException;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.*;

public final class TestPackage {
    private static final POILogger logger = POILogFactory.getLogger(TestPackage.class);

	/**
	 * Test that just opening and closing the file doesn't alter the document.
	 */
    @Test
	public void openSave() throws IOException, InvalidFormatException {
		String originalFile = OpenXML4JTestDataSamples.getSampleFileName("TestPackageCommon.docx");
		File targetFile = OpenXML4JTestDataSamples.getOutputFile("TestPackageOpenSaveTMP.docx");

		@SuppressWarnings("resource")
        OPCPackage p = OPCPackage.open(originalFile, PackageAccess.READ_WRITE);
		try {
    		p.save(targetFile.getAbsoluteFile());
    
    		// Compare the original and newly saved document
    		assertTrue(targetFile.exists());
    		ZipFileAssert.assertEquals(new File(originalFile), targetFile);
    		assertTrue(targetFile.delete());
		} finally {
            // use revert to not re-write the input file
            p.revert();
		}
	}

	/**
	 * Test that when we create a new Package, we give it
	 *  the correct default content types
	 */
    @Test
	public void createGetsContentTypes()
    throws IOException, InvalidFormatException, SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
		File targetFile = OpenXML4JTestDataSamples.getOutputFile("TestCreatePackageTMP.docx");

		// Zap the target file, in case of an earlier run
		if(targetFile.exists()) {
			assertTrue(targetFile.delete());
		}

		@SuppressWarnings("resource")
        OPCPackage pkg = OPCPackage.create(targetFile);

		// Check it has content types for rels and xml
		ContentTypeManager ctm = getContentTypeManager(pkg);
		assertEquals(
				"application/xml",
				ctm.getContentType(
						PackagingURIHelper.createPartName("/foo.xml")
				)
		);
		assertEquals(
				ContentTypes.RELATIONSHIPS_PART,
				ctm.getContentType(
						PackagingURIHelper.createPartName("/foo.rels")
				)
		);
		assertNull(
				ctm.getContentType(
						PackagingURIHelper.createPartName("/foo.txt")
				)
		);
		
		pkg.revert();
	}

	/**
	 * Test package creation.
	 */
    @Test
	public void createPackageAddPart() throws IOException, InvalidFormatException {
		File targetFile = OpenXML4JTestDataSamples.getOutputFile("TestCreatePackageTMP.docx");

		File expectedFile = OpenXML4JTestDataSamples.getSampleFile("TestCreatePackageOUTPUT.docx");

        // Zap the target file, in case of an earlier run
        if(targetFile.exists()) {
			assertTrue(targetFile.delete());
		}

        // Create a package
        OPCPackage pkg = OPCPackage.create(targetFile);
        PackagePartName corePartName = PackagingURIHelper
                .createPartName("/word/document.xml");

        pkg.addRelationship(corePartName, TargetMode.INTERNAL,
                PackageRelationshipTypes.CORE_DOCUMENT, "rId1");

        PackagePart corePart = pkg
                .createPart(
                        corePartName,
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml");

        Document doc = DocumentHelper.createDocument();
        Element elDocument = doc.createElementNS("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "w:document");
        doc.appendChild(elDocument);
        Element elBody = doc.createElementNS("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "w:body");
        elDocument.appendChild(elBody);
        Element elParagraph = doc.createElementNS("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "w:p");
        elBody.appendChild(elParagraph);
        Element elRun = doc.createElementNS("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "w:r");
        elParagraph.appendChild(elRun);
        Element elText = doc.createElementNS("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "w:t");
        elRun.appendChild(elText);
        elText.setTextContent("Hello Open XML !");

        StreamHelper.saveXmlInStream(doc, corePart.getOutputStream());
        pkg.close();

        ZipFileAssert.assertEquals(expectedFile, targetFile);
        assertTrue(targetFile.delete());
	}

	/**
	 * Tests that we can create a new package, add a core
	 *  document and another part, save and re-load and
	 *  have everything setup as expected
	 */
    @Test
	public void createPackageWithCoreDocument() throws IOException, InvalidFormatException, URISyntaxException, SAXException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OPCPackage pkg = OPCPackage.create(baos);

		// Add a core document
        PackagePartName corePartName = PackagingURIHelper.createPartName("/xl/workbook.xml");
        // Create main part relationship
        pkg.addRelationship(corePartName, TargetMode.INTERNAL, PackageRelationshipTypes.CORE_DOCUMENT, "rId1");
        // Create main document part
        PackagePart corePart = pkg.createPart(corePartName, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml");
        // Put in some dummy content
        OutputStream coreOut = corePart.getOutputStream();
        coreOut.write("<dummy-xml />".getBytes("UTF-8"));
        coreOut.close();

		// And another bit
        PackagePartName sheetPartName = PackagingURIHelper.createPartName("/xl/worksheets/sheet1.xml");
        PackageRelationship rel =
        	 corePart.addRelationship(sheetPartName, TargetMode.INTERNAL, "http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet", "rSheet1");
		assertNotNull(rel);

        PackagePart part = pkg.createPart(sheetPartName, "application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml");
        assertNotNull(part);

        // Dummy content again
        coreOut = corePart.getOutputStream();
        coreOut.write("<dummy-xml2 />".getBytes("UTF-8"));
        coreOut.close();

        //add a relationship with internal target: "#Sheet1!A1"
        corePart.addRelationship(new URI("#Sheet1!A1"), TargetMode.INTERNAL, "http://schemas.openxmlformats.org/officeDocument/2006/relationships/hyperlink", "rId2");

        // Check things are as expected
        PackageRelationshipCollection coreRels =
        	pkg.getRelationshipsByType(PackageRelationshipTypes.CORE_DOCUMENT);
        assertEquals(1, coreRels.size());
        PackageRelationship coreRel = coreRels.getRelationship(0);
		assertNotNull(coreRel);
        assertEquals("/", coreRel.getSourceURI().toString());
        assertEquals("/xl/workbook.xml", coreRel.getTargetURI().toString());
        assertNotNull(pkg.getPart(coreRel));


        // Save and re-load
        pkg.close();
        File tmp = TempFile.createTempFile("testCreatePackageWithCoreDocument", ".zip");
        OutputStream fout = new FileOutputStream(tmp);
        try {
            fout.write(baos.toByteArray());
        } finally {
            fout.close();
        }
        pkg = OPCPackage.open(tmp.getPath());
        //tmp.delete();

        try {
            // Check still right
            coreRels = pkg.getRelationshipsByType(PackageRelationshipTypes.CORE_DOCUMENT);
            assertEquals(1, coreRels.size());
            coreRel = coreRels.getRelationship(0);

			assertNotNull(coreRel);
            assertEquals("/", coreRel.getSourceURI().toString());
            assertEquals("/xl/workbook.xml", coreRel.getTargetURI().toString());
            corePart = pkg.getPart(coreRel);
            assertNotNull(corePart);
    
            PackageRelationshipCollection rels = corePart.getRelationshipsByType("http://schemas.openxmlformats.org/officeDocument/2006/relationships/hyperlink");
            assertEquals(1, rels.size());
            rel = rels.getRelationship(0);
			assertNotNull(rel);
            assertEquals("Sheet1!A1", rel.getTargetURI().getRawFragment());
    
            assertMSCompatibility(pkg);
        } finally {
            pkg.close();
        }
    }

    private void assertMSCompatibility(OPCPackage pkg) throws IOException, InvalidFormatException, SAXException {
        PackagePartName relName = PackagingURIHelper.createPartName(PackageRelationship.getContainerPartRelationship());
        PackagePart relPart = pkg.getPart(relName);

        Document xmlRelationshipsDoc = DocumentHelper.readDocument(relPart.getInputStream());

        Element root = xmlRelationshipsDoc.getDocumentElement();
        NodeList nodeList = root.getElementsByTagName(PackageRelationship.RELATIONSHIP_TAG_NAME);
        int nodeCount = nodeList.getLength();
        for (int i = 0; i < nodeCount; i++) {
            Element element = (Element) nodeList.item(i);
            String value = element.getAttribute(PackageRelationship.TARGET_ATTRIBUTE_NAME);
            assertTrue("Root target must not start with a leading slash ('/'): " + value, value.charAt(0) != '/');
        }

    }

    /**
	 * Test package opening.
	 */
    @Test
	public void openPackage() throws IOException, InvalidFormatException {
		File targetFile = OpenXML4JTestDataSamples.getOutputFile("TestOpenPackageTMP.docx");

		File inputFile = OpenXML4JTestDataSamples.getSampleFile("TestOpenPackageINPUT.docx");

		File expectedFile = OpenXML4JTestDataSamples.getSampleFile("TestOpenPackageOUTPUT.docx");

		// Copy the input file in the output directory
		FileHelper.copyFile(inputFile, targetFile);

		// Create a package
		OPCPackage pkg = OPCPackage.open(targetFile.getAbsolutePath());

		// Modify core part
		PackagePartName corePartName = PackagingURIHelper
				.createPartName("/word/document.xml");

		PackagePart corePart = pkg.getPart(corePartName);

		// Delete some part to have a valid document
		for (PackageRelationship rel : corePart.getRelationships()) {
			corePart.removeRelationship(rel.getId());
			pkg.removePart(PackagingURIHelper.createPartName(PackagingURIHelper
					.resolvePartUri(corePart.getPartName().getURI(), rel
							.getTargetURI())));
		}

		// Create a content
		Document doc = DocumentHelper.createDocument();
        Element elDocument = doc.createElementNS("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "w:document");
        doc.appendChild(elDocument);
        Element elBody = doc.createElementNS("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "w:body");
        elDocument.appendChild(elBody);
        Element elParagraph = doc.createElementNS("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "w:p");
        elBody.appendChild(elParagraph);
        Element elRun = doc.createElementNS("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "w:r");
        elParagraph.appendChild(elRun);
        Element elText = doc.createElementNS("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "w:t");
        elRun.appendChild(elText);
        elText.setTextContent("Hello Open XML !");

		StreamHelper.saveXmlInStream(doc, corePart.getOutputStream());

		// Save and close
		try {
			pkg.close();
		} catch (IOException e) {
			fail();
		}

		ZipFileAssert.assertEquals(expectedFile, targetFile);
		assertTrue(targetFile.delete());
	}

	/**
	 * Checks that we can write a package to a simple
	 *  OutputStream, in addition to the normal writing
	 *  to a file
	 */
    @Test
	public void saveToOutputStream() throws IOException, InvalidFormatException {
		String originalFile = OpenXML4JTestDataSamples.getSampleFileName("TestPackageCommon.docx");
		File targetFile = OpenXML4JTestDataSamples.getOutputFile("TestPackageOpenSaveTMP.docx");

		@SuppressWarnings("resource")
        OPCPackage p = OPCPackage.open(originalFile, PackageAccess.READ_WRITE);
		try {
    		FileOutputStream fout = new FileOutputStream(targetFile);
    		try {
    		    p.save(fout);
    		} finally {
    		    fout.close();
    		}
    
    		// Compare the original and newly saved document
    		assertTrue(targetFile.exists());
    		ZipFileAssert.assertEquals(new File(originalFile), targetFile);
    		assertTrue(targetFile.delete());
		} finally {
		    // use revert to not re-write the input file
		    p.revert();
		}
	}

	/**
	 * Checks that we can open+read a package from a
	 *  simple InputStream, in addition to the normal
	 *  reading from a file
	 */
    @Test
	public void openFromInputStream() throws IOException, InvalidFormatException {
		String originalFile = OpenXML4JTestDataSamples.getSampleFileName("TestPackageCommon.docx");

		FileInputStream finp = new FileInputStream(originalFile);

		@SuppressWarnings("resource")
        OPCPackage p = OPCPackage.open(finp);

		assertNotNull(p);
		assertNotNull(p.getRelationships());
		assertEquals(12, p.getParts().size());

		// Check it has the usual bits
		assertTrue(p.hasRelationships());
		assertTrue(p.containPart(PackagingURIHelper.createPartName("/_rels/.rels")));
		
		p.revert();
		finp.close();
	}

    /**
     * TODO: fix and enable
     */
    @Test
    @Ignore
    public void removePartRecursive() throws IOException, InvalidFormatException, URISyntaxException {
		String originalFile = OpenXML4JTestDataSamples.getSampleFileName("TestPackageCommon.docx");
		File targetFile = OpenXML4JTestDataSamples.getOutputFile("TestPackageRemovePartRecursiveOUTPUT.docx");
		File tempFile = OpenXML4JTestDataSamples.getOutputFile("TestPackageRemovePartRecursiveTMP.docx");

		@SuppressWarnings("resource")
        OPCPackage p = OPCPackage.open(originalFile, PackageAccess.READ_WRITE);
		p.removePartRecursive(PackagingURIHelper.createPartName(new URI(
				"/word/document.xml")));
		p.save(tempFile.getAbsoluteFile());

		// Compare the original and newly saved document
		assertTrue(targetFile.exists());
		ZipFileAssert.assertEquals(targetFile, tempFile);
		assertTrue(targetFile.delete());
		
		p.revert();
	}

    @Test
	public void deletePart() throws InvalidFormatException {
		TreeMap<PackagePartName, String> expectedValues;
		TreeMap<PackagePartName, String> values;

		values = new TreeMap<PackagePartName, String>();

		// Expected values
		expectedValues = new TreeMap<PackagePartName, String>();
		expectedValues.put(PackagingURIHelper.createPartName("/_rels/.rels"),
				"application/vnd.openxmlformats-package.relationships+xml");

		expectedValues
				.put(PackagingURIHelper.createPartName("/docProps/app.xml"),
						"application/vnd.openxmlformats-officedocument.extended-properties+xml");
		expectedValues.put(PackagingURIHelper
				.createPartName("/docProps/core.xml"),
				"application/vnd.openxmlformats-package.core-properties+xml");
		expectedValues
				.put(PackagingURIHelper.createPartName("/word/fontTable.xml"),
						"application/vnd.openxmlformats-officedocument.wordprocessingml.fontTable+xml");
		expectedValues.put(PackagingURIHelper
				.createPartName("/word/media/image1.gif"), "image/gif");
		expectedValues
				.put(PackagingURIHelper.createPartName("/word/settings.xml"),
						"application/vnd.openxmlformats-officedocument.wordprocessingml.settings+xml");
		expectedValues
				.put(PackagingURIHelper.createPartName("/word/styles.xml"),
						"application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml");
		expectedValues.put(PackagingURIHelper
				.createPartName("/word/theme/theme1.xml"),
				"application/vnd.openxmlformats-officedocument.theme+xml");
		expectedValues
				.put(
						PackagingURIHelper
								.createPartName("/word/webSettings.xml"),
						"application/vnd.openxmlformats-officedocument.wordprocessingml.webSettings+xml");

		String filepath =  OpenXML4JTestDataSamples.getSampleFileName("sample.docx");

		@SuppressWarnings("resource")
        OPCPackage p = OPCPackage.open(filepath, PackageAccess.READ_WRITE);
		// Remove the core part
		p.deletePart(PackagingURIHelper.createPartName("/word/document.xml"));

		for (PackagePart part : p.getParts()) {
			values.put(part.getPartName(), part.getContentType());
			logger.log(POILogger.DEBUG, part.getPartName());
		}

		// Compare expected values with values return by the package
		for (PackagePartName partName : expectedValues.keySet()) {
			assertNotNull(values.get(partName));
			assertEquals(expectedValues.get(partName), values.get(partName));
		}
		// Don't save modifications
		p.revert();
	}

    @Test
	public void deletePartRecursive() throws InvalidFormatException {
		TreeMap<PackagePartName, String> expectedValues;
		TreeMap<PackagePartName, String> values;

		values = new TreeMap<PackagePartName, String>();

		// Expected values
		expectedValues = new TreeMap<PackagePartName, String>();
		expectedValues.put(PackagingURIHelper.createPartName("/_rels/.rels"),
				"application/vnd.openxmlformats-package.relationships+xml");

		expectedValues
				.put(PackagingURIHelper.createPartName("/docProps/app.xml"),
						"application/vnd.openxmlformats-officedocument.extended-properties+xml");
		expectedValues.put(PackagingURIHelper
				.createPartName("/docProps/core.xml"),
				"application/vnd.openxmlformats-package.core-properties+xml");

		String filepath = OpenXML4JTestDataSamples.getSampleFileName("sample.docx");

		@SuppressWarnings("resource")
        OPCPackage p = OPCPackage.open(filepath, PackageAccess.READ_WRITE);
		// Remove the core part
		p.deletePartRecursive(PackagingURIHelper.createPartName("/word/document.xml"));

		for (PackagePart part : p.getParts()) {
			values.put(part.getPartName(), part.getContentType());
			logger.log(POILogger.DEBUG, part.getPartName());
		}

		// Compare expected values with values return by the package
		for (PackagePartName partName : expectedValues.keySet()) {
			assertNotNull(values.get(partName));
			assertEquals(expectedValues.get(partName), values.get(partName));
		}
		// Don't save modifications
		p.revert();
	}
	
	/**
	 * Test that we can open a file by path, and then
	 *  write changes to it.
	 */
    @Test
	public void openFileThenOverwrite() throws IOException, InvalidFormatException {
        File tempFile = TempFile.createTempFile("poiTesting","tmp");
        File origFile = OpenXML4JTestDataSamples.getSampleFile("TestPackageCommon.docx");
        FileHelper.copyFile(origFile, tempFile);
        
        // Open the temp file
        OPCPackage p = OPCPackage.open(tempFile.toString(), PackageAccess.READ_WRITE);
        // Close it
        p.close();
        // Delete it
        assertTrue(tempFile.delete());
        
        // Reset
        FileHelper.copyFile(origFile, tempFile);
        p = OPCPackage.open(tempFile.toString(), PackageAccess.READ_WRITE);
        
        // Save it to the same file - not allowed
        try {
            p.save(tempFile);
            fail("You shouldn't be able to call save(File) to overwrite the current file");
        } catch(InvalidOperationException e) {
			// expected here
		}

        p.close();
        // Delete it
        assertTrue(tempFile.delete());
        
        
        // Open it read only, then close and delete - allowed
        FileHelper.copyFile(origFile, tempFile);
        p = OPCPackage.open(tempFile.toString(), PackageAccess.READ);
        p.close();
        assertTrue(tempFile.delete());
	}
    /**
     * Test that we can open a file by path, save it
     *  to another file, then delete both
     */
    @Test
    public void openFileThenSaveDelete() throws IOException, InvalidFormatException {
        File tempFile = TempFile.createTempFile("poiTesting","tmp");
        File tempFile2 = TempFile.createTempFile("poiTesting","tmp");
        File origFile = OpenXML4JTestDataSamples.getSampleFile("TestPackageCommon.docx");
        FileHelper.copyFile(origFile, tempFile);
        
        // Open the temp file
        OPCPackage p = OPCPackage.open(tempFile.toString(), PackageAccess.READ_WRITE);

        // Save it to a different file
        p.save(tempFile2);
        p.close();
        
        // Delete both the files
        assertTrue(tempFile.delete());
        assertTrue(tempFile2.delete());
    }

	private static ContentTypeManager getContentTypeManager(OPCPackage pkg) {
	    return POITestCase.getFieldValue(OPCPackage.class, pkg, ContentTypeManager.class, "contentTypeManager");
	}

    @Test
    public void getPartsByName() throws IOException, InvalidFormatException {
        String filepath =  OpenXML4JTestDataSamples.getSampleFileName("sample.docx");

        @SuppressWarnings("resource")
        OPCPackage pkg = OPCPackage.open(filepath, PackageAccess.READ_WRITE);
        try {
            List<PackagePart> rs =  pkg.getPartsByName(Pattern.compile("/word/.*?\\.xml"));
            HashMap<String, PackagePart>  selected = new HashMap<String, PackagePart>();
    
            for(PackagePart p : rs)
                selected.put(p.getPartName().getName(), p);
    
            assertEquals(6, selected.size());
            assertTrue(selected.containsKey("/word/document.xml"));
            assertTrue(selected.containsKey("/word/fontTable.xml"));
            assertTrue(selected.containsKey("/word/settings.xml"));
            assertTrue(selected.containsKey("/word/styles.xml"));
            assertTrue(selected.containsKey("/word/theme/theme1.xml"));
            assertTrue(selected.containsKey("/word/webSettings.xml"));
        } finally {
            // use revert to not re-write the input file
            pkg.revert();
        }
    }
    
    @Test
    public void getPartSize() throws IOException, InvalidFormatException {
       String filepath =  OpenXML4JTestDataSamples.getSampleFileName("sample.docx");
       OPCPackage pkg = OPCPackage.open(filepath, PackageAccess.READ);
       try {
           int checked = 0;
           for (PackagePart part : pkg.getParts()) {
              // Can get the size of zip parts
              if (part.getPartName().getName().equals("/word/document.xml")) {
                 checked++;
                 assertEquals(ZipPackagePart.class, part.getClass());
                 assertEquals(6031L, part.getSize());
              }
              if (part.getPartName().getName().equals("/word/fontTable.xml")) {
                 checked++;
                 assertEquals(ZipPackagePart.class, part.getClass());
                 assertEquals(1312L, part.getSize());
              }
              
              // But not from the others
              if (part.getPartName().getName().equals("/docProps/core.xml")) {
                 checked++;
                 assertEquals(PackagePropertiesPart.class, part.getClass());
                 assertEquals(-1, part.getSize());
              }
           }
           // Ensure we actually found the parts we want to check
           assertEquals(3, checked);
       } finally {
           pkg.close();
       }
    }

    @Test
    public void replaceContentType()
    throws IOException, InvalidFormatException, SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
        InputStream is = OpenXML4JTestDataSamples.openSampleStream("sample.xlsx");
        @SuppressWarnings("resource")
        OPCPackage p = OPCPackage.open(is);

        ContentTypeManager mgr = getContentTypeManager(p);

        assertTrue(mgr.isContentTypeRegister("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"));
        assertFalse(mgr.isContentTypeRegister("application/vnd.ms-excel.sheet.macroEnabled.main+xml"));

        assertTrue(
                p.replaceContentType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml",
                "application/vnd.ms-excel.sheet.macroEnabled.main+xml")
        );

        assertFalse(mgr.isContentTypeRegister("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"));
        assertTrue(mgr.isContentTypeRegister("application/vnd.ms-excel.sheet.macroEnabled.main+xml"));
        p.revert();
        is.close();
    }
    
    /**
     * Verify we give helpful exceptions (or as best we can) when
     *  supplied with non-OOXML file types (eg OLE2, ODF)
     */
    @Test
    public void NonOOXMLFileTypes() throws Exception {
        // Spreadsheet has a good mix of alternate file types
        POIDataSamples files = POIDataSamples.getSpreadSheetInstance();
        
        // OLE2 - Stream
        try {
			InputStream stream = files.openResourceAsStream("SampleSS.xls");
			try {
				OPCPackage.open(stream);
			} finally {
				stream.close();
			}
            fail("Shouldn't be able to open OLE2");
        } catch (OLE2NotOfficeXmlFileException e) {
            assertTrue(e.getMessage().contains("The supplied data appears to be in the OLE2 Format"));
            assertTrue(e.getMessage().contains("You are calling the part of POI that deals with OOXML"));
        }
        // OLE2 - File
        try {
            OPCPackage.open(files.getFile("SampleSS.xls"));
            fail("Shouldn't be able to open OLE2");
        } catch (OLE2NotOfficeXmlFileException e) {
            assertTrue(e.getMessage().contains("The supplied data appears to be in the OLE2 Format"));
            assertTrue(e.getMessage().contains("You are calling the part of POI that deals with OOXML"));
        }
        
        // Raw XML - Stream
        try {
			InputStream stream = files.openResourceAsStream("SampleSS.xml");
			try {
				OPCPackage.open(stream);
			} finally {
				stream.close();
			}
            fail("Shouldn't be able to open XML");
        } catch (NotOfficeXmlFileException e) {
            assertTrue(e.getMessage().contains("The supplied data appears to be a raw XML file"));
            assertTrue(e.getMessage().contains("Formats such as Office 2003 XML"));
        }
        // Raw XML - File
        try {
            OPCPackage.open(files.getFile("SampleSS.xml"));
            fail("Shouldn't be able to open XML");
        } catch (NotOfficeXmlFileException e) {
            assertTrue(e.getMessage().contains("The supplied data appears to be a raw XML file"));
            assertTrue(e.getMessage().contains("Formats such as Office 2003 XML"));
        }
        
        // ODF / ODS - Stream
        try {
			InputStream stream = files.openResourceAsStream("SampleSS.ods");
			try {
				OPCPackage.open(stream);
			} finally {
				stream.close();
			}
            fail("Shouldn't be able to open ODS");
        } catch (ODFNotOfficeXmlFileException e) {
            assertTrue(e.toString().contains("The supplied data appears to be in ODF"));
            assertTrue(e.toString().contains("Formats like these (eg ODS"));
        }
        // ODF / ODS - File
        try {
            OPCPackage.open(files.getFile("SampleSS.ods"));
            fail("Shouldn't be able to open ODS");
        } catch (ODFNotOfficeXmlFileException e) {
            assertTrue(e.toString().contains("The supplied data appears to be in ODF"));
            assertTrue(e.toString().contains("Formats like these (eg ODS"));
        }
        
        // Plain Text - Stream
        try {
			InputStream stream = files.openResourceAsStream("SampleSS.txt");
			try {
				OPCPackage.open(stream);
			} finally {
				stream.close();
			}
            fail("Shouldn't be able to open Plain Text");
        } catch (NotOfficeXmlFileException e) {
            assertTrue(e.getMessage().contains("No valid entries or contents found"));
            assertTrue(e.getMessage().contains("not a valid OOXML"));
        }
        // Plain Text - File
        try {
            OPCPackage.open(files.getFile("SampleSS.txt"));
            fail("Shouldn't be able to open Plain Text");
        } catch (UnsupportedFileFormatException e) {
            // Unhelpful low-level error, sorry
        }
    }

    @Test(expected=IOException.class)
    public void zipBombCreateAndHandle()
    throws IOException, EncryptedDocumentException, InvalidFormatException {
        // #50090 / #56865
        ZipFile zipFile = ZipHelper.openZipFile(OpenXML4JTestDataSamples.getSampleFile("sample.xlsx"));
		assertNotNull(zipFile);

		ByteArrayOutputStream bos = new ByteArrayOutputStream(2500000);
		ZipOutputStream append = new ZipOutputStream(bos);
		// first, copy contents from existing war
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry e2 = entries.nextElement();
            ZipEntry e = new ZipEntry(e2.getName());
            e.setTime(e2.getTime());
            e.setComment(e2.getComment());
            e.setSize(e2.getSize());
            
            append.putNextEntry(e);
            if (!e.isDirectory()) {
                InputStream is = zipFile.getInputStream(e);
                if (e.getName().equals("[Content_Types].xml")) {
                    ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
                    IOUtils.copy(is, bos2);
                    long size = bos2.size()-"</Types>".length();
                    append.write(bos2.toByteArray(), 0, (int)size);
                    byte spam[] = new byte[0x7FFF];
                    for (int i=0; i<spam.length; i++) spam[i] = ' ';
                    // 0x7FFF0000 is the maximum for 32-bit zips, but less still works
                    while (size < 0x7FFF00) {
                        append.write(spam);
                        size += spam.length;
                    }
                    append.write("</Types>".getBytes("UTF-8"));
                    size += 8;
                    e.setSize(size);
                } else {
                    IOUtils.copy(is, append);
                }
                is.close();
            }
            append.closeEntry();
        }
        
        append.close();
        zipFile.close();

        byte buf[] = bos.toByteArray();
		//noinspection UnusedAssignment
		bos = null;
        
        Workbook wb = WorkbookFactory.create(new ByteArrayInputStream(buf));
        wb.getSheetAt(0);
        wb.close();
        zipFile.close();
    }

    @Test
	public void zipBombSampleFiles() throws IOException, OpenXML4JException, XmlException {
    	openZipBombFile("poc-shared-strings.xlsx");
    	openZipBombFile("poc-xmlbomb.xlsx");
    	openZipBombFile("poc-xmlbomb-empty.xlsx");
	}

	private void openZipBombFile(String file) throws IOException, OpenXML4JException, XmlException {
    	try {
			Workbook wb = XSSFTestDataSamples.openSampleWorkbook(file);
			wb.close();

			POITextExtractor extractor = ExtractorFactory.createExtractor(HSSFTestDataSamples.getSampleFile("poc-shared-strings.xlsx"));
			try  {
				assertNotNull(extractor);
				extractor.getText();
			} finally {
				extractor.close();
			}

			fail("Should catch an exception because of a ZipBomb");
		} catch (IllegalStateException e) {
    		if(!e.getMessage().contains("The text would exceed the max allowed overall size of extracted text.")) {
				throw e;
			}
		} catch (POIXMLException e) {
    		checkForZipBombException(e);
		}
	}
    
    @Test
    public void zipBombCheckSizes() throws IOException, EncryptedDocumentException, InvalidFormatException {
        File file = OpenXML4JTestDataSamples.getSampleFile("sample.xlsx");

        try {
            double min_ratio = Double.MAX_VALUE;
            long max_size = 0;
            ZipFile zf = ZipHelper.openZipFile(file);
			assertNotNull(zf);
            Enumeration<? extends ZipEntry> entries = zf.entries();
            while (entries.hasMoreElements()) {
                ZipEntry ze = entries.nextElement();
                double ratio = (double)ze.getCompressedSize() / (double)ze.getSize();
                min_ratio = Math.min(min_ratio, ratio);
                max_size = Math.max(max_size, ze.getSize());
            }
            zf.close();
    
            // use values close to, but within the limits 
            ZipSecureFile.setMinInflateRatio(min_ratio-0.002);
			assertEquals(min_ratio-0.002, ZipSecureFile.getMinInflateRatio(), 0.00001);
            ZipSecureFile.setMaxEntrySize(max_size+1);
			assertEquals(max_size+1, ZipSecureFile.getMaxEntrySize());
			
            WorkbookFactory.create(file, null, true).close();
    
            // check ratio out of bounds
            ZipSecureFile.setMinInflateRatio(min_ratio+0.002);
            try {
                WorkbookFactory.create(file, null, true).close();
                // this is a bit strange, as there will be different exceptions thrown
                // depending if this executed via "ant test" or within eclipse
                // maybe a difference in JDK ...
            } catch (InvalidFormatException e) {
                checkForZipBombException(e);
            } catch (POIXMLException e) {
                checkForZipBombException(e);
            }
    
            // check max entry size ouf of bounds
            ZipSecureFile.setMinInflateRatio(min_ratio-0.002);
            ZipSecureFile.setMaxEntrySize(max_size-1);
            try {
                WorkbookFactory.create(file, null, true).close();
            } catch (InvalidFormatException e) {
                checkForZipBombException(e);
            } catch (POIXMLException e) {
                checkForZipBombException(e);
            }
        } finally {
            // reset otherwise a lot of ooxml tests will fail
            ZipSecureFile.setMinInflateRatio(0.01d);
            ZipSecureFile.setMaxEntrySize(0xFFFFFFFFL);
        }
    }

    private void checkForZipBombException(Throwable e) {
    	// unwrap InvocationTargetException as they usually contain the nested exception in the "target" member
        if(e instanceof InvocationTargetException) {
			e = ((InvocationTargetException)e).getTargetException();
        }
        
        String msg = e.getMessage();
        if(msg != null && (msg.startsWith("Zip bomb detected!") ||
				msg.contains("The parser has encountered more than \"4,096\" entity expansions in this document;") ||
				msg.contains("The parser has encountered more than \"4096\" entity expansions in this document;"))) {
            return;
        }
        
        // recursively check the causes for the message as it can be nested further down in the exception-tree
        if(e.getCause() != null && e.getCause() != e) {
            checkForZipBombException(e.getCause());
            return;
        }

        throw new IllegalStateException("Expected to catch an Exception because of a detected Zip Bomb, but did not find the related error message in the exception", e);        
    }

    @Test
    public void testConstructors() throws IOException {
        // verify the various ways to construct a ZipSecureFile
        File file = OpenXML4JTestDataSamples.getSampleFile("sample.xlsx");
        ZipSecureFile zipFile = new ZipSecureFile(file);
        assertNotNull(zipFile.getName());
        zipFile.close();

        zipFile = new ZipSecureFile(file, ZipFile.OPEN_READ);
        assertNotNull(zipFile.getName());
        zipFile.close();

        zipFile = new ZipSecureFile(file.getAbsolutePath());
        assertNotNull(zipFile.getName());
        zipFile.close();
    }

    @Test
    public void testMaxTextSize() {
        long before = ZipSecureFile.getMaxTextSize();
        try {
            ZipSecureFile.setMaxTextSize(12345);
            assertEquals(12345, ZipSecureFile.getMaxTextSize());
        } finally {
            ZipSecureFile.setMaxTextSize(before);
        }
    }
    
    // bug 60128
    @Test
    public void testCorruptFile() throws IOException {
        OPCPackage pkg = null;
        File file = OpenXML4JTestDataSamples.getSampleFile("invalid.xlsx");
        try {
            pkg = OPCPackage.open(file, PackageAccess.READ);
        } catch (Exception e) {
            System.out.println(e.getClass().getName());
            System.out.println(e.getMessage());
            e.printStackTrace();
        } finally {
            if (pkg != null) {
                pkg.close();
            }
        }
    }
}
