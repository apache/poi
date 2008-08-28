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

package org.apache.poi.hssf.record.aggregates;

import org.apache.poi.hssf.record.CellValueRecordInterface;
import org.apache.poi.hssf.record.FormulaRecord;
import org.apache.poi.hssf.record.StringRecord;

/**
 * The formula record aggregate is used to join together the formula record and it's
 * (optional) string record and (optional) Shared Formula Record (template reads, excel optimization).
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public final class FormulaRecordAggregate extends RecordAggregate implements CellValueRecordInterface {

    private FormulaRecord _formulaRecord;
    private StringRecord _stringRecord;
    
    public FormulaRecordAggregate( FormulaRecord formulaRecord, StringRecord stringRecord )
    {
        _formulaRecord = formulaRecord;
        _stringRecord = stringRecord;
    }

    public void setStringRecord( StringRecord stringRecord ) {
        _stringRecord = stringRecord;
    }

    public void setFormulaRecord( FormulaRecord formulaRecord )
    {
        _formulaRecord = formulaRecord;
    }
    
    public FormulaRecord getFormulaRecord()
    {
        return _formulaRecord;
    }

    public StringRecord getStringRecord()
    {
        return _stringRecord;
    }
    
    public short getXFIndex()
    {
        return _formulaRecord.getXFIndex();
    }

    public void setXFIndex(short xf)
    {
        _formulaRecord.setXFIndex( xf );
    }

    public void setColumn(short col)
    {
        _formulaRecord.setColumn( col );
    }

    public void setRow(int row)
    {
        _formulaRecord.setRow( row );
    }

    public short getColumn()
    {
        return _formulaRecord.getColumn();
    }

    public int getRow()
    {
        return _formulaRecord.getRow();
    }

    public String toString() {
        return _formulaRecord.toString();
    }
   
    public void visitContainedRecords(RecordVisitor rv) {
         rv.visitRecord(_formulaRecord);
         if (_stringRecord != null) {
              rv.visitRecord(_stringRecord);
         }
    }
   
    public String getStringValue() {
        if(_stringRecord==null) {
            return null;
        }
        return _stringRecord.getString();
    }
}
