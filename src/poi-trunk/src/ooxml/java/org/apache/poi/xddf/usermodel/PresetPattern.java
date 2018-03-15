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

package org.apache.poi.xddf.usermodel;

import java.util.HashMap;

import org.openxmlformats.schemas.drawingml.x2006.main.STPresetPatternVal;

public enum PresetPattern {
    CROSS(STPresetPatternVal.CROSS),
    DASH_DOWNWARD_DIAGONAL(STPresetPatternVal.DASH_DN_DIAG),
    DASH_HORIZONTAL(STPresetPatternVal.DASH_HORZ),
    DASH_UPWARD_DIAGONAL(STPresetPatternVal.DASH_UP_DIAG),
    DASH_VERTICAL(STPresetPatternVal.DASH_VERT),
    DIAGONAL_BRICK(STPresetPatternVal.DIAG_BRICK),
    DIAGONAL_CROSS(STPresetPatternVal.DIAG_CROSS),
    DIVOT(STPresetPatternVal.DIVOT),
    DARK_DOWNWARD_DIAGONAL(STPresetPatternVal.DK_DN_DIAG),
    DARK_HORIZONTAL(STPresetPatternVal.DK_HORZ),
    DARK_UPWARD_DIAGONAL(STPresetPatternVal.DK_UP_DIAG),
    DARK_VERTICAL(STPresetPatternVal.DK_VERT),
    DOWNWARD_DIAGONAL(STPresetPatternVal.DN_DIAG),
    DOTTED_DIAMOND(STPresetPatternVal.DOT_DMND),
    DOTTED_GRID(STPresetPatternVal.DOT_GRID),
    HORIZONTAL(STPresetPatternVal.HORZ),
    HORIZONTAL_BRICK(STPresetPatternVal.HORZ_BRICK),
    LARGE_CHECKER_BOARD(STPresetPatternVal.LG_CHECK),
    LARGE_CONFETTI(STPresetPatternVal.LG_CONFETTI),
    LARGE_GRID(STPresetPatternVal.LG_GRID),
    LIGHT_DOWNWARD_DIAGONAL(STPresetPatternVal.LT_DN_DIAG),
    LIGHT_HORIZONTAL(STPresetPatternVal.LT_HORZ),
    LIGHT_UPWARD_DIAGONAL(STPresetPatternVal.LT_UP_DIAG),
    LIGHT_VERTICAL(STPresetPatternVal.LT_VERT),
    NARROW_HORIZONTAL(STPresetPatternVal.NAR_HORZ),
    NARROW_VERTICAL(STPresetPatternVal.NAR_VERT),
    OPEN_DIAMOND(STPresetPatternVal.OPEN_DMND),
    PERCENT_5(STPresetPatternVal.PCT_5),
    PERCENT_10(STPresetPatternVal.PCT_10),
    PERCENT_20(STPresetPatternVal.PCT_20),
    PERCENT_25(STPresetPatternVal.PCT_25),
    PERCENT_30(STPresetPatternVal.PCT_30),
    PERCENT_40(STPresetPatternVal.PCT_40),
    PERCENT_50(STPresetPatternVal.PCT_50),
    PERCENT_60(STPresetPatternVal.PCT_60),
    PERCENT_70(STPresetPatternVal.PCT_70),
    PERCENT_75(STPresetPatternVal.PCT_75),
    PERCENT_80(STPresetPatternVal.PCT_80),
    PERCENT_90(STPresetPatternVal.PCT_90),
    PLAID(STPresetPatternVal.PLAID),
    SHINGLE(STPresetPatternVal.SHINGLE),
    SMALL_CHECKER_BOARD(STPresetPatternVal.SM_CHECK),
    SMALL_CONFETTI(STPresetPatternVal.SM_CONFETTI),
    SMALL_GRID(STPresetPatternVal.SM_GRID),
    SOLID_DIAMOND(STPresetPatternVal.SOLID_DMND),
    SPHERE(STPresetPatternVal.SPHERE),
    TRELLIS(STPresetPatternVal.TRELLIS),
    UPWARD_DIAGONAL(STPresetPatternVal.UP_DIAG),
    VERTICAL(STPresetPatternVal.VERT),
    WAVE(STPresetPatternVal.WAVE),
    WEAVE(STPresetPatternVal.WEAVE),
    WIDE_DOWNWARD_DIAGONAL(STPresetPatternVal.WD_DN_DIAG),
    WIDE_UPWARD_DIAGONAL(STPresetPatternVal.WD_UP_DIAG),
    ZIG_ZAG(STPresetPatternVal.ZIG_ZAG);

    final STPresetPatternVal.Enum underlying;

    PresetPattern(STPresetPatternVal.Enum pattern) {
        this.underlying = pattern;
    }

    private final static HashMap<STPresetPatternVal.Enum, PresetPattern> reverse = new HashMap<STPresetPatternVal.Enum, PresetPattern>();
    static {
        for (PresetPattern value : values()) {
            reverse.put(value.underlying, value);
        }
    }

    static PresetPattern valueOf(STPresetPatternVal.Enum pattern) {
        return reverse.get(pattern);
    }
}
