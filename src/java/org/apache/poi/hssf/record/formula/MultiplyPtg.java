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

import java.util.List;
import org.apache.poi.hssf.model.Workbook;

/**
 * Implements the standard mathmatical multiplication - *
 * @author  Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 */

public class MultiplyPtg
    extends OperationPtg
{
    public final static int  SIZE = 1;
    public final static byte sid  = 0x05;
    
    private final static String MULTIPLY="*";

    /** Creates new AddPtg */

    public MultiplyPtg()
    {
    }

    public MultiplyPtg(byte [] data, int offset)
    {

        // doesn't need anything
    }
    
    public void writeBytes(byte [] array, int offset)
    {
        array[ offset + 0 ] = sid;
    }

    public int getSize()
    {
        return SIZE;
    }

    public int getType()
    {
        return TYPE_BINARY;
    }

    public int getNumberOfOperands()
    {
        return 2;
    }

    public int getStringLength() {
        return 1;
    }
    

    public String toFormulaString(Workbook book)
    {
        return "*";
    }

    public String toFormulaString(Ptg [] operands)
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append(operands[ 0 ].toFormulaString((Workbook)null));
        buffer.append("*");
        buffer.append(operands[ 1 ].toFormulaString((Workbook)null));
        return buffer.toString();
    }
    
    public String toFormulaString(String[] operands) {
        StringBuffer buffer = new StringBuffer();

        buffer.append(operands[ 0 ]);
        buffer.append(toFormulaString((Workbook)null));
        buffer.append(operands[ 1 ]);
        return buffer.toString();
    }                  

    public Object clone() {
      return new MultiplyPtg();
    }
}
