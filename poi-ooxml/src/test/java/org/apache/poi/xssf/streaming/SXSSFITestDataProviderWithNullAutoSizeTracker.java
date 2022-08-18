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

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.SXSSFITestDataProvider;

public class SXSSFITestDataProviderWithNullAutoSizeTracker extends SXSSFITestDataProvider {
    public static final SXSSFITestDataProviderWithNullAutoSizeTracker instance = new SXSSFITestDataProviderWithNullAutoSizeTracker();

    private SXSSFITestDataProviderWithNullAutoSizeTracker() {
        // enforce singleton
    }

    @Override
    public SXSSFWorkbook createWorkbook() {
        SXSSFWorkbook wb = new SXSSFWorkbookWithNullAutoSizeTracker();
        instances.add(wb);
        return wb;
    }

    //************ SXSSF-specific methods ***************//
    @Override
    public SXSSFWorkbook createWorkbook(int rowAccessWindowSize) {
        SXSSFWorkbook wb = new SXSSFWorkbookWithNullAutoSizeTracker(rowAccessWindowSize);
        instances.add(wb);
        return wb;
    }

    @Override
    public void trackAllColumnsForAutosizing(Sheet sheet) {
        ((SXSSFSheet)sheet).trackAllColumnsForAutoSizing();
    }
    //************ End SXSSF-specific methods ***************//

    private static class SXSSFWorkbookWithNullAutoSizeTracker extends SXSSFWorkbook {
        SXSSFWorkbookWithNullAutoSizeTracker() {
            super();
        }

        SXSSFWorkbookWithNullAutoSizeTracker(int rowAccessWindowSize) {
            super(rowAccessWindowSize);
        }

        @Override
        public SXSSFSheet createSheet() {
            SXSSFSheet sheet = super.createSheet();
            sheet._autoSizeColumnTracker = null;
            return sheet;
        }

        @Override
        public SXSSFSheet createSheet(String name) {
            SXSSFSheet sheet = super.createSheet(name);
            sheet._autoSizeColumnTracker = null;
            return sheet;
        }
    }
}
