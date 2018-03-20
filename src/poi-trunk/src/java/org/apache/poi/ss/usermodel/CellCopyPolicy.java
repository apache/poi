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

import org.apache.poi.util.Beta;

@Beta
public class CellCopyPolicy {
    // cell-level policies
    public static final boolean DEFAULT_COPY_CELL_VALUE_POLICY = true;
    public static final boolean DEFAULT_COPY_CELL_STYLE_POLICY = true;
    public static final boolean DEFAULT_COPY_CELL_FORMULA_POLICY = true;
    public static final boolean DEFAULT_COPY_HYPERLINK_POLICY = true;
    public static final boolean DEFAULT_MERGE_HYPERLINK_POLICY = false;
    
    // row-level policies
    public static final boolean DEFAULT_COPY_ROW_HEIGHT_POLICY = true;
    public static final boolean DEFAULT_CONDENSE_ROWS_POLICY = false;
    
    // sheet-level policies
    public static final boolean DEFAULT_COPY_MERGED_REGIONS_POLICY = true;
    
    // cell-level policies
    private boolean copyCellValue = DEFAULT_COPY_CELL_VALUE_POLICY;
    private boolean copyCellStyle = DEFAULT_COPY_CELL_STYLE_POLICY;
    private boolean copyCellFormula = DEFAULT_COPY_CELL_FORMULA_POLICY;
    private boolean copyHyperlink = DEFAULT_COPY_HYPERLINK_POLICY;
    private boolean mergeHyperlink = DEFAULT_MERGE_HYPERLINK_POLICY;
    
    // row-level policies
    private boolean copyRowHeight = DEFAULT_COPY_ROW_HEIGHT_POLICY;
    private boolean condenseRows = DEFAULT_CONDENSE_ROWS_POLICY;
    
    // sheet-level policies
    private boolean copyMergedRegions = DEFAULT_COPY_MERGED_REGIONS_POLICY;
    
    /** 
     * Default CellCopyPolicy, uses default policy
     * For custom CellCopyPolicy, use {@link Builder} class
     */
    public CellCopyPolicy() { }
    
    /**
     * Copy constructor
     *
     * @param other policy to copy
     */
    public CellCopyPolicy(CellCopyPolicy other) {
        copyCellValue = other.isCopyCellValue();
        copyCellStyle = other.isCopyCellStyle();
        copyCellFormula = other.isCopyCellFormula();
        copyHyperlink = other.isCopyHyperlink();
        mergeHyperlink = other.isMergeHyperlink();
        
        copyRowHeight = other.isCopyRowHeight();
        condenseRows = other.isCondenseRows();
        
        copyMergedRegions = other.isCopyMergedRegions();
    }
    
    // should builder be replaced with CellCopyPolicy setters that return the object
    // to allow setters to be chained together?
    // policy.setCopyCellValue(true).setCopyCellStyle(true)
    private CellCopyPolicy(Builder builder) {
        copyCellValue = builder.copyCellValue;
        copyCellStyle = builder.copyCellStyle;
        copyCellFormula = builder.copyCellFormula;
        copyHyperlink = builder.copyHyperlink;
        mergeHyperlink = builder.mergeHyperlink;
        
        copyRowHeight = builder.copyRowHeight;
        condenseRows = builder.condenseRows;
        
        copyMergedRegions = builder.copyMergedRegions;
    }
    
    public static class Builder {
        // cell-level policies
        private boolean copyCellValue = DEFAULT_COPY_CELL_VALUE_POLICY;
        private boolean copyCellStyle = DEFAULT_COPY_CELL_STYLE_POLICY;
        private boolean copyCellFormula = DEFAULT_COPY_CELL_FORMULA_POLICY;
        private boolean copyHyperlink = DEFAULT_COPY_HYPERLINK_POLICY;
        private boolean mergeHyperlink = DEFAULT_MERGE_HYPERLINK_POLICY;
        
        // row-level policies
        private boolean copyRowHeight = DEFAULT_COPY_ROW_HEIGHT_POLICY;
        private boolean condenseRows = DEFAULT_CONDENSE_ROWS_POLICY;
        
        // sheet-level policies
        private boolean copyMergedRegions = DEFAULT_COPY_MERGED_REGIONS_POLICY;
        
        /**
         * Builder class for CellCopyPolicy
         */
        public Builder() {
        }
        
        // cell-level policies
        public Builder cellValue(boolean copyCellValue) {
            this.copyCellValue = copyCellValue;
            return this;
        }
        public Builder cellStyle(boolean copyCellStyle) {
            this.copyCellStyle = copyCellStyle;
            return this;
        }
        public Builder cellFormula(boolean copyCellFormula) {
            this.copyCellFormula = copyCellFormula;
            return this;
        }
        public Builder copyHyperlink(boolean copyHyperlink) {
            this.copyHyperlink = copyHyperlink;
            return this;
        }
        public Builder mergeHyperlink(boolean mergeHyperlink) {
            this.mergeHyperlink = mergeHyperlink;
            return this;
        }
        
        // row-level policies
        public Builder rowHeight(boolean copyRowHeight) {
            this.copyRowHeight = copyRowHeight;
            return this;
        }
        public Builder condenseRows(boolean condenseRows) {
            this.condenseRows = condenseRows;
            return this;
        }
        
        // sheet-level policies
        public Builder mergedRegions(boolean copyMergedRegions) {
            this.copyMergedRegions = copyMergedRegions;
            return this;
        }
        public CellCopyPolicy build() {
            return new CellCopyPolicy(this);
        }
    }
    
    public Builder createBuilder() {
        return new Builder()
                .cellValue(copyCellValue)
                .cellStyle(copyCellStyle)
                .cellFormula(copyCellFormula)
                .copyHyperlink(copyHyperlink)
                .mergeHyperlink(mergeHyperlink)
                .rowHeight(copyRowHeight)
                .condenseRows(condenseRows)
                .mergedRegions(copyMergedRegions);
    }

/*
 * Cell-level policies 
 */
    /**
     * @return the copyCellValue
     */
    public boolean isCopyCellValue() {
        return copyCellValue;
    }

    /**
     * @param copyCellValue the copyCellValue to set
     */
    public void setCopyCellValue(boolean copyCellValue) {
        this.copyCellValue = copyCellValue;
    }

    /**
     * @return the copyCellStyle
     */
    public boolean isCopyCellStyle() {
        return copyCellStyle;
    }

    /**
     * @param copyCellStyle the copyCellStyle to set
     */
    public void setCopyCellStyle(boolean copyCellStyle) {
        this.copyCellStyle = copyCellStyle;
    }

    /**
     * @return the copyCellFormula
     */
    public boolean isCopyCellFormula() {
        return copyCellFormula;
    }

    /**
     * @param copyCellFormula the copyCellFormula to set
     */
    public void setCopyCellFormula(boolean copyCellFormula) {
        this.copyCellFormula = copyCellFormula;
    }
    
    /**
     * @return the copyHyperlink
     */
    public boolean isCopyHyperlink() {
        return copyHyperlink;
    }

    /**
     * @param copyHyperlink the copyHyperlink to set
     */
    public void setCopyHyperlink(boolean copyHyperlink) {
        this.copyHyperlink = copyHyperlink;
    }
    
    /**
     * @return the mergeHyperlink
     */
    public boolean isMergeHyperlink() {
        return mergeHyperlink;
    }

    /**
     * @param mergeHyperlink the mergeHyperlink to set
     */
    public void setMergeHyperlink(boolean mergeHyperlink) {
        this.mergeHyperlink = mergeHyperlink;
    }

/*
 * Row-level policies 
 */
    /**
     * @return the copyRowHeight
     */
    public boolean isCopyRowHeight() {
        return copyRowHeight;
    }

    /**
     * @param copyRowHeight the copyRowHeight to set
     */
    public void setCopyRowHeight(boolean copyRowHeight) {
        this.copyRowHeight = copyRowHeight;
    }
    
    /**
     * If condenseRows is true, a discontinuities in srcRows will be removed when copied to destination
     * For example:
     * Sheet.copyRows({Row(1), Row(2), Row(5)}, 11, policy) results in rows 1, 2, and 5
     * being copied to rows 11, 12, and 13 if condenseRows is True, or rows 11, 11, 15 if condenseRows is false
     * @return the condenseRows
     */
    public boolean isCondenseRows() {
        return condenseRows;
    }

    /**
     * @param condenseRows the condenseRows to set
     */
    public void setCondenseRows(boolean condenseRows) {
        this.condenseRows = condenseRows;
    }
    
    
/*
 * Sheet-level policies 
 */
    /**
     * @return the copyMergedRegions
     */
    public boolean isCopyMergedRegions() {
        return copyMergedRegions;
    }

    /**
     * @param copyMergedRegions the copyMergedRegions to set
     */
    public void setCopyMergedRegions(boolean copyMergedRegions) {
        this.copyMergedRegions = copyMergedRegions;
    }

}
