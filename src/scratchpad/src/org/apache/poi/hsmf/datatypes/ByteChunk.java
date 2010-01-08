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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.poi.util.IOUtils;

/**
 * A Chunk that holds binary data, normally
 *  unparsed.
 * Generally as we know how to make sense of the
 *  contents, we create a new Chunk class and add
 *  a special case in the parser for them.
 */

public class ByteChunk extends Chunk {
	private byte[] value;
	
	/**
	 * Creates a Byte Chunk.
	 */
	public ByteChunk(String entryName) {
		super(entryName);
	}
	
	/**
	 * Create a Byte Chunk, with the specified
	 *  type.
	 */
	public ByteChunk(int chunkId, int type) {
	   super(chunkId, type);
	}

   public void readValue(InputStream value) throws IOException {
      this.value = IOUtils.toByteArray(value);
   }

   public void writeValue(OutputStream out) throws IOException {
      out.write(value);
   }

   public byte[] getValue() {
      return value;
   }
   public void setValue(byte[] value) {
      this.value = value;
   }
}
