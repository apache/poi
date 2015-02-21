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

public interface StrokeStyle {
    enum LineCap {
        /** Rounded ends */
        ROUND,
        /** Square protrudes by half line width */
        SQUARE,
        /** Line ends at end point*/
        FLAT;
    }

    /**
     * The line dash with pattern.
     * The pattern is derived empirically on PowerPoint 2010 and needs to be multiplied
     * with actual line width
     */
    enum LineDash {
        SOLID(1),
        DOT(1,1),
        DASH(3,4),
        LG_DASH(8,3),
        DASH_DOT(4,3,1,3),
        LG_DASH_DOT(8,3,1,3),
        LG_DASH_DOT_DOT(8,3,1,3,1,3),
        SYS_DASH(2,2),
        SYS_DOT(1,1),
        SYS_DASH_DOT,
        SYS_DASH_DOT_DOT;

        public int pattern[];
        
        LineDash(int... pattern) {
            this.pattern = (pattern == null || pattern.length == 0) ? new int[]{1} : pattern;
        }
    }
    
    PaintStyle getPaint();
    LineCap getLineCap();
    LineDash getLineDash();
    double getLineWidth();
}
