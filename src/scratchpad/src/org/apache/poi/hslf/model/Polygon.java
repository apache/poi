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

package org.apache.poi.hslf.model;

import org.apache.poi.ddf.*;
import org.apache.poi.hslf.usermodel.*;
import org.apache.poi.sl.usermodel.ShapeContainer;
import org.apache.poi.sl.usermodel.ShapeType;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.Units;

import java.awt.geom.Point2D;

/**
 * A simple closed polygon shape
 *
 * @author Yegor Kozlov
 */
public final class Polygon extends HSLFAutoShape {
    /**
     * Create a Polygon object and initialize it from the supplied Record container.
     *
     * @param escherRecord       <code>EscherSpContainer</code> container which holds information about this shape
     * @param parent    the parent of the shape
     */
   protected Polygon(EscherContainerRecord escherRecord, ShapeContainer<HSLFShape,HSLFTextParagraph> parent){
        super(escherRecord, parent);

    }

    /**
     * Create a new Polygon. This constructor is used when a new shape is created.
     *
     * @param parent    the parent of this Shape. For example, if this text box is a cell
     * in a table then the parent is Table.
     */
    public Polygon(ShapeContainer<HSLFShape,HSLFTextParagraph> parent){
        super((EscherContainerRecord)null, parent);
        createSpContainer(ShapeType.NOT_PRIMITIVE, parent instanceof HSLFGroupShape);
    }

    /**
     * Create a new Polygon. This constructor is used when a new shape is created.
     *
     */
    public Polygon(){
        this(null);
    }

    /**
     * Set the polygon vertices
     *
     * @param xPoints
     * @param yPoints
     */
    public void setPoints(float[] xPoints, float[] yPoints)
    {
        float right  = findBiggest(xPoints);
        float bottom = findBiggest(yPoints);
        float left   = findSmallest(xPoints);
        float top    = findSmallest(yPoints);

        AbstractEscherOptRecord opt = getEscherOptRecord();
        opt.addEscherProperty(new EscherSimpleProperty(EscherProperties.GEOMETRY__RIGHT, Units.pointsToMaster(right - left)));
        opt.addEscherProperty(new EscherSimpleProperty(EscherProperties.GEOMETRY__BOTTOM, Units.pointsToMaster(bottom - top)));

        for (int i = 0; i < xPoints.length; i++) {
            xPoints[i] += -left;
            yPoints[i] += -top;
        }

        int numpoints = xPoints.length;

        EscherArrayProperty verticesProp = new EscherArrayProperty(EscherProperties.GEOMETRY__VERTICES, false, new byte[0] );
        verticesProp.setNumberOfElementsInArray(numpoints+1);
        verticesProp.setNumberOfElementsInMemory(numpoints+1);
        verticesProp.setSizeOfElements(0xFFF0);
        for (int i = 0; i < numpoints; i++)
        {
            byte[] data = new byte[4];
            LittleEndian.putShort(data, 0, (short)Units.pointsToMaster(xPoints[i]));
            LittleEndian.putShort(data, 2, (short)Units.pointsToMaster(yPoints[i]));
            verticesProp.setElement(i, data);
        }
        byte[] data = new byte[4];
        LittleEndian.putShort(data, 0, (short)Units.pointsToMaster(xPoints[0]));
        LittleEndian.putShort(data, 2, (short)Units.pointsToMaster(yPoints[0]));
        verticesProp.setElement(numpoints, data);
        opt.addEscherProperty(verticesProp);

        EscherArrayProperty segmentsProp = new EscherArrayProperty(EscherProperties.GEOMETRY__SEGMENTINFO, false, null );
        segmentsProp.setSizeOfElements(0x0002);
        segmentsProp.setNumberOfElementsInArray(numpoints * 2 + 4);
        segmentsProp.setNumberOfElementsInMemory(numpoints * 2 + 4);
        segmentsProp.setElement(0, new byte[] { (byte)0x00, (byte)0x40 } );
        segmentsProp.setElement(1, new byte[] { (byte)0x00, (byte)0xAC } );
        for (int i = 0; i < numpoints; i++)
        {
            segmentsProp.setElement(2 + i * 2, new byte[] { (byte)0x01, (byte)0x00 } );
            segmentsProp.setElement(3 + i * 2, new byte[] { (byte)0x00, (byte)0xAC } );
        }
        segmentsProp.setElement(segmentsProp.getNumberOfElementsInArray() - 2, new byte[] { (byte)0x01, (byte)0x60 } );
        segmentsProp.setElement(segmentsProp.getNumberOfElementsInArray() - 1, new byte[] { (byte)0x00, (byte)0x80 } );
        opt.addEscherProperty(segmentsProp);

        opt.sortProperties();
    }

    /**
     * Set the polygon vertices
     *
     * @param points the polygon vertices
     */
      public void setPoints(Point2D[] points)
     {
        float[] xpoints = new float[points.length];
        float[] ypoints = new float[points.length];
        for (int i = 0; i < points.length; i++) {
            xpoints[i] = (float)points[i].getX();
            ypoints[i] = (float)points[i].getY();

        }

        setPoints(xpoints, ypoints);
    }

    private float findBiggest( float[] values )
    {
        float result = Float.MIN_VALUE;
        for ( int i = 0; i < values.length; i++ )
        {
            if (values[i] > result)
                result = values[i];
        }
        return result;
    }

    private float findSmallest( float[] values )
    {
        float result = Float.MAX_VALUE;
        for ( int i = 0; i < values.length; i++ )
        {
            if (values[i] < result)
                result = values[i];
        }
        return result;
    }


}
