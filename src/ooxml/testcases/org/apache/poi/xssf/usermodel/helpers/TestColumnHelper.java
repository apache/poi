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

package org.apache.poi.xssf.usermodel.helpers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.SharedStringSource;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCell;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCol;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCols;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorksheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STCellType;


public class TestColumnHelper extends TestCase {
	
	public void testGetColumnList() {
		CTWorksheet worksheet = CTWorksheet.Factory.newInstance();
		ColumnHelper columnHelper = new ColumnHelper(worksheet);
		
		CTCols cols1 = worksheet.addNewCols();
		CTCols cols2 = worksheet.addNewCols();
		
		CTCol col1_1 = cols1.addNewCol();
		col1_1.setMin(1);
		col1_1.setMax(10);
		col1_1.setWidth(13);
		CTCol col1_2 = cols1.addNewCol();
		col1_2.setMin(15);
		col1_2.setMax(15);
		col1_2.setWidth(14);

		CTCol col2_1 = cols2.addNewCol();
		col2_1.setMin(6);
		col2_1.setMax(10);
		CTCol col2_2 = cols2.addNewCol();
		col2_2.setMin(20);
		col2_2.setMax(20);
		
		columnHelper.setColumns(worksheet);
		List<CTCol> columns = columnHelper.getColumns();
		
		assertEquals(12, columns.size());
		assertEquals((double) 14, columnHelper.getColumn(15).getWidth());
	}
	
}
