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

import java.io.IOException;

import org.apache.poi.hssf.HSSFITestDataProvider;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.ss.usermodel.BaseTestSheetShiftColumns;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class TestHSSFSheetShiftColumns extends BaseTestSheetShiftColumns {
    public TestHSSFSheetShiftColumns() {
        super();
        workbook = new HSSFWorkbook();
        _testDataProvider = HSSFITestDataProvider.instance;
    }

    protected Workbook openWorkbook(String spreadsheetFileName) {
        return HSSFTestDataSamples.openSampleWorkbook(spreadsheetFileName);
    }

    protected Workbook getReadBackWorkbook(Workbook wb) {
        return HSSFTestDataSamples.writeOutAndReadBack((HSSFWorkbook)wb);
    }

    @Override
    @Disabled("see <https://bz.apache.org/bugzilla/show_bug.cgi?id=62030>")
    protected void shiftMergedColumnsToMergedColumnsLeft() {
        // This override is used only in order to test failing for HSSF. Please remove method after code is fixed on hssf,
        // so that original method from BaseTestSheetShiftColumns can be executed.
    }

    @Override
    @Disabled("see <https://bz.apache.org/bugzilla/show_bug.cgi?id=62030>")
    protected void shiftMergedColumnsToMergedColumnsRight() {
        // This override is used only in order to test failing for HSSF. Please remove method after code is fixed on hssf,
        // so that original method from BaseTestSheetShiftColumns can be executed.
    }

    @Override
    @Disabled("see <https://bz.apache.org/bugzilla/show_bug.cgi?id=62030>")
    protected void testBug54524() {
        // This override is used only in order to test failing for HSSF. Please remove method after code is fixed on hssf,
        // so that original method from BaseTestSheetShiftColumns can be executed.
    }

    @Override
    @Disabled("see <https://bz.apache.org/bugzilla/show_bug.cgi?id=62030>")
    protected void testCommentsShifting() {
        // This override is used only in order to test failing for HSSF. Please remove method after code is fixed on hssf,
        // so that original method from BaseTestSheetShiftColumns can be executed.
    }

    @Override
    @Disabled("see <https://bz.apache.org/bugzilla/show_bug.cgi?id=62030>")
    protected void testShiftWithMergedRegions() {
        // This override is used only in order to test failing for HSSF. Please remove method after code is fixed on hssf,
        // so that original method from BaseTestSheetShiftColumns can be executed.
        // After removing, you can re-add 'final' keyword to specification of original method.
    }

    @Override
    @Disabled("see <https://bz.apache.org/bugzilla/show_bug.cgi?id=62030>")
    protected void testShiftHyperlinks() {}
}
