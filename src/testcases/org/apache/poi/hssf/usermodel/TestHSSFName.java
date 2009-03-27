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

package org.apache.poi.hssf.usermodel;

import java.util.Date;
import java.util.GregorianCalendar;

import junit.framework.AssertionFailedError;
import org.apache.poi.hssf.HSSFITestDataProvider;
import org.apache.poi.hssf.model.Sheet;
import org.apache.poi.hssf.record.DBCellRecord;
import org.apache.poi.hssf.record.FormulaRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.StringRecord;
import org.apache.poi.ss.usermodel.ErrorConstants;
import org.apache.poi.ss.usermodel.BaseTestCell;
import org.apache.poi.ss.usermodel.BaseTestNamedRange;

/**
 * Tests various functionality having to do with {@link org.apache.poi.hssf.usermodel.HSSFCell}.  For instance support for
 * particular datatypes, etc.
 * @author Andrew C. Oliver (andy at superlinksoftware dot com)
 * @author  Dan Sherman (dsherman at isisph.com)
 * @author Alex Jacoby (ajacoby at gmail.com)
 */
public final class TestHSSFName extends BaseTestNamedRange {

    @Override
    protected HSSFITestDataProvider getTestDataProvider(){
        return HSSFITestDataProvider.getInstance();
    }

}