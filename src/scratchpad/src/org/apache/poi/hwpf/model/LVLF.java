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

import java.util.Arrays;

import org.apache.poi.hwpf.model.types.LVLFAbstractType;
import org.apache.poi.util.Internal;

/**
 * The LVLF structure contains formatting properties for an individual level in
 * a list
 * 
 * @author Sergey Vladimirov; according to Microsoft Office Word 97-2007 Binary
 *         File Format Specification [*.doc] and [MS-DOC] - v20110608 Word
 *         (.doc) Binary File Format
 */
@Internal
class LVLF extends LVLFAbstractType
{

    public LVLF()
    {
    }

    public LVLF( byte[] std, int offset )
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
        LVLF other = (LVLF) obj;
        if ( field_10_grfhic != other.field_10_grfhic )
            return false;
        if ( field_1_iStartAt != other.field_1_iStartAt )
            return false;
        if ( field_2_info2 != other.field_2_info2 )
            return false;
        if ( !Arrays.equals( field_3_rgbxchNums, other.field_3_rgbxchNums ) )
            return false;
        if ( field_4_ixchFollow != other.field_4_ixchFollow )
            return false;
        if ( field_5_dxaIndentSav != other.field_5_dxaIndentSav )
            return false;
        if ( field_6_unused2 != other.field_6_unused2 )
            return false;
        if ( field_7_cbGrpprlChpx != other.field_7_cbGrpprlChpx )
            return false;
        if ( field_8_cbGrpprlPapx != other.field_8_cbGrpprlPapx )
            return false;
        if ( field_9_ilvlRestartLim != other.field_9_ilvlRestartLim )
            return false;
        return true;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + field_10_grfhic;
        result = prime * result + field_1_iStartAt;
        result = prime * result + field_2_info2;
        result = prime * result + Arrays.hashCode( field_3_rgbxchNums );
        result = prime * result + field_4_ixchFollow;
        result = prime * result + field_5_dxaIndentSav;
        result = prime * result + field_6_unused2;
        result = prime * result + field_7_cbGrpprlChpx;
        result = prime * result + field_8_cbGrpprlPapx;
        result = prime * result + field_9_ilvlRestartLim;
        return result;
    }

}
