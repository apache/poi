/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */
package org.apache.poi.hwpf.model;

import java.util.Arrays;

import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianConsts;

/**
 * The Xst structure is a string. The string is prepended by its length and is not null-terminated.
 */
public class Xst
{

    /**
     * An unsigned integer that specifies the number of characters that are
     * contained in the rgtchar array.
     */
    private int _cch;

    /**
     * An array of 16-bit Unicode characters that make up a string.
     */
    private char[] _rgtchar;

    public Xst()
    {
        _cch = 0;
        _rgtchar = new char[0];
    }

    public Xst( byte[] data, int startOffset )
    {
        int offset = startOffset;

        _cch = LittleEndian.getUShort( data, offset );
        offset += LittleEndianConsts.SHORT_SIZE;

        _rgtchar = new char[_cch];
        for ( int x = 0; x < _cch; x++ )
        {
            _rgtchar[x] = (char) LittleEndian.getShort( data, offset );
            offset += LittleEndianConsts.SHORT_SIZE;
        }

    }

    public Xst( String str )
    {
        _cch = str.length();
        _rgtchar = str.toCharArray();
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        Xst other = (Xst) obj;
        if ( _cch != other._cch )
            return false;
        if ( !Arrays.equals( _rgtchar, other._rgtchar ) )
            return false;
        return true;
    }

    public String getAsJavaString()
    {
        return new String( _rgtchar );
    }

    /**
     * An unsigned integer that specifies the number of characters that are
     * contained in the rgtchar array.
     */
    public int getCch()
    {
        return _cch;
    }

    /**
     * An array of 16-bit Unicode characters that make up a string.
     */
    public char[] getRgtchar()
    {
        return _rgtchar;
    }

    public int getSize()
    {
        return LittleEndianConsts.SHORT_SIZE + _rgtchar.length * 2;
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(new Object[]{_cch,_rgtchar});
    }

    public void serialize( byte[] data, int startOffset )
    {
        int offset = startOffset;

        LittleEndian.putUShort( data, offset, _cch );
        offset += LittleEndianConsts.SHORT_SIZE;

        for ( char c : _rgtchar )
        {
            LittleEndian.putShort( data, offset, (short) c );
            offset += LittleEndianConsts.SHORT_SIZE;
        }
    }

    @Override
    public String toString()
    {
        return "Xst [" + _cch + "; " + Arrays.toString(_rgtchar) + "]";
    }
}
