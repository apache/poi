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
 * The FibRgW97 structure is a variable-length portion of the Fib. <p>Class and
        fields descriptions are quoted from Microsoft Office Word 97-2007 Binary File Format and
        [MS-DOC] - v20110608 Word (.doc) Binary File Format
    
 * <p>
 * NOTE: This source is automatically generated please do not modify this file.  Either subclass or
 *       remove the record in src/types/definitions.
 * <p>
 * This class is internal. It content or properties may change without notice 
 * due to changes in our knowledge of internal Microsoft Word binary structures.

 * @author Sergey Vladimirov; according to Microsoft Office Word 97-2007 Binary File Format
        Specification [*.doc] and [MS-DOC] - v20110608 Word (.doc) Binary File Format
    
 */
@Internal
public abstract class FibRgW97AbstractType
{

    @Deprecated
    protected short field_1_reserved1;
    @Deprecated
    protected short field_2_reserved2;
    @Deprecated
    protected short field_3_reserved3;
    @Deprecated
    protected short field_4_reserved4;
    @Deprecated
    protected short field_5_reserved5;
    @Deprecated
    protected short field_6_reserved6;
    @Deprecated
    protected short field_7_reserved7;
    @Deprecated
    protected short field_8_reserved8;
    @Deprecated
    protected short field_9_reserved9;
    @Deprecated
    protected short field_10_reserved10;
    @Deprecated
    protected short field_11_reserved11;
    @Deprecated
    protected short field_12_reserved12;
    @Deprecated
    protected short field_13_reserved13;
    protected short field_14_lidFE;

    protected FibRgW97AbstractType()
    {
    }

    protected void fillFields( byte[] data, int offset )
    {
        field_1_reserved1              = LittleEndian.getShort( data, 0x0 + offset );
        field_2_reserved2              = LittleEndian.getShort( data, 0x2 + offset );
        field_3_reserved3              = LittleEndian.getShort( data, 0x4 + offset );
        field_4_reserved4              = LittleEndian.getShort( data, 0x6 + offset );
        field_5_reserved5              = LittleEndian.getShort( data, 0x8 + offset );
        field_6_reserved6              = LittleEndian.getShort( data, 0xa + offset );
        field_7_reserved7              = LittleEndian.getShort( data, 0xc + offset );
        field_8_reserved8              = LittleEndian.getShort( data, 0xe + offset );
        field_9_reserved9              = LittleEndian.getShort( data, 0x10 + offset );
        field_10_reserved10            = LittleEndian.getShort( data, 0x12 + offset );
        field_11_reserved11            = LittleEndian.getShort( data, 0x14 + offset );
        field_12_reserved12            = LittleEndian.getShort( data, 0x16 + offset );
        field_13_reserved13            = LittleEndian.getShort( data, 0x18 + offset );
        field_14_lidFE                 = LittleEndian.getShort( data, 0x1a + offset );
    }

    public void serialize( byte[] data, int offset )
    {
        LittleEndian.putShort( data, 0x0 + offset, field_1_reserved1 );
        LittleEndian.putShort( data, 0x2 + offset, field_2_reserved2 );
        LittleEndian.putShort( data, 0x4 + offset, field_3_reserved3 );
        LittleEndian.putShort( data, 0x6 + offset, field_4_reserved4 );
        LittleEndian.putShort( data, 0x8 + offset, field_5_reserved5 );
        LittleEndian.putShort( data, 0xa + offset, field_6_reserved6 );
        LittleEndian.putShort( data, 0xc + offset, field_7_reserved7 );
        LittleEndian.putShort( data, 0xe + offset, field_8_reserved8 );
        LittleEndian.putShort( data, 0x10 + offset, field_9_reserved9 );
        LittleEndian.putShort( data, 0x12 + offset, field_10_reserved10 );
        LittleEndian.putShort( data, 0x14 + offset, field_11_reserved11 );
        LittleEndian.putShort( data, 0x16 + offset, field_12_reserved12 );
        LittleEndian.putShort( data, 0x18 + offset, field_13_reserved13 );
        LittleEndian.putShort( data, 0x1a + offset, field_14_lidFE );
    }

    public byte[] serialize()
    {
        final byte[] result = new byte[ getSize() ];
        serialize( result, 0 );
        return result;
    }

    /**
     * Size of record
     */
    public static int getSize()
    {
        return 0 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2;
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("[FibRgW97]\n");
        builder.append("    .reserved1            = ");
        builder.append(" (").append(getReserved1()).append(" )\n");
        builder.append("    .reserved2            = ");
        builder.append(" (").append(getReserved2()).append(" )\n");
        builder.append("    .reserved3            = ");
        builder.append(" (").append(getReserved3()).append(" )\n");
        builder.append("    .reserved4            = ");
        builder.append(" (").append(getReserved4()).append(" )\n");
        builder.append("    .reserved5            = ");
        builder.append(" (").append(getReserved5()).append(" )\n");
        builder.append("    .reserved6            = ");
        builder.append(" (").append(getReserved6()).append(" )\n");
        builder.append("    .reserved7            = ");
        builder.append(" (").append(getReserved7()).append(" )\n");
        builder.append("    .reserved8            = ");
        builder.append(" (").append(getReserved8()).append(" )\n");
        builder.append("    .reserved9            = ");
        builder.append(" (").append(getReserved9()).append(" )\n");
        builder.append("    .reserved10           = ");
        builder.append(" (").append(getReserved10()).append(" )\n");
        builder.append("    .reserved11           = ");
        builder.append(" (").append(getReserved11()).append(" )\n");
        builder.append("    .reserved12           = ");
        builder.append(" (").append(getReserved12()).append(" )\n");
        builder.append("    .reserved13           = ");
        builder.append(" (").append(getReserved13()).append(" )\n");
        builder.append("    .lidFE                = ");
        builder.append(" (").append(getLidFE()).append(" )\n");

        builder.append("[/FibRgW97]\n");
        return builder.toString();
    }

    /**
     * This value is undefined and MUST be ignored.
     */
    @Internal
    public short getReserved1()
    {
        return field_1_reserved1;
    }

    /**
     * This value is undefined and MUST be ignored.
     */
    @Internal
    public void setReserved1( short field_1_reserved1 )
    {
        this.field_1_reserved1 = field_1_reserved1;
    }

    /**
     * This value is undefined and MUST be ignored.
     */
    @Internal
    public short getReserved2()
    {
        return field_2_reserved2;
    }

    /**
     * This value is undefined and MUST be ignored.
     */
    @Internal
    public void setReserved2( short field_2_reserved2 )
    {
        this.field_2_reserved2 = field_2_reserved2;
    }

    /**
     * This value is undefined and MUST be ignored.
     */
    @Internal
    public short getReserved3()
    {
        return field_3_reserved3;
    }

    /**
     * This value is undefined and MUST be ignored.
     */
    @Internal
    public void setReserved3( short field_3_reserved3 )
    {
        this.field_3_reserved3 = field_3_reserved3;
    }

    /**
     * This value is undefined and MUST be ignored.
     */
    @Internal
    public short getReserved4()
    {
        return field_4_reserved4;
    }

    /**
     * This value is undefined and MUST be ignored.
     */
    @Internal
    public void setReserved4( short field_4_reserved4 )
    {
        this.field_4_reserved4 = field_4_reserved4;
    }

    /**
     * This value is undefined and MUST be ignored.
     */
    @Internal
    public short getReserved5()
    {
        return field_5_reserved5;
    }

    /**
     * This value is undefined and MUST be ignored.
     */
    @Internal
    public void setReserved5( short field_5_reserved5 )
    {
        this.field_5_reserved5 = field_5_reserved5;
    }

    /**
     * This value is undefined and MUST be ignored.
     */
    @Internal
    public short getReserved6()
    {
        return field_6_reserved6;
    }

    /**
     * This value is undefined and MUST be ignored.
     */
    @Internal
    public void setReserved6( short field_6_reserved6 )
    {
        this.field_6_reserved6 = field_6_reserved6;
    }

    /**
     * This value is undefined and MUST be ignored.
     */
    @Internal
    public short getReserved7()
    {
        return field_7_reserved7;
    }

    /**
     * This value is undefined and MUST be ignored.
     */
    @Internal
    public void setReserved7( short field_7_reserved7 )
    {
        this.field_7_reserved7 = field_7_reserved7;
    }

    /**
     * This value is undefined and MUST be ignored.
     */
    @Internal
    public short getReserved8()
    {
        return field_8_reserved8;
    }

    /**
     * This value is undefined and MUST be ignored.
     */
    @Internal
    public void setReserved8( short field_8_reserved8 )
    {
        this.field_8_reserved8 = field_8_reserved8;
    }

    /**
     * This value is undefined and MUST be ignored.
     */
    @Internal
    public short getReserved9()
    {
        return field_9_reserved9;
    }

    /**
     * This value is undefined and MUST be ignored.
     */
    @Internal
    public void setReserved9( short field_9_reserved9 )
    {
        this.field_9_reserved9 = field_9_reserved9;
    }

    /**
     * This value is undefined and MUST be ignored.
     */
    @Internal
    public short getReserved10()
    {
        return field_10_reserved10;
    }

    /**
     * This value is undefined and MUST be ignored.
     */
    @Internal
    public void setReserved10( short field_10_reserved10 )
    {
        this.field_10_reserved10 = field_10_reserved10;
    }

    /**
     * This value is undefined and MUST be ignored.
     */
    @Internal
    public short getReserved11()
    {
        return field_11_reserved11;
    }

    /**
     * This value is undefined and MUST be ignored.
     */
    @Internal
    public void setReserved11( short field_11_reserved11 )
    {
        this.field_11_reserved11 = field_11_reserved11;
    }

    /**
     * This value is undefined and MUST be ignored.
     */
    @Internal
    public short getReserved12()
    {
        return field_12_reserved12;
    }

    /**
     * This value is undefined and MUST be ignored.
     */
    @Internal
    public void setReserved12( short field_12_reserved12 )
    {
        this.field_12_reserved12 = field_12_reserved12;
    }

    /**
     * This value is undefined and MUST be ignored.
     */
    @Internal
    public short getReserved13()
    {
        return field_13_reserved13;
    }

    /**
     * This value is undefined and MUST be ignored.
     */
    @Internal
    public void setReserved13( short field_13_reserved13 )
    {
        this.field_13_reserved13 = field_13_reserved13;
    }

    /**
     * A LID whose meaning depends on the nFib value.
     */
    @Internal
    public short getLidFE()
    {
        return field_14_lidFE;
    }

    /**
     * A LID whose meaning depends on the nFib value.
     */
    @Internal
    public void setLidFE( short field_14_lidFE )
    {
        this.field_14_lidFE = field_14_lidFE;
    }

}  // END OF CLASS
