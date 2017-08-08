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

import org.openxmlformats.schemas.drawingml.x2006.chart.STGrouping;

public enum Grouping {
    STANDARD(STGrouping.STANDARD),
    STACKED(STGrouping.STACKED),
    PERCENT_STACKED(STGrouping.PERCENT_STACKED);

    final STGrouping.Enum underlying;

    Grouping(STGrouping.Enum grouping) {
        this.underlying = grouping;
    }

    private final static HashMap<STGrouping.Enum, Grouping> reverse = new HashMap<STGrouping.Enum, Grouping>();
    static {
        reverse.put(STGrouping.STANDARD, Grouping.STANDARD);
        reverse.put(STGrouping.STACKED, Grouping.STACKED);
        reverse.put(STGrouping.PERCENT_STACKED, Grouping.PERCENT_STACKED);
    }

    static Grouping valueOf(STGrouping.Enum grouping) {
        return reverse.get(grouping);
    }
}
