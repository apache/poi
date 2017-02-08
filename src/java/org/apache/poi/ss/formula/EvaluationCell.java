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

import org.apache.poi.ss.usermodel.CellType;

/**
 * Abstracts a cell for the purpose of formula evaluation.  This interface represents both formula
 * and non-formula cells.<br/>
 * 
 * For POI internal use only
 * 
 * @author Josh Micich
 */
public interface EvaluationCell {
	/**
	 * @return an Object that identifies the underlying cell,
	 * suitable for use as a key in a {@link java.util.HashMap}
	 */
	Object getIdentityKey();

	EvaluationSheet getSheet();
	int getRowIndex();
	int getColumnIndex();
	/**
	 * Will return {@link CellType} in a future version of POI.
	 * For forwards compatibility, do not hard-code cell type literals in your code.
	 *
	 * @return cell type
	 * @deprecated 3.15. Will return a {@link CellType} enum in the future.
	 */
	int getCellType();
	/**
	 * @since POI 3.15 beta 3
	 * @deprecated POI 3.15 beta 3.
	 * Will be deleted when we make the CellType enum transition. See bug 59791.
	 */
	CellType getCellTypeEnum();

	double getNumericCellValue();
	String getStringCellValue();
	boolean getBooleanCellValue();
	int getErrorCellValue();

	/**
	 * Will return {@link CellType} in a future version of POI.
	 * For forwards compatibility, do not hard-code cell type literals in your code.
	 *
	 * @return cell type of cached formula result
	 * @deprecated 3.15. Will return a {@link CellType} enum in the future.
	 */
	int getCachedFormulaResultType();
	/**
	 * @since POI 3.15 beta 3
	 * @deprecated POI 3.15 beta 3.
	 * Will be deleted when we make the CellType enum transition. See bug 59791.
	 */
	CellType getCachedFormulaResultTypeEnum();
}
