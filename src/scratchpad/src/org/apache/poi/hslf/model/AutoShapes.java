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

import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import org.apache.poi.ddf.EscherProperties;
import org.apache.poi.sl.usermodel.ShapeType;

/**
 * Stores definition of auto-shapes.
 * See the Office Drawing 97-2007 Binary Format Specification for details.
 *
 * TODO: follow the spec and define all the auto-shapes
 *
 * @author Yegor Kozlov
 */
public final class AutoShapes {
	protected static final ShapeOutline[] shapes;


    /**
     * Return shape outline by shape type
     * @param type shape type see {@link ShapeTypes}
     *
     * @return the shape outline
     */
    public static ShapeOutline getShapeOutline(ShapeType type){
        ShapeOutline outline = shapes[type.nativeId];
        return outline;
    }

    /**
     * Auto-shapes are defined in the [0,21600] coordinate system.
     * We need to transform it into normal slide coordinates
     *
    */
    public static java.awt.Shape transform(java.awt.Shape outline, Rectangle2D anchor){
        AffineTransform at = new AffineTransform();
        at.translate(anchor.getX(), anchor.getY());
        at.scale(
                1.0f/21600*anchor.getWidth(),
                1.0f/21600*anchor.getHeight()
        );
        return at.createTransformedShape(outline);
    }

    static {
        shapes = new ShapeOutline[255];

        shapes[ShapeType.RECT.nativeId] = new ShapeOutline(){
            public java.awt.Shape getOutline(Shape shape){
                Rectangle2D path = new Rectangle2D.Float(0, 0, 21600, 21600);
                return path;
            }
        };

        shapes[ShapeType.ROUND_RECT.nativeId] = new ShapeOutline(){
            public java.awt.Shape getOutline(Shape shape){
                int adjval = shape.getEscherProperty(EscherProperties.GEOMETRY__ADJUSTVALUE, 5400);
                RoundRectangle2D path = new RoundRectangle2D.Float(0, 0, 21600, 21600, adjval, adjval);
                return path;
            }
        };

        shapes[ShapeType.ELLIPSE.nativeId] = new ShapeOutline(){
            public java.awt.Shape getOutline(Shape shape){
                Ellipse2D path = new Ellipse2D.Float(0, 0, 21600, 21600);
                return path;
            }
        };

        shapes[ShapeType.DIAMOND.nativeId] = new ShapeOutline(){
            public java.awt.Shape getOutline(Shape shape){
                GeneralPath path = new GeneralPath();
                path.moveTo(10800, 0);
                path.lineTo(21600, 10800);
                path.lineTo(10800, 21600);
                path.lineTo(0, 10800);
                path.closePath();
                return path;
           }
        };

        //m@0,l,21600r21600
        shapes[ShapeType.TRIANGLE.nativeId] = new ShapeOutline(){
            public java.awt.Shape getOutline(Shape shape){
                int adjval = shape.getEscherProperty(EscherProperties.GEOMETRY__ADJUSTVALUE, 10800);
                GeneralPath path = new GeneralPath();
                path.moveTo(adjval, 0);
                path.lineTo(0, 21600);
                path.lineTo(21600, 21600);
                path.closePath();
                return path;
           }
        };

        shapes[ShapeType.RT_TRIANGLE.nativeId] = new ShapeOutline(){
            public java.awt.Shape getOutline(Shape shape){
                GeneralPath path = new GeneralPath();
                path.moveTo(0, 0);
                path.lineTo(21600, 21600);
                path.lineTo(0, 21600);
                path.closePath();
                return path;
           }
        };

        shapes[ShapeType.PARALLELOGRAM.nativeId] = new ShapeOutline(){
            public java.awt.Shape getOutline(Shape shape){
                int adjval = shape.getEscherProperty(EscherProperties.GEOMETRY__ADJUSTVALUE, 5400);

                GeneralPath path = new GeneralPath();
                path.moveTo(adjval, 0);
                path.lineTo(21600, 0);
                path.lineTo(21600 - adjval, 21600);
                path.lineTo(0, 21600);
                path.closePath();
                return path;
            }
        };

        shapes[ShapeType.TRAPEZOID.nativeId] = new ShapeOutline(){
            public java.awt.Shape getOutline(Shape shape){
                int adjval = shape.getEscherProperty(EscherProperties.GEOMETRY__ADJUSTVALUE, 5400);

                GeneralPath path = new GeneralPath();
                path.moveTo(0, 0);
                path.lineTo(adjval, 21600);
                path.lineTo(21600 - adjval, 21600);
                path.lineTo(21600, 0);
                path.closePath();
                return path;
            }
        };

        shapes[ShapeType.HEXAGON.nativeId] = new ShapeOutline(){
            public java.awt.Shape getOutline(Shape shape){
                int adjval = shape.getEscherProperty(EscherProperties.GEOMETRY__ADJUSTVALUE, 5400);

                GeneralPath path = new GeneralPath();
                path.moveTo(adjval, 0);
                path.lineTo(21600 - adjval, 0);
                path.lineTo(21600, 10800);
                path.lineTo(21600 - adjval, 21600);
                path.lineTo(adjval, 21600);
                path.lineTo(0, 10800);
                path.closePath();
                return path;
            }
        };

        shapes[ShapeType.OCTAGON.nativeId] = new ShapeOutline(){
            public java.awt.Shape getOutline(Shape shape){
                int adjval = shape.getEscherProperty(EscherProperties.GEOMETRY__ADJUSTVALUE, 6326);

                GeneralPath path = new GeneralPath();
                path.moveTo(adjval, 0);
                path.lineTo(21600 - adjval, 0);
                path.lineTo(21600, adjval);
                path.lineTo(21600, 21600-adjval);
                path.lineTo(21600-adjval, 21600);
                path.lineTo(adjval, 21600);
                path.lineTo(0, 21600-adjval);
                path.lineTo(0, adjval);
                path.closePath();
                return path;
            }
        };

        shapes[ShapeType.PLUS.nativeId] = new ShapeOutline(){
            public java.awt.Shape getOutline(Shape shape){
                int adjval = shape.getEscherProperty(EscherProperties.GEOMETRY__ADJUSTVALUE, 5400);

                GeneralPath path = new GeneralPath();
                path.moveTo(adjval, 0);
                path.lineTo(21600 - adjval, 0);
                path.lineTo(21600 - adjval, adjval);
                path.lineTo(21600, adjval);
                path.lineTo(21600, 21600-adjval);
                path.lineTo(21600-adjval, 21600-adjval);
                path.lineTo(21600-adjval, 21600);
                path.lineTo(adjval, 21600);
                path.lineTo(adjval, 21600-adjval);
                path.lineTo(0, 21600-adjval);
                path.lineTo(0, adjval);
                path.lineTo(adjval, adjval);
                path.closePath();
                return path;
            }
        };

        shapes[ShapeType.PENTAGON.nativeId] = new ShapeOutline(){
            public java.awt.Shape getOutline(Shape shape){

                GeneralPath path = new GeneralPath();
                path.moveTo(10800, 0);
                path.lineTo(21600, 8259);
                path.lineTo(21600 - 4200, 21600);
                path.lineTo(4200, 21600);
                path.lineTo(0, 8259);
                path.closePath();
                return path;
            }
        };

        shapes[ShapeType.DOWN_ARROW.nativeId] = new ShapeOutline(){
            public java.awt.Shape getOutline(Shape shape){
                //m0@0 l@1@0 @1,0 @2,0 @2@0,21600@0,10800,21600xe
                int adjval = shape.getEscherProperty(EscherProperties.GEOMETRY__ADJUSTVALUE, 16200);
                int adjval2 = shape.getEscherProperty(EscherProperties.GEOMETRY__ADJUST2VALUE, 5400);
                GeneralPath path = new GeneralPath();
                path.moveTo(0, adjval);
                path.lineTo(adjval2, adjval);
                path.lineTo(adjval2, 0);
                path.lineTo(21600-adjval2, 0);
                path.lineTo(21600-adjval2, adjval);
                path.lineTo(21600, adjval);
                path.lineTo(10800, 21600);
                path.closePath();
                return path;
            }
        };

        shapes[ShapeType.UP_ARROW.nativeId] = new ShapeOutline(){
            public java.awt.Shape getOutline(Shape shape){
                //m0@0 l@1@0 @1,21600@2,21600@2@0,21600@0,10800,xe
                int adjval = shape.getEscherProperty(EscherProperties.GEOMETRY__ADJUSTVALUE, 5400);
                int adjval2 = shape.getEscherProperty(EscherProperties.GEOMETRY__ADJUST2VALUE, 5400);
                GeneralPath path = new GeneralPath();
                path.moveTo(0, adjval);
                path.lineTo(adjval2, adjval);
                path.lineTo(adjval2, 21600);
                path.lineTo(21600-adjval2, 21600);
                path.lineTo(21600-adjval2, adjval);
                path.lineTo(21600, adjval);
                path.lineTo(10800, 0);
                path.closePath();
                return path;
            }
        };

        shapes[ShapeType.RIGHT_ARROW.nativeId] = new ShapeOutline(){
            public java.awt.Shape getOutline(Shape shape){
                //m@0, l@0@1 ,0@1,0@2@0@2@0,21600,21600,10800xe
                int adjval = shape.getEscherProperty(EscherProperties.GEOMETRY__ADJUSTVALUE, 16200);
                int adjval2 = shape.getEscherProperty(EscherProperties.GEOMETRY__ADJUST2VALUE, 5400);
                GeneralPath path = new GeneralPath();
                path.moveTo(adjval, 0);
                path.lineTo(adjval, adjval2);
                path.lineTo(0, adjval2);
                path.lineTo(0, 21600-adjval2);
                path.lineTo(adjval, 21600-adjval2);
                path.lineTo(adjval, 21600);
                path.lineTo(21600, 10800);
                path.closePath();
                return path;
            }
        };

        shapes[ShapeType.LEFT_ARROW.nativeId] = new ShapeOutline(){
            public java.awt.Shape getOutline(Shape shape){
                //m@0, l@0@1,21600@1,21600@2@0@2@0,21600,,10800xe
                int adjval = shape.getEscherProperty(EscherProperties.GEOMETRY__ADJUSTVALUE, 5400);
                int adjval2 = shape.getEscherProperty(EscherProperties.GEOMETRY__ADJUST2VALUE, 5400);
                GeneralPath path = new GeneralPath();
                path.moveTo(adjval, 0);
                path.lineTo(adjval, adjval2);
                path.lineTo(21600, adjval2);
                path.lineTo(21600, 21600-adjval2);
                path.lineTo(adjval, 21600-adjval2);
                path.lineTo(adjval, 21600);
                path.lineTo(0, 10800);
                path.closePath();
                return path;
            }
        };

        shapes[ShapeType.CAN.nativeId] = new ShapeOutline(){
            public java.awt.Shape getOutline(Shape shape){
                //m10800,qx0@1l0@2qy10800,21600,21600@2l21600@1qy10800,xem0@1qy10800@0,21600@1nfe
                int adjval = shape.getEscherProperty(EscherProperties.GEOMETRY__ADJUSTVALUE, 5400);

                GeneralPath path = new GeneralPath();

                path.append(new Arc2D.Float(0, 0, 21600, adjval, 0, 180, Arc2D.OPEN), false);
                path.moveTo(0, adjval/2);

                path.lineTo(0, 21600 - adjval/2);
                path.closePath();

                path.append(new Arc2D.Float(0, 21600 - adjval, 21600, adjval, 180, 180, Arc2D.OPEN), false);
                path.moveTo(21600, 21600 - adjval/2);

                path.lineTo(21600, adjval/2);
                path.append(new Arc2D.Float(0, 0, 21600, adjval, 180, 180, Arc2D.OPEN), false);
                path.moveTo(0, adjval/2);
                path.closePath();
                return path;
            }
        };

        shapes[ShapeType.LEFT_BRACE.nativeId] = new ShapeOutline(){
            public java.awt.Shape getOutline(Shape shape){
                //m21600,qx10800@0l10800@2qy0@11,10800@3l10800@1qy21600,21600e
                int adjval = shape.getEscherProperty(EscherProperties.GEOMETRY__ADJUSTVALUE, 1800);
                int adjval2 = shape.getEscherProperty(EscherProperties.GEOMETRY__ADJUST2VALUE, 10800);

                GeneralPath path = new GeneralPath();
                path.moveTo(21600, 0);

                path.append(new Arc2D.Float(10800, 0, 21600, adjval*2, 90, 90, Arc2D.OPEN), false);
                path.moveTo(10800, adjval);

                path.lineTo(10800, adjval2 - adjval);

                path.append(new Arc2D.Float(-10800, adjval2 - 2*adjval, 21600, adjval*2, 270, 90, Arc2D.OPEN), false);
                path.moveTo(0, adjval2);

                path.append(new Arc2D.Float(-10800, adjval2, 21600, adjval*2, 0, 90, Arc2D.OPEN), false);
                path.moveTo(10800, adjval2 + adjval);

                path.lineTo(10800, 21600 - adjval);

                path.append(new Arc2D.Float(10800, 21600 - 2*adjval, 21600, adjval*2, 180, 90, Arc2D.OPEN), false);

                return path;
            }
        };

        shapes[ShapeType.RIGHT_BRACE.nativeId] = new ShapeOutline(){
            public java.awt.Shape getOutline(Shape shape){
                //m,qx10800@0 l10800@2qy21600@11,10800@3l10800@1qy,21600e
                int adjval = shape.getEscherProperty(EscherProperties.GEOMETRY__ADJUSTVALUE, 1800);
                int adjval2 = shape.getEscherProperty(EscherProperties.GEOMETRY__ADJUST2VALUE, 10800);

                GeneralPath path = new GeneralPath();
                path.moveTo(0, 0);

                path.append(new Arc2D.Float(-10800, 0, 21600, adjval*2, 0, 90, Arc2D.OPEN), false);
                path.moveTo(10800, adjval);

                path.lineTo(10800, adjval2 - adjval);

                path.append(new Arc2D.Float(10800, adjval2 - 2*adjval, 21600, adjval*2, 180, 90, Arc2D.OPEN), false);
                path.moveTo(21600, adjval2);

                path.append(new Arc2D.Float(10800, adjval2, 21600, adjval*2, 90, 90, Arc2D.OPEN), false);
                path.moveTo(10800, adjval2 + adjval);

                path.lineTo(10800, 21600 - adjval);

                path.append(new Arc2D.Float(-10800, 21600 - 2*adjval, 21600, adjval*2, 270, 90, Arc2D.OPEN), false);

                return path;
            }
        };

        shapes[ShapeType.STRAIGHT_CONNECTOR_1.nativeId] = new ShapeOutline(){
            public java.awt.Shape getOutline(Shape shape){
                return new Line2D.Float(0, 0, 21600, 21600);
            }
        };


    }
}
