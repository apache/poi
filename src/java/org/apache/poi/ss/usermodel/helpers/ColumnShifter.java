package org.apache.poi.ss.usermodel.helpers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.poi.ss.formula.FormulaShifter;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.usermodel.Cell;

public class ColumnShifter {
    protected final Sheet shiftingSheet;
    protected FormulaShifter shifter;

    public ColumnShifter(Sheet sheet, FormulaShifter shifter) {
        shiftingSheet = sheet;
        this.shifter = shifter;
    }

    
    /**
     * Shifts, grows, or shrinks the merged regions due to a column shift.
     * Merged regions that are completely overlaid by shifting will be deleted.
     *
     * @param startColumnIndex index of the column to start shifting
     * @param endColumnIndex   index of the column to end shifting
     * @param n        the number of columns to shift
     * @return an array of affected merged regions, doesn't contain deleted ones
     */
    public List<CellRangeAddress> shiftMergedRegions(int startColumnIndex, int endColumnIndex, int n) {
        List<CellRangeAddress> shiftedRegions = new ArrayList<CellRangeAddress>();
        Set<Integer> removedIndices = new HashSet<Integer>();
        //move merged regions completely if they fall within the new region boundaries when they are shifted
        int size = shiftingSheet.getNumMergedRegions();
        for (int i = 0; i < size; i++) {
            CellRangeAddress merged = shiftingSheet.getMergedRegion(i);

            // remove merged region that overlaps shifting
            if (startColumnIndex + n <= merged.getFirstColumn() && endColumnIndex + n >= merged.getLastColumn()) {
                removedIndices.add(i);
                continue;
            }

            boolean inStart = (merged.getFirstColumn() >= startColumnIndex || merged.getLastColumn() >= startColumnIndex);
            boolean inEnd = (merged.getFirstColumn() <= endColumnIndex || merged.getLastColumn() <= endColumnIndex);

            //don't check if it's not within the shifted area
            if (!inStart || !inEnd) 
                continue;

            //only shift if the region outside the shifted columns is not merged too
            if (!merged.containsColumn(startColumnIndex - 1) && !merged.containsColumn(endColumnIndex + 1)) {
                merged.setFirstColumn(merged.getFirstColumn() + n);
                merged.setLastColumn(merged.getLastColumn() + n);
                //have to remove/add it back
                shiftedRegions.add(merged);
                removedIndices.add(i);
            }
        }
        
        if(!removedIndices.isEmpty()) {
            shiftingSheet.removeMergedRegions(removedIndices);
        }

        //read so it doesn't get shifted again
        for (CellRangeAddress region : shiftedRegions) {
            shiftingSheet.addMergedRegion(region);
        }
        return shiftedRegions;
    }
    
    public static void cloneCellValue(Cell oldCell, Cell newCell) {
        newCell.setCellComment(oldCell.getCellComment());
        switch (oldCell.getCellType()) {
            case STRING:
                newCell.setCellValue(oldCell.getStringCellValue());
                break;
            case NUMERIC:
                newCell.setCellValue(oldCell.getNumericCellValue());
                break;
            case BOOLEAN:
                newCell.setCellValue(oldCell.getBooleanCellValue());
                break;
            case FORMULA:
                newCell.setCellFormula(oldCell.getCellFormula());
                break;
            case ERROR:
                newCell.setCellErrorValue(oldCell.getErrorCellValue());
            case BLANK:
            case _NONE:
                break;
        }
    }
    
}
