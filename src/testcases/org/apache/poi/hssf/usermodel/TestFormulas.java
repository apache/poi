
/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.poi.hssf.usermodel;

import junit.framework.TestCase;

import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.hssf.model.Sheet;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.BOFRecord;
import org.apache.poi.hssf.record.EOFRecord;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import java.util.List;
import java.util.Iterator;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * @author Andrew C. Oliver (acoliver at apache dot org)
 */

public class TestFormulas
extends TestCase {
    public TestFormulas(String s) {
        super(s);
    }
    
    /**
     * Add 1+1 -- WHoohoo!
     */
    
    public void testBasicAddIntegers()
    throws Exception {
        //System.out.println("Converted Text form is : "+fp.toFormulaString(fp.getRPNPtg()));
        
        short            rownum = 0;
        File file = File.createTempFile("testFormula",".xls");
        FileOutputStream out    = new FileOutputStream(file);
        HSSFWorkbook     wb     = new HSSFWorkbook();
        HSSFSheet        s      = wb.createSheet();
        HSSFRow          r      = null;
        HSSFCell         c      = null;
        
        //get our minimum values
        r = s.createRow((short)1);
        c = r.createCell((short)1);        
        c.setCellFormula(1 + "+" + 1);
        
        wb.write(out);
        out.close();        
        
        FileInputStream in = new FileInputStream(file);
        wb = new HSSFWorkbook(in);
        s  = wb.getSheetAt(0);
        r  = s.getRow((short)1);
        c  = r.getCell((short)1);
        
        assertTrue("Formula is as expected",("1+1".equals(c.getCellFormula())));
        in.close();
    }
    
    /**
     * Add various integers
     */
    
    public void testAddIntegers()
    throws Exception {
        binomialOperator("+");
    }
    
    /**
     * Multiply various integers
     */
    
    public void testMultplyIntegers()
    throws Exception {
        binomialOperator("*");
    }
    
    /**
     * Subtract various integers
     */    
    public void testSubtractIntegers()
    throws Exception {
        binomialOperator("-");
    }
    
    /**
     * Subtract various integers
     */    
    public void testDivideIntegers()
    throws Exception {
        binomialOperator("/");
    }
    
    /**
     * Exponentialize various integers;
     */
    public void testExponentIntegers() 
    throws Exception {
        binomialOperator("^");
    }
    
    
    
    private void binomialOperator(String operator) 
    throws Exception {
        short            rownum = 0;
        File file = File.createTempFile("testFormula",".xls");
        FileOutputStream out    = new FileOutputStream(file);
        HSSFWorkbook     wb     = new HSSFWorkbook();
        HSSFSheet        s      = wb.createSheet();
        HSSFRow          r      = null;
        HSSFCell         c      = null;
        
        //get our minimum values
        r = s.createRow((short)0);
        c = r.createCell((short)1);
        c.setCellFormula(1 + operator + 1);
        
        for (short x = 1; x < Short.MAX_VALUE && x > 0; x=(short)(x*2)) {
            r = s.createRow((short) x);
            //System.out.println("x="+x);
            for (short y = 1; y < 256 && y > 0; y++) {
                //System.out.println("y="+y);
                c = r.createCell((short) y);
                c.setCellFormula("" + x + operator + y);
                
            }
        }
        
        //make sure we do the maximum value of the Int operator
        if (s.getLastRowNum() < Short.MAX_VALUE) {
            r = s.createRow((short)0);
            c = r.createCell((short)0);
            c.setCellFormula("" + Short.MAX_VALUE + operator + Short.MAX_VALUE);
        }
        
        wb.write(out);
        out.close();
        assertTrue("file exists",file.exists());
        
        binomialVerify(operator,file);
    }
    
    private void binomialVerify(String operator, File file) 
    throws Exception {
        short            rownum = 0;
        
        FileInputStream  in     = new FileInputStream(file);
        HSSFWorkbook     wb     = new HSSFWorkbook(in);
        HSSFSheet        s      = wb.getSheetAt(0);
        HSSFRow          r      = null;
        HSSFCell         c      = null;
        
        //get our minimum values
        r = s.getRow((short)0);
        c = r.getCell((short)1);
        assertTrue("minval Formula is as expected",
                            ( ("1"+operator+"1").equals(c.getCellFormula())
                            ));
        
        for (short x = 1; x < Short.MAX_VALUE && x > 0; x=(short)(x*2)) {
            r = s.getRow((short) x);
            //System.out.println("x="+x);
            for (short y = 1; y < 256 && y > 0; y++) {
                //System.out.println("y="+y);
                c = r.getCell((short) y);

                assertTrue("loop Formula is as expected",(
                           (""+x+operator+y).equals(
                                                 c.getCellFormula()
                                                   )
                                                           ) 
                          );

                
            }
        }

        //test our maximum values
        r = s.getRow((short)0);
        c = r.getCell((short)0);
        assertTrue("maxval Formula is as expected",(
                (""+Short.MAX_VALUE+operator+Short.MAX_VALUE).equals(
                                                                c.getCellFormula()
                                                                  )
                                                   ) 
                  );
                        
        in.close();
        assertTrue("file exists",file.exists());               
    }

    
    
    public static void main(String [] args) {
        System.out
        .println("Testing org.apache.poi.hssf.usermodel.TestFormulas");
        junit.textui.TestRunner.run(TestFormulas.class);
    }
    
}
