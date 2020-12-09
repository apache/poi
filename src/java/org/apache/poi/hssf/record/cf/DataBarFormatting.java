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

package org.apache.poi.hssf.record.cf;

import static org.apache.poi.util.GenericRecordUtil.getBitsAsString;

import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.common.Duplicatable;
import org.apache.poi.common.usermodel.GenericRecord;
import org.apache.poi.hssf.record.common.ExtendedColor;
import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.GenericRecordJsonWriter;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndianInput;
import org.apache.poi.util.LittleEndianOutput;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * Data Bar Conditional Formatting Rule Record.
 */
public final class DataBarFormatting implements Duplicatable, GenericRecord {
    private static final POILogger LOG = POILogFactory.getLogger(DataBarFormatting.class);

    private static final BitField ICON_ONLY = BitFieldFactory.getInstance(0x01);
    private static final BitField REVERSED = BitFieldFactory.getInstance(0x04);

    private byte options;
    private byte percentMin;
    private byte percentMax;
    private ExtendedColor color;
    private DataBarThreshold thresholdMin;
    private DataBarThreshold thresholdMax;

    public DataBarFormatting() {
        options = 2;
    }

    public DataBarFormatting(DataBarFormatting other) {
        options = other.options;
        percentMin = other.percentMin;
        percentMax = other.percentMax;
        color = (other.color == null) ? null : other.color.copy();
        thresholdMin = (other.thresholdMin == null) ? null : other.thresholdMin.copy();
        thresholdMax = (other.thresholdMax == null) ? null : other.thresholdMax.copy();
    }

    public DataBarFormatting(LittleEndianInput in) {
        in.readShort(); // Ignored
        in.readByte();  // Reserved
        options = in.readByte();

        percentMin = in.readByte();
        percentMax = in.readByte();
        if (percentMin < 0 || percentMin > 100)
            LOG.log(POILogger.WARN, "Inconsistent Minimum Percentage found " + percentMin);
        if (percentMax < 0 || percentMax > 100)
            LOG.log(POILogger.WARN, "Inconsistent Minimum Percentage found " + percentMin);

        color = new ExtendedColor(in);
        thresholdMin = new DataBarThreshold(in);
        thresholdMax = new DataBarThreshold(in);
    }

    public boolean isIconOnly() {
        return getOptionFlag(ICON_ONLY);
    }
    public void setIconOnly(boolean only) {
        setOptionFlag(only, ICON_ONLY);
    }

    public boolean isReversed() {
        return getOptionFlag(REVERSED);
    }
    public void setReversed(boolean rev) {
        setOptionFlag(rev, REVERSED);
    }

    private boolean getOptionFlag(BitField field) {
        int value = field.getValue(options);
        return value != 0;
    }
    private void setOptionFlag(boolean option, BitField field) {
        options = field.setByteBoolean(options, option);
    }

    public byte getPercentMin() {
        return percentMin;
    }
    public void setPercentMin(byte percentMin) {
        this.percentMin = percentMin;
    }

    public byte getPercentMax() {
        return percentMax;
    }
    public void setPercentMax(byte percentMax) {
        this.percentMax = percentMax;
    }

    public ExtendedColor getColor() {
        return color;
    }
    public void setColor(ExtendedColor color) {
        this.color = color;
    }

    public DataBarThreshold getThresholdMin() {
        return thresholdMin;
    }
    public void setThresholdMin(DataBarThreshold thresholdMin) {
        this.thresholdMin = thresholdMin;
    }

    public DataBarThreshold getThresholdMax() {
        return thresholdMax;
    }
    public void setThresholdMax(DataBarThreshold thresholdMax) {
        this.thresholdMax = thresholdMax;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "options", getBitsAsString(() -> options, new BitField[]{ICON_ONLY, REVERSED}, new String[]{"ICON_ONLY", "REVERSED"}),
            "color", this::getColor,
            "percentMin", this::getPercentMin,
            "percentMax", this::getPercentMax,
            "thresholdMin", this::getThresholdMin,
            "thresholdMax", this::getThresholdMax
        );
    }

    public String toString() {
        return GenericRecordJsonWriter.marshal(this);
    }

    public DataBarFormatting copy()  {
      return new DataBarFormatting(this);
    }

    public int getDataLength() {
        return 6 + color.getDataLength() +
               thresholdMin.getDataLength() +
               thresholdMax.getDataLength();
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(0);
        out.writeByte(0);
        out.writeByte(options);
        out.writeByte(percentMin);
        out.writeByte(percentMax);
        color.serialize(out);
        thresholdMin.serialize(out);
        thresholdMax.serialize(out);
    }
}
