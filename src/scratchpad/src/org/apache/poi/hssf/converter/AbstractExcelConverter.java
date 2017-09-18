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
package org.apache.poi.hssf.converter;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormatter;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hwpf.converter.AbstractWordConverter;
import org.apache.poi.hwpf.converter.DefaultFontReplacer;
import org.apache.poi.hwpf.converter.FontReplacer;
import org.apache.poi.hwpf.converter.NumberFormatter;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.util.Beta;
import org.w3c.dom.Document;

/**
 * Common class for {@link ExcelToFoConverter} and {@link ExcelToHtmlConverter}
 * 
 * @author Sergey Vladimirov (vlsergey {at} gmail {dot} com)
 * @see AbstractWordConverter
 */
@Beta
public abstract class AbstractExcelConverter
{
    protected static int getColumnWidth( HSSFSheet sheet, int columnIndex )
    {
        return ExcelToHtmlUtils.getColumnWidthInPx( sheet
                .getColumnWidth( columnIndex ) );
    }

    protected static int getDefaultColumnWidth( HSSFSheet sheet )
    {
        return ExcelToHtmlUtils.getColumnWidthInPx( sheet
                .getDefaultColumnWidth() );
    }

    protected final HSSFDataFormatter _formatter = new HSSFDataFormatter();

    private FontReplacer fontReplacer = new DefaultFontReplacer();

    private boolean outputColumnHeaders = true;

    private boolean outputHiddenColumns;

    private boolean outputHiddenRows;

    private boolean outputLeadingSpacesAsNonBreaking = true;

    private boolean outputRowNumbers = true;

    /**
     * Generates name for output as column header in case
     * <tt>{@link #isOutputColumnHeaders()} == true</tt>
     * 
     * @param columnIndex
     *            0-based column index
     */
    protected String getColumnName( int columnIndex )
    {
        return NumberFormatter.getNumber( columnIndex + 1, 3 );
    }

    protected abstract Document getDocument();

    public FontReplacer getFontReplacer()
    {
        return fontReplacer;
    }

    /**
     * Generates name for output as row number in case
     * <tt>{@link #isOutputRowNumbers()} == true</tt>
     */
    protected String getRowName( HSSFRow row )
    {
        return String.valueOf( row.getRowNum() + 1 );
    }

    public boolean isOutputColumnHeaders()
    {
        return outputColumnHeaders;
    }

    public boolean isOutputHiddenColumns()
    {
        return outputHiddenColumns;
    }

    public boolean isOutputHiddenRows()
    {
        return outputHiddenRows;
    }

    public boolean isOutputLeadingSpacesAsNonBreaking()
    {
        return outputLeadingSpacesAsNonBreaking;
    }

    public boolean isOutputRowNumbers()
    {
        return outputRowNumbers;
    }

    protected boolean isTextEmpty( HSSFCell cell )
    {
        final String value;
        switch ( cell.getCellType() )
        {
        case STRING:
            // XXX: enrich
            value = cell.getRichStringCellValue().getString();
            break;
        case FORMULA:
            switch ( cell.getCachedFormulaResultType() )
            {
            case STRING:
                HSSFRichTextString str = cell.getRichStringCellValue();
                if ( str == null || str.length() <= 0 )
                    return false;

                value = str.toString();
                break;
            case NUMERIC:
                HSSFCellStyle style = cell.getCellStyle();
                double nval = cell.getNumericCellValue();
                short df = style.getDataFormat();
                String dfs = style.getDataFormatString();
                value = _formatter.formatRawCellContents(nval, df, dfs);
                break;
            case BOOLEAN:
                value = String.valueOf( cell.getBooleanCellValue() );
                break;
            case ERROR:
                value = ErrorEval.getText( cell.getErrorCellValue() );
                break;
            default:
                value = ExcelToHtmlUtils.EMPTY;
                break;
            }
            break;
        case BLANK:
            value = ExcelToHtmlUtils.EMPTY;
            break;
        case NUMERIC:
            value = _formatter.formatCellValue( cell );
            break;
        case BOOLEAN:
            value = String.valueOf( cell.getBooleanCellValue() );
            break;
        case ERROR:
            value = ErrorEval.getText( cell.getErrorCellValue() );
            break;
        default:
            return true;
        }

        return ExcelToHtmlUtils.isEmpty( value );
    }

    public void setFontReplacer( FontReplacer fontReplacer )
    {
        this.fontReplacer = fontReplacer;
    }

    public void setOutputColumnHeaders( boolean outputColumnHeaders )
    {
        this.outputColumnHeaders = outputColumnHeaders;
    }

    public void setOutputHiddenColumns( boolean outputZeroWidthColumns )
    {
        this.outputHiddenColumns = outputZeroWidthColumns;
    }

    public void setOutputHiddenRows( boolean outputZeroHeightRows )
    {
        this.outputHiddenRows = outputZeroHeightRows;
    }

    public void setOutputLeadingSpacesAsNonBreaking(
            boolean outputPrePostSpacesAsNonBreaking )
    {
        this.outputLeadingSpacesAsNonBreaking = outputPrePostSpacesAsNonBreaking;
    }

    public void setOutputRowNumbers( boolean outputRowNumbers )
    {
        this.outputRowNumbers = outputRowNumbers;
    }

}
