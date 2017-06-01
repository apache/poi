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

package org.apache.poi.xslf.usermodel.charts;

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.Beta;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTAxDataSource;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumData;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumDataSource;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumVal;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTStrData;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTStrVal;

/**
 * Base of all XSLF Chart Series
 */
@Beta
public abstract class XSLFChartSeries {
	protected XSSFSheet sheet;
	protected XSLFCategoryDataSource categoryData;
	protected XSLFNumericalDataSource<? extends Number> firstValues;

	protected abstract CTAxDataSource getAxDS();
	protected abstract CTNumDataSource getNumDS();

	protected XSLFChartSeries(XSSFSheet sheet) {
		this.sheet = sheet;
	}

	public void setCategoryData(XSLFCategoryDataSource category) {
		this.categoryData = category;
	}

	public void setFirstValues(XSLFNumericalDataSource<? extends Number> values) {
		this.firstValues = values;
	}

	protected String setSheetTitle(String title) {
		sheet.createRow(0).createCell(1).setCellValue(title);
		return new CellReference(sheet.getSheetName(), 0, 1, true, true).formatAsString();
	}

	public abstract void setTitle(String title);

	public abstract void setShowLeaderLines(boolean showLeaderLines);

	public abstract void setVaryColors(boolean varyColors);

	public void fillChartData() {
		if (categoryData == null || firstValues == null) {
			throw new IllegalStateException("Category and values must be defined before filling chart data.");
		}
		int numOfPoints = categoryData.getPointCount();
		if (numOfPoints != firstValues.getPointCount()) {
			throw new IllegalStateException("Category and values must have the same point count.");
		}
		fillAxDataSource(getAxDS(), numOfPoints);
		fillFirstValuesDataSource(getNumDS(), numOfPoints);
		fillSheet(numOfPoints);
	}

	private void fillAxDataSource(CTAxDataSource ds, int numOfPoints) {
		CTStrData cache = ds.getStrRef().getStrCache();
		cache.setPtArray(null);  // unset old axis text
        cache.getPtCount().setVal(numOfPoints);
        for (int i = 0; i < numOfPoints; ++i) {
            String value = categoryData.getPointAt(i);
            if (value != null) {
                CTStrVal ctStrVal = cache.addNewPt();
                ctStrVal.setIdx(i);
                ctStrVal.setV(value);
            }
        }
        String dataRange = new CellRangeAddress(1, numOfPoints, 0, 0).formatAsString(sheet.getSheetName(), true);
        ds.getStrRef().setF(dataRange);
	}

	private void fillFirstValuesDataSource(CTNumDataSource ds, int numOfPoints) {
		CTNumData cache = ds.getNumRef().getNumCache();
		String formatCode = firstValues.getFormatCode();
		if (formatCode == null) {
			cache.unsetFormatCode();
		} else {
			cache.setFormatCode(formatCode);
		}
        cache.setPtArray(null);  // unset old values
        cache.getPtCount().setVal(numOfPoints);
        for (int i = 0; i < numOfPoints; ++i) {
            Number value = firstValues.getPointAt(i);
            if (value != null) {
                CTNumVal ctNumVal = cache.addNewPt();
                ctNumVal.setIdx(i);
                ctNumVal.setV(value.toString());
            }
        }
        String dataRange = new CellRangeAddress(1, numOfPoints, 1, 1).formatAsString(sheet.getSheetName(), true);
        ds.getNumRef().setF(dataRange);
	}

	private void fillSheet(int numOfPoints) {
		for (int i = 0; i < numOfPoints; i++) {
			XSSFRow row = sheet.createRow(i + 1); // first row is for title
			row.createCell(0).setCellValue(categoryData.getPointAt(i));
			row.createCell(1).setCellValue(firstValues.getPointAt(i).doubleValue());
		}
	}
}
