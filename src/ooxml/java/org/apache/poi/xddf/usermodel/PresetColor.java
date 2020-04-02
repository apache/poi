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

import org.openxmlformats.schemas.drawingml.x2006.main.STPresetColorVal;

public enum PresetColor {
    ALICE_BLUE(STPresetColorVal.ALICE_BLUE),
    ANTIQUE_WHITE(STPresetColorVal.ANTIQUE_WHITE),
    AQUA(STPresetColorVal.AQUA),
    AQUAMARINE(STPresetColorVal.AQUAMARINE),
    AZURE(STPresetColorVal.AZURE),
    BEIGE(STPresetColorVal.BEIGE),
    BISQUE(STPresetColorVal.BISQUE),
    BLACK(STPresetColorVal.BLACK),
    BLANCHED_ALMOND(STPresetColorVal.BLANCHED_ALMOND),
    BLUE(STPresetColorVal.BLUE),
    BLUE_VIOLET(STPresetColorVal.BLUE_VIOLET),
    CADET_BLUE(STPresetColorVal.CADET_BLUE),
    CHARTREUSE(STPresetColorVal.CHARTREUSE),
    CHOCOLATE(STPresetColorVal.CHOCOLATE),
    CORAL(STPresetColorVal.CORAL),
    CORNFLOWER_BLUE(STPresetColorVal.CORNFLOWER_BLUE),
    CORNSILK(STPresetColorVal.CORNSILK),
    CRIMSON(STPresetColorVal.CRIMSON),
    CYAN(STPresetColorVal.CYAN),
    DEEP_PINK(STPresetColorVal.DEEP_PINK),
    DEEP_SKY_BLUE(STPresetColorVal.DEEP_SKY_BLUE),
    DIM_GRAY(STPresetColorVal.DIM_GRAY),
    DARK_BLUE(STPresetColorVal.DK_BLUE),
    DARK_CYAN(STPresetColorVal.DK_CYAN),
    DARK_GOLDENROD(STPresetColorVal.DK_GOLDENROD),
    DARK_GRAY(STPresetColorVal.DK_GRAY),
    DARK_GREEN(STPresetColorVal.DK_GREEN),
    DARK_KHAKI(STPresetColorVal.DK_KHAKI),
    DARK_MAGENTA(STPresetColorVal.DK_MAGENTA),
    DARK_OLIVE_GREEN(STPresetColorVal.DK_OLIVE_GREEN),
    DARK_ORANGE(STPresetColorVal.DK_ORANGE),
    DARK_ORCHID(STPresetColorVal.DK_ORCHID),
    DARK_RED(STPresetColorVal.DK_RED),
    DARK_SALMON(STPresetColorVal.DK_SALMON),
    DARK_SEA_GREEN(STPresetColorVal.DK_SEA_GREEN),
    DARK_SLATE_BLUE(STPresetColorVal.DK_SLATE_BLUE),
    DARK_SLATE_GRAY(STPresetColorVal.DK_SLATE_GRAY),
    DARK_TURQUOISE(STPresetColorVal.DK_TURQUOISE),
    DARK_VIOLET(STPresetColorVal.DK_VIOLET),
    DODGER_BLUE(STPresetColorVal.DODGER_BLUE),
    FIREBRICK(STPresetColorVal.FIREBRICK),
    FLORAL_WHITE(STPresetColorVal.FLORAL_WHITE),
    FOREST_GREEN(STPresetColorVal.FOREST_GREEN),
    FUCHSIA(STPresetColorVal.FUCHSIA),
    GAINSBORO(STPresetColorVal.GAINSBORO),
    GHOST_WHITE(STPresetColorVal.GHOST_WHITE),
    GOLD(STPresetColorVal.GOLD),
    GOLDENROD(STPresetColorVal.GOLDENROD),
    GRAY(STPresetColorVal.GRAY),
    GREEN(STPresetColorVal.GREEN),
    GREEN_YELLOW(STPresetColorVal.GREEN_YELLOW),
    HONEYDEW(STPresetColorVal.HONEYDEW),
    HOT_PINK(STPresetColorVal.HOT_PINK),
    INDIAN_RED(STPresetColorVal.INDIAN_RED),
    INDIGO(STPresetColorVal.INDIGO),
    IVORY(STPresetColorVal.IVORY),
    KHAKI(STPresetColorVal.KHAKI),
    LAVENDER(STPresetColorVal.LAVENDER),
    LAVENDER_BLUSH(STPresetColorVal.LAVENDER_BLUSH),
    LAWN_GREEN(STPresetColorVal.LAWN_GREEN),
    LEMON_CHIFFON(STPresetColorVal.LEMON_CHIFFON),
    LIME(STPresetColorVal.LIME),
    LIME_GREEN(STPresetColorVal.LIME_GREEN),
    LINEN(STPresetColorVal.LINEN),
    LIGHT_BLUE(STPresetColorVal.LT_BLUE),
    LIGHT_CORAL(STPresetColorVal.LT_CORAL),
    LIGHT_CYAN(STPresetColorVal.LT_CYAN),
    LIGHT_GOLDENROD_YELLOW(STPresetColorVal.LT_GOLDENROD_YELLOW),
    LIGHT_GRAY(STPresetColorVal.LT_GRAY),
    LIGHT_GREEN(STPresetColorVal.LT_GREEN),
    LIGHT_PINK(STPresetColorVal.LT_PINK),
    LIGHT_SALMON(STPresetColorVal.LT_SALMON),
    LIGHT_SEA_GREEN(STPresetColorVal.LT_SEA_GREEN),
    LIGHT_SKY_BLUE(STPresetColorVal.LT_SKY_BLUE),
    LIGHT_SLATE_GRAY(STPresetColorVal.LT_SLATE_GRAY),
    LIGHT_STEEL_BLUE(STPresetColorVal.LT_STEEL_BLUE),
    LIGHT_YELLOW(STPresetColorVal.LT_YELLOW),
    MAGENTA(STPresetColorVal.MAGENTA),
    MAROON(STPresetColorVal.MAROON),
    MEDIUM_AQUAMARINE(STPresetColorVal.MED_AQUAMARINE),
    MEDIUM_BLUE(STPresetColorVal.MED_BLUE),
    MEDIUM_ORCHID(STPresetColorVal.MED_ORCHID),
    MEDIUM_PURPLE(STPresetColorVal.MED_PURPLE),
    MEDIUM_SEA_GREEN(STPresetColorVal.MED_SEA_GREEN),
    MEDIUM_SLATE_BLUE(STPresetColorVal.MED_SLATE_BLUE),
    MEDIUM_SPRING_GREEN(STPresetColorVal.MED_SPRING_GREEN),
    MEDIUM_TURQUOISE(STPresetColorVal.MED_TURQUOISE),
    MEDIUM_VIOLET_RED(STPresetColorVal.MED_VIOLET_RED),
    MIDNIGHT_BLUE(STPresetColorVal.MIDNIGHT_BLUE),
    MINT_CREAM(STPresetColorVal.MINT_CREAM),
    MISTY_ROSE(STPresetColorVal.MISTY_ROSE),
    MOCCASIN(STPresetColorVal.MOCCASIN),
    NAVAJO_WHITE(STPresetColorVal.NAVAJO_WHITE),
    NAVY(STPresetColorVal.NAVY),
    OLD_LACE(STPresetColorVal.OLD_LACE),
    OLIVE(STPresetColorVal.OLIVE),
    OLIVE_DRAB(STPresetColorVal.OLIVE_DRAB),
    ORANGE(STPresetColorVal.ORANGE),
    ORANGE_RED(STPresetColorVal.ORANGE_RED),
    ORCHID(STPresetColorVal.ORCHID),
    PALE_GOLDENROD(STPresetColorVal.PALE_GOLDENROD),
    PALE_GREEN(STPresetColorVal.PALE_GREEN),
    PALE_TURQUOISE(STPresetColorVal.PALE_TURQUOISE),
    PALE_VIOLET_RED(STPresetColorVal.PALE_VIOLET_RED),
    PAPAYA_WHIP(STPresetColorVal.PAPAYA_WHIP),
    PEACH_PUFF(STPresetColorVal.PEACH_PUFF),
    PERU(STPresetColorVal.PERU),
    PINK(STPresetColorVal.PINK),
    PLUM(STPresetColorVal.PLUM),
    POWDER_BLUE(STPresetColorVal.POWDER_BLUE),
    PURPLE(STPresetColorVal.PURPLE),
    RED(STPresetColorVal.RED),
    ROSY_BROWN(STPresetColorVal.ROSY_BROWN),
    ROYAL_BLUE(STPresetColorVal.ROYAL_BLUE),
    SADDLE_BROWN(STPresetColorVal.SADDLE_BROWN),
    SALMON(STPresetColorVal.SALMON),
    SANDY_BROWN(STPresetColorVal.SANDY_BROWN),
    SEA_GREEN(STPresetColorVal.SEA_GREEN),
    SEA_SHELL(STPresetColorVal.SEA_SHELL),
    SIENNA(STPresetColorVal.SIENNA),
    SILVER(STPresetColorVal.SILVER),
    SKY_BLUE(STPresetColorVal.SKY_BLUE),
    SLATE_BLUE(STPresetColorVal.SLATE_BLUE),
    SLATE_GRAY(STPresetColorVal.SLATE_GRAY),
    SNOW(STPresetColorVal.SNOW),
    SPRING_GREEN(STPresetColorVal.SPRING_GREEN),
    STEEL_BLUE(STPresetColorVal.STEEL_BLUE),
    TAN(STPresetColorVal.TAN),
    TEAL(STPresetColorVal.TEAL),
    THISTLE(STPresetColorVal.THISTLE),
    TOMATO(STPresetColorVal.TOMATO),
    TURQUOISE(STPresetColorVal.TURQUOISE),
    VIOLET(STPresetColorVal.VIOLET),
    WHEAT(STPresetColorVal.WHEAT),
    WHITE(STPresetColorVal.WHITE),
    WHITE_SMOKE(STPresetColorVal.WHITE_SMOKE),
    YELLOW(STPresetColorVal.YELLOW),
    YELLOW_GREEN(STPresetColorVal.YELLOW_GREEN);

    final STPresetColorVal.Enum underlying;

    PresetColor(STPresetColorVal.Enum color) {
        this.underlying = color;
    }

    private final static HashMap<STPresetColorVal.Enum, PresetColor> reverse = new HashMap<>();
    static {
        for (PresetColor value : values()) {
            reverse.put(value.underlying, value);
        }
    }

    static PresetColor valueOf(STPresetColorVal.Enum color) {
        return reverse.get(color);
    }
}
