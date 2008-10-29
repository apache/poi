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

import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTColor;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFill;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPatternFill;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STPatternType;
import org.apache.poi.xssf.usermodel.XSSFColor;

/**
 * This element specifies fill formatting.
 * A cell fill consists of a background color, foreground color, and pattern to be applied across the cell.
 */
public final class XSSFCellFill {

    private CTFill _fill;

    /**
     * Creates a CellFill from the supplied parts
     *
     * @param fill - fill
     */
    public XSSFCellFill(CTFill fill) {
        _fill = fill;
    }

    /**
     * Creates an empty CellFill
     */
    public XSSFCellFill() {
        _fill = CTFill.Factory.newInstance();
    }

    /**
     * Get the background fill color.
     *
     * @return fill color, null if color is not set
     */
    public XSSFColor getFillBackgroundColor() {
        CTPatternFill ptrn = _fill.getPatternFill();
        if (ptrn == null) return null;

        CTColor ctColor = ptrn.getBgColor();
        return ctColor == null ? null : new XSSFColor(ctColor);
    }

    /**
     * Set the background fill color represented as a indexed color value.
     *
     * @param index
     */
    public void setFillBackgroundColor(int index) {
        CTPatternFill ptrn = ensureCTPatternFill();
        CTColor ctColor = ptrn.isSetBgColor() ? ptrn.getBgColor() : ptrn.addNewBgColor();
        ctColor.setIndexed(index);
    }

    /**
     * Set the background fill color represented as a {@link XSSFColor} value.
     *
     * @param color
     */
    public void setFillBackgroundColor(XSSFColor color) {
        CTPatternFill ptrn = ensureCTPatternFill();
        ptrn.setBgColor(color.getCTColor());
    }

    /**
     * Get the foreground fill color.
     *
     * @return XSSFColor - foreground color. null if color is not set
     */
    public XSSFColor getFillForegroundColor() {
        CTPatternFill ptrn = _fill.getPatternFill();
        if (ptrn == null) return null;

        CTColor ctColor = ptrn.getFgColor();
        return ctColor == null ? null : new XSSFColor(ctColor);
    }

    /**
     * Set the foreground fill color as a indexed color value
     *
     * @param index - the color to use
     */
    public void setFillForegroundColor(int index) {
        CTPatternFill ptrn = ensureCTPatternFill();
        CTColor ctColor = ptrn.isSetFgColor() ? ptrn.getFgColor() : ptrn.addNewFgColor();
        ctColor.setIndexed(index);
    }

    /**
     * Set the foreground fill color represented as a {@link XSSFColor} value.
     *
     * @param color - the color to use
     */
    public void setFillForegroundColor(XSSFColor color) {
        CTPatternFill ptrn = ensureCTPatternFill();
        ptrn.setFgColor(color.getCTColor());
    }

    /**
     * get the fill pattern
     *
     * @return fill pattern type. null if fill pattern is not set
     */
    public STPatternType.Enum getPatternType() {
        CTPatternFill ptrn = _fill.getPatternFill();
        return ptrn == null ? null : ptrn.getPatternType();
    }

    /**
     * set the fill pattern
     *
     * @param patternType fill pattern to use
     */
    public void setPatternType(STPatternType.Enum patternType) {
        CTPatternFill ptrn = ensureCTPatternFill();
        ptrn.setPatternType(patternType);
    }

    private CTPatternFill ensureCTPatternFill() {
        CTPatternFill patternFill = _fill.getPatternFill();
        if (patternFill == null) {
            patternFill = _fill.addNewPatternFill();
        }
        return patternFill;
    }

    /**
     * Returns the underlying XML bean.
     *
     * @return CTFill
     */
    public CTFill getCTFill() {
        return _fill;
    }


    public int hashCode() {
        return _fill.toString().hashCode();
    }

    public boolean equals(Object o) {
        if (!(o instanceof XSSFCellFill)) return false;

        XSSFCellFill cf = (XSSFCellFill) o;
        return _fill.toString().equals(cf.getCTFill().toString());
    }
}
