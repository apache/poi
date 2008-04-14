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
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.POILogger;

import java.awt.geom.*;
import java.util.ArrayList;

/**
 * A "Freeform" shape.
 *
 * <p>
 * Shapes drawn with the "Freeform" tool have cubic bezier curve segments in the smooth sections
 * and straight-line segments in the straight sections. This object closely corresponds to <code>java.awt.geom.GeneralPath</code>.
 * </p>
 * @author Yegor Kozlov
 */
public class Freeform extends AutoShape {
    /**
     * Create a Freeform object and initialize it from the supplied Record container.
     *
     * @param escherRecord       <code>EscherSpContainer</code> container which holds information about this shape
     * @param parent    the parent of the shape
     */
   protected Freeform(EscherContainerRecord escherRecord, Shape parent){
        super(escherRecord, parent);

    }

    /**
     * Create a new Freeform. This constructor is used when a new shape is created.
     *
     * @param parent    the parent of this Shape. For example, if this text box is a cell
     * in a table then the parent is Table.
     */
    public Freeform(Shape parent){
        super(null, parent);
        _escherContainer = createSpContainer(ShapeTypes.NotPrimitive, parent instanceof ShapeGroup);
    }

    /**
     * Create a new Freeform. This constructor is used when a new shape is created.
     *
     */
    public Freeform(){
        this(null);
    }

    /**
     * Set the shape path
     *
     * @param path
     */
    public void setPath(GeneralPath path)
    {
        Rectangle2D bounds = path.getBounds2D();
        PathIterator it = path.getPathIterator(new AffineTransform());

        ArrayList segInfo = new ArrayList();
        ArrayList pntInfo = new ArrayList();
        boolean isClosed = false;
        while (!it.isDone()) {
            double[] vals = new double[6];
            int type = it.currentSegment(vals);
            switch (type) {
                case PathIterator.SEG_MOVETO:
                    pntInfo.add(new Point2D.Double(vals[0], vals[1]));
                    segInfo.add(new byte[]{0x00, 0x40});
                    break;
                case PathIterator.SEG_LINETO:
                    pntInfo.add(new Point2D.Double(vals[0], vals[1]));
                    segInfo.add(new byte[]{0x00, (byte)0xAC});
                    segInfo.add(new byte[]{0x01, 0x00 });
                    break;
                case PathIterator.SEG_CUBICTO:
                    pntInfo.add(new Point2D.Double(vals[0], vals[1]));
                    pntInfo.add(new Point2D.Double(vals[2], vals[3]));
                    pntInfo.add(new Point2D.Double(vals[4], vals[5]));
                    segInfo.add(new byte[]{0x00, (byte)0xAD});
                    segInfo.add(new byte[]{0x01, 0x20 });
                    break;
                case PathIterator.SEG_QUADTO:
                    logger.log(POILogger.WARN, "SEG_QUADTO is not supported");
                    break;
                case PathIterator.SEG_CLOSE:
                    pntInfo.add(pntInfo.get(0));
                    segInfo.add(new byte[]{0x00, (byte)0xAC});
                    segInfo.add(new byte[]{0x01, 0x00 });
                    segInfo.add(new byte[]{0x00, (byte)0xAC});
                    segInfo.add(new byte[]{0x01, (byte)0x60});
                    isClosed = true;
                    break;
            }

            it.next();
        }
        if(!isClosed) segInfo.add(new byte[]{0x00, (byte)0xAC});
        segInfo.add(new byte[]{0x00, (byte)0x80});

        EscherOptRecord opt = (EscherOptRecord)getEscherChild(_escherContainer, EscherOptRecord.RECORD_ID);
        opt.addEscherProperty(new EscherSimpleProperty(EscherProperties.GEOMETRY__SHAPEPATH, 0x4));

        EscherArrayProperty verticesProp = new EscherArrayProperty((short)(EscherProperties.GEOMETRY__VERTICES + 0x4000), false, null);
        verticesProp.setNumberOfElementsInArray(pntInfo.size());
        verticesProp.setNumberOfElementsInMemory(pntInfo.size());
        verticesProp.setSizeOfElements(0xFFF0);
        for (int i = 0; i < pntInfo.size(); i++) {
            Point2D.Double pnt = (Point2D.Double)pntInfo.get(i);
            byte[] data = new byte[4];
            LittleEndian.putShort(data, 0, (short)((pnt.getX() - bounds.getX())*MASTER_DPI/POINT_DPI));
            LittleEndian.putShort(data, 2, (short)((pnt.getY() - bounds.getY())*MASTER_DPI/POINT_DPI));
            verticesProp.setElement(i, data);
        }
        opt.addEscherProperty(verticesProp);

        EscherArrayProperty segmentsProp = new EscherArrayProperty((short)(EscherProperties.GEOMETRY__SEGMENTINFO + 0x4000), false, null);
        segmentsProp.setNumberOfElementsInArray(segInfo.size());
        segmentsProp.setNumberOfElementsInMemory(segInfo.size());
        segmentsProp.setSizeOfElements(0x2);
        for (int i = 0; i < segInfo.size(); i++) {
            byte[] seg = (byte[])segInfo.get(i);
            segmentsProp.setElement(i, seg);
        }
        opt.addEscherProperty(segmentsProp);

        opt.addEscherProperty(new EscherSimpleProperty(EscherProperties.GEOMETRY__RIGHT, (int)(bounds.getWidth()*MASTER_DPI/POINT_DPI)));
        opt.addEscherProperty(new EscherSimpleProperty(EscherProperties.GEOMETRY__BOTTOM, (int)(bounds.getHeight()*MASTER_DPI/POINT_DPI)));

        opt.sortProperties();

        setAnchor(bounds);
    }
}
