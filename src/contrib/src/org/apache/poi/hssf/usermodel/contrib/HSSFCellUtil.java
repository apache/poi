
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
        

package org.apache.poi.hssf.usermodel.contrib;


import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.NestableException;
import org.apache.poi.hssf.usermodel.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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

public class HSSFCellUtil
{

    private static HashMap unicodeMappings = new HashMap();


    /**
     *  Get a row from the spreadsheet, and create it if it doesn't exist.
     *
     *@param  rowCounter  The 0 based row number
     *@param  sheet       The sheet that the row is part of.
     *@return             The row indicated by the rowCounter
     */
    public static HSSFRow getRow( int rowCounter, HSSFSheet sheet )
    {
        HSSFRow row = sheet.getRow( rowCounter );
        if ( row == null )
        {
            row = sheet.createRow( rowCounter );
        }

        return row;
    }


    /**
     * Get a specific cell from a row. If the cell doesn't exist, 
     *  then create it.
     *
     *@param  row     The row that the cell is part of
     *@param  column  The column index that the cell is in.
     *@return         The cell indicated by the column.
     */
    public static HSSFCell getCell( HSSFRow row, int column )
    {
        HSSFCell cell = row.getCell( column );

        if ( cell == null )
        {
            cell = row.createCell( (short)column );
        }
        return cell;
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

    public static HSSFCell createCell( HSSFRow row, int column, String value, HSSFCellStyle style )
    {
        HSSFCell cell = getCell( row, column );

        cell.setCellValue(new HSSFRichTextString(value));
        if ( style != null )
        {
            cell.setCellStyle( style );
        }

        return cell;
    }


    /**
     *  Create a cell, and give it a value.
     *
     *@param  row     the row to create the cell in
     *@param  column  the column index to create the cell in
     *@param  value   The value of the cell
     *@return         A new HSSFCell.
     */
    public static HSSFCell createCell( HSSFRow row, int column, String value )
    {
        return createCell( row, column, value, null );
    }


    /**
     *  Take a cell, and align it.
     *
     *@param  cell     the cell to set the alignment for
     *@param  workbook               The workbook that is being worked with.
     *@param  align  the column alignment to use.
     *@exception  NestableException  Thrown if an error happens.
     *
     * @see HSSFCellStyle for alignment options
     */
    public static void setAlignment( HSSFCell cell, HSSFWorkbook workbook, short align ) throws NestableException
    {
        setCellStyleProperty( cell, workbook, "alignment", new Short( align ) );
    }

    /**
     *  Take a cell, and apply a font to it
     *
     *@param  cell     the cell to set the alignment for
     *@param  workbook               The workbook that is being worked with.
     *@param  font  The HSSFFont that you want to set...
     *@exception  NestableException  Thrown if an error happens.
     */
    public static void setFont( HSSFCell cell, HSSFWorkbook workbook, HSSFFont font ) throws NestableException
    {
        setCellStyleProperty( cell, workbook, "font", font );
    }

    /**
     *  This method attempt to find an already existing HSSFCellStyle that matches
     *  what you want the style to be. If it does not find the style, then it
     *  creates a new one. If it does create a new one, then it applyies the
     *  propertyName and propertyValue to the style. This is nessasary because
     *  Excel has an upper limit on the number of Styles that it supports.
     *
     *@param  workbook               The workbook that is being worked with.
     *@param  propertyName           The name of the property that is to be
     *      changed.
     *@param  propertyValue          The value of the property that is to be
     *      changed.
     *@param  cell                   The cell that needs it's style changes
     *@exception  NestableException  Thrown if an error happens.
     */
    public static void setCellStyleProperty( HSSFCell cell, HSSFWorkbook workbook, String propertyName, Object propertyValue )
            throws NestableException
    {
        try
        {
            HSSFCellStyle originalStyle = cell.getCellStyle();
            HSSFCellStyle newStyle = null;
            Map values = PropertyUtils.describe( originalStyle );
            values.put( propertyName, propertyValue );
            values.remove( "index" );

            // index seems like what  index the cellstyle is in the list of styles for a workbook.
            // not good to compare on!
            short numberCellStyles = workbook.getNumCellStyles();

            for ( short i = 0; i < numberCellStyles; i++ )
            {
                HSSFCellStyle wbStyle = workbook.getCellStyleAt( i );
                Map wbStyleMap = PropertyUtils.describe( wbStyle );
                wbStyleMap.remove( "index" );

                if ( wbStyleMap.equals( values ) )
                {
                    newStyle = wbStyle;
                    break;
                }
            }

            if ( newStyle == null )
            {
                newStyle = workbook.createCellStyle();
                newStyle.setFont( workbook.getFontAt( originalStyle.getFontIndex() ) );
                PropertyUtils.copyProperties( newStyle, originalStyle );
                PropertyUtils.setProperty( newStyle, propertyName, propertyValue );
            }

            cell.setCellStyle( newStyle );
        }
        catch ( Exception e )
        {
            e.printStackTrace();

            throw new NestableException( "Couldn't setCellStyleProperty.", e );
        }
    }


    /**
     *  Looks for text in the cell that should be unicode, like &alpha; and provides the
     *  unicode version of it.
     *
     *@param  cell  The cell to check for unicode values
     *@return       transalted to unicode
     */
    public static HSSFCell translateUnicodeValues( HSSFCell cell )
    {

        String s = cell.getRichStringCellValue().getString(); 
        boolean foundUnicode = false;

        for ( Iterator i = unicodeMappings.entrySet().iterator(); i.hasNext(); )
        {
            Map.Entry entry = (Map.Entry) i.next();
            String key = (String) entry.getKey();
            if ( s.toLowerCase().indexOf( key ) != -1 )
            {
                s = StringUtils.replace( s, key, "" + entry.getValue().toString() + "" );
                foundUnicode = true;
            }
        }
        if ( foundUnicode )
        {
            cell.setEncoding( HSSFCell.ENCODING_UTF_16 );
            cell.setCellValue( s );
        }
        return cell;
    }

    
    static {
        unicodeMappings.put( "&alpha;",   "\u03B1" );
        unicodeMappings.put( "&beta;",    "\u03B2" );
        unicodeMappings.put( "&gamma;",   "\u03B3" );
        unicodeMappings.put( "&delta;",   "\u03B4" );
        unicodeMappings.put( "&epsilon;", "\u03B5" );
        unicodeMappings.put( "&zeta;",    "\u03B6" );
        unicodeMappings.put( "&eta;",     "\u03B7" );
        unicodeMappings.put( "&theta;",   "\u03B8" );
        unicodeMappings.put( "&iota;",    "\u03B9" );
        unicodeMappings.put( "&kappa;",   "\u03BA" );
        unicodeMappings.put( "&lambda;",  "\u03BB" );
        unicodeMappings.put( "&mu;",      "\u03BC" );
        unicodeMappings.put( "&nu;",      "\u03BD" );
        unicodeMappings.put( "&xi;",      "\u03BE" );
        unicodeMappings.put( "&omicron;", "\u03BF" );
    }

}
