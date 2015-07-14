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
    class IconSet {
        public final int id;
        public final String name;
        public String toString() {
            return id + " - " + (name==null?"default":name);
        }
        private IconSet(int id, String name) {
            this.id = id; this.name = name;
        }
    }
    /** Green Up / Yellow Side / Red Down arrows */
    static final IconSet GYR_3_ARROWS = new IconSet(0, "3Arrows");
    /** Grey Up / Side / Down arrows */
    static final IconSet GREY_3_ARROWS = new IconSet(1, "3ArrowsGray");
    /** Green / Yellow / Red flags */
    static final IconSet GYR_3_FLAGS = new IconSet(2, "3Flags");
    /** Green / Yellow / Red traffic lights (no background) */
    static final IconSet GYR_3_TRAFFIC_LIGHTS = new IconSet(3, null);
    /** Green Circle / Yellow Triangle / Red Diamond */ 
    static final IconSet GYR_3_SHAPES = new IconSet(4, "3Signs");
    /** Green / Yellow / Red traffic lights on a black square background */
    static final IconSet GYR_3_TRAFFIC_LIGHTS_BOX = new IconSet(5, "3TrafficLights2");
    /** Green Tick / Yellow ! / Red Cross on a circle background */
    static final IconSet GYR_3_SYMBOLS_CIRCLE = new IconSet(6, "3Symbols");
    /** Green Tick / Yellow ! / Red Cross (no background) */
    static final IconSet GYR_3_SYMBOLS = new IconSet(7, "3Symbols2");
    /** Green Up / Yellow NE / Yellow SE / Red Down arrows */
    static final IconSet GYR_4_ARROWS = new IconSet(8, "4Arrows");
    /** Grey Up / NE / SE / Down arrows */
    static final IconSet GREY_4_ARROWS = new IconSet(9, "4ArrowsGray");
    /** Red / Light Red / Grey / Black traffic lights */
    static final IconSet RB_4_TRAFFIC_LIGHTS = new IconSet(0xA, "4RedToBlack");
    static final IconSet RATINGS_4 = new IconSet(0xB, "4Rating");
    /** Green / Yellow / Red / Black traffic lights */
    static final IconSet GYRB_4_TRAFFIC_LIGHTS = new IconSet(0xC, "4TrafficLights");
    static final IconSet GYYYR_5_ARROWS = new IconSet(0xD, "5Arrows");
    static final IconSet GREY_5_ARROWS = new IconSet(0xE, "5ArrowsGray");
    static final IconSet RATINGS_5 = new IconSet(0xF, "5Rating");
    static final IconSet QUARTERS_5 = new IconSet(0x10, "5Quarters");
}
