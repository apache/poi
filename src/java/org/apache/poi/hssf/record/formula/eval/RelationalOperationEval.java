/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
/*
 * Created on May 10, 2005
 *
 */
package org.apache.poi.hssf.record.formula.eval;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 *
 */
public abstract class RelationalOperationEval implements OperationEval {

    protected class RelationalValues {
        public Double[] ds = new Double[2];
        public Boolean[] bs = new Boolean[2];
        public String[] ss = new String[3];
        public ErrorEval ee = null;
    }

    
    /*
     * This is a description of how the relational operators apply in MS Excel.
     * Use this as a guideline when testing/implementing the evaluate methods 
     * for the relational operators Evals.
     * 
     * Bool > any number. ALWAYS
     * Bool > any string. ALWAYS
     * Bool.TRUE > Bool.FALSE
     * 
     * String > any number. ALWAYS
     * String > Blank. ALWAYS
     * String are sorted dictionary wise
     * 
     * Blank == 0 (numeric)
     */
    public RelationalValues doEvaluate(Eval[] operands, int srcRow, short srcCol) {
        RelationalValues retval = new RelationalValues();
        
        switch (operands.length) {
        default:
            retval.ee = ErrorEval.VALUE_INVALID;
            break;
        case 2:
            internalDoEvaluate(operands, srcRow, srcCol, retval, 0);
            internalDoEvaluate(operands, srcRow, srcCol, retval, 1);
        } // end switch
        return retval;
    }
    
    /**
     * convenience method to avoid code duplication for multiple operands
     * @param operands
     * @param srcRow
     * @param srcCol
     * @param retval
     * @param index
     */
    private void internalDoEvaluate(Eval[] operands, int srcRow, short srcCol, RelationalValues retval, int index) {
        if (operands[index] instanceof BoolEval) {
            BoolEval be = (BoolEval) operands[index];
            retval.bs[index] = Boolean.valueOf(be.getBooleanValue());
        }
        else if (operands[index] instanceof NumericValueEval) {
            NumericValueEval ne = (NumericValueEval) operands[index];
            retval.ds[index] = new Double(ne.getNumberValue());
        }
        else if (operands[index] instanceof StringValueEval) {
            StringValueEval se = (StringValueEval) operands[index];
            retval.ss[index] = se.getStringValue();
        }
        else if (operands[index] instanceof RefEval) {
            RefEval re = (RefEval) operands[index];
            ValueEval ve = re.getInnerValueEval();
            if (ve instanceof BoolEval) {
                BoolEval be = (BoolEval) ve;
                retval.bs[index] = Boolean.valueOf(be.getBooleanValue());
            }
            else if (ve instanceof BlankEval) {
                retval.ds[index] = new Double(0);
            }
            else if (ve instanceof NumericValueEval) {
                NumericValueEval ne = (NumericValueEval) ve;
                retval.ds[index] = new Double(ne.getNumberValue());
            }
            else if (ve instanceof StringValueEval) {
                StringValueEval se = (StringValueEval) ve;
                retval.ss[index] = se.getStringValue();
            }
        }
        else if (operands[index] instanceof AreaEval) {
            AreaEval ae = (AreaEval) operands[index];
            if (ae.isRow()) {
                if (ae.containsColumn(srcCol)) {
                    ValueEval ve = ae.getValueAt(ae.getFirstRow(), srcCol);
                    if (ve instanceof BoolEval) {
                        BoolEval be = (BoolEval) ve;
                        retval.bs[index] = Boolean.valueOf(be.getBooleanValue());
                    }
                    else if (ve instanceof BlankEval) {
                        retval.ds[index] = new Double(0);
                    }
                    else if (ve instanceof NumericValueEval) {
                        NumericValueEval ne = (NumericValueEval) ve;
                        retval.ds[index] = new Double(ne.getNumberValue());
                    }
                    else if (ve instanceof StringValueEval) {
                        StringValueEval se = (StringValueEval) ve;
                        retval.ss[index] = se.getStringValue();
                    }
                    else {
                        retval.ee = ErrorEval.VALUE_INVALID;
                    }
                }
                else {
                    retval.ee = ErrorEval.VALUE_INVALID;
                }
            }
            else if (ae.isColumn()) {
                if (ae.containsRow(srcRow)) {
                    ValueEval ve = ae.getValueAt(srcRow, ae.getFirstColumn());
                    if (ve instanceof BoolEval) {
                        BoolEval be = (BoolEval) ve;
                        retval.bs[index] = Boolean.valueOf(be.getBooleanValue());
                    }
                    else if (ve instanceof BlankEval) {
                        retval.ds[index] = new Double(0);
                    }
                    else if (ve instanceof NumericValueEval) {
                        NumericValueEval ne = (NumericValueEval) ve;
                        retval.ds[index] = new Double(ne.getNumberValue());
                    }
                    else if (ve instanceof StringValueEval) {
                        StringValueEval se = (StringValueEval) ve;
                        retval.ss[index] = se.getStringValue();
                    }
                    else {
                        retval.ee = ErrorEval.VALUE_INVALID;
                    }
                }
                else {
                    retval.ee = ErrorEval.VALUE_INVALID;
                }
            }
            else {
                retval.ee = ErrorEval.VALUE_INVALID;
            }
        }
    }
    
    // if both null return 0, else non null wins, else TRUE wins
    protected int doComparison(Boolean[] bs) {
        int retval = 0;
        if (bs[0] != null || bs[1] != null) {
            retval = bs[0] != null
                    ? bs[1] != null
                            ? bs[0].booleanValue()
                                    ? bs[1].booleanValue()
                                            ? 0
                                            : 1
                                    : bs[1].booleanValue()
                                            ? -1
                                            : 0
                            : 1
                    : bs[1] != null
                            ? -1
                            : 0;
        }
        return retval;
    }

    // if both null return 0, else non null wins, else string compare
    protected int doComparison(String[] ss) {
        int retval = 0;
        if (ss[0] != null || ss[1] != null) {
            retval = ss[0] != null
                    ? ss[1] != null
                            ? ss[0].compareTo(ss[1])
                            : 1
                    : ss[1] != null
                            ? -1
                            : 0;
        }
        return retval;
    }

    // if both null return 0, else non null wins, else doublevalue compare
    protected int doComparison(Double[] ds) {
        int retval = 0;
        if (ds[0] != null || ds[1] != null) {
            retval = ds[0] != null
                    ? ds[1] != null
                            ? ds[0].compareTo(ds[1])
                            : 1
                    : ds[1] != null
                            ? -1
                            : 0;
        }
        return retval;
    }
}
