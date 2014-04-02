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

import org.apache.poi.hssf.record.cont.ContinuableRecord;
import org.apache.poi.hssf.record.cont.ContinuableRecordOutput;
import org.apache.poi.util.StringUtil;

/**
 * STRING (0x0207)<p/>
 * 
 * Stores the cached result of a text formula
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public final class StringRecord extends ContinuableRecord {

	public final static short sid = 0x0207;

	private boolean _is16bitUnicode;
	private String _text;


    public StringRecord()
    {
    }

    /**
     * @param in the RecordInputstream to read the record from
     */
    public StringRecord( RecordInputStream in) {
        int field_1_string_length           = in.readUShort();
        _is16bitUnicode            = in.readByte() != 0x00;
        
        if (_is16bitUnicode){
            _text = in.readUnicodeLEString(field_1_string_length);
        } else {
            _text = in.readCompressedUnicode(field_1_string_length);
        }
    }


    protected void serialize(ContinuableRecordOutput out) {
        out.writeShort(_text.length());
        out.writeStringData(_text);
    }


    public short getSid()
    {
        return sid;
    }

    /**
     * @return The string represented by this record.
     */
    public String getString()
    {
        return _text;
    }


    /**
     * Sets the string represented by this record.
     */
    public void setString(String string) {
        _text = string;
        _is16bitUnicode = StringUtil.hasMultibyte(string);        
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[STRING]\n");
        buffer.append("    .string            = ")
            .append(_text).append("\n");
        buffer.append("[/STRING]\n");
        return buffer.toString();
    }
    
    public Object clone() {
        StringRecord rec = new StringRecord();
        rec._is16bitUnicode= _is16bitUnicode;
        rec._text = _text;
        return rec;
    }
}
