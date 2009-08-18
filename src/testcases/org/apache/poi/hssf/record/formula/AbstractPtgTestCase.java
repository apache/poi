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

package org.apache.poi.hssf.record.formula;

import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 * Convenient abstract class to reduce the amount of boilerplate code needed
 * in ptg-related unit tests.
 *
 * @author Daniel Noll (daniel at nuix dot com dot au)
 */
public abstract class AbstractPtgTestCase extends TestCase {

    /**
     * Loads a workbook from the given filename in the test data dir.
     *
     * @param sampleFileName the filename.
     * @return the loaded workbook.
     */
    protected static final HSSFWorkbook loadWorkbook(String sampleFileName) {
        return HSSFTestDataSamples.openSampleWorkbook(sampleFileName);
    }

    /**
     * Creates a new Workbook and adds one sheet with the specified name
     */
    protected static final HSSFWorkbook createWorkbookWithSheet(String sheetName) {
        HSSFWorkbook book = new HSSFWorkbook();
        book.createSheet(sheetName);
        return book;
    }
}
