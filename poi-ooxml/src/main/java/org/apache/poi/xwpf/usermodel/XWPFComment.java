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

import java.math.BigInteger;
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
                    XWPFTable t = new XWPFTable((CTTbl) o, this, false);
                    bodyElements.add(t);
                    tables.add(t);
                } else if (o instanceof CTSdtBlock) {
                    XWPFSDT c = new XWPFSDT((CTSdtBlock) o, this);
                    bodyElements.add(c);
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
        return Collections.unmodifiableList(bodyElements);
    }

    /**
     * Returns the paragraph(s) that holds the text of the comment.
     */
    @Override
    public List<XWPFParagraph> getParagraphs() {
        return Collections.unmodifiableList(paragraphs);
    }

    /**
     * Get the list of {@link XWPFTable}s in the comment.
     *
     * @return List of tables
     */
    @Override
    public List<XWPFTable> getTables() {
        return Collections.unmodifiableList(tables);
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
                if (o instanceof CTP || o instanceof CTTbl)
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
                    if (o instanceof CTP || o instanceof CTTbl) {
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
        final BigInteger id = ctComment.getId();
        return id == null ? "-1" : id.toString();
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

}
