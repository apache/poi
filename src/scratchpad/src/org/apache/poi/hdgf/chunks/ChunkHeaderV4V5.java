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

package org.apache.poi.hdgf.chunks;

/**
 * A chunk header from v4 or v5
 */
public final class ChunkHeaderV4V5 extends ChunkHeader {
	protected short unknown2;
	protected short unknown3;

	public short getUnknown2() {
		return unknown2;
	}
	public short getUnknown3() {
		return unknown3;
	}

	protected static int getHeaderSize() {
		return 12;
	}

	public int getSizeInBytes() {
		return getHeaderSize();
	}

	/**
	 * Does the chunk have a trailer?
	 */
	public boolean hasTrailer() {
		// V4 and V5 never has trailers
		return false;
	}

	/**
	 * Does the chunk have a separator?
	 */
	public boolean hasSeparator() {
		// V4 and V5 never has separators
		return false;
	}
}
