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
 * Created on May 9, 2005
 *
 */
package org.apache.poi.hssf.record.formula.eval;

import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.hssf.record.formula.ReferencePtg;

/**
 * @author adeshmukh
 *  
 */
public class Ref2DEval implements RefEval {

    private ValueEval value;

    private ReferencePtg delegate;
    
    private boolean evaluated;

    public Ref2DEval(Ptg ptg, ValueEval value, boolean evaluated) {
        this.value = value;
        this.delegate = (ReferencePtg) ptg;
        this.evaluated = evaluated;
    }

    public ValueEval getInnerValueEval() {
        return value;
    }

    public short getRow() {
        return delegate.getRow();
    }

    public short getColumn() {
        return delegate.getColumn();
    }
    
    public boolean isEvaluated() {
        return evaluated;
    }

}
