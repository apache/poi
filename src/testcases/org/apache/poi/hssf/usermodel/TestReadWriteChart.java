
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

import java.io.FileInputStream;
import java.io.FileOutputStream;

import java.util.List;
import java.util.Iterator;
import java.util.Date;
import java.util.GregorianCalendar;

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
        String          path      = System.getProperty("HSSF.testdata.path");
        String          filename  = path + "/SimpleChart.xls";
        POIFSFileSystem fs        =
            new POIFSFileSystem(new FileInputStream(filename));
        HSSFWorkbook    workbook  = new HSSFWorkbook(fs);
        HSSFSheet       sheet     = workbook.getSheetAt(0);
        HSSFRow         firstRow  = sheet.getRow(0);
        HSSFCell        firstCell = firstRow.getCell(( short ) 0);

        assertEquals(new GregorianCalendar(2000, 0, 1, 10, 51, 2).getTime(),
                     HSSFDateUtil
                         .getJavaDate(firstCell.getNumericCellValue()));
        HSSFRow  row  = sheet.createRow(( short ) 15);
        HSSFCell cell = row.createCell(( short ) 1,
                                       HSSFCell.CELL_TYPE_NUMERIC);

        cell.setCellValue(22);
        Sheet newSheet = workbook.getSheetAt(0).getSheet();
        List  records  = newSheet.getRecords();

        assertTrue(records.get(0) instanceof BOFRecord);
        assertTrue(records.get(records.size() - 1) instanceof EOFRecord);
    }
}
