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

abstract public class Chunk {
	protected int chunkId;
	protected int type;
	protected String namePrefix = "__substg1.0_";

	/**
	 * Gets the id of this chunk
	 */
	public int getChunkId() {
		return this.chunkId;
	}

	/**
	 * Gets the numeric type of this chunk.
	 */
	public int getType() {
		return this.type;
	}

	/**
	 * Creates a string to use to identify this chunk in the POI file system object.
	 */
	public String getEntryName() {
		String type = Integer.toHexString(this.type);
		while(type.length() < 4) type = "0" + type;

		String chunkId = Integer.toHexString(this.chunkId);
		while(chunkId.length() < 4) chunkId = "0" + chunkId;

		return this.namePrefix + chunkId.toUpperCase() + type.toUpperCase();
	}

	/**
	 * Gets a reference to a ByteArrayOutputStream that contains the value of this chunk.
	 */
	public abstract ByteArrayOutputStream getValueByteArray();

	/**
	 * Sets the value of this chunk using a OutputStream
	 * @param value
	 */
	public abstract void setValue(ByteArrayOutputStream value);
}
