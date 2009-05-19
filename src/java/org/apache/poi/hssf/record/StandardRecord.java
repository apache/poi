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

package org.apache.poi.hssf.record;

import org.apache.poi.util.LittleEndianByteArrayOutputStream;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Subclasses of this class (the majority of BIFF records) are non-continuable.  This allows for
 * some simplification of serialization logic
 *
 * @author Josh Micich
 */
public abstract class StandardRecord extends Record {
	protected abstract int getDataSize();
	public final int getRecordSize() {
		return 4 + getDataSize();
	}
	@Override
	public final int serialize(int offset, byte[] data) {
		int dataSize = getDataSize();
		int recSize = 4 + dataSize;
		LittleEndianByteArrayOutputStream out = new LittleEndianByteArrayOutputStream(data, offset, recSize);
		out.writeShort(getSid());
		out.writeShort(dataSize);
		serialize(out);
		if (out.getWriteIndex() - offset != recSize) {
			throw new IllegalStateException("Error in serialization of (" + getClass().getName() + "): "
					+ "Incorrect number of bytes written - expected "
					+ recSize + " but got " + (out.getWriteIndex() - offset));
		}
		return recSize;
	}

	/**
	 * Write the data content of this BIFF record.  The 'ushort sid' and 'ushort size' header fields
	 * have already been written by the superclass.<br/>
	 *
	 * The subclass must write the exact number of bytes as reported by {@link org.apache.poi.hssf.record.Record#getRecordSize()}}
	 */
	protected abstract void serialize(LittleEndianOutput out);
}
