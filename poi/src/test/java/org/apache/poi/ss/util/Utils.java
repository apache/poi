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

package org.apache.poi.ss.util;

import org.apache.poi.ss.usermodel.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Utils {
    private static final String provider = System.getProperty("java.locale.providers");

    public static void addRow(Sheet sheet, int rownum, Object... values) {
        Row row = sheet.createRow(rownum);
        for (int i = 0; i < values.length; i++) {
            Cell cell = row.createCell(i);
            if (values[i] instanceof Integer) {
                cell.setCellValue((Integer) values[i]);
            } else if (values[i] instanceof Double) {
                cell.setCellValue((Double) values[i]);
            } else if (values[i] instanceof Boolean) {
                cell.setCellValue((Boolean) values[i]);
            } else if (values[i] instanceof Calendar) {
                cell.setCellValue((Calendar) values[i]);
            } else if (values[i] instanceof Date) {
                cell.setCellValue((Date) values[i]);
            } else if (values[i] instanceof LocalDate) {
                cell.setCellValue((LocalDate) values[i]);
            } else if (values[i] instanceof LocalDateTime) {
                cell.setCellValue((LocalDateTime) values[i]);
            } else if (values[i] instanceof FormulaError) {
                cell.setCellErrorValue(((FormulaError)values[i]).getCode());
            } else if (values[i] == null) {
                cell.setBlank();
            } else {
                cell.setCellValue(values[i].toString());
            }
        }
    }

    public static void assertString(FormulaEvaluator fe, Cell cell, String formulaText, String expectedResult) {
        cell.setCellFormula(formulaText);
        fe.notifyUpdateCell(cell);
        CellValue result = fe.evaluate(cell);
        assertEquals(CellType.STRING, result.getCellType());
        assertEquals(expectedResult, result.getStringValue(),
                "Failed with locale provider: " + provider);
    }

    public static void assertDouble(FormulaEvaluator fe, Cell cell, String formulaText, double expectedResult) {
        cell.setCellFormula(formulaText);
        fe.notifyUpdateCell(cell);
        CellValue result = fe.evaluate(cell);
        assertEquals(CellType.NUMERIC, result.getCellType());
        assertEquals(expectedResult, result.getNumberValue());
    }

    public static void assertDouble(FormulaEvaluator fe, Cell cell, String formulaText,
                                    double expectedResult, double tolerance) {
        cell.setCellFormula(formulaText);
        fe.notifyUpdateCell(cell);
        CellValue result = fe.evaluate(cell);
        assertEquals(CellType.NUMERIC, result.getCellType());
        assertEquals(expectedResult, result.getNumberValue(), tolerance);
    }

    public static void assertBoolean(FormulaEvaluator fe, Cell cell, String formulaText, boolean expectedResult) {
        cell.setCellFormula(formulaText);
        fe.notifyUpdateCell(cell);
        CellValue result = fe.evaluate(cell);
        assertEquals(CellType.BOOLEAN, result.getCellType());
        assertEquals(expectedResult, result.getBooleanValue());
    }

    public static void assertError(FormulaEvaluator fe, Cell cell, String formulaText, FormulaError expectedError) {
        cell.setCellFormula(formulaText);
        fe.notifyUpdateCell(cell);
        CellValue result = fe.evaluate(cell);
        assertEquals(CellType.ERROR, result.getCellType());
        assertEquals(expectedError.getCode(), result.getErrorValue());
    }
}
