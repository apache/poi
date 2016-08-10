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

package org.apache.poi.hdgf.streams;

/**
 * Holds the representation of the stream on-disk, and
 *  handles de-compressing it as required.
 * In future, may also handle writing it back out again
 */
public class StreamStore { // TODO - instantiable superclass
	private byte[] contents;

	/**
	 * Creates a new, non compressed Stream Store
	 */
	protected StreamStore(byte[] data, int offset, int length) {
		contents = new byte[length];
		System.arraycopy(data, offset, contents, 0, length);
	}

	protected void prependContentsWith(byte[] b) {
		byte[] newContents = new byte[contents.length + b.length];
		System.arraycopy(b, 0, newContents, 0, b.length);
		System.arraycopy(contents, 0, newContents, b.length, contents.length);
		contents = newContents;
	}
	protected void copyBlockHeaderToContents() {}

	protected byte[] getContents() { return contents; }
	public byte[] _getContents() { return contents; }
}
