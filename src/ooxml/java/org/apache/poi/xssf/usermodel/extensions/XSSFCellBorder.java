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


import java.util.LinkedList;

import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBorder;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBorderPr;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STBorderStyle;
import org.apache.poi.xssf.usermodel.BorderStyle;


public class XSSFCellBorder {

    private CTBorder border;

    /**
     * Creates a Cell Border from the supplied XML definition
     */
    public XSSFCellBorder(CTBorder border) {
        this.border = border;
    }
    /**
     * Creates a new, empty Cell Border, on the
     *  given Styles Table
     */
    public XSSFCellBorder() {
        border = CTBorder.Factory.newInstance();
    }

    public static enum BorderSide {
        TOP, RIGHT, BOTTOM, LEFT
    }

    public CTBorder getCTBorder() {
        return border;
    }

    public BorderStyle getBorderStyle(BorderSide side) {
        CTBorderPr ctBorder = getBorder(side);
        STBorderStyle.Enum border = ctBorder == null ? STBorderStyle.NONE : ctBorder.getStyle();
        return BorderStyle.values()[border.intValue() - 1];
    }

    public void setBorderStyle(BorderSide side, BorderStyle style) {
        getBorder(side, true).setStyle(STBorderStyle.Enum.forInt(style.ordinal() + 1));
    }

    public XSSFColor getBorderColor(BorderSide side) {
        CTBorderPr borderPr = getBorder(side);
        return borderPr != null && borderPr.isSetColor() ?
                new XSSFColor(borderPr.getColor()) : null;
    }

    public void setBorderColor(BorderSide side, XSSFColor color) {
        CTBorderPr borderPr = getBorder(side, true);
        if(color == null) borderPr.unsetColor();
        else borderPr.setColor(color.getCTColor());
    }

    private CTBorderPr getBorder(BorderSide side) {
        return getBorder(side, false);
    }

    private CTBorderPr getBorder(BorderSide side, boolean ensure) {
        CTBorderPr borderPr;
        switch (side) {
            case TOP:
                borderPr = border.getTop();
                if(ensure && borderPr == null) borderPr = border.addNewTop();
                break;
            case RIGHT:
                borderPr = border.getRight();
                if(ensure && borderPr == null) borderPr = border.addNewRight();
                break;
            case BOTTOM:
                borderPr = border.getBottom();
                if(ensure && borderPr == null) borderPr = border.addNewBottom();
                break;
            case LEFT:
                borderPr = border.getLeft();
                if(ensure && borderPr == null) borderPr = border.addNewLeft();
                break;
            default:
                throw new IllegalArgumentException("No suitable side specified for the border");
        }
        return borderPr;
    }


    public int hashCode(){
        return border.toString().hashCode();
    }

    public boolean equals(Object o){
        if(!(o instanceof XSSFCellBorder)) return false;

        XSSFCellBorder cf = (XSSFCellBorder)o;
        return border.toString().equals(cf.getCTBorder().toString());
    }

}