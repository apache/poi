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

package org.apache.poi.hwpf.sprm;

import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.LittleEndian;

/**
 * This class is used to represent a sprm operation from a Word 97/2000/XP
 * document.
 * @author Ryan Ackley
 * @version 1.0
 */
public final class SprmOperation
{
  final static private BitField OP_BITFIELD = BitFieldFactory.getInstance(0x1ff);
  final static private BitField SPECIAL_BITFIELD = BitFieldFactory.getInstance(0x200);
  final static private BitField TYPE_BITFIELD = BitFieldFactory.getInstance(0x1c00);
  final static private BitField SIZECODE_BITFIELD = BitFieldFactory.getInstance(0xe000);

  final static private short LONG_SPRM_TABLE = (short)0xd608;
  final static private short LONG_SPRM_PARAGRAPH = (short)0xc615;

  final static public int PAP_TYPE = 1;
  final static public int TAP_TYPE = 5;

  private int _type;
  private int _operation;
  private int _gOffset;
  private byte[] _grpprl;
  private int _sizeCode;
  private int _size;

  public SprmOperation(byte[] grpprl, int offset)
  {
    _grpprl = grpprl;

    short sprmStart = LittleEndian.getShort(grpprl, offset);

    _gOffset = offset + 2;

    _operation = OP_BITFIELD.getValue(sprmStart);
    _type = TYPE_BITFIELD.getValue(sprmStart);
    _sizeCode = SIZECODE_BITFIELD.getValue(sprmStart);
    _size = initSize(sprmStart);
  }

  public static int getOperationFromOpcode(short opcode)
  {
    return OP_BITFIELD.getValue(opcode);
  }

  public static int getTypeFromOpcode(short opcode)
  {
    return TYPE_BITFIELD.getValue(opcode);
  }

  public int getType()
  {
    return _type;
  }

  public int getOperation()
  {
    return _operation;
  }

  public int getGrpprlOffset()
  {
    return _gOffset;
  }
  public int getOperand()
  {
    switch (_sizeCode)
    {
      case 0:
      case 1:
        return _grpprl[_gOffset];
      case 2:
      case 4:
      case 5:
        return LittleEndian.getShort(_grpprl, _gOffset);
      case 3:
        return LittleEndian.getInt(_grpprl, _gOffset);
      case 6:
          byte operandLength = _grpprl[_gOffset + 1];   //surely shorter than an int...

          byte [] codeBytes = new byte[LittleEndian.INT_SIZE]; //initialized to zeros by JVM
          for(int i = 0; i < operandLength; i++)
              if(_gOffset + i < _grpprl.length)
    			  codeBytes[i] = _grpprl[_gOffset + 1 + i];

          return LittleEndian.getInt(codeBytes, 0);
      case 7:
        byte threeByteInt[] = new byte[4];
        threeByteInt[0] = _grpprl[_gOffset];
        threeByteInt[1] = _grpprl[_gOffset + 1];
        threeByteInt[2] = _grpprl[_gOffset + 2];
        threeByteInt[3] = (byte)0;
        return LittleEndian.getInt(threeByteInt, 0);
      default:
        throw new IllegalArgumentException("SPRM contains an invalid size code");
    }
  }
  public int getSizeCode()
  {
    return _sizeCode;
  }

  public int size()
  {
    return _size;
  }

  public byte[] getGrpprl()
  {
    return _grpprl;
  }
  private int initSize(short sprm)
  {
    switch (_sizeCode)
    {
      case 0:
      case 1:
        return 3;
      case 2:
      case 4:
      case 5:
        return 4;
      case 3:
        return 6;
      case 6:
        if (sprm == LONG_SPRM_TABLE || sprm == LONG_SPRM_PARAGRAPH)
        {
          int retVal = (0x0000ffff & LittleEndian.getShort(_grpprl, _gOffset)) + 3;
          _gOffset += 2;
          return retVal;
        }
        return (0x000000ff & _grpprl[_gOffset++]) + 3;
      case 7:
        return 5;
      default:
        throw new IllegalArgumentException("SPRM contains an invalid size code");
    }
  }
}
