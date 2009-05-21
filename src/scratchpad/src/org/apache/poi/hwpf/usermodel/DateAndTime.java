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

import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.LittleEndian;

/**
 * This class is used to represent a date and time in a Word document.
 *
 * @author Ryan Ackley
 */
public final class DateAndTime
  implements Cloneable
{
  public static final int SIZE = 4;
  private short _info;
    private static final BitField _minutes = BitFieldFactory.getInstance(0x3f);
    private static final BitField _hours = BitFieldFactory.getInstance(0x7c0);
    private static final BitField _dom = BitFieldFactory.getInstance(0xf800);
  private short _info2;
    private static final BitField _months = BitFieldFactory.getInstance(0xf);
    private static final BitField _years = BitFieldFactory.getInstance(0x1ff0);
    private static final BitField _weekday = BitFieldFactory.getInstance(0xe000);

  public DateAndTime()
  {
  }

  public DateAndTime(byte[] buf, int offset)
  {
    _info = LittleEndian.getShort(buf, offset);
    _info2 = LittleEndian.getShort(buf, offset + LittleEndian.SHORT_SIZE);
  }

  public void serialize(byte[] buf, int offset)
  {
    LittleEndian.putShort(buf, offset, _info);
    LittleEndian.putShort(buf, offset + LittleEndian.SHORT_SIZE, _info2);
  }

  public boolean equals(Object o)
  {
    DateAndTime dttm = (DateAndTime)o;
    return _info == dttm._info && _info2 == dttm._info2;
  }

  public Object clone()
    throws CloneNotSupportedException
  {
    return super.clone();
  }
}
