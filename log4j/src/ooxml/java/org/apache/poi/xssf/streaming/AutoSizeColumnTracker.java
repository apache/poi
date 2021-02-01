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

package org.apache.poi.xssf.streaming;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.SheetUtil;
import org.apache.poi.util.Internal;

/**
 * Tracks best fit column width for rows of an {@link SXSSFSheet},
 * to be able to correctly calculate auto-sized column widths even
 * if some rows are already flushed to disk.
 * This is an auxiliary data structure that uses a TreeMap containing
 * one entry per tracked column, where the key is the column index and
 * the value is a pair of doubles. This data structure's memory footprint
 * is linear with the number of *tracked* columns and invariant with
 * the number of rows or columns in the sheet.
 * @since 3.14beta1
*/
@Internal
/*package*/ class AutoSizeColumnTracker {
    private final int defaultCharWidth;
    private final DataFormatter dataFormatter = new DataFormatter();

    // map of tracked columns, with values containing the best-fit width for the column
    // Using a HashMap instead of a TreeMap because insertion (trackColumn), removal (untrackColumn), and membership (everything)
    // will be called more frequently than getTrackedColumns(). The O(1) cost of insertion, removal, and membership operations
    // outweigh the infrequent O(n*log n) cost of sorting getTrackedColumns().
    // Memory consumption for a HashMap and TreeMap is about the same
    private final Map<Integer, ColumnWidthPair> maxColumnWidths = new HashMap<>();
    // untrackedColumns stores columns have been explicitly untracked so they aren't implicitly re-tracked by trackAllColumns
    // Using a HashSet instead of a TreeSet because we don't care about order.
    private final Set<Integer> untrackedColumns = new HashSet<>();
    private boolean trackAllColumns;

    /**
     * Tuple to store the column widths considering and not considering merged cells
     * If more permutations are needed, it may be prudent to require the user to specify
     * how they intend to auto-size a column when they track the column, so calculations
     * are limited to the desired intentions. Unless this proves to be a performance problem,
     * it's probably better to let the user defer how they want to auto-size to SXSSFSheet.autoSizeColumn,
     * rather than twice (via SXSSFSheet.trackColumn(int column, boolean useMergedCells) and again at
     * SXSFSheet.autoSizeColumn(int column, boolean useMergedCells))
     * @since 3.14beta1
     */
    private static class ColumnWidthPair {
        private double withSkipMergedCells;
        private double withUseMergedCells;
        
        public ColumnWidthPair() {
            this(-1.0, -1.0);
        }
        
        public ColumnWidthPair(final double columnWidthSkipMergedCells, final double columnWidthUseMergedCells) {
            withSkipMergedCells = columnWidthSkipMergedCells;
            withUseMergedCells = columnWidthUseMergedCells;
        }
        
        /**
         * Gets the current best-fit column width for the provided settings
         *
         * @param useMergedCells true if merged cells are considered into the best-fit column width calculation
         * @return best fit column width, measured in default character widths.
         */
        public double getMaxColumnWidth(final boolean useMergedCells) {
            return useMergedCells ? withUseMergedCells : withSkipMergedCells;
        }
        
        /**
         * Sets the best-fit column width to the maximum of the current width and the provided width
         *
         * @param unmergedWidth the best-fit column width calculated with useMergedCells=False
         * @param mergedWidth the best-fit column width calculated with useMergedCells=True
         */
        public void setMaxColumnWidths(double unmergedWidth, double mergedWidth) {
            withUseMergedCells = Math.max(withUseMergedCells, mergedWidth);
            withSkipMergedCells = Math.max(withUseMergedCells, unmergedWidth);
        }
    }
    
    /**
     * AutoSizeColumnTracker constructor. Holds no reference to <code>sheet</code>
     *
     * @param sheet the sheet associated with this auto-size column tracker
     * @since 3.14beta1
     */
    public AutoSizeColumnTracker(final Sheet sheet) {
        // If sheet needs to be saved, use a java.lang.ref.WeakReference to avoid garbage collector gridlock.
        defaultCharWidth = SheetUtil.getDefaultCharWidth(sheet.getWorkbook());
    }
    
    /**
     * Get the currently tracked columns, naturally ordered.
     * Note if all columns are tracked, this will only return the columns that have been explicitly or implicitly tracked,
     * which is probably only columns containing 1 or more non-blank values
     *
     * @return a set of the indices of all tracked columns
     * @since 3.14beta1
     */
    public SortedSet<Integer> getTrackedColumns() {
        SortedSet<Integer> sorted = new TreeSet<>(maxColumnWidths.keySet());
        return Collections.unmodifiableSortedSet(sorted);
    }
    
    /**
     * Returns true if column is currently tracked for auto-sizing.
     *
     * @param column the index of the column to check
     * @return true if column is tracked
     * @since 3.14beta1
     */
    public boolean isColumnTracked(int column) {
        return (trackAllColumns && !untrackedColumns.contains(column))
                || maxColumnWidths.containsKey(column);
    }
    
    /**
     * Returns true if all columns are implicitly tracked.
     *
     * @return true if all columns are implicitly tracked
     * @since 3.14beta1
     */
    public boolean isAllColumnsTracked() {
        return trackAllColumns;
    }
    
    /**
     * Tracks all non-blank columns
     * Allows columns that have been explicitly untracked to be tracked
     * @since 3.14beta1
     */
    public void trackAllColumns() {
        trackAllColumns = true;
        untrackedColumns.clear();
    }
    
    /**
     * Untrack all columns that were previously tracked for auto-sizing.
     * All best-fit column widths are forgotten.
     * @since 3.14beta1
     */
    public void untrackAllColumns() {
        trackAllColumns = false;
        maxColumnWidths.clear();
        untrackedColumns.clear();
    }
    
    /**
     * Marks multiple columns for inclusion in auto-size column tracking.
     * Note this has undefined behavior if columns are tracked after one or more rows are written to the sheet.
     * Any column in <code>columns</code> that are already tracked are ignored by this call. 
     *
     * @param columns the indices of the columns to track
     * @since 3.14beta1
     */
    public void trackColumns(Collection<Integer> columns)
    {
        for (final int column : columns) {
            trackColumn(column);
        }
    }

    /**
     * Marks a column for inclusion in auto-size column tracking.
     * Note this has undefined behavior if a column is tracked after one or more rows are written to the sheet.
     * If <code>column</code> is already tracked, this call does nothing.
     *
     * @param column the index of the column to track for auto-sizing
     * @return if column is already tracked, the call does nothing and returns false
     * @since 3.14beta1
     */
    public boolean trackColumn(int column) {
        untrackedColumns.remove(column);
        if (!maxColumnWidths.containsKey(column)) {
            maxColumnWidths.put(column, new ColumnWidthPair());
            return true;
        }
        return false;
    }
    
    /**
     * Implicitly track a column if it has not been explicitly untracked
     * If it has been explicitly untracked, this call does nothing and returns false.
     * Otherwise return true
     *
     * @param column the column to implicitly track
     * @return false if column has been explicitly untracked, otherwise return true
     */
    private boolean implicitlyTrackColumn(int column) {
        if (!untrackedColumns.contains(column)) {
            trackColumn(column);
            return true;
        }
        return false;
    }
    
    /**
     * Removes columns that were previously marked for inclusion in auto-size column tracking.
     * When a column is untracked, the best-fit width is forgotten.
     * Any column in <code>columns</code> that is not tracked will be ignored by this call.
     *
     * @param columns the indices of the columns to track for auto-sizing
     * @return true if one or more columns were untracked as a result of this call
     * @since 3.14beta1
     */
    public boolean untrackColumns(Collection<Integer> columns)
    {
        untrackedColumns.addAll(columns);
        return maxColumnWidths.keySet().removeAll(columns);
    }
    
    /**
     * Removes a column that was previously marked for inclusion in auto-size column tracking.
     * When a column is untracked, the best-fit width is forgotten.
     * If <code>column</code> is not tracked, it will be ignored by this call.
     *
     * @param column the index of the column to track for auto-sizing
     * @return true if column was tracked prior this call, false if no action was taken
     * @since 3.14beta1
     */
    public boolean untrackColumn(int column) {
        untrackedColumns.add(column);
        return maxColumnWidths.keySet().remove(column);
    }

    /**
     * Get the best-fit width of a tracked column
     *
     * @param column the index of the column to get the current best-fit width of
     * @param useMergedCells true if merged cells should be considered when computing the best-fit width
     * @return best-fit column width, measured in number of characters
     * @throws IllegalStateException if column is not tracked and trackAllColumns is false
     * @since 3.14beta1
     */
    public int getBestFitColumnWidth(int column, boolean useMergedCells) {
        if (!maxColumnWidths.containsKey(column)) {
            // if column is not tracked, implicitly track the column if trackAllColumns is True and column has not been explicitly untracked
            if (trackAllColumns) {
                if (!implicitlyTrackColumn(column)) {
                    final Throwable reason = new IllegalStateException(
                            "Column was explicitly untracked after trackAllColumns() was called.");
                    throw new IllegalStateException(
                            "Cannot get best fit column width on explicitly untracked column " + column + ". " +
                            "Either explicitly track the column or track all columns.", reason);
                }
            }
            else {
                final Throwable reason = new IllegalStateException(
                        "Column was never explicitly tracked and isAllColumnsTracked() is false " +
                        "(trackAllColumns() was never called or untrackAllColumns() was called after trackAllColumns() was called).");
                throw new IllegalStateException(
                        "Cannot get best fit column width on untracked column " + column + ". " +
                        "Either explicitly track the column or track all columns.", reason);
            }
        }
        final double width = maxColumnWidths.get(column).getMaxColumnWidth(useMergedCells);
        return Math.toIntExact(Math.round(256*width));
    }
    

    
    /**
     * Calculate the best fit width for each tracked column in row
     *
     * @param row the row to get the cells
     * @since 3.14beta1
     */
    public void updateColumnWidths(Row row) {
        // track new columns
        implicitlyTrackColumnsInRow(row);
        
        // update the widths
        // for-loop over the shorter of the number of cells in the row and the number of tracked columns
        // these two for-loops should do the same thing
        if (maxColumnWidths.size() < row.getPhysicalNumberOfCells()) {
            // loop over the tracked columns, because there are fewer tracked columns than cells in this row
            for (final Entry<Integer, ColumnWidthPair> e : maxColumnWidths.entrySet()) {
                final int column = e.getKey();
                final Cell cell = row.getCell(column); //is MissingCellPolicy=Row.RETURN_NULL_AND_BLANK needed?

                // FIXME: if cell belongs to a merged region, some of the merged region may have fallen outside of the random access window
                // In this case, getting the column width may result in an error. Need to gracefully handle this.

                // FIXME: Most cells are not merged, so calling getCellWidth twice re-computes the same value twice.
                // Need to rewrite this to avoid unnecessary computation if this proves to be a performance bottleneck.

                if (cell != null) {
                    final ColumnWidthPair pair = e.getValue();
                    updateColumnWidth(cell, pair);
                }
            }
        }
        else {
            // loop over the cells in this row, because there are fewer cells in this row than tracked columns
            for (final Cell cell : row) {
                final int column = cell.getColumnIndex();

                // FIXME: if cell belongs to a merged region, some of the merged region may have fallen outside of the random access window
                // In this case, getting the column width may result in an error. Need to gracefully handle this.

                // FIXME: Most cells are not merged, so calling getCellWidth twice re-computes the same value twice.
                // Need to rewrite this to avoid unnecessary computation if this proves to be a performance bottleneck.

                if (maxColumnWidths.containsKey(column)) {
                    final ColumnWidthPair pair = maxColumnWidths.get(column);
                    updateColumnWidth(cell, pair);
                }
            }
        }
    }
    
    /**
     * Helper for {@link #updateColumnWidths(Row)}.
     * Implicitly track the columns corresponding to the cells in row.
     * If all columns in the row are already tracked, this call does nothing.
     * Explicitly untracked columns will not be tracked.
     *
     * @param row the row containing cells to implicitly track the columns
     * @since 3.14beta1
     */
    private void implicitlyTrackColumnsInRow(Row row) {
        // track new columns
        if (trackAllColumns) {
            // if column is not tracked, implicitly track the column if trackAllColumns is True and column has not been explicitly untracked 
            for (final Cell cell : row) {
                final int column = cell.getColumnIndex();
                implicitlyTrackColumn(column);
            }
        }
    }
    
    /**
     * Helper for {@link #updateColumnWidths(Row)}.
     *
     * @param cell the cell to compute the best fit width on
     * @param pair the column width pair to update
     * @since 3.14beta1
     */
    private void updateColumnWidth(final Cell cell, final ColumnWidthPair pair) {
        final double unmergedWidth = SheetUtil.getCellWidth(cell, defaultCharWidth, dataFormatter, false);
        final double mergedWidth = SheetUtil.getCellWidth(cell, defaultCharWidth, dataFormatter, true);
        pair.setMaxColumnWidths(unmergedWidth, mergedWidth);
    }
}
