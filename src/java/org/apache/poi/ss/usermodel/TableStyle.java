package org.apache.poi.ss.usermodel;

/**
 * Data table style definition.  Includes style elements for various table components.
 * Any number of style elements may be represented, and any cell may be styled by
 * multiple elements.  The order of elements in {@link TableStyleType} defines precedence.
 * 
 * @since 3.17 beta 1
 */
public interface TableStyle {

    /**
     * @return name (may be a builtin name)
     */
    String getName();
    
    /**
     *
     * @param type
     * @return style definition for the given type, or null if not defined in this style.
     */
    DifferentialStyleProvider getStyle(TableStyleType type);
}
