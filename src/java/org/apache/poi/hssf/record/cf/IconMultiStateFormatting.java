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

import org.apache.poi.ss.usermodel.IconMultiStateFormatting.IconSet;
import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.LittleEndianInput;
import org.apache.poi.util.LittleEndianOutput;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * Icon / Multi-State Conditional Formatting Rule Record.
 */
public final class IconMultiStateFormatting implements Cloneable {
    private static POILogger log = POILogFactory.getLogger(IconMultiStateFormatting.class);
            
    private IconSet iconSet;
    private byte options;
    private Threshold[] thresholds;
    
    private static BitField iconOnly = BitFieldFactory.getInstance(0x01);
    private static BitField reversed = BitFieldFactory.getInstance(0x04);

    public IconMultiStateFormatting() {
        iconSet = IconSet.GYR_3_TRAFFIC_LIGHTS;
        options = 0;
        thresholds = new Threshold[iconSet.num];
    }
    public IconMultiStateFormatting(LittleEndianInput in) {
        in.readShort(); // Ignored
        in.readByte();  // Reserved
        int num = in.readByte();
        int set = in.readByte();
        iconSet = IconSet.byId(set);
        if (iconSet.num != num) {
            log.log(POILogger.WARN, "Inconsistent Icon Set defintion, found " + iconSet + " but defined as " + num + " entries");
        }
        options = in.readByte();
        
        thresholds = new Threshold[iconSet.num];
        for (int i=0; i<thresholds.length; i++) {
            thresholds[i] = new IconMultiStateThreshold(in);
        }
    }
    
    public IconSet getIconSet() {
        return iconSet;
    }
    public void setIconSet(IconSet set) {
        this.iconSet = set;
    }

    public Threshold[] getThresholds() {
        return thresholds;
    }
    public void setThresholds(Threshold[] thresholds) {
        this.thresholds = (thresholds == null) ? null : thresholds.clone();
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
    
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("    [Icon Formatting]\n");
        buffer.append("          .icon_set = ").append(iconSet).append("\n");
        buffer.append("          .icon_only= ").append(isIconOnly()).append("\n");
        buffer.append("          .reversed = ").append(isReversed()).append("\n");
        for (Threshold t : thresholds) {
            buffer.append(t);
        }
        buffer.append("    [/Icon Formatting]\n");
        return buffer.toString();
    }
    
    public Object clone()  {
      IconMultiStateFormatting rec = new IconMultiStateFormatting();
      rec.iconSet = iconSet;
      rec.options = options;
      rec.thresholds = new Threshold[thresholds.length];
      System.arraycopy(thresholds, 0, rec.thresholds, 0, thresholds.length);
      return rec;
    }
    
    public int getDataLength() {
        int len = 6;
        for (Threshold t : thresholds) {
            len += t.getDataLength();
        }
        return len;
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(0);
        out.writeByte(0);
        out.writeByte(iconSet.num);
        out.writeByte(iconSet.id);
        out.writeByte(options);
        for (Threshold t : thresholds) {
            t.serialize(out);
        }
    }
}
