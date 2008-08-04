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
 *
 * @author Josh Micich
 */
public final class RecordStream {

	private final List _list;
	private int _nextIndex;
	private int _countRead;

	public RecordStream(List inputList, int startIndex) {
		_list = inputList;
		_nextIndex = startIndex;
		_countRead = 0;
	}

	public boolean hasNext() {
		return _nextIndex < _list.size();
	}

	public Record getNext() {
		if(_nextIndex >= _list.size()) {
			throw new RuntimeException("Attempt to read past end of record stream");
		}
		_countRead ++;
		return (Record) _list.get(_nextIndex++);
	}

	/**
	 * @return the {@link Class} of the next Record. <code>null</code> if this stream is exhausted.
	 */
	public Class peekNextClass() {
		if(_nextIndex >= _list.size()) {
			return null;
		}
		return _list.get(_nextIndex).getClass();
	}

	public int getCountRead() {
		return _countRead;
	}
}
