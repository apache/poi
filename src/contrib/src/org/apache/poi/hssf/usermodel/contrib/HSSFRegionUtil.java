
/*
 *  ====================================================================
 *  The Apache Software License, Version 1.1
 *
 *  Copyright (c) 2003 The Apache Software Foundation.  All rights
 *  reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in
 *  the documentation and/or other materials provided with the
 *  distribution.
 *
 *  3. The end-user documentation included with the redistribution,
 *  if any, must include the following acknowledgment:
 *  "This product includes software developed by the
 *  Apache Software Foundation (http://www.apache.org/)."
 *  Alternately, this acknowledgment may appear in the software itself,
 *  if and wherever such third-party acknowledgments normally appear.
 *
 *  4. The names "Apache" and "Apache Software Foundation" and
 *  "Apache POI" must not be used to endorse or promote products
 *  derived from this software without prior written permission. For
 *  written permission, please contact apache@apache.org.
 *
 *  5. Products derived from this software may not be called "Apache",
 *  "Apache POI", nor may "Apache" appear in their name, without
 *  prior written permission of the Apache Software Foundation.
 *
 *  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 *  ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *  USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 *  OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 *  SUCH DAMAGE.
 *  ====================================================================
 *
 *  This software consists of voluntary contributions made by many
 *  individuals on behalf of the Apache Software Foundation.  For more
 *  information on the Apache Software Foundation, please see
 *  <http://www.apache.org/>.
 */
package org.apache.poi.hssf.usermodel.contrib;

import org.apache.log4j.Category;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import org.apache.poi.hssf.util.Region;

import org.apache.commons.lang.exception.NestableException;

/**
 *  Various utility functions that make working with a region of cells easier.
 *
 *@author     Eric Pugh epugh@upstate.com
 *@since      July 29, 2002
 */

public class HSSFRegionUtil {
  private static Category log = Category.getInstance( HSSFRegionUtil.class.getName() );

  /**  Constructor for the HSSFRegionUtil object */
  public HSSFRegionUtil() { }

  /**
   *  Sets the left border for a region of cells by manipulating the cell style
   *  of the indidual cells on the left
   *
   *@param  border                 The new border
   *@param  region                 The region that should have the border
   *@param  workbook               The workbook that the region is on.
   *@param  sheet                  The sheet that the region is on.
   *@exception  NestableException  Thrown if the CellStyle can't be changed
   */
  public static void setBorderLeft( short border, Region region, HSSFSheet sheet, HSSFWorkbook workbook )
    throws NestableException {
    int rowStart = region.getRowFrom();
    int rowEnd = region.getRowTo();
    int column = region.getColumnFrom();

    for ( int i = rowStart; i <= rowEnd; i++ ) {
      HSSFRow row = HSSFCellUtil.getRow( i, sheet );
      HSSFCell cell = HSSFCellUtil.getCell( row, column );
      HSSFCellUtil.setCellStyleProperty( cell, workbook, "borderLeft", new Short( border ) );
    }
  }

  /**
   *  Sets the leftBorderColor attribute of the HSSFRegionUtil object
   *
   *@param  color                  The color of the border
   *@param  region                 The region that should have the border
   *@param  workbook               The workbook that the region is on.
   *@param  sheet                  The sheet that the region is on.
   *@exception  NestableException  Thrown if the CellStyle can't be changed
   *      properly.
   */
  public static void setLeftBorderColor( short color, Region region, HSSFSheet sheet, HSSFWorkbook workbook )
    throws NestableException {
    int rowStart = region.getRowFrom();
    int rowEnd = region.getRowTo();
    int column = region.getColumnFrom();

    for ( int i = rowStart; i <= rowEnd; i++ ) {
      HSSFRow row = HSSFCellUtil.getRow( i, sheet );
      HSSFCell cell = HSSFCellUtil.getCell( row, column );
      HSSFCellUtil.setCellStyleProperty( cell, workbook, "leftBorderColor", new Short( color ) );
    }
  }

  /**
   *  Sets the borderRight attribute of the HSSFRegionUtil object
   *
   *@param  border                 The new border
   *@param  region                 The region that should have the border
   *@param  workbook               The workbook that the region is on.
   *@param  sheet                  The sheet that the region is on.
   *@exception  NestableException  Thrown if the CellStyle can't be changed
   */
  public static void setBorderRight( short border, Region region, HSSFSheet sheet, HSSFWorkbook workbook )
    throws NestableException {
    int rowStart = region.getRowFrom();
    int rowEnd = region.getRowTo();
    int column = region.getColumnTo();

    for ( int i = rowStart; i <= rowEnd; i++ ) {
      HSSFRow row = HSSFCellUtil.getRow( i, sheet );
      HSSFCell cell = HSSFCellUtil.getCell( row, column );

      HSSFCellUtil.setCellStyleProperty( cell, workbook, "borderRight", new Short( border ) );
    }
  }

  /**
   *  Sets the rightBorderColor attribute of the HSSFRegionUtil object
   *
   *@param  color                  The color of the border
   *@param  region                 The region that should have the border
   *@param  workbook               The workbook that the region is on.
   *@param  sheet                  The sheet that the region is on.
   *@exception  NestableException  Thrown if the CellStyle can't be changed
   *      properly.
   */
  public static void setRightBorderColor( short color, Region region, HSSFSheet sheet, HSSFWorkbook workbook )
    throws NestableException {
    int rowStart = region.getRowFrom();
    int rowEnd = region.getRowTo();
    int column = region.getColumnTo();

    for ( int i = rowStart; i <= rowEnd; i++ ) {
      HSSFRow row = HSSFCellUtil.getRow( i, sheet );
      HSSFCell cell = HSSFCellUtil.getCell( row, column );
      HSSFCellUtil.setCellStyleProperty( cell, workbook, "rightBorderColor", new Short( color ) );
    }
  }

  /**
   *  Sets the borderBottom attribute of the HSSFRegionUtil object
   *
   *@param  border                 The new border
   *@param  region                 The region that should have the border
   *@param  workbook               The workbook that the region is on.
   *@param  sheet                  The sheet that the region is on.
   *@exception  NestableException  Thrown if the CellStyle can't be changed
   */
  public static void setBorderBottom( short border, Region region, HSSFSheet sheet, HSSFWorkbook workbook )
    throws NestableException {
    int colStart = region.getColumnFrom();
    int colEnd = region.getColumnTo();
    int rowIndex = region.getRowTo();
    HSSFRow row = HSSFCellUtil.getRow( rowIndex, sheet );
    for ( int i = colStart; i <= colEnd; i++ ) {

      HSSFCell cell = HSSFCellUtil.getCell( row, i );
      HSSFCellUtil.setCellStyleProperty( cell, workbook, "borderBottom", new Short( border ) );
    }
  }

  /**
   *  Sets the bottomBorderColor attribute of the HSSFRegionUtil object
   *
   *@param  color                  The color of the border
   *@param  region                 The region that should have the border
   *@param  workbook               The workbook that the region is on.
   *@param  sheet                  The sheet that the region is on.
   *@exception  NestableException  Thrown if the CellStyle can't be changed
   *      properly.
   */
  public static void setBottomBorderColor( short color, Region region, HSSFSheet sheet, HSSFWorkbook workbook )
    throws NestableException {
    int colStart = region.getColumnFrom();
    int colEnd = region.getColumnTo();
    int rowIndex = region.getRowTo();
    HSSFRow row = HSSFCellUtil.getRow( rowIndex, sheet );
    for ( int i = colStart; i <= colEnd; i++ ) {
      HSSFCell cell = HSSFCellUtil.getCell( row, i );
      HSSFCellUtil.setCellStyleProperty( cell, workbook, "bottomBorderColor", new Short( color ) );
    }
  }


  /**
   *  Sets the borderBottom attribute of the HSSFRegionUtil object
   *
   *@param  border                 The new border
   *@param  region                 The region that should have the border
   *@param  workbook               The workbook that the region is on.
   *@param  sheet                  The sheet that the region is on.
   *@exception  NestableException  Thrown if the CellStyle can't be changed
   */
  public static void setBorderTop( short border, Region region, HSSFSheet sheet, HSSFWorkbook workbook )
    throws NestableException {
    int colStart = region.getColumnFrom();
    int colEnd = region.getColumnTo();
    int rowIndex = region.getRowFrom();
    HSSFRow row = HSSFCellUtil.getRow( rowIndex, sheet );
    for ( int i = colStart; i <= colEnd; i++ ) {

      HSSFCell cell = HSSFCellUtil.getCell( row, i );
      HSSFCellUtil.setCellStyleProperty( cell, workbook, "borderTop", new Short( border ) );
    }
  }

  /**
   *  Sets the topBorderColor attribute of the HSSFRegionUtil object
   *
   *@param  color                  The color of the border
   *@param  region                 The region that should have the border
   *@param  workbook               The workbook that the region is on.
   *@param  sheet                  The sheet that the region is on.
   *@exception  NestableException  Thrown if the CellStyle can't be changed
   *      properly.
   */
  public static void setTopBorderColor( short color, Region region, HSSFSheet sheet, HSSFWorkbook workbook )
    throws NestableException {
    int colStart = region.getColumnFrom();
    int colEnd = region.getColumnTo();
    int rowIndex = region.getRowFrom();
    HSSFRow row = HSSFCellUtil.getRow( rowIndex, sheet );
    for ( int i = colStart; i <= colEnd; i++ ) {
      HSSFCell cell = HSSFCellUtil.getCell( row, i );
      HSSFCellUtil.setCellStyleProperty( cell, workbook, "topBorderColor", new Short( color ) );

    }
  }

}

