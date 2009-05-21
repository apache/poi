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

public final class ShadingDescriptor
  implements Cloneable
{
  public static final int SIZE = 2;

  private short _info;
    private final static BitField _icoFore = BitFieldFactory.getInstance(0x1f);
    private final static BitField _icoBack = BitFieldFactory.getInstance(0x3e0);
    private final static BitField _ipat = BitFieldFactory.getInstance(0xfc00);

  public ShadingDescriptor()
  {
  }

  public ShadingDescriptor(byte[] buf, int offset)
  {
    this(LittleEndian.getShort(buf, offset));
  }

  public ShadingDescriptor(short info)
  {
    _info = info;
  }

  public short toShort()
  {
    return _info;
  }

  public void serialize(byte[] buf, int offset)
  {
    LittleEndian.putShort(buf, offset, _info);
  }

  public Object clone()
    throws CloneNotSupportedException
  {
    return super.clone();
  }
}
