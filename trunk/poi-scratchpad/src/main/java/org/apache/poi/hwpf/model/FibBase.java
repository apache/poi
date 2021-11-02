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

import org.apache.poi.hwpf.model.types.FibBaseAbstractType;
import org.apache.poi.util.Internal;

/**
 * Base part of the File information Block (FibBase). Holds the core part of the
 * FIB, from the first 32 bytes.
 * <p>
 * Class and fields descriptions are quoted from Microsoft Office Word 97-2007
 * Binary File Format and [MS-DOC] - v20110608 Word (.doc) Binary File Format.
 */
@Internal
public class FibBase extends FibBaseAbstractType {

    public FibBase() {
    }

    public FibBase( byte[] std, int offset ) {
        fillFields( std, offset );
    }

    @Override
    @SuppressWarnings( "deprecation" )
    public boolean equals( Object obj ) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        FibBase other = (FibBase) obj;
        if ( field_10_flags2 != other.field_10_flags2 )
            return false;
        if ( field_11_Chs != other.field_11_Chs )
            return false;
        if ( field_12_chsTables != other.field_12_chsTables )
            return false;
        if ( field_13_fcMin != other.field_13_fcMin )
            return false;
        if ( field_14_fcMac != other.field_14_fcMac )
            return false;
        if ( field_1_wIdent != other.field_1_wIdent )
            return false;
        if ( field_2_nFib != other.field_2_nFib )
            return false;
        if ( field_3_unused != other.field_3_unused )
            return false;
        if ( field_4_lid != other.field_4_lid )
            return false;
        if ( field_5_pnNext != other.field_5_pnNext )
            return false;
        if ( field_6_flags1 != other.field_6_flags1 )
            return false;
        if ( field_7_nFibBack != other.field_7_nFibBack )
            return false;
        if ( field_8_lKey != other.field_8_lKey )
            return false;
        if ( field_9_envr != other.field_9_envr )
            return false;
        return true;
    }

    @Override
    @SuppressWarnings( "deprecation" )
    public int hashCode() {
        return Objects.hash(field_1_wIdent, field_2_nFib, field_3_unused, field_4_lid, field_5_pnNext, field_6_flags1,
            field_7_nFibBack, field_8_lKey, field_9_envr, field_10_flags2, field_11_Chs, field_12_chsTables,
            field_13_fcMin, field_14_fcMac);
    }
}
