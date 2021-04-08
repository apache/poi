/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.ss.usermodel;


/**
 * The Threshold / CFVO / Conditional Formatting Value Object.
 * <p>This defines how to calculate the ranges for a conditional
 *  formatting rule, eg which values get a Green Traffic Light
 *  icon and which Yellow or Red.</p>
 */
public interface ConditionalFormattingThreshold {
    public enum RangeType {
        /** Number / Parameter */
        NUMBER(1, "num"),
        /** The minimum value from the range */
        MIN(2, "min"),
        /** The maximum value from the range */
        MAX(3, "max"),
        /** Percent of the way from the mi to the max value in the range */
        PERCENT(4, "percent"),
        /** The minimum value of the cell that is in X percentile of the range */
        PERCENTILE(5, "percentile"),
        UNALLOCATED(6, null),
        /** Formula result */
        FORMULA(7, "formula");
        
        /** Numeric ID of the type */
        public final int id;
        /** Name (system) of the type */
        public final String name;
        
        public String toString() {
            return id + " - " + name;
        }
        
        public static RangeType byId(int id) {
            return values()[id-1]; // 1-based IDs
        }
        public static RangeType byName(String name) {
            for (RangeType t : values()) {
                if (t.name.equals(name)) return t;
            }
            return null;
        }
        
        private RangeType(int id, String name) {
            this.id = id; this.name = name;
        }
    }
    
    /**
     * Get the Range Type used
     */
    RangeType getRangeType();
    
    /**
     * Changes the Range Type used
     * 
     * <p>If you change the range type, you need to
     *  ensure that the Formula and Value parameters
     *  are compatible with it before saving</p>
     */
    void setRangeType(RangeType type);
    
    /**
     * Formula to use to calculate the threshold,
     *  or <code>null</code> if no formula 
     */
    String getFormula();

    /**
     * Sets the formula used to calculate the threshold,
     *  or unsets it if <code>null</code> is given.
     */
    void setFormula(String formula);
    
    /**
     * Gets the value used for the threshold, or 
     *  <code>null</code> if there isn't one.
     */
    Double getValue();
    
    /**
     * Sets the value used for the threshold. 
     * <p>If the type is {@link RangeType#PERCENT} or 
     *  {@link RangeType#PERCENTILE} it must be between 0 and 100.
     * <p>If the type is {@link RangeType#MIN} or {@link RangeType#MAX}
     *  or {@link RangeType#FORMULA} it shouldn't be set.
     * <p>Use <code>null</code> to unset
     */
    void setValue(Double value);
}
