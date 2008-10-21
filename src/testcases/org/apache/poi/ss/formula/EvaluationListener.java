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
 * Tests should extend this class if they need to track the internal working of the {@link WorkbookEvaluator}.<br/>
 * 
 * Default method implementations all do nothing
 * 
 * @author Josh Micich
 */
public abstract class EvaluationListener implements IEvaluationListener {
	public void onCacheHit(int sheetIndex, int rowIndex, int columnIndex, ValueEval result) {
		// do nothing 
	}
	public void onReadPlainValue(int sheetIndex, int rowIndex, int columnIndex, ICacheEntry entry) {
		// do nothing 
	}
	public void onStartEvaluate(EvaluationCell cell, ICacheEntry entry, Ptg[] ptgs) {
		// do nothing 
	}
	public void onEndEvaluate(ICacheEntry entry, ValueEval result) {
		// do nothing 
	}
	public void onClearWholeCache() {
		// do nothing 
	}
	public void onClearCachedValue(ICacheEntry entry) {
		// do nothing 
	}
	public void onChangeFromBlankValue(int sheetIndex, int rowIndex, int columnIndex,
			EvaluationCell cell, ICacheEntry entry) {
		// do nothing 
	}
	public void sortDependentCachedValues(ICacheEntry[] entries) {
		// do nothing 
	}
	public void onClearDependentCachedValue(ICacheEntry entry, int depth) {
		// do nothing 
	}
}
