
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

        int pos = 0;
        field_1_minimumCategory        = LittleEndian.getShort(data, pos + 0x0 + offset);
        field_2_maximumCategory        = LittleEndian.getShort(data, pos + 0x2 + offset);
        field_3_majorUnitValue         = LittleEndian.getShort(data, pos + 0x4 + offset);
        field_4_majorUnit              = LittleEndian.getShort(data, pos + 0x6 + offset);
        field_5_minorUnitValue         = LittleEndian.getShort(data, pos + 0x8 + offset);
        field_6_minorUnit              = LittleEndian.getShort(data, pos + 0xa + offset);
        field_7_baseUnit               = LittleEndian.getShort(data, pos + 0xc + offset);
        field_8_crossingPoint          = LittleEndian.getShort(data, pos + 0xe + offset);
        field_9_options                = LittleEndian.getShort(data, pos + 0x10 + offset);

    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[AXCEXT]\n");
        buffer.append("    .minimumCategory      = ")
            .append("0x").append(HexDump.toHex(  getMinimumCategory ()))
            .append(" (").append( getMinimumCategory() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .maximumCategory      = ")
            .append("0x").append(HexDump.toHex(  getMaximumCategory ()))
            .append(" (").append( getMaximumCategory() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .majorUnitValue       = ")
            .append("0x").append(HexDump.toHex(  getMajorUnitValue ()))
            .append(" (").append( getMajorUnitValue() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .majorUnit            = ")
            .append("0x").append(HexDump.toHex(  getMajorUnit ()))
            .append(" (").append( getMajorUnit() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .minorUnitValue       = ")
            .append("0x").append(HexDump.toHex(  getMinorUnitValue ()))
            .append(" (").append( getMinorUnitValue() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .minorUnit            = ")
            .append("0x").append(HexDump.toHex(  getMinorUnit ()))
            .append(" (").append( getMinorUnit() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .baseUnit             = ")
            .append("0x").append(HexDump.toHex(  getBaseUnit ()))
            .append(" (").append( getBaseUnit() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .crossingPoint        = ")
            .append("0x").append(HexDump.toHex(  getCrossingPoint ()))
            .append(" (").append( getCrossingPoint() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .options              = ")
            .append("0x").append(HexDump.toHex(  getOptions ()))
            .append(" (").append( getOptions() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("         .defaultMinimum           = ").append(isDefaultMinimum()).append('\n'); 
        buffer.append("         .defaultMaximum           = ").append(isDefaultMaximum()).append('\n'); 
        buffer.append("         .defaultMajor             = ").append(isDefaultMajor()).append('\n'); 
        buffer.append("         .defaultMinorUnit         = ").append(isDefaultMinorUnit()).append('\n'); 
        buffer.append("         .isDate                   = ").append(isIsDate()).append('\n'); 
        buffer.append("         .defaultBase              = ").append(isDefaultBase()).append('\n'); 
        buffer.append("         .defaultCross             = ").append(isDefaultCross()).append('\n'); 
        buffer.append("         .defaultDateSettings      = ").append(isDefaultDateSettings()).append('\n'); 

        buffer.append("[/AXCEXT]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte[] data)
    {
        int pos = 0;

        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, (short)(getRecordSize() - 4));

        LittleEndian.putShort(data, 4 + offset + pos, field_1_minimumCategory);
        LittleEndian.putShort(data, 6 + offset + pos, field_2_maximumCategory);
        LittleEndian.putShort(data, 8 + offset + pos, field_3_majorUnitValue);
        LittleEndian.putShort(data, 10 + offset + pos, field_4_majorUnit);
        LittleEndian.putShort(data, 12 + offset + pos, field_5_minorUnitValue);
        LittleEndian.putShort(data, 14 + offset + pos, field_6_minorUnit);
        LittleEndian.putShort(data, 16 + offset + pos, field_7_baseUnit);
        LittleEndian.putShort(data, 18 + offset + pos, field_8_crossingPoint);
        LittleEndian.putShort(data, 20 + offset + pos, field_9_options);

        return getRecordSize();
    }

    /**
     * Size of record (exluding 4 byte header)
     */
    public int getRecordSize()
    {
        return 4  + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2;
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




