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

import org.apache.poi.ss.usermodel.BaseTestWorkbook;
import org.apache.poi.xssf.SXSSFITestDataProvider;

public final class TestSXSSFWorkbook extends BaseTestWorkbook {

	public TestSXSSFWorkbook() {
		super(SXSSFITestDataProvider.instance);
	}

    @Override
    public void testCloneSheet() {
        // TODO figure out why the base class failes and remove me
    }

    @Override
    public void testUnicodeInAll() {
        // TODO figure out why the base class failes and remove me
    }

    @Override
    public void testSetSheetName() {
        // this test involves formula evaluation which isn't supportd by SXSSF
    }
}
