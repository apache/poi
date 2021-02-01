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


/**
 * Updates Formulas as rows or sheets are shifted
 */
public final class FormulaShifter {

    private enum ShiftMode {
        RowMove,
        RowCopy,
        /** @since POI 4.0.0 */
        ColumnMove,
        /** @since POI 4.0.0 */
        ColumnCopy,
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
    }
    
    public static FormulaShifter createForRowShift(int externSheetIndex, String sheetName, int firstMovedRowIndex, int lastMovedRowIndex, int numberOfRowsToMove,
            SpreadsheetVersion version) {
        return new FormulaShifter(externSheetIndex, sheetName, firstMovedRowIndex, lastMovedRowIndex, numberOfRowsToMove, ShiftMode.RowMove, version);
    }
    
    public static FormulaShifter createForRowCopy(int externSheetIndex, String sheetName, int firstMovedRowIndex, int lastMovedRowIndex, int numberOfRowsToMove,
            SpreadsheetVersion version) {
        return new FormulaShifter(externSheetIndex, sheetName, firstMovedRowIndex, lastMovedRowIndex, numberOfRowsToMove, ShiftMode.RowCopy, version);
    }

    /**
     * @since POI 4.0.0
     */
    public static FormulaShifter createForColumnShift(int externSheetIndex, String sheetName, int firstMovedColumnIndex, int lastMovedColumnIndex, int numberOfColumnsToMove,
            SpreadsheetVersion version) {
        return new FormulaShifter(externSheetIndex, sheetName, firstMovedColumnIndex, lastMovedColumnIndex, numberOfColumnsToMove, ShiftMode.ColumnMove, version);
    }
    
    /**
     * @since POI 4.0.0
     */
    public static FormulaShifter createForColumnCopy(int externSheetIndex, String sheetName, int firstMovedColumnIndex, int lastMovedColumnIndex, int numberOfColumnsToMove,
                                                      SpreadsheetVersion version) {
        return new FormulaShifter(externSheetIndex, sheetName, firstMovedColumnIndex, lastMovedColumnIndex, numberOfColumnsToMove, ShiftMode.ColumnCopy, version);
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

    /**
     * @param ptgs - if necessary, will get modified by this method
     * @param currentExternSheetIx - the extern sheet index of the sheet that contains the formula being adjusted
     * @return <code>true</code> if a change was made to the formula tokens
     */
    public boolean adjustFormula(Ptg[] ptgs, int currentExternSheetIx) {
        boolean refsWereChanged = false;
        for(int i=0; i<ptgs.length; i++) {
            Ptg newPtg = adjustPtg(ptgs[i], currentExternSheetIx);
            if (newPtg != null) {
                refsWereChanged = true;
                ptgs[i] = newPtg;
            }
        }
        return refsWereChanged;
    }

    private Ptg adjustPtg(Ptg ptg, int currentExternSheetIx) {
        switch(_mode){
            case RowMove:
                return adjustPtgDueToRowMove(ptg, currentExternSheetIx);
            case RowCopy:
                // Covered Scenarios:
                // * row copy on same sheet
                // * row copy between different sheets in the same workbook
                return adjustPtgDueToRowCopy(ptg);
            case ColumnMove:
                return adjustPtgDueToColumnMove(ptg, currentExternSheetIx);
            case ColumnCopy:
                return adjustPtgDueToColumnCopy(ptg);
            case SheetMove:
                return adjustPtgDueToSheetMove(ptg);
            default:
                throw new IllegalStateException("Unsupported shift mode: " + _mode);
        }
    }

    /**
     * @return in-place modified ptg (if column move would cause Ptg to change),
     * deleted ref ptg (if column move causes an error),
     * or null (if no Ptg change is needed)
     */
    private Ptg adjustPtgDueToMove(Ptg ptg, int currentExternSheetIx, boolean isRowMove) {
        if(ptg instanceof RefPtg) {
            if (currentExternSheetIx != _externSheetIndex) {
                // local refs on other sheets are unaffected
                return null;
            }
            RefPtg rptg = (RefPtg)ptg;
            return isRowMove ? rowMoveRefPtg(rptg) : columnMoveRefPtg(rptg);
        }
        if(ptg instanceof Ref3DPtg) {
            Ref3DPtg rptg = (Ref3DPtg)ptg;
            if (_externSheetIndex != rptg.getExternSheetIndex()) {
                // only move 3D refs that refer to the sheet with cells being moved
                // (currentExternSheetIx is irrelevant)
                return null;
            }
            return isRowMove ? rowMoveRefPtg(rptg) : columnMoveRefPtg(rptg);
        }
        if(ptg instanceof Ref3DPxg) {
            Ref3DPxg rpxg = (Ref3DPxg)ptg;
            if (rpxg.getExternalWorkbookNumber() > 0 ||
                    ! _sheetName.equalsIgnoreCase(rpxg.getSheetName())) {
                // only move 3D refs that refer to the sheet with cells being moved
                return null;
            }
            return isRowMove ? rowMoveRefPtg(rpxg) : columnMoveRefPtg(rpxg);
        }
        if(ptg instanceof Area2DPtgBase) {
            if (currentExternSheetIx != _externSheetIndex) {
                // local refs on other sheets are unaffected
                return ptg;
            }
            Area2DPtgBase aptg = (Area2DPtgBase) ptg;
            return isRowMove ? rowMoveAreaPtg(aptg) : columnMoveAreaPtg(aptg);
        }
        if(ptg instanceof Area3DPtg) {
            Area3DPtg aptg = (Area3DPtg)ptg;
            if (_externSheetIndex != aptg.getExternSheetIndex()) {
                // only move 3D refs that refer to the sheet with cells being moved
                // (currentExternSheetIx is irrelevant)
                return null;
            }
            return isRowMove ? rowMoveAreaPtg(aptg) : columnMoveAreaPtg(aptg);
        }
        if(ptg instanceof Area3DPxg) {
            Area3DPxg apxg = (Area3DPxg)ptg;
            if (apxg.getExternalWorkbookNumber() > 0 ||
                    ! _sheetName.equalsIgnoreCase(apxg.getSheetName())) {
                // only move 3D refs that refer to the sheet with cells being moved
                return null;
            }
            return isRowMove ? rowMoveAreaPtg(apxg) : columnMoveAreaPtg(apxg);
        }
        return null;
    }

    /**
     * @return in-place modified ptg (if row move would cause Ptg to change),
     * deleted ref ptg (if row move causes an error),
     * or null (if no Ptg change is needed)
     */
    private Ptg adjustPtgDueToRowMove(Ptg ptg, int currentExternSheetIx) {
        return adjustPtgDueToMove(ptg, currentExternSheetIx, true);
    }

    /**
     * @return in-place modified ptg (if column move would cause Ptg to change),
     * deleted ref ptg (if column move causes an error),
     * or null (if no Ptg change is needed)
     */
    private Ptg adjustPtgDueToColumnMove(Ptg ptg, int currentExternSheetIx) {
        return adjustPtgDueToMove(ptg, currentExternSheetIx, false);
    }

    /**
     * Call this on any ptg reference contained in a row or column of cells that was copied.
     * If the ptg reference is relative, the references will be shifted by the distance
     * that the rows or columns were copied.
     *
     * @param ptg the ptg to shift
     * @return deleted ref ptg, in-place modified ptg, or null
     * If Ptg would be shifted off the first or last row or columns of a sheet, return deleted ref
     * If Ptg needs to be changed, modifies Ptg in-place
     * If Ptg doesn't need to be changed, returns <code>null</code>
     */
    private Ptg adjustPtgDueToCopy(Ptg ptg, boolean isRowCopy) {
        if(ptg instanceof RefPtg) {
            RefPtg rptg = (RefPtg)ptg;
            return isRowCopy ? rowCopyRefPtg(rptg) : columnCopyRefPtg(rptg);
        }
        if(ptg instanceof Ref3DPtg) {
            Ref3DPtg rptg = (Ref3DPtg)ptg;
            return isRowCopy ? rowCopyRefPtg(rptg) : columnCopyRefPtg(rptg);
        }
        if(ptg instanceof Ref3DPxg) {
            Ref3DPxg rpxg = (Ref3DPxg)ptg;
            return isRowCopy ? rowCopyRefPtg(rpxg) : columnCopyRefPtg(rpxg);
        }
        if(ptg instanceof Area2DPtgBase) {
            Area2DPtgBase aptg = (Area2DPtgBase) ptg;
            return isRowCopy ? rowCopyAreaPtg(aptg) : columnCopyAreaPtg(aptg);
        }
        if(ptg instanceof Area3DPtg) {
            Area3DPtg aptg = (Area3DPtg)ptg;
            return isRowCopy ? rowCopyAreaPtg(aptg) : columnCopyAreaPtg(aptg);
        }
        if(ptg instanceof Area3DPxg) {
            Area3DPxg apxg = (Area3DPxg)ptg;
            return isRowCopy ? rowCopyAreaPtg(apxg) : columnCopyAreaPtg(apxg);
        }
        return null;
    }

    /**
     * Call this on any ptg reference contained in a row of cells that was copied.
     * If the ptg reference is relative, the references will be shifted by the distance
     * that the rows were copied.
     *
     * @param ptg the ptg to shift
     * @return deleted ref ptg, in-place modified ptg, or null
     * If Ptg would be shifted off the first or last row of a sheet, return deleted ref
     * If Ptg needs to be changed, modifies Ptg in-place
     * If Ptg doesn't need to be changed, returns <code>null</code>
     */
    private Ptg adjustPtgDueToRowCopy(Ptg ptg) {
        return adjustPtgDueToCopy(ptg, true);
    }

    /**
     * Call this on any ptg reference contained in a column of cells that was copied.
     * If the ptg reference is relative, the references will be shifted by the distance
     * that the columns were copied.
     *
     * @param ptg the ptg to shift
     * @return deleted ref ptg, in-place modified ptg, or null
     * If Ptg would be shifted off the first or last column of a sheet, return deleted ref
     * If Ptg needs to be changed, modifies Ptg in-place
     * If Ptg doesn't need to be changed, returns <code>null</code>
     */
    private Ptg adjustPtgDueToColumnCopy(Ptg ptg) {
        return adjustPtgDueToCopy(ptg, false);
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


    private Ptg columnMoveRefPtg(RefPtgBase rptg) {
        int refColumn = rptg.getColumn();
        if (_firstMovedIndex <= refColumn && refColumn <= _lastMovedIndex) {
            // Columns being moved completely enclose the ref.
            // - move the area ref along with the columns regardless of destination
            rptg.setColumn(refColumn + _amountToMove);
            return rptg;
        }
        // else rules for adjusting area may also depend on the destination of the moved columns

        int destFirstColumnIndex = _firstMovedIndex + _amountToMove;
        int destLastColumnIndex = _lastMovedIndex + _amountToMove;

        // ref is outside source columns
        // check for clashes with destination

        if (destLastColumnIndex < refColumn || refColumn < destFirstColumnIndex) {
            // destination columns are completely outside ref
            return null;
        }

        if (destFirstColumnIndex <= refColumn && refColumn <= destLastColumnIndex) {
            // destination columns enclose the area (possibly exactly)
            return createDeletedRef(rptg);
        }
        throw new IllegalStateException("Situation not covered: (" + _firstMovedIndex + ", " +
                _lastMovedIndex + ", " + _amountToMove + ", " + refColumn + ", " + refColumn + ")");
    }

    private Ptg columnMoveAreaPtg(AreaPtgBase aptg) {
        int aFirstColumn = aptg.getFirstColumn();
        int aLastColumn = aptg.getLastColumn();
        if (_firstMovedIndex <= aFirstColumn && aLastColumn <= _lastMovedIndex) {
            // Columns being moved completely enclose the area ref.
            // - move the area ref along with the columns regardless of destination
            aptg.setFirstColumn(aFirstColumn + _amountToMove);
            aptg.setLastColumn(aLastColumn + _amountToMove);
            return aptg;
        }
        // else rules for adjusting area may also depend on the destination of the moved columns

        int destFirstColumnIndex = _firstMovedIndex + _amountToMove;
        int destLastColumnIndex = _lastMovedIndex + _amountToMove;

        if (aFirstColumn < _firstMovedIndex && _lastMovedIndex < aLastColumn) {
            // Columns moved were originally *completely* within the area ref

            // If the destination of the columns overlaps either the top
            // or bottom of the area ref there will be a change
            if (destFirstColumnIndex < aFirstColumn && aFirstColumn <= destLastColumnIndex) {
                // truncate the top of the area by the moved columns
                aptg.setFirstColumn(destLastColumnIndex+1);
                return aptg;
            } else if (destFirstColumnIndex <= aLastColumn && aLastColumn < destLastColumnIndex) {
                // truncate the bottom of the area by the moved columns
                aptg.setLastColumn(destFirstColumnIndex-1);
                return aptg;
            }
            // else - columns have moved completely outside the area ref,
            // or still remain completely within the area ref
            return null; // - no change to the area
        }
        if (_firstMovedIndex <= aFirstColumn && aFirstColumn <= _lastMovedIndex) {
            // Columns moved include the first column of the area ref, but not the last column
            // btw: (aLastColumn > _lastMovedIndex)
            if (_amountToMove < 0) {
                // simple case - expand area by shifting top upward
                aptg.setFirstColumn(aFirstColumn + _amountToMove);
                return aptg;
            }
            if (destFirstColumnIndex > aLastColumn) {
                // in this case, excel ignores the column move
                return null;
            }
            int newFirstColumnIx = aFirstColumn + _amountToMove;
            if (destLastColumnIndex < aLastColumn) {
                // end of area is preserved (will remain exact same column)
                // the top area column is moved simply
                aptg.setFirstColumn(newFirstColumnIx);
                return aptg;
            }
            // else - bottom area column has been replaced - both area top and bottom may move now
            int areaRemainingTopColumnIx = _lastMovedIndex + 1;
            if (destFirstColumnIndex > areaRemainingTopColumnIx) {
                // old top column of area has moved deep within the area, and exposed a new top column
                newFirstColumnIx = areaRemainingTopColumnIx;
            }
            aptg.setFirstColumn(newFirstColumnIx);
            aptg.setLastColumn(Math.max(aLastColumn, destLastColumnIndex));
            return aptg;
        }
        if (_firstMovedIndex <= aLastColumn && aLastColumn <= _lastMovedIndex) {
            // Columns moved include the last column of the area ref, but not the first
            // btw: (aFirstColumn < _firstMovedIndex)
            if (_amountToMove > 0) {
                // simple case - expand area by shifting bottom downward
                aptg.setLastColumn(aLastColumn + _amountToMove);
                return aptg;
            }
            if (destLastColumnIndex < aFirstColumn) {
                // in this case, excel ignores the column move
                return null;
            }
            int newLastColumnIx = aLastColumn + _amountToMove;
            if (destFirstColumnIndex > aFirstColumn) {
                // top of area is preserved (will remain exact same column)
                // the bottom area column is moved simply
                aptg.setLastColumn(newLastColumnIx);
                return aptg;
            }
            // else - top area column has been replaced - both area top and bottom may move now
            int areaRemainingBottomColumnIx = _firstMovedIndex - 1;
            if (destLastColumnIndex < areaRemainingBottomColumnIx) {
                // old bottom column of area has moved up deep within the area, and exposed a new bottom column
                newLastColumnIx = areaRemainingBottomColumnIx;
            }
            aptg.setFirstColumn(Math.min(aFirstColumn, destFirstColumnIndex));
            aptg.setLastColumn(newLastColumnIx);
            return aptg;
        }
        // else source columns include none of the columns of the area ref
        // check for clashes with destination

        if (destLastColumnIndex < aFirstColumn || aLastColumn < destFirstColumnIndex) {
            // destination columns are completely outside area ref
            return null;
        }

        if (destFirstColumnIndex <= aFirstColumn && aLastColumn <= destLastColumnIndex) {
            // destination columns enclose the area (possibly exactly)
            return createDeletedRef(aptg);
        }

        if (aFirstColumn <= destFirstColumnIndex && destLastColumnIndex <= aLastColumn) {
            // destination columns are within area ref (possibly exact on top or bottom, but not both)
            return null; // - no change to area
        }

        if (destFirstColumnIndex < aFirstColumn && aFirstColumn <= destLastColumnIndex) {
            // dest columns overlap top of area
            // - truncate the top
            aptg.setFirstColumn(destLastColumnIndex+1);
            return aptg;
        }
        if (destFirstColumnIndex <= aLastColumn && aLastColumn < destLastColumnIndex) {
            // dest columns overlap bottom of area
            // - truncate the bottom
            aptg.setLastColumn(destFirstColumnIndex-1);
            return aptg;
        }
        throw new IllegalStateException("Situation not covered: (" + _firstMovedIndex + ", " +
                _lastMovedIndex + ", " + _amountToMove + ", " + aFirstColumn + ", " + aLastColumn + ")");
    }

    /**
     * Modifies rptg in-place and return a reference to rptg if the cell reference
     * would move due to a column copy operation
     * Returns <code>null</code> or {@link RefErrorPtg} if no change was made
     *
     * @param rptg The REF that is copied
     * @return The Ptg reference if the cell would move due to copy, otherwise null
     */
    private Ptg columnCopyRefPtg(RefPtgBase rptg) {
        final int refColumn = rptg.getColumn();
        if (rptg.isColRelative()) {
            // check new location where the ref is located
            final int destColumnIndex = _firstMovedIndex + _amountToMove;
            if (destColumnIndex < 0 || _version.getLastColumnIndex() < destColumnIndex) {
                return createDeletedRef(rptg);
            }

            // check new location where the ref points to
            final int newColumnIndex = refColumn + _amountToMove;
            if(newColumnIndex < 0 || _version.getLastColumnIndex() < newColumnIndex) {
                return createDeletedRef(rptg);
            }

            rptg.setColumn(newColumnIndex);
            return rptg;
        }
        return null;
    }

    /**
     * Modifies aptg in-place and return a reference to aptg if the first or last column of
     * of the Area reference would move due to a column copy operation
     * Returns <code>null</code> or {@link AreaErrPtg} if no change was made
     *
     * @param aptg The Area that is copied
     * @return null, AreaErrPtg, or modified aptg
     */
    private Ptg columnCopyAreaPtg(AreaPtgBase aptg) {
        boolean changed = false;

        final int aFirstColumn = aptg.getFirstColumn();
        final int aLastColumn = aptg.getLastColumn();

        if (aptg.isFirstColRelative()) {
            final int destFirstColumnIndex = aFirstColumn + _amountToMove;
            if (destFirstColumnIndex < 0 || _version.getLastColumnIndex() < destFirstColumnIndex)
                return createDeletedRef(aptg);
            aptg.setFirstColumn(destFirstColumnIndex);
            changed = true;
        }
        if (aptg.isLastColRelative()) {
            final int destLastColumnIndex = aLastColumn + _amountToMove;
            if (destLastColumnIndex < 0 || _version.getLastColumnIndex() < destLastColumnIndex)
                return createDeletedRef(aptg);
            aptg.setLastColumn(destLastColumnIndex);
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
}