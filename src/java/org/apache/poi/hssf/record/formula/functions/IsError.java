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

package org.apache.poi.hssf.record.formula.functions;

import org.apache.poi.hssf.record.formula.eval.AreaEval;
import org.apache.poi.hssf.record.formula.eval.BoolEval;
import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.RefEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 *
 */
public final class IsError implements Function {

    public ValueEval evaluate(ValueEval[] operands, int srcCellRow, short srcCellCol) {
        ValueEval retval = null;
        boolean b = false;

        switch (operands.length) {
        default:
            retval = ErrorEval.VALUE_INVALID;
            break;
        case 1:
            if (operands[0] instanceof ErrorEval) {
                b = true;
            }
            else if (operands[0] instanceof AreaEval) {
                AreaEval ae = (AreaEval) operands[0];
                if (ae.contains(srcCellRow, srcCellCol)) { // circular ref!
                    retval = ErrorEval.CIRCULAR_REF_ERROR;
                }
                else if (ae.isRow()) {
                    if (ae.containsColumn(srcCellCol)) {
                        ValueEval ve = ae.getValueAt(ae.getFirstRow(), srcCellCol);
                        if (ve instanceof RefEval)
                            b = ((RefEval) ve).getInnerValueEval() instanceof ErrorEval;
                        else
                            b = (ve instanceof ErrorEval);
                    }
                    else {
                        b = true;
                    }
                }
                else if (ae.isColumn()) {
                    if (ae.containsRow(srcCellRow)) {
                        ValueEval ve = ae.getValueAt(srcCellRow, ae.getFirstColumn());
                        if (ve instanceof RefEval)
                            b = ((RefEval) ve).getInnerValueEval() instanceof ErrorEval;
                        else
                            b = (ve instanceof ErrorEval);
                    }
                    else {
                        b = true;
                    }
                }
                else {
                    b = true;
                }
            }
            else if (operands[0] instanceof RefEval) {
                b = ((RefEval) operands[0]).getInnerValueEval() instanceof ErrorEval;
            }
            else {
                b = false;
            }
        }

        if (retval == null) {
            retval = b
                    ? BoolEval.TRUE
                    : BoolEval.FALSE;
        }
        return retval;
    }
}
