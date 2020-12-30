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

import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.ooxml.util.POIXMLUnits;
import org.apache.poi.util.Internal;
import org.apache.poi.util.Units;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSdtBlock;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSdtRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTShd;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTbl;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblWidth;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTVerticalJc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STShd;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth;
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
        alignMap = new EnumMap<>(XWPFVertAlign.class);
        alignMap.put(XWPFVertAlign.TOP, STVerticalJc.Enum.forInt(STVerticalJc.INT_TOP));
        alignMap.put(XWPFVertAlign.CENTER, STVerticalJc.Enum.forInt(STVerticalJc.INT_CENTER));
        alignMap.put(XWPFVertAlign.BOTH, STVerticalJc.Enum.forInt(STVerticalJc.INT_BOTH));
        alignMap.put(XWPFVertAlign.BOTTOM, STVerticalJc.Enum.forInt(STVerticalJc.INT_BOTTOM));

        stVertAlignTypeMap = new HashMap<>();
        stVertAlignTypeMap.put(STVerticalJc.INT_TOP, XWPFVertAlign.TOP);
        stVertAlignTypeMap.put(STVerticalJc.INT_CENTER, XWPFVertAlign.CENTER);
        stVertAlignTypeMap.put(STVerticalJc.INT_BOTH, XWPFVertAlign.BOTH);
        stVertAlignTypeMap.put(STVerticalJc.INT_BOTTOM, XWPFVertAlign.BOTTOM);

    }

    private final CTTc ctTc;
    protected List<XWPFParagraph> paragraphs;
    protected List<XWPFTable> tables;
    protected List<IBodyElement> bodyElements;

    protected IBody part;
    private XWPFTableRow tableRow;

    /**
     * If a table cell does not include at least one block-level element, then this document shall be considered corrupt
     */
    public XWPFTableCell(CTTc cell, XWPFTableRow tableRow, IBody part) {
        this.ctTc = cell;
        this.part = part;
        this.tableRow = tableRow;

        bodyElements = new ArrayList<>();
        paragraphs = new ArrayList<>();
        tables = new ArrayList<>();

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
        return Collections.unmodifiableList(paragraphs);
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
     * @param p the paragraph which has to be added
     */
    public void addParagraph(XWPFParagraph p) {
        paragraphs.add(p);
        bodyElements.add(p);
    }

    /**
     * removes a paragraph of this tablecell
     *
     * @param pos The position in the list of paragraphs, 0-based
     */
    public void removeParagraph(int pos) {
        XWPFParagraph removedParagraph = paragraphs.get(pos);
        paragraphs.remove(pos);
        ctTc.removeP(pos);
        bodyElements.remove(removedParagraph);
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
        CTTcPr tcpr = getTcPr();
        CTShd ctshd = tcpr.isSetShd() ? tcpr.getShd() : tcpr.addNewShd();
        ctshd.setColor("auto");
        ctshd.setVal(STShd.CLEAR);
        ctshd.setFill(rgbStr);
    }

    /**
     * Get the vertical alignment of the cell.
     *
     * @return the cell alignment enum value or <code>null</code>
     * if no vertical alignment is set.
     */
    public XWPFVertAlign getVerticalAlignment() {
        XWPFVertAlign vAlign = null;
        CTTcPr tcpr = ctTc.getTcPr();
        if (tcpr != null) {
            CTVerticalJc va = tcpr.getVAlign();
            if(va != null) {
                vAlign = stVertAlignTypeMap.get(va.getVal().intValue());
            }
        }
        return vAlign;
    }

    /**
     * Set the vertical alignment of the cell.
     *
     * @param vAlign - the desired alignment enum value
     */
    public void setVerticalAlignment(XWPFVertAlign vAlign) {
        CTTcPr tcpr = getTcPr();
        CTVerticalJc va = tcpr.addNewVAlign();
        va.setVal(alignMap.get(vAlign));
    }

    /**
     * add a new paragraph at position of the cursor
     *
     * @param cursor The XmlCursor structure created with XmlBeans
     * @return the inserted paragraph
     */
    public XWPFParagraph insertNewParagraph(final XmlCursor cursor) {
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
        if ((!(o instanceof CTP)) || o == p) {
            paragraphs.add(0, newP);
        } else {
            int pos = paragraphs.indexOf(getParagraph((CTP) o)) + 1;
            paragraphs.add(pos, newP);
        }
        int i = 0;
        XmlCursor p2 = p.newCursor();
        cursor.toCursor(p2);
        p2.dispose();
        while (cursor.toPrevSibling()) {
            o = cursor.getObject();
            if (o instanceof CTP || o instanceof CTTbl)
                i++;
        }
        bodyElements.add(i, newP);
        p2 = p.newCursor();
        cursor.toCursor(p2);
        p2.dispose();
        cursor.toEndToken();
        return newP;
    }

    public XWPFTable insertNewTbl(final XmlCursor cursor) {
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
            XmlCursor cursor2 = t.newCursor();
            while (cursor2.toPrevSibling()) {
                o = cursor2.getObject();
                if (o instanceof CTP || o instanceof CTTbl)
                    i++;
            }
            cursor2.dispose();
            bodyElements.add(i, newT);
            cursor2 = t.newCursor();
            cursor.toCursor(cursor2);
            cursor.toEndToken();
            cursor2.dispose();
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
        boolean result = (verify.getObject() == this.ctTc);
        verify.dispose();
        return result;
    }

    /**
     * @see org.apache.poi.xwpf.usermodel.IBody#getParagraphArray(int)
     */
    public XWPFParagraph getParagraphArray(int pos) {
        if (pos >= 0 && pos < paragraphs.size()) {
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
        if(pos >= 0 && pos < tables.size()) {
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
    public void insertTable(int pos, XWPFTable table) {
        bodyElements.add(pos, table);
        int i = 0;
        for (CTTbl tbl : ctTc.getTblList()) {
            if (tbl == table.getCTTbl()) {
                break;
            }
            i++;
        }
        tables.add(i, table);
    }

    /**
     * removes a table of this table cell
     *
     * @param pos The position in the list of tables, 0-based
     */
    public void removeTable(int pos) {
        XWPFTable removedTable = tables.get(pos);
        tables.remove(pos);
        ctTc.removeTbl(pos);
        bodyElements.remove(removedTable);
    }

    public String getText() {
        StringBuilder text = new StringBuilder();
        for (XWPFParagraph p : paragraphs) {
            text.append(p.getText());
        }
        return text.toString();
    }

    public void setText(String text) {
        XWPFParagraph par = (paragraphs.size() == 0) ? addParagraph() : paragraphs.get(0);
        par.createRun().setText(text);
    }

    /**
     * extracts all text recursively through embedded tables and embedded SDTs
     */
    public String getTextRecursively() {

        StringBuilder text = new StringBuilder(64);
        for (int i = 0; i < bodyElements.size(); i++) {
            boolean isLast = (i == bodyElements.size() - 1);
            appendBodyElementText(text, bodyElements.get(i), isLast);
        }

        return text.toString();
    }

    private void appendBodyElementText(StringBuilder text, IBodyElement e, boolean isLast) {
        if (e instanceof XWPFParagraph) {
            text.append(((XWPFParagraph) e).getText());
            if (!isLast) {
                text.append('\t');
            }
        } else if (e instanceof XWPFTable) {
            XWPFTable eTable = (XWPFTable) e;
            for (XWPFTableRow row : eTable.getRows()) {
                for (XWPFTableCell cell : row.getTableCells()) {
                    List<IBodyElement> localBodyElements = cell.getBodyElements();
                    for (int i = 0; i < localBodyElements.size(); i++) {
                        boolean localIsLast = (i == localBodyElements.size() - 1);
                        appendBodyElementText(text, localBodyElements.get(i), localIsLast);
                    }
                }
            }

            if (!isLast) {
                text.append('\n');
            }
        } else if (e instanceof XWPFSDT) {
            text.append(((XWPFSDT) e).getContent().getText());
            if (!isLast) {
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
        XWPFTableRow tr = table.getRow(row);
        if (tr == null) {
            return null;
        }
        return tr.getTableCell(cell);
    }

    public XWPFDocument getXWPFDocument() {
        return part.getXWPFDocument();
    }

    // Create a map from this XWPF-level enum to the STVerticalJc.Enum values
    public enum XWPFVertAlign {
        TOP, CENTER, BOTH, BOTTOM
    }

    /**
     * Get the table width as a decimal value.
     * <p>If the width type is DXA or AUTO, then the value will always have
     * a fractional part of zero (because these values are really integers).
     * If the with type is percentage, then value may have a non-zero fractional
     * part.
     *
     * @return Width value as a double-precision decimal.
     * @since 4.0.0
     */
    public double getWidthDecimal() {
        return XWPFTable.getWidthDecimal(getTcWidth());
    }

    /**
     * Get the width type for the table, as an {@link STTblWidth.Enum} value.
     * A table width can be specified as an absolute measurement (an integer
     * number of twips), a percentage, or the value "AUTO".
     *
     * @return The width type.
     * @since 4.0.0
     */
    public TableWidthType getWidthType() {
        return XWPFTable.getWidthType(getTcWidth());
    }

    /**
     * Set the width to the value "auto", an integer value (20ths of a point), or a percentage ("nn.nn%").
     *
     * @param widthValue String matching one of "auto", [0-9]+, or [0-9]+(\.[0-9]+)%.
     * @since 4.0.0
     */
    public void setWidth(String widthValue) {
        XWPFTable.setWidthValue(widthValue, getTcWidth());
    }

    private CTTblWidth getTcWidth() {
        CTTcPr tcPr = getTcPr();
        return tcPr.isSetTcW() ? tcPr.getTcW() : tcPr.addNewTcW();
    }

    /**
     * Get the cell properties for the cell.
     * @return The cell properties
     * @since 4.0.0
     */
    protected CTTcPr getTcPr() {
        return ctTc.isSetTcPr() ? ctTc.getTcPr() : ctTc.addNewTcPr();
    }

    /**
     * Set the width value type for the table.
     * <p>If the width type is changed from the current type and the currently-set value
     * is not consistent with the new width type, the value is reset to the default value
     * for the specified width type.</p>
     *
     * @param widthType Width type
     * @since 4.0.0
     */
    public void setWidthType(TableWidthType widthType) {
        XWPFTable.setWidthType(widthType, getTcWidth());
    }

    public int getWidth() {
        return (int) Units.toDXA(POIXMLUnits.parseLength(getTcWidth().xgetW()));
    }
}
