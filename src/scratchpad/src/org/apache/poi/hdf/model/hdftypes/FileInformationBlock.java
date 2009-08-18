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

package org.apache.poi.hdf.model.hdftypes;


import org.apache.poi.hdf.model.hdftypes.definitions.FIBAbstractType;

/**
 *
 * @author  andy
 */
public final class FileInformationBlock extends FIBAbstractType
{
/*
    private  short field_1_id;
    private  short field_2_version; // 101 = Word 6.0 +
    private  short field_3_product_version;
    private  short field_4_language_stamp;
    private  short field_5_unknown;
    private  short field_6_options;

        private static final BitField template          = BitFieldFactory.getInstance(0x0001);
        private static final BitField glossary          = BitFieldFactory.getInstance(0x0002);
        private static final BitField quicksave         = BitFieldFactory.getInstance(0x0004);
        private static final BitField haspictr          = BitFieldFactory.getInstance(0x0008);
        private static final BitField nquicksaves       = BitFieldFactory.getInstance(0x00F0);
        private static final BitField encrypted         = BitFieldFactory.getInstance(0x0100);
        private static final BitField tabletype         = BitFieldFactory.getInstance(0x0200);
        private static final BitField readonly          = BitFieldFactory.getInstance(0x0400);
        private static final BitField writeReservation  = BitFieldFactory.getInstance(0x0800);
        private static final BitField extendedCharacter = BitFieldFactory.getInstance(0x1000);
        private static final BitField loadOverride      = BitFieldFactory.getInstance(0x2000);
        private static final BitField farEast           = BitFieldFactory.getInstance(0x4000);
        private static final BitField crypto            = BitFieldFactory.getInstance(0x8000);

    private short field_7_minversion;
    private short field_8_encrypted_key;
    private short field_9_environment; // 0 or 1 - windows or mac
    private short field_10_history;

        private static final BitField history_mac   = BitFieldFactory.getInstance(0x01);
        private static final BitField empty_special = BitFieldFactory.getInstance(0x02);
        private static final BitField load_override = BitFieldFactory.getInstance(0x04);
        private static final BitField future_undo   = BitFieldFactory.getInstance(0x08);
        private static final BitField w97_saved     = BitFieldFactory.getInstance(0x10);
        private static final BitField spare         = BitFieldFactory.getInstance(0xfe);

    private short field_11_default_charset;
    private short field_12_default_extcharset;
    private int  field_13_offset_first_char;
    private int  field_14_offset_last_char;
    private short field_15_count_shorts;

    private short field_16_beg_shorts; //why same offset?

    private short field_16_creator_id;
    private short field_17_revisor_id;
    private short field_18_creator_private;
    private short field_19_revisor_private;

    private short field_20_unused;
    private short field_21_unused;
    private short field_22_unused;
    private short field_23_unused;
    private short field_24_unused;
    private short field_25_unused;
    private short field_26_unused;
    private short field_27_unused;
    private short field_28_unused;

    private short field_29_fareastid;
    private short field_30_count_ints;

    private int field_31_beg_ints; //why same offset?

    private int field_31_last_byte;

    private int field_32_creator_build_date;
    private int field_33_revisor_build_date; */
    /** length of main document text stream*/
//    private int field_34_main_streamlen;
    /**length of footnote subdocument text stream*/
/*    private int field_35_footnote_streamlen;
    private int field_36_header_streamlen;
    private int field_37_macro_streamlen;
    private int field_38_annotation_streamlen;
    private int field_39_endnote_streamlen;
    private int field_40_textbox_streamlen;
    private int field_41_headbox_streamlen; */
    /**offset in table stream of character property bin table*/
//    private int field_42_pointer_to_plc_list_chp; //rename me!
//    private int field_43_first_chp; //rename me
//    private int field_44_count_chps; //rename me
    /**offset in table stream of paragraph property bin */
 /*   private int field_45_pointer_to_plc_list_pap; //rename me.
    private int field_46_first_pap; //rename me
    private int field_47_count_paps; //rename me
    private int field_48_pointer_to_plc_list_lvc; //rename me
    private int field_49_first_lvc; //rename me
    private int field_50_count_lvc; //rename me

    private int field_51_unknown;
    private int field_52_unknown; */
    //not sure about this array.
/*
    private short field_53_fc_lcb_array_size;
    private int field_54_original_stylesheet_offset;
    private int field_55_original_stylesheet_size;
    private int field_56_stylesheet_offset;
    private int field_57_stylesheet_size;
    private int field_58_footnote_ref_offset;
    private int field_59_footnote_ref_size;
    private int field_60_footnote_plc_offset;
    private int field_61_footnote_plc_size;
    private int field_62_annotation_ref_offset;
    private int field_63_annotation_ref_size;
    private int field_64_annotation_plc_offset;
    private int field_65_annotation_plc_size; */
    /** offset in table stream of section descriptor SED PLC*/
/*    private int field_66_section_plc_offset;
    private int field_67_section_plc_size;
    private int field_68_unused;
    private int field_69_unused;
    private int field_70_pheplc_offset;
    private int field_71_pheplc_size;
    private int field_72_glossaryST_offset;
    private int field_73_glossaryST_size;
    private int field_74_glossaryPLC_offset;
    private int field_75_glossaryPLC_size;
    private int field_76_headerPLC_offset;
    private int field_77_headerPLC_size;
    private int field_78_chp_bin_table_offset;
    private int field_79_chp_bin_table_size;
    private int field_80_pap_bin_table_offset;
    private int field_81_pap_bin_table_size;
    private int field_82_sea_plc_offset;
    private int field_83_sea_plc_size;
    private int field_84_fonts_offset;
    private int field_85_fonts_size;
    private int field_86_main_fields_offset;
    private int field_87_main_fields_size;
    private int field_88_header_fields_offset;
    private int field_89_header_fields_size;
    private int field_90_footnote_fields_offset;
    private int field_91_footnote_fields_size;
    private int field_92_ann_fields_offset;
    private int field_93_ann_fields_size;
    private int field_94_unused;
    private int field_95_unused;
    private int field_96_bookmark_names_offset;
    private int field_97_bookmark_names_size;
    private int field_98_bookmark_offsets_offset;
    private int field_99_bookmark_offsets_size;
    private int field_100_macros_offset;
    private int field_101_macros_size;
    private int field_102_unused;
    private int field_103_unused;
    private int field_104_unused;
    private int field_105_unused;
    private int field_106_printer_offset;
    private int field_107_printer_size;
    private int field_108_printer_portrait_offset;
    private int field_109_printer_portrait_size;
    private int field_110_printer_landscape_offset;
    private int field_111_printer_landscape_size;
    private int field_112_wss_offset;
    private int field_113_wss_size;
    private int field_114_DOP_offset;
    private int field_115_DOP_size;
    private int field_116_sttbfassoc_offset;
    private int field_117_sttbfassoc_size; */
    /**offset in table stream of beginning of information for complex files.
     * Also, this is the beginning of the Text piece table*/ /*
    private int field_118_textPieceTable_offset;
    private int field_119_textPieceTable_size;
    private int field_199_list_format_offset;
    private int field_200_list_format_size;
    private int field_201_list_format_override_offset;
    private int field_202_list_format_override_size;




^/
    /** Creates a new instance of FileInformationBlock */
    public FileInformationBlock(byte[] mainDocument)
    {
        fillFields(mainDocument, (short)0, (short)0);
/*        field_1_id = LittleEndian.getShort(mainDocument, 0);
        field_2_version = LittleEndian.getShort(mainDocument, 0x2); // 101 = Word 6.0 +
        field_3_product_version = LittleEndian.getShort(mainDocument, 0x4);
        field_4_language_stamp = LittleEndian.getShort(mainDocument, 0x6);
        field_5_unknown = LittleEndian.getShort(mainDocument, 0x8);
        field_6_options = LittleEndian.getShort(mainDocument, 0xa);



        field_13_offset_first_char = LittleEndian.getInt(mainDocument, 0x18);
        field_34_main_streamlen = LittleEndian.getInt(mainDocument, 0x4c);
        field_35_footnote_streamlen = LittleEndian.getInt(mainDocument, 0x50);

        field_56_stylesheet_offset = LittleEndian.getInt(mainDocument, 0xa2);
        field_57_stylesheet_size = LittleEndian.getInt(mainDocument, 0xa6);
        field_66_section_plc_offset = LittleEndian.getInt(mainDocument, 0xca);
        field_67_section_plc_size = LittleEndian.getInt(mainDocument, 0xce);

        field_78_chp_bin_table_offset = LittleEndian.getInt(mainDocument, 0xfa);
        field_79_chp_bin_table_size = LittleEndian.getInt(mainDocument, 0xfe);
        field_80_pap_bin_table_offset = LittleEndian.getInt(mainDocument, 0x102);
        field_81_pap_bin_table_size = LittleEndian.getInt(mainDocument, 0x106);

        field_84_fonts_offset = LittleEndian.getInt(mainDocument, 0x112);
        field_85_fonts_size = LittleEndian.getInt(mainDocument, 0x116);

        field_114_DOP_offset = LittleEndian.getInt(mainDocument, 0x192);
        field_115_DOP_size = LittleEndian.getInt(mainDocument, 0x196);
        field_118_textPieceTable_offset = LittleEndian.getInt(mainDocument, 0x1a2);

        field_199_list_format_offset = LittleEndian.getInt(mainDocument, 0x2e2);
        field_200_list_format_size = LittleEndian.getInt(mainDocument, 0x2e6);
        field_201_list_format_override_offset = LittleEndian.getInt(mainDocument, 0x2ea);
        field_202_list_format_override_size= LittleEndian.getInt(mainDocument, 0x2ee);*/

    }
/*
    public boolean useTable1()
    {
        return tabletype.setShort(field_6_options) > 0;
    }
    public int getFirstCharOffset()
    {
        return field_13_offset_first_char;
    }
    public int getStshOffset()
    {
        return field_56_stylesheet_offset;
    }
    public int getStshSize()
    {
        return field_57_stylesheet_size;
    }
    public int getSectionDescriptorOffset()
    {
        return field_66_section_plc_offset;
    }
    public int getSectionDescriptorSize()
    {
        return field_67_section_plc_size;
    }
    public int getChpBinTableOffset()
    {
        return field_78_chp_bin_table_offset;
    }
    public int getChpBinTableSize()
    {
        return field_79_chp_bin_table_size;
    }
    public int getPapBinTableOffset()
    {
        return field_80_pap_bin_table_offset;
    }
    public int getPapBinTableSize()
    {
        return field_81_pap_bin_table_size;
    }
    public int getFontsOffset()
    {
        return field_84_fonts_offset;
    }
    public int getFontsSize()
    {
        return field_85_fonts_size;
    }
    public int getDOPOffset()
    {
         return field_114_DOP_offset;
    }
    public int getDOPSize()
    {
        return field_115_DOP_size;
    }
    public int getComplexOffset()
    {
        return field_118_textPieceTable_offset;
    }
    public int getLSTOffset()
    {
        return field_199_list_format_offset;
    }
    public int getLSTSize()
    {
        return field_200_list_format_size;
    }
    public int getLFOOffset()
    {
        return field_201_list_format_override_offset;
    }
    public int getLFOSize()
    {
        return field_202_list_format_override_size;
    }
*/

}


