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

import org.apache.poi.common.Duplicatable;
import org.apache.poi.common.usermodel.GenericRecord;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndianInput;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Color Gradient / Color Scale specific Threshold / value (CFVO),
 *  for changes in Conditional Formatting
 */
public final class ColorGradientThreshold extends Threshold implements Duplicatable, GenericRecord {
    private double position;

    public ColorGradientThreshold() {
        position = 0d;
    }

    public ColorGradientThreshold(ColorGradientThreshold other) {
        super(other);
        position = other.position;
    }

    /** Creates new Color Gradient Threshold */
    public ColorGradientThreshold(LittleEndianInput in) {
        super(in);
        position = in.readDouble();
    }

    public double getPosition() {
        return position;
    }
    public void setPosition(double position) {
        this.position = position;
    }

    public int getDataLength() {
        return super.getDataLength() + 8;
    }

    @Override
    public ColorGradientThreshold copy() {
      return new ColorGradientThreshold(this);
    }

    public void serialize(LittleEndianOutput out) {
        super.serialize(out);
        out.writeDouble(position);
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties("position", this::getPosition);
    }
}
