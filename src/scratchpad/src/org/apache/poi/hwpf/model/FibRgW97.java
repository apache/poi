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

import org.apache.poi.hwpf.model.types.FibRgW97AbstractType;
import org.apache.poi.util.Internal;

/**
 * The FibRgW97 structure is a variable-length portion of the Fib.
 * <p>
 * Class and fields descriptions are quoted from Microsoft Office Word 97-2007
 * Binary File Format and [MS-DOC] - v20110608 Word (.doc) Binary File Format.
 * 
 * @author Sergey Vladimirov; according to Microsoft Office Word 97-2007 Binary
 *         File Format Specification [*.doc] and [MS-DOC] - v20110608 Word
 *         (.doc) Binary File Format
 */
@Internal
class FibRgW97 extends FibRgW97AbstractType
{

    public FibRgW97()
    {
    }

    public FibRgW97( byte[] std, int offset )
    {
        fillFields( std, offset );
    }

    @Override
    @SuppressWarnings( "deprecation" )
    public boolean equals( Object obj )
    {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        FibRgW97 other = (FibRgW97) obj;
        if ( field_10_reserved10 != other.field_10_reserved10 )
            return false;
        if ( field_11_reserved11 != other.field_11_reserved11 )
            return false;
        if ( field_12_reserved12 != other.field_12_reserved12 )
            return false;
        if ( field_13_reserved13 != other.field_13_reserved13 )
            return false;
        if ( field_14_lidFE != other.field_14_lidFE )
            return false;
        if ( field_1_reserved1 != other.field_1_reserved1 )
            return false;
        if ( field_2_reserved2 != other.field_2_reserved2 )
            return false;
        if ( field_3_reserved3 != other.field_3_reserved3 )
            return false;
        if ( field_4_reserved4 != other.field_4_reserved4 )
            return false;
        if ( field_5_reserved5 != other.field_5_reserved5 )
            return false;
        if ( field_6_reserved6 != other.field_6_reserved6 )
            return false;
        if ( field_7_reserved7 != other.field_7_reserved7 )
            return false;
        if ( field_8_reserved8 != other.field_8_reserved8 )
            return false;
        if ( field_9_reserved9 != other.field_9_reserved9 )
            return false;
        return true;
    }

    @Override
    @SuppressWarnings( "deprecation" )
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + field_10_reserved10;
        result = prime * result + field_11_reserved11;
        result = prime * result + field_12_reserved12;
        result = prime * result + field_13_reserved13;
        result = prime * result + field_14_lidFE;
        result = prime * result + field_1_reserved1;
        result = prime * result + field_2_reserved2;
        result = prime * result + field_3_reserved3;
        result = prime * result + field_4_reserved4;
        result = prime * result + field_5_reserved5;
        result = prime * result + field_6_reserved6;
        result = prime * result + field_7_reserved7;
        result = prime * result + field_8_reserved8;
        result = prime * result + field_9_reserved9;
        return result;
    }

}
