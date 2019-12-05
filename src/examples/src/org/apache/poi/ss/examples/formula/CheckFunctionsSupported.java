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
package org.apache.poi.ss.examples.formula;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.poi.ss.formula.eval.NotImplementedException;
import org.apache.poi.ss.formula.eval.NotImplementedFunctionException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellReference;

/**
 * Attempts to re-evaluate all the formulas in the workbook, and
 *  reports what (if any) formula functions used are not (currently)
 *  supported by Apache POI.
 *
 * <p>This provides examples of how to evaluate formulas in excel
 *  files using Apache POI, along with how to handle errors whilst
 *  doing so.
 */
public class CheckFunctionsSupported {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Use:");
            System.err.println("  CheckFunctionsSupported <filename>");
            return;
        }

        Workbook wb = WorkbookFactory.create(new File(args[0]));
        CheckFunctionsSupported check = new CheckFunctionsSupported(wb);

        // Fetch all the problems
        List<FormulaEvaluationProblems> problems = new ArrayList<>();
        for (int sn=0; sn<wb.getNumberOfSheets(); sn++) {
            problems.add(check.getEvaluationProblems(sn));
        }

        // Produce an overall summary
        Set<String> unsupportedFunctions = new TreeSet<>();
        for (FormulaEvaluationProblems p : problems) {
            unsupportedFunctions.addAll(p.unsupportedFunctions);
        }
        if (unsupportedFunctions.isEmpty()) {
            System.out.println("There are no unsupported formula functions used");
        } else {
            System.out.println("Unsupported formula functions:");
            for (String function : unsupportedFunctions) {
                System.out.println("  " + function);
            }
            System.out.println("Total unsupported functions = " + unsupportedFunctions.size());
        }

        // Report sheet by sheet
        for (int sn=0; sn<wb.getNumberOfSheets(); sn++) {
            String sheetName = wb.getSheetName(sn);
            FormulaEvaluationProblems probs = problems.get(sn);

            System.out.println();
            System.out.println("Sheet = " + sheetName);

            if (probs.unevaluatableCells.isEmpty()) {
                System.out.println(" All cells evaluated without error");
            } else {
                for (CellReference cr : probs.unevaluatableCells.keySet()) {
                    System.out.println(" " + cr.formatAsString() + " - " +
                            probs.unevaluatableCells.get(cr));
                }
            }
        }
    }

    private Workbook workbook;
    private FormulaEvaluator evaluator;
    public CheckFunctionsSupported(Workbook workbook) {
        this.workbook = workbook;
        this.evaluator = workbook.getCreationHelper().createFormulaEvaluator();
    }

    public Set<String> getUnsupportedFunctions(String sheetName) {
        return getUnsupportedFunctions(workbook.getSheet(sheetName));
    }
    public Set<String> getUnsupportedFunctions(int sheetIndex) {
        return getUnsupportedFunctions(workbook.getSheetAt(sheetIndex));
    }
    public Set<String> getUnsupportedFunctions(Sheet sheet) {
        FormulaEvaluationProblems problems = getEvaluationProblems(sheet);
        return problems.unsupportedFunctions;
    }

    public FormulaEvaluationProblems getEvaluationProblems(String sheetName) {
        return getEvaluationProblems(workbook.getSheet(sheetName));
    }
    public FormulaEvaluationProblems getEvaluationProblems(int sheetIndex) {
        return getEvaluationProblems(workbook.getSheetAt(sheetIndex));
    }
    public FormulaEvaluationProblems getEvaluationProblems(Sheet sheet) {
        Set<String> unsupportedFunctions = new HashSet<>();
        Map<CellReference,Exception> unevaluatableCells = new HashMap<>();

        for (Row r : sheet) {
            for (Cell c : r) {
                try {
                    evaluator.evaluate(c);
                } catch (Exception e) {
                    if (e instanceof NotImplementedException && e.getCause() != null) {
                        // Has been wrapped with cell details, but we know those
                        e = (Exception)e.getCause();
                    }

                    if (e instanceof NotImplementedFunctionException) {
                        NotImplementedFunctionException nie = (NotImplementedFunctionException)e;
                        unsupportedFunctions.add(nie.getFunctionName());
                    }
                    unevaluatableCells.put(new CellReference(c), e);
                }
            }
        }

        return new FormulaEvaluationProblems(unsupportedFunctions, unevaluatableCells);
    }

    public static class FormulaEvaluationProblems {
        /** Which used functions are unsupported by POI at this time */
        private final Set<String> unsupportedFunctions;
        /** Which cells had unevaluatable formulas, and why? */
        private final Map<CellReference,Exception> unevaluatableCells;

        protected FormulaEvaluationProblems(Set<String> unsupportedFunctions,
                             Map<CellReference, Exception> unevaluatableCells) {
            this.unsupportedFunctions = Collections.unmodifiableSet(unsupportedFunctions);
            this.unevaluatableCells = Collections.unmodifiableMap(unevaluatableCells);
        }
    }
}
