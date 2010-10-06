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
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTbl;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblBorders;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblWidth;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth;

/**
 * Sketch of XWPFTable class. Only table's text is being hold.
 * <p/>
 * Specifies the contents of a table present in the document. A table is a set
 * of paragraphs (and other block-level content) arranged in rows and columns.
 *
 * @author Yury Batrakov (batrakov at gmail.com)
 */
public class XWPFTable implements IBodyElement{

    protected StringBuffer text = new StringBuffer();
    private CTTbl ctTbl;
    protected List<XWPFTableRow> tableRows;
    protected List<String> styleIDs;
    protected IBody part;

    public XWPFTable(CTTbl table, IBody part, int row, int col) {
        this(table, part);
        for (int i = 0; i < row; i++) {
            XWPFTableRow tabRow = (getRow(i) == null) ? createRow() : getRow(i);
            tableRows.add(tabRow);
            for (int k = 0; k < col; k++) {
                XWPFTableCell tabCell = (tabRow.getCell(k) == null) ? tabRow
                        .createCell() : null;
            }
        }
    }


    public XWPFTable(CTTbl table, IBody part){
    	this.part = part;
        this.ctTbl = table;
     
        tableRows = new ArrayList<XWPFTableRow>();

        // is an empty table: I add one row and one column as default
        if (table.sizeOfTrArray() == 0)
            createEmptyTable(table);

        for (CTRow row : table.getTrList()) {
            StringBuffer rowText = new StringBuffer();
            XWPFTableRow tabRow = new XWPFTableRow(row, this);
            tableRows.add(tabRow);
            for (CTTc cell : row.getTcList()) {
                for (CTP ctp : cell.getPList()) {
                    XWPFParagraph p = new XWPFParagraph(ctp, part);
                    if (rowText.length() > 0) {
                        rowText.append('\t');
                    }
                    rowText.append(p.getText());
                }
            }
            if (rowText.length() > 0) {
                this.text.append(rowText);
                this.text.append('\n');
            }
        }
    }

    private void createEmptyTable(CTTbl table) {
        // MINIMUM ELEMENTS FOR A TABLE
        table.addNewTr().addNewTc().addNewP();

        CTTblPr tblpro = table.addNewTblPr();
        tblpro.addNewTblW().setW(new BigInteger("0"));
        tblpro.getTblW().setType(STTblWidth.AUTO);

        // layout
        // tblpro.addNewTblLayout().setType(STTblLayoutType.AUTOFIT);

        // borders
        CTTblBorders borders = tblpro.addNewTblBorders();
        borders.addNewBottom().setVal(STBorder.SINGLE);
        borders.addNewInsideH().setVal(STBorder.SINGLE);
        borders.addNewInsideV().setVal(STBorder.SINGLE);
        borders.addNewLeft().setVal(STBorder.SINGLE);
        borders.addNewRight().setVal(STBorder.SINGLE);
        borders.addNewTop().setVal(STBorder.SINGLE);

        /*
       * CTTblGrid tblgrid=table.addNewTblGrid();
       * tblgrid.addNewGridCol().setW(new BigInteger("2000"));
       */
		getRows();
    }

    /**
     * @return ctTbl object
     */
    @Internal
    public CTTbl getCTTbl() {
        return ctTbl;
    }

    /**
     * @return text
     */
    public String getText() {
        return text.toString();
    }


    public void addNewRowBetween(int start, int end) {
        // TODO
    }


    /**
     * add a new column for each row in this table
     */
    public void addNewCol() {
        if (ctTbl.sizeOfTrArray() == 0) createRow();
        for (int i = 0; i < ctTbl.sizeOfTrArray(); i++) {
            XWPFTableRow tabRow = new XWPFTableRow(ctTbl.getTrArray(i), this);
            tabRow.createCell();
        }
    }

    /**
     * create a new XWPFTableRow object with as many cells as the number of columns defined in that moment
     *
     * @return tableRow
     */
    public XWPFTableRow createRow() {
        int sizeCol = ctTbl.sizeOfTrArray() > 0 ? ctTbl.getTrArray(0)
                .sizeOfTcArray() : 0;
        XWPFTableRow tabRow = new XWPFTableRow(ctTbl.addNewTr(), this);
        addColumn(tabRow, sizeCol);
        return tabRow;
    }

    /**
     * @param pos - index of the row
     * @return the row at the position specified or null if no rows is defined or if the position is greather than the max size of rows array
     */
    public XWPFTableRow getRow(int pos) {
        if (pos >= 0 && pos < ctTbl.sizeOfTrArray()) {
            //return new XWPFTableRow(ctTbl.getTrArray(pos));
        	return getRows().get(pos);
        }
        return null;
    }


    /**
     * @param width
     */
    public void setWidth(int width) {
        CTTblPr tblPr = getTrPr();
        CTTblWidth tblWidth = tblPr.isSetTblW() ? tblPr.getTblW() : tblPr
                .addNewTblW();
        tblWidth.setW(new BigInteger("" + width));
    }

    /**
     * @return width value
     */
    public int getWidth() {
        CTTblPr tblPr = getTrPr();
        return tblPr.isSetTblW() ? tblPr.getTblW().getW().intValue() : -1;
    }

    /**
     * @return number of rows in table
     */
    public int getNumberOfRows() {
        return ctTbl.sizeOfTrArray();
    }

    private CTTblPr getTrPr() {
        return (ctTbl.getTblPr() != null) ? ctTbl.getTblPr() : ctTbl
                .addNewTblPr();
    }

    private void addColumn(XWPFTableRow tabRow, int sizeCol) {
        if (sizeCol > 0) {
            for (int i = 0; i < sizeCol; i++) {
                tabRow.createCell();
            }
        }
    }
    
    /**
     * get the StyleID of the table
     * @return	style-ID of the table
     */
    public String getStyleID(){
    	return ctTbl.getTblPr().getTblStyle().getVal();
    }
    
    /**
     * add a new Row to the table
     * 
     * @param row	the row which should be added
     */
    public void addRow(XWPFTableRow row){
    	ctTbl.addNewTr();
    	ctTbl.setTrArray(getNumberOfRows()-1, row.getCtRow());
    	tableRows.add(row);
    }
    
    /**
     * add a new Row to the table
     * at position pos
     * @param row	the row which should be added
     */
    public boolean addRow(XWPFTableRow row, int pos){
    	if(pos >= 0 && pos <= tableRows.size()){
    		ctTbl.insertNewTr(pos);
    		ctTbl.setTrArray(pos,row.getCtRow());
    		tableRows.add(pos, row);
    		return true;
    	}
    	return false;
    }
    
    /**
     * inserts a new tablerow 
     * @param pos
     * @return  the inserted row
     */
    public XWPFTableRow insertNewTableRow(int pos){
    	if(pos >= 0 && pos <= tableRows.size()){
    		CTRow row = ctTbl.insertNewTr(pos);
    		XWPFTableRow tableRow = new XWPFTableRow(row, this);
    		tableRows.add(pos, tableRow);
    		return tableRow;
    	}
    	return null;
    }
    
    
    /**
     * Remove a row at position pos from the table
     * @param pos	position the Row in the Table
     */
    public boolean removeRow(int pos) throws IndexOutOfBoundsException {
    	if(pos > 0 && pos < tableRows.size()){
    		ctTbl.removeTr(pos);
    		tableRows.remove(pos);
    		return true;
    	}
    	return false;
    }
	
    public List<XWPFTableRow> getRows() {
        return tableRows;
    }


	/**
	 * returns the type of the BodyElement Table
	 * @see org.apache.poi.xwpf.usermodel.IBodyElement#getElementType()
	 */
	public BodyElementType getElementType() {
		return BodyElementType.TABLE;
	}


	/**
	 * returns the part of the bodyElement
	 * @see org.apache.poi.xwpf.usermodel.IBody#getPart()
	 */
	public IBody getPart() {
		if(part != null){
			return part.getPart();
		}
		return null;
	}


	/**
	 * returns the partType of the bodyPart which owns the bodyElement
	 * @see org.apache.poi.xwpf.usermodel.IBody#getPartType()
	 */
	public BodyType getPartType() {
		return ((IBody)part).getPartType();
	}

	/**
	 * returns the XWPFRow which belongs to the CTRow row
	 * if this row is not existing in the table null will be returned
	 */
	public XWPFTableRow getRow(CTRow row) {
		for(int i=0; i<getRows().size(); i++){
			if(getRows().get(i).getCtRow()== row) return getRow(i); 
		}
		return null;
	}
}// end class
