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
 * A trailer that follows a chunk
 */
public final class ChunkTrailer {
	private byte[] trailerData;

	public ChunkTrailer(byte[] data, int offset) {
		trailerData = new byte[8];
		System.arraycopy(data, offset, trailerData, 0, 8);
	}

	public String toString() {
		return "<ChunkTrailer of length " + trailerData.length + ">";
	}

    byte[] getTrailerData() {
        return trailerData;
    }
}
