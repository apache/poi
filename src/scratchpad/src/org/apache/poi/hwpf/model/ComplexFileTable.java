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

import org.apache.poi.util.LittleEndian;
import org.apache.poi.hwpf.model.io.*;

public final class ComplexFileTable
{

  private static final byte GRPPRL_TYPE = 1;
  private static final byte TEXT_PIECE_TABLE_TYPE = 2;

  protected TextPieceTable _tpt;

  public ComplexFileTable()
  {
    _tpt = new TextPieceTable();
  }

  public ComplexFileTable(byte[] documentStream, byte[] tableStream, int offset, int fcMin) throws IOException
  {
    //skips through the prms before we reach the piece table. These contain data
    //for actual fast saved files
    while (tableStream[offset] == GRPPRL_TYPE)
    {
      offset++;
      int skip = LittleEndian.getShort(tableStream, offset);
      offset += LittleEndian.SHORT_SIZE + skip;
    }
    if(tableStream[offset] != TEXT_PIECE_TABLE_TYPE)
    {
      throw new IOException("The text piece table is corrupted");
    }
    int pieceTableSize = LittleEndian.getInt(tableStream, ++offset);
    offset += LittleEndian.INT_SIZE;
    _tpt = new TextPieceTable(documentStream, tableStream, offset, pieceTableSize, fcMin);
  }

  public TextPieceTable getTextPieceTable()
  {
    return _tpt;
  }

  public void writeTo(HWPFFileSystem sys)
    throws IOException
  {
    HWPFOutputStream docStream = sys.getStream("WordDocument");
    HWPFOutputStream tableStream = sys.getStream("1Table");

    tableStream.write(TEXT_PIECE_TABLE_TYPE);

    byte[] table = _tpt.writeTo(docStream);

    byte[] numHolder = new byte[LittleEndian.INT_SIZE];
    LittleEndian.putInt(numHolder, table.length);
    tableStream.write(numHolder);
    tableStream.write(table);
  }

}
