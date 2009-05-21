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

package org.apache.poi.hwpf.model;

import org.apache.poi.util.LittleEndian;

public final class SectionDescriptor
{

  private short fn;
  private int fc;
  private short fnMpr;
  private int fcMpr;

  public SectionDescriptor()
  {
  }

  public SectionDescriptor(byte[] buf, int offset)
  {
    fn = LittleEndian.getShort(buf, offset);
    offset += LittleEndian.SHORT_SIZE;
    fc = LittleEndian.getInt(buf, offset);
    offset += LittleEndian.INT_SIZE;
    fnMpr = LittleEndian.getShort(buf, offset);
    offset += LittleEndian.SHORT_SIZE;
    fcMpr = LittleEndian.getInt(buf, offset);
  }

  public int getFc()
  {
    return fc;
  }

  public void setFc(int fc)
  {
    this.fc = fc;
  }

  public boolean equals(Object o)
  {
    SectionDescriptor sed = (SectionDescriptor)o;
    return sed.fn == fn && sed.fnMpr == fnMpr;
  }

  public byte[] toByteArray()
  {
    int offset = 0;
    byte[] buf = new byte[12];

    LittleEndian.putShort(buf, offset, fn);
    offset += LittleEndian.SHORT_SIZE;
    LittleEndian.putInt(buf, offset, fc);
    offset += LittleEndian.INT_SIZE;
    LittleEndian.putShort(buf, offset, fnMpr);
    offset += LittleEndian.SHORT_SIZE;
    LittleEndian.putInt(buf, offset, fcMpr);

    return buf;
  }
}
