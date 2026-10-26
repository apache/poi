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

package org.apache.poi.fuzz;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.BufferUnderflowException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.poi.hssf.usermodel.HSSFEvaluationWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.format.CellFormat;
import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.formula.FormulaParser;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.util.RecordFormatException;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;

/**
 * Fuzz target for the Apache POI cell formatter.
 * Used by Google's OSS-Fuzz for continuous security testing.
 */
public class CellFormatFuzzer {
    public static void fuzzerTestOneInput(FuzzedDataProvider data) {
        Locale locale = switch (data.consumeInt(0, 6)) {
            case 0 -> Locale.ROOT;
            case 1 -> Locale.ENGLISH;
            case 2 -> Locale.GERMAN;
            case 3 -> Locale.CANADA;
            case 4 -> Locale.CHINESE;
            case 5 -> Locale.JAPAN;
            case 6 -> Locale.KOREAN;
            default -> throw new IllegalArgumentException();
        };

        Object obj = switch (data.consumeInt(0, 2)) {
            case 0 -> data.consumeDouble();
            case 1 -> data.consumeLong();
            case 2 -> data.consumeString(data.consumeInt(0, 100));
            default -> throw new IllegalArgumentException();
        };

        try {
            CellFormat cellFormat = CellFormat.getInstance(locale, data.consumeRemainingAsString());

            cellFormat.apply(obj);
        } catch (IllegalArgumentException | IndexOutOfBoundsException | IllegalStateException |
                ArithmeticException e) {
            // expected
        }
    }
}
