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

package org.apache.poi.xssf;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPhoneticRun;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRow;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheetData;

// aim is to get these classes loaded and included in poi-ooxml-lite.jar
class TestNecessaryOOXMLClasses {

    @Test
    void testProblemClasses() {
        CTRow row = CTRow.Factory.newInstance();
        CTSheetData sheetData = CTSheetData.Factory.newInstance();
        // need to get the inner class that implements the row list class loaded
        assertTrue(sheetData.getRowList().add(row));
        assertTrue(sheetData.getRowList().iterator().hasNext());
        //important class missing in v5.0.0 poi-ooxml-lite
        CTPhoneticRun run = CTPhoneticRun.Factory.newInstance();
    }
}
