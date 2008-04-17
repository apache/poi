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


import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * Paint a shape into java.awt.Graphics2D
 * 
 * @author Yegor Kozlov
 */
public class ShapePainter {

    public static void paint(SimpleShape shape, Graphics2D graphics){
        Rectangle2D anchor = shape.getAnchor2D();
        java.awt.Shape outline = shape.getOutline();

        //flip vertical
        if(shape.getFlipVertical()){
            graphics.translate(anchor.getX(), anchor.getY() + anchor.getHeight());
            graphics.scale(1, -1);
            graphics.translate(-anchor.getX(), -anchor.getY());
        }
        //flip horizontal
        if(shape.getFlipHorizontal()){
            graphics.translate(anchor.getX() + anchor.getWidth(), anchor.getY());
            graphics.scale(-1, 1);
            graphics.translate(-anchor.getX() , -anchor.getY());
        }

        //rotate transform
        double angle = shape.getRotation();

        if(angle != 0){
            double centerX = anchor.getX() + anchor.getWidth()/2;
            double centerY = anchor.getY() + anchor.getHeight()/2;

            graphics.translate(centerX, centerY);
            graphics.rotate(Math.toRadians(angle));
            graphics.translate(-centerX, -centerY);
        }

        //fill
        Color fillColor = shape.getFill().getForegroundColor();
        if (fillColor != null) {
            graphics.setPaint(fillColor);
            graphics.fill(outline);
        }

        //border
        Color lineColor = shape.getLineColor();
        if (lineColor != null){
            graphics.setPaint(lineColor);
            float width = (float)shape.getLineWidth();
            int dashing = shape.getLineDashing();
            //TODO: implement more dashing styles
            float[] dashptrn = null;
            switch(dashing){
                case Line.PEN_PS_DASH:
                    dashptrn = new float[]{2, 2};
                    break;
            }

            Stroke stroke = new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dashptrn, 0.0f);
            graphics.setStroke(stroke);
            graphics.draw(outline);
        }
    }
}
