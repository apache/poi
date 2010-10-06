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
package org.apache.poi.xwpf.usermodel;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.util.Internal;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHeight;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTrPr;


/**
 * @author gisellabronzetti
 */
public class XWPFTableRow {

    private CTRow ctRow;
    private XWPFTable table;
    private List<XWPFTableCell> tableCells;

    public XWPFTableRow(CTRow row, XWPFTable table) {
    	this.table = table;
        this.ctRow = row;
        getTableCells();
    }

    @Internal
    public CTRow getCtRow() {
        return ctRow;
    }

    /**
     * create a new XWPFTableCell and add it to the tableCell-list of this tableRow
     * @return the newly created XWPFTableCell
     */
    public XWPFTableCell createCell() {
        XWPFTableCell tableCell = new XWPFTableCell(ctRow.addNewTc(), this, table.getPart());
        tableCells.add(tableCell);
        return tableCell;
    }

    public XWPFTableCell getCell(int pos) {
        if (pos >= 0 && pos < ctRow.sizeOfTcArray()) {
        	return getTableCells().get(pos);
        }
        return null;
    }
    
    /**
     * adds a new TableCell at the end of this tableRow
     */
    public XWPFTableCell addNewTableCell(){
    	CTTc cell = ctRow.addNewTc();
    	XWPFTableCell tableCell = new XWPFTableCell(cell, this, table.getPart());
    	tableCells.add(tableCell);
    	return tableCell;
    }

    /**
     * This element specifies the height of the current table row within the
     * current table. This height shall be used to determine the resulting
     * height of the table row, which may be absolute or relative (depending on
     * its attribute values). If omitted, then the table row shall automatically
     * resize its height to the height required by its contents (the equivalent
     * of an hRule value of auto).
     *
     * @param height
     */
    public void setHeight(int height) {
        CTTrPr properties = getTrPr();
        CTHeight h = properties.sizeOfTrHeightArray() == 0 ? properties.addNewTrHeight() : properties.getTrHeightArray(0);
        h.setVal(new BigInteger("" + height));
    }

    /**
     * This element specifies the height of the current table row within the
     * current table. This height shall be used to determine the resulting
     * height of the table row, which may be absolute or relative (depending on
     * its attribute values). If omitted, then the table row shall automatically
     * resize its height to the height required by its contents (the equivalent
     * of an hRule value of auto).
     *
     * @return height
     */
    public int getHeight() {
        CTTrPr properties = getTrPr();
        return properties.sizeOfTrHeightArray() == 0 ? 0 : properties.getTrHeightArray(0).getVal().intValue();
    }


    private CTTrPr getTrPr() {
        return (ctRow.isSetTrPr()) ? ctRow.getTrPr() : ctRow.addNewTrPr();
    }
    
    public XWPFTable getTable(){
    	return table;
    }
    
    /**
     * create and return a list of all XWPFTableCell
     * who belongs to this row
     * @return a list of {@link XWPFTableCell} 
     */
    public List<XWPFTableCell> getTableCells(){
    	if(tableCells == null){
    		List<XWPFTableCell> cells = new ArrayList<XWPFTableCell>();
    		for (CTTc tableCell : ctRow.getTcList()) {
    			cells.add(new XWPFTableCell(tableCell, this, table.getPart()));
    		}
    		this.tableCells = cells;
    	}
    	return tableCells;
    }

	/**
	 * returns the XWPFTableCell which belongs to the CTTC cell
	 * if there is no XWPFTableCell which belongs to the parameter CTTc cell null will be returned
	 */
	public XWPFTableCell getTableCell(CTTc cell) {
		for(int i=0; i<tableCells.size(); i++){
			if(tableCells.get(i).getCTTc() == cell) return tableCells.get(i); 
		}
		return null;
	}

}// end class
