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
 * Created on May 8, 2005
 *
 */
package org.apache.poi.hssf.record.formula.eval;

import org.apache.poi.hssf.record.formula.Area3DPtg;
import org.apache.poi.hssf.record.formula.Ptg;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 *  
 */
public class Area3DEval implements AreaEval {

    private Area3DPtg delegate;

    private ValueEval[] values;

    public Area3DEval(Ptg ptg, ValueEval[] values) {
        this.values = values;
        this.delegate = (Area3DPtg) ptg;
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
        int index = (row-getFirstRow())*(col-getFirstColumn());
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
