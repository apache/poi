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
    //private short field_8_
        
        
    /** Creates a new instance of FileInformationBlock */
    public FileInformationBlock() {
    }

}
