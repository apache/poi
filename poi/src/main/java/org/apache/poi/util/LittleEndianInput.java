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

package org.apache.poi.util;

public interface LittleEndianInput {
	int available();
	byte readByte();
	int readUByte();
	short readShort();
	int readUShort();
	int readInt();
	long readLong();
	double readDouble();
	void readFully(byte[] buf);
	void readFully(byte[] buf, int off, int len);

	/**
	 * Usually acts the same as {@link #readFully(byte[], int, int)}, but
	 * for an encrypted stream the raw (unencrypted) data is filled 
	 *
	 * @param buf the byte array to receive the bytes
	 * @param off the start offset into the byte array 
	 * @param len the amount of bytes to fill
	 */
	void readPlain(byte[] buf, int off, int len);
}
