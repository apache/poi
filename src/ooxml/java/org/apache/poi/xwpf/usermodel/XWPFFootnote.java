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
import java.util.Iterator;
import java.util.List;

import org.apache.poi.POIXMLDocumentPart;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTFtnEdn;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTbl;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTc;

public class XWPFFootnote implements Iterable<XWPFParagraph>,IBody {
    private List<XWPFParagraph> paragraphs = new ArrayList<XWPFParagraph>();
    private List<XWPFTable> tables= new ArrayList<XWPFTable>();
    private List<XWPFPictureData> pictures = new ArrayList<XWPFPictureData>();
    private List<IBodyElement> bodyElements = new ArrayList<IBodyElement>();

    private CTFtnEdn ctFtnEdn;
    private XWPFFootnotes footnotes;

    public XWPFFootnote(CTFtnEdn note, XWPFFootnotes xFootnotes) {
       footnotes = xFootnotes;
       ctFtnEdn = note;
       for (CTP p : ctFtnEdn.getPList())	{
          paragraphs.add(new XWPFParagraph(p, this));
       }
    }

    public XWPFFootnote(XWPFDocument document, CTFtnEdn body) {
        for (CTP p : body.getPList())	{
            paragraphs.add(new XWPFParagraph(p, document));
        }
    }

    public List<XWPFParagraph> getParagraphs() {
        return paragraphs;
    }

    public Iterator<XWPFParagraph> iterator(){
        return paragraphs.iterator();
    }

    public List<XWPFTable> getTables() {
        return tables;
    }

    public List<XWPFPictureData> getPictures() {
        return pictures;
    }

    public List<IBodyElement> getBodyElements() {
        return bodyElements;
    }

    public CTFtnEdn getCTFtnEdn() {
       return ctFtnEdn;
    }

    public void setCTFtnEdn(CTFtnEdn footnote) {
       ctFtnEdn = footnote;
    }

    /**
     * @param position in table array
     * @return The table at position pos
     * @see org.apache.poi.xwpf.usermodel.IBody#getTableArray(int)
     */
    public XWPFTable getTableArray(int pos) {
        if(pos > 0 && pos < tables.size()){
            return tables.get(pos);
        }
        return null;
    }

    /**
     * inserts an existing XWPFTable to the arrays bodyElements and tables
     * @param pos
     * @param table
	 * @see org.apache.poi.xwpf.usermodel.IBody#insertTable(int pos, XWPFTable table)
     */
    public void insertTable(int pos, XWPFTable table) {
        bodyElements.add(pos, table);
        int i;
        for (i = 0; i < ctFtnEdn.getTblList().size(); i++) {
            CTTbl tbl = ctFtnEdn.getTblArray(i);
            if(tbl == table.getCTTbl()){
                break;
            }
        }
        tables.add(i, table);

    }

    /**
     * if there is a corresponding {@link XWPFTable} of the parameter ctTable in the tableList of this header
     * the method will return this table
     * if there is no corresponding {@link XWPFTable} the method will return null 
     * @param ctTable
	 * @see org.apache.poi.xwpf.usermodel.IBody#getTable(CTTbl ctTable)
     */
    public XWPFTable getTable(CTTbl ctTable){
        for (XWPFTable table : tables) {
            if(table==null)
                return null;
            if(table.getCTTbl().equals(ctTable))
                return table;	
        }
        return null;
    }

    /**
     * if there is a corresponding {@link XWPFParagraph} of the parameter ctTable in the paragraphList of this header or footer
     * the method will return this paragraph
     * if there is no corresponding {@link XWPFParagraph} the method will return null 
     * @param p is instance of CTP and is searching for an XWPFParagraph
     * @return null if there is no XWPFParagraph with an corresponding CTPparagraph in the paragraphList of this header or footer
     * 		   XWPFParagraph with the correspondig CTP p
	 * @see org.apache.poi.xwpf.usermodel.IBody#getParagraph(CTP p)
     */
    public XWPFParagraph getParagraph(CTP p){
        for (XWPFParagraph paragraph : paragraphs) {
            if(paragraph.getCTP().equals(p))
                return paragraph;
        }
        return null;
    }

    /**
     * Returns the paragraph that holds
     *  the text of the header or footer.
	 * @see org.apache.poi.xwpf.usermodel.IBody#getParagraphArray(int pos)
     */
    public XWPFParagraph getParagraphArray(int pos) {

        return paragraphs.get(pos);
    }

    /**
     * get the TableCell which belongs to the TableCell
     * @param cell
	 * @see org.apache.poi.xwpf.usermodel.IBody#getTableCell(CTTc cell)
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
        cursor.dispose();
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

    /**
     * verifies that cursor is on the right position
     * @param cursor
     */
    private boolean isCursorInFtn(XmlCursor cursor) {
        XmlCursor verify = cursor.newCursor();
        verify.toParent();
        if(verify.getObject() == this.ctFtnEdn){
            return true;
        }
        return false;
    }

    public POIXMLDocumentPart getOwner(){
        return footnotes;
    }

    /**
     * 
     * @param cursor
     * @return the inserted table
	 * @see org.apache.poi.xwpf.usermodel.IBody#insertNewTbl(XmlCursor cursor)
     */
    public XWPFTable insertNewTbl(XmlCursor cursor) {
        if(isCursorInFtn(cursor)){
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
     * add a new paragraph at position of the cursor
     * @param cursor
     * @return the inserted paragraph
	 * @see org.apache.poi.xwpf.usermodel.IBody#insertNewParagraph(XmlCursor cursor)
     */
    public XWPFParagraph insertNewParagraph(XmlCursor cursor){
        if(isCursorInFtn(cursor)){
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
        return null;
    }

	/**
	 * add a new table to the end of the footnote
	 * @param table
	 * @return the added XWPFTable
	 */
    public XWPFTable addNewTbl(CTTbl table) {
		CTTbl newTable = ctFtnEdn.addNewTbl();
		newTable.set(table);
		XWPFTable xTable = new XWPFTable(newTable, this);
		tables.add(xTable);
		return xTable;
    }
	
	/**
	 * add a new paragraph to the end of the footnote
	 * @param paragraph
	 * @return the added XWPFParagraph
	 */
    public XWPFParagraph addNewParagraph(CTP paragraph) {
		CTP newPara = ctFtnEdn.addNewP();
		newPara.set(paragraph);
		XWPFParagraph xPara = new XWPFParagraph(newPara, this);
		paragraphs.add(xPara);
		return xPara;
    }
	
    /**
     * @see org.apache.poi.xwpf.usermodel.IBody#getXWPFDocument()
     */
    public  XWPFDocument getXWPFDocument() {
       return footnotes.getXWPFDocument();
    }

    /**
     * returns the Part, to which the body belongs, which you need for adding relationship to other parts
     * @see org.apache.poi.xwpf.usermodel.IBody#getPart()
     */
    public POIXMLDocumentPart getPart() {
        return footnotes;
    }

    /**
     * get the PartType of the body
     * @see org.apache.poi.xwpf.usermodel.IBody#getPartType()
     */
    public BodyType getPartType() {
        return BodyType.FOOTNOTE;
    }
}
