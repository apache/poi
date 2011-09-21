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

import java.util.Arrays;

import org.apache.poi.hwpf.model.types.LVLFAbstractType;

import org.apache.poi.util.BitField;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;

/**
 * "List LeVeL (on File) (LVLF)"
 * 
 * See page 170 for details.
 */
@Internal
public final class ListLevel 
{
//    private int _iStartAt;
//    private byte _nfc;
//    private byte _info;
//    /*   */private static BitField _jc;
//    /*   */private static BitField _fLegal;
//    /*   */private static BitField _fNoRestart;
//    /*   */private static BitField _fPrev;
//    /*   */private static BitField _fPrevSpace;
//    /*   */private static BitField _fWord6;
//    private byte[] _rgbxchNums;
//    private byte _ixchFollow;
//    private int _dxaSpace;
//    private int _dxaIndent;
//    private int _cbGrpprlChpx;
//    private int _cbGrpprlPapx;
//    private short _reserved;
    private LVLF _lvlf;
    private byte[] _grpprlPapx;
    private byte[] _grpprlChpx;
    private char[] _numberText = null;

    public ListLevel( final byte[] buf, final int originalOffset )
    {
        int offset = originalOffset;

        _lvlf = new LVLF(buf, offset);
        offset += LVLF.getSize();

        _grpprlPapx = new byte[_lvlf.getCbGrpprlPapx()];
        System.arraycopy( buf, offset, _grpprlPapx, 0, _lvlf.getCbGrpprlPapx() );
        offset += _lvlf.getCbGrpprlPapx();

        _grpprlChpx = new byte[_lvlf.getCbGrpprlChpx()];
        System.arraycopy( buf, offset, _grpprlChpx, 0, _lvlf.getCbGrpprlChpx() );
        offset += _lvlf.getCbGrpprlChpx();

        int numberTextLength = LittleEndian.getShort( buf, offset );
        /* sometimes numberTextLength<0 */
        /* by derjohng */
        if ( numberTextLength > 0 )
        {
            _numberText = new char[numberTextLength];
            offset += LittleEndian.SHORT_SIZE;
            for ( int x = 0; x < numberTextLength; x++ )
            {
                _numberText[x] = (char) LittleEndian.getShort( buf, offset );
                offset += LittleEndian.SHORT_SIZE;
            }
        }

    }

    public ListLevel( int level, boolean numbered )
    {
        _lvlf = new LVLF();
        setStartAt( 1 );
        _grpprlPapx = new byte[0];
        _grpprlChpx = new byte[0];
        _numberText = new char[0];
        
        if ( numbered )
        {
            _lvlf.getRgbxchNums()[0] = 1;
            _numberText = new char[] { (char) level, '.' };
        }
        else
        {
            _numberText = new char[] { '\u2022' };
        }
    }

    public ListLevel( int startAt, int numberFormatCode, int alignment,
            byte[] numberProperties, byte[] entryProperties, String numberText )
    {
        _lvlf = new LVLF();
        setStartAt( startAt );
        _lvlf.setNfc( (short) numberFormatCode );
        _lvlf.setJc( (byte) alignment );
        _grpprlChpx = numberProperties;
        _grpprlPapx = entryProperties;
        _numberText = numberText.toCharArray();
    }

    public boolean equals( Object obj )
    {
        if ( obj == null )
            return false;

        ListLevel lvl = (ListLevel) obj;
        return lvl._lvlf.equals( this._lvlf )
                && Arrays.equals( lvl._grpprlChpx, _grpprlChpx )
                && Arrays.equals( lvl._grpprlPapx, _grpprlPapx )
                && Arrays.equals( lvl._numberText, _numberText );
    }

    /**
     * "Alignment (left, right, or centered) of the paragraph number."
     */
    public int getAlignment()
    {
        return _lvlf.getJc();
    }

    public byte[] getLevelProperties()
    {
        return _grpprlPapx;
    }

    /**
     * "Number format code (see anld.nfc for a list of options)"
     */
    public int getNumberFormat()
    {
        return _lvlf.getNfc();
    }

    public String getNumberText()
    {
        if ( _numberText == null )
            return null;

        return new String( _numberText );
    }

    public int getSizeInBytes()
    {
        int result = LVLF.getSize() + _lvlf.getCbGrpprlChpx()
                + _lvlf.getCbGrpprlPapx() + 2; // numberText length
        if ( _numberText != null )
        {
            result += _numberText.length * LittleEndian.SHORT_SIZE;
        }
        return result;
    }

    public int getStartAt()
    {
        return _lvlf.getIStartAt();
    }

    /**
     * "The type of character following the number text for the paragraph: 0 == tab, 1 == space, 2 == nothing."
     */
    public byte getTypeOfCharFollowingTheNumber()
    {
        return _lvlf.getIxchFollow();
    }

    public void setAlignment( int alignment )
    {
        _lvlf.setJc( (byte) alignment );
    }

    public void setLevelProperties( byte[] grpprl )
    {
        _grpprlPapx = grpprl;
    }

    public void setNumberFormat( int numberFormatCode )
    {
        _lvlf.setNfc( (short) numberFormatCode );
    }

    public void setNumberProperties( byte[] grpprl )
    {
        _grpprlChpx = grpprl;

    }

    public void setStartAt( int startAt )
    {
        _lvlf.setIStartAt( startAt );
    }

    public void setTypeOfCharFollowingTheNumber( byte value )
    {
        _lvlf.setIxchFollow( value );
    }

    public byte[] toByteArray()
    {
        byte[] buf = new byte[getSizeInBytes()];
        int offset = 0;

        _lvlf.setCbGrpprlChpx( (short) _grpprlChpx.length );
        _lvlf.setCbGrpprlPapx( (short) _grpprlPapx.length );
        _lvlf.serialize( buf, offset );
        offset += LVLF.getSize();

        System.arraycopy( _grpprlPapx, 0, buf, offset, _grpprlPapx.length );
        offset += _grpprlPapx.length;
        System.arraycopy( _grpprlChpx, 0, buf, offset, _grpprlChpx.length );
        offset += _grpprlChpx.length;

        if ( _numberText == null )
        {
            // TODO - write junit to test this flow
            LittleEndian.putUShort( buf, offset, 0 );
        }
        else
        {
            LittleEndian.putUShort( buf, offset, _numberText.length );
            offset += LittleEndian.SHORT_SIZE;
            for ( int x = 0; x < _numberText.length; x++ )
            {
                LittleEndian.putUShort( buf, offset, _numberText[x] );
                offset += LittleEndian.SHORT_SIZE;
            }
        }
        return buf;
    }

}
