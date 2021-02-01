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

package org.apache.poi.ss.formula;

import org.apache.poi.ss.formula.eval.AreaEval;
import org.apache.poi.ss.formula.eval.AreaEvalBase;
import org.apache.poi.ss.formula.eval.BlankEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.formula.ptg.AreaI;
import org.apache.poi.ss.formula.ptg.AreaI.OffsetArea;
import org.apache.poi.ss.util.CellReference;

/**
 * @author Robert Hulbert
 * Provides holding structure for temporary values in arrays during the evaluation process.
 * As such, Row/Column references do not actually correspond to data in the file.
 */

public final class CacheAreaEval extends AreaEvalBase {
    
    /* Value Containter */
    private final ValueEval[] _values;
    
    public CacheAreaEval(AreaI ptg, ValueEval[] values) {
        super(ptg);
        _values = values;
    }
    
    public CacheAreaEval(int firstRow, int firstColumn, int lastRow, int lastColumn, ValueEval[] values) {
        super(firstRow, firstColumn, lastRow, lastColumn);
        _values = values;
    }
    
    public ValueEval getRelativeValue(int relativeRowIndex, int relativeColumnIndex) {
        return getRelativeValue(-1, relativeRowIndex, relativeColumnIndex);
    }
    
    public ValueEval getRelativeValue(int sheetIndex, int relativeRowIndex, int relativeColumnIndex) {
        int oneDimensionalIndex = relativeRowIndex * getWidth() + relativeColumnIndex;
        return _values[oneDimensionalIndex];
    }

    public AreaEval offset(int relFirstRowIx, int relLastRowIx,
            int relFirstColIx, int relLastColIx) {
        
        AreaI area = new OffsetArea(getFirstRow(), getFirstColumn(),
                relFirstRowIx, relLastRowIx, relFirstColIx, relLastColIx);
        
        int height = area.getLastRow() - area.getFirstRow() + 1;
        int width = area.getLastColumn() - area.getFirstColumn() + 1;

        ValueEval[] newVals = new ValueEval[height * width];
        
        int startRow = area.getFirstRow() - getFirstRow();
        int startCol = area.getFirstColumn() - getFirstColumn();
        
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                ValueEval temp;
                
                /* CacheAreaEval is only temporary value representation, does not equal sheet selection
                 * so any attempts going beyond the selection results in BlankEval
                 */
                if (startRow + j > getLastRow() || startCol + i > getLastColumn()) {
                    temp = BlankEval.instance;
                }
                else {
                    temp = _values[(startRow + j) * getWidth() + (startCol + i)];
                }
                newVals[j * width + i] = temp;     
            }
        }

        return new CacheAreaEval(area, newVals);
    }

    public TwoDEval getRow(int rowIndex) {
        if (rowIndex >= getHeight()) {
            throw new IllegalArgumentException("Invalid rowIndex " + rowIndex
                    + ".  Allowable range is (0.." + getHeight() + ").");
        }
        int absRowIndex = getFirstRow() + rowIndex;
        ValueEval[] values = new ValueEval[getWidth()];
        
        for (int i = 0; i < values.length; i++) {
            values[i] = getRelativeValue(rowIndex, i);
        }
        return new CacheAreaEval(absRowIndex, getFirstColumn() , absRowIndex, getLastColumn(), values);
    }

    public TwoDEval getColumn(int columnIndex) {
        if (columnIndex >= getWidth()) {
            throw new IllegalArgumentException("Invalid columnIndex " + columnIndex
                    + ".  Allowable range is (0.." + getWidth() + ").");
        }
        int absColIndex = getFirstColumn() + columnIndex;
        ValueEval[] values = new ValueEval[getHeight()];
        
        for (int i = 0; i < values.length; i++) {
            values[i] = getRelativeValue(i, columnIndex);
        }
        
        return new CacheAreaEval(getFirstRow(), absColIndex, getLastRow(), absColIndex, values);
    }
    
    public String toString() {
        CellReference crA = new CellReference(getFirstRow(), getFirstColumn());
        CellReference crB = new CellReference(getLastRow(), getLastColumn());
        return getClass().getName() + "[" +
                crA.formatAsString() +
                ':' +
                crB.formatAsString() +
                "]";
    }
}
