package org.apache.poi.ss.usermodel;

/**
 * style information for a specific table instance, referencing the document style
 * and indicating which optional portions of the style to apply.
 * 
 * @since 3.17 beta 1
 */
public interface TableStyleInfo {

    /**
     * @return true if alternating column styles should be applied
     */
    boolean isShowColumnStripes();
    
    /**
     * @return true if alternating row styles should be applied
     */
    boolean isShowRowStripes();
    
    /**
     * @return true if the distinct first column style should be applied
     */
    boolean isShowFirstColumn();
    
    /**
     * @return true if the distinct last column style should be applied
     */
    boolean isShowLastColumn();
    
    /**
     * @return the name of the style (may reference a built-in style)
     */
    String getName();
    
    /**
     * @return style definition
     */
    TableStyle getStyle();
}
