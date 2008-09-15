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

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellAlignment;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellFill;
import org.apache.poi.xssf.usermodel.extensions.XSSFColor;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder.BorderSide;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCellAlignment;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCellProtection;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTXf;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STBorderStyle;


public class XSSFCellStyle implements CellStyle {

    private int cellXfId;
    private int cellStyleXfId;
    private StylesSource stylesSource;
    private CTXf cellXf;
    private CTXf cellStyleXf;
    private XSSFCellBorder cellBorder;
    private XSSFCellFill cellFill;
    private XSSFFont font;
    private XSSFCellAlignment cellAlignment;

    /**
     * Creates a Cell Style from the supplied parts
     * @param cellXf The main XF for the cell
     * @param cellStyleXf Optional, style xf
     * @param stylesSource Styles Source to work off
     */
    public XSSFCellStyle(int cellXfId, int cellStyleXfId, StylesTable stylesSource) {
        this.cellXfId = cellXfId;
        this.cellStyleXfId = cellStyleXfId;
        this.stylesSource = stylesSource;
        this.cellXf = stylesSource.getCellXfAt(this.cellXfId);
        this.cellStyleXf = stylesSource.getCellStyleXfAt(this.cellStyleXfId);
    }

    /**
     * Used so that StylesSource can figure out our location
     */
    public CTXf getCoreXf() {
        return cellXf;
    }
    /**
     * Used so that StylesSource can figure out our location
     */
    public CTXf getStyleXf() {
        return cellStyleXf;
    }

    /**
     * Creates an empty Cell Style
     */
    public XSSFCellStyle(StylesSource stylesSource) {
        this.stylesSource = stylesSource;

        // We need a new CTXf for the main styles
        // TODO decide on a style ctxf
        cellXf = CTXf.Factory.newInstance();
        cellStyleXf = null;
    }

    /**
     * Verifies that this style belongs to the supplied Workbook
     *  Styles Source.
     * Will throw an exception if it belongs to a different one.
     * This is normally called when trying to assign a style to a
     *  cell, to ensure the cell and the style are from the same
     *  workbook (if they're not, it won't work)
     * @throws IllegalArgumentException if there's a workbook mis-match
     */
    public void verifyBelongsToStylesSource(StylesSource src) {
        if(this.stylesSource != src) {
            throw new IllegalArgumentException("This Style does not belong to the supplied Workbook Stlyes Source. Are you trying to assign a style from one workbook to the cell of a differnt workbook?");
        }
    }

    /**
     * Clones all the style information from another
     *  XSSFCellStyle, onto this one. This
     *  XSSFCellStyle will then have all the same
     *  properties as the source, but the two may
     *  be edited independently.
     * Any stylings on this XSSFCellStyle will be lost!
     *
     * The source XSSFCellStyle could be from another
     *  XSSFWorkbook if you like. This allows you to
     *  copy styles from one XSSFWorkbook to another.
     */
    public void cloneStyleFrom(CellStyle source) {
        if(source instanceof XSSFCellStyle) {
            this.cloneStyleFrom((XSSFCellStyle)source);
        }
        throw new IllegalArgumentException("Can only clone from one XSSFCellStyle to another, not between HSSFCellStyle and XSSFCellStyle");
    }
    public void cloneStyleFrom(XSSFCellStyle source) {
        throw new IllegalStateException("TODO");
    }

    public short getAlignment() {
        return (short)(getAlignmentEnum().ordinal());
    }

    public HorizontalAlignment getAlignmentEnum() {
        return getCellAlignment().getHorizontal();
    }

    public short getBorderBottom() {
        return getBorderStyleAsShort(BorderSide.BOTTOM);
    }

    public String getBorderBottomAsString() {
        return getBorderStyleAsString(BorderSide.BOTTOM);
    }

    public short getBorderLeft() {
        return getBorderStyleAsShort(BorderSide.LEFT);
    }

    public String getBorderLeftAsString() {
        return getBorderStyleAsString(BorderSide.LEFT);
    }

    public short getBorderRight() {
        return getBorderStyleAsShort(BorderSide.RIGHT);
    }

    public String getBorderRightAsString() {
        return getBorderStyleAsString(BorderSide.RIGHT);
    }

    public short getBorderTop() {
        return getBorderStyleAsShort(BorderSide.TOP);
    }

    public String getBorderTopAsString() {
        return getBorderStyleAsString(BorderSide.TOP);
    }

    public short getBottomBorderColor() {
        return getBorderColorIndexed(BorderSide.BOTTOM);
    }

    public short getDataFormat() {
        return (short)cellXf.getNumFmtId();
    }
    public String getDataFormatString() {
        return stylesSource.getNumberFormatAt(getDataFormat());
    }

    public short getFillBackgroundColor() {
        return (short) getCellFill().getFillBackgroundColor().getIndexed();
    }

    public short getFillForegroundColor() {
        return (short) getCellFill().getFillForegroundColor().getIndexed();
    }

    public short getFillPattern() {
        return (short) getCellFill().getPatternType().intValue();
    }

    public Font getFont(Workbook parentWorkbook) {
        return getFont();
    }

    public Font getFont() {
        if (font == null) {
            font = (XSSFFont) ((StylesTable)stylesSource).getFontAt(getFontId());
        }
        return font;
    }

    public short getFontIndex() {
        return (short) getFontId();
    }

    public boolean getHidden() {
        return getCellProtection().getHidden();
    }

    public short getIndention() {
        return (short) getCellAlignment().getIndent();
    }

    public short getIndex() {
        return (short) this.cellXfId;
    }

    public short getLeftBorderColor() {
        return getBorderColorIndexed(BorderSide.LEFT);
    }

    public boolean getLocked() {
        return getCellProtection().getLocked();
    }

    public short getRightBorderColor() {
        return getBorderColorIndexed(BorderSide.RIGHT);
    }

    public short getRotation() {
        return (short) getCellAlignment().getTextRotation();
    }

    public short getTopBorderColor() {
        return getBorderColorIndexed(BorderSide.TOP);
    }

    public short getVerticalAlignment() {
        return (short) (getVerticalAlignmentEnum().ordinal());
    }

    public VerticalAlignment getVerticalAlignmentEnum() {
        return getCellAlignment().getVertical();
    }

    public boolean getWrapText() {
        return getCellAlignment().getWrapText();
    }

    public void setAlignment(short align) {
        getCellAlignment().setHorizontal(HorizontalAlignment.values()[align]);
    }

    public void setAlignment(HorizontalAlignment align) {
        setAlignment((short)align.ordinal());
    }

    public void setBorderBottom(short border) {
        setBorderBottomEnum(STBorderStyle.Enum.forInt(border));
    }

    public void setBorderBottomEnum(STBorderStyle.Enum style) {
        getCellBorder().setBorderStyle(BorderSide.BOTTOM, style);
    }

    public void setBorderLeft(short border) {
        setBorderLeftEnum(STBorderStyle.Enum.forInt(border));
    }

    public void setBorderLeftEnum(STBorderStyle.Enum style) {
        getCellBorder().setBorderStyle(BorderSide.LEFT, style);
    }

    public void setBorderRight(short border) {
        setBorderRightEnum(STBorderStyle.Enum.forInt(border));
    }

    public void setBorderRightEnum(STBorderStyle.Enum style) {
        getCellBorder().setBorderStyle(BorderSide.RIGHT, style);
    }

    public void setBorderTop(short border) {
        setBorderTopEnum(STBorderStyle.Enum.forInt(border));
    }

    public void setBorderTopEnum(STBorderStyle.Enum style) {
        getCellBorder().setBorderStyle(BorderSide.TOP, style);
    }

    public void setBottomBorderColor(short color) {
        setBorderColorIndexed(BorderSide.BOTTOM, color);
    }

    public void setDataFormat(short fmt) {
        cellXf.setNumFmtId((long)fmt);
    }

    public void setFillBackgroundColor(short bg) {
        getCellFill().setFillBackgroundColor(bg);
    }

    public void setFillForegroundColor(short bg) {
        getCellFill().setFillForegroundColor(bg);
    }

    public void setFillPattern(short fp) {
        // TODO Auto-generated method stub

    }

    public void setFont(Font font) {
        if(font!=null){
            long index=this.stylesSource.putFont(font);
            this.cellXf.setFontId(index);
        }
    }

    public void setHidden(boolean hidden) {
        getCellProtection().setHidden(hidden);
    }

    public void setIndention(short indent) {
        getCellAlignment().setIndent(indent);
    }

    public void setLeftBorderColor(short color) {
        setBorderColorIndexed(BorderSide.LEFT, color);
    }

    public void setLocked(boolean locked) {
        getCellProtection().setLocked(locked);
    }

    public void setRightBorderColor(short color) {
        setBorderColorIndexed(BorderSide.RIGHT, color);
    }

    public void setRotation(short rotation) {
        getCellAlignment().setTextRotation(rotation);
    }

    public void setTopBorderColor(short color) {
        setBorderColorIndexed(BorderSide.TOP, color);
    }

    public void setVerticalAlignment(short align) {
        getCellAlignment().setVertical(VerticalAlignment.values()[align]);
    }

    public void setVerticalAlignment(VerticalAlignment align) {
        getCellAlignment().setVertical(align);
    }

    public void setWrapText(boolean wrapped) {
        getCellAlignment().setWrapText(wrapped);
    }

    public XSSFColor getBorderColor(BorderSide side) {
        return getCellBorder().getBorderColor(side);
    }

    public void setBorderColor(BorderSide side, XSSFColor color) {
        getCellBorder().setBorderColor(side, color);
    }

    private XSSFCellBorder getCellBorder() {
        if (cellBorder == null) {
            // TODO make a common Cell Border object
            cellBorder = ((StylesTable)stylesSource).getBorderAt(getBorderId());
        }
        return cellBorder;
    }

    private int getBorderId() {
        if (cellXf.isSetBorderId()) {
            return (int) cellXf.getBorderId();
        }
        return (int) cellStyleXf.getBorderId();
    }

    private XSSFCellFill getCellFill() {
        if (cellFill == null) {
            cellFill = ((StylesTable)stylesSource).getFillAt(getFillId());
        }
        return cellFill;
    }

    private int getFillId() {
        if (cellXf.isSetFillId()) {
            return (int) cellXf.getFillId();
        }
        return (int) cellStyleXf.getFillId();
    }

    private int getFontId() {
        if (cellXf.isSetFontId()) {
            return (int) cellXf.getFontId();
        }
        return (int) cellStyleXf.getFontId();
    }

    private CTCellProtection getCellProtection() {
        if (cellXf.getProtection() == null) {
            cellXf.addNewProtection();
        }
        return cellXf.getProtection();
    }

    public XSSFCellAlignment getCellAlignment() {
        if (this.cellAlignment == null) {
            this.cellAlignment = new XSSFCellAlignment(getCTCellAlignment());
        }
        return this.cellAlignment;
    }

    private CTCellAlignment getCTCellAlignment() {
        if (cellXf.getAlignment() == null) {
            cellXf.setAlignment(CTCellAlignment.Factory.newInstance());
        }
        return cellXf.getAlignment();
    }

    private short getBorderColorIndexed(BorderSide side) {
        return (short) getBorderColor(side).getIndexed();
    }

    private void setBorderColorIndexed(BorderSide side, long color) {
        getBorderColor(side).setIndexed(color);
    }

    private short getBorderStyleAsShort(BorderSide side) {
        return (short) (getBorderStyle(side).intValue() - 1);
    }

    private String getBorderStyleAsString(BorderSide side) {
        return getBorderStyle(side).toString();
    }

    private STBorderStyle.Enum getBorderStyle(BorderSide side) {
        return getCellBorder().getBorderStyle(side);
    }

}
