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

package org.apache.poi.hwmf.record;

import java.awt.BasicStroke;

import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;

/**
 * The 16-bit PenStyle Enumeration is used to specify different types of pens
 * that can be used in graphics operations.
 * 
 * Various styles can be combined by using a logical OR statement, one from
 * each subsection of Style, EndCap, Join, and Type (Cosmetic).
 * 
 * The defaults in case the other values of the subsection aren't set are
 * solid, round end caps, round joins and cosmetic type.
 */
public class HwmfPenStyle implements Cloneable {
    public enum HwmfLineCap {
        /** Rounded ends */
        ROUND(0, BasicStroke.CAP_ROUND),
        /** Square protrudes by half line width */
        SQUARE(1, BasicStroke.CAP_SQUARE),
        /** Line ends at end point*/
        FLAT(2, BasicStroke.CAP_BUTT);        

        public final int wmfFlag;
        public final int awtFlag;
        HwmfLineCap(int wmfFlag, int awtFlag) {
            this.wmfFlag = wmfFlag;
            this.awtFlag = awtFlag;
        }

        static HwmfLineCap valueOf(int wmfFlag) {
            for (HwmfLineCap hs : values()) {
                if (hs.wmfFlag == wmfFlag) return hs;
            }
            return null;
        }    
    }
    
    public enum HwmfLineJoin {
        /**Line joins are round. */
        ROUND(0, BasicStroke.JOIN_ROUND),
        /** Line joins are beveled. */
        BEVEL(1, BasicStroke.JOIN_BEVEL),
        /**
         * Line joins are mitered when they are within the current limit set by the
         * SETMITERLIMIT META_ESCAPE record. A join is beveled when it would exceed the limit
         */
        MITER(2, BasicStroke.JOIN_MITER);

        public final int wmfFlag;
        public final int awtFlag;
        HwmfLineJoin(int wmfFlag, int awtFlag) {
            this.wmfFlag = wmfFlag;
            this.awtFlag = awtFlag;
        }

        static HwmfLineJoin valueOf(int wmfFlag) {
            for (HwmfLineJoin hs : values()) {
                if (hs.wmfFlag == wmfFlag) return hs;
            }
            return null;
        }    
    }
    
    public enum HwmfLineDash {
        /**
         * The pen is solid.
         */
        SOLID(0x0000, null),
        /**
         * The pen is dashed. (-----) 
         */
        DASH(0x0001, 10, 8),
        /**
         * The pen is dotted. (.....)
         */
        DOT(0x0002, 2, 4),
        /**
         * The pen has alternating dashes and dots. (_._._._)
         */
        DASHDOT(0x0003, 10, 8, 2, 8),
        /**
         * The pen has dashes and double dots. (_.._.._)
         */
        DASHDOTDOT(0x0004, 10, 4, 2, 4, 2, 4),
        /**
         * The pen is invisible.
         */
        NULL(0x0005, null),
        /**
         * The pen is solid. When this pen is used in any drawing record that takes a
         * bounding rectangle, the dimensions of the figure are shrunk so that it fits
         * entirely in the bounding rectangle, taking into account the width of the pen.
         */
        INSIDEFRAME(0x0006, null),
        /**
         * The pen uses a styling array supplied by the user.
         * (this is currently not supported and drawn as solid ... no idea where the user
         * styling is supposed to come from ...)
         */
        USERSTYLE(0x0007, null);
        

        public final int wmfFlag;
        public final float[] dashes;
        HwmfLineDash(int wmfFlag, float... dashes) {
            this.wmfFlag = wmfFlag;
            this.dashes = dashes;
        }

        static HwmfLineDash valueOf(int wmfFlag) {
            for (HwmfLineDash hs : values()) {
                if (hs.wmfFlag == wmfFlag) return hs;
            }
            return null;
        }    
    }
    
    private static final BitField SUBSECTION_DASH      = BitFieldFactory.getInstance(0x0007);
    private static final BitField SUBSECTION_ALTERNATE = BitFieldFactory.getInstance(0x0008);
    private static final BitField SUBSECTION_ENDCAP    = BitFieldFactory.getInstance(0x0300);
    private static final BitField SUBSECTION_JOIN      = BitFieldFactory.getInstance(0x3000);
    
    private int flag;
    
    public static HwmfPenStyle valueOf(int flag) {
        HwmfPenStyle ps = new HwmfPenStyle();
        ps.flag = flag;
        return ps;
    }
    
    public HwmfLineCap getLineCap() {
        return HwmfLineCap.valueOf(SUBSECTION_ENDCAP.getValue(flag));
    }

    public HwmfLineJoin getLineJoin() {
        return HwmfLineJoin.valueOf(SUBSECTION_JOIN.getValue(flag));
    }
    
    public HwmfLineDash getLineDash() {
        return HwmfLineDash.valueOf(SUBSECTION_DASH.getValue(flag));
    }
    

    /**
     * The pen sets every other pixel (this style is applicable only for cosmetic pens).
     */
    public boolean isAlternateDash() {
        return SUBSECTION_ALTERNATE.isSet(flag);
    }


    /**
     * Creates a new object of the same class and with the
     * same contents as this object.
     * @return     a clone of this instance.
     * @exception  OutOfMemoryError            if there is not enough memory.
     * @see        java.lang.Cloneable
     */
    @Override
    public HwmfPenStyle clone() {
        try {
            return (HwmfPenStyle)super.clone();
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError();
        }
    }
}
