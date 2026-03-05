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

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFEvaluationWorkbook;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.EvaluationCell;
import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.formula.WorkbookEvaluator;
import org.apache.poi.ss.formula.eval.NotImplementedException;
import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.util.RecordFormatException;

/**
 * Fuzz target for the Apache POI WorkbookEvaluator engine.
 * <p>
 * This fuzzer exercises the core formula evaluation pipeline by
 * setting fuzz-generated formulas on a pre-populated workbook and
 * evaluating them through the {@link WorkbookEvaluator}. A 20x20 data
 * grid with mixed cell types (numbers, strings, booleans, errors)
 * ensures that referential functions (SUM, VLOOKUP, INDEX, etc.)
 * reach deep evaluation branches.
 * <p>
 * Used by Google's OSS-Fuzz for continuous security testing.
 */
public class WorkbookEvaluatorFuzzer {
    private static HSSFWorkbook workbook;
    private static HSSFSheet sheet;
    private static HSSFCell fuzzerCell;
    private static HSSFEvaluationWorkbook evalWorkbook;
    private static EvaluationCell fuzzerEvalCell;
    private static WorkbookEvaluator evaluator;

    private static final String[] EXCEL_FUNCTIONS = {
        "SUM(", "IF(", "VLOOKUP(", "HLOOKUP(", "INDEX(", "MATCH(", "OFFSET(", "INDIRECT(",
        "CHOOSE(", "ADDRESS(", "AREAS(", "CELL(", "COLUMN(", "COLUMNS(", "ROW(", "ROWS(",
        "SUMPRODUCT(", "SUMIFS(", "COUNTIFS(", "AVERAGEIFS(", "MAXIFS(", "MINIFS(",
        "NPV(", "XIRR(", "PMT(", "FV(", "IRR(", "MIRR(", "NPER(", "RATE(",
        "DGET(", "DSUM(", "DAVERAGE(", "DCOUNT(", "DMAX(", "DMIN(", "DPRODUCT(",
        "AND(", "OR(", "NOT(", "XOR(", "IFERROR(", "IFNA(", "SWITCH(",
        "CONCATENATE(", "TEXTJOIN(", "MID(", "LEFT(", "RIGHT(", "FIND(", "SEARCH(", "SUBSTITUTE(",
        "DATE(", "TIME(", "NOW(", "TODAY(", "DATEDIF(", "WORKDAY(", "NETWORKDAYS("
    };

    public static void fuzzerInitialize() {
        workbook = new HSSFWorkbook();
        sheet = workbook.createSheet("FuzzSheet");

        // Target cell for the fuzzer (A1)
        HSSFRow fuzzerRow = sheet.createRow(0);
        fuzzerCell = fuzzerRow.createCell(0);

        // Pre-populate a 20x20 grid with mixed data so that referential
        // formulas (SUM, MATCH, VLOOKUP, etc.) exercise real evaluation paths.
        for (int r = 1; r <= 20; r++) {
            HSSFRow row = sheet.createRow(r);
            for (int c = 0; c < 20; c++) {
                HSSFCell dataCell = row.createCell(c);
                switch ((r + c) % 5) {
                    case 0: dataCell.setCellValue(42.0 * r); break;
                    case 1: dataCell.setCellValue(-r * 1.5); break;
                    case 2: dataCell.setCellValue("Data" + c); break;
                    case 3: dataCell.setCellValue((r % 2 == 0)); break;
                    case 4: dataCell.setCellErrorValue(FormulaError.VALUE.getCode()); break;
                }
            }
        }

        evalWorkbook = HSSFEvaluationWorkbook.create(workbook);
        evaluator = new WorkbookEvaluator(evalWorkbook, null, null);
        fuzzerEvalCell = evalWorkbook.getSheet(0).getCell(0, 0);
    }

    public static void fuzzerTestOneInput(FuzzedDataProvider data) {
        try {
            StringBuilder sb = new StringBuilder();
            if (data.consumeBoolean()) {
                sb.append(data.pickValue(EXCEL_FUNCTIONS));
            }
            sb.append(data.consumeRemainingAsString());

            String formula = sb.toString();
            if (formula.isEmpty()) {
                return;
            }

            // 1. Compile: swallow parser exceptions per POI convention
            try {
                fuzzerCell.setCellFormula(formula);
            } catch (Exception e) {
                return;
            }

            // 2. Evaluate through the core engine
            evaluator.evaluate(fuzzerEvalCell);

        } catch (IllegalArgumentException | IllegalStateException |
                 FormulaParseException | NotImplementedException |
                 RecordFormatException e) {
            // Expected engine/logic limitations on malformed formulas
        }
    }
}
