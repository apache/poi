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

import org.openxmlformats.schemas.drawingml.x2006.main.STSystemColorVal;

public enum SystemColor {
    ACTIVE_BORDER(STSystemColorVal.ACTIVE_BORDER),
    ACTIVE_CAPTION(STSystemColorVal.ACTIVE_CAPTION),
    APPLICATION_WORKSPACE(STSystemColorVal.APP_WORKSPACE),
    BACKGROUND(STSystemColorVal.BACKGROUND),
    BUTTON_FACE(STSystemColorVal.BTN_FACE),
    BUTTON_HIGHLIGHT(STSystemColorVal.BTN_HIGHLIGHT),
    BUTTON_SHADOW(STSystemColorVal.BTN_SHADOW),
    BUTTON_TEXT(STSystemColorVal.BTN_TEXT),
    CAPTION_TEXT(STSystemColorVal.CAPTION_TEXT),
    GRADIENT_ACTIVE_CAPTION(STSystemColorVal.GRADIENT_ACTIVE_CAPTION),
    GRADIENT_INACTIVE_CAPTION(STSystemColorVal.GRADIENT_INACTIVE_CAPTION),
    GRAY_TEXT(STSystemColorVal.GRAY_TEXT),
    HIGHLIGHT(STSystemColorVal.HIGHLIGHT),
    HIGHLIGHT_TEXT(STSystemColorVal.HIGHLIGHT_TEXT),
    HOT_LIGHT(STSystemColorVal.HOT_LIGHT),
    INACTIVE_BORDER(STSystemColorVal.INACTIVE_BORDER),
    INACTIVE_CAPTION(STSystemColorVal.INACTIVE_CAPTION),
    INACTIVE_CAPTION_TEXT(STSystemColorVal.INACTIVE_CAPTION_TEXT),
    INFO_BACKGROUND(STSystemColorVal.INFO_BK),
    INFO_TEXT(STSystemColorVal.INFO_TEXT),
    MENU(STSystemColorVal.MENU),
    MENU_BAR(STSystemColorVal.MENU_BAR),
    MENU_HIGHLIGHT(STSystemColorVal.MENU_HIGHLIGHT),
    MENU_TEXT(STSystemColorVal.MENU_TEXT),
    SCROLL_BAR(STSystemColorVal.SCROLL_BAR),
    WINDOW(STSystemColorVal.WINDOW),
    WINDOW_FRAME(STSystemColorVal.WINDOW_FRAME),
    WINDOW_TEXT(STSystemColorVal.WINDOW_TEXT),
    X_3D_DARK_SHADOW(STSystemColorVal.X_3_D_DK_SHADOW),
    X_3D_LIGHT(STSystemColorVal.X_3_D_LIGHT);

    final STSystemColorVal.Enum underlying;

    SystemColor(STSystemColorVal.Enum color) {
        this.underlying = color;
    }

    private static final HashMap<STSystemColorVal.Enum, SystemColor> reverse = new HashMap<>();
    static {
        for (SystemColor value : values()) {
            reverse.put(value.underlying, value);
        }
    }

    static SystemColor valueOf(STSystemColorVal.Enum color) {
        return reverse.get(color);
    }
}
