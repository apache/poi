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

import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.hssf.record.formula.eval.ValueEval;

/**
 * Tests can implement this class to track the internal working of the {@link WorkbookEvaluator}.<br/>
 * 
 * For POI internal testing use only
 * 
 * @author Josh Micich
 */
interface IEvaluationListener {

	void onCacheHit(int sheetIndex, int rowIndex, int columnIndex, ValueEval result);
	void onReadPlainValue(int sheetIndex, int srcRowNum, int srcColNum, ValueEval value);
	void onStartEvaluate(int sheetIndex, int rowIndex, int columnIndex, Ptg[] ptgs);
	void onEndEvaluate(int sheetIndex, int rowIndex, int columnIndex, ValueEval result);
	void onClearWholeCache();
	void onClearCachedValue(int sheetIndex, int rowIndex, int columnIndex, ValueEval value);
	void onClearDependentCachedValue(int sheetIndex, int rowIndex, int columnIndex, ValueEval value, int depth);
}
