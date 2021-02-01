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

package org.apache.poi.openxml4j.opc.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Build an output stream for MemoryPackagePart.
 *
 * @author Julien Chable
 */
public final class MemoryPackagePartOutputStream extends OutputStream {

	private MemoryPackagePart _part;

	private ByteArrayOutputStream _buff;

	public MemoryPackagePartOutputStream(MemoryPackagePart part) {
		this._part = part;
		_buff = new ByteArrayOutputStream();
	}

	@Override
	public void write(int b) {
		_buff.write(b);
	}

	/**
	 * Close this stream and flush the content.
	 * @see #flush()
	 */
	@Override
	public void close() throws IOException {
		this.flush();
	}

	/**
	 * Flush this output stream. This method is called by the close() method.
	 * Warning : don't call this method for output consistency.
	 * @see #close()
	 */
	@Override
	public void flush() throws IOException {
		_buff.flush();
		if (_part.data != null) {
			byte[] newArray = new byte[_part.data.length + _buff.size()];
			// copy the previous contents of part.data in newArray
			System.arraycopy(_part.data, 0, newArray, 0, _part.data.length);

			// append the newly added data
			byte[] buffArr = _buff.toByteArray();
			System.arraycopy(buffArr, 0, newArray, _part.data.length,
					buffArr.length);

			// save the result as new data
			_part.data = newArray;
		} else {
			// was empty, just fill it
			_part.data = _buff.toByteArray();
		}

		/*
		 * Clear this streams buffer, in case flush() is called a second time
		 * Fix bug 1921637 - provided by Rainer Schwarze
		 */
		_buff.reset();
	}

	@Override
	public void write(byte[] b, int off, int len) {
		_buff.write(b, off, len);
	}

	@Override
	public void write(byte[] b) throws IOException {
		_buff.write(b);
	}
}
