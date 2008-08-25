/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.poi.hssf.usermodel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;

import org.apache.poi.util.TempFile;

import junit.framework.TestCase;

public class TestUnicodeWorkbook extends TestCase {

    public TestUnicodeWorkbook(String s) {
        super(s);
    }
    
    /** Tests that all of the unicode capable string fields can be set, written and then read back
     * 
     *
     */
    public void testUnicodeInAll() throws Exception {
        HSSFWorkbook wb = new HSSFWorkbook();
        //Create a unicode dataformat (contains euro symbol)
        HSSFDataFormat df = wb.createDataFormat();
        final String formatStr = "_([$\u20ac-2]\\\\\\ * #,##0.00_);_([$\u20ac-2]\\\\\\ * \\\\\\(#,##0.00\\\\\\);_([$\u20ac-2]\\\\\\ *\\\"\\-\\\\\"??_);_(@_)";
        short fmt = df.getFormat(formatStr);
        
        //Create a unicode sheet name (euro symbol)
        HSSFSheet s = wb.createSheet("\u20ac");
        
        //Set a unicode header (you guessed it the euro symbol)
        HSSFHeader h = s.getHeader();
        h.setCenter("\u20ac");
        h.setLeft("\u20ac");
        h.setRight("\u20ac");
        
        //Set a unicode footer
        HSSFFooter f = s.getFooter();
        f.setCenter("\u20ac");
        f.setLeft("\u20ac");
        f.setRight("\u20ac");                

        HSSFRow r = s.createRow(0);
        HSSFCell c = r.createCell(1);
        c.setCellValue(12.34);
        c.getCellStyle().setDataFormat(fmt);
        
        HSSFCell c2 = r.createCell(2);
        c.setCellValue(new HSSFRichTextString("\u20ac"));

        HSSFCell c3 = r.createCell(3);
        String formulaString = "TEXT(12.34,\"\u20ac###,##\")";
        c3.setCellFormula(formulaString);

        
        File tempFile = TempFile.createTempFile("unicode", "test.xls");
        FileOutputStream stream = new FileOutputStream(tempFile);
        wb.write(stream);
        
        wb = null;
        FileInputStream in = new FileInputStream(tempFile);
        wb = new HSSFWorkbook(in);

        //Test the sheetname
        s = wb.getSheet("\u20ac");
        assertNotNull(s);
        
        //Test the header
        h = s.getHeader();
        assertEquals(h.getCenter(), "\u20ac");
        assertEquals(h.getLeft(), "\u20ac");
        assertEquals(h.getRight(), "\u20ac");
        
        //Test the footer
        f = s.getFooter();
        assertEquals(f.getCenter(), "\u20ac");
        assertEquals(f.getLeft(), "\u20ac");
        assertEquals(f.getRight(), "\u20ac");                

        //Test the dataformat
        r = s.getRow(0);
        c = r.getCell(1);
        df = wb.createDataFormat();
        assertEquals(formatStr, df.getFormat(c.getCellStyle().getDataFormat()));
        
        //Test the cell string value
        c2 = r.getCell(2);
        assertEquals(c.getRichStringCellValue().getString(), "\u20ac");
        
        //Test the cell formula
        c3 = r.getCell(3);
        assertEquals(c3.getCellFormula(), formulaString);
    }
    
    /** Tests Bug38230
     *  That a Umlat is written  and then read back.
     *  It should have been written as a compressed unicode.
     * 
     * 
     *
     */
    public void testUmlatReadWrite() throws Exception {
        HSSFWorkbook wb = new HSSFWorkbook();
        
        //Create a unicode sheet name (euro symbol)
        HSSFSheet s = wb.createSheet("test");
        
        HSSFRow r = s.createRow(0);
        HSSFCell c = r.createCell(1);
        c.setCellValue(new HSSFRichTextString("\u00e4"));
        
        //Confirm that the sring will be compressed
        assertEquals(c.getRichStringCellValue().getUnicodeString().getOptionFlags(), 0);
        
        File tempFile = TempFile.createTempFile("umlat", "test.xls");
        FileOutputStream stream = new FileOutputStream(tempFile);
        wb.write(stream);
        
        wb = null;
        FileInputStream in = new FileInputStream(tempFile);
        wb = new HSSFWorkbook(in);

        //Test the sheetname
        s = wb.getSheet("test");
        assertNotNull(s);
        
        c = r.getCell(1);
        assertEquals(c.getRichStringCellValue().getString(), "\u00e4");
    }    

}
