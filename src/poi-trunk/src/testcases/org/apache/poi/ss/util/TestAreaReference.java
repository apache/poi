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
package org.apache.poi.ss.util;

import org.apache.poi.ss.SpreadsheetVersion;

import junit.framework.TestCase;

/**
 * Test for {@link AreaReference} handling of max rows.
 * 
 * @author David North
 */
public class TestAreaReference extends TestCase {
    
    public void testWholeColumn() {
        AreaReference oldStyle = AreaReference.getWholeColumn(SpreadsheetVersion.EXCEL97, "A", "B");
        assertEquals(0, oldStyle.getFirstCell().getCol());
        assertEquals(0, oldStyle.getFirstCell().getRow());
        assertEquals(1, oldStyle.getLastCell().getCol());
        assertEquals(SpreadsheetVersion.EXCEL97.getLastRowIndex(), oldStyle.getLastCell().getRow());
        assertTrue(oldStyle.isWholeColumnReference());

        AreaReference oldStyleNonWholeColumn = new AreaReference("A1:B23", SpreadsheetVersion.EXCEL97);
        assertFalse(oldStyleNonWholeColumn.isWholeColumnReference());

        AreaReference newStyle = AreaReference.getWholeColumn(SpreadsheetVersion.EXCEL2007, "A", "B");
        assertEquals(0, newStyle.getFirstCell().getCol());
        assertEquals(0, newStyle.getFirstCell().getRow());
        assertEquals(1, newStyle.getLastCell().getCol());
        assertEquals(SpreadsheetVersion.EXCEL2007.getLastRowIndex(), newStyle.getLastCell().getRow());
        assertTrue(newStyle.isWholeColumnReference());

        AreaReference newStyleNonWholeColumn = new AreaReference("A1:B23", SpreadsheetVersion.EXCEL2007);
        assertFalse(newStyleNonWholeColumn.isWholeColumnReference());
    }
    
    public void testWholeRow() {
        AreaReference oldStyle = AreaReference.getWholeRow(SpreadsheetVersion.EXCEL97, "1", "2");
        assertEquals(0, oldStyle.getFirstCell().getCol());
        assertEquals(0, oldStyle.getFirstCell().getRow());
        assertEquals(SpreadsheetVersion.EXCEL97.getLastColumnIndex(), oldStyle.getLastCell().getCol());
        assertEquals(1, oldStyle.getLastCell().getRow());
        
        AreaReference newStyle = AreaReference.getWholeRow(SpreadsheetVersion.EXCEL2007, "1", "2");
        assertEquals(0, newStyle.getFirstCell().getCol());
        assertEquals(0, newStyle.getFirstCell().getRow());
        assertEquals(SpreadsheetVersion.EXCEL2007.getLastColumnIndex(), newStyle.getLastCell().getCol());
        assertEquals(1, newStyle.getLastCell().getRow());
    }
}
