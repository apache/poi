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

import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.RecordBase;
import org.apache.poi.hssf.record.RecordInputStream;

/**
 * <tt>RecordAggregate</tt>s are groups of of BIFF <tt>Record</tt>s that are typically stored 
 * together and/or updated together.  Workbook / Sheet records are typically stored in a sequential
 * list, which does not provide much structure to coordinate updates.
 * 
 * @author Josh Micich
 */
public abstract class RecordAggregate extends RecordBase {
	// TODO - delete these methods when all subclasses have been converted
	protected final void validateSid(short id) {
		throw new RuntimeException("Should not be called");
	}
	protected final void fillFields(RecordInputStream in) {
		throw new RuntimeException("Should not be called");
	}
    public final short getSid() {
		throw new RuntimeException("Should not be called");
    }

	public abstract void visitContainedRecords(RecordVisitor rv);
	
	public final int serialize(int offset, byte[] data) {
		SerializingRecordVisitor srv = new SerializingRecordVisitor(data, offset);
		visitContainedRecords(srv);
		return srv.countBytesWritten();
	}
	public int getRecordSize() {
		RecordSizingVisitor rsv = new RecordSizingVisitor();
		visitContainedRecords(rsv);
		return rsv.getTotalSize();
	}
	
	public interface RecordVisitor {
		/**
		 * Implementors may call non-mutating methods on Record r.
		 * @param r must not be <code>null</code>
		 */
		void visitRecord(Record r);
	}
	
	private static final class SerializingRecordVisitor implements RecordVisitor {

		private final byte[] _data;
		private final int _startOffset;
		private int _countBytesWritten;

		public SerializingRecordVisitor(byte[] data, int startOffset) {
			_data = data;
			_startOffset = startOffset;
			_countBytesWritten = 0;
		}
		public int countBytesWritten() {
			return _countBytesWritten;
		}
		public void visitRecord(Record r) {
			int currentOffset = _startOffset + _countBytesWritten;
			_countBytesWritten += r.serialize(currentOffset, _data);
		}
	}
	private static final class RecordSizingVisitor implements RecordVisitor {

		private int _totalSize;
		
		public RecordSizingVisitor() {
			_totalSize = 0;
		}
		public int getTotalSize() {
			return _totalSize;
		}
		public void visitRecord(Record r) {
			_totalSize += r.getRecordSize();
		}
	}
}
