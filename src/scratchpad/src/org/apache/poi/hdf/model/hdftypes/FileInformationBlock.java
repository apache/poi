/*
 * FileInformationBlock.java
 *
 * Created on February 24, 2002, 2:37 PM
 */

package org.apache.poi.hdf.model.hdftypes;

import org.apache.poi.util.BitField;

/**
 *
 * @author  andy
 */
public class FileInformationBlock implements HDFType {

    private  short field_1_id;
    private  short field_2_version; // 101 = Word 6.0 +
    private  short field_3_product_version;
    private  short field_4_language_stamp;
    private  short field_5_unknown;
    private  short field_6_options;    
    
        private static final BitField template          = new BitField(0x0001);
        private static final BitField glossary          = new BitField(0x0002);
        private static final BitField quicksave         = new BitField(0x0004);
        private static final BitField haspictr          = new BitField(0x0008);        
        private static final BitField nquicksaves       = new BitField(0x000F);
        private static final BitField encrypted         = new BitField(0x0100);        
        private static final BitField tabletype         = new BitField(0x0200);        
        private static final BitField readonly          = new BitField(0x0400);
        private static final BitField writeReservation  = new BitField(0x0800);
        private static final BitField extendedCharacter = new BitField(0x1000);
        private static final BitField loadOverride      = new BitField(0x2000);
        private static final BitField farEast           = new BitField(0x4000);
        private static final BitField crypto            = new BitField(0x8000);
   
    private short field_7_minversion;
    private short field_8_encrypted_key;
    private short field_9_environment; // 0 or 1 - windows or mac
    private short field_10_history;
        
        private static final BitField history_mac   = new BitField(0x01);
        private static final BitField empty_special = new BitField(0x02);
        private static final BitField load_override = new BitField(0x04);
        private static final BitField future_undo   = new BitField(0x08);
        private static final BitField w97_saved     = new BitField(0x10);
        private static final BitField spare         = new BitField(0xfe); 
        
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
    private int field_33_revisor_build_date;
    private int field_34_main_streamlen;
    private int field_35_footnote_streamlen;
    private int field_36_header_streamlen;
    private int field_37_macro_streamlen;
    private int field_38_annotation_streamlen;
    private int field_39_endnote_streamlen;
    private int field_40_textbox_streamlen;
    private int field_41_headbox_streamlen;
    private int field_42_pointer_to_plc_list_chp; //rename me!
    private int field_43_first_chp; //rename me
    private int field_44_count_chps; //rename me
    private int field_45_pointer_to_plc_list_pap; //rename me.
    private int field_46_first_pap; //rename me
    private int field_47_count_paps; //rename me
    private int field_48_pointer_to_plc_list_lvc; //rename me
    private int field_49_first_lvc; //rename me
    private int field_50_count_lvc; //rename me
    
    private int field_51_unknown;
    private int field_52_unknown;
    
    
    
    
    
    
        
    /** Creates a new instance of FileInformationBlock */
    public FileInformationBlock() {
    }

}

