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

package org.apache.poi.ss.util;

import org.apache.poi.ss.usermodel.*;

import java.text.AttributedString;
import java.awt.font.TextLayout;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;


/**
 * Helper methods for when working with Usermodel sheets
 *
 * @author Yegor Kozlov
 */
public class SheetUtil {

    /**
     * Excel measures columns in units of 1/256th of a character width
     * but the docs say nothing about what particular character is used.
     * '0' looks to be a good choice.
     */
    private static final char defaultChar = '0';

    /**
     * This is the multiple that the font height is scaled by when determining the
     * boundary of rotated text.
     */
    private static final double fontHeightMultiple = 2.0;

    /**
     *  Dummy formula evaluator that does nothing.
     *  YK: The only reason of having this class is that
     *  {@link org.apache.poi.ss.usermodel.DataFormatter#formatCellValue(org.apache.poi.ss.usermodel.Cell)}
     *  returns formula string for formula cells. Dummy evaluator makes it to format the cached formula result.
     *
     *  See Bugzilla #50021 
     */
    private static final FormulaEvaluator dummyEvaluator = new FormulaEvaluator(){
        public void clearAllCachedResultValues(){}
        public void notifySetFormula(Cell cell) {}
        public void notifyDeleteCell(Cell cell) {}
        public void notifyUpdateCell(Cell cell) {}
        public CellValue evaluate(Cell cell) {return null;  }
        public Cell evaluateInCell(Cell cell) { return null; }

        public int evaluateFormulaCell(Cell cell) {
            return cell.getCachedFormulaResultType();
        }

    };

    /**
     * drawing context to measure text
     */
    private static final FontRenderContext fontRenderContext = new FontRenderContext(null, true, true);

    /**
     * Compute width of a column and return the result
     *
     * @param sheet the sheet to calculate
     * @param column    0-based index of the column
     * @param useMergedCells    whether to use merged cells
     * @return  the width in pixels
     */
    public static double getColumnWidth(Sheet sheet, int column, boolean useMergedCells){
        AttributedString str;
        TextLayout layout;

        Workbook wb = sheet.getWorkbook();
        DataFormatter formatter = new DataFormatter();
        Font defaultFont = wb.getFontAt((short) 0);

        str = new AttributedString(String.valueOf(defaultChar));
        copyAttributes(defaultFont, str, 0, 1);
        layout = new TextLayout(str.getIterator(), fontRenderContext);
        int defaultCharWidth = (int)layout.getAdvance();

        double width = -1;
        rows:
        for (Row row : sheet) {
            Cell cell = row.getCell(column);

            if (cell == null) {
                continue;
            }

            int colspan = 1;
            for (int i = 0 ; i < sheet.getNumMergedRegions(); i++) {
                CellRangeAddress region = sheet.getMergedRegion(i);
                if (containsCell(region, row.getRowNum(), column)) {
                    if (!useMergedCells) {
                        // If we're not using merged cells, skip this one and move on to the next.
                        continue rows;
                    }
                    cell = row.getCell(region.getFirstColumn());
                    colspan = 1 + region.getLastColumn() - region.getFirstColumn();
                }
            }

            CellStyle style = cell.getCellStyle();
            int cellType = cell.getCellType();

            // for formula cells we compute the cell width for the cached formula result
            if(cellType == Cell.CELL_TYPE_FORMULA) cellType = cell.getCachedFormulaResultType();

            Font font = wb.getFontAt(style.getFontIndex());

            if (cellType == Cell.CELL_TYPE_STRING) {
                RichTextString rt = cell.getRichStringCellValue();
                String[] lines = rt.getString().split("\\n");
                for (int i = 0; i < lines.length; i++) {
                    String txt = lines[i] + defaultChar;

                    str = new AttributedString(txt);
                    copyAttributes(font, str, 0, txt.length());

                    if (rt.numFormattingRuns() > 0) {
                        // TODO: support rich text fragments
                    }

                    layout = new TextLayout(str.getIterator(), fontRenderContext);
                    if(style.getRotation() != 0){
                        /*
                         * Transform the text using a scale so that it's height is increased by a multiple of the leading,
                         * and then rotate the text before computing the bounds. The scale results in some whitespace around
                         * the unrotated top and bottom of the text that normally wouldn't be present if unscaled, but
                         * is added by the standard Excel autosize.
                         */
                        AffineTransform trans = new AffineTransform();
                        trans.concatenate(AffineTransform.getRotateInstance(style.getRotation()*2.0*Math.PI/360.0));
                        trans.concatenate(
                        AffineTransform.getScaleInstance(1, fontHeightMultiple)
                        );
                        width = Math.max(width, ((layout.getOutline(trans).getBounds().getWidth() / colspan) / defaultCharWidth) + cell.getCellStyle().getIndention());
                    } else {
                        width = Math.max(width, ((layout.getBounds().getWidth() / colspan) / defaultCharWidth) + cell.getCellStyle().getIndention());
                    }
                }
            } else {
                String sval = null;
                if (cellType == Cell.CELL_TYPE_NUMERIC) {
                    // Try to get it formatted to look the same as excel
                    try {
                        sval = formatter.formatCellValue(cell, dummyEvaluator);
                    } catch (Exception e) {
                        sval = String.valueOf(cell.getNumericCellValue());
                    }
                } else if (cellType == Cell.CELL_TYPE_BOOLEAN) {
                    sval = String.valueOf(cell.getBooleanCellValue()).toUpperCase();
                }
                if(sval != null) {
                    String txt = sval + defaultChar;
                    str = new AttributedString(txt);
                    copyAttributes(font, str, 0, txt.length());

                    layout = new TextLayout(str.getIterator(), fontRenderContext);
                    if(style.getRotation() != 0){
                        /*
                         * Transform the text using a scale so that it's height is increased by a multiple of the leading,
                         * and then rotate the text before computing the bounds. The scale results in some whitespace around
                         * the unrotated top and bottom of the text that normally wouldn't be present if unscaled, but
                         * is added by the standard Excel autosize.
                         */
                        AffineTransform trans = new AffineTransform();
                        trans.concatenate(AffineTransform.getRotateInstance(style.getRotation()*2.0*Math.PI/360.0));
                        trans.concatenate(
                        AffineTransform.getScaleInstance(1, fontHeightMultiple)
                        );
                        width = Math.max(width, ((layout.getOutline(trans).getBounds().getWidth() / colspan) / defaultCharWidth) + cell.getCellStyle().getIndention());
                    } else {
                        width = Math.max(width, ((layout.getBounds().getWidth() / colspan) / defaultCharWidth) + cell.getCellStyle().getIndention());
                    }
                }
            }

        }
        return width;        
    }

    /**
     * Copy text attributes from the supplied Font to Java2D AttributedString
     */
    private static void copyAttributes(Font font, AttributedString str, int startIdx, int endIdx) {
        str.addAttribute(TextAttribute.FAMILY, font.getFontName(), startIdx, endIdx);
        str.addAttribute(TextAttribute.SIZE, (float)font.getFontHeightInPoints());
        if (font.getBoldweight() == Font.BOLDWEIGHT_BOLD) str.addAttribute(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD, startIdx, endIdx);
        if (font.getItalic() ) str.addAttribute(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE, startIdx, endIdx);
        if (font.getUnderline() == Font.U_SINGLE ) str.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON, startIdx, endIdx);
    }

    public static boolean containsCell(CellRangeAddress cr, int rowIx, int colIx) {
        if (cr.getFirstRow() <= rowIx && cr.getLastRow() >= rowIx
                && cr.getFirstColumn() <= colIx && cr.getLastColumn() >= colIx)
        {
            return true;
        }
        return false;
    }

}