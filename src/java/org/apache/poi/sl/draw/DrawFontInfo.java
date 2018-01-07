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

import org.apache.poi.common.usermodel.fonts.FontCharset;
import org.apache.poi.common.usermodel.fonts.FontFamily;
import org.apache.poi.common.usermodel.fonts.FontInfo;
import org.apache.poi.common.usermodel.fonts.FontPitch;
import org.apache.poi.util.Internal;

/**
 * Convenience class to handle FontInfo mappings
 */
@Internal
/* package */ class DrawFontInfo implements FontInfo {

    private final String typeface;
    
    DrawFontInfo(String typeface) {
        this.typeface = typeface;
    }
    
    @Override
    public Integer getIndex() {
        return null;
    }

    @Override
    public void setIndex(int index) {
        throw new UnsupportedOperationException("DrawFontManagers FontInfo can't be changed.");
    }

    @Override
    public String getTypeface() {
        return typeface;
    }

    @Override
    public void setTypeface(String typeface) {
        throw new UnsupportedOperationException("DrawFontManagers FontInfo can't be changed.");
    }

    @Override
    public FontCharset getCharset() {
        return FontCharset.ANSI;
    }

    @Override
    public void setCharset(FontCharset charset) {
        throw new UnsupportedOperationException("DrawFontManagers FontInfo can't be changed.");
    }

    @Override
    public FontFamily getFamily() {
        return FontFamily.FF_SWISS;
    }

    @Override
    public void setFamily(FontFamily family) {
        throw new UnsupportedOperationException("DrawFontManagers FontInfo can't be changed.");
    }

    @Override
    public FontPitch getPitch() {
        return FontPitch.VARIABLE;
    }

    @Override
    public void setPitch(FontPitch pitch) {
        throw new UnsupportedOperationException("DrawFontManagers FontInfo can't be changed.");
    }
}
