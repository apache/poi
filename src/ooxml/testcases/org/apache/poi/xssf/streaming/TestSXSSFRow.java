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

package org.apache.poi.xssf.streaming;

import java.io.IOException;

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.BaseTestRow;
import org.apache.poi.xssf.SXSSFITestDataProvider;
import org.junit.After;
import org.junit.Test;

/**
 * Tests for XSSFRow
 */
public final class TestSXSSFRow extends BaseTestRow {

    public TestSXSSFRow() {
        super(SXSSFITestDataProvider.instance);
    }


    @After
    public void tearDown() {
        SXSSFITestDataProvider.instance.cleanup();
    }

    @Test
    public void testRowBounds() throws IOException {
        baseTestRowBounds(SpreadsheetVersion.EXCEL2007.getLastRowIndex());
    }

    @Test
    public void testCellBounds() throws IOException {
        baseTestCellBounds(SpreadsheetVersion.EXCEL2007.getLastColumnIndex());
    }

}
