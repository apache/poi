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

package org.apache.poi.hdgf.pointers;

/**
 * A Pointer from v6+
 */
public final class PointerV6 extends Pointer {
	public boolean destinationHasStrings() {
		return (0x40 <= format && format < 0x50);
	}
	public boolean destinationHasPointers() {
		if(type == 20) return true;
		if(format == 0x1d || format == 0x1e) return true;
		return (0x50 <= format && format < 0x60);
	}
	public boolean destinationHasChunks() {
		return (0xd0 <= format && format < 0xdf);
	}

	public boolean destinationCompressed() {
		// Apparently, it's the second least significant bit
		return (format & 2) > 0;
	}

	/**
	 * With v6 pointers, the on-disk size is 18 bytes
	 */
	public int getSizeInBytes() { return 18; }
}
