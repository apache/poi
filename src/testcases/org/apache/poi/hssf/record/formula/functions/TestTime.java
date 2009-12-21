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

package org.apache.poi.hssf.record.formula.functions;

import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFDataFormatter;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 * Tests for {@link TimeFunc}
 *
 * @author @author Steven Butler (sebutler @ gmail dot com)
 */
public final class TestTime extends TestCase {

	private static final int SECONDS_PER_MINUTE = 60;
	private static final int SECONDS_PER_HOUR = 60 * SECONDS_PER_MINUTE;
	private static final double SECONDS_PER_DAY = 24 * SECONDS_PER_HOUR;
	private HSSFCell cell11;
	private HSSFFormulaEvaluator evaluator;
	private HSSFWorkbook wb;
	private HSSFDataFormatter form;
	private HSSFCellStyle style;

	public void setUp() {
		wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("new sheet");
		style = wb.createCellStyle();
		HSSFDataFormat fmt = wb.createDataFormat();
		style.setDataFormat(fmt.getFormat("hh:mm:ss"));

		cell11 = sheet.createRow(0).createCell(0);
		form = new HSSFDataFormatter();

		evaluator = new HSSFFormulaEvaluator(wb);
	}

	public void testSomeArgumentsMissing() {
		confirm("00:00:00", "TIME(, 0, 0)");
		confirm("12:00:00", "TIME(12, , )");
	}

	public void testValid() {
		confirm("00:00:01", 0, 0, 1);
		confirm("00:01:00", 0, 1, 0);

		confirm("00:00:00", 0, 0, 0);

		confirm("01:00:00", 1, 0, 0);
		confirm("12:00:00", 12, 0, 0);
		confirm("23:00:00", 23, 0, 0);
		confirm("00:00:00", 24, 0, 0);
		confirm("01:00:00", 25, 0, 0);
		confirm("00:00:00", 48, 0, 0);
		confirm("06:00:00", 6, 0, 0);
		confirm("06:01:00", 6, 1, 0);
		confirm("06:30:00", 6, 30, 0);

		confirm("06:59:00", 6, 59, 0);
		confirm("07:00:00", 6, 60, 0);
		confirm("07:01:00", 6, 61, 0);
		confirm("08:00:00", 6, 120, 0);
		confirm("06:00:00", 6, 1440, 0);
		confirm("18:49:00", 18, 49, 0);
		confirm("18:49:01", 18, 49, 1);
		confirm("18:49:30", 18, 49, 30);
		confirm("18:49:59", 18, 49, 59);
		confirm("18:50:00", 18, 49, 60);
		confirm("18:50:01", 18, 49, 61);
		confirm("18:50:59", 18, 49, 119);
		confirm("18:51:00", 18, 49, 120);
		confirm("03:55:07", 18, 49, 32767);
		confirm("12:08:01", 18, 32767, 61);
		confirm("07:50:01", 32767, 49, 61);
	}
	private void confirm(String expectedTimeStr, int inH, int inM, int inS) {
		confirm(expectedTimeStr, "TIME(" + inH + "," + inM + "," + inS + ")");
	}

	private void confirm(String expectedTimeStr, String formulaText) {
//		System.out.println("=" + formulaText);
		String[] parts = Pattern.compile(":").split(expectedTimeStr);
		int expH = Integer.parseInt(parts[0]);
		int expM = Integer.parseInt(parts[1]);
		int expS = Integer.parseInt(parts[2]);

		double expectedValue = (expH*SECONDS_PER_HOUR + expM*SECONDS_PER_MINUTE + expS)/SECONDS_PER_DAY;

		cell11.setCellFormula(formulaText);
		cell11.setCellStyle(style);
		evaluator.clearAllCachedResultValues();

		double actualValue = evaluator.evaluate(cell11).getNumberValue();
		assertEquals(expectedValue, actualValue, 0.0);

		String actualText = form.formatCellValue(cell11, evaluator);
		assertEquals(expectedTimeStr, actualText);
	}
}
