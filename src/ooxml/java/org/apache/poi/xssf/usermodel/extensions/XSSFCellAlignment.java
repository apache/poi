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
package org.apache.poi.xssf.usermodel.extensions;

import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCellAlignment;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STHorizontalAlignment;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STVerticalAlignment;
import org.apache.poi.xssf.usermodel.HorizontalAlignment;
import org.apache.poi.xssf.usermodel.VerticalAlignment;


/**
 * Cell settings avaiable in the Format/Alignment tab
 */
public class XSSFCellAlignment {

    private CTCellAlignment cellAlignement;

    public XSSFCellAlignment(CTCellAlignment cellAlignment) {
        this.cellAlignement = cellAlignment;
    }

    public VerticalAlignment getVertical() {
        STVerticalAlignment.Enum align = cellAlignement.getVertical();
        if(align == null) align = STVerticalAlignment.BOTTOM;

        return VerticalAlignment.values()[align.intValue() - 1];
    }

    public void setVertical(VerticalAlignment vertical) {
        cellAlignement.setVertical(STVerticalAlignment.Enum.forInt(vertical.ordinal() + 1));
    }

    public HorizontalAlignment getHorizontal() {
        STHorizontalAlignment.Enum align = cellAlignement.getHorizontal();
        if(align == null) align = STHorizontalAlignment.GENERAL;

        return HorizontalAlignment.values()[align.intValue() - 1];
    }

    public void setHorizontal(HorizontalAlignment align) {
        cellAlignement.setHorizontal(STHorizontalAlignment.Enum.forInt(align.ordinal() + 1));
    }

    public long getIndent() {
        return cellAlignement.getIndent();
    }

    public void setIndent(long indent) {
        cellAlignement.setIndent(indent);
    }

    public long getTextRotation() {
        return cellAlignement.getTextRotation();
    }

    public void setTextRotation(long rotation) {
        cellAlignement.setTextRotation(rotation);
    }

    public boolean getWrapText() {
        return cellAlignement.getWrapText();
    }

    public void setWrapText(boolean wrapped) {
        cellAlignement.setWrapText(wrapped);
    }

    /**
     * Access to low-level data
     */
    public CTCellAlignment getCTCellAlignment() {
        return cellAlignement;
    }
}
