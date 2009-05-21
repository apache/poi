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

import org.apache.poi.hwpf.model.types.SEPAbstractType;

import java.lang.reflect.Field;
import java.lang.reflect.AccessibleObject;

public final class SectionProperties
  extends SEPAbstractType
{
  public SectionProperties()
  {
    field_20_brcTop = new BorderCode();
    field_21_brcLeft = new BorderCode();
    field_22_brcBottom = new BorderCode();
    field_23_brcRight = new BorderCode();
    field_26_dttmPropRMark = new DateAndTime();
  }

  public Object clone()
    throws CloneNotSupportedException
  {
    SectionProperties copy = (SectionProperties)super.clone();
    copy.field_20_brcTop = (BorderCode)field_20_brcTop.clone();
    copy.field_21_brcLeft = (BorderCode)field_21_brcLeft.clone();
    copy.field_22_brcBottom = (BorderCode)field_22_brcBottom.clone();
    copy.field_23_brcRight = (BorderCode)field_23_brcRight.clone();
    copy.field_26_dttmPropRMark = (DateAndTime)field_26_dttmPropRMark.clone();

    return copy;
  }

  public boolean equals(Object obj)
  {
    Field[] fields = SectionProperties.class.getSuperclass().getDeclaredFields();
    AccessibleObject.setAccessible(fields, true);
    try
    {
      for (int x = 0; x < fields.length; x++)
      {
        Object obj1 = fields[x].get(this);
        Object obj2 = fields[x].get(obj);
        if (obj1 == null && obj2 == null)
        {
          continue;
        }
        if (!obj1.equals(obj2))
        {
          return false;
        }
      }
      return true;
    }
    catch (Exception e)
    {
      return false;
    }
  }

}
