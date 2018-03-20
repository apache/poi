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

package org.apache.poi.hssf.record.aggregates;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.poi.hssf.model.RecordStream;
import org.apache.poi.hssf.record.ColumnInfoRecord;

/**
 * @author Glen Stampoultzis
 */
public final class ColumnInfoRecordsAggregate extends RecordAggregate implements Cloneable {
	/**
	 * List of {@link ColumnInfoRecord}s assumed to be in order
	 */
	private final List<ColumnInfoRecord> records;


	private static final class CIRComparator implements Comparator<ColumnInfoRecord> {
		public static final Comparator<ColumnInfoRecord> instance = new CIRComparator();
		private CIRComparator() {
			// enforce singleton
		}
		public int compare(ColumnInfoRecord a, ColumnInfoRecord b) {
			return compareColInfos(a, b);
		}
		public static int compareColInfos(ColumnInfoRecord a, ColumnInfoRecord b) {
			return a.getFirstColumn()-b.getFirstColumn();
		}
	}

	/**
	 * Creates an empty aggregate
	 */
	public ColumnInfoRecordsAggregate() {
		records = new ArrayList<>();
	}
	public ColumnInfoRecordsAggregate(RecordStream rs) {
		this();

		boolean isInOrder = true;
		ColumnInfoRecord cirPrev = null;
		while(rs.peekNextClass() == ColumnInfoRecord.class) {
			ColumnInfoRecord cir = (ColumnInfoRecord) rs.getNext();
			records.add(cir);
			if (cirPrev != null && CIRComparator.compareColInfos(cirPrev, cir) > 0) {
				isInOrder = false;
			}
			cirPrev = cir;
		}
		if (records.size() < 1) {
			throw new RuntimeException("No column info records found");
		}
		if (!isInOrder) {
			records.sort(CIRComparator.instance);
		}
	}

	@Override
	public ColumnInfoRecordsAggregate clone() {
		ColumnInfoRecordsAggregate rec = new ColumnInfoRecordsAggregate();
		for (ColumnInfoRecord ci : records) {
			rec.records.add(ci.clone());
		}
		return rec;
	}

	/**
	 * Inserts a column into the aggregate (at the end of the list).
	 */
	public void insertColumn(ColumnInfoRecord col) {
		records.add(col);
		records.sort(CIRComparator.instance);
	}

	/**
	 * Inserts a column into the aggregate (at the position specified by
	 * <code>idx</code>.
	 */
	private void insertColumn(int idx, ColumnInfoRecord col) {
		records.add(idx, col);
	}

	/* package */ int getNumColumns() {
		return records.size();
	}

	public void visitContainedRecords(RecordVisitor rv) {
		int nItems = records.size();
		if (nItems < 1) {
			return;
		}
		ColumnInfoRecord cirPrev = null;
		for (ColumnInfoRecord cir : records) {
			rv.visitRecord(cir);
			if (cirPrev != null && CIRComparator.compareColInfos(cirPrev, cir) > 0) {
				// Excel probably wouldn't mind, but there is much logic in this class
				// that assumes the column info records are kept in order
				throw new RuntimeException("Column info records are out of order");
			}
			cirPrev = cir;
		}
	}

	private int findStartOfColumnOutlineGroup(int pIdx) {
		// Find the start of the group.
		ColumnInfoRecord columnInfo = records.get(pIdx);
		int level = columnInfo.getOutlineLevel();
		int idx = pIdx;
		while (idx != 0) {
			ColumnInfoRecord prevColumnInfo = records.get(idx - 1);
			if (!prevColumnInfo.isAdjacentBefore(columnInfo)) {
				break;
			}
			if (prevColumnInfo.getOutlineLevel() < level) {
				break;
			}
			idx--;
			columnInfo = prevColumnInfo;
		}

		return idx;
	}

	private int findEndOfColumnOutlineGroup(int colInfoIndex) {
		// Find the end of the group.
		ColumnInfoRecord columnInfo = records.get(colInfoIndex);
		int level = columnInfo.getOutlineLevel();
		int idx = colInfoIndex;
		while (idx < records.size() - 1) {
			ColumnInfoRecord nextColumnInfo = records.get(idx + 1);
			if (!columnInfo.isAdjacentBefore(nextColumnInfo)) {
				break;
			}
			if (nextColumnInfo.getOutlineLevel() < level) {
				break;
			}
			idx++;
			columnInfo = nextColumnInfo;
		}
		return idx;
	}

	private ColumnInfoRecord getColInfo(int idx) {
		return records.get( idx );
	}

	/**
	 * 'Collapsed' state is stored in a single column col info record immediately after the outline group
	 * @param idx
	 * @return true, if the column is collapsed, false otherwise.
	 */
	private boolean isColumnGroupCollapsed(int idx) {
		int endOfOutlineGroupIdx = findEndOfColumnOutlineGroup(idx);
		int nextColInfoIx = endOfOutlineGroupIdx+1;
		if (nextColInfoIx >= records.size()) {
			return false;
		}
		ColumnInfoRecord nextColInfo = getColInfo(nextColInfoIx);
		if (!getColInfo(endOfOutlineGroupIdx).isAdjacentBefore(nextColInfo)) {
			return false;
		}
		return nextColInfo.getCollapsed();
	}


	private boolean isColumnGroupHiddenByParent(int idx) {
		// Look out outline details of end
		int endLevel = 0;
		boolean endHidden = false;
		int endOfOutlineGroupIdx = findEndOfColumnOutlineGroup( idx );
		if (endOfOutlineGroupIdx < records.size()) {
			ColumnInfoRecord nextInfo = getColInfo(endOfOutlineGroupIdx + 1);
			if (getColInfo(endOfOutlineGroupIdx).isAdjacentBefore(nextInfo)) {
				endLevel = nextInfo.getOutlineLevel();
				endHidden = nextInfo.getHidden();
			}
		}
		// Look out outline details of start
		int startLevel = 0;
		boolean startHidden = false;
		int startOfOutlineGroupIdx = findStartOfColumnOutlineGroup( idx );
		if (startOfOutlineGroupIdx > 0) {
			ColumnInfoRecord prevInfo = getColInfo(startOfOutlineGroupIdx - 1);
			if (prevInfo.isAdjacentBefore(getColInfo(startOfOutlineGroupIdx))) {
				startLevel = prevInfo.getOutlineLevel();
				startHidden = prevInfo.getHidden();
			}
		}
		if (endLevel > startLevel) {
			return endHidden;
		}
		return startHidden;
	}

	public void collapseColumn(int columnIndex) {
		int colInfoIx = findColInfoIdx(columnIndex, 0);
		if (colInfoIx == -1) {
			return;
		}

		// Find the start of the group.
		int groupStartColInfoIx = findStartOfColumnOutlineGroup(colInfoIx);
		ColumnInfoRecord columnInfo = getColInfo(groupStartColInfoIx);

		// Hide all the columns until the end of the group
		int lastColIx = setGroupHidden(groupStartColInfoIx, columnInfo.getOutlineLevel(), true);

		// Write collapse field
		setColumn(lastColIx + 1, null, null, null, null, Boolean.TRUE);
	}
	/**
	 * Sets all adjacent columns of the same outline level to the specified hidden status.
	 * @param pIdx the col info index of the start of the outline group
	 * @return the column index of the last column in the outline group
	 */
	private int setGroupHidden(int pIdx, int level, boolean hidden) {
		int idx = pIdx;
		ColumnInfoRecord columnInfo = getColInfo(idx);
		while (idx < records.size()) {
			columnInfo.setHidden(hidden);
			if (idx + 1 < records.size()) {
				ColumnInfoRecord nextColumnInfo = getColInfo(idx + 1);
				if (!columnInfo.isAdjacentBefore(nextColumnInfo)) {
					break;
				}
				if (nextColumnInfo.getOutlineLevel() < level) {
					break;
				}
				columnInfo = nextColumnInfo;
			}
			idx++;
		}
		return columnInfo.getLastColumn();
	}


	public void expandColumn(int columnIndex) {
		int idx = findColInfoIdx(columnIndex, 0);
		if (idx == -1) {
			return;
		}

		// If it is already expanded do nothing.
		if (!isColumnGroupCollapsed(idx)) {
			return;
		}

		// Find the start/end of the group.
		int startIdx = findStartOfColumnOutlineGroup(idx);
		int endIdx = findEndOfColumnOutlineGroup(idx);

		// expand:
		// colapsed bit must be unset
		// hidden bit gets unset _if_ surrounding groups are expanded you can determine
		//   this by looking at the hidden bit of the enclosing group.  You will have
		//   to look at the start and the end of the current group to determine which
		//   is the enclosing group
		// hidden bit only is altered for this outline level.  ie.  don't uncollapse contained groups
		ColumnInfoRecord columnInfo = getColInfo(endIdx);
		if (!isColumnGroupHiddenByParent(idx)) {
			int outlineLevel = columnInfo.getOutlineLevel();
			for (int i = startIdx; i <= endIdx; i++) {
				ColumnInfoRecord ci = getColInfo(i);
				if (outlineLevel == ci.getOutlineLevel())
					ci.setHidden(false);
			}
		}

		// Write collapse flag (stored in a single col info record after this outline group)
		setColumn(columnInfo.getLastColumn() + 1, null, null, null, null, Boolean.FALSE);
	}

	private static ColumnInfoRecord copyColInfo(ColumnInfoRecord ci) {
		return ci.clone();
	}


	public void setColumn(int targetColumnIx, Short xfIndex, Integer width,
					Integer level, Boolean hidden, Boolean collapsed) {
		ColumnInfoRecord ci = null;
		int k;
		for (k = 0; k < records.size(); k++) {
			ColumnInfoRecord tci = records.get(k);
			if (tci.containsColumn(targetColumnIx)) {
				ci = tci;
				break;
			}
			if (tci.getFirstColumn() > targetColumnIx) {
				// call column infos after k are for later columns
				break; // exit now so k will be the correct insert pos
			}
		}

		if (ci == null) {
			// okay so there ISN'T a column info record that covers this column so lets create one!
			ColumnInfoRecord nci = new ColumnInfoRecord();

			nci.setFirstColumn(targetColumnIx);
			nci.setLastColumn(targetColumnIx);
			setColumnInfoFields( nci, xfIndex, width, level, hidden, collapsed );
			insertColumn(k, nci);
			attemptMergeColInfoRecords(k);
			return;
		}

		boolean styleChanged = xfIndex != null && ci.getXFIndex() != xfIndex.shortValue();
		boolean widthChanged = width != null && ci.getColumnWidth() != width.shortValue();
		boolean levelChanged = level != null && ci.getOutlineLevel() != level.intValue();
		boolean hiddenChanged = hidden != null && ci.getHidden() != hidden.booleanValue();
		boolean collapsedChanged = collapsed != null && ci.getCollapsed() != collapsed.booleanValue();

		boolean columnChanged = styleChanged || widthChanged || levelChanged || hiddenChanged || collapsedChanged;
		if (!columnChanged) {
			// do nothing...nothing changed.
			return;
		}

		if (ci.getFirstColumn() == targetColumnIx && ci.getLastColumn() == targetColumnIx) {
			// ColumnInfo ci for a single column, the target column
			setColumnInfoFields(ci, xfIndex, width, level, hidden, collapsed);
			attemptMergeColInfoRecords(k);
			return;
		}

		if (ci.getFirstColumn() == targetColumnIx || ci.getLastColumn() == targetColumnIx) {
			// The target column is at either end of the multi-column ColumnInfo ci
			// we'll just divide the info and create a new one
			if (ci.getFirstColumn() == targetColumnIx) {
				ci.setFirstColumn(targetColumnIx + 1);
			} else {
				ci.setLastColumn(targetColumnIx - 1);
				k++; // adjust insert pos to insert after
			}
			ColumnInfoRecord nci = copyColInfo(ci);

			nci.setFirstColumn(targetColumnIx);
			nci.setLastColumn(targetColumnIx);
			setColumnInfoFields( nci, xfIndex, width, level, hidden, collapsed );

			insertColumn(k, nci);
			attemptMergeColInfoRecords(k);
		} else {
			//split to 3 records
            ColumnInfoRecord ciMid = copyColInfo(ci);
			ColumnInfoRecord ciEnd = copyColInfo(ci);
			int lastcolumn = ci.getLastColumn();

			ci.setLastColumn(targetColumnIx - 1);

			ciMid.setFirstColumn(targetColumnIx);
			ciMid.setLastColumn(targetColumnIx);
			setColumnInfoFields(ciMid, xfIndex, width, level, hidden, collapsed);
			insertColumn(++k, ciMid);

			ciEnd.setFirstColumn(targetColumnIx+1);
			ciEnd.setLastColumn(lastcolumn);
			insertColumn(++k, ciEnd);
			// no need to attemptMergeColInfoRecords because we
			// know both on each side are different
		}
	}

	/**
	 * Sets all non null fields into the <code>ci</code> parameter.
	 */
	private static void setColumnInfoFields(ColumnInfoRecord ci, Short xfStyle, Integer width,
				Integer level, Boolean hidden, Boolean collapsed) {
		if (xfStyle != null) {
			ci.setXFIndex(xfStyle.shortValue());
		}
		if (width != null) {
			ci.setColumnWidth(width.intValue());
		}
		if (level != null) {
			ci.setOutlineLevel( level.shortValue() );
		}
		if (hidden != null) {
			ci.setHidden( hidden.booleanValue() );
		}
		if (collapsed != null) {
			ci.setCollapsed( collapsed.booleanValue() );
		}
	}

	private int findColInfoIdx(int columnIx, int fromColInfoIdx) {
		if (columnIx < 0) {
			throw new IllegalArgumentException( "column parameter out of range: " + columnIx );
		}
		if (fromColInfoIdx < 0) {
			throw new IllegalArgumentException( "fromIdx parameter out of range: " + fromColInfoIdx );
		}

		for (int k = fromColInfoIdx; k < records.size(); k++) {
			ColumnInfoRecord ci = getColInfo(k);
			if (ci.containsColumn(columnIx)) {
				return k;
			}
			if (ci.getFirstColumn() > columnIx) {
				break;
			}
		}
		return -1;
	}

	/**
	 * Attempts to merge the col info record at the specified index
	 * with either or both of its neighbours
	 */
	private void attemptMergeColInfoRecords(int colInfoIx) {
		int nRecords = records.size();
		if (colInfoIx < 0 || colInfoIx >= nRecords) {
			throw new IllegalArgumentException("colInfoIx " + colInfoIx
					+ " is out of range (0.." + (nRecords-1) + ")");
		}
		ColumnInfoRecord currentCol = getColInfo(colInfoIx);
		int nextIx = colInfoIx+1;
		if (nextIx < nRecords) {
			if (mergeColInfoRecords(currentCol, getColInfo(nextIx))) {
    			records.remove(nextIx);
			}
		}
		if (colInfoIx > 0) {
			if (mergeColInfoRecords(getColInfo(colInfoIx - 1), currentCol)) {
    			records.remove(colInfoIx);
    		}
		}
	}
	/**
	 * merges two column info records (if they are adjacent and have the same formatting, etc)
	 * @return <code>false</code> if the two column records could not be merged
	 */
	private static boolean mergeColInfoRecords(ColumnInfoRecord ciA, ColumnInfoRecord ciB) {
		if (ciA.isAdjacentBefore(ciB) && ciA.formatMatches(ciB)) {
			ciA.setLastColumn(ciB.getLastColumn());
			return true;
		}
		return false;
	}
	/**
	 * Creates an outline group for the specified columns, by setting the level
	 * field for each col info record in the range. {@link ColumnInfoRecord}s
	 * may be created, split or merged as a result of this operation.
	 *
	 * @param fromColumnIx
	 *            group from this column (inclusive)
	 * @param toColumnIx
	 *            group to this column (inclusive)
	 * @param indent
	 *            if <code>true</code> the group will be indented by one
	 *            level, if <code>false</code> indenting will be decreased by
	 *            one level.
	 */
	public void groupColumnRange(int fromColumnIx, int toColumnIx, boolean indent) {

		int colInfoSearchStartIdx = 0; // optimization to speed up the search for col infos
		for (int i = fromColumnIx; i <= toColumnIx; i++) {
			int level = 1;
			int colInfoIdx = findColInfoIdx(i, colInfoSearchStartIdx);
			if (colInfoIdx != -1) {
				level = getColInfo(colInfoIdx).getOutlineLevel();
				if (indent) {
					level++;
				} else {
					level--;
				}
				level = Math.max(0, level);
				level = Math.min(7, level);
				colInfoSearchStartIdx = Math.max(0, colInfoIdx - 1); // -1 just in case this column is collapsed later.
			}
			setColumn(i, null, null, Integer.valueOf(level), null, null);
		}
	}

	/**
	 * Finds the <tt>ColumnInfoRecord</tt> which contains the specified columnIndex
	 * @param columnIndex index of the column (not the index of the ColumnInfoRecord)
	 * @return <code>null</code> if no column info found for the specified column
	 */
	public ColumnInfoRecord findColumnInfo(int columnIndex) {
		int nInfos = records.size();
		for(int i=0; i< nInfos; i++) {
			ColumnInfoRecord ci = getColInfo(i);
			if (ci.containsColumn(columnIndex)) {
				return ci;
			}
		}
		return null;
	}

	public int getMaxOutlineLevel() {
		int result = 0;
		int count=records.size();
		for (int i=0; i<count; i++) {
			ColumnInfoRecord columnInfoRecord = getColInfo(i);
			result = Math.max(columnInfoRecord.getOutlineLevel(), result);
		}
		return result;
	}

	public int getOutlineLevel(int columnIndex) {
	    ColumnInfoRecord ci = findColumnInfo(columnIndex);
	    if (ci != null) {
            return ci.getOutlineLevel();
	    } else {
	        return 0;
	    }
	}

	public int getMinColumnIndex() {
		if(records.isEmpty()) {
			return 0;
		}

		int minIndex = Integer.MAX_VALUE;
		int nInfos = records.size();
		for(int i=0; i< nInfos; i++) {
			ColumnInfoRecord ci = getColInfo(i);
			minIndex = Math.min(minIndex, ci.getFirstColumn());
		}

		return minIndex;
	}

	public int getMaxColumnIndex() {
		if(records.isEmpty()) {
			return 0;
		}

		int maxIndex = 0;
		int nInfos = records.size();
		for(int i=0; i< nInfos; i++) {
			ColumnInfoRecord ci = getColInfo(i);
			maxIndex = Math.max(maxIndex, ci.getLastColumn());
		}

		return maxIndex;
	}
}
