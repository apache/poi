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

import org.apache.poi.hssf.record.common.ExtendedColor;
import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.LittleEndianInput;
import org.apache.poi.util.LittleEndianOutput;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * Data Bar Conditional Formatting Rule Record.
 */
public final class DataBarFormatting implements Cloneable {
    private static POILogger log = POILogFactory.getLogger(DataBarFormatting.class);

    private byte options;
    private byte percentMin;
    private byte percentMax;
    private ExtendedColor color;
    private DataBarThreshold thresholdMin;
    private DataBarThreshold thresholdMax;
    
    private static BitField iconOnly = BitFieldFactory.getInstance(0x01);
    private static BitField reversed = BitFieldFactory.getInstance(0x04);
    
    public DataBarFormatting() {
        options = 2;
    }
    public DataBarFormatting(LittleEndianInput in) {
        in.readShort(); // Ignored
        in.readByte();  // Reserved
        options = in.readByte();
        
        percentMin = in.readByte();
        percentMax = in.readByte();
        if (percentMin < 0 || percentMin > 100)
            log.log(POILogger.WARN, "Inconsistent Minimum Percentage found " + percentMin);
        if (percentMax < 0 || percentMax > 100)
            log.log(POILogger.WARN, "Inconsistent Minimum Percentage found " + percentMin);
        
        color = new ExtendedColor(in);
        thresholdMin = new DataBarThreshold(in);
        thresholdMax = new DataBarThreshold(in);
    }
    
    public boolean isIconOnly() {
        return getOptionFlag(iconOnly);
    }
    public void setIconOnly(boolean only) {
        setOptionFlag(only, iconOnly);
    }
    
    public boolean isReversed() {
        return getOptionFlag(reversed);
    }
    public void setReversed(boolean rev) {
        setOptionFlag(rev, reversed);
    }
    
    private boolean getOptionFlag(BitField field) {
        int value = field.getValue(options);
        return value==0 ? false : true;
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
    
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("    [Data Bar Formatting]\n");
        buffer.append("          .icon_only= ").append(isIconOnly()).append("\n");
        buffer.append("          .reversed = ").append(isReversed()).append("\n");
        buffer.append(color);
        buffer.append(thresholdMin);
        buffer.append(thresholdMax);
        buffer.append("    [/Data Bar Formatting]\n");
        return buffer.toString();
    }
    
    public Object clone()  {
      DataBarFormatting rec = new DataBarFormatting();
      rec.options = options;
      rec.percentMin = percentMin;
      rec.percentMax = percentMax;
      rec.color = color.clone();
      rec.thresholdMin = thresholdMin.clone();
      rec.thresholdMax = thresholdMax.clone();
      return rec;
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
