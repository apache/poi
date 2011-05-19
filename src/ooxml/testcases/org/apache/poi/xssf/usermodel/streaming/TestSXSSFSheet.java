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

package org.apache.poi.xssf.usermodel.streaming;

import org.apache.poi.ss.usermodel.BaseTestSheet;
import org.apache.poi.xssf.SXSSFITestDataProvider;


public class TestSXSSFSheet extends BaseTestSheet {

    public TestSXSSFSheet() {
        super(SXSSFITestDataProvider.instance);
    }

    /**
     * cloning of sheets is not supported in SXSSF
     */
    @Override
    public void testCloneSheet() {
        try {
            super.testCloneSheet();
            fail("expected exception");
        } catch (RuntimeException e){
            assertEquals("NotImplemented", e.getMessage());
        }
    }

    @Override
    public void testCloneSheetMultipleTimes() {
        try {
            super.testCloneSheetMultipleTimes();
            fail("expected exception");
        } catch (RuntimeException e){
            assertEquals("NotImplemented", e.getMessage());
        }
    }
    /**
     * shifting rows is not supported in SXSSF
     */
    @Override
    public void testShiftMerged(){
        try {
            super.testShiftMerged();
            fail("expected exception");
        } catch (RuntimeException e){
            assertEquals("NotImplemented", e.getMessage());
        }
    }

    /**
     *  Bug 35084: cloning cells with formula
     *
     *  The test is disabled because cloning of sheets is not supported in SXSSF
     */
    @Override
    public void test35084(){
        try {
            super.test35084();
            fail("expected exception");
        } catch (RuntimeException e){
            assertEquals("NotImplemented", e.getMessage());
        }
    }

    @Override
    public void testDefaultColumnStyle() {
        //TODO column styles are not yet supported by XSSF
    }
}
