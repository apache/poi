
/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
        

package org.apache.poi.hssf.usermodel;

import junit.framework.TestCase;
import org.apache.poi.hssf.usermodel.HSSFHeader;
import org.apache.poi.hssf.usermodel.HSSFFooter;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Tests row shifting capabilities.
 *
 *
 * @author Shawn Laubach (slaubach at apache dot com)
 */

public class TestHSSFHeaderFooter extends TestCase {

    /**
     * Constructor for TestHeaderFooter.
     * @param arg0
     */
    public TestHSSFHeaderFooter(String arg0) {
	super(arg0);
    }

    /**
     * Tests that get header retreives the proper values.
     *
     * @author Shawn Laubach (slaubach at apache dot org)
     */
    public void testRetrieveCorrectHeader() throws Exception
    {
        // Read initial file in
        String filename = System.getProperty( "HSSF.testdata.path" );
        filename = filename + "/EmbeddedChartHeaderTest.xls";
        FileInputStream fin = new FileInputStream( filename );
        HSSFWorkbook wb = new HSSFWorkbook( fin );
        fin.close();
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
    public void testRetrieveCorrectFooter() throws Exception
    {
        // Read initial file in
        String filename = System.getProperty( "HSSF.testdata.path" );
        filename = filename + "/EmbeddedChartHeaderTest.xls";
        FileInputStream fin = new FileInputStream( filename );
        HSSFWorkbook wb = new HSSFWorkbook( fin );
        fin.close();
        HSSFSheet s = wb.getSheetAt( 0 );
	HSSFFooter foot = s.getFooter();

	assertEquals("Bottom Left", foot.getLeft());
	assertEquals("Bottom Center", foot.getCenter());
	assertEquals("Bottom Right", foot.getRight());
    }
}

