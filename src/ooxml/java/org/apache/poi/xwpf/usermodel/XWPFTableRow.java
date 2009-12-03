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

import java.math.BigInteger;

import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHeight;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTrPr;
import org.apache.poi.util.Internal;


/**
 * @author gisellabronzetti
 */
public class XWPFTableRow {

    private CTRow ctRow;

    public XWPFTableRow(CTRow row) {
        this.ctRow = row;
    }

    @Internal
    public CTRow getCtRow() {
        return ctRow;
    }


    public XWPFTableCell createCell() {
        return new XWPFTableCell(ctRow.addNewTc());
    }

    public XWPFTableCell getCell(int pos) {
        if (pos >= 0 && pos < ctRow.sizeOfTcArray()) {
            return new XWPFTableCell(ctRow.getTcArray(pos));
        }
        return null;
    }

    /**
     * This element specifies the height of the current table row within the
     * current table. This height shall be used to determine the resulting
     * height of the table row, which may be absolute or relative (depending on
     * its attribute values). If omitted, then the table row shall automatically
     * resize its height to the height required by its contents (the equivalent
     * of an hRule value of auto).
     *
     * @param height
     */
    public void setHeight(int height) {
        CTTrPr properties = getTrPr();
        CTHeight h = properties.sizeOfTrHeightArray() == 0 ? properties.addNewTrHeight() : properties.getTrHeightArray(0);
        h.setVal(new BigInteger("" + height));
    }

    /**
     * This element specifies the height of the current table row within the
     * current table. This height shall be used to determine the resulting
     * height of the table row, which may be absolute or relative (depending on
     * its attribute values). If omitted, then the table row shall automatically
     * resize its height to the height required by its contents (the equivalent
     * of an hRule value of auto).
     *
     * @return height
     */
    public int getHeight() {
        CTTrPr properties = getTrPr();
        return properties.sizeOfTrHeightArray() == 0 ? 0 : properties.getTrHeightArray(0).getVal().intValue();
    }


    private CTTrPr getTrPr() {
        return (ctRow.isSetTrPr()) ? ctRow.getTrPr() : ctRow.addNewTrPr();
    }


}// end class
