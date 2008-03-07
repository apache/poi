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

import org.apache.poi.hssf.record.formula.AreaPtg;
import org.apache.poi.hssf.record.formula.Ptg;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 *   
 */
public final class Area2DEval implements AreaEval {
// TODO -refactor with Area3DEval
    private final AreaPtg _delegate;

    private final ValueEval[] _values;

    public Area2DEval(Ptg ptg, ValueEval[] values) {
        if(ptg == null) {
            throw new IllegalArgumentException("ptg must not be null");
        }
        if(values == null) {
            throw new IllegalArgumentException("values must not be null");
        }
        for(int i=values.length-1; i>=0; i--) {
            if(values[i] == null) {
                throw new IllegalArgumentException("value array elements must not be null");
            }
        }
        // TODO - check size of array vs size of AreaPtg
        _delegate = (AreaPtg) ptg;
        _values = values;
    }

    public int getFirstColumn() {
        return _delegate.getFirstColumn();
    }

    public int getFirstRow() {
        return _delegate.getFirstRow();
    }

    public int getLastColumn() {
        return _delegate.getLastColumn();
    }

    public int getLastRow() {
        return _delegate.getLastRow();
    }

    public ValueEval[] getValues() {
        return _values;
    }
    
    public ValueEval getValueAt(int row, int col) {
        ValueEval retval;
        int index = ((row-getFirstRow())*(getLastColumn()-getFirstColumn()+1))+(col-getFirstColumn());
        if (index <0 || index >= _values.length)
            retval = ErrorEval.VALUE_INVALID;
        else 
            retval = _values[index];
        return retval;
    }
    
    public boolean contains(int row, int col) {
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
        return _delegate.getFirstColumn() == _delegate.getLastColumn();
    }

    public boolean isRow() {
        return _delegate.getFirstRow() == _delegate.getLastRow();
    }
}
