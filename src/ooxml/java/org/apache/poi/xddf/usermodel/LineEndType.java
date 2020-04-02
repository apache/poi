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

import org.openxmlformats.schemas.drawingml.x2006.main.STLineEndType;

public enum LineEndType {
    ARROW(STLineEndType.ARROW),
    DIAMOND(STLineEndType.DIAMOND),
    NONE(STLineEndType.NONE),
    OVAL(STLineEndType.OVAL),
    STEALTH(STLineEndType.STEALTH),
    TRIANGLE(STLineEndType.TRIANGLE);

    final STLineEndType.Enum underlying;

    LineEndType(STLineEndType.Enum lineEnd) {
        this.underlying = lineEnd;
    }

    private final static HashMap<STLineEndType.Enum, LineEndType> reverse = new HashMap<>();
    static {
        for (LineEndType value : values()) {
            reverse.put(value.underlying, value);
        }
    }

    static LineEndType valueOf(STLineEndType.Enum LineEndWidth) {
        return reverse.get(LineEndWidth);
    }
}
