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

package org.apache.poi.sl.usermodel;

/**
 * This interface specifies the line style of a shape
 */
public interface StrokeStyle {
    enum LineCap {
        /** Rounded ends */
        ROUND(0,1),
        /** Square protrudes by half line width */
        SQUARE(1,2),
        /** Line ends at end point*/
        FLAT(2,3);
        
        public final int nativeId;
        public final int ooxmlId;
        
        LineCap(int nativeId, int ooxmlId) {
            this.nativeId = nativeId;
            this.ooxmlId = ooxmlId;
        }

        public static LineCap fromNativeId(int nativeId) {
            for (LineCap ld : values()) {
                if (ld.nativeId == nativeId) return ld;
            }
            return null;
        }

        public static LineCap fromOoxmlId(int ooxmlId) {
            for (LineCap lc : values()) {
                if (lc.ooxmlId == ooxmlId) return lc;
            }
            return null;
        }
    }

    /**
     * The line dash with pattern.
     * The pattern is derived empirically on PowerPoint 2010 and needs to be multiplied
     * with actual line width
     */
    enum LineDash {
        /** Solid (continuous) pen - native 1 */
        SOLID(1, 1, null),
        /** square dot style - native 6 */
        DOT(6, 2, 1,1),
        /** dash style - native 7 */
        DASH(7, 3, 3,4),
        /** dash short dash - native 9*/
        DASH_DOT(9, 5, 4,3,1,3),
        /** long dash style - native 8 */
        LG_DASH(8, 4, 8,3),
        /** long dash short dash - native 10 */
        LG_DASH_DOT(10, 6, 8,3,1,3),
        /** long dash short dash short dash - native 11 */
        LG_DASH_DOT_DOT(11, 7, 8,3,1,3,1,3),
        /** PS_DASH system dash style - native 2 */
        SYS_DASH(2, 8, 2,2),
        /** PS_DOT system dash style - native 3 */
        SYS_DOT(3, 9, 1,1),
        /** PS_DASHDOT system dash style - native 4 */
        SYS_DASH_DOT(4, 10, 2,2,1,1),
        /** PS_DASHDOTDOT system dash style / native 5 */
        SYS_DASH_DOT_DOT(5, 11, 2,2,1,1,1,1);

        public final int[] pattern;
        public final int nativeId;
        public final int ooxmlId;

        LineDash(int nativeId, int ooxmlId, int... pattern) {
            this.nativeId = nativeId;
            this.ooxmlId = ooxmlId;
            this.pattern = (pattern == null || pattern.length == 0) ? null : pattern;
        }

        public static LineDash fromNativeId(int nativeId) {
            for (LineDash ld : values()) {
                if (ld.nativeId == nativeId) return ld;
            }
            return null;
        }

        public static LineDash fromOoxmlId(int ooxmlId) {
            for (LineDash ld : values()) {
                if (ld.ooxmlId == ooxmlId) return ld;
            }
            return null;
        }
    }

    enum LineCompound {
        /** Single line (of width lineWidth) - native 0 / ooxml default */
        SINGLE(0, 1),
        /** Double lines of equal width - native 1 / ooxml "dbl" */
        DOUBLE(1, 2),
        /** Double lines, one thick, one thin - native 2 / ooxml "thickThin" */
        THICK_THIN(2, 3),
        /** Double lines, reverse order - native 3 / ooxml "thinThick" */
        THIN_THICK(3, 4),
        /** Three lines, thin, thick, thin - native 4 / ooxml "tri" */
        TRIPLE(4, 5);
        
        public final int nativeId;
        public final int ooxmlId;
        
        LineCompound(int nativeId, int ooxmlId) {
            this.nativeId = nativeId;
            this.ooxmlId = ooxmlId;
        }

        public static LineCompound fromNativeId(int nativeId) {
            for (LineCompound lc : values()) {
                if (lc.nativeId == nativeId) return lc;
            }
            return null;
        }

        public static LineCompound fromOoxmlId(int ooxmlId) {
            for (LineCompound lc : values()) {
                if (lc.ooxmlId == ooxmlId) return lc;
            }
            return null;
        }
    }


    PaintStyle getPaint();
    LineCap getLineCap();
    LineDash getLineDash();
    LineCompound getLineCompound();
    double getLineWidth();
}
