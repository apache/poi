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

package org.apache.poi.hssf.usermodel.examples;

import org.apache.poi.hssf.usermodel.*;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Demonstrates many features of the user API at once.  Used in the HOW-TO guide.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 * @author Andrew Oliver (acoliver at apache.org)
 */
public class BigExample
{
    public static void main(String[] args)
        throws IOException
    {
        short rownum;

        // create a new file
        FileOutputStream out = new FileOutputStream("workbook.xls");
        // create a new workbook
        HSSFWorkbook wb = new HSSFWorkbook();
        // create a new sheet
        HSSFSheet s = wb.createSheet();
        // declare a row object reference
        HSSFRow r = null;
        // declare a cell object reference
        HSSFCell c = null;
        // create 3 cell styles
        HSSFCellStyle cs = wb.createCellStyle();
        HSSFCellStyle cs2 = wb.createCellStyle();
        HSSFCellStyle cs3 = wb.createCellStyle();
        // create 2 fonts objects
        HSSFFont f = wb.createFont();
        HSSFFont f2 = wb.createFont();

        //set font 1 to 12 point type
        f.setFontHeightInPoints((short) 12);
        //make it red
        f.setColor((short) HSSFCellStyle.RED);
        // make it bold
        //arial is the default font
        f.setBoldweight(f.BOLDWEIGHT_BOLD);

        //set font 2 to 10 point type
        f2.setFontHeightInPoints((short) 10);
        //make it the color at palette index 0xf (white)
        f2.setColor((short) HSSFCellStyle.WHITE);
        //make it bold
        f2.setBoldweight(f2.BOLDWEIGHT_BOLD);

        //set cell stlye
        cs.setFont(f);
        //set the cell format see HSSFDataFromat for a full list
        cs.setDataFormat(HSSFDataFormat.getFormat("($#,##0_);[Red]($#,##0)"));

        //set a thin border
        cs2.setBorderBottom(cs2.BORDER_THIN);
        //fill w fg fill color
        cs2.setFillPattern((short) HSSFCellStyle.SOLID_FOREGROUND);
        // set foreground fill to red
        cs2.setFillForegroundColor((short) HSSFCellStyle.RED);

        // set the font
        cs2.setFont(f2);

        // set the sheet name to HSSF Test
        wb.setSheetName(0, "HSSF Test");
        // create a sheet with 300 rows (0-299)
        for (rownum = (short) 0; rownum < 300; rownum++)
        {
            // create a row
            r = s.createRow(rownum);
            // on every other row
            if ((rownum % 2) == 0)
            {
                // make the row height bigger  (in twips - 1/20 of a point)
                r.setHeight((short) 0x249);
            }

            //r.setRowNum(( short ) rownum);
            // create 50 cells (0-49) (the += 2 becomes apparent later
            for (short cellnum = (short) 0; cellnum < 50; cellnum += 2)
            {
                // create a numeric cell
                c = r.createCell(cellnum);
                // do some goofy math to demonstrate decimals
                c.setCellValue(rownum * 10000 + cellnum
                        + (((double) rownum / 1000)
                        + ((double) cellnum / 10000)));

                // on every other row
                if ((rownum % 2) == 0)
                {
                    // set this cell to the first cell style we defined
                    c.setCellStyle(cs);
                }

                // create a string cell (see why += 2 in the
                c = r.createCell((short) (cellnum + 1));

                // set the cell's string value to "TEST"
                c.setCellValue("TEST");
                // make this column a bit wider
                s.setColumnWidth((short) (cellnum + 1), (short) ((50 * 8) / ((double) 1 / 20)));

                // on every other row
                if ((rownum % 2) == 0)
                {
                    // set this to the white on red cell style
                    // we defined above
                    c.setCellStyle(cs2);
                }

            }
        }

        //draw a thick black border on the row at the bottom using BLANKS
        // advance 2 rows
        rownum++;
        rownum++;

        r = s.createRow(rownum);

        // define the third style to be the default
        // except with a thick black border at the bottom
        cs3.setBorderBottom(cs3.BORDER_THICK);

        //create 50 cells
        for (short cellnum = (short) 0; cellnum < 50; cellnum++)
        {
            //create a blank type cell (no value)
            c = r.createCell(cellnum);
            // set it to the thick black border style
            c.setCellStyle(cs3);
        }

        //end draw thick black border


        // demonstrate adding/naming and deleting a sheet
        // create a sheet, set its title then delete it
        s = wb.createSheet();
        wb.setSheetName(1, "DeletedSheet");
        wb.removeSheetAt(1);
        //end deleted sheet

        // write the workbook to the output stream
        // close our file (don't blow out our file handles
        wb.write(out);
        out.close();
    }
}
