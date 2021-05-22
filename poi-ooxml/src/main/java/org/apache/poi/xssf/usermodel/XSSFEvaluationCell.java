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

package org.apache.poi.xssf.usermodel;

import org.apache.poi.ss.formula.EvaluationCell;
import org.apache.poi.ss.formula.EvaluationSheet;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 * XSSF wrapper for a cell under evaluation
 */
final class XSSFEvaluationCell implements EvaluationCell {

    private final EvaluationSheet _evalSheet;
    private final XSSFCell _cell;

    public XSSFEvaluationCell(XSSFCell cell, XSSFEvaluationSheet evaluationSheet) {
        _cell = cell;
        _evalSheet = evaluationSheet;
    }

    public XSSFEvaluationCell(XSSFCell cell) {
        this(cell, new XSSFEvaluationSheet(cell.getSheet()));
    }

    @Override
    public Object getIdentityKey() {
        // save memory by just using the cell itself as the identity key
        // Note - this assumes XSSFCell has not overridden hashCode and equals
        return _cell;
    }

    public XSSFCell getXSSFCell() {
        return _cell;
    }
    @Override
    public boolean getBooleanCellValue() {
        return _cell.getBooleanCellValue();
    }
    /**
     * @return cell type
     */
    @Override
    public CellType getCellType() {
        return _cell.getCellType();
    }
    @Override
    public int getColumnIndex() {
        return _cell.getColumnIndex();
    }
    @Override
    public int getErrorCellValue() {
        return _cell.getErrorCellValue();
    }
    @Override
    public double getNumericCellValue() {
        return _cell.getNumericCellValue();
    }
    @Override
    public int getRowIndex() {
        return _cell.getRowIndex();
    }
    @Override
    public EvaluationSheet getSheet() {
        return _evalSheet;
    }
    @Override
    public String getStringCellValue() {
        return _cell.getRichStringCellValue().getString();
    }
    
    @Override
    public CellRangeAddress getArrayFormulaRange() {
        return _cell.getArrayFormulaRange();
    }
    
    @Override
    public boolean isPartOfArrayFormulaGroup() {
        return _cell.isPartOfArrayFormulaGroup();
    }
    
    /**
     * @return cell type of cached formula result
     */
    @Override
    public CellType getCachedFormulaResultType() {
        return _cell.getCachedFormulaResultType();
    }
}
