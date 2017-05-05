package org.apache.poi.ss.usermodel;

import java.util.List;

import org.apache.poi.ss.formula.ConditionalFormattingEvaluator;
import org.apache.poi.ss.formula.EvaluationConditionalFormatRule;

/**
 * Object to hold a number format index and string, for various formatting evaluations
 */
public class ExcelNumberFormat {

    private final int idx;
    private final String format;
    
    /**
     * @param style
     * @return null if the style is null, instance from style data format values otherwise
     */
    public static ExcelNumberFormat from(CellStyle style) {
        if (style == null) return null;
        return new ExcelNumberFormat(style.getDataFormat(), style.getDataFormatString());
    }
    
    /**
    * @param cell cell to extract format from
    * @param cfEvaluator ConditionalFormattingEvaluator to use, or null if none in this context
    * @return number format from highest-priority rule with a number format, or the cell style, or null if none of the above apply/are defined
    */
   public static ExcelNumberFormat from(Cell cell, ConditionalFormattingEvaluator cfEvaluator) {
       if (cell == null) return null;
       
       ExcelNumberFormat nf = null;
       
       if (cfEvaluator != null) {
           // first one wins (priority order, per Excel help)
           List<EvaluationConditionalFormatRule> rules = cfEvaluator.getConditionalFormattingForCell(cell);
           for (EvaluationConditionalFormatRule rule : rules) {
               nf = rule.getNumberFormat();
               if (nf != null) break;
           }
       }
       if (nf == null) {
           CellStyle style = cell.getCellStyle();
           nf = ExcelNumberFormat.from(style);
       }
       return nf;
   }
   
    /**
     * Use this carefully, prefer factory methods to ensure id/format relationships are not broken or confused.
     * Left public so {@link ConditionalFormattingRule#getNumberFormat()} implementations can use it.
     * @param idx Excel number format index, either a built-in or a higher custom # mapped in the workbook style table
     * @param format Excel number format string for the index
     */
    public ExcelNumberFormat(int idx, String format) {
        this.idx = idx;
        this.format = format;
    }
    
    

    /**
     *
     * @return Excel number format index, either a built-in or a higher custom # mapped in the workbook style table
     */
    public int getIdx() {
        return idx;
    }
    
    /**
     *
     * @return Excel number format string for the index
     */
    public String getFormat() {
        return format;
    }
}
