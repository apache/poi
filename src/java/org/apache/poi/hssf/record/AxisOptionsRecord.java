
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


package org.apache.poi.hssf.record;



import org.apache.poi.util.*;

/**
 * The axis options record provides unit information and other various tidbits about the axis.
 * NOTE: This source is automatically generated please do not modify this file.  Either subclass or
 *       remove the record in src/records/definitions.

 * @author Andrew C. Oliver(acoliver at apache.org)
 */
public class AxisOptionsRecord
    extends Record
{
    public final static short      sid                             = 0x1062;
    private  short      field_1_minimumCategory;
    private  short      field_2_maximumCategory;
    private  short      field_3_majorUnitValue;
    private  short      field_4_majorUnit;
    private  short      field_5_minorUnitValue;
    private  short      field_6_minorUnit;
    private  short      field_7_baseUnit;
    private  short      field_8_crossingPoint;
    private  short      field_9_options;
    private  BitField   defaultMinimum                              = new BitField(0x1);
    private  BitField   defaultMaximum                              = new BitField(0x2);
    private  BitField   defaultMajor                                = new BitField(0x4);
    private  BitField   defaultMinorUnit                            = new BitField(0x8);
    private  BitField   isDate                                      = new BitField(0x10);
    private  BitField   defaultBase                                 = new BitField(0x20);
    private  BitField   defaultCross                                = new BitField(0x40);
    private  BitField   defaultDateSettings                         = new BitField(0x80);


    public AxisOptionsRecord()
    {

    }

    /**
     * Constructs a AxisOptions record and sets its fields appropriately.
     *
     * @param id    id must be 0x1062 or an exception
     *              will be throw upon validation
     * @param size  size the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public AxisOptionsRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    }

    /**
     * Constructs a AxisOptions record and sets its fields appropriately.
     *
     * @param id    id must be 0x1062 or an exception
     *              will be throw upon validation
     * @param size  size the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the record's data
     */

    public AxisOptionsRecord(short id, short size, byte [] data, int offset)
    {
        super(id, size, data, offset);
    }

    /**
     * Checks the sid matches the expected side for this record
     *
     * @param id   the expected sid.
     */
    protected void validateSid(short id)
    {
        if (id != sid)
        {
            throw new RecordFormatException("Not a AxisOptions record");
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {
        field_1_minimumCategory         = LittleEndian.getShort(data, 0x0 + offset);
        field_2_maximumCategory         = LittleEndian.getShort(data, 0x2 + offset);
        field_3_majorUnitValue          = LittleEndian.getShort(data, 0x4 + offset);
        field_4_majorUnit               = LittleEndian.getShort(data, 0x6 + offset);
        field_5_minorUnitValue          = LittleEndian.getShort(data, 0x8 + offset);
        field_6_minorUnit               = LittleEndian.getShort(data, 0xa + offset);
        field_7_baseUnit                = LittleEndian.getShort(data, 0xc + offset);
        field_8_crossingPoint           = LittleEndian.getShort(data, 0xe + offset);
        field_9_options                 = LittleEndian.getShort(data, 0x10 + offset);

    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[AxisOptions]\n");

        buffer.append("    .minimumCategory      = ")
            .append("0x")
            .append(HexDump.toHex((short)getMinimumCategory()))
            .append(" (").append(getMinimumCategory()).append(" )\n");

        buffer.append("    .maximumCategory      = ")
            .append("0x")
            .append(HexDump.toHex((short)getMaximumCategory()))
            .append(" (").append(getMaximumCategory()).append(" )\n");

        buffer.append("    .majorUnitValue       = ")
            .append("0x")
            .append(HexDump.toHex((short)getMajorUnitValue()))
            .append(" (").append(getMajorUnitValue()).append(" )\n");

        buffer.append("    .majorUnit            = ")
            .append("0x")
            .append(HexDump.toHex((short)getMajorUnit()))
            .append(" (").append(getMajorUnit()).append(" )\n");

        buffer.append("    .minorUnitValue       = ")
            .append("0x")
            .append(HexDump.toHex((short)getMinorUnitValue()))
            .append(" (").append(getMinorUnitValue()).append(" )\n");

        buffer.append("    .minorUnit            = ")
            .append("0x")
            .append(HexDump.toHex((short)getMinorUnit()))
            .append(" (").append(getMinorUnit()).append(" )\n");

        buffer.append("    .baseUnit             = ")
            .append("0x")
            .append(HexDump.toHex((short)getBaseUnit()))
            .append(" (").append(getBaseUnit()).append(" )\n");

        buffer.append("    .crossingPoint        = ")
            .append("0x")
            .append(HexDump.toHex((short)getCrossingPoint()))
            .append(" (").append(getCrossingPoint()).append(" )\n");

        buffer.append("    .options              = ")
            .append("0x")
            .append(HexDump.toHex((short)getOptions()))
            .append(" (").append(getOptions()).append(" )\n");
        buffer.append("         .defaultMinimum           = ").append(isDefaultMinimum      ()).append('\n');
        buffer.append("         .defaultMaximum           = ").append(isDefaultMaximum      ()).append('\n');
        buffer.append("         .defaultMajor             = ").append(isDefaultMajor        ()).append('\n');
        buffer.append("         .defaultMinorUnit         = ").append(isDefaultMinorUnit    ()).append('\n');
        buffer.append("         .isDate                   = ").append(isIsDate              ()).append('\n');
        buffer.append("         .defaultBase              = ").append(isDefaultBase         ()).append('\n');
        buffer.append("         .defaultCross             = ").append(isDefaultCross        ()).append('\n');
        buffer.append("         .defaultDateSettings      = ").append(isDefaultDateSettings ()).append('\n');

        buffer.append("[/AxisOptions]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte[] data)
    {
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, (short)(getRecordSize() - 4));

        LittleEndian.putShort(data, 4 + offset, field_1_minimumCategory);
        LittleEndian.putShort(data, 6 + offset, field_2_maximumCategory);
        LittleEndian.putShort(data, 8 + offset, field_3_majorUnitValue);
        LittleEndian.putShort(data, 10 + offset, field_4_majorUnit);
        LittleEndian.putShort(data, 12 + offset, field_5_minorUnitValue);
        LittleEndian.putShort(data, 14 + offset, field_6_minorUnit);
        LittleEndian.putShort(data, 16 + offset, field_7_baseUnit);
        LittleEndian.putShort(data, 18 + offset, field_8_crossingPoint);
        LittleEndian.putShort(data, 20 + offset, field_9_options);

        return getRecordSize();
    }

    /**
     * Size of record (exluding 4 byte header)
     */
    public int getRecordSize()
    {
        return 4 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2;
    }

    public short getSid()
    {
        return this.sid;
    }

    public Object clone() {
      AxisOptionsRecord rec = new AxisOptionsRecord();
      
      rec.field_1_minimumCategory = field_1_minimumCategory;
      rec.field_2_maximumCategory = field_2_maximumCategory;
      rec.field_3_majorUnitValue = field_3_majorUnitValue;
      rec.field_4_majorUnit = field_4_majorUnit;
      rec.field_5_minorUnitValue = field_5_minorUnitValue;
      rec.field_6_minorUnit = field_6_minorUnit;
      rec.field_7_baseUnit = field_7_baseUnit;
      rec.field_8_crossingPoint = field_8_crossingPoint;
      rec.field_9_options = field_9_options;

      return rec;
    }


    /**
     * Get the minimum category field for the AxisOptions record.
     */
    public short getMinimumCategory()
    {
        return field_1_minimumCategory;
    }

    /**
     * Set the minimum category field for the AxisOptions record.
     */
    public void setMinimumCategory(short field_1_minimumCategory)
    {
        this.field_1_minimumCategory = field_1_minimumCategory;
    }

    /**
     * Get the maximum category field for the AxisOptions record.
     */
    public short getMaximumCategory()
    {
        return field_2_maximumCategory;
    }

    /**
     * Set the maximum category field for the AxisOptions record.
     */
    public void setMaximumCategory(short field_2_maximumCategory)
    {
        this.field_2_maximumCategory = field_2_maximumCategory;
    }

    /**
     * Get the major unit value field for the AxisOptions record.
     */
    public short getMajorUnitValue()
    {
        return field_3_majorUnitValue;
    }

    /**
     * Set the major unit value field for the AxisOptions record.
     */
    public void setMajorUnitValue(short field_3_majorUnitValue)
    {
        this.field_3_majorUnitValue = field_3_majorUnitValue;
    }

    /**
     * Get the major unit field for the AxisOptions record.
     */
    public short getMajorUnit()
    {
        return field_4_majorUnit;
    }

    /**
     * Set the major unit field for the AxisOptions record.
     */
    public void setMajorUnit(short field_4_majorUnit)
    {
        this.field_4_majorUnit = field_4_majorUnit;
    }

    /**
     * Get the minor unit value field for the AxisOptions record.
     */
    public short getMinorUnitValue()
    {
        return field_5_minorUnitValue;
    }

    /**
     * Set the minor unit value field for the AxisOptions record.
     */
    public void setMinorUnitValue(short field_5_minorUnitValue)
    {
        this.field_5_minorUnitValue = field_5_minorUnitValue;
    }

    /**
     * Get the minor unit field for the AxisOptions record.
     */
    public short getMinorUnit()
    {
        return field_6_minorUnit;
    }

    /**
     * Set the minor unit field for the AxisOptions record.
     */
    public void setMinorUnit(short field_6_minorUnit)
    {
        this.field_6_minorUnit = field_6_minorUnit;
    }

    /**
     * Get the base unit field for the AxisOptions record.
     */
    public short getBaseUnit()
    {
        return field_7_baseUnit;
    }

    /**
     * Set the base unit field for the AxisOptions record.
     */
    public void setBaseUnit(short field_7_baseUnit)
    {
        this.field_7_baseUnit = field_7_baseUnit;
    }

    /**
     * Get the crossing point field for the AxisOptions record.
     */
    public short getCrossingPoint()
    {
        return field_8_crossingPoint;
    }

    /**
     * Set the crossing point field for the AxisOptions record.
     */
    public void setCrossingPoint(short field_8_crossingPoint)
    {
        this.field_8_crossingPoint = field_8_crossingPoint;
    }

    /**
     * Get the options field for the AxisOptions record.
     */
    public short getOptions()
    {
        return field_9_options;
    }

    /**
     * Set the options field for the AxisOptions record.
     */
    public void setOptions(short field_9_options)
    {
        this.field_9_options = field_9_options;
    }

    /**
     * Sets the default minimum field value.
     * use the default minimum category
     */
    public void setDefaultMinimum(boolean value)
    {
        field_9_options = defaultMinimum.setShortBoolean(field_9_options, value);
    }

    /**
     * use the default minimum category
     * @return  the default minimum field value.
     */
    public boolean isDefaultMinimum()
    {
        return defaultMinimum.isSet(field_9_options);
    }

    /**
     * Sets the default maximum field value.
     * use the default maximum category
     */
    public void setDefaultMaximum(boolean value)
    {
        field_9_options = defaultMaximum.setShortBoolean(field_9_options, value);
    }

    /**
     * use the default maximum category
     * @return  the default maximum field value.
     */
    public boolean isDefaultMaximum()
    {
        return defaultMaximum.isSet(field_9_options);
    }

    /**
     * Sets the default major field value.
     * use the default major unit
     */
    public void setDefaultMajor(boolean value)
    {
        field_9_options = defaultMajor.setShortBoolean(field_9_options, value);
    }

    /**
     * use the default major unit
     * @return  the default major field value.
     */
    public boolean isDefaultMajor()
    {
        return defaultMajor.isSet(field_9_options);
    }

    /**
     * Sets the default minor unit field value.
     * use the default minor unit
     */
    public void setDefaultMinorUnit(boolean value)
    {
        field_9_options = defaultMinorUnit.setShortBoolean(field_9_options, value);
    }

    /**
     * use the default minor unit
     * @return  the default minor unit field value.
     */
    public boolean isDefaultMinorUnit()
    {
        return defaultMinorUnit.isSet(field_9_options);
    }

    /**
     * Sets the isDate field value.
     * this is a date axis
     */
    public void setIsDate(boolean value)
    {
        field_9_options = isDate.setShortBoolean(field_9_options, value);
    }

    /**
     * this is a date axis
     * @return  the isDate field value.
     */
    public boolean isIsDate()
    {
        return isDate.isSet(field_9_options);
    }

    /**
     * Sets the default base field value.
     * use the default base unit
     */
    public void setDefaultBase(boolean value)
    {
        field_9_options = defaultBase.setShortBoolean(field_9_options, value);
    }

    /**
     * use the default base unit
     * @return  the default base field value.
     */
    public boolean isDefaultBase()
    {
        return defaultBase.isSet(field_9_options);
    }

    /**
     * Sets the default cross field value.
     * use the default crossing point
     */
    public void setDefaultCross(boolean value)
    {
        field_9_options = defaultCross.setShortBoolean(field_9_options, value);
    }

    /**
     * use the default crossing point
     * @return  the default cross field value.
     */
    public boolean isDefaultCross()
    {
        return defaultCross.isSet(field_9_options);
    }

    /**
     * Sets the default date settings field value.
     * use default date setttings for this axis
     */
    public void setDefaultDateSettings(boolean value)
    {
        field_9_options = defaultDateSettings.setShortBoolean(field_9_options, value);
    }

    /**
     * use default date setttings for this axis
     * @return  the default date settings field value.
     */
    public boolean isDefaultDateSettings()
    {
        return defaultDateSettings.isSet(field_9_options);
    }


}  // END OF CLASS




