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

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.aggregates.RecordAggregate.RecordVisitor;

/**
 * Test utility class to get {@link Record}s out of HSSF objects
 * 
 * @author Josh Micich
 */
public final class RecordInspector {

	private RecordInspector() {
		// no instances of this class
	}

	public static final class RecordCollector implements RecordVisitor {

		private List<Record> _list;

		public RecordCollector() {
			_list = new ArrayList<Record>(128);
		}

		public void visitRecord(Record r) {
			_list.add(r);
		}

		public Record[] getRecords() {
			Record[] result = new Record[_list.size()];
			_list.toArray(result);
			return result;
		}
	}

	/**
	 * @param streamOffset start position for serialization. This affects values in some
	 *         records such as INDEX, but most callers will be OK to pass zero.
	 * @return the {@link Record}s (in order) which will be output when the
	 *         specified sheet is serialized
	 */
	public static Record[] getRecords(HSSFSheet hSheet, int streamOffset) {
		RecordCollector rc = new RecordCollector();
		hSheet.getSheet().visitContainedRecords(rc, streamOffset);
		return rc.getRecords();
	}
}
