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

import org.apache.poi.util.LittleEndian;

import java.util.Arrays;

public class SprmBuffer
  implements Cloneable
{
  byte[] _buf;
  int _offset;
  boolean _istd;

  public SprmBuffer(byte[] buf, boolean istd)
  {
    _offset = buf.length;
    _buf = buf;
    _istd = istd;
  }
  public SprmBuffer(byte[] buf)
  {
    this(buf, false);
  }
  public SprmBuffer()
  {
    _buf = new byte[4];
    _offset = 0;
  }

  private int findSprm(short opcode)
  {
    int operation = SprmOperation.getOperationFromOpcode(opcode);
    int type = SprmOperation.getTypeFromOpcode(opcode);

    SprmIterator si = new SprmIterator(_buf, 2);
    while(si.hasNext())
    {
      SprmOperation i = si.next();
      if(i.getOperation() == operation && i.getType() == type)
        return i.getGrpprlOffset();
    }
    return -1;
  }

  public void updateSprm(short opcode, byte operand)
  {
    int grpprlOffset = findSprm(opcode);
    if(grpprlOffset != -1)
    {
      _buf[grpprlOffset] = operand;
      return;
    }
    else addSprm(opcode, operand);
  }

  public void updateSprm(short opcode, short operand)
  {
    int grpprlOffset = findSprm(opcode);
    if(grpprlOffset != -1)
    {
      LittleEndian.putShort(_buf, grpprlOffset, operand);
      return;
    }
    else addSprm(opcode, operand);
  }

  public void updateSprm(short opcode, int operand)
  {
    int grpprlOffset = findSprm(opcode);
    if(grpprlOffset != -1)
    {
      LittleEndian.putInt(_buf, grpprlOffset, operand);
      return;
    }
    else addSprm(opcode, operand);
  }

  public void addSprm(short opcode, byte operand)
  {
    int addition = LittleEndian.SHORT_SIZE + LittleEndian.BYTE_SIZE;
    ensureCapacity(addition);
    LittleEndian.putShort(_buf, _offset, opcode);
    _offset += LittleEndian.SHORT_SIZE;
    _buf[_offset++] = operand;
  }
  public void addSprm(short opcode, short operand)
  {
    int addition = LittleEndian.SHORT_SIZE + LittleEndian.SHORT_SIZE;
    ensureCapacity(addition);
    LittleEndian.putShort(_buf, _offset, opcode);
    _offset += LittleEndian.SHORT_SIZE;
    LittleEndian.putShort(_buf, _offset, operand);
    _offset += LittleEndian.SHORT_SIZE;
  }
  public void addSprm(short opcode, int operand)
  {
    int addition = LittleEndian.SHORT_SIZE + LittleEndian.INT_SIZE;
    ensureCapacity(addition);
    LittleEndian.putShort(_buf, _offset, opcode);
    _offset += LittleEndian.SHORT_SIZE;
    LittleEndian.putInt(_buf, _offset, operand);
    _offset += LittleEndian.INT_SIZE;
  }
  public void addSprm(short opcode, byte[] operand)
  {
    int addition = LittleEndian.SHORT_SIZE + LittleEndian.BYTE_SIZE + operand.length;
    ensureCapacity(addition);
    LittleEndian.putShort(_buf, _offset, opcode);
    _offset += LittleEndian.SHORT_SIZE;
    _buf[_offset++] = (byte)operand.length;
    System.arraycopy(operand, 0, _buf, _offset, operand.length);
  }

  public byte[] toByteArray()
  {
    return _buf;
  }

  public boolean equals(Object obj)
  {
    SprmBuffer sprmBuf = (SprmBuffer)obj;
    return (Arrays.equals(_buf, sprmBuf._buf));
  }

  public void append(byte[] grpprl)
  {
    ensureCapacity(grpprl.length);
    System.arraycopy(grpprl, 0, _buf, _offset, grpprl.length);
  }

  public Object clone()
    throws CloneNotSupportedException
  {
    SprmBuffer retVal = (SprmBuffer)super.clone();
    retVal._buf = new byte[_buf.length];
    System.arraycopy(_buf, 0, retVal._buf, 0, _buf.length);
    return retVal;
  }

  private void ensureCapacity(int addition)
  {
    if (_offset + addition >= _buf.length)
    {
      // add 6 more than they need for use the next iteration
      byte[] newBuf = new byte[_offset + addition + 6];
      System.arraycopy(_buf, 0, newBuf, 0, _buf.length);
      _buf = newBuf;
    }
  }
}
