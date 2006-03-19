/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

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

import java.util.List;

/**
 *  Represents a group of shapes.
 *
 * @author Yegor Kozlov
 */
public class ShapeGroup extends Shape{

    public ShapeGroup(Shape parent){
        super(null, parent);
        _escherContainer = create();
    }

    public ShapeGroup(){
        this(null);
    }

    protected ShapeGroup(EscherContainerRecord escherRecord, Shape parent){
        super(escherRecord, parent);
    }

    /**
     * @return the shapes contained in this group container
     */
    public Shape[] getShapes() {
        //several SpContainers, the first of which is the group shape itself
        List lst = _escherContainer.getChildRecords();

        //don't include the first SpContainer, it is always NotPrimitive
        Shape[] shapes = new Shape[lst.size() - 1];
        for (int i = 1; i < lst.size(); i++){
            EscherContainerRecord container = (EscherContainerRecord)lst.get(i);
            shapes[i-1] = ShapeFactory.createShape(container, this);
        }
         return shapes;
    }

    /**
     * Sets the anchor (the bounding box rectangle) of this shape.
     * All coordinates should be expressed in Master units (576 dpi).
     *
     * @param anchor new anchor
     */
    public void setAnchor(java.awt.Rectangle anchor){

        EscherContainerRecord spContainer = (EscherContainerRecord)_escherContainer.getChildRecords().get(0);

        EscherClientAnchorRecord clientAnchor = (EscherClientAnchorRecord)getEscherChild(spContainer, EscherClientAnchorRecord.RECORD_ID);
        //hack. internal variable EscherClientAnchorRecord.shortRecord can be
        //initialized only in fillFields(). We need to set shortRecord=false;
        byte[] header = new byte[16];
        LittleEndian.putUShort(header, 0, 0);
        LittleEndian.putUShort(header, 2, 0);
        LittleEndian.putInt(header, 4, 8);
        clientAnchor.fillFields(header, 0, null);

        clientAnchor.setFlag((short)anchor.y);
        clientAnchor.setCol1((short)anchor.x);
        clientAnchor.setDx1((short)(anchor.width + anchor.x));
        clientAnchor.setRow1((short)(anchor.height + anchor.y));

        EscherSpgrRecord spgr = (EscherSpgrRecord)getEscherChild(spContainer, EscherSpgrRecord.RECORD_ID);

        spgr.setRectX1(anchor.x);
        spgr.setRectY1(anchor.y);
        spgr.setRectX2(anchor.x + anchor.width);
        spgr.setRectY2(anchor.y + anchor.height);
    }

    /**
     * Create a new ShapeGroup and create an instance of <code>EscherSpgrContainer</code> which represents a group of shapes
     */
    protected EscherContainerRecord create() {
        EscherContainerRecord spgr = new EscherContainerRecord();
        spgr.setRecordId(EscherContainerRecord.SPGR_CONTAINER);
        spgr.setOptions((short)15);

        //The group itself is a shape, and always appears as the first EscherSpContainer in the group container.
        EscherContainerRecord spcont = new EscherContainerRecord();
        spcont.setRecordId(EscherContainerRecord.SP_CONTAINER);
        spcont.setOptions((short)15);

        EscherSpgrRecord spg = new EscherSpgrRecord();
        spg.setOptions((short)1);
        spcont.addChildRecord(spg);

        EscherSpRecord sp = new EscherSpRecord();
        short type = (ShapeTypes.NotPrimitive << 4) + 2;
        sp.setOptions(type);
        sp.setFlags(EscherSpRecord.FLAG_HAVEANCHOR | EscherSpRecord.FLAG_GROUP);
        spcont.addChildRecord(sp);

        EscherClientAnchorRecord anchor = new EscherClientAnchorRecord();
        spcont.addChildRecord(anchor);

        spgr.addChildRecord(spcont);
        return spgr;
    }

    /**
     * Add a shape to this group.
     *
     * @param shape - the Shape to add
     */
    public void addShape(Shape shape){
        _escherContainer.addChildRecord(shape.getShapeRecord());
    }

    /**
     * Moves this <code>ShapeGroup</code> to the specified location.
     * <p>
     * @param x the x coordinate of the top left corner of the shape in new location
     * @param y the y coordinate of the top left corner of the shape in new location
     */
    public void moveTo(int x, int y){
        java.awt.Rectangle anchor = getAnchor();
        int dx = x - anchor.x;
        int dy = y - anchor.y;
        anchor.translate(dx, dy);
        setAnchor(anchor);

        Shape[] shape = getShapes();
        for (int i = 0; i < shape.length; i++) {
            java.awt.Rectangle chanchor = shape[i].getAnchor();
            chanchor.translate(dx, dy);
            shape[i].setAnchor(chanchor);
        }
    }

}
