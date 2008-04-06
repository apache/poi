
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

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import org.apache.poi.ss.util.Region;

import org.apache.commons.lang.exception.NestableException;

/**
 *  Various utility functions that make working with a region of cells easier.
 *
 *@author     Eric Pugh epugh@upstate.com
 *@since      July 29, 2002
 */

public class HSSFRegionUtil
{

  /**  Constructor for the HSSFRegionUtil object */
  private HSSFRegionUtil() { }

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

