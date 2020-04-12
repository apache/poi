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

import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Stores the attributes of the workbook window.
 * This is basically so the gui knows how big to make the window holding the spreadsheet document.
 *
 * @version 2.0-pre
 */
public final class WindowOneRecord extends StandardRecord {
    public static final short sid = 0x3d;

    // horizontal position
    private short field_1_h_hold;
    // vertical position
    private short field_2_v_hold;
    private short field_3_width;
    private short field_4_height;
    private short field_5_options;
    // is this window is hidden
    static final private BitField hidden   = BitFieldFactory.getInstance(0x01);
    // is this window is an icon
    static final private BitField iconic   = BitFieldFactory.getInstance(0x02);
    // reserved
    @SuppressWarnings("unused")
    static final private BitField reserved = BitFieldFactory.getInstance(0x04);
    // display horizontal scrollbar
    static final private BitField hscroll  = BitFieldFactory.getInstance(0x08);
    // display vertical scrollbar
    static final private BitField vscroll  = BitFieldFactory.getInstance(0x10);
    // display tabs at the bottom
    static final private BitField tabs     = BitFieldFactory.getInstance(0x20);

    // all the rest are "reserved"
    private int   field_6_active_sheet;
    private int   field_7_first_visible_tab;
    private short field_8_num_selected_tabs;
    private short field_9_tab_width_ratio;

    public WindowOneRecord() {}

    public WindowOneRecord(WindowOneRecord other) {
        super(other);
        field_1_h_hold            = other.field_1_h_hold;
        field_2_v_hold            = other.field_2_v_hold;
        field_3_width             = other.field_3_width;
        field_4_height            = other.field_4_height;
        field_5_options           = other.field_5_options;
        field_6_active_sheet      = other.field_6_active_sheet;
        field_7_first_visible_tab = other.field_7_first_visible_tab;
        field_8_num_selected_tabs = other.field_8_num_selected_tabs;
        field_9_tab_width_ratio   = other.field_9_tab_width_ratio;
    }

    public WindowOneRecord(RecordInputStream in) {
        field_1_h_hold            = in.readShort();
        field_2_v_hold            = in.readShort();
        field_3_width             = in.readShort();
        field_4_height            = in.readShort();
        field_5_options           = in.readShort();
        field_6_active_sheet      = in.readShort();
        field_7_first_visible_tab = in.readShort();
        field_8_num_selected_tabs = in.readShort();
        field_9_tab_width_ratio   = in.readShort();
    }

    /**
     * set the horizontal position of the window (in 1/20ths of a point)
     * @param h - horizontal location
     */

    public void setHorizontalHold(short h)
    {
        field_1_h_hold = h;
    }

    /**
     * set the vertical position of the window (in 1/20ths of a point)
     * @param v - vertical location
     */

    public void setVerticalHold(short v)
    {
        field_2_v_hold = v;
    }

    /**
     * set the width of the window
     * @param w  width
     */

    public void setWidth(short w)
    {
        field_3_width = w;
    }

    /**
     * set teh height of the window
     * @param h  height
     */

    public void setHeight(short h)
    {
        field_4_height = h;
    }

    /**
     * set the options bitmask (see bit setters)
     *
     * @param o - the bitmask
     */

    public void setOptions(short o)
    {
        field_5_options = o;
    }

    // bitfields for options

    /**
     * set whether the window is hidden or not
     * @param ishidden or not
     */

    public void setHidden(boolean ishidden)
    {
        field_5_options = hidden.setShortBoolean(field_5_options, ishidden);
    }

    /**
     * set whether the window has been iconized or not
     * @param isiconic  iconize  or not
     */

    public void setIconic(boolean isiconic)
    {
        field_5_options = iconic.setShortBoolean(field_5_options, isiconic);
    }

    /**
     * set whether to display the horizontal scrollbar or not
     * @param scroll display or not
     */

    public void setDisplayHorizonalScrollbar(boolean scroll)
    {
        field_5_options = hscroll.setShortBoolean(field_5_options, scroll);
    }

    /**
     * set whether to display the vertical scrollbar or not
     * @param scroll  display or not
     */

    public void setDisplayVerticalScrollbar(boolean scroll)
    {
        field_5_options = vscroll.setShortBoolean(field_5_options, scroll);
    }

    /**
     * set whether to display the tabs or not
     * @param disptabs  display or not
     */

    public void setDisplayTabs(boolean disptabs)
    {
        field_5_options = tabs.setShortBoolean(field_5_options, disptabs);
    }

    // end bitfields

    public void setActiveSheetIndex(int index) {
    	field_6_active_sheet = index;
	}

    /**
     * Sets the first visible sheet in the worksheet tab-bar.  This method does <b>not</b>
     *  hide, select or focus sheets.  It just sets the scroll position in the tab-bar.
     * @param t the sheet index of the tab that will become the first in the tab-bar
     */
    public void setFirstVisibleTab(int t) {
        field_7_first_visible_tab = t;
    }

    /**
     * set the number of selected tabs
     * @param n  number of tabs
     */

    public void setNumSelectedTabs(short n)
    {
        field_8_num_selected_tabs = n;
    }

    /**
     * ratio of the width of the tabs to the horizontal scrollbar
     * @param r  ratio
     */

    public void setTabWidthRatio(short r)
    {
        field_9_tab_width_ratio = r;
    }

    /**
     * get the horizontal position of the window (in 1/20ths of a point)
     * @return h - horizontal location
     */

    public short getHorizontalHold()
    {
        return field_1_h_hold;
    }

    /**
     * get the vertical position of the window (in 1/20ths of a point)
     * @return v - vertical location
     */

    public short getVerticalHold()
    {
        return field_2_v_hold;
    }

    /**
     * get the width of the window
     * @return width
     */

    public short getWidth()
    {
        return field_3_width;
    }

    /**
     * get the height of the window
     * @return height
     */

    public short getHeight()
    {
        return field_4_height;
    }

    /**
     * get the options bitmask (see bit setters)
     *
     * @return o - the bitmask
     */

    public short getOptions()
    {
        return field_5_options;
    }

    // bitfields for options

    /**
     * get whether the window is hidden or not
     * @return ishidden or not
     */

    public boolean getHidden()
    {
        return hidden.isSet(field_5_options);
    }

    /**
     * get whether the window has been iconized or not
     * @return iconize  or not
     */

    public boolean getIconic()
    {
        return iconic.isSet(field_5_options);
    }

    /**
     * get whether to display the horizontal scrollbar or not
     * @return display or not
     */

    public boolean getDisplayHorizontalScrollbar()
    {
        return hscroll.isSet(field_5_options);
    }

    /**
     * get whether to display the vertical scrollbar or not
     * @return display or not
     */

    public boolean getDisplayVerticalScrollbar()
    {
        return vscroll.isSet(field_5_options);
    }

    /**
     * get whether to display the tabs or not
     * @return display or not
     */

    public boolean getDisplayTabs()
    {
        return tabs.isSet(field_5_options);
    }

    // end options bitfields


    /**
     * @return the index of the currently displayed sheet
     */
    public int getActiveSheetIndex() {
    	return field_6_active_sheet;
    }

    /**
     * @return the first visible sheet in the worksheet tab-bar.
     * I.E. the scroll position of the tab-bar.
     */
    public int getFirstVisibleTab() {
        return field_7_first_visible_tab;
    }

    /**
     * get the number of selected tabs
     * @return number of tabs
     */

    public short getNumSelectedTabs()
    {
        return field_8_num_selected_tabs;
    }

    /**
     * ratio of the width of the tabs to the horizontal scrollbar
     * @return ratio
     */

    public short getTabWidthRatio()
    {
        return field_9_tab_width_ratio;
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(getHorizontalHold());
        out.writeShort(getVerticalHold());
        out.writeShort(getWidth());
        out.writeShort(getHeight());
        out.writeShort(getOptions());
        out.writeShort(getActiveSheetIndex());
        out.writeShort(getFirstVisibleTab());
        out.writeShort(getNumSelectedTabs());
        out.writeShort(getTabWidthRatio());
    }

    protected int getDataSize() {
        return 18;
    }

    public short getSid()
    {
        return sid;
    }

    @Override
    public WindowOneRecord copy() {
        return new WindowOneRecord(this);
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.WINDOW_ONE;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "horizontalHold", this::getHorizontalHold,
            "verticalHold", this::getVerticalHold,
            "width", this::getWidth,
            "options", getBitsAsString(this::getOptions,
                new BitField[]{hidden, iconic, reserved, hscroll, vscroll, tabs},
                new String[]{"HIDDEN", "ICONIC", "RESERVED", "HSCROLL", "VSCROLL", "TABS"}),
            "activeSheetIndex", this::getActiveSheetIndex,
            "firstVisibleTab", this::getFirstVisibleTab,
            "numSelectedTabs", this::getNumSelectedTabs,
            "tabWidthRatio", this::getTabWidthRatio
        );
    }
}
