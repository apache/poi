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
import org.apache.poi.hslf.model.ShapeTypes;

import java.awt.*;
import java.util.Iterator;

/**
  * Represents a Shape which is the elemental object that composes a drawing.
  *
  * @author Yegor Kozlov
 */
public class Shape {

    public static final int EMU_PER_POINT = 12700;

    /**
     *  The parent of the shape
     */
    protected Shape _parent;

    /**
     * Either EscherSpContainer or EscheSpgrContainer record
     * which holds information about this shape.
     */
    protected EscherContainerRecord _escherContainer;

    protected Shape(EscherContainerRecord escherRecord, Shape parent){
        _escherContainer = escherRecord;
        _parent = parent;
    }

    /**
     *  @return the parent of this shape
     */
    public Shape getParent(){
        return _parent;
    }

    /**
     * @return name of the shape.
     */
    public String getShapeName(){
        EscherSpRecord spRecord = _escherContainer.getChildById(EscherSpRecord.RECORD_ID);
        return ShapeTypes.typeName(spRecord.getOptions() >> 4);
    }

    /**
     * Returns the anchor (the bounding box rectangle) of this shape.
     * All coordinates are expressed in Master units (576 dpi).
     *
     * @return the anchor of this shape
     */
    public java.awt.Rectangle getAnchor(){
        EscherSpRecord spRecord = _escherContainer.getChildById(EscherSpRecord.RECORD_ID);
        int flags = spRecord.getFlags();
        java.awt.Rectangle anchor=null;
        if ((flags & EscherSpRecord.FLAG_CHILD) != 0){
            EscherChildAnchorRecord rec = (EscherChildAnchorRecord)getEscherChild(_escherContainer, EscherChildAnchorRecord.RECORD_ID);
            anchor = new java.awt.Rectangle();
            anchor.x = rec.getDx1();
            anchor.y = rec.getDy1();
            anchor.width = rec.getDx2() - anchor.x;
            anchor.height = rec.getDy2() - anchor.y;
        }
        else {
            EscherClientAnchorRecord rec = (EscherClientAnchorRecord)getEscherChild(_escherContainer, EscherClientAnchorRecord.RECORD_ID);
            anchor = new java.awt.Rectangle();
            anchor.y = rec.getFlag();
            anchor.x = rec.getCol1();
            anchor.width = rec.getDx1() - anchor.x;
            anchor.height = rec.getRow1() - anchor.y;
        }
        return anchor;
    }

    /**
     * Sets the anchor (the bounding box rectangle) of this shape.
     * All coordinates should be expressed in Master units (576 dpi).
     *
     * @param anchor new anchor
     */
    public void setAnchor(java.awt.Rectangle anchor){
        EscherSpRecord spRecord = _escherContainer.getChildById(EscherSpRecord.RECORD_ID);
        int flags = spRecord.getFlags();
        if ((flags & EscherSpRecord.FLAG_CHILD) != 0){
            EscherChildAnchorRecord rec = (EscherChildAnchorRecord)getEscherChild(_escherContainer, EscherChildAnchorRecord.RECORD_ID);
            rec.setDx1(anchor.x);
            rec.setDy1(anchor.y);
            rec.setDx2(anchor.width + anchor.x);
            rec.setDy2(anchor.height + anchor.y);
        }
        else {
            EscherClientAnchorRecord rec = (EscherClientAnchorRecord)getEscherChild(_escherContainer, EscherClientAnchorRecord.RECORD_ID);
            rec.setFlag((short)anchor.y);
            rec.setCol1((short)anchor.x);
            rec.setDx1((short)(anchor.width + anchor.x));
            rec.setRow1((short)(anchor.height + anchor.y));
        }

    }

    /**
     * Moves the top left corner of the shape to the specified point.
     *
     * @param x the x coordinate of the top left corner of the shape
     * @param y the y coordinate of the top left corner of the shape
     */
    public void moveTo(int x, int y){
        java.awt.Rectangle anchor = getAnchor();
        anchor.setLocation(x, y);
        setAnchor(anchor);
    }

    protected static EscherRecord getEscherChild(EscherContainerRecord owner, int recordId){
        for ( Iterator iterator = owner.getChildRecords().iterator(); iterator.hasNext(); )
        {
            EscherRecord escherRecord = (EscherRecord) iterator.next();
            if (escherRecord.getRecordId() == recordId)
                return (EscherRecord) escherRecord;
        }
        return null;
    }

    protected static EscherProperty getEscherProperty(EscherOptRecord opt, int propId){
        for ( Iterator iterator = opt.getEscherProperties().iterator(); iterator.hasNext(); )
        {
            EscherProperty prop = (EscherProperty) iterator.next();
            if (prop.getId() == propId)
                return prop;
        }
        return null;
    }

    protected static void setEscherProperty(EscherOptRecord opt, short propId, int value){
        java.util.List props = opt.getEscherProperties();
        for ( Iterator iterator = props.iterator(); iterator.hasNext(); ) {
            EscherProperty prop = (EscherProperty) iterator.next();
            if (prop.getId() == propId){
                iterator.remove();
            }
        }
        if (value != -1) {
            opt.addEscherProperty(new EscherSimpleProperty(propId, value));
            opt.sortProperties();
        }
    }

    /**
     *
     * @return escher container which holds information about this shape
     */
    public EscherContainerRecord getShapeRecord(){
        return _escherContainer;
    }
}
