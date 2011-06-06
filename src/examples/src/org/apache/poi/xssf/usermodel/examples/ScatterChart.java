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

package org.apache.poi.xssf.usermodel.examples;

import java.io.FileOutputStream;
import java.util.Date;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.*;
import org.apache.poi.ss.usermodel.charts.*;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.xssf.usermodel.charts.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook; 

/**
 * Illustrates how to create a simple scatter chart.
 */
public class ScatterChart {

	public static void main(String[]args) throws Exception {
		Workbook wb = new XSSFWorkbook();
		CreationHelper creationHelper = wb.getCreationHelper();
		Sheet sheet = wb.createSheet("Sheet 1");
		final int NUM_OF_ROWS = 3;
		final int NUM_OF_COLUMNS = 10;

		// Create a row and put some cells in it. Rows are 0 based.
		Row row;
		Cell cell;
		for (int rowIndex = 0; rowIndex < NUM_OF_ROWS; rowIndex++) {
			row = sheet.createRow((short)rowIndex);
			for (int colIndex = 0; colIndex < NUM_OF_COLUMNS; colIndex++) {
				cell = row.createCell((short)colIndex);
				cell.setCellValue(colIndex * (rowIndex + 1));
			}
		}

		Drawing drawing = sheet.createDrawingPatriarch();
		ClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 0, 5, 10, 15);

		Chart chart = drawing.createChart(anchor);
		ChartLegend legend = chart.getOrCreateLegend();
		legend.setPosition(LegendPosition.TOP_RIGHT);

		ScatterChartData data = chart.getChartDataFactory().createScatterChartData();

		ValueAxis bottomAxis = chart.getChartAxisFactory().createValueAxis(AxisPosition.BOTTOM);
		ValueAxis leftAxis = chart.getChartAxisFactory().createValueAxis(AxisPosition.LEFT);

		DataMarker xMarker = new DataMarker(sheet, new CellRangeAddress(0, 0, 0, NUM_OF_COLUMNS - 1));
		DataMarker y1Marker = new DataMarker(sheet, new CellRangeAddress(1, 1, 0, NUM_OF_COLUMNS - 1));
		DataMarker y2Marker = new DataMarker(sheet, new CellRangeAddress(2, 2, 0, NUM_OF_COLUMNS - 1));

		
		data.addSerie(xMarker, y1Marker);
		data.addSerie(xMarker, y2Marker);

		chart.plot(data, bottomAxis, leftAxis);

		// Write the output to a file
		FileOutputStream fileOut = new FileOutputStream("ooxml-scatter-chart.xlsx");
		wb.write(fileOut);
		fileOut.close();
	}
}
