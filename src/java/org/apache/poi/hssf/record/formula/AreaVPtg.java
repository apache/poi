
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

/*
 * AreaPtg.java
 *
 * Created on November 17, 2001, 9:30 PM
 */
package org.apache.poi.hssf.record.formula;

import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.BitField;

import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.hssf.util.AreaReference;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 * Specifies a rectangular area of cells A1:A4 for instance.
 * @author Jason Height (jheight at chariot dot net dot au)
 */

public final class AreaVPtg
    extends AreaPtg
{
    public final static short sid  = 0x45;

    protected AreaVPtg() {
      //Required for clone methods
    }

    public AreaVPtg(int firstRow, int lastRow, int firstColumn, int lastColumn, boolean firstRowRelative, boolean lastRowRelative, boolean firstColRelative, boolean lastColRelative) {
      super(firstRow, lastRow, firstColumn, lastColumn, firstRowRelative, lastRowRelative, firstColRelative, lastColRelative);
    }


    public AreaVPtg(RecordInputStream in)
    {
      super(in);
    }

    public String getAreaPtgName() {
      return "AreaVPtg";
    }

    public Object clone() {
      AreaVPtg ptg = new AreaVPtg();
      ptg.setFirstRow(getFirstRow());
      ptg.setLastRow(getLastRow());
      ptg.setFirstColumnRaw(getFirstColumnRaw());
      ptg.setLastColumnRaw(getLastColumnRaw());
      ptg.setClass(ptgClass);
      return ptg;
    }
}
