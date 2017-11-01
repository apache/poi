package org.apache.poi.hssf.usermodel.helpers; 
 
import org.apache.poi.hssf.usermodel.HSSFCell; 
import org.apache.poi.hssf.usermodel.HSSFRow; 
import org.apache.poi.ss.formula.FormulaShifter; 
import org.apache.poi.ss.usermodel.Cell; 
import org.apache.poi.ss.usermodel.CellType; 
import org.apache.poi.ss.usermodel.Row; 
import org.apache.poi.ss.usermodel.Sheet; 
import org.apache.poi.ss.usermodel.helpers.ColumnShifter; 
import org.apache.poi.util.POILogFactory; 
import org.apache.poi.util.POILogger; 
import org.apache.poi.xssf.usermodel.helpers.XSSFRowShifter; 
 
public class HSSFColumnShifter extends ColumnShifter{ 
    private static final POILogger logger = POILogFactory.getLogger(XSSFRowShifter.class); 
     
    private int firstShiftColumnIndex;  
    private int lastShiftColumnIndex;  
    private int shiftStep; 
     
    public HSSFColumnShifter(Sheet sh, FormulaShifter shifter) { 
        super(sh, shifter); 
    } 
 
    public void shiftColumns(int firstShiftColumnIndex, int lastShiftColumnIndex, int step){ 
        this.firstShiftColumnIndex = firstShiftColumnIndex; 
        this.lastShiftColumnIndex = lastShiftColumnIndex; 
        this.shiftStep = step; 
        if(shiftStep > 0) 
            shiftColumnsRight(); 
        else if(shiftStep < 0) 
            shiftColumnsLeft(); 
//        formulaShiftingManager.updateFormulas(); 
    } 
    /** 
     * Inserts shiftStep empty columns at firstShiftColumnIndex-th position, and shifts rest columns to the right  
     * (see constructor for parameters) 
     */ 
 
    private void shiftColumnsRight(){ 
        for(int rowNo = 0; rowNo <= shiftingSheet.getLastRowNum(); rowNo++) 
        {    
            Row row = shiftingSheet.getRow(rowNo); 
            if(row == null) 
                continue; 
            for (int columnIndex = lastShiftColumnIndex; columnIndex >= firstShiftColumnIndex; columnIndex--){ // process cells backwards, because of shifting  
                HSSFCell oldCell = (HSSFCell)row.getCell(columnIndex); 
                Cell newCell = null; 
                if(oldCell == null){ 
                    newCell = row.getCell(columnIndex + shiftStep); 
                    newCell = null; 
                    continue; 
                } 
                else { 
                    newCell = row.createCell(columnIndex + shiftStep, oldCell.getCellType()); 
                    cloneCellValue(oldCell,newCell); 
                    if(columnIndex <= firstShiftColumnIndex + shiftStep - 1){ // clear existing cells on place of insertion 
                        oldCell.setCellValue(""); 
                        oldCell.setCellType(CellType.STRING); 
                    } 
                } 
            } 
        } 
    } 
    private void shiftColumnsLeft(){ 
        for(int rowNo = 0; rowNo <= shiftingSheet.getLastRowNum(); rowNo++) 
        {    
            HSSFRow row = (HSSFRow)shiftingSheet.getRow(rowNo); 
            if(row == null) 
                continue; 
            for (int columnIndex = 0; columnIndex < row.getLastCellNum(); columnIndex++){  
                HSSFCell oldCell = row.getCell(columnIndex); 
                if(columnIndex >= firstShiftColumnIndex + shiftStep && columnIndex < row.getLastCellNum() - shiftStep){ // shift existing cell  
                    org.apache.poi.ss.usermodel.Cell newCell = null; 
                    newCell = row.getCell(columnIndex - shiftStep); 
                    if(oldCell != null){ 
                        if(newCell != null){ 
                            oldCell.setCellType(newCell.getCellType()); 
                            cloneCellValue(newCell, oldCell); 
                        } 
                        else { 
                            oldCell.setCellType(CellType.STRING); 
                            oldCell.setCellValue(""); 
                        } 
                    } 
                    else { 
                        oldCell = row.createCell(columnIndex); 
                        if(newCell != null){ 
                            oldCell.setCellType(newCell.getCellType()); 
                            cloneCellValue(newCell, oldCell); 
                        } 
                        else { 
                            oldCell.setCellType(CellType.STRING); 
                            oldCell.setCellValue(""); 
                        } 
                    } 
                } 
            } 
        } 
    } 
} 