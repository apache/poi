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

import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;

/**
 * 24-bit color structure
 * 
 * @author Sergey Vladimirov (vlsergey {at} gmail {dot} com)
 */
@Internal
public class Colorref implements Cloneable
{
    public static Colorref valueOfIco( int ico )
    {

        switch ( ico )
        {
        case 1:
            return new Colorref( 0x00000000 );
        case 2:
            return new Colorref( 0x00FF0000 );
        case 3:
            return new Colorref( 0x00FFFF00 );
        case 4:
            return new Colorref( 0x0000FF00 );
        case 5:
            return new Colorref( 0x00FF00FF );
        case 6:
            return new Colorref( 0x000000FF );
        case 7:
            return new Colorref( 0x0000FFFF );
        case 8:
            return new Colorref( 0x00FFFFFF );
        case 9:
            return new Colorref( 0x008B0000 );
        case 10:
            return new Colorref( 0x008B8B00 );
        case 11:
            return new Colorref( 0x00006400 );
        case 12:
            return new Colorref( 0x008B008B );
        case 13:
            return new Colorref( 0x0000008B );
        case 14:
            return new Colorref( 0x0000CCFF );
        case 15:
            return new Colorref( 0x00A9A9A9 );
        case 16:
            return new Colorref( 0x00C0C0C0 );
        default:
            return new Colorref( 0x00000000 );
        }
    }

    private int value;

    public Colorref()
    {
        this.value = -1;
    }

    public Colorref( byte[] data, int offset )
    {
        this.value = LittleEndian.getInt( data, offset );
    }

    public Colorref( int value )
    {
        this.value = value;
    }

    @Override
    public Colorref clone() throws CloneNotSupportedException
    {
        return new Colorref( this.value );
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
        Colorref other = (Colorref) obj;
        if ( value != other.value )
            return false;
        return true;
    }

    public int getValue()
    {
        return value;
    }

    @Override
    public int hashCode()
    {
        return value;
    }

    public boolean isEmpty()
    {
        return value == -1;
    }

    public void serialize( byte[] data, int offset )
    {
        LittleEndian.putInt( data, offset, this.value );
    }

    public void setValue( int value )
    {
        this.value = value;
    }

    public byte[] toByteArray()
    {
        if ( isEmpty() )
            throw new IllegalStateException(
                    "Structure state (EMPTY) is not good for serialization" );

        byte[] bs = new byte[4];
        serialize( bs, 0 );
        return bs;
    }

    @Override
    public String toString()
    {
        if ( isEmpty() )
            return "[COLORREF] EMPTY";

        return "[COLORREF] 0x" + Integer.toHexString( value );
    }
}
