
/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */


package org.apache.poi.hdf.model.hdftypes.definitions;



import org.apache.poi.util.BitField;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.StringUtil;
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

    private  short      field_1_id;
    private  short      field_2_version;
    private  short      field_3_productVersion;
    private  short      field_4_languageStamp;
    private  short      field_5_unknown0;
    private  short      field_6_options;
    private BitField   template                                   = new BitField(0x0001);
    private BitField   glossary                                   = new BitField(0x0002);
    private BitField   quicksave                                  = new BitField(0x0004);
    private BitField   haspictr                                   = new BitField(0x0008);
    private BitField   nquicksaves                                = new BitField(0x00F0);
    private BitField   encrypted                                  = new BitField(0x0100);
    private BitField   tabletype                                  = new BitField(0x0200);
    private BitField   readonly                                   = new BitField(0x0400);
    private BitField   writeReservation                           = new BitField(0x0800);
    private BitField   extendedCharacter                          = new BitField(0x1000);
    private BitField   loadOverride                               = new BitField(0x2000);
    private BitField   farEast                                    = new BitField(0x4000);
    private BitField   crypto                                     = new BitField(0x8000);
    private  short      field_7_minversion;
    private  short      field_8_encryptedKey;
    private  short      field_9_environment;
    private  short      field_10_history;
    private BitField   historyMac                                 = new BitField(0x0001);
    private BitField   emptySpecial                               = new BitField(0x0002);
    private BitField   loadOverrideHist                           = new BitField(0x0004);
    private BitField   featureUndo                                = new BitField(0x0008);
    private BitField   v97Saved                                   = new BitField(0x0010);
    private BitField   spare                                      = new BitField(0x00FE);
    private  short      field_11_defaultCharset;
    private  short      field_12_defaultExtcharset;
    private  int        field_13_offsetFirstChar;
    private  int        field_14_offsetLastChar;
    private  short      field_15_countShorts;
    private  short      field_16_creatorIdOrBegShorts;
    private  short      field_17_revisorId;
    private  short      field_18_creatorPrivate;
    private  short      field_19_revisorPrivate;
    private  short      field_20_unused1;
    private  short      field_21_unused2;
    private  short      field_22_unused3;
    private  short      field_23_unused4;
    private  short      field_24_unused5;
    private  short      field_25_unused6;
    private  short      field_26_unused7;
    private  short      field_27_unused8;
    private  short      field_28_unused9;
    private  short      field_29_fareastid;
    private  short      field_30_countints;
    private  int        field_31_lastByteOrBegInts;
    private  int        field_32_creatorBuildDate;
    private  int        field_33_revisorBuildDate;
    private  int        field_34_mainStreamlen;
    private  int        field_35_footnoteStreamlen;
    private  int        field_36_headerStreamlen;
    private  int        field_37_macroStreamlen;
    private  int        field_38_annotationStreamlen;
    private  int        field_39_endnoteStreamlen;
    private  int        field_40_textboxStreamlen;
    private  int        field_41_headboxStreamlen;
    private  int        field_42_ptrToPlcListChp;
    private  int        field_43_firstChp;
    private  int        field_44_countChps;
    private  int        field_45_ptrToPlcListPap;
    private  int        field_46_firstPap;
    private  int        field_47_countPaps;
    private  int        field_48_ptrToPlcListLvc;
    private  int        field_49_firstLvc;
    private  int        field_50_countLvc;
    private  int        field_51_unknown1;
    private  int        field_52_unknown2;
    private  short      field_53_lcbArraySize;
    private  int        field_54_originalStylesheetOffset;
    private  int        field_55_originalStylesheetSize;
    private  int        field_56_stylesheetOffset;
    private  int        field_57_stylesheetSize;
    private  int        field_58_footnoteRefOffset;
    private  int        field_59_footnoteRefSize;
    private  int        field_60_plcOffset;
    private  int        field_61_plcSize;
    private  int        field_62_annotationRefOffset;
    private  int        field_63_annotationRefSize;
    private  int        field_64_annotationPlcOffset;
    private  int        field_65_annotationPlcSize;
    private  int        field_66_sectionPlcOffset;
    private  int        field_67_sectionPlcSize;
    private  int        field_68_unusedA;
    private  int        field_69_unusedB;
    private  int        field_70_pheplcOffset;
    private  int        field_71_pheplcSize;
    private  int        field_72_glossarySTOffset;
    private  int        field_73_glossarySTSize;
    private  int        field_74_glossaryPLCOffset;
    private  int        field_75_glossaryPLCSize;
    private  int        field_76_headerPLCOffset;
    private  int        field_77_headerPLCSize;
    private  int        field_78_chp_bin_table_offset;
    private  int        field_79_chp_bin_table_size;
    private  int        field_80_pap_bin_table_offset;
    private  int        field_81_pap_bin_table_size;
    private  int        field_82_sea_bin_table_offset;
    private  int        field_83_sea_bin_table_size;
    private  int        field_84_fonts_bin_table_offset;
    private  int        field_85_fonts_bin_table_size;
    private  int        field_86_main_fields_offset;
    private  int        field_87_main_fields_size;
    private  int        field_88_header_fields_offset;
    private  int        field_89_header_fields_size;
    private  int        field_90_footnote_fields_offset;
    private  int        field_91_footnote_fields_size;
    private  int        field_92_ann_fields_offset;
    private  int        field_93_ann_fields_size;
    private  int        field_94_unusedC;
    private  int        field_95_unusedD;
    private  int        field_96_bookmark_names_offset;
    private  int        field_97_bookmark_names_size;
    private  int        field_98_bookmark_offsets_offset;
    private  int        field_99_bookmark_offsets_size;
    private  int        field_100_macros_offset;
    private  int        field_101_macros_size;
    private  int        field_102_unusedE;
    private  int        field_103_unusedF;
    private  int        field_104_unused10;
    private  int        field_105_unused11;
    private  int        field_106_printerOffset;
    private  int        field_107_printerSize;
    private  int        field_108_printerPortraitOffset;
    private  int        field_109_printerPortraitSize;
    private  int        field_110_printerLandscapeOffset;
    private  int        field_111_printerLandscapeSize;
    private  int        field_112_wssOffset;
    private  int        field_113_wssSize;
    private  int        field_114_DOPOffset;
    private  int        field_115_DOPSize;
    private  int        field_116_sttbfassoc_offset;
    private  int        field_117_sttbfassoc_size;
    private  int        field_118_textPieceTableOffset;
    private  int        field_119_textPieceTableSize;
    private  int        field_120_unused12;
    private  int        field_121_unused13;
    private  int        field_122_offsetAutosaveSource;
    private  int        field_123_countAutosaveSource;
    private  int        field_124_offsetGrpXstAtnOwners;
    private  int        field_125_countGrpXstAtnOwners;
    private  int        field_126_offsetSttbfAtnbkmk;
    private  int        field_127_lengthSttbfAtnbkmk;
    private  int        field_128_unused14;
    private  int        field_129_unused15;
    private  int        field_130_unused16;
    private  int        field_131_unused17;
    private  int        field_132_offsetPlcspaMom;
    private  int        field_133_lengthPlcspaMom;
    private  int        field_134_offsetPlcspaHdr;
    private  int        field_135_lengthPlcspaHdr;
    private  int        field_136_lengthPlcfAnnBkmrkFirst;
    private  int        field_137_offsetPlcfAnnBkmrkFirst;
    private  int        field_138_lengthPlcfAnnBkarkLast;
    private  int        field_139_PlcfAtnbkl;
    private  int        field_140_fcPms;
    private  int        field_141_lcbPms;
    private  int        field_142_fcFormFldSttbs;
    private  int        field_143_lcbFormFldSttbs;
    private  int        field_144_fcPlcfendRef;
    private  int        field_145_lcbPlcfendRef;
    private  int        field_146_fcPlcfendTxt;
    private  int        field_147_lcbPlcfendTxt;
    private  int        field_148_fcPlcffldEdn;
    private  int        field_149_lcbPlcffldEdn;
    private  int        field_150_fcPlcfpgdEdn;
    private  int        field_151_lcbPlcfpgdEdn;
    private  int        field_152_fcDggInfo;
    private  int        field_153_lcbDggInfo;
    private  int        field_154_fcSttbfRMark;
    private  int        field_155_lcbSttbfRMark;
    private  int        field_156_fcSttbCaption;
    private  int        field_157_lcbSttbCaption;
    private  int        field_158_fcSttbAutoCaption;
    private  int        field_159_lcbSttbAutoCaption;
    private  int        field_160_fcPlcfwkb;
    private  int        field_161_lcbPlcfwkb;
    private  int        field_162_fcPlcfsplfcPlcfspl;
    private  int        field_163_lcbPlcfspl;
    private  int        field_164_fcPlcftxbxTxt;
    private  int        field_165_lcbPlcftxbxTxt;
    private  int        field_166_fcPlcffldTxbx;
    private  int        field_167_lcbPlcffldTxbx;
    private  int        field_168_fcPlcfhdrtxbxTxt;
    private  int        field_169_lcbPlcfhdrtxbxTxt;
    private  int        field_170_fcPlcffldHdrTxbx;
    private  int        field_171_lcbPlcffldHdrTxbx;
    private  int        field_172_fcStwUser;
    private  int        field_173_lcbStwUser;
    private  int        field_174_fcSttbttmbd;
    private  int        field_175_cbSttbttmbd;
    private  int        field_176_fcUnused;
    private  int        field_177_lcbUnused;
    private  int        field_178_rgpgdbkd;
    private  int        field_179_fcPgdMother;
    private  int        field_180_lcbPgdMother;
    private  int        field_181_fcBkdMother;
    private  int        field_182_lcbBkdMother;
    private  int        field_183_fcPgdFtn;
    private  int        field_184_lcbPgdFtn;
    private  int        field_185_fcBkdFtn;
    private  int        field_186_lcbBkdFtn;
    private  int        field_187_fcPgdEdn;
    private  int        field_188_lcbPgdEdn;
    private  int        field_189_fcBkdEdn;
    private  int        field_190_lcbBkdEdn;
    private  int        field_191_fcSttbfIntlFld;
    private  int        field_192_lcbSttbfIntlFld;
    private  int        field_193_fcRouteSlip;
    private  int        field_194_lcbRouteSlip;
    private  int        field_195_fcSttbSavedBy;
    private  int        field_196_lcbSttbSavedBy;
    private  int        field_197_fcSttbFnm;
    private  int        field_198_lcbSttbFnm;
    private  int        field_199_fcPlcfLst;
    private  int        field_200_lcbPlcfLst;
    private  int        field_201_fcPlfLfo;
    private  int        field_202_lcbPlfLfo;
    private  int        field_203_fcPlcftxbxBkd;
    private  int        field_204_lcbPlcftxbxBkd;
    private  int        field_205_fcPlcftxbxHdrBkd;
    private  int        field_206_lcbPlcftxbxHdrBkd;
    private  int        field_207_fcDocUndo;
    private  int        field_208_lcbDocUndo;
    private  int        field_209_fcRgbuse;
    private  int        field_210_lcbRgbuse;
    private  int        field_211_fcUsp;
    private  int        field_212_lcbUsp;
    private  int        field_213_fcUskf;
    private  int        field_214_lcbUskf;
    private  int        field_215_fcPlcupcRgbuse;
    private  int        field_216_lcbPlcupcRgbuse;
    private  int        field_217_fcPlcupcUsp;
    private  int        field_218_lcbPlcupcUsp;
    private  int        field_219_fcSttbGlsyStyle;
    private  int        field_220_lcbSttbGlsyStyle;
    private  int        field_221_fcPlgosl;
    private  int        field_222_lcbPlgosl;
    private  int        field_223_fcPlcocx;
    private  int        field_224_lcbPlcocx;
    private  int        field_225_fcPlcfbteLvc;
    private  int        field_226_lcbPlcfbteLvc;
    private  int        field_227_ftModified;
    private  int        field_228_dwLowDateTime;
    private  int        field_229_dwHighDateTime;
    private  int        field_230_fcPlcflvc;
    private  int        field_231_lcbPlcflvc;
    private  int        field_232_fcPlcasumy;
    private  int        field_233_lcbPlcasumy;
    private  int        field_234_fcPlcfgram;
    private  int        field_235_lcbPlcfgram;
    private  int        field_236_fcSttbListNames;
    private  int        field_237_lcbSttbListNames;
    private  int        field_238_fcSttbfUssr;
    private  int        field_239_lcbSttbfUssr;


    public FIBAbstractType()
    {

    }

    protected void fillFields(byte [] data, short size, int offset)
    {
        field_1_id                      = LittleEndian.getShort(data, 0x0 + offset);
        field_2_version                 = LittleEndian.getShort(data, 0x2 + offset);
        field_3_productVersion          = LittleEndian.getShort(data, 0x4 + offset);
        field_4_languageStamp           = LittleEndian.getShort(data, 0x6 + offset);
        field_5_unknown0                = LittleEndian.getShort(data, 0x8 + offset);
        field_6_options                 = LittleEndian.getShort(data, 0xa + offset);
        field_7_minversion              = LittleEndian.getShort(data, 0xc + offset);
        field_8_encryptedKey            = LittleEndian.getShort(data, 0xe + offset);
        field_9_environment             = LittleEndian.getShort(data, 0x10 + offset);
        field_10_history                = LittleEndian.getShort(data, 0x12 + offset);
        field_11_defaultCharset         = LittleEndian.getShort(data, 0x14 + offset);
        field_12_defaultExtcharset      = LittleEndian.getShort(data, 0x16 + offset);
        field_13_offsetFirstChar        = LittleEndian.getInt(data, 0x18 + offset);
        field_14_offsetLastChar         = LittleEndian.getInt(data, 0x1c + offset);
        field_15_countShorts            = LittleEndian.getShort(data, 0x20 + offset);
        field_16_creatorIdOrBegShorts   = LittleEndian.getShort(data, 0x22 + offset);
        field_17_revisorId              = LittleEndian.getShort(data, 0x24 + offset);
        field_18_creatorPrivate         = LittleEndian.getShort(data, 0x26 + offset);
        field_19_revisorPrivate         = LittleEndian.getShort(data, 0x28 + offset);
        field_20_unused1                = LittleEndian.getShort(data, 0x2a + offset);
        field_21_unused2                = LittleEndian.getShort(data, 0x2c + offset);
        field_22_unused3                = LittleEndian.getShort(data, 0x2e + offset);
        field_23_unused4                = LittleEndian.getShort(data, 0x30 + offset);
        field_24_unused5                = LittleEndian.getShort(data, 0x32 + offset);
        field_25_unused6                = LittleEndian.getShort(data, 0x34 + offset);
        field_26_unused7                = LittleEndian.getShort(data, 0x36 + offset);
        field_27_unused8                = LittleEndian.getShort(data, 0x38 + offset);
        field_28_unused9                = LittleEndian.getShort(data, 0x3a + offset);
        field_29_fareastid              = LittleEndian.getShort(data, 0x3c + offset);
        field_30_countints              = LittleEndian.getShort(data, 0x3e + offset);
        field_31_lastByteOrBegInts      = LittleEndian.getInt(data, 0x40 + offset);
        field_32_creatorBuildDate       = LittleEndian.getInt(data, 0x44 + offset);
        field_33_revisorBuildDate       = LittleEndian.getInt(data, 0x48 + offset);
        field_34_mainStreamlen          = LittleEndian.getInt(data, 0x4c + offset);
        field_35_footnoteStreamlen      = LittleEndian.getInt(data, 0x50 + offset);
        field_36_headerStreamlen        = LittleEndian.getInt(data, 0x54 + offset);
        field_37_macroStreamlen         = LittleEndian.getInt(data, 0x58 + offset);
        field_38_annotationStreamlen    = LittleEndian.getInt(data, 0x5c + offset);
        field_39_endnoteStreamlen       = LittleEndian.getInt(data, 0x60 + offset);
        field_40_textboxStreamlen       = LittleEndian.getInt(data, 0x64 + offset);
        field_41_headboxStreamlen       = LittleEndian.getInt(data, 0x68 + offset);
        field_42_ptrToPlcListChp        = LittleEndian.getInt(data, 0x6c + offset);
        field_43_firstChp               = LittleEndian.getInt(data, 0x70 + offset);
        field_44_countChps              = LittleEndian.getInt(data, 0x74 + offset);
        field_45_ptrToPlcListPap        = LittleEndian.getInt(data, 0x78 + offset);
        field_46_firstPap               = LittleEndian.getInt(data, 0x7c + offset);
        field_47_countPaps              = LittleEndian.getInt(data, 0x80 + offset);
        field_48_ptrToPlcListLvc        = LittleEndian.getInt(data, 0x84 + offset);
        field_49_firstLvc               = LittleEndian.getInt(data, 0x88 + offset);
        field_50_countLvc               = LittleEndian.getInt(data, 0x8c + offset);
        field_51_unknown1               = LittleEndian.getInt(data, 0x90 + offset);
        field_52_unknown2               = LittleEndian.getInt(data, 0x94 + offset);
        field_53_lcbArraySize           = LittleEndian.getShort(data, 0x98 + offset);
        field_54_originalStylesheetOffset  = LittleEndian.getInt(data, 0x9a + offset);
        field_55_originalStylesheetSize  = LittleEndian.getInt(data, 0x9e + offset);
        field_56_stylesheetOffset       = LittleEndian.getInt(data, 0xa2 + offset);
        field_57_stylesheetSize         = LittleEndian.getInt(data, 0xa6 + offset);
        field_58_footnoteRefOffset      = LittleEndian.getInt(data, 0xaa + offset);
        field_59_footnoteRefSize        = LittleEndian.getInt(data, 0xae + offset);
        field_60_plcOffset              = LittleEndian.getInt(data, 0xb2 + offset);
        field_61_plcSize                = LittleEndian.getInt(data, 0xb6 + offset);
        field_62_annotationRefOffset    = LittleEndian.getInt(data, 0xba + offset);
        field_63_annotationRefSize      = LittleEndian.getInt(data, 0xbe + offset);
        field_64_annotationPlcOffset    = LittleEndian.getInt(data, 0xc2 + offset);
        field_65_annotationPlcSize      = LittleEndian.getInt(data, 0xc6 + offset);
        field_66_sectionPlcOffset       = LittleEndian.getInt(data, 0xca + offset);
        field_67_sectionPlcSize         = LittleEndian.getInt(data, 0xce + offset);
        field_68_unusedA                = LittleEndian.getInt(data, 0xd2 + offset);
        field_69_unusedB                = LittleEndian.getInt(data, 0xd6 + offset);
        field_70_pheplcOffset           = LittleEndian.getInt(data, 0xda + offset);
        field_71_pheplcSize             = LittleEndian.getInt(data, 0xde + offset);
        field_72_glossarySTOffset       = LittleEndian.getInt(data, 0xe2 + offset);
        field_73_glossarySTSize         = LittleEndian.getInt(data, 0xe6 + offset);
        field_74_glossaryPLCOffset      = LittleEndian.getInt(data, 0xea + offset);
        field_75_glossaryPLCSize        = LittleEndian.getInt(data, 0xee + offset);
        field_76_headerPLCOffset        = LittleEndian.getInt(data, 0xf2 + offset);
        field_77_headerPLCSize          = LittleEndian.getInt(data, 0xf6 + offset);
        field_78_chp_bin_table_offset   = LittleEndian.getInt(data, 0xfa + offset);
        field_79_chp_bin_table_size     = LittleEndian.getInt(data, 0xfe + offset);
        field_80_pap_bin_table_offset   = LittleEndian.getInt(data, 0x102 + offset);
        field_81_pap_bin_table_size     = LittleEndian.getInt(data, 0x106 + offset);
        field_82_sea_bin_table_offset   = LittleEndian.getInt(data, 0x10a + offset);
        field_83_sea_bin_table_size     = LittleEndian.getInt(data, 0x10e + offset);
        field_84_fonts_bin_table_offset  = LittleEndian.getInt(data, 0x112 + offset);
        field_85_fonts_bin_table_size   = LittleEndian.getInt(data, 0x116 + offset);
        field_86_main_fields_offset     = LittleEndian.getInt(data, 0x11a + offset);
        field_87_main_fields_size       = LittleEndian.getInt(data, 0x11e + offset);
        field_88_header_fields_offset   = LittleEndian.getInt(data, 0x122 + offset);
        field_89_header_fields_size     = LittleEndian.getInt(data, 0x126 + offset);
        field_90_footnote_fields_offset  = LittleEndian.getInt(data, 0x12a + offset);
        field_91_footnote_fields_size   = LittleEndian.getInt(data, 0x12e + offset);
        field_92_ann_fields_offset      = LittleEndian.getInt(data, 0x132 + offset);
        field_93_ann_fields_size        = LittleEndian.getInt(data, 0x136 + offset);
        field_94_unusedC                = LittleEndian.getInt(data, 0x13a + offset);
        field_95_unusedD                = LittleEndian.getInt(data, 0x13e + offset);
        field_96_bookmark_names_offset  = LittleEndian.getInt(data, 0x142 + offset);
        field_97_bookmark_names_size    = LittleEndian.getInt(data, 0x146 + offset);
        field_98_bookmark_offsets_offset  = LittleEndian.getInt(data, 0x14a + offset);
        field_99_bookmark_offsets_size  = LittleEndian.getInt(data, 0x14e + offset);
        field_100_macros_offset         = LittleEndian.getInt(data, 0x152 + offset);
        field_101_macros_size           = LittleEndian.getInt(data, 0x156 + offset);
        field_102_unusedE               = LittleEndian.getInt(data, 0x15a + offset);
        field_103_unusedF               = LittleEndian.getInt(data, 0x15e + offset);
        field_104_unused10              = LittleEndian.getInt(data, 0x162 + offset);
        field_105_unused11              = LittleEndian.getInt(data, 0x166 + offset);
        field_106_printerOffset         = LittleEndian.getInt(data, 0x16a + offset);
        field_107_printerSize           = LittleEndian.getInt(data, 0x16e + offset);
        field_108_printerPortraitOffset  = LittleEndian.getInt(data, 0x172 + offset);
        field_109_printerPortraitSize   = LittleEndian.getInt(data, 0x176 + offset);
        field_110_printerLandscapeOffset  = LittleEndian.getInt(data, 0x17a + offset);
        field_111_printerLandscapeSize  = LittleEndian.getInt(data, 0x17e + offset);
        field_112_wssOffset             = LittleEndian.getInt(data, 0x182 + offset);
        field_113_wssSize               = LittleEndian.getInt(data, 0x186 + offset);
        field_114_DOPOffset             = LittleEndian.getInt(data, 0x18a + offset);
        field_115_DOPSize               = LittleEndian.getInt(data, 0x18e + offset);
        field_116_sttbfassoc_offset     = LittleEndian.getInt(data, 0x192 + offset);
        field_117_sttbfassoc_size       = LittleEndian.getInt(data, 0x196 + offset);
        field_118_textPieceTableOffset  = LittleEndian.getInt(data, 0x19a + offset);
        field_119_textPieceTableSize    = LittleEndian.getInt(data, 0x19e + offset);
        field_120_unused12              = LittleEndian.getInt(data, 0x1a2 + offset);
        field_121_unused13              = LittleEndian.getInt(data, 0x1a6 + offset);
        field_122_offsetAutosaveSource  = LittleEndian.getInt(data, 0x1aa + offset);
        field_123_countAutosaveSource   = LittleEndian.getInt(data, 0x1ae + offset);
        field_124_offsetGrpXstAtnOwners  = LittleEndian.getInt(data, 0x1b2 + offset);
        field_125_countGrpXstAtnOwners  = LittleEndian.getInt(data, 0x1b6 + offset);
        field_126_offsetSttbfAtnbkmk    = LittleEndian.getInt(data, 0x1ba + offset);
        field_127_lengthSttbfAtnbkmk    = LittleEndian.getInt(data, 0x1be + offset);
        field_128_unused14              = LittleEndian.getInt(data, 0x1c2 + offset);
        field_129_unused15              = LittleEndian.getInt(data, 0x1c6 + offset);
        field_130_unused16              = LittleEndian.getInt(data, 0x1ca + offset);
        field_131_unused17              = LittleEndian.getInt(data, 0x1ce + offset);
        field_132_offsetPlcspaMom       = LittleEndian.getInt(data, 0x1d2 + offset);
        field_133_lengthPlcspaMom       = LittleEndian.getInt(data, 0x1d6 + offset);
        field_134_offsetPlcspaHdr       = LittleEndian.getInt(data, 0x1da + offset);
        field_135_lengthPlcspaHdr       = LittleEndian.getInt(data, 0x1de + offset);
        field_136_lengthPlcfAnnBkmrkFirst  = LittleEndian.getInt(data, 0x1e2 + offset);
        field_137_offsetPlcfAnnBkmrkFirst  = LittleEndian.getInt(data, 0x1e6 + offset);
        field_138_lengthPlcfAnnBkarkLast  = LittleEndian.getInt(data, 0x1ea + offset);
        field_139_PlcfAtnbkl            = LittleEndian.getInt(data, 0x1ee + offset);
        field_140_fcPms                 = LittleEndian.getInt(data, 0x1f2 + offset);
        field_141_lcbPms                = LittleEndian.getInt(data, 0x1f6 + offset);
        field_142_fcFormFldSttbs        = LittleEndian.getInt(data, 0x1fa + offset);
        field_143_lcbFormFldSttbs       = LittleEndian.getInt(data, 0x1fe + offset);
        field_144_fcPlcfendRef          = LittleEndian.getInt(data, 0x202 + offset);
        field_145_lcbPlcfendRef         = LittleEndian.getInt(data, 0x206 + offset);
        field_146_fcPlcfendTxt          = LittleEndian.getInt(data, 0x20a + offset);
        field_147_lcbPlcfendTxt         = LittleEndian.getInt(data, 0x20e + offset);
        field_148_fcPlcffldEdn          = LittleEndian.getInt(data, 0x212 + offset);
        field_149_lcbPlcffldEdn         = LittleEndian.getInt(data, 0x216 + offset);
        field_150_fcPlcfpgdEdn          = LittleEndian.getInt(data, 0x21a + offset);
        field_151_lcbPlcfpgdEdn         = LittleEndian.getInt(data, 0x21e + offset);
        field_152_fcDggInfo             = LittleEndian.getInt(data, 0x222 + offset);
        field_153_lcbDggInfo            = LittleEndian.getInt(data, 0x226 + offset);
        field_154_fcSttbfRMark          = LittleEndian.getInt(data, 0x22a + offset);
        field_155_lcbSttbfRMark         = LittleEndian.getInt(data, 0x22e + offset);
        field_156_fcSttbCaption         = LittleEndian.getInt(data, 0x232 + offset);
        field_157_lcbSttbCaption        = LittleEndian.getInt(data, 0x236 + offset);
        field_158_fcSttbAutoCaption     = LittleEndian.getInt(data, 0x23a + offset);
        field_159_lcbSttbAutoCaption    = LittleEndian.getInt(data, 0x23e + offset);
        field_160_fcPlcfwkb             = LittleEndian.getInt(data, 0x242 + offset);
        field_161_lcbPlcfwkb            = LittleEndian.getInt(data, 0x246 + offset);
        field_162_fcPlcfsplfcPlcfspl    = LittleEndian.getInt(data, 0x24a + offset);
        field_163_lcbPlcfspl            = LittleEndian.getInt(data, 0x24e + offset);
        field_164_fcPlcftxbxTxt         = LittleEndian.getInt(data, 0x252 + offset);
        field_165_lcbPlcftxbxTxt        = LittleEndian.getInt(data, 0x256 + offset);
        field_166_fcPlcffldTxbx         = LittleEndian.getInt(data, 0x25a + offset);
        field_167_lcbPlcffldTxbx        = LittleEndian.getInt(data, 0x25e + offset);
        field_168_fcPlcfhdrtxbxTxt      = LittleEndian.getInt(data, 0x262 + offset);
        field_169_lcbPlcfhdrtxbxTxt     = LittleEndian.getInt(data, 0x266 + offset);
        field_170_fcPlcffldHdrTxbx      = LittleEndian.getInt(data, 0x26a + offset);
        field_171_lcbPlcffldHdrTxbx     = LittleEndian.getInt(data, 0x26e + offset);
        field_172_fcStwUser             = LittleEndian.getInt(data, 0x272 + offset);
        field_173_lcbStwUser            = LittleEndian.getInt(data, 0x276 + offset);
        field_174_fcSttbttmbd           = LittleEndian.getInt(data, 0x27a + offset);
        field_175_cbSttbttmbd           = LittleEndian.getInt(data, 0x27e + offset);
        field_176_fcUnused              = LittleEndian.getInt(data, 0x282 + offset);
        field_177_lcbUnused             = LittleEndian.getInt(data, 0x286 + offset);
        field_178_rgpgdbkd              = LittleEndian.getInt(data, 0x28a + offset);
        field_179_fcPgdMother           = LittleEndian.getInt(data, 0x28e + offset);
        field_180_lcbPgdMother          = LittleEndian.getInt(data, 0x292 + offset);
        field_181_fcBkdMother           = LittleEndian.getInt(data, 0x296 + offset);
        field_182_lcbBkdMother          = LittleEndian.getInt(data, 0x29a + offset);
        field_183_fcPgdFtn              = LittleEndian.getInt(data, 0x29e + offset);
        field_184_lcbPgdFtn             = LittleEndian.getInt(data, 0x2a2 + offset);
        field_185_fcBkdFtn              = LittleEndian.getInt(data, 0x2a6 + offset);
        field_186_lcbBkdFtn             = LittleEndian.getInt(data, 0x2aa + offset);
        field_187_fcPgdEdn              = LittleEndian.getInt(data, 0x2ae + offset);
        field_188_lcbPgdEdn             = LittleEndian.getInt(data, 0x2b2 + offset);
        field_189_fcBkdEdn              = LittleEndian.getInt(data, 0x2b6 + offset);
        field_190_lcbBkdEdn             = LittleEndian.getInt(data, 0x2ba + offset);
        field_191_fcSttbfIntlFld        = LittleEndian.getInt(data, 0x2be + offset);
        field_192_lcbSttbfIntlFld       = LittleEndian.getInt(data, 0x2c2 + offset);
        field_193_fcRouteSlip           = LittleEndian.getInt(data, 0x2c6 + offset);
        field_194_lcbRouteSlip          = LittleEndian.getInt(data, 0x2ca + offset);
        field_195_fcSttbSavedBy         = LittleEndian.getInt(data, 0x2ce + offset);
        field_196_lcbSttbSavedBy        = LittleEndian.getInt(data, 0x2d2 + offset);
        field_197_fcSttbFnm             = LittleEndian.getInt(data, 0x2d6 + offset);
        field_198_lcbSttbFnm            = LittleEndian.getInt(data, 0x2da + offset);
        field_199_fcPlcfLst             = LittleEndian.getInt(data, 0x2de + offset);
        field_200_lcbPlcfLst            = LittleEndian.getInt(data, 0x2e2 + offset);
        field_201_fcPlfLfo              = LittleEndian.getInt(data, 0x2e6 + offset);
        field_202_lcbPlfLfo             = LittleEndian.getInt(data, 0x2ea + offset);
        field_203_fcPlcftxbxBkd         = LittleEndian.getInt(data, 0x2ee + offset);
        field_204_lcbPlcftxbxBkd        = LittleEndian.getInt(data, 0x2f2 + offset);
        field_205_fcPlcftxbxHdrBkd      = LittleEndian.getInt(data, 0x2f6 + offset);
        field_206_lcbPlcftxbxHdrBkd     = LittleEndian.getInt(data, 0x2fa + offset);
        field_207_fcDocUndo             = LittleEndian.getInt(data, 0x2fe + offset);
        field_208_lcbDocUndo            = LittleEndian.getInt(data, 0x302 + offset);
        field_209_fcRgbuse              = LittleEndian.getInt(data, 0x306 + offset);
        field_210_lcbRgbuse             = LittleEndian.getInt(data, 0x30a + offset);
        field_211_fcUsp                 = LittleEndian.getInt(data, 0x30e + offset);
        field_212_lcbUsp                = LittleEndian.getInt(data, 0x312 + offset);
        field_213_fcUskf                = LittleEndian.getInt(data, 0x316 + offset);
        field_214_lcbUskf               = LittleEndian.getInt(data, 0x31a + offset);
        field_215_fcPlcupcRgbuse        = LittleEndian.getInt(data, 0x31e + offset);
        field_216_lcbPlcupcRgbuse       = LittleEndian.getInt(data, 0x322 + offset);
        field_217_fcPlcupcUsp           = LittleEndian.getInt(data, 0x326 + offset);
        field_218_lcbPlcupcUsp          = LittleEndian.getInt(data, 0x32a + offset);
        field_219_fcSttbGlsyStyle       = LittleEndian.getInt(data, 0x32e + offset);
        field_220_lcbSttbGlsyStyle      = LittleEndian.getInt(data, 0x332 + offset);
        field_221_fcPlgosl              = LittleEndian.getInt(data, 0x336 + offset);
        field_222_lcbPlgosl             = LittleEndian.getInt(data, 0x33a + offset);
        field_223_fcPlcocx              = LittleEndian.getInt(data, 0x33e + offset);
        field_224_lcbPlcocx             = LittleEndian.getInt(data, 0x342 + offset);
        field_225_fcPlcfbteLvc          = LittleEndian.getInt(data, 0x346 + offset);
        field_226_lcbPlcfbteLvc         = LittleEndian.getInt(data, 0x34a + offset);
        field_227_ftModified            = LittleEndian.getInt(data, 0x34e + offset);
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

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[FIB]\n");

        buffer.append("    .id                   = ")
            .append("0x")
            .append(HexDump.toHex((short)getId()))
            .append(" (").append(getId()).append(" )\n");

        buffer.append("    .version              = ")
            .append("0x")
            .append(HexDump.toHex((short)getVersion()))
            .append(" (").append(getVersion()).append(" )\n");

        buffer.append("    .productVersion       = ")
            .append("0x")
            .append(HexDump.toHex((short)getProductVersion()))
            .append(" (").append(getProductVersion()).append(" )\n");

        buffer.append("    .languageStamp        = ")
            .append("0x")
            .append(HexDump.toHex((short)getLanguageStamp()))
            .append(" (").append(getLanguageStamp()).append(" )\n");

        buffer.append("    .unknown0             = ")
            .append("0x")
            .append(HexDump.toHex((short)getUnknown0()))
            .append(" (").append(getUnknown0()).append(" )\n");

        buffer.append("    .options              = ")
            .append("0x")
            .append(HexDump.toHex((short)getOptions()))
            .append(" (").append(getOptions()).append(" )\n");
        buffer.append("         .template                 = ").append(isTemplate            ()).append('\n');
        buffer.append("         .glossary                 = ").append(isGlossary            ()).append('\n');
        buffer.append("         .quicksave                = ").append(isQuicksave           ()).append('\n');
        buffer.append("         .haspictr                 = ").append(isHaspictr            ()).append('\n');
        buffer.append("         .nquicksaves              = ").append(isNquicksaves         ()).append('\n');
        buffer.append("         .encrypted                = ").append(isEncrypted           ()).append('\n');
        buffer.append("         .tabletype                = ").append(isTabletype           ()).append('\n');
        buffer.append("         .readonly                 = ").append(isReadonly            ()).append('\n');
        buffer.append("         .writeReservation         = ").append(isWriteReservation    ()).append('\n');
        buffer.append("         .extendedCharacter        = ").append(isExtendedCharacter   ()).append('\n');
        buffer.append("         .loadOverride             = ").append(isLoadOverride        ()).append('\n');
        buffer.append("         .farEast                  = ").append(isFarEast             ()).append('\n');
        buffer.append("         .crypto                   = ").append(isCrypto              ()).append('\n');

        buffer.append("    .minversion           = ")
            .append("0x")
            .append(HexDump.toHex((short)getMinversion()))
            .append(" (").append(getMinversion()).append(" )\n");

        buffer.append("    .encryptedKey         = ")
            .append("0x")
            .append(HexDump.toHex((short)getEncryptedKey()))
            .append(" (").append(getEncryptedKey()).append(" )\n");

        buffer.append("    .environment          = ")
            .append("0x")
            .append(HexDump.toHex((short)getEnvironment()))
            .append(" (").append(getEnvironment()).append(" )\n");

        buffer.append("    .history              = ")
            .append("0x")
            .append(HexDump.toHex((short)getHistory()))
            .append(" (").append(getHistory()).append(" )\n");
        buffer.append("         .historyMac               = ").append(isHistoryMac          ()).append('\n');
        buffer.append("         .emptySpecial             = ").append(isEmptySpecial        ()).append('\n');
        buffer.append("         .loadOverrideHist         = ").append(isLoadOverrideHist    ()).append('\n');
        buffer.append("         .featureUndo              = ").append(isFeatureUndo         ()).append('\n');
        buffer.append("         .v97Saved                 = ").append(isV97Saved            ()).append('\n');
        buffer.append("         .spare                    = ").append(isSpare               ()).append('\n');

        buffer.append("    .defaultCharset       = ")
            .append("0x")
            .append(HexDump.toHex((short)getDefaultCharset()))
            .append(" (").append(getDefaultCharset()).append(" )\n");

        buffer.append("    .defaultExtcharset    = ")
            .append("0x")
            .append(HexDump.toHex((short)getDefaultExtcharset()))
            .append(" (").append(getDefaultExtcharset()).append(" )\n");

        buffer.append("    .offsetFirstChar      = ")
            .append("0x")
            .append(HexDump.toHex((int)getOffsetFirstChar()))
            .append(" (").append(getOffsetFirstChar()).append(" )\n");

        buffer.append("    .offsetLastChar       = ")
            .append("0x")
            .append(HexDump.toHex((int)getOffsetLastChar()))
            .append(" (").append(getOffsetLastChar()).append(" )\n");

        buffer.append("    .countShorts          = ")
            .append("0x")
            .append(HexDump.toHex((short)getCountShorts()))
            .append(" (").append(getCountShorts()).append(" )\n");

        buffer.append("    .creatorIdOrBegShorts = ")
            .append("0x")
            .append(HexDump.toHex((short)getCreatorIdOrBegShorts()))
            .append(" (").append(getCreatorIdOrBegShorts()).append(" )\n");

        buffer.append("    .revisorId            = ")
            .append("0x")
            .append(HexDump.toHex((short)getRevisorId()))
            .append(" (").append(getRevisorId()).append(" )\n");

        buffer.append("    .creatorPrivate       = ")
            .append("0x")
            .append(HexDump.toHex((short)getCreatorPrivate()))
            .append(" (").append(getCreatorPrivate()).append(" )\n");

        buffer.append("    .revisorPrivate       = ")
            .append("0x")
            .append(HexDump.toHex((short)getRevisorPrivate()))
            .append(" (").append(getRevisorPrivate()).append(" )\n");

        buffer.append("    .unused1              = ")
            .append("0x")
            .append(HexDump.toHex((short)getUnused1()))
            .append(" (").append(getUnused1()).append(" )\n");

        buffer.append("    .unused2              = ")
            .append("0x")
            .append(HexDump.toHex((short)getUnused2()))
            .append(" (").append(getUnused2()).append(" )\n");

        buffer.append("    .unused3              = ")
            .append("0x")
            .append(HexDump.toHex((short)getUnused3()))
            .append(" (").append(getUnused3()).append(" )\n");

        buffer.append("    .unused4              = ")
            .append("0x")
            .append(HexDump.toHex((short)getUnused4()))
            .append(" (").append(getUnused4()).append(" )\n");

        buffer.append("    .unused5              = ")
            .append("0x")
            .append(HexDump.toHex((short)getUnused5()))
            .append(" (").append(getUnused5()).append(" )\n");

        buffer.append("    .unused6              = ")
            .append("0x")
            .append(HexDump.toHex((short)getUnused6()))
            .append(" (").append(getUnused6()).append(" )\n");

        buffer.append("    .unused7              = ")
            .append("0x")
            .append(HexDump.toHex((short)getUnused7()))
            .append(" (").append(getUnused7()).append(" )\n");

        buffer.append("    .unused8              = ")
            .append("0x")
            .append(HexDump.toHex((short)getUnused8()))
            .append(" (").append(getUnused8()).append(" )\n");

        buffer.append("    .unused9              = ")
            .append("0x")
            .append(HexDump.toHex((short)getUnused9()))
            .append(" (").append(getUnused9()).append(" )\n");

        buffer.append("    .fareastid            = ")
            .append("0x")
            .append(HexDump.toHex((short)getFareastid()))
            .append(" (").append(getFareastid()).append(" )\n");

        buffer.append("    .countints            = ")
            .append("0x")
            .append(HexDump.toHex((short)getCountints()))
            .append(" (").append(getCountints()).append(" )\n");

        buffer.append("    .lastByteOrBegInts    = ")
            .append("0x")
            .append(HexDump.toHex((int)getLastByteOrBegInts()))
            .append(" (").append(getLastByteOrBegInts()).append(" )\n");

        buffer.append("    .creatorBuildDate     = ")
            .append("0x")
            .append(HexDump.toHex((int)getCreatorBuildDate()))
            .append(" (").append(getCreatorBuildDate()).append(" )\n");

        buffer.append("    .revisorBuildDate     = ")
            .append("0x")
            .append(HexDump.toHex((int)getRevisorBuildDate()))
            .append(" (").append(getRevisorBuildDate()).append(" )\n");

        buffer.append("    .mainStreamlen        = ")
            .append("0x")
            .append(HexDump.toHex((int)getMainStreamlen()))
            .append(" (").append(getMainStreamlen()).append(" )\n");

        buffer.append("    .footnoteStreamlen    = ")
            .append("0x")
            .append(HexDump.toHex((int)getFootnoteStreamlen()))
            .append(" (").append(getFootnoteStreamlen()).append(" )\n");

        buffer.append("    .headerStreamlen      = ")
            .append("0x")
            .append(HexDump.toHex((int)getHeaderStreamlen()))
            .append(" (").append(getHeaderStreamlen()).append(" )\n");

        buffer.append("    .macroStreamlen       = ")
            .append("0x")
            .append(HexDump.toHex((int)getMacroStreamlen()))
            .append(" (").append(getMacroStreamlen()).append(" )\n");

        buffer.append("    .annotationStreamlen  = ")
            .append("0x")
            .append(HexDump.toHex((int)getAnnotationStreamlen()))
            .append(" (").append(getAnnotationStreamlen()).append(" )\n");

        buffer.append("    .endnoteStreamlen     = ")
            .append("0x")
            .append(HexDump.toHex((int)getEndnoteStreamlen()))
            .append(" (").append(getEndnoteStreamlen()).append(" )\n");

        buffer.append("    .textboxStreamlen     = ")
            .append("0x")
            .append(HexDump.toHex((int)getTextboxStreamlen()))
            .append(" (").append(getTextboxStreamlen()).append(" )\n");

        buffer.append("    .headboxStreamlen     = ")
            .append("0x")
            .append(HexDump.toHex((int)getHeadboxStreamlen()))
            .append(" (").append(getHeadboxStreamlen()).append(" )\n");

        buffer.append("    .ptrToPlcListChp      = ")
            .append("0x")
            .append(HexDump.toHex((int)getPtrToPlcListChp()))
            .append(" (").append(getPtrToPlcListChp()).append(" )\n");

        buffer.append("    .firstChp             = ")
            .append("0x")
            .append(HexDump.toHex((int)getFirstChp()))
            .append(" (").append(getFirstChp()).append(" )\n");

        buffer.append("    .countChps            = ")
            .append("0x")
            .append(HexDump.toHex((int)getCountChps()))
            .append(" (").append(getCountChps()).append(" )\n");

        buffer.append("    .ptrToPlcListPap      = ")
            .append("0x")
            .append(HexDump.toHex((int)getPtrToPlcListPap()))
            .append(" (").append(getPtrToPlcListPap()).append(" )\n");

        buffer.append("    .firstPap             = ")
            .append("0x")
            .append(HexDump.toHex((int)getFirstPap()))
            .append(" (").append(getFirstPap()).append(" )\n");

        buffer.append("    .countPaps            = ")
            .append("0x")
            .append(HexDump.toHex((int)getCountPaps()))
            .append(" (").append(getCountPaps()).append(" )\n");

        buffer.append("    .ptrToPlcListLvc      = ")
            .append("0x")
            .append(HexDump.toHex((int)getPtrToPlcListLvc()))
            .append(" (").append(getPtrToPlcListLvc()).append(" )\n");

        buffer.append("    .firstLvc             = ")
            .append("0x")
            .append(HexDump.toHex((int)getFirstLvc()))
            .append(" (").append(getFirstLvc()).append(" )\n");

        buffer.append("    .countLvc             = ")
            .append("0x")
            .append(HexDump.toHex((int)getCountLvc()))
            .append(" (").append(getCountLvc()).append(" )\n");

        buffer.append("    .unknown1             = ")
            .append("0x")
            .append(HexDump.toHex((int)getUnknown1()))
            .append(" (").append(getUnknown1()).append(" )\n");

        buffer.append("    .unknown2             = ")
            .append("0x")
            .append(HexDump.toHex((int)getUnknown2()))
            .append(" (").append(getUnknown2()).append(" )\n");

        buffer.append("    .lcbArraySize         = ")
            .append("0x")
            .append(HexDump.toHex((short)getLcbArraySize()))
            .append(" (").append(getLcbArraySize()).append(" )\n");

        buffer.append("    .originalStylesheetOffset = ")
            .append("0x")
            .append(HexDump.toHex((int)getOriginalStylesheetOffset()))
            .append(" (").append(getOriginalStylesheetOffset()).append(" )\n");

        buffer.append("    .originalStylesheetSize = ")
            .append("0x")
            .append(HexDump.toHex((int)getOriginalStylesheetSize()))
            .append(" (").append(getOriginalStylesheetSize()).append(" )\n");

        buffer.append("    .stylesheetOffset     = ")
            .append("0x")
            .append(HexDump.toHex((int)getStylesheetOffset()))
            .append(" (").append(getStylesheetOffset()).append(" )\n");

        buffer.append("    .stylesheetSize       = ")
            .append("0x")
            .append(HexDump.toHex((int)getStylesheetSize()))
            .append(" (").append(getStylesheetSize()).append(" )\n");

        buffer.append("    .footnoteRefOffset    = ")
            .append("0x")
            .append(HexDump.toHex((int)getFootnoteRefOffset()))
            .append(" (").append(getFootnoteRefOffset()).append(" )\n");

        buffer.append("    .footnoteRefSize      = ")
            .append("0x")
            .append(HexDump.toHex((int)getFootnoteRefSize()))
            .append(" (").append(getFootnoteRefSize()).append(" )\n");

        buffer.append("    .plcOffset            = ")
            .append("0x")
            .append(HexDump.toHex((int)getPlcOffset()))
            .append(" (").append(getPlcOffset()).append(" )\n");

        buffer.append("    .plcSize              = ")
            .append("0x")
            .append(HexDump.toHex((int)getPlcSize()))
            .append(" (").append(getPlcSize()).append(" )\n");

        buffer.append("    .annotationRefOffset  = ")
            .append("0x")
            .append(HexDump.toHex((int)getAnnotationRefOffset()))
            .append(" (").append(getAnnotationRefOffset()).append(" )\n");

        buffer.append("    .annotationRefSize    = ")
            .append("0x")
            .append(HexDump.toHex((int)getAnnotationRefSize()))
            .append(" (").append(getAnnotationRefSize()).append(" )\n");

        buffer.append("    .annotationPlcOffset  = ")
            .append("0x")
            .append(HexDump.toHex((int)getAnnotationPlcOffset()))
            .append(" (").append(getAnnotationPlcOffset()).append(" )\n");

        buffer.append("    .annotationPlcSize    = ")
            .append("0x")
            .append(HexDump.toHex((int)getAnnotationPlcSize()))
            .append(" (").append(getAnnotationPlcSize()).append(" )\n");

        buffer.append("    .sectionPlcOffset     = ")
            .append("0x")
            .append(HexDump.toHex((int)getSectionPlcOffset()))
            .append(" (").append(getSectionPlcOffset()).append(" )\n");

        buffer.append("    .sectionPlcSize       = ")
            .append("0x")
            .append(HexDump.toHex((int)getSectionPlcSize()))
            .append(" (").append(getSectionPlcSize()).append(" )\n");

        buffer.append("    .unusedA              = ")
            .append("0x")
            .append(HexDump.toHex((int)getUnusedA()))
            .append(" (").append(getUnusedA()).append(" )\n");

        buffer.append("    .unusedB              = ")
            .append("0x")
            .append(HexDump.toHex((int)getUnusedB()))
            .append(" (").append(getUnusedB()).append(" )\n");

        buffer.append("    .pheplcOffset         = ")
            .append("0x")
            .append(HexDump.toHex((int)getPheplcOffset()))
            .append(" (").append(getPheplcOffset()).append(" )\n");

        buffer.append("    .pheplcSize           = ")
            .append("0x")
            .append(HexDump.toHex((int)getPheplcSize()))
            .append(" (").append(getPheplcSize()).append(" )\n");

        buffer.append("    .glossarySTOffset     = ")
            .append("0x")
            .append(HexDump.toHex((int)getGlossarySTOffset()))
            .append(" (").append(getGlossarySTOffset()).append(" )\n");

        buffer.append("    .glossarySTSize       = ")
            .append("0x")
            .append(HexDump.toHex((int)getGlossarySTSize()))
            .append(" (").append(getGlossarySTSize()).append(" )\n");

        buffer.append("    .glossaryPLCOffset    = ")
            .append("0x")
            .append(HexDump.toHex((int)getGlossaryPLCOffset()))
            .append(" (").append(getGlossaryPLCOffset()).append(" )\n");

        buffer.append("    .glossaryPLCSize      = ")
            .append("0x")
            .append(HexDump.toHex((int)getGlossaryPLCSize()))
            .append(" (").append(getGlossaryPLCSize()).append(" )\n");

        buffer.append("    .headerPLCOffset      = ")
            .append("0x")
            .append(HexDump.toHex((int)getHeaderPLCOffset()))
            .append(" (").append(getHeaderPLCOffset()).append(" )\n");

        buffer.append("    .headerPLCSize        = ")
            .append("0x")
            .append(HexDump.toHex((int)getHeaderPLCSize()))
            .append(" (").append(getHeaderPLCSize()).append(" )\n");

        buffer.append("    .chp_bin_table_offset = ")
            .append("0x")
            .append(HexDump.toHex((int)getChp_bin_table_offset()))
            .append(" (").append(getChp_bin_table_offset()).append(" )\n");

        buffer.append("    .chp_bin_table_size   = ")
            .append("0x")
            .append(HexDump.toHex((int)getChp_bin_table_size()))
            .append(" (").append(getChp_bin_table_size()).append(" )\n");

        buffer.append("    .pap_bin_table_offset = ")
            .append("0x")
            .append(HexDump.toHex((int)getPap_bin_table_offset()))
            .append(" (").append(getPap_bin_table_offset()).append(" )\n");

        buffer.append("    .pap_bin_table_size   = ")
            .append("0x")
            .append(HexDump.toHex((int)getPap_bin_table_size()))
            .append(" (").append(getPap_bin_table_size()).append(" )\n");

        buffer.append("    .sea_bin_table_offset = ")
            .append("0x")
            .append(HexDump.toHex((int)getSea_bin_table_offset()))
            .append(" (").append(getSea_bin_table_offset()).append(" )\n");

        buffer.append("    .sea_bin_table_size   = ")
            .append("0x")
            .append(HexDump.toHex((int)getSea_bin_table_size()))
            .append(" (").append(getSea_bin_table_size()).append(" )\n");

        buffer.append("    .fonts_bin_table_offset = ")
            .append("0x")
            .append(HexDump.toHex((int)getFonts_bin_table_offset()))
            .append(" (").append(getFonts_bin_table_offset()).append(" )\n");

        buffer.append("    .fonts_bin_table_size = ")
            .append("0x")
            .append(HexDump.toHex((int)getFonts_bin_table_size()))
            .append(" (").append(getFonts_bin_table_size()).append(" )\n");

        buffer.append("    .main_fields_offset   = ")
            .append("0x")
            .append(HexDump.toHex((int)getMain_fields_offset()))
            .append(" (").append(getMain_fields_offset()).append(" )\n");

        buffer.append("    .main_fields_size     = ")
            .append("0x")
            .append(HexDump.toHex((int)getMain_fields_size()))
            .append(" (").append(getMain_fields_size()).append(" )\n");

        buffer.append("    .header_fields_offset = ")
            .append("0x")
            .append(HexDump.toHex((int)getHeader_fields_offset()))
            .append(" (").append(getHeader_fields_offset()).append(" )\n");

        buffer.append("    .header_fields_size   = ")
            .append("0x")
            .append(HexDump.toHex((int)getHeader_fields_size()))
            .append(" (").append(getHeader_fields_size()).append(" )\n");

        buffer.append("    .footnote_fields_offset = ")
            .append("0x")
            .append(HexDump.toHex((int)getFootnote_fields_offset()))
            .append(" (").append(getFootnote_fields_offset()).append(" )\n");

        buffer.append("    .footnote_fields_size = ")
            .append("0x")
            .append(HexDump.toHex((int)getFootnote_fields_size()))
            .append(" (").append(getFootnote_fields_size()).append(" )\n");

        buffer.append("    .ann_fields_offset    = ")
            .append("0x")
            .append(HexDump.toHex((int)getAnn_fields_offset()))
            .append(" (").append(getAnn_fields_offset()).append(" )\n");

        buffer.append("    .ann_fields_size      = ")
            .append("0x")
            .append(HexDump.toHex((int)getAnn_fields_size()))
            .append(" (").append(getAnn_fields_size()).append(" )\n");

        buffer.append("    .unusedC              = ")
            .append("0x")
            .append(HexDump.toHex((int)getUnusedC()))
            .append(" (").append(getUnusedC()).append(" )\n");

        buffer.append("    .unusedD              = ")
            .append("0x")
            .append(HexDump.toHex((int)getUnusedD()))
            .append(" (").append(getUnusedD()).append(" )\n");

        buffer.append("    .bookmark_names_offset = ")
            .append("0x")
            .append(HexDump.toHex((int)getBookmark_names_offset()))
            .append(" (").append(getBookmark_names_offset()).append(" )\n");

        buffer.append("    .bookmark_names_size  = ")
            .append("0x")
            .append(HexDump.toHex((int)getBookmark_names_size()))
            .append(" (").append(getBookmark_names_size()).append(" )\n");

        buffer.append("    .bookmark_offsets_offset = ")
            .append("0x")
            .append(HexDump.toHex((int)getBookmark_offsets_offset()))
            .append(" (").append(getBookmark_offsets_offset()).append(" )\n");

        buffer.append("    .bookmark_offsets_size = ")
            .append("0x")
            .append(HexDump.toHex((int)getBookmark_offsets_size()))
            .append(" (").append(getBookmark_offsets_size()).append(" )\n");

        buffer.append("    .macros_offset        = ")
            .append("0x")
            .append(HexDump.toHex((int)getMacros_offset()))
            .append(" (").append(getMacros_offset()).append(" )\n");

        buffer.append("    .macros_size          = ")
            .append("0x")
            .append(HexDump.toHex((int)getMacros_size()))
            .append(" (").append(getMacros_size()).append(" )\n");

        buffer.append("    .unusedE              = ")
            .append("0x")
            .append(HexDump.toHex((int)getUnusedE()))
            .append(" (").append(getUnusedE()).append(" )\n");

        buffer.append("    .unusedF              = ")
            .append("0x")
            .append(HexDump.toHex((int)getUnusedF()))
            .append(" (").append(getUnusedF()).append(" )\n");

        buffer.append("    .unused10             = ")
            .append("0x")
            .append(HexDump.toHex((int)getUnused10()))
            .append(" (").append(getUnused10()).append(" )\n");

        buffer.append("    .unused11             = ")
            .append("0x")
            .append(HexDump.toHex((int)getUnused11()))
            .append(" (").append(getUnused11()).append(" )\n");

        buffer.append("    .printerOffset        = ")
            .append("0x")
            .append(HexDump.toHex((int)getPrinterOffset()))
            .append(" (").append(getPrinterOffset()).append(" )\n");

        buffer.append("    .printerSize          = ")
            .append("0x")
            .append(HexDump.toHex((int)getPrinterSize()))
            .append(" (").append(getPrinterSize()).append(" )\n");

        buffer.append("    .printerPortraitOffset = ")
            .append("0x")
            .append(HexDump.toHex((int)getPrinterPortraitOffset()))
            .append(" (").append(getPrinterPortraitOffset()).append(" )\n");

        buffer.append("    .printerPortraitSize  = ")
            .append("0x")
            .append(HexDump.toHex((int)getPrinterPortraitSize()))
            .append(" (").append(getPrinterPortraitSize()).append(" )\n");

        buffer.append("    .printerLandscapeOffset = ")
            .append("0x")
            .append(HexDump.toHex((int)getPrinterLandscapeOffset()))
            .append(" (").append(getPrinterLandscapeOffset()).append(" )\n");

        buffer.append("    .printerLandscapeSize = ")
            .append("0x")
            .append(HexDump.toHex((int)getPrinterLandscapeSize()))
            .append(" (").append(getPrinterLandscapeSize()).append(" )\n");

        buffer.append("    .wssOffset            = ")
            .append("0x")
            .append(HexDump.toHex((int)getWssOffset()))
            .append(" (").append(getWssOffset()).append(" )\n");

        buffer.append("    .wssSize              = ")
            .append("0x")
            .append(HexDump.toHex((int)getWssSize()))
            .append(" (").append(getWssSize()).append(" )\n");

        buffer.append("    .DOPOffset            = ")
            .append("0x")
            .append(HexDump.toHex((int)getDOPOffset()))
            .append(" (").append(getDOPOffset()).append(" )\n");

        buffer.append("    .DOPSize              = ")
            .append("0x")
            .append(HexDump.toHex((int)getDOPSize()))
            .append(" (").append(getDOPSize()).append(" )\n");

        buffer.append("    .sttbfassoc_offset    = ")
            .append("0x")
            .append(HexDump.toHex((int)getSttbfassoc_offset()))
            .append(" (").append(getSttbfassoc_offset()).append(" )\n");

        buffer.append("    .sttbfassoc_size      = ")
            .append("0x")
            .append(HexDump.toHex((int)getSttbfassoc_size()))
            .append(" (").append(getSttbfassoc_size()).append(" )\n");

        buffer.append("    .textPieceTableOffset = ")
            .append("0x")
            .append(HexDump.toHex((int)getTextPieceTableOffset()))
            .append(" (").append(getTextPieceTableOffset()).append(" )\n");

        buffer.append("    .textPieceTableSize   = ")
            .append("0x")
            .append(HexDump.toHex((int)getTextPieceTableSize()))
            .append(" (").append(getTextPieceTableSize()).append(" )\n");

        buffer.append("    .unused12             = ")
            .append("0x")
            .append(HexDump.toHex((int)getUnused12()))
            .append(" (").append(getUnused12()).append(" )\n");

        buffer.append("    .unused13             = ")
            .append("0x")
            .append(HexDump.toHex((int)getUnused13()))
            .append(" (").append(getUnused13()).append(" )\n");

        buffer.append("    .offsetAutosaveSource = ")
            .append("0x")
            .append(HexDump.toHex((int)getOffsetAutosaveSource()))
            .append(" (").append(getOffsetAutosaveSource()).append(" )\n");

        buffer.append("    .countAutosaveSource  = ")
            .append("0x")
            .append(HexDump.toHex((int)getCountAutosaveSource()))
            .append(" (").append(getCountAutosaveSource()).append(" )\n");

        buffer.append("    .offsetGrpXstAtnOwners = ")
            .append("0x")
            .append(HexDump.toHex((int)getOffsetGrpXstAtnOwners()))
            .append(" (").append(getOffsetGrpXstAtnOwners()).append(" )\n");

        buffer.append("    .countGrpXstAtnOwners = ")
            .append("0x")
            .append(HexDump.toHex((int)getCountGrpXstAtnOwners()))
            .append(" (").append(getCountGrpXstAtnOwners()).append(" )\n");

        buffer.append("    .offsetSttbfAtnbkmk   = ")
            .append("0x")
            .append(HexDump.toHex((int)getOffsetSttbfAtnbkmk()))
            .append(" (").append(getOffsetSttbfAtnbkmk()).append(" )\n");

        buffer.append("    .lengthSttbfAtnbkmk   = ")
            .append("0x")
            .append(HexDump.toHex((int)getLengthSttbfAtnbkmk()))
            .append(" (").append(getLengthSttbfAtnbkmk()).append(" )\n");

        buffer.append("    .unused14             = ")
            .append("0x")
            .append(HexDump.toHex((int)getUnused14()))
            .append(" (").append(getUnused14()).append(" )\n");

        buffer.append("    .unused15             = ")
            .append("0x")
            .append(HexDump.toHex((int)getUnused15()))
            .append(" (").append(getUnused15()).append(" )\n");

        buffer.append("    .unused16             = ")
            .append("0x")
            .append(HexDump.toHex((int)getUnused16()))
            .append(" (").append(getUnused16()).append(" )\n");

        buffer.append("    .unused17             = ")
            .append("0x")
            .append(HexDump.toHex((int)getUnused17()))
            .append(" (").append(getUnused17()).append(" )\n");

        buffer.append("    .offsetPlcspaMom      = ")
            .append("0x")
            .append(HexDump.toHex((int)getOffsetPlcspaMom()))
            .append(" (").append(getOffsetPlcspaMom()).append(" )\n");

        buffer.append("    .lengthPlcspaMom      = ")
            .append("0x")
            .append(HexDump.toHex((int)getLengthPlcspaMom()))
            .append(" (").append(getLengthPlcspaMom()).append(" )\n");

        buffer.append("    .offsetPlcspaHdr      = ")
            .append("0x")
            .append(HexDump.toHex((int)getOffsetPlcspaHdr()))
            .append(" (").append(getOffsetPlcspaHdr()).append(" )\n");

        buffer.append("    .lengthPlcspaHdr      = ")
            .append("0x")
            .append(HexDump.toHex((int)getLengthPlcspaHdr()))
            .append(" (").append(getLengthPlcspaHdr()).append(" )\n");

        buffer.append("    .lengthPlcfAnnBkmrkFirst = ")
            .append("0x")
            .append(HexDump.toHex((int)getLengthPlcfAnnBkmrkFirst()))
            .append(" (").append(getLengthPlcfAnnBkmrkFirst()).append(" )\n");

        buffer.append("    .offsetPlcfAnnBkmrkFirst = ")
            .append("0x")
            .append(HexDump.toHex((int)getOffsetPlcfAnnBkmrkFirst()))
            .append(" (").append(getOffsetPlcfAnnBkmrkFirst()).append(" )\n");

        buffer.append("    .lengthPlcfAnnBkarkLast = ")
            .append("0x")
            .append(HexDump.toHex((int)getLengthPlcfAnnBkarkLast()))
            .append(" (").append(getLengthPlcfAnnBkarkLast()).append(" )\n");

        buffer.append("    .PlcfAtnbkl           = ")
            .append("0x")
            .append(HexDump.toHex((int)getPlcfAtnbkl()))
            .append(" (").append(getPlcfAtnbkl()).append(" )\n");

        buffer.append("    .fcPms                = ")
            .append("0x")
            .append(HexDump.toHex((int)getFcPms()))
            .append(" (").append(getFcPms()).append(" )\n");

        buffer.append("    .lcbPms               = ")
            .append("0x")
            .append(HexDump.toHex((int)getLcbPms()))
            .append(" (").append(getLcbPms()).append(" )\n");

        buffer.append("    .fcFormFldSttbs       = ")
            .append("0x")
            .append(HexDump.toHex((int)getFcFormFldSttbs()))
            .append(" (").append(getFcFormFldSttbs()).append(" )\n");

        buffer.append("    .lcbFormFldSttbs      = ")
            .append("0x")
            .append(HexDump.toHex((int)getLcbFormFldSttbs()))
            .append(" (").append(getLcbFormFldSttbs()).append(" )\n");

        buffer.append("    .fcPlcfendRef         = ")
            .append("0x")
            .append(HexDump.toHex((int)getFcPlcfendRef()))
            .append(" (").append(getFcPlcfendRef()).append(" )\n");

        buffer.append("    .lcbPlcfendRef        = ")
            .append("0x")
            .append(HexDump.toHex((int)getLcbPlcfendRef()))
            .append(" (").append(getLcbPlcfendRef()).append(" )\n");

        buffer.append("    .fcPlcfendTxt         = ")
            .append("0x")
            .append(HexDump.toHex((int)getFcPlcfendTxt()))
            .append(" (").append(getFcPlcfendTxt()).append(" )\n");

        buffer.append("    .lcbPlcfendTxt        = ")
            .append("0x")
            .append(HexDump.toHex((int)getLcbPlcfendTxt()))
            .append(" (").append(getLcbPlcfendTxt()).append(" )\n");

        buffer.append("    .fcPlcffldEdn         = ")
            .append("0x")
            .append(HexDump.toHex((int)getFcPlcffldEdn()))
            .append(" (").append(getFcPlcffldEdn()).append(" )\n");

        buffer.append("    .lcbPlcffldEdn        = ")
            .append("0x")
            .append(HexDump.toHex((int)getLcbPlcffldEdn()))
            .append(" (").append(getLcbPlcffldEdn()).append(" )\n");

        buffer.append("    .fcPlcfpgdEdn         = ")
            .append("0x")
            .append(HexDump.toHex((int)getFcPlcfpgdEdn()))
            .append(" (").append(getFcPlcfpgdEdn()).append(" )\n");

        buffer.append("    .lcbPlcfpgdEdn        = ")
            .append("0x")
            .append(HexDump.toHex((int)getLcbPlcfpgdEdn()))
            .append(" (").append(getLcbPlcfpgdEdn()).append(" )\n");

        buffer.append("    .fcDggInfo            = ")
            .append("0x")
            .append(HexDump.toHex((int)getFcDggInfo()))
            .append(" (").append(getFcDggInfo()).append(" )\n");

        buffer.append("    .lcbDggInfo           = ")
            .append("0x")
            .append(HexDump.toHex((int)getLcbDggInfo()))
            .append(" (").append(getLcbDggInfo()).append(" )\n");

        buffer.append("    .fcSttbfRMark         = ")
            .append("0x")
            .append(HexDump.toHex((int)getFcSttbfRMark()))
            .append(" (").append(getFcSttbfRMark()).append(" )\n");

        buffer.append("    .lcbSttbfRMark        = ")
            .append("0x")
            .append(HexDump.toHex((int)getLcbSttbfRMark()))
            .append(" (").append(getLcbSttbfRMark()).append(" )\n");

        buffer.append("    .fcSttbCaption        = ")
            .append("0x")
            .append(HexDump.toHex((int)getFcSttbCaption()))
            .append(" (").append(getFcSttbCaption()).append(" )\n");

        buffer.append("    .lcbSttbCaption       = ")
            .append("0x")
            .append(HexDump.toHex((int)getLcbSttbCaption()))
            .append(" (").append(getLcbSttbCaption()).append(" )\n");

        buffer.append("    .fcSttbAutoCaption    = ")
            .append("0x")
            .append(HexDump.toHex((int)getFcSttbAutoCaption()))
            .append(" (").append(getFcSttbAutoCaption()).append(" )\n");

        buffer.append("    .lcbSttbAutoCaption   = ")
            .append("0x")
            .append(HexDump.toHex((int)getLcbSttbAutoCaption()))
            .append(" (").append(getLcbSttbAutoCaption()).append(" )\n");

        buffer.append("    .fcPlcfwkb            = ")
            .append("0x")
            .append(HexDump.toHex((int)getFcPlcfwkb()))
            .append(" (").append(getFcPlcfwkb()).append(" )\n");

        buffer.append("    .lcbPlcfwkb           = ")
            .append("0x")
            .append(HexDump.toHex((int)getLcbPlcfwkb()))
            .append(" (").append(getLcbPlcfwkb()).append(" )\n");

        buffer.append("    .fcPlcfsplfcPlcfspl   = ")
            .append("0x")
            .append(HexDump.toHex((int)getFcPlcfsplfcPlcfspl()))
            .append(" (").append(getFcPlcfsplfcPlcfspl()).append(" )\n");

        buffer.append("    .lcbPlcfspl           = ")
            .append("0x")
            .append(HexDump.toHex((int)getLcbPlcfspl()))
            .append(" (").append(getLcbPlcfspl()).append(" )\n");

        buffer.append("    .fcPlcftxbxTxt        = ")
            .append("0x")
            .append(HexDump.toHex((int)getFcPlcftxbxTxt()))
            .append(" (").append(getFcPlcftxbxTxt()).append(" )\n");

        buffer.append("    .lcbPlcftxbxTxt       = ")
            .append("0x")
            .append(HexDump.toHex((int)getLcbPlcftxbxTxt()))
            .append(" (").append(getLcbPlcftxbxTxt()).append(" )\n");

        buffer.append("    .fcPlcffldTxbx        = ")
            .append("0x")
            .append(HexDump.toHex((int)getFcPlcffldTxbx()))
            .append(" (").append(getFcPlcffldTxbx()).append(" )\n");

        buffer.append("    .lcbPlcffldTxbx       = ")
            .append("0x")
            .append(HexDump.toHex((int)getLcbPlcffldTxbx()))
            .append(" (").append(getLcbPlcffldTxbx()).append(" )\n");

        buffer.append("    .fcPlcfhdrtxbxTxt     = ")
            .append("0x")
            .append(HexDump.toHex((int)getFcPlcfhdrtxbxTxt()))
            .append(" (").append(getFcPlcfhdrtxbxTxt()).append(" )\n");

        buffer.append("    .lcbPlcfhdrtxbxTxt    = ")
            .append("0x")
            .append(HexDump.toHex((int)getLcbPlcfhdrtxbxTxt()))
            .append(" (").append(getLcbPlcfhdrtxbxTxt()).append(" )\n");

        buffer.append("    .fcPlcffldHdrTxbx     = ")
            .append("0x")
            .append(HexDump.toHex((int)getFcPlcffldHdrTxbx()))
            .append(" (").append(getFcPlcffldHdrTxbx()).append(" )\n");

        buffer.append("    .lcbPlcffldHdrTxbx    = ")
            .append("0x")
            .append(HexDump.toHex((int)getLcbPlcffldHdrTxbx()))
            .append(" (").append(getLcbPlcffldHdrTxbx()).append(" )\n");

        buffer.append("    .fcStwUser            = ")
            .append("0x")
            .append(HexDump.toHex((int)getFcStwUser()))
            .append(" (").append(getFcStwUser()).append(" )\n");

        buffer.append("    .lcbStwUser           = ")
            .append("0x")
            .append(HexDump.toHex((int)getLcbStwUser()))
            .append(" (").append(getLcbStwUser()).append(" )\n");

        buffer.append("    .fcSttbttmbd          = ")
            .append("0x")
            .append(HexDump.toHex((int)getFcSttbttmbd()))
            .append(" (").append(getFcSttbttmbd()).append(" )\n");

        buffer.append("    .cbSttbttmbd          = ")
            .append("0x")
            .append(HexDump.toHex((int)getCbSttbttmbd()))
            .append(" (").append(getCbSttbttmbd()).append(" )\n");

        buffer.append("    .fcUnused             = ")
            .append("0x")
            .append(HexDump.toHex((int)getFcUnused()))
            .append(" (").append(getFcUnused()).append(" )\n");

        buffer.append("    .lcbUnused            = ")
            .append("0x")
            .append(HexDump.toHex((int)getLcbUnused()))
            .append(" (").append(getLcbUnused()).append(" )\n");

        buffer.append("    .rgpgdbkd             = ")
            .append("0x")
            .append(HexDump.toHex((int)getRgpgdbkd()))
            .append(" (").append(getRgpgdbkd()).append(" )\n");

        buffer.append("    .fcPgdMother          = ")
            .append("0x")
            .append(HexDump.toHex((int)getFcPgdMother()))
            .append(" (").append(getFcPgdMother()).append(" )\n");

        buffer.append("    .lcbPgdMother         = ")
            .append("0x")
            .append(HexDump.toHex((int)getLcbPgdMother()))
            .append(" (").append(getLcbPgdMother()).append(" )\n");

        buffer.append("    .fcBkdMother          = ")
            .append("0x")
            .append(HexDump.toHex((int)getFcBkdMother()))
            .append(" (").append(getFcBkdMother()).append(" )\n");

        buffer.append("    .lcbBkdMother         = ")
            .append("0x")
            .append(HexDump.toHex((int)getLcbBkdMother()))
            .append(" (").append(getLcbBkdMother()).append(" )\n");

        buffer.append("    .fcPgdFtn             = ")
            .append("0x")
            .append(HexDump.toHex((int)getFcPgdFtn()))
            .append(" (").append(getFcPgdFtn()).append(" )\n");

        buffer.append("    .lcbPgdFtn            = ")
            .append("0x")
            .append(HexDump.toHex((int)getLcbPgdFtn()))
            .append(" (").append(getLcbPgdFtn()).append(" )\n");

        buffer.append("    .fcBkdFtn             = ")
            .append("0x")
            .append(HexDump.toHex((int)getFcBkdFtn()))
            .append(" (").append(getFcBkdFtn()).append(" )\n");

        buffer.append("    .lcbBkdFtn            = ")
            .append("0x")
            .append(HexDump.toHex((int)getLcbBkdFtn()))
            .append(" (").append(getLcbBkdFtn()).append(" )\n");

        buffer.append("    .fcPgdEdn             = ")
            .append("0x")
            .append(HexDump.toHex((int)getFcPgdEdn()))
            .append(" (").append(getFcPgdEdn()).append(" )\n");

        buffer.append("    .lcbPgdEdn            = ")
            .append("0x")
            .append(HexDump.toHex((int)getLcbPgdEdn()))
            .append(" (").append(getLcbPgdEdn()).append(" )\n");

        buffer.append("    .fcBkdEdn             = ")
            .append("0x")
            .append(HexDump.toHex((int)getFcBkdEdn()))
            .append(" (").append(getFcBkdEdn()).append(" )\n");

        buffer.append("    .lcbBkdEdn            = ")
            .append("0x")
            .append(HexDump.toHex((int)getLcbBkdEdn()))
            .append(" (").append(getLcbBkdEdn()).append(" )\n");

        buffer.append("    .fcSttbfIntlFld       = ")
            .append("0x")
            .append(HexDump.toHex((int)getFcSttbfIntlFld()))
            .append(" (").append(getFcSttbfIntlFld()).append(" )\n");

        buffer.append("    .lcbSttbfIntlFld      = ")
            .append("0x")
            .append(HexDump.toHex((int)getLcbSttbfIntlFld()))
            .append(" (").append(getLcbSttbfIntlFld()).append(" )\n");

        buffer.append("    .fcRouteSlip          = ")
            .append("0x")
            .append(HexDump.toHex((int)getFcRouteSlip()))
            .append(" (").append(getFcRouteSlip()).append(" )\n");

        buffer.append("    .lcbRouteSlip         = ")
            .append("0x")
            .append(HexDump.toHex((int)getLcbRouteSlip()))
            .append(" (").append(getLcbRouteSlip()).append(" )\n");

        buffer.append("    .fcSttbSavedBy        = ")
            .append("0x")
            .append(HexDump.toHex((int)getFcSttbSavedBy()))
            .append(" (").append(getFcSttbSavedBy()).append(" )\n");

        buffer.append("    .lcbSttbSavedBy       = ")
            .append("0x")
            .append(HexDump.toHex((int)getLcbSttbSavedBy()))
            .append(" (").append(getLcbSttbSavedBy()).append(" )\n");

        buffer.append("    .fcSttbFnm            = ")
            .append("0x")
            .append(HexDump.toHex((int)getFcSttbFnm()))
            .append(" (").append(getFcSttbFnm()).append(" )\n");

        buffer.append("    .lcbSttbFnm           = ")
            .append("0x")
            .append(HexDump.toHex((int)getLcbSttbFnm()))
            .append(" (").append(getLcbSttbFnm()).append(" )\n");

        buffer.append("    .fcPlcfLst            = ")
            .append("0x")
            .append(HexDump.toHex((int)getFcPlcfLst()))
            .append(" (").append(getFcPlcfLst()).append(" )\n");

        buffer.append("    .lcbPlcfLst           = ")
            .append("0x")
            .append(HexDump.toHex((int)getLcbPlcfLst()))
            .append(" (").append(getLcbPlcfLst()).append(" )\n");

        buffer.append("    .fcPlfLfo             = ")
            .append("0x")
            .append(HexDump.toHex((int)getFcPlfLfo()))
            .append(" (").append(getFcPlfLfo()).append(" )\n");

        buffer.append("    .lcbPlfLfo            = ")
            .append("0x")
            .append(HexDump.toHex((int)getLcbPlfLfo()))
            .append(" (").append(getLcbPlfLfo()).append(" )\n");

        buffer.append("    .fcPlcftxbxBkd        = ")
            .append("0x")
            .append(HexDump.toHex((int)getFcPlcftxbxBkd()))
            .append(" (").append(getFcPlcftxbxBkd()).append(" )\n");

        buffer.append("    .lcbPlcftxbxBkd       = ")
            .append("0x")
            .append(HexDump.toHex((int)getLcbPlcftxbxBkd()))
            .append(" (").append(getLcbPlcftxbxBkd()).append(" )\n");

        buffer.append("    .fcPlcftxbxHdrBkd     = ")
            .append("0x")
            .append(HexDump.toHex((int)getFcPlcftxbxHdrBkd()))
            .append(" (").append(getFcPlcftxbxHdrBkd()).append(" )\n");

        buffer.append("    .lcbPlcftxbxHdrBkd    = ")
            .append("0x")
            .append(HexDump.toHex((int)getLcbPlcftxbxHdrBkd()))
            .append(" (").append(getLcbPlcftxbxHdrBkd()).append(" )\n");

        buffer.append("    .fcDocUndo            = ")
            .append("0x")
            .append(HexDump.toHex((int)getFcDocUndo()))
            .append(" (").append(getFcDocUndo()).append(" )\n");

        buffer.append("    .lcbDocUndo           = ")
            .append("0x")
            .append(HexDump.toHex((int)getLcbDocUndo()))
            .append(" (").append(getLcbDocUndo()).append(" )\n");

        buffer.append("    .fcRgbuse             = ")
            .append("0x")
            .append(HexDump.toHex((int)getFcRgbuse()))
            .append(" (").append(getFcRgbuse()).append(" )\n");

        buffer.append("    .lcbRgbuse            = ")
            .append("0x")
            .append(HexDump.toHex((int)getLcbRgbuse()))
            .append(" (").append(getLcbRgbuse()).append(" )\n");

        buffer.append("    .fcUsp                = ")
            .append("0x")
            .append(HexDump.toHex((int)getFcUsp()))
            .append(" (").append(getFcUsp()).append(" )\n");

        buffer.append("    .lcbUsp               = ")
            .append("0x")
            .append(HexDump.toHex((int)getLcbUsp()))
            .append(" (").append(getLcbUsp()).append(" )\n");

        buffer.append("    .fcUskf               = ")
            .append("0x")
            .append(HexDump.toHex((int)getFcUskf()))
            .append(" (").append(getFcUskf()).append(" )\n");

        buffer.append("    .lcbUskf              = ")
            .append("0x")
            .append(HexDump.toHex((int)getLcbUskf()))
            .append(" (").append(getLcbUskf()).append(" )\n");

        buffer.append("    .fcPlcupcRgbuse       = ")
            .append("0x")
            .append(HexDump.toHex((int)getFcPlcupcRgbuse()))
            .append(" (").append(getFcPlcupcRgbuse()).append(" )\n");

        buffer.append("    .lcbPlcupcRgbuse      = ")
            .append("0x")
            .append(HexDump.toHex((int)getLcbPlcupcRgbuse()))
            .append(" (").append(getLcbPlcupcRgbuse()).append(" )\n");

        buffer.append("    .fcPlcupcUsp          = ")
            .append("0x")
            .append(HexDump.toHex((int)getFcPlcupcUsp()))
            .append(" (").append(getFcPlcupcUsp()).append(" )\n");

        buffer.append("    .lcbPlcupcUsp         = ")
            .append("0x")
            .append(HexDump.toHex((int)getLcbPlcupcUsp()))
            .append(" (").append(getLcbPlcupcUsp()).append(" )\n");

        buffer.append("    .fcSttbGlsyStyle      = ")
            .append("0x")
            .append(HexDump.toHex((int)getFcSttbGlsyStyle()))
            .append(" (").append(getFcSttbGlsyStyle()).append(" )\n");

        buffer.append("    .lcbSttbGlsyStyle     = ")
            .append("0x")
            .append(HexDump.toHex((int)getLcbSttbGlsyStyle()))
            .append(" (").append(getLcbSttbGlsyStyle()).append(" )\n");

        buffer.append("    .fcPlgosl             = ")
            .append("0x")
            .append(HexDump.toHex((int)getFcPlgosl()))
            .append(" (").append(getFcPlgosl()).append(" )\n");

        buffer.append("    .lcbPlgosl            = ")
            .append("0x")
            .append(HexDump.toHex((int)getLcbPlgosl()))
            .append(" (").append(getLcbPlgosl()).append(" )\n");

        buffer.append("    .fcPlcocx             = ")
            .append("0x")
            .append(HexDump.toHex((int)getFcPlcocx()))
            .append(" (").append(getFcPlcocx()).append(" )\n");

        buffer.append("    .lcbPlcocx            = ")
            .append("0x")
            .append(HexDump.toHex((int)getLcbPlcocx()))
            .append(" (").append(getLcbPlcocx()).append(" )\n");

        buffer.append("    .fcPlcfbteLvc         = ")
            .append("0x")
            .append(HexDump.toHex((int)getFcPlcfbteLvc()))
            .append(" (").append(getFcPlcfbteLvc()).append(" )\n");

        buffer.append("    .lcbPlcfbteLvc        = ")
            .append("0x")
            .append(HexDump.toHex((int)getLcbPlcfbteLvc()))
            .append(" (").append(getLcbPlcfbteLvc()).append(" )\n");

        buffer.append("    .ftModified           = ")
            .append("0x")
            .append(HexDump.toHex((int)getFtModified()))
            .append(" (").append(getFtModified()).append(" )\n");

        buffer.append("    .dwLowDateTime        = ")
            .append("0x")
            .append(HexDump.toHex((int)getDwLowDateTime()))
            .append(" (").append(getDwLowDateTime()).append(" )\n");

        buffer.append("    .dwHighDateTime       = ")
            .append("0x")
            .append(HexDump.toHex((int)getDwHighDateTime()))
            .append(" (").append(getDwHighDateTime()).append(" )\n");

        buffer.append("    .fcPlcflvc            = ")
            .append("0x")
            .append(HexDump.toHex((int)getFcPlcflvc()))
            .append(" (").append(getFcPlcflvc()).append(" )\n");

        buffer.append("    .lcbPlcflvc           = ")
            .append("0x")
            .append(HexDump.toHex((int)getLcbPlcflvc()))
            .append(" (").append(getLcbPlcflvc()).append(" )\n");

        buffer.append("    .fcPlcasumy           = ")
            .append("0x")
            .append(HexDump.toHex((int)getFcPlcasumy()))
            .append(" (").append(getFcPlcasumy()).append(" )\n");

        buffer.append("    .lcbPlcasumy          = ")
            .append("0x")
            .append(HexDump.toHex((int)getLcbPlcasumy()))
            .append(" (").append(getLcbPlcasumy()).append(" )\n");

        buffer.append("    .fcPlcfgram           = ")
            .append("0x")
            .append(HexDump.toHex((int)getFcPlcfgram()))
            .append(" (").append(getFcPlcfgram()).append(" )\n");

        buffer.append("    .lcbPlcfgram          = ")
            .append("0x")
            .append(HexDump.toHex((int)getLcbPlcfgram()))
            .append(" (").append(getLcbPlcfgram()).append(" )\n");

        buffer.append("    .fcSttbListNames      = ")
            .append("0x")
            .append(HexDump.toHex((int)getFcSttbListNames()))
            .append(" (").append(getFcSttbListNames()).append(" )\n");

        buffer.append("    .lcbSttbListNames     = ")
            .append("0x")
            .append(HexDump.toHex((int)getLcbSttbListNames()))
            .append(" (").append(getLcbSttbListNames()).append(" )\n");

        buffer.append("    .fcSttbfUssr          = ")
            .append("0x")
            .append(HexDump.toHex((int)getFcSttbfUssr()))
            .append(" (").append(getFcSttbfUssr()).append(" )\n");

        buffer.append("    .lcbSttbfUssr         = ")
            .append("0x")
            .append(HexDump.toHex((int)getLcbSttbfUssr()))
            .append(" (").append(getLcbSttbfUssr()).append(" )\n");

        buffer.append("[/FIB]\n");
        return buffer.toString();
    }

    /**
     * Size of record (exluding 4 byte header)
     */
    public int getSize()
    {
        return 4 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 4 + 4 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 2 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4;
    }



    /**
     * Get the id field for the FIB record.
     */
    public short getId()
    {
        return field_1_id;
    }

    /**
     * Set the id field for the FIB record.
     */
    public void setId(short field_1_id)
    {
        this.field_1_id = field_1_id;
    }

    /**
     * Get the version field for the FIB record.
     */
    public short getVersion()
    {
        return field_2_version;
    }

    /**
     * Set the version field for the FIB record.
     */
    public void setVersion(short field_2_version)
    {
        this.field_2_version = field_2_version;
    }

    /**
     * Get the product version field for the FIB record.
     */
    public short getProductVersion()
    {
        return field_3_productVersion;
    }

    /**
     * Set the product version field for the FIB record.
     */
    public void setProductVersion(short field_3_productVersion)
    {
        this.field_3_productVersion = field_3_productVersion;
    }

    /**
     * Get the language stamp field for the FIB record.
     */
    public short getLanguageStamp()
    {
        return field_4_languageStamp;
    }

    /**
     * Set the language stamp field for the FIB record.
     */
    public void setLanguageStamp(short field_4_languageStamp)
    {
        this.field_4_languageStamp = field_4_languageStamp;
    }

    /**
     * Get the unknown 0 field for the FIB record.
     */
    public short getUnknown0()
    {
        return field_5_unknown0;
    }

    /**
     * Set the unknown 0 field for the FIB record.
     */
    public void setUnknown0(short field_5_unknown0)
    {
        this.field_5_unknown0 = field_5_unknown0;
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
     * Get the minversion field for the FIB record.
     */
    public short getMinversion()
    {
        return field_7_minversion;
    }

    /**
     * Set the minversion field for the FIB record.
     */
    public void setMinversion(short field_7_minversion)
    {
        this.field_7_minversion = field_7_minversion;
    }

    /**
     * Get the encrypted key field for the FIB record.
     */
    public short getEncryptedKey()
    {
        return field_8_encryptedKey;
    }

    /**
     * Set the encrypted key field for the FIB record.
     */
    public void setEncryptedKey(short field_8_encryptedKey)
    {
        this.field_8_encryptedKey = field_8_encryptedKey;
    }

    /**
     * Get the environment field for the FIB record.
     */
    public short getEnvironment()
    {
        return field_9_environment;
    }

    /**
     * Set the environment field for the FIB record.
     */
    public void setEnvironment(short field_9_environment)
    {
        this.field_9_environment = field_9_environment;
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
     * Get the default charset field for the FIB record.
     */
    public short getDefaultCharset()
    {
        return field_11_defaultCharset;
    }

    /**
     * Set the default charset field for the FIB record.
     */
    public void setDefaultCharset(short field_11_defaultCharset)
    {
        this.field_11_defaultCharset = field_11_defaultCharset;
    }

    /**
     * Get the default extcharset field for the FIB record.
     */
    public short getDefaultExtcharset()
    {
        return field_12_defaultExtcharset;
    }

    /**
     * Set the default extcharset field for the FIB record.
     */
    public void setDefaultExtcharset(short field_12_defaultExtcharset)
    {
        this.field_12_defaultExtcharset = field_12_defaultExtcharset;
    }

    /**
     * Get the offset first char field for the FIB record.
     */
    public int getOffsetFirstChar()
    {
        return field_13_offsetFirstChar;
    }

    /**
     * Set the offset first char field for the FIB record.
     */
    public void setOffsetFirstChar(int field_13_offsetFirstChar)
    {
        this.field_13_offsetFirstChar = field_13_offsetFirstChar;
    }

    /**
     * Get the offset last char field for the FIB record.
     */
    public int getOffsetLastChar()
    {
        return field_14_offsetLastChar;
    }

    /**
     * Set the offset last char field for the FIB record.
     */
    public void setOffsetLastChar(int field_14_offsetLastChar)
    {
        this.field_14_offsetLastChar = field_14_offsetLastChar;
    }

    /**
     * Get the count shorts field for the FIB record.
     */
    public short getCountShorts()
    {
        return field_15_countShorts;
    }

    /**
     * Set the count shorts field for the FIB record.
     */
    public void setCountShorts(short field_15_countShorts)
    {
        this.field_15_countShorts = field_15_countShorts;
    }

    /**
     * Get the creator id or beg shorts field for the FIB record.
     */
    public short getCreatorIdOrBegShorts()
    {
        return field_16_creatorIdOrBegShorts;
    }

    /**
     * Set the creator id or beg shorts field for the FIB record.
     */
    public void setCreatorIdOrBegShorts(short field_16_creatorIdOrBegShorts)
    {
        this.field_16_creatorIdOrBegShorts = field_16_creatorIdOrBegShorts;
    }

    /**
     * Get the revisor id field for the FIB record.
     */
    public short getRevisorId()
    {
        return field_17_revisorId;
    }

    /**
     * Set the revisor id field for the FIB record.
     */
    public void setRevisorId(short field_17_revisorId)
    {
        this.field_17_revisorId = field_17_revisorId;
    }

    /**
     * Get the creator private field for the FIB record.
     */
    public short getCreatorPrivate()
    {
        return field_18_creatorPrivate;
    }

    /**
     * Set the creator private field for the FIB record.
     */
    public void setCreatorPrivate(short field_18_creatorPrivate)
    {
        this.field_18_creatorPrivate = field_18_creatorPrivate;
    }

    /**
     * Get the revisor private field for the FIB record.
     */
    public short getRevisorPrivate()
    {
        return field_19_revisorPrivate;
    }

    /**
     * Set the revisor private field for the FIB record.
     */
    public void setRevisorPrivate(short field_19_revisorPrivate)
    {
        this.field_19_revisorPrivate = field_19_revisorPrivate;
    }

    /**
     * Get the unused1 field for the FIB record.
     */
    public short getUnused1()
    {
        return field_20_unused1;
    }

    /**
     * Set the unused1 field for the FIB record.
     */
    public void setUnused1(short field_20_unused1)
    {
        this.field_20_unused1 = field_20_unused1;
    }

    /**
     * Get the unused2 field for the FIB record.
     */
    public short getUnused2()
    {
        return field_21_unused2;
    }

    /**
     * Set the unused2 field for the FIB record.
     */
    public void setUnused2(short field_21_unused2)
    {
        this.field_21_unused2 = field_21_unused2;
    }

    /**
     * Get the unused3 field for the FIB record.
     */
    public short getUnused3()
    {
        return field_22_unused3;
    }

    /**
     * Set the unused3 field for the FIB record.
     */
    public void setUnused3(short field_22_unused3)
    {
        this.field_22_unused3 = field_22_unused3;
    }

    /**
     * Get the unused4 field for the FIB record.
     */
    public short getUnused4()
    {
        return field_23_unused4;
    }

    /**
     * Set the unused4 field for the FIB record.
     */
    public void setUnused4(short field_23_unused4)
    {
        this.field_23_unused4 = field_23_unused4;
    }

    /**
     * Get the unused5 field for the FIB record.
     */
    public short getUnused5()
    {
        return field_24_unused5;
    }

    /**
     * Set the unused5 field for the FIB record.
     */
    public void setUnused5(short field_24_unused5)
    {
        this.field_24_unused5 = field_24_unused5;
    }

    /**
     * Get the unused6 field for the FIB record.
     */
    public short getUnused6()
    {
        return field_25_unused6;
    }

    /**
     * Set the unused6 field for the FIB record.
     */
    public void setUnused6(short field_25_unused6)
    {
        this.field_25_unused6 = field_25_unused6;
    }

    /**
     * Get the unused7 field for the FIB record.
     */
    public short getUnused7()
    {
        return field_26_unused7;
    }

    /**
     * Set the unused7 field for the FIB record.
     */
    public void setUnused7(short field_26_unused7)
    {
        this.field_26_unused7 = field_26_unused7;
    }

    /**
     * Get the unused8 field for the FIB record.
     */
    public short getUnused8()
    {
        return field_27_unused8;
    }

    /**
     * Set the unused8 field for the FIB record.
     */
    public void setUnused8(short field_27_unused8)
    {
        this.field_27_unused8 = field_27_unused8;
    }

    /**
     * Get the unused9 field for the FIB record.
     */
    public short getUnused9()
    {
        return field_28_unused9;
    }

    /**
     * Set the unused9 field for the FIB record.
     */
    public void setUnused9(short field_28_unused9)
    {
        this.field_28_unused9 = field_28_unused9;
    }

    /**
     * Get the fareastid field for the FIB record.
     */
    public short getFareastid()
    {
        return field_29_fareastid;
    }

    /**
     * Set the fareastid field for the FIB record.
     */
    public void setFareastid(short field_29_fareastid)
    {
        this.field_29_fareastid = field_29_fareastid;
    }

    /**
     * Get the countints field for the FIB record.
     */
    public short getCountints()
    {
        return field_30_countints;
    }

    /**
     * Set the countints field for the FIB record.
     */
    public void setCountints(short field_30_countints)
    {
        this.field_30_countints = field_30_countints;
    }

    /**
     * Get the last byte or beg ints field for the FIB record.
     */
    public int getLastByteOrBegInts()
    {
        return field_31_lastByteOrBegInts;
    }

    /**
     * Set the last byte or beg ints field for the FIB record.
     */
    public void setLastByteOrBegInts(int field_31_lastByteOrBegInts)
    {
        this.field_31_lastByteOrBegInts = field_31_lastByteOrBegInts;
    }

    /**
     * Get the creator build date field for the FIB record.
     */
    public int getCreatorBuildDate()
    {
        return field_32_creatorBuildDate;
    }

    /**
     * Set the creator build date field for the FIB record.
     */
    public void setCreatorBuildDate(int field_32_creatorBuildDate)
    {
        this.field_32_creatorBuildDate = field_32_creatorBuildDate;
    }

    /**
     * Get the revisor build date field for the FIB record.
     */
    public int getRevisorBuildDate()
    {
        return field_33_revisorBuildDate;
    }

    /**
     * Set the revisor build date field for the FIB record.
     */
    public void setRevisorBuildDate(int field_33_revisorBuildDate)
    {
        this.field_33_revisorBuildDate = field_33_revisorBuildDate;
    }

    /**
     * Get the main streamlen field for the FIB record.
     */
    public int getMainStreamlen()
    {
        return field_34_mainStreamlen;
    }

    /**
     * Set the main streamlen field for the FIB record.
     */
    public void setMainStreamlen(int field_34_mainStreamlen)
    {
        this.field_34_mainStreamlen = field_34_mainStreamlen;
    }

    /**
     * Get the footnote streamlen field for the FIB record.
     */
    public int getFootnoteStreamlen()
    {
        return field_35_footnoteStreamlen;
    }

    /**
     * Set the footnote streamlen field for the FIB record.
     */
    public void setFootnoteStreamlen(int field_35_footnoteStreamlen)
    {
        this.field_35_footnoteStreamlen = field_35_footnoteStreamlen;
    }

    /**
     * Get the header streamlen field for the FIB record.
     */
    public int getHeaderStreamlen()
    {
        return field_36_headerStreamlen;
    }

    /**
     * Set the header streamlen field for the FIB record.
     */
    public void setHeaderStreamlen(int field_36_headerStreamlen)
    {
        this.field_36_headerStreamlen = field_36_headerStreamlen;
    }

    /**
     * Get the macro streamlen field for the FIB record.
     */
    public int getMacroStreamlen()
    {
        return field_37_macroStreamlen;
    }

    /**
     * Set the macro streamlen field for the FIB record.
     */
    public void setMacroStreamlen(int field_37_macroStreamlen)
    {
        this.field_37_macroStreamlen = field_37_macroStreamlen;
    }

    /**
     * Get the annotation streamlen field for the FIB record.
     */
    public int getAnnotationStreamlen()
    {
        return field_38_annotationStreamlen;
    }

    /**
     * Set the annotation streamlen field for the FIB record.
     */
    public void setAnnotationStreamlen(int field_38_annotationStreamlen)
    {
        this.field_38_annotationStreamlen = field_38_annotationStreamlen;
    }

    /**
     * Get the endnote streamlen field for the FIB record.
     */
    public int getEndnoteStreamlen()
    {
        return field_39_endnoteStreamlen;
    }

    /**
     * Set the endnote streamlen field for the FIB record.
     */
    public void setEndnoteStreamlen(int field_39_endnoteStreamlen)
    {
        this.field_39_endnoteStreamlen = field_39_endnoteStreamlen;
    }

    /**
     * Get the textbox streamlen field for the FIB record.
     */
    public int getTextboxStreamlen()
    {
        return field_40_textboxStreamlen;
    }

    /**
     * Set the textbox streamlen field for the FIB record.
     */
    public void setTextboxStreamlen(int field_40_textboxStreamlen)
    {
        this.field_40_textboxStreamlen = field_40_textboxStreamlen;
    }

    /**
     * Get the headbox streamlen field for the FIB record.
     */
    public int getHeadboxStreamlen()
    {
        return field_41_headboxStreamlen;
    }

    /**
     * Set the headbox streamlen field for the FIB record.
     */
    public void setHeadboxStreamlen(int field_41_headboxStreamlen)
    {
        this.field_41_headboxStreamlen = field_41_headboxStreamlen;
    }

    /**
     * Get the ptr to plc list chp field for the FIB record.
     */
    public int getPtrToPlcListChp()
    {
        return field_42_ptrToPlcListChp;
    }

    /**
     * Set the ptr to plc list chp field for the FIB record.
     */
    public void setPtrToPlcListChp(int field_42_ptrToPlcListChp)
    {
        this.field_42_ptrToPlcListChp = field_42_ptrToPlcListChp;
    }

    /**
     * Get the first chp field for the FIB record.
     */
    public int getFirstChp()
    {
        return field_43_firstChp;
    }

    /**
     * Set the first chp field for the FIB record.
     */
    public void setFirstChp(int field_43_firstChp)
    {
        this.field_43_firstChp = field_43_firstChp;
    }

    /**
     * Get the count chps field for the FIB record.
     */
    public int getCountChps()
    {
        return field_44_countChps;
    }

    /**
     * Set the count chps field for the FIB record.
     */
    public void setCountChps(int field_44_countChps)
    {
        this.field_44_countChps = field_44_countChps;
    }

    /**
     * Get the ptr to plc list pap field for the FIB record.
     */
    public int getPtrToPlcListPap()
    {
        return field_45_ptrToPlcListPap;
    }

    /**
     * Set the ptr to plc list pap field for the FIB record.
     */
    public void setPtrToPlcListPap(int field_45_ptrToPlcListPap)
    {
        this.field_45_ptrToPlcListPap = field_45_ptrToPlcListPap;
    }

    /**
     * Get the first pap field for the FIB record.
     */
    public int getFirstPap()
    {
        return field_46_firstPap;
    }

    /**
     * Set the first pap field for the FIB record.
     */
    public void setFirstPap(int field_46_firstPap)
    {
        this.field_46_firstPap = field_46_firstPap;
    }

    /**
     * Get the count paps field for the FIB record.
     */
    public int getCountPaps()
    {
        return field_47_countPaps;
    }

    /**
     * Set the count paps field for the FIB record.
     */
    public void setCountPaps(int field_47_countPaps)
    {
        this.field_47_countPaps = field_47_countPaps;
    }

    /**
     * Get the ptr to plc list lvc field for the FIB record.
     */
    public int getPtrToPlcListLvc()
    {
        return field_48_ptrToPlcListLvc;
    }

    /**
     * Set the ptr to plc list lvc field for the FIB record.
     */
    public void setPtrToPlcListLvc(int field_48_ptrToPlcListLvc)
    {
        this.field_48_ptrToPlcListLvc = field_48_ptrToPlcListLvc;
    }

    /**
     * Get the first lvc field for the FIB record.
     */
    public int getFirstLvc()
    {
        return field_49_firstLvc;
    }

    /**
     * Set the first lvc field for the FIB record.
     */
    public void setFirstLvc(int field_49_firstLvc)
    {
        this.field_49_firstLvc = field_49_firstLvc;
    }

    /**
     * Get the count lvc field for the FIB record.
     */
    public int getCountLvc()
    {
        return field_50_countLvc;
    }

    /**
     * Set the count lvc field for the FIB record.
     */
    public void setCountLvc(int field_50_countLvc)
    {
        this.field_50_countLvc = field_50_countLvc;
    }

    /**
     * Get the unknown1 field for the FIB record.
     */
    public int getUnknown1()
    {
        return field_51_unknown1;
    }

    /**
     * Set the unknown1 field for the FIB record.
     */
    public void setUnknown1(int field_51_unknown1)
    {
        this.field_51_unknown1 = field_51_unknown1;
    }

    /**
     * Get the unknown2 field for the FIB record.
     */
    public int getUnknown2()
    {
        return field_52_unknown2;
    }

    /**
     * Set the unknown2 field for the FIB record.
     */
    public void setUnknown2(int field_52_unknown2)
    {
        this.field_52_unknown2 = field_52_unknown2;
    }

    /**
     * Get the lcb array size field for the FIB record.
     */
    public short getLcbArraySize()
    {
        return field_53_lcbArraySize;
    }

    /**
     * Set the lcb array size field for the FIB record.
     */
    public void setLcbArraySize(short field_53_lcbArraySize)
    {
        this.field_53_lcbArraySize = field_53_lcbArraySize;
    }

    /**
     * Get the original stylesheet offset field for the FIB record.
     */
    public int getOriginalStylesheetOffset()
    {
        return field_54_originalStylesheetOffset;
    }

    /**
     * Set the original stylesheet offset field for the FIB record.
     */
    public void setOriginalStylesheetOffset(int field_54_originalStylesheetOffset)
    {
        this.field_54_originalStylesheetOffset = field_54_originalStylesheetOffset;
    }

    /**
     * Get the original stylesheet size field for the FIB record.
     */
    public int getOriginalStylesheetSize()
    {
        return field_55_originalStylesheetSize;
    }

    /**
     * Set the original stylesheet size field for the FIB record.
     */
    public void setOriginalStylesheetSize(int field_55_originalStylesheetSize)
    {
        this.field_55_originalStylesheetSize = field_55_originalStylesheetSize;
    }

    /**
     * Get the stylesheet offset field for the FIB record.
     */
    public int getStylesheetOffset()
    {
        return field_56_stylesheetOffset;
    }

    /**
     * Set the stylesheet offset field for the FIB record.
     */
    public void setStylesheetOffset(int field_56_stylesheetOffset)
    {
        this.field_56_stylesheetOffset = field_56_stylesheetOffset;
    }

    /**
     * Get the stylesheet size field for the FIB record.
     */
    public int getStylesheetSize()
    {
        return field_57_stylesheetSize;
    }

    /**
     * Set the stylesheet size field for the FIB record.
     */
    public void setStylesheetSize(int field_57_stylesheetSize)
    {
        this.field_57_stylesheetSize = field_57_stylesheetSize;
    }

    /**
     * Get the footnote ref offset field for the FIB record.
     */
    public int getFootnoteRefOffset()
    {
        return field_58_footnoteRefOffset;
    }

    /**
     * Set the footnote ref offset field for the FIB record.
     */
    public void setFootnoteRefOffset(int field_58_footnoteRefOffset)
    {
        this.field_58_footnoteRefOffset = field_58_footnoteRefOffset;
    }

    /**
     * Get the footnote ref size field for the FIB record.
     */
    public int getFootnoteRefSize()
    {
        return field_59_footnoteRefSize;
    }

    /**
     * Set the footnote ref size field for the FIB record.
     */
    public void setFootnoteRefSize(int field_59_footnoteRefSize)
    {
        this.field_59_footnoteRefSize = field_59_footnoteRefSize;
    }

    /**
     * Get the plc offset field for the FIB record.
     */
    public int getPlcOffset()
    {
        return field_60_plcOffset;
    }

    /**
     * Set the plc offset field for the FIB record.
     */
    public void setPlcOffset(int field_60_plcOffset)
    {
        this.field_60_plcOffset = field_60_plcOffset;
    }

    /**
     * Get the plc size field for the FIB record.
     */
    public int getPlcSize()
    {
        return field_61_plcSize;
    }

    /**
     * Set the plc size field for the FIB record.
     */
    public void setPlcSize(int field_61_plcSize)
    {
        this.field_61_plcSize = field_61_plcSize;
    }

    /**
     * Get the annotation ref offset field for the FIB record.
     */
    public int getAnnotationRefOffset()
    {
        return field_62_annotationRefOffset;
    }

    /**
     * Set the annotation ref offset field for the FIB record.
     */
    public void setAnnotationRefOffset(int field_62_annotationRefOffset)
    {
        this.field_62_annotationRefOffset = field_62_annotationRefOffset;
    }

    /**
     * Get the annotation ref size field for the FIB record.
     */
    public int getAnnotationRefSize()
    {
        return field_63_annotationRefSize;
    }

    /**
     * Set the annotation ref size field for the FIB record.
     */
    public void setAnnotationRefSize(int field_63_annotationRefSize)
    {
        this.field_63_annotationRefSize = field_63_annotationRefSize;
    }

    /**
     * Get the annotation plc offset field for the FIB record.
     */
    public int getAnnotationPlcOffset()
    {
        return field_64_annotationPlcOffset;
    }

    /**
     * Set the annotation plc offset field for the FIB record.
     */
    public void setAnnotationPlcOffset(int field_64_annotationPlcOffset)
    {
        this.field_64_annotationPlcOffset = field_64_annotationPlcOffset;
    }

    /**
     * Get the annotation plc size field for the FIB record.
     */
    public int getAnnotationPlcSize()
    {
        return field_65_annotationPlcSize;
    }

    /**
     * Set the annotation plc size field for the FIB record.
     */
    public void setAnnotationPlcSize(int field_65_annotationPlcSize)
    {
        this.field_65_annotationPlcSize = field_65_annotationPlcSize;
    }

    /**
     * Get the section plc offset field for the FIB record.
     */
    public int getSectionPlcOffset()
    {
        return field_66_sectionPlcOffset;
    }

    /**
     * Set the section plc offset field for the FIB record.
     */
    public void setSectionPlcOffset(int field_66_sectionPlcOffset)
    {
        this.field_66_sectionPlcOffset = field_66_sectionPlcOffset;
    }

    /**
     * Get the section plc size field for the FIB record.
     */
    public int getSectionPlcSize()
    {
        return field_67_sectionPlcSize;
    }

    /**
     * Set the section plc size field for the FIB record.
     */
    public void setSectionPlcSize(int field_67_sectionPlcSize)
    {
        this.field_67_sectionPlcSize = field_67_sectionPlcSize;
    }

    /**
     * Get the unusedA field for the FIB record.
     */
    public int getUnusedA()
    {
        return field_68_unusedA;
    }

    /**
     * Set the unusedA field for the FIB record.
     */
    public void setUnusedA(int field_68_unusedA)
    {
        this.field_68_unusedA = field_68_unusedA;
    }

    /**
     * Get the unusedB field for the FIB record.
     */
    public int getUnusedB()
    {
        return field_69_unusedB;
    }

    /**
     * Set the unusedB field for the FIB record.
     */
    public void setUnusedB(int field_69_unusedB)
    {
        this.field_69_unusedB = field_69_unusedB;
    }

    /**
     * Get the pheplc offset field for the FIB record.
     */
    public int getPheplcOffset()
    {
        return field_70_pheplcOffset;
    }

    /**
     * Set the pheplc offset field for the FIB record.
     */
    public void setPheplcOffset(int field_70_pheplcOffset)
    {
        this.field_70_pheplcOffset = field_70_pheplcOffset;
    }

    /**
     * Get the pheplc size field for the FIB record.
     */
    public int getPheplcSize()
    {
        return field_71_pheplcSize;
    }

    /**
     * Set the pheplc size field for the FIB record.
     */
    public void setPheplcSize(int field_71_pheplcSize)
    {
        this.field_71_pheplcSize = field_71_pheplcSize;
    }

    /**
     * Get the glossaryST offset field for the FIB record.
     */
    public int getGlossarySTOffset()
    {
        return field_72_glossarySTOffset;
    }

    /**
     * Set the glossaryST offset field for the FIB record.
     */
    public void setGlossarySTOffset(int field_72_glossarySTOffset)
    {
        this.field_72_glossarySTOffset = field_72_glossarySTOffset;
    }

    /**
     * Get the glossaryST size field for the FIB record.
     */
    public int getGlossarySTSize()
    {
        return field_73_glossarySTSize;
    }

    /**
     * Set the glossaryST size field for the FIB record.
     */
    public void setGlossarySTSize(int field_73_glossarySTSize)
    {
        this.field_73_glossarySTSize = field_73_glossarySTSize;
    }

    /**
     * Get the glossaryPLC offset field for the FIB record.
     */
    public int getGlossaryPLCOffset()
    {
        return field_74_glossaryPLCOffset;
    }

    /**
     * Set the glossaryPLC offset field for the FIB record.
     */
    public void setGlossaryPLCOffset(int field_74_glossaryPLCOffset)
    {
        this.field_74_glossaryPLCOffset = field_74_glossaryPLCOffset;
    }

    /**
     * Get the glossaryPLC size field for the FIB record.
     */
    public int getGlossaryPLCSize()
    {
        return field_75_glossaryPLCSize;
    }

    /**
     * Set the glossaryPLC size field for the FIB record.
     */
    public void setGlossaryPLCSize(int field_75_glossaryPLCSize)
    {
        this.field_75_glossaryPLCSize = field_75_glossaryPLCSize;
    }

    /**
     * Get the headerPLC offset field for the FIB record.
     */
    public int getHeaderPLCOffset()
    {
        return field_76_headerPLCOffset;
    }

    /**
     * Set the headerPLC offset field for the FIB record.
     */
    public void setHeaderPLCOffset(int field_76_headerPLCOffset)
    {
        this.field_76_headerPLCOffset = field_76_headerPLCOffset;
    }

    /**
     * Get the headerPLC size field for the FIB record.
     */
    public int getHeaderPLCSize()
    {
        return field_77_headerPLCSize;
    }

    /**
     * Set the headerPLC size field for the FIB record.
     */
    public void setHeaderPLCSize(int field_77_headerPLCSize)
    {
        this.field_77_headerPLCSize = field_77_headerPLCSize;
    }

    /**
     * Get the chp_bin_table_offset field for the FIB record.
     */
    public int getChp_bin_table_offset()
    {
        return field_78_chp_bin_table_offset;
    }

    /**
     * Set the chp_bin_table_offset field for the FIB record.
     */
    public void setChp_bin_table_offset(int field_78_chp_bin_table_offset)
    {
        this.field_78_chp_bin_table_offset = field_78_chp_bin_table_offset;
    }

    /**
     * Get the chp_bin_table_size field for the FIB record.
     */
    public int getChp_bin_table_size()
    {
        return field_79_chp_bin_table_size;
    }

    /**
     * Set the chp_bin_table_size field for the FIB record.
     */
    public void setChp_bin_table_size(int field_79_chp_bin_table_size)
    {
        this.field_79_chp_bin_table_size = field_79_chp_bin_table_size;
    }

    /**
     * Get the pap_bin_table_offset field for the FIB record.
     */
    public int getPap_bin_table_offset()
    {
        return field_80_pap_bin_table_offset;
    }

    /**
     * Set the pap_bin_table_offset field for the FIB record.
     */
    public void setPap_bin_table_offset(int field_80_pap_bin_table_offset)
    {
        this.field_80_pap_bin_table_offset = field_80_pap_bin_table_offset;
    }

    /**
     * Get the pap_bin_table_size field for the FIB record.
     */
    public int getPap_bin_table_size()
    {
        return field_81_pap_bin_table_size;
    }

    /**
     * Set the pap_bin_table_size field for the FIB record.
     */
    public void setPap_bin_table_size(int field_81_pap_bin_table_size)
    {
        this.field_81_pap_bin_table_size = field_81_pap_bin_table_size;
    }

    /**
     * Get the sea_bin_table_offset field for the FIB record.
     */
    public int getSea_bin_table_offset()
    {
        return field_82_sea_bin_table_offset;
    }

    /**
     * Set the sea_bin_table_offset field for the FIB record.
     */
    public void setSea_bin_table_offset(int field_82_sea_bin_table_offset)
    {
        this.field_82_sea_bin_table_offset = field_82_sea_bin_table_offset;
    }

    /**
     * Get the sea_bin_table_size field for the FIB record.
     */
    public int getSea_bin_table_size()
    {
        return field_83_sea_bin_table_size;
    }

    /**
     * Set the sea_bin_table_size field for the FIB record.
     */
    public void setSea_bin_table_size(int field_83_sea_bin_table_size)
    {
        this.field_83_sea_bin_table_size = field_83_sea_bin_table_size;
    }

    /**
     * Get the fonts_bin_table_offset field for the FIB record.
     */
    public int getFonts_bin_table_offset()
    {
        return field_84_fonts_bin_table_offset;
    }

    /**
     * Set the fonts_bin_table_offset field for the FIB record.
     */
    public void setFonts_bin_table_offset(int field_84_fonts_bin_table_offset)
    {
        this.field_84_fonts_bin_table_offset = field_84_fonts_bin_table_offset;
    }

    /**
     * Get the fonts_bin_table_size field for the FIB record.
     */
    public int getFonts_bin_table_size()
    {
        return field_85_fonts_bin_table_size;
    }

    /**
     * Set the fonts_bin_table_size field for the FIB record.
     */
    public void setFonts_bin_table_size(int field_85_fonts_bin_table_size)
    {
        this.field_85_fonts_bin_table_size = field_85_fonts_bin_table_size;
    }

    /**
     * Get the main_fields_offset field for the FIB record.
     */
    public int getMain_fields_offset()
    {
        return field_86_main_fields_offset;
    }

    /**
     * Set the main_fields_offset field for the FIB record.
     */
    public void setMain_fields_offset(int field_86_main_fields_offset)
    {
        this.field_86_main_fields_offset = field_86_main_fields_offset;
    }

    /**
     * Get the main_fields_size field for the FIB record.
     */
    public int getMain_fields_size()
    {
        return field_87_main_fields_size;
    }

    /**
     * Set the main_fields_size field for the FIB record.
     */
    public void setMain_fields_size(int field_87_main_fields_size)
    {
        this.field_87_main_fields_size = field_87_main_fields_size;
    }

    /**
     * Get the header_fields_offset field for the FIB record.
     */
    public int getHeader_fields_offset()
    {
        return field_88_header_fields_offset;
    }

    /**
     * Set the header_fields_offset field for the FIB record.
     */
    public void setHeader_fields_offset(int field_88_header_fields_offset)
    {
        this.field_88_header_fields_offset = field_88_header_fields_offset;
    }

    /**
     * Get the header_fields_size field for the FIB record.
     */
    public int getHeader_fields_size()
    {
        return field_89_header_fields_size;
    }

    /**
     * Set the header_fields_size field for the FIB record.
     */
    public void setHeader_fields_size(int field_89_header_fields_size)
    {
        this.field_89_header_fields_size = field_89_header_fields_size;
    }

    /**
     * Get the footnote_fields_offset field for the FIB record.
     */
    public int getFootnote_fields_offset()
    {
        return field_90_footnote_fields_offset;
    }

    /**
     * Set the footnote_fields_offset field for the FIB record.
     */
    public void setFootnote_fields_offset(int field_90_footnote_fields_offset)
    {
        this.field_90_footnote_fields_offset = field_90_footnote_fields_offset;
    }

    /**
     * Get the footnote_fields_size field for the FIB record.
     */
    public int getFootnote_fields_size()
    {
        return field_91_footnote_fields_size;
    }

    /**
     * Set the footnote_fields_size field for the FIB record.
     */
    public void setFootnote_fields_size(int field_91_footnote_fields_size)
    {
        this.field_91_footnote_fields_size = field_91_footnote_fields_size;
    }

    /**
     * Get the ann_fields_offset field for the FIB record.
     */
    public int getAnn_fields_offset()
    {
        return field_92_ann_fields_offset;
    }

    /**
     * Set the ann_fields_offset field for the FIB record.
     */
    public void setAnn_fields_offset(int field_92_ann_fields_offset)
    {
        this.field_92_ann_fields_offset = field_92_ann_fields_offset;
    }

    /**
     * Get the ann_fields_size field for the FIB record.
     */
    public int getAnn_fields_size()
    {
        return field_93_ann_fields_size;
    }

    /**
     * Set the ann_fields_size field for the FIB record.
     */
    public void setAnn_fields_size(int field_93_ann_fields_size)
    {
        this.field_93_ann_fields_size = field_93_ann_fields_size;
    }

    /**
     * Get the unusedC field for the FIB record.
     */
    public int getUnusedC()
    {
        return field_94_unusedC;
    }

    /**
     * Set the unusedC field for the FIB record.
     */
    public void setUnusedC(int field_94_unusedC)
    {
        this.field_94_unusedC = field_94_unusedC;
    }

    /**
     * Get the unusedD field for the FIB record.
     */
    public int getUnusedD()
    {
        return field_95_unusedD;
    }

    /**
     * Set the unusedD field for the FIB record.
     */
    public void setUnusedD(int field_95_unusedD)
    {
        this.field_95_unusedD = field_95_unusedD;
    }

    /**
     * Get the bookmark_names_offset field for the FIB record.
     */
    public int getBookmark_names_offset()
    {
        return field_96_bookmark_names_offset;
    }

    /**
     * Set the bookmark_names_offset field for the FIB record.
     */
    public void setBookmark_names_offset(int field_96_bookmark_names_offset)
    {
        this.field_96_bookmark_names_offset = field_96_bookmark_names_offset;
    }

    /**
     * Get the bookmark_names_size field for the FIB record.
     */
    public int getBookmark_names_size()
    {
        return field_97_bookmark_names_size;
    }

    /**
     * Set the bookmark_names_size field for the FIB record.
     */
    public void setBookmark_names_size(int field_97_bookmark_names_size)
    {
        this.field_97_bookmark_names_size = field_97_bookmark_names_size;
    }

    /**
     * Get the bookmark_offsets_offset field for the FIB record.
     */
    public int getBookmark_offsets_offset()
    {
        return field_98_bookmark_offsets_offset;
    }

    /**
     * Set the bookmark_offsets_offset field for the FIB record.
     */
    public void setBookmark_offsets_offset(int field_98_bookmark_offsets_offset)
    {
        this.field_98_bookmark_offsets_offset = field_98_bookmark_offsets_offset;
    }

    /**
     * Get the bookmark_offsets_size field for the FIB record.
     */
    public int getBookmark_offsets_size()
    {
        return field_99_bookmark_offsets_size;
    }

    /**
     * Set the bookmark_offsets_size field for the FIB record.
     */
    public void setBookmark_offsets_size(int field_99_bookmark_offsets_size)
    {
        this.field_99_bookmark_offsets_size = field_99_bookmark_offsets_size;
    }

    /**
     * Get the macros_offset field for the FIB record.
     */
    public int getMacros_offset()
    {
        return field_100_macros_offset;
    }

    /**
     * Set the macros_offset field for the FIB record.
     */
    public void setMacros_offset(int field_100_macros_offset)
    {
        this.field_100_macros_offset = field_100_macros_offset;
    }

    /**
     * Get the macros_size field for the FIB record.
     */
    public int getMacros_size()
    {
        return field_101_macros_size;
    }

    /**
     * Set the macros_size field for the FIB record.
     */
    public void setMacros_size(int field_101_macros_size)
    {
        this.field_101_macros_size = field_101_macros_size;
    }

    /**
     * Get the unusedE field for the FIB record.
     */
    public int getUnusedE()
    {
        return field_102_unusedE;
    }

    /**
     * Set the unusedE field for the FIB record.
     */
    public void setUnusedE(int field_102_unusedE)
    {
        this.field_102_unusedE = field_102_unusedE;
    }

    /**
     * Get the unusedF field for the FIB record.
     */
    public int getUnusedF()
    {
        return field_103_unusedF;
    }

    /**
     * Set the unusedF field for the FIB record.
     */
    public void setUnusedF(int field_103_unusedF)
    {
        this.field_103_unusedF = field_103_unusedF;
    }

    /**
     * Get the unused10 field for the FIB record.
     */
    public int getUnused10()
    {
        return field_104_unused10;
    }

    /**
     * Set the unused10 field for the FIB record.
     */
    public void setUnused10(int field_104_unused10)
    {
        this.field_104_unused10 = field_104_unused10;
    }

    /**
     * Get the unused11 field for the FIB record.
     */
    public int getUnused11()
    {
        return field_105_unused11;
    }

    /**
     * Set the unused11 field for the FIB record.
     */
    public void setUnused11(int field_105_unused11)
    {
        this.field_105_unused11 = field_105_unused11;
    }

    /**
     * Get the printer offset field for the FIB record.
     */
    public int getPrinterOffset()
    {
        return field_106_printerOffset;
    }

    /**
     * Set the printer offset field for the FIB record.
     */
    public void setPrinterOffset(int field_106_printerOffset)
    {
        this.field_106_printerOffset = field_106_printerOffset;
    }

    /**
     * Get the printer size field for the FIB record.
     */
    public int getPrinterSize()
    {
        return field_107_printerSize;
    }

    /**
     * Set the printer size field for the FIB record.
     */
    public void setPrinterSize(int field_107_printerSize)
    {
        this.field_107_printerSize = field_107_printerSize;
    }

    /**
     * Get the printer portrait offset field for the FIB record.
     */
    public int getPrinterPortraitOffset()
    {
        return field_108_printerPortraitOffset;
    }

    /**
     * Set the printer portrait offset field for the FIB record.
     */
    public void setPrinterPortraitOffset(int field_108_printerPortraitOffset)
    {
        this.field_108_printerPortraitOffset = field_108_printerPortraitOffset;
    }

    /**
     * Get the printer portrait size field for the FIB record.
     */
    public int getPrinterPortraitSize()
    {
        return field_109_printerPortraitSize;
    }

    /**
     * Set the printer portrait size field for the FIB record.
     */
    public void setPrinterPortraitSize(int field_109_printerPortraitSize)
    {
        this.field_109_printerPortraitSize = field_109_printerPortraitSize;
    }

    /**
     * Get the printer landscape offset field for the FIB record.
     */
    public int getPrinterLandscapeOffset()
    {
        return field_110_printerLandscapeOffset;
    }

    /**
     * Set the printer landscape offset field for the FIB record.
     */
    public void setPrinterLandscapeOffset(int field_110_printerLandscapeOffset)
    {
        this.field_110_printerLandscapeOffset = field_110_printerLandscapeOffset;
    }

    /**
     * Get the printer landscape size field for the FIB record.
     */
    public int getPrinterLandscapeSize()
    {
        return field_111_printerLandscapeSize;
    }

    /**
     * Set the printer landscape size field for the FIB record.
     */
    public void setPrinterLandscapeSize(int field_111_printerLandscapeSize)
    {
        this.field_111_printerLandscapeSize = field_111_printerLandscapeSize;
    }

    /**
     * Get the wss offset field for the FIB record.
     */
    public int getWssOffset()
    {
        return field_112_wssOffset;
    }

    /**
     * Set the wss offset field for the FIB record.
     */
    public void setWssOffset(int field_112_wssOffset)
    {
        this.field_112_wssOffset = field_112_wssOffset;
    }

    /**
     * Get the wss size field for the FIB record.
     */
    public int getWssSize()
    {
        return field_113_wssSize;
    }

    /**
     * Set the wss size field for the FIB record.
     */
    public void setWssSize(int field_113_wssSize)
    {
        this.field_113_wssSize = field_113_wssSize;
    }

    /**
     * Get the DOP offset field for the FIB record.
     */
    public int getDOPOffset()
    {
        return field_114_DOPOffset;
    }

    /**
     * Set the DOP offset field for the FIB record.
     */
    public void setDOPOffset(int field_114_DOPOffset)
    {
        this.field_114_DOPOffset = field_114_DOPOffset;
    }

    /**
     * Get the DOP size field for the FIB record.
     */
    public int getDOPSize()
    {
        return field_115_DOPSize;
    }

    /**
     * Set the DOP size field for the FIB record.
     */
    public void setDOPSize(int field_115_DOPSize)
    {
        this.field_115_DOPSize = field_115_DOPSize;
    }

    /**
     * Get the sttbfassoc_offset field for the FIB record.
     */
    public int getSttbfassoc_offset()
    {
        return field_116_sttbfassoc_offset;
    }

    /**
     * Set the sttbfassoc_offset field for the FIB record.
     */
    public void setSttbfassoc_offset(int field_116_sttbfassoc_offset)
    {
        this.field_116_sttbfassoc_offset = field_116_sttbfassoc_offset;
    }

    /**
     * Get the sttbfassoc_size field for the FIB record.
     */
    public int getSttbfassoc_size()
    {
        return field_117_sttbfassoc_size;
    }

    /**
     * Set the sttbfassoc_size field for the FIB record.
     */
    public void setSttbfassoc_size(int field_117_sttbfassoc_size)
    {
        this.field_117_sttbfassoc_size = field_117_sttbfassoc_size;
    }

    /**
     * Get the textPieceTable offset field for the FIB record.
     */
    public int getTextPieceTableOffset()
    {
        return field_118_textPieceTableOffset;
    }

    /**
     * Set the textPieceTable offset field for the FIB record.
     */
    public void setTextPieceTableOffset(int field_118_textPieceTableOffset)
    {
        this.field_118_textPieceTableOffset = field_118_textPieceTableOffset;
    }

    /**
     * Get the textPieceTable size field for the FIB record.
     */
    public int getTextPieceTableSize()
    {
        return field_119_textPieceTableSize;
    }

    /**
     * Set the textPieceTable size field for the FIB record.
     */
    public void setTextPieceTableSize(int field_119_textPieceTableSize)
    {
        this.field_119_textPieceTableSize = field_119_textPieceTableSize;
    }

    /**
     * Get the unused12 field for the FIB record.
     */
    public int getUnused12()
    {
        return field_120_unused12;
    }

    /**
     * Set the unused12 field for the FIB record.
     */
    public void setUnused12(int field_120_unused12)
    {
        this.field_120_unused12 = field_120_unused12;
    }

    /**
     * Get the unused13 field for the FIB record.
     */
    public int getUnused13()
    {
        return field_121_unused13;
    }

    /**
     * Set the unused13 field for the FIB record.
     */
    public void setUnused13(int field_121_unused13)
    {
        this.field_121_unused13 = field_121_unused13;
    }

    /**
     * Get the offset AutosaveSource field for the FIB record.
     */
    public int getOffsetAutosaveSource()
    {
        return field_122_offsetAutosaveSource;
    }

    /**
     * Set the offset AutosaveSource field for the FIB record.
     */
    public void setOffsetAutosaveSource(int field_122_offsetAutosaveSource)
    {
        this.field_122_offsetAutosaveSource = field_122_offsetAutosaveSource;
    }

    /**
     * Get the count AutosaveSource field for the FIB record.
     */
    public int getCountAutosaveSource()
    {
        return field_123_countAutosaveSource;
    }

    /**
     * Set the count AutosaveSource field for the FIB record.
     */
    public void setCountAutosaveSource(int field_123_countAutosaveSource)
    {
        this.field_123_countAutosaveSource = field_123_countAutosaveSource;
    }

    /**
     * Get the offset GrpXstAtnOwners field for the FIB record.
     */
    public int getOffsetGrpXstAtnOwners()
    {
        return field_124_offsetGrpXstAtnOwners;
    }

    /**
     * Set the offset GrpXstAtnOwners field for the FIB record.
     */
    public void setOffsetGrpXstAtnOwners(int field_124_offsetGrpXstAtnOwners)
    {
        this.field_124_offsetGrpXstAtnOwners = field_124_offsetGrpXstAtnOwners;
    }

    /**
     * Get the count GrpXstAtnOwners field for the FIB record.
     */
    public int getCountGrpXstAtnOwners()
    {
        return field_125_countGrpXstAtnOwners;
    }

    /**
     * Set the count GrpXstAtnOwners field for the FIB record.
     */
    public void setCountGrpXstAtnOwners(int field_125_countGrpXstAtnOwners)
    {
        this.field_125_countGrpXstAtnOwners = field_125_countGrpXstAtnOwners;
    }

    /**
     * Get the offset SttbfAtnbkmk field for the FIB record.
     */
    public int getOffsetSttbfAtnbkmk()
    {
        return field_126_offsetSttbfAtnbkmk;
    }

    /**
     * Set the offset SttbfAtnbkmk field for the FIB record.
     */
    public void setOffsetSttbfAtnbkmk(int field_126_offsetSttbfAtnbkmk)
    {
        this.field_126_offsetSttbfAtnbkmk = field_126_offsetSttbfAtnbkmk;
    }

    /**
     * Get the length SttbfAtnbkmk field for the FIB record.
     */
    public int getLengthSttbfAtnbkmk()
    {
        return field_127_lengthSttbfAtnbkmk;
    }

    /**
     * Set the length SttbfAtnbkmk field for the FIB record.
     */
    public void setLengthSttbfAtnbkmk(int field_127_lengthSttbfAtnbkmk)
    {
        this.field_127_lengthSttbfAtnbkmk = field_127_lengthSttbfAtnbkmk;
    }

    /**
     * Get the unused14 field for the FIB record.
     */
    public int getUnused14()
    {
        return field_128_unused14;
    }

    /**
     * Set the unused14 field for the FIB record.
     */
    public void setUnused14(int field_128_unused14)
    {
        this.field_128_unused14 = field_128_unused14;
    }

    /**
     * Get the unused15 field for the FIB record.
     */
    public int getUnused15()
    {
        return field_129_unused15;
    }

    /**
     * Set the unused15 field for the FIB record.
     */
    public void setUnused15(int field_129_unused15)
    {
        this.field_129_unused15 = field_129_unused15;
    }

    /**
     * Get the unused16 field for the FIB record.
     */
    public int getUnused16()
    {
        return field_130_unused16;
    }

    /**
     * Set the unused16 field for the FIB record.
     */
    public void setUnused16(int field_130_unused16)
    {
        this.field_130_unused16 = field_130_unused16;
    }

    /**
     * Get the unused17 field for the FIB record.
     */
    public int getUnused17()
    {
        return field_131_unused17;
    }

    /**
     * Set the unused17 field for the FIB record.
     */
    public void setUnused17(int field_131_unused17)
    {
        this.field_131_unused17 = field_131_unused17;
    }

    /**
     * Get the offset PlcspaMom field for the FIB record.
     */
    public int getOffsetPlcspaMom()
    {
        return field_132_offsetPlcspaMom;
    }

    /**
     * Set the offset PlcspaMom field for the FIB record.
     */
    public void setOffsetPlcspaMom(int field_132_offsetPlcspaMom)
    {
        this.field_132_offsetPlcspaMom = field_132_offsetPlcspaMom;
    }

    /**
     * Get the length PlcspaMom field for the FIB record.
     */
    public int getLengthPlcspaMom()
    {
        return field_133_lengthPlcspaMom;
    }

    /**
     * Set the length PlcspaMom field for the FIB record.
     */
    public void setLengthPlcspaMom(int field_133_lengthPlcspaMom)
    {
        this.field_133_lengthPlcspaMom = field_133_lengthPlcspaMom;
    }

    /**
     * Get the offset PlcspaHdr field for the FIB record.
     */
    public int getOffsetPlcspaHdr()
    {
        return field_134_offsetPlcspaHdr;
    }

    /**
     * Set the offset PlcspaHdr field for the FIB record.
     */
    public void setOffsetPlcspaHdr(int field_134_offsetPlcspaHdr)
    {
        this.field_134_offsetPlcspaHdr = field_134_offsetPlcspaHdr;
    }

    /**
     * Get the length PlcspaHdr field for the FIB record.
     */
    public int getLengthPlcspaHdr()
    {
        return field_135_lengthPlcspaHdr;
    }

    /**
     * Set the length PlcspaHdr field for the FIB record.
     */
    public void setLengthPlcspaHdr(int field_135_lengthPlcspaHdr)
    {
        this.field_135_lengthPlcspaHdr = field_135_lengthPlcspaHdr;
    }

    /**
     * Get the length Plcf Ann Bkmrk First field for the FIB record.
     */
    public int getLengthPlcfAnnBkmrkFirst()
    {
        return field_136_lengthPlcfAnnBkmrkFirst;
    }

    /**
     * Set the length Plcf Ann Bkmrk First field for the FIB record.
     */
    public void setLengthPlcfAnnBkmrkFirst(int field_136_lengthPlcfAnnBkmrkFirst)
    {
        this.field_136_lengthPlcfAnnBkmrkFirst = field_136_lengthPlcfAnnBkmrkFirst;
    }

    /**
     * Get the offset Plcf Ann Bkmrk First field for the FIB record.
     */
    public int getOffsetPlcfAnnBkmrkFirst()
    {
        return field_137_offsetPlcfAnnBkmrkFirst;
    }

    /**
     * Set the offset Plcf Ann Bkmrk First field for the FIB record.
     */
    public void setOffsetPlcfAnnBkmrkFirst(int field_137_offsetPlcfAnnBkmrkFirst)
    {
        this.field_137_offsetPlcfAnnBkmrkFirst = field_137_offsetPlcfAnnBkmrkFirst;
    }

    /**
     * Get the length Plcf Ann Bkark Last field for the FIB record.
     */
    public int getLengthPlcfAnnBkarkLast()
    {
        return field_138_lengthPlcfAnnBkarkLast;
    }

    /**
     * Set the length Plcf Ann Bkark Last field for the FIB record.
     */
    public void setLengthPlcfAnnBkarkLast(int field_138_lengthPlcfAnnBkarkLast)
    {
        this.field_138_lengthPlcfAnnBkarkLast = field_138_lengthPlcfAnnBkarkLast;
    }

    /**
     * Get the PlcfAtnbkl field for the FIB record.
     */
    public int getPlcfAtnbkl()
    {
        return field_139_PlcfAtnbkl;
    }

    /**
     * Set the PlcfAtnbkl field for the FIB record.
     */
    public void setPlcfAtnbkl(int field_139_PlcfAtnbkl)
    {
        this.field_139_PlcfAtnbkl = field_139_PlcfAtnbkl;
    }

    /**
     * Get the fcPms field for the FIB record.
     */
    public int getFcPms()
    {
        return field_140_fcPms;
    }

    /**
     * Set the fcPms field for the FIB record.
     */
    public void setFcPms(int field_140_fcPms)
    {
        this.field_140_fcPms = field_140_fcPms;
    }

    /**
     * Get the lcbPms field for the FIB record.
     */
    public int getLcbPms()
    {
        return field_141_lcbPms;
    }

    /**
     * Set the lcbPms field for the FIB record.
     */
    public void setLcbPms(int field_141_lcbPms)
    {
        this.field_141_lcbPms = field_141_lcbPms;
    }

    /**
     * Get the fcFormFldSttbs field for the FIB record.
     */
    public int getFcFormFldSttbs()
    {
        return field_142_fcFormFldSttbs;
    }

    /**
     * Set the fcFormFldSttbs field for the FIB record.
     */
    public void setFcFormFldSttbs(int field_142_fcFormFldSttbs)
    {
        this.field_142_fcFormFldSttbs = field_142_fcFormFldSttbs;
    }

    /**
     * Get the lcbFormFldSttbs field for the FIB record.
     */
    public int getLcbFormFldSttbs()
    {
        return field_143_lcbFormFldSttbs;
    }

    /**
     * Set the lcbFormFldSttbs field for the FIB record.
     */
    public void setLcbFormFldSttbs(int field_143_lcbFormFldSttbs)
    {
        this.field_143_lcbFormFldSttbs = field_143_lcbFormFldSttbs;
    }

    /**
     * Get the fcPlcfendRef field for the FIB record.
     */
    public int getFcPlcfendRef()
    {
        return field_144_fcPlcfendRef;
    }

    /**
     * Set the fcPlcfendRef field for the FIB record.
     */
    public void setFcPlcfendRef(int field_144_fcPlcfendRef)
    {
        this.field_144_fcPlcfendRef = field_144_fcPlcfendRef;
    }

    /**
     * Get the lcbPlcfendRef field for the FIB record.
     */
    public int getLcbPlcfendRef()
    {
        return field_145_lcbPlcfendRef;
    }

    /**
     * Set the lcbPlcfendRef field for the FIB record.
     */
    public void setLcbPlcfendRef(int field_145_lcbPlcfendRef)
    {
        this.field_145_lcbPlcfendRef = field_145_lcbPlcfendRef;
    }

    /**
     * Get the fcPlcfendTxt field for the FIB record.
     */
    public int getFcPlcfendTxt()
    {
        return field_146_fcPlcfendTxt;
    }

    /**
     * Set the fcPlcfendTxt field for the FIB record.
     */
    public void setFcPlcfendTxt(int field_146_fcPlcfendTxt)
    {
        this.field_146_fcPlcfendTxt = field_146_fcPlcfendTxt;
    }

    /**
     * Get the lcbPlcfendTxt field for the FIB record.
     */
    public int getLcbPlcfendTxt()
    {
        return field_147_lcbPlcfendTxt;
    }

    /**
     * Set the lcbPlcfendTxt field for the FIB record.
     */
    public void setLcbPlcfendTxt(int field_147_lcbPlcfendTxt)
    {
        this.field_147_lcbPlcfendTxt = field_147_lcbPlcfendTxt;
    }

    /**
     * Get the fcPlcffldEdn field for the FIB record.
     */
    public int getFcPlcffldEdn()
    {
        return field_148_fcPlcffldEdn;
    }

    /**
     * Set the fcPlcffldEdn field for the FIB record.
     */
    public void setFcPlcffldEdn(int field_148_fcPlcffldEdn)
    {
        this.field_148_fcPlcffldEdn = field_148_fcPlcffldEdn;
    }

    /**
     * Get the lcbPlcffldEdn field for the FIB record.
     */
    public int getLcbPlcffldEdn()
    {
        return field_149_lcbPlcffldEdn;
    }

    /**
     * Set the lcbPlcffldEdn field for the FIB record.
     */
    public void setLcbPlcffldEdn(int field_149_lcbPlcffldEdn)
    {
        this.field_149_lcbPlcffldEdn = field_149_lcbPlcffldEdn;
    }

    /**
     * Get the fcPlcfpgdEdn field for the FIB record.
     */
    public int getFcPlcfpgdEdn()
    {
        return field_150_fcPlcfpgdEdn;
    }

    /**
     * Set the fcPlcfpgdEdn field for the FIB record.
     */
    public void setFcPlcfpgdEdn(int field_150_fcPlcfpgdEdn)
    {
        this.field_150_fcPlcfpgdEdn = field_150_fcPlcfpgdEdn;
    }

    /**
     * Get the lcbPlcfpgdEdn field for the FIB record.
     */
    public int getLcbPlcfpgdEdn()
    {
        return field_151_lcbPlcfpgdEdn;
    }

    /**
     * Set the lcbPlcfpgdEdn field for the FIB record.
     */
    public void setLcbPlcfpgdEdn(int field_151_lcbPlcfpgdEdn)
    {
        this.field_151_lcbPlcfpgdEdn = field_151_lcbPlcfpgdEdn;
    }

    /**
     * Get the fcDggInfo field for the FIB record.
     */
    public int getFcDggInfo()
    {
        return field_152_fcDggInfo;
    }

    /**
     * Set the fcDggInfo field for the FIB record.
     */
    public void setFcDggInfo(int field_152_fcDggInfo)
    {
        this.field_152_fcDggInfo = field_152_fcDggInfo;
    }

    /**
     * Get the lcbDggInfo field for the FIB record.
     */
    public int getLcbDggInfo()
    {
        return field_153_lcbDggInfo;
    }

    /**
     * Set the lcbDggInfo field for the FIB record.
     */
    public void setLcbDggInfo(int field_153_lcbDggInfo)
    {
        this.field_153_lcbDggInfo = field_153_lcbDggInfo;
    }

    /**
     * Get the fcSttbfRMark field for the FIB record.
     */
    public int getFcSttbfRMark()
    {
        return field_154_fcSttbfRMark;
    }

    /**
     * Set the fcSttbfRMark field for the FIB record.
     */
    public void setFcSttbfRMark(int field_154_fcSttbfRMark)
    {
        this.field_154_fcSttbfRMark = field_154_fcSttbfRMark;
    }

    /**
     * Get the lcbSttbfRMark field for the FIB record.
     */
    public int getLcbSttbfRMark()
    {
        return field_155_lcbSttbfRMark;
    }

    /**
     * Set the lcbSttbfRMark field for the FIB record.
     */
    public void setLcbSttbfRMark(int field_155_lcbSttbfRMark)
    {
        this.field_155_lcbSttbfRMark = field_155_lcbSttbfRMark;
    }

    /**
     * Get the fcSttbCaption field for the FIB record.
     */
    public int getFcSttbCaption()
    {
        return field_156_fcSttbCaption;
    }

    /**
     * Set the fcSttbCaption field for the FIB record.
     */
    public void setFcSttbCaption(int field_156_fcSttbCaption)
    {
        this.field_156_fcSttbCaption = field_156_fcSttbCaption;
    }

    /**
     * Get the lcbSttbCaption field for the FIB record.
     */
    public int getLcbSttbCaption()
    {
        return field_157_lcbSttbCaption;
    }

    /**
     * Set the lcbSttbCaption field for the FIB record.
     */
    public void setLcbSttbCaption(int field_157_lcbSttbCaption)
    {
        this.field_157_lcbSttbCaption = field_157_lcbSttbCaption;
    }

    /**
     * Get the fcSttbAutoCaption field for the FIB record.
     */
    public int getFcSttbAutoCaption()
    {
        return field_158_fcSttbAutoCaption;
    }

    /**
     * Set the fcSttbAutoCaption field for the FIB record.
     */
    public void setFcSttbAutoCaption(int field_158_fcSttbAutoCaption)
    {
        this.field_158_fcSttbAutoCaption = field_158_fcSttbAutoCaption;
    }

    /**
     * Get the lcbSttbAutoCaption field for the FIB record.
     */
    public int getLcbSttbAutoCaption()
    {
        return field_159_lcbSttbAutoCaption;
    }

    /**
     * Set the lcbSttbAutoCaption field for the FIB record.
     */
    public void setLcbSttbAutoCaption(int field_159_lcbSttbAutoCaption)
    {
        this.field_159_lcbSttbAutoCaption = field_159_lcbSttbAutoCaption;
    }

    /**
     * Get the fcPlcfwkb field for the FIB record.
     */
    public int getFcPlcfwkb()
    {
        return field_160_fcPlcfwkb;
    }

    /**
     * Set the fcPlcfwkb field for the FIB record.
     */
    public void setFcPlcfwkb(int field_160_fcPlcfwkb)
    {
        this.field_160_fcPlcfwkb = field_160_fcPlcfwkb;
    }

    /**
     * Get the lcbPlcfwkb field for the FIB record.
     */
    public int getLcbPlcfwkb()
    {
        return field_161_lcbPlcfwkb;
    }

    /**
     * Set the lcbPlcfwkb field for the FIB record.
     */
    public void setLcbPlcfwkb(int field_161_lcbPlcfwkb)
    {
        this.field_161_lcbPlcfwkb = field_161_lcbPlcfwkb;
    }

    /**
     * Get the fcPlcfsplfcPlcfspl field for the FIB record.
     */
    public int getFcPlcfsplfcPlcfspl()
    {
        return field_162_fcPlcfsplfcPlcfspl;
    }

    /**
     * Set the fcPlcfsplfcPlcfspl field for the FIB record.
     */
    public void setFcPlcfsplfcPlcfspl(int field_162_fcPlcfsplfcPlcfspl)
    {
        this.field_162_fcPlcfsplfcPlcfspl = field_162_fcPlcfsplfcPlcfspl;
    }

    /**
     * Get the lcbPlcfspl field for the FIB record.
     */
    public int getLcbPlcfspl()
    {
        return field_163_lcbPlcfspl;
    }

    /**
     * Set the lcbPlcfspl field for the FIB record.
     */
    public void setLcbPlcfspl(int field_163_lcbPlcfspl)
    {
        this.field_163_lcbPlcfspl = field_163_lcbPlcfspl;
    }

    /**
     * Get the fcPlcftxbxTxt field for the FIB record.
     */
    public int getFcPlcftxbxTxt()
    {
        return field_164_fcPlcftxbxTxt;
    }

    /**
     * Set the fcPlcftxbxTxt field for the FIB record.
     */
    public void setFcPlcftxbxTxt(int field_164_fcPlcftxbxTxt)
    {
        this.field_164_fcPlcftxbxTxt = field_164_fcPlcftxbxTxt;
    }

    /**
     * Get the lcbPlcftxbxTxt field for the FIB record.
     */
    public int getLcbPlcftxbxTxt()
    {
        return field_165_lcbPlcftxbxTxt;
    }

    /**
     * Set the lcbPlcftxbxTxt field for the FIB record.
     */
    public void setLcbPlcftxbxTxt(int field_165_lcbPlcftxbxTxt)
    {
        this.field_165_lcbPlcftxbxTxt = field_165_lcbPlcftxbxTxt;
    }

    /**
     * Get the fcPlcffldTxbx field for the FIB record.
     */
    public int getFcPlcffldTxbx()
    {
        return field_166_fcPlcffldTxbx;
    }

    /**
     * Set the fcPlcffldTxbx field for the FIB record.
     */
    public void setFcPlcffldTxbx(int field_166_fcPlcffldTxbx)
    {
        this.field_166_fcPlcffldTxbx = field_166_fcPlcffldTxbx;
    }

    /**
     * Get the lcbPlcffldTxbx field for the FIB record.
     */
    public int getLcbPlcffldTxbx()
    {
        return field_167_lcbPlcffldTxbx;
    }

    /**
     * Set the lcbPlcffldTxbx field for the FIB record.
     */
    public void setLcbPlcffldTxbx(int field_167_lcbPlcffldTxbx)
    {
        this.field_167_lcbPlcffldTxbx = field_167_lcbPlcffldTxbx;
    }

    /**
     * Get the fcPlcfhdrtxbxTxt field for the FIB record.
     */
    public int getFcPlcfhdrtxbxTxt()
    {
        return field_168_fcPlcfhdrtxbxTxt;
    }

    /**
     * Set the fcPlcfhdrtxbxTxt field for the FIB record.
     */
    public void setFcPlcfhdrtxbxTxt(int field_168_fcPlcfhdrtxbxTxt)
    {
        this.field_168_fcPlcfhdrtxbxTxt = field_168_fcPlcfhdrtxbxTxt;
    }

    /**
     * Get the lcbPlcfhdrtxbxTxt field for the FIB record.
     */
    public int getLcbPlcfhdrtxbxTxt()
    {
        return field_169_lcbPlcfhdrtxbxTxt;
    }

    /**
     * Set the lcbPlcfhdrtxbxTxt field for the FIB record.
     */
    public void setLcbPlcfhdrtxbxTxt(int field_169_lcbPlcfhdrtxbxTxt)
    {
        this.field_169_lcbPlcfhdrtxbxTxt = field_169_lcbPlcfhdrtxbxTxt;
    }

    /**
     * Get the fcPlcffldHdrTxbx field for the FIB record.
     */
    public int getFcPlcffldHdrTxbx()
    {
        return field_170_fcPlcffldHdrTxbx;
    }

    /**
     * Set the fcPlcffldHdrTxbx field for the FIB record.
     */
    public void setFcPlcffldHdrTxbx(int field_170_fcPlcffldHdrTxbx)
    {
        this.field_170_fcPlcffldHdrTxbx = field_170_fcPlcffldHdrTxbx;
    }

    /**
     * Get the lcbPlcffldHdrTxbx field for the FIB record.
     */
    public int getLcbPlcffldHdrTxbx()
    {
        return field_171_lcbPlcffldHdrTxbx;
    }

    /**
     * Set the lcbPlcffldHdrTxbx field for the FIB record.
     */
    public void setLcbPlcffldHdrTxbx(int field_171_lcbPlcffldHdrTxbx)
    {
        this.field_171_lcbPlcffldHdrTxbx = field_171_lcbPlcffldHdrTxbx;
    }

    /**
     * Get the fcStwUser field for the FIB record.
     */
    public int getFcStwUser()
    {
        return field_172_fcStwUser;
    }

    /**
     * Set the fcStwUser field for the FIB record.
     */
    public void setFcStwUser(int field_172_fcStwUser)
    {
        this.field_172_fcStwUser = field_172_fcStwUser;
    }

    /**
     * Get the lcbStwUser field for the FIB record.
     */
    public int getLcbStwUser()
    {
        return field_173_lcbStwUser;
    }

    /**
     * Set the lcbStwUser field for the FIB record.
     */
    public void setLcbStwUser(int field_173_lcbStwUser)
    {
        this.field_173_lcbStwUser = field_173_lcbStwUser;
    }

    /**
     * Get the fcSttbttmbd field for the FIB record.
     */
    public int getFcSttbttmbd()
    {
        return field_174_fcSttbttmbd;
    }

    /**
     * Set the fcSttbttmbd field for the FIB record.
     */
    public void setFcSttbttmbd(int field_174_fcSttbttmbd)
    {
        this.field_174_fcSttbttmbd = field_174_fcSttbttmbd;
    }

    /**
     * Get the cbSttbttmbd field for the FIB record.
     */
    public int getCbSttbttmbd()
    {
        return field_175_cbSttbttmbd;
    }

    /**
     * Set the cbSttbttmbd field for the FIB record.
     */
    public void setCbSttbttmbd(int field_175_cbSttbttmbd)
    {
        this.field_175_cbSttbttmbd = field_175_cbSttbttmbd;
    }

    /**
     * Get the fcUnused field for the FIB record.
     */
    public int getFcUnused()
    {
        return field_176_fcUnused;
    }

    /**
     * Set the fcUnused field for the FIB record.
     */
    public void setFcUnused(int field_176_fcUnused)
    {
        this.field_176_fcUnused = field_176_fcUnused;
    }

    /**
     * Get the lcbUnused field for the FIB record.
     */
    public int getLcbUnused()
    {
        return field_177_lcbUnused;
    }

    /**
     * Set the lcbUnused field for the FIB record.
     */
    public void setLcbUnused(int field_177_lcbUnused)
    {
        this.field_177_lcbUnused = field_177_lcbUnused;
    }

    /**
     * Get the rgpgdbkd field for the FIB record.
     */
    public int getRgpgdbkd()
    {
        return field_178_rgpgdbkd;
    }

    /**
     * Set the rgpgdbkd field for the FIB record.
     */
    public void setRgpgdbkd(int field_178_rgpgdbkd)
    {
        this.field_178_rgpgdbkd = field_178_rgpgdbkd;
    }

    /**
     * Get the fcPgdMother field for the FIB record.
     */
    public int getFcPgdMother()
    {
        return field_179_fcPgdMother;
    }

    /**
     * Set the fcPgdMother field for the FIB record.
     */
    public void setFcPgdMother(int field_179_fcPgdMother)
    {
        this.field_179_fcPgdMother = field_179_fcPgdMother;
    }

    /**
     * Get the lcbPgdMother field for the FIB record.
     */
    public int getLcbPgdMother()
    {
        return field_180_lcbPgdMother;
    }

    /**
     * Set the lcbPgdMother field for the FIB record.
     */
    public void setLcbPgdMother(int field_180_lcbPgdMother)
    {
        this.field_180_lcbPgdMother = field_180_lcbPgdMother;
    }

    /**
     * Get the fcBkdMother field for the FIB record.
     */
    public int getFcBkdMother()
    {
        return field_181_fcBkdMother;
    }

    /**
     * Set the fcBkdMother field for the FIB record.
     */
    public void setFcBkdMother(int field_181_fcBkdMother)
    {
        this.field_181_fcBkdMother = field_181_fcBkdMother;
    }

    /**
     * Get the lcbBkdMother field for the FIB record.
     */
    public int getLcbBkdMother()
    {
        return field_182_lcbBkdMother;
    }

    /**
     * Set the lcbBkdMother field for the FIB record.
     */
    public void setLcbBkdMother(int field_182_lcbBkdMother)
    {
        this.field_182_lcbBkdMother = field_182_lcbBkdMother;
    }

    /**
     * Get the fcPgdFtn field for the FIB record.
     */
    public int getFcPgdFtn()
    {
        return field_183_fcPgdFtn;
    }

    /**
     * Set the fcPgdFtn field for the FIB record.
     */
    public void setFcPgdFtn(int field_183_fcPgdFtn)
    {
        this.field_183_fcPgdFtn = field_183_fcPgdFtn;
    }

    /**
     * Get the lcbPgdFtn field for the FIB record.
     */
    public int getLcbPgdFtn()
    {
        return field_184_lcbPgdFtn;
    }

    /**
     * Set the lcbPgdFtn field for the FIB record.
     */
    public void setLcbPgdFtn(int field_184_lcbPgdFtn)
    {
        this.field_184_lcbPgdFtn = field_184_lcbPgdFtn;
    }

    /**
     * Get the fcBkdFtn field for the FIB record.
     */
    public int getFcBkdFtn()
    {
        return field_185_fcBkdFtn;
    }

    /**
     * Set the fcBkdFtn field for the FIB record.
     */
    public void setFcBkdFtn(int field_185_fcBkdFtn)
    {
        this.field_185_fcBkdFtn = field_185_fcBkdFtn;
    }

    /**
     * Get the lcbBkdFtn field for the FIB record.
     */
    public int getLcbBkdFtn()
    {
        return field_186_lcbBkdFtn;
    }

    /**
     * Set the lcbBkdFtn field for the FIB record.
     */
    public void setLcbBkdFtn(int field_186_lcbBkdFtn)
    {
        this.field_186_lcbBkdFtn = field_186_lcbBkdFtn;
    }

    /**
     * Get the fcPgdEdn field for the FIB record.
     */
    public int getFcPgdEdn()
    {
        return field_187_fcPgdEdn;
    }

    /**
     * Set the fcPgdEdn field for the FIB record.
     */
    public void setFcPgdEdn(int field_187_fcPgdEdn)
    {
        this.field_187_fcPgdEdn = field_187_fcPgdEdn;
    }

    /**
     * Get the lcbPgdEdn field for the FIB record.
     */
    public int getLcbPgdEdn()
    {
        return field_188_lcbPgdEdn;
    }

    /**
     * Set the lcbPgdEdn field for the FIB record.
     */
    public void setLcbPgdEdn(int field_188_lcbPgdEdn)
    {
        this.field_188_lcbPgdEdn = field_188_lcbPgdEdn;
    }

    /**
     * Get the fcBkdEdn field for the FIB record.
     */
    public int getFcBkdEdn()
    {
        return field_189_fcBkdEdn;
    }

    /**
     * Set the fcBkdEdn field for the FIB record.
     */
    public void setFcBkdEdn(int field_189_fcBkdEdn)
    {
        this.field_189_fcBkdEdn = field_189_fcBkdEdn;
    }

    /**
     * Get the lcbBkdEdn field for the FIB record.
     */
    public int getLcbBkdEdn()
    {
        return field_190_lcbBkdEdn;
    }

    /**
     * Set the lcbBkdEdn field for the FIB record.
     */
    public void setLcbBkdEdn(int field_190_lcbBkdEdn)
    {
        this.field_190_lcbBkdEdn = field_190_lcbBkdEdn;
    }

    /**
     * Get the fcSttbfIntlFld field for the FIB record.
     */
    public int getFcSttbfIntlFld()
    {
        return field_191_fcSttbfIntlFld;
    }

    /**
     * Set the fcSttbfIntlFld field for the FIB record.
     */
    public void setFcSttbfIntlFld(int field_191_fcSttbfIntlFld)
    {
        this.field_191_fcSttbfIntlFld = field_191_fcSttbfIntlFld;
    }

    /**
     * Get the lcbSttbfIntlFld field for the FIB record.
     */
    public int getLcbSttbfIntlFld()
    {
        return field_192_lcbSttbfIntlFld;
    }

    /**
     * Set the lcbSttbfIntlFld field for the FIB record.
     */
    public void setLcbSttbfIntlFld(int field_192_lcbSttbfIntlFld)
    {
        this.field_192_lcbSttbfIntlFld = field_192_lcbSttbfIntlFld;
    }

    /**
     * Get the fcRouteSlip field for the FIB record.
     */
    public int getFcRouteSlip()
    {
        return field_193_fcRouteSlip;
    }

    /**
     * Set the fcRouteSlip field for the FIB record.
     */
    public void setFcRouteSlip(int field_193_fcRouteSlip)
    {
        this.field_193_fcRouteSlip = field_193_fcRouteSlip;
    }

    /**
     * Get the lcbRouteSlip field for the FIB record.
     */
    public int getLcbRouteSlip()
    {
        return field_194_lcbRouteSlip;
    }

    /**
     * Set the lcbRouteSlip field for the FIB record.
     */
    public void setLcbRouteSlip(int field_194_lcbRouteSlip)
    {
        this.field_194_lcbRouteSlip = field_194_lcbRouteSlip;
    }

    /**
     * Get the fcSttbSavedBy field for the FIB record.
     */
    public int getFcSttbSavedBy()
    {
        return field_195_fcSttbSavedBy;
    }

    /**
     * Set the fcSttbSavedBy field for the FIB record.
     */
    public void setFcSttbSavedBy(int field_195_fcSttbSavedBy)
    {
        this.field_195_fcSttbSavedBy = field_195_fcSttbSavedBy;
    }

    /**
     * Get the lcbSttbSavedBy field for the FIB record.
     */
    public int getLcbSttbSavedBy()
    {
        return field_196_lcbSttbSavedBy;
    }

    /**
     * Set the lcbSttbSavedBy field for the FIB record.
     */
    public void setLcbSttbSavedBy(int field_196_lcbSttbSavedBy)
    {
        this.field_196_lcbSttbSavedBy = field_196_lcbSttbSavedBy;
    }

    /**
     * Get the fcSttbFnm field for the FIB record.
     */
    public int getFcSttbFnm()
    {
        return field_197_fcSttbFnm;
    }

    /**
     * Set the fcSttbFnm field for the FIB record.
     */
    public void setFcSttbFnm(int field_197_fcSttbFnm)
    {
        this.field_197_fcSttbFnm = field_197_fcSttbFnm;
    }

    /**
     * Get the lcbSttbFnm field for the FIB record.
     */
    public int getLcbSttbFnm()
    {
        return field_198_lcbSttbFnm;
    }

    /**
     * Set the lcbSttbFnm field for the FIB record.
     */
    public void setLcbSttbFnm(int field_198_lcbSttbFnm)
    {
        this.field_198_lcbSttbFnm = field_198_lcbSttbFnm;
    }

    /**
     * Get the fcPlcfLst field for the FIB record.
     */
    public int getFcPlcfLst()
    {
        return field_199_fcPlcfLst;
    }

    /**
     * Set the fcPlcfLst field for the FIB record.
     */
    public void setFcPlcfLst(int field_199_fcPlcfLst)
    {
        this.field_199_fcPlcfLst = field_199_fcPlcfLst;
    }

    /**
     * Get the lcbPlcfLst field for the FIB record.
     */
    public int getLcbPlcfLst()
    {
        return field_200_lcbPlcfLst;
    }

    /**
     * Set the lcbPlcfLst field for the FIB record.
     */
    public void setLcbPlcfLst(int field_200_lcbPlcfLst)
    {
        this.field_200_lcbPlcfLst = field_200_lcbPlcfLst;
    }

    /**
     * Get the fcPlfLfo field for the FIB record.
     */
    public int getFcPlfLfo()
    {
        return field_201_fcPlfLfo;
    }

    /**
     * Set the fcPlfLfo field for the FIB record.
     */
    public void setFcPlfLfo(int field_201_fcPlfLfo)
    {
        this.field_201_fcPlfLfo = field_201_fcPlfLfo;
    }

    /**
     * Get the lcbPlfLfo field for the FIB record.
     */
    public int getLcbPlfLfo()
    {
        return field_202_lcbPlfLfo;
    }

    /**
     * Set the lcbPlfLfo field for the FIB record.
     */
    public void setLcbPlfLfo(int field_202_lcbPlfLfo)
    {
        this.field_202_lcbPlfLfo = field_202_lcbPlfLfo;
    }

    /**
     * Get the fcPlcftxbxBkd field for the FIB record.
     */
    public int getFcPlcftxbxBkd()
    {
        return field_203_fcPlcftxbxBkd;
    }

    /**
     * Set the fcPlcftxbxBkd field for the FIB record.
     */
    public void setFcPlcftxbxBkd(int field_203_fcPlcftxbxBkd)
    {
        this.field_203_fcPlcftxbxBkd = field_203_fcPlcftxbxBkd;
    }

    /**
     * Get the lcbPlcftxbxBkd field for the FIB record.
     */
    public int getLcbPlcftxbxBkd()
    {
        return field_204_lcbPlcftxbxBkd;
    }

    /**
     * Set the lcbPlcftxbxBkd field for the FIB record.
     */
    public void setLcbPlcftxbxBkd(int field_204_lcbPlcftxbxBkd)
    {
        this.field_204_lcbPlcftxbxBkd = field_204_lcbPlcftxbxBkd;
    }

    /**
     * Get the fcPlcftxbxHdrBkd field for the FIB record.
     */
    public int getFcPlcftxbxHdrBkd()
    {
        return field_205_fcPlcftxbxHdrBkd;
    }

    /**
     * Set the fcPlcftxbxHdrBkd field for the FIB record.
     */
    public void setFcPlcftxbxHdrBkd(int field_205_fcPlcftxbxHdrBkd)
    {
        this.field_205_fcPlcftxbxHdrBkd = field_205_fcPlcftxbxHdrBkd;
    }

    /**
     * Get the lcbPlcftxbxHdrBkd field for the FIB record.
     */
    public int getLcbPlcftxbxHdrBkd()
    {
        return field_206_lcbPlcftxbxHdrBkd;
    }

    /**
     * Set the lcbPlcftxbxHdrBkd field for the FIB record.
     */
    public void setLcbPlcftxbxHdrBkd(int field_206_lcbPlcftxbxHdrBkd)
    {
        this.field_206_lcbPlcftxbxHdrBkd = field_206_lcbPlcftxbxHdrBkd;
    }

    /**
     * Get the fcDocUndo field for the FIB record.
     */
    public int getFcDocUndo()
    {
        return field_207_fcDocUndo;
    }

    /**
     * Set the fcDocUndo field for the FIB record.
     */
    public void setFcDocUndo(int field_207_fcDocUndo)
    {
        this.field_207_fcDocUndo = field_207_fcDocUndo;
    }

    /**
     * Get the lcbDocUndo field for the FIB record.
     */
    public int getLcbDocUndo()
    {
        return field_208_lcbDocUndo;
    }

    /**
     * Set the lcbDocUndo field for the FIB record.
     */
    public void setLcbDocUndo(int field_208_lcbDocUndo)
    {
        this.field_208_lcbDocUndo = field_208_lcbDocUndo;
    }

    /**
     * Get the fcRgbuse field for the FIB record.
     */
    public int getFcRgbuse()
    {
        return field_209_fcRgbuse;
    }

    /**
     * Set the fcRgbuse field for the FIB record.
     */
    public void setFcRgbuse(int field_209_fcRgbuse)
    {
        this.field_209_fcRgbuse = field_209_fcRgbuse;
    }

    /**
     * Get the lcbRgbuse field for the FIB record.
     */
    public int getLcbRgbuse()
    {
        return field_210_lcbRgbuse;
    }

    /**
     * Set the lcbRgbuse field for the FIB record.
     */
    public void setLcbRgbuse(int field_210_lcbRgbuse)
    {
        this.field_210_lcbRgbuse = field_210_lcbRgbuse;
    }

    /**
     * Get the fcUsp field for the FIB record.
     */
    public int getFcUsp()
    {
        return field_211_fcUsp;
    }

    /**
     * Set the fcUsp field for the FIB record.
     */
    public void setFcUsp(int field_211_fcUsp)
    {
        this.field_211_fcUsp = field_211_fcUsp;
    }

    /**
     * Get the lcbUsp field for the FIB record.
     */
    public int getLcbUsp()
    {
        return field_212_lcbUsp;
    }

    /**
     * Set the lcbUsp field for the FIB record.
     */
    public void setLcbUsp(int field_212_lcbUsp)
    {
        this.field_212_lcbUsp = field_212_lcbUsp;
    }

    /**
     * Get the fcUskf field for the FIB record.
     */
    public int getFcUskf()
    {
        return field_213_fcUskf;
    }

    /**
     * Set the fcUskf field for the FIB record.
     */
    public void setFcUskf(int field_213_fcUskf)
    {
        this.field_213_fcUskf = field_213_fcUskf;
    }

    /**
     * Get the lcbUskf field for the FIB record.
     */
    public int getLcbUskf()
    {
        return field_214_lcbUskf;
    }

    /**
     * Set the lcbUskf field for the FIB record.
     */
    public void setLcbUskf(int field_214_lcbUskf)
    {
        this.field_214_lcbUskf = field_214_lcbUskf;
    }

    /**
     * Get the fcPlcupcRgbuse field for the FIB record.
     */
    public int getFcPlcupcRgbuse()
    {
        return field_215_fcPlcupcRgbuse;
    }

    /**
     * Set the fcPlcupcRgbuse field for the FIB record.
     */
    public void setFcPlcupcRgbuse(int field_215_fcPlcupcRgbuse)
    {
        this.field_215_fcPlcupcRgbuse = field_215_fcPlcupcRgbuse;
    }

    /**
     * Get the lcbPlcupcRgbuse field for the FIB record.
     */
    public int getLcbPlcupcRgbuse()
    {
        return field_216_lcbPlcupcRgbuse;
    }

    /**
     * Set the lcbPlcupcRgbuse field for the FIB record.
     */
    public void setLcbPlcupcRgbuse(int field_216_lcbPlcupcRgbuse)
    {
        this.field_216_lcbPlcupcRgbuse = field_216_lcbPlcupcRgbuse;
    }

    /**
     * Get the fcPlcupcUsp field for the FIB record.
     */
    public int getFcPlcupcUsp()
    {
        return field_217_fcPlcupcUsp;
    }

    /**
     * Set the fcPlcupcUsp field for the FIB record.
     */
    public void setFcPlcupcUsp(int field_217_fcPlcupcUsp)
    {
        this.field_217_fcPlcupcUsp = field_217_fcPlcupcUsp;
    }

    /**
     * Get the lcbPlcupcUsp field for the FIB record.
     */
    public int getLcbPlcupcUsp()
    {
        return field_218_lcbPlcupcUsp;
    }

    /**
     * Set the lcbPlcupcUsp field for the FIB record.
     */
    public void setLcbPlcupcUsp(int field_218_lcbPlcupcUsp)
    {
        this.field_218_lcbPlcupcUsp = field_218_lcbPlcupcUsp;
    }

    /**
     * Get the fcSttbGlsyStyle field for the FIB record.
     */
    public int getFcSttbGlsyStyle()
    {
        return field_219_fcSttbGlsyStyle;
    }

    /**
     * Set the fcSttbGlsyStyle field for the FIB record.
     */
    public void setFcSttbGlsyStyle(int field_219_fcSttbGlsyStyle)
    {
        this.field_219_fcSttbGlsyStyle = field_219_fcSttbGlsyStyle;
    }

    /**
     * Get the lcbSttbGlsyStyle field for the FIB record.
     */
    public int getLcbSttbGlsyStyle()
    {
        return field_220_lcbSttbGlsyStyle;
    }

    /**
     * Set the lcbSttbGlsyStyle field for the FIB record.
     */
    public void setLcbSttbGlsyStyle(int field_220_lcbSttbGlsyStyle)
    {
        this.field_220_lcbSttbGlsyStyle = field_220_lcbSttbGlsyStyle;
    }

    /**
     * Get the fcPlgosl field for the FIB record.
     */
    public int getFcPlgosl()
    {
        return field_221_fcPlgosl;
    }

    /**
     * Set the fcPlgosl field for the FIB record.
     */
    public void setFcPlgosl(int field_221_fcPlgosl)
    {
        this.field_221_fcPlgosl = field_221_fcPlgosl;
    }

    /**
     * Get the lcbPlgosl field for the FIB record.
     */
    public int getLcbPlgosl()
    {
        return field_222_lcbPlgosl;
    }

    /**
     * Set the lcbPlgosl field for the FIB record.
     */
    public void setLcbPlgosl(int field_222_lcbPlgosl)
    {
        this.field_222_lcbPlgosl = field_222_lcbPlgosl;
    }

    /**
     * Get the fcPlcocx field for the FIB record.
     */
    public int getFcPlcocx()
    {
        return field_223_fcPlcocx;
    }

    /**
     * Set the fcPlcocx field for the FIB record.
     */
    public void setFcPlcocx(int field_223_fcPlcocx)
    {
        this.field_223_fcPlcocx = field_223_fcPlcocx;
    }

    /**
     * Get the lcbPlcocx field for the FIB record.
     */
    public int getLcbPlcocx()
    {
        return field_224_lcbPlcocx;
    }

    /**
     * Set the lcbPlcocx field for the FIB record.
     */
    public void setLcbPlcocx(int field_224_lcbPlcocx)
    {
        this.field_224_lcbPlcocx = field_224_lcbPlcocx;
    }

    /**
     * Get the fcPlcfbteLvc field for the FIB record.
     */
    public int getFcPlcfbteLvc()
    {
        return field_225_fcPlcfbteLvc;
    }

    /**
     * Set the fcPlcfbteLvc field for the FIB record.
     */
    public void setFcPlcfbteLvc(int field_225_fcPlcfbteLvc)
    {
        this.field_225_fcPlcfbteLvc = field_225_fcPlcfbteLvc;
    }

    /**
     * Get the lcbPlcfbteLvc field for the FIB record.
     */
    public int getLcbPlcfbteLvc()
    {
        return field_226_lcbPlcfbteLvc;
    }

    /**
     * Set the lcbPlcfbteLvc field for the FIB record.
     */
    public void setLcbPlcfbteLvc(int field_226_lcbPlcfbteLvc)
    {
        this.field_226_lcbPlcfbteLvc = field_226_lcbPlcfbteLvc;
    }

    /**
     * Get the ftModified field for the FIB record.
     */
    public int getFtModified()
    {
        return field_227_ftModified;
    }

    /**
     * Set the ftModified field for the FIB record.
     */
    public void setFtModified(int field_227_ftModified)
    {
        this.field_227_ftModified = field_227_ftModified;
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
     * Sets the template field value.
     * 
     */
    public void setTemplate(boolean value)
    {
        field_6_options = template.setShortBoolean(field_6_options, value);
    }

    /**
     * 
     * @return  the template field value.
     */
    public boolean isTemplate()
    {
        return template.isSet(field_6_options);
    }

    /**
     * Sets the glossary field value.
     * 
     */
    public void setGlossary(boolean value)
    {
        field_6_options = glossary.setShortBoolean(field_6_options, value);
    }

    /**
     * 
     * @return  the glossary field value.
     */
    public boolean isGlossary()
    {
        return glossary.isSet(field_6_options);
    }

    /**
     * Sets the quicksave field value.
     * 
     */
    public void setQuicksave(boolean value)
    {
        field_6_options = quicksave.setShortBoolean(field_6_options, value);
    }

    /**
     * 
     * @return  the quicksave field value.
     */
    public boolean isQuicksave()
    {
        return quicksave.isSet(field_6_options);
    }

    /**
     * Sets the haspictr field value.
     * 
     */
    public void setHaspictr(boolean value)
    {
        field_6_options = haspictr.setShortBoolean(field_6_options, value);
    }

    /**
     * 
     * @return  the haspictr field value.
     */
    public boolean isHaspictr()
    {
        return haspictr.isSet(field_6_options);
    }

    /**
     * Sets the nquicksaves field value.
     * 
     */
    public void setNquicksaves(boolean value)
    {
        field_6_options = nquicksaves.setShortBoolean(field_6_options, value);
    }

    /**
     * 
     * @return  the nquicksaves field value.
     */
    public boolean isNquicksaves()
    {
        return nquicksaves.isSet(field_6_options);
    }

    /**
     * Sets the encrypted field value.
     * 
     */
    public void setEncrypted(boolean value)
    {
        field_6_options = encrypted.setShortBoolean(field_6_options, value);
    }

    /**
     * 
     * @return  the encrypted field value.
     */
    public boolean isEncrypted()
    {
        return encrypted.isSet(field_6_options);
    }

    /**
     * Sets the tabletype field value.
     * 
     */
    public void setTabletype(boolean value)
    {
        field_6_options = tabletype.setShortBoolean(field_6_options, value);
    }

    /**
     * 
     * @return  the tabletype field value.
     */
    public boolean isTabletype()
    {
        return tabletype.isSet(field_6_options);
    }

    /**
     * Sets the readonly field value.
     * 
     */
    public void setReadonly(boolean value)
    {
        field_6_options = readonly.setShortBoolean(field_6_options, value);
    }

    /**
     * 
     * @return  the readonly field value.
     */
    public boolean isReadonly()
    {
        return readonly.isSet(field_6_options);
    }

    /**
     * Sets the writeReservation field value.
     * 
     */
    public void setWriteReservation(boolean value)
    {
        field_6_options = writeReservation.setShortBoolean(field_6_options, value);
    }

    /**
     * 
     * @return  the writeReservation field value.
     */
    public boolean isWriteReservation()
    {
        return writeReservation.isSet(field_6_options);
    }

    /**
     * Sets the extendedCharacter field value.
     * 
     */
    public void setExtendedCharacter(boolean value)
    {
        field_6_options = extendedCharacter.setShortBoolean(field_6_options, value);
    }

    /**
     * 
     * @return  the extendedCharacter field value.
     */
    public boolean isExtendedCharacter()
    {
        return extendedCharacter.isSet(field_6_options);
    }

    /**
     * Sets the loadOverride field value.
     * 
     */
    public void setLoadOverride(boolean value)
    {
        field_6_options = loadOverride.setShortBoolean(field_6_options, value);
    }

    /**
     * 
     * @return  the loadOverride field value.
     */
    public boolean isLoadOverride()
    {
        return loadOverride.isSet(field_6_options);
    }

    /**
     * Sets the farEast field value.
     * 
     */
    public void setFarEast(boolean value)
    {
        field_6_options = farEast.setShortBoolean(field_6_options, value);
    }

    /**
     * 
     * @return  the farEast field value.
     */
    public boolean isFarEast()
    {
        return farEast.isSet(field_6_options);
    }

    /**
     * Sets the crypto field value.
     * 
     */
    public void setCrypto(boolean value)
    {
        field_6_options = crypto.setShortBoolean(field_6_options, value);
    }

    /**
     * 
     * @return  the crypto field value.
     */
    public boolean isCrypto()
    {
        return crypto.isSet(field_6_options);
    }

    /**
     * Sets the history mac field value.
     * 
     */
    public void setHistoryMac(boolean value)
    {
        field_10_history = historyMac.setShortBoolean(field_10_history, value);
    }

    /**
     * 
     * @return  the history mac field value.
     */
    public boolean isHistoryMac()
    {
        return historyMac.isSet(field_10_history);
    }

    /**
     * Sets the empty special field value.
     * 
     */
    public void setEmptySpecial(boolean value)
    {
        field_10_history = emptySpecial.setShortBoolean(field_10_history, value);
    }

    /**
     * 
     * @return  the empty special field value.
     */
    public boolean isEmptySpecial()
    {
        return emptySpecial.isSet(field_10_history);
    }

    /**
     * Sets the load override hist field value.
     * 
     */
    public void setLoadOverrideHist(boolean value)
    {
        field_10_history = loadOverrideHist.setShortBoolean(field_10_history, value);
    }

    /**
     * 
     * @return  the load override hist field value.
     */
    public boolean isLoadOverrideHist()
    {
        return loadOverrideHist.isSet(field_10_history);
    }

    /**
     * Sets the feature undo field value.
     * 
     */
    public void setFeatureUndo(boolean value)
    {
        field_10_history = featureUndo.setShortBoolean(field_10_history, value);
    }

    /**
     * 
     * @return  the feature undo field value.
     */
    public boolean isFeatureUndo()
    {
        return featureUndo.isSet(field_10_history);
    }

    /**
     * Sets the v97 saved field value.
     * 
     */
    public void setV97Saved(boolean value)
    {
        field_10_history = v97Saved.setShortBoolean(field_10_history, value);
    }

    /**
     * 
     * @return  the v97 saved field value.
     */
    public boolean isV97Saved()
    {
        return v97Saved.isSet(field_10_history);
    }

    /**
     * Sets the spare field value.
     * 
     */
    public void setSpare(boolean value)
    {
        field_10_history = spare.setShortBoolean(field_10_history, value);
    }

    /**
     * 
     * @return  the spare field value.
     */
    public boolean isSpare()
    {
        return spare.isSet(field_10_history);
    }


}  // END OF CLASS




