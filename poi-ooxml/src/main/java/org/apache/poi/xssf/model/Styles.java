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

package org.apache.poi.xssf.model;

import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellFill;

public interface Styles {

    /**
     * Get number format string given its id
     *
     * @param fmtId number format id
     * @return number format code
     */
    String getNumberFormatAt(short fmtId);

    /**
     * Puts <code>fmt</code> in the numberFormats map if the format is not
     * already in the number format style table.
     * Does nothing if <code>fmt</code> is already in number format style table.
     *
     * @param fmt the number format to add to number format style table
     * @return the index of <code>fmt</code> in the number format style table
     * @throws IllegalStateException if adding the number format to the styles table
     * would exceed the max allowed.
     */
    int putNumberFormat(String fmt);

    /**
     * Add a number format with a specific ID into the numberFormats map.
     * If a format with the same ID already exists, overwrite the format code
     * with <code>fmt</code>
     * This may be used to override built-in number formats.
     *
     * @param index the number format ID
     * @param fmt the number format code
     */
    void putNumberFormat(short index, String fmt);

    /**
     * Remove a number format from the style table if it exists.
     * All cell styles with this number format will be modified to use the default number format.
     *
     * @param index the number format id to remove
     * @return true if the number format was removed
     */
    boolean removeNumberFormat(short index);

    /**
     * Remove a number format from the style table if it exists
     * All cell styles with this number format will be modified to use the default number format
     *
     * @param fmt the number format to remove
     * @return true if the number format was removed
     */
    boolean removeNumberFormat(String fmt);

    XSSFFont getFontAt(int idx);

    /**
     * Records the given font in the font table.
     * Will re-use an existing font index if this
     *  font matches another, EXCEPT if forced
     *  registration is requested.
     * This allows people to create several fonts
     *  then customise them later.
     * Note - End Users probably want to call
     *  {@link XSSFFont#registerTo(StylesTable)}
     */
    int putFont(XSSFFont font, boolean forceRegistration);

    /**
     * Records the given font in the font table.
     * Will re-use an existing font index if this
     * font matches another.
     */
    int putFont(XSSFFont font);

    /**
     *
     * @param idx style index
     * @return XSSFCellStyle or null if idx is out of bounds for xfs array
     */
    XSSFCellStyle getStyleAt(int idx);

    int putStyle(XSSFCellStyle style);

    XSSFCellBorder getBorderAt(int idx);

    /**
     * Adds a border to the border style table if it isn't already in the style table
     * Does nothing if border is already in borders style table
     *
     * @param border border to add
     * @return the index of the added border
     */
    int putBorder(XSSFCellBorder border);

    XSSFCellFill getFillAt(int idx);

    /**
     * Adds a fill to the fill style table if it isn't already in the style table
     * Does nothing if fill is already in fill style table
     *
     * @param fill fill to add
     * @return the index of the added fill
     */
    int putFill(XSSFCellFill fill);

    int getNumCellStyles();

    int getNumDataFormats();
}
