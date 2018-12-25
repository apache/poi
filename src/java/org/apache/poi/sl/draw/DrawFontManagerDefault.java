/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.sl.draw;

import java.awt.Font;
import java.awt.Graphics2D;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.poi.common.usermodel.fonts.FontInfo;
import org.apache.poi.sl.draw.Drawable.DrawableHint;

/**
 * Manages fonts when rendering slides.
 *
 * Use this class to handle unknown / missing fonts or to substitute fonts
 */
public class DrawFontManagerDefault implements DrawFontManager {

    protected final Set<String> knownSymbolFonts = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

    public DrawFontManagerDefault() {
        knownSymbolFonts.add("Wingdings");
        knownSymbolFonts.add("Symbol");
    }

    @Override
    public FontInfo getMappedFont(Graphics2D graphics, FontInfo fontInfo) {
        return getFontWithFallback(graphics, Drawable.FONT_MAP, fontInfo);
    }

    @Override
    public FontInfo getFallbackFont(Graphics2D graphics, FontInfo fontInfo) {
        FontInfo fi = getFontWithFallback(graphics, Drawable.FONT_FALLBACK, fontInfo);
        if (fi == null) {
            fi = new DrawFontInfo(Font.SANS_SERIF);
        }
        return fi;
    }

    public String mapFontCharset(Graphics2D graphics, FontInfo fontInfo, String text) {
        // TODO: find a real charset mapping solution instead of hard coding for Wingdings
        return (fontInfo != null && knownSymbolFonts.contains(fontInfo.getTypeface()))
            ? mapSymbolChars(text)
            : text;
    }

    /**
     * Symbol fonts like "Wingdings" or "Symbol" have glyphs mapped to a Unicode private use range via the Java font loader,
     * although a system font viewer might show you the glyphs in the ASCII range.
     * This helper function maps the chars of the text string to the corresponding private use range chars.
     *
     * @param text the input string, typically consists of ASCII chars
     * @return the mapped string, typically consists of chars in the range of 0xf000 to 0xf0ff
     *
     * @since POI 4.0.0
     */
    public static String mapSymbolChars(String text) {
        // wingdings doesn't contain high-surrogates, so chars are ok
        boolean changed = false;
        char[] chrs = text.toCharArray();
        for (int i=0; i<chrs.length; i++) {
            // only change valid chars
            if ((0x20 <= chrs[i] && chrs[i] <= 0x7f) ||
                    (0xa0 <= chrs[i] && chrs[i] <= 0xff)) {
                chrs[i] |= 0xf000;
                changed = true;
            }
        }

        return changed ? new String(chrs) : text;
    }

    @Override
    public Font createAWTFont(Graphics2D graphics, FontInfo fontInfo, double fontSize, boolean bold, boolean italic) {
        int style = (bold ? Font.BOLD : 0) | (italic ? Font.ITALIC : 0);
        Font font = new Font(fontInfo.getTypeface(), style, 12);
        if (Font.DIALOG.equals(font.getFamily())) {
            // SansSerif is a better choice than Dialog
            font = new Font(Font.SANS_SERIF, style, 12);
        }
        return font.deriveFont((float)fontSize);
    }

    private FontInfo getFontWithFallback(Graphics2D graphics, DrawableHint hint, FontInfo fontInfo) {
        @SuppressWarnings("unchecked")
        Map<String,String> fontMap = (Map<String,String>)graphics.getRenderingHint(hint);
        if (fontMap == null) {
            return fontInfo;
        }
        
        String f = (fontInfo != null) ? fontInfo.getTypeface() : null;
        String mappedTypeface = null;
        if (fontMap.containsKey(f)) {
            mappedTypeface = fontMap.get(f);
        } else if (fontMap.containsKey("*")) {
            mappedTypeface = fontMap.get("*");
        }

        return (mappedTypeface != null) ? new DrawFontInfo(mappedTypeface) : fontInfo;
    }
}
