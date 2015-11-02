package org.apache.poi.ss.usermodel;

import org.apache.poi.util.Beta;

@Beta
public class CellCopyPolicy implements Cloneable {
    public static final boolean DEFAULT_COPY_CELL_VALUE_POLICY = true;
    public static final boolean DEFAULT_COPY_CELL_STYLE_POLICY = true;
    public static final boolean DEFAULT_COPY_CELL_FORMULA_POLICY = true;
    public static final boolean DEFAULT_COPY_MERGED_REGIONS_POLICY = true;
    public static final boolean DEFAULT_COPY_ROW_HEIGHT_POLICY = true;
    public static final boolean DEFAULT_CONDENSE_ROWS_POLICY = false;
    
    private boolean copyCellValue = DEFAULT_COPY_CELL_VALUE_POLICY;
    private boolean copyCellStyle = DEFAULT_COPY_CELL_STYLE_POLICY;
    private boolean copyCellFormula = DEFAULT_COPY_CELL_FORMULA_POLICY;
    private boolean copyMergedRegions = DEFAULT_COPY_MERGED_REGIONS_POLICY;
    private boolean copyRowHeight = DEFAULT_COPY_ROW_HEIGHT_POLICY;
    private boolean condenseRows = DEFAULT_CONDENSE_ROWS_POLICY;
    
    /** 
     * Default CellCopyPolicy, uses default policy
     * For custom CellCopyPolicy, use {@link #Builder} class
     */
    public CellCopyPolicy() { }
    
    // should builder be replaced with CellCopyPolicy setters that return the object
    // to allow setters to be chained together?
    // policy.setCopyCellValue(true).setCopyCellStyle(true)
    private CellCopyPolicy(Builder builder) {
        copyCellValue = builder.copyCellValue;
        copyCellStyle = builder.copyCellStyle;
        copyCellFormula = builder.copyCellFormula;
        copyMergedRegions = builder.copyMergedRegions;
        copyRowHeight = builder.copyRowHeight;
        condenseRows = builder.condenseRows;
    }
    
    public static class Builder {
        private boolean copyCellValue = DEFAULT_COPY_CELL_VALUE_POLICY;
        private boolean copyCellStyle = DEFAULT_COPY_CELL_STYLE_POLICY;
        private boolean copyCellFormula = DEFAULT_COPY_CELL_FORMULA_POLICY;
        private boolean copyMergedRegions = DEFAULT_COPY_MERGED_REGIONS_POLICY;
        private boolean copyRowHeight = DEFAULT_COPY_ROW_HEIGHT_POLICY;
        private boolean condenseRows = DEFAULT_CONDENSE_ROWS_POLICY;
        
        /**
         * Builder class for CellCopyPolicy
         */
        public Builder() {
        }
        
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
        public Builder mergedRegions(boolean copyMergedRegions) {
            this.copyMergedRegions = copyMergedRegions;
            return this;
        }
        public Builder rowHeight(boolean copyRowHeight) {
            this.copyRowHeight = copyRowHeight;
            return this;
        }
        public Builder condenseRows(boolean condenseRows) {
            this.condenseRows = condenseRows;
            return this;
        }
        public CellCopyPolicy build() {
            return new CellCopyPolicy(this);
        }
    }
    
    private Builder createBuilder() {
        final Builder builder = new Builder()
                .cellValue(copyCellValue)
                .cellStyle(copyCellStyle)
                .cellFormula(copyCellFormula)
                .mergedRegions(copyMergedRegions)
                .rowHeight(copyRowHeight)
                .condenseRows(condenseRows);
        return builder;
    }
    
    @Override
    public CellCopyPolicy clone() {
        return createBuilder().build();
    }
    
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

}
