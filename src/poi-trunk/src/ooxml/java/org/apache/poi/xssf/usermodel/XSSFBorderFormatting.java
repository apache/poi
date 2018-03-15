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
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBorderPr;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTColor;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STBorderStyle;

/**
 * XSSF high level representation for Border Formatting component
 * of Conditional Formatting settings
 */
public class XSSFBorderFormatting implements BorderFormatting  {
    IndexedColorMap _colorMap;
    CTBorder _border;

    /*package*/ XSSFBorderFormatting(CTBorder border, IndexedColorMap colorMap) {
        _border = border;
        _colorMap = colorMap;
    }

    @Override
    public BorderStyle getBorderBottom() {
        return getBorderStyle(_border.getBottom());
    }

    @Override
    public BorderStyle getBorderDiagonal() {
        return getBorderStyle(_border.getDiagonal());
    }

    @Override
    public BorderStyle getBorderLeft() {
        return getBorderStyle(_border.getLeft());
    }

    @Override
    public BorderStyle getBorderRight() {
        return getBorderStyle(_border.getRight());
    }

    @Override
    public BorderStyle getBorderTopEnum() { return getBorderTop(); }

    @Override
    public BorderStyle getBorderBottomEnum() { return getBorderBottom(); }

    @Override
    public BorderStyle getBorderDiagonalEnum() { return getBorderDiagonal(); }

    @Override
    public BorderStyle getBorderLeftEnum() {
        return getBorderLeft();
    }

    @Override
    public BorderStyle getBorderRightEnum() { return getBorderRight(); }

    @Override
    public BorderStyle getBorderTop() {
        return getBorderStyle(_border.getTop());
    }

    @Override
    public XSSFColor getBottomBorderColorColor() {
        return getColor(_border.getBottom());
    }
    @Override
    public short getBottomBorderColor() {
        return getIndexedColor(getBottomBorderColorColor());
    }

    @Override
    public XSSFColor getDiagonalBorderColorColor() {
        return getColor(_border.getDiagonal());
    }
    @Override
    public short getDiagonalBorderColor() {
        return getIndexedColor(getDiagonalBorderColorColor());
    }

    @Override
    public XSSFColor getLeftBorderColorColor() {
        return getColor(_border.getLeft());
    }
    @Override
    public short getLeftBorderColor() {
        return getIndexedColor(getLeftBorderColorColor());
    }

    @Override
    public XSSFColor getRightBorderColorColor() {
        return getColor(_border.getRight());
    }
    @Override
    public short getRightBorderColor() {
        return getIndexedColor(getRightBorderColorColor());
    }

    @Override
    public XSSFColor getTopBorderColorColor() {
        return getColor(_border.getTop());
    }
    @Override
    public short getTopBorderColor() {
        return getIndexedColor(getTopBorderColorColor());
    }

    @Override
    public void setBorderBottom(BorderStyle border) {
        CTBorderPr pr = _border.isSetBottom() ? _border.getBottom() : _border.addNewBottom();
        if(border == BorderStyle.NONE) _border.unsetBottom();
        else pr.setStyle(STBorderStyle.Enum.forInt(border.getCode() + 1));
    }

    @Override
    public void setBorderDiagonal(BorderStyle border) {
        CTBorderPr pr = _border.isSetDiagonal() ? _border.getDiagonal() : _border.addNewDiagonal();
        if(border == BorderStyle.NONE) _border.unsetDiagonal();
        else pr.setStyle(STBorderStyle.Enum.forInt(border.getCode() + 1));
    }

    @Override
    public void setBorderLeft(BorderStyle border) {
        CTBorderPr pr = _border.isSetLeft() ? _border.getLeft() : _border.addNewLeft();
        if(border == BorderStyle.NONE) _border.unsetLeft();
        else pr.setStyle(STBorderStyle.Enum.forInt(border.getCode() + 1));
    }

    @Override
    public void setBorderRight(BorderStyle border) {
        CTBorderPr pr = _border.isSetRight() ? _border.getRight() : _border.addNewRight();
        if(border == BorderStyle.NONE) _border.unsetRight();
        else pr.setStyle(STBorderStyle.Enum.forInt(border.getCode() + 1));
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

    @Override
    public BorderStyle getBorderVertical() {
        return getBorderStyle(_border.getVertical());
    }

    @Override
    public BorderStyle getBorderHorizontal() {
        return getBorderStyle(_border.getHorizontal());
    }

    @Override
    public BorderStyle getBorderVerticalEnum() { return getBorderVertical(); }

    @Override
    public BorderStyle getBorderHorizontalEnum() { return getBorderHorizontal(); }

    public short getVerticalBorderColor() {
        return getIndexedColor(getVerticalBorderColorColor());
    }

    public XSSFColor getVerticalBorderColorColor() {
        return getColor(_border.getVertical());
    }

    public short getHorizontalBorderColor() {
        return getIndexedColor(getHorizontalBorderColorColor());
    }

    public XSSFColor getHorizontalBorderColorColor() {
        return getColor(_border.getHorizontal());
    }

    public void setBorderHorizontal(BorderStyle border) {
        CTBorderPr pr = _border.isSetHorizontal() ? _border.getHorizontal() : _border.addNewHorizontal();
        if(border == BorderStyle.NONE) _border.unsetHorizontal();
        else pr.setStyle(STBorderStyle.Enum.forInt(border.getCode() + 1));
    }

    public void setBorderVertical(BorderStyle border) {
        CTBorderPr pr = _border.isSetVertical() ? _border.getVertical() : _border.addNewVertical();
        if(border == BorderStyle.NONE) _border.unsetVertical();
        else pr.setStyle(STBorderStyle.Enum.forInt(border.getCode() + 1));
    }

    public void setHorizontalBorderColor(short color) {
        CTColor ctColor = CTColor.Factory.newInstance();
        ctColor.setIndexed(color);
        setHorizontalBorderColor(ctColor);
    }

    public void setHorizontalBorderColor(Color color) {
        XSSFColor xcolor = XSSFColor.toXSSFColor(color);
        if (xcolor == null) setBottomBorderColor((CTColor)null);
        else setHorizontalBorderColor(xcolor.getCTColor());
    }
    
    public void setHorizontalBorderColor(CTColor color) {
        CTBorderPr pr = _border.isSetHorizontal() ? _border.getHorizontal() : _border.addNewHorizontal();
        if (color == null) {
            pr.unsetColor();
        } else {
            pr.setColor(color);
        }
    }

    public void setVerticalBorderColor(short color) {
        CTColor ctColor = CTColor.Factory.newInstance();
        ctColor.setIndexed(color);
        setVerticalBorderColor(ctColor);
    }

    public void setVerticalBorderColor(Color color) {
        XSSFColor xcolor = XSSFColor.toXSSFColor(color);
        if (xcolor == null) setBottomBorderColor((CTColor)null);
        else setVerticalBorderColor(xcolor.getCTColor());
    }
    
    public void setVerticalBorderColor(CTColor color) {
        CTBorderPr pr = _border.isSetVertical() ? _border.getVertical() : _border.addNewVertical();
        if (color == null) {
            pr.unsetColor();
        } else {
            pr.setColor(color);
        }
    }
    
    /**
     * @return BorderStyle from the given element's style, or NONE if border is null
     */
    private BorderStyle getBorderStyle(CTBorderPr borderPr) {
        if (borderPr == null) return BorderStyle.NONE;
        STBorderStyle.Enum ptrn = borderPr.getStyle();
        return ptrn == null ? BorderStyle.NONE : BorderStyle.valueOf((short)(ptrn.intValue() - 1));
    }
    
    private short getIndexedColor(XSSFColor color) {
        return color == null ? 0 : color.getIndexed();
    }
    
    private XSSFColor getColor(CTBorderPr pr) {
        return pr == null ? null : XSSFColor.from(pr.getColor(), _colorMap);
    }
}
