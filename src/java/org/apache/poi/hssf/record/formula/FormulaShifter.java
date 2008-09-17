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

package org.apache.poi.hssf.record.formula;


/**
 * @author Josh Micich
 */
public final class FormulaShifter {

	/**
	 * Extern sheet index of sheet where moving is occurring
	 */
	private final int _externSheetIndex;
	private final int _firstMovedIndex;
	private final int _lastMovedIndex;
	private final int _amountToMove;

	private FormulaShifter(int externSheetIndex, int firstMovedIndex, int lastMovedIndex, int amountToMove) {
		if (amountToMove == 0) {
			throw new IllegalArgumentException("amountToMove must not be zero");
		}
		if (firstMovedIndex > lastMovedIndex) {
			throw new IllegalArgumentException("firstMovedIndex, lastMovedIndex out of order");
		}
		_externSheetIndex = externSheetIndex;
		_firstMovedIndex = firstMovedIndex;
		_lastMovedIndex = lastMovedIndex;
		_amountToMove = amountToMove;
	}

	public static FormulaShifter createForRowShift(int externSheetIndex, int firstMovedRowIndex, int lastMovedRowIndex, int numberOfRowsToMove) {
		return new FormulaShifter(externSheetIndex, firstMovedRowIndex, lastMovedRowIndex, numberOfRowsToMove);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append(getClass().getName());
		sb.append(" [");
		sb.append(_firstMovedIndex);
		sb.append(_lastMovedIndex);
		sb.append(_amountToMove);
		return sb.toString();
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
		return adjustPtgDueToRowMove(ptg, currentExternSheetIx);
	}
	/**
	 * @return <code>true</code> if this Ptg needed to be changed
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
		if (destFirstRowIndex < aLastRow && aLastRow <= destLastRowIndex) {
			// dest rows overlap bottom of area
			// - truncate the bottom
			aptg.setLastRow(destFirstRowIndex-1);
			return aptg;
		}
		throw new IllegalStateException("Situation not covered: (" + _firstMovedIndex + ", " +
					_lastMovedIndex + ", " + _amountToMove + ", " + aFirstRow + ", " + aLastRow + ")");
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

		throw new IllegalArgumentException("Unexpected ref ptg class (" + ptg.getClass().getName() + ")");
	}
}
