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
import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSdtBlock;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSdtContentBlock;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTbl;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTc;


/**
 * Experimental class to offer rudimentary read-only processing of
 * the CTSdtContentBlock.
 * <p>
 * WARNING - APIs expected to change rapidly
 */
public class XWPFSDTContentBlock implements ISDTContent, IBody {

    private final CTSdtContentBlock ctSdtContentBlock;
    protected List<XWPFParagraph> paragraphs;
    protected List<XWPFTable> tables;
    protected List<IBodyElement> bodyElements;

    private final XWPFSDTBlock xwpfsdtBlock;

    public XWPFSDTContentBlock(CTSdtContentBlock ctSdtContentBlock, XWPFSDTBlock xwpfsdtBlock) {
        this.ctSdtContentBlock = ctSdtContentBlock;
        this.xwpfsdtBlock = xwpfsdtBlock;

        bodyElements = new ArrayList<>();
        paragraphs = new ArrayList<>();
        tables = new ArrayList<>();
        //sdtContentBlock is allowed to be null:  minOccurs="0" maxOccurs="1"
        if (ctSdtContentBlock == null) {
            return;
        }
        try (XmlCursor cursor = ctSdtContentBlock.newCursor()) {
            cursor.selectPath("./*");
            while (cursor.toNextSelection()) {
                XmlObject o = cursor.getObject();
                if (o instanceof CTP) {
                    XWPFParagraph p = new XWPFParagraph((CTP) o, this);
                    paragraphs.add(p);
                    bodyElements.add(p);
                }
                if (o instanceof CTTbl) {
                    XWPFTable t = new XWPFTable((CTTbl) o, this, false);
                    tables.add(t);
                    bodyElements.add(t);
                }
                if (o instanceof CTSdtBlock) {
                    XWPFSDTBlock c = new XWPFSDTBlock((CTSdtBlock) o, this);
                    bodyElements.add(c);
                }
            }
        }
    }

    public CTSdtContentBlock getCTSdtContentBlock() {
        return ctSdtContentBlock;
    }

    public XWPFSDTBlock getSDT() {
        return xwpfsdtBlock;
    }

    public String getText() {
        StringBuilder text = new StringBuilder();
        boolean addNewLine = false;
        for (int i = 0; i < bodyElements.size(); i++) {
            Object o = bodyElements.get(i);
            if (o instanceof XWPFParagraph) {
                appendParagraph((XWPFParagraph) o, text);
                addNewLine = true;
            } else if (o instanceof XWPFTable) {
                appendTable((XWPFTable) o, text);
                addNewLine = true;
            } else if (o instanceof XWPFSDTBlock) {
                text.append(((XWPFSDTBlock) o).getContent().getText());
                addNewLine = true;
            }
            if (addNewLine && i < bodyElements.size() - 1) {
                text.append("\n");
            }
        }
        return text.toString();
    }

    private void appendTable(XWPFTable table, StringBuilder text) {
        //this works recursively to pull embedded tables from within cells
        for (XWPFTableRow row : table.getRows()) {
            List<ICell> cells = row.getTableICells();
            for (int i = 0; i < cells.size(); i++) {
                ICell cell = cells.get(i);
                if (cell instanceof XWPFTableCell) {
                    text.append(((XWPFTableCell) cell).getTextRecursively());
                } else if (cell instanceof XWPFSDTCell) {
                    text.append(((XWPFSDTCell) cell).getContent().getText());
                }
                if (i < cells.size() - 1) {
                    text.append("\t");
                }
            }
            text.append('\n');
        }
    }

    private void appendParagraph(XWPFParagraph paragraph, StringBuilder text) {
        for (IRunElement run : paragraph.getRuns()) {
            text.append(run);
        }
    }


    public String toString() {
        return getText();
    }

    @Override
    public POIXMLDocumentPart getPart() {
        return this.xwpfsdtBlock.getPart();
    }

    @Override
    public BodyType getPartType() {
        return BodyType.CONTENTCONTROL;
    }

    @Override
    public List<IBodyElement> getBodyElements() {
        return Collections.unmodifiableList(bodyElements);
    }

    @Override
    public List<XWPFParagraph> getParagraphs() {
        return Collections.unmodifiableList(paragraphs);
    }

    @Override
    public List<XWPFTable> getTables() {
        return Collections.unmodifiableList(tables);
    }

    @Override
    public XWPFParagraph getParagraph(CTP p) {
        for (XWPFParagraph paragraph : paragraphs) {
            if (p.equals(paragraph.getCTP())) {
                return paragraph;
            }
        }
        return null;
    }

    @Override
    public XWPFTable getTable(CTTbl ctTable) {
        for (int i = 0; i < tables.size(); i++) {
            if (getTables().get(i).getCTTbl() == ctTable) return getTables().get(i);
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
        if(pos >= 0 && pos < tables.size()) {
            return tables.get(pos);
        }
        return null;
    }

    @Override
    public XWPFParagraph insertNewParagraph(XmlCursor cursor) {
        if (!isCursorInSdtContentBlock(cursor)) {
            return null;
        }

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
        try (XmlCursor p3 = p.newCursor()) {
            cursor.toCursor(p3);
        }
        cursor.toEndToken();
        return newP;
    }

    /**
     * verifies that cursor is on the right position
     */
    private boolean isCursorInSdtContentBlock(XmlCursor cursor) {
        try (XmlCursor verify = cursor.newCursor()) {
            verify.toParent();
            return (verify.getObject() == this.ctSdtContentBlock);
        }
    }

    @Override
    public XWPFTable insertNewTbl(XmlCursor cursor) {
        if (isCursorInSdtContentBlock(cursor)) {
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
                    if (o instanceof CTP || o instanceof CTTbl)
                        i++;
                }
            }
            bodyElements.add(i, newT);
            try (XmlCursor cursor3 = t.newCursor()) {
                cursor.toCursor(cursor3);
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
        for (CTTbl tbl : ctSdtContentBlock.getTblList()) {
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
        if (tableRow == null) {
            return null;
        }
        return tableRow.getTableCell(cell);
    }

    @Override
    public XWPFDocument getXWPFDocument() {
        return this.xwpfsdtBlock.getDocument();
    }
}
