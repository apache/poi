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

/**
 * A Chunk which holds fixed-length properties, and pointer
 *  to the variable length ones (which get their own chunk)
 */
public class PropertiesChunk extends Chunk {
   public static final String PREFIX = "__properties_version1.0";

	/**
	 * Creates a Properties Chunk.
	 */
	public PropertiesChunk() {
		super(PREFIX, -1, Types.UNKNOWN);
	}

	@Override
   public String getEntryName() {
	   return PREFIX;
   }

   public void readValue(InputStream value) throws IOException {
      // TODO
	}
	
	public void writeValue(OutputStream out) throws IOException {
	   // TODO
	}
}
