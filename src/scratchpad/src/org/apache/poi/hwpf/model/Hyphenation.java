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

import org.apache.poi.hwpf.model.types.HRESIAbstractType;
import org.apache.poi.hwpf.usermodel.CharacterProperties;
import org.apache.poi.util.LittleEndian;

/**
 * Hyphenation. Substructure of the {@link CharacterProperties}.
 * 
 * @author Sergey Vladimirov ( vlsergey {at} gmail {dot} com )
 */
public final class Hyphenation extends HRESIAbstractType implements Cloneable
{
    public Hyphenation()
    {
        super();
    }

    public Hyphenation( short hres )
    {
        byte[] data = new byte[2];
        LittleEndian.putShort( data, hres );
        fillFields( data, 0 );
    }

    public Hyphenation clone()
    {
        try
        {
            return (Hyphenation) super.clone();
        }
        catch ( CloneNotSupportedException e )
        {
            throw new RuntimeException( e );
        }
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
        Hyphenation other = (Hyphenation) obj;
        if ( field_1_hres != other.field_1_hres )
            return false;
        if ( field_2_chHres != other.field_2_chHres )
            return false;
        return true;
    }

    public short getValue()
    {
        byte[] data = new byte[2];
        serialize( data, 0 );
        return LittleEndian.getShort( data );
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + field_1_hres;
        result = prime * result + field_2_chHres;
        return result;
    }

    public boolean isEmpty()
    {
        return field_1_hres == 0 && field_2_chHres == 0;
    }

    @Override
    public String toString()
    {
        if ( isEmpty() )
            return "[HRESI] EMPTY";

        return super.toString();
    }
}
