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

package org.apache.poi.sl.draw;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import org.apache.poi.sl.usermodel.*;


public class DrawGroupShape extends DrawShape {

    public DrawGroupShape(GroupShape<?,?> shape) {
        super(shape);
    }
    
    public void draw(Graphics2D graphics) {

        // the coordinate system of this group of shape
        Rectangle2D interior = getShape().getInteriorAnchor();
        // anchor of this group relative to the parent shape
        Rectangle2D exterior = getShape().getAnchor();

        AffineTransform tx = (AffineTransform)graphics.getRenderingHint(Drawable.GROUP_TRANSFORM);
        AffineTransform tx0 = new AffineTransform(tx);

        double scaleX = interior.getWidth() == 0. ? 1.0 : exterior.getWidth() / interior.getWidth();
        double scaleY = interior.getHeight() == 0. ? 1.0 : exterior.getHeight() / interior.getHeight();

        tx.translate(exterior.getX(), exterior.getY());
        tx.scale(scaleX, scaleY);
        tx.translate(-interior.getX(), -interior.getY());

        DrawFactory drawFact = DrawFactory.getInstance(graphics);
        AffineTransform at2 = graphics.getTransform();
        
        for (Shape<?,?> child : getShape()) {
            // remember the initial transform and restore it after we are done with the drawing
            AffineTransform at = graphics.getTransform();
            graphics.setRenderingHint(Drawable.GSAVE, true);

            Drawable draw = drawFact.getDrawable(child);
            draw.applyTransform(graphics);
            draw.draw(graphics);

            // restore the coordinate system
            graphics.setTransform(at);
            graphics.setRenderingHint(Drawable.GRESTORE, true);
        }

        graphics.setTransform(at2);
        graphics.setRenderingHint(Drawable.GROUP_TRANSFORM, tx0);
    }

    @Override
    protected GroupShape<?,?> getShape() {
        return (GroupShape<?,?>)shape;
    }
}
