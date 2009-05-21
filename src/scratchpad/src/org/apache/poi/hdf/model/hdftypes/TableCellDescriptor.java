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

package org.apache.poi.hdf.model.hdftypes;

import org.apache.poi.hdf.model.hdftypes.definitions.TCAbstractType;
import org.apache.poi.util.LittleEndian;
/**
 * Comment me
 *
 * @author Ryan Ackley
 */

public final class TableCellDescriptor extends TCAbstractType implements HDFType
{

  /*boolean _fFirstMerged;
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
  short[] _brcRight = new short [2];*/

  public TableCellDescriptor()
  {
  }
  static TableCellDescriptor convertBytesToTC(byte[] array, int offset)
  {
    TableCellDescriptor tc = new TableCellDescriptor();
    int rgf = LittleEndian.getShort(array, offset);
    tc.setFFirstMerged((rgf & 0x0001) > 0);
    tc.setFMerged((rgf & 0x0002) > 0);
    tc.setFVertical((rgf & 0x0004) > 0);
    tc.setFBackward((rgf & 0x0008) > 0);
    tc.setFRotateFont((rgf & 0x0010) > 0);
    tc.setFVertMerge((rgf & 0x0020) > 0);
    tc.setFVertRestart((rgf & 0x0040) > 0);
    tc.setVertAlign((byte)((rgf & 0x0180) >> 7));

    short[] brcTop = new short[2];
    short[] brcLeft = new short[2];
    short[] brcBottom = new short[2];
    short[] brcRight = new short[2];

    brcTop[0] = LittleEndian.getShort(array, offset + 4);
    brcTop[1] = LittleEndian.getShort(array, offset + 6);

    brcLeft[0] = LittleEndian.getShort(array, offset + 8);
    brcLeft[1] = LittleEndian.getShort(array, offset + 10);

    brcBottom[0] = LittleEndian.getShort(array, offset + 12);
    brcBottom[1] = LittleEndian.getShort(array, offset + 14);

    brcRight[0] = LittleEndian.getShort(array, offset + 16);
    brcRight[1] = LittleEndian.getShort(array, offset + 18);

    return tc;
  }

}
