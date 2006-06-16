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
