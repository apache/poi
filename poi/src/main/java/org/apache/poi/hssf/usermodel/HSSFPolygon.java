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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ddf.EscherArrayProperty;
import org.apache.poi.ddf.EscherBoolProperty;
import org.apache.poi.ddf.EscherClientDataRecord;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherOptRecord;
import org.apache.poi.ddf.EscherPropertyTypes;
import org.apache.poi.ddf.EscherRGBProperty;
import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.ddf.EscherShapePathProperty;
import org.apache.poi.ddf.EscherSimpleProperty;
import org.apache.poi.ddf.EscherSpRecord;
import org.apache.poi.hssf.record.CommonObjectDataSubRecord;
import org.apache.poi.hssf.record.EndSubRecord;
import org.apache.poi.hssf.record.EscherAggregate;
import org.apache.poi.hssf.record.ObjRecord;
import org.apache.poi.hssf.record.TextObjectRecord;
import org.apache.poi.util.LittleEndian;

/**
 *
 */
public class HSSFPolygon  extends HSSFSimpleShape {
    public static final short OBJECT_TYPE_MICROSOFT_OFFICE_DRAWING = 0x1E;

    private static final Logger LOG = LogManager.getLogger(HSSFPolygon.class);

    public HSSFPolygon(EscherContainerRecord spContainer, ObjRecord objRecord, TextObjectRecord _textObjectRecord) {
        super(spContainer, objRecord, _textObjectRecord);
    }

    public HSSFPolygon(EscherContainerRecord spContainer, ObjRecord objRecord) {
        super(spContainer, objRecord);
    }

    HSSFPolygon(HSSFShape parent, HSSFAnchor anchor) {
        super(parent, anchor);
    }

    @Override
    protected TextObjectRecord createTextObjRecord() {
        return null;
    }

    /**
     * Generates the shape records for this shape.
     */
    protected EscherContainerRecord createSpContainer() {
        EscherContainerRecord spContainer = new EscherContainerRecord();
        EscherSpRecord sp = new EscherSpRecord();
        EscherOptRecord opt = new EscherOptRecord();
        EscherClientDataRecord clientData = new EscherClientDataRecord();

        spContainer.setRecordId(EscherContainerRecord.SP_CONTAINER);
        spContainer.setOptions((short) 0x000F);
        sp.setRecordId(EscherSpRecord.RECORD_ID);
        sp.setOptions((short) ((EscherAggregate.ST_NOT_PRIMATIVE << 4) | 0x2));
        if (getParent() == null) {
            sp.setFlags(EscherSpRecord.FLAG_HAVEANCHOR | EscherSpRecord.FLAG_HASSHAPETYPE);
        } else {
            sp.setFlags(EscherSpRecord.FLAG_CHILD | EscherSpRecord.FLAG_HAVEANCHOR | EscherSpRecord.FLAG_HASSHAPETYPE);
        }
        opt.setRecordId(EscherOptRecord.RECORD_ID);
        opt.setEscherProperty(new EscherSimpleProperty(EscherPropertyTypes.TRANSFORM__ROTATION, false, false, 0));
        opt.setEscherProperty(new EscherSimpleProperty(EscherPropertyTypes.GEOMETRY__RIGHT, false, false, 100));
        opt.setEscherProperty(new EscherSimpleProperty(EscherPropertyTypes.GEOMETRY__BOTTOM, false, false, 100));
        opt.setEscherProperty(new EscherShapePathProperty(EscherPropertyTypes.GEOMETRY__SHAPEPATH, EscherShapePathProperty.COMPLEX));

        opt.setEscherProperty(new EscherSimpleProperty(EscherPropertyTypes.GEOMETRY__FILLOK, false, false, 0x00010001));
        opt.setEscherProperty(new EscherSimpleProperty(EscherPropertyTypes.LINESTYLE__LINESTARTARROWHEAD, false, false, 0x0));
        opt.setEscherProperty(new EscherSimpleProperty(EscherPropertyTypes.LINESTYLE__LINEENDARROWHEAD, false, false, 0x0));
        opt.setEscherProperty(new EscherSimpleProperty(EscherPropertyTypes.LINESTYLE__LINEENDCAPSTYLE, false, false, 0x0));

        opt.setEscherProperty(new EscherSimpleProperty(EscherPropertyTypes.LINESTYLE__LINEDASHING, LINESTYLE_SOLID));
        opt.setEscherProperty( new EscherBoolProperty( EscherPropertyTypes.LINESTYLE__NOLINEDRAWDASH, 0x00080008));
        opt.setEscherProperty(new EscherSimpleProperty(EscherPropertyTypes.LINESTYLE__LINEWIDTH, LINEWIDTH_DEFAULT));
        opt.setEscherProperty(new EscherRGBProperty(EscherPropertyTypes.FILL__FILLCOLOR, FILL__FILLCOLOR_DEFAULT));
        opt.setEscherProperty(new EscherRGBProperty(EscherPropertyTypes.LINESTYLE__COLOR, LINESTYLE__COLOR_DEFAULT));
        opt.setEscherProperty(new EscherBoolProperty(EscherPropertyTypes.FILL__NOFILLHITTEST, 1));

        opt.setEscherProperty(new EscherBoolProperty( EscherPropertyTypes.GROUPSHAPE__FLAGS, 0x080000));

        EscherRecord anchor = getAnchor().getEscherAnchor();
        clientData.setRecordId(EscherClientDataRecord.RECORD_ID);
        clientData.setOptions((short) 0x0000);

        spContainer.addChildRecord(sp);
        spContainer.addChildRecord(opt);
        spContainer.addChildRecord(anchor);
        spContainer.addChildRecord(clientData);

        return spContainer;
    }

    /**
     * Creates the low level OBJ record for this shape.
     */
    protected ObjRecord createObjRecord() {
        ObjRecord obj = new ObjRecord();
        CommonObjectDataSubRecord c = new CommonObjectDataSubRecord();
        c.setObjectType(OBJECT_TYPE_MICROSOFT_OFFICE_DRAWING);
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
    protected void afterRemove(HSSFPatriarch patriarch) {
        patriarch.getBoundAggregate().removeShapeToObjRecord(getEscherContainer().getChildById(EscherClientDataRecord.RECORD_ID));
    }

    /**
     * @return array of x coordinates
     */
    public int[] getXPoints() {
        EscherArrayProperty verticesProp = getOptRecord().lookup(EscherPropertyTypes.GEOMETRY__VERTICES);
        if (null == verticesProp){
            return new int[]{};
        }
        int []array = new int[verticesProp.getNumberOfElementsInArray()-1];
        for (int i=0; i< verticesProp.getNumberOfElementsInArray()-1; i++){
            byte[] property = verticesProp.getElement(i);
            short x = LittleEndian.getShort(property, 0);
            array[i] = x;
        }
        return array;
    }

    /**
     * @return array of y coordinates
     */
    public int[] getYPoints() {
        EscherArrayProperty verticesProp = getOptRecord().lookup(EscherPropertyTypes.GEOMETRY__VERTICES);
        if (null == verticesProp){
            return new int[]{};
        }
        int []array = new int[verticesProp.getNumberOfElementsInArray()-1];
        for (int i=0; i< verticesProp.getNumberOfElementsInArray()-1; i++){
            byte[] property = verticesProp.getElement(i);
            short x = LittleEndian.getShort(property, 2);
            array[i] = x;
        }
        return array;
    }

    /**
     * @param xPoints - array of x coordinates
     * @param yPoints - array of y coordinates
     */
    public void setPoints(int[] xPoints, int[] yPoints) {
        if (xPoints.length != yPoints.length){
        	LOG.atError().log("xPoint.length must be equal to yPoints.length");
            return;
        }
        if (xPoints.length == 0){
        	LOG.atError().log("HSSFPolygon must have at least one point");
        }
        EscherArrayProperty verticesProp = new EscherArrayProperty(EscherPropertyTypes.GEOMETRY__VERTICES, false, 0);
        verticesProp.setNumberOfElementsInArray(xPoints.length+1);
        verticesProp.setNumberOfElementsInMemory(xPoints.length+1);
        verticesProp.setSizeOfElements(0xFFF0);
        for (int i = 0; i < xPoints.length; i++)
        {
            byte[] data = new byte[4];
            LittleEndian.putShort(data, 0, (short)xPoints[i]);
            LittleEndian.putShort(data, 2, (short)yPoints[i]);
            verticesProp.setElement(i, data);
        }
        int point = xPoints.length;
        byte[] data = new byte[4];
        LittleEndian.putShort(data, 0, (short)xPoints[0]);
        LittleEndian.putShort(data, 2, (short)yPoints[0]);
        verticesProp.setElement(point, data);
        setPropertyValue(verticesProp);

        EscherArrayProperty segmentsProp = new EscherArrayProperty(EscherPropertyTypes.GEOMETRY__SEGMENTINFO, false, 0);
        segmentsProp.setSizeOfElements(0x0002);
        segmentsProp.setNumberOfElementsInArray(xPoints.length * 2 + 4);
        segmentsProp.setNumberOfElementsInMemory(xPoints.length * 2 + 4);
        segmentsProp.setElement(0, new byte[] { (byte)0x00, (byte)0x40 } );
        segmentsProp.setElement(1, new byte[] { (byte)0x00, (byte)0xAC } );
        for (int i = 0; i < xPoints.length; i++)
        {
            segmentsProp.setElement(2 + i * 2, new byte[] { (byte)0x01, (byte)0x00 } );
            segmentsProp.setElement(3 + i * 2, new byte[] { (byte)0x00, (byte)0xAC } );
        }
        segmentsProp.setElement(segmentsProp.getNumberOfElementsInArray() - 2, new byte[] { (byte)0x01, (byte)0x60 } );
        segmentsProp.setElement(segmentsProp.getNumberOfElementsInArray() - 1, new byte[] { (byte)0x00, (byte)0x80 } );
        setPropertyValue(segmentsProp);
    }

    /**
     * Defines the width and height of the points in the polygon
     * @param width
     * @param height
     */
    public void setPolygonDrawArea(int width, int height) {
        setPropertyValue(new EscherSimpleProperty(EscherPropertyTypes.GEOMETRY__RIGHT, width));
        setPropertyValue(new EscherSimpleProperty(EscherPropertyTypes.GEOMETRY__BOTTOM, height));
    }

    /**
     * @return shape width
     */
    public int getDrawAreaWidth() {
        EscherSimpleProperty property = getOptRecord().lookup(EscherPropertyTypes.GEOMETRY__RIGHT);
        return property == null ? 100: property.getPropertyValue();
    }

    /**
     * @return shape height
     */
    public int getDrawAreaHeight() {
        EscherSimpleProperty property = getOptRecord().lookup(EscherPropertyTypes.GEOMETRY__BOTTOM);
        return property == null ? 100: property.getPropertyValue();
    }
}
