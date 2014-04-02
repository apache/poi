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

import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Title:        Window Two Record<P>
 * Description:  sheet window settings<P>
 * REFERENCE:  PG 422 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 * @version 2.0-pre
 */
public final class WindowTwoRecord extends StandardRecord {
    public final static short sid = 0x023E;

    // bitfields
    private static final BitField displayFormulas         = BitFieldFactory.getInstance(0x01);
    private static final BitField displayGridlines        = BitFieldFactory.getInstance(0x02);
    private static final BitField displayRowColHeadings   = BitFieldFactory.getInstance(0x04);
    private static final BitField freezePanes             = BitFieldFactory.getInstance(0x08);
    private static final BitField displayZeros            = BitFieldFactory.getInstance(0x10);
    /**  if false use color in field 4 if true use default foreground for headers */
    private static final BitField defaultHeader           = BitFieldFactory.getInstance(0x20);   
    private static final BitField arabic                  = BitFieldFactory.getInstance(0x040);
    private static final BitField displayGuts             = BitFieldFactory.getInstance(0x080);
    private static final BitField freezePanesNoSplit      = BitFieldFactory.getInstance(0x100);
    private static final BitField selected                = BitFieldFactory.getInstance(0x200);
    private static final BitField active                  = BitFieldFactory.getInstance(0x400);
    private static final BitField savedInPageBreakPreview = BitFieldFactory.getInstance(0x800);
    // 4-7 reserved
    // end bitfields

    private short             field_1_options;
    private short             field_2_top_row;
    private short             field_3_left_col;
    private int               field_4_header_color;
    private short             field_5_page_break_zoom;
    private short             field_6_normal_zoom;
    private int               field_7_reserved;

    public WindowTwoRecord()
    {
    }

    public WindowTwoRecord(RecordInputStream in)
    {
      int size = in.remaining();
        field_1_options      = in.readShort();
        field_2_top_row      = in.readShort();
        field_3_left_col     = in.readShort();
        field_4_header_color = in.readInt();
        if (size > 10)
        {
            field_5_page_break_zoom = in.readShort();
            field_6_normal_zoom     = in.readShort();
        }
        if (size > 14)
        {   // there is a special case of this record that has only 14 bytes...undocumented!
            field_7_reserved = in.readInt();
        }
    }

    /**
     * set the options bitmask or just use the bit setters.
     * @param options
     */

    public void setOptions(short options)
    {
        field_1_options = options;
    }

    // option bitfields

    /**
     * set whether the window should display formulas
     * @param formulas or not
     */

    public void setDisplayFormulas(boolean formulas)
    {
        field_1_options = displayFormulas.setShortBoolean(field_1_options, formulas);
    }

    /**
     * set whether the window should display gridlines
     * @param gridlines or not
     */

    public void setDisplayGridlines(boolean gridlines)
    {
        field_1_options = displayGridlines.setShortBoolean(field_1_options, gridlines);
    }

    /**
     * set whether the window should display row and column headings
     * @param headings or not
     */

    public void setDisplayRowColHeadings(boolean headings)
    {
        field_1_options = displayRowColHeadings.setShortBoolean(field_1_options, headings);
    }

    /**
     * set whether the window should freeze panes
     * @param freezepanes  freeze panes or not
     */

    public void setFreezePanes(boolean freezepanes)
    {
        field_1_options = freezePanes.setShortBoolean(field_1_options, freezepanes);
    }

    /**
     * set whether the window should display zero values
     * @param zeros or not
     */

    public void setDisplayZeros(boolean zeros)
    {
        field_1_options = displayZeros.setShortBoolean(field_1_options, zeros);
    }

    /**
     * set whether the window should display a default header
     * @param header or not
     */

    public void setDefaultHeader(boolean header)
    {
        field_1_options = defaultHeader.setShortBoolean(field_1_options, header);
    }

    /**
     * is this arabic?
     * @param isarabic  arabic or not
     */

    public void setArabic(boolean isarabic)
    {
        field_1_options = arabic.setShortBoolean(field_1_options, isarabic);
    }

    /**
     * set whether the outline symbols are displaed
     * @param guts  symbols or not
     */

    public void setDisplayGuts(boolean guts)
    {
        field_1_options = displayGuts.setShortBoolean(field_1_options, guts);
    }

    /**
     * freeze unsplit panes or not
     * @param freeze or not
     */

    public void setFreezePanesNoSplit(boolean freeze)
    {
        field_1_options = freezePanesNoSplit.setShortBoolean(field_1_options, freeze);
    }

    /**
     * sheet tab is selected
     * @param sel  selected or not
     */

    public void setSelected(boolean sel)
    {
        field_1_options = selected.setShortBoolean(field_1_options, sel);
    }

    /**
     * is the sheet currently displayed in the window
     * @param p  displayed or not
     */
    public void setActive(boolean p) {
        field_1_options = active.setShortBoolean(field_1_options, p);
    }
    /**
     * deprecated May 2008
     * @deprecated use setActive()
     */
    public void setPaged(boolean p) {
    	setActive(p);
    }
    /**
     * was the sheet saved in page break view
     * @param p  pagebreaksaved or not
     */

    public void setSavedInPageBreakPreview(boolean p)
    {
        field_1_options = savedInPageBreakPreview.setShortBoolean(field_1_options, p);
    }

    // end of bitfields.

    /**
     * set the top row visible in the window
     * @param topRow  top row visible
     */

    public void setTopRow(short topRow)
    {
        field_2_top_row = topRow;
    }

    /**
     * set the leftmost column displayed in the window
     * @param leftCol  leftmost column
     */

    public void setLeftCol(short leftCol)
    {
        field_3_left_col = leftCol;
    }

    /**
     * set the palette index for the header color
     * @param color
     */

    public void setHeaderColor(int color)
    {
        field_4_header_color = color;
    }

    /**
     * zoom magification in page break view
     * @param zoom
     */

    public void setPageBreakZoom(short zoom)
    {
        field_5_page_break_zoom = zoom;
    }

    /**
     * set the zoom magnification in normal view
     * @param zoom
     */

    public void setNormalZoom(short zoom)
    {
        field_6_normal_zoom = zoom;
    }

    /**
     * set the reserved (don't do this) value
     */

    public void setReserved(int reserved)
    {
        field_7_reserved = reserved;
    }

    /**
     * get the options bitmask or just use the bit setters.
     * @return options
     */

    public short getOptions()
    {
        return field_1_options;
    }

    // option bitfields

    /**
     * get whether the window should display formulas
     * @return formulas or not
     */

    public boolean getDisplayFormulas()
    {
        return displayFormulas.isSet(field_1_options);
    }

    /**
     * get whether the window should display gridlines
     * @return gridlines or not
     */

    public boolean getDisplayGridlines()
    {
        return displayGridlines.isSet(field_1_options);
    }

    /**
     * get whether the window should display row and column headings
     * @return headings or not
     */

    public boolean getDisplayRowColHeadings()
    {
        return displayRowColHeadings.isSet(field_1_options);
    }

    /**
     * get whether the window should freeze panes
     * @return freeze panes or not
     */

    public boolean getFreezePanes()
    {
        return freezePanes.isSet(field_1_options);
    }

    /**
     * get whether the window should display zero values
     * @return zeros or not
     */

    public boolean getDisplayZeros()
    {
        return displayZeros.isSet(field_1_options);
    }

    /**
     * get whether the window should display a default header
     * @return header or not
     */

    public boolean getDefaultHeader()
    {
        return defaultHeader.isSet(field_1_options);
    }

    /**
     * is this arabic?
     * @return arabic or not
     */

    public boolean getArabic()
    {
        return arabic.isSet(field_1_options);
    }

    /**
     * get whether the outline symbols are displaed
     * @return symbols or not
     */

    public boolean getDisplayGuts()
    {
        return displayGuts.isSet(field_1_options);
    }

    /**
     * freeze unsplit panes or not
     * @return freeze or not
     */

    public boolean getFreezePanesNoSplit()
    {
        return freezePanesNoSplit.isSet(field_1_options);
    }

    /**
     * sheet tab is selected
     * @return selected or not
     */

    public boolean getSelected()
    {
        return selected.isSet(field_1_options);
    }

    /**
     * is the sheet currently displayed in the window
     * @return displayed or not
     */

    public boolean isActive() {
        return active.isSet(field_1_options);
    }
    /**
     * deprecated May 2008
     * @deprecated use isActive()
     */
    public boolean getPaged() {
        return isActive();
    }

    /**
     * was the sheet saved in page break view
     * @return pagebreaksaved or not
     */

    public boolean getSavedInPageBreakPreview()
    {
        return savedInPageBreakPreview.isSet(field_1_options);
    }

    // end of bitfields.

    /**
     * get the top row visible in the window
     * @return toprow
     */

    public short getTopRow()
    {
        return field_2_top_row;
    }

    /**
     * get the leftmost column displayed in the window
     * @return leftmost
     */

    public short getLeftCol()
    {
        return field_3_left_col;
    }

    /**
     * get the palette index for the header color
     * @return color
     */

    public int getHeaderColor()
    {
        return field_4_header_color;
    }

    /**
     * zoom magification in page break view
     * @return zoom
     */

    public short getPageBreakZoom()
    {
        return field_5_page_break_zoom;
    }

    /**
     * get the zoom magnification in normal view
     * @return zoom
     */

    public short getNormalZoom()
    {
        return field_6_normal_zoom;
    }

    /**
     * get the reserved bits - why would you do this?
     * @return reserved stuff -probably garbage
     */

    public int getReserved()
    {
        return field_7_reserved;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[WINDOW2]\n");
        buffer.append("    .options        = ")
            .append(Integer.toHexString(getOptions())).append("\n");
        buffer.append("       .dispformulas= ").append(getDisplayFormulas())
            .append("\n");
        buffer.append("       .dispgridlins= ").append(getDisplayGridlines())
            .append("\n");
        buffer.append("       .disprcheadin= ")
            .append(getDisplayRowColHeadings()).append("\n");
        buffer.append("       .freezepanes = ").append(getFreezePanes())
            .append("\n");
        buffer.append("       .displayzeros= ").append(getDisplayZeros())
            .append("\n");
        buffer.append("       .defaultheadr= ").append(getDefaultHeader())
            .append("\n");
        buffer.append("       .arabic      = ").append(getArabic())
            .append("\n");
        buffer.append("       .displayguts = ").append(getDisplayGuts())
            .append("\n");
        buffer.append("       .frzpnsnosplt= ")
            .append(getFreezePanesNoSplit()).append("\n");
        buffer.append("       .selected    = ").append(getSelected())
            .append("\n");
        buffer.append("       .active       = ").append(isActive())
            .append("\n");
        buffer.append("       .svdinpgbrkpv= ")
            .append(getSavedInPageBreakPreview()).append("\n");
        buffer.append("    .toprow         = ")
            .append(Integer.toHexString(getTopRow())).append("\n");
        buffer.append("    .leftcol        = ")
            .append(Integer.toHexString(getLeftCol())).append("\n");
        buffer.append("    .headercolor    = ")
            .append(Integer.toHexString(getHeaderColor())).append("\n");
        buffer.append("    .pagebreakzoom  = ")
            .append(Integer.toHexString(getPageBreakZoom())).append("\n");
        buffer.append("    .normalzoom     = ")
            .append(Integer.toHexString(getNormalZoom())).append("\n");
        buffer.append("    .reserved       = ")
            .append(Integer.toHexString(getReserved())).append("\n");
        buffer.append("[/WINDOW2]\n");
        return buffer.toString();
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(getOptions());
        out.writeShort(getTopRow());
        out.writeShort(getLeftCol());
        out.writeInt(getHeaderColor());
        out.writeShort(getPageBreakZoom());
        out.writeShort(getNormalZoom());
        out.writeInt(getReserved());
    }

    protected int getDataSize() {
        return 18;
    }

    public short getSid()
    {
        return sid;
    }

    public Object clone() {
      WindowTwoRecord rec = new WindowTwoRecord();
      rec.field_1_options = field_1_options;
      rec.field_2_top_row = field_2_top_row;
      rec.field_3_left_col = field_3_left_col;
      rec.field_4_header_color = field_4_header_color;
      rec.field_5_page_break_zoom = field_5_page_break_zoom;
      rec.field_6_normal_zoom = field_6_normal_zoom;
      rec.field_7_reserved = field_7_reserved;
      return rec;
    }
}
