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

/**
 * Handles the fibRgW / FibRgW97 part of
 *  the FIB (File Information Block)
 */
public final class FIBShortHandler {
  public final static int MAGICCREATED = 0;
  public final static int MAGICREVISED = 1;
  public final static int MAGICCREATEDPRIVATE = 2;
  public final static int MAGICREVISEDPRIVATE = 3;
  public final static int LIDFE = 13;

  final static int START = 0x20;

  short[] _shorts;

  public FIBShortHandler(byte[] mainStream)
  {
    int offset = START;
    int shortCount = LittleEndian.getShort(mainStream, offset);
    offset += LittleEndian.SHORT_SIZE;
    _shorts = new short[shortCount];

    for (int x = 0; x < shortCount; x++)
    {
      _shorts[x] = LittleEndian.getShort(mainStream, offset);
      offset += LittleEndian.SHORT_SIZE;
    }
  }

  public short getShort(int shortCode)
  {
    return _shorts[shortCode];
  }

  int sizeInBytes()
  {
    return (_shorts.length * LittleEndian.SHORT_SIZE) + LittleEndian.SHORT_SIZE;
  }

  void serialize(byte[] mainStream) {
    int offset = START;
    LittleEndian.putShort(mainStream, offset, (short)_shorts.length);
    offset += LittleEndian.SHORT_SIZE;
    //mainStream.write(holder);

    for (int x = 0; x < _shorts.length; x++)
    {
      LittleEndian.putShort(mainStream, offset, _shorts[x]);
      offset += LittleEndian.SHORT_SIZE;
    }
  }


}
