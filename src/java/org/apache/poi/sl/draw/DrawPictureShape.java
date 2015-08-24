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
import java.awt.Insets;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import org.apache.poi.sl.usermodel.PictureData;
import org.apache.poi.sl.usermodel.PictureShape;


public class DrawPictureShape extends DrawSimpleShape {
    public DrawPictureShape(PictureShape<?,?> shape) {
        super(shape);
    }
    
    @Override
    public void drawContent(Graphics2D graphics) {
        PictureData data = getShape().getPictureData();
        if(data == null) return;

        ImageRenderer renderer = (ImageRenderer)graphics.getRenderingHint(Drawable.IMAGE_RENDERER);
        if (renderer == null) renderer = new ImageRenderer();
        
        Rectangle2D anchor = getAnchor(graphics, getShape());

        Insets insets = getShape().getClipping();

        try {
            renderer.loadImage(data.getData(), data.getContentType());
            renderer.drawImage(graphics, anchor, insets);
        } catch (IOException e) {
            // TODO: draw specific runtime exception?
            throw new RuntimeException(e);
        }
    }    

    @Override
    protected PictureShape<?,?> getShape() {
        return (PictureShape<?,?>)shape;
    }
}
