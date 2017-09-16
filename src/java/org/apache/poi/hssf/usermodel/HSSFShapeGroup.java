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

package org.apache.poi.hssf.usermodel;

import org.apache.poi.ddf.*;
import org.apache.poi.hssf.record.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Iterator;

/**
 * A shape group may contain other shapes.  It was no actual form on the
 * sheet.
 */
public class HSSFShapeGroup extends HSSFShape implements HSSFShapeContainer {
    private final List<HSSFShape> shapes = new ArrayList<>();
    private EscherSpgrRecord _spgrRecord;

    public HSSFShapeGroup(EscherContainerRecord spgrContainer, ObjRecord objRecord) {
        super(spgrContainer, objRecord);

        // read internal and external coordinates from spgrContainer
        EscherContainerRecord spContainer = spgrContainer.getChildContainers().get(0);
        _spgrRecord = (EscherSpgrRecord) spContainer.getChild(0);
        for (EscherRecord ch : spContainer.getChildRecords()) {
            switch (ch.getRecordId()) {
                case EscherSpgrRecord.RECORD_ID:
                    break;
                case EscherClientAnchorRecord.RECORD_ID:
                    anchor = new HSSFClientAnchor((EscherClientAnchorRecord) ch);
                    break;
                case EscherChildAnchorRecord.RECORD_ID:
                    anchor = new HSSFChildAnchor((EscherChildAnchorRecord) ch);
                    break;
                default:
                    break;
            }
        }
    }

    public HSSFShapeGroup(HSSFShape parent, HSSFAnchor anchor) {
        super(parent, anchor);
        _spgrRecord = ((EscherContainerRecord)getEscherContainer().getChild(0)).getChildById(EscherSpgrRecord.RECORD_ID);
    }

    @Override
    protected EscherContainerRecord createSpContainer() {
        EscherContainerRecord spgrContainer = new EscherContainerRecord();
        EscherContainerRecord spContainer = new EscherContainerRecord();
        EscherSpgrRecord spgr = new EscherSpgrRecord();
        EscherSpRecord sp = new EscherSpRecord();
        EscherOptRecord opt = new EscherOptRecord();
        EscherRecord anchor;
        EscherClientDataRecord clientData = new EscherClientDataRecord();

        spgrContainer.setRecordId(EscherContainerRecord.SPGR_CONTAINER);
        spgrContainer.setOptions((short) 0x000F);
        spContainer.setRecordId(EscherContainerRecord.SP_CONTAINER);
        spContainer.setOptions((short) 0x000F);
        spgr.setRecordId(EscherSpgrRecord.RECORD_ID);
        spgr.setOptions((short) 0x0001);
        spgr.setRectX1(0);
        spgr.setRectY1(0);
        spgr.setRectX2(1023);
        spgr.setRectY2(255);
        sp.setRecordId(EscherSpRecord.RECORD_ID);
        sp.setOptions((short) 0x0002);
        if (getAnchor() instanceof HSSFClientAnchor) {
            sp.setFlags(EscherSpRecord.FLAG_GROUP | EscherSpRecord.FLAG_HAVEANCHOR);
        } else {
            sp.setFlags(EscherSpRecord.FLAG_GROUP | EscherSpRecord.FLAG_HAVEANCHOR | EscherSpRecord.FLAG_CHILD);
        }
        opt.setRecordId(EscherOptRecord.RECORD_ID);
        opt.setOptions((short) 0x0023);
        opt.addEscherProperty(new EscherBoolProperty(EscherProperties.PROTECTION__LOCKAGAINSTGROUPING, 0x00040004));
        opt.addEscherProperty(new EscherBoolProperty(EscherProperties.GROUPSHAPE__PRINT, 0x00080000));

        anchor = getAnchor().getEscherAnchor();
        clientData.setRecordId(EscherClientDataRecord.RECORD_ID);
        clientData.setOptions((short) 0x0000);

        spgrContainer.addChildRecord(spContainer);
        spContainer.addChildRecord(spgr);
        spContainer.addChildRecord(sp);
        spContainer.addChildRecord(opt);
        spContainer.addChildRecord(anchor);
        spContainer.addChildRecord(clientData);
        return spgrContainer;
    }

    @Override
    protected ObjRecord createObjRecord() {
        ObjRecord obj = new ObjRecord();
        CommonObjectDataSubRecord cmo = new CommonObjectDataSubRecord();
        cmo.setObjectType(CommonObjectDataSubRecord.OBJECT_TYPE_GROUP);
        cmo.setLocked(true);
        cmo.setPrintable(true);
        cmo.setAutofill(true);
        cmo.setAutoline(true);
        GroupMarkerSubRecord gmo = new GroupMarkerSubRecord();
        EndSubRecord end = new EndSubRecord();
        obj.addSubRecord(cmo);
        obj.addSubRecord(gmo);
        obj.addSubRecord(end);
        return obj;
    }

    @Override
    protected void afterRemove(HSSFPatriarch patriarch) {
        patriarch.getBoundAggregate().removeShapeToObjRecord(getEscherContainer().getChildContainers().get(0)
                .getChildById(EscherClientDataRecord.RECORD_ID));
        for ( int i=0; i<shapes.size(); i++ ) {
            HSSFShape shape = shapes.get(i);
            removeShape(shape);
            shape.afterRemove(getPatriarch());
        }
        shapes.clear();
    }

    private void onCreate(HSSFShape shape){
        if(getPatriarch() != null){
            EscherContainerRecord spContainer = shape.getEscherContainer();
            int shapeId = getPatriarch().newShapeId();
            shape.setShapeId(shapeId);
            getEscherContainer().addChildRecord(spContainer);
            shape.afterInsert(getPatriarch());
            EscherSpRecord sp;
            if (shape instanceof HSSFShapeGroup){
                sp = shape.getEscherContainer().getChildContainers().get(0).getChildById(EscherSpRecord.RECORD_ID);
            } else {
                sp = shape.getEscherContainer().getChildById(EscherSpRecord.RECORD_ID);
            }
            sp.setFlags(sp.getFlags() | EscherSpRecord.FLAG_CHILD);
        }
    }

    /**
     * Create another group under this group.
     *
     * @param anchor the position of the new group.
     * @return the group
     */
    public HSSFShapeGroup createGroup(HSSFChildAnchor anchor) {
        HSSFShapeGroup group = new HSSFShapeGroup(this, anchor);
        group.setParent(this);
        group.setAnchor(anchor);
        shapes.add(group);
        onCreate(group);
        return group;
    }

    public void addShape(HSSFShape shape) {
        shape.setPatriarch(this.getPatriarch());
        shape.setParent(this);
        shapes.add(shape);
    }

    /**
     * Create a new simple shape under this group.
     *
     * @param anchor the position of the shape.
     * @return the shape
     */
    public HSSFSimpleShape createShape(HSSFChildAnchor anchor) {
        HSSFSimpleShape shape = new HSSFSimpleShape(this, anchor);
        shape.setParent(this);
        shape.setAnchor(anchor);
        shapes.add(shape);
        onCreate(shape);
        EscherSpRecord sp = shape.getEscherContainer().getChildById(EscherSpRecord.RECORD_ID);
        if (shape.getAnchor().isHorizontallyFlipped()){
            sp.setFlags(sp.getFlags() | EscherSpRecord.FLAG_FLIPHORIZ);
        }
        if (shape.getAnchor().isVerticallyFlipped()){
            sp.setFlags(sp.getFlags() | EscherSpRecord.FLAG_FLIPVERT);
        }
        return shape;
    }

    /**
     * Create a new textbox under this group.
     *
     * @param anchor the position of the shape.
     * @return the textbox
     */
    public HSSFTextbox createTextbox(HSSFChildAnchor anchor) {
        HSSFTextbox shape = new HSSFTextbox(this, anchor);
        shape.setParent(this);
        shape.setAnchor(anchor);
        shapes.add(shape);
        onCreate(shape);
        return shape;
    }

    /**
     * Creates a polygon
     *
     * @param anchor the client anchor describes how this group is attached
     *               to the sheet.
     * @return the newly created shape.
     */
    public HSSFPolygon createPolygon(HSSFChildAnchor anchor) {
        HSSFPolygon shape = new HSSFPolygon(this, anchor);
        shape.setParent(this);
        shape.setAnchor(anchor);
        shapes.add(shape);
        onCreate(shape);
        return shape;
    }

    /**
     * Creates a picture.
     *
     * @param anchor the client anchor describes how this group is attached
     *               to the sheet.
     * @return the newly created shape.
     */
    public HSSFPicture createPicture(HSSFChildAnchor anchor, int pictureIndex) {
        HSSFPicture shape = new HSSFPicture(this, anchor);
        shape.setParent(this);
        shape.setAnchor(anchor);
        shape.setPictureIndex(pictureIndex);
        shapes.add(shape);
        onCreate(shape);
        EscherSpRecord sp = shape.getEscherContainer().getChildById(EscherSpRecord.RECORD_ID);
        if (shape.getAnchor().isHorizontallyFlipped()){
            sp.setFlags(sp.getFlags() | EscherSpRecord.FLAG_FLIPHORIZ);
        }
        if (shape.getAnchor().isVerticallyFlipped()){
            sp.setFlags(sp.getFlags() | EscherSpRecord.FLAG_FLIPVERT);
        }
        return shape;
    }

    /**
     * Return all children contained by this shape.
     */
    public List<HSSFShape> getChildren() {
        return Collections.unmodifiableList(shapes);
    }

    /**
     * Sets the coordinate space of this group.  All children are constrained
     * to these coordinates.
     */
    public void setCoordinates(int x1, int y1, int x2, int y2) {
        _spgrRecord.setRectX1(x1);
        _spgrRecord.setRectX2(x2);
        _spgrRecord.setRectY1(y1);
        _spgrRecord.setRectY2(y2);
    }

    public void clear() {
        ArrayList <HSSFShape> copy = new ArrayList<>(shapes);
        for (HSSFShape shape: copy){
            removeShape(shape);
        }
    }

    /**
     * The top left x coordinate of this group.
     */
    public int getX1() {
        return _spgrRecord.getRectX1();
    }

    /**
     * The top left y coordinate of this group.
     */
    public int getY1() {
        return _spgrRecord.getRectY1();
    }

    /**
     * The bottom right x coordinate of this group.
     */
    public int getX2() {
        return _spgrRecord.getRectX2();
    }

    /**
     * The bottom right y coordinate of this group.
     */
    public int getY2() {
        return _spgrRecord.getRectY2();
    }

    /**
     * Count of all children and their childrens children.
     */
    public int countOfAllChildren() {
        int count = shapes.size();
        for (Iterator<HSSFShape> iterator = shapes.iterator(); iterator.hasNext(); ) {
            HSSFShape shape = iterator.next();
            count += shape.countOfAllChildren();
        }
        return count;
    }

    @Override
    void afterInsert(HSSFPatriarch patriarch){
        EscherAggregate agg = patriarch.getBoundAggregate();
        EscherContainerRecord containerRecord = getEscherContainer().getChildById(EscherContainerRecord.SP_CONTAINER);
        agg.associateShapeToObjRecord(containerRecord.getChildById(EscherClientDataRecord.RECORD_ID), getObjRecord());
    }

    @Override
    void setShapeId(int shapeId){
        EscherContainerRecord containerRecord = getEscherContainer().getChildById(EscherContainerRecord.SP_CONTAINER);
        EscherSpRecord spRecord = containerRecord.getChildById(EscherSpRecord.RECORD_ID);
        spRecord.setShapeId(shapeId);
        CommonObjectDataSubRecord cod = (CommonObjectDataSubRecord) getObjRecord().getSubRecords().get(0);
        cod.setObjectId((short) (shapeId % 1024));
    }

    @Override
    int getShapeId(){
        EscherContainerRecord containerRecord = getEscherContainer().getChildById(EscherContainerRecord.SP_CONTAINER);
        return ((EscherSpRecord)containerRecord.getChildById(EscherSpRecord.RECORD_ID)).getShapeId();
    }

    @Override
    protected HSSFShape cloneShape() {
        throw new IllegalStateException("Use method cloneShape(HSSFPatriarch patriarch)");
    }

    protected HSSFShape cloneShape(HSSFPatriarch patriarch) {
        EscherContainerRecord spgrContainer = new EscherContainerRecord();
        spgrContainer.setRecordId(EscherContainerRecord.SPGR_CONTAINER);
        spgrContainer.setOptions((short) 0x000F);
        EscherContainerRecord spContainer = new EscherContainerRecord();
        EscherContainerRecord cont = getEscherContainer().getChildById(EscherContainerRecord.SP_CONTAINER);
        byte [] inSp = cont.serialize();
        spContainer.fillFields(inSp, 0, new DefaultEscherRecordFactory());

        spgrContainer.addChildRecord(spContainer);
        ObjRecord obj = null;
        if (null != getObjRecord()){
            obj = (ObjRecord) getObjRecord().cloneViaReserialise();
        }

        HSSFShapeGroup group = new HSSFShapeGroup(spgrContainer, obj);
        group.setPatriarch(patriarch);

        for (HSSFShape shape: getChildren()){
            HSSFShape newShape;
            if (shape instanceof HSSFShapeGroup){
                newShape = ((HSSFShapeGroup)shape).cloneShape(patriarch);
            } else {
                newShape = shape.cloneShape();
            }
            group.addShape(newShape);
            group.onCreate(newShape);
        }
        return group;
    }

    public boolean removeShape(HSSFShape shape) {
        boolean  isRemoved = getEscherContainer().removeChildRecord(shape.getEscherContainer());
        if (isRemoved){
            shape.afterRemove(this.getPatriarch());
            shapes.remove(shape);
        }
        return isRemoved;
    }

    @Override
    public Iterator<HSSFShape> iterator() {
        return shapes.iterator();
    }
}
