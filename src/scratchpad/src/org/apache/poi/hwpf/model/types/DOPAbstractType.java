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


import org.apache.poi.hdf.model.hdftypes.HDFType;
import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.LittleEndian;

/**
 * Document Properties.
 * NOTE: This source is automatically generated please do not modify this file.  Either subclass or
 *       remove the record in src/records/definitions.
 *
 * @author S. Ryan Ackley
 */
public abstract class DOPAbstractType implements HDFType {

    protected  byte field_1_formatFlags;
        private static BitField  fFacingPages = BitFieldFactory.getInstance(0x01);
        private static BitField  fWidowControl = BitFieldFactory.getInstance(0x02);
        private static BitField  fPMHMainDoc = BitFieldFactory.getInstance(0x04);
        private static BitField  grfSupression = BitFieldFactory.getInstance(0x18);
        private static BitField  fpc = BitFieldFactory.getInstance(0x60);
        private static BitField  unused1 = BitFieldFactory.getInstance(0x80);
    protected  byte field_2_unused2;
    protected  short field_3_footnoteInfo;
        private static BitField  rncFtn = BitFieldFactory.getInstance(0x0003);
        private static BitField  nFtn = BitFieldFactory.getInstance(0xfffc);
    protected  byte field_4_fOutlineDirtySave;
    protected  byte field_5_docinfo;
        private static BitField  fOnlyMacPics = BitFieldFactory.getInstance(0x01);
        private static BitField  fOnlyWinPics = BitFieldFactory.getInstance(0x02);
        private static BitField  fLabelDoc = BitFieldFactory.getInstance(0x04);
        private static BitField  fHyphCapitals = BitFieldFactory.getInstance(0x08);
        private static BitField  fAutoHyphen = BitFieldFactory.getInstance(0x10);
        private static BitField  fFormNoFields = BitFieldFactory.getInstance(0x20);
        private static BitField  fLinkStyles = BitFieldFactory.getInstance(0x40);
        private static BitField  fRevMarking = BitFieldFactory.getInstance(0x80);
    protected  byte field_6_docinfo1;
        private static BitField  fBackup = BitFieldFactory.getInstance(0x01);
        private static BitField  fExactCWords = BitFieldFactory.getInstance(0x02);
        private static BitField  fPagHidden = BitFieldFactory.getInstance(0x04);
        private static BitField  fPagResults = BitFieldFactory.getInstance(0x08);
        private static BitField  fLockAtn = BitFieldFactory.getInstance(0x10);
        private static BitField  fMirrorMargins = BitFieldFactory.getInstance(0x20);
        private static BitField  unused3 = BitFieldFactory.getInstance(0x40);
        private static BitField  fDfltTrueType = BitFieldFactory.getInstance(0x80);
    protected  byte field_7_docinfo2;
        private static BitField  fPagSupressTopSpacing = BitFieldFactory.getInstance(0x01);
        private static BitField  fProtEnabled = BitFieldFactory.getInstance(0x02);
        private static BitField  fDispFormFldSel = BitFieldFactory.getInstance(0x04);
        private static BitField  fRMView = BitFieldFactory.getInstance(0x08);
        private static BitField  fRMPrint = BitFieldFactory.getInstance(0x10);
        private static BitField  unused4 = BitFieldFactory.getInstance(0x20);
        private static BitField  fLockRev = BitFieldFactory.getInstance(0x40);
        private static BitField  fEmbedFonts = BitFieldFactory.getInstance(0x80);
    protected  short field_8_docinfo3;
        private static BitField  oldfNoTabForInd = BitFieldFactory.getInstance(0x0001);
        private static BitField  oldfNoSpaceRaiseLower = BitFieldFactory.getInstance(0x0002);
        private static BitField  oldfSuppressSpbfAfterPageBreak = BitFieldFactory.getInstance(0x0004);
        private static BitField  oldfWrapTrailSpaces = BitFieldFactory.getInstance(0x0008);
        private static BitField  oldfMapPrintTextColor = BitFieldFactory.getInstance(0x0010);
        private static BitField  oldfNoColumnBalance = BitFieldFactory.getInstance(0x0020);
        private static BitField  oldfConvMailMergeEsc = BitFieldFactory.getInstance(0x0040);
        private static BitField  oldfSupressTopSpacing = BitFieldFactory.getInstance(0x0080);
        private static BitField  oldfOrigWordTableRules = BitFieldFactory.getInstance(0x0100);
        private static BitField  oldfTransparentMetafiles = BitFieldFactory.getInstance(0x0200);
        private static BitField  oldfShowBreaksInFrames = BitFieldFactory.getInstance(0x0400);
        private static BitField  oldfSwapBordersFacingPgs = BitFieldFactory.getInstance(0x0800);
        private static BitField  unused5 = BitFieldFactory.getInstance(0xf000);
    protected  int field_9_dxaTab;
    protected  int field_10_wSpare;
    protected  int field_11_dxaHotz;
    protected  int field_12_cConsexHypLim;
    protected  int field_13_wSpare2;
    protected  int field_14_dttmCreated;
    protected  int field_15_dttmRevised;
    protected  int field_16_dttmLastPrint;
    protected  int field_17_nRevision;
    protected  int field_18_tmEdited;
    protected  int field_19_cWords;
    protected  int field_20_cCh;
    protected  int field_21_cPg;
    protected  int field_22_cParas;
    protected  short field_23_Edn;
        private static BitField  rncEdn = BitFieldFactory.getInstance(0x0003);
        private static BitField  nEdn = BitFieldFactory.getInstance(0xfffc);
    protected  short field_24_Edn1;
        private static BitField  epc = BitFieldFactory.getInstance(0x0003);
        private static BitField  nfcFtnRef1 = BitFieldFactory.getInstance(0x003c);
        private static BitField  nfcEdnRef1 = BitFieldFactory.getInstance(0x03c0);
        private static BitField  fPrintFormData = BitFieldFactory.getInstance(0x0400);
        private static BitField  fSaveFormData = BitFieldFactory.getInstance(0x0800);
        private static BitField  fShadeFormData = BitFieldFactory.getInstance(0x1000);
        private static BitField  fWCFtnEdn = BitFieldFactory.getInstance(0x8000);
    protected  int field_25_cLines;
    protected  int field_26_cWordsFtnEnd;
    protected  int field_27_cChFtnEdn;
    protected  short field_28_cPgFtnEdn;
    protected  int field_29_cParasFtnEdn;
    protected  int field_30_cLinesFtnEdn;
    protected  int field_31_lKeyProtDoc;
    protected  short field_32_view;
        private static BitField  wvkSaved = BitFieldFactory.getInstance(0x0007);
        private static BitField  wScaleSaved = BitFieldFactory.getInstance(0x0ff8);
        private static BitField  zkSaved = BitFieldFactory.getInstance(0x3000);
        private static BitField  fRotateFontW6 = BitFieldFactory.getInstance(0x4000);
        private static BitField  iGutterPos = BitFieldFactory.getInstance(0x8000);
    protected  int field_33_docinfo4;
        private static BitField  fNoTabForInd = BitFieldFactory.getInstance(0x00000001);
        private static BitField  fNoSpaceRaiseLower = BitFieldFactory.getInstance(0x00000002);
        private static BitField  fSupressSpdfAfterPageBreak = BitFieldFactory.getInstance(0x00000004);
        private static BitField  fWrapTrailSpaces = BitFieldFactory.getInstance(0x00000008);
        private static BitField  fMapPrintTextColor = BitFieldFactory.getInstance(0x00000010);
        private static BitField  fNoColumnBalance = BitFieldFactory.getInstance(0x00000020);
        private static BitField  fConvMailMergeEsc = BitFieldFactory.getInstance(0x00000040);
        private static BitField  fSupressTopSpacing = BitFieldFactory.getInstance(0x00000080);
        private static BitField  fOrigWordTableRules = BitFieldFactory.getInstance(0x00000100);
        private static BitField  fTransparentMetafiles = BitFieldFactory.getInstance(0x00000200);
        private static BitField  fShowBreaksInFrames = BitFieldFactory.getInstance(0x00000400);
        private static BitField  fSwapBordersFacingPgs = BitFieldFactory.getInstance(0x00000800);
        private static BitField  fSuppressTopSPacingMac5 = BitFieldFactory.getInstance(0x00010000);
        private static BitField  fTruncDxaExpand = BitFieldFactory.getInstance(0x00020000);
        private static BitField  fPrintBodyBeforeHdr = BitFieldFactory.getInstance(0x00040000);
        private static BitField  fNoLeading = BitFieldFactory.getInstance(0x00080000);
        private static BitField  fMWSmallCaps = BitFieldFactory.getInstance(0x00200000);
    protected  short field_34_adt;
    protected  byte[] field_35_doptypography;
    protected  byte[] field_36_dogrid;
    protected  short field_37_docinfo5;
        private static BitField  lvl = BitFieldFactory.getInstance(0x001e);
        private static BitField  fGramAllDone = BitFieldFactory.getInstance(0x0020);
        private static BitField  fGramAllClean = BitFieldFactory.getInstance(0x0040);
        private static BitField  fSubsetFonts = BitFieldFactory.getInstance(0x0080);
        private static BitField  fHideLastVersion = BitFieldFactory.getInstance(0x0100);
        private static BitField  fHtmlDoc = BitFieldFactory.getInstance(0x0200);
        private static BitField  fSnapBorder = BitFieldFactory.getInstance(0x0800);
        private static BitField  fIncludeHeader = BitFieldFactory.getInstance(0x1000);
        private static BitField  fIncludeFooter = BitFieldFactory.getInstance(0x2000);
        private static BitField  fForcePageSizePag = BitFieldFactory.getInstance(0x4000);
        private static BitField  fMinFontSizePag = BitFieldFactory.getInstance(0x8000);
    protected  short field_38_docinfo6;
        private static BitField  fHaveVersions = BitFieldFactory.getInstance(0x0001);
        private static BitField  fAutoVersions = BitFieldFactory.getInstance(0x0002);
    protected  byte[] field_39_asumyi;
    protected  int field_40_cChWS;
    protected  int field_41_cChWSFtnEdn;
    protected  int field_42_grfDocEvents;
    protected  int field_43_virusinfo;
        private static BitField  fVirusPrompted = BitFieldFactory.getInstance(0x0001);
        private static BitField  fVirusLoadSafe = BitFieldFactory.getInstance(0x0002);
        private static BitField  KeyVirusSession30 = BitFieldFactory.getInstance(0xfffffffc);
    protected  byte[] field_44_Spare;
    protected  int field_45_reserved1;
    protected  int field_46_reserved2;
    protected  int field_47_cDBC;
    protected  int field_48_cDBCFtnEdn;
    protected  int field_49_reserved;
    protected  short field_50_nfcFtnRef;
    protected  short field_51_nfcEdnRef;
    protected  short field_52_hpsZoonFontPag;
    protected  short field_53_dywDispPag;


    public DOPAbstractType()
    {

    }

    protected void fillFields(byte [] data, int offset)
    {
        field_1_formatFlags             = data[ 0x0 + offset ];
        field_2_unused2                 = data[ 0x1 + offset ];
        field_3_footnoteInfo            = LittleEndian.getShort(data, 0x2 + offset);
        field_4_fOutlineDirtySave       = data[ 0x4 + offset ];
        field_5_docinfo                 = data[ 0x5 + offset ];
        field_6_docinfo1                = data[ 0x6 + offset ];
        field_7_docinfo2                = data[ 0x7 + offset ];
        field_8_docinfo3                = LittleEndian.getShort(data, 0x8 + offset);
        field_9_dxaTab                  = LittleEndian.getShort(data, 0xa + offset);
        field_10_wSpare                 = LittleEndian.getShort(data, 0xc + offset);
        field_11_dxaHotz                = LittleEndian.getShort(data, 0xe + offset);
        field_12_cConsexHypLim          = LittleEndian.getShort(data, 0x10 + offset);
        field_13_wSpare2                = LittleEndian.getShort(data, 0x12 + offset);
        field_14_dttmCreated            = LittleEndian.getInt(data, 0x14 + offset);
        field_15_dttmRevised            = LittleEndian.getInt(data, 0x18 + offset);
        field_16_dttmLastPrint          = LittleEndian.getInt(data, 0x1c + offset);
        field_17_nRevision              = LittleEndian.getShort(data, 0x20 + offset);
        field_18_tmEdited               = LittleEndian.getInt(data, 0x22 + offset);
        field_19_cWords                 = LittleEndian.getInt(data, 0x26 + offset);
        field_20_cCh                    = LittleEndian.getInt(data, 0x2a + offset);
        field_21_cPg                    = LittleEndian.getShort(data, 0x2e + offset);
        field_22_cParas                 = LittleEndian.getInt(data, 0x30 + offset);
        field_23_Edn                    = LittleEndian.getShort(data, 0x34 + offset);
        field_24_Edn1                   = LittleEndian.getShort(data, 0x36 + offset);
        field_25_cLines                 = LittleEndian.getInt(data, 0x38 + offset);
        field_26_cWordsFtnEnd           = LittleEndian.getInt(data, 0x3c + offset);
        field_27_cChFtnEdn              = LittleEndian.getInt(data, 0x40 + offset);
        field_28_cPgFtnEdn              = LittleEndian.getShort(data, 0x44 + offset);
        field_29_cParasFtnEdn           = LittleEndian.getInt(data, 0x46 + offset);
        field_30_cLinesFtnEdn           = LittleEndian.getInt(data, 0x4a + offset);
        field_31_lKeyProtDoc            = LittleEndian.getInt(data, 0x4e + offset);
        field_32_view                   = LittleEndian.getShort(data, 0x52 + offset);
        field_33_docinfo4               = LittleEndian.getInt(data, 0x54 + offset);
        field_34_adt                    = LittleEndian.getShort(data, 0x58 + offset);
        field_35_doptypography          = LittleEndian.getByteArray(data, 0x5a + offset,310);
        field_36_dogrid                 = LittleEndian.getByteArray(data, 0x190 + offset,10);
        field_37_docinfo5               = LittleEndian.getShort(data, 0x19a + offset);
        field_38_docinfo6               = LittleEndian.getShort(data, 0x19c + offset);
        field_39_asumyi                 = LittleEndian.getByteArray(data, 0x19e + offset,12);
        field_40_cChWS                  = LittleEndian.getInt(data, 0x1aa + offset);
        field_41_cChWSFtnEdn            = LittleEndian.getInt(data, 0x1ae + offset);
        field_42_grfDocEvents           = LittleEndian.getInt(data, 0x1b2 + offset);
        field_43_virusinfo              = LittleEndian.getInt(data, 0x1b6 + offset);
        field_44_Spare                  = LittleEndian.getByteArray(data, 0x1ba + offset,30);
        field_45_reserved1              = LittleEndian.getInt(data, 0x1d8 + offset);
        field_46_reserved2              = LittleEndian.getInt(data, 0x1dc + offset);
        field_47_cDBC                   = LittleEndian.getInt(data, 0x1e0 + offset);
        field_48_cDBCFtnEdn             = LittleEndian.getInt(data, 0x1e4 + offset);
        field_49_reserved               = LittleEndian.getInt(data, 0x1e8 + offset);
        field_50_nfcFtnRef              = LittleEndian.getShort(data, 0x1ec + offset);
        field_51_nfcEdnRef              = LittleEndian.getShort(data, 0x1ee + offset);
        field_52_hpsZoonFontPag         = LittleEndian.getShort(data, 0x1f0 + offset);
        field_53_dywDispPag             = LittleEndian.getShort(data, 0x1f2 + offset);
    }

    public void serialize(byte[] data, int offset)
    {
        data[ 0x0 + offset] = field_1_formatFlags;
        data[ 0x1 + offset] = field_2_unused2;
        LittleEndian.putShort(data, 0x2 + offset, field_3_footnoteInfo);
        data[ 0x4 + offset] = field_4_fOutlineDirtySave;
        data[ 0x5 + offset] = field_5_docinfo;
        data[ 0x6 + offset] = field_6_docinfo1;
        data[ 0x7 + offset] = field_7_docinfo2;
        LittleEndian.putShort(data, 0x8 + offset, field_8_docinfo3);
        LittleEndian.putShort(data, 0xa + offset, (short)field_9_dxaTab);
        LittleEndian.putShort(data, 0xc + offset, (short)field_10_wSpare);
        LittleEndian.putShort(data, 0xe + offset, (short)field_11_dxaHotz);
        LittleEndian.putShort(data, 0x10 + offset, (short)field_12_cConsexHypLim);
        LittleEndian.putShort(data, 0x12 + offset, (short)field_13_wSpare2);
        LittleEndian.putInt(data, 0x14 + offset, field_14_dttmCreated);
        LittleEndian.putInt(data, 0x18 + offset, field_15_dttmRevised);
        LittleEndian.putInt(data, 0x1c + offset, field_16_dttmLastPrint);
        LittleEndian.putShort(data, 0x20 + offset, (short)field_17_nRevision);
        LittleEndian.putInt(data, 0x22 + offset, field_18_tmEdited);
        LittleEndian.putInt(data, 0x26 + offset, field_19_cWords);
        LittleEndian.putInt(data, 0x2a + offset, field_20_cCh);
        LittleEndian.putShort(data, 0x2e + offset, (short)field_21_cPg);
        LittleEndian.putInt(data, 0x30 + offset, field_22_cParas);
        LittleEndian.putShort(data, 0x34 + offset, field_23_Edn);
        LittleEndian.putShort(data, 0x36 + offset, field_24_Edn1);
        LittleEndian.putInt(data, 0x38 + offset, field_25_cLines);
        LittleEndian.putInt(data, 0x3c + offset, field_26_cWordsFtnEnd);
        LittleEndian.putInt(data, 0x40 + offset, field_27_cChFtnEdn);
        LittleEndian.putShort(data, 0x44 + offset, field_28_cPgFtnEdn);
        LittleEndian.putInt(data, 0x46 + offset, field_29_cParasFtnEdn);
        LittleEndian.putInt(data, 0x4a + offset, field_30_cLinesFtnEdn);
        LittleEndian.putInt(data, 0x4e + offset, field_31_lKeyProtDoc);
        LittleEndian.putShort(data, 0x52 + offset, field_32_view);
        LittleEndian.putInt(data, 0x54 + offset, field_33_docinfo4);
        LittleEndian.putShort(data, 0x58 + offset, field_34_adt);
        System.arraycopy(field_35_doptypography, 0, data, 0x5a + offset, field_35_doptypography.length);
        System.arraycopy(field_36_dogrid, 0, data, 0x190 + offset, field_36_dogrid.length);
        LittleEndian.putShort(data, 0x19a + offset, field_37_docinfo5);
        LittleEndian.putShort(data, 0x19c + offset, field_38_docinfo6);
        System.arraycopy(field_39_asumyi, 0, data, 0x19e + offset, field_39_asumyi.length);
        LittleEndian.putInt(data, 0x1aa + offset, field_40_cChWS);
        LittleEndian.putInt(data, 0x1ae + offset, field_41_cChWSFtnEdn);
        LittleEndian.putInt(data, 0x1b2 + offset, field_42_grfDocEvents);
        LittleEndian.putInt(data, 0x1b6 + offset, field_43_virusinfo);
        System.arraycopy(field_44_Spare, 0, data, 0x1ba + offset, field_44_Spare.length);
        LittleEndian.putInt(data, 0x1d8 + offset, field_45_reserved1);
        LittleEndian.putInt(data, 0x1dc + offset, field_46_reserved2);
        LittleEndian.putInt(data, 0x1e0 + offset, field_47_cDBC);
        LittleEndian.putInt(data, 0x1e4 + offset, field_48_cDBCFtnEdn);
        LittleEndian.putInt(data, 0x1e8 + offset, field_49_reserved);
        LittleEndian.putShort(data, 0x1ec + offset, field_50_nfcFtnRef);
        LittleEndian.putShort(data, 0x1ee + offset, field_51_nfcEdnRef);
        LittleEndian.putShort(data, 0x1f0 + offset, field_52_hpsZoonFontPag);
        LittleEndian.putShort(data, 0x1f2 + offset, field_53_dywDispPag);
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[DOP]\n");

        buffer.append("    .formatFlags          = ");
        buffer.append(" (").append(getFormatFlags()).append(" )\n");
        buffer.append("         .fFacingPages             = ").append(isFFacingPages()).append('\n');
        buffer.append("         .fWidowControl            = ").append(isFWidowControl()).append('\n');
        buffer.append("         .fPMHMainDoc              = ").append(isFPMHMainDoc()).append('\n');
        buffer.append("         .grfSupression            = ").append(getGrfSupression()).append('\n');
        buffer.append("         .fpc                      = ").append(getFpc()).append('\n');
        buffer.append("         .unused1                  = ").append(isUnused1()).append('\n');

        buffer.append("    .unused2              = ");
        buffer.append(" (").append(getUnused2()).append(" )\n");

        buffer.append("    .footnoteInfo         = ");
        buffer.append(" (").append(getFootnoteInfo()).append(" )\n");
        buffer.append("         .rncFtn                   = ").append(getRncFtn()).append('\n');
        buffer.append("         .nFtn                     = ").append(getNFtn()).append('\n');

        buffer.append("    .fOutlineDirtySave    = ");
        buffer.append(" (").append(getFOutlineDirtySave()).append(" )\n");

        buffer.append("    .docinfo              = ");
        buffer.append(" (").append(getDocinfo()).append(" )\n");
        buffer.append("         .fOnlyMacPics             = ").append(isFOnlyMacPics()).append('\n');
        buffer.append("         .fOnlyWinPics             = ").append(isFOnlyWinPics()).append('\n');
        buffer.append("         .fLabelDoc                = ").append(isFLabelDoc()).append('\n');
        buffer.append("         .fHyphCapitals            = ").append(isFHyphCapitals()).append('\n');
        buffer.append("         .fAutoHyphen              = ").append(isFAutoHyphen()).append('\n');
        buffer.append("         .fFormNoFields            = ").append(isFFormNoFields()).append('\n');
        buffer.append("         .fLinkStyles              = ").append(isFLinkStyles()).append('\n');
        buffer.append("         .fRevMarking              = ").append(isFRevMarking()).append('\n');

        buffer.append("    .docinfo1             = ");
        buffer.append(" (").append(getDocinfo1()).append(" )\n");
        buffer.append("         .fBackup                  = ").append(isFBackup()).append('\n');
        buffer.append("         .fExactCWords             = ").append(isFExactCWords()).append('\n');
        buffer.append("         .fPagHidden               = ").append(isFPagHidden()).append('\n');
        buffer.append("         .fPagResults              = ").append(isFPagResults()).append('\n');
        buffer.append("         .fLockAtn                 = ").append(isFLockAtn()).append('\n');
        buffer.append("         .fMirrorMargins           = ").append(isFMirrorMargins()).append('\n');
        buffer.append("         .unused3                  = ").append(isUnused3()).append('\n');
        buffer.append("         .fDfltTrueType            = ").append(isFDfltTrueType()).append('\n');

        buffer.append("    .docinfo2             = ");
        buffer.append(" (").append(getDocinfo2()).append(" )\n");
        buffer.append("         .fPagSupressTopSpacing     = ").append(isFPagSupressTopSpacing()).append('\n');
        buffer.append("         .fProtEnabled             = ").append(isFProtEnabled()).append('\n');
        buffer.append("         .fDispFormFldSel          = ").append(isFDispFormFldSel()).append('\n');
        buffer.append("         .fRMView                  = ").append(isFRMView()).append('\n');
        buffer.append("         .fRMPrint                 = ").append(isFRMPrint()).append('\n');
        buffer.append("         .unused4                  = ").append(isUnused4()).append('\n');
        buffer.append("         .fLockRev                 = ").append(isFLockRev()).append('\n');
        buffer.append("         .fEmbedFonts              = ").append(isFEmbedFonts()).append('\n');

        buffer.append("    .docinfo3             = ");
        buffer.append(" (").append(getDocinfo3()).append(" )\n");
        buffer.append("         .oldfNoTabForInd          = ").append(isOldfNoTabForInd()).append('\n');
        buffer.append("         .oldfNoSpaceRaiseLower     = ").append(isOldfNoSpaceRaiseLower()).append('\n');
        buffer.append("         .oldfSuppressSpbfAfterPageBreak     = ").append(isOldfSuppressSpbfAfterPageBreak()).append('\n');
        buffer.append("         .oldfWrapTrailSpaces      = ").append(isOldfWrapTrailSpaces()).append('\n');
        buffer.append("         .oldfMapPrintTextColor     = ").append(isOldfMapPrintTextColor()).append('\n');
        buffer.append("         .oldfNoColumnBalance      = ").append(isOldfNoColumnBalance()).append('\n');
        buffer.append("         .oldfConvMailMergeEsc     = ").append(isOldfConvMailMergeEsc()).append('\n');
        buffer.append("         .oldfSupressTopSpacing     = ").append(isOldfSupressTopSpacing()).append('\n');
        buffer.append("         .oldfOrigWordTableRules     = ").append(isOldfOrigWordTableRules()).append('\n');
        buffer.append("         .oldfTransparentMetafiles     = ").append(isOldfTransparentMetafiles()).append('\n');
        buffer.append("         .oldfShowBreaksInFrames     = ").append(isOldfShowBreaksInFrames()).append('\n');
        buffer.append("         .oldfSwapBordersFacingPgs     = ").append(isOldfSwapBordersFacingPgs()).append('\n');
        buffer.append("         .unused5                  = ").append(getUnused5()).append('\n');

        buffer.append("    .dxaTab               = ");
        buffer.append(" (").append(getDxaTab()).append(" )\n");

        buffer.append("    .wSpare               = ");
        buffer.append(" (").append(getWSpare()).append(" )\n");

        buffer.append("    .dxaHotz              = ");
        buffer.append(" (").append(getDxaHotz()).append(" )\n");

        buffer.append("    .cConsexHypLim        = ");
        buffer.append(" (").append(getCConsexHypLim()).append(" )\n");

        buffer.append("    .wSpare2              = ");
        buffer.append(" (").append(getWSpare2()).append(" )\n");

        buffer.append("    .dttmCreated          = ");
        buffer.append(" (").append(getDttmCreated()).append(" )\n");

        buffer.append("    .dttmRevised          = ");
        buffer.append(" (").append(getDttmRevised()).append(" )\n");

        buffer.append("    .dttmLastPrint        = ");
        buffer.append(" (").append(getDttmLastPrint()).append(" )\n");

        buffer.append("    .nRevision            = ");
        buffer.append(" (").append(getNRevision()).append(" )\n");

        buffer.append("    .tmEdited             = ");
        buffer.append(" (").append(getTmEdited()).append(" )\n");

        buffer.append("    .cWords               = ");
        buffer.append(" (").append(getCWords()).append(" )\n");

        buffer.append("    .cCh                  = ");
        buffer.append(" (").append(getCCh()).append(" )\n");

        buffer.append("    .cPg                  = ");
        buffer.append(" (").append(getCPg()).append(" )\n");

        buffer.append("    .cParas               = ");
        buffer.append(" (").append(getCParas()).append(" )\n");

        buffer.append("    .Edn                  = ");
        buffer.append(" (").append(getEdn()).append(" )\n");
        buffer.append("         .rncEdn                   = ").append(getRncEdn()).append('\n');
        buffer.append("         .nEdn                     = ").append(getNEdn()).append('\n');

        buffer.append("    .Edn1                 = ");
        buffer.append(" (").append(getEdn1()).append(" )\n");
        buffer.append("         .epc                      = ").append(getEpc()).append('\n');
        buffer.append("         .nfcFtnRef1               = ").append(getNfcFtnRef1()).append('\n');
        buffer.append("         .nfcEdnRef1               = ").append(getNfcEdnRef1()).append('\n');
        buffer.append("         .fPrintFormData           = ").append(isFPrintFormData()).append('\n');
        buffer.append("         .fSaveFormData            = ").append(isFSaveFormData()).append('\n');
        buffer.append("         .fShadeFormData           = ").append(isFShadeFormData()).append('\n');
        buffer.append("         .fWCFtnEdn                = ").append(isFWCFtnEdn()).append('\n');

        buffer.append("    .cLines               = ");
        buffer.append(" (").append(getCLines()).append(" )\n");

        buffer.append("    .cWordsFtnEnd         = ");
        buffer.append(" (").append(getCWordsFtnEnd()).append(" )\n");

        buffer.append("    .cChFtnEdn            = ");
        buffer.append(" (").append(getCChFtnEdn()).append(" )\n");

        buffer.append("    .cPgFtnEdn            = ");
        buffer.append(" (").append(getCPgFtnEdn()).append(" )\n");

        buffer.append("    .cParasFtnEdn         = ");
        buffer.append(" (").append(getCParasFtnEdn()).append(" )\n");

        buffer.append("    .cLinesFtnEdn         = ");
        buffer.append(" (").append(getCLinesFtnEdn()).append(" )\n");

        buffer.append("    .lKeyProtDoc          = ");
        buffer.append(" (").append(getLKeyProtDoc()).append(" )\n");

        buffer.append("    .view                 = ");
        buffer.append(" (").append(getView()).append(" )\n");
        buffer.append("         .wvkSaved                 = ").append(getWvkSaved()).append('\n');
        buffer.append("         .wScaleSaved              = ").append(getWScaleSaved()).append('\n');
        buffer.append("         .zkSaved                  = ").append(getZkSaved()).append('\n');
        buffer.append("         .fRotateFontW6            = ").append(isFRotateFontW6()).append('\n');
        buffer.append("         .iGutterPos               = ").append(isIGutterPos()).append('\n');

        buffer.append("    .docinfo4             = ");
        buffer.append(" (").append(getDocinfo4()).append(" )\n");
        buffer.append("         .fNoTabForInd             = ").append(isFNoTabForInd()).append('\n');
        buffer.append("         .fNoSpaceRaiseLower       = ").append(isFNoSpaceRaiseLower()).append('\n');
        buffer.append("         .fSupressSpdfAfterPageBreak     = ").append(isFSupressSpdfAfterPageBreak()).append('\n');
        buffer.append("         .fWrapTrailSpaces         = ").append(isFWrapTrailSpaces()).append('\n');
        buffer.append("         .fMapPrintTextColor       = ").append(isFMapPrintTextColor()).append('\n');
        buffer.append("         .fNoColumnBalance         = ").append(isFNoColumnBalance()).append('\n');
        buffer.append("         .fConvMailMergeEsc        = ").append(isFConvMailMergeEsc()).append('\n');
        buffer.append("         .fSupressTopSpacing       = ").append(isFSupressTopSpacing()).append('\n');
        buffer.append("         .fOrigWordTableRules      = ").append(isFOrigWordTableRules()).append('\n');
        buffer.append("         .fTransparentMetafiles     = ").append(isFTransparentMetafiles()).append('\n');
        buffer.append("         .fShowBreaksInFrames      = ").append(isFShowBreaksInFrames()).append('\n');
        buffer.append("         .fSwapBordersFacingPgs     = ").append(isFSwapBordersFacingPgs()).append('\n');
        buffer.append("         .fSuppressTopSPacingMac5     = ").append(isFSuppressTopSPacingMac5()).append('\n');
        buffer.append("         .fTruncDxaExpand          = ").append(isFTruncDxaExpand()).append('\n');
        buffer.append("         .fPrintBodyBeforeHdr      = ").append(isFPrintBodyBeforeHdr()).append('\n');
        buffer.append("         .fNoLeading               = ").append(isFNoLeading()).append('\n');
        buffer.append("         .fMWSmallCaps             = ").append(isFMWSmallCaps()).append('\n');

        buffer.append("    .adt                  = ");
        buffer.append(" (").append(getAdt()).append(" )\n");

        buffer.append("    .doptypography        = ");
        buffer.append(" (").append(getDoptypography()).append(" )\n");

        buffer.append("    .dogrid               = ");
        buffer.append(" (").append(getDogrid()).append(" )\n");

        buffer.append("    .docinfo5             = ");
        buffer.append(" (").append(getDocinfo5()).append(" )\n");
        buffer.append("         .lvl                      = ").append(getLvl()).append('\n');
        buffer.append("         .fGramAllDone             = ").append(isFGramAllDone()).append('\n');
        buffer.append("         .fGramAllClean            = ").append(isFGramAllClean()).append('\n');
        buffer.append("         .fSubsetFonts             = ").append(isFSubsetFonts()).append('\n');
        buffer.append("         .fHideLastVersion         = ").append(isFHideLastVersion()).append('\n');
        buffer.append("         .fHtmlDoc                 = ").append(isFHtmlDoc()).append('\n');
        buffer.append("         .fSnapBorder              = ").append(isFSnapBorder()).append('\n');
        buffer.append("         .fIncludeHeader           = ").append(isFIncludeHeader()).append('\n');
        buffer.append("         .fIncludeFooter           = ").append(isFIncludeFooter()).append('\n');
        buffer.append("         .fForcePageSizePag        = ").append(isFForcePageSizePag()).append('\n');
        buffer.append("         .fMinFontSizePag          = ").append(isFMinFontSizePag()).append('\n');

        buffer.append("    .docinfo6             = ");
        buffer.append(" (").append(getDocinfo6()).append(" )\n");
        buffer.append("         .fHaveVersions            = ").append(isFHaveVersions()).append('\n');
        buffer.append("         .fAutoVersions            = ").append(isFAutoVersions()).append('\n');

        buffer.append("    .asumyi               = ");
        buffer.append(" (").append(getAsumyi()).append(" )\n");

        buffer.append("    .cChWS                = ");
        buffer.append(" (").append(getCChWS()).append(" )\n");

        buffer.append("    .cChWSFtnEdn          = ");
        buffer.append(" (").append(getCChWSFtnEdn()).append(" )\n");

        buffer.append("    .grfDocEvents         = ");
        buffer.append(" (").append(getGrfDocEvents()).append(" )\n");

        buffer.append("    .virusinfo            = ");
        buffer.append(" (").append(getVirusinfo()).append(" )\n");
        buffer.append("         .fVirusPrompted           = ").append(isFVirusPrompted()).append('\n');
        buffer.append("         .fVirusLoadSafe           = ").append(isFVirusLoadSafe()).append('\n');
        buffer.append("         .KeyVirusSession30        = ").append(getKeyVirusSession30()).append('\n');

        buffer.append("    .Spare                = ");
        buffer.append(" (").append(getSpare()).append(" )\n");

        buffer.append("    .reserved1            = ");
        buffer.append(" (").append(getReserved1()).append(" )\n");

        buffer.append("    .reserved2            = ");
        buffer.append(" (").append(getReserved2()).append(" )\n");

        buffer.append("    .cDBC                 = ");
        buffer.append(" (").append(getCDBC()).append(" )\n");

        buffer.append("    .cDBCFtnEdn           = ");
        buffer.append(" (").append(getCDBCFtnEdn()).append(" )\n");

        buffer.append("    .reserved             = ");
        buffer.append(" (").append(getReserved()).append(" )\n");

        buffer.append("    .nfcFtnRef            = ");
        buffer.append(" (").append(getNfcFtnRef()).append(" )\n");

        buffer.append("    .nfcEdnRef            = ");
        buffer.append(" (").append(getNfcEdnRef()).append(" )\n");

        buffer.append("    .hpsZoonFontPag       = ");
        buffer.append(" (").append(getHpsZoonFontPag()).append(" )\n");

        buffer.append("    .dywDispPag           = ");
        buffer.append(" (").append(getDywDispPag()).append(" )\n");

        buffer.append("[/DOP]\n");
        return buffer.toString();
    }

    /**
     * Size of record (exluding 4 byte header)
     */
    public int getSize()
    {
        return 4 +  + 1 + 1 + 2 + 1 + 1 + 1 + 1 + 2 + 2 + 2 + 2 + 2 + 2 + 4 + 4 + 4 + 2 + 4 + 4 + 4 + 2 + 4 + 2 + 2 + 4 + 4 + 4 + 2 + 4 + 4 + 4 + 2 + 4 + 2 + 310 + 10 + 2 + 2 + 12 + 4 + 4 + 4 + 4 + 30 + 4 + 4 + 4 + 4 + 4 + 2 + 2 + 2 + 2;
    }



    /**
     * Get the formatFlags field for the DOP record.
     */
    public byte getFormatFlags()
    {
        return field_1_formatFlags;
    }

    /**
     * Set the formatFlags field for the DOP record.
     */
    public void setFormatFlags(byte field_1_formatFlags)
    {
        this.field_1_formatFlags = field_1_formatFlags;
    }

    /**
     * Get the unused2 field for the DOP record.
     */
    public byte getUnused2()
    {
        return field_2_unused2;
    }

    /**
     * Set the unused2 field for the DOP record.
     */
    public void setUnused2(byte field_2_unused2)
    {
        this.field_2_unused2 = field_2_unused2;
    }

    /**
     * Get the footnoteInfo field for the DOP record.
     */
    public short getFootnoteInfo()
    {
        return field_3_footnoteInfo;
    }

    /**
     * Set the footnoteInfo field for the DOP record.
     */
    public void setFootnoteInfo(short field_3_footnoteInfo)
    {
        this.field_3_footnoteInfo = field_3_footnoteInfo;
    }

    /**
     * Get the fOutlineDirtySave field for the DOP record.
     */
    public byte getFOutlineDirtySave()
    {
        return field_4_fOutlineDirtySave;
    }

    /**
     * Set the fOutlineDirtySave field for the DOP record.
     */
    public void setFOutlineDirtySave(byte field_4_fOutlineDirtySave)
    {
        this.field_4_fOutlineDirtySave = field_4_fOutlineDirtySave;
    }

    /**
     * Get the docinfo field for the DOP record.
     */
    public byte getDocinfo()
    {
        return field_5_docinfo;
    }

    /**
     * Set the docinfo field for the DOP record.
     */
    public void setDocinfo(byte field_5_docinfo)
    {
        this.field_5_docinfo = field_5_docinfo;
    }

    /**
     * Get the docinfo1 field for the DOP record.
     */
    public byte getDocinfo1()
    {
        return field_6_docinfo1;
    }

    /**
     * Set the docinfo1 field for the DOP record.
     */
    public void setDocinfo1(byte field_6_docinfo1)
    {
        this.field_6_docinfo1 = field_6_docinfo1;
    }

    /**
     * Get the docinfo2 field for the DOP record.
     */
    public byte getDocinfo2()
    {
        return field_7_docinfo2;
    }

    /**
     * Set the docinfo2 field for the DOP record.
     */
    public void setDocinfo2(byte field_7_docinfo2)
    {
        this.field_7_docinfo2 = field_7_docinfo2;
    }

    /**
     * Get the docinfo3 field for the DOP record.
     */
    public short getDocinfo3()
    {
        return field_8_docinfo3;
    }

    /**
     * Set the docinfo3 field for the DOP record.
     */
    public void setDocinfo3(short field_8_docinfo3)
    {
        this.field_8_docinfo3 = field_8_docinfo3;
    }

    /**
     * Get the dxaTab field for the DOP record.
     */
    public int getDxaTab()
    {
        return field_9_dxaTab;
    }

    /**
     * Set the dxaTab field for the DOP record.
     */
    public void setDxaTab(int field_9_dxaTab)
    {
        this.field_9_dxaTab = field_9_dxaTab;
    }

    /**
     * Get the wSpare field for the DOP record.
     */
    public int getWSpare()
    {
        return field_10_wSpare;
    }

    /**
     * Set the wSpare field for the DOP record.
     */
    public void setWSpare(int field_10_wSpare)
    {
        this.field_10_wSpare = field_10_wSpare;
    }

    /**
     * Get the dxaHotz field for the DOP record.
     */
    public int getDxaHotz()
    {
        return field_11_dxaHotz;
    }

    /**
     * Set the dxaHotz field for the DOP record.
     */
    public void setDxaHotz(int field_11_dxaHotz)
    {
        this.field_11_dxaHotz = field_11_dxaHotz;
    }

    /**
     * Get the cConsexHypLim field for the DOP record.
     */
    public int getCConsexHypLim()
    {
        return field_12_cConsexHypLim;
    }

    /**
     * Set the cConsexHypLim field for the DOP record.
     */
    public void setCConsexHypLim(int field_12_cConsexHypLim)
    {
        this.field_12_cConsexHypLim = field_12_cConsexHypLim;
    }

    /**
     * Get the wSpare2 field for the DOP record.
     */
    public int getWSpare2()
    {
        return field_13_wSpare2;
    }

    /**
     * Set the wSpare2 field for the DOP record.
     */
    public void setWSpare2(int field_13_wSpare2)
    {
        this.field_13_wSpare2 = field_13_wSpare2;
    }

    /**
     * Get the dttmCreated field for the DOP record.
     */
    public int getDttmCreated()
    {
        return field_14_dttmCreated;
    }

    /**
     * Set the dttmCreated field for the DOP record.
     */
    public void setDttmCreated(int field_14_dttmCreated)
    {
        this.field_14_dttmCreated = field_14_dttmCreated;
    }

    /**
     * Get the dttmRevised field for the DOP record.
     */
    public int getDttmRevised()
    {
        return field_15_dttmRevised;
    }

    /**
     * Set the dttmRevised field for the DOP record.
     */
    public void setDttmRevised(int field_15_dttmRevised)
    {
        this.field_15_dttmRevised = field_15_dttmRevised;
    }

    /**
     * Get the dttmLastPrint field for the DOP record.
     */
    public int getDttmLastPrint()
    {
        return field_16_dttmLastPrint;
    }

    /**
     * Set the dttmLastPrint field for the DOP record.
     */
    public void setDttmLastPrint(int field_16_dttmLastPrint)
    {
        this.field_16_dttmLastPrint = field_16_dttmLastPrint;
    }

    /**
     * Get the nRevision field for the DOP record.
     */
    public int getNRevision()
    {
        return field_17_nRevision;
    }

    /**
     * Set the nRevision field for the DOP record.
     */
    public void setNRevision(int field_17_nRevision)
    {
        this.field_17_nRevision = field_17_nRevision;
    }

    /**
     * Get the tmEdited field for the DOP record.
     */
    public int getTmEdited()
    {
        return field_18_tmEdited;
    }

    /**
     * Set the tmEdited field for the DOP record.
     */
    public void setTmEdited(int field_18_tmEdited)
    {
        this.field_18_tmEdited = field_18_tmEdited;
    }

    /**
     * Get the cWords field for the DOP record.
     */
    public int getCWords()
    {
        return field_19_cWords;
    }

    /**
     * Set the cWords field for the DOP record.
     */
    public void setCWords(int field_19_cWords)
    {
        this.field_19_cWords = field_19_cWords;
    }

    /**
     * Get the cCh field for the DOP record.
     */
    public int getCCh()
    {
        return field_20_cCh;
    }

    /**
     * Set the cCh field for the DOP record.
     */
    public void setCCh(int field_20_cCh)
    {
        this.field_20_cCh = field_20_cCh;
    }

    /**
     * Get the cPg field for the DOP record.
     */
    public int getCPg()
    {
        return field_21_cPg;
    }

    /**
     * Set the cPg field for the DOP record.
     */
    public void setCPg(int field_21_cPg)
    {
        this.field_21_cPg = field_21_cPg;
    }

    /**
     * Get the cParas field for the DOP record.
     */
    public int getCParas()
    {
        return field_22_cParas;
    }

    /**
     * Set the cParas field for the DOP record.
     */
    public void setCParas(int field_22_cParas)
    {
        this.field_22_cParas = field_22_cParas;
    }

    /**
     * Get the Edn field for the DOP record.
     */
    public short getEdn()
    {
        return field_23_Edn;
    }

    /**
     * Set the Edn field for the DOP record.
     */
    public void setEdn(short field_23_Edn)
    {
        this.field_23_Edn = field_23_Edn;
    }

    /**
     * Get the Edn1 field for the DOP record.
     */
    public short getEdn1()
    {
        return field_24_Edn1;
    }

    /**
     * Set the Edn1 field for the DOP record.
     */
    public void setEdn1(short field_24_Edn1)
    {
        this.field_24_Edn1 = field_24_Edn1;
    }

    /**
     * Get the cLines field for the DOP record.
     */
    public int getCLines()
    {
        return field_25_cLines;
    }

    /**
     * Set the cLines field for the DOP record.
     */
    public void setCLines(int field_25_cLines)
    {
        this.field_25_cLines = field_25_cLines;
    }

    /**
     * Get the cWordsFtnEnd field for the DOP record.
     */
    public int getCWordsFtnEnd()
    {
        return field_26_cWordsFtnEnd;
    }

    /**
     * Set the cWordsFtnEnd field for the DOP record.
     */
    public void setCWordsFtnEnd(int field_26_cWordsFtnEnd)
    {
        this.field_26_cWordsFtnEnd = field_26_cWordsFtnEnd;
    }

    /**
     * Get the cChFtnEdn field for the DOP record.
     */
    public int getCChFtnEdn()
    {
        return field_27_cChFtnEdn;
    }

    /**
     * Set the cChFtnEdn field for the DOP record.
     */
    public void setCChFtnEdn(int field_27_cChFtnEdn)
    {
        this.field_27_cChFtnEdn = field_27_cChFtnEdn;
    }

    /**
     * Get the cPgFtnEdn field for the DOP record.
     */
    public short getCPgFtnEdn()
    {
        return field_28_cPgFtnEdn;
    }

    /**
     * Set the cPgFtnEdn field for the DOP record.
     */
    public void setCPgFtnEdn(short field_28_cPgFtnEdn)
    {
        this.field_28_cPgFtnEdn = field_28_cPgFtnEdn;
    }

    /**
     * Get the cParasFtnEdn field for the DOP record.
     */
    public int getCParasFtnEdn()
    {
        return field_29_cParasFtnEdn;
    }

    /**
     * Set the cParasFtnEdn field for the DOP record.
     */
    public void setCParasFtnEdn(int field_29_cParasFtnEdn)
    {
        this.field_29_cParasFtnEdn = field_29_cParasFtnEdn;
    }

    /**
     * Get the cLinesFtnEdn field for the DOP record.
     */
    public int getCLinesFtnEdn()
    {
        return field_30_cLinesFtnEdn;
    }

    /**
     * Set the cLinesFtnEdn field for the DOP record.
     */
    public void setCLinesFtnEdn(int field_30_cLinesFtnEdn)
    {
        this.field_30_cLinesFtnEdn = field_30_cLinesFtnEdn;
    }

    /**
     * Get the lKeyProtDoc field for the DOP record.
     */
    public int getLKeyProtDoc()
    {
        return field_31_lKeyProtDoc;
    }

    /**
     * Set the lKeyProtDoc field for the DOP record.
     */
    public void setLKeyProtDoc(int field_31_lKeyProtDoc)
    {
        this.field_31_lKeyProtDoc = field_31_lKeyProtDoc;
    }

    /**
     * Get the view field for the DOP record.
     */
    public short getView()
    {
        return field_32_view;
    }

    /**
     * Set the view field for the DOP record.
     */
    public void setView(short field_32_view)
    {
        this.field_32_view = field_32_view;
    }

    /**
     * Get the docinfo4 field for the DOP record.
     */
    public int getDocinfo4()
    {
        return field_33_docinfo4;
    }

    /**
     * Set the docinfo4 field for the DOP record.
     */
    public void setDocinfo4(int field_33_docinfo4)
    {
        this.field_33_docinfo4 = field_33_docinfo4;
    }

    /**
     * Get the adt field for the DOP record.
     */
    public short getAdt()
    {
        return field_34_adt;
    }

    /**
     * Set the adt field for the DOP record.
     */
    public void setAdt(short field_34_adt)
    {
        this.field_34_adt = field_34_adt;
    }

    /**
     * Get the doptypography field for the DOP record.
     */
    public byte[] getDoptypography()
    {
        return field_35_doptypography;
    }

    /**
     * Set the doptypography field for the DOP record.
     */
    public void setDoptypography(byte[] field_35_doptypography)
    {
        this.field_35_doptypography = field_35_doptypography;
    }

    /**
     * Get the dogrid field for the DOP record.
     */
    public byte[] getDogrid()
    {
        return field_36_dogrid;
    }

    /**
     * Set the dogrid field for the DOP record.
     */
    public void setDogrid(byte[] field_36_dogrid)
    {
        this.field_36_dogrid = field_36_dogrid;
    }

    /**
     * Get the docinfo5 field for the DOP record.
     */
    public short getDocinfo5()
    {
        return field_37_docinfo5;
    }

    /**
     * Set the docinfo5 field for the DOP record.
     */
    public void setDocinfo5(short field_37_docinfo5)
    {
        this.field_37_docinfo5 = field_37_docinfo5;
    }

    /**
     * Get the docinfo6 field for the DOP record.
     */
    public short getDocinfo6()
    {
        return field_38_docinfo6;
    }

    /**
     * Set the docinfo6 field for the DOP record.
     */
    public void setDocinfo6(short field_38_docinfo6)
    {
        this.field_38_docinfo6 = field_38_docinfo6;
    }

    /**
     * Get the asumyi field for the DOP record.
     */
    public byte[] getAsumyi()
    {
        return field_39_asumyi;
    }

    /**
     * Set the asumyi field for the DOP record.
     */
    public void setAsumyi(byte[] field_39_asumyi)
    {
        this.field_39_asumyi = field_39_asumyi;
    }

    /**
     * Get the cChWS field for the DOP record.
     */
    public int getCChWS()
    {
        return field_40_cChWS;
    }

    /**
     * Set the cChWS field for the DOP record.
     */
    public void setCChWS(int field_40_cChWS)
    {
        this.field_40_cChWS = field_40_cChWS;
    }

    /**
     * Get the cChWSFtnEdn field for the DOP record.
     */
    public int getCChWSFtnEdn()
    {
        return field_41_cChWSFtnEdn;
    }

    /**
     * Set the cChWSFtnEdn field for the DOP record.
     */
    public void setCChWSFtnEdn(int field_41_cChWSFtnEdn)
    {
        this.field_41_cChWSFtnEdn = field_41_cChWSFtnEdn;
    }

    /**
     * Get the grfDocEvents field for the DOP record.
     */
    public int getGrfDocEvents()
    {
        return field_42_grfDocEvents;
    }

    /**
     * Set the grfDocEvents field for the DOP record.
     */
    public void setGrfDocEvents(int field_42_grfDocEvents)
    {
        this.field_42_grfDocEvents = field_42_grfDocEvents;
    }

    /**
     * Get the virusinfo field for the DOP record.
     */
    public int getVirusinfo()
    {
        return field_43_virusinfo;
    }

    /**
     * Set the virusinfo field for the DOP record.
     */
    public void setVirusinfo(int field_43_virusinfo)
    {
        this.field_43_virusinfo = field_43_virusinfo;
    }

    /**
     * Get the Spare field for the DOP record.
     */
    public byte[] getSpare()
    {
        return field_44_Spare;
    }

    /**
     * Set the Spare field for the DOP record.
     */
    public void setSpare(byte[] field_44_Spare)
    {
        this.field_44_Spare = field_44_Spare;
    }

    /**
     * Get the reserved1 field for the DOP record.
     */
    public int getReserved1()
    {
        return field_45_reserved1;
    }

    /**
     * Set the reserved1 field for the DOP record.
     */
    public void setReserved1(int field_45_reserved1)
    {
        this.field_45_reserved1 = field_45_reserved1;
    }

    /**
     * Get the reserved2 field for the DOP record.
     */
    public int getReserved2()
    {
        return field_46_reserved2;
    }

    /**
     * Set the reserved2 field for the DOP record.
     */
    public void setReserved2(int field_46_reserved2)
    {
        this.field_46_reserved2 = field_46_reserved2;
    }

    /**
     * Get the cDBC field for the DOP record.
     */
    public int getCDBC()
    {
        return field_47_cDBC;
    }

    /**
     * Set the cDBC field for the DOP record.
     */
    public void setCDBC(int field_47_cDBC)
    {
        this.field_47_cDBC = field_47_cDBC;
    }

    /**
     * Get the cDBCFtnEdn field for the DOP record.
     */
    public int getCDBCFtnEdn()
    {
        return field_48_cDBCFtnEdn;
    }

    /**
     * Set the cDBCFtnEdn field for the DOP record.
     */
    public void setCDBCFtnEdn(int field_48_cDBCFtnEdn)
    {
        this.field_48_cDBCFtnEdn = field_48_cDBCFtnEdn;
    }

    /**
     * Get the reserved field for the DOP record.
     */
    public int getReserved()
    {
        return field_49_reserved;
    }

    /**
     * Set the reserved field for the DOP record.
     */
    public void setReserved(int field_49_reserved)
    {
        this.field_49_reserved = field_49_reserved;
    }

    /**
     * Get the nfcFtnRef field for the DOP record.
     */
    public short getNfcFtnRef()
    {
        return field_50_nfcFtnRef;
    }

    /**
     * Set the nfcFtnRef field for the DOP record.
     */
    public void setNfcFtnRef(short field_50_nfcFtnRef)
    {
        this.field_50_nfcFtnRef = field_50_nfcFtnRef;
    }

    /**
     * Get the nfcEdnRef field for the DOP record.
     */
    public short getNfcEdnRef()
    {
        return field_51_nfcEdnRef;
    }

    /**
     * Set the nfcEdnRef field for the DOP record.
     */
    public void setNfcEdnRef(short field_51_nfcEdnRef)
    {
        this.field_51_nfcEdnRef = field_51_nfcEdnRef;
    }

    /**
     * Get the hpsZoonFontPag field for the DOP record.
     */
    public short getHpsZoonFontPag()
    {
        return field_52_hpsZoonFontPag;
    }

    /**
     * Set the hpsZoonFontPag field for the DOP record.
     */
    public void setHpsZoonFontPag(short field_52_hpsZoonFontPag)
    {
        this.field_52_hpsZoonFontPag = field_52_hpsZoonFontPag;
    }

    /**
     * Get the dywDispPag field for the DOP record.
     */
    public short getDywDispPag()
    {
        return field_53_dywDispPag;
    }

    /**
     * Set the dywDispPag field for the DOP record.
     */
    public void setDywDispPag(short field_53_dywDispPag)
    {
        this.field_53_dywDispPag = field_53_dywDispPag;
    }

    /**
     * Sets the fFacingPages field value.
     *
     */
    public void setFFacingPages(boolean value)
    {
        field_1_formatFlags = (byte)fFacingPages.setBoolean(field_1_formatFlags, value);
    }

    /**
     *
     * @return  the fFacingPages field value.
     */
    public boolean isFFacingPages()
    {
        return fFacingPages.isSet(field_1_formatFlags);
    }

    /**
     * Sets the fWidowControl field value.
     *
     */
    public void setFWidowControl(boolean value)
    {
        field_1_formatFlags = (byte)fWidowControl.setBoolean(field_1_formatFlags, value);
    }

    /**
     *
     * @return  the fWidowControl field value.
     */
    public boolean isFWidowControl()
    {
        return fWidowControl.isSet(field_1_formatFlags);
    }

    /**
     * Sets the fPMHMainDoc field value.
     *
     */
    public void setFPMHMainDoc(boolean value)
    {
        field_1_formatFlags = (byte)fPMHMainDoc.setBoolean(field_1_formatFlags, value);
    }

    /**
     *
     * @return  the fPMHMainDoc field value.
     */
    public boolean isFPMHMainDoc()
    {
        return fPMHMainDoc.isSet(field_1_formatFlags);
    }

    /**
     * Sets the grfSupression field value.
     *
     */
    public void setGrfSupression(byte value)
    {
        field_1_formatFlags = (byte)grfSupression.setValue(field_1_formatFlags, value);
    }

    /**
     *
     * @return  the grfSupression field value.
     */
    public byte getGrfSupression()
    {
        return ( byte )grfSupression.getValue(field_1_formatFlags);
    }

    /**
     * Sets the fpc field value.
     *
     */
    public void setFpc(byte value)
    {
        field_1_formatFlags = (byte)fpc.setValue(field_1_formatFlags, value);
    }

    /**
     *
     * @return  the fpc field value.
     */
    public byte getFpc()
    {
        return ( byte )fpc.getValue(field_1_formatFlags);
    }

    /**
     * Sets the unused1 field value.
     *
     */
    public void setUnused1(boolean value)
    {
        field_1_formatFlags = (byte)unused1.setBoolean(field_1_formatFlags, value);
    }

    /**
     *
     * @return  the unused1 field value.
     */
    public boolean isUnused1()
    {
        return unused1.isSet(field_1_formatFlags);
    }

    /**
     * Sets the rncFtn field value.
     *
     */
    public void setRncFtn(byte value)
    {
        field_3_footnoteInfo = (short)rncFtn.setValue(field_3_footnoteInfo, value);
    }

    /**
     *
     * @return  the rncFtn field value.
     */
    public byte getRncFtn()
    {
        return ( byte )rncFtn.getValue(field_3_footnoteInfo);
    }

    /**
     * Sets the nFtn field value.
     *
     */
    public void setNFtn(short value)
    {
        field_3_footnoteInfo = (short)nFtn.setValue(field_3_footnoteInfo, value);
    }

    /**
     *
     * @return  the nFtn field value.
     */
    public short getNFtn()
    {
        return ( short )nFtn.getValue(field_3_footnoteInfo);
    }

    /**
     * Sets the fOnlyMacPics field value.
     *
     */
    public void setFOnlyMacPics(boolean value)
    {
        field_5_docinfo = (byte)fOnlyMacPics.setBoolean(field_5_docinfo, value);
    }

    /**
     *
     * @return  the fOnlyMacPics field value.
     */
    public boolean isFOnlyMacPics()
    {
        return fOnlyMacPics.isSet(field_5_docinfo);
    }

    /**
     * Sets the fOnlyWinPics field value.
     *
     */
    public void setFOnlyWinPics(boolean value)
    {
        field_5_docinfo = (byte)fOnlyWinPics.setBoolean(field_5_docinfo, value);
    }

    /**
     *
     * @return  the fOnlyWinPics field value.
     */
    public boolean isFOnlyWinPics()
    {
        return fOnlyWinPics.isSet(field_5_docinfo);
    }

    /**
     * Sets the fLabelDoc field value.
     *
     */
    public void setFLabelDoc(boolean value)
    {
        field_5_docinfo = (byte)fLabelDoc.setBoolean(field_5_docinfo, value);
    }

    /**
     *
     * @return  the fLabelDoc field value.
     */
    public boolean isFLabelDoc()
    {
        return fLabelDoc.isSet(field_5_docinfo);
    }

    /**
     * Sets the fHyphCapitals field value.
     *
     */
    public void setFHyphCapitals(boolean value)
    {
        field_5_docinfo = (byte)fHyphCapitals.setBoolean(field_5_docinfo, value);
    }

    /**
     *
     * @return  the fHyphCapitals field value.
     */
    public boolean isFHyphCapitals()
    {
        return fHyphCapitals.isSet(field_5_docinfo);
    }

    /**
     * Sets the fAutoHyphen field value.
     *
     */
    public void setFAutoHyphen(boolean value)
    {
        field_5_docinfo = (byte)fAutoHyphen.setBoolean(field_5_docinfo, value);
    }

    /**
     *
     * @return  the fAutoHyphen field value.
     */
    public boolean isFAutoHyphen()
    {
        return fAutoHyphen.isSet(field_5_docinfo);
    }

    /**
     * Sets the fFormNoFields field value.
     *
     */
    public void setFFormNoFields(boolean value)
    {
        field_5_docinfo = (byte)fFormNoFields.setBoolean(field_5_docinfo, value);
    }

    /**
     *
     * @return  the fFormNoFields field value.
     */
    public boolean isFFormNoFields()
    {
        return fFormNoFields.isSet(field_5_docinfo);
    }

    /**
     * Sets the fLinkStyles field value.
     *
     */
    public void setFLinkStyles(boolean value)
    {
        field_5_docinfo = (byte)fLinkStyles.setBoolean(field_5_docinfo, value);
    }

    /**
     *
     * @return  the fLinkStyles field value.
     */
    public boolean isFLinkStyles()
    {
        return fLinkStyles.isSet(field_5_docinfo);
    }

    /**
     * Sets the fRevMarking field value.
     *
     */
    public void setFRevMarking(boolean value)
    {
        field_5_docinfo = (byte)fRevMarking.setBoolean(field_5_docinfo, value);
    }

    /**
     *
     * @return  the fRevMarking field value.
     */
    public boolean isFRevMarking()
    {
        return fRevMarking.isSet(field_5_docinfo);
    }

    /**
     * Sets the fBackup field value.
     *
     */
    public void setFBackup(boolean value)
    {
        field_6_docinfo1 = (byte)fBackup.setBoolean(field_6_docinfo1, value);
    }

    /**
     *
     * @return  the fBackup field value.
     */
    public boolean isFBackup()
    {
        return fBackup.isSet(field_6_docinfo1);
    }

    /**
     * Sets the fExactCWords field value.
     *
     */
    public void setFExactCWords(boolean value)
    {
        field_6_docinfo1 = (byte)fExactCWords.setBoolean(field_6_docinfo1, value);
    }

    /**
     *
     * @return  the fExactCWords field value.
     */
    public boolean isFExactCWords()
    {
        return fExactCWords.isSet(field_6_docinfo1);
    }

    /**
     * Sets the fPagHidden field value.
     *
     */
    public void setFPagHidden(boolean value)
    {
        field_6_docinfo1 = (byte)fPagHidden.setBoolean(field_6_docinfo1, value);
    }

    /**
     *
     * @return  the fPagHidden field value.
     */
    public boolean isFPagHidden()
    {
        return fPagHidden.isSet(field_6_docinfo1);
    }

    /**
     * Sets the fPagResults field value.
     *
     */
    public void setFPagResults(boolean value)
    {
        field_6_docinfo1 = (byte)fPagResults.setBoolean(field_6_docinfo1, value);
    }

    /**
     *
     * @return  the fPagResults field value.
     */
    public boolean isFPagResults()
    {
        return fPagResults.isSet(field_6_docinfo1);
    }

    /**
     * Sets the fLockAtn field value.
     *
     */
    public void setFLockAtn(boolean value)
    {
        field_6_docinfo1 = (byte)fLockAtn.setBoolean(field_6_docinfo1, value);
    }

    /**
     *
     * @return  the fLockAtn field value.
     */
    public boolean isFLockAtn()
    {
        return fLockAtn.isSet(field_6_docinfo1);
    }

    /**
     * Sets the fMirrorMargins field value.
     *
     */
    public void setFMirrorMargins(boolean value)
    {
        field_6_docinfo1 = (byte)fMirrorMargins.setBoolean(field_6_docinfo1, value);
    }

    /**
     *
     * @return  the fMirrorMargins field value.
     */
    public boolean isFMirrorMargins()
    {
        return fMirrorMargins.isSet(field_6_docinfo1);
    }

    /**
     * Sets the unused3 field value.
     *
     */
    public void setUnused3(boolean value)
    {
        field_6_docinfo1 = (byte)unused3.setBoolean(field_6_docinfo1, value);
    }

    /**
     *
     * @return  the unused3 field value.
     */
    public boolean isUnused3()
    {
        return unused3.isSet(field_6_docinfo1);
    }

    /**
     * Sets the fDfltTrueType field value.
     *
     */
    public void setFDfltTrueType(boolean value)
    {
        field_6_docinfo1 = (byte)fDfltTrueType.setBoolean(field_6_docinfo1, value);
    }

    /**
     *
     * @return  the fDfltTrueType field value.
     */
    public boolean isFDfltTrueType()
    {
        return fDfltTrueType.isSet(field_6_docinfo1);
    }

    /**
     * Sets the fPagSupressTopSpacing field value.
     *
     */
    public void setFPagSupressTopSpacing(boolean value)
    {
        field_7_docinfo2 = (byte)fPagSupressTopSpacing.setBoolean(field_7_docinfo2, value);
    }

    /**
     *
     * @return  the fPagSupressTopSpacing field value.
     */
    public boolean isFPagSupressTopSpacing()
    {
        return fPagSupressTopSpacing.isSet(field_7_docinfo2);
    }

    /**
     * Sets the fProtEnabled field value.
     *
     */
    public void setFProtEnabled(boolean value)
    {
        field_7_docinfo2 = (byte)fProtEnabled.setBoolean(field_7_docinfo2, value);
    }

    /**
     *
     * @return  the fProtEnabled field value.
     */
    public boolean isFProtEnabled()
    {
        return fProtEnabled.isSet(field_7_docinfo2);
    }

    /**
     * Sets the fDispFormFldSel field value.
     *
     */
    public void setFDispFormFldSel(boolean value)
    {
        field_7_docinfo2 = (byte)fDispFormFldSel.setBoolean(field_7_docinfo2, value);
    }

    /**
     *
     * @return  the fDispFormFldSel field value.
     */
    public boolean isFDispFormFldSel()
    {
        return fDispFormFldSel.isSet(field_7_docinfo2);
    }

    /**
     * Sets the fRMView field value.
     *
     */
    public void setFRMView(boolean value)
    {
        field_7_docinfo2 = (byte)fRMView.setBoolean(field_7_docinfo2, value);
    }

    /**
     *
     * @return  the fRMView field value.
     */
    public boolean isFRMView()
    {
        return fRMView.isSet(field_7_docinfo2);
    }

    /**
     * Sets the fRMPrint field value.
     *
     */
    public void setFRMPrint(boolean value)
    {
        field_7_docinfo2 = (byte)fRMPrint.setBoolean(field_7_docinfo2, value);
    }

    /**
     *
     * @return  the fRMPrint field value.
     */
    public boolean isFRMPrint()
    {
        return fRMPrint.isSet(field_7_docinfo2);
    }

    /**
     * Sets the unused4 field value.
     *
     */
    public void setUnused4(boolean value)
    {
        field_7_docinfo2 = (byte)unused4.setBoolean(field_7_docinfo2, value);
    }

    /**
     *
     * @return  the unused4 field value.
     */
    public boolean isUnused4()
    {
        return unused4.isSet(field_7_docinfo2);
    }

    /**
     * Sets the fLockRev field value.
     *
     */
    public void setFLockRev(boolean value)
    {
        field_7_docinfo2 = (byte)fLockRev.setBoolean(field_7_docinfo2, value);
    }

    /**
     *
     * @return  the fLockRev field value.
     */
    public boolean isFLockRev()
    {
        return fLockRev.isSet(field_7_docinfo2);
    }

    /**
     * Sets the fEmbedFonts field value.
     *
     */
    public void setFEmbedFonts(boolean value)
    {
        field_7_docinfo2 = (byte)fEmbedFonts.setBoolean(field_7_docinfo2, value);
    }

    /**
     *
     * @return  the fEmbedFonts field value.
     */
    public boolean isFEmbedFonts()
    {
        return fEmbedFonts.isSet(field_7_docinfo2);
    }

    /**
     * Sets the oldfNoTabForInd field value.
     *
     */
    public void setOldfNoTabForInd(boolean value)
    {
        field_8_docinfo3 = (short)oldfNoTabForInd.setBoolean(field_8_docinfo3, value);
    }

    /**
     *
     * @return  the oldfNoTabForInd field value.
     */
    public boolean isOldfNoTabForInd()
    {
        return oldfNoTabForInd.isSet(field_8_docinfo3);
    }

    /**
     * Sets the oldfNoSpaceRaiseLower field value.
     *
     */
    public void setOldfNoSpaceRaiseLower(boolean value)
    {
        field_8_docinfo3 = (short)oldfNoSpaceRaiseLower.setBoolean(field_8_docinfo3, value);
    }

    /**
     *
     * @return  the oldfNoSpaceRaiseLower field value.
     */
    public boolean isOldfNoSpaceRaiseLower()
    {
        return oldfNoSpaceRaiseLower.isSet(field_8_docinfo3);
    }

    /**
     * Sets the oldfSuppressSpbfAfterPageBreak field value.
     *
     */
    public void setOldfSuppressSpbfAfterPageBreak(boolean value)
    {
        field_8_docinfo3 = (short)oldfSuppressSpbfAfterPageBreak.setBoolean(field_8_docinfo3, value);
    }

    /**
     *
     * @return  the oldfSuppressSpbfAfterPageBreak field value.
     */
    public boolean isOldfSuppressSpbfAfterPageBreak()
    {
        return oldfSuppressSpbfAfterPageBreak.isSet(field_8_docinfo3);
    }

    /**
     * Sets the oldfWrapTrailSpaces field value.
     *
     */
    public void setOldfWrapTrailSpaces(boolean value)
    {
        field_8_docinfo3 = (short)oldfWrapTrailSpaces.setBoolean(field_8_docinfo3, value);
    }

    /**
     *
     * @return  the oldfWrapTrailSpaces field value.
     */
    public boolean isOldfWrapTrailSpaces()
    {
        return oldfWrapTrailSpaces.isSet(field_8_docinfo3);
    }

    /**
     * Sets the oldfMapPrintTextColor field value.
     *
     */
    public void setOldfMapPrintTextColor(boolean value)
    {
        field_8_docinfo3 = (short)oldfMapPrintTextColor.setBoolean(field_8_docinfo3, value);
    }

    /**
     *
     * @return  the oldfMapPrintTextColor field value.
     */
    public boolean isOldfMapPrintTextColor()
    {
        return oldfMapPrintTextColor.isSet(field_8_docinfo3);
    }

    /**
     * Sets the oldfNoColumnBalance field value.
     *
     */
    public void setOldfNoColumnBalance(boolean value)
    {
        field_8_docinfo3 = (short)oldfNoColumnBalance.setBoolean(field_8_docinfo3, value);
    }

    /**
     *
     * @return  the oldfNoColumnBalance field value.
     */
    public boolean isOldfNoColumnBalance()
    {
        return oldfNoColumnBalance.isSet(field_8_docinfo3);
    }

    /**
     * Sets the oldfConvMailMergeEsc field value.
     *
     */
    public void setOldfConvMailMergeEsc(boolean value)
    {
        field_8_docinfo3 = (short)oldfConvMailMergeEsc.setBoolean(field_8_docinfo3, value);
    }

    /**
     *
     * @return  the oldfConvMailMergeEsc field value.
     */
    public boolean isOldfConvMailMergeEsc()
    {
        return oldfConvMailMergeEsc.isSet(field_8_docinfo3);
    }

    /**
     * Sets the oldfSupressTopSpacing field value.
     *
     */
    public void setOldfSupressTopSpacing(boolean value)
    {
        field_8_docinfo3 = (short)oldfSupressTopSpacing.setBoolean(field_8_docinfo3, value);
    }

    /**
     *
     * @return  the oldfSupressTopSpacing field value.
     */
    public boolean isOldfSupressTopSpacing()
    {
        return oldfSupressTopSpacing.isSet(field_8_docinfo3);
    }

    /**
     * Sets the oldfOrigWordTableRules field value.
     *
     */
    public void setOldfOrigWordTableRules(boolean value)
    {
        field_8_docinfo3 = (short)oldfOrigWordTableRules.setBoolean(field_8_docinfo3, value);
    }

    /**
     *
     * @return  the oldfOrigWordTableRules field value.
     */
    public boolean isOldfOrigWordTableRules()
    {
        return oldfOrigWordTableRules.isSet(field_8_docinfo3);
    }

    /**
     * Sets the oldfTransparentMetafiles field value.
     *
     */
    public void setOldfTransparentMetafiles(boolean value)
    {
        field_8_docinfo3 = (short)oldfTransparentMetafiles.setBoolean(field_8_docinfo3, value);
    }

    /**
     *
     * @return  the oldfTransparentMetafiles field value.
     */
    public boolean isOldfTransparentMetafiles()
    {
        return oldfTransparentMetafiles.isSet(field_8_docinfo3);
    }

    /**
     * Sets the oldfShowBreaksInFrames field value.
     *
     */
    public void setOldfShowBreaksInFrames(boolean value)
    {
        field_8_docinfo3 = (short)oldfShowBreaksInFrames.setBoolean(field_8_docinfo3, value);
    }

    /**
     *
     * @return  the oldfShowBreaksInFrames field value.
     */
    public boolean isOldfShowBreaksInFrames()
    {
        return oldfShowBreaksInFrames.isSet(field_8_docinfo3);
    }

    /**
     * Sets the oldfSwapBordersFacingPgs field value.
     *
     */
    public void setOldfSwapBordersFacingPgs(boolean value)
    {
        field_8_docinfo3 = (short)oldfSwapBordersFacingPgs.setBoolean(field_8_docinfo3, value);
    }

    /**
     *
     * @return  the oldfSwapBordersFacingPgs field value.
     */
    public boolean isOldfSwapBordersFacingPgs()
    {
        return oldfSwapBordersFacingPgs.isSet(field_8_docinfo3);
    }

    /**
     * Sets the unused5 field value.
     *
     */
    public void setUnused5(byte value)
    {
        field_8_docinfo3 = (short)unused5.setValue(field_8_docinfo3, value);
    }

    /**
     *
     * @return  the unused5 field value.
     */
    public byte getUnused5()
    {
        return ( byte )unused5.getValue(field_8_docinfo3);
    }

    /**
     * Sets the rncEdn field value.
     *
     */
    public void setRncEdn(byte value)
    {
        field_23_Edn = (short)rncEdn.setValue(field_23_Edn, value);
    }

    /**
     *
     * @return  the rncEdn field value.
     */
    public byte getRncEdn()
    {
        return ( byte )rncEdn.getValue(field_23_Edn);
    }

    /**
     * Sets the nEdn field value.
     *
     */
    public void setNEdn(short value)
    {
        field_23_Edn = (short)nEdn.setValue(field_23_Edn, value);
    }

    /**
     *
     * @return  the nEdn field value.
     */
    public short getNEdn()
    {
        return ( short )nEdn.getValue(field_23_Edn);
    }

    /**
     * Sets the epc field value.
     *
     */
    public void setEpc(byte value)
    {
        field_24_Edn1 = (short)epc.setValue(field_24_Edn1, value);
    }

    /**
     *
     * @return  the epc field value.
     */
    public byte getEpc()
    {
        return ( byte )epc.getValue(field_24_Edn1);
    }

    /**
     * Sets the nfcFtnRef1 field value.
     *
     */
    public void setNfcFtnRef1(byte value)
    {
        field_24_Edn1 = (short)nfcFtnRef1.setValue(field_24_Edn1, value);
    }

    /**
     *
     * @return  the nfcFtnRef1 field value.
     */
    public byte getNfcFtnRef1()
    {
        return ( byte )nfcFtnRef1.getValue(field_24_Edn1);
    }

    /**
     * Sets the nfcEdnRef1 field value.
     *
     */
    public void setNfcEdnRef1(byte value)
    {
        field_24_Edn1 = (short)nfcEdnRef1.setValue(field_24_Edn1, value);
    }

    /**
     *
     * @return  the nfcEdnRef1 field value.
     */
    public byte getNfcEdnRef1()
    {
        return ( byte )nfcEdnRef1.getValue(field_24_Edn1);
    }

    /**
     * Sets the fPrintFormData field value.
     *
     */
    public void setFPrintFormData(boolean value)
    {
        field_24_Edn1 = (short)fPrintFormData.setBoolean(field_24_Edn1, value);
    }

    /**
     *
     * @return  the fPrintFormData field value.
     */
    public boolean isFPrintFormData()
    {
        return fPrintFormData.isSet(field_24_Edn1);
    }

    /**
     * Sets the fSaveFormData field value.
     *
     */
    public void setFSaveFormData(boolean value)
    {
        field_24_Edn1 = (short)fSaveFormData.setBoolean(field_24_Edn1, value);
    }

    /**
     *
     * @return  the fSaveFormData field value.
     */
    public boolean isFSaveFormData()
    {
        return fSaveFormData.isSet(field_24_Edn1);
    }

    /**
     * Sets the fShadeFormData field value.
     *
     */
    public void setFShadeFormData(boolean value)
    {
        field_24_Edn1 = (short)fShadeFormData.setBoolean(field_24_Edn1, value);
    }

    /**
     *
     * @return  the fShadeFormData field value.
     */
    public boolean isFShadeFormData()
    {
        return fShadeFormData.isSet(field_24_Edn1);
    }

    /**
     * Sets the fWCFtnEdn field value.
     *
     */
    public void setFWCFtnEdn(boolean value)
    {
        field_24_Edn1 = (short)fWCFtnEdn.setBoolean(field_24_Edn1, value);
    }

    /**
     *
     * @return  the fWCFtnEdn field value.
     */
    public boolean isFWCFtnEdn()
    {
        return fWCFtnEdn.isSet(field_24_Edn1);
    }

    /**
     * Sets the wvkSaved field value.
     *
     */
    public void setWvkSaved(byte value)
    {
        field_32_view = (short)wvkSaved.setValue(field_32_view, value);
    }

    /**
     *
     * @return  the wvkSaved field value.
     */
    public byte getWvkSaved()
    {
        return ( byte )wvkSaved.getValue(field_32_view);
    }

    /**
     * Sets the wScaleSaved field value.
     *
     */
    public void setWScaleSaved(short value)
    {
        field_32_view = (short)wScaleSaved.setValue(field_32_view, value);
    }

    /**
     *
     * @return  the wScaleSaved field value.
     */
    public short getWScaleSaved()
    {
        return ( short )wScaleSaved.getValue(field_32_view);
    }

    /**
     * Sets the zkSaved field value.
     *
     */
    public void setZkSaved(byte value)
    {
        field_32_view = (short)zkSaved.setValue(field_32_view, value);
    }

    /**
     *
     * @return  the zkSaved field value.
     */
    public byte getZkSaved()
    {
        return ( byte )zkSaved.getValue(field_32_view);
    }

    /**
     * Sets the fRotateFontW6 field value.
     *
     */
    public void setFRotateFontW6(boolean value)
    {
        field_32_view = (short)fRotateFontW6.setBoolean(field_32_view, value);
    }

    /**
     *
     * @return  the fRotateFontW6 field value.
     */
    public boolean isFRotateFontW6()
    {
        return fRotateFontW6.isSet(field_32_view);
    }

    /**
     * Sets the iGutterPos field value.
     *
     */
    public void setIGutterPos(boolean value)
    {
        field_32_view = (short)iGutterPos.setBoolean(field_32_view, value);
    }

    /**
     *
     * @return  the iGutterPos field value.
     */
    public boolean isIGutterPos()
    {
        return iGutterPos.isSet(field_32_view);
    }

    /**
     * Sets the fNoTabForInd field value.
     *
     */
    public void setFNoTabForInd(boolean value)
    {
        field_33_docinfo4 = fNoTabForInd.setBoolean(field_33_docinfo4, value);
    }

    /**
     *
     * @return  the fNoTabForInd field value.
     */
    public boolean isFNoTabForInd()
    {
        return fNoTabForInd.isSet(field_33_docinfo4);
    }

    /**
     * Sets the fNoSpaceRaiseLower field value.
     *
     */
    public void setFNoSpaceRaiseLower(boolean value)
    {
        field_33_docinfo4 = fNoSpaceRaiseLower.setBoolean(field_33_docinfo4, value);
    }

    /**
     *
     * @return  the fNoSpaceRaiseLower field value.
     */
    public boolean isFNoSpaceRaiseLower()
    {
        return fNoSpaceRaiseLower.isSet(field_33_docinfo4);
    }

    /**
     * Sets the fSupressSpdfAfterPageBreak field value.
     *
     */
    public void setFSupressSpdfAfterPageBreak(boolean value)
    {
        field_33_docinfo4 = fSupressSpdfAfterPageBreak.setBoolean(field_33_docinfo4, value);
    }

    /**
     *
     * @return  the fSupressSpdfAfterPageBreak field value.
     */
    public boolean isFSupressSpdfAfterPageBreak()
    {
        return fSupressSpdfAfterPageBreak.isSet(field_33_docinfo4);
    }

    /**
     * Sets the fWrapTrailSpaces field value.
     *
     */
    public void setFWrapTrailSpaces(boolean value)
    {
        field_33_docinfo4 = fWrapTrailSpaces.setBoolean(field_33_docinfo4, value);
    }

    /**
     *
     * @return  the fWrapTrailSpaces field value.
     */
    public boolean isFWrapTrailSpaces()
    {
        return fWrapTrailSpaces.isSet(field_33_docinfo4);
    }

    /**
     * Sets the fMapPrintTextColor field value.
     *
     */
    public void setFMapPrintTextColor(boolean value)
    {
        field_33_docinfo4 = fMapPrintTextColor.setBoolean(field_33_docinfo4, value);
    }

    /**
     *
     * @return  the fMapPrintTextColor field value.
     */
    public boolean isFMapPrintTextColor()
    {
        return fMapPrintTextColor.isSet(field_33_docinfo4);
    }

    /**
     * Sets the fNoColumnBalance field value.
     *
     */
    public void setFNoColumnBalance(boolean value)
    {
        field_33_docinfo4 = fNoColumnBalance.setBoolean(field_33_docinfo4, value);
    }

    /**
     *
     * @return  the fNoColumnBalance field value.
     */
    public boolean isFNoColumnBalance()
    {
        return fNoColumnBalance.isSet(field_33_docinfo4);
    }

    /**
     * Sets the fConvMailMergeEsc field value.
     *
     */
    public void setFConvMailMergeEsc(boolean value)
    {
        field_33_docinfo4 = fConvMailMergeEsc.setBoolean(field_33_docinfo4, value);
    }

    /**
     *
     * @return  the fConvMailMergeEsc field value.
     */
    public boolean isFConvMailMergeEsc()
    {
        return fConvMailMergeEsc.isSet(field_33_docinfo4);
    }

    /**
     * Sets the fSupressTopSpacing field value.
     *
     */
    public void setFSupressTopSpacing(boolean value)
    {
        field_33_docinfo4 = fSupressTopSpacing.setBoolean(field_33_docinfo4, value);
    }

    /**
     *
     * @return  the fSupressTopSpacing field value.
     */
    public boolean isFSupressTopSpacing()
    {
        return fSupressTopSpacing.isSet(field_33_docinfo4);
    }

    /**
     * Sets the fOrigWordTableRules field value.
     *
     */
    public void setFOrigWordTableRules(boolean value)
    {
        field_33_docinfo4 = fOrigWordTableRules.setBoolean(field_33_docinfo4, value);
    }

    /**
     *
     * @return  the fOrigWordTableRules field value.
     */
    public boolean isFOrigWordTableRules()
    {
        return fOrigWordTableRules.isSet(field_33_docinfo4);
    }

    /**
     * Sets the fTransparentMetafiles field value.
     *
     */
    public void setFTransparentMetafiles(boolean value)
    {
        field_33_docinfo4 = fTransparentMetafiles.setBoolean(field_33_docinfo4, value);
    }

    /**
     *
     * @return  the fTransparentMetafiles field value.
     */
    public boolean isFTransparentMetafiles()
    {
        return fTransparentMetafiles.isSet(field_33_docinfo4);
    }

    /**
     * Sets the fShowBreaksInFrames field value.
     *
     */
    public void setFShowBreaksInFrames(boolean value)
    {
        field_33_docinfo4 = fShowBreaksInFrames.setBoolean(field_33_docinfo4, value);
    }

    /**
     *
     * @return  the fShowBreaksInFrames field value.
     */
    public boolean isFShowBreaksInFrames()
    {
        return fShowBreaksInFrames.isSet(field_33_docinfo4);
    }

    /**
     * Sets the fSwapBordersFacingPgs field value.
     *
     */
    public void setFSwapBordersFacingPgs(boolean value)
    {
        field_33_docinfo4 = fSwapBordersFacingPgs.setBoolean(field_33_docinfo4, value);
    }

    /**
     *
     * @return  the fSwapBordersFacingPgs field value.
     */
    public boolean isFSwapBordersFacingPgs()
    {
        return fSwapBordersFacingPgs.isSet(field_33_docinfo4);
    }

    /**
     * Sets the fSuppressTopSPacingMac5 field value.
     *
     */
    public void setFSuppressTopSPacingMac5(boolean value)
    {
        field_33_docinfo4 = fSuppressTopSPacingMac5.setBoolean(field_33_docinfo4, value);
    }

    /**
     *
     * @return  the fSuppressTopSPacingMac5 field value.
     */
    public boolean isFSuppressTopSPacingMac5()
    {
        return fSuppressTopSPacingMac5.isSet(field_33_docinfo4);
    }

    /**
     * Sets the fTruncDxaExpand field value.
     *
     */
    public void setFTruncDxaExpand(boolean value)
    {
        field_33_docinfo4 = fTruncDxaExpand.setBoolean(field_33_docinfo4, value);
    }

    /**
     *
     * @return  the fTruncDxaExpand field value.
     */
    public boolean isFTruncDxaExpand()
    {
        return fTruncDxaExpand.isSet(field_33_docinfo4);
    }

    /**
     * Sets the fPrintBodyBeforeHdr field value.
     *
     */
    public void setFPrintBodyBeforeHdr(boolean value)
    {
        field_33_docinfo4 = fPrintBodyBeforeHdr.setBoolean(field_33_docinfo4, value);
    }

    /**
     *
     * @return  the fPrintBodyBeforeHdr field value.
     */
    public boolean isFPrintBodyBeforeHdr()
    {
        return fPrintBodyBeforeHdr.isSet(field_33_docinfo4);
    }

    /**
     * Sets the fNoLeading field value.
     *
     */
    public void setFNoLeading(boolean value)
    {
        field_33_docinfo4 = fNoLeading.setBoolean(field_33_docinfo4, value);
    }

    /**
     *
     * @return  the fNoLeading field value.
     */
    public boolean isFNoLeading()
    {
        return fNoLeading.isSet(field_33_docinfo4);
    }

    /**
     * Sets the fMWSmallCaps field value.
     *
     */
    public void setFMWSmallCaps(boolean value)
    {
        field_33_docinfo4 = fMWSmallCaps.setBoolean(field_33_docinfo4, value);
    }

    /**
     *
     * @return  the fMWSmallCaps field value.
     */
    public boolean isFMWSmallCaps()
    {
        return fMWSmallCaps.isSet(field_33_docinfo4);
    }

    /**
     * Sets the lvl field value.
     *
     */
    public void setLvl(byte value)
    {
        field_37_docinfo5 = (short)lvl.setValue(field_37_docinfo5, value);
    }

    /**
     *
     * @return  the lvl field value.
     */
    public byte getLvl()
    {
        return ( byte )lvl.getValue(field_37_docinfo5);
    }

    /**
     * Sets the fGramAllDone field value.
     *
     */
    public void setFGramAllDone(boolean value)
    {
        field_37_docinfo5 = (short)fGramAllDone.setBoolean(field_37_docinfo5, value);
    }

    /**
     *
     * @return  the fGramAllDone field value.
     */
    public boolean isFGramAllDone()
    {
        return fGramAllDone.isSet(field_37_docinfo5);
    }

    /**
     * Sets the fGramAllClean field value.
     *
     */
    public void setFGramAllClean(boolean value)
    {
        field_37_docinfo5 = (short)fGramAllClean.setBoolean(field_37_docinfo5, value);
    }

    /**
     *
     * @return  the fGramAllClean field value.
     */
    public boolean isFGramAllClean()
    {
        return fGramAllClean.isSet(field_37_docinfo5);
    }

    /**
     * Sets the fSubsetFonts field value.
     *
     */
    public void setFSubsetFonts(boolean value)
    {
        field_37_docinfo5 = (short)fSubsetFonts.setBoolean(field_37_docinfo5, value);
    }

    /**
     *
     * @return  the fSubsetFonts field value.
     */
    public boolean isFSubsetFonts()
    {
        return fSubsetFonts.isSet(field_37_docinfo5);
    }

    /**
     * Sets the fHideLastVersion field value.
     *
     */
    public void setFHideLastVersion(boolean value)
    {
        field_37_docinfo5 = (short)fHideLastVersion.setBoolean(field_37_docinfo5, value);
    }

    /**
     *
     * @return  the fHideLastVersion field value.
     */
    public boolean isFHideLastVersion()
    {
        return fHideLastVersion.isSet(field_37_docinfo5);
    }

    /**
     * Sets the fHtmlDoc field value.
     *
     */
    public void setFHtmlDoc(boolean value)
    {
        field_37_docinfo5 = (short)fHtmlDoc.setBoolean(field_37_docinfo5, value);
    }

    /**
     *
     * @return  the fHtmlDoc field value.
     */
    public boolean isFHtmlDoc()
    {
        return fHtmlDoc.isSet(field_37_docinfo5);
    }

    /**
     * Sets the fSnapBorder field value.
     *
     */
    public void setFSnapBorder(boolean value)
    {
        field_37_docinfo5 = (short)fSnapBorder.setBoolean(field_37_docinfo5, value);
    }

    /**
     *
     * @return  the fSnapBorder field value.
     */
    public boolean isFSnapBorder()
    {
        return fSnapBorder.isSet(field_37_docinfo5);
    }

    /**
     * Sets the fIncludeHeader field value.
     *
     */
    public void setFIncludeHeader(boolean value)
    {
        field_37_docinfo5 = (short)fIncludeHeader.setBoolean(field_37_docinfo5, value);
    }

    /**
     *
     * @return  the fIncludeHeader field value.
     */
    public boolean isFIncludeHeader()
    {
        return fIncludeHeader.isSet(field_37_docinfo5);
    }

    /**
     * Sets the fIncludeFooter field value.
     *
     */
    public void setFIncludeFooter(boolean value)
    {
        field_37_docinfo5 = (short)fIncludeFooter.setBoolean(field_37_docinfo5, value);
    }

    /**
     *
     * @return  the fIncludeFooter field value.
     */
    public boolean isFIncludeFooter()
    {
        return fIncludeFooter.isSet(field_37_docinfo5);
    }

    /**
     * Sets the fForcePageSizePag field value.
     *
     */
    public void setFForcePageSizePag(boolean value)
    {
        field_37_docinfo5 = (short)fForcePageSizePag.setBoolean(field_37_docinfo5, value);
    }

    /**
     *
     * @return  the fForcePageSizePag field value.
     */
    public boolean isFForcePageSizePag()
    {
        return fForcePageSizePag.isSet(field_37_docinfo5);
    }

    /**
     * Sets the fMinFontSizePag field value.
     *
     */
    public void setFMinFontSizePag(boolean value)
    {
        field_37_docinfo5 = (short)fMinFontSizePag.setBoolean(field_37_docinfo5, value);
    }

    /**
     *
     * @return  the fMinFontSizePag field value.
     */
    public boolean isFMinFontSizePag()
    {
        return fMinFontSizePag.isSet(field_37_docinfo5);
    }

    /**
     * Sets the fHaveVersions field value.
     *
     */
    public void setFHaveVersions(boolean value)
    {
        field_38_docinfo6 = (short)fHaveVersions.setBoolean(field_38_docinfo6, value);
    }

    /**
     *
     * @return  the fHaveVersions field value.
     */
    public boolean isFHaveVersions()
    {
        return fHaveVersions.isSet(field_38_docinfo6);
    }

    /**
     * Sets the fAutoVersions field value.
     *
     */
    public void setFAutoVersions(boolean value)
    {
        field_38_docinfo6 = (short)fAutoVersions.setBoolean(field_38_docinfo6, value);
    }

    /**
     *
     * @return  the fAutoVersions field value.
     */
    public boolean isFAutoVersions()
    {
        return fAutoVersions.isSet(field_38_docinfo6);
    }

    /**
     * Sets the fVirusPrompted field value.
     *
     */
    public void setFVirusPrompted(boolean value)
    {
        field_43_virusinfo = fVirusPrompted.setBoolean(field_43_virusinfo, value);
    }

    /**
     *
     * @return  the fVirusPrompted field value.
     */
    public boolean isFVirusPrompted()
    {
        return fVirusPrompted.isSet(field_43_virusinfo);
    }

    /**
     * Sets the fVirusLoadSafe field value.
     *
     */
    public void setFVirusLoadSafe(boolean value)
    {
        field_43_virusinfo = fVirusLoadSafe.setBoolean(field_43_virusinfo, value);
    }

    /**
     *
     * @return  the fVirusLoadSafe field value.
     */
    public boolean isFVirusLoadSafe()
    {
        return fVirusLoadSafe.isSet(field_43_virusinfo);
    }

    /**
     * Sets the KeyVirusSession30 field value.
     *
     */
    public void setKeyVirusSession30(int value)
    {
        field_43_virusinfo = KeyVirusSession30.setValue(field_43_virusinfo, value);
    }

    /**
     *
     * @return  the KeyVirusSession30 field value.
     */
    public int getKeyVirusSession30()
    {
        return KeyVirusSession30.getValue(field_43_virusinfo);
    }
}
