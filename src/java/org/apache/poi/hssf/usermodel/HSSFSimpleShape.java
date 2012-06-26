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
import org.apache.poi.hssf.record.CommonObjectDataSubRecord;
import org.apache.poi.hssf.record.EndSubRecord;
import org.apache.poi.hssf.record.EscherAggregate;
import org.apache.poi.hssf.record.ObjRecord;
import org.apache.poi.hssf.usermodel.drawing.HSSFShapeType;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a simple shape such as a line, rectangle or oval.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public class HSSFSimpleShape
    extends HSSFShape
{
    // The commented out ones haven't been tested yet or aren't supported
    // by HSSFSimpleShape.

    public final static short       OBJECT_TYPE_LINE               = 1;
    public final static short       OBJECT_TYPE_RECTANGLE          = 2;
    public final static short       OBJECT_TYPE_OVAL               = 3;
//    public final static short       OBJECT_TYPE_ARC                = 4;
//    public final static short       OBJECT_TYPE_CHART              = 5;
//    public final static short       OBJECT_TYPE_TEXT               = 6;
//    public final static short       OBJECT_TYPE_BUTTON             = 7;
    public final static short       OBJECT_TYPE_PICTURE            = 8;
//    public final static short       OBJECT_TYPE_POLYGON            = 9;
//    public final static short       OBJECT_TYPE_CHECKBOX           = 11;
//    public final static short       OBJECT_TYPE_OPTION_BUTTON      = 12;
//    public final static short       OBJECT_TYPE_EDIT_BOX           = 13;
//    public final static short       OBJECT_TYPE_LABEL              = 14;
//    public final static short       OBJECT_TYPE_DIALOG_BOX         = 15;
//    public final static short       OBJECT_TYPE_SPINNER            = 16;
//    public final static short       OBJECT_TYPE_SCROLL_BAR         = 17;
//    public final static short       OBJECT_TYPE_LIST_BOX           = 18;
//    public final static short       OBJECT_TYPE_GROUP_BOX          = 19;
    public final static short       OBJECT_TYPE_COMBO_BOX          = 20;
    public final static short       OBJECT_TYPE_COMMENT            = 25;
//    public final static short       OBJECT_TYPE_MICROSOFT_OFFICE_DRAWING = 30;

    int shapeType = OBJECT_TYPE_LINE;

    private static final Map <Short, Short> objTypeToShapeType = new HashMap<Short, Short>();

    static {
        objTypeToShapeType.put(OBJECT_TYPE_RECTANGLE, HSSFShapeType.RECTANGLE.getType());
        objTypeToShapeType.put(OBJECT_TYPE_PICTURE, HSSFShapeType.PICTURE.getType());
        objTypeToShapeType.put(OBJECT_TYPE_LINE, HSSFShapeType.LINE.getType());
    }

    public HSSFSimpleShape(EscherContainerRecord spContainer, ObjRecord objRecord) {
        super(spContainer, objRecord);
    }

    public HSSFSimpleShape( HSSFShape parent, HSSFAnchor anchor)
    {
        super( parent, anchor );
        _escherContainer = createSpContainer();
        _objRecord = createObjRecord();
        setShapeType(OBJECT_TYPE_LINE);
    }

    @Override
    protected EscherContainerRecord createSpContainer() {
        EscherContainerRecord spContainer = new EscherContainerRecord();
        spContainer.setRecordId( EscherContainerRecord.SP_CONTAINER );
        spContainer.setOptions( (short) 0x000F );

        EscherSpRecord sp = new EscherSpRecord();
        sp.setRecordId( EscherSpRecord.RECORD_ID );
        sp.setFlags( EscherSpRecord.FLAG_HAVEANCHOR | EscherSpRecord.FLAG_HASSHAPETYPE );

        EscherClientDataRecord clientData = new EscherClientDataRecord();
        clientData.setRecordId( EscherClientDataRecord.RECORD_ID );
        clientData.setOptions( (short) 0x0000 );

        spContainer.addChildRecord(sp);
        spContainer.addChildRecord(_optRecord);
        spContainer.addChildRecord(anchor.getEscherAnchor());
        spContainer.addChildRecord(clientData);
        return spContainer;
    }

    @Override
    protected ObjRecord createObjRecord() {
        ObjRecord obj = new ObjRecord();
        CommonObjectDataSubRecord c = new CommonObjectDataSubRecord();
        c.setLocked(true);
        c.setPrintable(true);
        c.setAutofill(true);
        c.setAutoline(true);
        EndSubRecord e = new EndSubRecord();

        obj.addSubRecord(c);
        obj.addSubRecord(e);
        return obj;
    }

    @Override
    void afterInsert(HSSFPatriarch patriarch){
        EscherAggregate agg = patriarch._getBoundAggregate();
        agg.associateShapeToObjRecord(_escherContainer.getChildById(EscherClientDataRecord.RECORD_ID), getObjRecord());
    }


    /**
     * Gets the shape type.
     * @return  One of the OBJECT_TYPE_* constants.
     *
     * @see #OBJECT_TYPE_LINE
     * @see #OBJECT_TYPE_OVAL
     * @see #OBJECT_TYPE_RECTANGLE
     * @see #OBJECT_TYPE_PICTURE
     * @see #OBJECT_TYPE_COMMENT
     */
    public int getShapeType() {
        CommonObjectDataSubRecord cod = (CommonObjectDataSubRecord) _objRecord.getSubRecords().get(0);
        return cod.getObjectType();
    }

    /**
     * Sets the shape types.
     *
     * @param shapeType One of the OBJECT_TYPE_* constants.
     *
     * @see #OBJECT_TYPE_LINE
     * @see #OBJECT_TYPE_OVAL
     * @see #OBJECT_TYPE_RECTANGLE
     * @see #OBJECT_TYPE_PICTURE
     * @see #OBJECT_TYPE_COMMENT
     */
    public void setShapeType( int shapeType ){
        CommonObjectDataSubRecord cod = (CommonObjectDataSubRecord) _objRecord.getSubRecords().get(0);
        cod.setObjectType((short) shapeType);
        EscherSpRecord spRecord = _escherContainer.getChildById(EscherSpRecord.RECORD_ID);
        if (null == objTypeToShapeType.get((short)shapeType)){
            System.out.println("Unknown shape type: "+shapeType);
            return;
        }
        spRecord.setShapeType(objTypeToShapeType.get((short)shapeType));
    }
}
