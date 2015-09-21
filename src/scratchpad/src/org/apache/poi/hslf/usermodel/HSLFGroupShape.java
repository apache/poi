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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ddf.EscherChildAnchorRecord;
import org.apache.poi.ddf.EscherClientAnchorRecord;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.ddf.EscherSpRecord;
import org.apache.poi.ddf.EscherSpgrRecord;
import org.apache.poi.sl.usermodel.GroupShape;
import org.apache.poi.sl.usermodel.PictureData;
import org.apache.poi.sl.usermodel.ShapeContainer;
import org.apache.poi.sl.usermodel.ShapeType;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.Units;

/**
 *  Represents a group of shapes.
 *
 * @author Yegor Kozlov
 */
public class HSLFGroupShape extends HSLFShape
implements HSLFShapeContainer, GroupShape<HSLFShape,HSLFTextParagraph> {

    /**
      * Create a new ShapeGroup. This constructor is used when a new shape is created.
      *
      */
    public HSLFGroupShape(){
        this(null, null);
        _escherContainer = createSpContainer(false);
    }

    /**
      * Create a new ShapeGroup. This constructor is used when a new shape is created.
      *
      * @param parent    the parent of the shape
      */
    public HSLFGroupShape(ShapeContainer<HSLFShape,HSLFTextParagraph> parent){
        this(null, parent);
        _escherContainer = createSpContainer(parent instanceof HSLFGroupShape);
    }

    /**
      * Create a ShapeGroup object and initialize it from the supplied Record container.
      *
      * @param escherRecord       <code>EscherSpContainer</code> container which holds information about this shape
      * @param parent    the parent of the shape
      */
    protected HSLFGroupShape(EscherContainerRecord escherRecord, ShapeContainer<HSLFShape,HSLFTextParagraph> parent){
        super(escherRecord, parent);
    }

    /**
     * Sets the anchor (the bounding box rectangle) of this shape.
     * All coordinates should be expressed in Master units (576 dpi).
     *
     * @param anchor new anchor
     */
    public void setAnchor(java.awt.Rectangle anchor){

        EscherClientAnchorRecord clientAnchor = getEscherChild(EscherClientAnchorRecord.RECORD_ID);
        //hack. internal variable EscherClientAnchorRecord.shortRecord can be
        //initialized only in fillFields(). We need to set shortRecord=false;
        byte[] header = new byte[16];
        LittleEndian.putUShort(header, 0, 0);
        LittleEndian.putUShort(header, 2, 0);
        LittleEndian.putInt(header, 4, 8);
        clientAnchor.fillFields(header, 0, null);

        clientAnchor.setFlag((short)Units.pointsToMaster(anchor.y));
        clientAnchor.setCol1((short)Units.pointsToMaster(anchor.x));
        clientAnchor.setDx1((short)Units.pointsToMaster(anchor.width + anchor.x));
        clientAnchor.setRow1((short)Units.pointsToMaster(anchor.height + anchor.y));

        EscherSpgrRecord spgr = getEscherChild(EscherSpgrRecord.RECORD_ID);

        spgr.setRectX1(Units.pointsToMaster(anchor.x));
        spgr.setRectY1(Units.pointsToMaster(anchor.y));
        spgr.setRectX2(Units.pointsToMaster(anchor.x + anchor.width));
        spgr.setRectY2(Units.pointsToMaster(anchor.y + anchor.height));
    }

    @Override
    public void setInteriorAnchor(Rectangle anchor){
        EscherSpgrRecord spgr = getEscherChild(EscherSpgrRecord.RECORD_ID);

        int x1 = Units.pointsToMaster(anchor.getX());
        int y1 = Units.pointsToMaster(anchor.getY());
        int x2 = Units.pointsToMaster(anchor.getX() + anchor.getWidth());
        int y2 = Units.pointsToMaster(anchor.getY() + anchor.getHeight());

        spgr.setRectX1(x1);
        spgr.setRectY1(y1);
        spgr.setRectX2(x2);
        spgr.setRectY2(y2);

    }

    @Override
    public Rectangle getInteriorAnchor(){
        EscherSpgrRecord rec = getEscherChild(EscherSpgrRecord.RECORD_ID);
        int x1 = (int)Units.masterToPoints(rec.getRectX1());
        int y1 = (int)Units.masterToPoints(rec.getRectY1());
        int x2 = (int)Units.masterToPoints(rec.getRectX2());
        int y2 = (int)Units.masterToPoints(rec.getRectY2());
        return new Rectangle(x1,y1,x2-x1,y2-y1);
    }

    /**
     * Create a new ShapeGroup and create an instance of <code>EscherSpgrContainer</code> which represents a group of shapes
     */
    protected EscherContainerRecord createSpContainer(boolean isChild) {
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
        short type = (short)((ShapeType.NOT_PRIMITIVE.nativeId << 4) + 2);
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
    public void addShape(HSLFShape shape){
        _escherContainer.addChildRecord(shape.getSpContainer());

        HSLFSheet sheet = getSheet();
        shape.setSheet(sheet);
        shape.setShapeId(sheet.allocateShapeId());
        shape.afterInsert(sheet);
    }

    /**
     * Moves this <code>ShapeGroup</code> to the specified location.
     * <p>
     * @param x the x coordinate of the top left corner of the shape in new location
     * @param y the y coordinate of the top left corner of the shape in new location
     */
    public void moveTo(int x, int y){
        Rectangle anchor = getAnchor();
        int dx = x - anchor.x;
        int dy = y - anchor.y;
        anchor.translate(dx, dy);
        setAnchor(anchor);

        
        for (HSLFShape shape : getShapes()) {
            Rectangle chanchor = shape.getAnchor();
            chanchor.translate(dx, dy);
            shape.setAnchor(chanchor);
        }
    }

    /**
     * Returns the anchor (the bounding box rectangle) of this shape group.
     * All coordinates are expressed in points (72 dpi).
     *
     * @return the anchor of this shape group
     */
    public Rectangle getAnchor(){
        EscherClientAnchorRecord clientAnchor = getEscherChild(EscherClientAnchorRecord.RECORD_ID);
        int x1,y1,x2,y2;
        if(clientAnchor == null){
            logger.log(POILogger.INFO, "EscherClientAnchorRecord was not found for shape group. Searching for EscherChildAnchorRecord.");
            EscherChildAnchorRecord rec = getEscherChild(EscherChildAnchorRecord.RECORD_ID);
            x1 = rec.getDx1();
            y1 = rec.getDy1();
            x2 = rec.getDx2();
            y2 = rec.getDy2();
        } else {
            x1 = clientAnchor.getCol1();
            y1 = clientAnchor.getFlag();
            x2 = clientAnchor.getDx1();
            y2 = clientAnchor.getRow1();
        }
        Rectangle anchor= new Rectangle(
            (int)(x1 == -1 ? -1 : Units.masterToPoints(x1)),
            (int)(y1 == -1 ? -1 : Units.masterToPoints(y1)),
            (int)(x2 == -1 ? -1 : Units.masterToPoints(x2-x1)),
            (int)(y2 == -1 ? -1 : Units.masterToPoints(y2-y1))
        );

        return anchor;
    }

    /**
     * Return type of the shape.
     * In most cases shape group type is {@link org.apache.poi.sl.usermodel.ShapeType#NOT_PRIMITIVE}
     *
     * @return type of the shape.
     */
    public ShapeType getShapeType(){
        EscherSpRecord spRecord = getEscherChild(EscherSpRecord.RECORD_ID);
        int nativeId = spRecord.getOptions() >> 4;
        return ShapeType.forId(nativeId, false);
    }

    /**
     * Returns <code>null</code> - shape groups can't have hyperlinks
     *
     * @return <code>null</code>.
     */
     public HSLFHyperlink getHyperlink(){
        return null;
    }

    @Override
    public <T extends EscherRecord> T getEscherChild(int recordId){
        EscherContainerRecord groupInfoContainer = (EscherContainerRecord)_escherContainer.getChild(0);
        return groupInfoContainer.getChildById((short)recordId);
    }

    public Iterator<HSLFShape> iterator() {
        return getShapes().iterator();
    }

    public boolean removeShape(HSLFShape shape) {
        // TODO: implement!
        throw new UnsupportedOperationException();
    }

    /**
     * @return the shapes contained in this group container
     */
    @Override
    public List<HSLFShape> getShapes() {
        // Out escher container record should contain several
        //  SpContainers, the first of which is the group shape itself
        Iterator<EscherRecord> iter = _escherContainer.getChildIterator();

        // Don't include the first SpContainer, it is always NotPrimitive
        if (iter.hasNext()) {
            iter.next();
        }
        List<HSLFShape> shapeList = new ArrayList<HSLFShape>();
        while (iter.hasNext()) {
            EscherRecord r = iter.next();
            if(r instanceof EscherContainerRecord) {
                // Create the Shape for it
                EscherContainerRecord container = (EscherContainerRecord)r;
                HSLFShape shape = HSLFShapeFactory.createShape(container, this);
                shape.setSheet(getSheet());
                shapeList.add( shape );
            } else {
                // Should we do anything special with these non
                //  Container records?
                logger.log(POILogger.ERROR, "Shape contained non container escher record, was " + r.getClass().getName());
            }
        }

        return shapeList;
    }

    @Override
    public HSLFTextBox createTextBox() {
        HSLFTextBox s = new HSLFTextBox(this);
        s.setHorizontalCentered(true);
        s.setAnchor(new Rectangle(0, 0, 100, 100));
        addShape(s);
        return s;
    }

    @Override
    public HSLFAutoShape createAutoShape() {
        HSLFAutoShape s = new HSLFAutoShape(ShapeType.RECT, this);
        s.setHorizontalCentered(true);
        s.setAnchor(new Rectangle(0, 0, 100, 100));
        addShape(s);
        return s;
    }

    @Override
    public HSLFFreeformShape createFreeform() {
        HSLFFreeformShape s = new HSLFFreeformShape(this);
        s.setHorizontalCentered(true);
        s.setAnchor(new Rectangle(0, 0, 100, 100));
        addShape(s);
        return s;
    }

    @Override
    public HSLFConnectorShape createConnector() {
        HSLFConnectorShape s = new HSLFConnectorShape(this);
        s.setAnchor(new Rectangle(0, 0, 100, 100));
        addShape(s);
        return s;
    }

    @Override
    public HSLFGroupShape createGroup() {
        HSLFGroupShape s = new HSLFGroupShape(this);
        s.setAnchor(new Rectangle(0, 0, 100, 100));
        addShape(s);
        return s;
    }

    @Override
    public HSLFPictureShape createPicture(PictureData pictureData) {
        if (!(pictureData instanceof HSLFPictureData)) {
            throw new IllegalArgumentException("pictureData needs to be of type HSLFPictureData");
        }
        HSLFPictureShape s = new HSLFPictureShape((HSLFPictureData)pictureData, this);
        s.setAnchor(new Rectangle(0, 0, 100, 100));
        addShape(s);
        return s;
    }

    @Override
    public HSLFTable createTable(int numRows, int numCols) {
        if (numRows < 1 || numCols < 1) {
            throw new IllegalArgumentException("numRows and numCols must be greater than 0");
        }
        HSLFTable s = new HSLFTable(numRows,numCols,this);
        s.setAnchor(new Rectangle(0, 0, 100, 100));
        addShape(s);
        return s;
    }
}
