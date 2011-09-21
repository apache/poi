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

import org.apache.poi.hwpf.model.types.LFOAbstractType;

/**
 * "The LFO structure specifies the LSTF element that corresponds to a list that
 * contains a paragraph. An LFO can also specify formatting information that
 * overrides the LSTF element to which it corresponds." -- [MS-DOC] -- v20110315
 * Word (.doc) Binary File Format
 * 
 * @author Sergey Vladimirov (vlsergey {at} gmail {dot} com)
 */
@Internal
class LFO extends LFOAbstractType
{

    public LFO()
    {
    }

    public LFO( byte[] std, int offset )
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
        LFO other = (LFO) obj;
        if ( field_1_lsid != other.field_1_lsid )
            return false;
        if ( field_2_reserved1 != other.field_2_reserved1 )
            return false;
        if ( field_3_reserved2 != other.field_3_reserved2 )
            return false;
        if ( field_4_clfolvl != other.field_4_clfolvl )
            return false;
        if ( field_5_ibstFltAutoNum != other.field_5_ibstFltAutoNum )
            return false;
        if ( field_6_grfhic != other.field_6_grfhic )
            return false;
        if ( field_7_reserved3 != other.field_7_reserved3 )
            return false;
        return true;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + field_1_lsid;
        result = prime * result + field_2_reserved1;
        result = prime * result + field_3_reserved2;
        result = prime * result + field_4_clfolvl;
        result = prime * result + field_5_ibstFltAutoNum;
        result = prime * result + field_6_grfhic;
        result = prime * result + field_7_reserved3;
        return result;
    }
}
