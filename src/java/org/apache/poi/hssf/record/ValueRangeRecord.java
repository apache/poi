
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



import org.apache.poi.util.BitField;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.StringUtil;
import org.apache.poi.util.HexDump;

/**
 * The value range record defines the range of the value axis.
 * NOTE: This source is automatically generated please do not modify this file.  Either subclass or
 *       remove the record in src/records/definitions.

 * @author Glen Stampoultzis (glens at apache.org)
 */
public class ValueRangeRecord
    extends Record
{
    public final static short      sid                             = 0x101f;
    private  double     field_1_minimumAxisValue;
    private  double     field_2_maximumAxisValue;
    private  double     field_3_majorIncrement;
    private  double     field_4_minorIncrement;
    private  double     field_5_categoryAxisCross;
    private  short      field_6_options;
    private BitField   automaticMinimum                           = new BitField(0x1);
    private BitField   automaticMaximum                           = new BitField(0x2);
    private BitField   automaticMajor                             = new BitField(0x4);
    private BitField   automaticMinor                             = new BitField(0x8);
    private BitField   automaticCategoryCrossing                  = new BitField(0x10);
    private BitField   logarithmicScale                           = new BitField(0x20);
    private BitField   valuesInReverse                            = new BitField(0x40);
    private BitField   crossCategoryAxisAtMaximum                 = new BitField(0x80);
    private BitField   reserved                                   = new BitField(0x100);


    public ValueRangeRecord()
    {

    }

    /**
     * Constructs a ValueRange record and sets its fields appropriately.
     *
     * @param id    id must be 0x101f or an exception
     *              will be throw upon validation
     * @param size  size the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public ValueRangeRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    }

    /**
     * Constructs a ValueRange record and sets its fields appropriately.
     *
     * @param id    id must be 0x101f or an exception
     *              will be throw upon validation
     * @param size  size the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the record's data
     */

    public ValueRangeRecord(short id, short size, byte [] data, int offset)
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
            throw new RecordFormatException("Not a ValueRange record");
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {
        field_1_minimumAxisValue        = LittleEndian.getDouble(data, 0x0 + offset);
        field_2_maximumAxisValue        = LittleEndian.getDouble(data, 0x8 + offset);
        field_3_majorIncrement          = LittleEndian.getDouble(data, 0x10 + offset);
        field_4_minorIncrement          = LittleEndian.getDouble(data, 0x18 + offset);
        field_5_categoryAxisCross       = LittleEndian.getDouble(data, 0x20 + offset);
        field_6_options                 = LittleEndian.getShort(data, 0x28 + offset);

    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[ValueRange]\n");

        buffer.append("    .minimumAxisValue     = ")
            .append(" (").append(getMinimumAxisValue()).append(" )\n");

        buffer.append("    .maximumAxisValue     = ")
            .append(" (").append(getMaximumAxisValue()).append(" )\n");

        buffer.append("    .majorIncrement       = ")
            .append(" (").append(getMajorIncrement()).append(" )\n");

        buffer.append("    .minorIncrement       = ")
            .append(" (").append(getMinorIncrement()).append(" )\n");

        buffer.append("    .categoryAxisCross    = ")
            .append(" (").append(getCategoryAxisCross()).append(" )\n");

        buffer.append("    .options              = ")
            .append("0x")
            .append(HexDump.toHex((short)getOptions()))
            .append(" (").append(getOptions()).append(" )\n");
        buffer.append("         .automaticMinimum         = ").append(isAutomaticMinimum    ()).append('\n');
        buffer.append("         .automaticMaximum         = ").append(isAutomaticMaximum    ()).append('\n');
        buffer.append("         .automaticMajor           = ").append(isAutomaticMajor      ()).append('\n');
        buffer.append("         .automaticMinor           = ").append(isAutomaticMinor      ()).append('\n');
        buffer.append("         .automaticCategoryCrossing     = ").append(isAutomaticCategoryCrossing()).append('\n');
        buffer.append("         .logarithmicScale         = ").append(isLogarithmicScale    ()).append('\n');
        buffer.append("         .valuesInReverse          = ").append(isValuesInReverse     ()).append('\n');
        buffer.append("         .crossCategoryAxisAtMaximum     = ").append(isCrossCategoryAxisAtMaximum()).append('\n');
        buffer.append("         .reserved                 = ").append(isReserved            ()).append('\n');

        buffer.append("[/ValueRange]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte[] data)
    {
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, (short)(getRecordSize() - 4));

        LittleEndian.putDouble(data, 4 + offset, field_1_minimumAxisValue);
        LittleEndian.putDouble(data, 12 + offset, field_2_maximumAxisValue);
        LittleEndian.putDouble(data, 20 + offset, field_3_majorIncrement);
        LittleEndian.putDouble(data, 28 + offset, field_4_minorIncrement);
        LittleEndian.putDouble(data, 36 + offset, field_5_categoryAxisCross);
        LittleEndian.putShort(data, 44 + offset, field_6_options);

        return getRecordSize();
    }

    /**
     * Size of record (exluding 4 byte header)
     */
    public int getRecordSize()
    {
        return 4 + 8 + 8 + 8 + 8 + 8 + 2;
    }

    public short getSid()
    {
        return this.sid;
    }


    /**
     * Get the minimum axis value field for the ValueRange record.
     */
    public double getMinimumAxisValue()
    {
        return field_1_minimumAxisValue;
    }

    /**
     * Set the minimum axis value field for the ValueRange record.
     */
    public void setMinimumAxisValue(double field_1_minimumAxisValue)
    {
        this.field_1_minimumAxisValue = field_1_minimumAxisValue;
    }

    /**
     * Get the maximum axis value field for the ValueRange record.
     */
    public double getMaximumAxisValue()
    {
        return field_2_maximumAxisValue;
    }

    /**
     * Set the maximum axis value field for the ValueRange record.
     */
    public void setMaximumAxisValue(double field_2_maximumAxisValue)
    {
        this.field_2_maximumAxisValue = field_2_maximumAxisValue;
    }

    /**
     * Get the major increment field for the ValueRange record.
     */
    public double getMajorIncrement()
    {
        return field_3_majorIncrement;
    }

    /**
     * Set the major increment field for the ValueRange record.
     */
    public void setMajorIncrement(double field_3_majorIncrement)
    {
        this.field_3_majorIncrement = field_3_majorIncrement;
    }

    /**
     * Get the minor increment field for the ValueRange record.
     */
    public double getMinorIncrement()
    {
        return field_4_minorIncrement;
    }

    /**
     * Set the minor increment field for the ValueRange record.
     */
    public void setMinorIncrement(double field_4_minorIncrement)
    {
        this.field_4_minorIncrement = field_4_minorIncrement;
    }

    /**
     * Get the category axis cross field for the ValueRange record.
     */
    public double getCategoryAxisCross()
    {
        return field_5_categoryAxisCross;
    }

    /**
     * Set the category axis cross field for the ValueRange record.
     */
    public void setCategoryAxisCross(double field_5_categoryAxisCross)
    {
        this.field_5_categoryAxisCross = field_5_categoryAxisCross;
    }

    /**
     * Get the options field for the ValueRange record.
     */
    public short getOptions()
    {
        return field_6_options;
    }

    /**
     * Set the options field for the ValueRange record.
     */
    public void setOptions(short field_6_options)
    {
        this.field_6_options = field_6_options;
    }

    /**
     * Sets the automatic minimum field value.
     * automatic minimum value selected
     */
    public void setAutomaticMinimum(boolean value)
    {
        field_6_options = automaticMinimum.setShortBoolean(field_6_options, value);
    }

    /**
     * automatic minimum value selected
     * @return  the automatic minimum field value.
     */
    public boolean isAutomaticMinimum()
    {
        return automaticMinimum.isSet(field_6_options);
    }

    /**
     * Sets the automatic maximum field value.
     * automatic maximum value selected
     */
    public void setAutomaticMaximum(boolean value)
    {
        field_6_options = automaticMaximum.setShortBoolean(field_6_options, value);
    }

    /**
     * automatic maximum value selected
     * @return  the automatic maximum field value.
     */
    public boolean isAutomaticMaximum()
    {
        return automaticMaximum.isSet(field_6_options);
    }

    /**
     * Sets the automatic major field value.
     * automatic major unit selected
     */
    public void setAutomaticMajor(boolean value)
    {
        field_6_options = automaticMajor.setShortBoolean(field_6_options, value);
    }

    /**
     * automatic major unit selected
     * @return  the automatic major field value.
     */
    public boolean isAutomaticMajor()
    {
        return automaticMajor.isSet(field_6_options);
    }

    /**
     * Sets the automatic minor field value.
     * automatic minor unit selected
     */
    public void setAutomaticMinor(boolean value)
    {
        field_6_options = automaticMinor.setShortBoolean(field_6_options, value);
    }

    /**
     * automatic minor unit selected
     * @return  the automatic minor field value.
     */
    public boolean isAutomaticMinor()
    {
        return automaticMinor.isSet(field_6_options);
    }

    /**
     * Sets the automatic category crossing field value.
     * category crossing point is automatically selected
     */
    public void setAutomaticCategoryCrossing(boolean value)
    {
        field_6_options = automaticCategoryCrossing.setShortBoolean(field_6_options, value);
    }

    /**
     * category crossing point is automatically selected
     * @return  the automatic category crossing field value.
     */
    public boolean isAutomaticCategoryCrossing()
    {
        return automaticCategoryCrossing.isSet(field_6_options);
    }

    /**
     * Sets the logarithmic scale field value.
     * use logarithmic scale
     */
    public void setLogarithmicScale(boolean value)
    {
        field_6_options = logarithmicScale.setShortBoolean(field_6_options, value);
    }

    /**
     * use logarithmic scale
     * @return  the logarithmic scale field value.
     */
    public boolean isLogarithmicScale()
    {
        return logarithmicScale.isSet(field_6_options);
    }

    /**
     * Sets the values in reverse field value.
     * values are reverses in graph
     */
    public void setValuesInReverse(boolean value)
    {
        field_6_options = valuesInReverse.setShortBoolean(field_6_options, value);
    }

    /**
     * values are reverses in graph
     * @return  the values in reverse field value.
     */
    public boolean isValuesInReverse()
    {
        return valuesInReverse.isSet(field_6_options);
    }

    /**
     * Sets the cross category axis at maximum field value.
     * category axis to cross at maximum value
     */
    public void setCrossCategoryAxisAtMaximum(boolean value)
    {
        field_6_options = crossCategoryAxisAtMaximum.setShortBoolean(field_6_options, value);
    }

    /**
     * category axis to cross at maximum value
     * @return  the cross category axis at maximum field value.
     */
    public boolean isCrossCategoryAxisAtMaximum()
    {
        return crossCategoryAxisAtMaximum.isSet(field_6_options);
    }

    /**
     * Sets the reserved field value.
     * reserved, must equal 1 (excel dev. guide says otherwise)
     */
    public void setReserved(boolean value)
    {
        field_6_options = reserved.setShortBoolean(field_6_options, value);
    }

    /**
     * reserved, must equal 1 (excel dev. guide says otherwise)
     * @return  the reserved field value.
     */
    public boolean isReserved()
    {
        return reserved.isSet(field_6_options);
    }


}  // END OF CLASS




