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

package org.apache.poi.ss.formula;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.ptg.Area2DPtgBase;
import org.apache.poi.ss.formula.ptg.Area3DPtg;
import org.apache.poi.ss.formula.ptg.Area3DPxg;
import org.apache.poi.ss.formula.ptg.AreaErrPtg;
import org.apache.poi.ss.formula.ptg.AreaPtg;
import org.apache.poi.ss.formula.ptg.AreaPtgBase;
import org.apache.poi.ss.formula.ptg.Deleted3DPxg;
import org.apache.poi.ss.formula.ptg.DeletedArea3DPtg;
import org.apache.poi.ss.formula.ptg.DeletedRef3DPtg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.formula.ptg.Ref3DPtg;
import org.apache.poi.ss.formula.ptg.Ref3DPxg;
import org.apache.poi.ss.formula.ptg.RefErrorPtg;
import org.apache.poi.ss.formula.ptg.RefPtg;
import org.apache.poi.ss.formula.ptg.RefPtgBase;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


/**
 * Updates Formulas as rows or sheets are shifted
 */
public final class FormulaShifter {

    public static enum ShiftMode {
        RowMove,
        RowCopy,
        SheetMove,
    }

    /**
     * Extern sheet index of sheet where moving is occurring,
     *  used for updating HSSF style 3D references
     */
    private final int _externSheetIndex;
    /**
     * Sheet name of the sheet where moving is occurring, 
     *  used for updating XSSF style 3D references on row shifts.
     */
    private final String _sheetName;

    private final int _firstMovedIndex;
    private final int _lastMovedIndex;
    private final int _amountToMove;

    private final int _srcSheetIndex;
    private final int _dstSheetIndex;
    private final SpreadsheetVersion _version;

    private final ShiftMode _mode;

    private boolean _rowModeElseColumn; 

    
    /**
     * Create an instance for shifting row.
     *
     * For example, this will be called on {@link org.apache.poi.hssf.usermodel.HSSFSheet#shiftRows(int, int, int)} }
     */
    private FormulaShifter(int externSheetIndex, String sheetName, int firstMovedIndex, int lastMovedIndex, int amountToMove,
            ShiftMode mode, SpreadsheetVersion version) {
        if (amountToMove == 0) {
            throw new IllegalArgumentException("amountToMove must not be zero");
        }
        if (firstMovedIndex > lastMovedIndex) {
            throw new IllegalArgumentException("firstMovedIndex, lastMovedIndex out of order");
        }
        _externSheetIndex = externSheetIndex;
        _sheetName = sheetName;
        _firstMovedIndex = firstMovedIndex;
        _lastMovedIndex = lastMovedIndex;
        _amountToMove = amountToMove;
        _mode = mode;
        _version = version;

        _srcSheetIndex = _dstSheetIndex = -1;
        _rowModeElseColumn = true; // default
    }

    /**
     * Create an instance for shifting sheets.
     *
     * For example, this will be called on {@link org.apache.poi.hssf.usermodel.HSSFWorkbook#setSheetOrder(String, int)}  
     */
    private FormulaShifter(int srcSheetIndex, int dstSheetIndex) {
        _externSheetIndex = _firstMovedIndex = _lastMovedIndex = _amountToMove = -1;
        _sheetName = null;
        _version = null;

        _srcSheetIndex = srcSheetIndex;
        _dstSheetIndex = dstSheetIndex;
        _mode = ShiftMode.SheetMove;
        _rowModeElseColumn = true; // default
    }
    
    public static FormulaShifter createForItemShift(Sheet shiftingSheet, boolean _rowModeElseColumn, int firstShiftItemIndex, int lastShiftItemIndex, int shiftStep){
        FormulaShifter instance = new FormulaShifter(shiftingSheet.getWorkbook().getSheetIndex(shiftingSheet), shiftingSheet.getSheetName(), 
                firstShiftItemIndex, lastShiftItemIndex, shiftStep, ShiftMode.RowMove, getSpreadsheetVersion(shiftingSheet));
        instance._rowModeElseColumn = _rowModeElseColumn;
        return instance; 
    }
    // maybe should be deprecated, and previous one should be used
    public static FormulaShifter createForRowShift(int externSheetIndex, String sheetName, int firstMovedRowIndex, int lastMovedRowIndex, int numberOfRowsToMove,
            SpreadsheetVersion version) {
        FormulaShifter instance = new FormulaShifter(externSheetIndex, sheetName, firstMovedRowIndex, lastMovedRowIndex, numberOfRowsToMove, ShiftMode.RowMove, version);
        return instance;
    }
    
    public static FormulaShifter createForItemCopy(Sheet shiftingSheet, boolean rowModeElseColumn, int firstMovedItemIndex, int lastMovedItemIndex, int shiftStep){
        FormulaShifter instance = new FormulaShifter(shiftingSheet.getWorkbook().getSheetIndex(shiftingSheet), shiftingSheet.getSheetName(), 
                firstMovedItemIndex, lastMovedItemIndex, shiftStep, ShiftMode.RowCopy, getSpreadsheetVersion(shiftingSheet));
        instance._rowModeElseColumn = rowModeElseColumn;
        return instance; 
    }
    // maybe should be deprecated, and previous one should be used
    public static FormulaShifter createForRowCopy(int externSheetIndex, String sheetName, int firstMovedRowIndex, int lastMovedRowIndex, int numberOfRowsToMove,
            SpreadsheetVersion version) {
        FormulaShifter instance = new FormulaShifter(externSheetIndex, sheetName, firstMovedRowIndex, lastMovedRowIndex, numberOfRowsToMove, ShiftMode.RowCopy, version);
        return instance;
    }

    public static FormulaShifter createForSheetShift(int srcSheetIndex, int dstSheetIndex) {
        return new FormulaShifter(srcSheetIndex, dstSheetIndex);
    }

    @Override
    public String toString() {
        return getClass().getName() +
                " [" +
                _firstMovedIndex +
                _lastMovedIndex +
                _amountToMove +
                "]";
    }

    private Ptg adjustPtg(Ptg ptg, int currentExternSheetIx) {
        switch(_mode){
            case RowMove:
                return adjustPtgDueToRowMove(ptg, currentExternSheetIx);
            case RowCopy:
                // Covered Scenarios:
                // * row copy on same sheet
                // * row copy between different sheetsin the same workbook
                return adjustPtgDueToRowCopy(ptg);
            case SheetMove:
                return adjustPtgDueToSheetMove(ptg);
            default:
                throw new IllegalStateException("Unsupported shift mode: " + _mode);
        }
    }
    /**
     * @return in-place modified ptg (if row move would cause Ptg to change),
     * deleted ref ptg (if row move causes an error),
     * or null (if no Ptg change is needed)
     */
    private Ptg adjustPtgDueToRowMove(Ptg ptg, int currentExternSheetIx) {
        if(ptg instanceof RefPtg) {
            if (currentExternSheetIx != _externSheetIndex) {
                // local refs on other sheets are unaffected
                return null;
            }
            RefPtg rptg = (RefPtg)ptg;
            return rowMoveRefPtg(rptg);
        }
        if(ptg instanceof Ref3DPtg) {
            Ref3DPtg rptg = (Ref3DPtg)ptg;
            if (_externSheetIndex != rptg.getExternSheetIndex()) {
                // only move 3D refs that refer to the sheet with cells being moved
                // (currentExternSheetIx is irrelevant)
                return null;
            }
            return rowMoveRefPtg(rptg);
        }
        if(ptg instanceof Ref3DPxg) {
            Ref3DPxg rpxg = (Ref3DPxg)ptg;
            if (rpxg.getExternalWorkbookNumber() > 0 ||
                   ! _sheetName.equals(rpxg.getSheetName())) {
                // only move 3D refs that refer to the sheet with cells being moved
                return null;
            }
            return rowMoveRefPtg(rpxg);
        }
        if(ptg instanceof Area2DPtgBase) {
            if (currentExternSheetIx != _externSheetIndex) {
                // local refs on other sheets are unaffected
                return ptg;
            }
            return rowMoveAreaPtg((Area2DPtgBase)ptg);
        }
        if(ptg instanceof Area3DPtg) {
            Area3DPtg aptg = (Area3DPtg)ptg;
            if (_externSheetIndex != aptg.getExternSheetIndex()) {
                // only move 3D refs that refer to the sheet with cells being moved
                // (currentExternSheetIx is irrelevant)
                return null;
            }
            return rowMoveAreaPtg(aptg);
        }
        if(ptg instanceof Area3DPxg) {
            Area3DPxg apxg = (Area3DPxg)ptg;
            if (apxg.getExternalWorkbookNumber() > 0 ||
                    ! _sheetName.equals(apxg.getSheetName())) {
                // only move 3D refs that refer to the sheet with cells being moved
                return null;
            }
            return rowMoveAreaPtg(apxg);
        }
        return null;
    }
    
    /**
     * Call this on any ptg reference contained in a row of cells that was copied.
     * If the ptg reference is relative, the references will be shifted by the distance
     * that the rows were copied.
     * In the future similar functions could be written due to column copying or
     * individual cell copying. Just make sure to only call adjustPtgDueToRowCopy on
     * formula cells that are copied (unless row shifting, where references outside
     * of the shifted region need to be updated to reflect the shift, a copy is self-contained).
     * 
     * @param ptg the ptg to shift
     * @return deleted ref ptg, in-place modified ptg, or null
     * If Ptg would be shifted off the first or last row of a sheet, return deleted ref
     * If Ptg needs to be changed, modifies Ptg in-place
     * If Ptg doesn't need to be changed, returns <code>null</code>
     */
    private Ptg adjustPtgDueToRowCopy(Ptg ptg) {
        if(ptg instanceof RefPtg) {
            RefPtg rptg = (RefPtg)ptg;
            return rowCopyRefPtg(rptg);
        }
        if(ptg instanceof Ref3DPtg) {
            Ref3DPtg rptg = (Ref3DPtg)ptg;
            return rowCopyRefPtg(rptg);
        }
        if(ptg instanceof Ref3DPxg) {
            Ref3DPxg rpxg = (Ref3DPxg)ptg;
            return rowCopyRefPtg(rpxg);
        }
        if(ptg instanceof Area2DPtgBase) {
            return rowCopyAreaPtg((Area2DPtgBase)ptg);
        }
        if(ptg instanceof Area3DPtg) {
            Area3DPtg aptg = (Area3DPtg)ptg;
            return rowCopyAreaPtg(aptg);
        }
        if(ptg instanceof Area3DPxg) {
            Area3DPxg apxg = (Area3DPxg)ptg;
            return rowCopyAreaPtg(apxg);
        }
        return null;
    }


    private Ptg adjustPtgDueToSheetMove(Ptg ptg) {
        if(ptg instanceof Ref3DPtg) {
            Ref3DPtg ref = (Ref3DPtg)ptg;
            int oldSheetIndex = ref.getExternSheetIndex();
            
            // we have to handle a few cases here
            
            // 1. sheet is outside moved sheets, no change necessary
            if(oldSheetIndex < _srcSheetIndex &&
                    oldSheetIndex < _dstSheetIndex) {
                return null;
            }
            if(oldSheetIndex > _srcSheetIndex &&
                    oldSheetIndex > _dstSheetIndex) {
                return null;
            }
            
            // 2. ptg refers to the moved sheet
            if(oldSheetIndex == _srcSheetIndex) {
                ref.setExternSheetIndex(_dstSheetIndex);
                return ref;
            }

            // 3. new index is lower than old one => sheets get moved up
            if (_dstSheetIndex < _srcSheetIndex) {
                ref.setExternSheetIndex(oldSheetIndex+1);
                return ref;
            }

            // 4. new index is higher than old one => sheets get moved down
            if (_dstSheetIndex > _srcSheetIndex) {
                ref.setExternSheetIndex(oldSheetIndex-1);
                return ref;
            }
        }

        return null;
    }

    private Ptg rowMoveRefPtg(RefPtgBase rptg) {
        int refRow = rptg.getRow();
        if (_firstMovedIndex <= refRow && refRow <= _lastMovedIndex) {
            // Rows being moved completely enclose the ref.
            // - move the area ref along with the rows regardless of destination
            rptg.setRow(refRow + _amountToMove);
            return rptg;
        }
        // else rules for adjusting area may also depend on the destination of the moved rows

        int destFirstRowIndex = _firstMovedIndex + _amountToMove;
        int destLastRowIndex = _lastMovedIndex + _amountToMove;

        // ref is outside source rows
        // check for clashes with destination

        if (destLastRowIndex < refRow || refRow < destFirstRowIndex) {
            // destination rows are completely outside ref
            return null;
        }

        if (destFirstRowIndex <= refRow && refRow <= destLastRowIndex) {
            // destination rows enclose the area (possibly exactly)
            return createDeletedRef(rptg);
        }
        throw new IllegalStateException("Situation not covered: (" + _firstMovedIndex + ", " +
                    _lastMovedIndex + ", " + _amountToMove + ", " + refRow + ", " + refRow + ")");
    }

    private Ptg rowMoveAreaPtg(AreaPtgBase aptg) {
        int aFirstRow = aptg.getFirstRow();
        int aLastRow = aptg.getLastRow();
        if (_firstMovedIndex <= aFirstRow && aLastRow <= _lastMovedIndex) {
            // Rows being moved completely enclose the area ref.
            // - move the area ref along with the rows regardless of destination
            aptg.setFirstRow(aFirstRow + _amountToMove);
            aptg.setLastRow(aLastRow + _amountToMove);
            return aptg;
        }
        // else rules for adjusting area may also depend on the destination of the moved rows

        int destFirstRowIndex = _firstMovedIndex + _amountToMove;
        int destLastRowIndex = _lastMovedIndex + _amountToMove;

        if (aFirstRow < _firstMovedIndex && _lastMovedIndex < aLastRow) {
            // Rows moved were originally *completely* within the area ref

            // If the destination of the rows overlaps either the top
            // or bottom of the area ref there will be a change
            if (destFirstRowIndex < aFirstRow && aFirstRow <= destLastRowIndex) {
                // truncate the top of the area by the moved rows
                aptg.setFirstRow(destLastRowIndex+1);
                return aptg;
            } else if (destFirstRowIndex <= aLastRow && aLastRow < destLastRowIndex) {
                // truncate the bottom of the area by the moved rows
                aptg.setLastRow(destFirstRowIndex-1);
                return aptg;
            }
            // else - rows have moved completely outside the area ref,
            // or still remain completely within the area ref
            return null; // - no change to the area
        }
        if (_firstMovedIndex <= aFirstRow && aFirstRow <= _lastMovedIndex) {
            // Rows moved include the first row of the area ref, but not the last row
            // btw: (aLastRow > _lastMovedIndex)
            if (_amountToMove < 0) {
                // simple case - expand area by shifting top upward
                aptg.setFirstRow(aFirstRow + _amountToMove);
                return aptg;
            }
            if (destFirstRowIndex > aLastRow) {
                // in this case, excel ignores the row move
                return null;
            }
            int newFirstRowIx = aFirstRow + _amountToMove;
            if (destLastRowIndex < aLastRow) {
                // end of area is preserved (will remain exact same row)
                // the top area row is moved simply
                aptg.setFirstRow(newFirstRowIx);
                return aptg;
            }
            // else - bottom area row has been replaced - both area top and bottom may move now
            int areaRemainingTopRowIx = _lastMovedIndex + 1;
            if (destFirstRowIndex > areaRemainingTopRowIx) {
                // old top row of area has moved deep within the area, and exposed a new top row
                newFirstRowIx = areaRemainingTopRowIx;
            }
            aptg.setFirstRow(newFirstRowIx);
            aptg.setLastRow(Math.max(aLastRow, destLastRowIndex));
            return aptg;
        }
        if (_firstMovedIndex <= aLastRow && aLastRow <= _lastMovedIndex) {
            // Rows moved include the last row of the area ref, but not the first
            // btw: (aFirstRow < _firstMovedIndex)
            if (_amountToMove > 0) {
                // simple case - expand area by shifting bottom downward
                aptg.setLastRow(aLastRow + _amountToMove);
                return aptg;
            }
            if (destLastRowIndex < aFirstRow) {
                // in this case, excel ignores the row move
                return null;
            }
            int newLastRowIx = aLastRow + _amountToMove;
            if (destFirstRowIndex > aFirstRow) {
                // top of area is preserved (will remain exact same row)
                // the bottom area row is moved simply
                aptg.setLastRow(newLastRowIx);
                return aptg;
            }
            // else - top area row has been replaced - both area top and bottom may move now
            int areaRemainingBottomRowIx = _firstMovedIndex - 1;
            if (destLastRowIndex < areaRemainingBottomRowIx) {
                // old bottom row of area has moved up deep within the area, and exposed a new bottom row
                newLastRowIx = areaRemainingBottomRowIx;
            }
            aptg.setFirstRow(Math.min(aFirstRow, destFirstRowIndex));
            aptg.setLastRow(newLastRowIx);
            return aptg;
        }
        // else source rows include none of the rows of the area ref
        // check for clashes with destination

        if (destLastRowIndex < aFirstRow || aLastRow < destFirstRowIndex) {
            // destination rows are completely outside area ref
            return null;
        }

        if (destFirstRowIndex <= aFirstRow && aLastRow <= destLastRowIndex) {
            // destination rows enclose the area (possibly exactly)
            return createDeletedRef(aptg);
        }

        if (aFirstRow <= destFirstRowIndex && destLastRowIndex <= aLastRow) {
            // destination rows are within area ref (possibly exact on top or bottom, but not both)
            return null; // - no change to area
        }

        if (destFirstRowIndex < aFirstRow && aFirstRow <= destLastRowIndex) {
            // dest rows overlap top of area
            // - truncate the top
            aptg.setFirstRow(destLastRowIndex+1);
            return aptg;
        }
        if (destFirstRowIndex <= aLastRow && aLastRow < destLastRowIndex) {
            // dest rows overlap bottom of area
            // - truncate the bottom
            aptg.setLastRow(destFirstRowIndex-1);
            return aptg;
        }
        throw new IllegalStateException("Situation not covered: (" + _firstMovedIndex + ", " +
                    _lastMovedIndex + ", " + _amountToMove + ", " + aFirstRow + ", " + aLastRow + ")");
    }
    
    /**
     * Modifies rptg in-place and return a reference to rptg if the cell reference
     * would move due to a row copy operation
     * Returns <code>null</code> or {@link RefErrorPtg} if no change was made
     *
     * @param rptg The REF that is copied
     * @return The Ptg reference if the cell would move due to copy, otherwise null
     */
    private Ptg rowCopyRefPtg(RefPtgBase rptg) {
        final int refRow = rptg.getRow();
        if (rptg.isRowRelative()) {
            // check new location where the ref is located
            final int destRowIndex = _firstMovedIndex + _amountToMove;
            if (destRowIndex < 0 || _version.getLastRowIndex() < destRowIndex) {
                return createDeletedRef(rptg);
            }

            // check new location where the ref points to
            final int newRowIndex = refRow + _amountToMove;
            if(newRowIndex < 0 || _version.getLastRowIndex() < newRowIndex) {
                return createDeletedRef(rptg);
            }

            rptg.setRow(newRowIndex);
            return rptg;
        }
        return null;
    }

    /**
     * Modifies aptg in-place and return a reference to aptg if the first or last row of
     * of the Area reference would move due to a row copy operation
     * Returns <code>null</code> or {@link AreaErrPtg} if no change was made
     *
     * @param aptg The Area that is copied
     * @return null, AreaErrPtg, or modified aptg
     */
    private Ptg rowCopyAreaPtg(AreaPtgBase aptg) {
        boolean changed = false;
    
        final int aFirstRow = aptg.getFirstRow();
        final int aLastRow = aptg.getLastRow();
    
        if (aptg.isFirstRowRelative()) {
            final int destFirstRowIndex = aFirstRow + _amountToMove;
            if (destFirstRowIndex < 0 || _version.getLastRowIndex() < destFirstRowIndex)
                return createDeletedRef(aptg);
            aptg.setFirstRow(destFirstRowIndex);
            changed = true;
        }
        if (aptg.isLastRowRelative()) {
            final int destLastRowIndex = aLastRow + _amountToMove;
            if (destLastRowIndex < 0 || _version.getLastRowIndex() < destLastRowIndex)
                return createDeletedRef(aptg);
            aptg.setLastRow(destLastRowIndex);
            changed = true;
        }
        if (changed) {
            aptg.sortTopLeftToBottomRight();
        }

        return changed ? aptg : null;
    }

    private static Ptg createDeletedRef(Ptg ptg) {
        if (ptg instanceof RefPtg) {
            return new RefErrorPtg();
        }
        if (ptg instanceof Ref3DPtg) {
            Ref3DPtg rptg = (Ref3DPtg) ptg;
            return new DeletedRef3DPtg(rptg.getExternSheetIndex());
        }
        if (ptg instanceof AreaPtg) {
            return new AreaErrPtg();
        }
        if (ptg instanceof Area3DPtg) {
            Area3DPtg area3DPtg = (Area3DPtg) ptg;
            return new DeletedArea3DPtg(area3DPtg.getExternSheetIndex());
        }
        if (ptg instanceof Ref3DPxg) {
            Ref3DPxg pxg = (Ref3DPxg)ptg;
            return new Deleted3DPxg(pxg.getExternalWorkbookNumber(), pxg.getSheetName());
        }
        if (ptg instanceof Area3DPxg) {
            Area3DPxg pxg = (Area3DPxg)ptg;
            return new Deleted3DPxg(pxg.getExternalWorkbookNumber(), pxg.getSheetName());
        }

        throw new IllegalArgumentException("Unexpected ref ptg class (" + ptg.getClass().getName() + ")");
    }
    
    
    // ******** logic which processes columns in same way as row ********
    

    /**
     * @param ptgs - if necessary, will get modified by this method
     * @param currentExternSheetIx - the extern sheet index of the sheet that contains the formula being adjusted
     * @return <code>true</code> if a change was made to the formula tokens
     */
    public boolean adjustFormula(Ptg[] ptgs, int currentExternSheetIx) {
        boolean refsWereChanged = false;
        for(int i=0; i<ptgs.length; i++) {
            Ptg newPtg;
            if(_rowModeElseColumn){
                newPtg = adjustPtg(ptgs[i], currentExternSheetIx);
                if (newPtg != null) {
                    refsWereChanged = true;
                       ptgs[i] = newPtg;
                }
            }
            else {
                Ptg transposedPtg = transpose(ptgs[i]);
                newPtg = adjustPtg(transposedPtg, currentExternSheetIx);
                if (newPtg != null) {
                    refsWereChanged = true;
                       ptgs[i] = transpose(transposedPtg);
                }
            }
        }
        return refsWereChanged;
    }


    private Ptg transpose(Ptg ptg){
            String ptgType = ptg.getClass().getSimpleName();  
            if(ptgType.equals("Ref3DPtg")){  //3D means (sheetNo, col, row) reference, for example Sheet1!B3; xls version  
                int oldColumnIndex = ((Ref3DPtg) ptg).getColumn();
                ((Ref3DPtg) ptg).setColumn(((Ref3DPtg) ptg).getRow());
                ((Ref3DPtg) ptg).setRow(oldColumnIndex);
                return ptg;
            } else if(ptgType.equals("Ref3DPxg")){  //3D means (sheetNo, col, row) reference, for example Sheet1!B3; xlsx version  
                int oldColumnIndex = ((Ref3DPxg) ptg).getColumn();
                ((Ref3DPxg) ptg).setColumn(((Ref3DPxg) ptg).getRow());
                ((Ref3DPxg) ptg).setRow(oldColumnIndex);
                return ptg;
            } else if(ptgType.equals("AreaPtg")){  // region for aggregate function, for example A1:B3 or Sheet1!B3:Sheet1!C3
                    int oldFirstColumnIndex = ((AreaPtg) ptg).getFirstColumn();
                    ((AreaPtg) ptg).setFirstColumn(((AreaPtg) ptg).getFirstRow());
                    ((AreaPtg) ptg).setFirstRow(oldFirstColumnIndex);
                    int oldLastColumnIndex = ((AreaPtg) ptg).getLastColumn();
                    ((AreaPtg) ptg).setLastColumn(((AreaPtg) ptg).getLastRow());
                    ((AreaPtg) ptg).setLastRow(oldLastColumnIndex);
                    return ptg;
            }
             else if(ptgType.equals("Area3DPtg")){ //for example SUM(Sheet1!B3:C3); xls version 
                    int oldFirstColumnIndex = ((Area3DPtg) ptg).getFirstColumn();
                    ((Area3DPtg) ptg).setFirstColumn(((Area3DPtg) ptg).getFirstRow());
                    ((Area3DPtg) ptg).setFirstRow(oldFirstColumnIndex);
                    int oldLastColumnIndex = ((Area3DPtg) ptg).getLastColumn();
                    ((Area3DPtg) ptg).setLastColumn(((Area3DPtg) ptg).getLastRow());
                    ((Area3DPtg) ptg).setLastRow(oldLastColumnIndex);
                    return ptg;
             }
             else if(ptgType.equals("Area3DPxg")){ //for example SUM(Sheet1!B3:C3); xlsx version 
                     int oldFirstColumnIndex = ((Area3DPxg) ptg).getFirstColumn();
                    ((Area3DPxg) ptg).setFirstColumn(((Area3DPxg) ptg).getFirstRow());
                    ((Area3DPxg) ptg).setFirstRow(oldFirstColumnIndex);
                    int oldLastColumnIndex = ((Area3DPxg) ptg).getLastColumn();
                    ((Area3DPxg) ptg).setLastColumn(((Area3DPxg) ptg).getLastRow());
                    ((Area3DPxg) ptg).setLastRow(oldLastColumnIndex);
                    return ptg;
             }    else if(ptgType.equals("RefPtg")){  // common simple reference, like A2 
                    RefPtg transponedCellRefToken = new RefPtg(transpose(ptg.toFormulaString()));
                    return transponedCellRefToken;
             }
             else // operators like + or SUM, for example
                 return ptg;
    }

    public static String transpose(String cellreference){
        CellReference original = new CellReference(cellreference);
        // transpose, calling public CellReference(int *pRow*, int *pCol*) !!!!
        CellReference transposed = new CellReference(original.getCol(), original.getRow(), original.isColAbsolute(), original.isRowAbsolute());  
        return transposed.formatAsString();
    }
    
    private int getSheetIndex(Sheet sheet){
        return sheet.getWorkbook().getSheetIndex(sheet);
    }

    public static SpreadsheetVersion getSpreadsheetVersion(Sheet sheet){
        if(sheet.getWorkbook() instanceof XSSFWorkbook)
            return SpreadsheetVersion.EXCEL2007;
        else if(sheet.getWorkbook() instanceof HSSFWorkbook)
            return SpreadsheetVersion.EXCEL97;
        else return null;
    }
    
    
    
}
