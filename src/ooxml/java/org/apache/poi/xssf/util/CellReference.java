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

package org.apache.poi.xssf.util;

public class CellReference {
	
	private int row;
	private short col;
	
	public CellReference() {
	}
	
	public CellReference(String cellRef) {
		if (cellRef == null || "".equals(cellRef)) {
			throw new IllegalArgumentException("Invalid Formula cell reference: '"+cellRef+"'");
		}
		String[] parts = getCellRefParts(cellRef);
        col = getColNumFromRef(parts[0]);
        row = getRowNumFromRef(parts[1]);
	}
	
	public CellReference(short col, int row) {
		this.col = col;
		this.row = row;
	}
	
	public int getRow() {
		return this.row;
	}
	
	public short getCol() {
		return this.col;
	}

	public String[] getCellRefParts(String cellRef) {
		StringBuffer sb0 = new StringBuffer("");
		StringBuffer sb1 = new StringBuffer("");
		String[] parts = new String[2]; 
		for (int i = 0 ; i < cellRef.length() ; i++) {
			char item = cellRef.charAt(i);
			if ((int)item >= 65 && (int)item <= 90) {
				sb0.append(item);
			}
			else {
				sb1.append(item);
			}
		}
		parts[0] = sb0.toString();
		parts[1] = sb1.toString();
		return parts;
	}

	public int getRowNumFromRef(String rowRef) {
		return (new Integer(rowRef).intValue()) - 1;
	}

	public short getColNumFromRef(String colRef) {
		double columnIndex = -1;
		for (int i = (colRef.length() - 1) ; i >= 0 ; i--) {
			char numericCharValue = colRef.charAt(colRef.length() - (i + 1));
			int convertedNumericCharValue = (numericCharValue - 64);
			double indexIncrement = (convertedNumericCharValue * Math.pow(26, i));
			columnIndex = columnIndex + indexIncrement;
		}
		return (short)columnIndex;
	}
	
	public String convertNumToColString(short col) {
		String colRef = "";
		double div = 1;
		double mod = 0;
		for (int i = 0 ; div >= 1 ; i ++) {
			mod = col % 26;
			div = col / 26;
			int AsciiIncrement = (i != 0 ? 64 : 65);
			char modToChar = (char)(mod + AsciiIncrement);
			colRef = modToChar + colRef;
			col = (short) ((col - mod) / 26);
		}
		return colRef;
	}
	
	public String convertRowColToString(short row, short col) {
		return convertNumToColString(col) + ((short) (row + 1));
	}

}
