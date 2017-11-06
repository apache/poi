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

package org.apache.poi.xssf.usermodel.helpers;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.poi.ss.formula.FormulaShifter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.helpers.RowShifter;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.xssf.model.CommentsTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFVMLDrawing;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTComment;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCommentList;

/**
 * Helper for shifting rows up or down
 * 
 * When possible, code should be implemented in the RowShifter abstract class to avoid duplication with {@link org.apache.poi.hssf.usermodel.helpers.HSSFRowShifter}
 */
public final class XSSFRowShifter extends RowShifter {
    private static final POILogger logger = POILogFactory.getLogger(XSSFRowShifter.class);

    private XSSFShiftingManager formulaShiftingManager;
    
    public XSSFRowShifter(XSSFSheet sh) {
        super(sh);
    }
    public XSSFRowShifter(Sheet sh, FormulaShifter shifter) {
        super(sh, shifter);
        formulaShiftingManager = new XSSFShiftingManager(sh, shifter);
    }
    
    // do the actual moving and also adjust comments/rowHeight
    // we need to sort it in a way so the shifting does not mess up the structures, 
    // i.e. when shifting down, start from down and go up, when shifting up, vice-versa
    public void doShiftingAndProcessComments(XSSFVMLDrawing vml, int startRow, int endRow, final int n, 
            boolean copyRowHeight, Iterator<Row> rowIterator, CommentsTable sheetComments){
        SortedMap<XSSFComment, Integer> commentsToShift = new TreeMap<>(new Comparator<XSSFComment>() {
            @Override
            public int compare(XSSFComment o1, XSSFComment o2) {
                int row1 = o1.getRow();
                int row2 = o2.getRow();
                
                if(row1 == row2) {
                    // ordering is not important when row is equal, but don't return zero to still 
                    // get multiple comments per row into the map
                    return o1.hashCode() - o2.hashCode();
                }

                // when shifting down, sort higher row-values first
                if(n > 0) {
                    return row1 < row2 ? 1 : -1;
                } else {
                    // sort lower-row values first when shifting up
                    return row1 > row2 ? 1 : -1;
                }
            }
        });
        
        for (Iterator<Row> it = rowIterator; it.hasNext() ; ) {
            XSSFRow row = (XSSFRow)it.next();
            int rownum = row.getRowNum();

            if(sheetComments != null){
                // calculate the new rownum
                int newrownum = XSSFShiftingManager.shiftedItemIndex(startRow, endRow, n, rownum);
                
                // is there a change necessary for the current row?
                if(newrownum != rownum) {
                    CTCommentList lst = sheetComments.getCTComments().getCommentList();
                    for (CTComment comment : lst.getCommentArray()) {
                        String oldRef = comment.getRef();
                        CellReference ref = new CellReference(oldRef);
                        
                        // is this comment part of the current row?
                        if(ref.getRow() == rownum) {
                            XSSFComment xssfComment = new XSSFComment(sheetComments, comment,
                                    vml == null ? null : vml.findCommentShape(rownum, ref.getCol()));
                            
                            // we should not perform the shifting right here as we would then find
                            // already shifted comments and would shift them again...
                            commentsToShift.put(xssfComment, newrownum);
                        }
                    }
                }
            }

            if(rownum < startRow || rownum > endRow) {
                continue;
            }
            if (!copyRowHeight) {
                row.setHeight((short)-1);
            }
            row.shift(n);
        }
        
        // adjust all the affected comment-structures now
        // the Map is sorted and thus provides them in the order that we need here, 
        // i.e. from down to up if shifting down, vice-versa otherwise
        for(Map.Entry<XSSFComment, Integer> entry : commentsToShift.entrySet()) {
            entry.getKey().setRow(entry.getValue());
        }
        
    }
    

    
    /**
     * Shift merged regions
     * 
     * @param startRow the row to start shifting
     * @param endRow   the row to end shifting
     * @param n        the number of rows to shift
     * @return an array of merged cell regions
     * @deprecated POI 3.15 beta 2. Use {@link #shiftMergedRegions(int, int, int)} instead.
     */
    public List<CellRangeAddress> shiftMerged(int startRow, int endRow, int n) {
        return shiftMergedRegions(startRow, endRow, n);
    }
    
    /**
        @deprecated in POI 4.0.0, use FormulaShiftingManager.updateNamedRanges() directly instead
    */
    @Deprecated
    public void updateNamedRanges(FormulaShifter shifter) {
        formulaShiftingManager.updateNamedRanges();
    }
    /**
        @deprecated in POI 4.0.0, use FormulaShiftingManager.updateFormulas() directly instead
    */ 
    @Deprecated
    public void updateFormulas(FormulaShifter shifter) {
        formulaShiftingManager.updateFormulas();
    }
    /**
        @deprecated in POI 4.0.0, use FormulaShiftingManager.updateConditionalFormatting() directly instead
     */ 
    @Deprecated
    public void updateConditionalFormatting(FormulaShifter shifter) {
        formulaShiftingManager.updateConditionalFormatting();
    }    
    /**
        @deprecated in POI 4.0.0, use FormulaShiftingManager.updateHyperlinks() directly instead
     */ 
    @Deprecated
    public void updateHyperlinks(FormulaShifter shifter) {
        formulaShiftingManager.updateHyperlinks();
    }
    public void updateRowFormulas(Row row, FormulaShifter shifter) {
    }
}
