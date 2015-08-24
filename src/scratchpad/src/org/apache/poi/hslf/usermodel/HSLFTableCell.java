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

import java.awt.Rectangle;

import org.apache.poi.ddf.AbstractEscherOptRecord;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherProperties;
import org.apache.poi.sl.usermodel.ShapeContainer;
import org.apache.poi.sl.usermodel.ShapeType;
import org.apache.poi.sl.usermodel.TableCell;

/**
 * Represents a cell in a ppt table
 *
 * @author Yegor Kozlov
 */
public final class HSLFTableCell extends HSLFTextBox implements TableCell<HSLFShape,HSLFTextParagraph> {
    protected static final int DEFAULT_WIDTH = 100;
    protected static final int DEFAULT_HEIGHT = 40;

    private HSLFLine borderLeft;
    private HSLFLine borderRight;
    private HSLFLine borderTop;
    private HSLFLine borderBottom;

    /**
     * Create a TableCell object and initialize it from the supplied Record container.
     *
     * @param escherRecord       EscherSpContainer which holds information about this shape
     * @param parent    the parent of the shape
     */
   protected HSLFTableCell(EscherContainerRecord escherRecord, ShapeContainer<HSLFShape,HSLFTextParagraph> parent){
        super(escherRecord, parent);
    }

    /**
     * Create a new TableCell. This constructor is used when a new shape is created.
     *
     * @param parent    the parent of this Shape. For example, if this text box is a cell
     * in a table then the parent is Table.
     */
    public HSLFTableCell(ShapeContainer<HSLFShape,HSLFTextParagraph> parent){
        super(parent);

        setShapeType(ShapeType.RECT);
        //_txtrun.setRunType(TextHeaderAtom.HALF_BODY_TYPE);
        //_txtrun.getRichTextRuns()[0].setFlag(false, 0, false);
    }

    protected EscherContainerRecord createSpContainer(boolean isChild){
        _escherContainer = super.createSpContainer(isChild);
        AbstractEscherOptRecord opt = getEscherOptRecord();
        setEscherProperty(opt, EscherProperties.TEXT__TEXTID, 0);
        setEscherProperty(opt, EscherProperties.TEXT__SIZE_TEXT_TO_FIT_SHAPE, 0x20000);
        setEscherProperty(opt, EscherProperties.FILL__NOFILLHITTEST, 0x150001);
        setEscherProperty(opt, EscherProperties.SHADOWSTYLE__SHADOWOBSURED, 0x20000);
        setEscherProperty(opt, EscherProperties.PROTECTION__LOCKAGAINSTGROUPING, 0x40000);

        return _escherContainer;
    }

    protected void anchorBorder(int type, HSLFLine line){
        Rectangle cellAnchor = getAnchor();
        Rectangle lineAnchor = new Rectangle();
        switch(type){
            case HSLFTable.BORDER_TOP:
                lineAnchor.x = cellAnchor.x;
                lineAnchor.y = cellAnchor.y;
                lineAnchor.width = cellAnchor.width;
                lineAnchor.height = 0;
                break;
            case HSLFTable.BORDER_RIGHT:
                lineAnchor.x = cellAnchor.x + cellAnchor.width;
                lineAnchor.y = cellAnchor.y;
                lineAnchor.width = 0;
                lineAnchor.height = cellAnchor.height;
                break;
            case HSLFTable.BORDER_BOTTOM:
                lineAnchor.x = cellAnchor.x;
                lineAnchor.y = cellAnchor.y + cellAnchor.height;
                lineAnchor.width = cellAnchor.width;
                lineAnchor.height = 0;
                break;
            case HSLFTable.BORDER_LEFT:
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

    public HSLFLine getBorderLeft() {
        return borderLeft;
    }

    public void setBorderLeft(HSLFLine line) {
        if(line != null) anchorBorder(HSLFTable.BORDER_LEFT, line);
        this.borderLeft = line;
    }

    public HSLFLine getBorderRight() {
        return borderRight;
    }

    public void setBorderRight(HSLFLine line) {
        if(line != null) anchorBorder(HSLFTable.BORDER_RIGHT, line);
        this.borderRight = line;
    }

    public HSLFLine getBorderTop() {
        return borderTop;
    }

    public void setBorderTop(HSLFLine line) {
        if(line != null) anchorBorder(HSLFTable.BORDER_TOP, line);
        this.borderTop = line;
    }

    public HSLFLine getBorderBottom() {
        return borderBottom;
    }

    public void setBorderBottom(HSLFLine line) {
        if(line != null) anchorBorder(HSLFTable.BORDER_BOTTOM, line);
        this.borderBottom = line;
    }

    public void setAnchor(Rectangle anchor){
        super.setAnchor(anchor);

        if(borderTop != null) anchorBorder(HSLFTable.BORDER_TOP, borderTop);
        if(borderRight != null) anchorBorder(HSLFTable.BORDER_RIGHT, borderRight);
        if(borderBottom != null) anchorBorder(HSLFTable.BORDER_BOTTOM, borderBottom);
        if(borderLeft != null) anchorBorder(HSLFTable.BORDER_LEFT, borderLeft);
    }
}
