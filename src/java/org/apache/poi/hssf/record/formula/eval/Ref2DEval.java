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

package org.apache.poi.hssf.record.formula.eval;

import org.apache.poi.hssf.record.formula.RefPtg;

/**
 * @author adeshmukh
 *  
 */
public final class Ref2DEval implements RefEval {

    private final ValueEval value;
    private final RefPtg delegate;
    
    public Ref2DEval(RefPtg ptg, ValueEval ve) {
        if(ve == null) {
            throw new IllegalArgumentException("ve must not be null");
        }
        if(false && ptg == null) { // TODO - fix dodgy code in MultiOperandNumericFunction
            throw new IllegalArgumentException("ptg must not be null");
        }
        value = ve;
        delegate = ptg;
    }
    public ValueEval getInnerValueEval() {
        return value;
    }
    public int getRow() {
        return delegate.getRow();
    }
    public int getColumn() {
        return delegate.getColumn();
    }
}
