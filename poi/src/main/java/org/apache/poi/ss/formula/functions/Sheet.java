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

package org.apache.poi.ss.formula.functions;

import org.apache.poi.ss.formula.EvaluationWorkbook;
import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.formula.eval.*;

/**
 * Implementation for Excel SHEET() function.
 * <p>
 * <b>Syntax</b>:<br> <b>SHEET</b>([value])<br>
 * </p>
 * <p>
 * Returns the sheet number of the referenced sheet or the current sheet if no argument is provided.
 * </p>
 * <p>
 * Examples:
 * <ul>
 *     <li><code>=SHEET()</code> → returns the current sheet number (1-based)</li>
 *     <li><code>=SHEET(A1)</code> → returns the sheet number of the reference A1</li>
 *     <li><code>=SHEET(A1:B5)</code> → returns the sheet number of the range A1:B5</li>
 *     <li><code>=SHEET("Sheet3")</code> → returns the sheet number of the sheet named "Sheet3"</li>
 * </ul>
 * </p>
 * <p>
 * See <a href="https://support.microsoft.com/en-us/office/sheet-function-44718b6f-8b87-47a1-a9d6-b701c06cff24">Microsoft Documentation</a>
 * </p>
 */
public class Sheet implements FreeRefFunction {

    public static final Sheet instance = new Sheet();

    @Override
    public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {
        try {
            if (args.length == 0) {
                // No argument provided → return the current sheet index +1 (Excel uses 1-based index)
                return new NumberEval((double) ec.getSheetIndex() + 1);
            } else {
                ValueEval arg = args[0];

                if (arg instanceof RefEval) {
                    // Argument is a single cell reference → return the sheet index of that reference +1
                    RefEval ref = (RefEval) arg;
                    int sheetIndex = ref.getFirstSheetIndex();
                    return new NumberEval((double) sheetIndex + 1);
                } else if (arg instanceof AreaEval) {
                    // Argument is a cell range → return the sheet index of that area +1
                    AreaEval area = (AreaEval) arg;
                    int sheetIndex = area.getFirstSheetIndex();
                    return new NumberEval((double) sheetIndex + 1);
                } else if (arg instanceof StringEval) {
                    // Argument is a string (sheet name, e.g., "Sheet3") → look up the sheet index by name
                    String sheetName = ((StringEval) arg).getStringValue();
                    EvaluationWorkbook wb = ec.getWorkbook();
                    int sheetIndex = wb.getSheetIndex(sheetName);
                    if (sheetIndex >= 0) {
                        return new NumberEval((double) sheetIndex + 1);
                    } else {
                        // Sheet name not found → return #N/A error
                        return ErrorEval.NA;
                    }
                } else {
                    // Unsupported argument type → return #N/A error
                    return ErrorEval.NA;
                }
            }
        } catch (Exception e) {
            // Any unexpected exception (e.g., null pointers) → return #VALUE! error
            return ErrorEval.VALUE_INVALID;
        }
    }
}
