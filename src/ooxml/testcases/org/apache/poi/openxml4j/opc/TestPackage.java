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

import static org.apache.poi.openxml4j.OpenXML4JTestDataSamples.getOutputFile;
import static org.apache.poi.openxml4j.OpenXML4JTestDataSamples.getSampleFile;
import static org.apache.poi.openxml4j.OpenXML4JTestDataSamples.getSampleFileName;
import static org.apache.poi.openxml4j.OpenXML4JTestDataSamples.openSampleStream;
import static org.apache.poi.openxml4j.opc.PackagingURIHelper.createPartName;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.POIDataSamples;
import org.apache.poi.POITestCase;
import org.apache.poi.extractor.ExtractorFactory;
import org.apache.poi.extractor.POITextExtractor;
import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.ooxml.util.DocumentHelper;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException;
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
import org.apache.poi.xssf.usermodel.XSSFRelation;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFRelation;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public final class TestPackage {
    private static final POILogger logger = POILogFactory.getLogger(TestPackage.class);
	private static final String NS_OOXML_WP_MAIN = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";
	private static final String CONTENT_EXT_PROPS = "application/vnd.openxmlformats-officedocument.extended-properties+xml";
	private static final POIDataSamples xlsSamples = POIDataSamples.getSpreadSheetInstance();

	/**
	 * Test that just opening and closing the file doesn't alter the document.
	 */
    @Test
	void openSave() throws IOException, InvalidFormatException {
		String originalFile = getSampleFileName("TestPackageCommon.docx");
		File targetFile = getOutputFile("TestPackageOpenSaveTMP.docx");

        try (OPCPackage p = OPCPackage.open(originalFile, PackageAccess.READ_WRITE)) {
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
	}

	/**
	 * Test that when we create a new Package, we give it
	 *  the correct default content types
	 */
    @Test
	void createGetsContentTypes()
    throws IOException, InvalidFormatException, SecurityException, IllegalArgumentException {
		File targetFile = getOutputFile("TestCreatePackageTMP.docx");

		// Zap the target file, in case of an earlier run
		if(targetFile.exists()) {
			assertTrue(targetFile.delete());
		}

        try (OPCPackage pkg = OPCPackage.create(targetFile)) {
			try {
				// Check it has content types for rels and xml
				ContentTypeManager ctm = getContentTypeManager(pkg);
				assertEquals("application/xml", ctm.getContentType(createPartName("/foo.xml")));
				assertEquals(ContentTypes.RELATIONSHIPS_PART, ctm.getContentType(createPartName("/foo.rels")));
				assertNull(ctm.getContentType(createPartName("/foo.txt")));
			} finally {
				pkg.revert();
			}
		}
	}

	/**
	 * Test package creation.
	 */
    @Test
	void createPackageAddPart() throws IOException, InvalidFormatException {
		File targetFile = getOutputFile("TestCreatePackageTMP.docx");

		File expectedFile = getSampleFile("TestCreatePackageOUTPUT.docx");

        // Zap the target file, in case of an earlier run
        if(targetFile.exists()) {
			assertTrue(targetFile.delete());
		}

        // Create a package
        OPCPackage pkg = OPCPackage.create(targetFile);
        PackagePartName corePartName = createPartName("/word/document.xml");

        pkg.addRelationship(corePartName, TargetMode.INTERNAL,
                PackageRelationshipTypes.CORE_DOCUMENT, "rId1");

        PackagePart corePart = pkg.createPart(corePartName, XWPFRelation.DOCUMENT.getContentType());

        Document doc = DocumentHelper.createDocument();
        Element elDocument = doc.createElementNS(NS_OOXML_WP_MAIN, "w:document");
        doc.appendChild(elDocument);
        Element elBody = doc.createElementNS(NS_OOXML_WP_MAIN, "w:body");
        elDocument.appendChild(elBody);
        Element elParagraph = doc.createElementNS(NS_OOXML_WP_MAIN, "w:p");
        elBody.appendChild(elParagraph);
        Element elRun = doc.createElementNS(NS_OOXML_WP_MAIN, "w:r");
        elParagraph.appendChild(elRun);
        Element elText = doc.createElementNS(NS_OOXML_WP_MAIN, "w:t");
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
	void createPackageWithCoreDocument() throws IOException, InvalidFormatException, URISyntaxException, SAXException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (OPCPackage pkg = OPCPackage.create(baos)) {

			// Add a core document
			PackagePartName corePartName = createPartName("/xl/workbook.xml");
			// Create main part relationship
			pkg.addRelationship(corePartName, TargetMode.INTERNAL, PackageRelationshipTypes.CORE_DOCUMENT, "rId1");
			// Create main document part
			PackagePart corePart = pkg.createPart(corePartName, XSSFRelation.WORKBOOK.getContentType());
			// Put in some dummy content
			try (OutputStream coreOut = corePart.getOutputStream()) {
				coreOut.write("<dummy-xml />".getBytes(StandardCharsets.UTF_8));
			}

			// And another bit
			PackagePartName sheetPartName = createPartName("/xl/worksheets/sheet1.xml");
			PackageRelationship rel = corePart.addRelationship(
					sheetPartName, TargetMode.INTERNAL, XSSFRelation.WORKSHEET.getRelation(), "rSheet1");
			assertNotNull(rel);

			PackagePart part = pkg.createPart(sheetPartName, XSSFRelation.WORKSHEET.getContentType());
			assertNotNull(part);

			// Dummy content again
			try (OutputStream coreOut = corePart.getOutputStream()) {
				coreOut.write("<dummy-xml2 />".getBytes(StandardCharsets.UTF_8));
			}

			//add a relationship with internal target: "#Sheet1!A1"
			corePart.addRelationship(new URI("#Sheet1!A1"), TargetMode.INTERNAL, PackageRelationshipTypes.HYPERLINK_PART, "rId2");

			// Check things are as expected
			PackageRelationshipCollection coreRels =
					pkg.getRelationshipsByType(PackageRelationshipTypes.CORE_DOCUMENT);
			assertEquals(1, coreRels.size());
			PackageRelationship coreRel = coreRels.getRelationship(0);
			assertNotNull(coreRel);
			assertEquals("/", coreRel.getSourceURI().toString());
			assertEquals("/xl/workbook.xml", coreRel.getTargetURI().toString());
			assertNotNull(pkg.getPart(coreRel));
		}


		// Save and re-load
        File tmp = TempFile.createTempFile("testCreatePackageWithCoreDocument", ".zip");
		try (OutputStream fout = new FileOutputStream(tmp)) {
			baos.writeTo(fout);
			fout.flush();
		}

        try (OPCPackage pkg = OPCPackage.open(tmp.getPath())) {
            // Check still right
			PackageRelationshipCollection coreRels = pkg.getRelationshipsByType(PackageRelationshipTypes.CORE_DOCUMENT);
            assertEquals(1, coreRels.size());
			PackageRelationship coreRel = coreRels.getRelationship(0);

			assertNotNull(coreRel);
            assertEquals("/", coreRel.getSourceURI().toString());
            assertEquals("/xl/workbook.xml", coreRel.getTargetURI().toString());
			PackagePart corePart = pkg.getPart(coreRel);
            assertNotNull(corePart);

            PackageRelationshipCollection rels = corePart.getRelationshipsByType(PackageRelationshipTypes.HYPERLINK_PART);
            assertEquals(1, rels.size());
			PackageRelationship rel = rels.getRelationship(0);
			assertNotNull(rel);
            assertEquals("Sheet1!A1", rel.getTargetURI().getRawFragment());

            assertMSCompatibility(pkg);
        }
    }

    private void assertMSCompatibility(OPCPackage pkg) throws IOException, InvalidFormatException, SAXException {
        PackagePartName relName = createPartName(PackageRelationship.getContainerPartRelationship());
        PackagePart relPart = pkg.getPart(relName);

        Document xmlRelationshipsDoc = DocumentHelper.readDocument(relPart.getInputStream());

        Element root = xmlRelationshipsDoc.getDocumentElement();
        NodeList nodeList = root.getElementsByTagName(PackageRelationship.RELATIONSHIP_TAG_NAME);
        int nodeCount = nodeList.getLength();
        for (int i = 0; i < nodeCount; i++) {
            Element element = (Element) nodeList.item(i);
            String value = element.getAttribute(PackageRelationship.TARGET_ATTRIBUTE_NAME);
            assertTrue(value.charAt(0) != '/', "Root target must not start with a leading slash ('/'): " + value);
        }

    }

    /**
	 * Test package opening.
	 */
    @Test
	void openPackage() throws IOException, InvalidFormatException {
		File targetFile = getOutputFile("TestOpenPackageTMP.docx");

		File inputFile = getSampleFile("TestOpenPackageINPUT.docx");

		File expectedFile = getSampleFile("TestOpenPackageOUTPUT.docx");

		// Copy the input file in the output directory
		FileHelper.copyFile(inputFile, targetFile);

		// Create a package
		OPCPackage pkg = OPCPackage.open(targetFile.getAbsolutePath());

		// Modify core part
		PackagePartName corePartName = createPartName("/word/document.xml");

		PackagePart corePart = pkg.getPart(corePartName);

		// Delete some part to have a valid document
		for (PackageRelationship rel : corePart.getRelationships()) {
			corePart.removeRelationship(rel.getId());
			pkg.removePart(createPartName(PackagingURIHelper
					.resolvePartUri(corePart.getPartName().getURI(), rel
							.getTargetURI())));
		}

		// Create a content
		Document doc = DocumentHelper.createDocument();
        Element elDocument = doc.createElementNS(NS_OOXML_WP_MAIN, "w:document");
        doc.appendChild(elDocument);
        Element elBody = doc.createElementNS(NS_OOXML_WP_MAIN, "w:body");
        elDocument.appendChild(elBody);
        Element elParagraph = doc.createElementNS(NS_OOXML_WP_MAIN, "w:p");
        elBody.appendChild(elParagraph);
        Element elRun = doc.createElementNS(NS_OOXML_WP_MAIN, "w:r");
        elParagraph.appendChild(elRun);
        Element elText = doc.createElementNS(NS_OOXML_WP_MAIN, "w:t");
        elRun.appendChild(elText);
        elText.setTextContent("Hello Open XML !");

		StreamHelper.saveXmlInStream(doc, corePart.getOutputStream());

		// Save and close
		assertDoesNotThrow(pkg::close);

		ZipFileAssert.assertEquals(expectedFile, targetFile);
		assertTrue(targetFile.delete());
	}

	/**
	 * Checks that we can write a package to a simple
	 *  OutputStream, in addition to the normal writing
	 *  to a file
	 */
    @Test
	void saveToOutputStream() throws IOException, InvalidFormatException {
		String originalFile = getSampleFileName("TestPackageCommon.docx");
		File targetFile = getOutputFile("TestPackageOpenSaveTMP.docx");

		try (OPCPackage p = OPCPackage.open(originalFile, PackageAccess.READ_WRITE)) {
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
	}

	/**
	 * Checks that we can open+read a package from a
	 *  simple InputStream, in addition to the normal
	 *  reading from a file
	 */
    @Test
	void openFromInputStream() throws IOException, InvalidFormatException {
		String originalFile = getSampleFileName("TestPackageCommon.docx");

		try (FileInputStream finp = new FileInputStream(originalFile);
			 OPCPackage p = OPCPackage.open(finp)) {
			try {
				assertNotNull(p);
				assertNotNull(p.getRelationships());
				assertEquals(12, p.getParts().size());

				// Check it has the usual bits
				assertTrue(p.hasRelationships());
				assertTrue(p.containPart(createPartName("/_rels/.rels")));
			} finally {
				p.revert();
			}
		}
	}

    /**
     * TODO: fix and enable
     */
    @Test
	@Disabled
    void removePartRecursive() throws IOException, InvalidFormatException, URISyntaxException {
		String originalFile = getSampleFileName("TestPackageCommon.docx");
		File targetFile = getOutputFile("TestPackageRemovePartRecursiveOUTPUT.docx");
		File tempFile = getOutputFile("TestPackageRemovePartRecursiveTMP.docx");

		try (OPCPackage p = OPCPackage.open(originalFile, PackageAccess.READ_WRITE))  {
			p.removePartRecursive(createPartName(new URI("/word/document.xml")));
			p.save(tempFile.getAbsoluteFile());

			// Compare the original and newly saved document
			assertTrue(targetFile.exists());
			ZipFileAssert.assertEquals(targetFile, tempFile);
			assertTrue(targetFile.delete());
			p.revert();
		}
	}

    @Test
	void deletePart() throws InvalidFormatException, IOException {
		final TreeMap<PackagePartName, String> expectedValues = new TreeMap<>();
		final TreeMap<PackagePartName, String> values = new TreeMap<>();

		// Expected values
		expectedValues.put(createPartName("/_rels/.rels"), ContentTypes.RELATIONSHIPS_PART);
		expectedValues.put(createPartName("/docProps/app.xml"), CONTENT_EXT_PROPS);
		expectedValues.put(createPartName("/docProps/core.xml"), ContentTypes.CORE_PROPERTIES_PART);
		expectedValues.put(createPartName("/word/fontTable.xml"), XWPFRelation.FONT_TABLE.getContentType());
		expectedValues.put(createPartName("/word/media/image1.gif"), XWPFRelation.IMAGE_GIF.getContentType());
		expectedValues.put(createPartName("/word/settings.xml"), XWPFRelation.SETTINGS.getContentType());
		expectedValues.put(createPartName("/word/styles.xml"), XWPFRelation.STYLES.getContentType());
		expectedValues.put(createPartName("/word/theme/theme1.xml"), XWPFRelation.THEME.getContentType());
		expectedValues.put(createPartName("/word/webSettings.xml"), XWPFRelation.WEB_SETTINGS.getContentType());

		String filepath = getSampleFileName("sample.docx");

        try (OPCPackage p = OPCPackage.open(filepath, PackageAccess.READ_WRITE)) {
        	try {
				// Remove the core part
				p.deletePart(createPartName("/word/document.xml"));

				for (PackagePart part : p.getParts()) {
					values.put(part.getPartName(), part.getContentType());
					logger.log(POILogger.DEBUG, part.getPartName());
				}

				// Compare expected values with values return by the package
				for (PackagePartName partName : expectedValues.keySet()) {
					assertNotNull(values.get(partName));
					assertEquals(expectedValues.get(partName), values.get(partName));
				}
			} finally {
				// Don't save modifications
				p.revert();
			}
		}
	}

    @Test
	void deletePartRecursive() throws InvalidFormatException, IOException {
		final TreeMap<PackagePartName, String> expectedValues = new TreeMap<>();
		final TreeMap<PackagePartName, String> values = new TreeMap<>();

		// Expected values
		expectedValues.put(createPartName("/_rels/.rels"), ContentTypes.RELATIONSHIPS_PART);
		expectedValues.put(createPartName("/docProps/app.xml"), CONTENT_EXT_PROPS);
		expectedValues.put(createPartName("/docProps/core.xml"), ContentTypes.CORE_PROPERTIES_PART);

		String filepath = getSampleFileName("sample.docx");

        try (OPCPackage p = OPCPackage.open(filepath, PackageAccess.READ_WRITE)) {
        	try {
				// Remove the core part
				p.deletePartRecursive(createPartName("/word/document.xml"));

				for (PackagePart part : p.getParts()) {
					values.put(part.getPartName(), part.getContentType());
					logger.log(POILogger.DEBUG, part.getPartName());
				}

				// Compare expected values with values return by the package
				for (PackagePartName partName : expectedValues.keySet()) {
					assertNotNull(values.get(partName));
					assertEquals(expectedValues.get(partName), values.get(partName));
				}
			} finally {
				// Don't save modifications
				p.revert();
			}
		}
	}

	/**
	 * Test that we can open a file by path, and then
	 *  write changes to it.
	 */
    @Test
	void openFileThenOverwrite() throws IOException, InvalidFormatException {
        File tempFile = TempFile.createTempFile("poiTesting","tmp");
        File origFile = getSampleFile("TestPackageCommon.docx");
        FileHelper.copyFile(origFile, tempFile);

        // Open and close the temp file
        try (OPCPackage p = OPCPackage.open(tempFile.toString(), PackageAccess.READ_WRITE)) {
        	assertNotNull(p);
		}
        // Delete it
        assertTrue(tempFile.delete());

        // Reset
        FileHelper.copyFile(origFile, tempFile);
		try (OPCPackage p = OPCPackage.open(tempFile.toString(), PackageAccess.READ_WRITE)) {
			// Save it to the same file - not allowed
			assertThrows(InvalidOperationException.class, () -> p.save(tempFile),
				"You shouldn't be able to call save(File) to overwrite the current file");
		}
        // Delete it
        assertTrue(tempFile.delete());


        // Open it read only, then close and delete - allowed
        FileHelper.copyFile(origFile, tempFile);
		try (OPCPackage p = OPCPackage.open(tempFile.toString(), PackageAccess.READ)) {
			assertNotNull(p);
		}
        assertTrue(tempFile.delete());
	}

    /**
     * Test that we can open a file by path, save it
     *  to another file, then delete both
     */
    @Test
    void openFileThenSaveDelete() throws IOException, InvalidFormatException {
        File tempFile = TempFile.createTempFile("poiTesting","tmp");
        File tempFile2 = TempFile.createTempFile("poiTesting","tmp");
        File origFile = getSampleFile("TestPackageCommon.docx");
        FileHelper.copyFile(origFile, tempFile);

        // Open the temp file
		try (OPCPackage p = OPCPackage.open(tempFile.toString(), PackageAccess.READ_WRITE)) {
			// Save it to a different file
			p.save(tempFile2);
		}

        // Delete both the files
        assertTrue(tempFile.delete());
        assertTrue(tempFile2.delete());
    }

	private static ContentTypeManager getContentTypeManager(OPCPackage pkg) {
	    return POITestCase.getFieldValue(OPCPackage.class, pkg, ContentTypeManager.class, "contentTypeManager");
	}

    @Test
    void getPartsByName() throws InvalidFormatException, IOException {
        String filepath =  getSampleFileName("sample.docx");

        try (OPCPackage pkg = OPCPackage.open(filepath, PackageAccess.READ_WRITE)) {
			try {
				List<PackagePart> rs = pkg.getPartsByName(Pattern.compile("/word/.*?\\.xml"));
				HashMap<String, PackagePart> selected = new HashMap<>();

				for (PackagePart p : rs)
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
    }

    @Test
    void getPartSize() throws IOException, InvalidFormatException {
       String filepath =  getSampleFileName("sample.docx");
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
    void replaceContentType() throws IOException, InvalidFormatException {
        try (InputStream is = openSampleStream("sample.xlsx");
        	OPCPackage p = OPCPackage.open(is)) {
			try {
				ContentTypeManager mgr = getContentTypeManager(p);

				assertTrue(mgr.isContentTypeRegister(XSSFRelation.WORKBOOK.getContentType()));
				assertFalse(mgr.isContentTypeRegister(XSSFRelation.MACROS_WORKBOOK.getContentType()));
				assertTrue(p.replaceContentType(XSSFRelation.WORKBOOK.getContentType(), XSSFRelation.MACROS_WORKBOOK.getContentType()));

				assertFalse(mgr.isContentTypeRegister(XSSFRelation.WORKBOOK.getContentType()));
				assertTrue(mgr.isContentTypeRegister(XSSFRelation.MACROS_WORKBOOK.getContentType()));
			} finally {
				p.revert();
			}
		}
    }

	@SuppressWarnings("unchecked")
	@ParameterizedTest
	@CsvSource({
		"SampleSS.xls, org.apache.poi.openxml4j.exceptions.OLE2NotOfficeXmlFileException, The supplied data appears to be in the OLE2 Format, You are calling the part of POI that deals with OOXML",
		"SampleSS.xml, org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException, The supplied data appears to be a raw XML file, Formats such as Office 2003 XML",
		"SampleSS.ods, org.apache.poi.openxml4j.exceptions.ODFNotOfficeXmlFileException, The supplied data appears to be in ODF, Formats like these (eg ODS",
		"SampleSS.txt, org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException, No valid entries or contents found, not a valid OOXML"
	})
	void NonOOXML_File(String file, String exClazzStr, String msg1, String msg2) throws Exception {
    	Class<? extends Exception> exClazz = (Class<? extends Exception>)Class.forName(exClazzStr);

		try (InputStream stream = xlsSamples.openResourceAsStream(file)) {
			Executable[] trs = {
				() -> OPCPackage.open(stream),
				() -> OPCPackage.open(xlsSamples.getFile(file))
			};
			for (Executable tr : trs) {
				Exception ex = assertThrows(exClazz, tr, "Shouldn't be able to open "+file);
				Stream.of(msg1, msg2).forEach(mp -> assertTrue(ex.getMessage().contains(mp)));
			}
		}
	}

	/**
	 * Zip bomb handling test
	 *
	 * see bug #50090 / #56865
	 */
    @Test
    void zipBombCreateAndHandle()
    throws IOException, EncryptedDocumentException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream(2500000);

        try (ZipFile zipFile = ZipHelper.openZipFile(getSampleFile("sample.xlsx"));
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
                            byte[] spam = new byte[0x7FFF];
							Arrays.fill(spam, (byte) ' ');
							// 0x7FFF0000 is the maximum for 32-bit zips, but less still works
							while (size < 0x7FFF00) {
								append.write(spam);
								size += spam.length;
							}
							append.write("</Types>".getBytes(StandardCharsets.UTF_8));
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

		IOException ex = assertThrows(
			IOException.class,
			() -> WorkbookFactory.create(new ByteArrayInputStream(bos.toByteArray()))
		);
        assertTrue(ex.getMessage().contains("Zip bomb detected!"));
    }

	@Test
	void testZipEntityExpansionTerminates() {
		IllegalStateException ex = assertThrows(
			IllegalStateException.class,
			() -> openXmlBombFile("poc-shared-strings.xlsx")
		);
		assertTrue(ex.getMessage().contains("The text would exceed the max allowed overall size of extracted text."));
	}

	@Test
	void testZipEntityExpansionSharedStringTableEvents() {
		boolean before = ExtractorFactory.getThreadPrefersEventExtractors();
		ExtractorFactory.setThreadPrefersEventExtractors(true);
		try {
			IllegalStateException ex = assertThrows(
				IllegalStateException.class,
				() -> openXmlBombFile("poc-shared-strings.xlsx")
			);
			assertTrue(ex.getMessage().contains("The text would exceed the max allowed overall size of extracted text."));
		} finally {
			ExtractorFactory.setThreadPrefersEventExtractors(before);
		}
	}


	@Test
	void testZipEntityExpansionExceedsMemory() {
		IOException ex = assertThrows(
			IOException.class,
			() -> openXmlBombFile("poc-xmlbomb.xlsx")
		);
		assertTrue(ex.getMessage().contains("unable to parse shared strings table"));
		assertTrue(matchSAXEx(ex));
	}

	@Test
	void testZipEntityExpansionExceedsMemory2() {
		IOException ex = assertThrows(
			IOException.class,
			() -> openXmlBombFile("poc-xmlbomb-empty.xlsx")
		);
		assertTrue(ex.getMessage().contains("unable to parse shared strings table"));
		assertTrue(matchSAXEx(ex));
	}

	private static boolean matchSAXEx(Exception root) {
		for (Throwable t = root; t != null; t = t.getCause()) {
			if (t.getClass().isAssignableFrom(SAXParseException.class) &&
				t.getMessage().contains("The parser has encountered more than")) {
				return true;
			}
		}
		return false;
	}

	private void openXmlBombFile(String file) throws IOException {
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
    void zipBombCheckSizesWithinLimits() throws IOException, EncryptedDocumentException {
		getZipStatsAndConsume((max_size, min_ratio) -> {
			// use values close to, but within the limits
			ZipSecureFile.setMinInflateRatio(min_ratio - 0.002);
			assertEquals(min_ratio - 0.002, ZipSecureFile.getMinInflateRatio(), 0.00001);
			ZipSecureFile.setMaxEntrySize(max_size + 1);
			assertEquals(max_size + 1, ZipSecureFile.getMaxEntrySize());
		});
	}

	@Test
	void zipBombCheckSizesRatioTooSmall() {
		POIXMLException ex = assertThrows(
			POIXMLException.class,
			() -> getZipStatsAndConsume((max_size, min_ratio) -> {
				// check ratio out of bounds
				ZipSecureFile.setMinInflateRatio(min_ratio+0.002);
			})
		);
		assertTrue(ex.getMessage().contains("You can adjust this limit via ZipSecureFile.setMinInflateRatio()"));
	}

	@Test
	void zipBombCheckSizesSizeTooBig() throws EncryptedDocumentException {
		POIXMLException ex = assertThrows(
			POIXMLException.class,
			() -> getZipStatsAndConsume((max_size, min_ratio) -> {
				// check max entry size ouf of bounds
				ZipSecureFile.setMinInflateRatio(min_ratio-0.002);
				ZipSecureFile.setMaxEntrySize(max_size-200);
			})
		);
		assertTrue(ex.getMessage().contains("You can adjust this limit via ZipSecureFile.setMaxEntrySize()"));
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
				// add zip entry header ~ 128 bytes
				long size = ze.getSize()+128;
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
    void testConstructors() throws IOException {
        // verify the various ways to construct a ZipSecureFile
        File file = getSampleFile("sample.xlsx");
        try (ZipSecureFile zipFile = new ZipSecureFile(file)) {
			assertNotNull(zipFile.getName());
		}

        try (ZipSecureFile zipFile = new ZipSecureFile(file.getAbsolutePath())) {
			assertNotNull(zipFile.getName());
		}
    }

    @Test
    void testMaxTextSize() {
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
    void testCorruptFile() {
        File file = getSampleFile("invalid.xlsx");
        assertThrows(NotOfficeXmlFileException.class, () -> OPCPackage.open(file, PackageAccess.READ));
    }

	private interface CountingStream {
    	InputStream create(InputStream is, int length);
	}

	// bug 61381
    @Test
    void testTooShortFilterStreams() throws IOException {
		for (String file : new String[]{"sample.xlsx","SampleSS.xls"}) {
			for (CountingStream cs : new CountingStream[]{PushbackInputStream::new, BufferedInputStream::new}) {
				try (InputStream is = cs.create(xlsSamples.openResourceAsStream(file), 2);
					 Workbook wb = WorkbookFactory.create(is)) {
					assertEquals(3, wb.getNumberOfSheets());
				}
			}
		}
    }

	@Test
	void testBug56479() throws Exception {
		try (InputStream is = openSampleStream("dcterms_bug_56479.zip");
			OPCPackage p = OPCPackage.open(is)) {

			// Check we found the contents of it
			boolean foundCoreProps = false, foundDocument = false, foundTheme1 = false;
			for (final PackagePart part : p.getParts()) {
				final String partName = part.getPartName().toString();
				final String contentType = part.getContentType();
				switch (partName) {
					case "/docProps/core.xml":
						assertEquals(ContentTypes.CORE_PROPERTIES_PART, contentType);
						foundCoreProps = true;
						break;
					case "/word/document.xml":
						assertEquals(XWPFRelation.DOCUMENT.getContentType(), contentType);
						foundDocument = true;
						break;
					case "/word/theme/theme1.xml":
						assertEquals(XWPFRelation.THEME.getContentType(), contentType);
						foundTheme1 = true;
						break;
				}
			}
			assertTrue(foundCoreProps, "Core not found in " + p.getParts());
			assertFalse(foundDocument, "Document should not be found in " + p.getParts());
			assertFalse(foundTheme1, "Theme1 should not found in " + p.getParts());
		}
	}

	@Test
	void unparseableCentralDirectory() throws IOException {
		File f = getSampleFile("at.pzp.www_uploads_media_PP_Scheinecker-jdk6error.pptx");
		try (SlideShow<?,?> ppt = SlideShowFactory.create(f, null, true)) {
			assertNotNull(ppt);
			assertNotNull(ppt.getSlides().get(0));
		}
	}

	@Test
	void testClosingStreamOnException() throws IOException {
		File tmp = File.createTempFile("poi-test-truncated-zip", "");

		// create a corrupted zip file by truncating a valid zip file to the first 100 bytes
		try (InputStream is = openSampleStream("dcterms_bug_56479.zip");
			OutputStream os = new FileOutputStream(tmp)) {
			IOUtils.copy(is, os, 100);
		}

		// feed the corrupted zip file to OPCPackage
		// expected: the zip file is invalid
		// this test does not care if open() throws an exception or not.
		assertThrows(Exception.class, () -> OPCPackage.open(tmp, PackageAccess.READ));

		// If the stream is not closed on exception, it will keep a file descriptor to tmp,
		// and requests to the OS to delete the file will fail.
		assertTrue(tmp.delete(), "Can't delete tmp file");
	}

	/**
	 * If ZipPackage is passed an invalid file, a call to close
	 *  (eg from the OPCPackage open method) should tidy up the
	 *  stream / file the broken file is being read from.
	 * See bug #60128 for more
	 */
	@Test
	void testTidyStreamOnInvalidFile1() throws Exception {
		openInvalidFile("SampleSS.ods", false);
	}

	@Test
	void testTidyStreamOnInvalidFile2() throws Exception {
		openInvalidFile("SampleSS.ods", true);
	}

	@Test
	void testTidyStreamOnInvalidFile3() throws Exception {
		openInvalidFile("SampleSS.txt", false);
	}

	@Test
	void testTidyStreamOnInvalidFile4() throws Exception {
		openInvalidFile("SampleSS.txt", true);
	}

	@Test
	void testBug62592() throws Exception {
		try (InputStream is = openSampleStream("62592.thmx")) {
			assertThrows(InvalidFormatException.class, () -> OPCPackage.open(is));
		}
	}

	@Test
	void testBug62592SequentialCallsToGetParts() throws Exception {
		//make absolutely certain that sequential calls don't throw InvalidFormatExceptions
		String originalFile = getSampleFileName("TestPackageCommon.docx");
		try (OPCPackage p2 = OPCPackage.open(originalFile, PackageAccess.READ)) {
			assertDoesNotThrow(p2::getParts);
			assertDoesNotThrow(p2::getParts);
		}
	}

	@Test
	void testDoNotCloseStream() throws IOException {
		// up to JDK 10 we did use Mockito here, but OutputStream is
		// an abstract class and fails mocking with some changes in JDK 11
		// so we use a simple empty output stream implementation instead
		OutputStream os = new OutputStream() {
			@Override
			public void write(int b) {
			}

			@Override
			public void close() {
				fail("close should not be called here");
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



	private static void openInvalidFile(final String name, final boolean useStream) throws IOException {
		ZipPackage[] pkgTest = { null };
		try (final InputStream is = (useStream) ? xlsSamples.openResourceAsStream(name) : null) {
			assertThrows(NotOfficeXmlFileException.class, () -> {
				try (final ZipPackage pkg = (useStream)
					? new ZipPackage(is, PackageAccess.READ)
					: new ZipPackage(xlsSamples.getFile(name), PackageAccess.READ)) {
					pkgTest[0] = pkg;
					assertNotNull(pkg.getZipArchive());
					assertFalse(pkg.getZipArchive().isClosed());
					pkg.getParts();
				}
			});
		} finally {
			if (pkgTest[0] != null) {
				assertNotNull(pkgTest[0].getZipArchive());
				assertTrue(pkgTest[0].getZipArchive().isClosed());
			}
		}
	}

	@SuppressWarnings("UnstableApiUsage")
	@Test
	void testBug63029() throws Exception {
		File testFile = getSampleFile("sample.docx");
		File tmpFile = getOutputFile("Bug63029.docx");
		Files.copy(testFile, tmpFile);

		int numPartsBefore = 0;
		String md5Before = Files.asByteSource(tmpFile).hash(Hashing.sha256()).toString();

		try(OPCPackage pkg = OPCPackage.open(tmpFile, PackageAccess.READ_WRITE))
		{
			numPartsBefore = pkg.getParts().size();

			// add a marshaller that will throw an exception on save
			pkg.addMarshaller("poi/junit", (part, out) -> {
				throw new RuntimeException("Bugzilla 63029");
			});

			pkg.createPart(createPartName("/poi/test.xml"), "poi/junit");

			RuntimeException ex = assertThrows(RuntimeException.class, pkg::close);
			// verify there was an exception while closing the file
			assertEquals("Fail to save: an error occurs while saving the package : Bugzilla 63029", ex.getMessage());
		}

		// assert that md5 after closing is the same, i.e. the source is left intact
		String md5After = Files.asByteSource(tmpFile).hash(Hashing.sha256()).toString();
		assertEquals(md5Before, md5After);

		// try to read the source file once again
		try ( OPCPackage pkg = OPCPackage.open(tmpFile, PackageAccess.READ_WRITE)){
			// the source is still a valid zip archive.
			// prior to the fix this used to throw NotOfficeXmlFileException("archive is not a ZIP archive")

			// assert that the number of parts remained the same
			assertEquals(pkg.getParts().size(), numPartsBefore);
		}

	}
}
