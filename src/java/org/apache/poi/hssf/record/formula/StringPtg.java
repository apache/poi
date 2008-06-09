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

package org.apache.poi.hssf.record.formula;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.StringUtil;
import org.apache.poi.hssf.record.RecordInputStream;

/**
 * String Stores a String value in a formula value stored in the format
 * &lt;length 2 bytes&gt;char[]
 * 
 * @author Werner Froidevaux
 * @author Jason Height (jheight at chariot dot net dot au)
 * @author Bernard Chesnoy
 */
public final class StringPtg extends ScalarConstantPtg {
    public final static int SIZE = 9;
    public final static byte sid = 0x17;
    private static final BitField fHighByte = BitFieldFactory.getInstance(0x01);
    /** the character (")used in formulas to delimit string literals */
    private static final char FORMULA_DELIMITER = '"';

    /**
     * NOTE: OO doc says 16bit length, but BiffViewer says 8 Book says something
     * totally different, so don't look there!
     */
    private final int field_1_length;
    private final byte field_2_options;
    private final String field_3_string;

    /** Create a StringPtg from a stream */
    public StringPtg(RecordInputStream in) {
        field_1_length = in.readUByte();
        field_2_options = in.readByte();
        if (fHighByte.isSet(field_2_options)) {
            field_3_string = in.readUnicodeLEString(field_1_length);
        } else {
            field_3_string = in.readCompressedUnicode(field_1_length);
        }

        // setValue(new String(data, offset+3, data[offset+1] + 256*data[offset+2]));
    }

    /**
     * Create a StringPtg from a string representation of the number Number
     * format is not checked, it is expected to be validated in the parser that
     * calls this method.
     * 
     * @param value :
     *            String representation of a floating point number
     */
    public StringPtg(String value) {
        if (value.length() > 255) {
            throw new IllegalArgumentException(
                    "String literals in formulas can't be bigger than 255 characters ASCII");
        }
        field_2_options = (byte) fHighByte.setBoolean(0, StringUtil.hasMultibyte(value));
        field_3_string = value;
        field_1_length = value.length(); // for the moment, we support only ASCII strings in formulas we create
    }

    public String getValue() {
        return field_3_string;
    }

    public void writeBytes(byte[] array, int offset) {
        array[offset + 0] = sid;
        array[offset + 1] = (byte) field_1_length;
        array[offset + 2] = field_2_options;
        if (fHighByte.isSet(field_2_options)) {
            StringUtil.putUnicodeLE(getValue(), array, offset + 3);
        } else {
            StringUtil.putCompressedUnicode(getValue(), array, offset + 3);
        }
    }

    public int getSize() {
        if (fHighByte.isSet(field_2_options)) {
            return 2 * field_1_length + 3;
        } else {
            return field_1_length + 3;
        }
    }

    public String toFormulaString(Workbook book) {
        String value = field_3_string;
        int len = value.length();
        StringBuffer sb = new StringBuffer(len + 4);
        sb.append(FORMULA_DELIMITER);

        for (int i = 0; i < len; i++) {
            char c = value.charAt(i);
            if (c == FORMULA_DELIMITER) {
                sb.append(FORMULA_DELIMITER);
            }
            sb.append(c);
        }

        sb.append(FORMULA_DELIMITER);
        return sb.toString();
    }

    public String toString() {
        StringBuffer sb = new StringBuffer(64);
        sb.append(getClass().getName()).append(" [");
        sb.append(field_3_string);
        sb.append("]");
        return sb.toString();
    }
}
