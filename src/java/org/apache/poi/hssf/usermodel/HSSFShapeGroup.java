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

import org.apache.poi.ddf.EscherChildAnchorRecord;
import org.apache.poi.ddf.EscherClientAnchorRecord;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.ddf.EscherSpgrRecord;
import org.apache.poi.hssf.model.TextboxShape;
import org.apache.poi.hssf.record.EscherAggregate;
import org.apache.poi.hssf.record.ObjRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

/**
 * A shape group may contain other shapes.  It was no actual form on the
 * sheet.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public class HSSFShapeGroup
        extends HSSFShape
        implements HSSFShapeContainer
{
    List<HSSFShape> shapes = new ArrayList<HSSFShape>();
    private EscherSpgrRecord _spgrRecord;

    public HSSFShapeGroup(EscherContainerRecord spgrContainer, ObjRecord objRecord) {
        super(spgrContainer, objRecord);

        // read internal and external coordinates from spgrContainer
        EscherContainerRecord spContainer = spgrContainer.getChildContainers().get(0);
        _spgrRecord = (EscherSpgrRecord) spContainer.getChild(0);
        for(EscherRecord ch : spContainer.getChildRecords()){
            switch(ch.getRecordId()) {
                case EscherSpgrRecord.RECORD_ID:
                    break;
                case EscherClientAnchorRecord.RECORD_ID:
                    anchor = new HSSFClientAnchor((EscherClientAnchorRecord)ch);
                    break;
                case EscherChildAnchorRecord.RECORD_ID:
                    anchor = new HSSFChildAnchor((EscherChildAnchorRecord)ch);
                    break;
            }
        }

    }

    public HSSFShapeGroup( HSSFShape parent, HSSFAnchor anchor )
    {
        super( parent, anchor );
        _spgrRecord = new EscherSpgrRecord();
        _spgrRecord.setRectX1(0);
        _spgrRecord.setRectX2(1023);
        _spgrRecord.setRectY1(0);
        _spgrRecord.setRectY2(255);
    }

    /**
     * Create another group under this group.
     * @param anchor    the position of the new group.
     * @return  the group
     */
    public HSSFShapeGroup createGroup(HSSFChildAnchor anchor)
    {
        HSSFShapeGroup group = new HSSFShapeGroup(this, anchor);
        group.anchor = anchor;
        shapes.add(group);
        return group;
    }

    public void addShape(HSSFShape shape){
        shape._patriarch = this._patriarch;
        shapes.add(shape);
    }

    public void addTextBox(TextboxShape textboxShape){
//        HSSFTextbox shape = new HSSFTextbox(this, textboxShape.geanchor);
//        shapes.add(textboxShape);
    }

    /**
     * Create a new simple shape under this group.
     * @param anchor    the position of the shape.
     * @return  the shape
     */
    public HSSFSimpleShape createShape(HSSFChildAnchor anchor)
    {
        HSSFSimpleShape shape = new HSSFSimpleShape(this, anchor);
        shape.anchor = anchor;
        shapes.add(shape);
        return shape;
    }

    /**
     * Create a new textbox under this group.
     * @param anchor    the position of the shape.
     * @return  the textbox
     */
    public HSSFTextbox createTextbox(HSSFChildAnchor anchor)
    {
        HSSFTextbox shape = new HSSFTextbox(this, anchor);
        shape.anchor = anchor;
        shapes.add(shape);
        return shape;
    }

    /**
     * Creates a polygon
     *
     * @param anchor    the client anchor describes how this group is attached
     *                  to the sheet.
     * @return  the newly created shape.
     */
    public HSSFPolygon createPolygon(HSSFChildAnchor anchor)
    {
        HSSFPolygon shape = new HSSFPolygon(this, anchor);
        shape.anchor = anchor;
        shapes.add(shape);
        return shape;
    }

    /**
     * Creates a picture.
     *
     * @param anchor    the client anchor describes how this group is attached
     *                  to the sheet.
     * @return  the newly created shape.
     */
    public HSSFPicture createPicture(HSSFChildAnchor anchor, int pictureIndex)
    {
      HSSFPicture shape = new HSSFPicture(this, anchor);
      shape.anchor = anchor;
      shape.setPictureIndex( pictureIndex );
      shapes.add(shape);
      return shape;
    }
    /**
     * Return all children contained by this shape.
     */
    public List<HSSFShape> getChildren()
    {
        return shapes;
    }

    /**
     * Sets the coordinate space of this group.  All children are constrained
     * to these coordinates.
     */
    public void setCoordinates( int x1, int y1, int x2, int y2 )
    {
        _spgrRecord.setRectX1(x1);
        _spgrRecord.setRectX2(x2);
        _spgrRecord.setRectY1(y1);
        _spgrRecord.setRectY2(y2);
    }

    /**
     * The top left x coordinate of this group.
     */
    public int getX1()
    {
        return _spgrRecord.getRectX1();
    }

    /**
     * The top left y coordinate of this group.
     */
    public int getY1()
    {
        return _spgrRecord.getRectY1();
    }

    /**
     * The bottom right x coordinate of this group.
     */
    public int getX2()
    {
        return _spgrRecord.getRectX2();
    }

    /**
     * The bottom right y coordinate of this group.
     */
    public int getY2()
    {
        return _spgrRecord.getRectY2();
    }

    /**
     * Count of all children and their childrens children.
     */
    public int countOfAllChildren()
    {
        int count = shapes.size();
        for ( Iterator iterator = shapes.iterator(); iterator.hasNext(); )
        {
            HSSFShape shape = (HSSFShape) iterator.next();
            count += shape.countOfAllChildren();
        }
        return count;
    }
}
