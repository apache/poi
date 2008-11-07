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

package org.apache.poi.ss.usermodel;

import java.util.Date;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFSheet;

/**
 * This is a JDK 1.4 compatible interface for HSSFCell.
 * If you are using JDK 1.5 or later, use the other set of interfaces,
 *  which work properly for both HSSFCell and XSSFCell
 */
public interface Cell {
    /**
     * Numeric Cell type (0)
     * @see #setCellType(int)
     * @see #getCellType()
     */

    public final static int CELL_TYPE_NUMERIC = 0;

    /**
     * String Cell type (1)
     * @see #setCellType(int)
     * @see #getCellType()
     */

    public final static int CELL_TYPE_STRING = 1;

    /**
     * Formula Cell type (2)
     * @see #setCellType(int)
     * @see #getCellType()
     */

    public final static int CELL_TYPE_FORMULA = 2;

    /**
     * Blank Cell type (3)
     * @see #setCellType(int)
     * @see #getCellType()
     */

    public final static int CELL_TYPE_BLANK = 3;

    /**
     * Boolean Cell type (4)
     * @see #setCellType(int)
     * @see #getCellType()
     */

    public final static int CELL_TYPE_BOOLEAN = 4;

    /**
     * Error Cell type (5)
     * @see #setCellType(int)
     * @see #getCellType()
     */

    public final static int CELL_TYPE_ERROR = 5;


    int getCellType();
    /**
     * @deprecated (Oct 2008) use {@link #getColumnIndex()}
     */
    short getCellNum();
    
    /**
     * @return the cell's column index (zero based)
     */
    int getColumnIndex();
    int getRowIndex();

    String getCellFormula();

    byte getErrorCellValue();
    void setCellErrorValue(byte value);

    HSSFCellStyle getCellStyle();
    HSSFSheet getSheet();

    boolean getBooleanCellValue();
    double getNumericCellValue();
    /**
     * get the value of the cell as a date.  For strings we throw an exception.
     * For blank cells we return a null.
     */
    Date getDateCellValue();
    HSSFRichTextString getRichStringCellValue();

    void setCellType(int cellType);
    void setCellValue(boolean value);
    void setCellValue(double value);
    void setCellValue(RichTextString value);
    void setCellFormula(String formula);
}
