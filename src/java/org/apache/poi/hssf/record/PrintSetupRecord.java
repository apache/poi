
/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
        

package org.apache.poi.hssf.record;

import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.BitField;

/**
 * Title:        Print Setup Record<P>
 * Description:  Stores print setup options -- bogus for HSSF (and marked as such)<P>
 * REFERENCE:  PG 385 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 * @version 2.0-pre
 */

public class PrintSetupRecord
    extends Record
{
    public final static short     sid = 0xa1;
    private short                 field_1_paper_size;
    private short                 field_2_scale;
    private short                 field_3_page_start;
    private short                 field_4_fit_width;
    private short                 field_5_fit_height;
    private short                 field_6_options;
    static final private BitField lefttoright   =
        new BitField(0x01);   // print over then down
    static final private BitField landscape     =
        new BitField(0x02);   // landscape mode
    static final private BitField validsettings = new BitField(
        0x04);                // if papersize, scale, resolution, copies, landscape

    // weren't obtained from the print consider them
    // mere bunk
    static final private BitField nocolor       =
        new BitField(0x08);   // print mono/b&w, colorless
    static final private BitField draft         =
        new BitField(0x10);   // print draft quality
    static final private BitField notes         =
        new BitField(0x20);   // print the notes
    static final private BitField noOrientation =
        new BitField(0x40);   // the orientation is not set
    static final private BitField usepage       =
        new BitField(0x80);   // use a user set page no, instead of auto
    private short                 field_7_hresolution;
    private short                 field_8_vresolution;
    private double                field_9_headermargin;
    private double                field_10_footermargin;
    private short                 field_11_copies;

    public PrintSetupRecord()
    {
    }

    /**
     * Constructs a PrintSetup (SETUP) record and sets its fields appropriately.
     *
     * @param id     id must be 0xa1 or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public PrintSetupRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    }

    /**
     * Constructs a PrintSetup (SETUP) record and sets its fields appropriately.
     *
     * @param id     id must be 0xa1 or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public PrintSetupRecord(short id, short size, byte [] data, int offset)
    {
        super(id, size, data, offset);
    }

    protected void validateSid(short id)
    {
        if (id != sid)
        {
            throw new RecordFormatException(
                "NOT A valid PrintSetup record RECORD");
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {
        field_1_paper_size    = LittleEndian.getShort(data, 0 + offset);
        field_2_scale         = LittleEndian.getShort(data, 2 + offset);
        field_3_page_start    = LittleEndian.getShort(data, 4 + offset);
        field_4_fit_width     = LittleEndian.getShort(data, 6 + offset);
        field_5_fit_height    = LittleEndian.getShort(data, 8 + offset);
        field_6_options       = LittleEndian.getShort(data, 10 + offset);
        field_7_hresolution   = LittleEndian.getShort(data, 12 + offset);
        field_8_vresolution   = LittleEndian.getShort(data, 14 + offset);
        field_9_headermargin  = LittleEndian.getDouble(data, 16 + offset);
        field_10_footermargin = LittleEndian.getDouble(data, 24 + offset);
        field_11_copies       = LittleEndian.getShort(data, 32 + offset);
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

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[PRINTSETUP]\n");
        buffer.append("    .papersize      = ").append(getPaperSize())
            .append("\n");
        buffer.append("    .scale          = ").append(getScale())
            .append("\n");
        buffer.append("    .pagestart      = ").append(getPageStart())
            .append("\n");
        buffer.append("    .fitwidth       = ").append(getFitWidth())
            .append("\n");
        buffer.append("    .fitheight      = ").append(getFitHeight())
            .append("\n");
        buffer.append("    .options        = ").append(getOptions())
            .append("\n");
        buffer.append("        .ltor       = ").append(getLeftToRight())
            .append("\n");
        buffer.append("        .landscape  = ").append(getLandscape())
            .append("\n");
        buffer.append("        .valid      = ").append(getValidSettings())
            .append("\n");
        buffer.append("        .mono       = ").append(getNoColor())
            .append("\n");
        buffer.append("        .draft      = ").append(getDraft())
            .append("\n");
        buffer.append("        .notes      = ").append(getNotes())
            .append("\n");
        buffer.append("        .noOrientat = ").append(getNoOrientation())
            .append("\n");
        buffer.append("        .usepage    = ").append(getUsePage())
            .append("\n");
        buffer.append("    .hresolution    = ").append(getHResolution())
            .append("\n");
        buffer.append("    .vresolution    = ").append(getVResolution())
            .append("\n");
        buffer.append("    .headermargin   = ").append(getHeaderMargin())
            .append("\n");
        buffer.append("    .footermargin   = ").append(getFooterMargin())
            .append("\n");
        buffer.append("    .copies         = ").append(getCopies())
            .append("\n");
        buffer.append("[/PRINTSETUP]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte [] data)
    {
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, ( short ) 34);
        LittleEndian.putShort(data, 4 + offset, getPaperSize());
        LittleEndian.putShort(data, 6 + offset, getScale());
        LittleEndian.putShort(data, 8 + offset, getPageStart());
        LittleEndian.putShort(data, 10 + offset, getFitWidth());
        LittleEndian.putShort(data, 12 + offset, getFitHeight());
        LittleEndian.putShort(data, 14 + offset, getOptions());
        LittleEndian.putShort(data, 16 + offset, getHResolution());
        LittleEndian.putShort(data, 18 + offset, getVResolution());
        LittleEndian.putDouble(data, 20 + offset, getHeaderMargin());
        LittleEndian.putDouble(data, 28 + offset, getFooterMargin());
        LittleEndian.putShort(data, 36 + offset, getCopies());
        return getRecordSize();
    }

    public int getRecordSize()
    {
        return 38;
    }

    public short getSid()
    {
        return this.sid;
    }

    public Object clone() {
      PrintSetupRecord rec = new PrintSetupRecord();
      rec.field_1_paper_size = field_1_paper_size;
      rec.field_2_scale = field_2_scale;
      rec.field_3_page_start = field_3_page_start;
      rec.field_4_fit_width = field_4_fit_width;
      rec.field_5_fit_height = field_5_fit_height;
      rec.field_6_options = field_6_options;
      rec.field_7_hresolution = field_7_hresolution;
      rec.field_8_vresolution = field_8_vresolution;
      rec.field_9_headermargin = field_9_headermargin;
      rec.field_10_footermargin = field_10_footermargin;
      rec.field_11_copies = field_11_copies;
      return rec;
    }
}
