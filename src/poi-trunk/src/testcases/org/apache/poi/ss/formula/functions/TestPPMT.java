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

package org.apache.poi.ss.formula.functions;

import junit.framework.TestCase;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 * Test cases for PPMT()
 *
 */
public final class TestPPMT extends TestCase {


    /**
     *  http://office.microsoft.com/en-001/excel-help/ppmt-function-HP010342774.aspx
     */
    public void testFromFile() {

        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("finance.xls");
        HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);

        HSSFSheet example1 = wb.getSheet("PPMT");
        HSSFCell ex1cell1 = example1.getRow(5).getCell(0);
        fe.evaluate(ex1cell1);
        assertEquals(-75.62, ex1cell1.getNumericCellValue(), 0.1);

        HSSFCell ex1cell2 = example1.getRow(16).getCell(0);
        fe.evaluate(ex1cell2);
        assertEquals(-27598.05, ex1cell2.getNumericCellValue(), 0.1);

    }
}
