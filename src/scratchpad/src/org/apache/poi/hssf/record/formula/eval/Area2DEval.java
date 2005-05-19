/*
 * Created on May 8, 2005
 *
 */
package org.apache.poi.hssf.record.formula.eval;

import org.apache.poi.hssf.record.formula.AreaPtg;
import org.apache.poi.hssf.record.formula.Ptg;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 *   
 */
public class Area2DEval implements AreaEval {

    private AreaPtg delegate;

    private ValueEval[] values;

    public Area2DEval(Ptg ptg, ValueEval[] values) {
        this.delegate = (AreaPtg) ptg;
        this.values = values;
    }

    public short getFirstColumn() {
        return delegate.getFirstColumn();
    }

    public int getFirstRow() {
        return delegate.getFirstRow();
    }

    public short getLastColumn() {
        return delegate.getLastColumn();
    }

    public int getLastRow() {
        return delegate.getLastRow();
    }

    public ValueEval[] getValues() {
        return values;
    }
    
    public ValueEval getValueAt(int row, short col) {
        ValueEval retval;
        int index = ((row-getFirstRow())*(getLastColumn()-getFirstColumn()+1))+(col-getFirstColumn());
        if (index <0 || index >= values.length)
            retval = ErrorEval.VALUE_INVALID;
        else 
            retval = values[index];
        return retval;
    }
    
    public boolean contains(int row, short col) {
        return (getFirstRow() <= row) && (getLastRow() >= row) 
            && (getFirstColumn() <= col) && (getLastColumn() >= col);
    }
    
    public boolean containsRow(int row) {
        return (getFirstRow() <= row) && (getLastRow() >= row);
    }
    
    public boolean containsColumn(short col) {
        return (getFirstColumn() <= col) && (getLastColumn() >= col);
    }
    
    public boolean isColumn() {
        return delegate.getFirstColumn() == delegate.getLastColumn();
    }

    public boolean isRow() {
        return delegate.getFirstRow() == delegate.getLastRow();
    }
}
