/* ====================================================================
   Copyright 2003-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.hssf.record.formula;

import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.BitField;
import org.apache.poi.hssf.model.Workbook;
import org.apache.poi.util.StringUtil;

/**
 * Number
 * Stores a String value in a formula value stored in the format <length 2 bytes>char[]
 * @author  Werner Froidevaux
 * @author Jason Height (jheight at chariot dot net dot au)
 */

public class StringPtg
    extends Ptg
{
    public final static int  SIZE = 9;
    public final static byte sid  = 0x17;
    //NOTE: OO doc says 16bit lenght, but BiffViewer says 8
    // Book says something totally different, so dont look there!
    byte field_1_length;
    byte field_2_options;
    BitField fHighByte = new BitField(0x01);
    private String            field_3_string;

    private StringPtg() {
      //Required for clone methods
    }

    /** Create a StringPtg from a byte array read from disk */
    public StringPtg(byte [] data, int offset)
    {
        offset++;
        field_1_length = data[offset];
        field_2_options = data[offset+1];
        if (fHighByte.isSet(field_2_options)) {
            field_3_string= StringUtil.getFromUnicode(data,offset+2,field_1_length);
        }else {
            field_3_string=StringUtil.getFromCompressedUnicode(data,offset+2,field_1_length);
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
        this.fHighByte.setBoolean(field_2_options, false);
        this.field_3_string=value;
        this.field_1_length=(byte)value.length(); //for the moment, we support only ASCII strings in formulas we create
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
        array[ offset + 1 ] = field_1_length;
        array[ offset + 2 ] = field_2_options;
        if (fHighByte.isSet(field_2_options)) {
            StringUtil.putUncompressedUnicode(getValue(),array,offset+3);
        }else {
            StringUtil.putCompressedUnicode(getValue(),array,offset+3);
        }
    }

    public int getSize()
    {
        if (fHighByte.isSet(field_2_options)) {
            return 2*field_1_length+3;
        }else {
            return field_1_length+3;
        }
    }

    public String toFormulaString(Workbook book)
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
