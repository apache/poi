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

package org.apache.poi.xssf.usermodel.charts;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.apache.poi.ss.util.DataMarker;
import org.apache.poi.ss.util.cellwalk.CellWalk;
import org.apache.poi.ss.util.cellwalk.CellHandler;
import org.apache.poi.ss.util.cellwalk.CellWalkContext;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumRef;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumData;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumVal;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTUnsignedInt;

/**
 * Package private class to fill chart's number reference with cached
 * numeric values. If a formula-typed cell referenced by data marker,
 * cell's value will be calculated and placed to cache. Numeric cells
 * will be placed to cache as is. Non-numeric cells will be ignored.
 *
 * @author Roman Kashitsyn
 */
class XSSFNumberCache {

    private CTNumData ctNumData;

    XSSFNumberCache(CTNumData ctNumData) {
	this.ctNumData = ctNumData;
    }

    /**
     * Builds new numeric cache container.
     * @param marker data marker to use for cache evaluation
     * @param ctNumRef parent number reference
     * @return numeric cache instance
     */
    static XSSFNumberCache buildCache(DataMarker marker, CTNumRef ctNumRef) {
	CellRangeAddress range = marker.getRange();
	int numOfPoints = range.getNumberOfCells();

	if (numOfPoints == 0) {
	    // Nothing to do.
	    return null;
	}

	XSSFNumberCache cache = new XSSFNumberCache(ctNumRef.addNewNumCache());
	cache.setPointCount(numOfPoints);

	Workbook wb = marker.getSheet().getWorkbook();
	FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();

	CellWalk cellWalk = new CellWalk(marker);
	NumCacheCellHandler numCacheHandler = cache.new NumCacheCellHandler(evaluator);
	cellWalk.traverse(numCacheHandler);
	return cache;
    }
    
    /**
     * Returns total count of points in cache. Some (or even all) of
     * them might be empty.
     * @return total count of points in cache
     */
    long getPointCount() {
	CTUnsignedInt pointCount = ctNumData.getPtCount();
	if (pointCount != null) {
	    return pointCount.getVal();
	} else {
	    return 0L;
	}
    }

    /**
     * Returns cache value at specified index.
     * @param index index of the point in cache
     * @return point value
     */
    double getValueAt(int index) {
	/* TODO: consider more effective algorithm. Left as is since
	 * this method should be invoked mostly in tests. */
	for (CTNumVal pt : ctNumData.getPtList()) {
	    if (pt.getIdx() == index) {
		return Double.valueOf(pt.getV()).doubleValue();
	    }
	}
	return 0.0;
    }

    private void setPointCount(int numOfPoints) {
	ctNumData.addNewPtCount().setVal(numOfPoints);
    }

    private class NumCacheCellHandler implements CellHandler {

	private FormulaEvaluator evaluator;

	public NumCacheCellHandler(FormulaEvaluator evaluator) {
	    this.evaluator = evaluator;
	}

	public void onCell(Cell cell, CellWalkContext ctx) {
	    double pointValue = getOrEvalCellValue(cell);
	    /* Silently ignore non-numeric values.
	     * This is Office default behaviour. */
	    if (Double.isNaN(pointValue)) {
		return;
	    }

	    CTNumVal point = ctNumData.addNewPt();
	    point.setIdx(ctx.getOrdinalNumber());
	    point.setV(NumberToTextConverter.toText(pointValue));
	}

	private double getOrEvalCellValue(Cell cell) {
	    int cellType = cell.getCellType();

	    if (cellType == Cell.CELL_TYPE_NUMERIC) {
		return cell.getNumericCellValue();
	    } else if (cellType == Cell.CELL_TYPE_FORMULA) {
		CellValue value = evaluator.evaluate(cell);
		if (value.getCellType() == Cell.CELL_TYPE_NUMERIC) {
		    return value.getNumberValue();
		}
	    }
	    return Double.NaN;
	}

    }
}
