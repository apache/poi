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
 * Base class of pointers, which hold metadata and offsets about
 *  blocks elsewhere in the file
 */
public abstract class Pointer {
	protected int type;
	protected int address;
	protected int offset;
	protected int length;
	protected short format;

	public int getAddress() {
		return address;
	}
	public short getFormat() {
		return format;
	}
	public int getLength() {
		return length;
	}
	public int getOffset() {
		return offset;
	}
	public int getType() {
		return type;
	}

	public abstract int getSizeInBytes();
	public abstract boolean destinationHasStrings();
	public abstract boolean destinationHasPointers();
	public abstract boolean destinationHasChunks();
	public abstract boolean destinationCompressed();
}
