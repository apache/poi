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

package org.apache.poi.ss.formula.eval;

import org.apache.poi.ss.formula.SheetRange;

/**
 * Common base class for implementors of {@link RefEval}
 */
public abstract class RefEvalBase implements RefEval {
    private final int _firstSheetIndex;
    private final int _lastSheetIndex;
	private final int _rowIndex;
	private final int _columnIndex;

    protected RefEvalBase(SheetRange sheetRange, int rowIndex, int columnIndex) {
        if (sheetRange == null) {
            throw new IllegalArgumentException("sheetRange must not be null");
        }
        _firstSheetIndex = sheetRange.getFirstSheetIndex();
        _lastSheetIndex = sheetRange.getLastSheetIndex();
        _rowIndex = rowIndex;
        _columnIndex = columnIndex;
    }
	protected RefEvalBase(int firstSheetIndex, int lastSheetIndex, int rowIndex, int columnIndex) {
	    _firstSheetIndex = firstSheetIndex;
	    _lastSheetIndex = lastSheetIndex;
		_rowIndex = rowIndex;
		_columnIndex = columnIndex;
	}
    protected RefEvalBase(int onlySheetIndex, int rowIndex, int columnIndex) {
        this(onlySheetIndex, onlySheetIndex, rowIndex, columnIndex);
    }
    
	public int getNumberOfSheets() {
	    return _lastSheetIndex-_firstSheetIndex+1;
    }
    public int getFirstSheetIndex() {
        return _firstSheetIndex;
    }
    public int getLastSheetIndex() {
        return _lastSheetIndex;
    }
    public final int getRow() {
		return _rowIndex;
	}
	public final int getColumn() {
		return _columnIndex;
	}
}
