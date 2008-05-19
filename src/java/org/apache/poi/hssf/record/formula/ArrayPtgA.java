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

/**
 * ArrayPtgA - handles arrays
 *  
 * @author Jason Height (jheight at chariot dot net dot au)
 */
public final class ArrayPtgA extends ArrayPtg {
    public final static byte sid  = 0x60;

    private ArrayPtgA() {
      //Required for clone methods
    }

    public ArrayPtgA(RecordInputStream in) {
    	super(in);
    }
        
    public Object clone() {
      ArrayPtgA ptg = new ArrayPtgA();
      ptg.field_1_reserved = (byte[]) field_1_reserved.clone();
      
      ptg.token_1_columns = token_1_columns;
      ptg.token_2_rows = token_2_rows;
      ptg.token_3_arrayValues = (Object[]) token_3_arrayValues.clone();
      ptg.setClass(ptgClass);
      return ptg;
    }
}
