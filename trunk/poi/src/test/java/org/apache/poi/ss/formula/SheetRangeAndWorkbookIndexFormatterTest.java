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

package org.apache.poi.ss.formula;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class SheetRangeAndWorkbookIndexFormatterTest {
    @Test
    void noDelimiting_ifASingleSheetNameDoesntNeedDelimiting() {
        StringBuilder sb = new StringBuilder();
        String result = SheetRangeAndWorkbookIndexFormatter.format(sb, 0, "noDelimiting", null);
        assertEquals("[0]noDelimiting", result);
    }

    @Test
    void everythingIsScreened_ifASingleSheetNameNeedsDelimiting() {
        StringBuilder sb = new StringBuilder();
        String result = SheetRangeAndWorkbookIndexFormatter.format(sb, 0, "1delimiting", null);
        assertEquals("'[0]1delimiting'", result);
    }

    @Test
    void noDelimiting_ifBothSheetNamesDontNeedDelimiting() {
        StringBuilder sb = new StringBuilder();
        String result = SheetRangeAndWorkbookIndexFormatter.format(sb, 0, "noDelimiting1", "noDelimiting2");
        assertEquals("[0]noDelimiting1:noDelimiting2", result);
    }

    @Test
    void everythingIsScreened_ifFirstSheetNamesNeedsDelimiting() {
        StringBuilder sb = new StringBuilder();
        String result = SheetRangeAndWorkbookIndexFormatter.format(sb, 0, "1delimiting", "noDelimiting");
        assertEquals("'[0]1delimiting:noDelimiting'", result);
    }

    @Test
    void everythingIsScreened_ifLastSheetNamesNeedsDelimiting() {
        StringBuilder sb = new StringBuilder();
        String result = SheetRangeAndWorkbookIndexFormatter.format(sb, 0, "noDelimiting", "1delimiting");
        assertEquals("'[0]noDelimiting:1delimiting'", result);
    }

    @Test
    void everythingIsScreened_ifBothSheetNamesNeedDelimiting() {
        StringBuilder sb = new StringBuilder();
        String result = SheetRangeAndWorkbookIndexFormatter.format(sb, 0, "1delimiting", "2delimiting");
        assertEquals("'[0]1delimiting:2delimiting'", result);
    }
}
