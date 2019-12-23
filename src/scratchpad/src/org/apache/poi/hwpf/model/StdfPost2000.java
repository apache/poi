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

import org.apache.poi.hwpf.model.types.StdfPost2000AbstractType;
import org.apache.poi.util.Internal;

/**
 * The StdfBase structure specifies general information about a style.
 */
@Internal
class StdfPost2000 extends StdfPost2000AbstractType
{

    public StdfPost2000()
    {
    }

    public StdfPost2000( byte[] std, int offset )
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
        StdfPost2000 other = (StdfPost2000) obj;
        if ( field_1_info1 != other.field_1_info1 )
            return false;
        if ( field_2_rsid != other.field_2_rsid )
            return false;
        if ( field_3_info3 != other.field_3_info3 )
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(field_1_info1, field_2_rsid, field_3_info3);
    }

    public byte[] serialize()
    {
        byte[] result = new byte[getSize()];
        serialize( result, 0 );
        return result;
    }
}
