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
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class XWPFTextBoxContent implements IBody {
    private final CTTxbxContent ctTxbxContent;
    private final Object parent;
    private final List<IBodyElement> bodyElements = new ArrayList<>();
    private final List<XWPFParagraph> paragraphs = new ArrayList<>();
    private final List<XWPFTable> tables = new ArrayList<>();
    private final List<XWPFSDTBlock> sdtBlocks = new ArrayList<>();

    public XWPFTextBoxContent(CTTxbxContent ctTxbxContent, XWPFShape parent) {
        this.ctTxbxContent = ctTxbxContent;
        this.parent = parent;
        init();
    }

    public XWPFTextBoxContent(CTTxbxContent ctTxbxContent, XWPFWordprocessingShape parent) {
        this.ctTxbxContent = ctTxbxContent;
        this.parent = parent;
        init();
    }

    private void init() {
        for (XmlObject o : ctTxbxContent.selectChildren(QNameSet.ALL)) {
            if (o instanceof CTP) {
                XWPFParagraph p = new XWPFParagraph((CTP) o, this);
                bodyElements.add(p);
                paragraphs.add(p);
            } else if (o instanceof CTTbl) {
                XWPFTable t = new XWPFTable((CTTbl) o, this);
                bodyElements.add(t);
                tables.add(t);
            } else if (o instanceof CTSdtBlock) {
                XWPFSDTBlock c = new XWPFSDTBlock(((CTSdtBlock) o), this);
                bodyElements.add(c);
                sdtBlocks.add(c);
            }
        }
    }

    public CTTxbxContent getCtTxbxContent() {
        return ctTxbxContent;
    }

    @Override
    public POIXMLDocumentPart getPart() {
        if (parent instanceof XWPFShape) {
            return ((XWPFShape) parent).getParent().getParent().getPart();
        }
        if (parent instanceof XWPFWordprocessingShape) {
            return ((XWPFWordprocessingShape) parent).getParent().getParent().getParent().getParent().getParent().getPart();
        }
        return null;
    }

    @Override
    public BodyType getPartType() {
        return BodyType.TEXTBOXCONTENT;
    }

    @Override
    public List<IBodyElement> getBodyElements() {
        return bodyElements;
    }

    @Override
    public List<XWPFParagraph> getParagraphs() {
        return paragraphs;
    }

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
        for (int i = 0; i < getParagraphs().size(); i++) {
            if (getParagraphs().get(i).getCTP() == p) {
                return getParagraphs().get(i);
            }
        }
        return null;
    }

    /**
     * verifies that cursor is on the right position
     */
    private boolean isCursorInTextBoxContent(XmlCursor cursor) {
        try (XmlCursor verify = cursor.newCursor()) {
            verify.toParent();
            return verify.getObject() == ctTxbxContent;
        }
    }

    @Override
    public XWPFParagraph insertNewParagraph(XmlCursor cursor) {
        if (isCursorInTextBoxContent(cursor)) {
            String uri = CTP.type.getName().getNamespaceURI();
            String localPart = "p";
            // creates a new Paragraph, cursor is positioned inside the new
            // element
            cursor.beginElement(localPart, uri);
            // move the cursor to the START token to the paragraph just created
            cursor.toParent();
            CTP p = (CTP) cursor.getObject();
            XWPFParagraph newP = new XWPFParagraph(p, this);
            XmlObject o = null;
            /*
             * move the cursor to the previous element until a) the next
             * paragraph is found or b) all elements have been passed
             */
            while (!(o instanceof CTP) && (cursor.toPrevSibling())) {
                o = cursor.getObject();
            }
            /*
             * if the object that has been found is a) not a paragraph or b) is
             * the paragraph that has just been inserted, as the cursor in the
             * while loop above was not moved as there were no other siblings,
             * then the paragraph that was just inserted is the first paragraph
             * in the body. Otherwise, take the previous paragraph and calculate
             * the new index for the new paragraph.
             */
            if ((!(o instanceof CTP)) || o == p) {
                paragraphs.add(0, newP);
            } else {
                int pos = paragraphs.indexOf(getParagraph((CTP) o)) + 1;
                paragraphs.add(pos, newP);
            }

            /*
             * create a new cursor, that points to the START token of the just
             * inserted paragraph
             */
            try (XmlCursor newParaPos = p.newCursor()) {
                /*
                 * Calculate the paragraphs index in the list of all body
                 * elements
                 */
                int i = 0;
                cursor.toCursor(newParaPos);
                while (cursor.toPrevSibling()) {
                    o = cursor.getObject();
                    if (o instanceof CTP || o instanceof CTTbl || o instanceof CTSdtBlock) {
                        i++;
                    }
                }
                bodyElements.add(i, newP);
                cursor.toCursor(newParaPos);
                cursor.toEndToken();
                return newP;
            }
        }
        return null;
    }

    @Override
    public XWPFParagraph createParagraph() {
        XWPFParagraph p = new XWPFParagraph(ctTxbxContent.addNewP(), this);
        bodyElements.add(p);
        paragraphs.add(p);
        return p;
    }

    @Override
    public XWPFTable createTable() {
        XWPFTable table = new XWPFTable(ctTxbxContent.addNewTbl(), this);
        bodyElements.add(table);
        tables.add(table);
        return table;
    }

    @Override
    public void setSDTBlock(int pos, XWPFSDTBlock sdt) {
        sdtBlocks.set(pos, sdt);
        ctTxbxContent.setSdtArray(pos, sdt.getCtSdtBlock());
    }

    @Override
    public XWPFSDTBlock createSdt() {
        XWPFSDTBlock sdt = new XWPFSDTBlock(ctTxbxContent.addNewSdt(), this);
        bodyElements.add(sdt);
        sdtBlocks.add(sdt);
        return sdt;
    }

    @Override
    public XWPFTable getTable(CTTbl ctTbl) {
        for (int i = 0; i < tables.size(); i++) {
            if (getTables().get(i).getCTTbl() == ctTbl) {
                return getTables().get(i);
            }
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
    public XWPFTable insertNewTbl(XmlCursor cursor) {
        if (isCursorInTextBoxContent(cursor)) {
            String uri = CTTbl.type.getName().getNamespaceURI();
            String localPart = "tbl";
            cursor.beginElement(localPart, uri);
            cursor.toParent();
            CTTbl t = (CTTbl) cursor.getObject();
            XWPFTable newT = new XWPFTable(t, this);
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
            try (XmlCursor tableCursor = t.newCursor()) {
                cursor.toCursor(tableCursor);
                while (cursor.toPrevSibling()) {
                    o = cursor.getObject();
                    if (o instanceof CTP || o instanceof CTTbl || o instanceof CTSdtBlock) {
                        i++;
                    }
                }
                bodyElements.add(i, newT);
                cursor.toCursor(tableCursor);
                cursor.toEndToken();
                return newT;
            }
        }
        return null;
    }

    @Override
    public XWPFSDTBlock insertNewSdtBlock(XmlCursor cursor) {
        if (isCursorInTextBoxContent(cursor)) {
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
        for (CTTbl tbl : ctTxbxContent.getTblArray()) {
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
        try (XmlCursor cursor = cell.newCursor()) {
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
        if (tableRow == null) {
            return null;
        }
        return tableRow.getTableCell(cell);
    }

    @Override
    public boolean removeBodyElement(int pos) {
        if (pos >= 0 && pos < bodyElements.size()) {
            BodyElementType type = bodyElements.get(pos).getElementType();
            if (type == BodyElementType.TABLE) {
                int tablePos = getTablePos(pos);
                tables.remove(tablePos);
                ctTxbxContent.removeTbl(tablePos);
            }
            if (type == BodyElementType.PARAGRAPH) {
                int paraPos = getParagraphPos(pos);
                paragraphs.remove(paraPos);
                ctTxbxContent.removeP(paraPos);
            }
            if (type == BodyElementType.CONTENTCONTROL) {
                int sdtPos = getSDTPos(pos);
                sdtBlocks.remove(sdtPos);
                ctTxbxContent.removeSdt(sdtPos);
            }
            bodyElements.remove(pos);
            return true;
        }
        return false;
    }

    @Override
    public XWPFDocument getXWPFDocument() {
        if (parent instanceof XWPFWordprocessingShape) {
            return ((XWPFWordprocessingShape) parent).getParent().getParent().getParent().getParent().getParent().getDocument();
        } else if (parent instanceof XWPFShape) {
            return ((XWPFShape) parent).getParent().getParent().getDocument();
        }
        return null;
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
     * the position of this SDT in the contentControls array list
     *
     * @param pos position of the SDT in the bodyelement array list
     * @return if there is a table at the position in the bodyelement array list,
     * else it will return null.
     */
    public int getSDTPos(int pos) {
        return getBodyElementSpecificPos(pos, sdtBlocks);
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
     * the position of this table in the table array list
     *
     * @param pos position of the table in the bodyelement array list
     * @return if there is a table at the position in the bodyelement array list,
     * else it will return null.
     */
    public int getTablePos(int pos) {
        return getBodyElementSpecificPos(pos, tables);
    }

    public Object getParent() {
        return parent;
    }
}
