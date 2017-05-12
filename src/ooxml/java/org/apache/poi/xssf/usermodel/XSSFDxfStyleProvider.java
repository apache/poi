package org.apache.poi.xssf.usermodel;

import org.apache.poi.ss.usermodel.BorderFormatting;
import org.apache.poi.ss.usermodel.DifferentialStyleProvider;
import org.apache.poi.ss.usermodel.ExcelNumberFormat;
import org.apache.poi.ss.usermodel.FontFormatting;
import org.apache.poi.ss.usermodel.PatternFormatting;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDxf;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTNumFmt;

/**
 * Style based on a dxf record - e.g. table style element or conditional formatting rule
 */
public class XSSFDxfStyleProvider implements DifferentialStyleProvider {
    
    private final BorderFormatting border;
    private final FontFormatting font;
    private final ExcelNumberFormat number;
    private final PatternFormatting fill;
    
    /**
     * @param dxf
     */
    public XSSFDxfStyleProvider(CTDxf dxf) {
        if (dxf == null) {
            border = null;
            font = null;
            number = null;
            fill = null;
        } else {
            border = dxf.isSetBorder() ? new XSSFBorderFormatting(dxf.getBorder()) : null; 
            font = dxf.isSetFont() ? new XSSFFontFormatting(dxf.getFont()) : null; 
            if (dxf.isSetNumFmt()) {
                CTNumFmt numFmt = dxf.getNumFmt();
                number = new ExcelNumberFormat((int) numFmt.getNumFmtId(), numFmt.getFormatCode());
            } else {
                number = null;
            }
            fill = dxf.isSetFill() ? new XSSFPatternFormatting(dxf.getFill()) : null; 
        }
    }

    public BorderFormatting getBorderFormatting() {
        return border;
    }

    public FontFormatting getFontFormatting() {
        return font;
    }

    public ExcelNumberFormat getNumberFormat() {
        return number;
    }

    public PatternFormatting getPatternFormatting() {
        return fill;
    }

}
