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

   2012 - Alfresco Software, Ltd.
   Alfresco Software has modified source of this file
   The details of changes as svn diff can be found in svn at location root/projects/3rd-party/src 
==================================================================== */

package org.apache.poi.ss.usermodel;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.poi.ss.ITestDataProvider;
import org.junit.Test;

/**
 * Tests of {@link BorderStyle}
 */
public class BaseTestBorderStyle {
    
    private final ITestDataProvider _testDataProvider;

    protected BaseTestBorderStyle(ITestDataProvider testDataProvider) {
        _testDataProvider = testDataProvider;
    }

    /**
     * Test that we use the specified locale when deciding
     *   how to format normal numbers
     */
    @Test
    public void testBorderStyle() throws IOException {
        String ext = _testDataProvider.getStandardFileNameExtension();
        Workbook wb = _testDataProvider.openSampleWorkbook("59264."+ext);
        Sheet sh = wb.getSheetAt(0);
        
        assertBorderStyleEquals(BorderStyle.NONE, getDiagonalCell(sh, 0));
        assertBorderStyleEquals(BorderStyle.THIN, getDiagonalCell(sh, 1));
        assertBorderStyleEquals(BorderStyle.MEDIUM, getDiagonalCell(sh, 2));
        assertBorderStyleEquals(BorderStyle.DASHED, getDiagonalCell(sh, 3));
        assertBorderStyleEquals(BorderStyle.DOTTED, getDiagonalCell(sh, 4));
        assertBorderStyleEquals(BorderStyle.THICK, getDiagonalCell(sh, 5));
        assertBorderStyleEquals(BorderStyle.DOUBLE, getDiagonalCell(sh, 6));
        assertBorderStyleEquals(BorderStyle.HAIR, getDiagonalCell(sh, 7));
        assertBorderStyleEquals(BorderStyle.MEDIUM_DASHED, getDiagonalCell(sh, 8));
        assertBorderStyleEquals(BorderStyle.DASH_DOT, getDiagonalCell(sh, 9));
        assertBorderStyleEquals(BorderStyle.MEDIUM_DASH_DOT, getDiagonalCell(sh, 10));
        assertBorderStyleEquals(BorderStyle.DASH_DOT_DOT, getDiagonalCell(sh, 11));
        assertBorderStyleEquals(BorderStyle.MEDIUM_DASH_DOT_DOT, getDiagonalCell(sh, 12));
        assertBorderStyleEquals(BorderStyle.SLANTED_DASH_DOT, getDiagonalCell(sh, 13));
        
        wb.close();
    }
    
    private Cell getDiagonalCell(Sheet sheet, int n) {
        return sheet.getRow(n).getCell(n);
    }
    
    protected void assertBorderStyleEquals(BorderStyle expected, Cell cell) {
        CellStyle style = cell.getCellStyle();
        assertEquals(expected, style.getBorderTop());
        assertEquals(expected, style.getBorderBottom());
        assertEquals(expected, style.getBorderLeft());
        assertEquals(expected, style.getBorderRight());
    }

}
