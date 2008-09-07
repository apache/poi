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

package org.apache.poi.hssf.usermodel;

import org.apache.poi.hssf.model.FormulaParser;
import org.apache.poi.hssf.record.formula.OperationPtg;
import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 * 
 */
public class HSSFFormulaEvaluator extends FormulaEvaluator {
    public HSSFFormulaEvaluator(HSSFSheet sheet, HSSFWorkbook workbook) {
    	super(sheet, workbook);
    }

    /**
     * Returns an underlying FormulaParser, for the specified
     *  Formula String and HSSFWorkbook.
     * This will allow you to generate the Ptgs yourself, if
     *  your needs are more complex than just having the
     *  formula evaluated. 
     */
    public static FormulaParser getUnderlyingParser(Workbook workbook, String formula) {
        return new FormulaParser(formula, workbook);
    }
    
    /**
     * debug method
     */
    void inspectPtgs(String formula) {
        Ptg[] ptgs = FormulaParser.parse(formula, _workbook);
        System.out.println("<ptg-group>");
        for (int i = 0, iSize = ptgs.length; i < iSize; i++) {
            System.out.println("<ptg>");
            System.out.println(ptgs[i]);
            if (ptgs[i] instanceof OperationPtg) {
                System.out.println("numoperands: " + ((OperationPtg) ptgs[i]).getNumberOfOperands());
            }
            System.out.println("</ptg>");
        }
        System.out.println("</ptg-group>");
    }

	/**
     * Compatibility class.
     * Seems to do more harm than good though
     */
//    public static class CellValue extends FormulaEvaluator.CellValue {
//		public CellValue(int cellType, CreationHelper creationHelper) {
//			super(cellType, creationHelper);
//		}
//    }
}
