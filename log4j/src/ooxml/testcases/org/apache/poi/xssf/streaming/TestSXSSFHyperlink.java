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

import org.junit.jupiter.api.AfterEach;

import org.apache.poi.ss.usermodel.BaseTestHyperlink;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.xssf.SXSSFITestDataProvider;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;

/**
 * Test setting hyperlinks in SXSSF
 *
 * @author Yegor Kozlov
 */
class TestSXSSFHyperlink extends BaseTestHyperlink {

    public TestSXSSFHyperlink() {
        super(SXSSFITestDataProvider.instance);
    }


    @AfterEach
    void tearDown(){
        SXSSFITestDataProvider.instance.cleanup();
    }

    @Override
    public XSSFHyperlink copyHyperlink(Hyperlink link) {
        // FIXME: replace with SXSSFHyperlink if it ever gets created
        return new XSSFHyperlink(link);
    }

}