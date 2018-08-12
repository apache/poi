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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.util.Units;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTableCell;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTableRow;

/**
 * Represents a table in a .pptx presentation
 */
public class XSLFTableRow implements Iterable<XSLFTableCell> {
    private final CTTableRow _row;
    private final List<XSLFTableCell> _cells;
    private final XSLFTable _table;

    /*package*/ XSLFTableRow(CTTableRow row, XSLFTable table){
        _row = row;
        _table = table;
        @SuppressWarnings("deprecation")
        CTTableCell[] tcArray = _row.getTcArray();
        _cells = new ArrayList<>(tcArray.length);
        for(CTTableCell cell : tcArray) {
            _cells.add(new XSLFTableCell(cell, table));
        }
    }

    public CTTableRow getXmlObject(){
        return _row;
    }

    public Iterator<XSLFTableCell> iterator(){
        return _cells.iterator();
    }

    public List<XSLFTableCell> getCells(){
        return Collections.unmodifiableList(_cells);
    }

    public double getHeight(){
        return Units.toPoints(_row.getH());
    }

    public void setHeight(double height){
        _row.setH(Units.toEMU(height));
    }

    public XSLFTableCell addCell(){
        CTTableCell c = _row.addNewTc();
        c.set(XSLFTableCell.prototype());
        XSLFTableCell cell = new XSLFTableCell(c, _table);
        _cells.add(cell);

        if(_table.getNumberOfColumns() < _row.sizeOfTcArray()) {
            _table.getCTTable().getTblGrid().addNewGridCol().setW(Units.toEMU(100.0));    
        }
        _table.updateRowColIndexes();
        return cell;
    }
    
    /**
     * Merge cells of a table row, inclusive.
     * Indices are 0-based.
     *
     * @param firstCol 0-based index of first column to merge, inclusive
     * @param lastCol 0-based index of last column to merge, inclusive
     */
    @SuppressWarnings("WeakerAccess")
    public void mergeCells(int firstCol, int lastCol)
    {
        if (firstCol >= lastCol) {
            throw new IllegalArgumentException(
                "Cannot merge, first column >= last column : "
                + firstCol + " >= " + lastCol
            );
        }

        final int colSpan = (lastCol - firstCol) + 1;

        _cells.get(firstCol).setGridSpan(colSpan);
        for (final XSLFTableCell cell : _cells.subList(firstCol+1, lastCol+1)) {
            cell.setHMerge();
        }
    }

}
