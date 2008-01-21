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
	
	 

    public void test43493() throws Exception {
        // Has crazy corrup subrecords on
        //  a EmbeddedObjectRefSubRecord
        File f = new File(cwd, "43493.xls");
        HSSFWorkbook wb = new HSSFWorkbook(
                new FileInputStream(f)
        );
    }
}
