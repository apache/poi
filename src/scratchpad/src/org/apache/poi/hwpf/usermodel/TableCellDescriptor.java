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

package org.apache.poi.hwpf.usermodel;

import org.apache.poi.hwpf.model.types.TCAbstractType;

public final class TableCellDescriptor
  extends TCAbstractType
{
  public static final int SIZE = 20;

  public TableCellDescriptor()
  {
    field_3_brcTop = new BorderCode();
    field_4_brcLeft = new BorderCode();
    field_5_brcBottom = new BorderCode();
    field_6_brcRight = new BorderCode();

  }

  public Object clone()
    throws CloneNotSupportedException
  {
    TableCellDescriptor tc = (TableCellDescriptor)super.clone();
    tc.field_3_brcTop = (BorderCode)field_3_brcTop.clone();
    tc.field_4_brcLeft = (BorderCode)field_4_brcLeft.clone();
    tc.field_5_brcBottom = (BorderCode)field_5_brcBottom.clone();
    tc.field_6_brcRight = (BorderCode)field_6_brcRight.clone();
    return tc;
  }

  public static TableCellDescriptor convertBytesToTC(byte[] buf, int offset)
  {
    TableCellDescriptor tc = new TableCellDescriptor();
    tc.fillFields(buf, offset);
    return tc;
  }

}
