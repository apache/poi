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

import java.util.HashSet;
import java.util.Set;

/**
 * Stores details about the current evaluation of a cell.<br/>
 */
final class CellEvaluationFrame {

	private final CellLocation _cellLocation;
	private final Set _usedCells;

	public CellEvaluationFrame(CellLocation cellLoc) {
		_cellLocation = cellLoc;
		_usedCells = new HashSet();
	}
	public CellLocation getCoordinates() {
		return _cellLocation;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer(64);
		sb.append(getClass().getName()).append(" [");
		sb.append(_cellLocation.formatAsString());
		sb.append("]");
		return sb.toString();
	}
	public void addUsedCell(CellLocation coordinates) {
		_usedCells.add(coordinates);
	}
	/**
	 * @return never <code>null</code>, (possibly empty) array of all cells directly used while 
	 * evaluating the formula of this frame.  For non-formula cells this will always be an empty
	 * array;
	 */
	public CellLocation[] getUsedCells() {
		int nItems = _usedCells.size();
		if (nItems < 1) {
			return CellLocation.EMPTY_ARRAY;
		}
		CellLocation[] result = new CellLocation[nItems];
		_usedCells.toArray(result);
		return result;
	}
}