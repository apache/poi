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
package org.apache.poi.xssf.streaming;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.apache.poi.ss.usermodel.BaseTestDataValidation;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.SXSSFITestDataProvider;
import org.junit.jupiter.api.Test;

class TestSXSSFDataValidation extends BaseTestDataValidation {

    public TestSXSSFDataValidation(){
        super(SXSSFITestDataProvider.instance);
    }

    @Test
    void test53965() throws Exception {
        try (SXSSFWorkbook wb = new SXSSFWorkbook()) {
            Sheet sheet = wb.createSheet();
            List<? extends DataValidation> lst = sheet.getDataValidations();    //<-- works
            assertEquals(0, lst.size());

            //create the cell that will have the validation applied
            sheet.createRow(0).createCell(0);

            DataValidationHelper dataValidationHelper = sheet.getDataValidationHelper();
            DataValidationConstraint constraint = dataValidationHelper.createCustomConstraint("SUM($A$1:$A$1) <= 3500");
            CellRangeAddressList addressList = new CellRangeAddressList(0, 0, 0, 0);
            DataValidation validation = dataValidationHelper.createValidation(constraint, addressList);
            sheet.addValidationData(validation);

            // this line caused XmlValueOutOfRangeException , see Bugzilla 3965
            lst = sheet.getDataValidations();
            assertEquals(1, lst.size());
        }
    }
}
