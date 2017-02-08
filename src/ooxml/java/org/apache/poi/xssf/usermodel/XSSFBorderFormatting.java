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

import org.apache.poi.ss.usermodel.BorderFormatting;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Color;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBorder;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STBorderStyle;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBorderPr;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTColor;

/**
 * XSSF high level representation for Border Formatting component
 * of Conditional Formatting settings
 */
public class XSSFBorderFormatting implements BorderFormatting  {
    CTBorder _border;

    /*package*/ XSSFBorderFormatting(CTBorder border) {
        _border = border;
    }

    /**
     * @deprecated POI 3.15. Use {@link #getBorderBottomEnum()}.
     * This method will return an BorderStyle enum in the future.
     */
    @Override
    public short getBorderBottom() {
        return getBorderBottomEnum().getCode();
    }
    /**
     * @since POI 3.15
     */
    @Override
    public BorderStyle getBorderBottomEnum() {
        STBorderStyle.Enum ptrn = _border.isSetBottom() ? _border.getBottom().getStyle() : null;
        return ptrn == null ? BorderStyle.NONE : BorderStyle.valueOf((short)(ptrn.intValue() - 1));
    }

    /**
     * @deprecated POI 3.15. Use {@link #getBorderDiagonalEnum()}.
     * This method will return an BorderStyle enum in the future.
     */
    @Override
    public short getBorderDiagonal() {
        return getBorderDiagonalEnum().getCode();
    }
    /**
     * @since POI 3.15
     */
    @Override
    public BorderStyle getBorderDiagonalEnum() {
        STBorderStyle.Enum ptrn = _border.isSetDiagonal() ? _border.getDiagonal().getStyle() : null;
        return ptrn == null ? BorderStyle.NONE : BorderStyle.valueOf((short)(ptrn.intValue() - 1));
    }

    /**
     * @deprecated POI 3.15. Use {@link #getBorderLeftEnum()}.
     * This method will return an BorderStyle enum in the future.
     */
    @Override
    public short getBorderLeft() {
        return getBorderLeftEnum().getCode();
    }
    /**
     * @since POI 3.15
     */
    @Override
    public BorderStyle getBorderLeftEnum() {
        STBorderStyle.Enum ptrn = _border.isSetLeft() ? _border.getLeft().getStyle() : null;
        return ptrn == null ? BorderStyle.NONE : BorderStyle.valueOf((short)(ptrn.intValue() - 1));
    }

    /**
     * @deprecated POI 3.15. Use {@link #getBorderRightEnum()}.
     * This method will return an BorderStyle enum in the future.
     */
    @Override
    public short getBorderRight() {
        return getBorderRightEnum().getCode();
    }
    /**
     * @since POI 3.15
     */
    @Override
    public BorderStyle getBorderRightEnum() {
        STBorderStyle.Enum ptrn = _border.isSetRight() ? _border.getRight().getStyle() : null;
        return ptrn == null ? BorderStyle.NONE : BorderStyle.valueOf((short)(ptrn.intValue() - 1));
    }

    /**
     * @deprecated POI 3.15. Use {@link #getBorderTopEnum()}.
     * This method will return an BorderStyle enum in the future.
     */
    @Override
    public short getBorderTop() {
        return getBorderTopEnum().getCode();
    }
    /**
     * @since POI 3.15
     */
    @Override
    public BorderStyle getBorderTopEnum() {
        STBorderStyle.Enum ptrn = _border.isSetTop() ? _border.getTop().getStyle() : null;
        return ptrn == null ? BorderStyle.NONE : BorderStyle.valueOf((short)(ptrn.intValue() - 1));
    }

    @Override
    public XSSFColor getBottomBorderColorColor() {
        if(!_border.isSetBottom()) return null;

        CTBorderPr pr = _border.getBottom();
        return new XSSFColor(pr.getColor());
    }
    @Override
    public short getBottomBorderColor() {
        XSSFColor color = getBottomBorderColorColor();
        if (color == null) return 0;
        return color.getIndexed();
    }

    @Override
    public XSSFColor getDiagonalBorderColorColor() {
        if(!_border.isSetDiagonal()) return null;

        CTBorderPr pr = _border.getDiagonal();
        return new XSSFColor(pr.getColor());
    }
    @Override
    public short getDiagonalBorderColor() {
        XSSFColor color = getDiagonalBorderColorColor();
        if (color == null) return 0;
        return color.getIndexed();
    }

    @Override
    public XSSFColor getLeftBorderColorColor() {
        if(!_border.isSetLeft()) return null;

        CTBorderPr pr = _border.getLeft();
        return new XSSFColor(pr.getColor());
    }
    @Override
    public short getLeftBorderColor() {
        XSSFColor color = getLeftBorderColorColor();
        if (color == null) return 0;
        return color.getIndexed();
    }

    @Override
    public XSSFColor getRightBorderColorColor() {
        if(!_border.isSetRight()) return null;

        CTBorderPr pr = _border.getRight();
        return new XSSFColor(pr.getColor());
    }
    @Override
    public short getRightBorderColor() {
        XSSFColor color = getRightBorderColorColor();
        if (color == null) return 0;
        return color.getIndexed();
    }

    @Override
    public XSSFColor getTopBorderColorColor() {
        if(!_border.isSetTop()) return null;

        CTBorderPr pr = _border.getTop();
        return new XSSFColor(pr.getColor());
    }
    @Override
    public short getTopBorderColor() {
        XSSFColor color = getRightBorderColorColor();
        if (color == null) return 0;
        return color.getIndexed();
    }

    /**
     * @deprecated 3.15 beta 2. Use {@link #setBorderBottom(BorderStyle)}
     */
    @Override
    public void setBorderBottom(short border) {
        setBorderBottom(BorderStyle.valueOf(border));
    }
    @Override
    public void setBorderBottom(BorderStyle border) {
        CTBorderPr pr = _border.isSetBottom() ? _border.getBottom() : _border.addNewBottom();
        if(border == BorderStyle.NONE) _border.unsetBottom();
        else pr.setStyle(STBorderStyle.Enum.forInt(border.getCode() + 1));
    }

    /**
     * @deprecated 3.15 beta 2. Use {@link #setBorderDiagonal(BorderStyle)}
     */
    @Override
    public void setBorderDiagonal(short border) {
        setBorderDiagonal(BorderStyle.valueOf(border));
    }
    @Override
    public void setBorderDiagonal(BorderStyle border) {
        CTBorderPr pr = _border.isSetDiagonal() ? _border.getDiagonal() : _border.addNewDiagonal();
        if(border == BorderStyle.NONE) _border.unsetDiagonal();
        else pr.setStyle(STBorderStyle.Enum.forInt(border.getCode() + 1));
    }

    /**
     * @deprecated 3.15 beta 2. Use {@link #setBorderLeft(BorderStyle)}
     */
    @Override
    public void setBorderLeft(short border) {
        setBorderLeft(BorderStyle.valueOf(border));
    }
    @Override
    public void setBorderLeft(BorderStyle border) {
        CTBorderPr pr = _border.isSetLeft() ? _border.getLeft() : _border.addNewLeft();
        if(border == BorderStyle.NONE) _border.unsetLeft();
        else pr.setStyle(STBorderStyle.Enum.forInt(border.getCode() + 1));
    }

    /**
     * @deprecated 3.15 beta 2. Use {@link #setBorderRight(BorderStyle)}
     */
    @Override
    public void setBorderRight(short border) {
        setBorderRight(BorderStyle.valueOf(border));
    }
    @Override
    public void setBorderRight(BorderStyle border) {
        CTBorderPr pr = _border.isSetRight() ? _border.getRight() : _border.addNewRight();
        if(border == BorderStyle.NONE) _border.unsetRight();
        else pr.setStyle(STBorderStyle.Enum.forInt(border.getCode() + 1));
    }

    /**
     * @deprecated 3.15 beta 2. Use {@link #setBorderTop(BorderStyle)}
     */
    @Override
    public void setBorderTop(short border) {
        setBorderTop(BorderStyle.valueOf(border));
    }
    @Override
    public void setBorderTop(BorderStyle border) {
        CTBorderPr pr = _border.isSetTop() ? _border.getTop() : _border.addNewTop();
        if(border == BorderStyle.NONE) _border.unsetTop();
        else pr.setStyle(STBorderStyle.Enum.forInt(border.getCode() + 1));
    }

    @Override
    public void setBottomBorderColor(Color color) {
        XSSFColor xcolor = XSSFColor.toXSSFColor(color);
        if (xcolor == null) setBottomBorderColor((CTColor)null);
        else setBottomBorderColor(xcolor.getCTColor());
    }
    @Override
    public void setBottomBorderColor(short color) {
        CTColor ctColor = CTColor.Factory.newInstance();
        ctColor.setIndexed(color);
        setBottomBorderColor(ctColor);
    }
    public void setBottomBorderColor(CTColor color) {
        CTBorderPr pr = _border.isSetBottom() ? _border.getBottom() : _border.addNewBottom();
        if (color == null) {
            pr.unsetColor();
        } else {
            pr.setColor(color);
        }
    }

    @Override
    public void setDiagonalBorderColor(Color color) {
        XSSFColor xcolor = XSSFColor.toXSSFColor(color);
        if (xcolor == null) setDiagonalBorderColor((CTColor)null);
        else setDiagonalBorderColor(xcolor.getCTColor());
    }
    @Override
    public void setDiagonalBorderColor(short color) {
        CTColor ctColor = CTColor.Factory.newInstance();
        ctColor.setIndexed(color);
        setDiagonalBorderColor(ctColor);
    }
    public void setDiagonalBorderColor(CTColor color) {
        CTBorderPr pr = _border.isSetDiagonal() ? _border.getDiagonal() : _border.addNewDiagonal();
        if (color == null) {
            pr.unsetColor();
        } else {
            pr.setColor(color);
        }
    }

    @Override
    public void setLeftBorderColor(Color color) {
        XSSFColor xcolor = XSSFColor.toXSSFColor(color);
        if (xcolor == null) setLeftBorderColor((CTColor)null);
        else setLeftBorderColor(xcolor.getCTColor());
    }
    @Override
    public void setLeftBorderColor(short color) {
        CTColor ctColor = CTColor.Factory.newInstance();
        ctColor.setIndexed(color);
        setLeftBorderColor(ctColor);
    }
    public void setLeftBorderColor(CTColor color) {
        CTBorderPr pr = _border.isSetLeft() ? _border.getLeft() : _border.addNewLeft();
        if (color == null) {
            pr.unsetColor();
        } else {
            pr.setColor(color);
        }
    }

    @Override
    public void setRightBorderColor(Color color) {
        XSSFColor xcolor = XSSFColor.toXSSFColor(color);
        if (xcolor == null) setRightBorderColor((CTColor)null);
        else setRightBorderColor(xcolor.getCTColor());
    }
    @Override
    public void setRightBorderColor(short color) {
        CTColor ctColor = CTColor.Factory.newInstance();
        ctColor.setIndexed(color);
        setRightBorderColor(ctColor);
    }
    public void setRightBorderColor(CTColor color) {
        CTBorderPr pr = _border.isSetRight() ? _border.getRight() : _border.addNewRight();
        if (color == null) {
            pr.unsetColor();
        } else {
            pr.setColor(color);
        }
    }

    @Override
    public void setTopBorderColor(Color color) {
        XSSFColor xcolor = XSSFColor.toXSSFColor(color);
        if (xcolor == null) setTopBorderColor((CTColor)null);
        else setTopBorderColor(xcolor.getCTColor());
    }
    @Override
    public void setTopBorderColor(short color) {
        CTColor ctColor = CTColor.Factory.newInstance();
        ctColor.setIndexed(color);
        setTopBorderColor(ctColor);
    }
    public void setTopBorderColor(CTColor color) {
        CTBorderPr pr = _border.isSetTop() ? _border.getTop() : _border.addNewTop();
        if (color == null) {
            pr.unsetColor();
        } else {
            pr.setColor(color);
        }
    }
}
