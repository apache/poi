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

import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ddf.AbstractEscherOptRecord;
import org.apache.poi.ddf.EscherArrayProperty;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherPropertyTypes;
import org.apache.poi.ddf.EscherSimpleProperty;
import org.apache.poi.sl.usermodel.FreeformShape;
import org.apache.poi.sl.usermodel.ShapeContainer;
import org.apache.poi.sl.usermodel.ShapeType;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.Units;

/**
 * A "Freeform" shape.
 *
 * <p>
 * Shapes drawn with the "Freeform" tool have cubic bezier curve segments in the smooth sections
 * and straight-line segments in the straight sections. This object closely corresponds to <code>java.awt.geom.GeneralPath</code>.
 * </p>
 */
public final class HSLFFreeformShape extends HSLFAutoShape implements FreeformShape<HSLFShape,HSLFTextParagraph> {
    private static final POILogger LOG = POILogFactory.getLogger(HSLFFreeformShape.class);


    enum ShapePath {
        LINES(0),
        LINES_CLOSED(1),
        CURVES(2),
        CURVES_CLOSED(3),
        COMPLEX(4);

        private final int flag;
        ShapePath(int flag) {
            this.flag = flag;
        }
        public int getFlag() {
            return flag;
        }
        static ShapePath valueOf(int flag) {
            for (ShapePath v : values()) {
                if (v.flag == flag) {
                    return v;
                }
            }
            return null;
        }
    }
    
    /**
     * Create a Freeform object and initialize it from the supplied Record container.
     *
     * @param escherRecord       <code>EscherSpContainer</code> container which holds information about this shape
     * @param parent    the parent of the shape
     */
   protected HSLFFreeformShape(EscherContainerRecord escherRecord, ShapeContainer<HSLFShape,HSLFTextParagraph> parent){
        super(escherRecord, parent);

    }

    /**
     * Create a new Freeform. This constructor is used when a new shape is created.
     *
     * @param parent    the parent of this Shape. For example, if this text box is a cell
     * in a table then the parent is Table.
     */
    public HSLFFreeformShape(ShapeContainer<HSLFShape,HSLFTextParagraph> parent){
        super((EscherContainerRecord)null, parent);
        createSpContainer(ShapeType.NOT_PRIMITIVE, parent instanceof HSLFGroupShape);
    }

    /**
     * Create a new Freeform. This constructor is used when a new shape is created.
     *
     */
    public HSLFFreeformShape(){
        this(null);
    }

    @Override
    public int setPath(Path2D path) {
        Rectangle2D bounds = path.getBounds2D();
        PathIterator it = path.getPathIterator(null);

        List<byte[]> segInfo = new ArrayList<>();
        List<Point2D.Double> pntInfo = new ArrayList<>();
        boolean isClosed = false;
        int numPoints = 0;
        while (!it.isDone()) {
            double[] vals = new double[6];
            int type = it.currentSegment(vals);
            switch (type) {
                case PathIterator.SEG_MOVETO:
                    pntInfo.add(new Point2D.Double(vals[0], vals[1]));
                    segInfo.add(SEGMENTINFO_MOVETO);
                    numPoints++;
                    break;
                case PathIterator.SEG_LINETO:
                    pntInfo.add(new Point2D.Double(vals[0], vals[1]));
                    segInfo.add(SEGMENTINFO_LINETO);
                    segInfo.add(SEGMENTINFO_ESCAPE);
                    numPoints++;
                    break;
                case PathIterator.SEG_CUBICTO:
                    pntInfo.add(new Point2D.Double(vals[0], vals[1]));
                    pntInfo.add(new Point2D.Double(vals[2], vals[3]));
                    pntInfo.add(new Point2D.Double(vals[4], vals[5]));
                    segInfo.add(SEGMENTINFO_CUBICTO);
                    segInfo.add(SEGMENTINFO_ESCAPE2);
                    numPoints++;
                    break;
                case PathIterator.SEG_QUADTO:
                    //TODO: figure out how to convert SEG_QUADTO into SEG_CUBICTO
                    LOG.log(POILogger.WARN, "SEG_QUADTO is not supported");
                    break;
                case PathIterator.SEG_CLOSE:
                    pntInfo.add(pntInfo.get(0));
                    segInfo.add(SEGMENTINFO_LINETO);
                    segInfo.add(SEGMENTINFO_ESCAPE);
                    segInfo.add(SEGMENTINFO_LINETO);
                    segInfo.add(SEGMENTINFO_CLOSE);
                    isClosed = true;
                    numPoints++;
                    break;
                default:
                    LOG.log(POILogger.WARN, "Ignoring invalid segment type ", type);
                    break;
            }

            it.next();
        }
        if(!isClosed) {
            segInfo.add(SEGMENTINFO_LINETO);
        }
        segInfo.add(SEGMENTINFO_END);

        AbstractEscherOptRecord opt = getEscherOptRecord();
        opt.addEscherProperty(new EscherSimpleProperty(EscherPropertyTypes.GEOMETRY__SHAPEPATH, 0x4));

        EscherArrayProperty verticesProp = new EscherArrayProperty(EscherPropertyTypes.GEOMETRY__VERTICES, true, 0);
        verticesProp.setNumberOfElementsInArray(pntInfo.size());
        verticesProp.setNumberOfElementsInMemory(pntInfo.size());
        verticesProp.setSizeOfElements(8);
        for (int i = 0; i < pntInfo.size(); i++) {
            Point2D.Double pnt = pntInfo.get(i);
            byte[] data = new byte[8];
            LittleEndian.putInt(data, 0, Units.pointsToMaster(pnt.getX() - bounds.getX()));
            LittleEndian.putInt(data, 4, Units.pointsToMaster(pnt.getY() - bounds.getY()));
            verticesProp.setElement(i, data);
        }
        opt.addEscherProperty(verticesProp);

        EscherArrayProperty segmentsProp = new EscherArrayProperty(EscherPropertyTypes.GEOMETRY__SEGMENTINFO, true, 0);
        segmentsProp.setNumberOfElementsInArray(segInfo.size());
        segmentsProp.setNumberOfElementsInMemory(segInfo.size());
        segmentsProp.setSizeOfElements(0x2);
        for (int i = 0; i < segInfo.size(); i++) {
            byte[] seg = segInfo.get(i);
            segmentsProp.setElement(i, seg);
        }
        opt.addEscherProperty(segmentsProp);

        opt.addEscherProperty(new EscherSimpleProperty(EscherPropertyTypes.GEOMETRY__RIGHT, Units.pointsToMaster(bounds.getWidth())));
        opt.addEscherProperty(new EscherSimpleProperty(EscherPropertyTypes.GEOMETRY__BOTTOM, Units.pointsToMaster(bounds.getHeight())));

        opt.sortProperties();

        setAnchor(bounds);

        return numPoints;
    }

    @Override
    public Path2D getPath(){
        Path2D path2D = new Path2D.Double();
        getGeometry(path2D);

        Rectangle2D bounds = path2D.getBounds2D();
        Rectangle2D anchor = getAnchor();
        AffineTransform at = new AffineTransform();
        at.translate(anchor.getX(), anchor.getY());
        at.scale(
                anchor.getWidth()/bounds.getWidth(),
                anchor.getHeight()/bounds.getHeight()
        );

        path2D.transform(at);


        return path2D;
    }


}
