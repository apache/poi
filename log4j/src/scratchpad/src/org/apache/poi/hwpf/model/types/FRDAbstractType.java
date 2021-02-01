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
package org.apache.poi.hwpf.model.types;

import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;

/**
 * Footnote Reference Descriptor (FRD).
 * <p>
 * Class and fields descriptions are quoted from Microsoft Office Word 97-2007
 * Binary File Format (.doc) Specification
 */
@Internal
public abstract class FRDAbstractType {

    protected short field_1_nAuto;

    protected FRDAbstractType() {}

    protected FRDAbstractType(FRDAbstractType other) {
        field_1_nAuto = other.field_1_nAuto;
    }

    protected void fillFields( byte[] data, int offset )
    {
        field_1_nAuto = LittleEndian.getShort( data, 0x0 + offset );
    }

    public void serialize( byte[] data, int offset )
    {
        LittleEndian.putShort( data, 0x0 + offset, field_1_nAuto );
    }

    /**
     * Size of record
     */
    public static int getSize()
    {
        return 0 + 2;
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append( "[FRD]\n" );
        builder.append( "    .nAuto                = " );
        builder.append( " (" ).append( getNAuto() ).append( " )\n" );

        builder.append( "[/FRD]\n" );
        return builder.toString();
    }

    /**
     * If > 0, the note is an automatically numbered note, otherwise it has a
     * custom mark.
     */
    public short getNAuto()
    {
        return field_1_nAuto;
    }

    /**
     * If > 0, the note is an automatically numbered note, otherwise it has a
     * custom mark.
     */
    public void setNAuto( short field_1_nAuto )
    {
        this.field_1_nAuto = field_1_nAuto;
    }

} // END OF CLASS
