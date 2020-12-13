/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.xslf.usermodel;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.poi.ooxml.util.POIXMLUnits;
import org.apache.poi.sl.draw.DrawFactory;
import org.apache.poi.sl.draw.DrawTableShape;
import org.apache.poi.sl.draw.DrawTextShape;
import org.apache.poi.sl.usermodel.TableShape;
import org.apache.poi.util.Internal;
import org.apache.poi.util.Units;
import org.apache.poi.xddf.usermodel.text.XDDFTextBody;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.values.XmlAnyTypeImpl;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGraphicalObjectData;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNonVisualDrawingProps;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTable;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTableCol;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTableRow;
import org.openxmlformats.schemas.presentationml.x2006.main.CTGraphicalObjectFrame;
import org.openxmlformats.schemas.presentationml.x2006.main.CTGraphicalObjectFrameNonVisual;

/**
 * Represents a table in a .pptx presentation
 */
public class XSLFTable extends XSLFGraphicFrame implements Iterable<XSLFTableRow>,
    TableShape<XSLFShape,XSLFTextParagraph> {
    /* package */ static final String TABLE_URI = "http://schemas.openxmlformats.org/drawingml/2006/table";

    private final CTTable _table;
    private final List<XSLFTableRow> _rows;

    /*package*/ XSLFTable(CTGraphicalObjectFrame shape, XSLFSheet sheet){
        super(shape, sheet);

        CTGraphicalObjectData god = shape.getGraphic().getGraphicData();
        XmlCursor xc = god.newCursor();
        try {
            if (!xc.toChild(XSLFRelation.NS_DRAWINGML, "tbl")) {
                throw new IllegalStateException("a:tbl element was not found in\n " + god);
            }

            XmlObject xo = xc.getObject();
            // Pesky XmlBeans bug - see Bugzilla #49934
            // it never happens when using poi-ooxml-full jar but may happen with the abridged poi-ooxml-lite jar
            if (xo instanceof XmlAnyTypeImpl){
                String errStr =
                    "Schemas (*.xsb) for CTTable can't be loaded - usually this happens when OSGI " +
                    "loading is used and the thread context classloader has no reference to " +
                    "the xmlbeans classes"
                ;
                throw new IllegalStateException(errStr);
            }
            _table = (CTTable)xo;
        } finally {
            xc.dispose();
        }

        _rows = new ArrayList<>(_table.sizeOfTrArray());
        for(CTTableRow row : _table.getTrList()) {
            _rows.add(new XSLFTableRow(row, this));
        }
        updateRowColIndexes();
    }

    @Override
    public XSLFTableCell getCell(int row, int col) {
        if (row < 0 || _rows.size() <= row) {
            return null;
        }
        XSLFTableRow r = _rows.get(row);
        if (r == null) {
            // empty row
            return null;
        }
        List<XSLFTableCell> cells = r.getCells();
        if (col < 0 || cells.size() <= col) {
            return null;
        }
        // cell can be potentially empty ...
        return cells.get(col);
    }

    @Internal
    public CTTable getCTTable(){
        return _table;
    }

    @Override
    public int getNumberOfColumns() {
        return _table.getTblGrid().sizeOfGridColArray();
    }

    @Override
    public int getNumberOfRows() {
        return _table.sizeOfTrArray();
    }

    @Override
    public double getColumnWidth(int idx){
        return Units.toPoints(POIXMLUnits.parseLength(
                _table.getTblGrid().getGridColArray(idx).xgetW()));
    }

    @Override
    public void setColumnWidth(int idx, double width) {
        _table.getTblGrid().getGridColArray(idx).setW(Units.toEMU(width));
    }

    @Override
    public double getRowHeight(int row) {
        return Units.toPoints(POIXMLUnits.parseLength(_table.getTrArray(row).xgetH()));
    }

    @Override
    public void setRowHeight(int row, double height) {
        _table.getTrArray(row).setH(Units.toEMU(height));
    }

    @Override
    public Iterator<XSLFTableRow> iterator(){
        return _rows.iterator();
    }

    public List<XSLFTableRow> getRows(){
        return Collections.unmodifiableList(_rows);
    }

    public XSLFTableRow addRow(){
        CTTableRow tr = _table.addNewTr();
        XSLFTableRow row = initializeRow(tr);
        _rows.add(row);
        return row;
    }

    private XSLFTableRow initializeRow(CTTableRow tr) {
        XSLFTableRow row = new XSLFTableRow(tr, this);
        // default height is 20 points
        row.setHeight(20.0);
        for (int i = 0;  i < getNumberOfColumns(); i++) {
            row.addCell();
        }
        return row;
    }

    /**
     * Insert a new row at the given index.
     * @param rowIdx the row index.
     * @since POI 5.0.0
     */
    public XSLFTableRow insertRow(int rowIdx) {
        if (getNumberOfRows() < rowIdx) {
            throw new IndexOutOfBoundsException("Cannot insert row at " + rowIdx + "; table has only " + getNumberOfRows() + "rows.");
        }
        CTTableRow tr = _table.insertNewTr(rowIdx);
        XSLFTableRow row = initializeRow(tr);
        _rows.add(rowIdx, row);
        return row;
    }

    /**
     * Remove the row on the given index
     * @param rowIdx the row index
     */
    public void removeRow(int rowIdx) {
        if (getNumberOfRows() < rowIdx) {
            throw new IndexOutOfBoundsException("Cannot remove row at " + rowIdx + "; table has only " + getNumberOfRows() + "rows.");
        }
        _table.removeTr(rowIdx);
        _rows.remove(rowIdx);
        updateRowColIndexes();
    }

    /**
     * Add a new column at the end of the table.
     * @since POI 4.1.2
     */
    public void addColumn() {
        long width = POIXMLUnits.parseLength(_table.getTblGrid().getGridColArray(getNumberOfColumns() - 1).xgetW());
        CTTableCol col = _table.getTblGrid().addNewGridCol();
        col.setW(width);
        for (XSLFTableRow row : _rows) {
            XSLFTableCell cell = row.addCell();
            new XDDFTextBody(cell, cell.getTextBody(true)).initialize();
        }
    }

    /**
     * Insert a new column at the given index.
     * @param colIdx the column index.
     * @since POI 4.1.2
     */
    public void insertColumn(int colIdx) {
        if (getNumberOfColumns() < colIdx) {
            throw new IndexOutOfBoundsException("Cannot insert column at " + colIdx + "; table has only " + getNumberOfColumns() + "columns.");
        }
        long width = POIXMLUnits.parseLength(_table.getTblGrid().getGridColArray(colIdx).xgetW());
        CTTableCol col = _table.getTblGrid().insertNewGridCol(colIdx);
        col.setW(width);
        for (XSLFTableRow row : _rows) {
            XSLFTableCell cell = row.insertCell(colIdx);
            new XDDFTextBody(cell, cell.getTextBody(true)).initialize();
        }
    }

    /**
     * Remove the column at the given index.
     * @param colIdx the column index.
     * @since POI 4.1.2
     */
    public void removeColumn(int colIdx) {
        if (getNumberOfColumns() < colIdx) {
            throw new IndexOutOfBoundsException("Cannot remove column at " + colIdx + "; table has only " + getNumberOfColumns() + "columns.");
        }
        _table.getTblGrid().removeGridCol(colIdx);
        for (XSLFTableRow row : _rows) {
            row.removeCell(colIdx);
        }
    }

    static CTGraphicalObjectFrame prototype(int shapeId){
        CTGraphicalObjectFrame frame = CTGraphicalObjectFrame.Factory.newInstance();
        CTGraphicalObjectFrameNonVisual nvGr = frame.addNewNvGraphicFramePr();

        CTNonVisualDrawingProps cnv = nvGr.addNewCNvPr();
        cnv.setName("Table " + shapeId);
        cnv.setId(shapeId);
        nvGr.addNewCNvGraphicFramePr().addNewGraphicFrameLocks().setNoGrp(true);
        nvGr.addNewNvPr();

        frame.addNewXfrm();
        CTGraphicalObjectData gr = frame.addNewGraphic().addNewGraphicData();
        XmlCursor grCur = gr.newCursor();
        grCur.toNextToken();
        grCur.beginElement(new QName(XSLFRelation.NS_DRAWINGML, "tbl"));

        CTTable tbl = CTTable.Factory.newInstance();
        tbl.addNewTblPr();
        tbl.addNewTblGrid();
        XmlCursor tblCur = tbl.newCursor();

        tblCur.moveXmlContents(grCur);
        tblCur.dispose();
        grCur.dispose();
        gr.setUri(TABLE_URI);
        return frame;
    }

    /**
     * Merge cells of a table
     */
    @SuppressWarnings("unused")
    public void mergeCells(int firstRow, int lastRow, int firstCol, int lastCol) {

    	if(firstRow > lastRow) {
    		throw new IllegalArgumentException(
    			"Cannot merge, first row > last row : "
    			+ firstRow + " > " + lastRow
    		);
    	}

    	if(firstCol > lastCol) {
    		throw new IllegalArgumentException(
    			"Cannot merge, first column > last column : "
    			+ firstCol + " > " + lastCol
    		);
    	}

    	int rowSpan = (lastRow - firstRow) + 1;
    	boolean mergeRowRequired = rowSpan > 1;

    	int colSpan = (lastCol - firstCol) + 1;
    	boolean mergeColumnRequired = colSpan > 1;

    	for(int i = firstRow; i <= lastRow; i++) {

    		XSLFTableRow row = _rows.get(i);

    		for(int colPos = firstCol; colPos <= lastCol; colPos++) {

    			XSLFTableCell cell = row.getCells().get(colPos);

    			if(mergeRowRequired) {
	    			if(i == firstRow) {
	    				cell.setRowSpan(rowSpan);
	    			} else {
	    				cell.setVMerge();
	    			}
    			}
    			if(mergeColumnRequired) {
    				if(colPos == firstCol) {
    					cell.setGridSpan(colSpan);
    				} else {
    					cell.setHMerge();
    				}
    			}
    		}
    	}
    }

    /**
     * Get assigned TableStyle
     *
     * @return the assigned TableStyle
     *
     * @since POI 3.15-beta2
     */
    protected XSLFTableStyle getTableStyle() {
        CTTable tab = getCTTable();
        // TODO: support inline table style
        if (!tab.isSetTblPr() || !tab.getTblPr().isSetTableStyleId()) {
            return null;
        }

        String styleId = tab.getTblPr().getTableStyleId();
        XSLFTableStyles styles = getSheet().getSlideShow().getTableStyles();
        for (XSLFTableStyle style : styles.getStyles()) {
            if (style.getStyleId().equals(styleId)) {
                return style;
            }
        }
        return null;
    }

    /* package */ void updateRowColIndexes() {
        int rowIdx = 0;
        for (XSLFTableRow xr : this) {
            int colIdx = 0;
            for (XSLFTableCell tc : xr) {
                tc.setRowColIndex(rowIdx, colIdx);
                colIdx++;
            }
            rowIdx++;
        }
    }

    /**
     * Calculates the bounding boxes of all cells and updates the dimension of the table
     */
    public void updateCellAnchor() {
        int rows = getNumberOfRows();
        int cols = getNumberOfColumns();

        double[] colWidths = new double[cols];
        double[] rowHeights = new double[rows];

        for (int row=0; row<rows; row++) {
            rowHeights[row] = getRowHeight(row);
        }
        for (int col=0; col<cols; col++) {
            colWidths[col] = getColumnWidth(col);
        }

        Rectangle2D tblAnc = getAnchor();
        DrawFactory df = DrawFactory.getInstance(null);

        double nextY = tblAnc.getY();
        double nextX = tblAnc.getX();

        // #1 pass - determine row heights, the height values might be too low or 0 ...
        for (int row=0; row<rows; row++) {
            double maxHeight = 0;
            for (int col=0; col<cols; col++) {
                XSLFTableCell tc = getCell(row, col);
                if (tc == null || tc.getGridSpan() != 1 || tc.getRowSpan() != 1) {
                    continue;
                }
                // need to set the anchor before height calculation
                tc.setAnchor(new Rectangle2D.Double(0,0,colWidths[col],0));
                DrawTextShape dts = df.getDrawable(tc);
                maxHeight = Math.max(maxHeight, dts.getTextHeight());
            }
            rowHeights[row] = Math.max(rowHeights[row],maxHeight);
        }

        // #2 pass - init properties
        for (int row=0; row<rows; row++) {
            nextX = tblAnc.getX();
            for (int col=0; col<cols; col++) {
                Rectangle2D bounds = new Rectangle2D.Double(nextX, nextY, colWidths[col], rowHeights[row]);
                XSLFTableCell tc = getCell(row, col);
                if (tc != null) {
                    tc.setAnchor(bounds);
                    nextX += colWidths[col]+DrawTableShape.borderSize;
                }
            }
            nextY += rowHeights[row]+DrawTableShape.borderSize;
        }

        // #3 pass - update merge info
        for (int row=0; row<rows; row++) {
            for (int col=0; col<cols; col++) {
                XSLFTableCell tc = getCell(row, col);
                if (tc == null) {
                    continue;
                }
                Rectangle2D mergedBounds = tc.getAnchor();
                for (int col2=col+1; col2<col+tc.getGridSpan(); col2++) {
                    assert(col2 < cols);
                    XSLFTableCell tc2 = getCell(row, col2);
                    assert(tc2.getGridSpan() == 1 && tc2.getRowSpan() == 1);
                    mergedBounds.add(tc2.getAnchor());
                }
                for (int row2=row+1; row2<row+tc.getRowSpan(); row2++) {
                    assert(row2 < rows);
                    XSLFTableCell tc2 = getCell(row2, col);
                    assert(tc2.getGridSpan() == 1 && tc2.getRowSpan() == 1);
                    mergedBounds.add(tc2.getAnchor());
                }
                tc.setAnchor(mergedBounds);
            }
        }

        setAnchor(new Rectangle2D.Double(tblAnc.getX(),tblAnc.getY(),
                nextX-tblAnc.getX(),
                nextY-tblAnc.getY()));
    }
}
