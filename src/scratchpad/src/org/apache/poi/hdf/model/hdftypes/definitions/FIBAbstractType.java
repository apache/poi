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

package org.apache.poi.hdf.model.hdftypes.definitions;


import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.HexDump;
import org.apache.poi.hdf.model.hdftypes.HDFType;

/**
 * File information Block.
 * NOTE: This source is automatically generated please do not modify this file.  Either subclass or
 *       remove the record in src/records/definitions.

 * @author Andrew C. Oliver
 */
public abstract class FIBAbstractType
    implements HDFType
{

    private  int field_1_wIdent;
    private  int field_2_nFib;
    private  int field_3_nProduct;
    private  int field_4_lid;
    private  int field_5_pnNext;
    private  short field_6_options;
        private static BitField  fDot = BitFieldFactory.getInstance(0x0001);
        private static BitField  fGlsy = BitFieldFactory.getInstance(0x0002);
        private static BitField  fComplex = BitFieldFactory.getInstance(0x0004);
        private static BitField  fHasPic = BitFieldFactory.getInstance(0x0008);
        private static BitField  cQuickSaves = BitFieldFactory.getInstance(0x00F0);
        private static BitField  fEncrypted = BitFieldFactory.getInstance(0x0100);
        private static BitField  fWhichTblStm = BitFieldFactory.getInstance(0x0200);
        private static BitField  fReadOnlyRecommended = BitFieldFactory.getInstance(0x0400);
        private static BitField  fWriteReservation = BitFieldFactory.getInstance(0x0800);
        private static BitField  fExtChar = BitFieldFactory.getInstance(0x1000);
        private static BitField  fLoadOverride = BitFieldFactory.getInstance(0x2000);
        private static BitField  fFarEast = BitFieldFactory.getInstance(0x4000);
        private static BitField  fCrypto = BitFieldFactory.getInstance(0x8000);
    private  int field_7_nFibBack;
    private  int field_8_lKey;
    private  int field_9_envr;
    private  short field_10_history;
        private static BitField  fMac = BitFieldFactory.getInstance(0x0001);
        private static BitField  fEmptySpecial = BitFieldFactory.getInstance(0x0002);
        private static BitField  fLoadOverridePage = BitFieldFactory.getInstance(0x0004);
        private static BitField  fFutureSavedUndo = BitFieldFactory.getInstance(0x0008);
        private static BitField  fWord97Saved = BitFieldFactory.getInstance(0x0010);
        private static BitField  fSpare0 = BitFieldFactory.getInstance(0x00FE);
    private  int field_11_chs;
    private  int field_12_chsTables;
    private  int field_13_fcMin;
    private  int field_14_fcMac;
    private  int field_15_csw;
    private  int field_16_wMagicCreated;
    private  int field_17_wMagicRevised;
    private  int field_18_wMagicCreatedPrivate;
    private  int field_19_wMagicRevisedPrivate;
    private  int field_20_pnFbpChpFirst_W6;
    private  int field_21_pnChpFirst_W6;
    private  int field_22_cpnBteChp_W6;
    private  int field_23_pnFbpPapFirst_W6;
    private  int field_24_pnPapFirst_W6;
    private  int field_25_cpnBtePap_W6;
    private  int field_26_pnFbpLvcFirst_W6;
    private  int field_27_pnLvcFirst_W6;
    private  int field_28_cpnBteLvc_W6;
    private  int field_29_lidFE;
    private  int field_30_clw;
    private  int field_31_cbMac;
    private  int field_32_lProductCreated;
    private  int field_33_lProductRevised;
    private  int field_34_ccpText;
    private  int field_35_ccpFtn;
    private  int field_36_ccpHdd;
    private  int field_37_ccpMcr;
    private  int field_38_ccpAtn;
    private  int field_39_ccpEdn;
    private  int field_40_ccpTxbx;
    private  int field_41_ccpHdrTxbx;
    private  int field_42_pnFbpChpFirst;
    private  int field_43_pnChpFirst;
    private  int field_44_cpnBteChp;
    private  int field_45_pnFbpPapFirst;
    private  int field_46_pnPapFirst;
    private  int field_47_cpnBtePap;
    private  int field_48_pnFbpLvcFirst;
    private  int field_49_pnLvcFirst;
    private  int field_50_cpnBteLvc;
    private  int field_51_fcIslandFirst;
    private  int field_52_fcIslandLim;
    private  int field_53_cfclcb;
    private  int field_54_fcStshfOrig;
    private  int field_55_lcbStshfOrig;
    private  int field_56_fcStshf;
    private  int field_57_lcbStshf;
    private  int field_58_fcPlcffndRef;
    private  int field_59_lcbPlcffndRef;
    private  int field_60_fcPlcffndTxt;
    private  int field_61_lcbPlcffndTxt;
    private  int field_62_fcPlcfandRef;
    private  int field_63_lcbPlcfandRef;
    private  int field_64_fcPlcfandTxt;
    private  int field_65_lcbPlcfandTxt;
    private  int field_66_fcPlcfsed;
    private  int field_67_lcbPlcfsed;
    private  int field_68_fcPlcpad;
    private  int field_69_lcbPlcpad;
    private  int field_70_fcPlcfphe;
    private  int field_71_lcbPlcfphe;
    private  int field_72_fcSttbfglsy;
    private  int field_73_lcbSttbfglsy;
    private  int field_74_fcPlcfglsy;
    private  int field_75_lcbPlcfglsy;
    private  int field_76_fcPlcfhdd;
    private  int field_77_lcbPlcfhdd;
    private  int field_78_fcPlcfbteChpx;
    private  int field_79_lcbPlcfbteChpx;
    private  int field_80_fcPlcfbtePapx;
    private  int field_81_lcbPlcfbtePapx;
    private  int field_82_fcPlcfsea;
    private  int field_83_lcbPlcfsea;
    private  int field_84_fcSttbfffn;
    private  int field_85_lcbSttbfffn;
    private  int field_86_fcPlcffldMom;
    private  int field_87_lcbPlcffldMom;
    private  int field_88_fcPlcffldHdr;
    private  int field_89_lcbPlcffldHdr;
    private  int field_90_fcPlcffldFtn;
    private  int field_91_lcbPlcffldFtn;
    private  int field_92_fcPlcffldAtn;
    private  int field_93_lcbPlcffldAtn;
    private  int field_94_fcPlcffldMcr;
    private  int field_95_lcbPlcffldMcr;
    private  int field_96_fcSttbfbkmk;
    private  int field_97_lcbSttbfbkmk;
    private  int field_98_fcPlcfbkf;
    private  int field_99_lcbPlcfbkf;
    private  int field_100_fcPlcfbkl;
    private  int field_101_lcbPlcfbkl;
    private  int field_102_fcCmds;
    private  int field_103_lcbCmds;
    private  int field_104_fcPlcmcr;
    private  int field_105_lcbPlcmcr;
    private  int field_106_fcSttbfmcr;
    private  int field_107_lcbSttbfmcr;
    private  int field_108_fcPrDrvr;
    private  int field_109_lcbPrDrvr;
    private  int field_110_fcPrEnvPort;
    private  int field_111_lcbPrEnvPort;
    private  int field_112_fcPrEnvLand;
    private  int field_113_lcbPrEnvLand;
    private  int field_114_fcWss;
    private  int field_115_lcbWss;
    private  int field_116_fcDop;
    private  int field_117_lcbDop;
    private  int field_118_fcSttbfAssoc;
    private  int field_119_lcbSttbfAssoc;
    private  int field_120_fcClx;
    private  int field_121_lcbClx;
    private  int field_122_fcPlcfpgdFtn;
    private  int field_123_lcbPlcfpgdFtn;
    private  int field_124_fcAutosaveSource;
    private  int field_125_lcbAutosaveSource;
    private  int field_126_fcGrpXstAtnOwners;
    private  int field_127_lcbGrpXstAtnOwners;
    private  int field_128_fcSttbfAtnbkmk;
    private  int field_129_lcbSttbfAtnbkmk;
    private  int field_130_fcPlcdoaMom;
    private  int field_131_lcbPlcdoaMom;
    private  int field_132_fcPlcdoaHdr;
    private  int field_133_lcbPlcdoaHdr;
    private  int field_134_fcPlcspaMom;
    private  int field_135_lcbPlcspaMom;
    private  int field_136_fcPlcspaHdr;
    private  int field_137_lcbPlcspaHdr;
    private  int field_138_fcPlcfAtnbkf;
    private  int field_139_lcbPlcfAtnbkf;
    private  int field_140_fcPlcfAtnbkl;
    private  int field_141_lcbPlcfAtnbkl;
    private  int field_142_fcPms;
    private  int field_143_lcbPms;
    private  int field_144_fcFormFldSttbs;
    private  int field_145_lcbFormFldSttbs;
    private  int field_146_fcPlcfendRef;
    private  int field_147_lcbPlcfendRef;
    private  int field_148_fcPlcfendTxt;
    private  int field_149_lcbPlcfendTxt;
    private  int field_150_fcPlcffldEdn;
    private  int field_151_lcbPlcffldEdn;
    private  int field_152_fcPlcfpgdEdn;
    private  int field_153_lcbPlcfpgdEdn;
    private  int field_154_fcDggInfo;
    private  int field_155_lcbDggInfo;
    private  int field_156_fcSttbfRMark;
    private  int field_157_lcbSttbfRMark;
    private  int field_158_fcSttbCaption;
    private  int field_159_lcbSttbCaption;
    private  int field_160_fcSttbAutoCaption;
    private  int field_161_lcbSttbAutoCaption;
    private  int field_162_fcPlcfwkb;
    private  int field_163_lcbPlcfwkb;
    private  int field_164_fcPlcfspl;
    private  int field_165_lcbPlcfspl;
    private  int field_166_fcPlcftxbxTxt;
    private  int field_167_lcbPlcftxbxTxt;
    private  int field_168_fcPlcffldTxbx;
    private  int field_169_lcbPlcffldTxbx;
    private  int field_170_fcPlcfhdrtxbxTxt;
    private  int field_171_lcbPlcfhdrtxbxTxt;
    private  int field_172_fcPlcffldHdrTxbx;
    private  int field_173_lcbPlcffldHdrTxbx;
    private  int field_174_fcStwUser;
    private  int field_175_lcbStwUser;
    private  int field_176_fcSttbttmbd;
    private  int field_177_cbSttbttmbd;
    private  int field_178_fcUnused;
    private  int field_179_lcbUnused;
    private  int field_180_fcPgdMother;
    private  int field_181_lcbPgdMother;
    private  int field_182_fcBkdMother;
    private  int field_183_lcbBkdMother;
    private  int field_184_fcPgdFtn;
    private  int field_185_lcbPgdFtn;
    private  int field_186_fcBkdFtn;
    private  int field_187_lcbBkdFtn;
    private  int field_188_fcPgdEdn;
    private  int field_189_lcbPgdEdn;
    private  int field_190_fcBkdEdn;
    private  int field_191_lcbBkdEdn;
    private  int field_192_fcSttbfIntlFld;
    private  int field_193_lcbSttbfIntlFld;
    private  int field_194_fcRouteSlip;
    private  int field_195_lcbRouteSlip;
    private  int field_196_fcSttbSavedBy;
    private  int field_197_lcbSttbSavedBy;
    private  int field_198_fcSttbFnm;
    private  int field_199_lcbSttbFnm;
    private  int field_200_fcPlcfLst;
    private  int field_201_lcbPlcfLst;
    private  int field_202_fcPlfLfo;
    private  int field_203_lcbPlfLfo;
    private  int field_204_fcPlcftxbxBkd;
    private  int field_205_lcbPlcftxbxBkd;
    private  int field_206_fcPlcftxbxHdrBkd;
    private  int field_207_lcbPlcftxbxHdrBkd;
    private  int field_208_fcDocUndo;
    private  int field_209_lcbDocUndo;
    private  int field_210_fcRgbuse;
    private  int field_211_lcbRgbuse;
    private  int field_212_fcUsp;
    private  int field_213_lcbUsp;
    private  int field_214_fcUskf;
    private  int field_215_lcbUskf;
    private  int field_216_fcPlcupcRgbuse;
    private  int field_217_lcbPlcupcRgbuse;
    private  int field_218_fcPlcupcUsp;
    private  int field_219_lcbPlcupcUsp;
    private  int field_220_fcSttbGlsyStyle;
    private  int field_221_lcbSttbGlsyStyle;
    private  int field_222_fcPlgosl;
    private  int field_223_lcbPlgosl;
    private  int field_224_fcPlcocx;
    private  int field_225_lcbPlcocx;
    private  int field_226_fcPlcfbteLvc;
    private  int field_227_lcbPlcfbteLvc;
    private  int field_228_dwLowDateTime;
    private  int field_229_dwHighDateTime;
    private  int field_230_fcPlcflvc;
    private  int field_231_lcbPlcflvc;
    private  int field_232_fcPlcasumy;
    private  int field_233_lcbPlcasumy;
    private  int field_234_fcPlcfgram;
    private  int field_235_lcbPlcfgram;
    private  int field_236_fcSttbListNames;
    private  int field_237_lcbSttbListNames;
    private  int field_238_fcSttbfUssr;
    private  int field_239_lcbSttbfUssr;


    public FIBAbstractType()
    {

    }

    protected void fillFields(byte [] data, short size, int offset)
    {
        field_1_wIdent                  = LittleEndian.getShort(data, 0x0 + offset);
        field_2_nFib                    = LittleEndian.getShort(data, 0x2 + offset);
        field_3_nProduct                = LittleEndian.getShort(data, 0x4 + offset);
        field_4_lid                     = LittleEndian.getShort(data, 0x6 + offset);
        field_5_pnNext                  = LittleEndian.getShort(data, 0x8 + offset);
        field_6_options                 = LittleEndian.getShort(data, 0xa + offset);
        field_7_nFibBack                = LittleEndian.getShort(data, 0xc + offset);
        field_8_lKey                    = LittleEndian.getShort(data, 0xe + offset);
        field_9_envr                    = LittleEndian.getShort(data, 0x10 + offset);
        field_10_history                = LittleEndian.getShort(data, 0x12 + offset);
        field_11_chs                    = LittleEndian.getShort(data, 0x14 + offset);
        field_12_chsTables              = LittleEndian.getShort(data, 0x16 + offset);
        field_13_fcMin                  = LittleEndian.getInt(data, 0x18 + offset);
        field_14_fcMac                  = LittleEndian.getInt(data, 0x1c + offset);
        field_15_csw                    = LittleEndian.getShort(data, 0x20 + offset);
        field_16_wMagicCreated          = LittleEndian.getShort(data, 0x22 + offset);
        field_17_wMagicRevised          = LittleEndian.getShort(data, 0x24 + offset);
        field_18_wMagicCreatedPrivate   = LittleEndian.getShort(data, 0x26 + offset);
        field_19_wMagicRevisedPrivate   = LittleEndian.getShort(data, 0x28 + offset);
        field_20_pnFbpChpFirst_W6       = LittleEndian.getShort(data, 0x2a + offset);
        field_21_pnChpFirst_W6          = LittleEndian.getShort(data, 0x2c + offset);
        field_22_cpnBteChp_W6           = LittleEndian.getShort(data, 0x2e + offset);
        field_23_pnFbpPapFirst_W6       = LittleEndian.getShort(data, 0x30 + offset);
        field_24_pnPapFirst_W6          = LittleEndian.getShort(data, 0x32 + offset);
        field_25_cpnBtePap_W6           = LittleEndian.getShort(data, 0x34 + offset);
        field_26_pnFbpLvcFirst_W6       = LittleEndian.getShort(data, 0x36 + offset);
        field_27_pnLvcFirst_W6          = LittleEndian.getShort(data, 0x38 + offset);
        field_28_cpnBteLvc_W6           = LittleEndian.getShort(data, 0x3a + offset);
        field_29_lidFE                  = LittleEndian.getShort(data, 0x3c + offset);
        field_30_clw                    = LittleEndian.getShort(data, 0x3e + offset);
        field_31_cbMac                  = LittleEndian.getInt(data, 0x40 + offset);
        field_32_lProductCreated        = LittleEndian.getInt(data, 0x44 + offset);
        field_33_lProductRevised        = LittleEndian.getInt(data, 0x48 + offset);
        field_34_ccpText                = LittleEndian.getInt(data, 0x4c + offset);
        field_35_ccpFtn                 = LittleEndian.getInt(data, 0x50 + offset);
        field_36_ccpHdd                 = LittleEndian.getInt(data, 0x54 + offset);
        field_37_ccpMcr                 = LittleEndian.getInt(data, 0x58 + offset);
        field_38_ccpAtn                 = LittleEndian.getInt(data, 0x5c + offset);
        field_39_ccpEdn                 = LittleEndian.getInt(data, 0x60 + offset);
        field_40_ccpTxbx                = LittleEndian.getInt(data, 0x64 + offset);
        field_41_ccpHdrTxbx             = LittleEndian.getInt(data, 0x68 + offset);
        field_42_pnFbpChpFirst          = LittleEndian.getInt(data, 0x6c + offset);
        field_43_pnChpFirst             = LittleEndian.getInt(data, 0x70 + offset);
        field_44_cpnBteChp              = LittleEndian.getInt(data, 0x74 + offset);
        field_45_pnFbpPapFirst          = LittleEndian.getInt(data, 0x78 + offset);
        field_46_pnPapFirst             = LittleEndian.getInt(data, 0x7c + offset);
        field_47_cpnBtePap              = LittleEndian.getInt(data, 0x80 + offset);
        field_48_pnFbpLvcFirst          = LittleEndian.getInt(data, 0x84 + offset);
        field_49_pnLvcFirst             = LittleEndian.getInt(data, 0x88 + offset);
        field_50_cpnBteLvc              = LittleEndian.getInt(data, 0x8c + offset);
        field_51_fcIslandFirst          = LittleEndian.getInt(data, 0x90 + offset);
        field_52_fcIslandLim            = LittleEndian.getInt(data, 0x94 + offset);
        field_53_cfclcb                 = LittleEndian.getShort(data, 0x98 + offset);
        field_54_fcStshfOrig            = LittleEndian.getInt(data, 0x9a + offset);
        field_55_lcbStshfOrig           = LittleEndian.getInt(data, 0x9e + offset);
        field_56_fcStshf                = LittleEndian.getInt(data, 0xa2 + offset);
        field_57_lcbStshf               = LittleEndian.getInt(data, 0xa6 + offset);
        field_58_fcPlcffndRef           = LittleEndian.getInt(data, 0xaa + offset);
        field_59_lcbPlcffndRef          = LittleEndian.getInt(data, 0xae + offset);
        field_60_fcPlcffndTxt           = LittleEndian.getInt(data, 0xb2 + offset);
        field_61_lcbPlcffndTxt          = LittleEndian.getInt(data, 0xb6 + offset);
        field_62_fcPlcfandRef           = LittleEndian.getInt(data, 0xba + offset);
        field_63_lcbPlcfandRef          = LittleEndian.getInt(data, 0xbe + offset);
        field_64_fcPlcfandTxt           = LittleEndian.getInt(data, 0xc2 + offset);
        field_65_lcbPlcfandTxt          = LittleEndian.getInt(data, 0xc6 + offset);
        field_66_fcPlcfsed              = LittleEndian.getInt(data, 0xca + offset);
        field_67_lcbPlcfsed             = LittleEndian.getInt(data, 0xce + offset);
        field_68_fcPlcpad               = LittleEndian.getInt(data, 0xd2 + offset);
        field_69_lcbPlcpad              = LittleEndian.getInt(data, 0xd6 + offset);
        field_70_fcPlcfphe              = LittleEndian.getInt(data, 0xda + offset);
        field_71_lcbPlcfphe             = LittleEndian.getInt(data, 0xde + offset);
        field_72_fcSttbfglsy            = LittleEndian.getInt(data, 0xe2 + offset);
        field_73_lcbSttbfglsy           = LittleEndian.getInt(data, 0xe6 + offset);
        field_74_fcPlcfglsy             = LittleEndian.getInt(data, 0xea + offset);
        field_75_lcbPlcfglsy            = LittleEndian.getInt(data, 0xee + offset);
        field_76_fcPlcfhdd              = LittleEndian.getInt(data, 0xf2 + offset);
        field_77_lcbPlcfhdd             = LittleEndian.getInt(data, 0xf6 + offset);
        field_78_fcPlcfbteChpx          = LittleEndian.getInt(data, 0xfa + offset);
        field_79_lcbPlcfbteChpx         = LittleEndian.getInt(data, 0xfe + offset);
        field_80_fcPlcfbtePapx          = LittleEndian.getInt(data, 0x102 + offset);
        field_81_lcbPlcfbtePapx         = LittleEndian.getInt(data, 0x106 + offset);
        field_82_fcPlcfsea              = LittleEndian.getInt(data, 0x10a + offset);
        field_83_lcbPlcfsea             = LittleEndian.getInt(data, 0x10e + offset);
        field_84_fcSttbfffn             = LittleEndian.getInt(data, 0x112 + offset);
        field_85_lcbSttbfffn            = LittleEndian.getInt(data, 0x116 + offset);
        field_86_fcPlcffldMom           = LittleEndian.getInt(data, 0x11a + offset);
        field_87_lcbPlcffldMom          = LittleEndian.getInt(data, 0x11e + offset);
        field_88_fcPlcffldHdr           = LittleEndian.getInt(data, 0x122 + offset);
        field_89_lcbPlcffldHdr          = LittleEndian.getInt(data, 0x126 + offset);
        field_90_fcPlcffldFtn           = LittleEndian.getInt(data, 0x12a + offset);
        field_91_lcbPlcffldFtn          = LittleEndian.getInt(data, 0x12e + offset);
        field_92_fcPlcffldAtn           = LittleEndian.getInt(data, 0x132 + offset);
        field_93_lcbPlcffldAtn          = LittleEndian.getInt(data, 0x136 + offset);
        field_94_fcPlcffldMcr           = LittleEndian.getInt(data, 0x13a + offset);
        field_95_lcbPlcffldMcr          = LittleEndian.getInt(data, 0x13e + offset);
        field_96_fcSttbfbkmk            = LittleEndian.getInt(data, 0x142 + offset);
        field_97_lcbSttbfbkmk           = LittleEndian.getInt(data, 0x146 + offset);
        field_98_fcPlcfbkf              = LittleEndian.getInt(data, 0x14a + offset);
        field_99_lcbPlcfbkf             = LittleEndian.getInt(data, 0x14e + offset);
        field_100_fcPlcfbkl             = LittleEndian.getInt(data, 0x152 + offset);
        field_101_lcbPlcfbkl            = LittleEndian.getInt(data, 0x156 + offset);
        field_102_fcCmds                = LittleEndian.getInt(data, 0x15a + offset);
        field_103_lcbCmds               = LittleEndian.getInt(data, 0x15e + offset);
        field_104_fcPlcmcr              = LittleEndian.getInt(data, 0x162 + offset);
        field_105_lcbPlcmcr             = LittleEndian.getInt(data, 0x166 + offset);
        field_106_fcSttbfmcr            = LittleEndian.getInt(data, 0x16a + offset);
        field_107_lcbSttbfmcr           = LittleEndian.getInt(data, 0x16e + offset);
        field_108_fcPrDrvr              = LittleEndian.getInt(data, 0x172 + offset);
        field_109_lcbPrDrvr             = LittleEndian.getInt(data, 0x176 + offset);
        field_110_fcPrEnvPort           = LittleEndian.getInt(data, 0x17a + offset);
        field_111_lcbPrEnvPort          = LittleEndian.getInt(data, 0x17e + offset);
        field_112_fcPrEnvLand           = LittleEndian.getInt(data, 0x182 + offset);
        field_113_lcbPrEnvLand          = LittleEndian.getInt(data, 0x186 + offset);
        field_114_fcWss                 = LittleEndian.getInt(data, 0x18a + offset);
        field_115_lcbWss                = LittleEndian.getInt(data, 0x18e + offset);
        field_116_fcDop                 = LittleEndian.getInt(data, 0x192 + offset);
        field_117_lcbDop                = LittleEndian.getInt(data, 0x196 + offset);
        field_118_fcSttbfAssoc          = LittleEndian.getInt(data, 0x19a + offset);
        field_119_lcbSttbfAssoc         = LittleEndian.getInt(data, 0x19e + offset);
        field_120_fcClx                 = LittleEndian.getInt(data, 0x1a2 + offset);
        field_121_lcbClx                = LittleEndian.getInt(data, 0x1a6 + offset);
        field_122_fcPlcfpgdFtn          = LittleEndian.getInt(data, 0x1aa + offset);
        field_123_lcbPlcfpgdFtn         = LittleEndian.getInt(data, 0x1ae + offset);
        field_124_fcAutosaveSource      = LittleEndian.getInt(data, 0x1b2 + offset);
        field_125_lcbAutosaveSource     = LittleEndian.getInt(data, 0x1b6 + offset);
        field_126_fcGrpXstAtnOwners     = LittleEndian.getInt(data, 0x1ba + offset);
        field_127_lcbGrpXstAtnOwners    = LittleEndian.getInt(data, 0x1be + offset);
        field_128_fcSttbfAtnbkmk        = LittleEndian.getInt(data, 0x1c2 + offset);
        field_129_lcbSttbfAtnbkmk       = LittleEndian.getInt(data, 0x1c6 + offset);
        field_130_fcPlcdoaMom           = LittleEndian.getInt(data, 0x1ca + offset);
        field_131_lcbPlcdoaMom          = LittleEndian.getInt(data, 0x1ce + offset);
        field_132_fcPlcdoaHdr           = LittleEndian.getInt(data, 0x1d2 + offset);
        field_133_lcbPlcdoaHdr          = LittleEndian.getInt(data, 0x1d6 + offset);
        field_134_fcPlcspaMom           = LittleEndian.getInt(data, 0x1da + offset);
        field_135_lcbPlcspaMom          = LittleEndian.getInt(data, 0x1de + offset);
        field_136_fcPlcspaHdr           = LittleEndian.getInt(data, 0x1e2 + offset);
        field_137_lcbPlcspaHdr          = LittleEndian.getInt(data, 0x1e6 + offset);
        field_138_fcPlcfAtnbkf          = LittleEndian.getInt(data, 0x1ea + offset);
        field_139_lcbPlcfAtnbkf         = LittleEndian.getInt(data, 0x1ee + offset);
        field_140_fcPlcfAtnbkl          = LittleEndian.getInt(data, 0x1f2 + offset);
        field_141_lcbPlcfAtnbkl         = LittleEndian.getInt(data, 0x1f6 + offset);
        field_142_fcPms                 = LittleEndian.getInt(data, 0x1fa + offset);
        field_143_lcbPms                = LittleEndian.getInt(data, 0x1fe + offset);
        field_144_fcFormFldSttbs        = LittleEndian.getInt(data, 0x202 + offset);
        field_145_lcbFormFldSttbs       = LittleEndian.getInt(data, 0x206 + offset);
        field_146_fcPlcfendRef          = LittleEndian.getInt(data, 0x20a + offset);
        field_147_lcbPlcfendRef         = LittleEndian.getInt(data, 0x20e + offset);
        field_148_fcPlcfendTxt          = LittleEndian.getInt(data, 0x212 + offset);
        field_149_lcbPlcfendTxt         = LittleEndian.getInt(data, 0x216 + offset);
        field_150_fcPlcffldEdn          = LittleEndian.getInt(data, 0x21a + offset);
        field_151_lcbPlcffldEdn         = LittleEndian.getInt(data, 0x21e + offset);
        field_152_fcPlcfpgdEdn          = LittleEndian.getInt(data, 0x222 + offset);
        field_153_lcbPlcfpgdEdn         = LittleEndian.getInt(data, 0x226 + offset);
        field_154_fcDggInfo             = LittleEndian.getInt(data, 0x22a + offset);
        field_155_lcbDggInfo            = LittleEndian.getInt(data, 0x22e + offset);
        field_156_fcSttbfRMark          = LittleEndian.getInt(data, 0x232 + offset);
        field_157_lcbSttbfRMark         = LittleEndian.getInt(data, 0x236 + offset);
        field_158_fcSttbCaption         = LittleEndian.getInt(data, 0x23a + offset);
        field_159_lcbSttbCaption        = LittleEndian.getInt(data, 0x23e + offset);
        field_160_fcSttbAutoCaption     = LittleEndian.getInt(data, 0x242 + offset);
        field_161_lcbSttbAutoCaption    = LittleEndian.getInt(data, 0x246 + offset);
        field_162_fcPlcfwkb             = LittleEndian.getInt(data, 0x24a + offset);
        field_163_lcbPlcfwkb            = LittleEndian.getInt(data, 0x24e + offset);
        field_164_fcPlcfspl             = LittleEndian.getInt(data, 0x252 + offset);
        field_165_lcbPlcfspl            = LittleEndian.getInt(data, 0x256 + offset);
        field_166_fcPlcftxbxTxt         = LittleEndian.getInt(data, 0x25a + offset);
        field_167_lcbPlcftxbxTxt        = LittleEndian.getInt(data, 0x25e + offset);
        field_168_fcPlcffldTxbx         = LittleEndian.getInt(data, 0x262 + offset);
        field_169_lcbPlcffldTxbx        = LittleEndian.getInt(data, 0x266 + offset);
        field_170_fcPlcfhdrtxbxTxt      = LittleEndian.getInt(data, 0x26a + offset);
        field_171_lcbPlcfhdrtxbxTxt     = LittleEndian.getInt(data, 0x26e + offset);
        field_172_fcPlcffldHdrTxbx      = LittleEndian.getInt(data, 0x272 + offset);
        field_173_lcbPlcffldHdrTxbx     = LittleEndian.getInt(data, 0x276 + offset);
        field_174_fcStwUser             = LittleEndian.getInt(data, 0x27a + offset);
        field_175_lcbStwUser            = LittleEndian.getInt(data, 0x27e + offset);
        field_176_fcSttbttmbd           = LittleEndian.getInt(data, 0x282 + offset);
        field_177_cbSttbttmbd           = LittleEndian.getInt(data, 0x286 + offset);
        field_178_fcUnused              = LittleEndian.getInt(data, 0x28a + offset);
        field_179_lcbUnused             = LittleEndian.getInt(data, 0x28e + offset);
        field_180_fcPgdMother           = LittleEndian.getInt(data, 0x292 + offset);
        field_181_lcbPgdMother          = LittleEndian.getInt(data, 0x296 + offset);
        field_182_fcBkdMother           = LittleEndian.getInt(data, 0x29a + offset);
        field_183_lcbBkdMother          = LittleEndian.getInt(data, 0x29e + offset);
        field_184_fcPgdFtn              = LittleEndian.getInt(data, 0x2a2 + offset);
        field_185_lcbPgdFtn             = LittleEndian.getInt(data, 0x2a6 + offset);
        field_186_fcBkdFtn              = LittleEndian.getInt(data, 0x2aa + offset);
        field_187_lcbBkdFtn             = LittleEndian.getInt(data, 0x2ae + offset);
        field_188_fcPgdEdn              = LittleEndian.getInt(data, 0x2b2 + offset);
        field_189_lcbPgdEdn             = LittleEndian.getInt(data, 0x2b6 + offset);
        field_190_fcBkdEdn              = LittleEndian.getInt(data, 0x2ba + offset);
        field_191_lcbBkdEdn             = LittleEndian.getInt(data, 0x2be + offset);
        field_192_fcSttbfIntlFld        = LittleEndian.getInt(data, 0x2c2 + offset);
        field_193_lcbSttbfIntlFld       = LittleEndian.getInt(data, 0x2c6 + offset);
        field_194_fcRouteSlip           = LittleEndian.getInt(data, 0x2ca + offset);
        field_195_lcbRouteSlip          = LittleEndian.getInt(data, 0x2ce + offset);
        field_196_fcSttbSavedBy         = LittleEndian.getInt(data, 0x2d2 + offset);
        field_197_lcbSttbSavedBy        = LittleEndian.getInt(data, 0x2d6 + offset);
        field_198_fcSttbFnm             = LittleEndian.getInt(data, 0x2da + offset);
        field_199_lcbSttbFnm            = LittleEndian.getInt(data, 0x2de + offset);
        field_200_fcPlcfLst             = LittleEndian.getInt(data, 0x2e2 + offset);
        field_201_lcbPlcfLst            = LittleEndian.getInt(data, 0x2e6 + offset);
        field_202_fcPlfLfo              = LittleEndian.getInt(data, 0x2ea + offset);
        field_203_lcbPlfLfo             = LittleEndian.getInt(data, 0x2ee + offset);
        field_204_fcPlcftxbxBkd         = LittleEndian.getInt(data, 0x2f2 + offset);
        field_205_lcbPlcftxbxBkd        = LittleEndian.getInt(data, 0x2f6 + offset);
        field_206_fcPlcftxbxHdrBkd      = LittleEndian.getInt(data, 0x2fa + offset);
        field_207_lcbPlcftxbxHdrBkd     = LittleEndian.getInt(data, 0x2fe + offset);
        field_208_fcDocUndo             = LittleEndian.getInt(data, 0x302 + offset);
        field_209_lcbDocUndo            = LittleEndian.getInt(data, 0x306 + offset);
        field_210_fcRgbuse              = LittleEndian.getInt(data, 0x30a + offset);
        field_211_lcbRgbuse             = LittleEndian.getInt(data, 0x30e + offset);
        field_212_fcUsp                 = LittleEndian.getInt(data, 0x312 + offset);
        field_213_lcbUsp                = LittleEndian.getInt(data, 0x316 + offset);
        field_214_fcUskf                = LittleEndian.getInt(data, 0x31a + offset);
        field_215_lcbUskf               = LittleEndian.getInt(data, 0x31e + offset);
        field_216_fcPlcupcRgbuse        = LittleEndian.getInt(data, 0x322 + offset);
        field_217_lcbPlcupcRgbuse       = LittleEndian.getInt(data, 0x326 + offset);
        field_218_fcPlcupcUsp           = LittleEndian.getInt(data, 0x32a + offset);
        field_219_lcbPlcupcUsp          = LittleEndian.getInt(data, 0x32e + offset);
        field_220_fcSttbGlsyStyle       = LittleEndian.getInt(data, 0x332 + offset);
        field_221_lcbSttbGlsyStyle      = LittleEndian.getInt(data, 0x336 + offset);
        field_222_fcPlgosl              = LittleEndian.getInt(data, 0x33a + offset);
        field_223_lcbPlgosl             = LittleEndian.getInt(data, 0x33e + offset);
        field_224_fcPlcocx              = LittleEndian.getInt(data, 0x342 + offset);
        field_225_lcbPlcocx             = LittleEndian.getInt(data, 0x346 + offset);
        field_226_fcPlcfbteLvc          = LittleEndian.getInt(data, 0x34a + offset);
        field_227_lcbPlcfbteLvc         = LittleEndian.getInt(data, 0x34e + offset);
        field_228_dwLowDateTime         = LittleEndian.getInt(data, 0x352 + offset);
        field_229_dwHighDateTime        = LittleEndian.getInt(data, 0x356 + offset);
        field_230_fcPlcflvc             = LittleEndian.getInt(data, 0x35a + offset);
        field_231_lcbPlcflvc            = LittleEndian.getInt(data, 0x35e + offset);
        field_232_fcPlcasumy            = LittleEndian.getInt(data, 0x362 + offset);
        field_233_lcbPlcasumy           = LittleEndian.getInt(data, 0x366 + offset);
        field_234_fcPlcfgram            = LittleEndian.getInt(data, 0x36a + offset);
        field_235_lcbPlcfgram           = LittleEndian.getInt(data, 0x36e + offset);
        field_236_fcSttbListNames       = LittleEndian.getInt(data, 0x372 + offset);
        field_237_lcbSttbListNames      = LittleEndian.getInt(data, 0x376 + offset);
        field_238_fcSttbfUssr           = LittleEndian.getInt(data, 0x37a + offset);
        field_239_lcbSttbfUssr          = LittleEndian.getInt(data, 0x37e + offset);

    }

    public void serialize(byte[] data, int offset)
    {
        LittleEndian.putShort(data, 0x0 + offset, (short)field_1_wIdent);
        LittleEndian.putShort(data, 0x2 + offset, (short)field_2_nFib);
        LittleEndian.putShort(data, 0x4 + offset, (short)field_3_nProduct);
        LittleEndian.putShort(data, 0x6 + offset, (short)field_4_lid);
        LittleEndian.putShort(data, 0x8 + offset, (short)field_5_pnNext);
        LittleEndian.putShort(data, 0xa + offset, field_6_options);
        LittleEndian.putShort(data, 0xc + offset, (short)field_7_nFibBack);
        LittleEndian.putShort(data, 0xe + offset, (short)field_8_lKey);
        LittleEndian.putShort(data, 0x10 + offset, (short)field_9_envr);
        LittleEndian.putShort(data, 0x12 + offset, field_10_history);
        LittleEndian.putShort(data, 0x14 + offset, (short)field_11_chs);
        LittleEndian.putShort(data, 0x16 + offset, (short)field_12_chsTables);
        LittleEndian.putInt(data, 0x18 + offset, field_13_fcMin);
        LittleEndian.putInt(data, 0x1c + offset, field_14_fcMac);
        LittleEndian.putShort(data, 0x20 + offset, (short)field_15_csw);
        LittleEndian.putShort(data, 0x22 + offset, (short)field_16_wMagicCreated);
        LittleEndian.putShort(data, 0x24 + offset, (short)field_17_wMagicRevised);
        LittleEndian.putShort(data, 0x26 + offset, (short)field_18_wMagicCreatedPrivate);
        LittleEndian.putShort(data, 0x28 + offset, (short)field_19_wMagicRevisedPrivate);
        LittleEndian.putShort(data, 0x2a + offset, (short)field_20_pnFbpChpFirst_W6);
        LittleEndian.putShort(data, 0x2c + offset, (short)field_21_pnChpFirst_W6);
        LittleEndian.putShort(data, 0x2e + offset, (short)field_22_cpnBteChp_W6);
        LittleEndian.putShort(data, 0x30 + offset, (short)field_23_pnFbpPapFirst_W6);
        LittleEndian.putShort(data, 0x32 + offset, (short)field_24_pnPapFirst_W6);
        LittleEndian.putShort(data, 0x34 + offset, (short)field_25_cpnBtePap_W6);
        LittleEndian.putShort(data, 0x36 + offset, (short)field_26_pnFbpLvcFirst_W6);
        LittleEndian.putShort(data, 0x38 + offset, (short)field_27_pnLvcFirst_W6);
        LittleEndian.putShort(data, 0x3a + offset, (short)field_28_cpnBteLvc_W6);
        LittleEndian.putShort(data, 0x3c + offset, (short)field_29_lidFE);
        LittleEndian.putShort(data, 0x3e + offset, (short)field_30_clw);
        LittleEndian.putInt(data, 0x40 + offset, field_31_cbMac);
        LittleEndian.putInt(data, 0x44 + offset, field_32_lProductCreated);
        LittleEndian.putInt(data, 0x48 + offset, field_33_lProductRevised);
        LittleEndian.putInt(data, 0x4c + offset, field_34_ccpText);
        LittleEndian.putInt(data, 0x50 + offset, field_35_ccpFtn);
        LittleEndian.putInt(data, 0x54 + offset, field_36_ccpHdd);
        LittleEndian.putInt(data, 0x58 + offset, field_37_ccpMcr);
        LittleEndian.putInt(data, 0x5c + offset, field_38_ccpAtn);
        LittleEndian.putInt(data, 0x60 + offset, field_39_ccpEdn);
        LittleEndian.putInt(data, 0x64 + offset, field_40_ccpTxbx);
        LittleEndian.putInt(data, 0x68 + offset, field_41_ccpHdrTxbx);
        LittleEndian.putInt(data, 0x6c + offset, field_42_pnFbpChpFirst);
        LittleEndian.putInt(data, 0x70 + offset, field_43_pnChpFirst);
        LittleEndian.putInt(data, 0x74 + offset, field_44_cpnBteChp);
        LittleEndian.putInt(data, 0x78 + offset, field_45_pnFbpPapFirst);
        LittleEndian.putInt(data, 0x7c + offset, field_46_pnPapFirst);
        LittleEndian.putInt(data, 0x80 + offset, field_47_cpnBtePap);
        LittleEndian.putInt(data, 0x84 + offset, field_48_pnFbpLvcFirst);
        LittleEndian.putInt(data, 0x88 + offset, field_49_pnLvcFirst);
        LittleEndian.putInt(data, 0x8c + offset, field_50_cpnBteLvc);
        LittleEndian.putInt(data, 0x90 + offset, field_51_fcIslandFirst);
        LittleEndian.putInt(data, 0x94 + offset, field_52_fcIslandLim);
        LittleEndian.putShort(data, 0x98 + offset, (short)field_53_cfclcb);
        LittleEndian.putInt(data, 0x9a + offset, field_54_fcStshfOrig);
        LittleEndian.putInt(data, 0x9e + offset, field_55_lcbStshfOrig);
        LittleEndian.putInt(data, 0xa2 + offset, field_56_fcStshf);
        LittleEndian.putInt(data, 0xa6 + offset, field_57_lcbStshf);
        LittleEndian.putInt(data, 0xaa + offset, field_58_fcPlcffndRef);
        LittleEndian.putInt(data, 0xae + offset, field_59_lcbPlcffndRef);
        LittleEndian.putInt(data, 0xb2 + offset, field_60_fcPlcffndTxt);
        LittleEndian.putInt(data, 0xb6 + offset, field_61_lcbPlcffndTxt);
        LittleEndian.putInt(data, 0xba + offset, field_62_fcPlcfandRef);
        LittleEndian.putInt(data, 0xbe + offset, field_63_lcbPlcfandRef);
        LittleEndian.putInt(data, 0xc2 + offset, field_64_fcPlcfandTxt);
        LittleEndian.putInt(data, 0xc6 + offset, field_65_lcbPlcfandTxt);
        LittleEndian.putInt(data, 0xca + offset, field_66_fcPlcfsed);
        LittleEndian.putInt(data, 0xce + offset, field_67_lcbPlcfsed);
        LittleEndian.putInt(data, 0xd2 + offset, field_68_fcPlcpad);
        LittleEndian.putInt(data, 0xd6 + offset, field_69_lcbPlcpad);
        LittleEndian.putInt(data, 0xda + offset, field_70_fcPlcfphe);
        LittleEndian.putInt(data, 0xde + offset, field_71_lcbPlcfphe);
        LittleEndian.putInt(data, 0xe2 + offset, field_72_fcSttbfglsy);
        LittleEndian.putInt(data, 0xe6 + offset, field_73_lcbSttbfglsy);
        LittleEndian.putInt(data, 0xea + offset, field_74_fcPlcfglsy);
        LittleEndian.putInt(data, 0xee + offset, field_75_lcbPlcfglsy);
        LittleEndian.putInt(data, 0xf2 + offset, field_76_fcPlcfhdd);
        LittleEndian.putInt(data, 0xf6 + offset, field_77_lcbPlcfhdd);
        LittleEndian.putInt(data, 0xfa + offset, field_78_fcPlcfbteChpx);
        LittleEndian.putInt(data, 0xfe + offset, field_79_lcbPlcfbteChpx);
        LittleEndian.putInt(data, 0x102 + offset, field_80_fcPlcfbtePapx);
        LittleEndian.putInt(data, 0x106 + offset, field_81_lcbPlcfbtePapx);
        LittleEndian.putInt(data, 0x10a + offset, field_82_fcPlcfsea);
        LittleEndian.putInt(data, 0x10e + offset, field_83_lcbPlcfsea);
        LittleEndian.putInt(data, 0x112 + offset, field_84_fcSttbfffn);
        LittleEndian.putInt(data, 0x116 + offset, field_85_lcbSttbfffn);
        LittleEndian.putInt(data, 0x11a + offset, field_86_fcPlcffldMom);
        LittleEndian.putInt(data, 0x11e + offset, field_87_lcbPlcffldMom);
        LittleEndian.putInt(data, 0x122 + offset, field_88_fcPlcffldHdr);
        LittleEndian.putInt(data, 0x126 + offset, field_89_lcbPlcffldHdr);
        LittleEndian.putInt(data, 0x12a + offset, field_90_fcPlcffldFtn);
        LittleEndian.putInt(data, 0x12e + offset, field_91_lcbPlcffldFtn);
        LittleEndian.putInt(data, 0x132 + offset, field_92_fcPlcffldAtn);
        LittleEndian.putInt(data, 0x136 + offset, field_93_lcbPlcffldAtn);
        LittleEndian.putInt(data, 0x13a + offset, field_94_fcPlcffldMcr);
        LittleEndian.putInt(data, 0x13e + offset, field_95_lcbPlcffldMcr);
        LittleEndian.putInt(data, 0x142 + offset, field_96_fcSttbfbkmk);
        LittleEndian.putInt(data, 0x146 + offset, field_97_lcbSttbfbkmk);
        LittleEndian.putInt(data, 0x14a + offset, field_98_fcPlcfbkf);
        LittleEndian.putInt(data, 0x14e + offset, field_99_lcbPlcfbkf);
        LittleEndian.putInt(data, 0x152 + offset, field_100_fcPlcfbkl);
        LittleEndian.putInt(data, 0x156 + offset, field_101_lcbPlcfbkl);
        LittleEndian.putInt(data, 0x15a + offset, field_102_fcCmds);
        LittleEndian.putInt(data, 0x15e + offset, field_103_lcbCmds);
        LittleEndian.putInt(data, 0x162 + offset, field_104_fcPlcmcr);
        LittleEndian.putInt(data, 0x166 + offset, field_105_lcbPlcmcr);
        LittleEndian.putInt(data, 0x16a + offset, field_106_fcSttbfmcr);
        LittleEndian.putInt(data, 0x16e + offset, field_107_lcbSttbfmcr);
        LittleEndian.putInt(data, 0x172 + offset, field_108_fcPrDrvr);
        LittleEndian.putInt(data, 0x176 + offset, field_109_lcbPrDrvr);
        LittleEndian.putInt(data, 0x17a + offset, field_110_fcPrEnvPort);
        LittleEndian.putInt(data, 0x17e + offset, field_111_lcbPrEnvPort);
        LittleEndian.putInt(data, 0x182 + offset, field_112_fcPrEnvLand);
        LittleEndian.putInt(data, 0x186 + offset, field_113_lcbPrEnvLand);
        LittleEndian.putInt(data, 0x18a + offset, field_114_fcWss);
        LittleEndian.putInt(data, 0x18e + offset, field_115_lcbWss);
        LittleEndian.putInt(data, 0x192 + offset, field_116_fcDop);
        LittleEndian.putInt(data, 0x196 + offset, field_117_lcbDop);
        LittleEndian.putInt(data, 0x19a + offset, field_118_fcSttbfAssoc);
        LittleEndian.putInt(data, 0x19e + offset, field_119_lcbSttbfAssoc);
        LittleEndian.putInt(data, 0x1a2 + offset, field_120_fcClx);
        LittleEndian.putInt(data, 0x1a6 + offset, field_121_lcbClx);
        LittleEndian.putInt(data, 0x1aa + offset, field_122_fcPlcfpgdFtn);
        LittleEndian.putInt(data, 0x1ae + offset, field_123_lcbPlcfpgdFtn);
        LittleEndian.putInt(data, 0x1b2 + offset, field_124_fcAutosaveSource);
        LittleEndian.putInt(data, 0x1b6 + offset, field_125_lcbAutosaveSource);
        LittleEndian.putInt(data, 0x1ba + offset, field_126_fcGrpXstAtnOwners);
        LittleEndian.putInt(data, 0x1be + offset, field_127_lcbGrpXstAtnOwners);
        LittleEndian.putInt(data, 0x1c2 + offset, field_128_fcSttbfAtnbkmk);
        LittleEndian.putInt(data, 0x1c6 + offset, field_129_lcbSttbfAtnbkmk);
        LittleEndian.putInt(data, 0x1ca + offset, field_130_fcPlcdoaMom);
        LittleEndian.putInt(data, 0x1ce + offset, field_131_lcbPlcdoaMom);
        LittleEndian.putInt(data, 0x1d2 + offset, field_132_fcPlcdoaHdr);
        LittleEndian.putInt(data, 0x1d6 + offset, field_133_lcbPlcdoaHdr);
        LittleEndian.putInt(data, 0x1da + offset, field_134_fcPlcspaMom);
        LittleEndian.putInt(data, 0x1de + offset, field_135_lcbPlcspaMom);
        LittleEndian.putInt(data, 0x1e2 + offset, field_136_fcPlcspaHdr);
        LittleEndian.putInt(data, 0x1e6 + offset, field_137_lcbPlcspaHdr);
        LittleEndian.putInt(data, 0x1ea + offset, field_138_fcPlcfAtnbkf);
        LittleEndian.putInt(data, 0x1ee + offset, field_139_lcbPlcfAtnbkf);
        LittleEndian.putInt(data, 0x1f2 + offset, field_140_fcPlcfAtnbkl);
        LittleEndian.putInt(data, 0x1f6 + offset, field_141_lcbPlcfAtnbkl);
        LittleEndian.putInt(data, 0x1fa + offset, field_142_fcPms);
        LittleEndian.putInt(data, 0x1fe + offset, field_143_lcbPms);
        LittleEndian.putInt(data, 0x202 + offset, field_144_fcFormFldSttbs);
        LittleEndian.putInt(data, 0x206 + offset, field_145_lcbFormFldSttbs);
        LittleEndian.putInt(data, 0x20a + offset, field_146_fcPlcfendRef);
        LittleEndian.putInt(data, 0x20e + offset, field_147_lcbPlcfendRef);
        LittleEndian.putInt(data, 0x212 + offset, field_148_fcPlcfendTxt);
        LittleEndian.putInt(data, 0x216 + offset, field_149_lcbPlcfendTxt);
        LittleEndian.putInt(data, 0x21a + offset, field_150_fcPlcffldEdn);
        LittleEndian.putInt(data, 0x21e + offset, field_151_lcbPlcffldEdn);
        LittleEndian.putInt(data, 0x222 + offset, field_152_fcPlcfpgdEdn);
        LittleEndian.putInt(data, 0x226 + offset, field_153_lcbPlcfpgdEdn);
        LittleEndian.putInt(data, 0x22a + offset, field_154_fcDggInfo);
        LittleEndian.putInt(data, 0x22e + offset, field_155_lcbDggInfo);
        LittleEndian.putInt(data, 0x232 + offset, field_156_fcSttbfRMark);
        LittleEndian.putInt(data, 0x236 + offset, field_157_lcbSttbfRMark);
        LittleEndian.putInt(data, 0x23a + offset, field_158_fcSttbCaption);
        LittleEndian.putInt(data, 0x23e + offset, field_159_lcbSttbCaption);
        LittleEndian.putInt(data, 0x242 + offset, field_160_fcSttbAutoCaption);
        LittleEndian.putInt(data, 0x246 + offset, field_161_lcbSttbAutoCaption);
        LittleEndian.putInt(data, 0x24a + offset, field_162_fcPlcfwkb);
        LittleEndian.putInt(data, 0x24e + offset, field_163_lcbPlcfwkb);
        LittleEndian.putInt(data, 0x252 + offset, field_164_fcPlcfspl);
        LittleEndian.putInt(data, 0x256 + offset, field_165_lcbPlcfspl);
        LittleEndian.putInt(data, 0x25a + offset, field_166_fcPlcftxbxTxt);
        LittleEndian.putInt(data, 0x25e + offset, field_167_lcbPlcftxbxTxt);
        LittleEndian.putInt(data, 0x262 + offset, field_168_fcPlcffldTxbx);
        LittleEndian.putInt(data, 0x266 + offset, field_169_lcbPlcffldTxbx);
        LittleEndian.putInt(data, 0x26a + offset, field_170_fcPlcfhdrtxbxTxt);
        LittleEndian.putInt(data, 0x26e + offset, field_171_lcbPlcfhdrtxbxTxt);
        LittleEndian.putInt(data, 0x272 + offset, field_172_fcPlcffldHdrTxbx);
        LittleEndian.putInt(data, 0x276 + offset, field_173_lcbPlcffldHdrTxbx);
        LittleEndian.putInt(data, 0x27a + offset, field_174_fcStwUser);
        LittleEndian.putInt(data, 0x27e + offset, field_175_lcbStwUser);
        LittleEndian.putInt(data, 0x282 + offset, field_176_fcSttbttmbd);
        LittleEndian.putInt(data, 0x286 + offset, field_177_cbSttbttmbd);
        LittleEndian.putInt(data, 0x28a + offset, field_178_fcUnused);
        LittleEndian.putInt(data, 0x28e + offset, field_179_lcbUnused);
        LittleEndian.putInt(data, 0x292 + offset, field_180_fcPgdMother);
        LittleEndian.putInt(data, 0x296 + offset, field_181_lcbPgdMother);
        LittleEndian.putInt(data, 0x29a + offset, field_182_fcBkdMother);
        LittleEndian.putInt(data, 0x29e + offset, field_183_lcbBkdMother);
        LittleEndian.putInt(data, 0x2a2 + offset, field_184_fcPgdFtn);
        LittleEndian.putInt(data, 0x2a6 + offset, field_185_lcbPgdFtn);
        LittleEndian.putInt(data, 0x2aa + offset, field_186_fcBkdFtn);
        LittleEndian.putInt(data, 0x2ae + offset, field_187_lcbBkdFtn);
        LittleEndian.putInt(data, 0x2b2 + offset, field_188_fcPgdEdn);
        LittleEndian.putInt(data, 0x2b6 + offset, field_189_lcbPgdEdn);
        LittleEndian.putInt(data, 0x2ba + offset, field_190_fcBkdEdn);
        LittleEndian.putInt(data, 0x2be + offset, field_191_lcbBkdEdn);
        LittleEndian.putInt(data, 0x2c2 + offset, field_192_fcSttbfIntlFld);
        LittleEndian.putInt(data, 0x2c6 + offset, field_193_lcbSttbfIntlFld);
        LittleEndian.putInt(data, 0x2ca + offset, field_194_fcRouteSlip);
        LittleEndian.putInt(data, 0x2ce + offset, field_195_lcbRouteSlip);
        LittleEndian.putInt(data, 0x2d2 + offset, field_196_fcSttbSavedBy);
        LittleEndian.putInt(data, 0x2d6 + offset, field_197_lcbSttbSavedBy);
        LittleEndian.putInt(data, 0x2da + offset, field_198_fcSttbFnm);
        LittleEndian.putInt(data, 0x2de + offset, field_199_lcbSttbFnm);
        LittleEndian.putInt(data, 0x2e2 + offset, field_200_fcPlcfLst);
        LittleEndian.putInt(data, 0x2e6 + offset, field_201_lcbPlcfLst);
        LittleEndian.putInt(data, 0x2ea + offset, field_202_fcPlfLfo);
        LittleEndian.putInt(data, 0x2ee + offset, field_203_lcbPlfLfo);
        LittleEndian.putInt(data, 0x2f2 + offset, field_204_fcPlcftxbxBkd);
        LittleEndian.putInt(data, 0x2f6 + offset, field_205_lcbPlcftxbxBkd);
        LittleEndian.putInt(data, 0x2fa + offset, field_206_fcPlcftxbxHdrBkd);
        LittleEndian.putInt(data, 0x2fe + offset, field_207_lcbPlcftxbxHdrBkd);
        LittleEndian.putInt(data, 0x302 + offset, field_208_fcDocUndo);
        LittleEndian.putInt(data, 0x306 + offset, field_209_lcbDocUndo);
        LittleEndian.putInt(data, 0x30a + offset, field_210_fcRgbuse);
        LittleEndian.putInt(data, 0x30e + offset, field_211_lcbRgbuse);
        LittleEndian.putInt(data, 0x312 + offset, field_212_fcUsp);
        LittleEndian.putInt(data, 0x316 + offset, field_213_lcbUsp);
        LittleEndian.putInt(data, 0x31a + offset, field_214_fcUskf);
        LittleEndian.putInt(data, 0x31e + offset, field_215_lcbUskf);
        LittleEndian.putInt(data, 0x322 + offset, field_216_fcPlcupcRgbuse);
        LittleEndian.putInt(data, 0x326 + offset, field_217_lcbPlcupcRgbuse);
        LittleEndian.putInt(data, 0x32a + offset, field_218_fcPlcupcUsp);
        LittleEndian.putInt(data, 0x32e + offset, field_219_lcbPlcupcUsp);
        LittleEndian.putInt(data, 0x332 + offset, field_220_fcSttbGlsyStyle);
        LittleEndian.putInt(data, 0x336 + offset, field_221_lcbSttbGlsyStyle);
        LittleEndian.putInt(data, 0x33a + offset, field_222_fcPlgosl);
        LittleEndian.putInt(data, 0x33e + offset, field_223_lcbPlgosl);
        LittleEndian.putInt(data, 0x342 + offset, field_224_fcPlcocx);
        LittleEndian.putInt(data, 0x346 + offset, field_225_lcbPlcocx);
        LittleEndian.putInt(data, 0x34a + offset, field_226_fcPlcfbteLvc);
        LittleEndian.putInt(data, 0x34e + offset, field_227_lcbPlcfbteLvc);
        LittleEndian.putInt(data, 0x352 + offset, field_228_dwLowDateTime);
        LittleEndian.putInt(data, 0x356 + offset, field_229_dwHighDateTime);
        LittleEndian.putInt(data, 0x35a + offset, field_230_fcPlcflvc);
        LittleEndian.putInt(data, 0x35e + offset, field_231_lcbPlcflvc);
        LittleEndian.putInt(data, 0x362 + offset, field_232_fcPlcasumy);
        LittleEndian.putInt(data, 0x366 + offset, field_233_lcbPlcasumy);
        LittleEndian.putInt(data, 0x36a + offset, field_234_fcPlcfgram);
        LittleEndian.putInt(data, 0x36e + offset, field_235_lcbPlcfgram);
        LittleEndian.putInt(data, 0x372 + offset, field_236_fcSttbListNames);
        LittleEndian.putInt(data, 0x376 + offset, field_237_lcbSttbListNames);
        LittleEndian.putInt(data, 0x37a + offset, field_238_fcSttbfUssr);
        LittleEndian.putInt(data, 0x37e + offset, field_239_lcbSttbfUssr);
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[FIB]\n");

        buffer.append("    .wIdent               = ");
        buffer.append(HexDump.intToHex(getWIdent()));
        buffer.append(" (").append(getWIdent()).append(" )\n");

        buffer.append("    .nFib                 = ");
        buffer.append(HexDump.intToHex(getNFib()));
        buffer.append(" (").append(getNFib()).append(" )\n");

        buffer.append("    .nProduct             = ");
        buffer.append(HexDump.intToHex(getNProduct()));
        buffer.append(" (").append(getNProduct()).append(" )\n");

        buffer.append("    .lid                  = ");
        buffer.append(HexDump.intToHex(getLid()));
        buffer.append(" (").append(getLid()).append(" )\n");

        buffer.append("    .pnNext               = ");
        buffer.append(HexDump.intToHex(getPnNext()));
        buffer.append(" (").append(getPnNext()).append(" )\n");

        buffer.append("    .options              = ");
        buffer.append(HexDump.shortToHex(getOptions()));
        buffer.append(" (").append(getOptions()).append(" )\n");
        buffer.append("         .fDot                     = ").append(isFDot()).append('\n');
        buffer.append("         .fGlsy                    = ").append(isFGlsy()).append('\n');
        buffer.append("         .fComplex                 = ").append(isFComplex()).append('\n');
        buffer.append("         .fHasPic                  = ").append(isFHasPic()).append('\n');
        buffer.append("         .cQuickSaves              = ").append(getCQuickSaves()).append('\n');
        buffer.append("         .fEncrypted               = ").append(isFEncrypted()).append('\n');
        buffer.append("         .fWhichTblStm             = ").append(isFWhichTblStm()).append('\n');
        buffer.append("         .fReadOnlyRecommended     = ").append(isFReadOnlyRecommended()).append('\n');
        buffer.append("         .fWriteReservation        = ").append(isFWriteReservation()).append('\n');
        buffer.append("         .fExtChar                 = ").append(isFExtChar()).append('\n');
        buffer.append("         .fLoadOverride            = ").append(isFLoadOverride()).append('\n');
        buffer.append("         .fFarEast                 = ").append(isFFarEast()).append('\n');
        buffer.append("         .fCrypto                  = ").append(isFCrypto()).append('\n');

        buffer.append("    .nFibBack             = ");
        buffer.append(HexDump.intToHex(getNFibBack()));
        buffer.append(" (").append(getNFibBack()).append(" )\n");

        buffer.append("    .lKey                 = ");
        buffer.append(HexDump.intToHex(getLKey()));
        buffer.append(" (").append(getLKey()).append(" )\n");

        buffer.append("    .envr                 = ");
        buffer.append(HexDump.intToHex(getEnvr()));
        buffer.append(" (").append(getEnvr()).append(" )\n");

        buffer.append("    .history              = ");
        buffer.append(HexDump.shortToHex(getHistory()));
        buffer.append(" (").append(getHistory()).append(" )\n");
        buffer.append("         .fMac                     = ").append(isFMac()).append('\n');
        buffer.append("         .fEmptySpecial            = ").append(isFEmptySpecial()).append('\n');
        buffer.append("         .fLoadOverridePage        = ").append(isFLoadOverridePage()).append('\n');
        buffer.append("         .fFutureSavedUndo         = ").append(isFFutureSavedUndo()).append('\n');
        buffer.append("         .fWord97Saved             = ").append(isFWord97Saved()).append('\n');
        buffer.append("         .fSpare0                  = ").append(getFSpare0()).append('\n');

        buffer.append("    .chs                  = ");
        buffer.append(HexDump.intToHex(getChs()));
        buffer.append(" (").append(getChs()).append(" )\n");

        buffer.append("    .chsTables            = ");
        buffer.append(HexDump.intToHex(getChsTables()));
        buffer.append(" (").append(getChsTables()).append(" )\n");

        buffer.append("    .fcMin                = ");
        buffer.append(HexDump.intToHex(getFcMin()));
        buffer.append(" (").append(getFcMin()).append(" )\n");

        buffer.append("    .fcMac                = ");
        buffer.append(HexDump.intToHex(getFcMac()));
        buffer.append(" (").append(getFcMac()).append(" )\n");

        buffer.append("    .csw                  = ");
        buffer.append(HexDump.intToHex(getCsw()));
        buffer.append(" (").append(getCsw()).append(" )\n");

        buffer.append("    .wMagicCreated        = ");
        buffer.append(HexDump.intToHex(getWMagicCreated()));
        buffer.append(" (").append(getWMagicCreated()).append(" )\n");

        buffer.append("    .wMagicRevised        = ");
        buffer.append(HexDump.intToHex(getWMagicRevised()));
        buffer.append(" (").append(getWMagicRevised()).append(" )\n");

        buffer.append("    .wMagicCreatedPrivate = ");
        buffer.append(HexDump.intToHex(getWMagicCreatedPrivate()));
        buffer.append(" (").append(getWMagicCreatedPrivate()).append(" )\n");

        buffer.append("    .wMagicRevisedPrivate = ");
        buffer.append(HexDump.intToHex(getWMagicRevisedPrivate()));
        buffer.append(" (").append(getWMagicRevisedPrivate()).append(" )\n");

        buffer.append("    .pnFbpChpFirst_W6     = ");
        buffer.append(HexDump.intToHex(getPnFbpChpFirst_W6()));
        buffer.append(" (").append(getPnFbpChpFirst_W6()).append(" )\n");

        buffer.append("    .pnChpFirst_W6        = ");
        buffer.append(HexDump.intToHex(getPnChpFirst_W6()));
        buffer.append(" (").append(getPnChpFirst_W6()).append(" )\n");

        buffer.append("    .cpnBteChp_W6         = ");
        buffer.append(HexDump.intToHex(getCpnBteChp_W6()));
        buffer.append(" (").append(getCpnBteChp_W6()).append(" )\n");

        buffer.append("    .pnFbpPapFirst_W6     = ");
        buffer.append(HexDump.intToHex(getPnFbpPapFirst_W6()));
        buffer.append(" (").append(getPnFbpPapFirst_W6()).append(" )\n");

        buffer.append("    .pnPapFirst_W6        = ");
        buffer.append(HexDump.intToHex(getPnPapFirst_W6()));
        buffer.append(" (").append(getPnPapFirst_W6()).append(" )\n");

        buffer.append("    .cpnBtePap_W6         = ");
        buffer.append(HexDump.intToHex(getCpnBtePap_W6()));
        buffer.append(" (").append(getCpnBtePap_W6()).append(" )\n");

        buffer.append("    .pnFbpLvcFirst_W6     = ");
        buffer.append(HexDump.intToHex(getPnFbpLvcFirst_W6()));
        buffer.append(" (").append(getPnFbpLvcFirst_W6()).append(" )\n");

        buffer.append("    .pnLvcFirst_W6        = ");
        buffer.append(HexDump.intToHex(getPnLvcFirst_W6()));
        buffer.append(" (").append(getPnLvcFirst_W6()).append(" )\n");

        buffer.append("    .cpnBteLvc_W6         = ");
        buffer.append(HexDump.intToHex(getCpnBteLvc_W6()));
        buffer.append(" (").append(getCpnBteLvc_W6()).append(" )\n");

        buffer.append("    .lidFE                = ");
        buffer.append(HexDump.intToHex(getLidFE()));
        buffer.append(" (").append(getLidFE()).append(" )\n");

        buffer.append("    .clw                  = ");
        buffer.append(HexDump.intToHex(getClw()));
        buffer.append(" (").append(getClw()).append(" )\n");

        buffer.append("    .cbMac                = ");
        buffer.append(HexDump.intToHex(getCbMac()));
        buffer.append(" (").append(getCbMac()).append(" )\n");

        buffer.append("    .lProductCreated      = ");
        buffer.append(HexDump.intToHex(getLProductCreated()));
        buffer.append(" (").append(getLProductCreated()).append(" )\n");

        buffer.append("    .lProductRevised      = ");
        buffer.append(HexDump.intToHex(getLProductRevised()));
        buffer.append(" (").append(getLProductRevised()).append(" )\n");

        buffer.append("    .ccpText              = ");
        buffer.append(HexDump.intToHex(getCcpText()));
        buffer.append(" (").append(getCcpText()).append(" )\n");

        buffer.append("    .ccpFtn               = ");
        buffer.append(HexDump.intToHex(getCcpFtn()));
        buffer.append(" (").append(getCcpFtn()).append(" )\n");

        buffer.append("    .ccpHdd               = ");
        buffer.append(HexDump.intToHex(getCcpHdd()));
        buffer.append(" (").append(getCcpHdd()).append(" )\n");

        buffer.append("    .ccpMcr               = ");
        buffer.append(HexDump.intToHex(getCcpMcr()));
        buffer.append(" (").append(getCcpMcr()).append(" )\n");

        buffer.append("    .ccpAtn               = ");
        buffer.append(HexDump.intToHex(getCcpAtn()));
        buffer.append(" (").append(getCcpAtn()).append(" )\n");

        buffer.append("    .ccpEdn               = ");
        buffer.append(HexDump.intToHex(getCcpEdn()));
        buffer.append(" (").append(getCcpEdn()).append(" )\n");

        buffer.append("    .ccpTxbx              = ");
        buffer.append(HexDump.intToHex(getCcpTxbx()));
        buffer.append(" (").append(getCcpTxbx()).append(" )\n");

        buffer.append("    .ccpHdrTxbx           = ");
        buffer.append(HexDump.intToHex(getCcpHdrTxbx()));
        buffer.append(" (").append(getCcpHdrTxbx()).append(" )\n");

        buffer.append("    .pnFbpChpFirst        = ");
        buffer.append(HexDump.intToHex(getPnFbpChpFirst()));
        buffer.append(" (").append(getPnFbpChpFirst()).append(" )\n");

        buffer.append("    .pnChpFirst           = ");
        buffer.append(HexDump.intToHex(getPnChpFirst()));
        buffer.append(" (").append(getPnChpFirst()).append(" )\n");

        buffer.append("    .cpnBteChp            = ");
        buffer.append(HexDump.intToHex(getCpnBteChp()));
        buffer.append(" (").append(getCpnBteChp()).append(" )\n");

        buffer.append("    .pnFbpPapFirst        = ");
        buffer.append(HexDump.intToHex(getPnFbpPapFirst()));
        buffer.append(" (").append(getPnFbpPapFirst()).append(" )\n");

        buffer.append("    .pnPapFirst           = ");
        buffer.append(HexDump.intToHex(getPnPapFirst()));
        buffer.append(" (").append(getPnPapFirst()).append(" )\n");

        buffer.append("    .cpnBtePap            = ");
        buffer.append(HexDump.intToHex(getCpnBtePap()));
        buffer.append(" (").append(getCpnBtePap()).append(" )\n");

        buffer.append("    .pnFbpLvcFirst        = ");
        buffer.append(HexDump.intToHex(getPnFbpLvcFirst()));
        buffer.append(" (").append(getPnFbpLvcFirst()).append(" )\n");

        buffer.append("    .pnLvcFirst           = ");
        buffer.append(HexDump.intToHex(getPnLvcFirst()));
        buffer.append(" (").append(getPnLvcFirst()).append(" )\n");

        buffer.append("    .cpnBteLvc            = ");
        buffer.append(HexDump.intToHex(getCpnBteLvc()));
        buffer.append(" (").append(getCpnBteLvc()).append(" )\n");

        buffer.append("    .fcIslandFirst        = ");
        buffer.append(HexDump.intToHex(getFcIslandFirst()));
        buffer.append(" (").append(getFcIslandFirst()).append(" )\n");

        buffer.append("    .fcIslandLim          = ");
        buffer.append(HexDump.intToHex(getFcIslandLim()));
        buffer.append(" (").append(getFcIslandLim()).append(" )\n");

        buffer.append("    .cfclcb               = ");
        buffer.append(HexDump.intToHex(getCfclcb()));
        buffer.append(" (").append(getCfclcb()).append(" )\n");

        buffer.append("    .fcStshfOrig          = ");
        buffer.append(HexDump.intToHex(getFcStshfOrig()));
        buffer.append(" (").append(getFcStshfOrig()).append(" )\n");

        buffer.append("    .lcbStshfOrig         = ");
        buffer.append(HexDump.intToHex(getLcbStshfOrig()));
        buffer.append(" (").append(getLcbStshfOrig()).append(" )\n");

        buffer.append("    .fcStshf              = ");
        buffer.append(HexDump.intToHex(getFcStshf()));
        buffer.append(" (").append(getFcStshf()).append(" )\n");

        buffer.append("    .lcbStshf             = ");
        buffer.append(HexDump.intToHex(getLcbStshf()));
        buffer.append(" (").append(getLcbStshf()).append(" )\n");

        buffer.append("    .fcPlcffndRef         = ");
        buffer.append(HexDump.intToHex(getFcPlcffndRef()));
        buffer.append(" (").append(getFcPlcffndRef()).append(" )\n");

        buffer.append("    .lcbPlcffndRef        = ");
        buffer.append(HexDump.intToHex(getLcbPlcffndRef()));
        buffer.append(" (").append(getLcbPlcffndRef()).append(" )\n");

        buffer.append("    .fcPlcffndTxt         = ");
        buffer.append(HexDump.intToHex(getFcPlcffndTxt()));
        buffer.append(" (").append(getFcPlcffndTxt()).append(" )\n");

        buffer.append("    .lcbPlcffndTxt        = ");
        buffer.append(HexDump.intToHex(getLcbPlcffndTxt()));
        buffer.append(" (").append(getLcbPlcffndTxt()).append(" )\n");

        buffer.append("    .fcPlcfandRef         = ");
        buffer.append(HexDump.intToHex(getFcPlcfandRef()));
        buffer.append(" (").append(getFcPlcfandRef()).append(" )\n");

        buffer.append("    .lcbPlcfandRef        = ");
        buffer.append(HexDump.intToHex(getLcbPlcfandRef()));
        buffer.append(" (").append(getLcbPlcfandRef()).append(" )\n");

        buffer.append("    .fcPlcfandTxt         = ");
        buffer.append(HexDump.intToHex(getFcPlcfandTxt()));
        buffer.append(" (").append(getFcPlcfandTxt()).append(" )\n");

        buffer.append("    .lcbPlcfandTxt        = ");
        buffer.append(HexDump.intToHex(getLcbPlcfandTxt()));
        buffer.append(" (").append(getLcbPlcfandTxt()).append(" )\n");

        buffer.append("    .fcPlcfsed            = ");
        buffer.append(HexDump.intToHex(getFcPlcfsed()));
        buffer.append(" (").append(getFcPlcfsed()).append(" )\n");

        buffer.append("    .lcbPlcfsed           = ");
        buffer.append(HexDump.intToHex(getLcbPlcfsed()));
        buffer.append(" (").append(getLcbPlcfsed()).append(" )\n");

        buffer.append("    .fcPlcpad             = ");
        buffer.append(HexDump.intToHex(getFcPlcpad()));
        buffer.append(" (").append(getFcPlcpad()).append(" )\n");

        buffer.append("    .lcbPlcpad            = ");
        buffer.append(HexDump.intToHex(getLcbPlcpad()));
        buffer.append(" (").append(getLcbPlcpad()).append(" )\n");

        buffer.append("    .fcPlcfphe            = ");
        buffer.append(HexDump.intToHex(getFcPlcfphe()));
        buffer.append(" (").append(getFcPlcfphe()).append(" )\n");

        buffer.append("    .lcbPlcfphe           = ");
        buffer.append(HexDump.intToHex(getLcbPlcfphe()));
        buffer.append(" (").append(getLcbPlcfphe()).append(" )\n");

        buffer.append("    .fcSttbfglsy          = ");
        buffer.append(HexDump.intToHex(getFcSttbfglsy()));
        buffer.append(" (").append(getFcSttbfglsy()).append(" )\n");

        buffer.append("    .lcbSttbfglsy         = ");
        buffer.append(HexDump.intToHex(getLcbSttbfglsy()));
        buffer.append(" (").append(getLcbSttbfglsy()).append(" )\n");

        buffer.append("    .fcPlcfglsy           = ");
        buffer.append(HexDump.intToHex(getFcPlcfglsy()));
        buffer.append(" (").append(getFcPlcfglsy()).append(" )\n");

        buffer.append("    .lcbPlcfglsy          = ");
        buffer.append(HexDump.intToHex(getLcbPlcfglsy()));
        buffer.append(" (").append(getLcbPlcfglsy()).append(" )\n");

        buffer.append("    .fcPlcfhdd            = ");
        buffer.append(HexDump.intToHex(getFcPlcfhdd()));
        buffer.append(" (").append(getFcPlcfhdd()).append(" )\n");

        buffer.append("    .lcbPlcfhdd           = ");
        buffer.append(HexDump.intToHex(getLcbPlcfhdd()));
        buffer.append(" (").append(getLcbPlcfhdd()).append(" )\n");

        buffer.append("    .fcPlcfbteChpx        = ");
        buffer.append(HexDump.intToHex(getFcPlcfbteChpx()));
        buffer.append(" (").append(getFcPlcfbteChpx()).append(" )\n");

        buffer.append("    .lcbPlcfbteChpx       = ");
        buffer.append(HexDump.intToHex(getLcbPlcfbteChpx()));
        buffer.append(" (").append(getLcbPlcfbteChpx()).append(" )\n");

        buffer.append("    .fcPlcfbtePapx        = ");
        buffer.append(HexDump.intToHex(getFcPlcfbtePapx()));
        buffer.append(" (").append(getFcPlcfbtePapx()).append(" )\n");

        buffer.append("    .lcbPlcfbtePapx       = ");
        buffer.append(HexDump.intToHex(getLcbPlcfbtePapx()));
        buffer.append(" (").append(getLcbPlcfbtePapx()).append(" )\n");

        buffer.append("    .fcPlcfsea            = ");
        buffer.append(HexDump.intToHex(getFcPlcfsea()));
        buffer.append(" (").append(getFcPlcfsea()).append(" )\n");

        buffer.append("    .lcbPlcfsea           = ");
        buffer.append(HexDump.intToHex(getLcbPlcfsea()));
        buffer.append(" (").append(getLcbPlcfsea()).append(" )\n");

        buffer.append("    .fcSttbfffn           = ");
        buffer.append(HexDump.intToHex(getFcSttbfffn()));
        buffer.append(" (").append(getFcSttbfffn()).append(" )\n");

        buffer.append("    .lcbSttbfffn          = ");
        buffer.append(HexDump.intToHex(getLcbSttbfffn()));
        buffer.append(" (").append(getLcbSttbfffn()).append(" )\n");

        buffer.append("    .fcPlcffldMom         = ");
        buffer.append(HexDump.intToHex(getFcPlcffldMom()));
        buffer.append(" (").append(getFcPlcffldMom()).append(" )\n");

        buffer.append("    .lcbPlcffldMom        = ");
        buffer.append(HexDump.intToHex(getLcbPlcffldMom()));
        buffer.append(" (").append(getLcbPlcffldMom()).append(" )\n");

        buffer.append("    .fcPlcffldHdr         = ");
        buffer.append(HexDump.intToHex(getFcPlcffldHdr()));
        buffer.append(" (").append(getFcPlcffldHdr()).append(" )\n");

        buffer.append("    .lcbPlcffldHdr        = ");
        buffer.append(HexDump.intToHex(getLcbPlcffldHdr()));
        buffer.append(" (").append(getLcbPlcffldHdr()).append(" )\n");

        buffer.append("    .fcPlcffldFtn         = ");
        buffer.append(HexDump.intToHex(getFcPlcffldFtn()));
        buffer.append(" (").append(getFcPlcffldFtn()).append(" )\n");

        buffer.append("    .lcbPlcffldFtn        = ");
        buffer.append(HexDump.intToHex(getLcbPlcffldFtn()));
        buffer.append(" (").append(getLcbPlcffldFtn()).append(" )\n");

        buffer.append("    .fcPlcffldAtn         = ");
        buffer.append(HexDump.intToHex(getFcPlcffldAtn()));
        buffer.append(" (").append(getFcPlcffldAtn()).append(" )\n");

        buffer.append("    .lcbPlcffldAtn        = ");
        buffer.append(HexDump.intToHex(getLcbPlcffldAtn()));
        buffer.append(" (").append(getLcbPlcffldAtn()).append(" )\n");

        buffer.append("    .fcPlcffldMcr         = ");
        buffer.append(HexDump.intToHex(getFcPlcffldMcr()));
        buffer.append(" (").append(getFcPlcffldMcr()).append(" )\n");

        buffer.append("    .lcbPlcffldMcr        = ");
        buffer.append(HexDump.intToHex(getLcbPlcffldMcr()));
        buffer.append(" (").append(getLcbPlcffldMcr()).append(" )\n");

        buffer.append("    .fcSttbfbkmk          = ");
        buffer.append(HexDump.intToHex(getFcSttbfbkmk()));
        buffer.append(" (").append(getFcSttbfbkmk()).append(" )\n");

        buffer.append("    .lcbSttbfbkmk         = ");
        buffer.append(HexDump.intToHex(getLcbSttbfbkmk()));
        buffer.append(" (").append(getLcbSttbfbkmk()).append(" )\n");

        buffer.append("    .fcPlcfbkf            = ");
        buffer.append(HexDump.intToHex(getFcPlcfbkf()));
        buffer.append(" (").append(getFcPlcfbkf()).append(" )\n");

        buffer.append("    .lcbPlcfbkf           = ");
        buffer.append(HexDump.intToHex(getLcbPlcfbkf()));
        buffer.append(" (").append(getLcbPlcfbkf()).append(" )\n");

        buffer.append("    .fcPlcfbkl            = ");
        buffer.append(HexDump.intToHex(getFcPlcfbkl()));
        buffer.append(" (").append(getFcPlcfbkl()).append(" )\n");

        buffer.append("    .lcbPlcfbkl           = ");
        buffer.append(HexDump.intToHex(getLcbPlcfbkl()));
        buffer.append(" (").append(getLcbPlcfbkl()).append(" )\n");

        buffer.append("    .fcCmds               = ");
        buffer.append(HexDump.intToHex(getFcCmds()));
        buffer.append(" (").append(getFcCmds()).append(" )\n");

        buffer.append("    .lcbCmds              = ");
        buffer.append(HexDump.intToHex(getLcbCmds()));
        buffer.append(" (").append(getLcbCmds()).append(" )\n");

        buffer.append("    .fcPlcmcr             = ");
        buffer.append(HexDump.intToHex(getFcPlcmcr()));
        buffer.append(" (").append(getFcPlcmcr()).append(" )\n");

        buffer.append("    .lcbPlcmcr            = ");
        buffer.append(HexDump.intToHex(getLcbPlcmcr()));
        buffer.append(" (").append(getLcbPlcmcr()).append(" )\n");

        buffer.append("    .fcSttbfmcr           = ");
        buffer.append(HexDump.intToHex(getFcSttbfmcr()));
        buffer.append(" (").append(getFcSttbfmcr()).append(" )\n");

        buffer.append("    .lcbSttbfmcr          = ");
        buffer.append(HexDump.intToHex(getLcbSttbfmcr()));
        buffer.append(" (").append(getLcbSttbfmcr()).append(" )\n");

        buffer.append("    .fcPrDrvr             = ");
        buffer.append(HexDump.intToHex(getFcPrDrvr()));
        buffer.append(" (").append(getFcPrDrvr()).append(" )\n");

        buffer.append("    .lcbPrDrvr            = ");
        buffer.append(HexDump.intToHex(getLcbPrDrvr()));
        buffer.append(" (").append(getLcbPrDrvr()).append(" )\n");

        buffer.append("    .fcPrEnvPort          = ");
        buffer.append(HexDump.intToHex(getFcPrEnvPort()));
        buffer.append(" (").append(getFcPrEnvPort()).append(" )\n");

        buffer.append("    .lcbPrEnvPort         = ");
        buffer.append(HexDump.intToHex(getLcbPrEnvPort()));
        buffer.append(" (").append(getLcbPrEnvPort()).append(" )\n");

        buffer.append("    .fcPrEnvLand          = ");
        buffer.append(HexDump.intToHex(getFcPrEnvLand()));
        buffer.append(" (").append(getFcPrEnvLand()).append(" )\n");

        buffer.append("    .lcbPrEnvLand         = ");
        buffer.append(HexDump.intToHex(getLcbPrEnvLand()));
        buffer.append(" (").append(getLcbPrEnvLand()).append(" )\n");

        buffer.append("    .fcWss                = ");
        buffer.append(HexDump.intToHex(getFcWss()));
        buffer.append(" (").append(getFcWss()).append(" )\n");

        buffer.append("    .lcbWss               = ");
        buffer.append(HexDump.intToHex(getLcbWss()));
        buffer.append(" (").append(getLcbWss()).append(" )\n");

        buffer.append("    .fcDop                = ");
        buffer.append(HexDump.intToHex(getFcDop()));
        buffer.append(" (").append(getFcDop()).append(" )\n");

        buffer.append("    .lcbDop               = ");
        buffer.append(HexDump.intToHex(getLcbDop()));
        buffer.append(" (").append(getLcbDop()).append(" )\n");

        buffer.append("    .fcSttbfAssoc         = ");
        buffer.append(HexDump.intToHex(getFcSttbfAssoc()));
        buffer.append(" (").append(getFcSttbfAssoc()).append(" )\n");

        buffer.append("    .lcbSttbfAssoc        = ");
        buffer.append(HexDump.intToHex(getLcbSttbfAssoc()));
        buffer.append(" (").append(getLcbSttbfAssoc()).append(" )\n");

        buffer.append("    .fcClx                = ");
        buffer.append(HexDump.intToHex(getFcClx()));
        buffer.append(" (").append(getFcClx()).append(" )\n");

        buffer.append("    .lcbClx               = ");
        buffer.append(HexDump.intToHex(getLcbClx()));
        buffer.append(" (").append(getLcbClx()).append(" )\n");

        buffer.append("    .fcPlcfpgdFtn         = ");
        buffer.append(HexDump.intToHex(getFcPlcfpgdFtn()));
        buffer.append(" (").append(getFcPlcfpgdFtn()).append(" )\n");

        buffer.append("    .lcbPlcfpgdFtn        = ");
        buffer.append(HexDump.intToHex(getLcbPlcfpgdFtn()));
        buffer.append(" (").append(getLcbPlcfpgdFtn()).append(" )\n");

        buffer.append("    .fcAutosaveSource     = ");
        buffer.append(HexDump.intToHex(getFcAutosaveSource()));
        buffer.append(" (").append(getFcAutosaveSource()).append(" )\n");

        buffer.append("    .lcbAutosaveSource    = ");
        buffer.append(HexDump.intToHex(getLcbAutosaveSource()));
        buffer.append(" (").append(getLcbAutosaveSource()).append(" )\n");

        buffer.append("    .fcGrpXstAtnOwners    = ");
        buffer.append(HexDump.intToHex(getFcGrpXstAtnOwners()));
        buffer.append(" (").append(getFcGrpXstAtnOwners()).append(" )\n");

        buffer.append("    .lcbGrpXstAtnOwners   = ");
        buffer.append(HexDump.intToHex(getLcbGrpXstAtnOwners()));
        buffer.append(" (").append(getLcbGrpXstAtnOwners()).append(" )\n");

        buffer.append("    .fcSttbfAtnbkmk       = ");
        buffer.append(HexDump.intToHex(getFcSttbfAtnbkmk()));
        buffer.append(" (").append(getFcSttbfAtnbkmk()).append(" )\n");

        buffer.append("    .lcbSttbfAtnbkmk      = ");
        buffer.append(HexDump.intToHex(getLcbSttbfAtnbkmk()));
        buffer.append(" (").append(getLcbSttbfAtnbkmk()).append(" )\n");

        buffer.append("    .fcPlcdoaMom          = ");
        buffer.append(HexDump.intToHex(getFcPlcdoaMom()));
        buffer.append(" (").append(getFcPlcdoaMom()).append(" )\n");

        buffer.append("    .lcbPlcdoaMom         = ");
        buffer.append(HexDump.intToHex(getLcbPlcdoaMom()));
        buffer.append(" (").append(getLcbPlcdoaMom()).append(" )\n");

        buffer.append("    .fcPlcdoaHdr          = ");
        buffer.append(HexDump.intToHex(getFcPlcdoaHdr()));
        buffer.append(" (").append(getFcPlcdoaHdr()).append(" )\n");

        buffer.append("    .lcbPlcdoaHdr         = ");
        buffer.append(HexDump.intToHex(getLcbPlcdoaHdr()));
        buffer.append(" (").append(getLcbPlcdoaHdr()).append(" )\n");

        buffer.append("    .fcPlcspaMom          = ");
        buffer.append(HexDump.intToHex(getFcPlcspaMom()));
        buffer.append(" (").append(getFcPlcspaMom()).append(" )\n");

        buffer.append("    .lcbPlcspaMom         = ");
        buffer.append(HexDump.intToHex(getLcbPlcspaMom()));
        buffer.append(" (").append(getLcbPlcspaMom()).append(" )\n");

        buffer.append("    .fcPlcspaHdr          = ");
        buffer.append(HexDump.intToHex(getFcPlcspaHdr()));
        buffer.append(" (").append(getFcPlcspaHdr()).append(" )\n");

        buffer.append("    .lcbPlcspaHdr         = ");
        buffer.append(HexDump.intToHex(getLcbPlcspaHdr()));
        buffer.append(" (").append(getLcbPlcspaHdr()).append(" )\n");

        buffer.append("    .fcPlcfAtnbkf         = ");
        buffer.append(HexDump.intToHex(getFcPlcfAtnbkf()));
        buffer.append(" (").append(getFcPlcfAtnbkf()).append(" )\n");

        buffer.append("    .lcbPlcfAtnbkf        = ");
        buffer.append(HexDump.intToHex(getLcbPlcfAtnbkf()));
        buffer.append(" (").append(getLcbPlcfAtnbkf()).append(" )\n");

        buffer.append("    .fcPlcfAtnbkl         = ");
        buffer.append(HexDump.intToHex(getFcPlcfAtnbkl()));
        buffer.append(" (").append(getFcPlcfAtnbkl()).append(" )\n");

        buffer.append("    .lcbPlcfAtnbkl        = ");
        buffer.append(HexDump.intToHex(getLcbPlcfAtnbkl()));
        buffer.append(" (").append(getLcbPlcfAtnbkl()).append(" )\n");

        buffer.append("    .fcPms                = ");
        buffer.append(HexDump.intToHex(getFcPms()));
        buffer.append(" (").append(getFcPms()).append(" )\n");

        buffer.append("    .lcbPms               = ");
        buffer.append(HexDump.intToHex(getLcbPms()));
        buffer.append(" (").append(getLcbPms()).append(" )\n");

        buffer.append("    .fcFormFldSttbs       = ");
        buffer.append(HexDump.intToHex(getFcFormFldSttbs()));
        buffer.append(" (").append(getFcFormFldSttbs()).append(" )\n");

        buffer.append("    .lcbFormFldSttbs      = ");
        buffer.append(HexDump.intToHex(getLcbFormFldSttbs()));
        buffer.append(" (").append(getLcbFormFldSttbs()).append(" )\n");

        buffer.append("    .fcPlcfendRef         = ");
        buffer.append(HexDump.intToHex(getFcPlcfendRef()));
        buffer.append(" (").append(getFcPlcfendRef()).append(" )\n");

        buffer.append("    .lcbPlcfendRef        = ");
        buffer.append(HexDump.intToHex(getLcbPlcfendRef()));
        buffer.append(" (").append(getLcbPlcfendRef()).append(" )\n");

        buffer.append("    .fcPlcfendTxt         = ");
        buffer.append(HexDump.intToHex(getFcPlcfendTxt()));
        buffer.append(" (").append(getFcPlcfendTxt()).append(" )\n");

        buffer.append("    .lcbPlcfendTxt        = ");
        buffer.append(HexDump.intToHex(getLcbPlcfendTxt()));
        buffer.append(" (").append(getLcbPlcfendTxt()).append(" )\n");

        buffer.append("    .fcPlcffldEdn         = ");
        buffer.append(HexDump.intToHex(getFcPlcffldEdn()));
        buffer.append(" (").append(getFcPlcffldEdn()).append(" )\n");

        buffer.append("    .lcbPlcffldEdn        = ");
        buffer.append(HexDump.intToHex(getLcbPlcffldEdn()));
        buffer.append(" (").append(getLcbPlcffldEdn()).append(" )\n");

        buffer.append("    .fcPlcfpgdEdn         = ");
        buffer.append(HexDump.intToHex(getFcPlcfpgdEdn()));
        buffer.append(" (").append(getFcPlcfpgdEdn()).append(" )\n");

        buffer.append("    .lcbPlcfpgdEdn        = ");
        buffer.append(HexDump.intToHex(getLcbPlcfpgdEdn()));
        buffer.append(" (").append(getLcbPlcfpgdEdn()).append(" )\n");

        buffer.append("    .fcDggInfo            = ");
        buffer.append(HexDump.intToHex(getFcDggInfo()));
        buffer.append(" (").append(getFcDggInfo()).append(" )\n");

        buffer.append("    .lcbDggInfo           = ");
        buffer.append(HexDump.intToHex(getLcbDggInfo()));
        buffer.append(" (").append(getLcbDggInfo()).append(" )\n");

        buffer.append("    .fcSttbfRMark         = ");
        buffer.append(HexDump.intToHex(getFcSttbfRMark()));
        buffer.append(" (").append(getFcSttbfRMark()).append(" )\n");

        buffer.append("    .lcbSttbfRMark        = ");
        buffer.append(HexDump.intToHex(getLcbSttbfRMark()));
        buffer.append(" (").append(getLcbSttbfRMark()).append(" )\n");

        buffer.append("    .fcSttbCaption        = ");
        buffer.append(HexDump.intToHex(getFcSttbCaption()));
        buffer.append(" (").append(getFcSttbCaption()).append(" )\n");

        buffer.append("    .lcbSttbCaption       = ");
        buffer.append(HexDump.intToHex(getLcbSttbCaption()));
        buffer.append(" (").append(getLcbSttbCaption()).append(" )\n");

        buffer.append("    .fcSttbAutoCaption    = ");
        buffer.append(HexDump.intToHex(getFcSttbAutoCaption()));
        buffer.append(" (").append(getFcSttbAutoCaption()).append(" )\n");

        buffer.append("    .lcbSttbAutoCaption   = ");
        buffer.append(HexDump.intToHex(getLcbSttbAutoCaption()));
        buffer.append(" (").append(getLcbSttbAutoCaption()).append(" )\n");

        buffer.append("    .fcPlcfwkb            = ");
        buffer.append(HexDump.intToHex(getFcPlcfwkb()));
        buffer.append(" (").append(getFcPlcfwkb()).append(" )\n");

        buffer.append("    .lcbPlcfwkb           = ");
        buffer.append(HexDump.intToHex(getLcbPlcfwkb()));
        buffer.append(" (").append(getLcbPlcfwkb()).append(" )\n");

        buffer.append("    .fcPlcfspl            = ");
        buffer.append(HexDump.intToHex(getFcPlcfspl()));
        buffer.append(" (").append(getFcPlcfspl()).append(" )\n");

        buffer.append("    .lcbPlcfspl           = ");
        buffer.append(HexDump.intToHex(getLcbPlcfspl()));
        buffer.append(" (").append(getLcbPlcfspl()).append(" )\n");

        buffer.append("    .fcPlcftxbxTxt        = ");
        buffer.append(HexDump.intToHex(getFcPlcftxbxTxt()));
        buffer.append(" (").append(getFcPlcftxbxTxt()).append(" )\n");

        buffer.append("    .lcbPlcftxbxTxt       = ");
        buffer.append(HexDump.intToHex(getLcbPlcftxbxTxt()));
        buffer.append(" (").append(getLcbPlcftxbxTxt()).append(" )\n");

        buffer.append("    .fcPlcffldTxbx        = ");
        buffer.append(HexDump.intToHex(getFcPlcffldTxbx()));
        buffer.append(" (").append(getFcPlcffldTxbx()).append(" )\n");

        buffer.append("    .lcbPlcffldTxbx       = ");
        buffer.append(HexDump.intToHex(getLcbPlcffldTxbx()));
        buffer.append(" (").append(getLcbPlcffldTxbx()).append(" )\n");

        buffer.append("    .fcPlcfhdrtxbxTxt     = ");
        buffer.append(HexDump.intToHex(getFcPlcfhdrtxbxTxt()));
        buffer.append(" (").append(getFcPlcfhdrtxbxTxt()).append(" )\n");

        buffer.append("    .lcbPlcfhdrtxbxTxt    = ");
        buffer.append(HexDump.intToHex(getLcbPlcfhdrtxbxTxt()));
        buffer.append(" (").append(getLcbPlcfhdrtxbxTxt()).append(" )\n");

        buffer.append("    .fcPlcffldHdrTxbx     = ");
        buffer.append(HexDump.intToHex(getFcPlcffldHdrTxbx()));
        buffer.append(" (").append(getFcPlcffldHdrTxbx()).append(" )\n");

        buffer.append("    .lcbPlcffldHdrTxbx    = ");
        buffer.append(HexDump.intToHex(getLcbPlcffldHdrTxbx()));
        buffer.append(" (").append(getLcbPlcffldHdrTxbx()).append(" )\n");

        buffer.append("    .fcStwUser            = ");
        buffer.append(HexDump.intToHex(getFcStwUser()));
        buffer.append(" (").append(getFcStwUser()).append(" )\n");

        buffer.append("    .lcbStwUser           = ");
        buffer.append(HexDump.intToHex(getLcbStwUser()));
        buffer.append(" (").append(getLcbStwUser()).append(" )\n");

        buffer.append("    .fcSttbttmbd          = ");
        buffer.append(HexDump.intToHex(getFcSttbttmbd()));
        buffer.append(" (").append(getFcSttbttmbd()).append(" )\n");

        buffer.append("    .cbSttbttmbd          = ");
        buffer.append(HexDump.intToHex(getCbSttbttmbd()));
        buffer.append(" (").append(getCbSttbttmbd()).append(" )\n");

        buffer.append("    .fcUnused             = ");
        buffer.append(HexDump.intToHex(getFcUnused()));
        buffer.append(" (").append(getFcUnused()).append(" )\n");

        buffer.append("    .lcbUnused            = ");
        buffer.append(HexDump.intToHex(getLcbUnused()));
        buffer.append(" (").append(getLcbUnused()).append(" )\n");

        buffer.append("    .fcPgdMother          = ");
        buffer.append(HexDump.intToHex(getFcPgdMother()));
        buffer.append(" (").append(getFcPgdMother()).append(" )\n");

        buffer.append("    .lcbPgdMother         = ");
        buffer.append(HexDump.intToHex(getLcbPgdMother()));
        buffer.append(" (").append(getLcbPgdMother()).append(" )\n");

        buffer.append("    .fcBkdMother          = ");
        buffer.append(HexDump.intToHex(getFcBkdMother()));
        buffer.append(" (").append(getFcBkdMother()).append(" )\n");

        buffer.append("    .lcbBkdMother         = ");
        buffer.append(HexDump.intToHex(getLcbBkdMother()));
        buffer.append(" (").append(getLcbBkdMother()).append(" )\n");

        buffer.append("    .fcPgdFtn             = ");
        buffer.append(HexDump.intToHex(getFcPgdFtn()));
        buffer.append(" (").append(getFcPgdFtn()).append(" )\n");

        buffer.append("    .lcbPgdFtn            = ");
        buffer.append(HexDump.intToHex(getLcbPgdFtn()));
        buffer.append(" (").append(getLcbPgdFtn()).append(" )\n");

        buffer.append("    .fcBkdFtn             = ");
        buffer.append(HexDump.intToHex(getFcBkdFtn()));
        buffer.append(" (").append(getFcBkdFtn()).append(" )\n");

        buffer.append("    .lcbBkdFtn            = ");
        buffer.append(HexDump.intToHex(getLcbBkdFtn()));
        buffer.append(" (").append(getLcbBkdFtn()).append(" )\n");

        buffer.append("    .fcPgdEdn             = ");
        buffer.append(HexDump.intToHex(getFcPgdEdn()));
        buffer.append(" (").append(getFcPgdEdn()).append(" )\n");

        buffer.append("    .lcbPgdEdn            = ");
        buffer.append(HexDump.intToHex(getLcbPgdEdn()));
        buffer.append(" (").append(getLcbPgdEdn()).append(" )\n");

        buffer.append("    .fcBkdEdn             = ");
        buffer.append(HexDump.intToHex(getFcBkdEdn()));
        buffer.append(" (").append(getFcBkdEdn()).append(" )\n");

        buffer.append("    .lcbBkdEdn            = ");
        buffer.append(HexDump.intToHex(getLcbBkdEdn()));
        buffer.append(" (").append(getLcbBkdEdn()).append(" )\n");

        buffer.append("    .fcSttbfIntlFld       = ");
        buffer.append(HexDump.intToHex(getFcSttbfIntlFld()));
        buffer.append(" (").append(getFcSttbfIntlFld()).append(" )\n");

        buffer.append("    .lcbSttbfIntlFld      = ");
        buffer.append(HexDump.intToHex(getLcbSttbfIntlFld()));
        buffer.append(" (").append(getLcbSttbfIntlFld()).append(" )\n");

        buffer.append("    .fcRouteSlip          = ");
        buffer.append(HexDump.intToHex(getFcRouteSlip()));
        buffer.append(" (").append(getFcRouteSlip()).append(" )\n");

        buffer.append("    .lcbRouteSlip         = ");
        buffer.append(HexDump.intToHex(getLcbRouteSlip()));
        buffer.append(" (").append(getLcbRouteSlip()).append(" )\n");

        buffer.append("    .fcSttbSavedBy        = ");
        buffer.append(HexDump.intToHex(getFcSttbSavedBy()));
        buffer.append(" (").append(getFcSttbSavedBy()).append(" )\n");

        buffer.append("    .lcbSttbSavedBy       = ");
        buffer.append(HexDump.intToHex(getLcbSttbSavedBy()));
        buffer.append(" (").append(getLcbSttbSavedBy()).append(" )\n");

        buffer.append("    .fcSttbFnm            = ");
        buffer.append(HexDump.intToHex(getFcSttbFnm()));
        buffer.append(" (").append(getFcSttbFnm()).append(" )\n");

        buffer.append("    .lcbSttbFnm           = ");
        buffer.append(HexDump.intToHex(getLcbSttbFnm()));
        buffer.append(" (").append(getLcbSttbFnm()).append(" )\n");

        buffer.append("    .fcPlcfLst            = ");
        buffer.append(HexDump.intToHex(getFcPlcfLst()));
        buffer.append(" (").append(getFcPlcfLst()).append(" )\n");

        buffer.append("    .lcbPlcfLst           = ");
        buffer.append(HexDump.intToHex(getLcbPlcfLst()));
        buffer.append(" (").append(getLcbPlcfLst()).append(" )\n");

        buffer.append("    .fcPlfLfo             = ");
        buffer.append(HexDump.intToHex(getFcPlfLfo()));
        buffer.append(" (").append(getFcPlfLfo()).append(" )\n");

        buffer.append("    .lcbPlfLfo            = ");
        buffer.append(HexDump.intToHex(getLcbPlfLfo()));
        buffer.append(" (").append(getLcbPlfLfo()).append(" )\n");

        buffer.append("    .fcPlcftxbxBkd        = ");
        buffer.append(HexDump.intToHex(getFcPlcftxbxBkd()));
        buffer.append(" (").append(getFcPlcftxbxBkd()).append(" )\n");

        buffer.append("    .lcbPlcftxbxBkd       = ");
        buffer.append(HexDump.intToHex(getLcbPlcftxbxBkd()));
        buffer.append(" (").append(getLcbPlcftxbxBkd()).append(" )\n");

        buffer.append("    .fcPlcftxbxHdrBkd     = ");
        buffer.append(HexDump.intToHex(getFcPlcftxbxHdrBkd()));
        buffer.append(" (").append(getFcPlcftxbxHdrBkd()).append(" )\n");

        buffer.append("    .lcbPlcftxbxHdrBkd    = ");
        buffer.append(HexDump.intToHex(getLcbPlcftxbxHdrBkd()));
        buffer.append(" (").append(getLcbPlcftxbxHdrBkd()).append(" )\n");

        buffer.append("    .fcDocUndo            = ");
        buffer.append(HexDump.intToHex(getFcDocUndo()));
        buffer.append(" (").append(getFcDocUndo()).append(" )\n");

        buffer.append("    .lcbDocUndo           = ");
        buffer.append(HexDump.intToHex(getLcbDocUndo()));
        buffer.append(" (").append(getLcbDocUndo()).append(" )\n");

        buffer.append("    .fcRgbuse             = ");
        buffer.append(HexDump.intToHex(getFcRgbuse()));
        buffer.append(" (").append(getFcRgbuse()).append(" )\n");

        buffer.append("    .lcbRgbuse            = ");
        buffer.append(HexDump.intToHex(getLcbRgbuse()));
        buffer.append(" (").append(getLcbRgbuse()).append(" )\n");

        buffer.append("    .fcUsp                = ");
        buffer.append(HexDump.intToHex(getFcUsp()));
        buffer.append(" (").append(getFcUsp()).append(" )\n");

        buffer.append("    .lcbUsp               = ");
        buffer.append(HexDump.intToHex(getLcbUsp()));
        buffer.append(" (").append(getLcbUsp()).append(" )\n");

        buffer.append("    .fcUskf               = ");
        buffer.append(HexDump.intToHex(getFcUskf()));
        buffer.append(" (").append(getFcUskf()).append(" )\n");

        buffer.append("    .lcbUskf              = ");
        buffer.append(HexDump.intToHex(getLcbUskf()));
        buffer.append(" (").append(getLcbUskf()).append(" )\n");

        buffer.append("    .fcPlcupcRgbuse       = ");
        buffer.append(HexDump.intToHex(getFcPlcupcRgbuse()));
        buffer.append(" (").append(getFcPlcupcRgbuse()).append(" )\n");

        buffer.append("    .lcbPlcupcRgbuse      = ");
        buffer.append(HexDump.intToHex(getLcbPlcupcRgbuse()));
        buffer.append(" (").append(getLcbPlcupcRgbuse()).append(" )\n");

        buffer.append("    .fcPlcupcUsp          = ");
        buffer.append(HexDump.intToHex(getFcPlcupcUsp()));
        buffer.append(" (").append(getFcPlcupcUsp()).append(" )\n");

        buffer.append("    .lcbPlcupcUsp         = ");
        buffer.append(HexDump.intToHex(getLcbPlcupcUsp()));
        buffer.append(" (").append(getLcbPlcupcUsp()).append(" )\n");

        buffer.append("    .fcSttbGlsyStyle      = ");
        buffer.append(HexDump.intToHex(getFcSttbGlsyStyle()));
        buffer.append(" (").append(getFcSttbGlsyStyle()).append(" )\n");

        buffer.append("    .lcbSttbGlsyStyle     = ");
        buffer.append(HexDump.intToHex(getLcbSttbGlsyStyle()));
        buffer.append(" (").append(getLcbSttbGlsyStyle()).append(" )\n");

        buffer.append("    .fcPlgosl             = ");
        buffer.append(HexDump.intToHex(getFcPlgosl()));
        buffer.append(" (").append(getFcPlgosl()).append(" )\n");

        buffer.append("    .lcbPlgosl            = ");
        buffer.append(HexDump.intToHex(getLcbPlgosl()));
        buffer.append(" (").append(getLcbPlgosl()).append(" )\n");

        buffer.append("    .fcPlcocx             = ");
        buffer.append(HexDump.intToHex(getFcPlcocx()));
        buffer.append(" (").append(getFcPlcocx()).append(" )\n");

        buffer.append("    .lcbPlcocx            = ");
        buffer.append(HexDump.intToHex(getLcbPlcocx()));
        buffer.append(" (").append(getLcbPlcocx()).append(" )\n");

        buffer.append("    .fcPlcfbteLvc         = ");
        buffer.append(HexDump.intToHex(getFcPlcfbteLvc()));
        buffer.append(" (").append(getFcPlcfbteLvc()).append(" )\n");

        buffer.append("    .lcbPlcfbteLvc        = ");
        buffer.append(HexDump.intToHex(getLcbPlcfbteLvc()));
        buffer.append(" (").append(getLcbPlcfbteLvc()).append(" )\n");

        buffer.append("    .dwLowDateTime        = ");
        buffer.append(HexDump.intToHex(getDwLowDateTime()));
        buffer.append(" (").append(getDwLowDateTime()).append(" )\n");

        buffer.append("    .dwHighDateTime       = ");
        buffer.append(HexDump.intToHex(getDwHighDateTime()));
        buffer.append(" (").append(getDwHighDateTime()).append(" )\n");

        buffer.append("    .fcPlcflvc            = ");
        buffer.append(HexDump.intToHex(getFcPlcflvc()));
        buffer.append(" (").append(getFcPlcflvc()).append(" )\n");

        buffer.append("    .lcbPlcflvc           = ");
        buffer.append(HexDump.intToHex(getLcbPlcflvc()));
        buffer.append(" (").append(getLcbPlcflvc()).append(" )\n");

        buffer.append("    .fcPlcasumy           = ");
        buffer.append(HexDump.intToHex(getFcPlcasumy()));
        buffer.append(" (").append(getFcPlcasumy()).append(" )\n");

        buffer.append("    .lcbPlcasumy          = ");
        buffer.append(HexDump.intToHex(getLcbPlcasumy()));
        buffer.append(" (").append(getLcbPlcasumy()).append(" )\n");

        buffer.append("    .fcPlcfgram           = ");
        buffer.append(HexDump.intToHex(getFcPlcfgram()));
        buffer.append(" (").append(getFcPlcfgram()).append(" )\n");

        buffer.append("    .lcbPlcfgram          = ");
        buffer.append(HexDump.intToHex(getLcbPlcfgram()));
        buffer.append(" (").append(getLcbPlcfgram()).append(" )\n");

        buffer.append("    .fcSttbListNames      = ");
        buffer.append(HexDump.intToHex(getFcSttbListNames()));
        buffer.append(" (").append(getFcSttbListNames()).append(" )\n");

        buffer.append("    .lcbSttbListNames     = ");
        buffer.append(HexDump.intToHex(getLcbSttbListNames()));
        buffer.append(" (").append(getLcbSttbListNames()).append(" )\n");

        buffer.append("    .fcSttbfUssr          = ");
        buffer.append(HexDump.intToHex(getFcSttbfUssr()));
        buffer.append(" (").append(getFcSttbfUssr()).append(" )\n");

        buffer.append("    .lcbSttbfUssr         = ");
        buffer.append(HexDump.intToHex(getLcbSttbfUssr()));
        buffer.append(" (").append(getLcbSttbfUssr()).append(" )\n");

        buffer.append("[/FIB]\n");
        return buffer.toString();
    }

    /**
     * Size of record (exluding 4 byte header)
     */
    public int getSize()
    {
        return 4 +  + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 4 + 4 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 2 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4;
    }



    /**
     * Get the wIdent field for the FIB record.
     */
    public int getWIdent()
    {
        return field_1_wIdent;
    }

    /**
     * Set the wIdent field for the FIB record.
     */
    public void setWIdent(int field_1_wIdent)
    {
        this.field_1_wIdent = field_1_wIdent;
    }

    /**
     * Get the nFib field for the FIB record.
     */
    public int getNFib()
    {
        return field_2_nFib;
    }

    /**
     * Set the nFib field for the FIB record.
     */
    public void setNFib(int field_2_nFib)
    {
        this.field_2_nFib = field_2_nFib;
    }

    /**
     * Get the nProduct field for the FIB record.
     */
    public int getNProduct()
    {
        return field_3_nProduct;
    }

    /**
     * Set the nProduct field for the FIB record.
     */
    public void setNProduct(int field_3_nProduct)
    {
        this.field_3_nProduct = field_3_nProduct;
    }

    /**
     * Get the lid field for the FIB record.
     */
    public int getLid()
    {
        return field_4_lid;
    }

    /**
     * Set the lid field for the FIB record.
     */
    public void setLid(int field_4_lid)
    {
        this.field_4_lid = field_4_lid;
    }

    /**
     * Get the pnNext field for the FIB record.
     */
    public int getPnNext()
    {
        return field_5_pnNext;
    }

    /**
     * Set the pnNext field for the FIB record.
     */
    public void setPnNext(int field_5_pnNext)
    {
        this.field_5_pnNext = field_5_pnNext;
    }

    /**
     * Get the options field for the FIB record.
     */
    public short getOptions()
    {
        return field_6_options;
    }

    /**
     * Set the options field for the FIB record.
     */
    public void setOptions(short field_6_options)
    {
        this.field_6_options = field_6_options;
    }

    /**
     * Get the nFibBack field for the FIB record.
     */
    public int getNFibBack()
    {
        return field_7_nFibBack;
    }

    /**
     * Set the nFibBack field for the FIB record.
     */
    public void setNFibBack(int field_7_nFibBack)
    {
        this.field_7_nFibBack = field_7_nFibBack;
    }

    /**
     * Get the lKey field for the FIB record.
     */
    public int getLKey()
    {
        return field_8_lKey;
    }

    /**
     * Set the lKey field for the FIB record.
     */
    public void setLKey(int field_8_lKey)
    {
        this.field_8_lKey = field_8_lKey;
    }

    /**
     * Get the envr field for the FIB record.
     */
    public int getEnvr()
    {
        return field_9_envr;
    }

    /**
     * Set the envr field for the FIB record.
     */
    public void setEnvr(int field_9_envr)
    {
        this.field_9_envr = field_9_envr;
    }

    /**
     * Get the history field for the FIB record.
     */
    public short getHistory()
    {
        return field_10_history;
    }

    /**
     * Set the history field for the FIB record.
     */
    public void setHistory(short field_10_history)
    {
        this.field_10_history = field_10_history;
    }

    /**
     * Get the chs field for the FIB record.
     */
    public int getChs()
    {
        return field_11_chs;
    }

    /**
     * Set the chs field for the FIB record.
     */
    public void setChs(int field_11_chs)
    {
        this.field_11_chs = field_11_chs;
    }

    /**
     * Get the chsTables field for the FIB record.
     */
    public int getChsTables()
    {
        return field_12_chsTables;
    }

    /**
     * Set the chsTables field for the FIB record.
     */
    public void setChsTables(int field_12_chsTables)
    {
        this.field_12_chsTables = field_12_chsTables;
    }

    /**
     * Get the fcMin field for the FIB record.
     */
    public int getFcMin()
    {
        return field_13_fcMin;
    }

    /**
     * Set the fcMin field for the FIB record.
     */
    public void setFcMin(int field_13_fcMin)
    {
        this.field_13_fcMin = field_13_fcMin;
    }

    /**
     * Get the fcMac field for the FIB record.
     */
    public int getFcMac()
    {
        return field_14_fcMac;
    }

    /**
     * Set the fcMac field for the FIB record.
     */
    public void setFcMac(int field_14_fcMac)
    {
        this.field_14_fcMac = field_14_fcMac;
    }

    /**
     * Get the csw field for the FIB record.
     */
    public int getCsw()
    {
        return field_15_csw;
    }

    /**
     * Set the csw field for the FIB record.
     */
    public void setCsw(int field_15_csw)
    {
        this.field_15_csw = field_15_csw;
    }

    /**
     * Get the wMagicCreated field for the FIB record.
     */
    public int getWMagicCreated()
    {
        return field_16_wMagicCreated;
    }

    /**
     * Set the wMagicCreated field for the FIB record.
     */
    public void setWMagicCreated(int field_16_wMagicCreated)
    {
        this.field_16_wMagicCreated = field_16_wMagicCreated;
    }

    /**
     * Get the wMagicRevised field for the FIB record.
     */
    public int getWMagicRevised()
    {
        return field_17_wMagicRevised;
    }

    /**
     * Set the wMagicRevised field for the FIB record.
     */
    public void setWMagicRevised(int field_17_wMagicRevised)
    {
        this.field_17_wMagicRevised = field_17_wMagicRevised;
    }

    /**
     * Get the wMagicCreatedPrivate field for the FIB record.
     */
    public int getWMagicCreatedPrivate()
    {
        return field_18_wMagicCreatedPrivate;
    }

    /**
     * Set the wMagicCreatedPrivate field for the FIB record.
     */
    public void setWMagicCreatedPrivate(int field_18_wMagicCreatedPrivate)
    {
        this.field_18_wMagicCreatedPrivate = field_18_wMagicCreatedPrivate;
    }

    /**
     * Get the wMagicRevisedPrivate field for the FIB record.
     */
    public int getWMagicRevisedPrivate()
    {
        return field_19_wMagicRevisedPrivate;
    }

    /**
     * Set the wMagicRevisedPrivate field for the FIB record.
     */
    public void setWMagicRevisedPrivate(int field_19_wMagicRevisedPrivate)
    {
        this.field_19_wMagicRevisedPrivate = field_19_wMagicRevisedPrivate;
    }

    /**
     * Get the pnFbpChpFirst_W6 field for the FIB record.
     */
    public int getPnFbpChpFirst_W6()
    {
        return field_20_pnFbpChpFirst_W6;
    }

    /**
     * Set the pnFbpChpFirst_W6 field for the FIB record.
     */
    public void setPnFbpChpFirst_W6(int field_20_pnFbpChpFirst_W6)
    {
        this.field_20_pnFbpChpFirst_W6 = field_20_pnFbpChpFirst_W6;
    }

    /**
     * Get the pnChpFirst_W6 field for the FIB record.
     */
    public int getPnChpFirst_W6()
    {
        return field_21_pnChpFirst_W6;
    }

    /**
     * Set the pnChpFirst_W6 field for the FIB record.
     */
    public void setPnChpFirst_W6(int field_21_pnChpFirst_W6)
    {
        this.field_21_pnChpFirst_W6 = field_21_pnChpFirst_W6;
    }

    /**
     * Get the cpnBteChp_W6 field for the FIB record.
     */
    public int getCpnBteChp_W6()
    {
        return field_22_cpnBteChp_W6;
    }

    /**
     * Set the cpnBteChp_W6 field for the FIB record.
     */
    public void setCpnBteChp_W6(int field_22_cpnBteChp_W6)
    {
        this.field_22_cpnBteChp_W6 = field_22_cpnBteChp_W6;
    }

    /**
     * Get the pnFbpPapFirst_W6 field for the FIB record.
     */
    public int getPnFbpPapFirst_W6()
    {
        return field_23_pnFbpPapFirst_W6;
    }

    /**
     * Set the pnFbpPapFirst_W6 field for the FIB record.
     */
    public void setPnFbpPapFirst_W6(int field_23_pnFbpPapFirst_W6)
    {
        this.field_23_pnFbpPapFirst_W6 = field_23_pnFbpPapFirst_W6;
    }

    /**
     * Get the pnPapFirst_W6 field for the FIB record.
     */
    public int getPnPapFirst_W6()
    {
        return field_24_pnPapFirst_W6;
    }

    /**
     * Set the pnPapFirst_W6 field for the FIB record.
     */
    public void setPnPapFirst_W6(int field_24_pnPapFirst_W6)
    {
        this.field_24_pnPapFirst_W6 = field_24_pnPapFirst_W6;
    }

    /**
     * Get the cpnBtePap_W6 field for the FIB record.
     */
    public int getCpnBtePap_W6()
    {
        return field_25_cpnBtePap_W6;
    }

    /**
     * Set the cpnBtePap_W6 field for the FIB record.
     */
    public void setCpnBtePap_W6(int field_25_cpnBtePap_W6)
    {
        this.field_25_cpnBtePap_W6 = field_25_cpnBtePap_W6;
    }

    /**
     * Get the pnFbpLvcFirst_W6 field for the FIB record.
     */
    public int getPnFbpLvcFirst_W6()
    {
        return field_26_pnFbpLvcFirst_W6;
    }

    /**
     * Set the pnFbpLvcFirst_W6 field for the FIB record.
     */
    public void setPnFbpLvcFirst_W6(int field_26_pnFbpLvcFirst_W6)
    {
        this.field_26_pnFbpLvcFirst_W6 = field_26_pnFbpLvcFirst_W6;
    }

    /**
     * Get the pnLvcFirst_W6 field for the FIB record.
     */
    public int getPnLvcFirst_W6()
    {
        return field_27_pnLvcFirst_W6;
    }

    /**
     * Set the pnLvcFirst_W6 field for the FIB record.
     */
    public void setPnLvcFirst_W6(int field_27_pnLvcFirst_W6)
    {
        this.field_27_pnLvcFirst_W6 = field_27_pnLvcFirst_W6;
    }

    /**
     * Get the cpnBteLvc_W6 field for the FIB record.
     */
    public int getCpnBteLvc_W6()
    {
        return field_28_cpnBteLvc_W6;
    }

    /**
     * Set the cpnBteLvc_W6 field for the FIB record.
     */
    public void setCpnBteLvc_W6(int field_28_cpnBteLvc_W6)
    {
        this.field_28_cpnBteLvc_W6 = field_28_cpnBteLvc_W6;
    }

    /**
     * Get the lidFE field for the FIB record.
     */
    public int getLidFE()
    {
        return field_29_lidFE;
    }

    /**
     * Set the lidFE field for the FIB record.
     */
    public void setLidFE(int field_29_lidFE)
    {
        this.field_29_lidFE = field_29_lidFE;
    }

    /**
     * Get the clw field for the FIB record.
     */
    public int getClw()
    {
        return field_30_clw;
    }

    /**
     * Set the clw field for the FIB record.
     */
    public void setClw(int field_30_clw)
    {
        this.field_30_clw = field_30_clw;
    }

    /**
     * Get the cbMac field for the FIB record.
     */
    public int getCbMac()
    {
        return field_31_cbMac;
    }

    /**
     * Set the cbMac field for the FIB record.
     */
    public void setCbMac(int field_31_cbMac)
    {
        this.field_31_cbMac = field_31_cbMac;
    }

    /**
     * Get the lProductCreated field for the FIB record.
     */
    public int getLProductCreated()
    {
        return field_32_lProductCreated;
    }

    /**
     * Set the lProductCreated field for the FIB record.
     */
    public void setLProductCreated(int field_32_lProductCreated)
    {
        this.field_32_lProductCreated = field_32_lProductCreated;
    }

    /**
     * Get the lProductRevised field for the FIB record.
     */
    public int getLProductRevised()
    {
        return field_33_lProductRevised;
    }

    /**
     * Set the lProductRevised field for the FIB record.
     */
    public void setLProductRevised(int field_33_lProductRevised)
    {
        this.field_33_lProductRevised = field_33_lProductRevised;
    }

    /**
     * Get the ccpText field for the FIB record.
     */
    public int getCcpText()
    {
        return field_34_ccpText;
    }

    /**
     * Set the ccpText field for the FIB record.
     */
    public void setCcpText(int field_34_ccpText)
    {
        this.field_34_ccpText = field_34_ccpText;
    }

    /**
     * Get the ccpFtn field for the FIB record.
     */
    public int getCcpFtn()
    {
        return field_35_ccpFtn;
    }

    /**
     * Set the ccpFtn field for the FIB record.
     */
    public void setCcpFtn(int field_35_ccpFtn)
    {
        this.field_35_ccpFtn = field_35_ccpFtn;
    }

    /**
     * Get the ccpHdd field for the FIB record.
     */
    public int getCcpHdd()
    {
        return field_36_ccpHdd;
    }

    /**
     * Set the ccpHdd field for the FIB record.
     */
    public void setCcpHdd(int field_36_ccpHdd)
    {
        this.field_36_ccpHdd = field_36_ccpHdd;
    }

    /**
     * Get the ccpMcr field for the FIB record.
     */
    public int getCcpMcr()
    {
        return field_37_ccpMcr;
    }

    /**
     * Set the ccpMcr field for the FIB record.
     */
    public void setCcpMcr(int field_37_ccpMcr)
    {
        this.field_37_ccpMcr = field_37_ccpMcr;
    }

    /**
     * Get the ccpAtn field for the FIB record.
     */
    public int getCcpAtn()
    {
        return field_38_ccpAtn;
    }

    /**
     * Set the ccpAtn field for the FIB record.
     */
    public void setCcpAtn(int field_38_ccpAtn)
    {
        this.field_38_ccpAtn = field_38_ccpAtn;
    }

    /**
     * Get the ccpEdn field for the FIB record.
     */
    public int getCcpEdn()
    {
        return field_39_ccpEdn;
    }

    /**
     * Set the ccpEdn field for the FIB record.
     */
    public void setCcpEdn(int field_39_ccpEdn)
    {
        this.field_39_ccpEdn = field_39_ccpEdn;
    }

    /**
     * Get the ccpTxbx field for the FIB record.
     */
    public int getCcpTxbx()
    {
        return field_40_ccpTxbx;
    }

    /**
     * Set the ccpTxbx field for the FIB record.
     */
    public void setCcpTxbx(int field_40_ccpTxbx)
    {
        this.field_40_ccpTxbx = field_40_ccpTxbx;
    }

    /**
     * Get the ccpHdrTxbx field for the FIB record.
     */
    public int getCcpHdrTxbx()
    {
        return field_41_ccpHdrTxbx;
    }

    /**
     * Set the ccpHdrTxbx field for the FIB record.
     */
    public void setCcpHdrTxbx(int field_41_ccpHdrTxbx)
    {
        this.field_41_ccpHdrTxbx = field_41_ccpHdrTxbx;
    }

    /**
     * Get the pnFbpChpFirst field for the FIB record.
     */
    public int getPnFbpChpFirst()
    {
        return field_42_pnFbpChpFirst;
    }

    /**
     * Set the pnFbpChpFirst field for the FIB record.
     */
    public void setPnFbpChpFirst(int field_42_pnFbpChpFirst)
    {
        this.field_42_pnFbpChpFirst = field_42_pnFbpChpFirst;
    }

    /**
     * Get the pnChpFirst field for the FIB record.
     */
    public int getPnChpFirst()
    {
        return field_43_pnChpFirst;
    }

    /**
     * Set the pnChpFirst field for the FIB record.
     */
    public void setPnChpFirst(int field_43_pnChpFirst)
    {
        this.field_43_pnChpFirst = field_43_pnChpFirst;
    }

    /**
     * Get the cpnBteChp field for the FIB record.
     */
    public int getCpnBteChp()
    {
        return field_44_cpnBteChp;
    }

    /**
     * Set the cpnBteChp field for the FIB record.
     */
    public void setCpnBteChp(int field_44_cpnBteChp)
    {
        this.field_44_cpnBteChp = field_44_cpnBteChp;
    }

    /**
     * Get the pnFbpPapFirst field for the FIB record.
     */
    public int getPnFbpPapFirst()
    {
        return field_45_pnFbpPapFirst;
    }

    /**
     * Set the pnFbpPapFirst field for the FIB record.
     */
    public void setPnFbpPapFirst(int field_45_pnFbpPapFirst)
    {
        this.field_45_pnFbpPapFirst = field_45_pnFbpPapFirst;
    }

    /**
     * Get the pnPapFirst field for the FIB record.
     */
    public int getPnPapFirst()
    {
        return field_46_pnPapFirst;
    }

    /**
     * Set the pnPapFirst field for the FIB record.
     */
    public void setPnPapFirst(int field_46_pnPapFirst)
    {
        this.field_46_pnPapFirst = field_46_pnPapFirst;
    }

    /**
     * Get the cpnBtePap field for the FIB record.
     */
    public int getCpnBtePap()
    {
        return field_47_cpnBtePap;
    }

    /**
     * Set the cpnBtePap field for the FIB record.
     */
    public void setCpnBtePap(int field_47_cpnBtePap)
    {
        this.field_47_cpnBtePap = field_47_cpnBtePap;
    }

    /**
     * Get the pnFbpLvcFirst field for the FIB record.
     */
    public int getPnFbpLvcFirst()
    {
        return field_48_pnFbpLvcFirst;
    }

    /**
     * Set the pnFbpLvcFirst field for the FIB record.
     */
    public void setPnFbpLvcFirst(int field_48_pnFbpLvcFirst)
    {
        this.field_48_pnFbpLvcFirst = field_48_pnFbpLvcFirst;
    }

    /**
     * Get the pnLvcFirst field for the FIB record.
     */
    public int getPnLvcFirst()
    {
        return field_49_pnLvcFirst;
    }

    /**
     * Set the pnLvcFirst field for the FIB record.
     */
    public void setPnLvcFirst(int field_49_pnLvcFirst)
    {
        this.field_49_pnLvcFirst = field_49_pnLvcFirst;
    }

    /**
     * Get the cpnBteLvc field for the FIB record.
     */
    public int getCpnBteLvc()
    {
        return field_50_cpnBteLvc;
    }

    /**
     * Set the cpnBteLvc field for the FIB record.
     */
    public void setCpnBteLvc(int field_50_cpnBteLvc)
    {
        this.field_50_cpnBteLvc = field_50_cpnBteLvc;
    }

    /**
     * Get the fcIslandFirst field for the FIB record.
     */
    public int getFcIslandFirst()
    {
        return field_51_fcIslandFirst;
    }

    /**
     * Set the fcIslandFirst field for the FIB record.
     */
    public void setFcIslandFirst(int field_51_fcIslandFirst)
    {
        this.field_51_fcIslandFirst = field_51_fcIslandFirst;
    }

    /**
     * Get the fcIslandLim field for the FIB record.
     */
    public int getFcIslandLim()
    {
        return field_52_fcIslandLim;
    }

    /**
     * Set the fcIslandLim field for the FIB record.
     */
    public void setFcIslandLim(int field_52_fcIslandLim)
    {
        this.field_52_fcIslandLim = field_52_fcIslandLim;
    }

    /**
     * Get the cfclcb field for the FIB record.
     */
    public int getCfclcb()
    {
        return field_53_cfclcb;
    }

    /**
     * Set the cfclcb field for the FIB record.
     */
    public void setCfclcb(int field_53_cfclcb)
    {
        this.field_53_cfclcb = field_53_cfclcb;
    }

    /**
     * Get the fcStshfOrig field for the FIB record.
     */
    public int getFcStshfOrig()
    {
        return field_54_fcStshfOrig;
    }

    /**
     * Set the fcStshfOrig field for the FIB record.
     */
    public void setFcStshfOrig(int field_54_fcStshfOrig)
    {
        this.field_54_fcStshfOrig = field_54_fcStshfOrig;
    }

    /**
     * Get the lcbStshfOrig field for the FIB record.
     */
    public int getLcbStshfOrig()
    {
        return field_55_lcbStshfOrig;
    }

    /**
     * Set the lcbStshfOrig field for the FIB record.
     */
    public void setLcbStshfOrig(int field_55_lcbStshfOrig)
    {
        this.field_55_lcbStshfOrig = field_55_lcbStshfOrig;
    }

    /**
     * Get the fcStshf field for the FIB record.
     */
    public int getFcStshf()
    {
        return field_56_fcStshf;
    }

    /**
     * Set the fcStshf field for the FIB record.
     */
    public void setFcStshf(int field_56_fcStshf)
    {
        this.field_56_fcStshf = field_56_fcStshf;
    }

    /**
     * Get the lcbStshf field for the FIB record.
     */
    public int getLcbStshf()
    {
        return field_57_lcbStshf;
    }

    /**
     * Set the lcbStshf field for the FIB record.
     */
    public void setLcbStshf(int field_57_lcbStshf)
    {
        this.field_57_lcbStshf = field_57_lcbStshf;
    }

    /**
     * Get the fcPlcffndRef field for the FIB record.
     */
    public int getFcPlcffndRef()
    {
        return field_58_fcPlcffndRef;
    }

    /**
     * Set the fcPlcffndRef field for the FIB record.
     */
    public void setFcPlcffndRef(int field_58_fcPlcffndRef)
    {
        this.field_58_fcPlcffndRef = field_58_fcPlcffndRef;
    }

    /**
     * Get the lcbPlcffndRef field for the FIB record.
     */
    public int getLcbPlcffndRef()
    {
        return field_59_lcbPlcffndRef;
    }

    /**
     * Set the lcbPlcffndRef field for the FIB record.
     */
    public void setLcbPlcffndRef(int field_59_lcbPlcffndRef)
    {
        this.field_59_lcbPlcffndRef = field_59_lcbPlcffndRef;
    }

    /**
     * Get the fcPlcffndTxt field for the FIB record.
     */
    public int getFcPlcffndTxt()
    {
        return field_60_fcPlcffndTxt;
    }

    /**
     * Set the fcPlcffndTxt field for the FIB record.
     */
    public void setFcPlcffndTxt(int field_60_fcPlcffndTxt)
    {
        this.field_60_fcPlcffndTxt = field_60_fcPlcffndTxt;
    }

    /**
     * Get the lcbPlcffndTxt field for the FIB record.
     */
    public int getLcbPlcffndTxt()
    {
        return field_61_lcbPlcffndTxt;
    }

    /**
     * Set the lcbPlcffndTxt field for the FIB record.
     */
    public void setLcbPlcffndTxt(int field_61_lcbPlcffndTxt)
    {
        this.field_61_lcbPlcffndTxt = field_61_lcbPlcffndTxt;
    }

    /**
     * Get the fcPlcfandRef field for the FIB record.
     */
    public int getFcPlcfandRef()
    {
        return field_62_fcPlcfandRef;
    }

    /**
     * Set the fcPlcfandRef field for the FIB record.
     */
    public void setFcPlcfandRef(int field_62_fcPlcfandRef)
    {
        this.field_62_fcPlcfandRef = field_62_fcPlcfandRef;
    }

    /**
     * Get the lcbPlcfandRef field for the FIB record.
     */
    public int getLcbPlcfandRef()
    {
        return field_63_lcbPlcfandRef;
    }

    /**
     * Set the lcbPlcfandRef field for the FIB record.
     */
    public void setLcbPlcfandRef(int field_63_lcbPlcfandRef)
    {
        this.field_63_lcbPlcfandRef = field_63_lcbPlcfandRef;
    }

    /**
     * Get the fcPlcfandTxt field for the FIB record.
     */
    public int getFcPlcfandTxt()
    {
        return field_64_fcPlcfandTxt;
    }

    /**
     * Set the fcPlcfandTxt field for the FIB record.
     */
    public void setFcPlcfandTxt(int field_64_fcPlcfandTxt)
    {
        this.field_64_fcPlcfandTxt = field_64_fcPlcfandTxt;
    }

    /**
     * Get the lcbPlcfandTxt field for the FIB record.
     */
    public int getLcbPlcfandTxt()
    {
        return field_65_lcbPlcfandTxt;
    }

    /**
     * Set the lcbPlcfandTxt field for the FIB record.
     */
    public void setLcbPlcfandTxt(int field_65_lcbPlcfandTxt)
    {
        this.field_65_lcbPlcfandTxt = field_65_lcbPlcfandTxt;
    }

    /**
     * Get the fcPlcfsed field for the FIB record.
     */
    public int getFcPlcfsed()
    {
        return field_66_fcPlcfsed;
    }

    /**
     * Set the fcPlcfsed field for the FIB record.
     */
    public void setFcPlcfsed(int field_66_fcPlcfsed)
    {
        this.field_66_fcPlcfsed = field_66_fcPlcfsed;
    }

    /**
     * Get the lcbPlcfsed field for the FIB record.
     */
    public int getLcbPlcfsed()
    {
        return field_67_lcbPlcfsed;
    }

    /**
     * Set the lcbPlcfsed field for the FIB record.
     */
    public void setLcbPlcfsed(int field_67_lcbPlcfsed)
    {
        this.field_67_lcbPlcfsed = field_67_lcbPlcfsed;
    }

    /**
     * Get the fcPlcpad field for the FIB record.
     */
    public int getFcPlcpad()
    {
        return field_68_fcPlcpad;
    }

    /**
     * Set the fcPlcpad field for the FIB record.
     */
    public void setFcPlcpad(int field_68_fcPlcpad)
    {
        this.field_68_fcPlcpad = field_68_fcPlcpad;
    }

    /**
     * Get the lcbPlcpad field for the FIB record.
     */
    public int getLcbPlcpad()
    {
        return field_69_lcbPlcpad;
    }

    /**
     * Set the lcbPlcpad field for the FIB record.
     */
    public void setLcbPlcpad(int field_69_lcbPlcpad)
    {
        this.field_69_lcbPlcpad = field_69_lcbPlcpad;
    }

    /**
     * Get the fcPlcfphe field for the FIB record.
     */
    public int getFcPlcfphe()
    {
        return field_70_fcPlcfphe;
    }

    /**
     * Set the fcPlcfphe field for the FIB record.
     */
    public void setFcPlcfphe(int field_70_fcPlcfphe)
    {
        this.field_70_fcPlcfphe = field_70_fcPlcfphe;
    }

    /**
     * Get the lcbPlcfphe field for the FIB record.
     */
    public int getLcbPlcfphe()
    {
        return field_71_lcbPlcfphe;
    }

    /**
     * Set the lcbPlcfphe field for the FIB record.
     */
    public void setLcbPlcfphe(int field_71_lcbPlcfphe)
    {
        this.field_71_lcbPlcfphe = field_71_lcbPlcfphe;
    }

    /**
     * Get the fcSttbfglsy field for the FIB record.
     */
    public int getFcSttbfglsy()
    {
        return field_72_fcSttbfglsy;
    }

    /**
     * Set the fcSttbfglsy field for the FIB record.
     */
    public void setFcSttbfglsy(int field_72_fcSttbfglsy)
    {
        this.field_72_fcSttbfglsy = field_72_fcSttbfglsy;
    }

    /**
     * Get the lcbSttbfglsy field for the FIB record.
     */
    public int getLcbSttbfglsy()
    {
        return field_73_lcbSttbfglsy;
    }

    /**
     * Set the lcbSttbfglsy field for the FIB record.
     */
    public void setLcbSttbfglsy(int field_73_lcbSttbfglsy)
    {
        this.field_73_lcbSttbfglsy = field_73_lcbSttbfglsy;
    }

    /**
     * Get the fcPlcfglsy field for the FIB record.
     */
    public int getFcPlcfglsy()
    {
        return field_74_fcPlcfglsy;
    }

    /**
     * Set the fcPlcfglsy field for the FIB record.
     */
    public void setFcPlcfglsy(int field_74_fcPlcfglsy)
    {
        this.field_74_fcPlcfglsy = field_74_fcPlcfglsy;
    }

    /**
     * Get the lcbPlcfglsy field for the FIB record.
     */
    public int getLcbPlcfglsy()
    {
        return field_75_lcbPlcfglsy;
    }

    /**
     * Set the lcbPlcfglsy field for the FIB record.
     */
    public void setLcbPlcfglsy(int field_75_lcbPlcfglsy)
    {
        this.field_75_lcbPlcfglsy = field_75_lcbPlcfglsy;
    }

    /**
     * Get the fcPlcfhdd field for the FIB record.
     */
    public int getFcPlcfhdd()
    {
        return field_76_fcPlcfhdd;
    }

    /**
     * Set the fcPlcfhdd field for the FIB record.
     */
    public void setFcPlcfhdd(int field_76_fcPlcfhdd)
    {
        this.field_76_fcPlcfhdd = field_76_fcPlcfhdd;
    }

    /**
     * Get the lcbPlcfhdd field for the FIB record.
     */
    public int getLcbPlcfhdd()
    {
        return field_77_lcbPlcfhdd;
    }

    /**
     * Set the lcbPlcfhdd field for the FIB record.
     */
    public void setLcbPlcfhdd(int field_77_lcbPlcfhdd)
    {
        this.field_77_lcbPlcfhdd = field_77_lcbPlcfhdd;
    }

    /**
     * Get the fcPlcfbteChpx field for the FIB record.
     */
    public int getFcPlcfbteChpx()
    {
        return field_78_fcPlcfbteChpx;
    }

    /**
     * Set the fcPlcfbteChpx field for the FIB record.
     */
    public void setFcPlcfbteChpx(int field_78_fcPlcfbteChpx)
    {
        this.field_78_fcPlcfbteChpx = field_78_fcPlcfbteChpx;
    }

    /**
     * Get the lcbPlcfbteChpx field for the FIB record.
     */
    public int getLcbPlcfbteChpx()
    {
        return field_79_lcbPlcfbteChpx;
    }

    /**
     * Set the lcbPlcfbteChpx field for the FIB record.
     */
    public void setLcbPlcfbteChpx(int field_79_lcbPlcfbteChpx)
    {
        this.field_79_lcbPlcfbteChpx = field_79_lcbPlcfbteChpx;
    }

    /**
     * Get the fcPlcfbtePapx field for the FIB record.
     */
    public int getFcPlcfbtePapx()
    {
        return field_80_fcPlcfbtePapx;
    }

    /**
     * Set the fcPlcfbtePapx field for the FIB record.
     */
    public void setFcPlcfbtePapx(int field_80_fcPlcfbtePapx)
    {
        this.field_80_fcPlcfbtePapx = field_80_fcPlcfbtePapx;
    }

    /**
     * Get the lcbPlcfbtePapx field for the FIB record.
     */
    public int getLcbPlcfbtePapx()
    {
        return field_81_lcbPlcfbtePapx;
    }

    /**
     * Set the lcbPlcfbtePapx field for the FIB record.
     */
    public void setLcbPlcfbtePapx(int field_81_lcbPlcfbtePapx)
    {
        this.field_81_lcbPlcfbtePapx = field_81_lcbPlcfbtePapx;
    }

    /**
     * Get the fcPlcfsea field for the FIB record.
     */
    public int getFcPlcfsea()
    {
        return field_82_fcPlcfsea;
    }

    /**
     * Set the fcPlcfsea field for the FIB record.
     */
    public void setFcPlcfsea(int field_82_fcPlcfsea)
    {
        this.field_82_fcPlcfsea = field_82_fcPlcfsea;
    }

    /**
     * Get the lcbPlcfsea field for the FIB record.
     */
    public int getLcbPlcfsea()
    {
        return field_83_lcbPlcfsea;
    }

    /**
     * Set the lcbPlcfsea field for the FIB record.
     */
    public void setLcbPlcfsea(int field_83_lcbPlcfsea)
    {
        this.field_83_lcbPlcfsea = field_83_lcbPlcfsea;
    }

    /**
     * Get the fcSttbfffn field for the FIB record.
     */
    public int getFcSttbfffn()
    {
        return field_84_fcSttbfffn;
    }

    /**
     * Set the fcSttbfffn field for the FIB record.
     */
    public void setFcSttbfffn(int field_84_fcSttbfffn)
    {
        this.field_84_fcSttbfffn = field_84_fcSttbfffn;
    }

    /**
     * Get the lcbSttbfffn field for the FIB record.
     */
    public int getLcbSttbfffn()
    {
        return field_85_lcbSttbfffn;
    }

    /**
     * Set the lcbSttbfffn field for the FIB record.
     */
    public void setLcbSttbfffn(int field_85_lcbSttbfffn)
    {
        this.field_85_lcbSttbfffn = field_85_lcbSttbfffn;
    }

    /**
     * Get the fcPlcffldMom field for the FIB record.
     */
    public int getFcPlcffldMom()
    {
        return field_86_fcPlcffldMom;
    }

    /**
     * Set the fcPlcffldMom field for the FIB record.
     */
    public void setFcPlcffldMom(int field_86_fcPlcffldMom)
    {
        this.field_86_fcPlcffldMom = field_86_fcPlcffldMom;
    }

    /**
     * Get the lcbPlcffldMom field for the FIB record.
     */
    public int getLcbPlcffldMom()
    {
        return field_87_lcbPlcffldMom;
    }

    /**
     * Set the lcbPlcffldMom field for the FIB record.
     */
    public void setLcbPlcffldMom(int field_87_lcbPlcffldMom)
    {
        this.field_87_lcbPlcffldMom = field_87_lcbPlcffldMom;
    }

    /**
     * Get the fcPlcffldHdr field for the FIB record.
     */
    public int getFcPlcffldHdr()
    {
        return field_88_fcPlcffldHdr;
    }

    /**
     * Set the fcPlcffldHdr field for the FIB record.
     */
    public void setFcPlcffldHdr(int field_88_fcPlcffldHdr)
    {
        this.field_88_fcPlcffldHdr = field_88_fcPlcffldHdr;
    }

    /**
     * Get the lcbPlcffldHdr field for the FIB record.
     */
    public int getLcbPlcffldHdr()
    {
        return field_89_lcbPlcffldHdr;
    }

    /**
     * Set the lcbPlcffldHdr field for the FIB record.
     */
    public void setLcbPlcffldHdr(int field_89_lcbPlcffldHdr)
    {
        this.field_89_lcbPlcffldHdr = field_89_lcbPlcffldHdr;
    }

    /**
     * Get the fcPlcffldFtn field for the FIB record.
     */
    public int getFcPlcffldFtn()
    {
        return field_90_fcPlcffldFtn;
    }

    /**
     * Set the fcPlcffldFtn field for the FIB record.
     */
    public void setFcPlcffldFtn(int field_90_fcPlcffldFtn)
    {
        this.field_90_fcPlcffldFtn = field_90_fcPlcffldFtn;
    }

    /**
     * Get the lcbPlcffldFtn field for the FIB record.
     */
    public int getLcbPlcffldFtn()
    {
        return field_91_lcbPlcffldFtn;
    }

    /**
     * Set the lcbPlcffldFtn field for the FIB record.
     */
    public void setLcbPlcffldFtn(int field_91_lcbPlcffldFtn)
    {
        this.field_91_lcbPlcffldFtn = field_91_lcbPlcffldFtn;
    }

    /**
     * Get the fcPlcffldAtn field for the FIB record.
     */
    public int getFcPlcffldAtn()
    {
        return field_92_fcPlcffldAtn;
    }

    /**
     * Set the fcPlcffldAtn field for the FIB record.
     */
    public void setFcPlcffldAtn(int field_92_fcPlcffldAtn)
    {
        this.field_92_fcPlcffldAtn = field_92_fcPlcffldAtn;
    }

    /**
     * Get the lcbPlcffldAtn field for the FIB record.
     */
    public int getLcbPlcffldAtn()
    {
        return field_93_lcbPlcffldAtn;
    }

    /**
     * Set the lcbPlcffldAtn field for the FIB record.
     */
    public void setLcbPlcffldAtn(int field_93_lcbPlcffldAtn)
    {
        this.field_93_lcbPlcffldAtn = field_93_lcbPlcffldAtn;
    }

    /**
     * Get the fcPlcffldMcr field for the FIB record.
     */
    public int getFcPlcffldMcr()
    {
        return field_94_fcPlcffldMcr;
    }

    /**
     * Set the fcPlcffldMcr field for the FIB record.
     */
    public void setFcPlcffldMcr(int field_94_fcPlcffldMcr)
    {
        this.field_94_fcPlcffldMcr = field_94_fcPlcffldMcr;
    }

    /**
     * Get the lcbPlcffldMcr field for the FIB record.
     */
    public int getLcbPlcffldMcr()
    {
        return field_95_lcbPlcffldMcr;
    }

    /**
     * Set the lcbPlcffldMcr field for the FIB record.
     */
    public void setLcbPlcffldMcr(int field_95_lcbPlcffldMcr)
    {
        this.field_95_lcbPlcffldMcr = field_95_lcbPlcffldMcr;
    }

    /**
     * Get the fcSttbfbkmk field for the FIB record.
     */
    public int getFcSttbfbkmk()
    {
        return field_96_fcSttbfbkmk;
    }

    /**
     * Set the fcSttbfbkmk field for the FIB record.
     */
    public void setFcSttbfbkmk(int field_96_fcSttbfbkmk)
    {
        this.field_96_fcSttbfbkmk = field_96_fcSttbfbkmk;
    }

    /**
     * Get the lcbSttbfbkmk field for the FIB record.
     */
    public int getLcbSttbfbkmk()
    {
        return field_97_lcbSttbfbkmk;
    }

    /**
     * Set the lcbSttbfbkmk field for the FIB record.
     */
    public void setLcbSttbfbkmk(int field_97_lcbSttbfbkmk)
    {
        this.field_97_lcbSttbfbkmk = field_97_lcbSttbfbkmk;
    }

    /**
     * Get the fcPlcfbkf field for the FIB record.
     */
    public int getFcPlcfbkf()
    {
        return field_98_fcPlcfbkf;
    }

    /**
     * Set the fcPlcfbkf field for the FIB record.
     */
    public void setFcPlcfbkf(int field_98_fcPlcfbkf)
    {
        this.field_98_fcPlcfbkf = field_98_fcPlcfbkf;
    }

    /**
     * Get the lcbPlcfbkf field for the FIB record.
     */
    public int getLcbPlcfbkf()
    {
        return field_99_lcbPlcfbkf;
    }

    /**
     * Set the lcbPlcfbkf field for the FIB record.
     */
    public void setLcbPlcfbkf(int field_99_lcbPlcfbkf)
    {
        this.field_99_lcbPlcfbkf = field_99_lcbPlcfbkf;
    }

    /**
     * Get the fcPlcfbkl field for the FIB record.
     */
    public int getFcPlcfbkl()
    {
        return field_100_fcPlcfbkl;
    }

    /**
     * Set the fcPlcfbkl field for the FIB record.
     */
    public void setFcPlcfbkl(int field_100_fcPlcfbkl)
    {
        this.field_100_fcPlcfbkl = field_100_fcPlcfbkl;
    }

    /**
     * Get the lcbPlcfbkl field for the FIB record.
     */
    public int getLcbPlcfbkl()
    {
        return field_101_lcbPlcfbkl;
    }

    /**
     * Set the lcbPlcfbkl field for the FIB record.
     */
    public void setLcbPlcfbkl(int field_101_lcbPlcfbkl)
    {
        this.field_101_lcbPlcfbkl = field_101_lcbPlcfbkl;
    }

    /**
     * Get the fcCmds field for the FIB record.
     */
    public int getFcCmds()
    {
        return field_102_fcCmds;
    }

    /**
     * Set the fcCmds field for the FIB record.
     */
    public void setFcCmds(int field_102_fcCmds)
    {
        this.field_102_fcCmds = field_102_fcCmds;
    }

    /**
     * Get the lcbCmds field for the FIB record.
     */
    public int getLcbCmds()
    {
        return field_103_lcbCmds;
    }

    /**
     * Set the lcbCmds field for the FIB record.
     */
    public void setLcbCmds(int field_103_lcbCmds)
    {
        this.field_103_lcbCmds = field_103_lcbCmds;
    }

    /**
     * Get the fcPlcmcr field for the FIB record.
     */
    public int getFcPlcmcr()
    {
        return field_104_fcPlcmcr;
    }

    /**
     * Set the fcPlcmcr field for the FIB record.
     */
    public void setFcPlcmcr(int field_104_fcPlcmcr)
    {
        this.field_104_fcPlcmcr = field_104_fcPlcmcr;
    }

    /**
     * Get the lcbPlcmcr field for the FIB record.
     */
    public int getLcbPlcmcr()
    {
        return field_105_lcbPlcmcr;
    }

    /**
     * Set the lcbPlcmcr field for the FIB record.
     */
    public void setLcbPlcmcr(int field_105_lcbPlcmcr)
    {
        this.field_105_lcbPlcmcr = field_105_lcbPlcmcr;
    }

    /**
     * Get the fcSttbfmcr field for the FIB record.
     */
    public int getFcSttbfmcr()
    {
        return field_106_fcSttbfmcr;
    }

    /**
     * Set the fcSttbfmcr field for the FIB record.
     */
    public void setFcSttbfmcr(int field_106_fcSttbfmcr)
    {
        this.field_106_fcSttbfmcr = field_106_fcSttbfmcr;
    }

    /**
     * Get the lcbSttbfmcr field for the FIB record.
     */
    public int getLcbSttbfmcr()
    {
        return field_107_lcbSttbfmcr;
    }

    /**
     * Set the lcbSttbfmcr field for the FIB record.
     */
    public void setLcbSttbfmcr(int field_107_lcbSttbfmcr)
    {
        this.field_107_lcbSttbfmcr = field_107_lcbSttbfmcr;
    }

    /**
     * Get the fcPrDrvr field for the FIB record.
     */
    public int getFcPrDrvr()
    {
        return field_108_fcPrDrvr;
    }

    /**
     * Set the fcPrDrvr field for the FIB record.
     */
    public void setFcPrDrvr(int field_108_fcPrDrvr)
    {
        this.field_108_fcPrDrvr = field_108_fcPrDrvr;
    }

    /**
     * Get the lcbPrDrvr field for the FIB record.
     */
    public int getLcbPrDrvr()
    {
        return field_109_lcbPrDrvr;
    }

    /**
     * Set the lcbPrDrvr field for the FIB record.
     */
    public void setLcbPrDrvr(int field_109_lcbPrDrvr)
    {
        this.field_109_lcbPrDrvr = field_109_lcbPrDrvr;
    }

    /**
     * Get the fcPrEnvPort field for the FIB record.
     */
    public int getFcPrEnvPort()
    {
        return field_110_fcPrEnvPort;
    }

    /**
     * Set the fcPrEnvPort field for the FIB record.
     */
    public void setFcPrEnvPort(int field_110_fcPrEnvPort)
    {
        this.field_110_fcPrEnvPort = field_110_fcPrEnvPort;
    }

    /**
     * Get the lcbPrEnvPort field for the FIB record.
     */
    public int getLcbPrEnvPort()
    {
        return field_111_lcbPrEnvPort;
    }

    /**
     * Set the lcbPrEnvPort field for the FIB record.
     */
    public void setLcbPrEnvPort(int field_111_lcbPrEnvPort)
    {
        this.field_111_lcbPrEnvPort = field_111_lcbPrEnvPort;
    }

    /**
     * Get the fcPrEnvLand field for the FIB record.
     */
    public int getFcPrEnvLand()
    {
        return field_112_fcPrEnvLand;
    }

    /**
     * Set the fcPrEnvLand field for the FIB record.
     */
    public void setFcPrEnvLand(int field_112_fcPrEnvLand)
    {
        this.field_112_fcPrEnvLand = field_112_fcPrEnvLand;
    }

    /**
     * Get the lcbPrEnvLand field for the FIB record.
     */
    public int getLcbPrEnvLand()
    {
        return field_113_lcbPrEnvLand;
    }

    /**
     * Set the lcbPrEnvLand field for the FIB record.
     */
    public void setLcbPrEnvLand(int field_113_lcbPrEnvLand)
    {
        this.field_113_lcbPrEnvLand = field_113_lcbPrEnvLand;
    }

    /**
     * Get the fcWss field for the FIB record.
     */
    public int getFcWss()
    {
        return field_114_fcWss;
    }

    /**
     * Set the fcWss field for the FIB record.
     */
    public void setFcWss(int field_114_fcWss)
    {
        this.field_114_fcWss = field_114_fcWss;
    }

    /**
     * Get the lcbWss field for the FIB record.
     */
    public int getLcbWss()
    {
        return field_115_lcbWss;
    }

    /**
     * Set the lcbWss field for the FIB record.
     */
    public void setLcbWss(int field_115_lcbWss)
    {
        this.field_115_lcbWss = field_115_lcbWss;
    }

    /**
     * Get the fcDop field for the FIB record.
     */
    public int getFcDop()
    {
        return field_116_fcDop;
    }

    /**
     * Set the fcDop field for the FIB record.
     */
    public void setFcDop(int field_116_fcDop)
    {
        this.field_116_fcDop = field_116_fcDop;
    }

    /**
     * Get the lcbDop field for the FIB record.
     */
    public int getLcbDop()
    {
        return field_117_lcbDop;
    }

    /**
     * Set the lcbDop field for the FIB record.
     */
    public void setLcbDop(int field_117_lcbDop)
    {
        this.field_117_lcbDop = field_117_lcbDop;
    }

    /**
     * Get the fcSttbfAssoc field for the FIB record.
     */
    public int getFcSttbfAssoc()
    {
        return field_118_fcSttbfAssoc;
    }

    /**
     * Set the fcSttbfAssoc field for the FIB record.
     */
    public void setFcSttbfAssoc(int field_118_fcSttbfAssoc)
    {
        this.field_118_fcSttbfAssoc = field_118_fcSttbfAssoc;
    }

    /**
     * Get the lcbSttbfAssoc field for the FIB record.
     */
    public int getLcbSttbfAssoc()
    {
        return field_119_lcbSttbfAssoc;
    }

    /**
     * Set the lcbSttbfAssoc field for the FIB record.
     */
    public void setLcbSttbfAssoc(int field_119_lcbSttbfAssoc)
    {
        this.field_119_lcbSttbfAssoc = field_119_lcbSttbfAssoc;
    }

    /**
     * Get the fcClx field for the FIB record.
     */
    public int getFcClx()
    {
        return field_120_fcClx;
    }

    /**
     * Set the fcClx field for the FIB record.
     */
    public void setFcClx(int field_120_fcClx)
    {
        this.field_120_fcClx = field_120_fcClx;
    }

    /**
     * Get the lcbClx field for the FIB record.
     */
    public int getLcbClx()
    {
        return field_121_lcbClx;
    }

    /**
     * Set the lcbClx field for the FIB record.
     */
    public void setLcbClx(int field_121_lcbClx)
    {
        this.field_121_lcbClx = field_121_lcbClx;
    }

    /**
     * Get the fcPlcfpgdFtn field for the FIB record.
     */
    public int getFcPlcfpgdFtn()
    {
        return field_122_fcPlcfpgdFtn;
    }

    /**
     * Set the fcPlcfpgdFtn field for the FIB record.
     */
    public void setFcPlcfpgdFtn(int field_122_fcPlcfpgdFtn)
    {
        this.field_122_fcPlcfpgdFtn = field_122_fcPlcfpgdFtn;
    }

    /**
     * Get the lcbPlcfpgdFtn field for the FIB record.
     */
    public int getLcbPlcfpgdFtn()
    {
        return field_123_lcbPlcfpgdFtn;
    }

    /**
     * Set the lcbPlcfpgdFtn field for the FIB record.
     */
    public void setLcbPlcfpgdFtn(int field_123_lcbPlcfpgdFtn)
    {
        this.field_123_lcbPlcfpgdFtn = field_123_lcbPlcfpgdFtn;
    }

    /**
     * Get the fcAutosaveSource field for the FIB record.
     */
    public int getFcAutosaveSource()
    {
        return field_124_fcAutosaveSource;
    }

    /**
     * Set the fcAutosaveSource field for the FIB record.
     */
    public void setFcAutosaveSource(int field_124_fcAutosaveSource)
    {
        this.field_124_fcAutosaveSource = field_124_fcAutosaveSource;
    }

    /**
     * Get the lcbAutosaveSource field for the FIB record.
     */
    public int getLcbAutosaveSource()
    {
        return field_125_lcbAutosaveSource;
    }

    /**
     * Set the lcbAutosaveSource field for the FIB record.
     */
    public void setLcbAutosaveSource(int field_125_lcbAutosaveSource)
    {
        this.field_125_lcbAutosaveSource = field_125_lcbAutosaveSource;
    }

    /**
     * Get the fcGrpXstAtnOwners field for the FIB record.
     */
    public int getFcGrpXstAtnOwners()
    {
        return field_126_fcGrpXstAtnOwners;
    }

    /**
     * Set the fcGrpXstAtnOwners field for the FIB record.
     */
    public void setFcGrpXstAtnOwners(int field_126_fcGrpXstAtnOwners)
    {
        this.field_126_fcGrpXstAtnOwners = field_126_fcGrpXstAtnOwners;
    }

    /**
     * Get the lcbGrpXstAtnOwners field for the FIB record.
     */
    public int getLcbGrpXstAtnOwners()
    {
        return field_127_lcbGrpXstAtnOwners;
    }

    /**
     * Set the lcbGrpXstAtnOwners field for the FIB record.
     */
    public void setLcbGrpXstAtnOwners(int field_127_lcbGrpXstAtnOwners)
    {
        this.field_127_lcbGrpXstAtnOwners = field_127_lcbGrpXstAtnOwners;
    }

    /**
     * Get the fcSttbfAtnbkmk field for the FIB record.
     */
    public int getFcSttbfAtnbkmk()
    {
        return field_128_fcSttbfAtnbkmk;
    }

    /**
     * Set the fcSttbfAtnbkmk field for the FIB record.
     */
    public void setFcSttbfAtnbkmk(int field_128_fcSttbfAtnbkmk)
    {
        this.field_128_fcSttbfAtnbkmk = field_128_fcSttbfAtnbkmk;
    }

    /**
     * Get the lcbSttbfAtnbkmk field for the FIB record.
     */
    public int getLcbSttbfAtnbkmk()
    {
        return field_129_lcbSttbfAtnbkmk;
    }

    /**
     * Set the lcbSttbfAtnbkmk field for the FIB record.
     */
    public void setLcbSttbfAtnbkmk(int field_129_lcbSttbfAtnbkmk)
    {
        this.field_129_lcbSttbfAtnbkmk = field_129_lcbSttbfAtnbkmk;
    }

    /**
     * Get the fcPlcdoaMom field for the FIB record.
     */
    public int getFcPlcdoaMom()
    {
        return field_130_fcPlcdoaMom;
    }

    /**
     * Set the fcPlcdoaMom field for the FIB record.
     */
    public void setFcPlcdoaMom(int field_130_fcPlcdoaMom)
    {
        this.field_130_fcPlcdoaMom = field_130_fcPlcdoaMom;
    }

    /**
     * Get the lcbPlcdoaMom field for the FIB record.
     */
    public int getLcbPlcdoaMom()
    {
        return field_131_lcbPlcdoaMom;
    }

    /**
     * Set the lcbPlcdoaMom field for the FIB record.
     */
    public void setLcbPlcdoaMom(int field_131_lcbPlcdoaMom)
    {
        this.field_131_lcbPlcdoaMom = field_131_lcbPlcdoaMom;
    }

    /**
     * Get the fcPlcdoaHdr field for the FIB record.
     */
    public int getFcPlcdoaHdr()
    {
        return field_132_fcPlcdoaHdr;
    }

    /**
     * Set the fcPlcdoaHdr field for the FIB record.
     */
    public void setFcPlcdoaHdr(int field_132_fcPlcdoaHdr)
    {
        this.field_132_fcPlcdoaHdr = field_132_fcPlcdoaHdr;
    }

    /**
     * Get the lcbPlcdoaHdr field for the FIB record.
     */
    public int getLcbPlcdoaHdr()
    {
        return field_133_lcbPlcdoaHdr;
    }

    /**
     * Set the lcbPlcdoaHdr field for the FIB record.
     */
    public void setLcbPlcdoaHdr(int field_133_lcbPlcdoaHdr)
    {
        this.field_133_lcbPlcdoaHdr = field_133_lcbPlcdoaHdr;
    }

    /**
     * Get the fcPlcspaMom field for the FIB record.
     */
    public int getFcPlcspaMom()
    {
        return field_134_fcPlcspaMom;
    }

    /**
     * Set the fcPlcspaMom field for the FIB record.
     */
    public void setFcPlcspaMom(int field_134_fcPlcspaMom)
    {
        this.field_134_fcPlcspaMom = field_134_fcPlcspaMom;
    }

    /**
     * Get the lcbPlcspaMom field for the FIB record.
     */
    public int getLcbPlcspaMom()
    {
        return field_135_lcbPlcspaMom;
    }

    /**
     * Set the lcbPlcspaMom field for the FIB record.
     */
    public void setLcbPlcspaMom(int field_135_lcbPlcspaMom)
    {
        this.field_135_lcbPlcspaMom = field_135_lcbPlcspaMom;
    }

    /**
     * Get the fcPlcspaHdr field for the FIB record.
     */
    public int getFcPlcspaHdr()
    {
        return field_136_fcPlcspaHdr;
    }

    /**
     * Set the fcPlcspaHdr field for the FIB record.
     */
    public void setFcPlcspaHdr(int field_136_fcPlcspaHdr)
    {
        this.field_136_fcPlcspaHdr = field_136_fcPlcspaHdr;
    }

    /**
     * Get the lcbPlcspaHdr field for the FIB record.
     */
    public int getLcbPlcspaHdr()
    {
        return field_137_lcbPlcspaHdr;
    }

    /**
     * Set the lcbPlcspaHdr field for the FIB record.
     */
    public void setLcbPlcspaHdr(int field_137_lcbPlcspaHdr)
    {
        this.field_137_lcbPlcspaHdr = field_137_lcbPlcspaHdr;
    }

    /**
     * Get the fcPlcfAtnbkf field for the FIB record.
     */
    public int getFcPlcfAtnbkf()
    {
        return field_138_fcPlcfAtnbkf;
    }

    /**
     * Set the fcPlcfAtnbkf field for the FIB record.
     */
    public void setFcPlcfAtnbkf(int field_138_fcPlcfAtnbkf)
    {
        this.field_138_fcPlcfAtnbkf = field_138_fcPlcfAtnbkf;
    }

    /**
     * Get the lcbPlcfAtnbkf field for the FIB record.
     */
    public int getLcbPlcfAtnbkf()
    {
        return field_139_lcbPlcfAtnbkf;
    }

    /**
     * Set the lcbPlcfAtnbkf field for the FIB record.
     */
    public void setLcbPlcfAtnbkf(int field_139_lcbPlcfAtnbkf)
    {
        this.field_139_lcbPlcfAtnbkf = field_139_lcbPlcfAtnbkf;
    }

    /**
     * Get the fcPlcfAtnbkl field for the FIB record.
     */
    public int getFcPlcfAtnbkl()
    {
        return field_140_fcPlcfAtnbkl;
    }

    /**
     * Set the fcPlcfAtnbkl field for the FIB record.
     */
    public void setFcPlcfAtnbkl(int field_140_fcPlcfAtnbkl)
    {
        this.field_140_fcPlcfAtnbkl = field_140_fcPlcfAtnbkl;
    }

    /**
     * Get the lcbPlcfAtnbkl field for the FIB record.
     */
    public int getLcbPlcfAtnbkl()
    {
        return field_141_lcbPlcfAtnbkl;
    }

    /**
     * Set the lcbPlcfAtnbkl field for the FIB record.
     */
    public void setLcbPlcfAtnbkl(int field_141_lcbPlcfAtnbkl)
    {
        this.field_141_lcbPlcfAtnbkl = field_141_lcbPlcfAtnbkl;
    }

    /**
     * Get the fcPms field for the FIB record.
     */
    public int getFcPms()
    {
        return field_142_fcPms;
    }

    /**
     * Set the fcPms field for the FIB record.
     */
    public void setFcPms(int field_142_fcPms)
    {
        this.field_142_fcPms = field_142_fcPms;
    }

    /**
     * Get the lcbPms field for the FIB record.
     */
    public int getLcbPms()
    {
        return field_143_lcbPms;
    }

    /**
     * Set the lcbPms field for the FIB record.
     */
    public void setLcbPms(int field_143_lcbPms)
    {
        this.field_143_lcbPms = field_143_lcbPms;
    }

    /**
     * Get the fcFormFldSttbs field for the FIB record.
     */
    public int getFcFormFldSttbs()
    {
        return field_144_fcFormFldSttbs;
    }

    /**
     * Set the fcFormFldSttbs field for the FIB record.
     */
    public void setFcFormFldSttbs(int field_144_fcFormFldSttbs)
    {
        this.field_144_fcFormFldSttbs = field_144_fcFormFldSttbs;
    }

    /**
     * Get the lcbFormFldSttbs field for the FIB record.
     */
    public int getLcbFormFldSttbs()
    {
        return field_145_lcbFormFldSttbs;
    }

    /**
     * Set the lcbFormFldSttbs field for the FIB record.
     */
    public void setLcbFormFldSttbs(int field_145_lcbFormFldSttbs)
    {
        this.field_145_lcbFormFldSttbs = field_145_lcbFormFldSttbs;
    }

    /**
     * Get the fcPlcfendRef field for the FIB record.
     */
    public int getFcPlcfendRef()
    {
        return field_146_fcPlcfendRef;
    }

    /**
     * Set the fcPlcfendRef field for the FIB record.
     */
    public void setFcPlcfendRef(int field_146_fcPlcfendRef)
    {
        this.field_146_fcPlcfendRef = field_146_fcPlcfendRef;
    }

    /**
     * Get the lcbPlcfendRef field for the FIB record.
     */
    public int getLcbPlcfendRef()
    {
        return field_147_lcbPlcfendRef;
    }

    /**
     * Set the lcbPlcfendRef field for the FIB record.
     */
    public void setLcbPlcfendRef(int field_147_lcbPlcfendRef)
    {
        this.field_147_lcbPlcfendRef = field_147_lcbPlcfendRef;
    }

    /**
     * Get the fcPlcfendTxt field for the FIB record.
     */
    public int getFcPlcfendTxt()
    {
        return field_148_fcPlcfendTxt;
    }

    /**
     * Set the fcPlcfendTxt field for the FIB record.
     */
    public void setFcPlcfendTxt(int field_148_fcPlcfendTxt)
    {
        this.field_148_fcPlcfendTxt = field_148_fcPlcfendTxt;
    }

    /**
     * Get the lcbPlcfendTxt field for the FIB record.
     */
    public int getLcbPlcfendTxt()
    {
        return field_149_lcbPlcfendTxt;
    }

    /**
     * Set the lcbPlcfendTxt field for the FIB record.
     */
    public void setLcbPlcfendTxt(int field_149_lcbPlcfendTxt)
    {
        this.field_149_lcbPlcfendTxt = field_149_lcbPlcfendTxt;
    }

    /**
     * Get the fcPlcffldEdn field for the FIB record.
     */
    public int getFcPlcffldEdn()
    {
        return field_150_fcPlcffldEdn;
    }

    /**
     * Set the fcPlcffldEdn field for the FIB record.
     */
    public void setFcPlcffldEdn(int field_150_fcPlcffldEdn)
    {
        this.field_150_fcPlcffldEdn = field_150_fcPlcffldEdn;
    }

    /**
     * Get the lcbPlcffldEdn field for the FIB record.
     */
    public int getLcbPlcffldEdn()
    {
        return field_151_lcbPlcffldEdn;
    }

    /**
     * Set the lcbPlcffldEdn field for the FIB record.
     */
    public void setLcbPlcffldEdn(int field_151_lcbPlcffldEdn)
    {
        this.field_151_lcbPlcffldEdn = field_151_lcbPlcffldEdn;
    }

    /**
     * Get the fcPlcfpgdEdn field for the FIB record.
     */
    public int getFcPlcfpgdEdn()
    {
        return field_152_fcPlcfpgdEdn;
    }

    /**
     * Set the fcPlcfpgdEdn field for the FIB record.
     */
    public void setFcPlcfpgdEdn(int field_152_fcPlcfpgdEdn)
    {
        this.field_152_fcPlcfpgdEdn = field_152_fcPlcfpgdEdn;
    }

    /**
     * Get the lcbPlcfpgdEdn field for the FIB record.
     */
    public int getLcbPlcfpgdEdn()
    {
        return field_153_lcbPlcfpgdEdn;
    }

    /**
     * Set the lcbPlcfpgdEdn field for the FIB record.
     */
    public void setLcbPlcfpgdEdn(int field_153_lcbPlcfpgdEdn)
    {
        this.field_153_lcbPlcfpgdEdn = field_153_lcbPlcfpgdEdn;
    }

    /**
     * Get the fcDggInfo field for the FIB record.
     */
    public int getFcDggInfo()
    {
        return field_154_fcDggInfo;
    }

    /**
     * Set the fcDggInfo field for the FIB record.
     */
    public void setFcDggInfo(int field_154_fcDggInfo)
    {
        this.field_154_fcDggInfo = field_154_fcDggInfo;
    }

    /**
     * Get the lcbDggInfo field for the FIB record.
     */
    public int getLcbDggInfo()
    {
        return field_155_lcbDggInfo;
    }

    /**
     * Set the lcbDggInfo field for the FIB record.
     */
    public void setLcbDggInfo(int field_155_lcbDggInfo)
    {
        this.field_155_lcbDggInfo = field_155_lcbDggInfo;
    }

    /**
     * Get the fcSttbfRMark field for the FIB record.
     */
    public int getFcSttbfRMark()
    {
        return field_156_fcSttbfRMark;
    }

    /**
     * Set the fcSttbfRMark field for the FIB record.
     */
    public void setFcSttbfRMark(int field_156_fcSttbfRMark)
    {
        this.field_156_fcSttbfRMark = field_156_fcSttbfRMark;
    }

    /**
     * Get the lcbSttbfRMark field for the FIB record.
     */
    public int getLcbSttbfRMark()
    {
        return field_157_lcbSttbfRMark;
    }

    /**
     * Set the lcbSttbfRMark field for the FIB record.
     */
    public void setLcbSttbfRMark(int field_157_lcbSttbfRMark)
    {
        this.field_157_lcbSttbfRMark = field_157_lcbSttbfRMark;
    }

    /**
     * Get the fcSttbCaption field for the FIB record.
     */
    public int getFcSttbCaption()
    {
        return field_158_fcSttbCaption;
    }

    /**
     * Set the fcSttbCaption field for the FIB record.
     */
    public void setFcSttbCaption(int field_158_fcSttbCaption)
    {
        this.field_158_fcSttbCaption = field_158_fcSttbCaption;
    }

    /**
     * Get the lcbSttbCaption field for the FIB record.
     */
    public int getLcbSttbCaption()
    {
        return field_159_lcbSttbCaption;
    }

    /**
     * Set the lcbSttbCaption field for the FIB record.
     */
    public void setLcbSttbCaption(int field_159_lcbSttbCaption)
    {
        this.field_159_lcbSttbCaption = field_159_lcbSttbCaption;
    }

    /**
     * Get the fcSttbAutoCaption field for the FIB record.
     */
    public int getFcSttbAutoCaption()
    {
        return field_160_fcSttbAutoCaption;
    }

    /**
     * Set the fcSttbAutoCaption field for the FIB record.
     */
    public void setFcSttbAutoCaption(int field_160_fcSttbAutoCaption)
    {
        this.field_160_fcSttbAutoCaption = field_160_fcSttbAutoCaption;
    }

    /**
     * Get the lcbSttbAutoCaption field for the FIB record.
     */
    public int getLcbSttbAutoCaption()
    {
        return field_161_lcbSttbAutoCaption;
    }

    /**
     * Set the lcbSttbAutoCaption field for the FIB record.
     */
    public void setLcbSttbAutoCaption(int field_161_lcbSttbAutoCaption)
    {
        this.field_161_lcbSttbAutoCaption = field_161_lcbSttbAutoCaption;
    }

    /**
     * Get the fcPlcfwkb field for the FIB record.
     */
    public int getFcPlcfwkb()
    {
        return field_162_fcPlcfwkb;
    }

    /**
     * Set the fcPlcfwkb field for the FIB record.
     */
    public void setFcPlcfwkb(int field_162_fcPlcfwkb)
    {
        this.field_162_fcPlcfwkb = field_162_fcPlcfwkb;
    }

    /**
     * Get the lcbPlcfwkb field for the FIB record.
     */
    public int getLcbPlcfwkb()
    {
        return field_163_lcbPlcfwkb;
    }

    /**
     * Set the lcbPlcfwkb field for the FIB record.
     */
    public void setLcbPlcfwkb(int field_163_lcbPlcfwkb)
    {
        this.field_163_lcbPlcfwkb = field_163_lcbPlcfwkb;
    }

    /**
     * Get the fcPlcfspl field for the FIB record.
     */
    public int getFcPlcfspl()
    {
        return field_164_fcPlcfspl;
    }

    /**
     * Set the fcPlcfspl field for the FIB record.
     */
    public void setFcPlcfspl(int field_164_fcPlcfspl)
    {
        this.field_164_fcPlcfspl = field_164_fcPlcfspl;
    }

    /**
     * Get the lcbPlcfspl field for the FIB record.
     */
    public int getLcbPlcfspl()
    {
        return field_165_lcbPlcfspl;
    }

    /**
     * Set the lcbPlcfspl field for the FIB record.
     */
    public void setLcbPlcfspl(int field_165_lcbPlcfspl)
    {
        this.field_165_lcbPlcfspl = field_165_lcbPlcfspl;
    }

    /**
     * Get the fcPlcftxbxTxt field for the FIB record.
     */
    public int getFcPlcftxbxTxt()
    {
        return field_166_fcPlcftxbxTxt;
    }

    /**
     * Set the fcPlcftxbxTxt field for the FIB record.
     */
    public void setFcPlcftxbxTxt(int field_166_fcPlcftxbxTxt)
    {
        this.field_166_fcPlcftxbxTxt = field_166_fcPlcftxbxTxt;
    }

    /**
     * Get the lcbPlcftxbxTxt field for the FIB record.
     */
    public int getLcbPlcftxbxTxt()
    {
        return field_167_lcbPlcftxbxTxt;
    }

    /**
     * Set the lcbPlcftxbxTxt field for the FIB record.
     */
    public void setLcbPlcftxbxTxt(int field_167_lcbPlcftxbxTxt)
    {
        this.field_167_lcbPlcftxbxTxt = field_167_lcbPlcftxbxTxt;
    }

    /**
     * Get the fcPlcffldTxbx field for the FIB record.
     */
    public int getFcPlcffldTxbx()
    {
        return field_168_fcPlcffldTxbx;
    }

    /**
     * Set the fcPlcffldTxbx field for the FIB record.
     */
    public void setFcPlcffldTxbx(int field_168_fcPlcffldTxbx)
    {
        this.field_168_fcPlcffldTxbx = field_168_fcPlcffldTxbx;
    }

    /**
     * Get the lcbPlcffldTxbx field for the FIB record.
     */
    public int getLcbPlcffldTxbx()
    {
        return field_169_lcbPlcffldTxbx;
    }

    /**
     * Set the lcbPlcffldTxbx field for the FIB record.
     */
    public void setLcbPlcffldTxbx(int field_169_lcbPlcffldTxbx)
    {
        this.field_169_lcbPlcffldTxbx = field_169_lcbPlcffldTxbx;
    }

    /**
     * Get the fcPlcfhdrtxbxTxt field for the FIB record.
     */
    public int getFcPlcfhdrtxbxTxt()
    {
        return field_170_fcPlcfhdrtxbxTxt;
    }

    /**
     * Set the fcPlcfhdrtxbxTxt field for the FIB record.
     */
    public void setFcPlcfhdrtxbxTxt(int field_170_fcPlcfhdrtxbxTxt)
    {
        this.field_170_fcPlcfhdrtxbxTxt = field_170_fcPlcfhdrtxbxTxt;
    }

    /**
     * Get the lcbPlcfhdrtxbxTxt field for the FIB record.
     */
    public int getLcbPlcfhdrtxbxTxt()
    {
        return field_171_lcbPlcfhdrtxbxTxt;
    }

    /**
     * Set the lcbPlcfhdrtxbxTxt field for the FIB record.
     */
    public void setLcbPlcfhdrtxbxTxt(int field_171_lcbPlcfhdrtxbxTxt)
    {
        this.field_171_lcbPlcfhdrtxbxTxt = field_171_lcbPlcfhdrtxbxTxt;
    }

    /**
     * Get the fcPlcffldHdrTxbx field for the FIB record.
     */
    public int getFcPlcffldHdrTxbx()
    {
        return field_172_fcPlcffldHdrTxbx;
    }

    /**
     * Set the fcPlcffldHdrTxbx field for the FIB record.
     */
    public void setFcPlcffldHdrTxbx(int field_172_fcPlcffldHdrTxbx)
    {
        this.field_172_fcPlcffldHdrTxbx = field_172_fcPlcffldHdrTxbx;
    }

    /**
     * Get the lcbPlcffldHdrTxbx field for the FIB record.
     */
    public int getLcbPlcffldHdrTxbx()
    {
        return field_173_lcbPlcffldHdrTxbx;
    }

    /**
     * Set the lcbPlcffldHdrTxbx field for the FIB record.
     */
    public void setLcbPlcffldHdrTxbx(int field_173_lcbPlcffldHdrTxbx)
    {
        this.field_173_lcbPlcffldHdrTxbx = field_173_lcbPlcffldHdrTxbx;
    }

    /**
     * Get the fcStwUser field for the FIB record.
     */
    public int getFcStwUser()
    {
        return field_174_fcStwUser;
    }

    /**
     * Set the fcStwUser field for the FIB record.
     */
    public void setFcStwUser(int field_174_fcStwUser)
    {
        this.field_174_fcStwUser = field_174_fcStwUser;
    }

    /**
     * Get the lcbStwUser field for the FIB record.
     */
    public int getLcbStwUser()
    {
        return field_175_lcbStwUser;
    }

    /**
     * Set the lcbStwUser field for the FIB record.
     */
    public void setLcbStwUser(int field_175_lcbStwUser)
    {
        this.field_175_lcbStwUser = field_175_lcbStwUser;
    }

    /**
     * Get the fcSttbttmbd field for the FIB record.
     */
    public int getFcSttbttmbd()
    {
        return field_176_fcSttbttmbd;
    }

    /**
     * Set the fcSttbttmbd field for the FIB record.
     */
    public void setFcSttbttmbd(int field_176_fcSttbttmbd)
    {
        this.field_176_fcSttbttmbd = field_176_fcSttbttmbd;
    }

    /**
     * Get the cbSttbttmbd field for the FIB record.
     */
    public int getCbSttbttmbd()
    {
        return field_177_cbSttbttmbd;
    }

    /**
     * Set the cbSttbttmbd field for the FIB record.
     */
    public void setCbSttbttmbd(int field_177_cbSttbttmbd)
    {
        this.field_177_cbSttbttmbd = field_177_cbSttbttmbd;
    }

    /**
     * Get the fcUnused field for the FIB record.
     */
    public int getFcUnused()
    {
        return field_178_fcUnused;
    }

    /**
     * Set the fcUnused field for the FIB record.
     */
    public void setFcUnused(int field_178_fcUnused)
    {
        this.field_178_fcUnused = field_178_fcUnused;
    }

    /**
     * Get the lcbUnused field for the FIB record.
     */
    public int getLcbUnused()
    {
        return field_179_lcbUnused;
    }

    /**
     * Set the lcbUnused field for the FIB record.
     */
    public void setLcbUnused(int field_179_lcbUnused)
    {
        this.field_179_lcbUnused = field_179_lcbUnused;
    }

    /**
     * Get the fcPgdMother field for the FIB record.
     */
    public int getFcPgdMother()
    {
        return field_180_fcPgdMother;
    }

    /**
     * Set the fcPgdMother field for the FIB record.
     */
    public void setFcPgdMother(int field_180_fcPgdMother)
    {
        this.field_180_fcPgdMother = field_180_fcPgdMother;
    }

    /**
     * Get the lcbPgdMother field for the FIB record.
     */
    public int getLcbPgdMother()
    {
        return field_181_lcbPgdMother;
    }

    /**
     * Set the lcbPgdMother field for the FIB record.
     */
    public void setLcbPgdMother(int field_181_lcbPgdMother)
    {
        this.field_181_lcbPgdMother = field_181_lcbPgdMother;
    }

    /**
     * Get the fcBkdMother field for the FIB record.
     */
    public int getFcBkdMother()
    {
        return field_182_fcBkdMother;
    }

    /**
     * Set the fcBkdMother field for the FIB record.
     */
    public void setFcBkdMother(int field_182_fcBkdMother)
    {
        this.field_182_fcBkdMother = field_182_fcBkdMother;
    }

    /**
     * Get the lcbBkdMother field for the FIB record.
     */
    public int getLcbBkdMother()
    {
        return field_183_lcbBkdMother;
    }

    /**
     * Set the lcbBkdMother field for the FIB record.
     */
    public void setLcbBkdMother(int field_183_lcbBkdMother)
    {
        this.field_183_lcbBkdMother = field_183_lcbBkdMother;
    }

    /**
     * Get the fcPgdFtn field for the FIB record.
     */
    public int getFcPgdFtn()
    {
        return field_184_fcPgdFtn;
    }

    /**
     * Set the fcPgdFtn field for the FIB record.
     */
    public void setFcPgdFtn(int field_184_fcPgdFtn)
    {
        this.field_184_fcPgdFtn = field_184_fcPgdFtn;
    }

    /**
     * Get the lcbPgdFtn field for the FIB record.
     */
    public int getLcbPgdFtn()
    {
        return field_185_lcbPgdFtn;
    }

    /**
     * Set the lcbPgdFtn field for the FIB record.
     */
    public void setLcbPgdFtn(int field_185_lcbPgdFtn)
    {
        this.field_185_lcbPgdFtn = field_185_lcbPgdFtn;
    }

    /**
     * Get the fcBkdFtn field for the FIB record.
     */
    public int getFcBkdFtn()
    {
        return field_186_fcBkdFtn;
    }

    /**
     * Set the fcBkdFtn field for the FIB record.
     */
    public void setFcBkdFtn(int field_186_fcBkdFtn)
    {
        this.field_186_fcBkdFtn = field_186_fcBkdFtn;
    }

    /**
     * Get the lcbBkdFtn field for the FIB record.
     */
    public int getLcbBkdFtn()
    {
        return field_187_lcbBkdFtn;
    }

    /**
     * Set the lcbBkdFtn field for the FIB record.
     */
    public void setLcbBkdFtn(int field_187_lcbBkdFtn)
    {
        this.field_187_lcbBkdFtn = field_187_lcbBkdFtn;
    }

    /**
     * Get the fcPgdEdn field for the FIB record.
     */
    public int getFcPgdEdn()
    {
        return field_188_fcPgdEdn;
    }

    /**
     * Set the fcPgdEdn field for the FIB record.
     */
    public void setFcPgdEdn(int field_188_fcPgdEdn)
    {
        this.field_188_fcPgdEdn = field_188_fcPgdEdn;
    }

    /**
     * Get the lcbPgdEdn field for the FIB record.
     */
    public int getLcbPgdEdn()
    {
        return field_189_lcbPgdEdn;
    }

    /**
     * Set the lcbPgdEdn field for the FIB record.
     */
    public void setLcbPgdEdn(int field_189_lcbPgdEdn)
    {
        this.field_189_lcbPgdEdn = field_189_lcbPgdEdn;
    }

    /**
     * Get the fcBkdEdn field for the FIB record.
     */
    public int getFcBkdEdn()
    {
        return field_190_fcBkdEdn;
    }

    /**
     * Set the fcBkdEdn field for the FIB record.
     */
    public void setFcBkdEdn(int field_190_fcBkdEdn)
    {
        this.field_190_fcBkdEdn = field_190_fcBkdEdn;
    }

    /**
     * Get the lcbBkdEdn field for the FIB record.
     */
    public int getLcbBkdEdn()
    {
        return field_191_lcbBkdEdn;
    }

    /**
     * Set the lcbBkdEdn field for the FIB record.
     */
    public void setLcbBkdEdn(int field_191_lcbBkdEdn)
    {
        this.field_191_lcbBkdEdn = field_191_lcbBkdEdn;
    }

    /**
     * Get the fcSttbfIntlFld field for the FIB record.
     */
    public int getFcSttbfIntlFld()
    {
        return field_192_fcSttbfIntlFld;
    }

    /**
     * Set the fcSttbfIntlFld field for the FIB record.
     */
    public void setFcSttbfIntlFld(int field_192_fcSttbfIntlFld)
    {
        this.field_192_fcSttbfIntlFld = field_192_fcSttbfIntlFld;
    }

    /**
     * Get the lcbSttbfIntlFld field for the FIB record.
     */
    public int getLcbSttbfIntlFld()
    {
        return field_193_lcbSttbfIntlFld;
    }

    /**
     * Set the lcbSttbfIntlFld field for the FIB record.
     */
    public void setLcbSttbfIntlFld(int field_193_lcbSttbfIntlFld)
    {
        this.field_193_lcbSttbfIntlFld = field_193_lcbSttbfIntlFld;
    }

    /**
     * Get the fcRouteSlip field for the FIB record.
     */
    public int getFcRouteSlip()
    {
        return field_194_fcRouteSlip;
    }

    /**
     * Set the fcRouteSlip field for the FIB record.
     */
    public void setFcRouteSlip(int field_194_fcRouteSlip)
    {
        this.field_194_fcRouteSlip = field_194_fcRouteSlip;
    }

    /**
     * Get the lcbRouteSlip field for the FIB record.
     */
    public int getLcbRouteSlip()
    {
        return field_195_lcbRouteSlip;
    }

    /**
     * Set the lcbRouteSlip field for the FIB record.
     */
    public void setLcbRouteSlip(int field_195_lcbRouteSlip)
    {
        this.field_195_lcbRouteSlip = field_195_lcbRouteSlip;
    }

    /**
     * Get the fcSttbSavedBy field for the FIB record.
     */
    public int getFcSttbSavedBy()
    {
        return field_196_fcSttbSavedBy;
    }

    /**
     * Set the fcSttbSavedBy field for the FIB record.
     */
    public void setFcSttbSavedBy(int field_196_fcSttbSavedBy)
    {
        this.field_196_fcSttbSavedBy = field_196_fcSttbSavedBy;
    }

    /**
     * Get the lcbSttbSavedBy field for the FIB record.
     */
    public int getLcbSttbSavedBy()
    {
        return field_197_lcbSttbSavedBy;
    }

    /**
     * Set the lcbSttbSavedBy field for the FIB record.
     */
    public void setLcbSttbSavedBy(int field_197_lcbSttbSavedBy)
    {
        this.field_197_lcbSttbSavedBy = field_197_lcbSttbSavedBy;
    }

    /**
     * Get the fcSttbFnm field for the FIB record.
     */
    public int getFcSttbFnm()
    {
        return field_198_fcSttbFnm;
    }

    /**
     * Set the fcSttbFnm field for the FIB record.
     */
    public void setFcSttbFnm(int field_198_fcSttbFnm)
    {
        this.field_198_fcSttbFnm = field_198_fcSttbFnm;
    }

    /**
     * Get the lcbSttbFnm field for the FIB record.
     */
    public int getLcbSttbFnm()
    {
        return field_199_lcbSttbFnm;
    }

    /**
     * Set the lcbSttbFnm field for the FIB record.
     */
    public void setLcbSttbFnm(int field_199_lcbSttbFnm)
    {
        this.field_199_lcbSttbFnm = field_199_lcbSttbFnm;
    }

    /**
     * Get the fcPlcfLst field for the FIB record.
     */
    public int getFcPlcfLst()
    {
        return field_200_fcPlcfLst;
    }

    /**
     * Set the fcPlcfLst field for the FIB record.
     */
    public void setFcPlcfLst(int field_200_fcPlcfLst)
    {
        this.field_200_fcPlcfLst = field_200_fcPlcfLst;
    }

    /**
     * Get the lcbPlcfLst field for the FIB record.
     */
    public int getLcbPlcfLst()
    {
        return field_201_lcbPlcfLst;
    }

    /**
     * Set the lcbPlcfLst field for the FIB record.
     */
    public void setLcbPlcfLst(int field_201_lcbPlcfLst)
    {
        this.field_201_lcbPlcfLst = field_201_lcbPlcfLst;
    }

    /**
     * Get the fcPlfLfo field for the FIB record.
     */
    public int getFcPlfLfo()
    {
        return field_202_fcPlfLfo;
    }

    /**
     * Set the fcPlfLfo field for the FIB record.
     */
    public void setFcPlfLfo(int field_202_fcPlfLfo)
    {
        this.field_202_fcPlfLfo = field_202_fcPlfLfo;
    }

    /**
     * Get the lcbPlfLfo field for the FIB record.
     */
    public int getLcbPlfLfo()
    {
        return field_203_lcbPlfLfo;
    }

    /**
     * Set the lcbPlfLfo field for the FIB record.
     */
    public void setLcbPlfLfo(int field_203_lcbPlfLfo)
    {
        this.field_203_lcbPlfLfo = field_203_lcbPlfLfo;
    }

    /**
     * Get the fcPlcftxbxBkd field for the FIB record.
     */
    public int getFcPlcftxbxBkd()
    {
        return field_204_fcPlcftxbxBkd;
    }

    /**
     * Set the fcPlcftxbxBkd field for the FIB record.
     */
    public void setFcPlcftxbxBkd(int field_204_fcPlcftxbxBkd)
    {
        this.field_204_fcPlcftxbxBkd = field_204_fcPlcftxbxBkd;
    }

    /**
     * Get the lcbPlcftxbxBkd field for the FIB record.
     */
    public int getLcbPlcftxbxBkd()
    {
        return field_205_lcbPlcftxbxBkd;
    }

    /**
     * Set the lcbPlcftxbxBkd field for the FIB record.
     */
    public void setLcbPlcftxbxBkd(int field_205_lcbPlcftxbxBkd)
    {
        this.field_205_lcbPlcftxbxBkd = field_205_lcbPlcftxbxBkd;
    }

    /**
     * Get the fcPlcftxbxHdrBkd field for the FIB record.
     */
    public int getFcPlcftxbxHdrBkd()
    {
        return field_206_fcPlcftxbxHdrBkd;
    }

    /**
     * Set the fcPlcftxbxHdrBkd field for the FIB record.
     */
    public void setFcPlcftxbxHdrBkd(int field_206_fcPlcftxbxHdrBkd)
    {
        this.field_206_fcPlcftxbxHdrBkd = field_206_fcPlcftxbxHdrBkd;
    }

    /**
     * Get the lcbPlcftxbxHdrBkd field for the FIB record.
     */
    public int getLcbPlcftxbxHdrBkd()
    {
        return field_207_lcbPlcftxbxHdrBkd;
    }

    /**
     * Set the lcbPlcftxbxHdrBkd field for the FIB record.
     */
    public void setLcbPlcftxbxHdrBkd(int field_207_lcbPlcftxbxHdrBkd)
    {
        this.field_207_lcbPlcftxbxHdrBkd = field_207_lcbPlcftxbxHdrBkd;
    }

    /**
     * Get the fcDocUndo field for the FIB record.
     */
    public int getFcDocUndo()
    {
        return field_208_fcDocUndo;
    }

    /**
     * Set the fcDocUndo field for the FIB record.
     */
    public void setFcDocUndo(int field_208_fcDocUndo)
    {
        this.field_208_fcDocUndo = field_208_fcDocUndo;
    }

    /**
     * Get the lcbDocUndo field for the FIB record.
     */
    public int getLcbDocUndo()
    {
        return field_209_lcbDocUndo;
    }

    /**
     * Set the lcbDocUndo field for the FIB record.
     */
    public void setLcbDocUndo(int field_209_lcbDocUndo)
    {
        this.field_209_lcbDocUndo = field_209_lcbDocUndo;
    }

    /**
     * Get the fcRgbuse field for the FIB record.
     */
    public int getFcRgbuse()
    {
        return field_210_fcRgbuse;
    }

    /**
     * Set the fcRgbuse field for the FIB record.
     */
    public void setFcRgbuse(int field_210_fcRgbuse)
    {
        this.field_210_fcRgbuse = field_210_fcRgbuse;
    }

    /**
     * Get the lcbRgbuse field for the FIB record.
     */
    public int getLcbRgbuse()
    {
        return field_211_lcbRgbuse;
    }

    /**
     * Set the lcbRgbuse field for the FIB record.
     */
    public void setLcbRgbuse(int field_211_lcbRgbuse)
    {
        this.field_211_lcbRgbuse = field_211_lcbRgbuse;
    }

    /**
     * Get the fcUsp field for the FIB record.
     */
    public int getFcUsp()
    {
        return field_212_fcUsp;
    }

    /**
     * Set the fcUsp field for the FIB record.
     */
    public void setFcUsp(int field_212_fcUsp)
    {
        this.field_212_fcUsp = field_212_fcUsp;
    }

    /**
     * Get the lcbUsp field for the FIB record.
     */
    public int getLcbUsp()
    {
        return field_213_lcbUsp;
    }

    /**
     * Set the lcbUsp field for the FIB record.
     */
    public void setLcbUsp(int field_213_lcbUsp)
    {
        this.field_213_lcbUsp = field_213_lcbUsp;
    }

    /**
     * Get the fcUskf field for the FIB record.
     */
    public int getFcUskf()
    {
        return field_214_fcUskf;
    }

    /**
     * Set the fcUskf field for the FIB record.
     */
    public void setFcUskf(int field_214_fcUskf)
    {
        this.field_214_fcUskf = field_214_fcUskf;
    }

    /**
     * Get the lcbUskf field for the FIB record.
     */
    public int getLcbUskf()
    {
        return field_215_lcbUskf;
    }

    /**
     * Set the lcbUskf field for the FIB record.
     */
    public void setLcbUskf(int field_215_lcbUskf)
    {
        this.field_215_lcbUskf = field_215_lcbUskf;
    }

    /**
     * Get the fcPlcupcRgbuse field for the FIB record.
     */
    public int getFcPlcupcRgbuse()
    {
        return field_216_fcPlcupcRgbuse;
    }

    /**
     * Set the fcPlcupcRgbuse field for the FIB record.
     */
    public void setFcPlcupcRgbuse(int field_216_fcPlcupcRgbuse)
    {
        this.field_216_fcPlcupcRgbuse = field_216_fcPlcupcRgbuse;
    }

    /**
     * Get the lcbPlcupcRgbuse field for the FIB record.
     */
    public int getLcbPlcupcRgbuse()
    {
        return field_217_lcbPlcupcRgbuse;
    }

    /**
     * Set the lcbPlcupcRgbuse field for the FIB record.
     */
    public void setLcbPlcupcRgbuse(int field_217_lcbPlcupcRgbuse)
    {
        this.field_217_lcbPlcupcRgbuse = field_217_lcbPlcupcRgbuse;
    }

    /**
     * Get the fcPlcupcUsp field for the FIB record.
     */
    public int getFcPlcupcUsp()
    {
        return field_218_fcPlcupcUsp;
    }

    /**
     * Set the fcPlcupcUsp field for the FIB record.
     */
    public void setFcPlcupcUsp(int field_218_fcPlcupcUsp)
    {
        this.field_218_fcPlcupcUsp = field_218_fcPlcupcUsp;
    }

    /**
     * Get the lcbPlcupcUsp field for the FIB record.
     */
    public int getLcbPlcupcUsp()
    {
        return field_219_lcbPlcupcUsp;
    }

    /**
     * Set the lcbPlcupcUsp field for the FIB record.
     */
    public void setLcbPlcupcUsp(int field_219_lcbPlcupcUsp)
    {
        this.field_219_lcbPlcupcUsp = field_219_lcbPlcupcUsp;
    }

    /**
     * Get the fcSttbGlsyStyle field for the FIB record.
     */
    public int getFcSttbGlsyStyle()
    {
        return field_220_fcSttbGlsyStyle;
    }

    /**
     * Set the fcSttbGlsyStyle field for the FIB record.
     */
    public void setFcSttbGlsyStyle(int field_220_fcSttbGlsyStyle)
    {
        this.field_220_fcSttbGlsyStyle = field_220_fcSttbGlsyStyle;
    }

    /**
     * Get the lcbSttbGlsyStyle field for the FIB record.
     */
    public int getLcbSttbGlsyStyle()
    {
        return field_221_lcbSttbGlsyStyle;
    }

    /**
     * Set the lcbSttbGlsyStyle field for the FIB record.
     */
    public void setLcbSttbGlsyStyle(int field_221_lcbSttbGlsyStyle)
    {
        this.field_221_lcbSttbGlsyStyle = field_221_lcbSttbGlsyStyle;
    }

    /**
     * Get the fcPlgosl field for the FIB record.
     */
    public int getFcPlgosl()
    {
        return field_222_fcPlgosl;
    }

    /**
     * Set the fcPlgosl field for the FIB record.
     */
    public void setFcPlgosl(int field_222_fcPlgosl)
    {
        this.field_222_fcPlgosl = field_222_fcPlgosl;
    }

    /**
     * Get the lcbPlgosl field for the FIB record.
     */
    public int getLcbPlgosl()
    {
        return field_223_lcbPlgosl;
    }

    /**
     * Set the lcbPlgosl field for the FIB record.
     */
    public void setLcbPlgosl(int field_223_lcbPlgosl)
    {
        this.field_223_lcbPlgosl = field_223_lcbPlgosl;
    }

    /**
     * Get the fcPlcocx field for the FIB record.
     */
    public int getFcPlcocx()
    {
        return field_224_fcPlcocx;
    }

    /**
     * Set the fcPlcocx field for the FIB record.
     */
    public void setFcPlcocx(int field_224_fcPlcocx)
    {
        this.field_224_fcPlcocx = field_224_fcPlcocx;
    }

    /**
     * Get the lcbPlcocx field for the FIB record.
     */
    public int getLcbPlcocx()
    {
        return field_225_lcbPlcocx;
    }

    /**
     * Set the lcbPlcocx field for the FIB record.
     */
    public void setLcbPlcocx(int field_225_lcbPlcocx)
    {
        this.field_225_lcbPlcocx = field_225_lcbPlcocx;
    }

    /**
     * Get the fcPlcfbteLvc field for the FIB record.
     */
    public int getFcPlcfbteLvc()
    {
        return field_226_fcPlcfbteLvc;
    }

    /**
     * Set the fcPlcfbteLvc field for the FIB record.
     */
    public void setFcPlcfbteLvc(int field_226_fcPlcfbteLvc)
    {
        this.field_226_fcPlcfbteLvc = field_226_fcPlcfbteLvc;
    }

    /**
     * Get the lcbPlcfbteLvc field for the FIB record.
     */
    public int getLcbPlcfbteLvc()
    {
        return field_227_lcbPlcfbteLvc;
    }

    /**
     * Set the lcbPlcfbteLvc field for the FIB record.
     */
    public void setLcbPlcfbteLvc(int field_227_lcbPlcfbteLvc)
    {
        this.field_227_lcbPlcfbteLvc = field_227_lcbPlcfbteLvc;
    }

    /**
     * Get the dwLowDateTime field for the FIB record.
     */
    public int getDwLowDateTime()
    {
        return field_228_dwLowDateTime;
    }

    /**
     * Set the dwLowDateTime field for the FIB record.
     */
    public void setDwLowDateTime(int field_228_dwLowDateTime)
    {
        this.field_228_dwLowDateTime = field_228_dwLowDateTime;
    }

    /**
     * Get the dwHighDateTime field for the FIB record.
     */
    public int getDwHighDateTime()
    {
        return field_229_dwHighDateTime;
    }

    /**
     * Set the dwHighDateTime field for the FIB record.
     */
    public void setDwHighDateTime(int field_229_dwHighDateTime)
    {
        this.field_229_dwHighDateTime = field_229_dwHighDateTime;
    }

    /**
     * Get the fcPlcflvc field for the FIB record.
     */
    public int getFcPlcflvc()
    {
        return field_230_fcPlcflvc;
    }

    /**
     * Set the fcPlcflvc field for the FIB record.
     */
    public void setFcPlcflvc(int field_230_fcPlcflvc)
    {
        this.field_230_fcPlcflvc = field_230_fcPlcflvc;
    }

    /**
     * Get the lcbPlcflvc field for the FIB record.
     */
    public int getLcbPlcflvc()
    {
        return field_231_lcbPlcflvc;
    }

    /**
     * Set the lcbPlcflvc field for the FIB record.
     */
    public void setLcbPlcflvc(int field_231_lcbPlcflvc)
    {
        this.field_231_lcbPlcflvc = field_231_lcbPlcflvc;
    }

    /**
     * Get the fcPlcasumy field for the FIB record.
     */
    public int getFcPlcasumy()
    {
        return field_232_fcPlcasumy;
    }

    /**
     * Set the fcPlcasumy field for the FIB record.
     */
    public void setFcPlcasumy(int field_232_fcPlcasumy)
    {
        this.field_232_fcPlcasumy = field_232_fcPlcasumy;
    }

    /**
     * Get the lcbPlcasumy field for the FIB record.
     */
    public int getLcbPlcasumy()
    {
        return field_233_lcbPlcasumy;
    }

    /**
     * Set the lcbPlcasumy field for the FIB record.
     */
    public void setLcbPlcasumy(int field_233_lcbPlcasumy)
    {
        this.field_233_lcbPlcasumy = field_233_lcbPlcasumy;
    }

    /**
     * Get the fcPlcfgram field for the FIB record.
     */
    public int getFcPlcfgram()
    {
        return field_234_fcPlcfgram;
    }

    /**
     * Set the fcPlcfgram field for the FIB record.
     */
    public void setFcPlcfgram(int field_234_fcPlcfgram)
    {
        this.field_234_fcPlcfgram = field_234_fcPlcfgram;
    }

    /**
     * Get the lcbPlcfgram field for the FIB record.
     */
    public int getLcbPlcfgram()
    {
        return field_235_lcbPlcfgram;
    }

    /**
     * Set the lcbPlcfgram field for the FIB record.
     */
    public void setLcbPlcfgram(int field_235_lcbPlcfgram)
    {
        this.field_235_lcbPlcfgram = field_235_lcbPlcfgram;
    }

    /**
     * Get the fcSttbListNames field for the FIB record.
     */
    public int getFcSttbListNames()
    {
        return field_236_fcSttbListNames;
    }

    /**
     * Set the fcSttbListNames field for the FIB record.
     */
    public void setFcSttbListNames(int field_236_fcSttbListNames)
    {
        this.field_236_fcSttbListNames = field_236_fcSttbListNames;
    }

    /**
     * Get the lcbSttbListNames field for the FIB record.
     */
    public int getLcbSttbListNames()
    {
        return field_237_lcbSttbListNames;
    }

    /**
     * Set the lcbSttbListNames field for the FIB record.
     */
    public void setLcbSttbListNames(int field_237_lcbSttbListNames)
    {
        this.field_237_lcbSttbListNames = field_237_lcbSttbListNames;
    }

    /**
     * Get the fcSttbfUssr field for the FIB record.
     */
    public int getFcSttbfUssr()
    {
        return field_238_fcSttbfUssr;
    }

    /**
     * Set the fcSttbfUssr field for the FIB record.
     */
    public void setFcSttbfUssr(int field_238_fcSttbfUssr)
    {
        this.field_238_fcSttbfUssr = field_238_fcSttbfUssr;
    }

    /**
     * Get the lcbSttbfUssr field for the FIB record.
     */
    public int getLcbSttbfUssr()
    {
        return field_239_lcbSttbfUssr;
    }

    /**
     * Set the lcbSttbfUssr field for the FIB record.
     */
    public void setLcbSttbfUssr(int field_239_lcbSttbfUssr)
    {
        this.field_239_lcbSttbfUssr = field_239_lcbSttbfUssr;
    }

    /**
     * Sets the fDot field value.
     *
     */
    public void setFDot(boolean value)
    {
        field_6_options = (short)fDot.setBoolean(field_6_options, value);


    }

    /**
     *
     * @return  the fDot field value.
     */
    public boolean isFDot()
    {
        return fDot.isSet(field_6_options);

    }

    /**
     * Sets the fGlsy field value.
     *
     */
    public void setFGlsy(boolean value)
    {
        field_6_options = (short)fGlsy.setBoolean(field_6_options, value);


    }

    /**
     *
     * @return  the fGlsy field value.
     */
    public boolean isFGlsy()
    {
        return fGlsy.isSet(field_6_options);

    }

    /**
     * Sets the fComplex field value.
     *
     */
    public void setFComplex(boolean value)
    {
        field_6_options = (short)fComplex.setBoolean(field_6_options, value);


    }

    /**
     *
     * @return  the fComplex field value.
     */
    public boolean isFComplex()
    {
        return fComplex.isSet(field_6_options);

    }

    /**
     * Sets the fHasPic field value.
     *
     */
    public void setFHasPic(boolean value)
    {
        field_6_options = (short)fHasPic.setBoolean(field_6_options, value);


    }

    /**
     *
     * @return  the fHasPic field value.
     */
    public boolean isFHasPic()
    {
        return fHasPic.isSet(field_6_options);

    }

    /**
     * Sets the cQuickSaves field value.
     *
     */
    public void setCQuickSaves(byte value)
    {
        field_6_options = (short)cQuickSaves.setValue(field_6_options, value);


    }

    /**
     *
     * @return  the cQuickSaves field value.
     */
    public byte getCQuickSaves()
    {
        return ( byte )cQuickSaves.getValue(field_6_options);

    }

    /**
     * Sets the fEncrypted field value.
     *
     */
    public void setFEncrypted(boolean value)
    {
        field_6_options = (short)fEncrypted.setBoolean(field_6_options, value);


    }

    /**
     *
     * @return  the fEncrypted field value.
     */
    public boolean isFEncrypted()
    {
        return fEncrypted.isSet(field_6_options);

    }

    /**
     * Sets the fWhichTblStm field value.
     *
     */
    public void setFWhichTblStm(boolean value)
    {
        field_6_options = (short)fWhichTblStm.setBoolean(field_6_options, value);


    }

    /**
     *
     * @return  the fWhichTblStm field value.
     */
    public boolean isFWhichTblStm()
    {
        return fWhichTblStm.isSet(field_6_options);

    }

    /**
     * Sets the fReadOnlyRecommended field value.
     *
     */
    public void setFReadOnlyRecommended(boolean value)
    {
        field_6_options = (short)fReadOnlyRecommended.setBoolean(field_6_options, value);


    }

    /**
     *
     * @return  the fReadOnlyRecommended field value.
     */
    public boolean isFReadOnlyRecommended()
    {
        return fReadOnlyRecommended.isSet(field_6_options);

    }

    /**
     * Sets the fWriteReservation field value.
     *
     */
    public void setFWriteReservation(boolean value)
    {
        field_6_options = (short)fWriteReservation.setBoolean(field_6_options, value);


    }

    /**
     *
     * @return  the fWriteReservation field value.
     */
    public boolean isFWriteReservation()
    {
        return fWriteReservation.isSet(field_6_options);

    }

    /**
     * Sets the fExtChar field value.
     *
     */
    public void setFExtChar(boolean value)
    {
        field_6_options = (short)fExtChar.setBoolean(field_6_options, value);


    }

    /**
     *
     * @return  the fExtChar field value.
     */
    public boolean isFExtChar()
    {
        return fExtChar.isSet(field_6_options);

    }

    /**
     * Sets the fLoadOverride field value.
     *
     */
    public void setFLoadOverride(boolean value)
    {
        field_6_options = (short)fLoadOverride.setBoolean(field_6_options, value);


    }

    /**
     *
     * @return  the fLoadOverride field value.
     */
    public boolean isFLoadOverride()
    {
        return fLoadOverride.isSet(field_6_options);

    }

    /**
     * Sets the fFarEast field value.
     *
     */
    public void setFFarEast(boolean value)
    {
        field_6_options = (short)fFarEast.setBoolean(field_6_options, value);


    }

    /**
     *
     * @return  the fFarEast field value.
     */
    public boolean isFFarEast()
    {
        return fFarEast.isSet(field_6_options);

    }

    /**
     * Sets the fCrypto field value.
     *
     */
    public void setFCrypto(boolean value)
    {
        field_6_options = (short)fCrypto.setBoolean(field_6_options, value);


    }

    /**
     *
     * @return  the fCrypto field value.
     */
    public boolean isFCrypto()
    {
        return fCrypto.isSet(field_6_options);

    }

    /**
     * Sets the fMac field value.
     *
     */
    public void setFMac(boolean value)
    {
        field_10_history = (short)fMac.setBoolean(field_10_history, value);


    }

    /**
     *
     * @return  the fMac field value.
     */
    public boolean isFMac()
    {
        return fMac.isSet(field_10_history);

    }

    /**
     * Sets the fEmptySpecial field value.
     *
     */
    public void setFEmptySpecial(boolean value)
    {
        field_10_history = (short)fEmptySpecial.setBoolean(field_10_history, value);


    }

    /**
     *
     * @return  the fEmptySpecial field value.
     */
    public boolean isFEmptySpecial()
    {
        return fEmptySpecial.isSet(field_10_history);

    }

    /**
     * Sets the fLoadOverridePage field value.
     *
     */
    public void setFLoadOverridePage(boolean value)
    {
        field_10_history = (short)fLoadOverridePage.setBoolean(field_10_history, value);


    }

    /**
     *
     * @return  the fLoadOverridePage field value.
     */
    public boolean isFLoadOverridePage()
    {
        return fLoadOverridePage.isSet(field_10_history);

    }

    /**
     * Sets the fFutureSavedUndo field value.
     *
     */
    public void setFFutureSavedUndo(boolean value)
    {
        field_10_history = (short)fFutureSavedUndo.setBoolean(field_10_history, value);


    }

    /**
     *
     * @return  the fFutureSavedUndo field value.
     */
    public boolean isFFutureSavedUndo()
    {
        return fFutureSavedUndo.isSet(field_10_history);

    }

    /**
     * Sets the fWord97Saved field value.
     *
     */
    public void setFWord97Saved(boolean value)
    {
        field_10_history = (short)fWord97Saved.setBoolean(field_10_history, value);


    }

    /**
     *
     * @return  the fWord97Saved field value.
     */
    public boolean isFWord97Saved()
    {
        return fWord97Saved.isSet(field_10_history);

    }

    /**
     * Sets the fSpare0 field value.
     *
     */
    public void setFSpare0(byte value)
    {
        field_10_history = (short)fSpare0.setValue(field_10_history, value);


    }

    /**
     *
     * @return  the fSpare0 field value.
     */
    public byte getFSpare0()
    {
        return ( byte )fSpare0.getValue(field_10_history);

    }


}  // END OF CLASS




