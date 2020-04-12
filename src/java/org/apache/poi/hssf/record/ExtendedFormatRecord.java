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


package org.apache.poi.hssf.record;

import static org.apache.poi.util.GenericRecordUtil.getBitsAsString;
import static org.apache.poi.util.GenericRecordUtil.getEnumBitsAsString;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Probably one of the more complex records.<p>
 * There are two breeds: Style and Cell.<p>
 * It should be noted that fields in the extended format record are somewhat arbitrary.
 * Almost all of the fields are bit-level, but we name them as best as possible by functional group.
 * In some places this is better than others.
 *
 * @since 2.0-pre
 */

public final class ExtendedFormatRecord extends StandardRecord {
    public static final short sid                 = 0xE0;

    // null constant
    public static final short NULL                = (short)0xfff0;

    // xf type
    public static final short XF_STYLE            = 1;
    public static final short XF_CELL             = 0;

    // borders
    public static final short NONE                = 0x0;
    public static final short THIN                = 0x1;
    public static final short MEDIUM              = 0x2;
    public static final short DASHED              = 0x3;
    public static final short DOTTED              = 0x4;
    public static final short THICK               = 0x5;
    public static final short DOUBLE              = 0x6;
    public static final short HAIR                = 0x7;
    public static final short MEDIUM_DASHED       = 0x8;
    public static final short DASH_DOT            = 0x9;
    public static final short MEDIUM_DASH_DOT     = 0xA;
    public static final short DASH_DOT_DOT        = 0xB;
    public static final short MEDIUM_DASH_DOT_DOT = 0xC;
    public static final short SLANTED_DASH_DOT    = 0xD;

    // alignment
    public static final short GENERAL             = 0x0;
    public static final short LEFT                = 0x1;
    public static final short CENTER              = 0x2;
    public static final short RIGHT               = 0x3;
    public static final short FILL                = 0x4;
    public static final short JUSTIFY             = 0x5;
    public static final short CENTER_SELECTION    = 0x6;

    // vertical alignment
    public static final short VERTICAL_TOP        = 0x0;
    public static final short VERTICAL_CENTER     = 0x1;
    public static final short VERTICAL_BOTTOM     = 0x2;
    public static final short VERTICAL_JUSTIFY    = 0x3;

    // fill
    public static final short NO_FILL             = 0;
    public static final short SOLID_FILL          = 1;
    public static final short FINE_DOTS           = 2;
    public static final short ALT_BARS            = 3;
    public static final short SPARSE_DOTS         = 4;
    public static final short THICK_HORZ_BANDS    = 5;
    public static final short THICK_VERT_BANDS    = 6;
    public static final short THICK_BACKWARD_DIAG = 7;
    public static final short THICK_FORWARD_DIAG  = 8;
    public static final short BIG_SPOTS           = 9;
    public static final short BRICKS              = 10;
    public static final short THIN_HORZ_BANDS     = 11;
    public static final short THIN_VERT_BANDS     = 12;
    public static final short THIN_BACKWARD_DIAG  = 13;
    public static final short THIN_FORWARD_DIAG   = 14;
    public static final short SQUARES             = 15;
    public static final short DIAMONDS            = 16;

    // field_3_cell_options bit map
    private static final BitField _locked       = bf(0x0001);
    private static final BitField _hidden       = bf(0x0002);
    private static final BitField _xf_type      = bf(0x0004);
    private static final BitField _123_prefix   = bf(0x0008);
    private static final BitField _parent_index = bf(0xFFF0);

    // field_4_alignment_options bit map
    private static final BitField _alignment          = bf(0x0007);
    private static final BitField _wrap_text          = bf(0x0008);
    private static final BitField _vertical_alignment = bf(0x0070);
    private static final BitField _justify_last       = bf(0x0080);
    private static final BitField _rotation           = bf(0xFF00);

    // field_5_indention_options
    private static final BitField _indent        = bf(0x000F);
    private static final BitField _shrink_to_fit = bf(0x0010);
    private static final BitField _merge_cells   = bf(0x0020);
    private static final BitField _reading_order = bf(0x00C0);

    // apparently bits 8 and 9 are unused
    private static final BitField _indent_not_parent_format       = bf(0x0400);
    private static final BitField _indent_not_parent_font         = bf(0x0800);
    private static final BitField _indent_not_parent_alignment    = bf(0x1000);
    private static final BitField _indent_not_parent_border       = bf(0x2000);
    private static final BitField _indent_not_parent_pattern      = bf(0x4000);
    private static final BitField _indent_not_parent_cell_options = bf(0x8000);

    // field_6_border_options bit map
    private static final BitField _border_left   = bf(0x000F);
    private static final BitField _border_right  = bf(0x00F0);
    private static final BitField _border_top    = bf(0x0F00);
    private static final BitField _border_bottom = bf(0xF000);

    // all three of the following attributes are palette options
    // field_7_palette_options bit map
    private static final BitField _left_border_palette_idx  = bf(0x007F);
    private static final BitField _right_border_palette_idx = bf(0x3F80);
    private static final BitField _diag                     = bf(0xC000);

    // field_8_adtl_palette_options bit map
    private static final BitField _top_border_palette_idx    = bf(0x0000007F);
    private static final BitField _bottom_border_palette_idx = bf(0x00003F80);
    private static final BitField _adtl_diag                 = bf(0x001fc000);
    private static final BitField _adtl_diag_line_style      = bf(0x01e00000);

    // apparently bit 25 is unused
    private static final BitField _adtl_fill_pattern         = bf(0xfc000000);

    // field_9_fill_palette_options bit map
    private static final BitField _fill_foreground = bf(0x007F);
    private static final BitField _fill_background = bf(0x3f80);

    private static BitField bf(int i) {
        return BitFieldFactory.getInstance(i);
    }


    // fields in BOTH style and Cell XF records
    private short field_1_font_index;             // not bit-mapped
    private short field_2_format_index;           // not bit-mapped

    private short field_3_cell_options;
    private short field_4_alignment_options;
    private short field_5_indention_options;
    private short field_6_border_options;
    private short field_7_palette_options;
    private int   field_8_adtl_palette_options;   // additional to avoid 2


    // apparently bits 15 and 14 are unused
    private short field_9_fill_palette_options;

    public ExtendedFormatRecord() {}

    public ExtendedFormatRecord(ExtendedFormatRecord other) {
        super(other);
        field_1_font_index           = other.field_1_font_index;
        field_2_format_index         = other.field_2_format_index;
        field_3_cell_options         = other.field_3_cell_options;
        field_4_alignment_options    = other.field_4_alignment_options;
        field_5_indention_options    = other.field_5_indention_options;
        field_6_border_options       = other.field_6_border_options;
        field_7_palette_options      = other.field_7_palette_options;
        field_8_adtl_palette_options = other.field_8_adtl_palette_options;
        field_9_fill_palette_options = other.field_9_fill_palette_options;
    }

    public ExtendedFormatRecord(RecordInputStream in) {
        field_1_font_index           = in.readShort();
        field_2_format_index         = in.readShort();
        field_3_cell_options         = in.readShort();
        field_4_alignment_options    = in.readShort();
        field_5_indention_options    = in.readShort();
        field_6_border_options       = in.readShort();
        field_7_palette_options      = in.readShort();
        field_8_adtl_palette_options = in.readInt();
        field_9_fill_palette_options = in.readShort();
    }

    /**
     * set the index to the FONT record (which font to use 0 based)
     *
     *
     * @param index to the font
     * @see org.apache.poi.hssf.record.FontRecord
     */

    public void setFontIndex(short index)
    {
        field_1_font_index = index;
    }

    /**
     *  set the index to the Format record (which FORMAT to use 0-based)
     *
     *
     * @param index to the format record
     * @see org.apache.poi.hssf.record.FormatRecord
     */

    public void setFormatIndex(short index)
    {
        field_2_format_index = index;
    }

    /**
     * sets the options bitmask - you can also use corresponding option bit setters
     * (see other methods that reference this one)
     *
     *
     * @param options bitmask to set
     *
     */

    public void setCellOptions(short options)
    {
        field_3_cell_options = options;
    }

    // These are the bit fields in cell options

    /**
     * set whether the cell is locked or not
     *
     *
     * @param locked - if the cell is locked
     * @see #setCellOptions(short)
     */

    public void setLocked(boolean locked)
    {
        field_3_cell_options = _locked.setShortBoolean(field_3_cell_options,
                locked);
    }

    /**
     * set whether the cell is hidden or not
     *
     *
     * @param hidden - if the cell is hidden
     * @see #setCellOptions(short)
     */

    public void setHidden(boolean hidden)
    {
        field_3_cell_options = _hidden.setShortBoolean(field_3_cell_options,
                hidden);
    }

    /**
     * set whether the cell is a cell or style XFRecord
     *
     *
     * @param type - cell or style (0/1)
     * @see #XF_STYLE
     * @see #XF_CELL
     * @see #setCellOptions(short)
     */

    public void setXFType(short type)
    {
        field_3_cell_options = _xf_type.setShortValue(field_3_cell_options,
                type);
    }

    /**
     * set some old holdover from lotus 123.  Who cares, its all over for Lotus.
     * RIP Lotus.
     *
     * @param prefix - the lotus thing to set.
     * @see #setCellOptions(short)
     */

    public void set123Prefix(boolean prefix)
    {
        field_3_cell_options =
            _123_prefix.setShortBoolean(field_3_cell_options, prefix);
    }

    // present in both but NULL except in cell records

    /**
     * for cell XF types this is the parent style (usually 0/normal).  For
     * style this should be NULL.
     *
     * @param parent  index of parent XF
     * @see #NULL
     * @see #setCellOptions(short)
     */

    public void setParentIndex(short parent)
    {
        field_3_cell_options =
            _parent_index.setShortValue(field_3_cell_options, parent);
    }

    // end bitfields in cell options

    /**
     * set the alignment options bitmask.  See corresponding bitsetter methods
     * that reference this one.
     *
     *
     * @param options     - the bitmask to set
     */

    public void setAlignmentOptions(short options)
    {
        field_4_alignment_options = options;
    }

    /**
     * set the horizontal alignment of the cell.
     *
     *
     * @param align - how to align the cell (see constants)
     * @see #GENERAL
     * @see #LEFT
     * @see #CENTER
     * @see #RIGHT
     * @see #FILL
     * @see #JUSTIFY
     * @see #CENTER_SELECTION
     * @see #setAlignmentOptions(short)
     */

    public void setAlignment(short align)
    {
        field_4_alignment_options =
            _alignment.setShortValue(field_4_alignment_options, align);
    }

    /**
     * set whether to wrap the text in the cell
     *
     *
     * @param wrapped - whether or not to wrap the cell text
     * @see #setAlignmentOptions(short)
     */

    public void setWrapText(boolean wrapped)
    {
        field_4_alignment_options =
            _wrap_text.setShortBoolean(field_4_alignment_options, wrapped);
    }

    /**
     * set the vertical alignment of text in the cell
     *
     *
     * @param align     where to align the text
     * @see #VERTICAL_TOP
     * @see #VERTICAL_CENTER
     * @see #VERTICAL_BOTTOM
     * @see #VERTICAL_JUSTIFY
     *
     * @see #setAlignmentOptions(short)
     */

    public void setVerticalAlignment(short align)
    {
        field_4_alignment_options =
            _vertical_alignment.setShortValue(field_4_alignment_options,
                                              align);
    }

    /**
     * Dunno.  Docs just say this is for far east versions..  (I'm guessing it
     * justifies for right-to-left read languages)
     *
     * @param justify use 0 for US
     * @see #setAlignmentOptions(short)
     */

    public void setJustifyLast(short justify)
    {   // for far east languages supported only for format always 0 for US
        field_4_alignment_options =
            _justify_last.setShortValue(field_4_alignment_options, justify);
    }

    /**
     * set the degree of rotation.
     *
     *
     * @param rotation the degree of rotation
     * @see #setAlignmentOptions(short)
     */

    public void setRotation(short rotation)
    {
        field_4_alignment_options =
            _rotation.setShortValue(field_4_alignment_options, rotation);
    }

    /**
     * set the indent options bitmask  (see corresponding bitmask setters that reference
     * this field)
     *
     *
     * @param options bitmask to set.
     *
     */

    public void setIndentionOptions(short options)
    {
        field_5_indention_options = options;
    }

    // set bitfields for indention options

    /**
     * set indention (not sure of the units, think its spaces)
     *
     * @param indent - how far to indent the cell
     * @see #setIndentionOptions(short)
     */

    public void setIndent(short indent)
    {
        field_5_indention_options =
            _indent.setShortValue(field_5_indention_options, indent);
    }

    /**
     * set whether to shrink the text to fit
     *
     *
     * @param shrink - shrink to fit or not
     * @see #setIndentionOptions(short)
     */

    public void setShrinkToFit(boolean shrink)
    {
        field_5_indention_options =
            _shrink_to_fit.setShortBoolean(field_5_indention_options, shrink);
    }

    /**
     * set whether to merge cells
     *
     *
     * @param merge - merge cells or not
     * @see #setIndentionOptions(short)
     */

    public void setMergeCells(boolean merge)
    {
        field_5_indention_options =
            _merge_cells.setShortBoolean(field_5_indention_options, merge);
    }

    /**
     * set the reading order for far east versions (0 - Context, 1 - Left to right,
     * 2 - right to left) - We could use some help with support for the far east.
     *
     * @param order - the reading order (0,1,2)
     * @see #setIndentionOptions(short)
     */

    public void setReadingOrder(short order)
    {   // only for far east  always 0 in US
        field_5_indention_options =
            _reading_order.setShortValue(field_5_indention_options, order);
    }

    /**
     * set whether or not to use the format in this XF instead of the parent XF.
     *
     *
     * @param parent - true if this XF has a different format value than its parent,
     *                 false otherwise.
     * @see #setIndentionOptions(short)
     */

    public void setIndentNotParentFormat(boolean parent)
    {
        field_5_indention_options =
            _indent_not_parent_format
                .setShortBoolean(field_5_indention_options, parent);
    }

    /**
     * set whether or not to use the font in this XF instead of the parent XF.
     *
     *
     * @param font   - true if this XF has a different font value than its parent,
     *                 false otherwise.
     * @see #setIndentionOptions(short)
     */

    public void setIndentNotParentFont(boolean font)
    {
        field_5_indention_options =
            _indent_not_parent_font.setShortBoolean(field_5_indention_options,
                                                    font);
    }

    /**
     * set whether or not to use the alignment in this XF instead of the parent XF.
     *
     *
     * @param alignment true if this XF has a different alignment value than its parent,
     *                  false otherwise.
     * @see #setIndentionOptions(short)
     */

    public void setIndentNotParentAlignment(boolean alignment)
    {
        field_5_indention_options =
            _indent_not_parent_alignment
                .setShortBoolean(field_5_indention_options, alignment);
    }

    /**
     * set whether or not to use the border in this XF instead of the parent XF.
     *
     *
     * @param border - true if this XF has a different border value than its parent,
     *                 false otherwise.
     * @see #setIndentionOptions(short)
     */

    public void setIndentNotParentBorder(boolean border)
    {
        field_5_indention_options =
            _indent_not_parent_border
                .setShortBoolean(field_5_indention_options, border);
    }

    /**
     * <p>Sets whether or not to use the pattern in this XF instead of the
     * parent XF (foreground/background).</p>
     *
     * @param pattern {@code true} if this XF has a different pattern
     *        value than its parent, {@code false} otherwise.
     * @see #setIndentionOptions(short)
     */

    public void setIndentNotParentPattern(boolean pattern)
    {
        field_5_indention_options =
            _indent_not_parent_pattern
                .setShortBoolean(field_5_indention_options, pattern);
    }

    /**
     * set whether or not to use the locking/hidden in this XF instead of the parent XF.
     *
     *
     * @param options true if this XF has a different locking or hidden value than its parent,
     *                 false otherwise.
     * @see #setIndentionOptions(short)
     */

    public void setIndentNotParentCellOptions(boolean options)
    {
        field_5_indention_options =
            _indent_not_parent_cell_options
                .setShortBoolean(field_5_indention_options, options);
    }

    // end indention options bitmask sets

    /**
     * set the border options bitmask (see the corresponding bitsetter methods
     * that reference back to this one)
     *
     * @param options - the bit mask to set
     *
     */

    public void setBorderOptions(short options)
    {
        field_6_border_options = options;
    }

    // border options bitfields

    /**
     * set the borderline style for the left border
     *
     *
     * @param border - type of border for the left side of the cell
     * @see     #NONE
     * @see     #THIN
     * @see     #MEDIUM
     * @see     #DASHED
     * @see     #DOTTED
     * @see     #THICK
     * @see     #DOUBLE
     * @see     #HAIR
     * @see     #MEDIUM_DASHED
     * @see     #DASH_DOT
     * @see     #MEDIUM_DASH_DOT
     * @see     #DASH_DOT_DOT
     * @see     #MEDIUM_DASH_DOT_DOT
     * @see     #SLANTED_DASH_DOT
     * @see #setBorderOptions(short)
     */

    public void setBorderLeft(short border)
    {
        field_6_border_options =
            _border_left.setShortValue(field_6_border_options, border);
    }

    /**
     * set the border line style for the right border
     *
     *
     * @param border - type of border for the right side of the cell
     * @see     #NONE
     * @see     #THIN
     * @see     #MEDIUM
     * @see     #DASHED
     * @see     #DOTTED
     * @see     #THICK
     * @see     #DOUBLE
     * @see     #HAIR
     * @see     #MEDIUM_DASHED
     * @see     #DASH_DOT
     * @see     #MEDIUM_DASH_DOT
     * @see     #DASH_DOT_DOT
     * @see     #MEDIUM_DASH_DOT_DOT
     * @see     #SLANTED_DASH_DOT
     * @see #setBorderOptions(short)
     */

    public void setBorderRight(short border)
    {
        field_6_border_options =
            _border_right.setShortValue(field_6_border_options, border);
    }

    /**
     * set the border line style for the top border
     *
     *
     * @param border - type of border for the top of the cell
     * @see     #NONE
     * @see     #THIN
     * @see     #MEDIUM
     * @see     #DASHED
     * @see     #DOTTED
     * @see     #THICK
     * @see     #DOUBLE
     * @see     #HAIR
     * @see     #MEDIUM_DASHED
     * @see     #DASH_DOT
     * @see     #MEDIUM_DASH_DOT
     * @see     #DASH_DOT_DOT
     * @see     #MEDIUM_DASH_DOT_DOT
     * @see     #SLANTED_DASH_DOT
     * @see #setBorderOptions(short)
     */

    public void setBorderTop(short border)
    {
        field_6_border_options =
            _border_top.setShortValue(field_6_border_options, border);
    }

    /**
     * set the border line style for the bottom border
     *
     *
     * @param border - type of border for the bottom of the cell
     * @see     #NONE
     * @see     #THIN
     * @see     #MEDIUM
     * @see     #DASHED
     * @see     #DOTTED
     * @see     #THICK
     * @see     #DOUBLE
     * @see     #HAIR
     * @see     #MEDIUM_DASHED
     * @see     #DASH_DOT
     * @see     #MEDIUM_DASH_DOT
     * @see     #DASH_DOT_DOT
     * @see     #MEDIUM_DASH_DOT_DOT
     * @see     #SLANTED_DASH_DOT
     * @see #setBorderOptions(short)
     */

    public void setBorderBottom(short border)
    {
        field_6_border_options =
            _border_bottom.setShortValue(field_6_border_options, border);
    }

    // end border option bitfields

    /**
     * set the palette options bitmask (see the individual bitsetter methods that
     * reference this one)
     *
     *
     * @param options - the bitmask to set
     *
     */

    public void setPaletteOptions(short options)
    {
        field_7_palette_options = options;
    }

    // bitfields for palette options

    /**
     * set the palette index for the left border color
     *
     *
     * @param border - palette index
     * @see #setPaletteOptions(short)
     */

    public void setLeftBorderPaletteIdx(short border)
    {
        field_7_palette_options =
            _left_border_palette_idx.setShortValue(field_7_palette_options,
                                                   border);
    }

    /**
     * set the palette index for the right border color
     *
     *
     * @param border - palette index
     * @see #setPaletteOptions(short)
     */

    public void setRightBorderPaletteIdx(short border)
    {
        field_7_palette_options =
            _right_border_palette_idx.setShortValue(field_7_palette_options,
                                                    border);
    }

    // i've no idea.. possible values are 1 for down, 2 for up and 3 for both...0 for none..
    // maybe a diagnal line?

    /**
     * Not sure what this is for (maybe fill lines?) 1 = down, 2 = up, 3 = both, 0 for none..
     *
     *
     * @param diag - set whatever it is that this is.
     * @see #setPaletteOptions(short)
     */

    public void setDiag(short diag)
    {
        field_7_palette_options = _diag.setShortValue(field_7_palette_options,
                diag);
    }

    // end of palette options

    /**
     * set the additional palette options bitmask (see individual bitsetter methods
     * that reference this method)
     *
     *
     * @param options - bitmask to set
     *
     */

    public void setAdtlPaletteOptions(short options)
    {
        field_8_adtl_palette_options = options;
    }

    // bitfields for additional palette options

    /**
     * set the palette index for the top border
     *
     *
     * @param border - palette index
     * @see #setAdtlPaletteOptions(short)
     */

    public void setTopBorderPaletteIdx(short border)
    {
        field_8_adtl_palette_options =
            _top_border_palette_idx.setValue(field_8_adtl_palette_options,
                                             border);
    }

    /**
     * set the palette index for the bottom border
     *
     *
     * @param border - palette index
     * @see #setAdtlPaletteOptions(short)
     */

    public void setBottomBorderPaletteIdx(short border)
    {
        field_8_adtl_palette_options =
            _bottom_border_palette_idx.setValue(field_8_adtl_palette_options,
                                                border);
    }

    /**
     * set for diagonal borders?  No idea (its a palette color for the other function
     * we didn't know what was?)
     *
     *
     * @param diag - the palette index?
     * @see #setAdtlPaletteOptions(short)
     */

    public void setAdtlDiag(short diag)
    {
        field_8_adtl_palette_options =
            _adtl_diag.setValue(field_8_adtl_palette_options, diag);
    }

    /**
     * set the diagonal border line style?  Who the heck ever heard of a diagonal border?
     *
     *
     * @param diag - the line style
     * @see     #NONE
     * @see     #THIN
     * @see     #MEDIUM
     * @see     #DASHED
     * @see     #DOTTED
     * @see     #THICK
     * @see     #DOUBLE
     * @see     #HAIR
     * @see     #MEDIUM_DASHED
     * @see     #DASH_DOT
     * @see     #MEDIUM_DASH_DOT
     * @see     #DASH_DOT_DOT
     * @see     #MEDIUM_DASH_DOT_DOT
     * @see     #SLANTED_DASH_DOT
     * @see #setAdtlPaletteOptions(short)
     */

    public void setAdtlDiagLineStyle(short diag)
    {
        field_8_adtl_palette_options =
            _adtl_diag_line_style.setValue(field_8_adtl_palette_options,
                                           diag);
    }

    /**
     * set the fill pattern
     *
     * @see #NO_FILL
     * @see #SOLID_FILL
     * @see #FINE_DOTS
     * @see #ALT_BARS
     * @see #SPARSE_DOTS
     * @see #THICK_HORZ_BANDS
     * @see #THICK_VERT_BANDS
     * @see #THICK_BACKWARD_DIAG
     * @see #THICK_FORWARD_DIAG
     * @see #BIG_SPOTS
     * @see #BRICKS
     * @see #THIN_HORZ_BANDS
     * @see #THIN_VERT_BANDS
     * @see #THIN_BACKWARD_DIAG
     * @see #THIN_FORWARD_DIAG
     * @see #SQUARES
     * @see #DIAMONDS
     *
     * @param fill - fill pattern??
     * @see #setAdtlPaletteOptions(short)
     */

    public void setAdtlFillPattern(short fill)
    {
        field_8_adtl_palette_options =
            _adtl_fill_pattern.setValue(field_8_adtl_palette_options, fill);
    }

    /**
     * set the fill palette options bitmask (see bitfields for additional palette options)
     *
     * @param options the palette options
     */

    public void setFillPaletteOptions(short options)
    {
        field_9_fill_palette_options = options;
    }

    /**
     * set the foreground palette color index
     *
     *
     * @param color - palette index
     * @see #setFillPaletteOptions(short)
     */

    public void setFillForeground(short color)
    {
        field_9_fill_palette_options =
            _fill_foreground.setShortValue(field_9_fill_palette_options,
                                           color);
    }

    /**
     * set the background palette color index
     *
     *
     * @param color - palette index
     * @see #setFillPaletteOptions(short)
     */

    public void setFillBackground(short color)
    {
        field_9_fill_palette_options =
            _fill_background.setShortValue(field_9_fill_palette_options,
                                           color);
    }

    /**
     * get the index to the FONT record (which font to use 0 based)
     *
     *
     * @return index to the font
     * @see org.apache.poi.hssf.record.FontRecord
     */

    public short getFontIndex()
    {
        return field_1_font_index;
    }

    /**
     *  get the index to the Format record (which FORMAT to use 0-based)
     *
     *
     * @return index to the format record
     * @see org.apache.poi.hssf.record.FormatRecord
     */

    public short getFormatIndex()
    {
        return field_2_format_index;
    }

    /**
     * gets the options bitmask - you can also use corresponding option bit getters
     * (see other methods that reference this one)
     *
     *
     * @return options bitmask
     *
     */

    public short getCellOptions()
    {
        return field_3_cell_options;
    }

    // These are the bit fields in cell options

    /**
     * get whether the cell is locked or not
     *
     *
     * @return locked - if the cell is locked
     * @see #getCellOptions()
     */

    public boolean isLocked()
    {
        return _locked.isSet(field_3_cell_options);
    }

    /**
     * get whether the cell is hidden or not
     *
     *
     * @return hidden - if the cell is hidden
     * @see #getCellOptions()
     */

    public boolean isHidden()
    {
        return _hidden.isSet(field_3_cell_options);
    }

    /**
     * get whether the cell is a cell or style XFRecord
     *
     *
     * @return type - cell or style (0/1)
     * @see #XF_STYLE
     * @see #XF_CELL
     * @see #getCellOptions()
     */

    public short getXFType()
    {
        return _xf_type.getShortValue(field_3_cell_options);
    }

    /**
     * get some old holdover from lotus 123.  Who cares, its all over for Lotus.
     * RIP Lotus.
     *
     * @return prefix - the lotus thing
     * @see #getCellOptions()
     */

    public boolean get123Prefix()
    {
        return _123_prefix.isSet(field_3_cell_options);
    }

    /**
     * for cell XF types this is the parent style (usually 0/normal).  For
     * style this should be NULL.
     *
     * @return index of parent XF
     * @see #NULL
     * @see #getCellOptions()
     */

    public short getParentIndex()
    {
        return _parent_index.getShortValue(field_3_cell_options);
    }

    // end bitfields in cell options

    /**
     * get the alignment options bitmask.  See corresponding bitgetter methods
     * that reference this one.
     *
     *
     * @return options     - the bitmask
     */

    public short getAlignmentOptions()
    {
        return field_4_alignment_options;
    }

    // bitfields in alignment options

    /**
     * get the horizontal alignment of the cell.
     *
     *
     * @return align - how to align the cell (see constants)
     * @see #GENERAL
     * @see #LEFT
     * @see #CENTER
     * @see #RIGHT
     * @see #FILL
     * @see #JUSTIFY
     * @see #CENTER_SELECTION
     * @see #getAlignmentOptions()
     */

    public short getAlignment()
    {
        return _alignment.getShortValue(field_4_alignment_options);
    }

    /**
     * get whether to wrap the text in the cell
     *
     *
     * @return wrapped - whether or not to wrap the cell text
     * @see #getAlignmentOptions()
     */

    public boolean getWrapText()
    {
        return _wrap_text.isSet(field_4_alignment_options);
    }

    /**
     * get the vertical alignment of text in the cell
     *
     *
     * @return where to align the text
     * @see #VERTICAL_TOP
     * @see #VERTICAL_CENTER
     * @see #VERTICAL_BOTTOM
     * @see #VERTICAL_JUSTIFY
     *
     * @see #getAlignmentOptions()
     */

    public short getVerticalAlignment()
    {
        return _vertical_alignment.getShortValue(field_4_alignment_options);
    }

    /**
     * Dunno.  Docs just say this is for far east versions..  (I'm guessing it
     * justifies for right-to-left read languages)
     *
     *
     * @return justify
     * @see #getAlignmentOptions()
     */

    public short getJustifyLast()
    {   // for far east languages supported only for format always 0 for US
        return _justify_last.getShortValue(field_4_alignment_options);
    }

    /**
     * get the degree of rotation.
     *
     *
     * @return rotation - the degree of rotation
     * @see #getAlignmentOptions()
     */

    public short getRotation()
    {
        return _rotation.getShortValue(field_4_alignment_options);
    }

    // end alignment options bitfields

    /**
     * get the indent options bitmask  (see corresponding bit getters that reference
     * this field)
     *
     *
     * @return options bitmask
     *
     */

    public short getIndentionOptions()
    {
        return field_5_indention_options;
    }

    // bitfields for indention options

    /**
     * get indention (not sure of the units, think its spaces)
     *
     * @return indent - how far to indent the cell
     * @see #getIndentionOptions()
     */

    public short getIndent()
    {
        return _indent.getShortValue(field_5_indention_options);
    }

    /**
     * get whether to shrink the text to fit
     *
     *
     * @return shrink - shrink to fit or not
     * @see #getIndentionOptions()
     */

    public boolean getShrinkToFit()
    {
        return _shrink_to_fit.isSet(field_5_indention_options);
    }

    /**
     * get whether to merge cells
     *
     *
     * @return merge - merge cells or not
     * @see #getIndentionOptions()
     */

    public boolean getMergeCells()
    {
        return _merge_cells.isSet(field_5_indention_options);
    }

    /**
     * get the reading order for far east versions (0 - Context, 1 - Left to right,
     * 2 - right to left) - We could use some help with support for the far east.
     *
     * @return order - the reading order (0,1,2)
     * @see #getIndentionOptions()
     */

    public short getReadingOrder()
    {   // only for far east  always 0 in US
        return _reading_order.getShortValue(field_5_indention_options);
    }

    /**
     * get whether or not to use the format in this XF instead of the parent XF.
     *
     *
     * @return parent - true if this XF has a different format value than its parent,
     *                 false otherwise.
     * @see #getIndentionOptions()
     */

    public boolean isIndentNotParentFormat()
    {
        return _indent_not_parent_format.isSet(field_5_indention_options);
    }

    /**
     * get whether or not to use the font in this XF instead of the parent XF.
     *
     *
     * @return font   - true if this XF has a different font value than its parent,
     *                 false otherwise.
     * @see #getIndentionOptions()
     */

    public boolean isIndentNotParentFont()
    {
        return _indent_not_parent_font.isSet(field_5_indention_options);
    }

    /**
     * get whether or not to use the alignment in this XF instead of the parent XF.
     *
     *
     * @return alignment true if this XF has a different alignment value than its parent,
     *                  false otherwise.
     * @see #getIndentionOptions()
     */

    public boolean isIndentNotParentAlignment()
    {
        return _indent_not_parent_alignment.isSet(field_5_indention_options);
    }

    /**
     * get whether or not to use the border in this XF instead of the parent XF.
     *
     *
     * @return border - true if this XF has a different border value than its parent,
     *                 false otherwise.
     * @see #getIndentionOptions()
     */

    public boolean isIndentNotParentBorder()
    {
        return _indent_not_parent_border.isSet(field_5_indention_options);
    }

    /**
     * get whether or not to use the pattern in this XF instead of the parent XF.
     * (foregrount/background)
     *
     * @return pattern- true if this XF has a different pattern value than its parent,
     *                 false otherwise.
     * @see #getIndentionOptions()
     */

    public boolean isIndentNotParentPattern()
    {
        return _indent_not_parent_pattern.isSet(field_5_indention_options);
    }

    /**
     * get whether or not to use the locking/hidden in this XF instead of the parent XF.
     *
     *
     * @return options- true if this XF has a different locking or hidden value than its parent,
     *                 false otherwise.
     * @see #getIndentionOptions()
     */

    public boolean isIndentNotParentCellOptions()
    {
        return _indent_not_parent_cell_options
            .isSet(field_5_indention_options);
    }

    // end of bitfields for indention options
    // border options

    /**
     * get the border options bitmask (see the corresponding bit getter methods
     * that reference back to this one)
     *
     * @return options - the bit mask to set
     *
     */

    public short getBorderOptions()
    {
        return field_6_border_options;
    }

    // bitfields for border options

    /**
     * get the borderline style for the left border
     *
     *
     * @return border - type of border for the left side of the cell
     * @see     #NONE
     * @see     #THIN
     * @see     #MEDIUM
     * @see     #DASHED
     * @see     #DOTTED
     * @see     #THICK
     * @see     #DOUBLE
     * @see     #HAIR
     * @see     #MEDIUM_DASHED
     * @see     #DASH_DOT
     * @see     #MEDIUM_DASH_DOT
     * @see     #DASH_DOT_DOT
     * @see     #MEDIUM_DASH_DOT_DOT
     * @see     #SLANTED_DASH_DOT
     * @see #getBorderOptions()
     */

    public short getBorderLeft()
    {
        return _border_left.getShortValue(field_6_border_options);
    }

    /**
     * get the borderline style for the right border
     *
     *
     * @return  border - type of border for the right side of the cell
     * @see     #NONE
     * @see     #THIN
     * @see     #MEDIUM
     * @see     #DASHED
     * @see     #DOTTED
     * @see     #THICK
     * @see     #DOUBLE
     * @see     #HAIR
     * @see     #MEDIUM_DASHED
     * @see     #DASH_DOT
     * @see     #MEDIUM_DASH_DOT
     * @see     #DASH_DOT_DOT
     * @see     #MEDIUM_DASH_DOT_DOT
     * @see     #SLANTED_DASH_DOT
     * @see #getBorderOptions()
     */

    public short getBorderRight()
    {
        return _border_right.getShortValue(field_6_border_options);
    }

    /**
     * get the borderline style for the top border
     *
     *
     * @return border - type of border for the top of the cell
     * @see     #NONE
     * @see     #THIN
     * @see     #MEDIUM
     * @see     #DASHED
     * @see     #DOTTED
     * @see     #THICK
     * @see     #DOUBLE
     * @see     #HAIR
     * @see     #MEDIUM_DASHED
     * @see     #DASH_DOT
     * @see     #MEDIUM_DASH_DOT
     * @see     #DASH_DOT_DOT
     * @see     #MEDIUM_DASH_DOT_DOT
     * @see     #SLANTED_DASH_DOT
     * @see #getBorderOptions()
     */

    public short getBorderTop()
    {
        return _border_top.getShortValue(field_6_border_options);
    }

    /**
     * get the borderline style for the bottom border
     *
     *
     * @return border - type of border for the bottom of the cell
     * @see     #NONE
     * @see     #THIN
     * @see     #MEDIUM
     * @see     #DASHED
     * @see     #DOTTED
     * @see     #THICK
     * @see     #DOUBLE
     * @see     #HAIR
     * @see     #MEDIUM_DASHED
     * @see     #DASH_DOT
     * @see     #MEDIUM_DASH_DOT
     * @see     #DASH_DOT_DOT
     * @see     #MEDIUM_DASH_DOT_DOT
     * @see     #SLANTED_DASH_DOT
     * @see #getBorderOptions()
     */

    public short getBorderBottom()
    {
        return _border_bottom.getShortValue(field_6_border_options);
    }

    // record types -- palette options

    /**
     * get the palette options bitmask (see the individual bit getter methods that
     * reference this one)
     *
     *
     * @return options - the bitmask
     *
     */

    public short getPaletteOptions()
    {
        return field_7_palette_options;
    }

    // bitfields for palette options

    /**
     * get the palette index for the left border color
     *
     *
     * @return border - palette index
     * @see #getPaletteOptions()
     */

    public short getLeftBorderPaletteIdx()
    {
        return _left_border_palette_idx
            .getShortValue(field_7_palette_options);
    }

    /**
     * get the palette index for the right border color
     *
     *
     * @return border - palette index
     * @see #getPaletteOptions()
     */

    public short getRightBorderPaletteIdx()
    {
        return _right_border_palette_idx
            .getShortValue(field_7_palette_options);
    }

    // i've no idea.. possible values are 1 for down, 2 for up and 3 for both...0 for none..
    // maybe a diagnal line?

    /**
     * Not sure what this is for (maybe fill lines?) 1 = down, 2 = up, 3 = both, 0 for none..
     *
     *
     * @return diag - whatever it is that this is.
     * @see #getPaletteOptions()
     */

    public short getDiag()
    {
        return _diag.getShortValue(field_7_palette_options);
    }

    // end of style palette options
    // additional palette options

    /**
     * get the additional palette options bitmask (see individual bit getter methods
     * that reference this method)
     *
     *
     * @return options - bitmask to set
     *
     */

    public int getAdtlPaletteOptions()
    {
        return field_8_adtl_palette_options;
    }

    // bitfields for additional palette options

    /**
     * get the palette index for the top border
     *
     *
     * @return border - palette index
     * @see #getAdtlPaletteOptions()
     */

    public short getTopBorderPaletteIdx()
    {
        return ( short ) _top_border_palette_idx
            .getValue(field_8_adtl_palette_options);
    }

    /**
     * get the palette index for the bottom border
     *
     *
     * @return border - palette index
     * @see #getAdtlPaletteOptions()
     */

    public short getBottomBorderPaletteIdx()
    {
        return ( short ) _bottom_border_palette_idx
            .getValue(field_8_adtl_palette_options);
    }

    /**
     * get for diagonal borders?  No idea (its a palette color for the other function
     * we didn't know what was?)
     *
     *
     * @return diag - the palette index?
     * @see #getAdtlPaletteOptions()
     */

    public short getAdtlDiag()
    {
        return ( short ) _adtl_diag.getValue(field_8_adtl_palette_options);
    }

    /**
     * get the diagonal border line style?  Who the heck ever heard of a diagonal border?
     *
     *
     * @return diag - the line style
     * @see     #NONE
     * @see     #THIN
     * @see     #MEDIUM
     * @see     #DASHED
     * @see     #DOTTED
     * @see     #THICK
     * @see     #DOUBLE
     * @see     #HAIR
     * @see     #MEDIUM_DASHED
     * @see     #DASH_DOT
     * @see     #MEDIUM_DASH_DOT
     * @see     #DASH_DOT_DOT
     * @see     #MEDIUM_DASH_DOT_DOT
     * @see     #SLANTED_DASH_DOT
     * @see #getAdtlPaletteOptions()
     */

    public short getAdtlDiagLineStyle()
    {
        return ( short ) _adtl_diag_line_style
            .getValue(field_8_adtl_palette_options);
    }

    /**
     * get the additional fill pattern
     *
     * @see #NO_FILL
     * @see #SOLID_FILL
     * @see #FINE_DOTS
     * @see #ALT_BARS
     * @see #SPARSE_DOTS
     * @see #THICK_HORZ_BANDS
     * @see #THICK_VERT_BANDS
     * @see #THICK_BACKWARD_DIAG
     * @see #THICK_FORWARD_DIAG
     * @see #BIG_SPOTS
     * @see #BRICKS
     * @see #THIN_HORZ_BANDS
     * @see #THIN_VERT_BANDS
     * @see #THIN_BACKWARD_DIAG
     * @see #THIN_FORWARD_DIAG
     * @see #SQUARES
     * @see #DIAMONDS
     *
     * @return fill - fill pattern??
     * @see #getAdtlPaletteOptions()
     */

    public short getAdtlFillPattern()
    {
        return ( short ) _adtl_fill_pattern
            .getValue(field_8_adtl_palette_options);
    }

    // end bitfields for additional palette options
    // fill palette options

    /**
     * get the fill palette options bitmask (see indivdual bit getters that
     * reference this method)
     *
     * @return options
     *
     */

    public short getFillPaletteOptions()
    {
        return field_9_fill_palette_options;
    }

    // bitfields for fill palette options

    /**
     * get the foreground palette color index
     *
     *
     * @return color - palette index
     * @see #getFillPaletteOptions()
     */

    public short getFillForeground()
    {
        return _fill_foreground.getShortValue(field_9_fill_palette_options);
    }

    /**
     * get the background palette color index
     *
     * @return color palette index
     * @see #getFillPaletteOptions()
     */

    public short getFillBackground()
    {
        return _fill_background.getShortValue(field_9_fill_palette_options);
    }

    @Override
    public void serialize(LittleEndianOutput out) {
        out.writeShort(getFontIndex());
        out.writeShort(getFormatIndex());
        out.writeShort(getCellOptions());
        out.writeShort(getAlignmentOptions());
        out.writeShort(getIndentionOptions());
        out.writeShort(getBorderOptions());
        out.writeShort(getPaletteOptions());
        out.writeInt(getAdtlPaletteOptions());
        out.writeShort(getFillPaletteOptions());
    }

    @Override
    protected int getDataSize() {
        return 20;
    }

    @Override
    public short getSid()
    {
        return sid;
    }

    /**
     * Clones all the style information from another
     *  ExtendedFormatRecord, onto this one. This
     *  will then hold all the same style options.
     *
     * If The source ExtendedFormatRecord comes from
     *  a different Workbook, you will need to sort
     *  out the font and format indices yourself!
     *
     * @param source the ExtendedFormatRecord to copy from
     */
    public void cloneStyleFrom(ExtendedFormatRecord source) {
        field_1_font_index           = source.field_1_font_index;
        field_2_format_index         = source.field_2_format_index;
        field_3_cell_options         = source.field_3_cell_options;
        field_4_alignment_options    = source.field_4_alignment_options;
        field_5_indention_options    = source.field_5_indention_options;
        field_6_border_options       = source.field_6_border_options;
        field_7_palette_options      = source.field_7_palette_options;
        field_8_adtl_palette_options = source.field_8_adtl_palette_options;
        field_9_fill_palette_options = source.field_9_fill_palette_options;
    }

	@Override
    public int hashCode() {
        return Objects.hash(
            field_1_font_index
            , field_2_format_index
            , field_3_cell_options
            , field_4_alignment_options
            , field_5_indention_options
            , field_6_border_options
            , field_7_palette_options
            , field_8_adtl_palette_options
            , field_9_fill_palette_options
        );
	}

	/**
	 * Will consider two different records with the same
	 *  contents as equals, as the various indexes
	 *  that matter are embedded in the records
	 */
	@Override
    public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (obj instanceof ExtendedFormatRecord) {
			final ExtendedFormatRecord other = (ExtendedFormatRecord) obj;
			return Arrays.equals(stateSummary(), other.stateSummary());
		}
		return false;
	}

	public int[] stateSummary() {
		return new int[] { field_1_font_index, field_2_format_index, field_3_cell_options, field_4_alignment_options,
				field_5_indention_options, field_6_border_options, field_7_palette_options, field_8_adtl_palette_options, field_9_fill_palette_options };
	}


    @Override
    public ExtendedFormatRecord copy() {
        return new ExtendedFormatRecord(this);
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.EXTENDED_FORMAT;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        final Map<String,Supplier<?>> m = new LinkedHashMap<>();
        m.put("xfType", getEnumBitsAsString(this::getXFType, new int[]{0,1}, new String[]{"CELL", "STYLE"}));
        m.put("fontIndex", this::getFontIndex);
        m.put("formatIndex", this::getFormatIndex);
        m.put("cellOptions", getBitsAsString(this::getCellOptions,
            new BitField[]{_locked,_hidden,_123_prefix},
            new String[]{"LOCKED","HIDDEN","LOTUS_123_PREFIX"}));
        m.put("parentIndex", this::getParentIndex);
        m.put("alignmentOptions", getBitsAsString(this::getAlignmentOptions,
            new BitField[]{_wrap_text, _justify_last},
            new String[]{"WRAP_TEXT", "JUSTIFY_LAST"}));
        m.put("alignment", this::getAlignment);
        m.put("verticalAlignment", this::getVerticalAlignment);
        m.put("rotation", this::getRotation);
        m.put("indentionOptions", getBitsAsString(this::getIndentionOptions,
            new BitField[]{_shrink_to_fit,_merge_cells,_indent_not_parent_format,_indent_not_parent_font,
                    _indent_not_parent_alignment,_indent_not_parent_border,_indent_not_parent_pattern,_indent_not_parent_cell_options},
            new String[]{"SHRINK_TO_FIT","MERGE_CELLS","NOT_PARENT_FORMAT","NOT_PARENT_FONT",
                    "NOT_PARENT_ALIGNMENT","NOT_PARENT_BORDER","NOT_PARENT_PATTERN","NOT_PARENT_CELL_OPTIONS"}));
        m.put("indent", this::getIndent);
        m.put("readingOrder", this::getReadingOrder);
        m.put("borderOptions", this::getBorderOptions);
        m.put("borderLeft", this::getBorderLeft);
        m.put("borderRight", this::getBorderRight);
        m.put("borderTop", this::getBorderTop);
        m.put("borderBottom", this::getBorderBottom);
        m.put("paletteOptions", this::getPaletteOptions);
        m.put("leftBorderPaletteIdx", this::getLeftBorderPaletteIdx);
        m.put("rightBorderPaletteIdx", this::getRightBorderPaletteIdx);
        m.put("diag", this::getDiag);
        m.put("adtlPaletteOptions", this::getAdtlPaletteOptions);
        m.put("topBorderPaletteIdx", this::getTopBorderPaletteIdx);
        m.put("bottomBorderPaletteIdx", this::getBottomBorderPaletteIdx);
        m.put("adtlDiag", this::getAdtlDiag);
        m.put("adtlDiagLineStyle", this::getAdtlDiagLineStyle);
        m.put("adtlFillPattern", this::getAdtlFillPattern);
        m.put("fillPaletteOptions", this::getFillPaletteOptions);
        m.put("fillForeground", this::getFillForeground);
        m.put("fillBackground", this::getFillBackground);

        return Collections.unmodifiableMap(m);
    }
}
