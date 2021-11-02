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

import org.openxmlformats.schemas.drawingml.x2006.main.STTextFontAlignType;

public enum FontAlignment {
    AUTOMATIC(STTextFontAlignType.AUTO),
    BOTTOM(STTextFontAlignType.B),
    BASELINE(STTextFontAlignType.BASE),
    CENTER(STTextFontAlignType.CTR),
    TOP(STTextFontAlignType.T);

    final STTextFontAlignType.Enum underlying;

    FontAlignment(STTextFontAlignType.Enum align) {
        this.underlying = align;
    }

    private static final HashMap<STTextFontAlignType.Enum, FontAlignment> reverse = new HashMap<>();
    static {
        for (FontAlignment value : values()) {
            reverse.put(value.underlying, value);
        }
    }

    static FontAlignment valueOf(STTextFontAlignType.Enum align) {
        return reverse.get(align);
    }
}
