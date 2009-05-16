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
package org.apache.poi.hsmf.datatypes;

import java.io.ByteArrayOutputStream;

/**
 * A Chunk made up of a ByteArrayOutputStream.
 */

public class ByteChunk extends Chunk {

	private ByteArrayOutputStream value;
	
	/**
	 * Creates a Byte Chunk, for either the old
	 *  or new style of string chunk types.
	 */
	public ByteChunk(int chunkId, boolean newStyleString) {
		this(chunkId, getStringType(newStyleString));
	}
	private static int getStringType(boolean newStyleString) {
		if(newStyleString)
			return Types.NEW_STRING;
		return Types.OLD_STRING;
	}
	
	/**
	 * Create a Byte Chunk, with the specified
	 *  type.
	 */
	public ByteChunk(int chunkId, int type) {
		this.chunkId = chunkId;
		this.type = type;
	}
	
	public ByteArrayOutputStream getValueByteArray() {
		return this.value;
	}

	public void setValue(ByteArrayOutputStream value) {
		this.value = value;
	}

	
}
