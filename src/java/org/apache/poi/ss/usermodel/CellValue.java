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

package org.apache.poi.ss.usermodel;

import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.util.Removal;

/**
 * Mimics the 'data view' of a cell. This allows formula evaluator
 * to return a CellValue instead of precasting the value to String
 * or Number or boolean type.
 */
public final class CellValue {
	public static final CellValue TRUE = new CellValue(CellType.BOOLEAN, 0.0, true,  null, 0);
	public static final CellValue FALSE= new CellValue(CellType.BOOLEAN, 0.0, false, null, 0);

	private final CellType _cellType;
	private final double _numberValue;
	private final boolean _booleanValue;
	private final String _textValue;
	private final int _errorCode;

	private CellValue(CellType cellType, double numberValue, boolean booleanValue,
			String textValue, int errorCode) {
		_cellType = cellType;
		_numberValue = numberValue;
		_booleanValue = booleanValue;
		_textValue = textValue;
		_errorCode = errorCode;
	}


	public CellValue(double numberValue) {
		this(CellType.NUMERIC, numberValue, false, null, 0);
	}

	public static CellValue valueOf(boolean booleanValue) {
		return booleanValue ? TRUE : FALSE;
	}

	public CellValue(String stringValue) {
		this(CellType.STRING, 0.0, false, stringValue, 0);
	}

	public static CellValue getError(int errorCode) {
		return new CellValue(CellType.ERROR, 0.0, false, null, errorCode);
	}


	/**
	 * @return Returns the booleanValue.
	 */
	public boolean getBooleanValue() {
		return _booleanValue;
	}

	/**
	 * @return Returns the numberValue.
	 */
	public double getNumberValue() {
		return _numberValue;
	}

	/**
	 * @return Returns the stringValue.
	 */
	public String getStringValue() {
		return _textValue;
	}

    /**
     * Return the cell type.
     *
     * @return the cell type
     * @since POI 3.15
     * @deprecated use <code>getCellType</code> instead
     */
    @Deprecated
    @Removal(version="4.2")
    public CellType getCellTypeEnum() { return getCellType(); }

	/**
	 * Return the cell type.
	 *
	 * @return the cell type
	 */
	public CellType getCellType() {
		return _cellType;
	}

	/**
	 * @return Returns the errorValue.
	 */
	public byte getErrorValue() {
		return (byte) _errorCode;
	}

	public String toString() {
		return getClass().getName() + " [" +
				formatAsString() +
				"]";
	}

	public String formatAsString() {
		switch (_cellType) {
			case NUMERIC:
				return String.valueOf(_numberValue);
			case STRING:
				return '"' + _textValue + '"';
			case BOOLEAN:
				return _booleanValue ? "TRUE" : "FALSE";
			case ERROR:
				return ErrorEval.getText(_errorCode);
			default:
			return "<error unexpected cell type " + _cellType + ">";
		}
		
	}
}
