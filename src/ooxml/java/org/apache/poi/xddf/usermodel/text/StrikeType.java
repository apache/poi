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

package org.apache.poi.xddf.usermodel.text;

import java.util.HashMap;

import org.openxmlformats.schemas.drawingml.x2006.main.STTextStrikeType;

public enum StrikeType {
    DOUBLE_STRIKE(STTextStrikeType.DBL_STRIKE),
    NO_STRIKE(STTextStrikeType.NO_STRIKE),
    SINGLE_STRIKE(STTextStrikeType.SNG_STRIKE);

    final STTextStrikeType.Enum underlying;

    StrikeType(STTextStrikeType.Enum strike) {
        this.underlying = strike;
    }

    private static final HashMap<STTextStrikeType.Enum, StrikeType> reverse = new HashMap<>();
    static {
        for (StrikeType value : values()) {
            reverse.put(value.underlying, value);
        }
    }

    static StrikeType valueOf(STTextStrikeType.Enum strike) {
        return reverse.get(strike);
    }
}
