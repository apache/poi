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

package org.apache.poi.hwpf.usermodel;

import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.LittleEndian;

/**
 * This data structure is used by a paragraph to determine how it should drop
 * its first letter. I think its the visual effect that will show a giant first
 * letter to a paragraph. I've seen this used in the first paragraph of a book
 * 
 * @author Ryan Ackley
 */
public final class DropCapSpecifier
{
    private short _fdct;
        private static BitField _lines = BitFieldFactory.getInstance( 0xf8 );
        private static BitField _type = BitFieldFactory.getInstance( 0x07 );

    public DropCapSpecifier()
    {
        this._fdct = 0;
    }

    public DropCapSpecifier( byte[] buf, int offset )
    {
        this( LittleEndian.getShort( buf, offset ) );
    }

    public DropCapSpecifier( short fdct )
    {
        this._fdct = fdct;
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
        DropCapSpecifier other = (DropCapSpecifier) obj;
        if ( _fdct != other._fdct )
            return false;
        return true;
    }

    public byte getCountOfLinesToDrop()
    {
        return (byte) _lines.getValue( _fdct );
    }

    public byte getDropCapType()
    {
        return (byte) _type.getValue( _fdct );
    }

    @Override
    public int hashCode()
    {
        return _fdct;
    }

    public boolean isEmpty()
    {
        return _fdct == 0;
    }

    public void setCountOfLinesToDrop( byte value )
    {
        _fdct = (short) _lines.setValue( _fdct, value );
    }

    public void setDropCapType( byte value )
    {
        _fdct = (short) _type.setValue( _fdct, value );
    }

    public short toShort()
    {
        return _fdct;
    }

    @Override
    public String toString()
    {
        if ( isEmpty() )
            return "[DCS] EMPTY";

        return "[DCS] (type: " + getDropCapType() + "; count: "
                + getCountOfLinesToDrop() + ")";
    }
}
