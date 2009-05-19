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

package org.apache.poi.hssf.record.cont;

import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.util.DelayableLittleEndianOutput;
import org.apache.poi.util.LittleEndianByteArrayOutputStream;
import org.apache.poi.util.LittleEndianOutput;
/**
 * Allows the writing of BIFF records when the 'ushort size' header field is not known in advance.
 * When the client is finished writing data, it calls {@link #terminate()}, at which point this
 * class updates the 'ushort size' with its final value.
 *
 * @author Josh Micich
 */
final class UnknownLengthRecordOutput implements LittleEndianOutput {
	private static final int MAX_DATA_SIZE = RecordInputStream.MAX_RECORD_DATA_SIZE;

	private final LittleEndianOutput _originalOut;
	/** for writing the 'ushort size'  field once its value is known */
	private final LittleEndianOutput _dataSizeOutput;
	private final byte[] _byteBuffer;
	private LittleEndianOutput _out;
	private int _size;

	public UnknownLengthRecordOutput(LittleEndianOutput out, int sid) {
		_originalOut = out;
		out.writeShort(sid);
		if (out instanceof DelayableLittleEndianOutput) {
			// optimisation
			DelayableLittleEndianOutput dleo = (DelayableLittleEndianOutput) out;
			_dataSizeOutput = dleo.createDelayedOutput(2);
			_byteBuffer = null;
			_out = out;
		} else {
			// otherwise temporarily write all subsequent data to a buffer
			_dataSizeOutput = out;
			_byteBuffer = new byte[RecordInputStream.MAX_RECORD_DATA_SIZE];
			_out = new LittleEndianByteArrayOutputStream(_byteBuffer, 0);
		}
	}
	/**
	 * includes 4 byte header
	 */
	public int getTotalSize() {
		return 4 + _size;
	}
	public int getAvailableSpace() {
		if (_out == null) {
			throw new IllegalStateException("Record already terminated");
		}
		return MAX_DATA_SIZE - _size;
	}
	/**
	 * Finishes writing the current record and updates 'ushort size' field.<br/>
	 * After this method is called, only {@link #getTotalSize()} may be called.
	 */
	public void terminate() {
		if (_out == null) {
			throw new IllegalStateException("Record already terminated");
		}
		_dataSizeOutput.writeShort(_size);
		if (_byteBuffer != null) {
			_originalOut.write(_byteBuffer, 0, _size);
			_out = null;
			return;
		}
		_out = null;
	}

	public void write(byte[] b) {
		_out.write(b);
		_size += b.length;
	}
	public void write(byte[] b, int offset, int len) {
		_out.write(b, offset, len);
		_size += len;
	}
	public void writeByte(int v) {
		_out.writeByte(v);
		_size += 1;
	}
	public void writeDouble(double v) {
		_out.writeDouble(v);
		_size += 8;
	}
	public void writeInt(int v) {
		_out.writeInt(v);
		_size += 4;
	}
	public void writeLong(long v) {
		_out.writeLong(v);
		_size += 8;
	}
	public void writeShort(int v) {
		_out.writeShort(v);
		_size += 2;
	}
}
