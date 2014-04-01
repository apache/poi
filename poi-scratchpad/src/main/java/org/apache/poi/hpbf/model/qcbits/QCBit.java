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

package org.apache.poi.hpbf.model.qcbits;

/**
 * Parent of all Quill CONTENTS bits
 */
public abstract class QCBit {
	protected String thingType;
	protected String bitType;
	protected byte[] data;

	protected int optA;
	protected int optB;
	protected int optC;

	protected int dataOffset;

	public QCBit(String thingType, String bitType, byte[] data) {
		this.thingType = thingType;
		this.bitType = bitType;
		this.data = data;
	}

	/**
	 * Returns the type of the thing, eg TEXT, FONT
	 *  or TOKN
	 */
	public String getThingType() { return thingType; }
	/**
	 * Returns the type of the bit data, eg TEXT
	 *  or PLC
	 */
	public String getBitType() { return bitType; }
	public byte[] getData() { return data; }

	public int getOptA() {
		return optA;
	}
	public void setOptA(int optA) {
		this.optA = optA;
	}

	public int getOptB() {
		return optB;
	}
	public void setOptB(int optB) {
		this.optB = optB;
	}

	public int getOptC() {
		return optC;
	}
	public void setOptC(int optC) {
		this.optC = optC;
	}

	public int getDataOffset() {
		return dataOffset;
	}
	public void setDataOffset(int offset) {
		this.dataOffset = offset;
	}

	public int getLength() {
		return data.length;
	}
}
