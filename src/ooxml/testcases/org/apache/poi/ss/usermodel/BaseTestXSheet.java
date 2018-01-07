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

package org.apache.poi.ss.usermodel;

import org.apache.poi.ss.ITestDataProvider;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFSheet;

/**
 * Class for combined testing of XML-specific functionality of 
 * {@link XSSFSheet} and {@link SXSSFSheet}.
 * 
 *  Any test that is applicable for {@link org.apache.poi.hssf.usermodel.HSSFSheet} as well should go into
 *  the common base class {@link BaseTestSheet}.
 */
public abstract class BaseTestXSheet extends BaseTestSheet {
    protected BaseTestXSheet(ITestDataProvider testDataProvider) {
        super(testDataProvider);
    }
}
