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

package org.apache.poi.ss.tests.util;

import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.BaseTestCellUtilCopy;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class TestXSSFCellUtilCopy extends BaseTestCellUtilCopy {

    @Override
    protected Workbook createNewWorkbook() {
        return new XSSFWorkbook();
    }

    @Override
    protected boolean compareRichText(RichTextString rts1, RichTextString rts2) {
        if (rts1 instanceof XSSFRichTextString && rts2 instanceof XSSFRichTextString) {
            XSSFRichTextString xrts1 = (XSSFRichTextString)rts1;
            XSSFRichTextString xrts2 = (XSSFRichTextString)rts2;
            return xrts1.getCTRst().xmlText().equals(xrts2.getCTRst().xmlText());
        } else {
            return super.compareRichText(rts1, rts2);
        }
    }
}
