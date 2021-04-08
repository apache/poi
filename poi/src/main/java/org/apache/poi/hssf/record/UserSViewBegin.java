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

import java.util.Arrays;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndianOutput;

/**
 * The UserSViewBegin record specifies settings for a custom view associated with the sheet.
 * This record also marks the start of custom view records, which save custom view settings.
 * Records between {@link UserSViewBegin} and {@link UserSViewEnd} contain settings for the custom view,
 * not settings for the sheet itself.
 */
public final class UserSViewBegin extends StandardRecord {

    public static final short sid = 0x01AA;

    private byte[] _rawData;

    public UserSViewBegin(UserSViewBegin other) {
        super(other);
        _rawData = (other._rawData == null) ? null : other._rawData.clone();
    }

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
        return Arrays.copyOf(_rawData, 16);
    }

    @Override
    public UserSViewBegin copy() {
        return new UserSViewBegin(this);
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.USER_SVIEW_BEGIN;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "guid", this::getGuid,
            "rawData", () -> _rawData
        );
    }
}
