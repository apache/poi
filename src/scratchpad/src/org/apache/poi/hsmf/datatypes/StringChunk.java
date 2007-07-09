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
 * A Chunk made up of a single string.
 * @author Travis Ferguson
 */
public class StringChunk extends Chunk {

	private String value;

	public StringChunk(int chunkId) {
		this.chunkId = chunkId;
		this.type = Types.STRING;
	}
	
	/* (non-Javadoc)
	 * @see org.apache.poi.hsmf.Chunk.Chunk#getValueByteArray()
	 */
	public ByteArrayOutputStream getValueByteArray() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.poi.hsmf.Chunk.Chunk#setValue(java.io.ByteArrayOutputStream)
	 */
	public void setValue(ByteArrayOutputStream value) {
		this.value = value.toString().replaceAll("\0", "");
	}

	public String toString() {
		return this.value;
	}
}
