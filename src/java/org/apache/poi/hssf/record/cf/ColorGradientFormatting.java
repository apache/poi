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

import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.LittleEndianInput;
import org.apache.poi.util.LittleEndianOutput;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * Color Gradient / Color Scale Conditional Formatting Rule Record.
 * (Called Color Gradient in the file format docs, but more commonly
 *  Color Scale in the UI)
 */
public final class ColorGradientFormatting implements Cloneable {
    private static POILogger log = POILogFactory.getLogger(ColorGradientFormatting.class);

    private byte options = 0;
    private Threshold[] thresholds;
    private byte[] colors; // TODO Decode
    
    private static BitField clamp = BitFieldFactory.getInstance(0x01);
    private static BitField background = BitFieldFactory.getInstance(0x02);
    
    public ColorGradientFormatting() {
        options = 3;
        thresholds = new Threshold[3];
    }
    public ColorGradientFormatting(LittleEndianInput in) {
        in.readShort(); // Ignored
        in.readByte();  // Reserved
        int numI = in.readByte();
        int numG = in.readByte();
        if (numI != numG) {
            log.log(POILogger.WARN, "Inconsistent Color Gradient defintion, found " + numI + " vs " + numG + " entries");
        }
        options = in.readByte();
        
        // TODO Are these correct?
        thresholds = new Threshold[numI];
        for (int i=0; i<thresholds.length; i++) {
            thresholds[i] = new Threshold(in);
            in.readDouble(); // Rather pointless value...
        }
        // TODO Decode colors
        colors = new byte[in.available()];
        in.readFully(colors);
    }
    
    public Threshold[] getThresholds() {
        return thresholds;
    }
    public void setThresholds(Threshold[] thresholds) {
        this.thresholds = thresholds;
    }

    // TODO Colors
    
    public boolean isClampToCurve() {
        return getOptionFlag(clamp);
    }
    public boolean isAppliesToBackground() {
        return getOptionFlag(background);
    }
    private boolean getOptionFlag(BitField field) {
        int value = field.getValue(options);
        return value==0 ? false : true;
    }
    
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("    [Color Gradient Formatting]\n");
        buffer.append("          .clamp     = ").append(isClampToCurve()).append("\n");
        buffer.append("          .background= ").append(isAppliesToBackground()).append("\n");
        for (Threshold t : thresholds) {
            buffer.append(t.toString());
        }
        buffer.append("    [/Color Gradient Formatting]\n");
        return buffer.toString();
    }
    
    public Object clone()  {
      ColorGradientFormatting rec = new ColorGradientFormatting();
      rec.options = options;
      rec.thresholds = new Threshold[thresholds.length];
      System.arraycopy(thresholds, 0, rec.thresholds, 0, thresholds.length);
      // TODO Colors
      return rec;
    }
    
    public int getDataLength() {
        int len = 6;
        for (Threshold t : thresholds) {
            len += t.getDataLength();
            len += 8;
        }
        len += colors.length;
        return len;
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(0);
        out.writeByte(0);
        out.writeByte(thresholds.length);
        out.writeByte(thresholds.length);
        out.writeByte(options);
        
        double step = 1d / (thresholds.length-1);
        for (int i=0; i<thresholds.length; i++) {
            Threshold t = thresholds[i];
            t.serialize(out);
            out.writeDouble(step*i);
        }
        
        out.write(colors);
    }
}
