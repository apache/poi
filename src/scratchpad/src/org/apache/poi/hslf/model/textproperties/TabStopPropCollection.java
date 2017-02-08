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

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianConsts;

/**
 * Container for tabstop lists
 */
public class TabStopPropCollection extends TextProp {
    public enum TabStopType {
        LEFT(0), CENTER(1), RIGHT(2), DECIMAL(3);
        private final int val;
        TabStopType(int val) {
            this.val = val;
        }
        public static TabStopType fromRecordVal(int val)  {
            for (TabStopType tst : values()) {
                if (tst.val == val) return tst;
            }
            return LEFT;
        }
    }

    public static class TabStop {
        /**
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

        public TabStop(int position, TabStopType type) {
            this.position = position;
            this.type = type;
        }
        
        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public TabStopType getType() {
            return type;
        }

        public void setType(TabStopType type) {
            this.type = type;
        }
    }
    
    private List<TabStop> tabStops = new ArrayList<TabStop>();
    
    public TabStopPropCollection() {
        super(0, 0x100000, "tabStops");
    }
    
    /**
     * Parses the tabstops from TxMasterStyle record
     *
     * @param data the data stream
     * @param offset the offset within the data
     */
    public void parseProperty(byte data[], int offset) {
        int count = LittleEndian.getUShort(data, offset);
        int off = offset + LittleEndianConsts.SHORT_SIZE;
        for (int i=0; i<count; i++) {
            int position = LittleEndian.getShort(data, off);
            off += LittleEndianConsts.SHORT_SIZE;
            int recVal = LittleEndian.getShort(data, off);
            TabStopType type = TabStopType.fromRecordVal(recVal);
            off += LittleEndianConsts.SHORT_SIZE;
            tabStops.add(new TabStop(position, type));
            
        }
    }
    
    @Override
    public int getSize() {
        return LittleEndianConsts.SHORT_SIZE + tabStops.size()*LittleEndianConsts.INT_SIZE;
    }
    
    @Override
    public TabStopPropCollection clone() {
        TabStopPropCollection other = (TabStopPropCollection)super.clone();
        other.tabStops = new ArrayList<TabStop>();
        for (TabStop ts : tabStops) {
            TabStop tso = new TabStop(ts.getPosition(), ts.getType());
            other.tabStops.add(tso);
        }
        return other;
    }
}
