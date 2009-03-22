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

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.ITestDataProvider;
import org.apache.poi.xssf.XSSFITestDataProvider;

/**
 * @author Yegor Kozlov
 */
public final class TestXSSFCell extends BaseTestCell {

    @Override
    protected ITestDataProvider getTestDataProvider(){
        return XSSFITestDataProvider.getInstance();
    }

    public void testSetValues() {
        baseTestSetValues();
    }

   public void testBoolErr() {
        baseTestBoolErr();
    }

    public void testFormulaStyle() {
        baseTestFormulaStyle();
    }

    public void testToString() {
        baseTestToString();
    }

    public void testSetFormulaValue() {
        baseTestSetFormulaValue();
    }

    public void testChangeCellType() {
        Workbook wb = getTestDataProvider().createWorkbook();
        Row row = wb.createSheet().createRow(0);
        baseTestChangeTypeStringToBool(row.createCell(0));
        baseTestChangeTypeBoolToString(row.createCell(1));
        baseTestChangeTypeErrorToNumber(row.createCell(2));
        baseTestChangeTypeErrorToBoolean(row.createCell(3));

        //TODO: works in HSSF but fails in XSSF
        //baseTestChangeTypeFormulaToBoolean(row.createCell(4));
    }


}