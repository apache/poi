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

import org.apache.poi.util.Units;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTable;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTableCell;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTableRow;
import org.openxmlformats.schemas.presentationml.x2006.main.CTGraphicalObjectFrame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Iterator;

/**
 * Represents a table in a .pptx presentation
 *
 * @author Yegor Kozlov
 */
public class XSLFTableRow implements Iterable<XSLFTableCell> {
    private CTTableRow _row;
    private List<XSLFTableCell> _cells;
    private XSLFTable _table;

    /*package*/ XSLFTableRow(CTTableRow row, XSLFTable table){
        _row = row;
        _table = table;
        _cells = new ArrayList<XSLFTableCell>(_row.sizeOfTcArray());
        for(CTTableCell cell : _row.getTcList()) {
            _cells.add(new XSLFTableCell(cell, table.getSheet()));
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
        XSLFTableCell cell = new XSLFTableCell(c, _table.getSheet());
        _cells.add(cell);

        if(_table.getNumberOfColumns() < _row.sizeOfTcArray()) {
            _table.getCTTable().getTblGrid().addNewGridCol().setW(Units.toEMU(100.0));    
        }
        return cell;
    }


}
