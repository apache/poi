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

package org.apache.poi.hssf.record.aggregates;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.model.RecordStream;
import org.apache.poi.hssf.record.CFHeaderRecord;
import org.apache.poi.hssf.record.formula.FormulaShifter;

/**
 * Holds all the conditional formatting for a workbook sheet.<p/>
 * 
 * See OOO exelfileformat.pdf sec 4.12 'Conditional Formatting Table'
 * 
 * @author Josh Micich
 */
public final class ConditionalFormattingTable extends RecordAggregate {

	private final List _cfHeaders;

	/**
	 * Creates an empty ConditionalFormattingTable
	 */
	public ConditionalFormattingTable() {
		_cfHeaders = new ArrayList();
	}

	public ConditionalFormattingTable(RecordStream rs) {

		List temp = new ArrayList();
		while (rs.peekNextClass() == CFHeaderRecord.class) {
			temp.add(CFRecordsAggregate.createCFAggregate(rs));
		}
		_cfHeaders = temp;
	}

	public void visitContainedRecords(RecordVisitor rv) {
		for (int i = 0; i < _cfHeaders.size(); i++) {
			CFRecordsAggregate subAgg = (CFRecordsAggregate) _cfHeaders.get(i);
			subAgg.visitContainedRecords(rv);
		}
	}

	/**
	 * @return index of the newly added CF header aggregate
	 */
	public int add(CFRecordsAggregate cfAggregate) {
		_cfHeaders.add(cfAggregate);
		return _cfHeaders.size() - 1;
	}

	public int size() {
		return _cfHeaders.size();
	}

	public CFRecordsAggregate get(int index) {
		checkIndex(index);
		return (CFRecordsAggregate) _cfHeaders.get(index);
	}

	public void remove(int index) {
		checkIndex(index);
		_cfHeaders.remove(index);
	}

	private void checkIndex(int index) {
		if (index < 0 || index >= _cfHeaders.size()) {
			throw new IllegalArgumentException("Specified CF index " + index
					+ " is outside the allowable range (0.." + (_cfHeaders.size() - 1) + ")");
		}
	}

	public void updateFormulasAfterCellShift(FormulaShifter shifter, int externSheetIndex) {
		for (int i = 0; i < _cfHeaders.size(); i++) {
			CFRecordsAggregate subAgg = (CFRecordsAggregate) _cfHeaders.get(i);
			boolean shouldKeep = subAgg.updateFormulasAfterCellShift(shifter, externSheetIndex);
			if (!shouldKeep) {
				_cfHeaders.remove(i);
				i--;
			}
		}
	}
}
