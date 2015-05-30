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

import org.apache.poi.ss.usermodel.CreationHelper;

public class HSSFCreationHelper implements CreationHelper {

    private final HSSFWorkbook workbook;
    private final HSSFDataFormat dataFormat;

    HSSFCreationHelper(HSSFWorkbook workbook) {
        this.workbook = workbook;

        // Create the things we only ever need one of
        dataFormat = new HSSFDataFormat(workbook.getWorkbook());
    }

    public HSSFRichTextString createRichTextString(String text) {
        return new HSSFRichTextString(text);
    }

    public HSSFDataFormat createDataFormat() {
        return dataFormat;
    }

    public HSSFHyperlink createHyperlink(int type) {
        return new HSSFHyperlink(type);
    }

    /**
     * Creates a HSSFFormulaEvaluator, the object that evaluates formula cells.
     *
     * @return a HSSFFormulaEvaluator instance
     */
    public HSSFFormulaEvaluator createFormulaEvaluator() {
        return new HSSFFormulaEvaluator(workbook);
    }

    /**
     * Creates a HSSFClientAnchor. Use this object to position drawing objects in a sheet.
     *
     * @return a HSSFClientAnchor instance
     * @see org.apache.poi.ss.usermodel.Drawing
     */
    public HSSFClientAnchor createClientAnchor() {
        return new HSSFClientAnchor();
    }
}
