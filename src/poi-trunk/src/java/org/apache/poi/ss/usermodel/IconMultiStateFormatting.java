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
 * High level representation for the Icon / Multi-State Formatting 
 *  component of Conditional Formatting settings
 */
public interface IconMultiStateFormatting {
    public enum IconSet {
        /** Green Up / Yellow Side / Red Down arrows */
        GYR_3_ARROW(0, 3, "3Arrows"),
        /** Grey Up / Side / Down arrows */
        GREY_3_ARROWS(1, 3, "3ArrowsGray"),
        /** Green / Yellow / Red flags */
        GYR_3_FLAGS(2, 3, "3Flags"),
        /** Green / Yellow / Red traffic lights (no background). Default */
        GYR_3_TRAFFIC_LIGHTS(3, 3, "3TrafficLights1"),
        /** Green / Yellow / Red traffic lights on a black square background. 
         * Note, MS-XLS docs v20141018 say this is id=5 but seems to be id=4 */
        GYR_3_TRAFFIC_LIGHTS_BOX(4, 3, "3TrafficLights2"),
        /** Green Circle / Yellow Triangle / Red Diamond.
         * Note, MS-XLS docs v20141018 say this is id=4 but seems to be id=5 */ 
        GYR_3_SHAPES(5, 3, "3Signs"),
        /** Green Tick / Yellow ! / Red Cross on a circle background */
        GYR_3_SYMBOLS_CIRCLE(6, 3, "3Symbols"),
        /** Green Tick / Yellow ! / Red Cross (no background) */
        GYR_3_SYMBOLS(7, 3, "3Symbols2"),
        /** Green Up / Yellow NE / Yellow SE / Red Down arrows */
        GYR_4_ARROWS(8, 4, "4Arrows"),
        /** Grey Up / NE / SE / Down arrows */
        GREY_4_ARROWS(9, 4, "4ArrowsGray"),
        /** Red / Light Red / Grey / Black traffic lights */
        RB_4_TRAFFIC_LIGHTS(0xA, 4, "4RedToBlack"),
        RATINGS_4(0xB, 4, "4Rating"),
        /** Green / Yellow / Red / Black traffic lights */
        GYRB_4_TRAFFIC_LIGHTS(0xC, 4, "4TrafficLights"),
        GYYYR_5_ARROWS(0xD, 5, "5Arrows"),
        GREY_5_ARROWS(0xE, 5, "5ArrowsGray"),
        RATINGS_5(0xF, 5, "5Rating"),
        QUARTERS_5(0x10, 5, "5Quarters");
        
        protected static final IconSet DEFAULT_ICONSET = IconSet.GYR_3_TRAFFIC_LIGHTS;
        
        /** Numeric ID of the icon set */
        public final int id;
        /** How many icons in the set */
        public final int num;
        /** Name (system) of the set */
        public final String name;
        
        public String toString() {
            return id + " - " + name;
        }
        
        public static IconSet byId(int id) {
            return values()[id];
        }
        public static IconSet byName(String name) {
            for (IconSet set : values()) {
                if (set.name.equals(name)) return set;
            }
            return null;
        }
        
        private IconSet(int id, int num, String name) {
            this.id = id; this.num = num; this.name = name;
        }
    }
    
    /**
     * Get the Icon Set used
     */
    IconSet getIconSet();
    
    /**
     * Changes the Icon Set used
     * 
     * <p>If the new Icon Set has a different number of
     *  icons to the old one, you <em>must</em> update the
     *  thresholds before saving!</p>
     */
    void setIconSet(IconSet set);
    
    /**
     * Should Icon + Value be displayed, or only the Icon?
     */
    boolean isIconOnly();
    /**
     * Control if only the Icon is shown, or Icon + Value
     */
    void setIconOnly(boolean only);
    
    boolean isReversed();
    void setReversed(boolean reversed);
    
    /**
     * Gets the list of thresholds
     */
    ConditionalFormattingThreshold[] getThresholds();
    /**
     * Sets the of thresholds. The number must match
     *  {@link IconSet#num} for the current {@link #getIconSet()}
     */
    void setThresholds(ConditionalFormattingThreshold[] thresholds);
    /**
     * Creates a new, empty Threshold
     */
    ConditionalFormattingThreshold createThreshold();
}
