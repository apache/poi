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

import org.openxmlformats.schemas.drawingml.x2006.main.STSchemeColorVal;

public enum SchemeColor {
    ACCENT_1(STSchemeColorVal.ACCENT_1),
    ACCENT_2(STSchemeColorVal.ACCENT_2),
    ACCENT_3(STSchemeColorVal.ACCENT_3),
    ACCENT_4(STSchemeColorVal.ACCENT_4),
    ACCENT_5(STSchemeColorVal.ACCENT_5),
    ACCENT_6(STSchemeColorVal.ACCENT_6),
    BACKGROUND_1(STSchemeColorVal.BG_1),
    BACKGROUND_2(STSchemeColorVal.BG_2),
    DARK_1(STSchemeColorVal.DK_1),
    DARK_2(STSchemeColorVal.DK_2),
    FOLLOWED_LINK(STSchemeColorVal.FOL_HLINK),
    LINK(STSchemeColorVal.HLINK),
    LIGHT_1(STSchemeColorVal.LT_1),
    LIGHT_2(STSchemeColorVal.LT_2),
    PLACEHOLDER(STSchemeColorVal.PH_CLR),
    TEXT_1(STSchemeColorVal.TX_1),
    TEXT_2(STSchemeColorVal.TX_2);

    final STSchemeColorVal.Enum underlying;

    SchemeColor(STSchemeColorVal.Enum color) {
        this.underlying = color;
    }

    private final static HashMap<STSchemeColorVal.Enum, SchemeColor> reverse = new HashMap<STSchemeColorVal.Enum, SchemeColor>();
    static {
        for (SchemeColor value : values()) {
            reverse.put(value.underlying, value);
        }
    }

    static SchemeColor valueOf(STSchemeColorVal.Enum color) {
        return reverse.get(color);
    }
}
