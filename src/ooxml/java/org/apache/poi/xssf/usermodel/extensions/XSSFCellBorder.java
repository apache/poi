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


import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBorder;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBorderPr;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STBorderStyle;

/**
 * This element contains border formatting information, specifying border definition formats (left, right, top, bottom, diagonal)
 * for cells in the workbook.
 * Color is optional.
 */
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
     * given Styles Table
     */
    public XSSFCellBorder() {
        border = CTBorder.Factory.newInstance();
    }

    /**
     * The enumeration value indicating the side being used for a cell border.
     */
    public static enum BorderSide {
        TOP, RIGHT, BOTTOM, LEFT
    }

    /**
     * Returns the underlying XML bean.
     *
     * @return CTBorder
     */
    public CTBorder getCTBorder() {
        return border;
    }

    /**
     * Get the type of border to use for the selected border
     *
     * @param side -  - where to apply the color definition
     * @return borderstyle - the type of border to use. default value is NONE if border style is not set.
     * @see BorderStyle
     */
    public BorderStyle getBorderStyle(BorderSide side) {
        CTBorderPr ctBorder = getBorder(side);
        STBorderStyle.Enum border = ctBorder == null ? STBorderStyle.NONE : ctBorder.getStyle();
        return BorderStyle.values()[border.intValue() - 1];
    }

    /**
     * Set the type of border to use for the selected border
     *
     * @param side  -  - where to apply the color definition
     * @param style - border style
     * @see BorderStyle
     */
    public void setBorderStyle(BorderSide side, BorderStyle style) {
        getBorder(side, true).setStyle(STBorderStyle.Enum.forInt(style.ordinal() + 1));
    }

    /**
     * Get the color to use for the selected border
     *
     * @param side - where to apply the color definition
     * @return color - color to use as XSSFColor. null if color is not set
     */
    public XSSFColor getBorderColor(BorderSide side) {
        CTBorderPr borderPr = getBorder(side);
        return borderPr != null && borderPr.isSetColor() ?
                new XSSFColor(borderPr.getColor()) : null;
    }

    /**
     * Set the color to use for the selected border
     *
     * @param side  - where to apply the color definition
     * @param color - the color to use
     */
    public void setBorderColor(BorderSide side, XSSFColor color) {
        CTBorderPr borderPr = getBorder(side, true);
        if (color == null) borderPr.unsetColor();
        else
            borderPr.setColor(color.getCTColor());
    }

    private CTBorderPr getBorder(BorderSide side) {
        return getBorder(side, false);
    }


    private CTBorderPr getBorder(BorderSide side, boolean ensure) {
        CTBorderPr borderPr;
        switch (side) {
            case TOP:
                borderPr = border.getTop();
                if (ensure && borderPr == null) borderPr = border.addNewTop();
                break;
            case RIGHT:
                borderPr = border.getRight();
                if (ensure && borderPr == null) borderPr = border.addNewRight();
                break;
            case BOTTOM:
                borderPr = border.getBottom();
                if (ensure && borderPr == null) borderPr = border.addNewBottom();
                break;
            case LEFT:
                borderPr = border.getLeft();
                if (ensure && borderPr == null) borderPr = border.addNewLeft();
                break;
            default:
                throw new IllegalArgumentException("No suitable side specified for the border");
        }
        return borderPr;
    }


    public int hashCode() {
        return border.toString().hashCode();
    }

    public boolean equals(Object o) {
        if (!(o instanceof XSSFCellBorder)) return false;

        XSSFCellBorder cf = (XSSFCellBorder) o;
        return border.toString().equals(cf.getCTBorder().toString());
    }

}