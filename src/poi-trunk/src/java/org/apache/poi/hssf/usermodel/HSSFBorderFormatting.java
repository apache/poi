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

package org.apache.poi.hssf.usermodel;

import org.apache.poi.hssf.record.CFRuleBase;
import org.apache.poi.hssf.record.cf.BorderFormatting;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.hssf.util.HSSFColor.HSSFColorPredefined;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Color;

/**
 * High level representation for Border Formatting component
 * of Conditional Formatting settings
 */
public final class HSSFBorderFormatting implements org.apache.poi.ss.usermodel.BorderFormatting {
    private final HSSFWorkbook workbook;
    private final CFRuleBase cfRuleRecord;
    private final BorderFormatting borderFormatting;

    protected HSSFBorderFormatting(CFRuleBase cfRuleRecord, HSSFWorkbook workbook) {
        this.workbook = workbook;
        this.cfRuleRecord = cfRuleRecord;
        this.borderFormatting = cfRuleRecord.getBorderFormatting();
    }

    protected BorderFormatting getBorderFormattingBlock() {
        return borderFormatting;
    }

    @Override
    public BorderStyle getBorderBottom() {
        return BorderStyle.valueOf((short) borderFormatting.getBorderBottom());
    }

    @Override
    public BorderStyle getBorderDiagonal() {
        return BorderStyle.valueOf((short) borderFormatting.getBorderDiagonal());
    }

    @Override
    public BorderStyle getBorderLeft() {
        return BorderStyle.valueOf((short) borderFormatting.getBorderLeft());
    }

    @Override
    public BorderStyle getBorderRight() {
        return BorderStyle.valueOf((short) borderFormatting.getBorderRight());
    }

    @Override
    public BorderStyle getBorderTop() {
        return BorderStyle.valueOf((short) borderFormatting.getBorderTop());
    }

    @Override
    public short getBottomBorderColor() {
        return (short) borderFormatting.getBottomBorderColor();
    }

    @Override
    public HSSFColor getBottomBorderColorColor() {
        return workbook.getCustomPalette().getColor(
                borderFormatting.getBottomBorderColor()
        );
    }

    @Override
    public short getDiagonalBorderColor() {
        return (short) borderFormatting.getDiagonalBorderColor();
    }

    @Override
    public HSSFColor getDiagonalBorderColorColor() {
        return workbook.getCustomPalette().getColor(
                borderFormatting.getDiagonalBorderColor()
        );
    }

    @Override
    public short getLeftBorderColor() {
        return (short) borderFormatting.getLeftBorderColor();
    }

    @Override
    public HSSFColor getLeftBorderColorColor() {
        return workbook.getCustomPalette().getColor(
                borderFormatting.getLeftBorderColor()
        );
    }

    @Override
    public short getRightBorderColor() {
        return (short) borderFormatting.getRightBorderColor();
    }

    @Override
    public HSSFColor getRightBorderColorColor() {
        return workbook.getCustomPalette().getColor(
                borderFormatting.getRightBorderColor()
        );
    }

    @Override
    public short getTopBorderColor() {
        return (short) borderFormatting.getTopBorderColor();
    }

    @Override
    public HSSFColor getTopBorderColorColor() {
        return workbook.getCustomPalette().getColor(
                borderFormatting.getTopBorderColor()
        );
    }

    public boolean isBackwardDiagonalOn() {
        return borderFormatting.isBackwardDiagonalOn();
    }

    public boolean isForwardDiagonalOn() {
        return borderFormatting.isForwardDiagonalOn();
    }

    public void setBackwardDiagonalOn(boolean on) {
        borderFormatting.setBackwardDiagonalOn(on);
        if (on) {
            cfRuleRecord.setTopLeftBottomRightBorderModified(on);
        }
    }

    public void setForwardDiagonalOn(boolean on) {
        borderFormatting.setForwardDiagonalOn(on);
        if (on) {
            cfRuleRecord.setBottomLeftTopRightBorderModified(on);
        }
    }

    @Override
    public void setBorderBottom(BorderStyle border) {
        short code = border.getCode();
        borderFormatting.setBorderBottom(code);
        if (code != 0) {
            cfRuleRecord.setBottomBorderModified(true);
        } else {
            cfRuleRecord.setBottomBorderModified(false);
        }
    }

    @Override
    public void setBorderDiagonal(BorderStyle border) {
        short code = border.getCode();
        borderFormatting.setBorderDiagonal(code);
        if (code != 0) {
            cfRuleRecord.setBottomLeftTopRightBorderModified(true);
            cfRuleRecord.setTopLeftBottomRightBorderModified(true);
        } else {
            cfRuleRecord.setBottomLeftTopRightBorderModified(false);
            cfRuleRecord.setTopLeftBottomRightBorderModified(false);
        }
    }

    @Override
    public void setBorderLeft(BorderStyle border) {
        short code = border.getCode();
        borderFormatting.setBorderLeft(code);
        if (code != 0) {
            cfRuleRecord.setLeftBorderModified(true);
        } else {
            cfRuleRecord.setLeftBorderModified(false);
        }
    }

    @Override
    public void setBorderRight(BorderStyle border) {
        short code = border.getCode();
        borderFormatting.setBorderRight(code);
        if (code != 0) {
            cfRuleRecord.setRightBorderModified(true);
        } else {
            cfRuleRecord.setRightBorderModified(false);
        }
    }

    @Override
    public void setBorderTop(BorderStyle border) {
        short code = border.getCode();
        borderFormatting.setBorderTop(code);
        if (code != 0) {
            cfRuleRecord.setTopBorderModified(true);
        } else {
            cfRuleRecord.setTopBorderModified(false);
        }
    }

    @Override
    public void setBottomBorderColor(short color) {
        borderFormatting.setBottomBorderColor(color);
        if (color != 0) {
            cfRuleRecord.setBottomBorderModified(true);
        } else {
            cfRuleRecord.setBottomBorderModified(false);
        }
    }

    public void setBottomBorderColor(Color color) {
        HSSFColor hcolor = HSSFColor.toHSSFColor(color);
        if (hcolor == null) {
            setBottomBorderColor((short) 0);
        } else {
            setBottomBorderColor(hcolor.getIndex());
        }
    }

    @Override
    public void setDiagonalBorderColor(short color) {
        borderFormatting.setDiagonalBorderColor(color);
        if (color != 0) {
            cfRuleRecord.setBottomLeftTopRightBorderModified(true);
            cfRuleRecord.setTopLeftBottomRightBorderModified(true);
        } else {
            cfRuleRecord.setBottomLeftTopRightBorderModified(false);
            cfRuleRecord.setTopLeftBottomRightBorderModified(false);
        }
    }

    @Override
    public void setDiagonalBorderColor(Color color) {
        HSSFColor hcolor = HSSFColor.toHSSFColor(color);
        if (hcolor == null) {
            setDiagonalBorderColor((short) 0);
        } else {
            setDiagonalBorderColor(hcolor.getIndex());
        }
    }

    @Override
    public void setLeftBorderColor(short color) {
        borderFormatting.setLeftBorderColor(color);
        if (color != 0) {
            cfRuleRecord.setLeftBorderModified(true);
        } else {
            cfRuleRecord.setLeftBorderModified(false);
        }
    }

    @Override
    public void setLeftBorderColor(Color color) {
        HSSFColor hcolor = HSSFColor.toHSSFColor(color);
        if (hcolor == null) {
            setLeftBorderColor((short) 0);
        } else {
            setLeftBorderColor(hcolor.getIndex());
        }
    }

    @Override
    public void setRightBorderColor(short color) {
        borderFormatting.setRightBorderColor(color);
        if (color != 0) {
            cfRuleRecord.setRightBorderModified(true);
        } else {
            cfRuleRecord.setRightBorderModified(false);
        }
    }

    @Override
    public void setRightBorderColor(Color color) {
        HSSFColor hcolor = HSSFColor.toHSSFColor(color);
        if (hcolor == null) {
            setRightBorderColor((short) 0);
        } else {
            setRightBorderColor(hcolor.getIndex());
        }
    }

    @Override
    public void setTopBorderColor(short color) {
        borderFormatting.setTopBorderColor(color);
        if (color != 0) {
            cfRuleRecord.setTopBorderModified(true);
        } else {
            cfRuleRecord.setTopBorderModified(false);
        }
    }

    @Override
    public void setTopBorderColor(Color color) {
        HSSFColor hcolor = HSSFColor.toHSSFColor(color);
        if (hcolor == null) {
            setTopBorderColor((short) 0);
        } else {
            setTopBorderColor(hcolor.getIndex());
        }
    }

    /**
     * HSSF doesn't support table borders, so always {@link BorderStyle#NONE}
     *
     * @see org.apache.poi.ss.usermodel.BorderFormatting#getBorderVertical()
     */
    public BorderStyle getBorderVertical() {
        return BorderStyle.NONE;
    }

    /**
     * HSSF doesn't support table borders, so always {@link BorderStyle#NONE}
     *
     * @see org.apache.poi.ss.usermodel.BorderFormatting#getBorderHorizontal()
     */
    public BorderStyle getBorderHorizontal() {
        return BorderStyle.NONE;
    }

    @Override
    public BorderStyle getBorderBottomEnum() {
        return getBorderBottom();
    }

    @Override
    public BorderStyle getBorderDiagonalEnum() {
        return getBorderDiagonal();
    }

    @Override
    public BorderStyle getBorderLeftEnum() {
        return getBorderLeft();
    }

    @Override
    public BorderStyle getBorderRightEnum() {
        return getBorderRight();
    }

    @Override
    public BorderStyle getBorderTopEnum() {
        return getBorderTop();
    }

    /**
     * HSSF doesn't support table borders, so always {@link BorderStyle#NONE}
     *
     * @see org.apache.poi.ss.usermodel.BorderFormatting#getBorderVertical()
     */
    @Deprecated
    @Override
    public BorderStyle getBorderVerticalEnum() {
        return getBorderVertical();
    }

    /**
     * HSSF doesn't support table borders, so always {@link BorderStyle#NONE}
     *
     * @see org.apache.poi.ss.usermodel.BorderFormatting#getBorderHorizontal()
     */
    @Deprecated
    @Override
    public BorderStyle getBorderHorizontalEnum() {
        return getBorderHorizontal();
    }

    /**
     * HSSF Doesn't support table borders, so always {@link HSSFColorPredefined#AUTOMATIC}
     *
     * @see org.apache.poi.ss.usermodel.BorderFormatting#getVerticalBorderColor()
     */
    public short getVerticalBorderColor() {
        return HSSFColorPredefined.AUTOMATIC.getIndex();
    }

    /**
     * HSSF Doesn't support table borders, so always {@link HSSFColorPredefined#AUTOMATIC}
     *
     * @see org.apache.poi.ss.usermodel.BorderFormatting#getVerticalBorderColorColor()
     */
    public Color getVerticalBorderColorColor() {
        return HSSFColorPredefined.AUTOMATIC.getColor();
    }

    /**
     * HSSF Doesn't support table borders, so always {@link HSSFColorPredefined#AUTOMATIC}
     *
     * @see org.apache.poi.ss.usermodel.BorderFormatting#getHorizontalBorderColor()
     */
    public short getHorizontalBorderColor() {
        return HSSFColorPredefined.AUTOMATIC.getIndex();
    }

    /**
     * HSSF Doesn't support table borders, so always {@link HSSFColorPredefined#AUTOMATIC}
     *
     * @see org.apache.poi.ss.usermodel.BorderFormatting#getHorizontalBorderColorColor()
     */
    public Color getHorizontalBorderColorColor() {
        return HSSFColorPredefined.AUTOMATIC.getColor();
    }

    /**
     * Not available for HSSF.
     *
     * @see org.apache.poi.ss.usermodel.BorderFormatting#setBorderHorizontal(org.apache.poi.ss.usermodel.BorderStyle)
     */
    public void setBorderHorizontal(BorderStyle border) {
        // nothing
    }

    /**
     * Not available for HSSF.
     *
     * @see org.apache.poi.ss.usermodel.BorderFormatting#setBorderVertical(org.apache.poi.ss.usermodel.BorderStyle)
     */
    public void setBorderVertical(BorderStyle border) {
        // nothing
    }

    /**
     * Not available for HSSF.
     *
     * @see org.apache.poi.ss.usermodel.BorderFormatting#setHorizontalBorderColor(short)
     */
    public void setHorizontalBorderColor(short color) {
        // nothing
    }

    /**
     * Not available for HSSF.
     *
     * @see org.apache.poi.ss.usermodel.BorderFormatting#setHorizontalBorderColor(org.apache.poi.ss.usermodel.Color)
     */
    public void setHorizontalBorderColor(Color color) {
        // nothing
    }

    /**
     * Not available for HSSF.
     *
     * @see org.apache.poi.ss.usermodel.BorderFormatting#setVerticalBorderColor(short)
     */
    public void setVerticalBorderColor(short color) {
        // nothing
    }

    /**
     * Not available for HSSF.
     *
     * @see org.apache.poi.ss.usermodel.BorderFormatting#setVerticalBorderColor(org.apache.poi.ss.usermodel.Color)
     */
    public void setVerticalBorderColor(Color color) {
        // nothing
    }
}
