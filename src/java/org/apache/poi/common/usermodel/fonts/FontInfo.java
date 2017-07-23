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

package org.apache.poi.common.usermodel.fonts;

/**
 * A FontInfo object holds information about a font configuration.
 * It is roughly an equivalent to the LOGFONT structure in Windows GDI.<p>
 *
 * If an implementation doesn't provide a property, the getter will return {@code null} -
 * if the value is unset, a default value will be returned.<p>
 *
 * Setting a unsupported property results in an {@link UnsupportedOperationException}.
 *
 * @since POI 3.17-beta2
 *
 * @see <a href="https://msdn.microsoft.com/en-us/library/dd145037.aspx">LOGFONT structure</a>
 */
public interface FontInfo {

    /**
     * Get the index within the collection of Font objects
     * @return unique index number of the underlying record this Font represents
     *   (probably you don't care unless you're comparing which one is which)
     */
    Integer getIndex();

    /**
     * Sets the index within the collection of Font objects
     *
     * @param index the index within the collection of Font objects
     *
     * @throws UnsupportedOperationException if unsupported
     */
    void setIndex(int index);
    
    
    /**
     * @return the full name of the font, i.e. font family + type face
     */
    String getTypeface();

    /**
     * Sets the font name
     *
     * @param typeface the full name of the font, when {@code null} removes the font definition -
     *    removal is implementation specific
     */
    void setTypeface(String typeface);

    /**
     * @return the font charset
     */
    FontCharset getCharset();

    /**
     * Sets the charset
     *
     * @param charset the charset
     */
    void setCharset(FontCharset charset);

    /**
     * @return the family class
     */
    FontFamily getFamily();

    /**
     * Sets the font family class
     *
     * @param family the font family class
     */
    void setFamily(FontFamily family);

    /**
     * @return the font pitch or {@code null} if unsupported
     */
    FontPitch getPitch();

    /**
     * Set the font pitch
     *
     * @param pitch the font pitch
     *
     * @throws UnsupportedOperationException if unsupported
     */
    void setPitch(FontPitch pitch);
}