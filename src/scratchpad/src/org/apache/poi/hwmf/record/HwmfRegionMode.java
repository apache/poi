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

import org.apache.poi.hemf.record.emf.HemfFill;

public enum HwmfRegionMode {
    /**
     * The new clipping region includes the intersection (overlapping areas)
     * of the current clipping region and the current path (or new region).
     */
    RGN_AND(0x01),
    /**
     * The new clipping region includes the union (combined areas)
     * of the current clipping region and the current path (or new region).
     */
    RGN_OR(0x02),
    /**
     * The new clipping region includes the union of the current clipping region
     * and the current path (or new region) but without the overlapping areas
     */
    RGN_XOR(0x03),
    /**
     * The new clipping region includes the areas of the current clipping region
     * with those of the current path (or new region) excluded.
     */
    RGN_DIFF(0x04),
    /**
     * The new clipping region is the current path (or the new region).
     */
    RGN_COPY(0x05);

    int flag;
    HwmfRegionMode(int flag) {
        this.flag = flag;
    }

    public static HwmfRegionMode valueOf(int flag) {
        for (HwmfRegionMode rm : values()) {
            if (rm.flag == flag) return rm;
        }
        return null;
    }
}
