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

import org.openxmlformats.schemas.drawingml.x2006.chart.STCrossBetween;

public enum AxisCrossBetween {
    BETWEEN(STCrossBetween.BETWEEN),
    MIDPOINT_CATEGORY(STCrossBetween.MID_CAT);

    final STCrossBetween.Enum underlying;

    AxisCrossBetween(STCrossBetween.Enum crossBetween) {
        this.underlying = crossBetween;
    }

    private final static HashMap<STCrossBetween.Enum, AxisCrossBetween> reverse = new HashMap<STCrossBetween.Enum, AxisCrossBetween>();
    static {
        for (AxisCrossBetween value : values()) {
            reverse.put(value.underlying, value);
        }
    }

    static AxisCrossBetween valueOf(STCrossBetween.Enum crossBetween) {
        return reverse.get(crossBetween);
    }
}
