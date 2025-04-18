package org.apache.poi.xwpf.usermodel;

import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class XWPFSDTContentBlock implements ISDTContent, IBody {
    private CTSdtContentBlock ctSdtContentBlock;
    private final List<IBodyElement> bodyElements = new ArrayList<>();
    private final List<XWPFParagraph> paragraphs = new ArrayList<>();
    private final List<XWPFTable> tables = new ArrayList<>();
    private final List<XWPFSDTBlock> contentControls = new ArrayList<>();
    private IBodyElement part;

    public XWPFSDTContentBlock(CTSdtContentBlock block, IBodyElement part) {
        if (block == null) {
            return;
        }
        this.ctSdtContentBlock = block;
        this.part = part;

        try (XmlCursor cursor = block.newCursor()) {
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
                    XWPFSDTBlock c = new XWPFSDTBlock(((CTSdtBlock) o), this);
                    bodyElements.add(c);
                    contentControls.add(c);
                }
            }
        }
    }

    public CTSdtContentBlock getCtSdtContentBlock() {
        return ctSdtContentBlock;
    }

    @Override
    public POIXMLDocumentPart getPart() {
        if (part != null) {
            return part.getPart();
        }
        return null;
    }

    @Override
    public BodyType getPartType() {
        return part.getPartType();
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
        return contentControls;
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
    private boolean isCursorInSdtContentBlock(XmlCursor cursor) {
        try (XmlCursor verify = cursor.newCursor()) {
            verify.toParent();
            return verify.getObject() == this.ctSdtContentBlock;
        }
    }

    @Override
    public XWPFParagraph insertNewParagraph(XmlCursor cursor) {
        if (isCursorInSdtContentBlock(cursor)) {
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
        XWPFParagraph p = new XWPFParagraph(ctSdtContentBlock.addNewP(), this);
        bodyElements.add(p);
        paragraphs.add(p);
        return p;
    }

    @Override
    public XWPFTable createTable() {
        XWPFTable table = new XWPFTable(ctSdtContentBlock.addNewTbl(), this);
        bodyElements.add(table);
        tables.add(table);
        return table;
    }

    @Override
    public void setSDTBlock(int pos, XWPFSDTBlock sdt) {
        contentControls.set(pos, sdt);
        ctSdtContentBlock.setSdtArray(pos, sdt.getCtSdtBlock());
    }

    @Override
    public XWPFSDTBlock createSdt() {
        XWPFSDTBlock sdt = new XWPFSDTBlock(ctSdtContentBlock.addNewSdt(), this);
        bodyElements.add(sdt);
        contentControls.add(sdt);
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
        for (int i = 0; i < contentControls.size(); i++) {
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
        if (isCursorInSdtContentBlock(cursor)) {
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
        if (isCursorInSdtContentBlock(cursor)) {
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
                contentControls.add(0, newSdtBlock);
            } else {
                int pos = contentControls.indexOf(getSdtBlock((CTSdtBlock) o)) + 1;
                contentControls.add(pos, newSdtBlock);
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
        for (CTTbl tbl : ctSdtContentBlock.getTblArray()) {
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
                ctSdtContentBlock.removeTbl(tablePos);
            }
            if (type == BodyElementType.PARAGRAPH) {
                int paraPos = getParagraphPos(pos);
                paragraphs.remove(paraPos);
                ctSdtContentBlock.removeP(paraPos);
            }
            if (type == BodyElementType.CONTENTCONTROL) {
                int sdtPos = getSDTPos(pos);
                contentControls.remove(sdtPos);
                ctSdtContentBlock.removeSdt(sdtPos);
            }
            bodyElements.remove(pos);
            return true;
        }
        return false;
    }

    @Override
    public XWPFDocument getXWPFDocument() {
        return part.getDocument();
    }

    public IBodyElement cloneExistingIBodyElement(IBodyElement elem) {
        if (elem instanceof XWPFParagraph) {
            CTP ctp = ctSdtContentBlock.addNewP();
            ctp.set(((XWPFParagraph) elem).getCTP());
            XWPFParagraph p = new XWPFParagraph(ctp, this);
            paragraphs.add(p);
            bodyElements.add(p);
            return p;
        } else if (elem instanceof XWPFTable) {
            if (((XWPFTable) elem).fetchTblText().toString().equals("")) {
                CTP ctp = ctSdtContentBlock.addNewP();
                XWPFParagraph p = new XWPFParagraph(ctp, this);
                paragraphs.add(p);
                bodyElements.add(p);
                return p;
            }
            CTTbl ctTbl = ctSdtContentBlock.addNewTbl();
            ctTbl.set(((XWPFTable) elem).getCTTbl());
            XWPFTable tbl = new XWPFTable(ctTbl, this);
            tables.add(tbl);
            bodyElements.add(tbl);
            return tbl;
        } else if (elem instanceof XWPFSDTBlock) {
            CTSdtBlock ctSdtBlock = ctSdtContentBlock.addNewSdt();
            ctSdtBlock.set(((XWPFSDTBlock) elem).getCtSdtBlock());
            XWPFSDTBlock sdtBlock = new XWPFSDTBlock(ctSdtBlock, this);
            contentControls.add(sdtBlock);
            bodyElements.add(sdtBlock);
            return sdtBlock;
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
        return getBodyElementSpecificPos(pos, contentControls);
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

    @Override
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

    @Override
    public String toString() {
        return getText();
    }

    public IBodyElement getParent() {
        return part;
    }
}
