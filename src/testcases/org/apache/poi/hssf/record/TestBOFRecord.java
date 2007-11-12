
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
        

package org.apache.poi.hssf.record;

import java.io.File;
import java.io.FileInputStream;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import junit.framework.TestCase;

public class TestBOFRecord extends TestCase
{
    private String _test_file_path;
    private static final String _test_file_path_property = "HSSF.testdata.path";

    public TestBOFRecord()
    {
        super();
        _test_file_path = System.getProperty( _test_file_path_property ) +
        	File.separator + "bug_42794.xls";
    }

    public void testBOFRecord() throws Exception {
    	POIFSFileSystem fs = new POIFSFileSystem(
    			new FileInputStream(_test_file_path)
    	);
    	
    	// This used to throw an error before
    	HSSFWorkbook hssf = new HSSFWorkbook(fs);
    }
}
