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

import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.LittleEndian;

public final class PieceDescriptor
{

  short descriptor;
   private static BitField fNoParaLast = BitFieldFactory.getInstance(0x01);
   private static BitField fPaphNil = BitFieldFactory.getInstance(0x02);
   private static BitField fCopied = BitFieldFactory.getInstance(0x04);
  int fc;
  short prm;
  boolean unicode;


  public PieceDescriptor(byte[] buf, int offset)
  {
    descriptor = LittleEndian.getShort(buf, offset);
    offset += LittleEndian.SHORT_SIZE;
    fc = LittleEndian.getInt(buf, offset);
    offset += LittleEndian.INT_SIZE;
    prm = LittleEndian.getShort(buf, offset);

    // see if this piece uses unicode.
    if ((fc & 0x40000000) == 0)
    {
        unicode = true;
    }
    else
    {
        unicode = false;
        fc &= ~(0x40000000);//gives me FC in doc stream
        fc /= 2;
    }

  }

  public int getFilePosition()
  {
    return fc;
  }

  public void setFilePosition(int pos)
  {
    fc = pos;
  }

  public boolean isUnicode()
  {
    return unicode;
  }

  protected byte[] toByteArray()
  {
    // set up the fc
    int tempFc = fc;
    if (!unicode)
    {
      tempFc *= 2;
      tempFc |= (0x40000000);
    }

    int offset = 0;
    byte[] buf = new byte[8];
    LittleEndian.putShort(buf, offset, descriptor);
    offset += LittleEndian.SHORT_SIZE;
    LittleEndian.putInt(buf, offset, tempFc);
    offset += LittleEndian.INT_SIZE;
    LittleEndian.putShort(buf, offset, prm);

    return buf;

  }

  public static int getSizeInBytes()
  {
    return 8;
  }

  public boolean equals(Object o)
  {
    PieceDescriptor pd = (PieceDescriptor)o;

    return descriptor == pd.descriptor && prm == pd.prm && unicode == pd.unicode;
  }
}
