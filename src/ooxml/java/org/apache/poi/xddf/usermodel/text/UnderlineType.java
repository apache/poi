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

import org.openxmlformats.schemas.drawingml.x2006.main.STTextUnderlineType;

public enum UnderlineType {
    DASH(STTextUnderlineType.DASH),
    DASH_HEAVY(STTextUnderlineType.DASH_HEAVY),
    DASH_LONG(STTextUnderlineType.DASH_LONG),
    DASH_LONG_HEAVY(STTextUnderlineType.DASH_LONG_HEAVY),
    DOUBLE(STTextUnderlineType.DBL),
    DOT_DASH(STTextUnderlineType.DOT_DASH),
    DOT_DASH_HEAVY(STTextUnderlineType.DOT_DASH_HEAVY),
    DOT_DOT_DASH(STTextUnderlineType.DOT_DOT_DASH),
    DOT_DOT_DASH_HEAVY(STTextUnderlineType.DOT_DOT_DASH_HEAVY),
    DOTTED(STTextUnderlineType.DOTTED),
    DOTTED_HEAVY(STTextUnderlineType.DOTTED_HEAVY),
    HEAVY(STTextUnderlineType.HEAVY),
    NONE(STTextUnderlineType.NONE),
    SINGLE(STTextUnderlineType.SNG),
    WAVY(STTextUnderlineType.WAVY),
    WAVY_DOUBLE(STTextUnderlineType.WAVY_DBL),
    WAVY_HEAVY(STTextUnderlineType.WAVY_HEAVY),
    WORDS(STTextUnderlineType.WORDS);

    final STTextUnderlineType.Enum underlying;

    UnderlineType(STTextUnderlineType.Enum underline) {
        this.underlying = underline;
    }

    private static final HashMap<STTextUnderlineType.Enum, UnderlineType> reverse = new HashMap<>();
    static {
        for (UnderlineType value : values()) {
            reverse.put(value.underlying, value);
        }
    }

    static UnderlineType valueOf(STTextUnderlineType.Enum underline) {
        return reverse.get(underline);
    }
}
