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
package org.apache.poi.xssf.usermodel;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.Internal;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTColor;

public class XSSFCreationHelper implements CreationHelper {
    private final XSSFWorkbook workbook;

    /**
     * Should only be called by {@link XSSFWorkbook#getCreationHelper()}
     *
     * @param wb the workbook to create objects for
     */
    @Internal
    public XSSFCreationHelper(XSSFWorkbook wb) {
        workbook = wb;
    }

    /**
     * Creates a new XSSFRichTextString for you.
     */
    @Override
    public XSSFRichTextString createRichTextString(String text) {
        XSSFRichTextString rt = new XSSFRichTextString(text);
        rt.setStylesTableReference(workbook.getStylesSource());
        return rt;
    }

    @Override
    public XSSFDataFormat createDataFormat() {
        return workbook.createDataFormat();
    }

    @Override
    public XSSFColor createExtendedColor() {
        return XSSFColor.from(CTColor.Factory.newInstance(), workbook.getStylesSource().getIndexedColors());
    }
    
    /**
     * Create a new XSSFHyperlink.
     *
     * @param type - the type of hyperlink to create, see {@link Hyperlink}
     */
    @Override
    public XSSFHyperlink createHyperlink(HyperlinkType type) {
        return new XSSFHyperlink(type);
    }

    /**
     * Creates a XSSFFormulaEvaluator, the object that evaluates formula cells.
     *
     * @return a XSSFFormulaEvaluator instance
     */
    @Override
    public XSSFFormulaEvaluator createFormulaEvaluator() {
        return new XSSFFormulaEvaluator(workbook);
    }

    /**
     * Creates a XSSFClientAnchor. Use this object to position drawing object in
     * a sheet
     *
     * @return a XSSFClientAnchor instance
     * @see org.apache.poi.ss.usermodel.Drawing
     */
    @Override
    public XSSFClientAnchor createClientAnchor() {
        return new XSSFClientAnchor();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AreaReference createAreaReference(String reference) {
        return new AreaReference(reference, workbook.getSpreadsheetVersion());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AreaReference createAreaReference(CellReference topLeft, CellReference bottomRight) {
        return new AreaReference(topLeft, bottomRight, workbook.getSpreadsheetVersion());
    }
}
