/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.poi.hwpf.sprm;

import org.apache.poi.util.BitField;
import org.apache.poi.util.LittleEndian;

/**
 * This class is used to represent a sprm operation from a Word 97/2000/XP
 * document.
 * @author Ryan Ackley
 * @version 1.0
 */
public class SprmOperation
{
  final static private BitField OP_BITFIELD = new BitField(0x1ff);
  final static private BitField SPECIAL_BITFIELD = new BitField(0x200);
  final static private BitField TYPE_BITFIELD = new BitField(0x1c00);
  final static private BitField SIZECODE_BITFIELD = new BitField(0xe000);

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
        throw new UnsupportedOperationException("This SPRM contains a variable length operand");
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
        else
        {
          return (0x000000ff & _grpprl[_gOffset++]) + 3;
        }
      case 7:
        return 5;
      default:
        throw new IllegalArgumentException("SPRM contains an invalid size code");
    }
  }
}
