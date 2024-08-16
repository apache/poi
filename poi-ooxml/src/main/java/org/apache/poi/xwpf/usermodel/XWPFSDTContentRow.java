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


import java.util.ArrayList;
import java.util.List;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSdtContentRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSdtRow;


/**
 * Experimental class to offer rudimentary read-only processing of
 * of the XWPFSDTContentRow.
 * <p>
 * WARNING - APIs expected to change rapidly
 */
public class XWPFSDTContentRow implements ISDTContent {
    private final CTSdtContentRow sdtContentRow;
    private final XWPFSDTRow xwpfsdtRow;
    protected final List<XWPFTableRow> tableRows = new ArrayList<>();
    protected final List<IRow> iRows = new ArrayList<>();

    public XWPFSDTContentRow(CTSdtContentRow sdtContentRow, XWPFSDTRow xwpfsdtRow) {
        super();
        this.sdtContentRow = sdtContentRow;
        this.xwpfsdtRow = xwpfsdtRow;
        //sdtContentRow is allowed to be null:  minOccurs="0" maxOccurs="1"
        if (sdtContentRow == null) {
            return;
        }
        try (XmlCursor cursor = sdtContentRow.newCursor()) {
            cursor.selectPath("./*");
            while (cursor.toNextSelection()) {
                XmlObject o = cursor.getObject();
                if (o instanceof CTRow) {
                    XWPFTableRow p = new XWPFTableRow((CTRow) o, xwpfsdtRow.getTable());
                    tableRows.add(p);
                    iRows.add(p);
                }
                if (o instanceof CTSdtRow) {
                    XWPFSDTRow t = new XWPFSDTRow((CTSdtRow) o, xwpfsdtRow.getTable());
                    iRows.add(t);
                }
            }
        }
    }

    public CTSdtContentRow getSdtContentRow() {
        return sdtContentRow;
    }

    public XWPFSDTRow getSDT() {
        return xwpfsdtRow;
    }

    public String getText() {
        StringBuilder text = new StringBuilder();
        for (IRow row : iRows) {
            if (row instanceof XWPFTableRow) {
                text.append(((XWPFTableRow) row).getText());
            } else if (row instanceof XWPFSDTRow) {
                if(((XWPFSDTRow) row).getContent() != null){
                    text.append(((XWPFSDTRow) row).getContent().getText());
                }
            }
        }
        return text.toString();
    }

    public String toString() {
        return getText();
    }
}
