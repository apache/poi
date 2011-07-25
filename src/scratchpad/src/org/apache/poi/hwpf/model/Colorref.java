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
        LittleEndian.putInt( bs, 0, this.value );
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
