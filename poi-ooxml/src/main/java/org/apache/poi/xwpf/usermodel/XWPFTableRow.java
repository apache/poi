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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ooxml.util.POIXMLUnits;
import org.apache.poi.util.Internal;
import org.apache.poi.util.Units;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.officeDocument.x2006.sharedTypes.STOnOff1;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHeight;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTOnOff;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSdtCell;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTrPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STHeightRule;


/**
 * A row within an {@link XWPFTable}. Rows mostly just have
 * sizings and stylings, the interesting content lives inside
 * the child {@link XWPFTableCell}s
 */
public class XWPFTableRow {
    private final CTRow ctRow;
    private final XWPFTable table;
    private List<XWPFTableCell> tableCells;

    public XWPFTableRow(CTRow row, XWPFTable table) {
        this.table = table;
        this.ctRow = row;
        getTableCells();
    }

    @Internal
    public CTRow getCtRow() {
        return ctRow;
    }

    /**
     * create a new XWPFTableCell and add it to the tableCell-list of this tableRow
     *
     * @return the newly created XWPFTableCell
     */
    public XWPFTableCell createCell() {
        XWPFTableCell tableCell = new XWPFTableCell(ctRow.addNewTc(), this, table.getBody());
        ensureBlockLevelElement(tableCell);
        tableCells.add(tableCell);
        return tableCell;
    }

    public XWPFTableCell getCell(int pos) {
        if (pos >= 0 && pos < ctRow.sizeOfTcArray()) {
            return getTableCells().get(pos);
        }
        return null;
    }

    public void removeCell(int pos) {
        if (pos >= 0 && pos < ctRow.sizeOfTcArray()) {
            tableCells.remove(pos);
            ctRow.removeTc(pos);
        }
    }

    /**
     * adds a new TableCell at the end of this tableRow
     */
    public XWPFTableCell addNewTableCell() {
        CTTc cell = ctRow.addNewTc();
        XWPFTableCell tableCell = new XWPFTableCell(cell, this, table.getBody());
        ensureBlockLevelElement(tableCell);
        tableCells.add(tableCell);
        return tableCell;
    }

    private void ensureBlockLevelElement(XWPFTableCell tableCell) {
        // If a table cell does not include at least one block-level element,
        // then this document shall be considered corrupt.
        if (tableCell.getParagraphs().isEmpty()) {
            tableCell.addParagraph();
        }
    }

    /**
     * This element specifies the height of the current table row within the
     * current table. This height shall be used to determine the resulting
     * height of the table row, which may be absolute or relative (depending on
     * its attribute values). If omitted, then the table row shall automatically
     * resize its height to the height required by its contents (the equivalent
     * of an hRule value of auto).
     *
     * @return height
     */
    public int getHeight() {
        CTTrPr properties = getTrPr();
        return properties.sizeOfTrHeightArray() == 0 ? 0 : (int) Units.toDXA(POIXMLUnits.parseLength(properties.getTrHeightArray(0).xgetVal()));
    }

    /**
     * This element specifies the height of the current table row within the
     * current table. This height shall be used to determine the resulting
     * height of the table row, which may be absolute or relative (depending on
     * its attribute values). If omitted, then the table row shall automatically
     * resize its height to the height required by its contents (the equivalent
     * of an hRule value of auto).
     */
    public void setHeight(int height) {
        CTTrPr properties = getTrPr();
        CTHeight h = properties.sizeOfTrHeightArray() == 0 ? properties.addNewTrHeight() : properties.getTrHeightArray(0);
        h.setVal(new BigInteger(Integer.toString(height)));
    }

    /**
     * Returns the meaning of the height specified for this table row.
     * <p>
     * If hRule is omitted, then its value shall be assumed to be auto.
     * </p>
     *
     * @return the height rule of this row.
     */
    public TableRowHeightRule getHeightRule() {
        CTTrPr properties = getTrPr();
        return properties.sizeOfTrHeightArray() == 0 ? TableRowHeightRule.AUTO
                : TableRowHeightRule.valueOf(properties.getTrHeightArray(0).getHRule().intValue());
    }

    /**
     * Specifies the height rule for this table row.
     * <p>
     * If the value of hRule is auto, then the table row's height should be automatically determined based on the
     * height of its contents. The h value is ignored.
     * If the value of hRule is atLeast, then the table row's height should be at least the value the h attribute.
     * If the value of hRule is exact, then the table row's height should be exactly the value of the h attribute.
     * </p>
     *
     * @param heightRule the height rule to apply to this row.
     */
    public void setHeightRule(TableRowHeightRule heightRule) {
        CTTrPr properties = getTrPr();
        CTHeight h = properties.sizeOfTrHeightArray() == 0 ? properties.addNewTrHeight() : properties.getTrHeightArray(0);
        h.setHRule(STHeightRule.Enum.forInt(heightRule.getValue()));
    }

    private CTTrPr getTrPr() {
        return (ctRow.isSetTrPr()) ? ctRow.getTrPr() : ctRow.addNewTrPr();
    }

    public XWPFTable getTable() {
        return table;
    }

    /**
     * create and return a list of all XWPFTableCell
     * who belongs to this row
     *
     * @return a list of {@link XWPFTableCell}
     */
    public List<ICell> getTableICells() {

        List<ICell> cells = new ArrayList<>();
        //Can't use ctRow.getTcList because that only gets table cells
        //Can't use ctRow.getSdtList because that only gets sdts that are at cell level
        try (XmlCursor cursor = ctRow.newCursor()) {
            cursor.selectPath("./*");
            while (cursor.toNextSelection()) {
                XmlObject o = cursor.getObject();
                if (o instanceof CTTc) {
                    cells.add(new XWPFTableCell((CTTc) o, this, table.getBody()));
                } else if (o instanceof CTSdtCell) {
                    cells.add(new XWPFSDTCell((CTSdtCell) o, this, table.getBody()));
                }
            }
        }
        return cells;
    }

    /**
     * create and return a list of all XWPFTableCell
     * who belongs to this row
     *
     * @return a list of {@link XWPFTableCell}
     */
    public List<XWPFTableCell> getTableCells() {
        if (tableCells == null) {
            List<XWPFTableCell> cells = new ArrayList<>();
            for (CTTc tableCell : ctRow.getTcArray()) {
                cells.add(new XWPFTableCell(tableCell, this, table.getBody()));
            }
            //TODO: it is possible to have an SDT that contains a cell in within a row
            //need to modify this code so that it pulls out SDT wrappers around cells, too.

            this.tableCells = cells;
        }
        return tableCells;
    }

    /**
     * returns the XWPFTableCell which belongs to the CTTC cell
     * if there is no XWPFTableCell which belongs to the parameter CTTc cell null will be returned
     */
    public XWPFTableCell getTableCell(CTTc cell) {
        for (XWPFTableCell tableCell : tableCells) {
            if (tableCell.getCTTc() == cell)
                return tableCell;
        }
        return null;
    }

    /**
     * Return true if the "can't split row" value is true. The logic for this
     * attribute is a little unusual: a TRUE value means DON'T allow rows to
     * split, FALSE means allow rows to split.
     *
     * @return true if rows can't be split, false otherwise.
     */
    public boolean isCantSplitRow() {
        boolean isCant = false;
        if (ctRow.isSetTrPr()) {
            CTTrPr trpr = getTrPr();
            if (trpr.sizeOfCantSplitArray() > 0) {
                CTOnOff onoff = trpr.getCantSplitArray(0);
                isCant = !onoff.isSetVal() || POIXMLUnits.parseOnOff(onoff.xgetVal());
            }
        }
        return isCant;
    }

    /**
     * Controls whether to allow this table row to split across pages.
     * The logic for this attribute is a little unusual: a true value means
     * DON'T allow rows to split, false means allow rows to split.
     *
     * @param split - if true, don't allow row to be split. If false, allow
     *              row to be split.
     */
    public void setCantSplitRow(boolean split) {
        CTTrPr trpr = getTrPr();
        CTOnOff onoff = (trpr.sizeOfCantSplitArray() > 0 ? trpr.getCantSplitArray(0) : trpr.addNewCantSplit());
        onoff.setVal(split ? STOnOff1.ON : STOnOff1.OFF);
    }

    /**
     * Return true if a table's header row should be repeated at the top of a
     * table split across pages. NOTE - Word will not repeat a table row unless
     * all preceding rows of the table are also repeated. This function returns
     * false if the row will not be repeated even if the repeat tag is present
     * for this row.
     *
     * @return true if table's header row should be repeated at the top of each
     * page of table, false otherwise.
     */
    public boolean isRepeatHeader() {
        boolean repeat = false;
        for (XWPFTableRow row : table.getRows()) {
            repeat = row.getRepeat();
            if (row == this || !repeat) {
                break;
            }
        }
        return repeat;
    }

    private boolean getRepeat() {
        boolean repeat = false;
        if (ctRow.isSetTrPr()) {
            CTTrPr trpr = getTrPr();
            if (trpr.sizeOfTblHeaderArray() > 0) {
                CTOnOff rpt = trpr.getTblHeaderArray(0);
                repeat = !rpt.isSetVal() || POIXMLUnits.parseOnOff(rpt.xgetVal());
            }
        }
        return repeat;
    }

    /**
     * This attribute controls whether to repeat a table's header row at the top
     * of a table split across pages. NOTE - for a row to be repeated, all preceding
     * rows in the table must also be repeated.
     *
     * @param repeat - if TRUE, repeat header row at the top of each page of table;
     *               if FALSE, don't repeat header row.
     */
    public void setRepeatHeader(boolean repeat) {
        CTTrPr trpr = getTrPr();
        CTOnOff onoff = (trpr.sizeOfTblHeaderArray() > 0 ? trpr.getTblHeaderArray(0) : trpr.addNewTblHeader());
        onoff.setVal(repeat ? STOnOff1.ON : STOnOff1.OFF);
    }
}
