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

package org.apache.poi.hssf.record;

import org.apache.poi.hssf.record.common.Ref8U;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Title: Feat (Feature) Record
 * <P>
 * This record specifies Shared Features data. It is normally paired
 *  up with a {@link FeatHdrRecord}.
 */
public final class FeatRecord extends StandardRecord  {
	public final static short sid = 0x0868;
	
	/**
	 * See SHAREDFEATURES_* on {@link FeatHdrRecord}
	 */
	private int isf_sharedFeatureType; 
	private byte reserved1; // Should always be zero
	private long reserved2; // Should always be zero
	/** The number of refs */
	private int cref;
	/** Only matters if type is ISFFEC2 */
	private long cbFeatData;
	private int reserved3; // Should always be zero
	private Ref8U[] cellRefs;

	private byte[] rgbFeat; 
	
	public FeatRecord() {
	}

	public short getSid() {
		return sid;
	}

	public FeatRecord(RecordInputStream in) {
		isf_sharedFeatureType = in.readShort();
		reserved1 = in.readByte();
		reserved2 = in.readLong();
		cref = in.readUShort();
		cbFeatData = in.readLong();
		reserved3 = in.readShort();

		cellRefs = new Ref8U[cref];
		for(int i=0; i<cellRefs.length; i++) {
			cellRefs[i] = new Ref8U(in);
		}
		
		rgbFeat = in.readRemainder();
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[SHARDED FEATURE]\n");
		
		// TODO ...
		
		buffer.append("[/SHARED FEATURE]\n");
		return buffer.toString();
	}

	public void serialize(LittleEndianOutput out) {
		out.writeShort(isf_sharedFeatureType);
		
		// TODO ...
		
	}

	protected int getDataSize() {
		return -1; // TODO
	}
}
