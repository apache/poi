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

import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.LittleEndian;

/**
 *
 * @author  andy
 * @author Jason Height (jheight at chariot dot net dot au)
 */

public class NamePtg
    extends Ptg
{
    public final static short sid  = 0x23;
    private final static int  SIZE = 5;
    /** one-based index to defined name record */
    private short             field_1_label_index;
    private short             field_2_zero;   // reserved must be 0
    boolean xtra=false;


    private NamePtg() {
      //Required for clone methods
    }

    /**
     * Creates new NamePtg and sets its name index to that of the corresponding defined name record
     * in the workbook.  The search for the name record is case insensitive.  If it is not found, 
     * it gets created.
     */
    public NamePtg(String name, Workbook book) {
        field_1_label_index = (short)(1+getOrCreateNameRecord(book, name)); // convert to 1-based
    }
    /**
     * @return zero based index of the found or newly created defined name record. 
     */
    private static final int getOrCreateNameRecord(Workbook book, String name) {
        // perhaps this logic belongs in Workbook?
        int countNames = book.getNumberOfNames();
        for (int i = 0; i < countNames; i++) {
            if(name.equalsIgnoreCase( book.getNameName(i) )) {
                return i; 
            }
        }
        
        Name nameObj = book.createName();
        nameObj.setNameName(name);
        
        return countNames;
    }

    /** Creates new NamePtg */

    public NamePtg(RecordInputStream in)
    {
        //field_1_ixti        = LittleEndian.getShort(data, offset);
        field_1_label_index = in.readShort();
        field_2_zero        = in.readShort();
        //if (data[offset+6]==0) xtra=true;
    }
    
    /**
     * @return zero based index to a defined name record in the LinkTable
     */
    public int getIndex() {
        return field_1_label_index-1; // convert to zero based
    }

    public void writeBytes(byte [] array, int offset)
    {
        array[offset+0]= (byte) (sid + ptgClass);
        LittleEndian.putShort(array,offset+1,field_1_label_index);
        LittleEndian.putShort(array,offset+3, field_2_zero);
    }

    public int getSize()
    {
        return SIZE;
    }

    public String toFormulaString(Workbook book)
    {
    	return book.getNameName(field_1_label_index - 1);
    }
    
    public byte getDefaultOperandClass() {return Ptg.CLASS_REF;}

    public Object clone() {
      NamePtg ptg = new NamePtg();
      ptg.field_1_label_index = field_1_label_index;
      ptg.field_2_zero = field_2_zero;
      return ptg;
    }
}
