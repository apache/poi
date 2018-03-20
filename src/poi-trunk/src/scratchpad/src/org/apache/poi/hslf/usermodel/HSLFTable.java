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

package org.apache.poi.hslf.usermodel;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.poi.ddf.AbstractEscherOptRecord;
import org.apache.poi.ddf.EscherArrayProperty;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherOptRecord;
import org.apache.poi.ddf.EscherProperties;
import org.apache.poi.ddf.EscherSimpleProperty;
import org.apache.poi.hslf.record.RecordTypes;
import org.apache.poi.sl.usermodel.ShapeContainer;
import org.apache.poi.sl.usermodel.TableShape;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.Units;

/**
 * Represents a table in a PowerPoint presentation
 *
 * @author Yegor Kozlov
 */
public final class HSLFTable extends HSLFGroupShape
implements HSLFShapeContainer, TableShape<HSLFShape,HSLFTextParagraph> {

    protected static final int BORDERS_ALL = 5;
    protected static final int BORDERS_OUTSIDE = 6;
    protected static final int BORDERS_INSIDE = 7;
    protected static final int BORDERS_NONE = 8;


    protected HSLFTableCell[][] cells;
    private int columnCount = -1;

    /**
     * Create a new Table of the given number of rows and columns
     *
     * @param numRows the number of rows
     * @param numCols the number of columns
     */
    protected HSLFTable(int numRows, int numCols) {
        this(numRows, numCols, null);
    }

    /**
     * Create a new Table of the given number of rows and columns
     *
     * @param numRows the number of rows
     * @param numCols the number of columns
     * @param parent the parent shape, or null if table is added to sheet
     */
    protected HSLFTable(int numRows, int numCols, ShapeContainer<HSLFShape,HSLFTextParagraph> parent) {
        super(parent);

        if(numRows < 1) {
            throw new IllegalArgumentException("The number of rows must be greater than 1");
        }
        if(numCols < 1) {
            throw new IllegalArgumentException("The number of columns must be greater than 1");
        }

        double x=0, y=0, tblWidth=0, tblHeight=0;
        cells = new HSLFTableCell[numRows][numCols];
        for (int i = 0; i < cells.length; i++) {
            x = 0;
            for (int j = 0; j < cells[i].length; j++) {
                cells[i][j] = new HSLFTableCell(this);
                Rectangle2D anchor = new Rectangle2D.Double(x, y, HSLFTableCell.DEFAULT_WIDTH, HSLFTableCell.DEFAULT_HEIGHT);
                cells[i][j].setAnchor(anchor);
                x += HSLFTableCell.DEFAULT_WIDTH;
            }
            y += HSLFTableCell.DEFAULT_HEIGHT;
        }
        tblWidth = x;
        tblHeight = y;
        setExteriorAnchor(new Rectangle2D.Double(0, 0, tblWidth, tblHeight));

        EscherContainerRecord spCont = (EscherContainerRecord) getSpContainer().getChild(0);
        AbstractEscherOptRecord opt = new EscherOptRecord();
        opt.setRecordId(RecordTypes.EscherUserDefined.typeID);
        opt.addEscherProperty(new EscherSimpleProperty(EscherProperties.GROUPSHAPE__TABLEPROPERTIES, 1));
        EscherArrayProperty p = new EscherArrayProperty((short)(0x4000 | EscherProperties.GROUPSHAPE__TABLEROWPROPERTIES), false, null);
        p.setSizeOfElements(0x0004);
        p.setNumberOfElementsInArray(numRows);
        p.setNumberOfElementsInMemory(numRows);
        opt.addEscherProperty(p);
        spCont.addChildBefore(opt, RecordTypes.EscherClientAnchor.typeID);
    }

    /**
     * Create a Table object and initialize it from the supplied Record container.
     *
     * @param escherRecord <code>EscherSpContainer</code> container which holds information about this shape
     * @param parent       the parent of the shape
     */
    protected HSLFTable(EscherContainerRecord escherRecord, ShapeContainer<HSLFShape,HSLFTextParagraph> parent) {
        super(escherRecord, parent);
    }

    @Override
    public HSLFTableCell getCell(int row, int col) {
        if (row < 0 || cells.length <= row) {
            return null;
        }
        HSLFTableCell[] r = cells[row];
        if (r == null || col < 0 || r.length <= col) {
            // empty row
            return null;
        }
        // cell can be potentially empty ...
        return r[col];
    }

    @Override
    public int getNumberOfColumns() {
        if (columnCount == -1) {
            // check all rows in case of merged rows
            for (HSLFTableCell[] hc : cells) {
                if (hc != null) {
                    columnCount = Math.max(columnCount, hc.length);
                }
            }
        }
        return columnCount;
    }

    @Override
    public int getNumberOfRows() {
        return cells.length;
    }

    @Override
    protected void afterInsert(HSLFSheet sh){
        super.afterInsert(sh);

        Set<HSLFLine> lineSet = new HashSet<>();
        for (HSLFTableCell row[] : cells) {
            for (HSLFTableCell c : row) {
                addShape(c);
                for (HSLFLine bt : new HSLFLine[]{ c.borderTop, c.borderRight, c.borderBottom, c.borderLeft }) {
                    if (bt != null) {
                        lineSet.add(bt);
                    }
                }
            }
        }

        for (HSLFLine l : lineSet) {
            addShape(l);
        }

        updateRowHeightsProperty();
    }

    private void cellListToArray() {
        List<HSLFTableCell> htc = new ArrayList<>();
        for (HSLFShape h : getShapes()) {
            if (h instanceof HSLFTableCell) {
                htc.add((HSLFTableCell)h);
            }
        }

        if (htc.isEmpty()) {
            throw new IllegalStateException("HSLFTable without HSLFTableCells");
        }
        
        SortedSet<Double> colSet = new TreeSet<>();
        SortedSet<Double> rowSet = new TreeSet<>();
        
        // #1 pass - determine cols and rows
        for (HSLFTableCell sh : htc) {
            Rectangle2D anchor = sh.getAnchor();
            colSet.add(anchor.getX());
            rowSet.add(anchor.getY());
        }
        cells = new HSLFTableCell[rowSet.size()][colSet.size()];
        
        List<Double> colLst = new ArrayList<>(colSet);
        List<Double> rowLst = new ArrayList<>(rowSet);
        
        // #2 pass - assign shape to table cells
        for (HSLFTableCell sh : htc) {
            Rectangle2D anchor = sh.getAnchor();
            int row = rowLst.indexOf(anchor.getY());
            int col = colLst.indexOf(anchor.getX());
            assert(row != -1 && col != -1);
            cells[row][col] = sh;
            
            // determine gridSpan / rowSpan
            int gridSpan = calcSpan(colLst, anchor.getWidth(), col);
            int rowSpan = calcSpan(rowLst, anchor.getHeight(), row);
            
            sh.setGridSpan(gridSpan);
            sh.setRowSpan(rowSpan);
        }        
    }

    private int calcSpan(List<Double> spaces, double totalSpace, int idx) {
        int span = 1;
        ListIterator<Double> li = spaces.listIterator(idx);
        double start = li.next();
        while (li.hasNext() && li.next()-start < totalSpace) {
            span++;
        }
        return span;
    }
    
    static class LineRect {
        final HSLFLine l;
        final double lx1, lx2, ly1, ly2;
        LineRect(HSLFLine l) {
            this.l = l;
            Rectangle2D r = l.getAnchor();
            lx1 = r.getMinX();
            lx2 = r.getMaxX();
            ly1 = r.getMinY();
            ly2 = r.getMaxY();
        }
        int leftFit(double x1, double x2, double y1, double y2) {
            return (int)(Math.abs(x1-lx1)+Math.abs(y1-ly1)+Math.abs(x1-lx2)+Math.abs(y2-ly2));
        }
        int topFit(double x1, double x2, double y1, double y2) {
            return (int)(Math.abs(x1-lx1)+Math.abs(y1-ly1)+Math.abs(x2-lx2)+Math.abs(y1-ly2));
        }
        int rightFit(double x1, double x2, double y1, double y2) {
            return (int)(Math.abs(x2-lx1)+Math.abs(y1-ly1)+Math.abs(x2-lx2)+Math.abs(y2-ly2));
        }
        int bottomFit(double x1, double x2, double y1, double y2) {
            return (int)(Math.abs(x1-lx1)+Math.abs(y2-ly1)+Math.abs(x2-lx2)+Math.abs(y2-ly2));
        }
    }

    private void fitLinesToCells() {
        List<LineRect> lines = new ArrayList<>();
        for (HSLFShape h : getShapes()) {
            if (h instanceof HSLFLine) {
                lines.add(new LineRect((HSLFLine)h));
            }
        }

        final int threshold = 5;

        // TODO: this only works for non-rotated tables
        for (HSLFTableCell[] tca : cells) {
            for (HSLFTableCell tc : tca) {
                if (tc == null) {
                    continue;
                }
                final Rectangle2D cellAnchor = tc.getAnchor();

                /**
                 * x1/y1 --------+
                 *   |           |
                 *   +---------x2/y2
                 */
                final double x1 = cellAnchor.getMinX();
                final double x2 = cellAnchor.getMaxX();
                final double y1 = cellAnchor.getMinY();
                final double y2 = cellAnchor.getMaxY();

                LineRect lline = null, tline = null, rline = null, bline = null;
                int lfit = Integer.MAX_VALUE, tfit = Integer.MAX_VALUE, rfit = Integer.MAX_VALUE, bfit = Integer.MAX_VALUE;

                for (LineRect lr : lines) {
                    // calculate border fit
                    int lfitx = lr.leftFit(x1, x2, y1, y2);
                    if (lfitx < lfit) {
                        lfit = lfitx;
                        lline = lr;
                    }

                    int tfitx = lr.topFit(x1, x2, y1, y2);
                    if (tfitx < tfit) {
                        tfit = tfitx;
                        tline = lr;
                    }

                    int rfitx = lr.rightFit(x1, x2, y1, y2);
                    if (rfitx < rfit) {
                        rfit = rfitx;
                        rline = lr;
                    }

                    int bfitx = lr.bottomFit(x1, x2, y1, y2);
                    if (bfitx < bfit) {
                        bfit = bfitx;
                        bline = lr;
                    }
                }

                if (lfit < threshold && lline != null) {
                    tc.borderLeft = lline.l;
                }
                if (tfit < threshold && tline != null) {
                    tc.borderTop = tline.l;
                }
                if (rfit < threshold && rline != null) {
                    tc.borderRight = rline.l;
                }
                if (bfit < threshold && bline != null) {
                    tc.borderBottom = bline.l;
                }
            }
        }
    }

    protected void initTable(){
        cellListToArray();
        fitLinesToCells();
    }

    /**
     * Assign the <code>SlideShow</code> this shape belongs to
     *
     * @param sheet owner of this shape
     */
    @Override
    public void setSheet(HSLFSheet sheet){
        super.setSheet(sheet);
        if (cells == null) {
            initTable();
        } else {
            for (HSLFTableCell cols[] : cells) {
                for (HSLFTableCell col : cols) {
                    col.setSheet(sheet);
                }
            }
        }
    }

    @Override
    public double getRowHeight(int row) {
        if (row < 0 || row >= cells.length) {
            throw new IllegalArgumentException("Row index '"+row+"' is not within range [0-"+(cells.length-1)+"]");
        }
        
        return cells[row][0].getAnchor().getHeight();
    }
    
    @Override
    public void setRowHeight(int row, final double height) {
        if (row < 0 || row >= cells.length) {
            throw new IllegalArgumentException("Row index '"+row+"' is not within range [0-"+(cells.length-1)+"]");
        }

        // update row height in the table properties
        AbstractEscherOptRecord opt = getEscherChild(RecordTypes.EscherUserDefined.typeID);
        EscherArrayProperty p = opt.lookup(EscherProperties.GROUPSHAPE__TABLEROWPROPERTIES);
        byte[] masterBytes = p.getElement(row);
        double currentHeight = Units.masterToPoints(LittleEndian.getInt(masterBytes, 0));
        LittleEndian.putInt(masterBytes, 0, Units.pointsToMaster(height));
        p.setElement(row, masterBytes);
        
        // move the cells
        double dy = height - currentHeight;
        for (int i = row; i < cells.length; i++) {
            for (HSLFTableCell c : cells[i]) {
                if (c == null) {
                    continue;
                }
                Rectangle2D anchor = c.getAnchor();
                if(i == row) {
                    anchor.setRect(anchor.getX(), anchor.getY(), anchor.getWidth(), height);
                } else {
                    anchor.setRect(anchor.getX(), anchor.getY()+dy, anchor.getWidth(), anchor.getHeight());
                }
                c.setAnchor(anchor);
            }
        }
        Rectangle2D tblanchor = getAnchor();
        tblanchor.setRect(tblanchor.getX(), tblanchor.getY(), tblanchor.getWidth(), tblanchor.getHeight() + dy);
        setExteriorAnchor(tblanchor);
    }

    @Override
    public double getColumnWidth(int col) {
        if (col < 0 || col >= cells[0].length) {
            throw new IllegalArgumentException("Column index '"+col+"' is not within range [0-"+(cells[0].length-1)+"]");
        }
        
        // TODO: check for merged cols
        return cells[0][col].getAnchor().getWidth();
    }

    @Override
    public void setColumnWidth(int col, final double width){
        if (col < 0 || col >= cells[0].length) {
            throw new IllegalArgumentException("Column index '"+col+"' is not within range [0-"+(cells[0].length-1)+"]");
        }
        double currentWidth = cells[0][col].getAnchor().getWidth();
        double dx = width - currentWidth;
        for (HSLFTableCell cols[] : cells) {
            Rectangle2D anchor = cols[col].getAnchor();
            anchor.setRect(anchor.getX(), anchor.getY(), width, anchor.getHeight());
            cols[col].setAnchor(anchor);

            if (col < cols.length - 1) {
                for (int j = col+1; j < cols.length; j++) {
                    anchor = cols[j].getAnchor();
                    anchor.setRect(anchor.getX()+dx, anchor.getY(), anchor.getWidth(), anchor.getHeight());
                    cols[j].setAnchor(anchor);
                }
            }
        }
        Rectangle2D tblanchor = getAnchor();
        tblanchor.setRect(tblanchor.getX(), tblanchor.getY(), tblanchor.getWidth() + dx, tblanchor.getHeight());
        setExteriorAnchor(tblanchor);
    }

    protected HSLFTableCell getRelativeCell(HSLFTableCell origin, int row, int col) {
        int thisRow = 0, thisCol = 0;
        boolean found = false;
        outer: for (HSLFTableCell[] tca : cells) {
            thisCol = 0;
            for (HSLFTableCell tc : tca) {
                if (tc == origin) {
                    found = true;
                    break outer;
                }
                thisCol++;
            }
            thisRow++;
        }

        int otherRow = thisRow + row;
        int otherCol = thisCol + col;
        return (found
            && 0 <= otherRow && otherRow < cells.length
            && 0 <= otherCol && otherCol < cells[otherRow].length)
            ? cells[otherRow][otherCol] : null;
    }

    @Override
    protected void moveAndScale(Rectangle2D anchorDest){
        super.moveAndScale(anchorDest);
        updateRowHeightsProperty();
    }

    private void updateRowHeightsProperty() {
        AbstractEscherOptRecord opt = getEscherChild(RecordTypes.EscherUserDefined.typeID);
        EscherArrayProperty p = opt.lookup(EscherProperties.GROUPSHAPE__TABLEROWPROPERTIES);
        byte[] val = new byte[4];
        for (int rowIdx = 0; rowIdx < cells.length; rowIdx++) {
            int rowHeight = Units.pointsToMaster(cells[rowIdx][0].getAnchor().getHeight());
            LittleEndian.putInt(val, 0, rowHeight);
            p.setElement(rowIdx, val);
        }
    }
}
