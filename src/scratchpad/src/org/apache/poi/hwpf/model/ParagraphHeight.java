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

import java.io.OutputStream;
import java.io.IOException;

import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.LittleEndian;

public final class ParagraphHeight
{
  private short infoField;
    private BitField fSpare = BitFieldFactory.getInstance(0x0001);
    private BitField fUnk = BitFieldFactory.getInstance(0x0002);
    private BitField fDiffLines = BitFieldFactory.getInstance(0x0004);
    private BitField clMac = BitFieldFactory.getInstance(0xff00);
  private short reserved;
  private int dxaCol;
  private int dymLineOrHeight;

  public ParagraphHeight(byte[] buf, int offset)
  {
    infoField = LittleEndian.getShort(buf, offset);
    offset += LittleEndian.SHORT_SIZE;
    reserved = LittleEndian.getShort(buf, offset);
    offset += LittleEndian.SHORT_SIZE;
    dxaCol = LittleEndian.getInt(buf, offset);
    offset += LittleEndian.INT_SIZE;
    dymLineOrHeight = LittleEndian.getInt(buf, offset);
  }

  public ParagraphHeight()
  {

  }

  public void write(OutputStream out)
    throws IOException
  {
    out.write(toByteArray());
  }

  protected byte[] toByteArray()
  {
    byte[] buf = new byte[12];
    int offset = 0;
    LittleEndian.putShort(buf, offset, infoField);
    offset += LittleEndian.SHORT_SIZE;
    LittleEndian.putShort(buf, offset, reserved);
    offset += LittleEndian.SHORT_SIZE;
    LittleEndian.putInt(buf, offset, dxaCol);
    offset += LittleEndian.INT_SIZE;
    LittleEndian.putInt(buf, offset, dymLineOrHeight);

    return buf;
  }

  public boolean equals(Object o)
  {
    ParagraphHeight ph = (ParagraphHeight)o;

    return infoField == ph.infoField && reserved == ph.reserved &&
           dxaCol == ph.dxaCol && dymLineOrHeight == ph.dymLineOrHeight;
  }
}
