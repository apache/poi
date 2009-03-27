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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Iterator;

import junit.framework.TestCase;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.XSSFITestDataProvider;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFCell;

/**
 * Tests for XSSFRow
 */
public final class TestXSSFRow extends BaseTestRow {

    @Override
    protected XSSFITestDataProvider getTestDataProvider(){
        return XSSFITestDataProvider.getInstance();
    }

    public void testRowBounds() {
        baseTestRowBounds(XSSFRow.MAX_ROW_NUMBER);
    }

    public void testCellBounds() {
        baseTestCellBounds(XSSFCell.LAST_COLUMN_NUMBER);
    }
}
