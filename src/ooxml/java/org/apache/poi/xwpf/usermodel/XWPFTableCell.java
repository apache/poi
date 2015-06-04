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
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.util.Internal;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSdtBlock;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSdtRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTShd;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTbl;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTVerticalJc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STShd;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STVerticalJc;

/**
 * Represents a Cell within a {@link XWPFTable}. The
 * Cell is the thing that holds the actual content (paragraphs etc)
 */
public class XWPFTableCell implements IBody, ICell {
    private static EnumMap<XWPFVertAlign, STVerticalJc.Enum> alignMap;
    // Create a map from the STVerticalJc.Enum values to the XWPF-level enums
    private static HashMap<Integer, XWPFVertAlign> stVertAlignTypeMap;

    static {
        // populate enum maps
        alignMap = new EnumMap<XWPFVertAlign, STVerticalJc.Enum>(XWPFVertAlign.class);
        alignMap.put(XWPFVertAlign.TOP, STVerticalJc.Enum.forInt(STVerticalJc.INT_TOP));
        alignMap.put(XWPFVertAlign.CENTER, STVerticalJc.Enum.forInt(STVerticalJc.INT_CENTER));
        alignMap.put(XWPFVertAlign.BOTH, STVerticalJc.Enum.forInt(STVerticalJc.INT_BOTH));
        alignMap.put(XWPFVertAlign.BOTTOM, STVerticalJc.Enum.forInt(STVerticalJc.INT_BOTTOM));

        stVertAlignTypeMap = new HashMap<Integer, XWPFVertAlign>();
        stVertAlignTypeMap.put(STVerticalJc.INT_TOP, XWPFVertAlign.TOP);
        stVertAlignTypeMap.put(STVerticalJc.INT_CENTER, XWPFVertAlign.CENTER);
        stVertAlignTypeMap.put(STVerticalJc.INT_BOTH, XWPFVertAlign.BOTH);
        stVertAlignTypeMap.put(STVerticalJc.INT_BOTTOM, XWPFVertAlign.BOTTOM);

    }

    private final CTTc ctTc;
    protected List<XWPFParagraph> paragraphs = null;
    protected List<XWPFTable> tables = null;
    protected List<IBodyElement> bodyElements = null;

    ;
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
        if (cell.sizeOfPArray() < 1)
            cell.addNewP();
        bodyElements = new ArrayList<IBodyElement>();
        paragraphs = new ArrayList<XWPFParagraph>();
        tables = new ArrayList<XWPFTable>();

        XmlCursor cursor = ctTc.newCursor();
        cursor.selectPath("./*");
        while (cursor.toNextSelection()) {
            XmlObject o = cursor.getObject();
            if (o instanceof CTP) {
                XWPFParagraph p = new XWPFParagraph((CTP) o, this);
                paragraphs.add(p);
                bodyElements.add(p);
            }
            if (o instanceof CTTbl) {
                XWPFTable t = new XWPFTable((CTTbl) o, this);
                tables.add(t);
                bodyElements.add(t);
            }
            if (o instanceof CTSdtBlock) {
                XWPFSDT c = new XWPFSDT((CTSdtBlock) o, this);
                bodyElements.add(c);
            }
            if (o instanceof CTSdtRun) {
                XWPFSDT c = new XWPFSDT((CTSdtRun) o, this);
                System.out.println(c.getContent().getText());
                bodyElements.add(c);
            }
        }
        cursor.dispose();
    }

    @Internal
    public CTTc getCTTc() {
        return ctTc;
    }

    /**
     * returns an Iterator with paragraphs and tables
     *
     * @see org.apache.poi.xwpf.usermodel.IBody#getBodyElements()
     */
    public List<IBodyElement> getBodyElements() {
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
    public List<XWPFParagraph> getParagraphs() {
        return paragraphs;
    }

    /**
     * Add a Paragraph to this Table Cell
     *
     * @return The paragraph which was added
     */
    public XWPFParagraph addParagraph() {
        XWPFParagraph p = new XWPFParagraph(ctTc.addNewP(), this);
        addParagraph(p);
        return p;
    }

    /**
     * add a Paragraph to this TableCell
     *
     * @param p the paragaph which has to be added
     */
    public void addParagraph(XWPFParagraph p) {
        paragraphs.add(p);
    }

    /**
     * removes a paragraph of this tablecell
     *
     * @param pos
     */
    public void removeParagraph(int pos) {
        paragraphs.remove(pos);
        ctTc.removeP(pos);
    }

    /**
     * if there is a corresponding {@link XWPFParagraph} of the parameter ctTable in the paragraphList of this table
     * the method will return this paragraph
     * if there is no corresponding {@link XWPFParagraph} the method will return null
     *
     * @param p is instance of CTP and is searching for an XWPFParagraph
     * @return null if there is no XWPFParagraph with an corresponding CTPparagraph in the paragraphList of this table
     * XWPFParagraph with the correspondig CTP p
     */
    public XWPFParagraph getParagraph(CTP p) {
        for (XWPFParagraph paragraph : paragraphs) {
            if (p.equals(paragraph.getCTP())) {
                return paragraph;
            }
        }
        return null;
    }

    public XWPFTableRow getTableRow() {
        return tableRow;
    }

    /**
     * Get cell color. Note that this method only returns the "fill" value.
     *
     * @return RGB string of cell color
     */
    public String getColor() {
        String color = null;
        CTTcPr tcpr = ctTc.getTcPr();
        if (tcpr != null) {
            CTShd ctshd = tcpr.getShd();
            if (ctshd != null) {
                color = ctshd.xgetFill().getStringValue();
            }
        }
        return color;
    }

    /**
     * Set cell color. This sets some associated values; for finer control
     * you may want to access these elements individually.
     *
     * @param rgbStr - the desired cell color, in the hex form "RRGGBB".
     */
    public void setColor(String rgbStr) {
        CTTcPr tcpr = ctTc.isSetTcPr() ? ctTc.getTcPr() : ctTc.addNewTcPr();
        CTShd ctshd = tcpr.isSetShd() ? tcpr.getShd() : tcpr.addNewShd();
        ctshd.setColor("auto");
        ctshd.setVal(STShd.CLEAR);
        ctshd.setFill(rgbStr);
    }

    /**
     * Get the vertical alignment of the cell.
     *
     * @return the cell alignment enum value
     */
    public XWPFVertAlign getVerticalAlignment() {
        XWPFVertAlign vAlign = null;
        CTTcPr tcpr = ctTc.getTcPr();
        if (ctTc != null) {
            CTVerticalJc va = tcpr.getVAlign();
            vAlign = stVertAlignTypeMap.get(va.getVal().intValue());
        }
        return vAlign;
    }

    /**
     * Set the vertical alignment of the cell.
     *
     * @param vAlign - the desired alignment enum value
     */
    public void setVerticalAlignment(XWPFVertAlign vAlign) {
        CTTcPr tcpr = ctTc.isSetTcPr() ? ctTc.getTcPr() : ctTc.addNewTcPr();
        CTVerticalJc va = tcpr.addNewVAlign();
        va.setVal(alignMap.get(vAlign));
    }

    /**
     * add a new paragraph at position of the cursor
     *
     * @param cursor
     * @return the inserted paragraph
     */
    public XWPFParagraph insertNewParagraph(XmlCursor cursor) {
        if (!isCursorInTableCell(cursor)) {
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
        if ((!(o instanceof CTP)) || (CTP) o == p) {
            paragraphs.add(0, newP);
        } else {
            int pos = paragraphs.indexOf(getParagraph((CTP) o)) + 1;
            paragraphs.add(pos, newP);
        }
        int i = 0;
        cursor.toCursor(p.newCursor());
        while (cursor.toPrevSibling()) {
            o = cursor.getObject();
            if (o instanceof CTP || o instanceof CTTbl)
                i++;
        }
        bodyElements.add(i, newP);
        cursor.toCursor(p.newCursor());
        cursor.toEndToken();
        return newP;
    }

    public XWPFTable insertNewTbl(XmlCursor cursor) {
        if (isCursorInTableCell(cursor)) {
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
            cursor = t.newCursor();
            while (cursor.toPrevSibling()) {
                o = cursor.getObject();
                if (o instanceof CTP || o instanceof CTTbl)
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
        if (verify.getObject() == this.ctTc) {
            return true;
        }
        return false;
    }

    /**
     * @see org.apache.poi.xwpf.usermodel.IBody#getParagraphArray(int)
     */
    public XWPFParagraph getParagraphArray(int pos) {
        if (pos > 0 && pos < paragraphs.size()) {
            return paragraphs.get(pos);
        }
        return null;
    }

    /**
     * get the to which the TableCell belongs
     *
     * @see org.apache.poi.xwpf.usermodel.IBody#getPart()
     */
    public POIXMLDocumentPart getPart() {
        return tableRow.getTable().getPart();
    }

    /**
     * @see org.apache.poi.xwpf.usermodel.IBody#getPartType()
     */
    public BodyType getPartType() {
        return BodyType.TABLECELL;
    }

    /**
     * get a table by its CTTbl-Object
     *
     * @see org.apache.poi.xwpf.usermodel.IBody#getTable(org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTbl)
     */
    public XWPFTable getTable(CTTbl ctTable) {
        for (int i = 0; i < tables.size(); i++) {
            if (getTables().get(i).getCTTbl() == ctTable) return getTables().get(i);
        }
        return null;
    }

    /**
     * @see org.apache.poi.xwpf.usermodel.IBody#getTableArray(int)
     */
    public XWPFTable getTableArray(int pos) {
        if (pos > 0 && pos < tables.size()) {
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
     *
     * @see org.apache.poi.xwpf.usermodel.IBody#insertTable(int, org.apache.poi.xwpf.usermodel.XWPFTable)
     */
    @SuppressWarnings("deprecation")
    public void insertTable(int pos, XWPFTable table) {
        bodyElements.add(pos, table);
        int i = 0;
        for (CTTbl tbl : ctTc.getTblArray()) {
            if (tbl == table.getCTTbl()) {
                break;
            }
            i++;
        }
        tables.add(i, table);
    }

    public String getText() {
        StringBuffer text = new StringBuffer();
        for (XWPFParagraph p : paragraphs) {
            text.append(p.getText());
        }
        return text.toString();
    }

    public void setText(String text) {
        CTP ctP = (ctTc.sizeOfPArray() == 0) ? ctTc.addNewP() : ctTc.getPArray(0);
        XWPFParagraph par = new XWPFParagraph(ctP, this);
        par.createRun().setText(text);
    }

    /**
     * extracts all text recursively through embedded tables and embedded SDTs
     */
    public String getTextRecursively() {

        StringBuffer text = new StringBuffer();
        for (int i = 0; i < bodyElements.size(); i++) {
            boolean isLast = (i == bodyElements.size() - 1) ? true : false;
            appendBodyElementText(text, bodyElements.get(i), isLast);
        }

        return text.toString();
    }

    private void appendBodyElementText(StringBuffer text, IBodyElement e, boolean isLast) {
        if (e instanceof XWPFParagraph) {
            text.append(((XWPFParagraph) e).getText());
            if (isLast == false) {
                text.append('\t');
            }
        } else if (e instanceof XWPFTable) {
            XWPFTable eTable = (XWPFTable) e;
            for (XWPFTableRow row : eTable.getRows()) {
                for (XWPFTableCell cell : row.getTableCells()) {
                    List<IBodyElement> localBodyElements = cell.getBodyElements();
                    for (int i = 0; i < localBodyElements.size(); i++) {
                        boolean localIsLast = (i == localBodyElements.size() - 1) ? true : false;
                        appendBodyElementText(text, localBodyElements.get(i), localIsLast);
                    }
                }
            }

            if (isLast == false) {
                text.append('\n');
            }
        } else if (e instanceof XWPFSDT) {
            text.append(((XWPFSDT) e).getContent().getText());
            if (isLast == false) {
                text.append('\t');
            }
        }
    }

    /**
     * get the TableCell which belongs to the TableCell
     */
    public XWPFTableCell getTableCell(CTTc cell) {
        XmlCursor cursor = cell.newCursor();
        cursor.toParent();
        XmlObject o = cursor.getObject();
        if (!(o instanceof CTRow)) {
            return null;
        }
        CTRow row = (CTRow) o;
        cursor.toParent();
        o = cursor.getObject();
        cursor.dispose();
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

    public XWPFDocument getXWPFDocument() {
        return part.getXWPFDocument();
    }

    // Create a map from this XWPF-level enum to the STVerticalJc.Enum values
    public static enum XWPFVertAlign {
        TOP, CENTER, BOTH, BOTTOM
    }
}
