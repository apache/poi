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

import junit.framework.TestCase;

import org.apache.poi.openxml4j.opc.Package;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.openxml4j.opc.PackageRelationshipTypes;

import org.apache.poi.openxml4j.TestCore;

/**
 * Test the addition of thumbnail in a package.
 * 
 * @author Julien Chable
 */
public class TestPackageThumbnail extends TestCase {

	TestCore testCore = new TestCore(this.getClass());

	/**
	 * Test package addThumbnail() method.
	 */
	public void testSetProperties() throws Exception {
		String inputPath = System.getProperty("openxml4j.testdata.input")
				+ File.separator + "TestPackageThumbnail.docx";

		String imagePath = System.getProperty("openxml4j.testdata.input")
				+ File.separator + "thumbnail.jpg";

		String outputFilename = System.getProperty("openxml4j.testdata.output")
				+ File.separator + "TestPackageThumbnailOUTPUT.docx";

		// Open package
		Package p = Package.open(inputPath, PackageAccess.READ_WRITE);
		p.addThumbnail(imagePath);
		// Save the package in the output directory
		p.save(new File(outputFilename));

		// Open the newly created file to check core properties saved values.
		File fOut = new File(outputFilename);
		Package p2 = Package.open(outputFilename, PackageAccess.READ);
		if (p2.getRelationshipsByType(PackageRelationshipTypes.THUMBNAIL)
				.size() == 0)
			fail("Thumbnail not added to the package !");
		p2.revert();
		//fOut.delete();
	}
}
