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

import org.apache.poi.util.Internal;
import org.apache.poi.util.Units;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGraphicalObjectData;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNonVisualDrawingProps;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTable;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTableRow;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGroupTransform2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPoint2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPositiveSize2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTransform2D;
import org.openxmlformats.schemas.presentationml.x2006.main.CTGraphicalObjectFrame;
import org.openxmlformats.schemas.presentationml.x2006.main.CTGraphicalObjectFrameNonVisual;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.awt.geom.Rectangle2D;

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

        for(XmlObject obj : shape.getGraphic().getGraphicData().selectPath("*")){
            if(obj instanceof CTTable){
                _table = (CTTable)obj;
            }
        }
        if(_table == null) throw new IllegalStateException("CTTable element was not found");

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

    public Rectangle2D getAnchor(){
        CTTransform2D xfrm = getXmlObject().getXfrm();
        CTPoint2D off = xfrm.getOff();
        long x = off.getX();
        long y = off.getY();
        CTPositiveSize2D ext = xfrm.getExt();
        long cx = ext.getCx();
        long cy = ext.getCy();
        return new Rectangle2D.Double(
                Units.toPoints(x), Units.toPoints(y),
                Units.toPoints(cx), Units.toPoints(cy));
    }

    public void setAnchor(Rectangle2D anchor){
        CTTransform2D xfrm = getXmlObject().getXfrm();
        CTPoint2D off = xfrm.isSetOff() ? xfrm.getOff() : xfrm.addNewOff();
        long x = Units.toEMU(anchor.getX());
        long y = Units.toEMU(anchor.getY());
        off.setX(x);
        off.setY(y);
        CTPositiveSize2D ext = xfrm.isSetExt() ? xfrm.getExt() : xfrm.addNewExt();
        long cx = Units.toEMU(anchor.getWidth());
        long cy = Units.toEMU(anchor.getHeight());
        ext.setCx(cx);
        ext.setCy(cy);
    }
    
}
