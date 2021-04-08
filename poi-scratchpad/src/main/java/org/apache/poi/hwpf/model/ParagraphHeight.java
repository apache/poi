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

import java.io.IOException;
import java.io.OutputStream;

import org.apache.poi.common.Duplicatable;
import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianConsts;

@Internal
public final class ParagraphHeight implements Duplicatable {
  private static final BitField fSpare = BitFieldFactory.getInstance(0x0001);
  private static final BitField fUnk = BitFieldFactory.getInstance(0x0002);
  private static final BitField fDiffLines = BitFieldFactory.getInstance(0x0004);
  private static final BitField clMac = BitFieldFactory.getInstance(0xff00);


  private short infoField;
  private short reserved;
  private int dxaCol;
  private int dymLineOrHeight;

  public ParagraphHeight() {}

  public ParagraphHeight(ParagraphHeight other) {
    infoField = other.infoField;
    reserved = other.reserved;
    dxaCol = other.dxaCol;
    dymLineOrHeight = other.dymLineOrHeight;
  }

  public ParagraphHeight(byte[] buf, int offset) {
    infoField = LittleEndian.getShort(buf, offset);
    offset += LittleEndianConsts.SHORT_SIZE;
    reserved = LittleEndian.getShort(buf, offset);
    offset += LittleEndianConsts.SHORT_SIZE;
    dxaCol = LittleEndian.getInt(buf, offset);
    offset += LittleEndianConsts.INT_SIZE;
    dymLineOrHeight = LittleEndian.getInt(buf, offset);
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
    offset += LittleEndianConsts.SHORT_SIZE;
    LittleEndian.putShort(buf, offset, reserved);
    offset += LittleEndianConsts.SHORT_SIZE;
    LittleEndian.putInt(buf, offset, dxaCol);
    offset += LittleEndianConsts.INT_SIZE;
    LittleEndian.putInt(buf, offset, dymLineOrHeight);

    return buf;
  }

  public boolean equals(Object o)
  {
    if (!(o instanceof ParagraphHeight)) return false;
    ParagraphHeight ph = (ParagraphHeight)o;

    return infoField == ph.infoField && reserved == ph.reserved &&
           dxaCol == ph.dxaCol && dymLineOrHeight == ph.dymLineOrHeight;
  }

  @Override
  public int hashCode() {
      assert false : "hashCode not designed";
      return 42; // any arbitrary constant will do
  }

  @Override
  public ParagraphHeight copy() {
    return new ParagraphHeight(this);
  }
}
