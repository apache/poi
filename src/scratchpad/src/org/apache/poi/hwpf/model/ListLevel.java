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

import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * "The LVL structure contains formatting information about a specific level in
 * a list. When a paragraph is formatted as part of this level, each placeholder
 * in xst is replaced with the inherited level number of the most recent or
 * current paragraph in the same list that is in the zero-based level specified
 * by that placeholder. The level number that replaces a placeholder is
 * formatted according to the lvlf.nfc of the LVL structure that corresponds to
 * the level that the placeholder specifies, unless the lvlf.fLegal of this LVL
 * structure is nonzero." -- Page 388 of 621 -- [MS-DOC] -- v20110315 Word
 * (.doc) Binary File Format
 */
@Internal
public final class ListLevel
{
    private static final POILogger logger = POILogFactory
            .getLogger( ListLevel.class );

    private byte[] _grpprlChpx;
    private byte[] _grpprlPapx;
    private LVLF _lvlf;
    private char[] _xst = {};

    ListLevel()
    {

    }

    @Deprecated
    public ListLevel( final byte[] buf, final int startOffset )
    {
        read( buf, startOffset );
    }

    public ListLevel( int level, boolean numbered )
    {
        _lvlf = new LVLF();
        setStartAt( 1 );
        _grpprlPapx = new byte[0];
        _grpprlChpx = new byte[0];

        if ( numbered )
        {
            _lvlf.getRgbxchNums()[0] = 1;
            _xst = new char[] { (char) level, '.' };
        }
        else
        {
            _xst = new char[] { '\u2022' };
        }
    }

    public ListLevel( int startAt, int numberFormatCode, int alignment,
            byte[] numberProperties, byte[] entryProperties, String numberText )
    {
        _lvlf = new LVLF();
        setStartAt( startAt );
        _lvlf.setNfc( (byte) numberFormatCode );
        _lvlf.setJc( (byte) alignment );
        _grpprlChpx = numberProperties;
        _grpprlPapx = entryProperties;
        _xst = numberText.toCharArray();
    }

    public boolean equals( Object obj )
    {
        if ( obj == null )
            return false;

        ListLevel lvl = (ListLevel) obj;
        return lvl._lvlf.equals( this._lvlf )
                && Arrays.equals( lvl._grpprlChpx, _grpprlChpx )
                && Arrays.equals( lvl._grpprlPapx, _grpprlPapx )
                && Arrays.equals( lvl._xst, _xst );
    }

    /**
     * "Alignment (left, right, or centered) of the paragraph number."
     */
    public int getAlignment()
    {
        return _lvlf.getJc();
    }

    public byte[] getGrpprlChpx()
    {
        return _grpprlChpx;
    }

    public byte[] getGrpprlPapx()
    {
        return _grpprlPapx;
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
        if ( _xst.length < 2 )
            return null;

        return new String( _xst, 0, _xst.length - 1 );
    }

    public int getSizeInBytes()
    {
        return LVLF.getSize() + _lvlf.getCbGrpprlChpx()
                + _lvlf.getCbGrpprlPapx() + LittleEndian.SHORT_SIZE
                + _xst.length * LittleEndian.SHORT_SIZE;
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

    int read( final byte[] data, final int startOffset )
    {
        int offset = startOffset;

        _lvlf = new LVLF( data, offset );
        offset += LVLF.getSize();

        _grpprlPapx = new byte[_lvlf.getCbGrpprlPapx()];
        System.arraycopy( data, offset, _grpprlPapx, 0, _lvlf.getCbGrpprlPapx() );
        offset += _lvlf.getCbGrpprlPapx();

        _grpprlChpx = new byte[_lvlf.getCbGrpprlChpx()];
        System.arraycopy( data, offset, _grpprlChpx, 0, _lvlf.getCbGrpprlChpx() );
        offset += _lvlf.getCbGrpprlChpx();

        /*
         * "If this level uses bullets (see lvlf.nfc), the cch field of this Xst
         * MUST be equal to 0x0001, and this MUST NOT contain any placeholders."
         * -- page 389 of 621 -- [MS-DOC] -- v20110315 Word (.doc) Binary File
         * Format
         */
        if ( _lvlf.getNfc() == 0x17 )
        {
            int cch = LittleEndian.getUShort( data, offset );
            offset += LittleEndian.SHORT_SIZE;

            if ( cch != 1 )
            {
                logger.log( POILogger.WARN, "LVL at offset ",
                        Integer.valueOf( startOffset ),
                        " has nfc == 0x17 (bullets), but cch != 1 (",
                        Integer.valueOf( cch ), ")" );
            }

            _xst = new char[cch];
            for ( int x = 0; x < cch; x++ )
            {
                _xst[x] = (char) LittleEndian.getShort( data, offset );
                offset += LittleEndian.SHORT_SIZE;
            }
        }
        else
        {
            int cch = LittleEndian.getUShort( data, offset );
            offset += LittleEndian.SHORT_SIZE;

            if ( cch > 0 )
            {
                _xst = new char[cch];
                for ( int x = 0; x < cch; x++ )
                {
                    _xst[x] = (char) LittleEndian.getShort( data, offset );
                    offset += LittleEndian.SHORT_SIZE;
                }
            }
            else
            {
                logger.log( POILogger.WARN, "LVL.xst.cch <= 0: ",
                        Integer.valueOf( cch ) );
                /* sometimes numberTextLength<0 */
                /* by derjohng */
                _xst = new char[] {};
            }
        }

        return offset - startOffset;
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
        _lvlf.setNfc( (byte) numberFormatCode );
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

        if ( _lvlf.getNfc() == 0x17 )
        {
            LittleEndian.putUShort( buf, offset, 1 );
            offset += LittleEndian.SHORT_SIZE;

            LittleEndian.putUShort( buf, offset, _xst[0] );
            offset += LittleEndian.SHORT_SIZE;
        }
        else
        {
            LittleEndian.putUShort( buf, offset, _xst.length );
            offset += LittleEndian.SHORT_SIZE;
            for ( char c : _xst )
            {
                LittleEndian.putUShort( buf, offset, c );
                offset += LittleEndian.SHORT_SIZE;
            }
        }

        return buf;
    }

    @Override
    public String toString()
    {
        return "LVL: " + ( "\n" + _lvlf ).replaceAll( "\n", "\n    " )
                + "\n"
                + ( "PAPX's grpprl: " + Arrays.toString( _grpprlPapx ) + "\n" )
                + ( "CHPX's grpprl: " + Arrays.toString( _grpprlChpx ) + "\n" )
                + ( "xst: " + Arrays.toString( _xst ) + "\n" );
    }
}
