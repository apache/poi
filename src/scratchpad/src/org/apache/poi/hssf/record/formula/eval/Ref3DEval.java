/*
 * Created on May 9, 2005
 *
 */
package org.apache.poi.hssf.record.formula.eval;

import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.hssf.record.formula.Ref3DPtg;

/**
 * @author Amol S. Deshmukh
 *  
 */
public class Ref3DEval implements RefEval {

    private ValueEval value;

    private Ref3DPtg delegate;

    private boolean evaluated;

    public Ref3DEval(Ptg ptg, ValueEval value, boolean evaluated) {
        this.value = value;
        this.delegate = (Ref3DPtg) ptg;
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
