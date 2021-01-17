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

package org.apache.poi.xddf.usermodel.chart;

import java.util.HashMap;

import org.openxmlformats.schemas.drawingml.x2006.chart.STScatterStyle;

public enum ScatterStyle {
    LINE(STScatterStyle.LINE),
    LINE_MARKER(STScatterStyle.LINE_MARKER),
    MARKER(STScatterStyle.MARKER),
    NONE(STScatterStyle.NONE),
    SMOOTH(STScatterStyle.SMOOTH),
    SMOOTH_MARKER(STScatterStyle.SMOOTH_MARKER);

    final STScatterStyle.Enum underlying;

    ScatterStyle(STScatterStyle.Enum style) {
        this.underlying = style;
    }

    private static final HashMap<STScatterStyle.Enum, ScatterStyle> reverse = new HashMap<>();
    static {
        for (ScatterStyle value : values()) {
            reverse.put(value.underlying, value);
        }
    }

    static ScatterStyle valueOf(STScatterStyle.Enum style) {
        return reverse.get(style);
    }
}
