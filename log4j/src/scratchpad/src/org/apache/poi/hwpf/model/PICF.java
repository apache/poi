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

import org.apache.poi.hwpf.model.types.PICFAbstractType;
import org.apache.poi.util.Internal;

/**
 * The PICF structure specifies the type of a picture, as well as the size of
 * the picture and information about its border.
 * <p>
 * Class and fields descriptions are quoted from Microsoft Office Word 97-2007
 * Binary File Format and [MS-DOC] - v20110608 Word (.doc) Binary File Format
 *
 * @author Sergey Vladimirov (vlsergey {at} gmail {dot} com)
 */
@Internal
public class PICF extends PICFAbstractType
{

    public PICF()
    {
    }

    public PICF( byte[] std, int offset )
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
        PICF other = (PICF) obj;
        if ( field_10_padding2 != other.field_10_padding2 )
            return false;
        if ( field_11_dxaGoal != other.field_11_dxaGoal )
            return false;
        if ( field_12_dyaGoal != other.field_12_dyaGoal )
            return false;
        if ( field_13_mx != other.field_13_mx )
            return false;
        if ( field_14_my != other.field_14_my )
            return false;
        if ( field_15_dxaReserved1 != other.field_15_dxaReserved1 )
            return false;
        if ( field_16_dyaReserved1 != other.field_16_dyaReserved1 )
            return false;
        if ( field_17_dxaReserved2 != other.field_17_dxaReserved2 )
            return false;
        if ( field_18_dyaReserved2 != other.field_18_dyaReserved2 )
            return false;
        if ( field_19_fReserved != other.field_19_fReserved )
            return false;
        if ( field_1_lcb != other.field_1_lcb )
            return false;
        if ( field_20_bpp != other.field_20_bpp )
            return false;
        if ( !Arrays.equals( field_21_brcTop80, other.field_21_brcTop80 ) )
            return false;
        if ( !Arrays.equals( field_22_brcLeft80, other.field_22_brcLeft80 ) )
            return false;
        if ( !Arrays.equals( field_23_brcBottom80, other.field_23_brcBottom80 ) )
            return false;
        if ( !Arrays.equals( field_24_brcRight80, other.field_24_brcRight80 ) )
            return false;
        if ( field_25_dxaReserved3 != other.field_25_dxaReserved3 )
            return false;
        if ( field_26_dyaReserved3 != other.field_26_dyaReserved3 )
            return false;
        if ( field_27_cProps != other.field_27_cProps )
            return false;
        if ( field_2_cbHeader != other.field_2_cbHeader )
            return false;
        if ( field_3_mm != other.field_3_mm )
            return false;
        if ( field_4_xExt != other.field_4_xExt )
            return false;
        if ( field_5_yExt != other.field_5_yExt )
            return false;
        if ( field_6_swHMF != other.field_6_swHMF )
            return false;
        if ( field_7_grf != other.field_7_grf )
            return false;
        if ( field_8_padding != other.field_8_padding )
            return false;
        if ( field_9_mmPM != other.field_9_mmPM )
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(new Object[]{field_1_lcb, field_2_cbHeader, field_3_mm, field_4_xExt, field_5_yExt,
            field_6_swHMF, field_7_grf, field_8_padding, field_9_mmPM, field_10_padding2, field_11_dxaGoal,
            field_12_dyaGoal, field_13_mx, field_14_my, field_15_dxaReserved1, field_16_dyaReserved1, field_17_dxaReserved2,
            field_18_dyaReserved2, field_19_fReserved, field_20_bpp, field_21_brcTop80, field_22_brcLeft80,
            field_23_brcBottom80, field_24_brcRight80, field_25_dxaReserved3, field_26_dyaReserved3, field_27_cProps});
    }

}
