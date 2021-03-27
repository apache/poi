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

import org.apache.poi.ss.tests.usermodel.BaseTestXRow;
import org.apache.poi.xssf.SXSSFITestDataProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Tests for XSSFRow
 */
public final class TestSXSSFRow extends BaseTestXRow {

    public TestSXSSFRow() {
        super(SXSSFITestDataProvider.instance);
    }


    @AfterEach
    void tearDown() {
        ((SXSSFITestDataProvider) _testDataProvider).cleanup();
    }

    @Override
    @Disabled("see <https://bz.apache.org/bugzilla/show_bug.cgi?id=62030#c1>")
    protected void testCellShiftingRight(){
        // Remove when SXSSFRow.shiftCellsRight() is implemented.
    }

    @Override
    @Disabled("see <https://bz.apache.org/bugzilla/show_bug.cgi?id=62030#c1>")
    protected void testCellShiftingLeft(){
        // Remove when SXSSFRow.shiftCellsLeft() is implemented.
    }


}
