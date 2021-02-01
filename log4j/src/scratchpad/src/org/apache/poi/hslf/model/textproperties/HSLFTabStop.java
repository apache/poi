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

package org.apache.poi.hslf.model.textproperties;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import org.apache.poi.common.Duplicatable;
import org.apache.poi.common.usermodel.GenericRecord;
import org.apache.poi.sl.usermodel.TabStop;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.Internal;
import org.apache.poi.util.Units;

@Internal
public class HSLFTabStop implements TabStop, Duplicatable, GenericRecord {
    /**
     * A signed integer that specifies an offset, in master units, of the tab stop.
     *
     * If the TextPFException record that contains this TabStop structure also contains a
     * leftMargin, then the value of position is relative to the left margin of the paragraph;
     * otherwise, the value is relative to the left side of the paragraph.
     *
     * If a TextRuler record contains this TabStop structure, the value is relative to the
     * left side of the text ruler.
     */
    private int position;

    /**
     * A enumeration that specifies how text aligns at the tab stop.
     */
    private TabStopType type;

    public HSLFTabStop(int position, TabStopType type) {
        this.position = position;
        this.type = type;
    }

    public HSLFTabStop(HSLFTabStop other) {
        position = other.position;
        type = other.type;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(final int position) {
        this.position = position;
    }

    @Override
    public double getPositionInPoints() {
        return Units.masterToPoints(getPosition());
    }

    @Override
    public void setPositionInPoints(final double points) {
        setPosition(Units.pointsToMaster(points));
    }

    @Override
    public TabStopType getType() {
        return type;
    }

    @Override
    public void setType(TabStopType type) {
        this.type = type;
    }

    @Override
    public HSLFTabStop copy() {
        return new HSLFTabStop(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, type);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof HSLFTabStop)) {
            return false;
        }
        HSLFTabStop other = (HSLFTabStop) obj;
        if (position != other.position) {
            return false;
        }
        if (type != other.type) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return type + " @ " + position;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "type", this::getType,
            "position", this::getPosition
        );
    }
}
