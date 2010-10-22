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

package org.apache.poi.hssf.util;


import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.CellUtil;

/**
 *  Various utility functions that make working with a cells and rows easier.  The various
 * methods that deal with style's allow you to create your HSSFCellStyles as you need them.
 * When you apply a style change to a cell, the code will attempt to see if a style already
 * exists that meets your needs.  If not, then it will create a new style.  This is to prevent
 * creating too many styles.  there is an upper limit in Excel on the number of styles that
 * can be supported.
 *
 *@author     Eric Pugh epugh@upstate.com
 */
public final class HSSFCellUtil {

    private HSSFCellUtil() {
        // no instances of this class
    }

    /**
     *  Get a row from the spreadsheet, and create it if it doesn't exist.
     *
     *@param  rowIndex  The 0 based row number
     *@param  sheet       The sheet that the row is part of.
     *@return             The row indicated by the rowCounter
     */
    public static HSSFRow getRow(int rowIndex, HSSFSheet sheet) {
    	return (HSSFRow) CellUtil.getRow(rowIndex, sheet);
    }

    /**
     * Get a specific cell from a row. If the cell doesn't exist,
     *  then create it.
     *
     *@param  row     The row that the cell is part of
     *@param  columnIndex  The column index that the cell is in.
     *@return         The cell indicated by the column.
     */
    public static HSSFCell getCell(HSSFRow row, int columnIndex) {
        return (HSSFCell) CellUtil.getCell(row, columnIndex);
    }

    /**
     *  Creates a cell, gives it a value, and applies a style if provided
     *
     * @param  row     the row to create the cell in
     * @param  column  the column index to create the cell in
     * @param  value   The value of the cell
     * @param  style   If the style is not null, then set
     * @return         A new HSSFCell
     */
    public static HSSFCell createCell(HSSFRow row, int column, String value, HSSFCellStyle style) {
    	return (HSSFCell) CellUtil.createCell(row, column, value, style);
    }

    /**
     *  Create a cell, and give it a value.
     *
     *@param  row     the row to create the cell in
     *@param  column  the column index to create the cell in
     *@param  value   The value of the cell
     *@return         A new HSSFCell.
     */
    public static HSSFCell createCell(HSSFRow row, int column, String value) {
        return createCell( row, column, value, null );
    }

    /**
     *  Take a cell, and align it.
     *
     *@param  cell     the cell to set the alignment for
     *@param  workbook               The workbook that is being worked with.
     *@param  align  the column alignment to use.
     *
     * @see HSSFCellStyle for alignment options
     */
    public static void setAlignment(HSSFCell cell, HSSFWorkbook workbook, short align) {
    	CellUtil.setAlignment(cell, workbook, align);
    }

    /**
     *  Take a cell, and apply a font to it
     *
     *@param  cell     the cell to set the alignment for
     *@param  workbook               The workbook that is being worked with.
     *@param  font  The HSSFFont that you want to set...
     */
    public static void setFont(HSSFCell cell, HSSFWorkbook workbook, HSSFFont font) {
    	CellUtil.setFont(cell, workbook, font);
    }

    /**
     *  This method attempt to find an already existing HSSFCellStyle that matches
     *  what you want the style to be. If it does not find the style, then it
     *  creates a new one. If it does create a new one, then it applies the
     *  propertyName and propertyValue to the style. This is necessary because
     *  Excel has an upper limit on the number of Styles that it supports.
     *
     *@param  workbook               The workbook that is being worked with.
     *@param  propertyName           The name of the property that is to be
     *      changed.
     *@param  propertyValue          The value of the property that is to be
     *      changed.
     *@param  cell                   The cell that needs it's style changes
     */
    public static void setCellStyleProperty(HSSFCell cell, HSSFWorkbook workbook,
			String propertyName, Object propertyValue) {
    	CellUtil.setCellStyleProperty(cell, workbook, propertyName, propertyValue);
    }

    /**
     *  Looks for text in the cell that should be unicode, like &alpha; and provides the
     *  unicode version of it.
     *
     *@param  cell  The cell to check for unicode values
     *@return       translated to unicode
     */
    public static HSSFCell translateUnicodeValues(HSSFCell cell){
    	CellUtil.translateUnicodeValues(cell);
    	return cell;
    }
}
