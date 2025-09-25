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

package org.apache.poi.ss.usermodel;

/**
 * The CellPropertyType enum represents the different types of cell properties that can be applied to a cell.
 * Each type is associated with a specific category {@link CellPropertyCategory}, which classifies and organizes
 * the properties based on their characteristics.
 *
 * @since POI 5.4.0
 */
public enum CellPropertyType {

    BORDER_BOTTOM(CellPropertyCategory.BORDER_TYPE),
    BORDER_LEFT(CellPropertyCategory.BORDER_TYPE),
    BORDER_RIGHT(CellPropertyCategory.BORDER_TYPE),
    BORDER_TOP(CellPropertyCategory.BORDER_TYPE),

    BOTTOM_BORDER_COLOR(CellPropertyCategory.SHORT),
    LEFT_BORDER_COLOR(CellPropertyCategory.SHORT),
    RIGHT_BORDER_COLOR(CellPropertyCategory.SHORT),
    TOP_BORDER_COLOR(CellPropertyCategory.SHORT),
    DATA_FORMAT(CellPropertyCategory.SHORT),
    FILL_BACKGROUND_COLOR(CellPropertyCategory.SHORT),
    FILL_FOREGROUND_COLOR(CellPropertyCategory.SHORT),
    INDENTION(CellPropertyCategory.SHORT),
    ROTATION(CellPropertyCategory.SHORT),

    FILL_BACKGROUND_COLOR_COLOR(CellPropertyCategory.COLOR),
    FILL_FOREGROUND_COLOR_COLOR(CellPropertyCategory.COLOR),

    FONT(CellPropertyCategory.INT),

    HIDDEN(CellPropertyCategory.BOOL),
    LOCKED(CellPropertyCategory.BOOL),
    WRAP_TEXT(CellPropertyCategory.BOOL),
    SHRINK_TO_FIT(CellPropertyCategory.BOOL),
    QUOTE_PREFIXED(CellPropertyCategory.BOOL),

    ALIGNMENT(CellPropertyCategory.OTHER),
    FILL_PATTERN(CellPropertyCategory.OTHER),
    VERTICAL_ALIGNMENT(CellPropertyCategory.OTHER);

    CellPropertyType(CellPropertyCategory category) {
        this.category = category;
    }

    private final CellPropertyCategory category;

    public CellPropertyCategory getCategory() {
        return this.category;
    }

}
