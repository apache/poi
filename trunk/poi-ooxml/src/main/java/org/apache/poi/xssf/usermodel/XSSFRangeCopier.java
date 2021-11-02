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

package org.apache.poi.xssf.usermodel;

import org.apache.poi.ss.formula.FormulaParser;
import org.apache.poi.ss.formula.FormulaRenderer;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.RangeCopier;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.util.Beta;

@Beta
public class XSSFRangeCopier extends RangeCopier {

    public XSSFRangeCopier(Sheet sourceSheet, Sheet destSheet) {
        super(sourceSheet, destSheet);
    }

    public XSSFRangeCopier(Sheet sheet) {
        super(sheet);
    }

    protected void adjustCellReferencesInsideFormula(Cell cell, Sheet destSheet, int deltaX, int deltaY){
        XSSFWorkbook hostWorkbook = (XSSFWorkbook) destSheet.getWorkbook();
        XSSFEvaluationWorkbook fpb = XSSFEvaluationWorkbook.create(hostWorkbook);
        Ptg[] ptgs = FormulaParser.parse(cell.getCellFormula(), fpb, FormulaType.CELL, 0);
        int destSheetIndex = hostWorkbook.getSheetIndex(destSheet);
        if(adjustInBothDirections(ptgs, destSheetIndex, deltaX, deltaY))
            cell.setCellFormula(FormulaRenderer.toFormulaString(fpb, ptgs));
    }
}
