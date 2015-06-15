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
package org.apache.poi.ss.formula;

import org.apache.poi.hssf.usermodel.HSSFEvaluationWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFEvaluationWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import junit.framework.TestCase;

/**
 * Test {@link FormulaParser}'s handling of row numbers at the edge of the
 * HSSF/XSSF ranges.
 * 
 * @author David North
 */
public class TestFormulaParser extends TestCase {

    public void testHSSFFailsForOver65536() {
        FormulaParsingWorkbook workbook = HSSFEvaluationWorkbook.create(new HSSFWorkbook());
        try {
            FormulaParser.parse("Sheet1!1:65537", workbook, FormulaType.CELL, 0);
            fail("Expected exception");
        }
        catch (FormulaParseException expected) {
        }
    }

    public void testHSSFPassCase() {
        FormulaParsingWorkbook workbook = HSSFEvaluationWorkbook.create(new HSSFWorkbook());
        FormulaParser.parse("Sheet1!1:65536", workbook, FormulaType.CELL, 0);
    }

    public void testXSSFWorksForOver65536() {
        FormulaParsingWorkbook workbook = XSSFEvaluationWorkbook.create(new XSSFWorkbook());
        FormulaParser.parse("Sheet1!1:65537", workbook, FormulaType.CELL, 0);
    }

    public void testXSSFFailCase() {
        FormulaParsingWorkbook workbook = XSSFEvaluationWorkbook.create(new XSSFWorkbook());
        try {
            FormulaParser.parse("Sheet1!1:1048577", workbook, FormulaType.CELL, 0); // one more than max rows.
            fail("Expected exception");
        }
        catch (FormulaParseException expected) {
        }
    }

}
