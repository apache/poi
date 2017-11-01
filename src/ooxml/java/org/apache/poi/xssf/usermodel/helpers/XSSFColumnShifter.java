package org.apache.poi.xssf.usermodel.helpers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.poi.ss.ITestDataProvider;
import org.apache.poi.ss.formula.FormulaShifter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.helpers.ColumnShifter;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.xssf.model.CommentsTable;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFVMLDrawing;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTComment;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCommentList;

public class XSSFColumnShifter extends ColumnShifter{
	
    private static final POILogger logger = POILogFactory.getLogger(XSSFRowShifter.class);
    
    private int firstShiftColumnIndex; 
    private int lastShiftColumnIndex; 
    private int shiftStep;
    
    private XSSFShiftingManager formulaShiftingManager;

   
    public XSSFColumnShifter(Sheet sh, FormulaShifter shifter) {
        super(sh, shifter);
        formulaShiftingManager = new XSSFShiftingManager(sh, shifter);
    }

	public void shiftColumns(int firstShiftColumnIndex, int lastShiftColumnIndex, int step){
		this.firstShiftColumnIndex = firstShiftColumnIndex;
		this.lastShiftColumnIndex = lastShiftColumnIndex;
		this.shiftStep = step;
		if(shiftStep > 0)
			shiftColumnsRight();
		else if(shiftStep < 0)
			shiftColumnsLeft();
//	      formulaShiftingManager.updateFormulas();
	}
	/**
	 * Inserts shiftStep empty columns at firstShiftColumnIndex-th position, and shifts rest columns to the right 
	 * (see constructor for parameters)
	 */

	private void shiftColumnsRight(){
		for(int rowNo = 0; rowNo <= shiftingSheet.getLastRowNum(); rowNo++)
		{	
			Row row = shiftingSheet.getRow(rowNo);
			if(row == null)
				continue;
		    for (int columnIndex = lastShiftColumnIndex; columnIndex >= firstShiftColumnIndex; columnIndex--){ // process cells backwards, because of shifting 
		    	XSSFCell oldCell = (XSSFCell)row.getCell(columnIndex);
		    	Cell newCell = null;
		    	if(oldCell == null){
		    		newCell = row.getCell(columnIndex + shiftStep);
		    		newCell = null;
		    		continue;
		    	}
		    	else {
		    		newCell = row.createCell(columnIndex + shiftStep, oldCell.getCellTypeEnum());
		    		cloneCellValue(oldCell,newCell);
		    		if(columnIndex <= firstShiftColumnIndex + shiftStep - 1){ // clear existing cells on place of insertion
		    			oldCell.setCellValue("");
		    			oldCell.setCellType(CellType.STRING);
		    		}
		    	}
		    }
		}
	}
	private void shiftColumnsLeft(){
		for(int rowNo = 0; rowNo <= shiftingSheet.getLastRowNum(); rowNo++)
		{	
		    XSSFRow row = (XSSFRow)shiftingSheet.getRow(rowNo);
			if(row == null)
				continue;
		    for (int columnIndex = 0; columnIndex < row.getLastCellNum(); columnIndex++){ 
		        XSSFCell oldCell = (XSSFCell)row.getCell(columnIndex);
		    	if(columnIndex >= firstShiftColumnIndex + shiftStep && columnIndex < row.getLastCellNum() - shiftStep){ // shift existing cell 
			    	org.apache.poi.ss.usermodel.Cell newCell = null;
		    		newCell = row.getCell(columnIndex - shiftStep);
		    		if(oldCell != null){
			    		if(newCell != null){
			    			oldCell.setCellType(newCell.getCellType());
			    			cloneCellValue(newCell, oldCell);
			    		}
			    		else {
			    			oldCell.setCellType(CellType.STRING);
			    			oldCell.setCellValue("");
			    		}
		    		}
		    		else {
		    			oldCell = row.createCell(columnIndex);
			    		if(newCell != null){
			    			oldCell.setCellType(newCell.getCellType());
			    			cloneCellValue(newCell, oldCell);
			    		}
			    		else {
			    			oldCell.setCellType(CellType.STRING);
			    			oldCell.setCellValue("");
			    		}
		    		}
		    	}
    		}
		}
	}
	
    public void shiftComments(XSSFVMLDrawing vml, int startColumnIndex, int endColumnIndex, final int n, CommentsTable sheetComments){
        SortedMap<XSSFComment, Integer> commentsToShift = new TreeMap<XSSFComment, Integer>(new Comparator<XSSFComment>() {
            @Override
            public int compare(XSSFComment o1, XSSFComment o2) {
                int column1 = o1.getColumn();
                int column2 = o2.getColumn();
                
                if(column1 == column2) {
                    // ordering is not important when column is equal, but don't return zero to still 
                    // get multiple comments per column into the map
                    return o1.hashCode() - o2.hashCode();
                }

                // when shifting down, sort higher column-values first
                if(n > 0) {
                    return column1 < column2 ? 1 : -1;
                } else {
                    // sort lower-column values first when shifting up
                    return column1 > column2 ? 1 : -1;
                }
            }
        });
        
        if(sheetComments != null){
            CTCommentList lst = sheetComments.getCTComments().getCommentList();
            for (CTComment comment : lst.getCommentArray()) {
                String oldRef = comment.getRef();
                CellReference ref = new CellReference(oldRef);
                
                int newColumnIndex = XSSFShiftingManager.shiftedItemIndex(startColumnIndex, endColumnIndex, n, ref.getCol());
                
                // is there a change necessary for the current row?
                if(newColumnIndex != ref.getCol()) {
                    XSSFComment xssfComment = new XSSFComment(sheetComments, comment,
                            vml == null ? null : vml.findCommentShape(ref.getRow(), ref.getCol()));
                    commentsToShift.put(xssfComment, newColumnIndex);
                }
            }
	        for(Map.Entry<XSSFComment, Integer> entry : commentsToShift.entrySet()) 
	            entry.getKey().setColumn(entry.getValue());
        }
    	
    }

}
