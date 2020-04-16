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

import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianConsts;

/**
 * This class is used to represent a sprm operation from a Word 97/2000/XP
 * document.
 */
@Internal(since="3.8 beta 4")
public final class SprmOperation
{
    private static final BitField BITFIELD_OP = BitFieldFactory
            .getInstance( 0x1ff );
    private static final BitField BITFIELD_SIZECODE = BitFieldFactory
            .getInstance( 0xe000 );
    private static final BitField BITFIELD_SPECIAL = BitFieldFactory
            .getInstance( 0x200 );
    private static final BitField BITFIELD_TYPE = BitFieldFactory
            .getInstance( 0x1c00 );

    final static private short SPRM_LONG_PARAGRAPH = (short) 0xc615;
    final static private short SPRM_LONG_TABLE = (short) 0xd608;

    public static final int TYPE_PAP = 1;
    public static final int TYPE_CHP = 2;
    public static final int TYPE_PIC = 3;
    public static final int TYPE_SEP = 4;
    public static final int TYPE_TAP = 5;

    public static int getOperationFromOpcode( short opcode )
    {
        return BITFIELD_OP.getValue( opcode );
    }

    public static int getTypeFromOpcode( short opcode )
    {
        return BITFIELD_TYPE.getValue( opcode );
    }

    private final int _offset;
    private int _gOffset;
    private final byte[] _grpprl;
    private final int _size;
    private final short _value;

    public SprmOperation( byte[] grpprl, int offset )
    {
        _grpprl = grpprl;
        _value = LittleEndian.getShort( grpprl, offset );
        _offset = offset;
        _gOffset = offset + 2;
        _size = initSize( _value );
    }

    public byte[] toByteArray()
    {
        return Arrays.copyOfRange(_grpprl, _offset, _offset + size());
    }

    public byte[] getGrpprl()
    {
        return _grpprl;
    }

    public int getGrpprlOffset()
    {
        return _gOffset;
    }

    public int getOperand()
    {
        switch ( getSizeCode() )
        {
        case 0:
        case 1:
            return _grpprl[_gOffset];
        case 2:
        case 4:
        case 5:
            return LittleEndian.getShort( _grpprl, _gOffset );
        case 3:
            return LittleEndian.getInt( _grpprl, _gOffset );
        case 6:
            // surely shorter than an int...
            byte operandLength = _grpprl[_gOffset + 1];

            // initialized to zeros by JVM
            byte[] codeBytes = new byte[LittleEndianConsts.INT_SIZE];
            for ( int i = 0; i < operandLength; i++ )
                if ( _gOffset + i < _grpprl.length )
                    codeBytes[i] = _grpprl[_gOffset + 1 + i];

            return LittleEndian.getInt( codeBytes, 0 );
        case 7:
            byte[] threeByteInt = new byte[4];
            threeByteInt[0] = _grpprl[_gOffset];
            threeByteInt[1] = _grpprl[_gOffset + 1];
            threeByteInt[2] = _grpprl[_gOffset + 2];
            threeByteInt[3] = (byte) 0;
            return LittleEndian.getInt( threeByteInt, 0 );
        default:
            throw new IllegalArgumentException(
                    "SPRM contains an invalid size code" );
        }
    }

    public short getOperandShortSigned()
    {
        int sizeCode = getSizeCode();
        if ( sizeCode != 2 && sizeCode != 4 && sizeCode != 5 )
            throw new UnsupportedOperationException(
                    "Current SPRM doesn't have signed short operand: " + this );

        return LittleEndian.getShort( _grpprl, _gOffset );
    }

    public int getOperation()
    {
        return BITFIELD_OP.getValue( _value );
    }

    public int getSizeCode()
    {
        return BITFIELD_SIZECODE.getValue( _value );
    }

    public int getType()
    {
        return BITFIELD_TYPE.getValue( _value );
    }

    private int initSize( short sprm )
    {
        switch ( getSizeCode() )
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
            int offset = _gOffset;
            if ( sprm == SPRM_LONG_TABLE || sprm == SPRM_LONG_PARAGRAPH )
            {
                int retVal = ( 0x0000ffff &
                        LittleEndian.getShort( _grpprl, offset ) ) + 3;
                _gOffset += 2;
                return retVal;
            }
            return ( 0x000000ff & _grpprl[_gOffset++] ) + 3;
        case 7:
            return 5;
        default:
            throw new IllegalArgumentException(
                    "SPRM contains an invalid size code" );
        }
    }

    public int size()
    {
        return _size;
    }

    @Override
    public String toString()
    {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append( "[SPRM] (0x" );
        stringBuilder.append( Integer.toHexString( _value & 0xffff ) );
        stringBuilder.append( "): " );
        try
        {
            stringBuilder.append( getOperand() );
        }
        catch ( Exception exc )
        {
            stringBuilder.append( "(error)" );
        }
        return stringBuilder.toString();
    }
}
