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

import org.openxmlformats.schemas.drawingml.x2006.chart.STMarkerStyle;

public enum MarkerStyle {
    CIRCLE(STMarkerStyle.CIRCLE),
    DASH(STMarkerStyle.DASH),
    DIAMOND(STMarkerStyle.DIAMOND),
    DOT(STMarkerStyle.DOT),
    NONE(STMarkerStyle.NONE),
    PICTURE(STMarkerStyle.PICTURE),
    PLUS(STMarkerStyle.PLUS),
    SQUARE(STMarkerStyle.SQUARE),
    STAR(STMarkerStyle.STAR),
    TRIANGLE(STMarkerStyle.TRIANGLE),
    X(STMarkerStyle.X);

    final STMarkerStyle.Enum underlying;

    MarkerStyle(STMarkerStyle.Enum style) {
        this.underlying = style;
    }

    private static final HashMap<STMarkerStyle.Enum, MarkerStyle> reverse = new HashMap<>();
    static {
        for (MarkerStyle value : values()) {
            reverse.put(value.underlying, value);
        }
    }

    static MarkerStyle valueOf(STMarkerStyle.Enum style) {
        return reverse.get(style);
    }
}
