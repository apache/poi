package org.apache.poi.ss.usermodel;

/**
 * Interface for classes providing differential style definitions, such as conditional format rules
 * and table/pivot table styles.
 * 
 * @since 3.17 beta 1
 */
public interface DifferentialStyleProvider {

    /**
     * @return - border formatting object  if defined,  <code>null</code> otherwise
     */
    BorderFormatting getBorderFormatting();

    /**
     * @return - font formatting object  if defined,  <code>null</code> otherwise
     */
    FontFormatting getFontFormatting();

    /**
     *
     * @return number format defined for this rule, or null if the cell default should be used
     */
    ExcelNumberFormat getNumberFormat();

    /**
     * @return - pattern formatting object if defined, <code>null</code> otherwise
     */
    PatternFormatting getPatternFormatting();

}
