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

import java.awt.geom.Rectangle2D;
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
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.Units;

/**
 *  Represents a group of shapes.
 */
public class HSLFGroupShape extends HSLFShape
implements HSLFShapeContainer, GroupShape<HSLFShape,HSLFTextParagraph> {
    private static final POILogger LOG = POILogFactory.getLogger(HSLFGroupShape.class);

    /**
      * Create a new ShapeGroup. This constructor is used when a new shape is created.
      *
      */
    public HSLFGroupShape(){
        this(null, null);
        createSpContainer(false);
    }

    /**
      * Create a new ShapeGroup. This constructor is used when a new shape is created.
      *
      * @param parent    the parent of the shape
      */
    public HSLFGroupShape(ShapeContainer<HSLFShape,HSLFTextParagraph> parent){
        this(null, parent);
        createSpContainer(parent instanceof HSLFGroupShape);
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

    @Override
    public void setAnchor(Rectangle2D anchor) {
        EscherClientAnchorRecord clientAnchor = getEscherChild(EscherClientAnchorRecord.RECORD_ID);
        boolean isInitialized = !(clientAnchor.getDx1() == 0 && clientAnchor.getRow1() == 0);
        
        if (isInitialized) {
            moveAndScale(anchor);
        } else {
            setExteriorAnchor(anchor);
        }
    }

    @Override
    public void setInteriorAnchor(Rectangle2D anchor){
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
    public Rectangle2D getInteriorAnchor(){
        EscherSpgrRecord rec = getEscherChild(EscherSpgrRecord.RECORD_ID);
        double x1 = Units.masterToPoints(rec.getRectX1());
        double y1 = Units.masterToPoints(rec.getRectY1());
        double x2 = Units.masterToPoints(rec.getRectX2());
        double y2 = Units.masterToPoints(rec.getRectY2());
        return new Rectangle2D.Double(x1,y1,x2-x1,y2-y1);
    }

    protected void setExteriorAnchor(Rectangle2D anchor) {
        EscherClientAnchorRecord clientAnchor = getEscherChild(EscherClientAnchorRecord.RECORD_ID);
        
        //hack. internal variable EscherClientAnchorRecord.shortRecord can be
        //initialized only in fillFields(). We need to set shortRecord=false;
        byte[] header = new byte[16];
        LittleEndian.putUShort(header, 0, 0);
        LittleEndian.putUShort(header, 2, 0);
        LittleEndian.putInt(header, 4, 8);
        clientAnchor.fillFields(header, 0, null);

        // All coordinates need to be converted to Master units (576 dpi)
        clientAnchor.setFlag((short)Units.pointsToMaster(anchor.getY()));
        clientAnchor.setCol1((short)Units.pointsToMaster(anchor.getX()));
        clientAnchor.setDx1((short)Units.pointsToMaster(anchor.getWidth() + anchor.getX()));
        clientAnchor.setRow1((short)Units.pointsToMaster(anchor.getHeight() + anchor.getY()));

        // TODO: does this make sense?
        setInteriorAnchor(anchor);
    }
    
    /**
     * Create a new ShapeGroup and create an instance of <code>EscherSpgrContainer</code> which represents a group of shapes
     */
    @Override
    protected EscherContainerRecord createSpContainer(boolean isChild) {
        EscherContainerRecord ecr = super.createSpContainer(isChild);
        ecr.setRecordId(EscherContainerRecord.SPGR_CONTAINER);

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

        ecr.addChildRecord(spcont);
        return ecr;
    }

    /**
     * Add a shape to this group.
     *
     * @param shape - the Shape to add
     */
    @Override
    public void addShape(HSLFShape shape){
        getSpContainer().addChildRecord(shape.getSpContainer());

        HSLFSheet sheet = getSheet();
        shape.setSheet(sheet);
        shape.setShapeId(sheet.allocateShapeId());
        shape.afterInsert(sheet);
    }

    /**
     * Moves and scales this <code>ShapeGroup</code> to the specified anchor.
     */
    protected void moveAndScale(Rectangle2D anchorDest){
        Rectangle2D anchorSrc = getAnchor();
        double scaleX = (anchorSrc.getWidth() == 0) ? 0 : anchorDest.getWidth() / anchorSrc.getWidth();
        double scaleY = (anchorSrc.getHeight() == 0) ? 0 : anchorDest.getHeight() / anchorSrc.getHeight();

        setExteriorAnchor(anchorDest);
        
        for (HSLFShape shape : getShapes()) {
            Rectangle2D chanchor = shape.getAnchor();
            double x = anchorDest.getX()+(chanchor.getX()-anchorSrc.getX())*scaleX;
            double y = anchorDest.getY()+(chanchor.getY()-anchorSrc.getY())*scaleY;
            double width = chanchor.getWidth()*scaleX;
            double height = chanchor.getHeight()*scaleY;
            shape.setAnchor(new Rectangle2D.Double(x, y, width, height));
        }
    }

    /**
     * Returns the anchor (the bounding box rectangle) of this shape group.
     * All coordinates are expressed in points (72 dpi).
     *
     * @return the anchor of this shape group
     */
    @Override
    public Rectangle2D getAnchor(){
        EscherClientAnchorRecord clientAnchor = getEscherChild(EscherClientAnchorRecord.RECORD_ID);
        int x1,y1,x2,y2;
        if(clientAnchor == null){
            LOG.log(POILogger.INFO, "EscherClientAnchorRecord was not found for shape group. Searching for EscherChildAnchorRecord.");
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

        return new Rectangle2D.Double(
            (x1 == -1 ? -1 : Units.masterToPoints(x1)),
            (y1 == -1 ? -1 : Units.masterToPoints(y1)),
            (x2 == -1 ? -1 : Units.masterToPoints(x2-x1)),
            (y2 == -1 ? -1 : Units.masterToPoints(y2-y1))
        );
    }

    /**
     * Return type of the shape.
     * In most cases shape group type is {@link org.apache.poi.sl.usermodel.ShapeType#NOT_PRIMITIVE}
     *
     * @return type of the shape.
     */
    @Override
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
        EscherContainerRecord groupInfoContainer = (EscherContainerRecord)getSpContainer().getChild(0);
        return groupInfoContainer.getChildById((short)recordId);
    }

    @Override
    public Iterator<HSLFShape> iterator() {
        return getShapes().iterator();
    }

    @Override
    public boolean removeShape(HSLFShape shape) {
        // TODO: implement!
        throw new UnsupportedOperationException();
    }

    @Override
    public List<HSLFShape> getShapes() {
        // Our escher container record should contain several
        // SpContainers, the first of which is the group shape itself
        List<HSLFShape> shapeList = new ArrayList<>();
        boolean isFirst = true;
        for (EscherRecord r : getSpContainer()) {
            if (isFirst) {
                // Don't include the first SpContainer, it is always NotPrimitive
                isFirst = false;
                continue;
            }
            
            if(r instanceof EscherContainerRecord) {
                // Create the Shape for it
                EscherContainerRecord container = (EscherContainerRecord)r;
                HSLFShape shape = HSLFShapeFactory.createShape(container, this);
                shape.setSheet(getSheet());
                shapeList.add( shape );
            } else {
                // Should we do anything special with these non
                //  Container records?
                LOG.log(POILogger.ERROR, "Shape contained non container escher record, was ", r.getClass().getName());
            }
        }

        return shapeList;
    }

    @Override
    public HSLFTextBox createTextBox() {
        HSLFTextBox s = new HSLFTextBox(this);
        s.setHorizontalCentered(true);
        s.setAnchor(new Rectangle2D.Double(0, 0, 100, 100));
        addShape(s);
        return s;
    }

    @Override
    public HSLFAutoShape createAutoShape() {
        HSLFAutoShape s = new HSLFAutoShape(ShapeType.RECT, this);
        s.setHorizontalCentered(true);
        s.setAnchor(new Rectangle2D.Double(0, 0, 100, 100));
        addShape(s);
        return s;
    }

    @Override
    public HSLFFreeformShape createFreeform() {
        HSLFFreeformShape s = new HSLFFreeformShape(this);
        s.setHorizontalCentered(true);
        s.setAnchor(new Rectangle2D.Double(0, 0, 100, 100));
        addShape(s);
        return s;
    }

    @Override
    public HSLFConnectorShape createConnector() {
        HSLFConnectorShape s = new HSLFConnectorShape(this);
        s.setAnchor(new Rectangle2D.Double(0, 0, 100, 100));
        addShape(s);
        return s;
    }

    @Override
    public HSLFGroupShape createGroup() {
        HSLFGroupShape s = new HSLFGroupShape(this);
        s.setAnchor(new Rectangle2D.Double(0, 0, 100, 100));
        addShape(s);
        return s;
    }

    @Override
    public HSLFPictureShape createPicture(PictureData pictureData) {
        if (!(pictureData instanceof HSLFPictureData)) {
            throw new IllegalArgumentException("pictureData needs to be of type HSLFPictureData");
        }
        HSLFPictureShape s = new HSLFPictureShape((HSLFPictureData)pictureData, this);
        s.setAnchor(new Rectangle2D.Double(0, 0, 100, 100));
        addShape(s);
        return s;
    }

    @Override
    public HSLFTable createTable(int numRows, int numCols) {
        if (numRows < 1 || numCols < 1) {
            throw new IllegalArgumentException("numRows and numCols must be greater than 0");
        }
        HSLFTable s = new HSLFTable(numRows,numCols,this);
        s.setAnchor(new Rectangle2D.Double(0, 0, 100, 100));
        addShape(s);
        return s;
    }

    @Override
    public HSLFObjectShape createOleShape(PictureData pictureData) {
        if (!(pictureData instanceof HSLFPictureData)) {
            throw new IllegalArgumentException("pictureData needs to be of type HSLFPictureData");
        }
        HSLFObjectShape s = new HSLFObjectShape((HSLFPictureData)pictureData, this);
        s.setAnchor(new Rectangle2D.Double(0, 0, 100, 100));
        addShape(s);
        return s;
    }
}
