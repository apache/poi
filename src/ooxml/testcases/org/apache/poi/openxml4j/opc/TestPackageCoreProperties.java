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

import java.io.File;
import java.io.IOException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.poi.openxml4j.OpenXML4JTestDataSamples;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.util.Nullable;

public final class TestPackageCoreProperties extends TestCase {

	/**
	 * Test package core properties getters.
	 */
	public void testGetProperties() {
		try {
			// Open the package
			OPCPackage p = OPCPackage.open(OpenXML4JTestDataSamples.openSampleStream("TestPackageCoreProperiesGetters.docx"));
			compareProperties(p);
			p.revert();
		} catch (OpenXML4JException e) {
			Logger.getLogger("org.apache.poi.openxml4j.demo").debug(e.getMessage());
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Test package core properties setters.
	 */
	public void testSetProperties() throws Exception {
		String inputPath = OpenXML4JTestDataSamples.getSampleFileName("TestPackageCoreProperiesSetters.docx");

		File outputFile = OpenXML4JTestDataSamples.getOutputFile("TestPackageCoreProperiesSettersOUTPUT.docx");

		// Open package
		OPCPackage p = OPCPackage.open(inputPath, PackageAccess.READ_WRITE);

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		Date dateToInsert = df.parse("2007-05-12T08:00:00Z", new ParsePosition(
				0));

		PackageProperties props = p.getPackageProperties();
		props.setCategoryProperty("MyCategory");
		props.setContentStatusProperty("MyContentStatus");
		props.setContentTypeProperty("MyContentType");
		props.setCreatedProperty(new Nullable<Date>(dateToInsert));
		props.setCreatorProperty("MyCreator");
		props.setDescriptionProperty("MyDescription");
		props.setIdentifierProperty("MyIdentifier");
		props.setKeywordsProperty("MyKeywords");
		props.setLanguageProperty("MyLanguage");
		props.setLastModifiedByProperty("Julien Chable");
		props.setLastPrintedProperty(new Nullable<Date>(dateToInsert));
		props.setModifiedProperty(new Nullable<Date>(dateToInsert));
		props.setRevisionProperty("2");
		props.setTitleProperty("MyTitle");
		props.setSubjectProperty("MySubject");
		props.setVersionProperty("2");
		// Save the package in the output directory
		p.save(outputFile);

		// Open the newly created file to check core properties saved values.
		OPCPackage p2 = OPCPackage.open(outputFile.getAbsolutePath(), PackageAccess.READ);
		compareProperties(p2);
		p2.revert();
		outputFile.delete();
	}

	private void compareProperties(OPCPackage p) throws InvalidFormatException {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		Date expectedDate = df.parse("2007-05-12T08:00:00Z", new ParsePosition(
				0));

		// Gets the core properties
		PackageProperties props = p.getPackageProperties();
		assertEquals("MyCategory", props.getCategoryProperty().getValue());
		assertEquals("MyContentStatus", props.getContentStatusProperty()
				.getValue());
		assertEquals("MyContentType", props.getContentTypeProperty().getValue());
		assertEquals(expectedDate, props.getCreatedProperty().getValue());
		assertEquals("MyCreator", props.getCreatorProperty().getValue());
		assertEquals("MyDescription", props.getDescriptionProperty().getValue());
		assertEquals("MyIdentifier", props.getIdentifierProperty().getValue());
		assertEquals("MyKeywords", props.getKeywordsProperty().getValue());
		assertEquals("MyLanguage", props.getLanguageProperty().getValue());
		assertEquals("Julien Chable", props.getLastModifiedByProperty()
				.getValue());
		assertEquals(expectedDate, props.getLastPrintedProperty().getValue());
		assertEquals(expectedDate, props.getModifiedProperty().getValue());
		assertEquals("2", props.getRevisionProperty().getValue());
		assertEquals("MySubject", props.getSubjectProperty().getValue());
		assertEquals("MyTitle", props.getTitleProperty().getValue());
		assertEquals("2", props.getVersionProperty().getValue());
	}
}
