package org.apache.poi.xssf.model;

import org.apache.poi.xssf.usermodel.XSSFColor;

public interface Themes {
    /**
     * Convert a theme "index" (as used by fonts etc) into a color.
     * @param idx A theme "index"
     * @return The mapped XSSFColor, or null if not mapped.
     */
    XSSFColor getThemeColor(int idx);

    /**
     * If the colour is based on a theme, then inherit
     *  information (currently just colours) from it as
     *  required.
     */
    void inheritFromThemeAsRequired(XSSFColor color);
}
