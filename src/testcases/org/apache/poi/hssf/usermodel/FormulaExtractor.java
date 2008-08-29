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

package org.apache.poi.hssf.usermodel;

import org.apache.poi.hssf.record.CellValueRecordInterface;
import org.apache.poi.hssf.record.aggregates.FormulaRecordAggregate;
import org.apache.poi.hssf.record.formula.Ptg;

/**
 * Test utility class to get <tt>Ptg</tt> arrays out of formula cells
 * 
 * @author Josh Micich
 */
public final class FormulaExtractor {

	private FormulaExtractor() {
		// no instances of this class
	}
	
	public static Ptg[] getPtgs(HSSFCell cell) {
		CellValueRecordInterface vr = cell.getCellValueRecord();
		if (!(vr instanceof FormulaRecordAggregate)) {
			throw new IllegalArgumentException("Not a formula cell");
		}
		FormulaRecordAggregate fra = (FormulaRecordAggregate) vr;
		return fra.getFormulaRecord().getParsedExpression();
	}
}
