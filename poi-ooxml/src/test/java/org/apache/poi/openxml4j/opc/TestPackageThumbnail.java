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

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;

import org.apache.poi.openxml4j.OpenXML4JTestDataSamples;
import org.junit.jupiter.api.Test;

/**
 * Test the addition of thumbnail in a package.
 */
public final class TestPackageThumbnail {

	/**
	 * Test package addThumbnail() method.
	 */
	@Test
	void testSetProperties() throws Exception {
		String inputPath = OpenXML4JTestDataSamples.getSampleFileName("TestPackageThumbnail.docx");

		String imagePath = OpenXML4JTestDataSamples.getSampleFileName("thumbnail.jpg");

		File outputFile = OpenXML4JTestDataSamples.getOutputFile("TestPackageThumbnailOUTPUT.docx");

		// Open package
		try (OPCPackage p = OPCPackage.open(inputPath, PackageAccess.READ_WRITE)) {
    		p.addThumbnail(imagePath);
    		// Save the package in the output directory
    		p.save(outputFile);

    		// Open the newly created file to check core properties saved values.
    		try (OPCPackage p2 = OPCPackage.open(outputFile.getAbsolutePath(), PackageAccess.READ)) {
    			assertNotEquals(0, p2.getRelationshipsByType(PackageRelationshipTypes.THUMBNAIL).size(),
					"Thumbnail not added to the package !");
				p2.revert();
    		}
			p.revert();
		}
		assertTrue(outputFile.delete());
	}
}
