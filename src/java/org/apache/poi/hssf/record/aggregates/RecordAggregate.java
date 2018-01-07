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

/**
 * <tt>RecordAggregate</tt>s are groups of of BIFF <tt>Record</tt>s that are typically stored
 * together and/or updated together.  Workbook / Sheet records are typically stored in a sequential
 * list, which does not provide much structure to coordinate updates.
 *
 * @author Josh Micich
 */
public abstract class RecordAggregate extends RecordBase {

	/**
	 * Visit each of the atomic BIFF records contained in this {@link RecordAggregate} in the order
	 * that they should be written to file.  Implementors may or may not return the actual
	 * {@link Record}s being used to manage POI's internal implementation.  Callers should not
	 * assume either way, and therefore only attempt to modify those {@link Record}s after cloning
	 */
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
	/**
	 * A wrapper for {@link RecordVisitor} which accumulates the sizes of all
	 * records visited.
	 */
	public static final class PositionTrackingVisitor implements RecordVisitor {
		private final RecordVisitor _rv;
		private int _position;

		public PositionTrackingVisitor(RecordVisitor rv, int initialPosition) {
			_rv = rv;
			_position = initialPosition;
		}
		public void visitRecord(Record r) {
			_position += r.getRecordSize();
			_rv.visitRecord(r);
		}
		public void setPosition(int position) {
			_position = position;
		}
		public int getPosition() {
			return _position;
		}
	}
}
