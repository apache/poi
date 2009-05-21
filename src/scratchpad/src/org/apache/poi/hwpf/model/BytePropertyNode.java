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

package org.apache.poi.hwpf.model;

/**
 * Normally PropertyNodes only ever work in characters, but
 *  a few cases actually store bytes, and this lets everything
 *  still work despite that.
 * It handles the conversion as required between bytes
 *  and characters.
 */
public abstract class BytePropertyNode extends PropertyNode {
	private boolean isUnicode;

	/**
	 * @param fcStart The start of the text for this property, in _bytes_
	 * @param fcEnd The end of the text for this property, in _bytes_
	 */
	public BytePropertyNode(int fcStart, int fcEnd, Object buf, boolean isUnicode) {
		super(
				generateCp(fcStart, isUnicode),
				generateCp(fcEnd, isUnicode),
				buf
		);
		this.isUnicode = isUnicode;
	}
	private static int generateCp(int val, boolean isUnicode) {
		if(isUnicode)
			return val/2;
		return val;
	}

	public boolean isUnicode() {
		return isUnicode;
	}
	public int getStartBytes() {
		if(isUnicode)
			return getStart()*2;
		return getStart();
	}
	public int getEndBytes() {
		if(isUnicode)
			return getEnd()*2;
		return getEnd();
	}
}
