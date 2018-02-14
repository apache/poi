/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.hssf.usermodel;

import org.apache.poi.hssf.record.aggregates.FormulaRecordAggregate;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.RangeCopier;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.util.Beta;

@Beta
public class HSSFRangeCopier extends RangeCopier {
    public HSSFRangeCopier(Sheet sourceSheet, Sheet destSheet) {
        super(sourceSheet, destSheet);
    }

    protected void adjustCellReferencesInsideFormula(Cell cell, Sheet destSheet, int deltaX, int deltaY) {
        FormulaRecordAggregate fra = (FormulaRecordAggregate)((HSSFCell)cell).getCellValueRecord();
        int destSheetIndex = destSheet.getWorkbook().getSheetIndex(destSheet);
        Ptg[] ptgs = fra.getFormulaTokens();
        if(adjustInBothDirections(ptgs, destSheetIndex, deltaX, deltaY))
            fra.setParsedExpression(ptgs);
    }
}
