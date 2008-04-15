
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

import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.BitField;

import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * RefNVPtg
 * @author Jason Height (jheight at chariot dot net dot au)
 */

public class RefNVPtg extends ReferencePtg
{
  public final static byte sid  = 0x4C;

  protected RefNVPtg() {
    //Required for clone methods
  }

  /** Creates new ValueReferencePtg */

  public RefNVPtg(RecordInputStream in)
  {
    super(in);
  }

  public void writeBytes(byte [] array, int offset)
  {
    throw new RuntimeException("Coding Error: This method should never be called. This ptg should be converted");
  }

  public String getRefPtgName() {
    return "RefNVPtg";
  }

  public String toFormulaString(Workbook book)
  {
    throw new RuntimeException("Coding Error: This method should never be called. This ptg should be converted");
  }

  public Object clone() {
    throw new RuntimeException("Coding Error: This method should never be called. This ptg should be converted");
  }
}
