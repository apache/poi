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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.POIXMLException;
import org.apache.poi.POIXMLRelation;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.TargetMode;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Internal;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHdrFtr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTbl;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTc;

/**
 * Parent of XWPF headers and footers
 */
public abstract class XWPFHeaderFooter extends POIXMLDocumentPart implements IBody {
    List<XWPFParagraph> paragraphs = new ArrayList<XWPFParagraph>(1);
    List<XWPFTable> tables= new ArrayList<XWPFTable>(1);
    List<XWPFPictureData> pictures = new ArrayList<XWPFPictureData>();
    List<IBodyElement> bodyElements = new ArrayList<IBodyElement>(1);

    CTHdrFtr headerFooter;
    XWPFDocument document;

    XWPFHeaderFooter(XWPFDocument doc, CTHdrFtr hdrFtr){
        if (doc==null) {
            throw new NullPointerException();
        }

        document = doc;
        headerFooter = hdrFtr;
        readHdrFtr();
    }

    protected XWPFHeaderFooter() {
        headerFooter = CTHdrFtr.Factory.newInstance();
        readHdrFtr();
    }

    public XWPFHeaderFooter(POIXMLDocumentPart parent, PackagePart part, PackageRelationship rel) throws IOException {
        super(parent, part, rel);
        this.document = (XWPFDocument)getParent();

        if (this.document==null) {
            throw new NullPointerException();
        }
    }

    @Override
    protected void onDocumentRead() throws IOException {
        for (POIXMLDocumentPart poixmlDocumentPart : getRelations()){
            if(poixmlDocumentPart instanceof XWPFPictureData){
                XWPFPictureData xwpfPicData = (XWPFPictureData) poixmlDocumentPart;
                pictures.add(xwpfPicData);
                document.registerPackagePictureData(xwpfPicData);
            }
        }
    }

    @Internal
    public CTHdrFtr _getHdrFtr() {
        return headerFooter;
    }

    public List<IBodyElement> getBodyElements(){
        return Collections.unmodifiableList(bodyElements);
    }

    /**
     * Returns the paragraph(s) that holds
     *  the text of the header or footer.
     * Normally there is only the one paragraph, but
     *  there could be more in certain cases, or 
     *  a table.
     */
    public List<XWPFParagraph> getParagraphs() {
        return Collections.unmodifiableList(paragraphs);
    }


    /**
     * Return the table(s) that holds the text
     *  of the header or footer, for complex cases
     *  where a paragraph isn't used.
     * Normally there's just one paragraph, but some
     *  complex headers/footers have a table or two
     *  in addition. 
     */
    public List<XWPFTable> getTables()throws ArrayIndexOutOfBoundsException {
        return Collections.unmodifiableList(tables);
    }



    /**
     * Returns the textual content of the header/footer,
     *  by flattening out the text of its paragraph(s)
     */
    public String getText() {
        StringBuffer t = new StringBuffer();

        for(int i=0; i<paragraphs.size(); i++) {
            if(! paragraphs.get(i).isEmpty()) {
                String text = paragraphs.get(i).getText();
                if(text != null && text.length() > 0) {
                    t.append(text);
                    t.append('\n');
                }
            }
        }

        List<XWPFTable> tables = getTables();
        for(int i=0; i<tables.size(); i++) {
            String text = tables.get(i).getText();
            if(text != null && text.length() > 0) {
                t.append(text);
                t.append('\n');
            }
        }

        return t.toString(); 
    }

    /**
     * set a new headerFooter
     */
    public void setHeaderFooter(CTHdrFtr headerFooter){
        this.headerFooter = headerFooter;
        readHdrFtr();
    }

    /**
     * if there is a corresponding {@link XWPFTable} of the parameter ctTable in the tableList of this header
     * the method will return this table
     * if there is no corresponding {@link XWPFTable} the method will return null 
     * @param ctTable
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
     */
    public XWPFParagraph getParagraphArray(int pos) {

        return paragraphs.get(pos);
    }

    /**
     * get a List of all Paragraphs
     * @return a list of {@link XWPFParagraph} 
     */
    public List<XWPFParagraph> getListParagraph(){
        return paragraphs;
    }

    public List<XWPFPictureData> getAllPictures() {
        return Collections.unmodifiableList(pictures);
    }

    /**
     * get all Pictures in this package
     * @return all Pictures in this package
     */
    public List<XWPFPictureData> getAllPackagePictures(){
        return document.getAllPackagePictures();

    }

    /**
     * Adds a picture to the document.
     *
     * @param is                The stream to read image from
     * @param format            The format of the picture.
     *
     * @return the index to this picture (0 based), the added picture can be obtained from {@link #getAllPictures()} .
     * @throws InvalidFormatException 
     */
    public String addPictureData(byte[] pictureData,int format) throws InvalidFormatException
    {
        XWPFPictureData xwpfPicData = document.findPackagePictureData(pictureData, format);
        POIXMLRelation relDesc = XWPFPictureData.RELATIONS[format];

        if (xwpfPicData == null)
        {
            /* Part doesn't exist, create a new one */
            int idx = document.getNextPicNameNumber(format);
            xwpfPicData = (XWPFPictureData) createRelationship(relDesc, XWPFFactory.getInstance(),idx);
            /* write bytes to new part */
            PackagePart picDataPart = xwpfPicData.getPackagePart();
            OutputStream out = null;
            try {
                out = picDataPart.getOutputStream();
                out.write(pictureData);
            } catch (IOException e) {
                throw new POIXMLException(e);
            } finally {
                try {
                    out.close();
                } catch (IOException e) {
                    // ignore
                }
            }
            
            document.registerPackagePictureData(xwpfPicData);
            pictures.add(xwpfPicData);
            return getRelationId(xwpfPicData);
        }
        else if (!getRelations().contains(xwpfPicData))
        {
            /*
             * Part already existed, but was not related so far. Create
             * relationship to the already existing part and update
             * POIXMLDocumentPart data.
             */
            PackagePart picDataPart = xwpfPicData.getPackagePart();
            // TODO add support for TargetMode.EXTERNAL relations.
            TargetMode targetMode = TargetMode.INTERNAL;
            PackagePartName partName = picDataPart.getPartName();
            String relation = relDesc.getRelation();
            PackageRelationship relShip = getPackagePart().addRelationship(partName,targetMode,relation);
            String id = relShip.getId();
            addRelation(id,xwpfPicData);
            pictures.add(xwpfPicData);
            return id;
        }
        else 
        {
            /* Part already existed, get relation id and return it */
            return getRelationId(xwpfPicData);
        }
    }

    /**
     * Adds a picture to the document.
     *
     * @param is                The stream to read image from
     * @param format            The format of the picture.
     *
     * @return the index to this picture (0 based), the added picture can be obtained from {@link #getAllPictures()} .
     * @throws InvalidFormatException 
     * @throws IOException 
     */
    public String addPictureData(InputStream is, int format) throws InvalidFormatException,IOException {
        byte[] data = IOUtils.toByteArray(is);
        return addPictureData(data,format);
    }

    /**
     * returns the PictureData by blipID
     * @param blipID
     * @return XWPFPictureData of a specificID
     * @throws Exception 
     */
    public XWPFPictureData getPictureDataByID(String blipID) {
        POIXMLDocumentPart relatedPart = getRelationById(blipID);
        if (relatedPart != null && relatedPart instanceof XWPFPictureData) {
            return (XWPFPictureData) relatedPart;
        }
        return null;	    	
    }

    /**
     * add a new paragraph at position of the cursor
     * @param cursor
     * @return the inserted paragraph
     */
    public XWPFParagraph insertNewParagraph(XmlCursor cursor){
        if(isCursorInHdrF(cursor)){
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
     * 
     * @param cursor
     * @return the inserted table
     */
    public XWPFTable insertNewTbl(XmlCursor cursor) {
        if(isCursorInHdrF(cursor)){
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
     * @param cursor
     */
    private boolean isCursorInHdrF(XmlCursor cursor) {
        XmlCursor verify = cursor.newCursor();
        verify.toParent();
        if(verify.getObject() == this.headerFooter){
            return true;
        }
        return false;
    }


    public POIXMLDocumentPart getOwner(){
        return this;
    }

    /**
     * Returns the table at position pos
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
     */
    public void insertTable(int pos, XWPFTable table) {
        bodyElements.add(pos, table);
        int i;
        for (i = 0; i < headerFooter.getTblList().size(); i++) {
            CTTbl tbl = headerFooter.getTblArray(i);
            if(tbl == table.getCTTbl()){
                break;
            }
        }
        tables.add(i, table);

    }

    public void readHdrFtr(){
        bodyElements = new ArrayList<IBodyElement>();
        paragraphs = new ArrayList<XWPFParagraph>();
        tables= new ArrayList<XWPFTable>();
        // parse the document with cursor and add
        // the XmlObject to its lists
        XmlCursor cursor = headerFooter.newCursor();
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
        cursor.dispose();
    }

    /**
     * get the TableCell which belongs to the TableCell
     * @param cell
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

    public XWPFDocument getXWPFDocument() {
        if (document!=null) {
            return document;
        } else {
            return (XWPFDocument)getParent();
        }
    }

    /**
     * returns the Part, to which the body belongs, which you need for adding relationship to other parts
     * @see org.apache.poi.xwpf.usermodel.IBody#getPart()
     */
    public POIXMLDocumentPart getPart() {
        return this;
    }
}//end class
