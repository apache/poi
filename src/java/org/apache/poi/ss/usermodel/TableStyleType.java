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

package org.apache.poi.ss.usermodel;

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressBase;
import org.apache.poi.ss.util.CellReference;

/**
 * Ordered list of table style elements, for both data tables and pivot tables.
 * Some elements only apply to pivot tables, but any style definition can omit any number,
 * so having them in one list should not be a problem.
 * <p>
 * The order is the specification order of application, with later elements overriding previous
 * ones, if style properties conflict.
 * <p>
 * Processing could iterate bottom-up if looking for specific properties, and stop when the
 * first style is found defining a value for that property.
 * <p>
 * Enum names match the OOXML spec values exactly, so {@link #valueOf(String)} will work.
 * 
 * @since 3.17 beta 1
 */
public enum TableStyleType {
    /***/
    wholeTable {
        public CellRangeAddressBase getRange(Table table, CellReference cell) {
            return new CellRangeAddress(table.getStartRowIndex(), table.getEndRowIndex(), table.getStartColIndex(), table.getEndColIndex());
        }
    },
    /***/
    pageFieldLabels, // pivot only
    /***/
    pageFieldValues, // pivot only
    /***/
    firstColumnStripe{
        public CellRangeAddressBase getRange(Table table, CellReference cell) {
            TableStyleInfo info = table.getStyle();
            if (! info.isShowColumnStripes()) return null;
            DifferentialStyleProvider c1Style = info.getStyle().getStyle(firstColumnStripe);
            DifferentialStyleProvider c2Style = info.getStyle().getStyle(secondColumnStripe);
            int c1Stripe = c1Style == null ? 1 : Math.max(1, c1Style.getStripeSize());
            int c2Stripe = c2Style == null ? 1 : Math.max(1, c2Style.getStripeSize());
            
            int firstStart = table.getStartColIndex();
            int secondStart = firstStart + c1Stripe;
            final int c = cell.getCol();
            
            // look for the stripe containing c, accounting for the style element stripe size
            // could do fancy math, but tables can't be that wide, a simple loop is fine
            // if not in this type of stripe, return null
            while (firstStart <= c) {
                if (c <= secondStart -1) {
                    return new CellRangeAddress(table.getStartRowIndex(), table.getEndRowIndex(), firstStart, secondStart - 1);
                }
                firstStart = secondStart + c2Stripe;
                secondStart = firstStart + c1Stripe;
            }
            return null;
        }
    },
    /***/
    secondColumnStripe{
        public CellRangeAddressBase getRange(Table table, CellReference cell) {
            TableStyleInfo info = table.getStyle();
            if (! info.isShowColumnStripes()) return null;
            
            DifferentialStyleProvider c1Style = info.getStyle().getStyle(firstColumnStripe);
            DifferentialStyleProvider c2Style = info.getStyle().getStyle(secondColumnStripe);
            int c1Stripe = c1Style == null ? 1 : Math.max(1, c1Style.getStripeSize());
            int c2Stripe = c2Style == null ? 1 : Math.max(1, c2Style.getStripeSize());

            int firstStart = table.getStartColIndex();
            int secondStart = firstStart + c1Stripe;
            final int c = cell.getCol();
            
            // look for the stripe containing c, accounting for the style element stripe size
            // could do fancy math, but tables can't be that wide, a simple loop is fine
            // if not in this type of stripe, return null
            while (firstStart <= c) {
                if (c >= secondStart && c <= secondStart + c2Stripe -1) {
                    return new CellRangeAddress(table.getStartRowIndex(), table.getEndRowIndex(), secondStart, secondStart + c2Stripe - 1);
                }
                firstStart = secondStart + c2Stripe;
                secondStart = firstStart + c1Stripe;
            }
            return null;
        }
    },
    /***/
    firstRowStripe {
        public CellRangeAddressBase getRange(Table table, CellReference cell) {
            TableStyleInfo info = table.getStyle();
            if (! info.isShowRowStripes()) return null;
            
            DifferentialStyleProvider c1Style = info.getStyle().getStyle(firstRowStripe);
            DifferentialStyleProvider c2Style = info.getStyle().getStyle(secondRowStripe);
            int c1Stripe = c1Style == null ? 1 : Math.max(1, c1Style.getStripeSize());
            int c2Stripe = c2Style == null ? 1 : Math.max(1, c2Style.getStripeSize());

            int firstStart = table.getStartRowIndex() + table.getHeaderRowCount();
            int secondStart = firstStart + c1Stripe;
            final int c = cell.getRow();
            
            // look for the stripe containing c, accounting for the style element stripe size
            // could do fancy math, but tables can't be that wide, a simple loop is fine
            // if not in this type of stripe, return null
            while (firstStart <= c) {
                if (c <= secondStart -1) {
                    return new CellRangeAddress(firstStart, secondStart - 1, table.getStartColIndex(), table.getEndColIndex());
                }
                firstStart = secondStart + c2Stripe;
                secondStart = firstStart + c1Stripe;
            }
            return null;
        }
    },
    /***/
    secondRowStripe{
        public CellRangeAddressBase getRange(Table table, CellReference cell) {
            TableStyleInfo info = table.getStyle();
            if (! info.isShowRowStripes()) return null;
            
            DifferentialStyleProvider c1Style = info.getStyle().getStyle(firstRowStripe);
            DifferentialStyleProvider c2Style = info.getStyle().getStyle(secondRowStripe);
            int c1Stripe = c1Style == null ? 1 : Math.max(1, c1Style.getStripeSize());
            int c2Stripe = c2Style == null ? 1 : Math.max(1, c2Style.getStripeSize());

            int firstStart = table.getStartRowIndex() + table.getHeaderRowCount();
            int secondStart = firstStart + c1Stripe;
            final int c = cell.getRow();
            
            // look for the stripe containing c, accounting for the style element stripe size
            // could do fancy math, but tables can't be that wide, a simple loop is fine
            // if not in this type of stripe, return null
            while (firstStart <= c) {
                if (c >= secondStart && c <= secondStart +c2Stripe -1) {
                    return new CellRangeAddress(secondStart, secondStart + c2Stripe - 1, table.getStartColIndex(), table.getEndColIndex());
                }
                firstStart = secondStart + c2Stripe;
                secondStart = firstStart + c1Stripe;
            }
            return null;
        }
    },
    /***/
    lastColumn {
        public CellRangeAddressBase getRange(Table table, CellReference cell) {
            if (! table.getStyle().isShowLastColumn()) return null;
            return new CellRangeAddress(table.getStartRowIndex(), table.getEndRowIndex(), table.getEndColIndex(), table.getEndColIndex());
        }
    },
    /***/
    firstColumn {
        public CellRangeAddressBase getRange(Table table, CellReference cell) {
            if (! table.getStyle().isShowFirstColumn()) return null;
            return new CellRangeAddress(table.getStartRowIndex(), table.getEndRowIndex(), table.getStartColIndex(), table.getStartColIndex());
        }
    },
    /***/
    headerRow {
        public CellRangeAddressBase getRange(Table table, CellReference cell) {
            if (table.getHeaderRowCount() < 1) return null;
            return new CellRangeAddress(table.getStartRowIndex(), table.getStartRowIndex() + table.getHeaderRowCount() -1, table.getStartColIndex(), table.getEndColIndex());
        }
    },
    /***/
    totalRow {
        public CellRangeAddressBase getRange(Table table, CellReference cell) {
            if (table.getTotalsRowCount() < 1) return null;
            return new CellRangeAddress(table.getEndRowIndex() - table.getTotalsRowCount() +1, table.getEndRowIndex(), table.getStartColIndex(), table.getEndColIndex());
        }
    },
    /***/
    firstHeaderCell {
        public CellRangeAddressBase getRange(Table table, CellReference cell) {
            if (table.getHeaderRowCount() < 1) return null;
            return new CellRangeAddress(table.getStartRowIndex(), table.getStartRowIndex(), table.getStartColIndex(), table.getStartColIndex());
        }
    },
    /***/
    lastHeaderCell {
        public CellRangeAddressBase getRange(Table table, CellReference cell) {
            if (table.getHeaderRowCount() < 1) return null;
            return new CellRangeAddress(table.getStartRowIndex(), table.getStartRowIndex(), table.getEndColIndex(), table.getEndColIndex());
        }
    },
    /***/
    firstTotalCell {
        public CellRangeAddressBase getRange(Table table, CellReference cell) {
            if (table.getTotalsRowCount() < 1) return null;
            return new CellRangeAddress(table.getEndRowIndex() - table.getTotalsRowCount() +1, table.getEndRowIndex(), table.getStartColIndex(), table.getStartColIndex());
        }
    },
    /***/
    lastTotalCell {
        public CellRangeAddressBase getRange(Table table, CellReference cell) {
            if (table.getTotalsRowCount() < 1) return null;
            return new CellRangeAddress(table.getEndRowIndex() - table.getTotalsRowCount() +1, table.getEndRowIndex(), table.getEndColIndex(), table.getEndColIndex());
        }
    },
    /* these are for pivot tables only */
    /***/
    firstSubtotalColumn,
    /***/
    secondSubtotalColumn,
    /***/
    thirdSubtotalColumn,
    /***/
    blankRow,
    /***/
    firstSubtotalRow,
    /***/
    secondSubtotalRow,
    /***/
    thirdSubtotalRow,
    /***/
    firstColumnSubheading,
    /***/
    secondColumnSubheading,
    /***/
    thirdColumnSubheading,
    /***/
    firstRowSubheading,
    /***/
    secondRowSubheading,
    /***/
    thirdRowSubheading,
    ;
    
    /**
     * A range is returned only for the part of the table matching this enum instance and containing the given cell.
     * Null is returned for all other cases, such as:
     * <ul>
     * <li>Cell on a different sheet than the table
     * <li>Cell outside the table
     * <li>this Enum part is not included in the table (i.e. no header/totals row)
     * <li>this Enum is for a table part not yet implemented in POI, such as pivot table elements
     * </ul>
     * The returned range can be used to determine how style options may or may not apply to this cell.
     * For example, {@link #wholeTable} borders only apply to the outer boundary of a table, while the
     * rest of the styling, such as font and color, could apply to all the interior cells as well.
     * 
     * @param table table to evaluate
     * @param cell to evaluate 
     * @return range in the table representing this class of cells, if it contains the given cell, or null if not applicable.
     * Stripe style types return only the stripe range containing the given cell, or null.
     */
    public CellRangeAddressBase appliesTo(Table table, Cell cell) {
        if (cell == null) return null;
        return appliesTo(table, new CellReference(cell.getSheet().getSheetName(), cell.getRowIndex(), cell.getColumnIndex(), true, true));
    }
    
    /**
     * A range is returned only for the part of the table matching this enum instance and containing the given cell reference.
     * Null is returned for all other cases, such as:
     * <ul>
     * <li>Cell on a different sheet than the table
     * <li>Cell outside the table
     * <li>this Enum part is not included in the table (i.e. no header/totals row)
     * <li>this Enum is for a table part not yet implemented in POI, such as pivot table elements
     * </ul>
     * The returned range can be used to determine how style options may or may not apply to this cell.
     * For example, {@link #wholeTable} borders only apply to the outer boundary of a table, while the
     * rest of the styling, such as font and color, could apply to all the interior cells as well.
     * 
     * @param table table to evaluate
     * @param cell CellReference to evaluate 
     * @return range in the table representing this class of cells, if it contains the given cell, or null if not applicable.
     * Stripe style types return only the stripe range containing the given cell, or null.
     */
    public CellRangeAddressBase appliesTo(Table table, CellReference cell) {
        if (table == null || cell == null) return null;
        if ( ! cell.getSheetName().equals(table.getSheetName())) return null;
        if ( ! table.contains(cell)) return null;
        
        final CellRangeAddressBase range = getRange(table, cell);
        if (range != null && range.isInRange(cell.getRow(), cell.getCol())) return range;
        // else
        return null;
    }

    /**
     * Calls {@link #getRange(Table, CellReference)}.  Use that instead for performance.
     * @param table
     * @param cell
     * @return default is unimplemented/null
     * @see #getRange(Table, CellReference)
     */
    public final CellRangeAddressBase getRange(Table table, Cell cell) {
        if (cell == null) return null;
        return getRange(table, new CellReference(cell.getSheet().getSheetName(), cell.getRowIndex(), cell.getColumnIndex(), true, true));
    }
    
    /**
     *
     * @param table
     * @param cell
     * @return default is unimplemented/null
     */
    public CellRangeAddressBase getRange(Table table, CellReference cell) {
        return null;
    }
}
