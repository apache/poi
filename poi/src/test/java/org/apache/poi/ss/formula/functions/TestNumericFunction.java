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
package org.apache.poi.ss.formula.functions;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.jupiter.api.Test;

import static org.apache.poi.ss.util.Utils.assertDouble;

final class TestNumericFunction {

    @Test
    void testINT() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFCell cell = wb.createSheet().createRow(0).createCell(0);
        HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
        assertDouble(fe, cell, "880000000*0.00849", 7471200.0, 0.00000001);
        assertDouble(fe, cell, "880000000*0.00849/3", 2490400.0, 0.00000001);
        assertDouble(fe, cell, "INT(880000000*0.00849/3)", 2490400.0, 0.00000001);
    }
}
