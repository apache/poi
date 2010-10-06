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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.poi.util.Internal;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTbl;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTc;


public class XWPFTableCell implements IBody {

    private CTTc ctTc;
    protected List<XWPFParagraph> paragraphs = null;
    protected List<XWPFTable> tables = null;
    protected List<IBodyElement> bodyElements = null;
    protected IBody part;
    private XWPFTableRow tableRow = null;
    /**
     * If a table cell does not include at least one block-level element, then this document shall be considered corrupt
     */
    public XWPFTableCell(CTTc cell, XWPFTableRow tableRow, IBody part) {
        this.ctTc = cell;
        this.part = part;
        this.tableRow = tableRow;
        // NB: If a table cell does not include at least one block-level element, then this document shall be considered corrupt.
        if(cell.getPList().size()<1)
        	cell.addNewP();
        bodyElements = new ArrayList<IBodyElement>();
        paragraphs = new ArrayList<XWPFParagraph>();
        tables = new ArrayList<XWPFTable>();
        
		XmlCursor cursor = ctTc.newCursor();
        cursor.selectPath("./*");
        while (cursor.toNextSelection()) {
            XmlObject o = cursor.getObject();
            if (o instanceof CTP) {
            	XWPFParagraph p = new XWPFParagraph((CTP)o, this);
            	paragraphs.add(p);
            	bodyElements.add(p);
            }
            if (o instanceof CTTbl) {
            	XWPFTable t = new XWPFTable((CTTbl)o, this);
            	tables.add(t);
            	bodyElements.add(t);
            }
        }
    }


    @Internal
    public CTTc getCTTc() {
        return ctTc;
    }

    /**
     * returns an Iterator with paragraphs and tables
     * @see org.apache.poi.xwpf.usermodel.IBody#getBodyElements()
     */
    public List<IBodyElement> getBodyElements(){
      return Collections.unmodifiableList(bodyElements);
    }
    
    public void setParagraph(XWPFParagraph p) {
        if (ctTc.sizeOfPArray() == 0) {
            ctTc.addNewP();
        }
        ctTc.setPArray(0, p.getCTP());
    }

    /**
     * returns a list of paragraphs
     */
    public List<XWPFParagraph> getParagraphs(){
    		return paragraphs;
    }
    
    /**
     * Add a Paragraph to this Table Cell
     * @return The paragraph which was added
     */
    public XWPFParagraph addParagraph() {
       XWPFParagraph p = new XWPFParagraph(ctTc.addNewP(), this);
       addParagraph(p);
       return p;
    }
    
    /**
     * add a Paragraph to this TableCell
     * @param p the paragaph which has to be added
     */
    public void addParagraph(XWPFParagraph p){
    	paragraphs.add(p);
    }
    
    /**
     * removes a paragraph of this tablecell
     * @param pos
     */
    public void removeParagraph(int pos){
    	paragraphs.remove(pos);
    	ctTc.removeP(pos);
    }
    
	/**
	 * if there is a corresponding {@link XWPFParagraph} of the parameter ctTable in the paragraphList of this table
	 * the method will return this paragraph
	 * if there is no corresponding {@link XWPFParagraph} the method will return null 
	 * @param p is instance of CTP and is searching for an XWPFParagraph
	 * @return null if there is no XWPFParagraph with an corresponding CTPparagraph in the paragraphList of this table
	 * 		   XWPFParagraph with the correspondig CTP p
	 */
    public XWPFParagraph getParagraph(CTP p){
    	for (XWPFParagraph paragraph : paragraphs) {
			if(p.equals(paragraph.getCTP())){
				return paragraph;
			}
		}
    	return null;
    }	
    
    public void setText(String text) {
        CTP ctP = (ctTc.sizeOfPArray() == 0) ? ctTc.addNewP() : ctTc.getPArray(0);
        XWPFParagraph par = new XWPFParagraph(ctP, this);
        par.createRun().setText(text);
    }
    
    public XWPFTableRow getTableRow(){
    	return tableRow;
    }
    
    /**
     * add a new paragraph at position of the cursor
     * @param cursor
     * @return the inserted paragraph
     */
    public XWPFParagraph insertNewParagraph(XmlCursor cursor){
    	if(!isCursorInTableCell(cursor))
    		return null;
    		
		String uri = CTP.type.getName().getNamespaceURI();
		String localPart = "p";
		cursor.beginElement(localPart,uri);
		cursor.toParent();
		CTP p = (CTP)cursor.getObject();
		XWPFParagraph newP = new XWPFParagraph(p, this);
		XmlObject o = null;
    	while(!(o instanceof CTP)&&(cursor.toPrevSibling())){
    		o = cursor.getObject();
    	}
    	if((!(o instanceof CTP)) || (CTP)o == p){
    		paragraphs.add(0, newP);
    	}
    	else{
    		int pos = paragraphs.indexOf(getParagraph((CTP)o))+1;
    		paragraphs.add(pos,newP);
    	}
    	int i=0;
    	cursor.toCursor(p.newCursor());
		while(cursor.toPrevSibling()){
			o =cursor.getObject();
			if(o instanceof CTP || o instanceof CTTbl)
				i++;
		}
		bodyElements.add(i, newP);
    	cursor.toCursor(p.newCursor());
    	cursor.toEndToken();
    	return newP;
    }

	public XWPFTable insertNewTbl(XmlCursor cursor) {
		if(isCursorInTableCell(cursor)){
			String uri = CTTbl.type.getName().getNamespaceURI();
			String localPart = "tbl";
    		cursor.beginElement(localPart,uri);
			cursor.toParent();
			CTTbl t = (CTTbl)cursor.getObject();
			XWPFTable newT = new XWPFTable(t, this);
			cursor.removeXmlContents();
			XmlObject o = null;
			while(!(o instanceof CTTbl)&&(cursor.toPrevSibling())){
				o = cursor.getObject();
			}
			if(!(o instanceof CTTbl)){
				tables.add(0, newT);
			}
			else{
				int pos = tables.indexOf(getTable((CTTbl)o))+1;
				tables.add(pos,newT);
			}
			int i=0;
			cursor = t.newCursor();
			while(cursor.toPrevSibling()){
				o =cursor.getObject();
				if(o instanceof CTP || o instanceof CTTbl)
					i++;
			}
			bodyElements.add(i, newT);
			cursor = t.newCursor();
			cursor.toEndToken();
			return newT;
		}
		return null;
	}
	
	/**
	 * verifies that cursor is on the right position
	 */
	private boolean isCursorInTableCell(XmlCursor cursor) {
		XmlCursor verify = cursor.newCursor();
		verify.toParent();
		if(verify.getObject() == this.ctTc){
			return true;
		}
		return false;
	}

	

	/**
	 * @see org.apache.poi.xwpf.usermodel.IBody#getParagraphArray(int)
	 */
	public XWPFParagraph getParagraphArray(int pos) {
		if(pos > 0 && pos < paragraphs.size()){
			return paragraphs.get(pos);
		}
		return null;
	}




	/**
	 * get the to which the TableCell belongs 
	 * @see org.apache.poi.xwpf.usermodel.IBody#getPart()
	 */
	public IBody getPart() {
		return (IBody) tableRow.getTable().getPart();
	}


	/** 
	 * @see org.apache.poi.xwpf.usermodel.IBody#getPartType()
	 */
	public BodyType getPartType() {
		return BodyType.TABLECELL;
	}


	/**
	 * get a table by its CTTbl-Object
	 * @see org.apache.poi.xwpf.usermodel.IBody#getTable(org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTbl)
	 */
	public XWPFTable getTable(CTTbl ctTable) {
		for(int i=0; i<tables.size(); i++){
			if(getTables().get(i).getCTTbl() == ctTable) return getTables().get(i); 
		}
		return null;
	}


	/** 
	 * @see org.apache.poi.xwpf.usermodel.IBody#getTableArray(int)
	 */
	public XWPFTable getTableArray(int pos) {
		if(pos > 0 && pos < tables.size()){
			return tables.get(pos);
		}
		return null;
	}


	/** 
	 * @see org.apache.poi.xwpf.usermodel.IBody#getTables()
	 */
	public List<XWPFTable> getTables() {
		return Collections.unmodifiableList(tables);
	}


	/**
	 * inserts an existing XWPFTable to the arrays bodyElements and tables
	 * @see org.apache.poi.xwpf.usermodel.IBody#insertTable(int, org.apache.poi.xwpf.usermodel.XWPFTable)
	 */
	public void insertTable(int pos, XWPFTable table) {
		bodyElements.add(pos, table);
		int i;
    	for (i = 0; i < ctTc.getTblList().size(); i++) {
			CTTbl tbl = ctTc.getTblArray(i);
			if(tbl == table.getCTTbl()){
				break;
			}
		}
		tables.add(i, table);
	}
	
	public String getText(){
		StringBuffer text = new StringBuffer();
		for (XWPFParagraph p : paragraphs) {
			text.append(p.getText());
		}
		return text.toString();
	}


	/**
	 * get the TableCell which belongs to the TableCell
	 */
	public XWPFTableCell getTableCell(CTTc cell) {
		XmlCursor cursor = cell.newCursor();
		cursor.toParent();
		XmlObject o = cursor.getObject();
		if(!(o instanceof CTRow)){
			return null;
		}
		CTRow row = (CTRow)o;
		cursor.toParent();
		o = cursor.getObject();
		if(! (o instanceof CTTbl)){
			return null;
		}
		CTTbl tbl = (CTTbl) o;
		XWPFTable table = getTable(tbl);
		if(table == null){
			return null;
		}
		XWPFTableRow tableRow = table.getRow(row);
		if(row == null){
			return null;
		}
		return tableRow.getTableCell(cell);
	}
}// end class
