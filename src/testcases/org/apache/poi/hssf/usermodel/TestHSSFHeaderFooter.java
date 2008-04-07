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

package org.apache.poi.hssf.usermodel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;

/**
 * Tests row shifting capabilities.
 *
 *
 * @author Shawn Laubach (slaubach at apache dot com)
 */
public final class TestHSSFHeaderFooter extends TestCase {

	/**
	 * Tests that get header retreives the proper values.
	 *
	 * @author Shawn Laubach (slaubach at apache dot org)
	 */
	public void testRetrieveCorrectHeader() {
		// Read initial file in
		HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("EmbeddedChartHeaderTest.xls");
		HSSFSheet s = wb.getSheetAt( 0 );
    	HSSFHeader head = s.getHeader();
    
    	assertEquals("Top Left", head.getLeft());
    	assertEquals("Top Center", head.getCenter());
    	assertEquals("Top Right", head.getRight());
	}

	/**
	 * Tests that get header retreives the proper values.
	 *
	 * @author Shawn Laubach (slaubach at apache dot org)
	 */
	public void testRetrieveCorrectFooter() {
		// Read initial file in
		HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("EmbeddedChartHeaderTest.xls");
		HSSFSheet s = wb.getSheetAt( 0 );
    	HSSFFooter foot = s.getFooter();
    
    	assertEquals("Bottom Left", foot.getLeft());
    	assertEquals("Bottom Center", foot.getCenter());
    	assertEquals("Bottom Right", foot.getRight());
	}
	
	/**
	 * Testcase for Bug 17039 HSSFHeader  doesnot support DBCS 
	 */
	public void testHeaderHas16bitCharacter() {
		HSSFWorkbook b = new HSSFWorkbook();
		HSSFSheet s = b.createSheet("Test");
		HSSFHeader h = s.getHeader();
		h.setLeft("\u0391");
		h.setCenter("\u0392");
		h.setRight("\u0393");
		
		HSSFWorkbook b2 = HSSFTestDataSamples.writeOutAndReadBack(b);
		HSSFHeader h2 = b2.getSheet("Test").getHeader();
		
		assertEquals(h2.getLeft(),"\u0391");
		assertEquals(h2.getCenter(),"\u0392");
		assertEquals(h2.getRight(),"\u0393");
	}
	
	/**
	 * Testcase for Bug 17039 HSSFFooter doesnot support DBCS 
	 */
	 public void testFooterHas16bitCharacter() {
		HSSFWorkbook b = new HSSFWorkbook();
		HSSFSheet s = b.createSheet("Test");
		HSSFFooter f = s.getFooter();
		f.setLeft("\u0391");
		f.setCenter("\u0392");
		f.setRight("\u0393");
		
		HSSFWorkbook b2 = HSSFTestDataSamples.writeOutAndReadBack(b);
		HSSFFooter f2 = b2.getSheet("Test").getFooter();
		
		assertEquals(f2.getLeft(),"\u0391");
		assertEquals(f2.getCenter(),"\u0392");
		assertEquals(f2.getRight(),"\u0393");
	}

	public void testReadDBCSHeaderFooter() {
		HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("DBCSHeader.xls");
		HSSFSheet s = wb.getSheetAt(0);
		HSSFHeader h = s.getHeader();
		assertEquals("Header Left " ,h.getLeft(),"\u090f\u0915");
		assertEquals("Header Center " ,h.getCenter(),"\u0939\u094b\u0917\u093e");
		assertEquals("Header Right " ,h.getRight(),"\u091c\u093e");
		
		HSSFFooter f = s.getFooter();
		assertEquals("Footer Left " ,f.getLeft(),"\u091c\u093e");
		assertEquals("Footer Center " ,f.getCenter(),"\u091c\u093e");
		assertEquals("Footer Right " ,f.getRight(),"\u091c\u093e");
	}
}

