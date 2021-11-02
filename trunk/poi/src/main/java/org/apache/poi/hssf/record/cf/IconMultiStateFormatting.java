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
import org.apache.poi.ss.usermodel.IconMultiStateFormatting.IconSet;
import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.GenericRecordJsonWriter;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndianInput;
import org.apache.poi.util.LittleEndianOutput;

import static org.apache.logging.log4j.util.Unbox.box;

/**
 * Icon / Multi-State Conditional Formatting Rule Record.
 */
public final class IconMultiStateFormatting implements Duplicatable, GenericRecord {
    private static final Logger LOG = LogManager.getLogger(IconMultiStateFormatting.class);

    private static BitField ICON_ONLY = BitFieldFactory.getInstance(0x01);
    private static BitField REVERSED = BitFieldFactory.getInstance(0x04);

    private IconSet iconSet;
    private byte options;
    private Threshold[] thresholds;

    public IconMultiStateFormatting() {
        iconSet = IconSet.GYR_3_TRAFFIC_LIGHTS;
        options = 0;
        thresholds = new Threshold[iconSet.num];
    }

    public IconMultiStateFormatting(IconMultiStateFormatting other) {
        iconSet = other.iconSet;
        options = other.options;
        if (other.thresholds != null) {
            thresholds = Stream.of(other.thresholds).map(Threshold::copy).toArray(Threshold[]::new);
        }
    }

    public IconMultiStateFormatting(LittleEndianInput in) {
        in.readShort(); // Ignored
        in.readByte();  // Reserved
        int num = in.readByte();
        int set = in.readByte();
        iconSet = IconSet.byId(set);
        if (iconSet.num != num) {
            LOG.atWarn().log("Inconsistent Icon Set definition, found {} but defined as {} entries", iconSet, box(num));
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
        return ICON_ONLY.isSet(options);
    }
    public void setIconOnly(boolean only) {
        options = ICON_ONLY.setByteBoolean(options, only);
    }

    public boolean isReversed() {
        return REVERSED.isSet(options);
    }

    public void setReversed(boolean rev) {
        options = REVERSED.setByteBoolean(options, rev);
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "iconSet", this::getIconSet,
            "iconOnly", this::isIconOnly,
            "reversed", this::isReversed,
            "thresholds", this::getThresholds
        );
    }

    public String toString() {
        return GenericRecordJsonWriter.marshal(this);
    }

    @Override
    public IconMultiStateFormatting copy()  {
        return new IconMultiStateFormatting(this);
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
