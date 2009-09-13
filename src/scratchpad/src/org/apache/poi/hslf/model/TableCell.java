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

import java.awt.*;

/**
 * Represents a cell in a ppt table
 *
 * @author Yegor Kozlov
 */
public final class TableCell extends TextBox {
    protected static final int DEFAULT_WIDTH = 100;
    protected static final int DEFAULT_HEIGHT = 40;

    private Line borderLeft;
    private Line borderRight;
    private Line borderTop;
    private Line borderBottom;

    /**
     * Create a TableCell object and initialize it from the supplied Record container.
     *
     * @param escherRecord       <code>EscherSpContainer</code> container which holds information about this shape
     * @param parent    the parent of the shape
     */
   protected TableCell(EscherContainerRecord escherRecord, Shape parent){
        super(escherRecord, parent);
    }

    /**
     * Create a new TableCell. This constructor is used when a new shape is created.
     *
     * @param parent    the parent of this Shape. For example, if this text box is a cell
     * in a table then the parent is Table.
     */
    public TableCell(Shape parent){
        super(parent);

        setShapeType(ShapeTypes.Rectangle);
        //_txtrun.setRunType(TextHeaderAtom.HALF_BODY_TYPE);
        //_txtrun.getRichTextRuns()[0].setFlag(false, 0, false);
    }

    protected EscherContainerRecord createSpContainer(boolean isChild){
        _escherContainer = super.createSpContainer(isChild);
        EscherOptRecord opt = (EscherOptRecord)getEscherChild(_escherContainer, EscherOptRecord.RECORD_ID);
        setEscherProperty(opt, EscherProperties.TEXT__TEXTID, 0);
        setEscherProperty(opt, EscherProperties.TEXT__SIZE_TEXT_TO_FIT_SHAPE, 0x20000);
        setEscherProperty(opt, EscherProperties.FILL__NOFILLHITTEST, 0x150001);
        setEscherProperty(opt, EscherProperties.SHADOWSTYLE__SHADOWOBSURED, 0x20000);
        setEscherProperty(opt, EscherProperties.PROTECTION__LOCKAGAINSTGROUPING, 0x40000);

        return _escherContainer;
    }

    protected void anchorBorder(int type, Line line){
        Rectangle cellAnchor = getAnchor();
        Rectangle lineAnchor = new Rectangle();
        switch(type){
            case Table.BORDER_TOP:
                lineAnchor.x = cellAnchor.x;
                lineAnchor.y = cellAnchor.y;
                lineAnchor.width = cellAnchor.width;
                lineAnchor.height = 0;
                break;
            case Table.BORDER_RIGHT:
                lineAnchor.x = cellAnchor.x + cellAnchor.width;
                lineAnchor.y = cellAnchor.y;
                lineAnchor.width = 0;
                lineAnchor.height = cellAnchor.height;
                break;
            case Table.BORDER_BOTTOM:
                lineAnchor.x = cellAnchor.x;
                lineAnchor.y = cellAnchor.y + cellAnchor.height;
                lineAnchor.width = cellAnchor.width;
                lineAnchor.height = 0;
                break;
            case Table.BORDER_LEFT:
                lineAnchor.x = cellAnchor.x;
                lineAnchor.y = cellAnchor.y;
                lineAnchor.width = 0;
                lineAnchor.height = cellAnchor.height;
                break;
            default:
                throw new IllegalArgumentException("Unknown border type: " + type);
        }
        line.setAnchor(lineAnchor);
    }

    public Line getBorderLeft() {
        return borderLeft;
    }

    public void setBorderLeft(Line line) {
        if(line != null) anchorBorder(Table.BORDER_LEFT, line);
        this.borderLeft = line;
    }

    public Line getBorderRight() {
        return borderRight;
    }

    public void setBorderRight(Line line) {
        if(line != null) anchorBorder(Table.BORDER_RIGHT, line);
        this.borderRight = line;
    }

    public Line getBorderTop() {
        return borderTop;
    }

    public void setBorderTop(Line line) {
        if(line != null) anchorBorder(Table.BORDER_TOP, line);
        this.borderTop = line;
    }

    public Line getBorderBottom() {
        return borderBottom;
    }

    public void setBorderBottom(Line line) {
        if(line != null) anchorBorder(Table.BORDER_BOTTOM, line);
        this.borderBottom = line;
    }

    public void setAnchor(Rectangle anchor){
        super.setAnchor(anchor);

        if(borderTop != null) anchorBorder(Table.BORDER_TOP, borderTop);
        if(borderRight != null) anchorBorder(Table.BORDER_RIGHT, borderRight);
        if(borderBottom != null) anchorBorder(Table.BORDER_BOTTOM, borderBottom);
        if(borderLeft != null) anchorBorder(Table.BORDER_LEFT, borderLeft);
    }
}
