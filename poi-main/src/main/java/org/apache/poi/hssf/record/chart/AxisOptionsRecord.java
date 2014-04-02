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

package org.apache.poi.hssf.record.chart;

import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.hssf.record.StandardRecord;
import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.LittleEndianOutput;

/**
 * The axis options record provides unit information and other various tidbits about the axis.<p/>
 * 
 * @author Andrew C. Oliver(acoliver at apache.org)
 */
public final class AxisOptionsRecord extends StandardRecord {
    public final static short sid = 0x1062;

    private static final BitField defaultMinimum      = BitFieldFactory.getInstance(0x01);
    private static final BitField defaultMaximum      = BitFieldFactory.getInstance(0x02);
    private static final BitField defaultMajor        = BitFieldFactory.getInstance(0x04);
    private static final BitField defaultMinorUnit    = BitFieldFactory.getInstance(0x08);
    private static final BitField isDate              = BitFieldFactory.getInstance(0x10);
    private static final BitField defaultBase         = BitFieldFactory.getInstance(0x20);
    private static final BitField defaultCross        = BitFieldFactory.getInstance(0x40);
    private static final BitField defaultDateSettings = BitFieldFactory.getInstance(0x80);

    private short field_1_minimumCategory;
    private short field_2_maximumCategory;
    private short field_3_majorUnitValue;
    private short field_4_majorUnit;
    private short field_5_minorUnitValue;
    private short field_6_minorUnit;
    private short field_7_baseUnit;
    private short field_8_crossingPoint;
    private short field_9_options;


    public AxisOptionsRecord()
    {

    }

    public AxisOptionsRecord(RecordInputStream in)
    {
        field_1_minimumCategory        = in.readShort();
        field_2_maximumCategory        = in.readShort();
        field_3_majorUnitValue         = in.readShort();
        field_4_majorUnit              = in.readShort();
        field_5_minorUnitValue         = in.readShort();
        field_6_minorUnit              = in.readShort();
        field_7_baseUnit               = in.readShort();
        field_8_crossingPoint          = in.readShort();
        field_9_options                = in.readShort();
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

    public void serialize(LittleEndianOutput out) {
        out.writeShort(field_1_minimumCategory);
        out.writeShort(field_2_maximumCategory);
        out.writeShort(field_3_majorUnitValue);
        out.writeShort(field_4_majorUnit);
        out.writeShort(field_5_minorUnitValue);
        out.writeShort(field_6_minorUnit);
        out.writeShort(field_7_baseUnit);
        out.writeShort(field_8_crossingPoint);
        out.writeShort(field_9_options);
    }

    protected int getDataSize() {
        return 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2;
    }

    public short getSid()
    {
        return sid;
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
}
