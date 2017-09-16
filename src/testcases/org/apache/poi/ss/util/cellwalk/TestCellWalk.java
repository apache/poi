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
package org.apache.poi.ss.util.cellwalk;

import junit.framework.TestCase;

import java.util.Date;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.SheetBuilder;

public class TestCellWalk extends TestCase {

    private static Object[][] testData = new Object[][] {
	{   1,          2,  null},
	{null, new Date(),  null},
	{null,       null, "str"}
    };

    private final CountCellHandler countCellHandler = new CountCellHandler();

    public void testNotTraverseEmptyCells() {
	Workbook wb = new HSSFWorkbook();
	Sheet sheet = new SheetBuilder(wb, testData).build();
	CellRangeAddress range = CellRangeAddress.valueOf("A1:C3");
	
	CellWalk cellWalk = new CellWalk(sheet, range);
	countCellHandler.reset();
	cellWalk.traverse(countCellHandler);

	assertEquals(4, countCellHandler.getVisitedCellsNumber());
	/* 1 + 2 + 5 + 9 */
	assertEquals(17L, countCellHandler.getOrdinalNumberSum());
    }


    private static class CountCellHandler implements CellHandler {

	private int cellsVisited;
	private long ordinalNumberSum;

	@Override
    public void onCell(Cell cell, CellWalkContext ctx) {
	    ++cellsVisited;
	    ordinalNumberSum += ctx.getOrdinalNumber();
	}

	public int getVisitedCellsNumber() {
	    return cellsVisited;
	}

	public long getOrdinalNumberSum() {
	    return ordinalNumberSum;
	}

	public void reset() {
	    cellsVisited = 0;
	    ordinalNumberSum = 0L;
	}
    }
}