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
 * Each individual style description is stored in an STD structure. <p>Class and
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
public abstract class STDAbstractType
{

    protected short field_1_info1;
    /**/private static final BitField sti = new BitField(0x0FFF);
    /**/private static final BitField fScratch = new BitField(0x1000);
    /**/private static final BitField fInvalHeight = new BitField(0x2000);
    /**/private static final BitField fHasUpe = new BitField(0x4000);
    /**/private static final BitField fMassCopy = new BitField(0x8000);
    protected short field_2_info2;
    /**/private static final BitField stk = new BitField(0x000F);
    /**/private static final BitField istdBase = new BitField(0xFFF0);
    protected short field_3_info3;
    /**/private static final BitField cupx = new BitField(0x000F);
    /**/private static final BitField istdNext = new BitField(0xFFF0);
    protected int field_4_bchUpe;
    protected short field_5_grfstd;
    /**/private static final BitField fAutoRedef = new BitField(0x0001);
    /**/private static final BitField fHidden = new BitField(0x0002);
    /**/private static final BitField f97LidsSet = new BitField(0x0004);
    /**/private static final BitField fCopyLang = new BitField(0x0008);
    /**/private static final BitField fPersonalCompose = new BitField(0x0010);
    /**/private static final BitField fPersonalReply = new BitField(0x0020);
    /**/private static final BitField fPersonal = new BitField(0x0040);
    /**/private static final BitField fNoHtmlExport = new BitField(0x0080);
    /**/private static final BitField fSemiHidden = new BitField(0x0100);
    /**/private static final BitField fLocked = new BitField(0x0200);
    /**/private static final BitField fInternalUse = new BitField(0x0400);
    /**/private static final BitField fUnhideWhenUsed = new BitField(0x0800);
    /**/private static final BitField fQFormat = new BitField(0x1000);
    /**/private static final BitField fReserved = new BitField(0xE000);

    protected STDAbstractType()
    {
    }

    protected void fillFields( byte[] data, int offset )
    {
        field_1_info1                  = LittleEndian.getShort(data, 0x0 + offset);
        field_2_info2                  = LittleEndian.getShort(data, 0x2 + offset);
        field_3_info3                  = LittleEndian.getShort(data, 0x4 + offset);
        field_4_bchUpe                 = LittleEndian.getShort(data, 0x6 + offset);
        field_5_grfstd                 = LittleEndian.getShort(data, 0x8 + offset);
    }

    public void serialize( byte[] data, int offset )
    {
        LittleEndian.putShort(data, 0x0 + offset, field_1_info1);
        LittleEndian.putShort(data, 0x2 + offset, field_2_info2);
        LittleEndian.putShort(data, 0x4 + offset, field_3_info3);
        LittleEndian.putUShort(data, 0x6 + offset, field_4_bchUpe);
        LittleEndian.putShort(data, 0x8 + offset, field_5_grfstd);
    }

    /**
     * Size of record
     */
    public static int getSize()
    {
        return 0 + 2 + 2 + 2 + 2 + 2;
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("[STD]\n");
        builder.append("    .info1                = ");
        builder.append(" (").append(getInfo1()).append(" )\n");
        builder.append("         .sti                      = ").append(getSti()).append('\n');
        builder.append("         .fScratch                 = ").append(isFScratch()).append('\n');
        builder.append("         .fInvalHeight             = ").append(isFInvalHeight()).append('\n');
        builder.append("         .fHasUpe                  = ").append(isFHasUpe()).append('\n');
        builder.append("         .fMassCopy                = ").append(isFMassCopy()).append('\n');
        builder.append("    .info2                = ");
        builder.append(" (").append(getInfo2()).append(" )\n");
        builder.append("         .stk                      = ").append(getStk()).append('\n');
        builder.append("         .istdBase                 = ").append(getIstdBase()).append('\n');
        builder.append("    .info3                = ");
        builder.append(" (").append(getInfo3()).append(" )\n");
        builder.append("         .cupx                     = ").append(getCupx()).append('\n');
        builder.append("         .istdNext                 = ").append(getIstdNext()).append('\n');
        builder.append("    .bchUpe               = ");
        builder.append(" (").append(getBchUpe()).append(" )\n");
        builder.append("    .grfstd               = ");
        builder.append(" (").append(getGrfstd()).append(" )\n");
        builder.append("         .fAutoRedef               = ").append(isFAutoRedef()).append('\n');
        builder.append("         .fHidden                  = ").append(isFHidden()).append('\n');
        builder.append("         .f97LidsSet               = ").append(isF97LidsSet()).append('\n');
        builder.append("         .fCopyLang                = ").append(isFCopyLang()).append('\n');
        builder.append("         .fPersonalCompose         = ").append(isFPersonalCompose()).append('\n');
        builder.append("         .fPersonalReply           = ").append(isFPersonalReply()).append('\n');
        builder.append("         .fPersonal                = ").append(isFPersonal()).append('\n');
        builder.append("         .fNoHtmlExport            = ").append(isFNoHtmlExport()).append('\n');
        builder.append("         .fSemiHidden              = ").append(isFSemiHidden()).append('\n');
        builder.append("         .fLocked                  = ").append(isFLocked()).append('\n');
        builder.append("         .fInternalUse             = ").append(isFInternalUse()).append('\n');
        builder.append("         .fUnhideWhenUsed          = ").append(isFUnhideWhenUsed()).append('\n');
        builder.append("         .fQFormat                 = ").append(isFQFormat()).append('\n');
        builder.append("         .fReserved                = ").append(getFReserved()).append('\n');

        builder.append("[/STD]\n");
        return builder.toString();
    }

    /**
     * Get the info1 field for the STD record.
     */
    @Internal
    public short getInfo1()
    {
        return field_1_info1;
    }

    /**
     * Set the info1 field for the STD record.
     */
    @Internal
    public void setInfo1( short field_1_info1 )
    {
        this.field_1_info1 = field_1_info1;
    }

    /**
     * Get the info2 field for the STD record.
     */
    @Internal
    public short getInfo2()
    {
        return field_2_info2;
    }

    /**
     * Set the info2 field for the STD record.
     */
    @Internal
    public void setInfo2( short field_2_info2 )
    {
        this.field_2_info2 = field_2_info2;
    }

    /**
     * Get the info3 field for the STD record.
     */
    @Internal
    public short getInfo3()
    {
        return field_3_info3;
    }

    /**
     * Set the info3 field for the STD record.
     */
    @Internal
    public void setInfo3( short field_3_info3 )
    {
        this.field_3_info3 = field_3_info3;
    }

    /**
     * An unsigned integer that specifies the size, in bytes, of std in LPStd. This value MUST be equal to cbStd in LPStd.
     */
    @Internal
    public int getBchUpe()
    {
        return field_4_bchUpe;
    }

    /**
     * An unsigned integer that specifies the size, in bytes, of std in LPStd. This value MUST be equal to cbStd in LPStd.
     */
    @Internal
    public void setBchUpe( int field_4_bchUpe )
    {
        this.field_4_bchUpe = field_4_bchUpe;
    }

    /**
     * A GRFSTD that specifies miscellaneous style properties.
     */
    @Internal
    public short getGrfstd()
    {
        return field_5_grfstd;
    }

    /**
     * A GRFSTD that specifies miscellaneous style properties.
     */
    @Internal
    public void setGrfstd( short field_5_grfstd )
    {
        this.field_5_grfstd = field_5_grfstd;
    }

    /**
     * Sets the sti field value.
     * An unsigned integer that specifies the invariant style identifier for application-defined styles, or 0x0FFE for user-defined styles
     */
    @Internal
    public void setSti( short value )
    {
        field_1_info1 = (short)sti.setValue(field_1_info1, value);
    }

    /**
     * An unsigned integer that specifies the invariant style identifier for application-defined styles, or 0x0FFE for user-defined styles
     * @return  the sti field value.
     */
    @Internal
    public short getSti()
    {
        return ( short )sti.getValue(field_1_info1);
    }

    /**
     * Sets the fScratch field value.
     * spare field for any temporary use, always reset back to zero!
     */
    @Internal
    public void setFScratch( boolean value )
    {
        field_1_info1 = (short)fScratch.setBoolean(field_1_info1, value);
    }

    /**
     * spare field for any temporary use, always reset back to zero!
     * @return  the fScratch field value.
     */
    @Internal
    public boolean isFScratch()
    {
        return fScratch.isSet(field_1_info1);
    }

    /**
     * Sets the fInvalHeight field value.
     * Specifies whether the paragraph height information in the fcPlcfPhe field of FibRgFcLcb97, for any paragraphs having this paragraph style, MUST be ignored. SHOULD be 0
     */
    @Internal
    public void setFInvalHeight( boolean value )
    {
        field_1_info1 = (short)fInvalHeight.setBoolean(field_1_info1, value);
    }

    /**
     * Specifies whether the paragraph height information in the fcPlcfPhe field of FibRgFcLcb97, for any paragraphs having this paragraph style, MUST be ignored. SHOULD be 0
     * @return  the fInvalHeight field value.
     */
    @Internal
    public boolean isFInvalHeight()
    {
        return fInvalHeight.isSet(field_1_info1);
    }

    /**
     * Sets the fHasUpe field value.
     * This bit is undefined and MUST be ignored
     */
    @Internal
    public void setFHasUpe( boolean value )
    {
        field_1_info1 = (short)fHasUpe.setBoolean(field_1_info1, value);
    }

    /**
     * This bit is undefined and MUST be ignored
     * @return  the fHasUpe field value.
     */
    @Internal
    public boolean isFHasUpe()
    {
        return fHasUpe.isSet(field_1_info1);
    }

    /**
     * Sets the fMassCopy field value.
     * This bit is undefined and MUST be ignored
     */
    @Internal
    public void setFMassCopy( boolean value )
    {
        field_1_info1 = (short)fMassCopy.setBoolean(field_1_info1, value);
    }

    /**
     * This bit is undefined and MUST be ignored
     * @return  the fMassCopy field value.
     */
    @Internal
    public boolean isFMassCopy()
    {
        return fMassCopy.isSet(field_1_info1);
    }

    /**
     * Sets the stk field value.
     * style kind
     */
    @Internal
    public void setStk( byte value )
    {
        field_2_info2 = (short)stk.setValue(field_2_info2, value);
    }

    /**
     * style kind
     * @return  the stk field value.
     */
    @Internal
    public byte getStk()
    {
        return ( byte )stk.getValue(field_2_info2);
    }

    /**
     * Sets the istdBase field value.
     * base style
     */
    @Internal
    public void setIstdBase( short value )
    {
        field_2_info2 = (short)istdBase.setValue(field_2_info2, value);
    }

    /**
     * base style
     * @return  the istdBase field value.
     */
    @Internal
    public short getIstdBase()
    {
        return ( short )istdBase.getValue(field_2_info2);
    }

    /**
     * Sets the cupx field value.
     * number of UPXs (and UPEs)
     */
    @Internal
    public void setCupx( byte value )
    {
        field_3_info3 = (short)cupx.setValue(field_3_info3, value);
    }

    /**
     * number of UPXs (and UPEs)
     * @return  the cupx field value.
     */
    @Internal
    public byte getCupx()
    {
        return ( byte )cupx.getValue(field_3_info3);
    }

    /**
     * Sets the istdNext field value.
     * next style
     */
    @Internal
    public void setIstdNext( short value )
    {
        field_3_info3 = (short)istdNext.setValue(field_3_info3, value);
    }

    /**
     * next style
     * @return  the istdNext field value.
     */
    @Internal
    public short getIstdNext()
    {
        return ( short )istdNext.getValue(field_3_info3);
    }

    /**
     * Sets the fAutoRedef field value.
     * number of UPXs (and UPEs)
     */
    @Internal
    public void setFAutoRedef( boolean value )
    {
        field_5_grfstd = (short)fAutoRedef.setBoolean(field_5_grfstd, value);
    }

    /**
     * number of UPXs (and UPEs)
     * @return  the fAutoRedef field value.
     */
    @Internal
    public boolean isFAutoRedef()
    {
        return fAutoRedef.isSet(field_5_grfstd);
    }

    /**
     * Sets the fHidden field value.
     * Specifies whether this style is not shown in the application UI
     */
    @Internal
    public void setFHidden( boolean value )
    {
        field_5_grfstd = (short)fHidden.setBoolean(field_5_grfstd, value);
    }

    /**
     * Specifies whether this style is not shown in the application UI
     * @return  the fHidden field value.
     */
    @Internal
    public boolean isFHidden()
    {
        return fHidden.isSet(field_5_grfstd);
    }

    /**
     * Sets the f97LidsSet field value.
     * Specifies whether sprmCRgLid0_80 and sprmCRgLid1_80 were applied, as appropriate, to this paragraph or character style for compatibility with applications that do not support sprmCRgLid0, sprmCRgLid1, and sprmCFNoProof
     */
    @Internal
    public void setF97LidsSet( boolean value )
    {
        field_5_grfstd = (short)f97LidsSet.setBoolean(field_5_grfstd, value);
    }

    /**
     * Specifies whether sprmCRgLid0_80 and sprmCRgLid1_80 were applied, as appropriate, to this paragraph or character style for compatibility with applications that do not support sprmCRgLid0, sprmCRgLid1, and sprmCFNoProof
     * @return  the f97LidsSet field value.
     */
    @Internal
    public boolean isF97LidsSet()
    {
        return f97LidsSet.isSet(field_5_grfstd);
    }

    /**
     * Sets the fCopyLang field value.
     * If f97LidsSet is 1, this value specifies whether the applied compatibility sprmCRgLid0_80 or sprmCRgLid1_80 specified an actual language or a special LID value (0x0400) signifying that no proofing is needed for the text. This MUST be ignored if f97LidsSet is 0
     */
    @Internal
    public void setFCopyLang( boolean value )
    {
        field_5_grfstd = (short)fCopyLang.setBoolean(field_5_grfstd, value);
    }

    /**
     * If f97LidsSet is 1, this value specifies whether the applied compatibility sprmCRgLid0_80 or sprmCRgLid1_80 specified an actual language or a special LID value (0x0400) signifying that no proofing is needed for the text. This MUST be ignored if f97LidsSet is 0
     * @return  the fCopyLang field value.
     */
    @Internal
    public boolean isFCopyLang()
    {
        return fCopyLang.isSet(field_5_grfstd);
    }

    /**
     * Sets the fPersonalCompose field value.
     * Specifies whether this character style can be used to automatically format the new message text in a new e-mail
     */
    @Internal
    public void setFPersonalCompose( boolean value )
    {
        field_5_grfstd = (short)fPersonalCompose.setBoolean(field_5_grfstd, value);
    }

    /**
     * Specifies whether this character style can be used to automatically format the new message text in a new e-mail
     * @return  the fPersonalCompose field value.
     */
    @Internal
    public boolean isFPersonalCompose()
    {
        return fPersonalCompose.isSet(field_5_grfstd);
    }

    /**
     * Sets the fPersonalReply field value.
     * Specifies whether this character style can be used to automatically format the new message text when replying to an e-mail
     */
    @Internal
    public void setFPersonalReply( boolean value )
    {
        field_5_grfstd = (short)fPersonalReply.setBoolean(field_5_grfstd, value);
    }

    /**
     * Specifies whether this character style can be used to automatically format the new message text when replying to an e-mail
     * @return  the fPersonalReply field value.
     */
    @Internal
    public boolean isFPersonalReply()
    {
        return fPersonalReply.isSet(field_5_grfstd);
    }

    /**
     * Sets the fPersonal field value.
     * Specifies whether this character style was applied to format all message text from one or more users in an e-mail
     */
    @Internal
    public void setFPersonal( boolean value )
    {
        field_5_grfstd = (short)fPersonal.setBoolean(field_5_grfstd, value);
    }

    /**
     * Specifies whether this character style was applied to format all message text from one or more users in an e-mail
     * @return  the fPersonal field value.
     */
    @Internal
    public boolean isFPersonal()
    {
        return fPersonal.isSet(field_5_grfstd);
    }

    /**
     * Sets the fNoHtmlExport field value.
     * This value MUST be 0 and MUST be ignored
     */
    @Internal
    public void setFNoHtmlExport( boolean value )
    {
        field_5_grfstd = (short)fNoHtmlExport.setBoolean(field_5_grfstd, value);
    }

    /**
     * This value MUST be 0 and MUST be ignored
     * @return  the fNoHtmlExport field value.
     */
    @Internal
    public boolean isFNoHtmlExport()
    {
        return fNoHtmlExport.isSet(field_5_grfstd);
    }

    /**
     * Sets the fSemiHidden field value.
     * Specifies whether this style is not shown in the simplified main styles UI of the application
     */
    @Internal
    public void setFSemiHidden( boolean value )
    {
        field_5_grfstd = (short)fSemiHidden.setBoolean(field_5_grfstd, value);
    }

    /**
     * Specifies whether this style is not shown in the simplified main styles UI of the application
     * @return  the fSemiHidden field value.
     */
    @Internal
    public boolean isFSemiHidden()
    {
        return fSemiHidden.isSet(field_5_grfstd);
    }

    /**
     * Sets the fLocked field value.
     * Specifies whether this style is prevented from being applied by using the application UI
     */
    @Internal
    public void setFLocked( boolean value )
    {
        field_5_grfstd = (short)fLocked.setBoolean(field_5_grfstd, value);
    }

    /**
     * Specifies whether this style is prevented from being applied by using the application UI
     * @return  the fLocked field value.
     */
    @Internal
    public boolean isFLocked()
    {
        return fLocked.isSet(field_5_grfstd);
    }

    /**
     * Sets the fInternalUse field value.
     * This bit is undefined and MUST be ignored
     */
    @Internal
    public void setFInternalUse( boolean value )
    {
        field_5_grfstd = (short)fInternalUse.setBoolean(field_5_grfstd, value);
    }

    /**
     * This bit is undefined and MUST be ignored
     * @return  the fInternalUse field value.
     */
    @Internal
    public boolean isFInternalUse()
    {
        return fInternalUse.isSet(field_5_grfstd);
    }

    /**
     * Sets the fUnhideWhenUsed field value.
     * Specifies whether the fSemiHidden property is to be set to 0 when this style is used
     */
    @Internal
    public void setFUnhideWhenUsed( boolean value )
    {
        field_5_grfstd = (short)fUnhideWhenUsed.setBoolean(field_5_grfstd, value);
    }

    /**
     * Specifies whether the fSemiHidden property is to be set to 0 when this style is used
     * @return  the fUnhideWhenUsed field value.
     */
    @Internal
    public boolean isFUnhideWhenUsed()
    {
        return fUnhideWhenUsed.isSet(field_5_grfstd);
    }

    /**
     * Sets the fQFormat field value.
     * Specifies whether this style is shown in the Ribbon Style gallery
     */
    @Internal
    public void setFQFormat( boolean value )
    {
        field_5_grfstd = (short)fQFormat.setBoolean(field_5_grfstd, value);
    }

    /**
     * Specifies whether this style is shown in the Ribbon Style gallery
     * @return  the fQFormat field value.
     */
    @Internal
    public boolean isFQFormat()
    {
        return fQFormat.isSet(field_5_grfstd);
    }

    /**
     * Sets the fReserved field value.
     * This value MUST be 0 and MUST be ignored
     */
    @Internal
    public void setFReserved( byte value )
    {
        field_5_grfstd = (short)fReserved.setValue(field_5_grfstd, value);
    }

    /**
     * This value MUST be 0 and MUST be ignored
     * @return  the fReserved field value.
     */
    @Internal
    public byte getFReserved()
    {
        return ( byte )fReserved.getValue(field_5_grfstd);
    }

}  // END OF CLASS
