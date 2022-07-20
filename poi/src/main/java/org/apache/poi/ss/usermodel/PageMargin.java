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

import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration which represents the various margins which are present within an
 * Excel worksheet
 *
 * <p>
 * Page margins are relevant when printing worksheets, and define the amount of
 * empty space on the edges of each printed page
 * </p>
 *
 * @since POI 5.2.3
 */
public enum PageMargin {

    /**
     * Left margin, the empty space on the left of displayed worksheet data when
     * printing
     */
    LEFT(Sheet.LeftMargin),

    /**
     * Right margin, the empty space on the right of displayed worksheet data
     * when printing
     */
    RIGHT(Sheet.RightMargin),

    /**
     * Top margin, the empty space on the top of displayed worksheet data when
     * printing
     */
    TOP(Sheet.TopMargin),

    /**
     * Bottom margin, the empty space on the bottom of displayed worksheet data
     * when printing
     */
    BOTTOM(Sheet.BottomMargin),

    /**
     * Header margin, the empty space between the header and the top of the page
     * when printing
     */
    HEADER(Sheet.HeaderMargin),

    /**
     * Footer margin, the empty space between the footer and the bottom of the
     * page when printing
     */
    FOOTER(Sheet.FooterMargin);

    /**
     * Map relating the old API constant values to their corresponding
     * enumeration value
     */
    private static final Map<Short, PageMargin> PAGE_MARGIN_BY_LEGACY_API_VALUE;

    static {
        PAGE_MARGIN_BY_LEGACY_API_VALUE = new HashMap<>();

        for (PageMargin margin : values()) {
            PAGE_MARGIN_BY_LEGACY_API_VALUE.put(margin.legacyApiValue, margin);
        }
    }

    /**
     * The old API constant value which corresponded to this page margin
     */
    private final short legacyApiValue;

    /**
     * @param legacyApiValue The old API constant value which corresponded to this page
     *                       margin
     */
    PageMargin(short legacyApiValue) {
        this.legacyApiValue = legacyApiValue;
    }

    public short getLegacyApiValue() {
        return legacyApiValue;
    }

    /**
     * Retrieves the enumeration value which corresponds to a legacy API
     * constant value
     *
     * @param legacyApiValue An old API margin constant value
     * @return The PageMargin enumeration which corresponds to the given value,
     * or null if no corresponding value exists
     */
    public static PageMargin getByShortValue(short legacyApiValue) {
        return PAGE_MARGIN_BY_LEGACY_API_VALUE.get(legacyApiValue);
    }
}