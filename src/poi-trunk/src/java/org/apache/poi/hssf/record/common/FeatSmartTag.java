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

package org.apache.poi.hssf.record.common;

import org.apache.poi.hssf.record.FeatRecord;
//import org.apache.poi.hssf.record.Feat11Record;
//import org.apache.poi.hssf.record.Feat12Record;
import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Title: FeatSmartTag (Smart Tag Shared Feature) common record part
 * <P>
 * This record part specifies Smart Tag data for a sheet, stored as part
 *  of a Shared Feature. It can be found in records such as  {@link FeatRecord}.
 * It is made up of a hash, and a set of Factoid Data that makes up
 *  the smart tags.
 * For more details, see page 669 of the Excel binary file
 *  format documentation.
 */
public final class FeatSmartTag implements SharedFeature {
	// TODO - process
	private byte[] data;
	
	public FeatSmartTag() {
		data = new byte[0];
	}

	public FeatSmartTag(RecordInputStream in) {
		data = in.readRemainder();
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(" [FEATURE SMART TAGS]\n");
		buffer.append(" [/FEATURE SMART TAGS]\n");
		return buffer.toString();
	}

	public int getDataSize() {
		return data.length;
	}

	public void serialize(LittleEndianOutput out) {
		out.write(data);
	}
}
