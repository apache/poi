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

import java.util.Objects;

import org.apache.poi.common.Duplicatable;
import org.apache.poi.util.BitField;
import org.apache.poi.util.Internal;

@Internal
public final class PropertyModifier implements Duplicatable {
    /**
     * <li>"Set to 0 for variant 1" <li>"Set to 1 for variant 2"
     */
    private static final BitField _fComplex = new BitField( 0x0001 );

    /**
     * "Index to a grpprl stored in CLX portion of file"
     */
    private static final BitField _figrpprl = new BitField( 0xfffe );

    /**
     * "Index to entry into rgsprmPrm"
     */
    private static final BitField _fisprm = new BitField( 0x00fe );

    /**
     * "sprm's operand"
     */
    private static final BitField _fval = new BitField( 0xff00 );

    private short value;

    public PropertyModifier( short value ) {
        this.value = value;
    }

    public PropertyModifier( PropertyModifier other ) {
        value = other.value;
    }

    @Override
    public PropertyModifier copy() {
        return new PropertyModifier(this);
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
        PropertyModifier other = (PropertyModifier) obj;
        if ( value != other.value )
            return false;
        return true;
    }

    /**
     * "Index to a grpprl stored in CLX portion of file"
     */
    public short getIgrpprl()
    {
        if ( !isComplex() )
            throw new IllegalStateException( "Not complex" );

        return _figrpprl.getShortValue( value );
    }

    public short getIsprm()
    {
        if ( isComplex() )
            throw new IllegalStateException( "Not simple" );

        return _fisprm.getShortValue( value );
    }

    public short getVal()
    {
        if ( isComplex() )
            throw new IllegalStateException( "Not simple" );

        return _fval.getShortValue( value );
    }

    public short getValue()
    {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    public boolean isComplex()
    {
        return _fComplex.isSet( value );
    }

    @Override
    public String toString()
    {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append( "[PRM] (complex: " );
        stringBuilder.append( isComplex() );
        stringBuilder.append( "; " );
        if ( isComplex() )
        {
            stringBuilder.append( "igrpprl: " );
            stringBuilder.append( getIgrpprl() );
            stringBuilder.append( "; " );
        }
        else
        {
            stringBuilder.append( "isprm: " );
            stringBuilder.append( getIsprm() );
            stringBuilder.append( "; " );
            stringBuilder.append( "val: " );
            stringBuilder.append( getVal() );
            stringBuilder.append( "; " );
        }
        stringBuilder.append( ")" );
        return stringBuilder.toString();
    }
}
