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

import static org.apache.poi.util.GenericRecordUtil.getEnumBitsAsString;

import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.hssf.record.HSSFRecordTypes;
import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.hssf.record.StandardRecord;
import org.apache.poi.ss.formula.Formula;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Describes a linked data record.  This record refers to the series data or text.
 */
public final class LinkedDataRecord extends StandardRecord {
    public static final short sid  = 0x1051;

    public static final byte LINK_TYPE_TITLE_OR_TEXT        = 0;
    public static final byte LINK_TYPE_VALUES               = 1;
    public static final byte LINK_TYPE_CATEGORIES           = 2;
    public static final byte LINK_TYPE_SECONDARY_CATEGORIES = 3;

    public static final byte REFERENCE_TYPE_DEFAULT_CATEGORIES = 0;
    public static final byte REFERENCE_TYPE_DIRECT          = 1;
    public static final byte REFERENCE_TYPE_WORKSHEET       = 2;
    public static final byte REFERENCE_TYPE_NOT_USED        = 3;
    public static final byte REFERENCE_TYPE_ERROR_REPORTED  = 4;

    private static final BitField customNumberFormat= BitFieldFactory.getInstance(0x1);

    private  byte       field_1_linkType;
    private  byte       field_2_referenceType;
    private  short      field_3_options;
    private  short      field_4_indexNumberFmtRecord;
    private  Formula field_5_formulaOfLink;


    public LinkedDataRecord() {}

    public LinkedDataRecord(LinkedDataRecord other) {
        super(other);
        field_1_linkType               = other.field_1_linkType;
        field_2_referenceType          = other.field_2_referenceType;
        field_3_options                = other.field_3_options;
        field_4_indexNumberFmtRecord   = other.field_4_indexNumberFmtRecord;
        field_5_formulaOfLink = (other.field_5_formulaOfLink == null) ? null : other.field_5_formulaOfLink.copy();
    }

    public LinkedDataRecord(RecordInputStream in) {
        field_1_linkType               = in.readByte();
        field_2_referenceType          = in.readByte();
        field_3_options                = in.readShort();
        field_4_indexNumberFmtRecord   = in.readShort();
        int encodedTokenLen = in.readUShort();
        field_5_formulaOfLink = Formula.read(encodedTokenLen, in);
    }

    public void serialize(LittleEndianOutput out) {
        out.writeByte(field_1_linkType);
        out.writeByte(field_2_referenceType);
        out.writeShort(field_3_options);
        out.writeShort(field_4_indexNumberFmtRecord);
        field_5_formulaOfLink.serialize(out);
    }

    protected int getDataSize() {
        return 1 + 1 + 2 + 2 + field_5_formulaOfLink.getEncodedSize();
    }

    public short getSid() {
        return sid;
    }

    @Override
    public LinkedDataRecord copy() {
        return new LinkedDataRecord(this);
    }

    /**
     * Get the link type field for the LinkedData record.
     *
     * @return  One of
     *        {@link #LINK_TYPE_TITLE_OR_TEXT},
     *        {@link #LINK_TYPE_VALUES},
     *        {@link #LINK_TYPE_CATEGORIES}, or
     *        {@link #LINK_TYPE_SECONDARY_CATEGORIES}
     */
    public byte getLinkType()
    {
        return field_1_linkType;
    }

    /**
     * Set the link type field for the LinkedData record.
     *
     * @param field_1_linkType
     *        One of
     *        {@link #LINK_TYPE_TITLE_OR_TEXT},
     *        {@link #LINK_TYPE_VALUES},
     *        {@link #LINK_TYPE_CATEGORIES}, or
     *        {@link #LINK_TYPE_SECONDARY_CATEGORIES}
     */
    public void setLinkType(byte field_1_linkType)
    {
        this.field_1_linkType = field_1_linkType;
    }

    /**
     * Get the reference type field for the LinkedData record.
     *
     * @return  One of
     *        {@link #REFERENCE_TYPE_DEFAULT_CATEGORIES}
     *        {@link #REFERENCE_TYPE_DIRECT}
     *        {@link #REFERENCE_TYPE_WORKSHEET}
     *        {@link #REFERENCE_TYPE_NOT_USED}
     *        {@link #REFERENCE_TYPE_ERROR_REPORTED}
     */
    public byte getReferenceType()
    {
        return field_2_referenceType;
    }

    /**
     * Set the reference type field for the LinkedData record.
     *
     * @param field_2_referenceType
     *        One of
     *        {@link #REFERENCE_TYPE_DEFAULT_CATEGORIES}
     *        {@link #REFERENCE_TYPE_DIRECT}
     *        {@link #REFERENCE_TYPE_WORKSHEET}
     *        {@link #REFERENCE_TYPE_NOT_USED}
     *        {@link #REFERENCE_TYPE_ERROR_REPORTED}
     */
    public void setReferenceType(byte field_2_referenceType)
    {
        this.field_2_referenceType = field_2_referenceType;
    }

    /**
     * Get the options field for the LinkedData record.
     */
    public short getOptions()
    {
        return field_3_options;
    }

    /**
     * Set the options field for the LinkedData record.
     */
    public void setOptions(short field_3_options)
    {
        this.field_3_options = field_3_options;
    }

    /**
     * Get the index number fmt record field for the LinkedData record.
     */
    public short getIndexNumberFmtRecord()
    {
        return field_4_indexNumberFmtRecord;
    }

    /**
     * Set the index number fmt record field for the LinkedData record.
     */
    public void setIndexNumberFmtRecord(short field_4_indexNumberFmtRecord)
    {
        this.field_4_indexNumberFmtRecord = field_4_indexNumberFmtRecord;
    }

    /**
     * Get the formula of link field for the LinkedData record.
     */
    public Ptg[] getFormulaOfLink() {
        return field_5_formulaOfLink.getTokens();
    }

    /**
     * Set the formula of link field for the LinkedData record.
     */
    public void setFormulaOfLink(Ptg[] ptgs)
    {
        this.field_5_formulaOfLink = Formula.create(ptgs);
    }

    /**
     * Sets the custom number format field value.
     * true if this object has a custom number format
     */
    public void setCustomNumberFormat(boolean value)
    {
        field_3_options = customNumberFormat.setShortBoolean(field_3_options, value);
    }

    /**
     * true if this object has a custom number format
     * @return  the custom number format field value.
     */
    public boolean isCustomNumberFormat()
    {
        return customNumberFormat.isSet(field_3_options);
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.LINKED_DATA;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "linkType", getEnumBitsAsString(this::getLinkType,
                new int[]{LINK_TYPE_TITLE_OR_TEXT,LINK_TYPE_VALUES,LINK_TYPE_CATEGORIES,LINK_TYPE_SECONDARY_CATEGORIES},
                new String[]{"TITLE_OR_TEXT","VALUES","CATEGORIES","SECONDARY_CATEGORIES"}),
            "referenceType", getEnumBitsAsString(this::getReferenceType,
                new int[]{REFERENCE_TYPE_DEFAULT_CATEGORIES, REFERENCE_TYPE_DIRECT, REFERENCE_TYPE_WORKSHEET, REFERENCE_TYPE_NOT_USED, REFERENCE_TYPE_ERROR_REPORTED},
                new String[]{"DEFAULT_CATEGORIES","DIRECT","WORKSHEET","NOT_USED","ERROR_REPORTED"}),
            "options", this::getOptions,
            "customNumberFormat", this::isCustomNumberFormat,
            "indexNumberFmtRecord", this::getIndexNumberFmtRecord,
            "formulaOfLink", () -> field_5_formulaOfLink
        );
    }
}
