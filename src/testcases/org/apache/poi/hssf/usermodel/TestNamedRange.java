/*
 * RangeTestTest.java
 * NetBeans JUnit based test
 *
 * Created on April 21, 2002, 6:23 PM
 */

package org.apache.poi.hssf.usermodel;

import junit.framework.*;

import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * 
 * @author ROMANL
 * @author Andrew C. Oliver (acoliver at apache dot org)
 */
public class TestNamedRange
    extends TestCase {
    
    public TestNamedRange(String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        String filename = System.getProperty("HSSF.testdata.path");
        
        // assume andy is running this in the debugger
        if (filename == null)
        {
            if (args != null && args.length == 1) {
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
        
        
        junit.textui.TestRunner.run(TestNamedRange.class);
    }
    
    /** Test of TestCase method, of class test.RangeTest. */
    public void testNamedRange() 
        throws IOException
    {
        FileInputStream fis = null;
        POIFSFileSystem fs  = null;
        HSSFWorkbook wb     = null;
        
        String filename = System.getProperty("HSSF.testdata.path");

        filename = filename + "/Simple.xls";
        
        
            fis = new FileInputStream(filename);
            fs = new POIFSFileSystem(fis);
            wb = new HSSFWorkbook(fs);
        
        
        //Creating new Named Range
        HSSFName newNamedRange = wb.createName();
        
        //Getting Sheet Name for the reference
        String sheetName = wb.getSheetName(0);
        
        //Setting its name
        newNamedRange.setNameName("RangeTest");
        //Setting its reference
        newNamedRange.setReference(sheetName + ".$D$4:$E$8");
  
        //Getting NAmed Range
        HSSFName namedRange1 = wb.getNameAt(0);
        //Getting it sheet name
        sheetName = namedRange1.getSheetName();
        //Getting its reference
        String referece = namedRange1.getReference();
                               
        File             file = File.createTempFile("testNamedRange",
                                        ".xls");

        FileOutputStream fileOut = new FileOutputStream(file);
        wb.write(fileOut);
        fis.close();
        fileOut.close();
        
        assertTrue("file exists",file.exists());
            
        FileInputStream in = new FileInputStream(file);
        wb = new HSSFWorkbook(in);
        HSSFName nm =wb.getNameAt(wb.getNameIndex("RangeTest"));
        assertTrue("Name is "+nm.getNameName(),"RangeTest".equals(nm.getNameName()));
        assertTrue("Reference is "+nm.getReference(),(wb.getSheetName(0)+"!$D$4:$E$8").equals(nm.getReference()));
        
        
    }
        
}

