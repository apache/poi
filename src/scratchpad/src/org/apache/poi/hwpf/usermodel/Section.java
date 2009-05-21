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

import org.apache.poi.hwpf.model.SEPX;

public final class Section
  extends Range
{

  private SectionProperties _props;

  public Section(SEPX sepx, Range parent)
  {
    super(Math.max(parent._start, sepx.getStart()), Math.min(parent._end, sepx.getEnd()), parent);
    _props = sepx.getSectionProperties();
  }

  public int type()
  {
    return TYPE_SECTION;
  }

  public int getNumColumns()
  {
    return _props.getCcolM1() + 1;
  }

  public Object clone()
     throws CloneNotSupportedException
   {
     Section s = (Section)super.clone();
     s._props = (SectionProperties)_props.clone();
     return s;
   }


}
