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

package org.apache.poi.hslf.model;

import org.apache.poi.ddf.*;
import org.apache.poi.util.LittleEndian;

import java.util.*;
import java.util.List;
import java.awt.*;

/**
 * Represents a table in a PowerPoint presentation
 *
 * @author Yegor Kozlov
 */
public final class Table extends ShapeGroup {

    protected static final int BORDER_TOP = 1;
    protected static final int BORDER_RIGHT = 2;
    protected static final int BORDER_BOTTOM = 3;
    protected static final int BORDER_LEFT = 4;

    protected static final int BORDERS_ALL = 5;
    protected static final int BORDERS_OUTSIDE = 6;
    protected static final int BORDERS_INSIDE = 7;
    protected static final int BORDERS_NONE = 8;


    protected TableCell[][] cells;

    /**
     * Create a new Table of the given number of rows and columns
     *
     * @param numrows the number of rows
     * @param numcols the number of columns
     */
    public Table(int numrows, int numcols) {
        super();

        if(numrows < 1) throw new IllegalArgumentException("The number of rows must be greater than 1");
        if(numcols < 1) throw new IllegalArgumentException("The number of columns must be greater than 1");

        int x=0, y=0, tblWidth=0, tblHeight=0;
        cells = new TableCell[numrows][numcols];
        for (int i = 0; i < cells.length; i++) {
            x = 0;
            for (int j = 0; j < cells[i].length; j++) {
                cells[i][j] = new TableCell(this);
                Rectangle anchor = new Rectangle(x, y, TableCell.DEFAULT_WIDTH, TableCell.DEFAULT_HEIGHT);
                cells[i][j].setAnchor(anchor);
                x += TableCell.DEFAULT_WIDTH;
            }
            y += TableCell.DEFAULT_HEIGHT;
        }
        tblWidth = x;
        tblHeight = y;
        setAnchor(new Rectangle(0, 0, tblWidth, tblHeight));

        EscherContainerRecord spCont = (EscherContainerRecord) getSpContainer().getChild(0);
        EscherOptRecord opt = new EscherOptRecord();
        opt.setRecordId((short)0xF122);
        opt.addEscherProperty(new EscherSimpleProperty((short)0x39F, 1));
        EscherArrayProperty p = new EscherArrayProperty((short)0x43A0, false, null);
        p.setSizeOfElements(0x0004);
        p.setNumberOfElementsInArray(numrows);
        p.setNumberOfElementsInMemory(numrows);
        opt.addEscherProperty(p);
        List<EscherRecord> lst = spCont.getChildRecords();
        lst.add(lst.size()-1, opt);
        spCont.setChildRecords(lst);
    }

    /**
     * Create a Table object and initilize it from the supplied Record container.
     *
     * @param escherRecord <code>EscherSpContainer</code> container which holds information about this shape
     * @param parent       the parent of the shape
     */
    public Table(EscherContainerRecord escherRecord, Shape parent) {
        super(escherRecord, parent);
    }

    /**
     * Gets a cell
     *
     * @param row the row index (0-based)
     * @param col the column index (0-based)
     * @return the cell
     */
    public TableCell getCell(int row, int col) {
        return cells[row][col];
    }

    public int getNumberOfColumns() {
        return cells[0].length;
    }
    public int getNumberOfRows() {
        return cells.length;
    }

    protected void afterInsert(Sheet sh){
        super.afterInsert(sh);

        EscherContainerRecord spCont = (EscherContainerRecord) getSpContainer().getChild(0);
        List<EscherRecord> lst = spCont.getChildRecords();
        EscherOptRecord opt = (EscherOptRecord)lst.get(lst.size()-2);
        EscherArrayProperty p = (EscherArrayProperty)opt.getEscherProperty(1);
        for (int i = 0; i < cells.length; i++) {
            TableCell cell = cells[i][0];
            int rowHeight = cell.getAnchor().height*MASTER_DPI/POINT_DPI;
            byte[] val = new byte[4];
            LittleEndian.putInt(val, rowHeight);
            p.setElement(i, val);
            for (int j = 0; j < cells[i].length; j++) {
                TableCell c = cells[i][j];
                addShape(c);

                Line bt = c.getBorderTop();
                if(bt != null) addShape(bt);

                Line br = c.getBorderRight();
                if(br != null) addShape(br);

                Line bb = c.getBorderBottom();
                if(bb != null) addShape(bb);

                Line bl = c.getBorderLeft();
                if(bl != null) addShape(bl);

            }
        }

    }

    protected void initTable(){
        Shape[] sh = getShapes();
        Arrays.sort(sh, new Comparator(){
            public int compare( Object o1, Object o2 ) {
                Rectangle anchor1 = ((Shape)o1).getAnchor();
                Rectangle anchor2 = ((Shape)o2).getAnchor();
                int delta = anchor1.y - anchor2.y;
                if(delta == 0) delta = anchor1.x - anchor2.x;
                return delta;
            }
        });
        int y0 = -1;
        int maxrowlen = 0;
        ArrayList lst = new ArrayList();
        ArrayList row = null;
        for (int i = 0; i < sh.length; i++) {
            if(sh[i] instanceof TextShape){
                Rectangle anchor = sh[i].getAnchor();
                if(anchor.y != y0){
                    y0 = anchor.y;
                    row = new ArrayList();
                    lst.add(row);
                }
                row.add(sh[i]);
                maxrowlen = Math.max(maxrowlen, row.size());
            }
        }
        cells = new TableCell[lst.size()][maxrowlen];
        for (int i = 0; i < lst.size(); i++) {
            row = (ArrayList)lst.get(i);
            for (int j = 0; j < row.size(); j++) {
                TextShape tx = (TextShape)row.get(j);
                cells[i][j] = new TableCell(tx.getSpContainer(), getParent());
                cells[i][j].setSheet(tx.getSheet());
            }
        }
    }

    /**
     * Assign the <code>SlideShow</code> this shape belongs to
     *
     * @param sheet owner of this shape
     */
    public void setSheet(Sheet sheet){
        super.setSheet(sheet);
        if(cells == null) initTable();
    }

    /**
     * Sets the row height.
     *
     * @param row the row index (0-based)
     * @param height the height to set (in pixels)
     */
    public void setRowHeight(int row, int height){
        int currentHeight = cells[row][0].getAnchor().height;
        int dy = height - currentHeight;

        for (int i = row; i < cells.length; i++) {
            for (int j = 0; j < cells[i].length; j++) {
                Rectangle anchor = cells[i][j].getAnchor();
                if(i == row) anchor.height = height;
                else anchor.y += dy;
                cells[i][j].setAnchor(anchor);
            }
        }
        Rectangle tblanchor = getAnchor();
        tblanchor.height += dy;
        setAnchor(tblanchor);

    }

    /**
     * Sets the column width.
     *
     * @param col the column index (0-based)
     * @param width the width to set (in pixels)
     */
    public void setColumnWidth(int col, int width){
        int currentWidth = cells[0][col].getAnchor().width;
        int dx = width - currentWidth;
        for (int i = 0; i < cells.length; i++) {
            Rectangle anchor = cells[i][col].getAnchor();
            anchor.width = width;
            cells[i][col].setAnchor(anchor);

            if(col < cells[i].length - 1) for (int j = col+1; j < cells[i].length; j++) {
                anchor = cells[i][j].getAnchor();
                anchor.x += dx;
                cells[i][j].setAnchor(anchor);
            }
        }
        Rectangle tblanchor = getAnchor();
        tblanchor.width += dx;
        setAnchor(tblanchor);
    }

    /**
     * Format the table and apply the specified Line to all cell boundaries,
     * both outside and inside
     *
     * @param line the border line
     */
    public void setAllBorders(Line line){
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[i].length; j++) {
                TableCell cell = cells[i][j];
                cell.setBorderTop(cloneBorder(line));
                cell.setBorderLeft(cloneBorder(line));
                if(j == cells[i].length - 1) cell.setBorderRight(cloneBorder(line));
                if(i == cells.length - 1) cell.setBorderBottom(cloneBorder(line));
            }
        }
    }

    /**
     * Format the outside border using the specified Line object
     *
     * @param line the border line
     */
    public void setOutsideBorders(Line line){
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[i].length; j++) {
                TableCell cell = cells[i][j];

                if(j == 0) cell.setBorderLeft(cloneBorder(line));
                if(j == cells[i].length - 1) cell.setBorderRight(cloneBorder(line));
                else {
                    cell.setBorderLeft(null);
                    cell.setBorderLeft(null);
                }

                if(i == 0) cell.setBorderTop(cloneBorder(line));
                else if(i == cells.length - 1) cell.setBorderBottom(cloneBorder(line));
                else {
                    cell.setBorderTop(null);
                    cell.setBorderBottom(null);
                }
            }
        }
    }

    /**
     * Format the inside border using the specified Line object
     *
     * @param line the border line
     */
    public void setInsideBorders(Line line){
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[i].length; j++) {
                TableCell cell = cells[i][j];

                if(j != cells[i].length - 1)
                    cell.setBorderRight(cloneBorder(line));
                else {
                    cell.setBorderLeft(null);
                    cell.setBorderLeft(null);
                }
                if(i != cells.length - 1) cell.setBorderBottom(cloneBorder(line));
                else {
                    cell.setBorderTop(null);
                    cell.setBorderBottom(null);
                }
            }
        }
    }

    private Line cloneBorder(Line line){
        Line border = createBorder();
        border.setLineWidth(line.getLineWidth());
        border.setLineStyle(line.getLineStyle());
        border.setLineDashing(line.getLineDashing());
        border.setLineColor(line.getLineColor());
        return border;
    }

    /**
     * Create a border to format this table
     *
     * @return the created border
     */
    public Line createBorder(){
        Line line = new Line(this);

        EscherOptRecord opt = (EscherOptRecord)getEscherChild(line.getSpContainer(), EscherOptRecord.RECORD_ID);
        setEscherProperty(opt, EscherProperties.GEOMETRY__SHAPEPATH, -1);
        setEscherProperty(opt, EscherProperties.GEOMETRY__FILLOK, -1);
        setEscherProperty(opt, EscherProperties.SHADOWSTYLE__SHADOWOBSURED, 0x20000);
        setEscherProperty(opt, EscherProperties.THREED__LIGHTFACE, 0x80000);

        return line;
    }
}
