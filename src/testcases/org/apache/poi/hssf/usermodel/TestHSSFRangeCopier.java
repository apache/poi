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

import org.apache.poi.hssf.HSSFITestDataProvider;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.ss.usermodel.TestRangeCopier;
import org.junit.Before;

public class TestHSSFRangeCopier extends TestRangeCopier {

    public TestHSSFRangeCopier() {
        super(); 
        workbook = new HSSFWorkbook();
        testDataProvider = HSSFITestDataProvider.instance; 
    }

    @Before
    public void init() {
        workbook = HSSFTestDataSamples.openSampleWorkbook("tile-range-test.xls");
        initSheets();
        rangeCopier = new HSSFRangeCopier(sheet1, sheet1);
        transSheetRangeCopier = new HSSFRangeCopier(sheet1, sheet2);
    }
}
