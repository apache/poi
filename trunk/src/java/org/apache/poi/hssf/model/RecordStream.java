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

package org.apache.poi.hssf.model;

import java.util.List;

import org.apache.poi.hssf.record.Record;
/**
 * Simplifies iteration over a sequence of <tt>Record</tt> objects.
 */
public final class RecordStream {

	private final List<Record> _list;
	private int _nextIndex;
	private int _countRead;
	private final int _endIx;

	/**
	 * Creates a RecordStream bounded by startIndex and endIndex
	 * 
	 * @param inputList the list to iterate over
	 * @param startIndex the start index within the list
	 * @param endIx the end index within the list, which is the index of the end element + 1
	 */
	public RecordStream(List<Record> inputList, int startIndex, int endIx) {
		_list = inputList;
		_nextIndex = startIndex;
		_endIx = endIx;
		_countRead = 0;
	}

	public RecordStream(List<Record> records, int startIx) {
		this(records, startIx, records.size());
	}

	public boolean hasNext() {
		return _nextIndex < _endIx;
	}

	public Record getNext() {
		if(!hasNext()) {
			throw new RuntimeException("Attempt to read past end of record stream");
		}
		_countRead ++;
		return _list.get(_nextIndex++);
	}

	/**
	 * @return the {@link Class} of the next Record. <code>null</code> if this stream is exhausted.
	 */
	public Class<? extends Record> peekNextClass() {
		if(!hasNext()) {
			return null;
		}
		return _list.get(_nextIndex).getClass();
	}

	/**
	 * @return -1 if at end of records
	 */
	public int peekNextSid() {
		if(!hasNext()) {
			return -1;
		}
		return _list.get(_nextIndex).getSid();
	}

	public int getCountRead() {
		return _countRead;
	}
}
