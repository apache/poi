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
import java.util.Collections;
import java.util.List;
import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlCursor.TokenType;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSdtCell;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSdtContentBlock;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSdtContentCell;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTc;


/**
 * Experimental class to offer rudimentary read-only processing of
 * the CTSdtContentCell.
 * <p>
 * WARNING - APIs expected to change rapidly
 */
public class XWPFSDTContentCell implements ISDTContent {
    private final CTSdtContentCell sdtContentCell;
    private final XWPFSDTCell xwpfsdtCell;
    //A full implementation would grab the icells
    //that a content cell can contain.  This would require
    //significant changes, including changing the notion that the
    //parent of a cell can be not just a row, but a sdt.
    //For now, we are just grabbing the text out of the text tokentypes.

    private List<XWPFTableCell> tableCells;
    private List<ICell> iCells;

    public XWPFSDTContentCell(CTSdtContentCell sdtContentCell, XWPFSDTCell xwpfsdtCell) {
        super();
        this.sdtContentCell = sdtContentCell;
        this.xwpfsdtCell = xwpfsdtCell;
        tableCells = new ArrayList<>();
        iCells = new ArrayList<>();
        //sdtContentCell is allowed to be null:  minOccurs="0" maxOccurs="1"
        if (sdtContentCell == null) {
            return;
        }

        //Can't use ctRow.getTcList because that only gets table cells
        //Can't use ctRow.getSdtList because that only gets sdts that are at cell level
        try (XmlCursor cursor = sdtContentCell.newCursor()) {
            cursor.selectPath("./*");
            while (cursor.toNextSelection()) {
                XmlObject o = cursor.getObject();
                if (o instanceof CTTc) {
                    tableCells.add(new XWPFTableCell((CTTc) o, xwpfsdtCell.getXwpfTableRow(), xwpfsdtCell.getXwpfTableRow().getTable().getBody()));
                    iCells.add(new XWPFTableCell((CTTc) o, xwpfsdtCell.getXwpfTableRow(), xwpfsdtCell.getXwpfTableRow().getTable().getBody()));
                } else if (o instanceof CTSdtCell) {
                    iCells.add(new XWPFSDTCell((CTSdtCell) o, xwpfsdtCell.getXwpfTableRow(), xwpfsdtCell.getXwpfTableRow().getTable().getBody()));
                }
            }
        }
    }

    public CTSdtContentCell getCTSdtContentCell() {
        return sdtContentCell;
    }

    public XWPFSDTCell getSDT() {
        return xwpfsdtCell;
    }

    public List<ICell> getTableICells() {
        return Collections.unmodifiableList(iCells);
    }

    public List<XWPFTableCell> getTableCells() {
        return Collections.unmodifiableList(tableCells);
    }

    @Override
    public String getText() {
        StringBuilder text = new StringBuilder();
        for (ICell cell : iCells) {
            if (text.length() > 0) {
                text.append('\t');
            }
            if (cell instanceof XWPFTableCell) {
                text.append(((XWPFTableCell) cell).getText());
            } else if (cell instanceof XWPFSDTCell) {
                if(((XWPFSDTCell) cell).getContent() != null){
                  text.append(((XWPFSDTCell) cell).getContent().getText());
                }
            }
        }
        return text.toString();
    }

    public String toString() {
        return getText();
    }
}
