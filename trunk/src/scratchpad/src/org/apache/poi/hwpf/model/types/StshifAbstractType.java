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


import org.apache.poi.util.BitField;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;

/**
 * The Stshif structure specifies general stylesheet information. <p>Class and
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
public abstract class StshifAbstractType
{

    protected int field_1_cstd;
    protected int field_2_cbSTDBaseInFile;
    protected int field_3_info3;
    /**/private static final BitField fHasOriginalStyle = new BitField(0x0001);
    /**/private static final BitField fReserved = new BitField(0xFFFE);
    protected int field_4_stiMaxWhenSaved;
    protected int field_5_istdMaxFixedWhenSaved;
    protected int field_6_nVerBuiltInNamesWhenSaved;
    protected short field_7_ftcAsci;
    protected short field_8_ftcFE;
    protected short field_9_ftcOther;

    protected StshifAbstractType()
    {
    }

    protected void fillFields( byte[] data, int offset )
    {
        field_1_cstd                   = LittleEndian.getShort(data, 0x0 + offset);
        field_2_cbSTDBaseInFile        = LittleEndian.getShort(data, 0x2 + offset);
        field_3_info3                  = LittleEndian.getShort(data, 0x4 + offset);
        field_4_stiMaxWhenSaved        = LittleEndian.getShort(data, 0x6 + offset);
        field_5_istdMaxFixedWhenSaved  = LittleEndian.getShort(data, 0x8 + offset);
        field_6_nVerBuiltInNamesWhenSaved = LittleEndian.getShort(data, 0xa + offset);
        field_7_ftcAsci                = LittleEndian.getShort(data, 0xc + offset);
        field_8_ftcFE                  = LittleEndian.getShort(data, 0xe + offset);
        field_9_ftcOther               = LittleEndian.getShort(data, 0x10 + offset);
    }

    public void serialize( byte[] data, int offset )
    {
        LittleEndian.putUShort(data, 0x0 + offset, field_1_cstd);
        LittleEndian.putUShort(data, 0x2 + offset, field_2_cbSTDBaseInFile);
        LittleEndian.putUShort(data, 0x4 + offset, field_3_info3);
        LittleEndian.putUShort(data, 0x6 + offset, field_4_stiMaxWhenSaved);
        LittleEndian.putUShort(data, 0x8 + offset, field_5_istdMaxFixedWhenSaved);
        LittleEndian.putUShort(data, 0xa + offset, field_6_nVerBuiltInNamesWhenSaved);
        LittleEndian.putShort(data, 0xc + offset, field_7_ftcAsci);
        LittleEndian.putShort(data, 0xe + offset, field_8_ftcFE);
        LittleEndian.putShort(data, 0x10 + offset, field_9_ftcOther);
    }

    /**
     * Size of record
     */
    public static int getSize()
    {
        return 0 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2;
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("[Stshif]\n");
        builder.append("    .cstd                 = ");
        builder.append(" (").append(getCstd()).append(" )\n");
        builder.append("    .cbSTDBaseInFile      = ");
        builder.append(" (").append(getCbSTDBaseInFile()).append(" )\n");
        builder.append("    .info3                = ");
        builder.append(" (").append(getInfo3()).append(" )\n");
        builder.append("         .fHasOriginalStyle        = ").append(isFHasOriginalStyle()).append('\n');
        builder.append("         .fReserved                = ").append(getFReserved()).append('\n');
        builder.append("    .stiMaxWhenSaved      = ");
        builder.append(" (").append(getStiMaxWhenSaved()).append(" )\n");
        builder.append("    .istdMaxFixedWhenSaved = ");
        builder.append(" (").append(getIstdMaxFixedWhenSaved()).append(" )\n");
        builder.append("    .nVerBuiltInNamesWhenSaved = ");
        builder.append(" (").append(getNVerBuiltInNamesWhenSaved()).append(" )\n");
        builder.append("    .ftcAsci              = ");
        builder.append(" (").append(getFtcAsci()).append(" )\n");
        builder.append("    .ftcFE                = ");
        builder.append(" (").append(getFtcFE()).append(" )\n");
        builder.append("    .ftcOther             = ");
        builder.append(" (").append(getFtcOther()).append(" )\n");

        builder.append("[/Stshif]\n");
        return builder.toString();
    }

    /**
     * An unsigned integer that specifies the count of elements in STSH.rglpstd. This value MUST be equal to or greater than 0x000F, and MUST be less than 0x0FFE.
     */
    @Internal
    public int getCstd()
    {
        return field_1_cstd;
    }

    /**
     * An unsigned integer that specifies the count of elements in STSH.rglpstd. This value MUST be equal to or greater than 0x000F, and MUST be less than 0x0FFE.
     */
    @Internal
    public void setCstd( int field_1_cstd )
    {
        this.field_1_cstd = field_1_cstd;
    }

    /**
     * An unsigned integer that specifies the size, in bytes, of the Stdf structure. The Stdf structure contains an StdfBase structure that is followed by a StdfPost2000OrNone structure which contains an optional StdfPost2000 structure. This value MUST be 0x000A when the Stdf structure does not contain an StdfPost2000 structure and MUST be 0x0012 when the Stdf structure does contain an StdfPost2000 structure..
     */
    @Internal
    public int getCbSTDBaseInFile()
    {
        return field_2_cbSTDBaseInFile;
    }

    /**
     * An unsigned integer that specifies the size, in bytes, of the Stdf structure. The Stdf structure contains an StdfBase structure that is followed by a StdfPost2000OrNone structure which contains an optional StdfPost2000 structure. This value MUST be 0x000A when the Stdf structure does not contain an StdfPost2000 structure and MUST be 0x0012 when the Stdf structure does contain an StdfPost2000 structure..
     */
    @Internal
    public void setCbSTDBaseInFile( int field_2_cbSTDBaseInFile )
    {
        this.field_2_cbSTDBaseInFile = field_2_cbSTDBaseInFile;
    }

    /**
     * Get the info3 field for the Stshif record.
     */
    @Internal
    public int getInfo3()
    {
        return field_3_info3;
    }

    /**
     * Set the info3 field for the Stshif record.
     */
    @Internal
    public void setInfo3( int field_3_info3 )
    {
        this.field_3_info3 = field_3_info3;
    }

    /**
     * An unsigned integer that specifies a value that is 1 larger than the largest StdfBase.sti index of any application-defined style. This SHOULD be equal to the largest sti index that is defined in the application, incremented by 1.
     */
    @Internal
    public int getStiMaxWhenSaved()
    {
        return field_4_stiMaxWhenSaved;
    }

    /**
     * An unsigned integer that specifies a value that is 1 larger than the largest StdfBase.sti index of any application-defined style. This SHOULD be equal to the largest sti index that is defined in the application, incremented by 1.
     */
    @Internal
    public void setStiMaxWhenSaved( int field_4_stiMaxWhenSaved )
    {
        this.field_4_stiMaxWhenSaved = field_4_stiMaxWhenSaved;
    }

    /**
     * An unsigned integer that specifies the count of elements at the start of STSH.rglpstd that are reserved for fixed-index application-defined styles. This value MUST be 0x000F.
     */
    @Internal
    public int getIstdMaxFixedWhenSaved()
    {
        return field_5_istdMaxFixedWhenSaved;
    }

    /**
     * An unsigned integer that specifies the count of elements at the start of STSH.rglpstd that are reserved for fixed-index application-defined styles. This value MUST be 0x000F.
     */
    @Internal
    public void setIstdMaxFixedWhenSaved( int field_5_istdMaxFixedWhenSaved )
    {
        this.field_5_istdMaxFixedWhenSaved = field_5_istdMaxFixedWhenSaved;
    }

    /**
     * An unsigned integer that specifies the version number of the style names as defined by the application that writes the file.
     */
    @Internal
    public int getNVerBuiltInNamesWhenSaved()
    {
        return field_6_nVerBuiltInNamesWhenSaved;
    }

    /**
     * An unsigned integer that specifies the version number of the style names as defined by the application that writes the file.
     */
    @Internal
    public void setNVerBuiltInNamesWhenSaved( int field_6_nVerBuiltInNamesWhenSaved )
    {
        this.field_6_nVerBuiltInNamesWhenSaved = field_6_nVerBuiltInNamesWhenSaved;
    }

    /**
     * A signed integer that specifies an operand value for the sprmCRgFtc0 for default document formatting.
     */
    @Internal
    public short getFtcAsci()
    {
        return field_7_ftcAsci;
    }

    /**
     * A signed integer that specifies an operand value for the sprmCRgFtc0 for default document formatting.
     */
    @Internal
    public void setFtcAsci( short field_7_ftcAsci )
    {
        this.field_7_ftcAsci = field_7_ftcAsci;
    }

    /**
     * A signed integer that specifies an operand value for the sprmCRgFtc1 for default document formatting, as defined in the section Determining Formatting Properties.
     */
    @Internal
    public short getFtcFE()
    {
        return field_8_ftcFE;
    }

    /**
     * A signed integer that specifies an operand value for the sprmCRgFtc1 for default document formatting, as defined in the section Determining Formatting Properties.
     */
    @Internal
    public void setFtcFE( short field_8_ftcFE )
    {
        this.field_8_ftcFE = field_8_ftcFE;
    }

    /**
     * A signed integer that specifies an operand value for the sprmCRgFtc2 for default document formatting, as defined in the section Determining Formatting Properties.
     */
    @Internal
    public short getFtcOther()
    {
        return field_9_ftcOther;
    }

    /**
     * A signed integer that specifies an operand value for the sprmCRgFtc2 for default document formatting, as defined in the section Determining Formatting Properties.
     */
    @Internal
    public void setFtcOther( short field_9_ftcOther )
    {
        this.field_9_ftcOther = field_9_ftcOther;
    }

    /**
     * Sets the fHasOriginalStyle field value.
     * This value MUST be 1 and MUST be ignored
     */
    @Internal
    public void setFHasOriginalStyle( boolean value )
    {
        field_3_info3 = fHasOriginalStyle.setBoolean(field_3_info3, value);
    }

    /**
     * This value MUST be 1 and MUST be ignored
     * @return  the fHasOriginalStyle field value.
     */
    @Internal
    public boolean isFHasOriginalStyle()
    {
        return fHasOriginalStyle.isSet(field_3_info3);
    }

    /**
     * Sets the fReserved field value.
     * This value MUST be zero and MUST be ignored
     */
    @Internal
    public void setFReserved( short value )
    {
        field_3_info3 = fReserved.setValue(field_3_info3, value);
    }

    /**
     * This value MUST be zero and MUST be ignored
     * @return  the fReserved field value.
     */
    @Internal
    public short getFReserved()
    {
        return ( short )fReserved.getValue(field_3_info3);
    }

}  // END OF CLASS
