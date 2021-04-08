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

import org.apache.poi.hwpf.model.types.StdfBaseAbstractType;
import org.apache.poi.util.Internal;

/**
 * The StdfBase structure specifies general information about a style.
 */
@Internal
class StdfBase extends StdfBaseAbstractType
{

    public StdfBase()
    {
    }

    public StdfBase( byte[] std, int offset )
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
        StdfBase other = (StdfBase) obj;
        if ( field_1_info1 != other.field_1_info1 )
            return false;
        if ( field_2_info2 != other.field_2_info2 )
            return false;
        if ( field_3_info3 != other.field_3_info3 )
            return false;
        if ( field_4_bchUpe != other.field_4_bchUpe )
            return false;
        if ( field_5_grfstd != other.field_5_grfstd )
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(field_1_info1, field_2_info2, field_3_info3, field_4_bchUpe, field_5_grfstd);
    }

    public byte[] serialize()
    {
        byte[] result = new byte[getSize()];
        serialize( result, 0 );
        return result;
    }
}
