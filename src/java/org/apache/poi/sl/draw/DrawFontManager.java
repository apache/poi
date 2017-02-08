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

/**
 * Manages fonts when rendering slides.
 *
 * Use this class to handle unknown / missing fonts or to substitute fonts
 */
public interface DrawFontManager {

    /**
     * select a font to be used to paint text
     *
     * @param typeface the font family as defined in the .pptx file.
     * This can be unknown or missing in the graphic environment.
     * @param pitchFamily a pitch-and-family,
     * see {@link org.apache.poi.hwmf.record.HwmfFont#getFamily()} and
     * {@link org.apache.poi.hwmf.record.HwmfFont#getPitch()}
     * for how to calculate those (ancient) values
     *
     * @return the font to be used to paint text
     */
    String getRendererableFont(String typeface, int pitchFamily);

    /**
     * In case the original font doesn't contain a glyph, use the
     * returned fallback font as an alternative
     *
     * @param typeface the font family as defined in the .pptx file.
     * @param pitchFamily a pitch-and-family,
     * see {@link org.apache.poi.hwmf.record.HwmfFont#getFamily()} and
     * {@link org.apache.poi.hwmf.record.HwmfFont#getPitch()}
     * for how to calculate those (ancient) values
     * 
     * @return the font to be used as a fallback for the original typeface
     */
    String getFallbackFont(String typeface, int pitchFamily);
}
