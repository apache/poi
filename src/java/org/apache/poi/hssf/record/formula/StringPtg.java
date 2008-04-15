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

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.StringUtil;
import org.apache.poi.hssf.record.RecordInputStream;

/**
 * Number
 * Stores a String value in a formula value stored in the format &lt;length 2 bytes&gt;char[]
 * @author  Werner Froidevaux
 * @author Jason Height (jheight at chariot dot net dot au)
 * @author Bernard Chesnoy
 */

public class StringPtg
    extends Ptg
{
    public final static int  SIZE = 9;
    public final static byte sid  = 0x17;
    //NOTE: OO doc says 16bit lenght, but BiffViewer says 8
    // Book says something totally different, so dont look there!
    int field_1_length;
    byte field_2_options;
    BitField fHighByte = BitFieldFactory.getInstance(0x01);
    private String            field_3_string;

    private StringPtg() {
      //Required for clone methods
    }

    /** Create a StringPtg from a byte array read from disk */
    public StringPtg(RecordInputStream in)
    {
        field_1_length = in.readByte() & 0xFF;
        field_2_options = in.readByte();
        if (fHighByte.isSet(field_2_options)) {
            field_3_string= in.readUnicodeLEString(field_1_length);
        }else {
            field_3_string=in.readCompressedUnicode(field_1_length);
        }

        //setValue(new String(data, offset+3, data[offset+1] + 256*data[offset+2]));
    }

    /** Create a StringPtg from a string representation of  the number
     *  Number format is not checked, it is expected to be validated in the parser
     *   that calls this method.
     *  @param value : String representation of a floating point number
     */
    public StringPtg(String value) {
        if (value.length() >255) {
            throw new IllegalArgumentException("String literals in formulas cant be bigger than 255 characters ASCII");
        }
        this.field_2_options=0;        
        field_2_options = (byte)this.fHighByte.setBoolean(field_2_options, StringUtil.hasMultibyte(value));
        this.field_3_string=value;
        this.field_1_length=value.length(); //for the moment, we support only ASCII strings in formulas we create
    }

    /*
    public void setValue(String value)
    {
        field_1_value = value;
    }*/


    public String getValue()
    {
        return field_3_string;
    }

    public void writeBytes(byte [] array, int offset)
    {
        array[ offset + 0 ] = sid;
        array[ offset + 1 ] = (byte)field_1_length;
        array[ offset + 2 ] = field_2_options;
        if (fHighByte.isSet(field_2_options)) {
            StringUtil.putUnicodeLE(getValue(),array,offset+3);
        }else {
            StringUtil.putCompressedUnicode(getValue(),array,offset+3);
        }
    }

    public int getSize()
    {
        if (fHighByte.isSet(field_2_options)) {
            return 2*field_1_length+3;
        } else {
            return field_1_length+3;
        }
    }

    public String toFormulaString(HSSFWorkbook book)
    {
        return "\""+getValue()+"\"";
    }
    public byte getDefaultOperandClass() {
       return Ptg.CLASS_VALUE;
   }

   public Object clone() {
     StringPtg ptg = new StringPtg();
     ptg.field_1_length = field_1_length;
     ptg.field_2_options=field_2_options;
     ptg.field_3_string=field_3_string;
     return ptg;
   }

}

