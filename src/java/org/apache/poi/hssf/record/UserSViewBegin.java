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

import org.apache.poi.hssf.record.aggregates.PageSettingsBlock;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.LittleEndianOutput;

import java.util.Arrays;

/**
 * The UserSViewBegin record specifies settings for a custom view associated with the sheet.
 * This record also marks the start of custom view records, which save custom view settings.
 * Records between {@link UserSViewBegin} and {@link UserSViewEnd} contain settings for the custom view,
 * not settings for the sheet itself.
 *
 * @author Yegor Kozlov
 */
public final class UserSViewBegin extends StandardRecord {

    public final static short sid = 0x01AA;
	private byte[] _rawData;

    public UserSViewBegin(byte[] data) {
        _rawData = data;
    }

	/**
	 * construct an UserSViewBegin record.  No fields are interpreted and the record will
	 * be serialized in its original form more or less
	 * @param in the RecordInputstream to read the record from
	 */
	public UserSViewBegin(RecordInputStream in) {
		_rawData = in.readRemainder();
	}

	/**
	 * spit the record out AS IS. no interpretation or identification
	 */
	public void serialize(LittleEndianOutput out) {
		out.write(_rawData);
	}

	protected int getDataSize() {
		return _rawData.length;
	}

    public short getSid()
    {
        return sid;
    }

    /**
     * @return Globally unique identifier for the custom view
     */
    public byte[] getGuid(){
        byte[] guid = new byte[16];
        System.arraycopy(_rawData, 0, guid, 0, guid.length);
        return guid;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append("[").append("USERSVIEWBEGIN").append("] (0x");
        sb.append(Integer.toHexString(sid).toUpperCase() + ")\n");
        sb.append("  rawData=").append(HexDump.toHex(_rawData)).append("\n");
        sb.append("[/").append("USERSVIEWBEGIN").append("]\n");
        return sb.toString();
    }

    //HACK: do a "cheat" clone, see Record.java for more information
    public Object clone() {
        return cloneViaReserialise();
    }
 
}