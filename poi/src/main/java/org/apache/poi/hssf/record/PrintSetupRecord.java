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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Stores print setup options -- bogus for HSSF (and marked as such)
 *
 * @since 2.0-pre
 */
public final class PrintSetupRecord extends StandardRecord {
    public static final short     sid = 0x00A1;
    // print over then down
    private static final  BitField lefttoright   = BitFieldFactory.getInstance(0x01);
    // landscape mode
    private static final  BitField landscape     = BitFieldFactory.getInstance(0x02);
    // if papersize, scale, resolution, copies, landscape
    private static final  BitField validsettings = BitFieldFactory.getInstance(0x04);
    // print mono/b&w, colorless
    private static final  BitField nocolor       = BitFieldFactory.getInstance(0x08);
    // print draft quality
    private static final  BitField draft         = BitFieldFactory.getInstance(0x10);
    // print the notes
    private static final  BitField notes         = BitFieldFactory.getInstance(0x20);
    // the orientation is not set
    private static final  BitField noOrientation = BitFieldFactory.getInstance(0x40);
    // use a user set page no, instead of auto
    private static final  BitField usepage       = BitFieldFactory.getInstance(0x80);


    /** Constants for this are held in {@link PrintSetup} */
    private short                 field_1_paper_size;
    private short                 field_2_scale;
    private short                 field_3_page_start;
    private short                 field_4_fit_width;
    private short                 field_5_fit_height;
    private short                 field_6_options;
    private short                 field_7_hresolution;
    private short                 field_8_vresolution;
    private double                field_9_headermargin;
    private double                field_10_footermargin;
    private short                 field_11_copies;

    public PrintSetupRecord() {}

    public PrintSetupRecord(PrintSetupRecord other) {
        super(other);
        field_1_paper_size    = other.field_1_paper_size;
        field_2_scale         = other.field_2_scale;
        field_3_page_start    = other.field_3_page_start;
        field_4_fit_width     = other.field_4_fit_width;
        field_5_fit_height    = other.field_5_fit_height;
        field_6_options       = other.field_6_options;
        field_7_hresolution   = other.field_7_hresolution;
        field_8_vresolution   = other.field_8_vresolution;
        field_9_headermargin  = other.field_9_headermargin;
        field_10_footermargin = other.field_10_footermargin;
        field_11_copies       = other.field_11_copies;
    }

    public PrintSetupRecord(RecordInputStream in) {
        field_1_paper_size    = in.readShort();
        field_2_scale         = in.readShort();
        field_3_page_start    = in.readShort();
        field_4_fit_width     = in.readShort();
        field_5_fit_height    = in.readShort();
        field_6_options       = in.readShort();
        field_7_hresolution   = in.readShort();
        field_8_vresolution   = in.readShort();
        field_9_headermargin  = in.readDouble();
        field_10_footermargin = in.readDouble();
        field_11_copies       = in.readShort();
    }

    public void setPaperSize(short size)
    {
        field_1_paper_size = size;
    }

    public void setScale(short scale)
    {
        field_2_scale = scale;
    }

    public void setPageStart(short start)
    {
        field_3_page_start = start;
    }

    public void setFitWidth(short width)
    {
        field_4_fit_width = width;
    }

    public void setFitHeight(short height)
    {
        field_5_fit_height = height;
    }

    public void setOptions(short options)
    {
        field_6_options = options;
    }

    // option bitfields
    public void setLeftToRight(boolean ltor)
    {
        field_6_options = lefttoright.setShortBoolean(field_6_options, ltor);
    }

    public void setLandscape(boolean ls)
    {
        field_6_options = landscape.setShortBoolean(field_6_options, ls);
    }

    public void setValidSettings(boolean valid)
    {
        field_6_options = validsettings.setShortBoolean(field_6_options, valid);
    }

    public void setNoColor(boolean mono)
    {
        field_6_options = nocolor.setShortBoolean(field_6_options, mono);
    }

    public void setDraft(boolean d)
    {
        field_6_options = draft.setShortBoolean(field_6_options, d);
    }

    public void setNotes(boolean printnotes)
    {
        field_6_options = notes.setShortBoolean(field_6_options, printnotes);
    }

    public void setNoOrientation(boolean orientation)
    {
        field_6_options = noOrientation.setShortBoolean(field_6_options, orientation);
    }

    public void setUsePage(boolean page)
    {
        field_6_options = usepage.setShortBoolean(field_6_options, page);
    }

    // end option bitfields
    public void setHResolution(short resolution)
    {
        field_7_hresolution = resolution;
    }

    public void setVResolution(short resolution)
    {
        field_8_vresolution = resolution;
    }

    public void setHeaderMargin(double headermargin)
    {
        field_9_headermargin = headermargin;
    }

    public void setFooterMargin(double footermargin)
    {
        field_10_footermargin = footermargin;
    }

    public void setCopies(short copies)
    {
        field_11_copies = copies;
    }

    public short getPaperSize()
    {
        return field_1_paper_size;
    }

    public short getScale()
    {
        return field_2_scale;
    }

    public short getPageStart()
    {
        return field_3_page_start;
    }

    public short getFitWidth()
    {
        return field_4_fit_width;
    }

    public short getFitHeight()
    {
        return field_5_fit_height;
    }

    public short getOptions()
    {
        return field_6_options;
    }

    // option bitfields
    public boolean getLeftToRight()
    {
        return lefttoright.isSet(field_6_options);
    }

    public boolean getLandscape()
    {
        return landscape.isSet(field_6_options);
    }

    public boolean getValidSettings()
    {
        return validsettings.isSet(field_6_options);
    }

    public boolean getNoColor()
    {
        return nocolor.isSet(field_6_options);
    }

    public boolean getDraft()
    {
        return draft.isSet(field_6_options);
    }

    public boolean getNotes()
    {
        return notes.isSet(field_6_options);
    }

    public boolean getNoOrientation()
    {
        return noOrientation.isSet(field_6_options);
    }

    public boolean getUsePage()
    {
        return usepage.isSet(field_6_options);
    }

    // end option bitfields
    public short getHResolution()
    {
        return field_7_hresolution;
    }

    public short getVResolution()
    {
        return field_8_vresolution;
    }

    public double getHeaderMargin()
    {
        return field_9_headermargin;
    }

    public double getFooterMargin()
    {
        return field_10_footermargin;
    }

    public short getCopies()
    {
        return field_11_copies;
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(getPaperSize());
        out.writeShort(getScale());
        out.writeShort(getPageStart());
        out.writeShort(getFitWidth());
        out.writeShort(getFitHeight());
        out.writeShort(getOptions());
        out.writeShort(getHResolution());
        out.writeShort(getVResolution());
        out.writeDouble(getHeaderMargin());
        out.writeDouble(getFooterMargin());
        out.writeShort(getCopies());
    }

    protected int getDataSize() {
        return 34;
    }

    public short getSid()
    {
        return sid;
    }

    @Override
    public PrintSetupRecord copy() {
      return new PrintSetupRecord(this);
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.PRINT_SETUP;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        final Map<String,Supplier<?>> m = new LinkedHashMap<>();
        m.put("paperSize", this::getPaperSize);
        m.put("scale", this::getScale);
        m.put("pageStart",this::getPageStart);
        m.put("fitWidth", this::getFitWidth);
        m.put("fitHeight", this::getFitHeight);
        m.put("options", getBitsAsString(this::getOptions,
             new BitField[]{lefttoright, landscape, validsettings, nocolor, draft, notes, noOrientation, usepage},
             new String[]{"lefttoright","landscape","validsettings","nocolor","draft","notes","noOrientation","usepage"}));
        m.put("hResolution", this::getHResolution);
        m.put("vResolution", this::getVResolution);
        m.put("headerMargin", this::getHeaderMargin);
        m.put("footerMargin", this::getFooterMargin);
        m.put("copies", this::getCopies);
        return Collections.unmodifiableMap(m);
    }
}
