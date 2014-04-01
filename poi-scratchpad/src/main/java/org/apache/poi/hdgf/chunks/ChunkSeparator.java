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
 * A separator between the trailer of one chunk, and the
 *  header of the next one
 */
public final class ChunkSeparator {
	protected byte[] separatorData;

	public ChunkSeparator(byte[] data, int offset) {
		separatorData = new byte[4];
		System.arraycopy(data, offset, separatorData, 0, 4);
	}

	public String toString() {
		return "<ChunkSeparator of length " + separatorData.length + ">";
	}
}
