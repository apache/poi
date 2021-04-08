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

import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.common.Duplicatable;
import org.apache.poi.common.usermodel.GenericRecord;
import org.apache.poi.hssf.record.common.ExtendedColor;
import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.GenericRecordJsonWriter;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndianInput;
import org.apache.poi.util.LittleEndianOutput;

import static org.apache.logging.log4j.util.Unbox.box;

/**
 * Color Gradient / Color Scale Conditional Formatting Rule Record.
 * (Called Color Gradient in the file format docs, but more commonly
 *  Color Scale in the UI)
 */
public final class ColorGradientFormatting implements Duplicatable, GenericRecord {
    private static final Logger LOGGER = LogManager.getLogger(ColorGradientFormatting.class);

    private static final BitField clamp = BitFieldFactory.getInstance(0x01);
    private static final BitField background = BitFieldFactory.getInstance(0x02);

    private final byte options;
    private ColorGradientThreshold[] thresholds;
    private ExtendedColor[] colors;

    public ColorGradientFormatting() {
        options = 3;
        thresholds = new ColorGradientThreshold[3];
        colors = new ExtendedColor[3];
    }

    public ColorGradientFormatting(ColorGradientFormatting other) {
        options = other.options;
        if (other.thresholds != null) {
            thresholds = Stream.of(other.thresholds).map(ColorGradientThreshold::copy).toArray(ColorGradientThreshold[]::new);
        }
        if (other.colors != null) {
            colors = Stream.of(other.colors).map(ExtendedColor::copy).toArray(ExtendedColor[]::new);
        }
    }

    public ColorGradientFormatting(LittleEndianInput in) {
        in.readShort(); // Ignored
        in.readByte();  // Reserved
        int numI = in.readByte();
        int numG = in.readByte();
        if (numI != numG) {
            LOGGER.atWarn().log("Inconsistent Color Gradient definition, found {} vs {} entries", box(numI),box(numG));
        }
        options = in.readByte();

        thresholds = new ColorGradientThreshold[numI];
        for (int i=0; i<thresholds.length; i++) {
            thresholds[i] = new ColorGradientThreshold(in);
        }
        colors = new ExtendedColor[numG];
        for (int i=0; i<colors.length; i++) {
            in.readDouble(); // Slightly pointless step counter
            colors[i] = new ExtendedColor(in);
        }
    }

    public int getNumControlPoints() {
        return thresholds.length;
    }
    public void setNumControlPoints(int num) {
        if (num != thresholds.length) {
            ColorGradientThreshold[] nt = new ColorGradientThreshold[num];
            ExtendedColor[] nc = new ExtendedColor[num];

            int copy = Math.min(thresholds.length, num);
            System.arraycopy(thresholds, 0, nt, 0, copy);
            System.arraycopy(colors, 0, nc, 0, copy);

            this.thresholds = nt;
            this.colors = nc;

            updateThresholdPositions();
        }
    }

    public ColorGradientThreshold[] getThresholds() {
        return thresholds;
    }
    public void setThresholds(ColorGradientThreshold[] thresholds) {
        this.thresholds = (thresholds == null) ? null : thresholds.clone();
        updateThresholdPositions();
    }

    public ExtendedColor[] getColors() {
        return colors;
    }
    public void setColors(ExtendedColor[] colors) {
        this.colors = (colors == null) ? null : colors.clone();
    }

    public boolean isClampToCurve() {
        return getOptionFlag(clamp);
    }
    public boolean isAppliesToBackground() {
        return getOptionFlag(background);
    }
    private boolean getOptionFlag(BitField field) {
        return field.isSet(options);
    }

    private void updateThresholdPositions() {
        double step = 1d / (thresholds.length-1);
        for (int i=0; i<thresholds.length; i++) {
            thresholds[i].setPosition(step*i);
        }
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "clampToCurve", this::isClampToCurve,
            "background", this::isAppliesToBackground,
            "thresholds", this::getThresholds,
            "colors", this::getColors
        );
    }

    public String toString() {
        return GenericRecordJsonWriter.marshal(this);
    }

    public ColorGradientFormatting copy()  {
        return new ColorGradientFormatting(this);
    }

    public int getDataLength() {
        int len = 6;
        for (Threshold t : thresholds) {
            len += t.getDataLength();
        }
        for (ExtendedColor c : colors) {
            len += c.getDataLength();
            len += 8;
        }
        return len;
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(0);
        out.writeByte(0);
        out.writeByte(thresholds.length);
        out.writeByte(thresholds.length);
        out.writeByte(options);

        for (ColorGradientThreshold t : thresholds) {
            t.serialize(out);
        }

        double step = 1d / (colors.length-1);
        for (int i=0; i<colors.length; i++) {
            out.writeDouble(i*step);

            ExtendedColor c = colors[i];
            c.serialize(out);
        }
    }
}
