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

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.hssf.record.FormulaRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.RecordFormatException;
import org.apache.poi.hssf.record.StringRecord;
import org.apache.poi.hssf.usermodel.RecordInspector.RecordCollector;

/**
 *
 * @author avik
 */
public final class TestFormulaRecordAggregate extends TestCase {

	public void testBasic() {
		FormulaRecord f = new FormulaRecord();
		f.setCachedResultTypeString();
		StringRecord s = new StringRecord();
		s.setString("abc");
		FormulaRecordAggregate fagg = new FormulaRecordAggregate(f, s, SharedValueManager.EMPTY);
		assertEquals("abc", fagg.getStringValue());
	}

	/**
	 * Sometimes a {@link StringRecord} appears after a {@link FormulaRecord} even though the
	 * formula has evaluated to a text value.  This might be more likely to occur when the formula
	 * <i>can</i> evaluate to a text value.<br/>
	 * Bug 46213 attachment 22874 has such an extra {@link StringRecord} at stream offset 0x5765.
	 * This file seems to open in Excel (2007) with no trouble.  When it is re-saved, Excel omits
	 * the extra record.  POI should do the same.
	 */
	public void testExtraStringRecord_bug46213() {
		FormulaRecord fr = new FormulaRecord();
		fr.setValue(2.0);
		StringRecord sr = new StringRecord();
		sr.setString("NA");
		SharedValueManager svm = SharedValueManager.EMPTY;
		FormulaRecordAggregate fra;

		try {
			fra = new FormulaRecordAggregate(fr, sr, svm);
		} catch (RecordFormatException e) {
			if ("String record was  supplied but formula record flag is not  set".equals(e.getMessage())) {
				throw new AssertionFailedError("Identified bug 46213");
			}
			throw e;
		}
		RecordCollector rc = new RecordCollector();
		fra.visitContainedRecords(rc);
		Record[] vraRecs = rc.getRecords();
		assertEquals(1, vraRecs.length);
		assertEquals(fr, vraRecs[0]);
	}
}
