/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.xssf.streaming;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

public final class TestOutlining {
    @Test
	void testSetRowGroupCollapsed() throws IOException {
		SXSSFWorkbook wb2 = new SXSSFWorkbook(100);
		wb2.setCompressTempFiles(true);
		SXSSFSheet sheet2 = wb2.createSheet("new sheet");

		int rowCount = 20;
		for (int i = 0; i < rowCount; i++) {
			sheet2.createRow(i);
		}

		sheet2.groupRow(4, 9);
		sheet2.groupRow(11, 19);

		sheet2.setRowGroupCollapsed(4, true);

		SXSSFRow r = sheet2.getRow(8);
		assertTrue(r.getHidden());
		r = sheet2.getRow(10);
		assertTrue(r.getCollapsed());
		r = sheet2.getRow(12);
		assertNull(r.getHidden());
		wb2.dispose();

		wb2.close();
	}

    @Test
    void testSetRowGroupCollapsedError() throws IOException {
		SXSSFWorkbook wb2 = new SXSSFWorkbook(100);
		wb2.setCompressTempFiles(true);
		SXSSFSheet sheet2 = wb2.createSheet("new sheet");

		int rowCount = 20;
		for (int i = 0; i < rowCount; i++) {
			sheet2.createRow(i);
		}

		sheet2.groupRow(4, 9);
		sheet2.groupRow(11, 19);

        IllegalArgumentException e;
		e = assertThrows(IllegalArgumentException.class, () -> sheet2.setRowGroupCollapsed(3, true));
        assertTrue(e.getMessage().contains("row (3)"));

        e = assertThrows(IllegalArgumentException.class, () -> sheet2.setRowGroupCollapsed(10, true));
        assertTrue(e.getMessage().contains("row (10)"));

        e = assertThrows(IllegalArgumentException.class, () -> sheet2.setRowGroupCollapsed(0, true));
        assertTrue(e.getMessage().contains("row (0)"));

        e = assertThrows(IllegalArgumentException.class, () -> sheet2.setRowGroupCollapsed(20, true));
        assertTrue(e.getMessage().contains("Row does not exist"), "Had: " + e.getMessage());

		SXSSFRow r = sheet2.getRow(8);
		assertNotNull(r);
		assertNull(r.getHidden());
		r = sheet2.getRow(10);
		assertNull(r.getCollapsed());
		r = sheet2.getRow(12);
		assertNull(r.getHidden());
		wb2.dispose();

		wb2.close();
	}

    @Test
    void testOutlineGettersHSSF() throws IOException {
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
        HSSFSheet hssfSheet = hssfWorkbook.createSheet();
        hssfSheet.createRow(0);
        hssfSheet.createRow(1);
        hssfSheet.createRow(2);
        hssfSheet.createRow(3);
        hssfSheet.createRow(4);
        hssfSheet.groupRow(1, 3);
        hssfSheet.groupRow(2, 3);

        assertEquals(0, hssfSheet.getRow(0).getOutlineLevel());
        assertEquals(1, hssfSheet.getRow(1).getOutlineLevel());
        assertEquals(2, hssfSheet.getRow(2).getOutlineLevel());
        assertEquals(2, hssfSheet.getRow(3).getOutlineLevel());
        assertEquals(0, hssfSheet.getRow(4).getOutlineLevel());
        hssfWorkbook.close();
    }

    @Test
    void testOutlineGettersXSSF() throws IOException {
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook();
        XSSFSheet xssfSheet = xssfWorkbook.createSheet();
        xssfSheet.createRow(0);
        xssfSheet.createRow(1);
        xssfSheet.createRow(2);
        xssfSheet.createRow(3);
        xssfSheet.createRow(4);
        xssfSheet.groupRow(1, 3);
        xssfSheet.groupRow(2, 3);

        assertEquals(0, xssfSheet.getRow(0).getOutlineLevel());
        assertEquals(1, xssfSheet.getRow(1).getOutlineLevel());
        assertEquals(2, xssfSheet.getRow(2).getOutlineLevel());
        assertEquals(2, xssfSheet.getRow(3).getOutlineLevel());
        assertEquals(0, xssfSheet.getRow(4).getOutlineLevel());
        xssfWorkbook.close();
    }

    @Test
    void testOutlineGettersSXSSF() throws IOException {
        SXSSFWorkbook sxssfWorkbook = new SXSSFWorkbook();
        SXSSFSheet sxssfSheet = sxssfWorkbook.createSheet();
        sxssfSheet.createRow(0);
        sxssfSheet.createRow(1);
        sxssfSheet.createRow(2);
        sxssfSheet.createRow(3);
        sxssfSheet.createRow(4);
        sxssfSheet.createRow(5);

        // nothing happens with empty row-area
        sxssfSheet.groupRow(1, 0);
        assertEquals(0, sxssfSheet.getRow(0).getOutlineLevel());
        assertEquals(0, sxssfSheet.getRow(1).getOutlineLevel());
        assertEquals(0, sxssfSheet.getRow(2).getOutlineLevel());
        assertEquals(0, sxssfSheet.getRow(3).getOutlineLevel());
        assertEquals(0, sxssfSheet.getRow(4).getOutlineLevel());
        assertEquals(0, sxssfSheet.getRow(5).getOutlineLevel());

        sxssfSheet.groupRow(1, 3);
        sxssfSheet.groupRow(2, 3);

        assertEquals(0, sxssfSheet.getRow(0).getOutlineLevel());
        assertEquals(1, sxssfSheet.getRow(1).getOutlineLevel());
        assertEquals(2, sxssfSheet.getRow(2).getOutlineLevel());
        assertEquals(2, sxssfSheet.getRow(3).getOutlineLevel());
        assertEquals(0, sxssfSheet.getRow(4).getOutlineLevel());
        assertEquals(0, sxssfSheet.getRow(5).getOutlineLevel());

        // add tests for direct setting - add row 4 to deepest group
        sxssfSheet.setRowOutlineLevel(4, 2);
        assertEquals(0, sxssfSheet.getRow(0).getOutlineLevel());
        assertEquals(1, sxssfSheet.getRow(1).getOutlineLevel());
        assertEquals(2, sxssfSheet.getRow(2).getOutlineLevel());
        assertEquals(2, sxssfSheet.getRow(3).getOutlineLevel());
        assertEquals(2, sxssfSheet.getRow(4).getOutlineLevel());
        assertEquals(0, sxssfSheet.getRow(5).getOutlineLevel());

        sxssfWorkbook.dispose();
        sxssfWorkbook.close();
    }

    @Test
    void testOutlineGettersSXSSFSetOutlineLevel() throws IOException {
        SXSSFWorkbook sxssfWorkbook = new SXSSFWorkbook();
        SXSSFSheet sxssfSheet = sxssfWorkbook.createSheet();
        sxssfSheet.createRow(0);
        sxssfSheet.createRow(1);
        sxssfSheet.createRow(2);
        sxssfSheet.createRow(3);
        sxssfSheet.createRow(4);
        sxssfSheet.createRow(5);

        // what happens with level below 1
        sxssfSheet.setRowOutlineLevel(0, -2);
        assertEquals(-2, sxssfSheet.getRow(0).getOutlineLevel());
        assertEquals(0, sxssfSheet.getRow(1).getOutlineLevel());
        assertEquals(0, sxssfSheet.getRow(2).getOutlineLevel());
        assertEquals(0, sxssfSheet.getRow(3).getOutlineLevel());
        assertEquals(0, sxssfSheet.getRow(4).getOutlineLevel());
        assertEquals(0, sxssfSheet.getRow(5).getOutlineLevel());

        // add tests for direct setting
        sxssfSheet.setRowOutlineLevel(4, 2);
        assertEquals(-2, sxssfSheet.getRow(0).getOutlineLevel());
        assertEquals(0, sxssfSheet.getRow(1).getOutlineLevel());
        assertEquals(0, sxssfSheet.getRow(2).getOutlineLevel());
        assertEquals(0, sxssfSheet.getRow(3).getOutlineLevel());
        assertEquals(2, sxssfSheet.getRow(4).getOutlineLevel());
        assertEquals(0, sxssfSheet.getRow(5).getOutlineLevel());

        sxssfWorkbook.dispose();
        sxssfWorkbook.close();
    }
}
