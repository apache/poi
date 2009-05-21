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

package org.apache.poi.hdf.extractor;

/**
 * Comment me
 *
 * @author Ryan Ackley
 */

public final class TC
{

  boolean _fFirstMerged;
  boolean _fMerged;
  boolean _fVertical;
  boolean _fBackward;
  boolean _fRotateFont;
  boolean _fVertMerge;
  boolean _fVertRestart;
  short _vertAlign;
  short[] _brcTop = new short[2];
  short[] _brcLeft = new short[2];
  short[] _brcBottom = new short[2];
  short[] _brcRight = new short [2];

  public TC()
  {
  }
  static TC convertBytesToTC(byte[] array, int offset)
  {
    TC tc = new TC();
    int rgf = Utils.convertBytesToShort(array, offset);
    tc._fFirstMerged = (rgf & 0x0001) > 0;
    tc._fMerged = (rgf & 0x0002) > 0;
    tc._fVertical = (rgf & 0x0004) > 0;
    tc._fBackward = (rgf & 0x0008) > 0;
    tc._fRotateFont = (rgf & 0x0010) > 0;
    tc._fVertMerge = (rgf & 0x0020) > 0;
    tc._fVertRestart = (rgf & 0x0040) > 0;
    tc._vertAlign = (short)((rgf & 0x0180) >> 7);

    tc._brcTop[0] = Utils.convertBytesToShort(array, offset + 4);
    tc._brcTop[1] = Utils.convertBytesToShort(array, offset + 6);

    tc._brcLeft[0] = Utils.convertBytesToShort(array, offset + 8);
    tc._brcLeft[1] = Utils.convertBytesToShort(array, offset + 10);

    tc._brcBottom[0] = Utils.convertBytesToShort(array, offset + 12);
    tc._brcBottom[1] = Utils.convertBytesToShort(array, offset + 14);

    tc._brcRight[0] = Utils.convertBytesToShort(array, offset + 16);
    tc._brcRight[1] = Utils.convertBytesToShort(array, offset + 18);

    return tc;
  }

}
