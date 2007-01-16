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

/**
 * @author aviks
 *
 * This testcase contains tests for bugs that are yet to be fixed. 
 * Therefore, the standard ant test target does not run these tests. 
 * Run this testcase with the single-test target. 
 * The names of the tests usually correspond to the Bugzilla id's
 * PLEASE MOVE tests from this class to TestBugs once the bugs are fixed,
 * so that they are then run automatically. 
 */
public class TestUnfixedBugs extends TestCase {


	public TestUnfixedBugs(String arg0) {
		super(arg0);

	}
	
	protected String cwd = System.getProperty("HSSF.testdata.path");
	
	 
	 /* ArrayIndexOutOfBound in BOFRecord */  
	 public void test28772() throws java.io.IOException {
       String filename = System.getProperty("HSSF.testdata.path");
       filename=filename+"/28772.xls";
       FileInputStream in = new FileInputStream(filename);
       HSSFWorkbook wb = new HSSFWorkbook(in);
       assertTrue("Read book fine!" , true);
   }
	 
	 /**
	     * Bug 37684: Unhandled Continue Record Error
	     * 
	     * BUT NOW(Jan07): It triggers bug 41026!!
	     * 
	     * java.lang.ArrayIndexOutOfBoundsException: 30
         at org.apache.poi.hssf.record.aggregates.ValueRecordsAggregate.rowHasCells(ValueRecordsAggregate.java:219)
	     */
	    public void test37684() throws Exception {
	        FileInputStream in = new FileInputStream(new File(cwd, "37684.xls"));
	        HSSFWorkbook wb = new HSSFWorkbook(in);
	        in.close();

	        HSSFSheet sheet = wb.getSheetAt( 0 );
	        assertNotNull(sheet);

	        assertTrue("No Exceptions while reading file", true);

	        //serialize and read again
	        ByteArrayOutputStream out = new ByteArrayOutputStream();
	        wb.write(out);
	        out.close();

	        wb = new HSSFWorkbook(new ByteArrayInputStream(out.toByteArray()));
	        assertTrue("No Exceptions while reading file", true);

	    }
	    
	    /**
	     * Bug 41139: Constructing HSSFWorkbook is failed,threw threw ArrayIndexOutOfBoundsException for creating UnknownRecord
	     * 
	     * BUT NOW (Jan07): It throws the following in write!!
	     * java.lang.RuntimeException: Coding Error: This method should never be called. This ptg should be converted
         at org.apache.poi.hssf.record.formula.AreaNPtg.writeBytes(AreaNPtg.java:54)
         at org.apache.poi.hssf.record.formula.Ptg.serializePtgStack(Ptg.java:384)
         at org.apache.poi.hssf.record.NameRecord.serialize(NameRecord.java:544)
         at org.apache.poi.hssf.model.Workbook.serialize(Workbook.java:757)
         at org.apache.poi.hssf.usermodel.HSSFWorkbook.getBytes(HSSFWorkbook.java:952)
         at org.apache.poi.hssf.usermodel.HSSFWorkbook.write(HSSFWorkbook.java:898)

	     */
	    public void test41139() throws Exception {
	        FileInputStream in = new FileInputStream(new File(cwd, "41139.xls"));
	        HSSFWorkbook wb = new HSSFWorkbook(in);
	        in.close();

	        assertTrue("No Exceptions while reading file", true);

	        //serialize and read again
	        ByteArrayOutputStream out = new ByteArrayOutputStream();
	        wb.write(out);
	        out.close();

	        wb = new HSSFWorkbook(new ByteArrayInputStream(out.toByteArray()));
	        assertTrue("No Exceptions while reading file", true);

	    }

	
}
