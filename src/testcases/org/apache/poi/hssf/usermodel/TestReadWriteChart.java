
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

import junit.framework.TestCase;
import org.apache.poi.hssf.model.Sheet;
import org.apache.poi.hssf.record.BOFRecord;
import org.apache.poi.hssf.record.EOFRecord;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.FileInputStream;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * @author Glen Stampoultzis (glens at apache.org)
 */

public class TestReadWriteChart
    extends TestCase
{
    public TestReadWriteChart(String s)
    {
        super(s);
    }

    /**
     * In the presence of a chart we need to make sure BOF/EOF records still exist.
     */

    public void testBOFandEOFRecords()
        throws Exception
    {
        //System.out.println("made it in testBOFandEOF");
        String          path      = System.getProperty("HSSF.testdata.path");
        String          filename  = path + "/SimpleChart.xls";
        //System.out.println("path is "+path);
        POIFSFileSystem fs        =
            new POIFSFileSystem(new FileInputStream(filename));
        //System.out.println("opened file");
        HSSFWorkbook    workbook  = new HSSFWorkbook(fs);
        HSSFSheet       sheet     = workbook.getSheetAt(0);
        HSSFRow         firstRow  = sheet.getRow(0);
        HSSFCell        firstCell = firstRow.getCell(( short ) 0);

        //System.out.println("first assertion for date");
        assertEquals(new GregorianCalendar(2000, 0, 1, 10, 51, 2).getTime(),
                     HSSFDateUtil
                         .getJavaDate(firstCell.getNumericCellValue()));
        HSSFRow  row  = sheet.createRow(( short ) 15);
        HSSFCell cell = row.createCell(( short ) 1);

        cell.setCellValue(22);
        Sheet newSheet = workbook.getSheetAt(0).getSheet();
        List  records  = newSheet.getRecords();

        //System.out.println("BOF Assertion");
        assertTrue(records.get(0) instanceof BOFRecord);
        //System.out.println("EOF Assertion");
        assertTrue(records.get(records.size() - 1) instanceof EOFRecord);
    }
    
    public static void main(String [] args)
    {
        String filename = System.getProperty("HSSF.testdata.path");

        // assume andy is running this in the debugger
        if (filename == null)
        {
            if (args != null && args[0].length() == 1) {
            System.setProperty(
                "HSSF.testdata.path",
                args[0]);
            } else {
                System.err.println("Geesh, no HSSF.testdata.path system " +
                          "property, no command line arg with the path "+
                          "what do you expect me to do, guess where teh data " +
                          "files are?  Sorry, I give up!");
                                   
            }
            
        }
        System.out
            .println("Testing org.apache.poi.hssf.usermodel.TestReadWriteChart");
        junit.textui.TestRunner.run(TestReadWriteChart.class);
    }
    
}
