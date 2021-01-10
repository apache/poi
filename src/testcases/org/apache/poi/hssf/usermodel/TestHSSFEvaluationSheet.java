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

package org.apache.poi.hssf.usermodel;

import static org.apache.poi.hssf.HSSFTestDataSamples.openSampleWorkbook;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.Map;

import org.apache.poi.ss.formula.EvaluationSheet;
import org.apache.poi.ss.usermodel.BaseTestXEvaluationSheet;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.jupiter.api.Test;

class TestHSSFEvaluationSheet extends BaseTestXEvaluationSheet {
    @Override
    protected Map.Entry<Sheet, EvaluationSheet> getInstance() {
        HSSFSheet sheet = new HSSFWorkbook().createSheet();
        return new AbstractMap.SimpleEntry<>(sheet, new HSSFEvaluationSheet(sheet));
    }

    @Test
    void testMissingExternalName() throws IOException {
        try (HSSFWorkbook wb = openSampleWorkbook("external_name.xls")) {
            // this sometimes causes exceptions
            wb.getAllNames().stream().filter(n -> !n.isFunctionName()).forEach(
                n -> assertDoesNotThrow(n::getRefersToFormula)
            );
        }
    }
}
