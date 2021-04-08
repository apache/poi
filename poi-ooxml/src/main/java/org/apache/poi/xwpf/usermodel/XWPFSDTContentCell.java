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


import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlCursor.TokenType;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSdtContentCell;


/**
 * Experimental class to offer rudimentary read-only processing of
 * of the XWPFSDTCellContent.
 * <p>
 * WARNING - APIs expected to change rapidly
 */
public class XWPFSDTContentCell implements ISDTContent {

    //A full implementation would grab the icells
    //that a content cell can contain.  This would require
    //significant changes, including changing the notion that the
    //parent of a cell can be not just a row, but an sdt.
    //For now we are just grabbing the text out of the text tokentypes.

    //private List<ICell> cells = new ArrayList<ICell>().

    private String text = "";

    public XWPFSDTContentCell(CTSdtContentCell sdtContentCell,
                              XWPFTableRow xwpfTableRow, IBody part) {
        super();
        //sdtContentCell is allowed to be null:  minOccurs="0" maxOccurs="1"
        if (sdtContentCell == null) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        XmlCursor cursor = sdtContentCell.newCursor();

        //keep track of the following,
        //and add "\n" only before the start of a body
        //element if it is not the first body element.

        //index of cell in row
        int tcCnt = 0;
        //count of body objects
        int iBodyCnt = 0;
        int depth = 1;

        while (cursor.hasNextToken() && depth > 0) {
            TokenType t = cursor.toNextToken();
            if (t.isText()) {
                sb.append(cursor.getTextValue());
            } else if (isStartToken(cursor, "tr")) {
                tcCnt = 0;
                iBodyCnt = 0;
            } else if (isStartToken(cursor, "tc")) {
                if (tcCnt++ > 0) {
                    sb.append("\t");
                }
                iBodyCnt = 0;
            } else if (isStartToken(cursor, "p") ||
                    isStartToken(cursor, "tbl") ||
                    isStartToken(cursor, "sdt")) {
                if (iBodyCnt > 0) {
                    sb.append("\n");
                }
                iBodyCnt++;
            }
            if (cursor.isStart()) {
                depth++;
            } else if (cursor.isEnd()) {
                depth--;
            }
        }
        text = sb.toString();
        cursor.dispose();
    }


    private boolean isStartToken(XmlCursor cursor, String string) {
        if (!cursor.isStart()) {
            return false;
        }
        QName qName = cursor.getName();
        if (qName != null && qName.getLocalPart() != null &&
                qName.getLocalPart().equals(string)) {
            return true;
        }
        return false;
    }


    public String getText() {
        return text;
    }

    public String toString() {
        return getText();
    }
}
