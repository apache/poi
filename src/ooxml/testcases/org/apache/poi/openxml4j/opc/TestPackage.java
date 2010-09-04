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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.TreeMap;
import java.util.Iterator;

import junit.framework.TestCase;

import org.apache.poi.openxml4j.OpenXML4JTestDataSamples;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import org.apache.poi.openxml4j.opc.internal.ContentTypeManager;
import org.apache.poi.openxml4j.opc.internal.FileHelper;
import org.apache.poi.util.TempFile;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.POILogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.io.SAXReader;

public final class TestPackage extends TestCase {
    private static final POILogger logger = POILogFactory.getLogger(TestPackage.class);

	/**
	 * Test that just opening and closing the file doesn't alter the document.
	 */
	public void testOpenSave() throws Exception {
		String originalFile = OpenXML4JTestDataSamples.getSampleFileName("TestPackageCommon.docx");
		File targetFile = OpenXML4JTestDataSamples.getOutputFile("TestPackageOpenSaveTMP.docx");

		OPCPackage p = OPCPackage.open(originalFile, PackageAccess.READ_WRITE);
		p.save(targetFile.getAbsoluteFile());

		// Compare the original and newly saved document
		assertTrue(targetFile.exists());
		ZipFileAssert.assertEquals(new File(originalFile), targetFile);
		assertTrue(targetFile.delete());
	}

	/**
	 * Test that when we create a new Package, we give it
	 *  the correct default content types
	 */
	public void testCreateGetsContentTypes() throws Exception {
		File targetFile = OpenXML4JTestDataSamples.getOutputFile("TestCreatePackageTMP.docx");

		// Zap the target file, in case of an earlier run
		if(targetFile.exists()) targetFile.delete();

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
	}

	/**
	 * Test package creation.
	 */
	public void testCreatePackageAddPart() throws Exception {
		File targetFile = OpenXML4JTestDataSamples.getOutputFile("TestCreatePackageTMP.docx");

		File expectedFile = OpenXML4JTestDataSamples.getSampleFile("TestCreatePackageOUTPUT.docx");

        // Zap the target file, in case of an earlier run
        if(targetFile.exists()) targetFile.delete();

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
        Namespace nsWordprocessinML = new Namespace("w",
                "http://schemas.openxmlformats.org/wordprocessingml/2006/main");
        Element elDocument = doc.addElement(new QName("document",
                nsWordprocessinML));
        Element elBody = elDocument.addElement(new QName("body",
                nsWordprocessinML));
        Element elParagraph = elBody.addElement(new QName("p",
                nsWordprocessinML));
        Element elRun = elParagraph
                .addElement(new QName("r", nsWordprocessinML));
        Element elText = elRun.addElement(new QName("t", nsWordprocessinML));
        elText.setText("Hello Open XML !");

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
	public void testCreatePackageWithCoreDocument() throws Exception {
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
        coreOut.write("<dummy-xml />".getBytes());
        coreOut.close();

		// And another bit
        PackagePartName sheetPartName = PackagingURIHelper.createPartName("/xl/worksheets/sheet1.xml");
        PackageRelationship rel =
        	 corePart.addRelationship(sheetPartName, TargetMode.INTERNAL, "http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet", "rSheet1");
        PackagePart part = pkg.createPart(sheetPartName, "application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml");
        // Dummy content again
        coreOut = corePart.getOutputStream();
        coreOut.write("<dummy-xml2 />".getBytes());
        coreOut.close();

        //add a relationship with internal target: "#Sheet1!A1"
        corePart.addRelationship(new URI("#Sheet1!A1"), TargetMode.INTERNAL, "http://schemas.openxmlformats.org/officeDocument/2006/relationships/hyperlink", "rId2");

        // Check things are as expected
        PackageRelationshipCollection coreRels =
        	pkg.getRelationshipsByType(PackageRelationshipTypes.CORE_DOCUMENT);
        assertEquals(1, coreRels.size());
        PackageRelationship coreRel = coreRels.getRelationship(0);
        assertEquals("/", coreRel.getSourceURI().toString());
        assertEquals("/xl/workbook.xml", coreRel.getTargetURI().toString());
        assertNotNull(pkg.getPart(coreRel));


        // Save and re-load
        pkg.close();
        File tmp = TempFile.createTempFile("testCreatePackageWithCoreDocument", ".zip");
            FileOutputStream fout = new FileOutputStream(tmp);
        fout.write(baos.toByteArray());
        fout.close();
        pkg = OPCPackage.open(tmp.getPath());
        //tmp.delete();

        // Check still right
        coreRels = pkg.getRelationshipsByType(PackageRelationshipTypes.CORE_DOCUMENT);
        assertEquals(1, coreRels.size());
        coreRel = coreRels.getRelationship(0);

        assertEquals("/", coreRel.getSourceURI().toString());
        assertEquals("/xl/workbook.xml", coreRel.getTargetURI().toString());
        corePart = pkg.getPart(coreRel);
        assertNotNull(corePart);

        PackageRelationshipCollection rels = corePart.getRelationshipsByType("http://schemas.openxmlformats.org/officeDocument/2006/relationships/hyperlink");
        assertEquals(1, rels.size());
        rel = rels.getRelationship(0);
        assertEquals("Sheet1!A1", rel.getTargetURI().getRawFragment());

        assertMSCompatibility(pkg);
    }

    private void assertMSCompatibility(OPCPackage pkg) throws Exception {
        PackagePartName relName = PackagingURIHelper.createPartName(PackageRelationship.getContainerPartRelationship());
        PackagePart relPart = pkg.getPart(relName);
        SAXReader reader = new SAXReader();
        Document xmlRelationshipsDoc = reader
                .read(relPart.getInputStream());

        Element root = xmlRelationshipsDoc.getRootElement();
        for (Iterator i = root
                .elementIterator(PackageRelationship.RELATIONSHIP_TAG_NAME); i
                .hasNext();) {
            Element element = (Element) i.next();
            String value = element.attribute(
                    PackageRelationship.TARGET_ATTRIBUTE_NAME)
                    .getValue();
            assertTrue("Root target must not start with a leadng slash ('/'): " + value, value.charAt(0) != '/');
        }

    }

    /**
	 * Test package opening.
	 */
	public void testOpenPackage() throws Exception {
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
		Namespace nsWordprocessinML = new Namespace("w",
				"http://schemas.openxmlformats.org/wordprocessingml/2006/main");
		Element elDocument = doc.addElement(new QName("document",
				nsWordprocessinML));
		Element elBody = elDocument.addElement(new QName("body",
				nsWordprocessinML));
		Element elParagraph = elBody.addElement(new QName("p",
				nsWordprocessinML));
		Element elRun = elParagraph
				.addElement(new QName("r", nsWordprocessinML));
		Element elText = elRun.addElement(new QName("t", nsWordprocessinML));
		elText.setText("Hello Open XML !");

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
	public void testSaveToOutputStream() throws Exception {
		String originalFile = OpenXML4JTestDataSamples.getSampleFileName("TestPackageCommon.docx");
		File targetFile = OpenXML4JTestDataSamples.getOutputFile("TestPackageOpenSaveTMP.docx");

		OPCPackage p = OPCPackage.open(originalFile, PackageAccess.READ_WRITE);
		FileOutputStream fout = new FileOutputStream(targetFile);
		p.save(fout);
		fout.close();

		// Compare the original and newly saved document
		assertTrue(targetFile.exists());
		ZipFileAssert.assertEquals(new File(originalFile), targetFile);
		assertTrue(targetFile.delete());
	}

	/**
	 * Checks that we can open+read a package from a
	 *  simple InputStream, in addition to the normal
	 *  reading from a file
	 */
	public void testOpenFromInputStream() throws Exception {
		String originalFile = OpenXML4JTestDataSamples.getSampleFileName("TestPackageCommon.docx");

		FileInputStream finp = new FileInputStream(originalFile);

		OPCPackage p = OPCPackage.open(finp);

		assertNotNull(p);
		assertNotNull(p.getRelationships());
		assertEquals(12, p.getParts().size());

		// Check it has the usual bits
		assertTrue(p.hasRelationships());
		assertTrue(p.containPart(PackagingURIHelper.createPartName("/_rels/.rels")));
	}

    /**
     * TODO: fix and enable
     */
    public void disabled_testRemovePartRecursive() throws Exception {
		String originalFile = OpenXML4JTestDataSamples.getSampleFileName("TestPackageCommon.docx");
		File targetFile = OpenXML4JTestDataSamples.getOutputFile("TestPackageRemovePartRecursiveOUTPUT.docx");
		File tempFile = OpenXML4JTestDataSamples.getOutputFile("TestPackageRemovePartRecursiveTMP.docx");

		OPCPackage p = OPCPackage.open(originalFile, PackageAccess.READ_WRITE);
		p.removePartRecursive(PackagingURIHelper.createPartName(new URI(
				"/word/document.xml")));
		p.save(tempFile.getAbsoluteFile());

		// Compare the original and newly saved document
		assertTrue(targetFile.exists());
		ZipFileAssert.assertEquals(targetFile, tempFile);
		assertTrue(targetFile.delete());
	}

	public void testDeletePart() throws InvalidFormatException {
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

	public void testDeletePartRecursive() throws InvalidFormatException {
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
	public void testOpenFileThenOverwrite() throws Exception {
        File tempFile = File.createTempFile("poiTesting","tmp");
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
        } catch(InvalidOperationException e) {}

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
    public void testOpenFileThenSaveDelete() throws Exception {
        File tempFile = File.createTempFile("poiTesting","tmp");
        File tempFile2 = File.createTempFile("poiTesting","tmp");
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

	private static ContentTypeManager getContentTypeManager(OPCPackage pkg) throws Exception {
		Field f = OPCPackage.class.getDeclaredField("contentTypeManager");
		f.setAccessible(true);
		return (ContentTypeManager)f.get(pkg);
	}
}
