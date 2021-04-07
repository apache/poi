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

public class SheetRangeAndWorkbookIndexFormatter {
    private SheetRangeAndWorkbookIndexFormatter() {
    }

    public static String format(StringBuilder sb, int workbookIndex, String firstSheetName, String lastSheetName) {
        if (anySheetNameNeedsEscaping(firstSheetName, lastSheetName)) {
            return formatWithDelimiting(sb, workbookIndex, firstSheetName, lastSheetName);
        } else {
            return formatWithoutDelimiting(sb, workbookIndex, firstSheetName, lastSheetName);
        }
    }

    private static String formatWithDelimiting(StringBuilder sb, int workbookIndex, String firstSheetName, String lastSheetName) {
        sb.append('\'');
        if (workbookIndex >= 0) {
            sb.append('[');
            sb.append(workbookIndex);
            sb.append(']');
        }

        SheetNameFormatter.appendAndEscape(sb, firstSheetName);

        if (lastSheetName != null) {
            sb.append(':');
            SheetNameFormatter.appendAndEscape(sb, lastSheetName);
        }

        sb.append('\'');
        return sb.toString();
    }

    private static String formatWithoutDelimiting(StringBuilder sb, int workbookIndex, String firstSheetName, String lastSheetName) {
        if (workbookIndex >= 0) {
            sb.append('[');
            sb.append(workbookIndex);
            sb.append(']');
        }

        sb.append(firstSheetName);

        if (lastSheetName != null) {
            sb.append(':');
            sb.append(lastSheetName);
        }

        return sb.toString();
    }

    private static boolean anySheetNameNeedsEscaping(String firstSheetName, String lastSheetName) {
        boolean anySheetNameNeedsDelimiting = SheetNameFormatter.needsDelimiting(firstSheetName);
        anySheetNameNeedsDelimiting |= SheetNameFormatter.needsDelimiting(lastSheetName);
        return anySheetNameNeedsDelimiting;
    }
}
