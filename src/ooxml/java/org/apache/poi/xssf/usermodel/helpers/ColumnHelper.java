package org.apache.poi.xssf.usermodel.helpers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCol;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCols;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorksheet;

public class ColumnHelper {
	
	private List<CTCol> columns;
	
    public ColumnHelper(CTWorksheet worksheet) {
		super();
		setColumns(worksheet);
	}

	public List<CTCol> getColumns() {
    	return columns;
    }
	
	public void setColumns(CTWorksheet worksheet) {
    	columns = new ArrayList<CTCol>();
    	CTCols[] colsArray = worksheet.getColsArray();
    	for (int i = 0 ; i < colsArray.length ; i++) {
    		CTCols cols = colsArray[i];
    		CTCol[] colArray = cols.getColArray();
    		for (int y = 0 ; y < colArray.length ; y++) {
    		 	CTCol col = colArray[y];
    		 	for (long k = col.getMin() ; k <= col.getMax() ; k++) {
    		 		setColumn(columns, col, k);
    		 	}
    		}
    	}
	}

	private void setColumn(List<CTCol> columns, CTCol col, long k) {
		CTCol column = getColumn(columns, k);
		if (column == null) {
			column = CTCol.Factory.newInstance();
			column.setMin(k);
			column.setMax(k);
			setColumnAttributes(col, column);
			columns.add(column);
		}
		else {
			setColumnAttributes(col, column);
		}
	}

	private void setColumnAttributes(CTCol col, CTCol column) {
		if (col.getWidth() > 0) {
			column.setWidth(col.getWidth());
		}
		// TODO set all col attributes
	}
    
    public CTCol getColumn(List<CTCol> columns, long k) {
    	for (Iterator<CTCol> it = columns.iterator() ; it.hasNext() ; ) {
    		CTCol column = it.next();
    		if (column.getMin() == k) {
    			return column;
    		}
    	}
    	return null;
    }
    
    public CTCol getColumn(long index) {
    	for (Iterator<CTCol> it = columns.iterator() ; it.hasNext() ; ) {
    		CTCol column = it.next();
    		if (getColumnIndex(column) == index) {
    			return column;
    		}
    	}
    	return null;
    }
    
    public long getColumnIndex(CTCol column) {
    	if (column.getMin() == column.getMax()) {
    		return column.getMin();
    	}
    	return -1;
    }
    
    public CTCol createColumn(long index) {
    	CTCol column = CTCol.Factory.newInstance();
    	setIndex(column, index);
    	columns.add(column);
    	return column;
    }

	private void setIndex(CTCol column, long index) {
		column.setMin(index);
		column.setMax(index);
	}

}
