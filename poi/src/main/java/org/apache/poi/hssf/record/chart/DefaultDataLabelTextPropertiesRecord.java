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

import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.hssf.record.HSSFRecordTypes;
import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.hssf.record.StandardRecord;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndianOutput;

/**
 * The default data label text properties record identifies the text characteristics of the preceding text record.
 */
public final class DefaultDataLabelTextPropertiesRecord extends StandardRecord {
    public static final short sid = 0x1024;
    public static final short CATEGORY_DATA_TYPE_SHOW_LABELS_CHARACTERISTIC = 0;
    public static final short CATEGORY_DATA_TYPE_VALUE_AND_PERCENTAGE_CHARACTERISTIC = 1;
    public static final short CATEGORY_DATA_TYPE_ALL_TEXT_CHARACTERISTIC = 2;

    private short field_1_categoryDataType;

    public DefaultDataLabelTextPropertiesRecord() {}

    public DefaultDataLabelTextPropertiesRecord(DefaultDataLabelTextPropertiesRecord other) {
        super(other);
        field_1_categoryDataType = other.field_1_categoryDataType;
    }

    public DefaultDataLabelTextPropertiesRecord(RecordInputStream in) {
        field_1_categoryDataType = in.readShort();
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(field_1_categoryDataType);
    }

    protected int getDataSize() {
        return 2;
    }

    public short getSid()
    {
        return sid;
    }

    @Override
    public DefaultDataLabelTextPropertiesRecord copy() {
        return new DefaultDataLabelTextPropertiesRecord(this);
    }

    /**
     * Get the category data type field for the DefaultDataLabelTextProperties record.
     *
     * @return  One of
     *        CATEGORY_DATA_TYPE_SHOW_LABELS_CHARACTERISTIC
     *        CATEGORY_DATA_TYPE_VALUE_AND_PERCENTAGE_CHARACTERISTIC
     *        CATEGORY_DATA_TYPE_ALL_TEXT_CHARACTERISTIC
     */
    public short getCategoryDataType()
    {
        return field_1_categoryDataType;
    }

    /**
     * Set the category data type field for the DefaultDataLabelTextProperties record.
     *
     * @param field_1_categoryDataType
     *        One of
     *        CATEGORY_DATA_TYPE_SHOW_LABELS_CHARACTERISTIC
     *        CATEGORY_DATA_TYPE_VALUE_AND_PERCENTAGE_CHARACTERISTIC
     *        CATEGORY_DATA_TYPE_ALL_TEXT_CHARACTERISTIC
     */
    public void setCategoryDataType(short field_1_categoryDataType)
    {
        this.field_1_categoryDataType = field_1_categoryDataType;
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.DEFAULT_DATA_LABEL_TEXT_PROPERTIES;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "categoryDataType", this::getCategoryDataType
        );
    }
}
