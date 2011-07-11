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

import java.util.Arrays;

import org.apache.poi.util.LittleEndian;

public final class SprmBuffer implements Cloneable
{
    byte[] _buf;
    boolean _istd;
    int _offset;

    private final int _sprmsStartOffset;

    /**
     * @deprecated Use {@link #SprmBuffer(int)} instead
     */
    @Deprecated
    public SprmBuffer()
    {
        this( 0 );
    }

    /**
     * @deprecated Use {@link #SprmBuffer(byte[],int)} instead
     */
    @Deprecated
    public SprmBuffer( byte[] buf )
    {
        this( buf, 0 );
    }

    /**
     * @deprecated Use {@link #SprmBuffer(byte[],boolean,int)} instead
     */
    @Deprecated
    public SprmBuffer( byte[] buf, boolean istd )
    {
        this( buf, istd, 0 );
    }

    public SprmBuffer( byte[] buf, boolean istd, int sprmsStartOffset )
    {
        _offset = buf.length;
        _buf = buf;
        _istd = istd;
        _sprmsStartOffset = sprmsStartOffset;
    }

    public SprmBuffer( byte[] buf, int _sprmsStartOffset )
    {
        this( buf, false, _sprmsStartOffset );
    }

    public SprmBuffer( int sprmsStartOffset )
    {
        _buf = new byte[sprmsStartOffset + 4];
        _offset = sprmsStartOffset;
        _sprmsStartOffset = sprmsStartOffset;
    }

    public void addSprm(short opcode, byte operand)
      {
        int addition = LittleEndian.SHORT_SIZE + LittleEndian.BYTE_SIZE;
        ensureCapacity(addition);
        LittleEndian.putShort(_buf, _offset, opcode);
        _offset += LittleEndian.SHORT_SIZE;
        _buf[_offset++] = operand;
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

  public void addSprm(short opcode, int operand)
  {
    int addition = LittleEndian.SHORT_SIZE + LittleEndian.INT_SIZE;
    ensureCapacity(addition);
    LittleEndian.putShort(_buf, _offset, opcode);
    _offset += LittleEndian.SHORT_SIZE;
    LittleEndian.putInt(_buf, _offset, operand);
    _offset += LittleEndian.INT_SIZE;
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

  public void append( byte[] grpprl )
{
    append( grpprl, 0 );
}

  public void append( byte[] grpprl, int offset )
{
    ensureCapacity( grpprl.length - offset );
    System.arraycopy( grpprl, offset, _buf, _offset, grpprl.length - offset );
    _offset += grpprl.length - offset;
}
  public Object clone()
    throws CloneNotSupportedException
  {
    SprmBuffer retVal = (SprmBuffer)super.clone();
    retVal._buf = new byte[_buf.length];
    System.arraycopy(_buf, 0, retVal._buf, 0, _buf.length);
    return retVal;
  }
  private void ensureCapacity( int addition )
{
    if ( _offset + addition >= _buf.length )
    {
        // add 6 more than they need for use the next iteration
        //
        // commented - buffer shall not contain any additional bytes --
        // sergey
        // byte[] newBuf = new byte[_offset + addition + 6];
         byte[] newBuf = new byte[_offset + addition];
        System.arraycopy( _buf, 0, newBuf, 0, _buf.length );
        _buf = newBuf;
    }
}
  public boolean equals(Object obj)
  {
    SprmBuffer sprmBuf = (SprmBuffer)obj;
    return (Arrays.equals(_buf, sprmBuf._buf));
  }

  public SprmOperation findSprm( short opcode )
{
    int operation = SprmOperation.getOperationFromOpcode( opcode );
    int type = SprmOperation.getTypeFromOpcode( opcode );

    SprmIterator si = new SprmIterator( _buf, 2 );
    while ( si.hasNext() )
    {
        SprmOperation i = si.next();
        if ( i.getOperation() == operation && i.getType() == type )
            return i;
    }
    return null;
}

  private int findSprmOffset( short opcode )
{
    SprmOperation sprmOperation = findSprm( opcode );
    if ( sprmOperation == null )
        return -1;

    return sprmOperation.getGrpprlOffset();
}

    public byte[] toByteArray()
      {
        return _buf;
      }

    public SprmIterator iterator()
    {
        return new SprmIterator( _buf, _sprmsStartOffset );
    }

    public void updateSprm(short opcode, byte operand)
      {
        int grpprlOffset = findSprmOffset(opcode);
        if(grpprlOffset != -1)
        {
          _buf[grpprlOffset] = operand;
          return;
        }
        addSprm(opcode, operand);
      }

  public void updateSprm(short opcode, int operand)
  {
    int grpprlOffset = findSprmOffset(opcode);
    if(grpprlOffset != -1)
    {
      LittleEndian.putInt(_buf, grpprlOffset, operand);
      return;
    }
    addSprm(opcode, operand);
  }

    public void updateSprm(short opcode, short operand)
      {
        int grpprlOffset = findSprmOffset(opcode);
        if(grpprlOffset != -1)
        {
          LittleEndian.putShort(_buf, grpprlOffset, operand);
          return;
        }
        addSprm(opcode, operand);
      }
}
