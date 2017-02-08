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

/**
 * The HatchStyle Enumeration specifies the hatch pattern.
 */
public enum HwmfHatchStyle {
    /** ----- - A horizontal hatch */
    HS_HORIZONTAL(0x0000),
    /** ||||| - A vertical hatch */
    HS_VERTICAL(0x0001),
    /** \\\\\ - A 45-degree downward, left-to-right hatch. */
    HS_FDIAGONAL(0x0002),
    /** ///// - A 45-degree upward, left-to-right hatch. */
    HS_BDIAGONAL(0x0003),
    /** +++++ - A horizontal and vertical cross-hatch. */
    HS_CROSS(0x0004),
    /** xxxxx - A 45-degree crosshatch. */
    HS_DIAGCROSS(0x0005);

    int flag;
    HwmfHatchStyle(int flag) {
        this.flag = flag;
    }

    static HwmfHatchStyle valueOf(int flag) {
        for (HwmfHatchStyle hs : values()) {
            if (hs.flag == flag) return hs;
        }
        return null;
    }
}