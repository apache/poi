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


package org.apache.poi.hssf.model;

import org.apache.poi.ddf.*;
import org.apache.poi.hssf.record.ObjRecord;
import org.apache.poi.hssf.record.EscherAggregate;
import org.apache.poi.hssf.record.CommonObjectDataSubRecord;
import org.apache.poi.hssf.record.EndSubRecord;
import org.apache.poi.hssf.usermodel.HSSFShape;
import org.apache.poi.hssf.usermodel.HSSFPolygon;
import org.apache.poi.util.LittleEndian;

public class PolygonShape
        extends AbstractShape
{
    public final static short       OBJECT_TYPE_MICROSOFT_OFFICE_DRAWING = 30;

    private EscherContainerRecord spContainer;
    private ObjRecord objRecord;

    /**
     * Creates the low evel records for an polygon.
     *
     * @param hssfShape  The highlevel shape.
     * @param shapeId    The shape id to use for this shape.
     */
    PolygonShape( HSSFPolygon hssfShape, int shapeId )
    {
        spContainer = createSpContainer( hssfShape, shapeId );
        objRecord = createObjRecord( hssfShape, shapeId );
    }

    /**
     * Generates the shape records for this shape.
     *
     */
    private EscherContainerRecord createSpContainer( HSSFPolygon hssfShape, int shapeId )
    {
        HSSFShape shape = hssfShape;

        EscherContainerRecord spContainer = new EscherContainerRecord();
        EscherSpRecord sp = new EscherSpRecord();
        EscherOptRecord opt = new EscherOptRecord();
        EscherClientDataRecord clientData = new EscherClientDataRecord();

        spContainer.setRecordId( EscherContainerRecord.SP_CONTAINER );
        spContainer.setOptions( (short) 0x000F );
        sp.setRecordId( EscherSpRecord.RECORD_ID );
        sp.setOptions( (short) ( ( EscherAggregate.ST_DONUT << 4 ) | 0x2 ) );
        sp.setShapeId( shapeId );
        if (hssfShape.getParent() == null)
            sp.setFlags( EscherSpRecord.FLAG_HAVEANCHOR | EscherSpRecord.FLAG_HASSHAPETYPE );
        else
            sp.setFlags( EscherSpRecord.FLAG_CHILD | EscherSpRecord.FLAG_HAVEANCHOR | EscherSpRecord.FLAG_HASSHAPETYPE );
        opt.setRecordId( EscherOptRecord.RECORD_ID );
        opt.addEscherProperty(new EscherSimpleProperty(EscherProperties.TRANSFORM__ROTATION, false, false, 0));
        opt.addEscherProperty(new EscherSimpleProperty(EscherProperties.GEOMETRY__RIGHT, false, false, hssfShape.getDrawAreaWidth()));
        opt.addEscherProperty(new EscherSimpleProperty(EscherProperties.GEOMETRY__BOTTOM, false, false, hssfShape.getDrawAreaHeight()));
        opt.addEscherProperty(new EscherShapePathProperty(EscherProperties.GEOMETRY__SHAPEPATH, EscherShapePathProperty.COMPLEX));
        EscherArrayProperty verticesProp = new EscherArrayProperty(EscherProperties.GEOMETRY__VERTICES, false, new byte[0] );
        verticesProp.setNumberOfElementsInArray(hssfShape.getXPoints().length+1);
        verticesProp.setNumberOfElementsInMemory(hssfShape.getXPoints().length+1);
        verticesProp.setSizeOfElements(0xFFF0);
        for (int i = 0; i < hssfShape.getXPoints().length; i++)
        {
            byte[] data = new byte[4];
            LittleEndian.putShort(data, 0, (short)hssfShape.getXPoints()[i]);
            LittleEndian.putShort(data, 2, (short)hssfShape.getYPoints()[i]);
            verticesProp.setElement(i, data);
        }
        int point = hssfShape.getXPoints().length;
        byte[] data = new byte[4];
        LittleEndian.putShort(data, 0, (short)hssfShape.getXPoints()[0]);
        LittleEndian.putShort(data, 2, (short)hssfShape.getYPoints()[0]);
        verticesProp.setElement(point, data);
        opt.addEscherProperty(verticesProp);
        EscherArrayProperty segmentsProp = new EscherArrayProperty(EscherProperties.GEOMETRY__SEGMENTINFO, false, null );
        segmentsProp.setSizeOfElements(0x0002);
        segmentsProp.setNumberOfElementsInArray(hssfShape.getXPoints().length * 2 + 4);
        segmentsProp.setNumberOfElementsInMemory(hssfShape.getXPoints().length * 2 + 4);
        segmentsProp.setElement(0, new byte[] { (byte)0x00, (byte)0x40 } );
        segmentsProp.setElement(1, new byte[] { (byte)0x00, (byte)0xAC } );
        for (int i = 0; i < hssfShape.getXPoints().length; i++)
        {
            segmentsProp.setElement(2 + i * 2, new byte[] { (byte)0x01, (byte)0x00 } );
            segmentsProp.setElement(3 + i * 2, new byte[] { (byte)0x00, (byte)0xAC } );
        }
        segmentsProp.setElement(segmentsProp.getNumberOfElementsInArray() - 2, new byte[] { (byte)0x01, (byte)0x60 } );
        segmentsProp.setElement(segmentsProp.getNumberOfElementsInArray() - 1, new byte[] { (byte)0x00, (byte)0x80 } );
        opt.addEscherProperty(segmentsProp);
        opt.addEscherProperty(new EscherSimpleProperty(EscherProperties.GEOMETRY__FILLOK, false, false, 0x00010001));
        opt.addEscherProperty(new EscherSimpleProperty(EscherProperties.LINESTYLE__LINESTARTARROWHEAD, false, false, 0x0));
        opt.addEscherProperty(new EscherSimpleProperty(EscherProperties.LINESTYLE__LINEENDARROWHEAD, false, false, 0x0));
        opt.addEscherProperty(new EscherSimpleProperty(EscherProperties.LINESTYLE__LINEENDCAPSTYLE, false, false, 0x0));

        addStandardOptions(shape, opt);

        EscherRecord anchor = createAnchor( shape.getAnchor() );
        clientData.setRecordId( EscherClientDataRecord.RECORD_ID );
        clientData.setOptions( (short) 0x0000 );

        spContainer.addChildRecord( sp );
        spContainer.addChildRecord( opt );
        spContainer.addChildRecord( anchor );
        spContainer.addChildRecord( clientData );

        return spContainer;
    }

    /**
     * Creates the low level OBJ record for this shape.
     */
    private ObjRecord createObjRecord( HSSFShape hssfShape, int shapeId )
    {
        HSSFShape shape = hssfShape;

        ObjRecord obj = new ObjRecord();
        CommonObjectDataSubRecord c = new CommonObjectDataSubRecord();
        c.setObjectType( OBJECT_TYPE_MICROSOFT_OFFICE_DRAWING );
        c.setObjectId(shapeId);
        c.setLocked( true );
        c.setPrintable( true );
        c.setAutofill( true );
        c.setAutoline( true );
        EndSubRecord e = new EndSubRecord();

        obj.addSubRecord( c );
        obj.addSubRecord( e );

        return obj;
    }

    public EscherContainerRecord getSpContainer()
    {
        return spContainer;
    }

    public ObjRecord getObjRecord()
    {
        return objRecord;
    }

}
