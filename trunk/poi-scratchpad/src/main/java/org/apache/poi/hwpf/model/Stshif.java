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

import org.apache.poi.hwpf.model.types.StshifAbstractType;
import org.apache.poi.util.Internal;

/**
 * The StdfBase structure specifies general information about a style.
 */
@Internal
class Stshif extends StshifAbstractType
{

    public Stshif()
    {
    }

    public Stshif( byte[] std, int offset )
    {
        fillFields( std, offset );
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
        Stshif other = (Stshif) obj;
        if ( field_1_cstd != other.field_1_cstd )
            return false;
        if ( field_2_cbSTDBaseInFile != other.field_2_cbSTDBaseInFile )
            return false;
        if ( field_3_info3 != other.field_3_info3 )
            return false;
        if ( field_4_stiMaxWhenSaved != other.field_4_stiMaxWhenSaved )
            return false;
        if ( field_5_istdMaxFixedWhenSaved != other.field_5_istdMaxFixedWhenSaved )
            return false;
        if ( field_6_nVerBuiltInNamesWhenSaved != other.field_6_nVerBuiltInNamesWhenSaved )
            return false;
        if ( field_7_ftcAsci != other.field_7_ftcAsci )
            return false;
        if ( field_8_ftcFE != other.field_8_ftcFE )
            return false;
        if ( field_9_ftcOther != other.field_9_ftcOther )
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(field_1_cstd, field_2_cbSTDBaseInFile, field_3_info3, field_4_stiMaxWhenSaved,
            field_5_istdMaxFixedWhenSaved, field_6_nVerBuiltInNamesWhenSaved, field_7_ftcAsci, field_8_ftcFE, field_9_ftcOther);
    }

    public byte[] serialize()
    {
        byte[] result = new byte[getSize()];
        serialize( result, 0 );
        return result;
    }
}
