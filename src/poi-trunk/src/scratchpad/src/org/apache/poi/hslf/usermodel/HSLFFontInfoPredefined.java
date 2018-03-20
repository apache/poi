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

package org.apache.poi.hslf.usermodel;

import org.apache.poi.common.usermodel.fonts.FontCharset;
import org.apache.poi.common.usermodel.fonts.FontFamily;
import org.apache.poi.common.usermodel.fonts.FontInfo;
import org.apache.poi.common.usermodel.fonts.FontPitch;

/**
 * Predefined fonts
 * 
 * @since POI 3.17-beta2
 */
public enum HSLFFontInfoPredefined implements FontInfo {
    ARIAL("Arial", FontCharset.ANSI, FontPitch.VARIABLE, FontFamily.FF_SWISS),
    TIMES_NEW_ROMAN("Times New Roman", FontCharset.ANSI, FontPitch.VARIABLE, FontFamily.FF_ROMAN),
    COURIER_NEW("Courier New", FontCharset.ANSI, FontPitch.FIXED, FontFamily.FF_MODERN),
    WINGDINGS("Wingdings", FontCharset.SYMBOL, FontPitch.VARIABLE, FontFamily.FF_DONTCARE);

    private String typeface;
    private FontCharset charset;
    private FontPitch pitch;
    private FontFamily family;
    
    HSLFFontInfoPredefined(String typeface, FontCharset charset, FontPitch pitch, FontFamily family) {
        this.typeface = typeface;
        this.charset = charset;
        this.pitch = pitch;
        this.family = family;
    }
    
    @Override
    public Integer getIndex() {
        return -1;
    }

    @Override
    public void setIndex(int index) {
        throw new UnsupportedOperationException("Predefined enum can't be changed.");
    }

    @Override
    public String getTypeface() {
        return typeface;
    }

    @Override
    public void setTypeface(String typeface) {
        throw new UnsupportedOperationException("Predefined enum can't be changed.");
    }

    @Override
    public FontCharset getCharset() {
        return charset;
    }

    @Override
    public void setCharset(FontCharset charset) {
        throw new UnsupportedOperationException("Predefined enum can't be changed.");
    }

    @Override
    public FontFamily getFamily() {
        return family;
    }

    @Override
    public void setFamily(FontFamily family) {
        throw new UnsupportedOperationException("Predefined enum can't be changed.");
    }

    @Override
    public FontPitch getPitch() {
        return pitch;
    }

    @Override
    public void setPitch(FontPitch pitch) {
        throw new UnsupportedOperationException("Predefined enum can't be changed.");
    }
}
