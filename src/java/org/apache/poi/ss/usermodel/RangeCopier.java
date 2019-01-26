/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.ss.usermodel;

import java.util.Map;
import org.apache.poi.ss.formula.FormulaShifter;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.Beta;

@Beta
public abstract class RangeCopier {
    private Sheet sourceSheet;
    private Sheet destSheet;
    private FormulaShifter horizontalFormulaShifter;
    private FormulaShifter verticalFormulaShifter;

    public RangeCopier(Sheet sourceSheet, Sheet destSheet) {
        this.sourceSheet = sourceSheet;
        this.destSheet = destSheet;
    }
    public RangeCopier(Sheet sheet) {
        this(sheet, sheet);
    }
    /** Uses input pattern to tile destination region, overwriting existing content. Works in following manner : 
     * 1.Start from top-left of destination.
     * 2.Paste source but only inside of destination borders.
     * 3.If there is space left on right or bottom side of copy, process it as in step 2. 
     * @param tilePatternRange source range which should be copied in tiled manner
     * @param tileDestRange     destination range, which should be overridden
     */
    public void copyRange(CellRangeAddress tilePatternRange, CellRangeAddress tileDestRange) {
        Sheet sourceCopy = sourceSheet.getWorkbook().cloneSheet(sourceSheet.getWorkbook().getSheetIndex(sourceSheet));
        int sourceWidthMinus1 = tilePatternRange.getLastColumn() - tilePatternRange.getFirstColumn();
        int sourceHeightMinus1 = tilePatternRange.getLastRow() - tilePatternRange.getFirstRow();
        int rightLimitToCopy; 
        int bottomLimitToCopy;

        int nextRowIndexToCopy = tileDestRange.getFirstRow();
        do { 
            int nextCellIndexInRowToCopy = tileDestRange.getFirstColumn();
            int heightToCopyMinus1 = Math.min(sourceHeightMinus1, tileDestRange.getLastRow() - nextRowIndexToCopy);
            bottomLimitToCopy = tilePatternRange.getFirstRow() + heightToCopyMinus1;
            do { 
                int widthToCopyMinus1 = Math.min(sourceWidthMinus1, tileDestRange.getLastColumn() - nextCellIndexInRowToCopy);
                rightLimitToCopy = tilePatternRange.getFirstColumn() + widthToCopyMinus1;
                CellRangeAddress rangeToCopy = new CellRangeAddress(
                        tilePatternRange.getFirstRow(),     bottomLimitToCopy,
                        tilePatternRange.getFirstColumn(),  rightLimitToCopy 
                       );
                copyRange(rangeToCopy, nextCellIndexInRowToCopy - rangeToCopy.getFirstColumn(), nextRowIndexToCopy - rangeToCopy.getFirstRow(), sourceCopy);
                nextCellIndexInRowToCopy += widthToCopyMinus1 + 1; 
            } while (nextCellIndexInRowToCopy <= tileDestRange.getLastColumn());
            nextRowIndexToCopy += heightToCopyMinus1 + 1;
        } while (nextRowIndexToCopy <= tileDestRange.getLastRow());
        
        int tempCopyIndex = sourceSheet.getWorkbook().getSheetIndex(sourceCopy);
        sourceSheet.getWorkbook().removeSheetAt(tempCopyIndex); 
    }

    private void copyRange(CellRangeAddress sourceRange, int deltaX, int deltaY, Sheet sourceClone) { //NOSONAR, it's a bit complex but monolith method, does not make much sense to divide it
        if(deltaX != 0)
            horizontalFormulaShifter = FormulaShifter.createForColumnCopy(sourceSheet.getWorkbook().getSheetIndex(sourceSheet), 
                    sourceSheet.getSheetName(), sourceRange.getFirstColumn(), sourceRange.getLastColumn(), deltaX, sourceSheet.getWorkbook().getSpreadsheetVersion());
        if(deltaY != 0)
            verticalFormulaShifter = FormulaShifter.createForRowCopy(sourceSheet.getWorkbook().getSheetIndex(sourceSheet), 
                    sourceSheet.getSheetName(), sourceRange.getFirstRow(), sourceRange.getLastRow(), deltaY, sourceSheet.getWorkbook().getSpreadsheetVersion());
        
        for(int rowNo = sourceRange.getFirstRow(); rowNo <= sourceRange.getLastRow(); rowNo++) {   
            Row sourceRow = sourceClone.getRow(rowNo); // copy from source copy, original source might be overridden in process!
            for (int columnIndex = sourceRange.getFirstColumn(); columnIndex <= sourceRange.getLastColumn(); columnIndex++) {  
                Cell sourceCell = sourceRow.getCell(columnIndex);
                if(sourceCell == null)
                    continue;
                Row destRow = destSheet.getRow(rowNo + deltaY);
                if(destRow == null)
                    destRow = destSheet.createRow(rowNo + deltaY);
                
                Cell newCell = destRow.getCell(columnIndex + deltaX);
                if(newCell == null) {
                    newCell = destRow.createCell(columnIndex + deltaX);
                }

                cloneCellContent(sourceCell, newCell, null);
                if(newCell.getCellType() == CellType.FORMULA)
                    adjustCellReferencesInsideFormula(newCell, destSheet, deltaX, deltaY);
            }
        }
    }
    
    protected abstract void adjustCellReferencesInsideFormula(Cell cell, Sheet destSheet, int deltaX, int deltaY); // this part is different for HSSF and XSSF
    
    protected boolean adjustInBothDirections(Ptg[] ptgs, int sheetIndex, int deltaX, int deltaY) {
        boolean adjustSucceeded = true;
        if(deltaY != 0)
            adjustSucceeded = verticalFormulaShifter.adjustFormula(ptgs, sheetIndex); 
        if(deltaX != 0)
            adjustSucceeded = adjustSucceeded && horizontalFormulaShifter.adjustFormula(ptgs, sheetIndex);
        return adjustSucceeded;
    }
    
    // TODO clone some more properties ? 
    public static void cloneCellContent(Cell srcCell, Cell destCell, Map<Integer, CellStyle> styleMap) {   
         if(styleMap != null) {   
             if(srcCell.getSheet().getWorkbook() == destCell.getSheet().getWorkbook()){   
                 destCell.setCellStyle(srcCell.getCellStyle());   
             } else {
                 int stHashCode = srcCell.getCellStyle().hashCode();   
                 CellStyle newCellStyle = styleMap.get(stHashCode);   
                 if(newCellStyle == null){   
                     newCellStyle = destCell.getSheet().getWorkbook().createCellStyle();   
                     newCellStyle.cloneStyleFrom(srcCell.getCellStyle());   
                     styleMap.put(stHashCode, newCellStyle);   
                 }   
                 destCell.setCellStyle(newCellStyle);   
             }   
         }   
         switch(srcCell.getCellType()) {   
             case STRING:   
                 destCell.setCellValue(srcCell.getStringCellValue());   
                 break;   
             case NUMERIC:
                 destCell.setCellValue(srcCell.getNumericCellValue());   
                 break;   
             case BLANK:   
                 destCell.setBlank();
                 break;   
             case BOOLEAN:   
                 destCell.setCellValue(srcCell.getBooleanCellValue());   
                 break;   
             case ERROR:   
                 destCell.setCellErrorValue(srcCell.getErrorCellValue());   
                 break;   
             case FORMULA: 
                 String oldFormula = srcCell.getCellFormula();
                 destCell.setCellFormula(oldFormula);   
                 break;   
             default:   
                 break;   
         }   
     }
}
