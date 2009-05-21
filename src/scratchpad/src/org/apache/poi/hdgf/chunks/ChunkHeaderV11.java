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
 * A chunk header from v11+
 */
public final class ChunkHeaderV11 extends ChunkHeaderV6 {
	/**
	 * Does the chunk have a separator?
	 */
	public boolean hasSeparator() {
		// For some reason, there are two types that don't have a
		//  separator despite the flags that indicate they do
		if(type == 0x1f || type == 0xc9) { return false; }

		// If there's a trailer, there's a separator
		if(hasTrailer()) { return true; }

		if(unknown2 == 2 && unknown3 == 0x55) { return true; }
		if(unknown2 == 2 && unknown3 == 0x54 && type == 0xaa) { return true; }
		if(unknown2 == 3 && unknown3 != 0x50) { return true; }
		if(type == 0x69) { return true; }

		return false;
	}
}
