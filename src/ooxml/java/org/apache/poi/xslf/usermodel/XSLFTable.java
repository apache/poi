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

import javax.xml.namespace.QName;

import org.apache.poi.POIXMLException;
import org.apache.poi.util.Internal;
import org.apache.poi.util.Units;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.values.XmlAnyTypeImpl;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGraphicalObjectData;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNonVisualDrawingProps;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTable;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTableRow;
import org.openxmlformats.schemas.presentationml.x2006.main.CTGraphicalObjectFrame;
import org.openxmlformats.schemas.presentationml.x2006.main.CTGraphicalObjectFrameNonVisual;

/**
 * Represents a table in a .pptx presentation
 *
 * @author Yegor Kozlov
 */
public class XSLFTable extends XSLFGraphicFrame implements Iterable<XSLFTableRow> {
    static String TABLE_URI = "http://schemas.openxmlformats.org/drawingml/2006/table";

    private CTTable _table;
    private List<XSLFTableRow> _rows;

    /*package*/ XSLFTable(CTGraphicalObjectFrame shape, XSLFSheet sheet){
        super(shape, sheet);

        XmlObject[] rs = shape.getGraphic().getGraphicData()
                .selectPath("declare namespace a='http://schemas.openxmlformats.org/drawingml/2006/main' ./a:tbl");
        if (rs.length == 0) {
            throw new IllegalStateException("a:tbl element was not found in\n " + shape.getGraphic().getGraphicData());
        }

        // Pesky XmlBeans bug - see Bugzilla #49934
        // it never happens when using the full ooxml-schemas jar but may happen with the abridged poi-ooxml-schemas
        if(rs[0] instanceof XmlAnyTypeImpl){
            try {
                rs[0] = CTTable.Factory.parse(rs[0].toString());
            }catch (XmlException e){
                throw new POIXMLException(e);
            }
        }

        _table = (CTTable) rs[0];
        _rows = new ArrayList<XSLFTableRow>(_table.sizeOfTrArray());
        for(CTTableRow row : _table.getTrList()) _rows.add(new XSLFTableRow(row, this));
    }

    @Internal
    public CTTable getCTTable(){
        return _table;
    }

    public int getNumberOfColumns() {
        return _table.getTblGrid().sizeOfGridColArray();
    }

    public int getNumberOfRows() {
        return _table.sizeOfTrArray();
    }

    public double getColumnWidth(int idx){
        return Units.toPoints(
                _table.getTblGrid().getGridColArray(idx).getW());
    }

    public void setColumnWidth(int idx, double width){
        _table.getTblGrid().getGridColArray(idx).setW(Units.toEMU(width));
    }

    public Iterator<XSLFTableRow> iterator(){
        return _rows.iterator();
    }

    public List<XSLFTableRow> getRows(){
        return Collections.unmodifiableList(_rows);
    }

    public XSLFTableRow addRow(){
        CTTableRow tr = _table.addNewTr();
        XSLFTableRow row = new XSLFTableRow(tr, this);
        row.setHeight(20.0);    // default height is 20 points
        _rows.add(row);
        return row;
    }

    static CTGraphicalObjectFrame prototype(int shapeId){
        CTGraphicalObjectFrame frame = CTGraphicalObjectFrame.Factory.newInstance();
        CTGraphicalObjectFrameNonVisual nvGr = frame.addNewNvGraphicFramePr();

        CTNonVisualDrawingProps cnv = nvGr.addNewCNvPr();
        cnv.setName("Table " + shapeId);
        cnv.setId(shapeId + 1);
        nvGr.addNewCNvGraphicFramePr().addNewGraphicFrameLocks().setNoGrp(true);
        nvGr.addNewNvPr();

        frame.addNewXfrm();
        CTGraphicalObjectData gr = frame.addNewGraphic().addNewGraphicData();
        XmlCursor cursor = gr.newCursor();
        cursor.toNextToken();
        cursor.beginElement(new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "tbl"));
        cursor.beginElement(new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "tblPr"));
        cursor.toNextToken();
        cursor.beginElement(new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "tblGrid"));
        cursor.dispose();
        gr.setUri(TABLE_URI);
        return frame;
    }

    /**
     * Merge cells of a table
     */
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
	    				cell.setVMerge(true);
	    			}
    			}
    			if(mergeColumnRequired) {
    				if(colPos == firstCol) {
    					cell.setGridSpan(colSpan);
    				} else {
    					cell.setHMerge(true);
    				}
    			}
    		}
    	}
    }
}
