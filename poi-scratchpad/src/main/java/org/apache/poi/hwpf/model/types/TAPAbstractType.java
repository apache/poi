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


import java.util.Arrays;
import java.util.stream.Stream;

import org.apache.poi.hwpf.usermodel.BorderCode;
import org.apache.poi.hwpf.usermodel.ShadingDescriptor;
import org.apache.poi.hwpf.usermodel.TableAutoformatLookSpecifier;
import org.apache.poi.hwpf.usermodel.TableCellDescriptor;
import org.apache.poi.util.BitField;
import org.apache.poi.util.Internal;

/**
 * Table Properties
 */
@SuppressWarnings("unused")
@Internal
public abstract class TAPAbstractType {
    private static final BitField fAutofit = new BitField(0x00000001);
    private static final BitField fKeepFollow = new BitField(0x00000002);
    private static final BitField ftsWidth = new BitField(0x0000001c);
    private static final BitField ftsWidthIndent = new BitField(0x000000e0);
    private static final BitField ftsWidthBefore = new BitField(0x00000700);
    private static final BitField ftsWidthAfter = new BitField(0x00003800);
    private static final BitField fNeverBeenAutofit = new BitField(0x00004000);
    private static final BitField fInvalAutofit = new BitField(0x00008000);
    private static final BitField widthAndFitsFlags_empty1 = new BitField(0x00070000);
    private static final BitField fVert = new BitField(0x00080000);
    private static final BitField pcVert = new BitField(0x00300000);
    private static final BitField pcHorz = new BitField(0x00c00000);
    private static final BitField widthAndFitsFlags_empty2 = new BitField(0xff000000);

    private static final BitField fFirstRow = new BitField(0x0001);
    private static final BitField fLastRow = new BitField(0x0002);
    private static final BitField fOutline = new BitField(0x0004);
    private static final BitField fOrigWordTableRules = new BitField(0x0008);
    private static final BitField fCellSpacing = new BitField(0x0010);
    private static final BitField grpfTap_unused = new BitField(0xffe0);

    private static final BitField fWrapToWwd = new BitField(0x0001);
    private static final BitField fNotPageView = new BitField(0x0002);
    private static final BitField viewFlags_unused1 = new BitField(0x0004);
    private static final BitField fWebView = new BitField(0x0008);
    private static final BitField fAdjusted = new BitField(0x0010);
    private static final BitField viewFlags_unused2 = new BitField(0xffe0);


    protected short field_1_istd;
    protected short field_2_jc;
    protected int field_3_dxaGapHalf;
    protected int field_4_dyaRowHeight;
    protected boolean field_5_fCantSplit;
    protected boolean field_6_fCantSplit90;
    protected boolean field_7_fTableHeader;
    protected TableAutoformatLookSpecifier field_8_tlp;
    protected short field_9_wWidth;
    protected short field_10_wWidthIndent;
    protected short field_11_wWidthBefore;
    protected short field_12_wWidthAfter;
    protected int field_13_widthAndFitsFlags;
    protected int field_14_dxaAbs;
    protected int field_15_dyaAbs;
    protected int field_16_dxaFromText;
    protected int field_17_dyaFromText;
    protected int field_18_dxaFromTextRight;
    protected int field_19_dyaFromTextBottom;
    protected byte field_20_fBiDi;
    protected byte field_21_fRTL;
    protected byte field_22_fNoAllowOverlap;
    protected byte field_23_fSpare;
    protected int field_24_grpfTap;
    protected int field_25_internalFlags;
    protected short field_26_itcMac;
    protected int field_27_dxaAdjust;
    protected int field_28_dxaWebView;
    protected int field_29_dxaRTEWrapWidth;
    protected int field_30_dxaColWidthWwd;
    protected short field_31_pctWwd;
    protected int field_32_viewFlags;
    protected short[] field_33_rgdxaCenter;
    protected short[] field_34_rgdxaCenterPrint;
    protected ShadingDescriptor field_35_shdTable;
    protected BorderCode field_36_brcBottom;
    protected BorderCode field_37_brcTop;
    protected BorderCode field_38_brcLeft;
    protected BorderCode field_39_brcRight;
    protected BorderCode field_40_brcVertical;
    protected BorderCode field_41_brcHorizontal;
    protected short field_42_wCellPaddingDefaultTop;
    protected short field_43_wCellPaddingDefaultLeft;
    protected short field_44_wCellPaddingDefaultBottom;
    protected short field_45_wCellPaddingDefaultRight;
    protected byte field_46_ftsCellPaddingDefaultTop;
    protected byte field_47_ftsCellPaddingDefaultLeft;
    protected byte field_48_ftsCellPaddingDefaultBottom;
    protected byte field_49_ftsCellPaddingDefaultRight;
    protected short field_50_wCellSpacingDefaultTop;
    protected short field_51_wCellSpacingDefaultLeft;
    protected short field_52_wCellSpacingDefaultBottom;
    protected short field_53_wCellSpacingDefaultRight;
    protected byte field_54_ftsCellSpacingDefaultTop;
    protected byte field_55_ftsCellSpacingDefaultLeft;
    protected byte field_56_ftsCellSpacingDefaultBottom;
    protected byte field_57_ftsCellSpacingDefaultRight;
    protected short field_58_wCellPaddingOuterTop;
    protected short field_59_wCellPaddingOuterLeft;
    protected short field_60_wCellPaddingOuterBottom;
    protected short field_61_wCellPaddingOuterRight;
    protected byte field_62_ftsCellPaddingOuterTop;
    protected byte field_63_ftsCellPaddingOuterLeft;
    protected byte field_64_ftsCellPaddingOuterBottom;
    protected byte field_65_ftsCellPaddingOuterRight;
    protected short field_66_wCellSpacingOuterTop;
    protected short field_67_wCellSpacingOuterLeft;
    protected short field_68_wCellSpacingOuterBottom;
    protected short field_69_wCellSpacingOuterRight;
    protected byte field_70_ftsCellSpacingOuterTop;
    protected byte field_71_ftsCellSpacingOuterLeft;
    protected byte field_72_ftsCellSpacingOuterBottom;
    protected byte field_73_ftsCellSpacingOuterRight;
    protected TableCellDescriptor[] field_74_rgtc;
    protected ShadingDescriptor[] field_75_rgshd;
    protected byte field_76_fPropRMark;
    protected byte field_77_fHasOldProps;
    protected short field_78_cHorzBands;
    protected short field_79_cVertBands;
    protected BorderCode field_80_rgbrcInsideDefault_0;
    protected BorderCode field_81_rgbrcInsideDefault_1;

    protected TAPAbstractType() {
        this.field_8_tlp = new TableAutoformatLookSpecifier();
        this.field_33_rgdxaCenter = new short[0];
        this.field_34_rgdxaCenterPrint = new short[0];
        this.field_35_shdTable = new ShadingDescriptor();
        this.field_36_brcBottom = new BorderCode();
        this.field_37_brcTop = new BorderCode();
        this.field_38_brcLeft = new BorderCode();
        this.field_39_brcRight = new BorderCode();
        this.field_40_brcVertical = new BorderCode();
        this.field_41_brcHorizontal = new BorderCode();
        this.field_74_rgtc = new TableCellDescriptor[0];
        this.field_75_rgshd = new ShadingDescriptor[0];
        this.field_80_rgbrcInsideDefault_0 = new BorderCode();
        this.field_81_rgbrcInsideDefault_1 = new BorderCode();
    }

    protected TAPAbstractType(TAPAbstractType other) {
        field_1_istd = other.field_1_istd;
        field_2_jc = other.field_2_jc;
        field_3_dxaGapHalf = other.field_3_dxaGapHalf;
        field_4_dyaRowHeight = other.field_4_dyaRowHeight;
        field_5_fCantSplit = other.field_5_fCantSplit;
        field_6_fCantSplit90 = other.field_6_fCantSplit90;
        field_7_fTableHeader = other.field_7_fTableHeader;
        field_8_tlp = (other.field_8_tlp == null) ? null : other.field_8_tlp.copy();
        field_9_wWidth = other.field_9_wWidth;
        field_10_wWidthIndent = other.field_10_wWidthIndent;
        field_11_wWidthBefore = other.field_11_wWidthBefore;
        field_12_wWidthAfter = other.field_12_wWidthAfter;
        field_13_widthAndFitsFlags = other.field_13_widthAndFitsFlags;
        field_14_dxaAbs = other.field_14_dxaAbs;
        field_15_dyaAbs = other.field_15_dyaAbs;
        field_16_dxaFromText = other.field_16_dxaFromText;
        field_17_dyaFromText = other.field_17_dyaFromText;
        field_18_dxaFromTextRight = other.field_18_dxaFromTextRight;
        field_19_dyaFromTextBottom = other.field_19_dyaFromTextBottom;
        field_20_fBiDi = other.field_20_fBiDi;
        field_21_fRTL = other.field_21_fRTL;
        field_22_fNoAllowOverlap = other.field_22_fNoAllowOverlap;
        field_23_fSpare = other.field_23_fSpare;
        field_24_grpfTap = other.field_24_grpfTap;
        field_25_internalFlags = other.field_25_internalFlags;
        field_26_itcMac = other.field_26_itcMac;
        field_27_dxaAdjust = other.field_27_dxaAdjust;
        field_28_dxaWebView = other.field_28_dxaWebView;
        field_29_dxaRTEWrapWidth = other.field_29_dxaRTEWrapWidth;
        field_30_dxaColWidthWwd = other.field_30_dxaColWidthWwd;
        field_31_pctWwd = other.field_31_pctWwd;
        field_32_viewFlags = other.field_32_viewFlags;
        field_33_rgdxaCenter = (other.field_33_rgdxaCenter == null) ? null : other.field_33_rgdxaCenter.clone();
        field_34_rgdxaCenterPrint = (other.field_34_rgdxaCenterPrint == null) ? null : other.field_34_rgdxaCenterPrint.clone();
        field_35_shdTable = (other.field_35_shdTable == null) ? null : other.field_35_shdTable.copy();
        field_36_brcBottom = (other.field_36_brcBottom == null) ? null : other.field_36_brcBottom.copy();
        field_37_brcTop = (other.field_37_brcTop == null) ? null : other.field_37_brcTop.copy();
        field_38_brcLeft = (other.field_38_brcLeft == null) ? null : other.field_38_brcLeft.copy();
        field_39_brcRight = (other.field_39_brcRight == null) ? null : other.field_39_brcRight.copy();
        field_40_brcVertical = (other.field_40_brcVertical == null) ? null : other.field_40_brcVertical.copy();
        field_41_brcHorizontal = (other.field_41_brcHorizontal == null) ? null : other.field_41_brcHorizontal.copy();
        field_42_wCellPaddingDefaultTop = other.field_42_wCellPaddingDefaultTop;
        field_43_wCellPaddingDefaultLeft = other.field_43_wCellPaddingDefaultLeft;
        field_44_wCellPaddingDefaultBottom = other.field_44_wCellPaddingDefaultBottom;
        field_45_wCellPaddingDefaultRight = other.field_45_wCellPaddingDefaultRight;
        field_46_ftsCellPaddingDefaultTop = other.field_46_ftsCellPaddingDefaultTop;
        field_47_ftsCellPaddingDefaultLeft = other.field_47_ftsCellPaddingDefaultLeft;
        field_48_ftsCellPaddingDefaultBottom = other.field_48_ftsCellPaddingDefaultBottom;
        field_49_ftsCellPaddingDefaultRight = other.field_49_ftsCellPaddingDefaultRight;
        field_50_wCellSpacingDefaultTop = other.field_50_wCellSpacingDefaultTop;
        field_51_wCellSpacingDefaultLeft = other.field_51_wCellSpacingDefaultLeft;
        field_52_wCellSpacingDefaultBottom = other.field_52_wCellSpacingDefaultBottom;
        field_53_wCellSpacingDefaultRight = other.field_53_wCellSpacingDefaultRight;
        field_54_ftsCellSpacingDefaultTop = other.field_54_ftsCellSpacingDefaultTop;
        field_55_ftsCellSpacingDefaultLeft = other.field_55_ftsCellSpacingDefaultLeft;
        field_56_ftsCellSpacingDefaultBottom = other.field_56_ftsCellSpacingDefaultBottom;
        field_57_ftsCellSpacingDefaultRight = other.field_57_ftsCellSpacingDefaultRight;
        field_58_wCellPaddingOuterTop = other.field_58_wCellPaddingOuterTop;
        field_59_wCellPaddingOuterLeft = other.field_59_wCellPaddingOuterLeft;
        field_60_wCellPaddingOuterBottom = other.field_60_wCellPaddingOuterBottom;
        field_61_wCellPaddingOuterRight = other.field_61_wCellPaddingOuterRight;
        field_62_ftsCellPaddingOuterTop = other.field_62_ftsCellPaddingOuterTop;
        field_63_ftsCellPaddingOuterLeft = other.field_63_ftsCellPaddingOuterLeft;
        field_64_ftsCellPaddingOuterBottom = other.field_64_ftsCellPaddingOuterBottom;
        field_65_ftsCellPaddingOuterRight = other.field_65_ftsCellPaddingOuterRight;
        field_66_wCellSpacingOuterTop = other.field_66_wCellSpacingOuterTop;
        field_67_wCellSpacingOuterLeft = other.field_67_wCellSpacingOuterLeft;
        field_68_wCellSpacingOuterBottom = other.field_68_wCellSpacingOuterBottom;
        field_69_wCellSpacingOuterRight = other.field_69_wCellSpacingOuterRight;
        field_70_ftsCellSpacingOuterTop = other.field_70_ftsCellSpacingOuterTop;
        field_71_ftsCellSpacingOuterLeft = other.field_71_ftsCellSpacingOuterLeft;
        field_72_ftsCellSpacingOuterBottom = other.field_72_ftsCellSpacingOuterBottom;
        field_73_ftsCellSpacingOuterRight = other.field_73_ftsCellSpacingOuterRight;
        field_74_rgtc = (other.field_74_rgtc == null) ? null
            : Stream.of(other.field_74_rgtc).map(TableCellDescriptor::copy).toArray(TableCellDescriptor[]::new);
        field_75_rgshd = (other.field_75_rgshd == null) ? null
                : Stream.of(other.field_75_rgshd).map(ShadingDescriptor::copy).toArray(ShadingDescriptor[]::new);
        field_76_fPropRMark = other.field_76_fPropRMark;
        field_77_fHasOldProps = other.field_77_fHasOldProps;
        field_78_cHorzBands = other.field_78_cHorzBands;
        field_79_cVertBands = other.field_79_cVertBands;
        field_80_rgbrcInsideDefault_0 = (other.field_80_rgbrcInsideDefault_0 == null) ? null : other.field_80_rgbrcInsideDefault_0.copy();
        field_81_rgbrcInsideDefault_1 = (other.field_81_rgbrcInsideDefault_1 == null) ? null : other.field_81_rgbrcInsideDefault_1.copy();
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("[TAP]\n");
        builder.append("    .istd                 = ");
        builder.append(" (").append(getIstd()).append(" )\n");
        builder.append("    .jc                   = ");
        builder.append(" (").append(getJc()).append(" )\n");
        builder.append("    .dxaGapHalf           = ");
        builder.append(" (").append(getDxaGapHalf()).append(" )\n");
        builder.append("    .dyaRowHeight         = ");
        builder.append(" (").append(getDyaRowHeight()).append(" )\n");
        builder.append("    .fCantSplit           = ");
        builder.append(" (").append(getFCantSplit()).append(" )\n");
        builder.append("    .fCantSplit90         = ");
        builder.append(" (").append(getFCantSplit90()).append(" )\n");
        builder.append("    .fTableHeader         = ");
        builder.append(" (").append(getFTableHeader()).append(" )\n");
        builder.append("    .tlp                  = ");
        builder.append(" (").append(getTlp()).append(" )\n");
        builder.append("    .wWidth               = ");
        builder.append(" (").append(getWWidth()).append(" )\n");
        builder.append("    .wWidthIndent         = ");
        builder.append(" (").append(getWWidthIndent()).append(" )\n");
        builder.append("    .wWidthBefore         = ");
        builder.append(" (").append(getWWidthBefore()).append(" )\n");
        builder.append("    .wWidthAfter          = ");
        builder.append(" (").append(getWWidthAfter()).append(" )\n");
        builder.append("    .widthAndFitsFlags    = ");
        builder.append(" (").append(getWidthAndFitsFlags()).append(" )\n");
        builder.append("         .fAutofit                 = ").append(isFAutofit()).append('\n');
        builder.append("         .fKeepFollow              = ").append(isFKeepFollow()).append('\n');
        builder.append("         .ftsWidth                 = ").append(getFtsWidth()).append('\n');
        builder.append("         .ftsWidthIndent           = ").append(getFtsWidthIndent()).append('\n');
        builder.append("         .ftsWidthBefore           = ").append(getFtsWidthBefore()).append('\n');
        builder.append("         .ftsWidthAfter            = ").append(getFtsWidthAfter()).append('\n');
        builder.append("         .fNeverBeenAutofit        = ").append(isFNeverBeenAutofit()).append('\n');
        builder.append("         .fInvalAutofit            = ").append(isFInvalAutofit()).append('\n');
        builder.append("         .widthAndFitsFlags_empty1     = ").append(getWidthAndFitsFlags_empty1()).append('\n');
        builder.append("         .fVert                    = ").append(isFVert()).append('\n');
        builder.append("         .pcVert                   = ").append(getPcVert()).append('\n');
        builder.append("         .pcHorz                   = ").append(getPcHorz()).append('\n');
        builder.append("         .widthAndFitsFlags_empty2     = ").append(getWidthAndFitsFlags_empty2()).append('\n');
        builder.append("    .dxaAbs               = ");
        builder.append(" (").append(getDxaAbs()).append(" )\n");
        builder.append("    .dyaAbs               = ");
        builder.append(" (").append(getDyaAbs()).append(" )\n");
        builder.append("    .dxaFromText          = ");
        builder.append(" (").append(getDxaFromText()).append(" )\n");
        builder.append("    .dyaFromText          = ");
        builder.append(" (").append(getDyaFromText()).append(" )\n");
        builder.append("    .dxaFromTextRight     = ");
        builder.append(" (").append(getDxaFromTextRight()).append(" )\n");
        builder.append("    .dyaFromTextBottom    = ");
        builder.append(" (").append(getDyaFromTextBottom()).append(" )\n");
        builder.append("    .fBiDi                = ");
        builder.append(" (").append(getFBiDi()).append(" )\n");
        builder.append("    .fRTL                 = ");
        builder.append(" (").append(getFRTL()).append(" )\n");
        builder.append("    .fNoAllowOverlap      = ");
        builder.append(" (").append(getFNoAllowOverlap()).append(" )\n");
        builder.append("    .fSpare               = ");
        builder.append(" (").append(getFSpare()).append(" )\n");
        builder.append("    .grpfTap              = ");
        builder.append(" (").append(getGrpfTap()).append(" )\n");
        builder.append("    .internalFlags        = ");
        builder.append(" (").append(getInternalFlags()).append(" )\n");
        builder.append("         .fFirstRow                = ").append(isFFirstRow()).append('\n');
        builder.append("         .fLastRow                 = ").append(isFLastRow()).append('\n');
        builder.append("         .fOutline                 = ").append(isFOutline()).append('\n');
        builder.append("         .fOrigWordTableRules      = ").append(isFOrigWordTableRules()).append('\n');
        builder.append("         .fCellSpacing             = ").append(isFCellSpacing()).append('\n');
        builder.append("         .grpfTap_unused           = ").append(getGrpfTap_unused()).append('\n');
        builder.append("    .itcMac               = ");
        builder.append(" (").append(getItcMac()).append(" )\n");
        builder.append("    .dxaAdjust            = ");
        builder.append(" (").append(getDxaAdjust()).append(" )\n");
        builder.append("    .dxaWebView           = ");
        builder.append(" (").append(getDxaWebView()).append(" )\n");
        builder.append("    .dxaRTEWrapWidth      = ");
        builder.append(" (").append(getDxaRTEWrapWidth()).append(" )\n");
        builder.append("    .dxaColWidthWwd       = ");
        builder.append(" (").append(getDxaColWidthWwd()).append(" )\n");
        builder.append("    .pctWwd               = ");
        builder.append(" (").append(getPctWwd()).append(" )\n");
        builder.append("    .viewFlags            = ");
        builder.append(" (").append(getViewFlags()).append(" )\n");
        builder.append("         .fWrapToWwd               = ").append(isFWrapToWwd()).append('\n');
        builder.append("         .fNotPageView             = ").append(isFNotPageView()).append('\n');
        builder.append("         .viewFlags_unused1        = ").append(isViewFlags_unused1()).append('\n');
        builder.append("         .fWebView                 = ").append(isFWebView()).append('\n');
        builder.append("         .fAdjusted                = ").append(isFAdjusted()).append('\n');
        builder.append("         .viewFlags_unused2        = ").append(getViewFlags_unused2()).append('\n');
        builder.append("    .rgdxaCenter          = ");
        builder.append(" (").append(Arrays.toString(getRgdxaCenter())).append(" )\n");
        builder.append("    .rgdxaCenterPrint     = ");
        builder.append(" (").append(Arrays.toString(getRgdxaCenterPrint())).append(" )\n");
        builder.append("    .shdTable             = ");
        builder.append(" (").append(getShdTable()).append(" )\n");
        builder.append("    .brcBottom            = ");
        builder.append(" (").append(getBrcBottom()).append(" )\n");
        builder.append("    .brcTop               = ");
        builder.append(" (").append(getBrcTop()).append(" )\n");
        builder.append("    .brcLeft              = ");
        builder.append(" (").append(getBrcLeft()).append(" )\n");
        builder.append("    .brcRight             = ");
        builder.append(" (").append(getBrcRight()).append(" )\n");
        builder.append("    .brcVertical          = ");
        builder.append(" (").append(getBrcVertical()).append(" )\n");
        builder.append("    .brcHorizontal        = ");
        builder.append(" (").append(getBrcHorizontal()).append(" )\n");
        builder.append("    .wCellPaddingDefaultTop = ");
        builder.append(" (").append(getWCellPaddingDefaultTop()).append(" )\n");
        builder.append("    .wCellPaddingDefaultLeft = ");
        builder.append(" (").append(getWCellPaddingDefaultLeft()).append(" )\n");
        builder.append("    .wCellPaddingDefaultBottom = ");
        builder.append(" (").append(getWCellPaddingDefaultBottom()).append(" )\n");
        builder.append("    .wCellPaddingDefaultRight = ");
        builder.append(" (").append(getWCellPaddingDefaultRight()).append(" )\n");
        builder.append("    .ftsCellPaddingDefaultTop = ");
        builder.append(" (").append(getFtsCellPaddingDefaultTop()).append(" )\n");
        builder.append("    .ftsCellPaddingDefaultLeft = ");
        builder.append(" (").append(getFtsCellPaddingDefaultLeft()).append(" )\n");
        builder.append("    .ftsCellPaddingDefaultBottom = ");
        builder.append(" (").append(getFtsCellPaddingDefaultBottom()).append(" )\n");
        builder.append("    .ftsCellPaddingDefaultRight = ");
        builder.append(" (").append(getFtsCellPaddingDefaultRight()).append(" )\n");
        builder.append("    .wCellSpacingDefaultTop = ");
        builder.append(" (").append(getWCellSpacingDefaultTop()).append(" )\n");
        builder.append("    .wCellSpacingDefaultLeft = ");
        builder.append(" (").append(getWCellSpacingDefaultLeft()).append(" )\n");
        builder.append("    .wCellSpacingDefaultBottom = ");
        builder.append(" (").append(getWCellSpacingDefaultBottom()).append(" )\n");
        builder.append("    .wCellSpacingDefaultRight = ");
        builder.append(" (").append(getWCellSpacingDefaultRight()).append(" )\n");
        builder.append("    .ftsCellSpacingDefaultTop = ");
        builder.append(" (").append(getFtsCellSpacingDefaultTop()).append(" )\n");
        builder.append("    .ftsCellSpacingDefaultLeft = ");
        builder.append(" (").append(getFtsCellSpacingDefaultLeft()).append(" )\n");
        builder.append("    .ftsCellSpacingDefaultBottom = ");
        builder.append(" (").append(getFtsCellSpacingDefaultBottom()).append(" )\n");
        builder.append("    .ftsCellSpacingDefaultRight = ");
        builder.append(" (").append(getFtsCellSpacingDefaultRight()).append(" )\n");
        builder.append("    .wCellPaddingOuterTop = ");
        builder.append(" (").append(getWCellPaddingOuterTop()).append(" )\n");
        builder.append("    .wCellPaddingOuterLeft = ");
        builder.append(" (").append(getWCellPaddingOuterLeft()).append(" )\n");
        builder.append("    .wCellPaddingOuterBottom = ");
        builder.append(" (").append(getWCellPaddingOuterBottom()).append(" )\n");
        builder.append("    .wCellPaddingOuterRight = ");
        builder.append(" (").append(getWCellPaddingOuterRight()).append(" )\n");
        builder.append("    .ftsCellPaddingOuterTop = ");
        builder.append(" (").append(getFtsCellPaddingOuterTop()).append(" )\n");
        builder.append("    .ftsCellPaddingOuterLeft = ");
        builder.append(" (").append(getFtsCellPaddingOuterLeft()).append(" )\n");
        builder.append("    .ftsCellPaddingOuterBottom = ");
        builder.append(" (").append(getFtsCellPaddingOuterBottom()).append(" )\n");
        builder.append("    .ftsCellPaddingOuterRight = ");
        builder.append(" (").append(getFtsCellPaddingOuterRight()).append(" )\n");
        builder.append("    .wCellSpacingOuterTop = ");
        builder.append(" (").append(getWCellSpacingOuterTop()).append(" )\n");
        builder.append("    .wCellSpacingOuterLeft = ");
        builder.append(" (").append(getWCellSpacingOuterLeft()).append(" )\n");
        builder.append("    .wCellSpacingOuterBottom = ");
        builder.append(" (").append(getWCellSpacingOuterBottom()).append(" )\n");
        builder.append("    .wCellSpacingOuterRight = ");
        builder.append(" (").append(getWCellSpacingOuterRight()).append(" )\n");
        builder.append("    .ftsCellSpacingOuterTop = ");
        builder.append(" (").append(getFtsCellSpacingOuterTop()).append(" )\n");
        builder.append("    .ftsCellSpacingOuterLeft = ");
        builder.append(" (").append(getFtsCellSpacingOuterLeft()).append(" )\n");
        builder.append("    .ftsCellSpacingOuterBottom = ");
        builder.append(" (").append(getFtsCellSpacingOuterBottom()).append(" )\n");
        builder.append("    .ftsCellSpacingOuterRight = ");
        builder.append(" (").append(getFtsCellSpacingOuterRight()).append(" )\n");
        builder.append("    .rgtc                 = ");
        builder.append(" (").append(Arrays.toString(getRgtc())).append(" )\n");
        builder.append("    .rgshd                = ");
        builder.append(" (").append(Arrays.toString(getRgshd())).append(" )\n");
        builder.append("    .fPropRMark           = ");
        builder.append(" (").append(getFPropRMark()).append(" )\n");
        builder.append("    .fHasOldProps         = ");
        builder.append(" (").append(getFHasOldProps()).append(" )\n");
        builder.append("    .cHorzBands           = ");
        builder.append(" (").append(getCHorzBands()).append(" )\n");
        builder.append("    .cVertBands           = ");
        builder.append(" (").append(getCVertBands()).append(" )\n");
        builder.append("    .rgbrcInsideDefault_0 = ");
        builder.append(" (").append(getRgbrcInsideDefault_0()).append(" )\n");
        builder.append("    .rgbrcInsideDefault_1 = ");
        builder.append(" (").append(getRgbrcInsideDefault_1()).append(" )\n");

        builder.append("[/TAP]\n");
        return builder.toString();
    }

    /**
     * Table style for the Table.
     */
    @Internal
    public short getIstd()
    {
        return field_1_istd;
    }

    /**
     * Table style for the Table.
     */
    @Internal
    public void setIstd( short field_1_istd )
    {
        this.field_1_istd = field_1_istd;
    }

    /**
     * Justification code. specifies how table row should be justified within its column. 0 -- left justify, 1 -- center, 2 -- right justify.
     */
    @Internal
    public short getJc()
    {
        return field_2_jc;
    }

    /**
     * Justification code. specifies how table row should be justified within its column. 0 -- left justify, 1 -- center, 2 -- right justify.
     */
    @Internal
    public void setJc( short field_2_jc )
    {
        this.field_2_jc = field_2_jc;
    }

    /**
     * Measures half of the white space that will be maintained between text in adjacent columns of a table row. A dxaGapHalf width of white space will be maintained on both sides of a column boundary..
     */
    @Internal
    public int getDxaGapHalf()
    {
        return field_3_dxaGapHalf;
    }

    /**
     * Measures half of the white space that will be maintained between text in adjacent columns of a table row. A dxaGapHalf width of white space will be maintained on both sides of a column boundary..
     */
    @Internal
    public void setDxaGapHalf( int field_3_dxaGapHalf )
    {
        this.field_3_dxaGapHalf = field_3_dxaGapHalf;
    }

    /**
     * When greater than 0, guarantees that the height of the table will be at least dyaRowHeight high. When less than 0, guarantees that the height of the table will be exactly absolute value of dyaRowHeight high. When 0, table will be given a height large enough to represent all of the text in all of the cells of the table. Cells with vertical text flow make no contribution to the computation of the height of rows with auto or at least height. Neither do vertically merged cells, except in the last row of the vertical merge. If an auto height row consists entirely of cells which have vertical text direction or are vertically merged, and the row does not contain the last cell in any vertical cell merge, then the row is given height equal to that of the end of cell mark in the first cell..
     */
    @Internal
    public int getDyaRowHeight()
    {
        return field_4_dyaRowHeight;
    }

    /**
     * When greater than 0, guarantees that the height of the table will be at least dyaRowHeight high. When less than 0, guarantees that the height of the table will be exactly absolute value of dyaRowHeight high. When 0, table will be given a height large enough to represent all of the text in all of the cells of the table. Cells with vertical text flow make no contribution to the computation of the height of rows with auto or at least height. Neither do vertically merged cells, except in the last row of the vertical merge. If an auto height row consists entirely of cells which have vertical text direction or are vertically merged, and the row does not contain the last cell in any vertical cell merge, then the row is given height equal to that of the end of cell mark in the first cell..
     */
    @Internal
    public void setDyaRowHeight( int field_4_dyaRowHeight )
    {
        this.field_4_dyaRowHeight = field_4_dyaRowHeight;
    }

    /**
     * When 1, table row may not be split across page bounds.
     */
    @Internal
    public boolean getFCantSplit()
    {
        return field_5_fCantSplit;
    }

    /**
     * When 1, table row may not be split across page bounds.
     */
    @Internal
    public void setFCantSplit( boolean field_5_fCantSplit )
    {
        this.field_5_fCantSplit = field_5_fCantSplit;
    }

    /**
     * When 1, table row may not be split across page bounds. Used for Word 2000 and Word 97..
     */
    @Internal
    public boolean getFCantSplit90()
    {
        return field_6_fCantSplit90;
    }

    /**
     * When 1, table row may not be split across page bounds. Used for Word 2000 and Word 97..
     */
    @Internal
    public void setFCantSplit90( boolean field_6_fCantSplit90 )
    {
        this.field_6_fCantSplit90 = field_6_fCantSplit90;
    }

    /**
     * When 1, table row is to be used as the header of the table.
     */
    @Internal
    public boolean getFTableHeader()
    {
        return field_7_fTableHeader;
    }

    /**
     * When 1, table row is to be used as the header of the table.
     */
    @Internal
    public void setFTableHeader( boolean field_7_fTableHeader )
    {
        this.field_7_fTableHeader = field_7_fTableHeader;
    }

    /**
     * Table look specifier.
     */
    @Internal
    public TableAutoformatLookSpecifier getTlp()
    {
        return field_8_tlp;
    }

    /**
     * Table look specifier.
     */
    @Internal
    public void setTlp( TableAutoformatLookSpecifier field_8_tlp )
    {
        this.field_8_tlp = field_8_tlp;
    }

    /**
     * Preferred table width.
     */
    @Internal
    public short getWWidth()
    {
        return field_9_wWidth;
    }

    /**
     * Preferred table width.
     */
    @Internal
    public void setWWidth( short field_9_wWidth )
    {
        this.field_9_wWidth = field_9_wWidth;
    }

    /**
     * Left Indent.
     */
    @Internal
    public short getWWidthIndent()
    {
        return field_10_wWidthIndent;
    }

    /**
     * Left Indent.
     */
    @Internal
    public void setWWidthIndent( short field_10_wWidthIndent )
    {
        this.field_10_wWidthIndent = field_10_wWidthIndent;
    }

    /**
     * Width of invisible cell (used for layout purposes) before the first visible cell in the row..
     */
    @Internal
    public short getWWidthBefore()
    {
        return field_11_wWidthBefore;
    }

    /**
     * Width of invisible cell (used for layout purposes) before the first visible cell in the row..
     */
    @Internal
    public void setWWidthBefore( short field_11_wWidthBefore )
    {
        this.field_11_wWidthBefore = field_11_wWidthBefore;
    }

    /**
     * Width of invisible cell (used for layout purposes) after the last visible cell in the row..
     */
    @Internal
    public short getWWidthAfter()
    {
        return field_12_wWidthAfter;
    }

    /**
     * Width of invisible cell (used for layout purposes) after the last visible cell in the row..
     */
    @Internal
    public void setWWidthAfter( short field_12_wWidthAfter )
    {
        this.field_12_wWidthAfter = field_12_wWidthAfter;
    }

    /**
     * Get the widthAndFitsFlags field for the TAP record.
     */
    @Internal
    public int getWidthAndFitsFlags()
    {
        return field_13_widthAndFitsFlags;
    }

    /**
     * Set the widthAndFitsFlags field for the TAP record.
     */
    @Internal
    public void setWidthAndFitsFlags( int field_13_widthAndFitsFlags )
    {
        this.field_13_widthAndFitsFlags = field_13_widthAndFitsFlags;
    }

    /**
     * Absolute horizontal position.
     */
    @Internal
    public int getDxaAbs()
    {
        return field_14_dxaAbs;
    }

    /**
     * Absolute horizontal position.
     */
    @Internal
    public void setDxaAbs( int field_14_dxaAbs )
    {
        this.field_14_dxaAbs = field_14_dxaAbs;
    }

    /**
     * Absolute vertical position.
     */
    @Internal
    public int getDyaAbs()
    {
        return field_15_dyaAbs;
    }

    /**
     * Absolute vertical position.
     */
    @Internal
    public void setDyaAbs( int field_15_dyaAbs )
    {
        this.field_15_dyaAbs = field_15_dyaAbs;
    }

    /**
     * Left distance from surrounding text when absolutely positioned.
     */
    @Internal
    public int getDxaFromText()
    {
        return field_16_dxaFromText;
    }

    /**
     * Left distance from surrounding text when absolutely positioned.
     */
    @Internal
    public void setDxaFromText( int field_16_dxaFromText )
    {
        this.field_16_dxaFromText = field_16_dxaFromText;
    }

    /**
     * Top distance from surrounding text when absolutely positioned.
     */
    @Internal
    public int getDyaFromText()
    {
        return field_17_dyaFromText;
    }

    /**
     * Top distance from surrounding text when absolutely positioned.
     */
    @Internal
    public void setDyaFromText( int field_17_dyaFromText )
    {
        this.field_17_dyaFromText = field_17_dyaFromText;
    }

    /**
     * Right distance from surrounding text when absolutely positioned.
     */
    @Internal
    public int getDxaFromTextRight()
    {
        return field_18_dxaFromTextRight;
    }

    /**
     * Right distance from surrounding text when absolutely positioned.
     */
    @Internal
    public void setDxaFromTextRight( int field_18_dxaFromTextRight )
    {
        this.field_18_dxaFromTextRight = field_18_dxaFromTextRight;
    }

    /**
     * Bottom distance from surrounding text when absolutely positioned.
     */
    @Internal
    public int getDyaFromTextBottom()
    {
        return field_19_dyaFromTextBottom;
    }

    /**
     * Bottom distance from surrounding text when absolutely positioned.
     */
    @Internal
    public void setDyaFromTextBottom( int field_19_dyaFromTextBottom )
    {
        this.field_19_dyaFromTextBottom = field_19_dyaFromTextBottom;
    }

    /**
     * When 1, table is right-to-left. Logical right-to-left table: The CP stream of a right-to-left table is meant to be displayed from right to left. So for example the first table cell is displayed on the right side of the table instead of the left..
     */
    @Internal
    public byte getFBiDi()
    {
        return field_20_fBiDi;
    }

    /**
     * When 1, table is right-to-left. Logical right-to-left table: The CP stream of a right-to-left table is meant to be displayed from right to left. So for example the first table cell is displayed on the right side of the table instead of the left..
     */
    @Internal
    public void setFBiDi( byte field_20_fBiDi )
    {
        this.field_20_fBiDi = field_20_fBiDi;
    }

    /**
     * Word 2000 style right-to-left table. Visual right-to-left table: The CP stream of a right-to-left table is displayed from left to right just as for a normal table. So, the text which is meant to be in the first (rightmost) table cell must be placed in the last table cell in the CP stream..
     */
    @Internal
    public byte getFRTL()
    {
        return field_21_fRTL;
    }

    /**
     * Word 2000 style right-to-left table. Visual right-to-left table: The CP stream of a right-to-left table is displayed from left to right just as for a normal table. So, the text which is meant to be in the first (rightmost) table cell must be placed in the last table cell in the CP stream..
     */
    @Internal
    public void setFRTL( byte field_21_fRTL )
    {
        this.field_21_fRTL = field_21_fRTL;
    }

    /**
     * When set to 1, do not allow absolutely positioned table to overlap with other tables.
     */
    @Internal
    public byte getFNoAllowOverlap()
    {
        return field_22_fNoAllowOverlap;
    }

    /**
     * When set to 1, do not allow absolutely positioned table to overlap with other tables.
     */
    @Internal
    public void setFNoAllowOverlap( byte field_22_fNoAllowOverlap )
    {
        this.field_22_fNoAllowOverlap = field_22_fNoAllowOverlap;
    }

    /**
     * Not used.
     */
    @Internal
    public byte getFSpare()
    {
        return field_23_fSpare;
    }

    /**
     * Not used.
     */
    @Internal
    public void setFSpare( byte field_23_fSpare )
    {
        this.field_23_fSpare = field_23_fSpare;
    }

    /**
     * Used internally by Word.
     */
    @Internal
    public int getGrpfTap()
    {
        return field_24_grpfTap;
    }

    /**
     * Used internally by Word.
     */
    @Internal
    public void setGrpfTap( int field_24_grpfTap )
    {
        this.field_24_grpfTap = field_24_grpfTap;
    }

    /**
     * Used internally by Word.
     */
    @Internal
    public int getInternalFlags()
    {
        return field_25_internalFlags;
    }

    /**
     * Used internally by Word.
     */
    @Internal
    public void setInternalFlags( int field_25_internalFlags )
    {
        this.field_25_internalFlags = field_25_internalFlags;
    }

    /**
     * Count of cells defined for this row. itcMac must be &gt;= 0 and less than or equal to 64..
     */
    @Internal
    public short getItcMac()
    {
        return field_26_itcMac;
    }

    /**
     * Count of cells defined for this row. itcMac must be &gt;= 0 and less than or equal to 64..
     */
    @Internal
    public void setItcMac( short field_26_itcMac )
    {
        this.field_26_itcMac = field_26_itcMac;
    }

    /**
     * Used internally by Word.
     */
    @Internal
    public int getDxaAdjust()
    {
        return field_27_dxaAdjust;
    }

    /**
     * Used internally by Word.
     */
    @Internal
    public void setDxaAdjust( int field_27_dxaAdjust )
    {
        this.field_27_dxaAdjust = field_27_dxaAdjust;
    }

    /**
     * Used internally by Word.
     */
    @Internal
    public int getDxaWebView()
    {
        return field_28_dxaWebView;
    }

    /**
     * Used internally by Word.
     */
    @Internal
    public void setDxaWebView( int field_28_dxaWebView )
    {
        this.field_28_dxaWebView = field_28_dxaWebView;
    }

    /**
     * Used internally by Word.
     */
    @Internal
    public int getDxaRTEWrapWidth()
    {
        return field_29_dxaRTEWrapWidth;
    }

    /**
     * Used internally by Word.
     */
    @Internal
    public void setDxaRTEWrapWidth( int field_29_dxaRTEWrapWidth )
    {
        this.field_29_dxaRTEWrapWidth = field_29_dxaRTEWrapWidth;
    }

    /**
     * Used internally by Word.
     */
    @Internal
    public int getDxaColWidthWwd()
    {
        return field_30_dxaColWidthWwd;
    }

    /**
     * Used internally by Word.
     */
    @Internal
    public void setDxaColWidthWwd( int field_30_dxaColWidthWwd )
    {
        this.field_30_dxaColWidthWwd = field_30_dxaColWidthWwd;
    }

    /**
     * Used internally by Word: percent of Window size for AutoFit in WebView.
     */
    @Internal
    public short getPctWwd()
    {
        return field_31_pctWwd;
    }

    /**
     * Used internally by Word: percent of Window size for AutoFit in WebView.
     */
    @Internal
    public void setPctWwd( short field_31_pctWwd )
    {
        this.field_31_pctWwd = field_31_pctWwd;
    }

    /**
     * Used internally by Word.
     */
    @Internal
    public int getViewFlags()
    {
        return field_32_viewFlags;
    }

    /**
     * Used internally by Word.
     */
    @Internal
    public void setViewFlags( int field_32_viewFlags )
    {
        this.field_32_viewFlags = field_32_viewFlags;
    }

    /**
     * rgdxaCenter[0] is the left boundary of cell 0 measured relative to margin rgdxaCenter[tap.itcMac - 1] is left boundary of last cell rgdxaCenter[tap.itcMac] is right boundary of last cell..
     */
    @Internal
    public short[] getRgdxaCenter()
    {
        return field_33_rgdxaCenter;
    }

    /**
     * rgdxaCenter[0] is the left boundary of cell 0 measured relative to margin rgdxaCenter[tap.itcMac - 1] is left boundary of last cell rgdxaCenter[tap.itcMac] is right boundary of last cell..
     */
    @Internal
    public void setRgdxaCenter( short[] field_33_rgdxaCenter )
    {
        this.field_33_rgdxaCenter = field_33_rgdxaCenter;
    }

    /**
     * Used internally by Word.
     */
    @Internal
    public short[] getRgdxaCenterPrint()
    {
        return field_34_rgdxaCenterPrint;
    }

    /**
     * Used internally by Word.
     */
    @Internal
    public void setRgdxaCenterPrint( short[] field_34_rgdxaCenterPrint )
    {
        this.field_34_rgdxaCenterPrint = field_34_rgdxaCenterPrint;
    }

    /**
     * Table shading.
     */
    @Internal
    public ShadingDescriptor getShdTable()
    {
        return field_35_shdTable;
    }

    /**
     * Table shading.
     */
    @Internal
    public void setShdTable( ShadingDescriptor field_35_shdTable )
    {
        this.field_35_shdTable = field_35_shdTable;
    }

    /**
     * Get the brcBottom field for the TAP record.
     */
    @Internal
    public BorderCode getBrcBottom()
    {
        return field_36_brcBottom;
    }

    /**
     * Set the brcBottom field for the TAP record.
     */
    @Internal
    public void setBrcBottom( BorderCode field_36_brcBottom )
    {
        this.field_36_brcBottom = field_36_brcBottom;
    }

    /**
     * Get the brcTop field for the TAP record.
     */
    @Internal
    public BorderCode getBrcTop()
    {
        return field_37_brcTop;
    }

    /**
     * Set the brcTop field for the TAP record.
     */
    @Internal
    public void setBrcTop( BorderCode field_37_brcTop )
    {
        this.field_37_brcTop = field_37_brcTop;
    }

    /**
     * Get the brcLeft field for the TAP record.
     */
    @Internal
    public BorderCode getBrcLeft()
    {
        return field_38_brcLeft;
    }

    /**
     * Set the brcLeft field for the TAP record.
     */
    @Internal
    public void setBrcLeft( BorderCode field_38_brcLeft )
    {
        this.field_38_brcLeft = field_38_brcLeft;
    }

    /**
     * Get the brcRight field for the TAP record.
     */
    @Internal
    public BorderCode getBrcRight()
    {
        return field_39_brcRight;
    }

    /**
     * Set the brcRight field for the TAP record.
     */
    @Internal
    public void setBrcRight( BorderCode field_39_brcRight )
    {
        this.field_39_brcRight = field_39_brcRight;
    }

    /**
     * Get the brcVertical field for the TAP record.
     */
    @Internal
    public BorderCode getBrcVertical()
    {
        return field_40_brcVertical;
    }

    /**
     * Set the brcVertical field for the TAP record.
     */
    @Internal
    public void setBrcVertical( BorderCode field_40_brcVertical )
    {
        this.field_40_brcVertical = field_40_brcVertical;
    }

    /**
     * Get the brcHorizontal field for the TAP record.
     */
    @Internal
    public BorderCode getBrcHorizontal()
    {
        return field_41_brcHorizontal;
    }

    /**
     * Set the brcHorizontal field for the TAP record.
     */
    @Internal
    public void setBrcHorizontal( BorderCode field_41_brcHorizontal )
    {
        this.field_41_brcHorizontal = field_41_brcHorizontal;
    }

    /**
     * Default top cell margin/padding.
     */
    @Internal
    public short getWCellPaddingDefaultTop()
    {
        return field_42_wCellPaddingDefaultTop;
    }

    /**
     * Default top cell margin/padding.
     */
    @Internal
    public void setWCellPaddingDefaultTop( short field_42_wCellPaddingDefaultTop )
    {
        this.field_42_wCellPaddingDefaultTop = field_42_wCellPaddingDefaultTop;
    }

    /**
     * Default left cell margin/padding.
     */
    @Internal
    public short getWCellPaddingDefaultLeft()
    {
        return field_43_wCellPaddingDefaultLeft;
    }

    /**
     * Default left cell margin/padding.
     */
    @Internal
    public void setWCellPaddingDefaultLeft( short field_43_wCellPaddingDefaultLeft )
    {
        this.field_43_wCellPaddingDefaultLeft = field_43_wCellPaddingDefaultLeft;
    }

    /**
     * Default bottom cell margin/padding.
     */
    @Internal
    public short getWCellPaddingDefaultBottom()
    {
        return field_44_wCellPaddingDefaultBottom;
    }

    /**
     * Default bottom cell margin/padding.
     */
    @Internal
    public void setWCellPaddingDefaultBottom( short field_44_wCellPaddingDefaultBottom )
    {
        this.field_44_wCellPaddingDefaultBottom = field_44_wCellPaddingDefaultBottom;
    }

    /**
     * Default right cell margin/padding.
     */
    @Internal
    public short getWCellPaddingDefaultRight()
    {
        return field_45_wCellPaddingDefaultRight;
    }

    /**
     * Default right cell margin/padding.
     */
    @Internal
    public void setWCellPaddingDefaultRight( short field_45_wCellPaddingDefaultRight )
    {
        this.field_45_wCellPaddingDefaultRight = field_45_wCellPaddingDefaultRight;
    }

    /**
     * Default top cell margin/padding units. 0 -- null; 1-2 -- not relevant; 3 -- twips..
     */
    @Internal
    public byte getFtsCellPaddingDefaultTop()
    {
        return field_46_ftsCellPaddingDefaultTop;
    }

    /**
     * Default top cell margin/padding units. 0 -- null; 1-2 -- not relevant; 3 -- twips..
     */
    @Internal
    public void setFtsCellPaddingDefaultTop( byte field_46_ftsCellPaddingDefaultTop )
    {
        this.field_46_ftsCellPaddingDefaultTop = field_46_ftsCellPaddingDefaultTop;
    }

    /**
     * Default left cell margin/padding units. 0 -- null; 1-2 -- not relevant; 3 -- twips..
     */
    @Internal
    public byte getFtsCellPaddingDefaultLeft()
    {
        return field_47_ftsCellPaddingDefaultLeft;
    }

    /**
     * Default left cell margin/padding units. 0 -- null; 1-2 -- not relevant; 3 -- twips..
     */
    @Internal
    public void setFtsCellPaddingDefaultLeft( byte field_47_ftsCellPaddingDefaultLeft )
    {
        this.field_47_ftsCellPaddingDefaultLeft = field_47_ftsCellPaddingDefaultLeft;
    }

    /**
     * Default bottom cell margin/padding units. 0 -- null; 1-2 -- not relevant; 3 -- twips..
     */
    @Internal
    public byte getFtsCellPaddingDefaultBottom()
    {
        return field_48_ftsCellPaddingDefaultBottom;
    }

    /**
     * Default bottom cell margin/padding units. 0 -- null; 1-2 -- not relevant; 3 -- twips..
     */
    @Internal
    public void setFtsCellPaddingDefaultBottom( byte field_48_ftsCellPaddingDefaultBottom )
    {
        this.field_48_ftsCellPaddingDefaultBottom = field_48_ftsCellPaddingDefaultBottom;
    }

    /**
     * Default right cell margin/padding units. 0 -- null; 1-2 -- not relevant; 3 -- twips..
     */
    @Internal
    public byte getFtsCellPaddingDefaultRight()
    {
        return field_49_ftsCellPaddingDefaultRight;
    }

    /**
     * Default right cell margin/padding units. 0 -- null; 1-2 -- not relevant; 3 -- twips..
     */
    @Internal
    public void setFtsCellPaddingDefaultRight( byte field_49_ftsCellPaddingDefaultRight )
    {
        this.field_49_ftsCellPaddingDefaultRight = field_49_ftsCellPaddingDefaultRight;
    }

    /**
     * Default top cell spacings.
     */
    @Internal
    public short getWCellSpacingDefaultTop()
    {
        return field_50_wCellSpacingDefaultTop;
    }

    /**
     * Default top cell spacings.
     */
    @Internal
    public void setWCellSpacingDefaultTop( short field_50_wCellSpacingDefaultTop )
    {
        this.field_50_wCellSpacingDefaultTop = field_50_wCellSpacingDefaultTop;
    }

    /**
     * Default left cell spacings.
     */
    @Internal
    public short getWCellSpacingDefaultLeft()
    {
        return field_51_wCellSpacingDefaultLeft;
    }

    /**
     * Default left cell spacings.
     */
    @Internal
    public void setWCellSpacingDefaultLeft( short field_51_wCellSpacingDefaultLeft )
    {
        this.field_51_wCellSpacingDefaultLeft = field_51_wCellSpacingDefaultLeft;
    }

    /**
     * Default bottom cell spacings.
     */
    @Internal
    public short getWCellSpacingDefaultBottom()
    {
        return field_52_wCellSpacingDefaultBottom;
    }

    /**
     * Default bottom cell spacings.
     */
    @Internal
    public void setWCellSpacingDefaultBottom( short field_52_wCellSpacingDefaultBottom )
    {
        this.field_52_wCellSpacingDefaultBottom = field_52_wCellSpacingDefaultBottom;
    }

    /**
     * Default right cell spacings.
     */
    @Internal
    public short getWCellSpacingDefaultRight()
    {
        return field_53_wCellSpacingDefaultRight;
    }

    /**
     * Default right cell spacings.
     */
    @Internal
    public void setWCellSpacingDefaultRight( short field_53_wCellSpacingDefaultRight )
    {
        this.field_53_wCellSpacingDefaultRight = field_53_wCellSpacingDefaultRight;
    }

    /**
     * Default top cell spacings units. 0 -- null; 1-2 -- not relevant; 3 -- twips..
     */
    @Internal
    public byte getFtsCellSpacingDefaultTop()
    {
        return field_54_ftsCellSpacingDefaultTop;
    }

    /**
     * Default top cell spacings units. 0 -- null; 1-2 -- not relevant; 3 -- twips..
     */
    @Internal
    public void setFtsCellSpacingDefaultTop( byte field_54_ftsCellSpacingDefaultTop )
    {
        this.field_54_ftsCellSpacingDefaultTop = field_54_ftsCellSpacingDefaultTop;
    }

    /**
     * Default left cell spacings units. 0 -- null; 1-2 -- not relevant; 3 -- twips..
     */
    @Internal
    public byte getFtsCellSpacingDefaultLeft()
    {
        return field_55_ftsCellSpacingDefaultLeft;
    }

    /**
     * Default left cell spacings units. 0 -- null; 1-2 -- not relevant; 3 -- twips..
     */
    @Internal
    public void setFtsCellSpacingDefaultLeft( byte field_55_ftsCellSpacingDefaultLeft )
    {
        this.field_55_ftsCellSpacingDefaultLeft = field_55_ftsCellSpacingDefaultLeft;
    }

    /**
     * Default bottom cell spacings units. 0 -- null; 1-2 -- not relevant; 3 -- twips..
     */
    @Internal
    public byte getFtsCellSpacingDefaultBottom()
    {
        return field_56_ftsCellSpacingDefaultBottom;
    }

    /**
     * Default bottom cell spacings units. 0 -- null; 1-2 -- not relevant; 3 -- twips..
     */
    @Internal
    public void setFtsCellSpacingDefaultBottom( byte field_56_ftsCellSpacingDefaultBottom )
    {
        this.field_56_ftsCellSpacingDefaultBottom = field_56_ftsCellSpacingDefaultBottom;
    }

    /**
     * Default right cell spacings units. 0 -- null; 1-2 -- not relevant; 3 -- twips..
     */
    @Internal
    public byte getFtsCellSpacingDefaultRight()
    {
        return field_57_ftsCellSpacingDefaultRight;
    }

    /**
     * Default right cell spacings units. 0 -- null; 1-2 -- not relevant; 3 -- twips..
     */
    @Internal
    public void setFtsCellSpacingDefaultRight( byte field_57_ftsCellSpacingDefaultRight )
    {
        this.field_57_ftsCellSpacingDefaultRight = field_57_ftsCellSpacingDefaultRight;
    }

    /**
     * Default outer top cell margin/padding.
     */
    @Internal
    public short getWCellPaddingOuterTop()
    {
        return field_58_wCellPaddingOuterTop;
    }

    /**
     * Default outer top cell margin/padding.
     */
    @Internal
    public void setWCellPaddingOuterTop( short field_58_wCellPaddingOuterTop )
    {
        this.field_58_wCellPaddingOuterTop = field_58_wCellPaddingOuterTop;
    }

    /**
     * Default outer left cell margin/padding.
     */
    @Internal
    public short getWCellPaddingOuterLeft()
    {
        return field_59_wCellPaddingOuterLeft;
    }

    /**
     * Default outer left cell margin/padding.
     */
    @Internal
    public void setWCellPaddingOuterLeft( short field_59_wCellPaddingOuterLeft )
    {
        this.field_59_wCellPaddingOuterLeft = field_59_wCellPaddingOuterLeft;
    }

    /**
     * Default outer bottom cell margin/padding.
     */
    @Internal
    public short getWCellPaddingOuterBottom()
    {
        return field_60_wCellPaddingOuterBottom;
    }

    /**
     * Default outer bottom cell margin/padding.
     */
    @Internal
    public void setWCellPaddingOuterBottom( short field_60_wCellPaddingOuterBottom )
    {
        this.field_60_wCellPaddingOuterBottom = field_60_wCellPaddingOuterBottom;
    }

    /**
     * Default outer right cell margin/padding.
     */
    @Internal
    public short getWCellPaddingOuterRight()
    {
        return field_61_wCellPaddingOuterRight;
    }

    /**
     * Default outer right cell margin/padding.
     */
    @Internal
    public void setWCellPaddingOuterRight( short field_61_wCellPaddingOuterRight )
    {
        this.field_61_wCellPaddingOuterRight = field_61_wCellPaddingOuterRight;
    }

    /**
     * Default outer top cell margin/padding units. 0 -- null; 1-2 -- not relevant; 3 -- twips..
     */
    @Internal
    public byte getFtsCellPaddingOuterTop()
    {
        return field_62_ftsCellPaddingOuterTop;
    }

    /**
     * Default outer top cell margin/padding units. 0 -- null; 1-2 -- not relevant; 3 -- twips..
     */
    @Internal
    public void setFtsCellPaddingOuterTop( byte field_62_ftsCellPaddingOuterTop )
    {
        this.field_62_ftsCellPaddingOuterTop = field_62_ftsCellPaddingOuterTop;
    }

    /**
     * Default outer left cell margin/padding units. 0 -- null; 1-2 -- not relevant; 3 -- twips..
     */
    @Internal
    public byte getFtsCellPaddingOuterLeft()
    {
        return field_63_ftsCellPaddingOuterLeft;
    }

    /**
     * Default outer left cell margin/padding units. 0 -- null; 1-2 -- not relevant; 3 -- twips..
     */
    @Internal
    public void setFtsCellPaddingOuterLeft( byte field_63_ftsCellPaddingOuterLeft )
    {
        this.field_63_ftsCellPaddingOuterLeft = field_63_ftsCellPaddingOuterLeft;
    }

    /**
     * Default outer bottom cell margin/padding units. 0 -- null; 1-2 -- not relevant; 3 -- twips..
     */
    @Internal
    public byte getFtsCellPaddingOuterBottom()
    {
        return field_64_ftsCellPaddingOuterBottom;
    }

    /**
     * Default outer bottom cell margin/padding units. 0 -- null; 1-2 -- not relevant; 3 -- twips..
     */
    @Internal
    public void setFtsCellPaddingOuterBottom( byte field_64_ftsCellPaddingOuterBottom )
    {
        this.field_64_ftsCellPaddingOuterBottom = field_64_ftsCellPaddingOuterBottom;
    }

    /**
     * Default outer right cell margin/padding units. 0 -- null; 1-2 -- not relevant; 3 -- twips..
     */
    @Internal
    public byte getFtsCellPaddingOuterRight()
    {
        return field_65_ftsCellPaddingOuterRight;
    }

    /**
     * Default outer right cell margin/padding units. 0 -- null; 1-2 -- not relevant; 3 -- twips..
     */
    @Internal
    public void setFtsCellPaddingOuterRight( byte field_65_ftsCellPaddingOuterRight )
    {
        this.field_65_ftsCellPaddingOuterRight = field_65_ftsCellPaddingOuterRight;
    }

    /**
     * Default outer top cell spacing.
     */
    @Internal
    public short getWCellSpacingOuterTop()
    {
        return field_66_wCellSpacingOuterTop;
    }

    /**
     * Default outer top cell spacing.
     */
    @Internal
    public void setWCellSpacingOuterTop( short field_66_wCellSpacingOuterTop )
    {
        this.field_66_wCellSpacingOuterTop = field_66_wCellSpacingOuterTop;
    }

    /**
     * Default outer left cell spacing.
     */
    @Internal
    public short getWCellSpacingOuterLeft()
    {
        return field_67_wCellSpacingOuterLeft;
    }

    /**
     * Default outer left cell spacing.
     */
    @Internal
    public void setWCellSpacingOuterLeft( short field_67_wCellSpacingOuterLeft )
    {
        this.field_67_wCellSpacingOuterLeft = field_67_wCellSpacingOuterLeft;
    }

    /**
     * Default outer bottom cell spacing.
     */
    @Internal
    public short getWCellSpacingOuterBottom()
    {
        return field_68_wCellSpacingOuterBottom;
    }

    /**
     * Default outer bottom cell spacing.
     */
    @Internal
    public void setWCellSpacingOuterBottom( short field_68_wCellSpacingOuterBottom )
    {
        this.field_68_wCellSpacingOuterBottom = field_68_wCellSpacingOuterBottom;
    }

    /**
     * Default outer right cell spacing.
     */
    @Internal
    public short getWCellSpacingOuterRight()
    {
        return field_69_wCellSpacingOuterRight;
    }

    /**
     * Default outer right cell spacing.
     */
    @Internal
    public void setWCellSpacingOuterRight( short field_69_wCellSpacingOuterRight )
    {
        this.field_69_wCellSpacingOuterRight = field_69_wCellSpacingOuterRight;
    }

    /**
     * Default outer top cell spacings units. 0 -- null; 1-2 -- not relevant; 3 -- twips..
     */
    @Internal
    public byte getFtsCellSpacingOuterTop()
    {
        return field_70_ftsCellSpacingOuterTop;
    }

    /**
     * Default outer top cell spacings units. 0 -- null; 1-2 -- not relevant; 3 -- twips..
     */
    @Internal
    public void setFtsCellSpacingOuterTop( byte field_70_ftsCellSpacingOuterTop )
    {
        this.field_70_ftsCellSpacingOuterTop = field_70_ftsCellSpacingOuterTop;
    }

    /**
     * Default outer left cell spacings units. 0 -- null; 1-2 -- not relevant; 3 -- twips..
     */
    @Internal
    public byte getFtsCellSpacingOuterLeft()
    {
        return field_71_ftsCellSpacingOuterLeft;
    }

    /**
     * Default outer left cell spacings units. 0 -- null; 1-2 -- not relevant; 3 -- twips..
     */
    @Internal
    public void setFtsCellSpacingOuterLeft( byte field_71_ftsCellSpacingOuterLeft )
    {
        this.field_71_ftsCellSpacingOuterLeft = field_71_ftsCellSpacingOuterLeft;
    }

    /**
     * Default outer bottom cell spacings units. 0 -- null; 1-2 -- not relevant; 3 -- twips..
     */
    @Internal
    public byte getFtsCellSpacingOuterBottom()
    {
        return field_72_ftsCellSpacingOuterBottom;
    }

    /**
     * Default outer bottom cell spacings units. 0 -- null; 1-2 -- not relevant; 3 -- twips..
     */
    @Internal
    public void setFtsCellSpacingOuterBottom( byte field_72_ftsCellSpacingOuterBottom )
    {
        this.field_72_ftsCellSpacingOuterBottom = field_72_ftsCellSpacingOuterBottom;
    }

    /**
     * Default outer right cell spacings units. 0 -- null; 1-2 -- not relevant; 3 -- twips..
     */
    @Internal
    public byte getFtsCellSpacingOuterRight()
    {
        return field_73_ftsCellSpacingOuterRight;
    }

    /**
     * Default outer right cell spacings units. 0 -- null; 1-2 -- not relevant; 3 -- twips..
     */
    @Internal
    public void setFtsCellSpacingOuterRight( byte field_73_ftsCellSpacingOuterRight )
    {
        this.field_73_ftsCellSpacingOuterRight = field_73_ftsCellSpacingOuterRight;
    }

    /**
     * Get the rgtc field for the TAP record.
     */
    @Internal
    public TableCellDescriptor[] getRgtc()
    {
        return field_74_rgtc;
    }

    /**
     * Set the rgtc field for the TAP record.
     */
    @Internal
    public void setRgtc( TableCellDescriptor[] field_74_rgtc )
    {
        this.field_74_rgtc = field_74_rgtc;
    }

    /**
     * Get the rgshd field for the TAP record.
     */
    @Internal
    public ShadingDescriptor[] getRgshd()
    {
        return field_75_rgshd;
    }

    /**
     * Set the rgshd field for the TAP record.
     */
    @Internal
    public void setRgshd( ShadingDescriptor[] field_75_rgshd )
    {
        this.field_75_rgshd = field_75_rgshd;
    }

    /**
     * Set to 1 if property revision.
     */
    @Internal
    public byte getFPropRMark()
    {
        return field_76_fPropRMark;
    }

    /**
     * Set to 1 if property revision.
     */
    @Internal
    public void setFPropRMark( byte field_76_fPropRMark )
    {
        this.field_76_fPropRMark = field_76_fPropRMark;
    }

    /**
     * Has old properties.
     */
    @Internal
    public byte getFHasOldProps()
    {
        return field_77_fHasOldProps;
    }

    /**
     * Has old properties.
     */
    @Internal
    public void setFHasOldProps( byte field_77_fHasOldProps )
    {
        this.field_77_fHasOldProps = field_77_fHasOldProps;
    }

    /**
     * Size of each horizontal style band, in number of rows.
     */
    @Internal
    public short getCHorzBands()
    {
        return field_78_cHorzBands;
    }

    /**
     * Size of each horizontal style band, in number of rows.
     */
    @Internal
    public void setCHorzBands( short field_78_cHorzBands )
    {
        this.field_78_cHorzBands = field_78_cHorzBands;
    }

    /**
     * Size of a vertical style band, in number of columns.
     */
    @Internal
    public short getCVertBands()
    {
        return field_79_cVertBands;
    }

    /**
     * Size of a vertical style band, in number of columns.
     */
    @Internal
    public void setCVertBands( short field_79_cVertBands )
    {
        this.field_79_cVertBands = field_79_cVertBands;
    }

    /**
     * Border definition for inside horizontal borders.
     */
    @Internal
    public BorderCode getRgbrcInsideDefault_0()
    {
        return field_80_rgbrcInsideDefault_0;
    }

    /**
     * Border definition for inside horizontal borders.
     */
    @Internal
    public void setRgbrcInsideDefault_0( BorderCode field_80_rgbrcInsideDefault_0 )
    {
        this.field_80_rgbrcInsideDefault_0 = field_80_rgbrcInsideDefault_0;
    }

    /**
     * Border definition for inside vertical borders.
     */
    @Internal
    public BorderCode getRgbrcInsideDefault_1()
    {
        return field_81_rgbrcInsideDefault_1;
    }

    /**
     * Border definition for inside vertical borders.
     */
    @Internal
    public void setRgbrcInsideDefault_1( BorderCode field_81_rgbrcInsideDefault_1 )
    {
        this.field_81_rgbrcInsideDefault_1 = field_81_rgbrcInsideDefault_1;
    }

    /**
     * Sets the fAutofit field value.
     * When set to 1, AutoFit this table
     */
    @Internal
    public void setFAutofit( boolean value )
    {
        field_13_widthAndFitsFlags = fAutofit.setBoolean(field_13_widthAndFitsFlags, value);
    }

    /**
     * When set to 1, AutoFit this table
     * @return  the fAutofit field value.
     */
    @Internal
    public boolean isFAutofit()
    {
        return fAutofit.isSet(field_13_widthAndFitsFlags);
    }

    /**
     * Sets the fKeepFollow field value.
     * When set to 1, keep this row with the following row
     */
    @Internal
    public void setFKeepFollow( boolean value )
    {
        field_13_widthAndFitsFlags = fKeepFollow.setBoolean(field_13_widthAndFitsFlags, value);
    }

    /**
     * When set to 1, keep this row with the following row
     * @return  the fKeepFollow field value.
     */
    @Internal
    public boolean isFKeepFollow()
    {
        return fKeepFollow.isSet(field_13_widthAndFitsFlags);
    }

    /**
     * Sets the ftsWidth field value.
     * Units for wWidth: 0 -- null; 1 -- auto, ignores wWidth, 2 -- percentage (in 50ths of a percent), 3 -- twips
     */
    @Internal
    public void setFtsWidth( byte value )
    {
        field_13_widthAndFitsFlags = ftsWidth.setValue(field_13_widthAndFitsFlags, value);
    }

    /**
     * Units for wWidth: 0 -- null; 1 -- auto, ignores wWidth, 2 -- percentage (in 50ths of a percent), 3 -- twips
     * @return  the ftsWidth field value.
     */
    @Internal
    public byte getFtsWidth()
    {
        return ( byte )ftsWidth.getValue(field_13_widthAndFitsFlags);
    }

    /**
     * Sets the ftsWidthIndent field value.
     * Units for wWidthIndent: 0 -- null; 1 -- auto, ignores wWidthIndent, 2 -- percentage (in 50ths of a percent), 3 -- twips
     */
    @Internal
    public void setFtsWidthIndent( byte value )
    {
        field_13_widthAndFitsFlags = ftsWidthIndent.setValue(field_13_widthAndFitsFlags, value);
    }

    /**
     * Units for wWidthIndent: 0 -- null; 1 -- auto, ignores wWidthIndent, 2 -- percentage (in 50ths of a percent), 3 -- twips
     * @return  the ftsWidthIndent field value.
     */
    @Internal
    public byte getFtsWidthIndent()
    {
        return ( byte )ftsWidthIndent.getValue(field_13_widthAndFitsFlags);
    }

    /**
     * Sets the ftsWidthBefore field value.
     * Units for wWidthBefore: 0 -- null; 1 -- auto, ignores wWidthBefore, 2 -- percentage (in 50ths of a percent), 3 -- twips
     */
    @Internal
    public void setFtsWidthBefore( byte value )
    {
        field_13_widthAndFitsFlags = ftsWidthBefore.setValue(field_13_widthAndFitsFlags, value);
    }

    /**
     * Units for wWidthBefore: 0 -- null; 1 -- auto, ignores wWidthBefore, 2 -- percentage (in 50ths of a percent), 3 -- twips
     * @return  the ftsWidthBefore field value.
     */
    @Internal
    public byte getFtsWidthBefore()
    {
        return ( byte )ftsWidthBefore.getValue(field_13_widthAndFitsFlags);
    }

    /**
     * Sets the ftsWidthAfter field value.
     * Units for wWidthAfter: 0 -- null; 1 -- auto, ignores wWidthAfter, 2 -- percentage (in 50ths of a percent), 3 -- twips
     */
    @Internal
    public void setFtsWidthAfter( byte value )
    {
        field_13_widthAndFitsFlags = ftsWidthAfter.setValue(field_13_widthAndFitsFlags, value);
    }

    /**
     * Units for wWidthAfter: 0 -- null; 1 -- auto, ignores wWidthAfter, 2 -- percentage (in 50ths of a percent), 3 -- twips
     * @return  the ftsWidthAfter field value.
     */
    @Internal
    public byte getFtsWidthAfter()
    {
        return ( byte )ftsWidthAfter.getValue(field_13_widthAndFitsFlags);
    }

    /**
     * Sets the fNeverBeenAutofit field value.
     * When 1, table has never been autofit
     */
    @Internal
    public void setFNeverBeenAutofit( boolean value )
    {
        field_13_widthAndFitsFlags = fNeverBeenAutofit.setBoolean(field_13_widthAndFitsFlags, value);
    }

    /**
     * When 1, table has never been autofit
     * @return  the fNeverBeenAutofit field value.
     */
    @Internal
    public boolean isFNeverBeenAutofit()
    {
        return fNeverBeenAutofit.isSet(field_13_widthAndFitsFlags);
    }

    /**
     * Sets the fInvalAutofit field value.
     * When 1, TAP is still valid, but autofit properties aren't
     */
    @Internal
    public void setFInvalAutofit( boolean value )
    {
        field_13_widthAndFitsFlags = fInvalAutofit.setBoolean(field_13_widthAndFitsFlags, value);
    }

    /**
     * When 1, TAP is still valid, but autofit properties aren't
     * @return  the fInvalAutofit field value.
     */
    @Internal
    public boolean isFInvalAutofit()
    {
        return fInvalAutofit.isSet(field_13_widthAndFitsFlags);
    }

    /**
     * Sets the widthAndFitsFlags_empty1 field value.
     * Not used
     */
    @Internal
    public void setWidthAndFitsFlags_empty1( byte value )
    {
        field_13_widthAndFitsFlags = widthAndFitsFlags_empty1.setValue(field_13_widthAndFitsFlags, value);
    }

    /**
     * Not used
     * @return  the widthAndFitsFlags_empty1 field value.
     */
    @Internal
    public byte getWidthAndFitsFlags_empty1()
    {
        return ( byte )widthAndFitsFlags_empty1.getValue(field_13_widthAndFitsFlags);
    }

    /**
     * Sets the fVert field value.
     * When 1, positioned in vertical text flow
     */
    @Internal
    public void setFVert( boolean value )
    {
        field_13_widthAndFitsFlags = fVert.setBoolean(field_13_widthAndFitsFlags, value);
    }

    /**
     * When 1, positioned in vertical text flow
     * @return  the fVert field value.
     */
    @Internal
    public boolean isFVert()
    {
        return fVert.isSet(field_13_widthAndFitsFlags);
    }

    /**
     * Sets the pcVert field value.
     * Vertical position code. Specifies coordinate frame to use when paragraphs are absolutely positioned. 0 -- vertical position coordinates are relative to margin; 1 -- coordinates are relative to page; 2 -- coordinates are relative to text. This means: relative to where the next non-APO text would have been placed if this APO did not exist.
     */
    @Internal
    public void setPcVert( byte value )
    {
        field_13_widthAndFitsFlags = pcVert.setValue(field_13_widthAndFitsFlags, value);
    }

    /**
     * Vertical position code. Specifies coordinate frame to use when paragraphs are absolutely positioned. 0 -- vertical position coordinates are relative to margin; 1 -- coordinates are relative to page; 2 -- coordinates are relative to text. This means: relative to where the next non-APO text would have been placed if this APO did not exist.
     * @return  the pcVert field value.
     */
    @Internal
    public byte getPcVert()
    {
        return ( byte )pcVert.getValue(field_13_widthAndFitsFlags);
    }

    /**
     * Sets the pcHorz field value.
     * Horizontal position code. Specifies coordinate frame to use when paragraphs are absolutely positioned. 0 -- horizontal position coordinates are relative to column; 1 -- coordinates are relative to margin; 2 -- coordinates are relative to page
     */
    @Internal
    public void setPcHorz( byte value )
    {
        field_13_widthAndFitsFlags = pcHorz.setValue(field_13_widthAndFitsFlags, value);
    }

    /**
     * Horizontal position code. Specifies coordinate frame to use when paragraphs are absolutely positioned. 0 -- horizontal position coordinates are relative to column; 1 -- coordinates are relative to margin; 2 -- coordinates are relative to page
     * @return  the pcHorz field value.
     */
    @Internal
    public byte getPcHorz()
    {
        return ( byte )pcHorz.getValue(field_13_widthAndFitsFlags);
    }

    /**
     * Sets the widthAndFitsFlags_empty2 field value.
     * Not used
     */
    @Internal
    public void setWidthAndFitsFlags_empty2( short value )
    {
        field_13_widthAndFitsFlags = widthAndFitsFlags_empty2.setValue(field_13_widthAndFitsFlags, value);
    }

    /**
     * Not used
     * @return  the widthAndFitsFlags_empty2 field value.
     */
    @Internal
    public short getWidthAndFitsFlags_empty2()
    {
        return ( short )widthAndFitsFlags_empty2.getValue(field_13_widthAndFitsFlags);
    }

    /**
     * Sets the fFirstRow field value.
     * Used internally by Word: first row
     */
    @Internal
    public void setFFirstRow( boolean value )
    {
        field_25_internalFlags = fFirstRow.setBoolean(field_25_internalFlags, value);
    }

    /**
     * Used internally by Word: first row
     * @return  the fFirstRow field value.
     */
    @Internal
    public boolean isFFirstRow()
    {
        return fFirstRow.isSet(field_25_internalFlags);
    }

    /**
     * Sets the fLastRow field value.
     * Used internally by Word: last row
     */
    @Internal
    public void setFLastRow( boolean value )
    {
        field_25_internalFlags = fLastRow.setBoolean(field_25_internalFlags, value);
    }

    /**
     * Used internally by Word: last row
     * @return  the fLastRow field value.
     */
    @Internal
    public boolean isFLastRow()
    {
        return fLastRow.isSet(field_25_internalFlags);
    }

    /**
     * Sets the fOutline field value.
     * Used internally by Word: row was cached for outline mode
     */
    @Internal
    public void setFOutline( boolean value )
    {
        field_25_internalFlags = fOutline.setBoolean(field_25_internalFlags, value);
    }

    /**
     * Used internally by Word: row was cached for outline mode
     * @return  the fOutline field value.
     */
    @Internal
    public boolean isFOutline()
    {
        return fOutline.isSet(field_25_internalFlags);
    }

    /**
     * Sets the fOrigWordTableRules field value.
     * Used internally by Word: table combining like Word 5.x for the Macintosh and WinWord 1.x
     */
    @Internal
    public void setFOrigWordTableRules( boolean value )
    {
        field_25_internalFlags = fOrigWordTableRules.setBoolean(field_25_internalFlags, value);
    }

    /**
     * Used internally by Word: table combining like Word 5.x for the Macintosh and WinWord 1.x
     * @return  the fOrigWordTableRules field value.
     */
    @Internal
    public boolean isFOrigWordTableRules()
    {
        return fOrigWordTableRules.isSet(field_25_internalFlags);
    }

    /**
     * Sets the fCellSpacing field value.
     * Used internally by Word: When set to 1 cell spacing is allowed
     */
    @Internal
    public void setFCellSpacing( boolean value )
    {
        field_25_internalFlags = fCellSpacing.setBoolean(field_25_internalFlags, value);
    }

    /**
     * Used internally by Word: When set to 1 cell spacing is allowed
     * @return  the fCellSpacing field value.
     */
    @Internal
    public boolean isFCellSpacing()
    {
        return fCellSpacing.isSet(field_25_internalFlags);
    }

    /**
     * Sets the grpfTap_unused field value.
     * Not used
     */
    @Internal
    public void setGrpfTap_unused( short value )
    {
        field_25_internalFlags = grpfTap_unused.setValue(field_25_internalFlags, value);
    }

    /**
     * Not used
     * @return  the grpfTap_unused field value.
     */
    @Internal
    public short getGrpfTap_unused()
    {
        return ( short )grpfTap_unused.getValue(field_25_internalFlags);
    }

    /**
     * Sets the fWrapToWwd field value.
     * Used internally by Word: Wrap to window is on when set to 1
     */
    @Internal
    public void setFWrapToWwd( boolean value )
    {
        field_32_viewFlags = fWrapToWwd.setBoolean(field_32_viewFlags, value);
    }

    /**
     * Used internally by Word: Wrap to window is on when set to 1
     * @return  the fWrapToWwd field value.
     */
    @Internal
    public boolean isFWrapToWwd()
    {
        return fWrapToWwd.isSet(field_32_viewFlags);
    }

    /**
     * Sets the fNotPageView field value.
     * Used internally by Word: when set to 1 we are not in Page View
     */
    @Internal
    public void setFNotPageView( boolean value )
    {
        field_32_viewFlags = fNotPageView.setBoolean(field_32_viewFlags, value);
    }

    /**
     * Used internally by Word: when set to 1 we are not in Page View
     * @return  the fNotPageView field value.
     */
    @Internal
    public boolean isFNotPageView()
    {
        return fNotPageView.isSet(field_32_viewFlags);
    }

    /**
     * Sets the viewFlags_unused1 field value.
     * Not used
     */
    @Internal
    public void setViewFlags_unused1( boolean value )
    {
        field_32_viewFlags = viewFlags_unused1.setBoolean(field_32_viewFlags, value);
    }

    /**
     * Not used
     * @return  the viewFlags_unused1 field value.
     */
    @Internal
    public boolean isViewFlags_unused1()
    {
        return viewFlags_unused1.isSet(field_32_viewFlags);
    }

    /**
     * Sets the fWebView field value.
     * Used internally by Word: Web View is on when set to 1
     */
    @Internal
    public void setFWebView( boolean value )
    {
        field_32_viewFlags = fWebView.setBoolean(field_32_viewFlags, value);
    }

    /**
     * Used internally by Word: Web View is on when set to 1
     * @return  the fWebView field value.
     */
    @Internal
    public boolean isFWebView()
    {
        return fWebView.isSet(field_32_viewFlags);
    }

    /**
     * Sets the fAdjusted field value.
     * Used internally by Word
     */
    @Internal
    public void setFAdjusted( boolean value )
    {
        field_32_viewFlags = fAdjusted.setBoolean(field_32_viewFlags, value);
    }

    /**
     * Used internally by Word
     * @return  the fAdjusted field value.
     */
    @Internal
    public boolean isFAdjusted()
    {
        return fAdjusted.isSet(field_32_viewFlags);
    }

    /**
     * Sets the viewFlags_unused2 field value.
     * Not used
     */
    @Internal
    public void setViewFlags_unused2( short value )
    {
        field_32_viewFlags = viewFlags_unused2.setValue(field_32_viewFlags, value);
    }

    /**
     * Not used
     * @return  the viewFlags_unused2 field value.
     */
    @Internal
    public short getViewFlags_unused2()
    {
        return ( short )viewFlags_unused2.getValue(field_32_viewFlags);
    }

}  // END OF CLASS
