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
package org.apache.poi.xwpf.usermodel;

import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSdtCell;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSdtRow;

/**
 * Experimental class to offer rudimentary read-only processing of
 * of StructuredDocumentTags/ContentControl that can appear
 * in a table row as if a table cell.
 * <p>
 * These can contain one or more cells or other SDTs within them.
 * <p>
 * WARNING - APIs expected to change rapidly
 */
public class XWPFSDTRow extends XWPFAbstractSDT implements IRow {
    private final XWPFSDTContentRow cellContent;
    private final XWPFTable xwpfTable;

    public XWPFSDTRow(CTSdtRow sdtRow, XWPFTable xwpfTable) {
        super(sdtRow.getSdtPr());
        this.xwpfTable = xwpfTable;
        cellContent = new XWPFSDTContentRow(sdtRow.getSdtContent(), this);
    }

    public XWPFTable getTable() {
        return xwpfTable;
    }

    @Override
    public ISDTContent getContent() {
        return cellContent;
    }

    @Override
    public XWPFDocument getDocument() {
        return this.xwpfTable.getBody().getXWPFDocument();
    }

}
