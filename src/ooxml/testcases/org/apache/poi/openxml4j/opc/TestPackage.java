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

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.POIDataSamples;
import org.apache.poi.POITestCase;
import org.apache.poi.UnsupportedFileFormatException;
import org.apache.poi.extractor.POITextExtractor;
import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.ooxml.extractor.ExtractorFactory;
import org.apache.poi.ooxml.util.DocumentHelper;
import org.apache.poi.openxml4j.OpenXML4JTestDataSamples;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException;
import org.apache.poi.openxml4j.exceptions.ODFNotOfficeXmlFileException;
import org.apache.poi.openxml4j.exceptions.OLE2NotOfficeXmlFileException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.internal.ContentTypeManager;
import org.apache.poi.openxml4j.opc.internal.FileHelper;
import org.apache.poi.openxml4j.opc.internal.PackagePropertiesPart;
import org.apache.poi.openxml4j.opc.internal.ZipHelper;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.sl.usermodel.SlideShowFactory;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.TempFile;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFRelation;
import org.apache.xmlbeans.XmlException;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public final class TestPackage {
    private static final POILogger logger = POILogFactory.getLogger(TestPackage.class);

	@Rule
	public ExpectedException expectedEx = ExpectedException.none();

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
    throws IOException, InvalidFormatException, SecurityException, IllegalArgumentException {
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
		try (OutputStream fout = new FileOutputStream(tmp)) {
			fout.write(baos.toByteArray());
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
			try (FileOutputStream fout = new FileOutputStream(targetFile)) {
				p.save(fout);
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

		values = new TreeMap<>();

		// Expected values
		expectedValues = new TreeMap<>();
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

		values = new TreeMap<>();

		// Expected values
		expectedValues = new TreeMap<>();
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
    public void getPartsByName() throws InvalidFormatException {
        String filepath =  OpenXML4JTestDataSamples.getSampleFileName("sample.docx");

        @SuppressWarnings("resource")
        OPCPackage pkg = OPCPackage.open(filepath, PackageAccess.READ_WRITE);
        try {
            List<PackagePart> rs =  pkg.getPartsByName(Pattern.compile("/word/.*?\\.xml"));
            HashMap<String, PackagePart>  selected = new HashMap<>();
    
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
		try (OPCPackage pkg = OPCPackage.open(filepath, PackageAccess.READ)) {
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
		}
    }

    @Test
    public void replaceContentType()
    throws IOException, InvalidFormatException, SecurityException, IllegalArgumentException {
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
			try (InputStream stream = files.openResourceAsStream("SampleSS.xls")) {
				OPCPackage.open(stream);
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
			try (InputStream stream = files.openResourceAsStream("SampleSS.xml")) {
				OPCPackage.open(stream);
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
			try (InputStream stream = files.openResourceAsStream("SampleSS.ods")) {
				OPCPackage.open(stream);
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
			try (InputStream stream = files.openResourceAsStream("SampleSS.txt")) {
				OPCPackage.open(stream);
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

	/**
	 * Zip bomb handling test
	 *
	 * see bug #50090 / #56865
	 */
    @Test
    public void zipBombCreateAndHandle()
    throws IOException, EncryptedDocumentException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream(2500000);

        try (ZipFile zipFile = ZipHelper.openZipFile(OpenXML4JTestDataSamples.getSampleFile("sample.xlsx"));
			 ZipArchiveOutputStream append = new ZipArchiveOutputStream(bos)) {
			assertNotNull(zipFile);

			// first, copy contents from existing war
			Enumeration<? extends ZipArchiveEntry> entries = zipFile.getEntries();
			while (entries.hasMoreElements()) {
				final ZipArchiveEntry eIn = entries.nextElement();
				final ZipArchiveEntry eOut = new ZipArchiveEntry(eIn.getName());
				eOut.setTime(eIn.getTime());
				eOut.setComment(eIn.getComment());
				eOut.setSize(eIn.getSize());

				append.putArchiveEntry(eOut);
				if (!eOut.isDirectory()) {
					try (InputStream is = zipFile.getInputStream(eIn)) {
						if (eOut.getName().equals("[Content_Types].xml")) {
							ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
							IOUtils.copy(is, bos2);
							long size = bos2.size() - "</Types>".length();
							append.write(bos2.toByteArray(), 0, (int) size);
							byte spam[] = new byte[0x7FFF];
							Arrays.fill(spam, (byte) ' ');
							// 0x7FFF0000 is the maximum for 32-bit zips, but less still works
							while (size < 0x7FFF00) {
								append.write(spam);
								size += spam.length;
							}
							append.write("</Types>".getBytes("UTF-8"));
							size += 8;
							eOut.setSize(size);
						} else {
							IOUtils.copy(is, append);
						}
					}
				}
				append.closeArchiveEntry();
			}
		}

		expectedEx.expect(IOException.class);
		expectedEx.expectMessage("Zip bomb detected!");

		try (Workbook wb = WorkbookFactory.create(new ByteArrayInputStream(bos.toByteArray()))) {
			wb.getSheetAt(0);
		}
    }

	@Test
	public void testZipEntityExpansionTerminates() throws IOException, OpenXML4JException, XmlException {
		expectedEx.expect(IllegalStateException.class);
		expectedEx.expectMessage("The text would exceed the max allowed overall size of extracted text.");
		openXmlBombFile("poc-shared-strings.xlsx");
	}

	@Test
	public void testZipEntityExpansionSharedStringTableEvents() throws IOException, OpenXML4JException, XmlException {
		boolean before = ExtractorFactory.getThreadPrefersEventExtractors();
		ExtractorFactory.setThreadPrefersEventExtractors(true);
		try {
			expectedEx.expect(IllegalStateException.class);
			expectedEx.expectMessage("The text would exceed the max allowed overall size of extracted text.");
			openXmlBombFile("poc-shared-strings.xlsx");
		} finally {
			ExtractorFactory.setThreadPrefersEventExtractors(before);
		}
	}


	@Test
	public void testZipEntityExpansionExceedsMemory() throws IOException, OpenXML4JException, XmlException {
		expectedEx.expect(POIXMLException.class);
		expectedEx.expectMessage("unable to parse shared strings table");
		expectedEx.expectCause(getCauseMatcher(SAXParseException.class, "The parser has encountered more than"));
		openXmlBombFile("poc-xmlbomb.xlsx");
	}

	@Test
	public void testZipEntityExpansionExceedsMemory2() throws IOException, OpenXML4JException, XmlException {
		expectedEx.expect(POIXMLException.class);
		expectedEx.expectMessage("unable to parse shared strings table");
		expectedEx.expectCause(getCauseMatcher(SAXParseException.class, "The parser has encountered more than"));
    	openXmlBombFile("poc-xmlbomb-empty.xlsx");
	}

	private void openXmlBombFile(String file) throws IOException, OpenXML4JException, XmlException {
		final double minInf = ZipSecureFile.getMinInflateRatio();
		ZipSecureFile.setMinInflateRatio(0.002);
		try (POITextExtractor extractor = ExtractorFactory.createExtractor(XSSFTestDataSamples.getSampleFile(file))) {
			assertNotNull(extractor);
			extractor.getText();
		} finally {
			ZipSecureFile.setMinInflateRatio(minInf);
		}
	}

    @Test
    public void zipBombCheckSizesWithinLimits() throws IOException, EncryptedDocumentException {
		getZipStatsAndConsume((max_size, min_ratio) -> {
			// use values close to, but within the limits
			ZipSecureFile.setMinInflateRatio(min_ratio - 0.002);
			assertEquals(min_ratio - 0.002, ZipSecureFile.getMinInflateRatio(), 0.00001);
			ZipSecureFile.setMaxEntrySize(max_size + 1);
			assertEquals(max_size + 1, ZipSecureFile.getMaxEntrySize());
		});
	}

	@Test
	public void zipBombCheckSizesRatioTooSmall() throws IOException, EncryptedDocumentException {
		expectedEx.expect(POIXMLException.class);
		expectedEx.expectMessage("You can adjust this limit via ZipSecureFile.setMinInflateRatio()");
		getZipStatsAndConsume((max_size, min_ratio) -> {
			// check ratio out of bounds
			ZipSecureFile.setMinInflateRatio(min_ratio+0.002);
		});
	}

	@Test
	public void zipBombCheckSizesSizeTooBig() throws IOException, EncryptedDocumentException {
		expectedEx.expect(POIXMLException.class);
		expectedEx.expectMessage("You can adjust this limit via ZipSecureFile.setMaxEntrySize()");
		getZipStatsAndConsume((max_size, min_ratio) -> {
			// check max entry size ouf of bounds
			ZipSecureFile.setMinInflateRatio(min_ratio-0.002);
			ZipSecureFile.setMaxEntrySize(max_size-100);
		});
	}

	private void getZipStatsAndConsume(BiConsumer<Long,Double> ratioCon) throws IOException {
    	// use a test file with a xml file bigger than 100k (ZipArchiveThresholdInputStream.GRACE_ENTRY_SIZE)
		final File file = XSSFTestDataSamples.getSampleFile("poc-shared-strings.xlsx");

		double min_ratio = Double.MAX_VALUE;
		long max_size = 0;
		try (ZipFile zf = ZipHelper.openZipFile(file)) {
			assertNotNull(zf);
			Enumeration<? extends ZipArchiveEntry> entries = zf.getEntries();
			while (entries.hasMoreElements()) {
				ZipArchiveEntry ze = entries.nextElement();
				if (ze.getSize() == 0) {
					continue;
				}
				// add zip entry header ~ 30 bytes
				long size = ze.getSize()+30;
				double ratio = ze.getCompressedSize() / (double)size;
				min_ratio = Math.min(min_ratio, ratio);
				max_size = Math.max(max_size, size);
			}
		}
		ratioCon.accept(max_size, min_ratio);

		//noinspection EmptyTryBlock,unused
		try (Workbook wb = WorkbookFactory.create(file, null, true)) {
		} finally {
			// reset otherwise a lot of ooxml tests will fail
			ZipSecureFile.setMinInflateRatio(0.01d);
			ZipSecureFile.setMaxEntrySize(0xFFFFFFFFL);
		}
	}

    @Test
    public void testConstructors() throws IOException {
        // verify the various ways to construct a ZipSecureFile
        File file = OpenXML4JTestDataSamples.getSampleFile("sample.xlsx");
        ZipSecureFile zipFile = new ZipSecureFile(file);
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
    @Test(expected=NotOfficeXmlFileException.class)
    public void testCorruptFile() throws InvalidFormatException {
        File file = OpenXML4JTestDataSamples.getSampleFile("invalid.xlsx");
        OPCPackage.open(file, PackageAccess.READ);
    }

    // bug 61381
    @Test
    public void testTooShortFilterStreams() throws IOException {
        File xssf = OpenXML4JTestDataSamples.getSampleFile("sample.xlsx");
        File hssf = POIDataSamples.getSpreadSheetInstance().getFile("SampleSS.xls");
        
        InputStream isList[] = {
            new PushbackInputStream(new FileInputStream(xssf), 2),
            new BufferedInputStream(new FileInputStream(xssf), 2),
            new PushbackInputStream(new FileInputStream(hssf), 2),
            new BufferedInputStream(new FileInputStream(hssf), 2),
        };
        
        try {
            for (InputStream is : isList) {
                WorkbookFactory.create(is).close();
            }
        } finally {
            for (InputStream is : isList) {
                IOUtils.closeQuietly(is);
            }
        }
    }

	@Test
	public void testBug56479() throws Exception {
		InputStream is = OpenXML4JTestDataSamples.openSampleStream("dcterms_bug_56479.zip");
		OPCPackage p = OPCPackage.open(is);

		// Check we found the contents of it
		boolean foundCoreProps = false, foundDocument = false, foundTheme1 = false;
		for (final PackagePart part : p.getParts()) {
			final String partName = part.getPartName().toString();
			final String contentType = part.getContentType();
			if ("/docProps/core.xml".equals(partName)) {
				assertEquals(ContentTypes.CORE_PROPERTIES_PART, contentType);
				foundCoreProps = true;
			}
			if ("/word/document.xml".equals(partName)) {
				assertEquals(XWPFRelation.DOCUMENT.getContentType(), contentType);
				foundDocument = true;
			}
			if ("/word/theme/theme1.xml".equals(partName)) {
				assertEquals(XWPFRelation.THEME.getContentType(), contentType);
				foundTheme1 = true;
			}
		}
		assertTrue("Core not found in " + p.getParts(), foundCoreProps);
		assertFalse("Document should not be found in " + p.getParts(), foundDocument);
		assertFalse("Theme1 should not found in " + p.getParts(), foundTheme1);
		p.close();
		is.close();
	}

	@Test
	public void unparseableCentralDirectory() throws IOException {
		File f = OpenXML4JTestDataSamples.getSampleFile("at.pzp.www_uploads_media_PP_Scheinecker-jdk6error.pptx");
		SlideShow<?,?> ppt = SlideShowFactory.create(f, null, true);
		ppt.close();
	}

	@Test
	public void testClosingStreamOnException() throws IOException {
		InputStream is = OpenXML4JTestDataSamples.openSampleStream("dcterms_bug_56479.zip");
		File tmp = File.createTempFile("poi-test-truncated-zip", "");
		// create a corrupted zip file by truncating a valid zip file to the first 100 bytes
		OutputStream os = new FileOutputStream(tmp);
		for (int i = 0; i < 100; i++) {
			os.write(is.read());
		}
		os.flush();
		os.close();
		is.close();

		// feed the corrupted zip file to OPCPackage
		try {
			OPCPackage.open(tmp, PackageAccess.READ);
		} catch (Exception e) {
			// expected: the zip file is invalid
			// this test does not care if open() throws an exception or not.
		}
		// If the stream is not closed on exception, it will keep a file descriptor to tmp,
		// and requests to the OS to delete the file will fail.
		assertTrue("Can't delete tmp file", tmp.delete());
	}

	/**
	 * If ZipPackage is passed an invalid file, a call to close
	 *  (eg from the OPCPackage open method) should tidy up the
	 *  stream / file the broken file is being read from.
	 * See bug #60128 for more
	 */
	@Test(expected = NotOfficeXmlFileException.class)
	public void testTidyStreamOnInvalidFile1() throws Exception {
		openInvalidFile("SampleSS.ods", false);
	}

	@Test(expected = NotOfficeXmlFileException.class)
	public void testTidyStreamOnInvalidFile2() throws Exception {
		openInvalidFile("SampleSS.ods", true);
	}

	@Test(expected = NotOfficeXmlFileException.class)
	public void testTidyStreamOnInvalidFile3() throws Exception {
		openInvalidFile("SampleSS.txt", false);
	}

	@Test(expected = NotOfficeXmlFileException.class)
	public void testTidyStreamOnInvalidFile4() throws Exception {
		openInvalidFile("SampleSS.txt", true);
	}

	@Test(expected = InvalidFormatException.class)
	public void testBug62592() throws Exception {
		InputStream is = OpenXML4JTestDataSamples.openSampleStream("62592.thmx");
		/*OPCPackage p =*/ OPCPackage.open(is);
	}

	@Test
	public void testBug62592SequentialCallsToGetParts() throws Exception {
		//make absolutely certain that sequential calls don't throw InvalidFormatExceptions
		String originalFile = OpenXML4JTestDataSamples.getSampleFileName("TestPackageCommon.docx");
		OPCPackage p2 = OPCPackage.open(originalFile, PackageAccess.READ);
		p2.getParts();
		p2.getParts();
	}

	@Test
	public void testDoNotCloseStream() throws IOException {
		// up to JDK 10 we did use Mockito here, but OutputStream is
		// an abstract class and fails mocking with some changes in JDK 11
		// so we use a simple empty output stream implementation instead
		OutputStream os = new OutputStream() {
			@Override
			public void write(int b) {
			}

			@Override
			public void close() {
				throw new IllegalStateException("close should not be called here");
			}
		};

		try (XSSFWorkbook wb = new XSSFWorkbook()) {
			wb.createSheet();
			wb.write(os);
		}

		try (SXSSFWorkbook wb = new SXSSFWorkbook()) {
			wb.createSheet();
			wb.write(os);
		}
	}



	private static void openInvalidFile(final String name, final boolean useStream) throws IOException, InvalidFormatException {
		// Spreadsheet has a good mix of alternate file types
		final POIDataSamples files = POIDataSamples.getSpreadSheetInstance();
		ZipPackage pkgTest = null;
		try (final InputStream is = (useStream) ? files.openResourceAsStream(name) : null) {
			try (final ZipPackage pkg = (useStream) ? new ZipPackage(is, PackageAccess.READ) : new ZipPackage(files.getFile(name), PackageAccess.READ)) {
				pkgTest = pkg;
				assertNotNull(pkg.getZipArchive());
				assertFalse(pkg.getZipArchive().isClosed());
				pkg.getParts();
				fail("Shouldn't work");
			}
		} finally {
			if (pkgTest != null) {
				assertNotNull(pkgTest.getZipArchive());
				assertTrue(pkgTest.getZipArchive().isClosed());
			}
		}
	}

	@SuppressWarnings("SameParameterValue")
	private static <T extends Throwable> AnyCauseMatcher<T> getCauseMatcher(Class<T> cause, String message) {
    	// junit is only using hamcrest-core, so instead of adding hamcrest-beans, we provide the throwable
		// search with the basics...
		// see https://stackoverflow.com/a/47703937/2066598
		return new AnyCauseMatcher<>(cause, message);
	}

	private static class AnyCauseMatcher<T extends Throwable> extends TypeSafeMatcher<T> {
		private final Class<T> expectedType;
		private final String expectedMessage;

		AnyCauseMatcher(Class<T> expectedType, String expectedMessage) {
			this.expectedType = expectedType;
			this.expectedMessage = expectedMessage;
		}

		@Override
		protected boolean matchesSafely(final Throwable root) {
			for (Throwable t = root; t != null; t = t.getCause()) {
				if (t.getClass().isAssignableFrom(expectedType) && t.getMessage().contains(expectedMessage)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public void describeTo(Description description) {
			description.appendText("expects type ")
					.appendValue(expectedType)
					.appendText(" and a message ")
					.appendValue(expectedMessage);
		}
	}
}
