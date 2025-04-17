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

import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * Sketch of XWPF comment class
 */
public class XWPFComment implements IBody {

    protected CTComment ctComment;
    protected XWPFComments comments;
    protected XWPFDocument document;
    private List<XWPFParagraph> paragraphs = new ArrayList<>();
    private List<XWPFTable> tables = new ArrayList<>();
    private List<IBodyElement> bodyElements = new ArrayList<>();
    private List<XWPFSDTBlock> sdtBlocks = new ArrayList<>();

    public XWPFComment(CTComment ctComment, XWPFComments comments) {
        this.comments = comments;
        this.ctComment = ctComment;
        this.document = comments.getXWPFDocument();
        init();
    }

    protected void init() {
        try (XmlCursor cursor = ctComment.newCursor()) {
            cursor.selectPath("./*");
            while (cursor.toNextSelection()) {
                XmlObject o = cursor.getObject();
                if (o instanceof CTP) {
                    XWPFParagraph p = new XWPFParagraph((CTP) o, this);
                    bodyElements.add(p);
                    paragraphs.add(p);
                } else if (o instanceof CTTbl) {
                    XWPFTable t = new XWPFTable((CTTbl) o, this);
                    bodyElements.add(t);
                    tables.add(t);
                } else if (o instanceof CTSdtBlock) {
                    XWPFSDTBlock c = new XWPFSDTBlock((CTSdtBlock) o, this);
                    bodyElements.add(c);
                    sdtBlocks.add(c);
                }

            }
        }
    }

    /**
     * Get the Part to which the comment belongs, which you need for adding
     * relationships to other parts
     *
     * @return {@link POIXMLDocumentPart} that contains the comment.
     * @see org.apache.poi.xwpf.usermodel.IBody#getPart()
     */
    @Override
    public POIXMLDocumentPart getPart() {
        return comments;
    }

    /**
     * Get the part type {@link BodyType} of the comment.
     *
     * @return The {@link BodyType} value.
     * @see org.apache.poi.xwpf.usermodel.IBody#getPartType()
     */
    @Override
    public BodyType getPartType() {
        return BodyType.COMMENT;
    }

    /**
     * Gets the body elements ({@link IBodyElement}) of the comment.
     *
     * @return List of body elements.
     */
    @Override
    public List<IBodyElement> getBodyElements() {
        return bodyElements;
    }

    /**
     * Returns the paragraph(s) that holds the text of the comment.
     */
    @Override
    public List<XWPFParagraph> getParagraphs() {
        return paragraphs;
    }

    /**
     * Get the list of {@link XWPFTable}s in the comment.
     *
     * @return List of tables
     */
    @Override
    public List<XWPFTable> getTables() {
        return tables;
    }

    @Override
    public List<XWPFSDTBlock> getSdtBlocks() {
        return sdtBlocks;
    }

    @Override
    public XWPFParagraph getParagraph(CTP p) {
        for (XWPFParagraph paragraph : paragraphs) {
            if (paragraph.getCTP().equals(p))
                return paragraph;
        }
        return null;
    }

    @Override
    public XWPFTable getTable(CTTbl ctTable) {
        for (XWPFTable table : tables) {
            if (table == null)
                return null;
            if (table.getCTTbl().equals(ctTable))
                return table;
        }
        return null;
    }

    @Override
    public XWPFSDTBlock getSdtBlock(CTSdtBlock ctSdtBlock) {
        for (int i = 0; i < sdtBlocks.size(); i++) {
            if (getSdtBlocks().get(i).getCtSdtBlock() == ctSdtBlock) {
                return getSdtBlocks().get(i);
            }
        }
        return null;
    }

    @Override
    public XWPFParagraph getParagraphArray(int pos) {
        if (pos >= 0 && pos < paragraphs.size()) {
            return paragraphs.get(pos);
        }
        return null;
    }

    @Override
    public XWPFTable getTableArray(int pos) {
        if (pos >= 0 && pos < tables.size()) {
            return tables.get(pos);
        }
        return null;
    }

    @Override
    public XWPFParagraph insertNewParagraph(XmlCursor cursor) {
        if (isCursorInCmt(cursor)) {
            String uri = CTP.type.getName().getNamespaceURI();
            String localPart = "p";
            cursor.beginElement(localPart, uri);
            cursor.toParent();
            CTP p = (CTP) cursor.getObject();
            XWPFParagraph newP = new XWPFParagraph(p, this);
            XmlObject o = null;
            while (!(o instanceof CTP) && (cursor.toPrevSibling())) {
                o = cursor.getObject();
            }
            if ((!(o instanceof CTP)) || o == p) {
                paragraphs.add(0, newP);
            } else {
                int pos = paragraphs.indexOf(getParagraph((CTP) o)) + 1;
                paragraphs.add(pos, newP);
            }
            int i = 0;
            try (XmlCursor p2 = p.newCursor()) {
                cursor.toCursor(p2);
            }
            while (cursor.toPrevSibling()) {
                o = cursor.getObject();
                if (o instanceof CTP || o instanceof CTTbl || o instanceof CTSdtBlock)
                    i++;
            }
            bodyElements.add(i, newP);
            try (XmlCursor p2 = p.newCursor()) {
                cursor.toCursor(p2);
                cursor.toEndToken();
            }
            return newP;
        }
        return null;
    }

    private boolean isCursorInCmt(XmlCursor cursor) {
        try (XmlCursor verify = cursor.newCursor()) {
            verify.toParent();
            return (verify.getObject() == this.ctComment);
        }
    }

    @Override
    public XWPFTable insertNewTbl(XmlCursor cursor) {
        if (isCursorInCmt(cursor)) {
            String uri = CTTbl.type.getName().getNamespaceURI();
            String localPart = "tbl";
            cursor.beginElement(localPart, uri);
            cursor.toParent();
            CTTbl t = (CTTbl) cursor.getObject();
            XWPFTable newT = new XWPFTable(t, this);
            cursor.removeXmlContents();
            XmlObject o = null;
            while (!(o instanceof CTTbl) && (cursor.toPrevSibling())) {
                o = cursor.getObject();
            }
            if (!(o instanceof CTTbl)) {
                tables.add(0, newT);
            } else {
                int pos = tables.indexOf(getTable((CTTbl) o)) + 1;
                tables.add(pos, newT);
            }
            int i = 0;
            try (XmlCursor cursor2 = t.newCursor()) {
                while (cursor2.toPrevSibling()) {
                    o = cursor2.getObject();
                    if (o instanceof CTP || o instanceof CTTbl || o instanceof CTSdtBlock) {
                        i++;
                    }
                }
            }
            bodyElements.add(i, newT);
            try (XmlCursor cursor2 = t.newCursor()) {
                cursor.toCursor(cursor2);
                cursor.toEndToken();
            }
            return newT;
        }
        return null;
    }

    @Override
    public XWPFSDTBlock insertNewSdtBlock(XmlCursor cursor) {
        if (isCursorInCmt(cursor)) {
            String uri = CTSdtBlock.type.getName().getNamespaceURI();
            String localPart = "sdt";
            cursor.beginElement(localPart, uri);
            cursor.toParent();
            CTSdtBlock sdt = (CTSdtBlock) cursor.getObject();
            XWPFSDTBlock newSdtBlock = new XWPFSDTBlock(sdt, this);
            XmlObject o = null;
            while (!(o instanceof CTSdtBlock) && (cursor.toPrevSibling())) {
                o = cursor.getObject();
            }
            if (!(o instanceof CTSdtBlock)) {
                sdtBlocks.add(0, newSdtBlock);
            } else {
                int pos = sdtBlocks.indexOf(getSdtBlock((CTSdtBlock) o)) + 1;
                sdtBlocks.add(pos, newSdtBlock);
            }
            int i = 0;
            try (XmlCursor sdtCursor = sdt.newCursor()) {
                cursor.toCursor(sdtCursor);
                while (cursor.toPrevSibling()) {
                    o = cursor.getObject();
                    if (o instanceof CTP || o instanceof CTTbl || o instanceof CTSdtBlock) {
                        i++;
                    }
                }
                bodyElements.add(i, newSdtBlock);
                cursor.toCursor(sdtCursor);
                cursor.toEndToken();
                return newSdtBlock;
            }
        }
        return null;
    }

    @Override
    public void insertTable(int pos, XWPFTable table) {
        bodyElements.add(pos, table);
        int i = 0;
        for (CTTbl tbl : ctComment.getTblList()) {
            if (tbl == table.getCTTbl()) {
                break;
            }
            i++;
        }
        tables.add(i, table);

    }

    @Override
    public XWPFTableCell getTableCell(CTTc cell) {
        XmlObject o;
        CTRow row;
        try (final XmlCursor cursor = cell.newCursor()) {
            cursor.toParent();
            o = cursor.getObject();
            if (!(o instanceof CTRow)) {
                return null;
            }
            row = (CTRow) o;
            cursor.toParent();
            o = cursor.getObject();
        }
        if (!(o instanceof CTTbl)) {
            return null;
        }
        CTTbl tbl = (CTTbl) o;
        XWPFTable table = getTable(tbl);
        if (table == null) {
            return null;
        }
        XWPFTableRow tableRow = table.getRow(row);
        return tableRow.getTableCell(cell);
    }

    /**
     * Get the {@link XWPFDocument} the comment is part of.
     *
     * @see org.apache.poi.xwpf.usermodel.IBody#getXWPFDocument()
     */
    @Override
    public XWPFDocument getXWPFDocument() {
        return document;
    }

    public String getText() {
        StringBuilder text = new StringBuilder();
        for (XWPFParagraph p : paragraphs) {
            if (text.length() > 0) {
                text.append("\n");
            }
            text.append(p.getText());
        }
        return text.toString();
    }

    public XWPFParagraph createParagraph() {
        XWPFParagraph paragraph = new XWPFParagraph(ctComment.addNewP(), this);
        paragraphs.add(paragraph);
        bodyElements.add(paragraph);
        return paragraph;
    }

    @Override
    public XWPFTable createTable() {
        XWPFTable table = new XWPFTable(ctComment.addNewTbl(), this);
        bodyElements.add(table);
        tables.add(table);
        return table;
    }

    @Override
    public XWPFSDTBlock createSdt() {
        XWPFSDTBlock sdt = new XWPFSDTBlock(ctComment.addNewSdt(), this);
        bodyElements.add(sdt);
        sdtBlocks.add(sdt);
        return sdt;
    }

    @Override
    public void setSDTBlock(int pos, XWPFSDTBlock sdt) {
        sdtBlocks.set(pos, sdt);
        ctComment.setSdtArray(pos, sdt.getCtSdtBlock());
    }

    public void removeParagraph(XWPFParagraph paragraph) {
        if (paragraphs.contains(paragraph)) {
            CTP ctP = paragraph.getCTP();
            try (XmlCursor c = ctP.newCursor()) {
                c.removeXml();
            }
            paragraphs.remove(paragraph);
            bodyElements.remove(paragraph);
        }
    }

    public void removeTable(XWPFTable table) {
        if (tables.contains(table)) {
            CTTbl ctTbl = table.getCTTbl();
            try (XmlCursor c = ctTbl.newCursor()) {
                c.removeXml();
            }
            tables.remove(table);
            bodyElements.remove(table);
        }
    }

    public XWPFTable createTable(int rows, int cols) {
        XWPFTable table = new XWPFTable(ctComment.addNewTbl(), this, rows, cols);
        tables.add(table);
        bodyElements.add(table);
        return table;
    }

    /**
     * Gets the underlying CTComment object for the comment.
     *
     * @return CTComment object
     */
    public CTComment getCtComment() {
        return ctComment;
    }

    /**
     * The owning object for this comment
     *
     * @return The {@link XWPFComments} object that contains this comment.
     */
    public XWPFComments getComments() {
        return comments;
    }

    /**
     * Get a unique identifier for the current comment. The restrictions on the
     * id attribute, if any, are defined by the parent XML element. If this
     * attribute is omitted, then the document is non-conformant.
     *
     * @return string id
     */
    public String getId() {
        return ctComment.getId().toString();
    }

    /**
     * Get the author of the current comment
     *
     * @return author of the current comment
     */
    public String getAuthor() {
        return ctComment.getAuthor();
    }

    /**
     * Specifies the author for the current comment If this attribute is
     * omitted, then no author shall be associated with the parent annotation
     * type.
     *
     * @param author author of the current comment
     */
    public void setAuthor(String author) {
        ctComment.setAuthor(author);
    }

    /**
     * Get the initials of the author of the current comment
     *
     * @return initials the initials of the author of the current comment
     */
    public String getInitials() {
        return ctComment.getInitials();
    }

    /**
     * Specifies the initials of the author of the current comment
     *
     * @param initials the initials of the author of the current comment
     */
    public void setInitials(String initials) {
        ctComment.setInitials(initials);
    }

    /**
     * Get the date information of the current comment
     *
     * @return the date information for the current comment.
     */
    public Calendar getDate() {
        return ctComment.getDate();
    }

    /**
     * Specifies the date information for the current comment. If this attribute
     * is omitted, then no date information shall be associated with the parent
     * annotation type.
     *
     * @param date the date information for the current comment.
     */
    public void setDate(Calendar date) {
        ctComment.setDate(date);
    }

    /**
     * Finds that for example the 2nd entry in the body list is the 1st paragraph
     */
    private int getBodyElementSpecificPos(int pos, List<? extends IBodyElement> list) {
        // If there's nothing to find, skip it
        if (list.isEmpty()) {
            return -1;
        }

        if (pos >= 0 && pos < bodyElements.size()) {
            // Ensure the type is correct
            IBodyElement needle = bodyElements.get(pos);
            if (needle.getElementType() != list.get(0).getElementType()) {
                // Wrong type
                return -1;
            }

            // Work back until we find it
            int startPos = Math.min(pos, list.size() - 1);
            for (int i = startPos; i >= 0; i--) {
                if (list.get(i) == needle) {
                    return i;
                }
            }
        }

        // Couldn't be found
        return -1;
    }

    /**
     * get with the position of a table in the bodyelement array list
     * the position of this table in the table array list
     *
     * @param pos position of the table in the bodyelement array list
     * @return if there is a table at the position in the bodyelement array list,
     * else it will return null.
     */
    public int getTablePos(int pos) {
        return getBodyElementSpecificPos(pos, tables);
    }

    /**
     * Look up the paragraph at the specified position in the body elements list
     * and return this paragraphs position in the paragraphs list
     *
     * @param pos The position of the relevant paragraph in the body elements
     *            list
     * @return the position of the paragraph in the paragraphs list, if there is
     * a paragraph at the position in the bodyelements list. Else it
     * will return -1
     */
    public int getParagraphPos(int pos) {
        return getBodyElementSpecificPos(pos, paragraphs);
    }

    /**
     * get with the position of a table in the bodyelement array list
     * the position of this SDT in the contentControls array list
     *
     * @param pos position of the SDT in the bodyelement array list
     * @return if there is a table at the position in the bodyelement array list,
     * else it will return null.
     */
    public int getSDTPos(int pos) {
        return getBodyElementSpecificPos(pos, sdtBlocks);
    }

    @Override
    public boolean removeBodyElement(int pos) {
        if (pos >= 0 && pos < bodyElements.size()) {
            BodyElementType type = bodyElements.get(pos).getElementType();
            if (type == BodyElementType.TABLE) {
                int tablePos = getTablePos(pos);
                tables.remove(tablePos);
                ctComment.removeTbl(tablePos);
            }
            if (type == BodyElementType.PARAGRAPH) {
                int paraPos = getParagraphPos(pos);
                paragraphs.remove(paraPos);
                ctComment.removeP(paraPos);
            }
            if (type == BodyElementType.CONTENTCONTROL) {
                int sdtPos = getSDTPos(pos);
                sdtBlocks.remove(sdtPos);
                ctComment.removeSdt(sdtPos);
            }
            bodyElements.remove(pos);
            return true;
        }
        return false;
    }
}
