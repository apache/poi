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

package org.apache.poi.hslf.blip;

import org.apache.poi.hslf.usermodel.PictureData;
import org.apache.poi.hslf.model.Picture;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.POILogFactory;




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
import javax.imageio.ImageIO;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

/**
 * Creates BufferedImage using javax.imageio.ImageIO and draws it in the specified graphics.
 *
 * @author  Yegor Kozlov.
 */
public final class BitmapPainter implements ImagePainter {
    protected POILogger logger = POILogFactory.getLogger(this.getClass());

    public void paint(Graphics2D graphics, PictureData pict, Picture parent) {
        BufferedImage img;
        try {
            img = ImageIO.read(new ByteArrayInputStream(pict.getData()));
        } catch (Exception e) {
            logger.log(POILogger.WARN, "ImageIO failed to create image. image.type: " + pict.getType());
            return;
        }

        boolean isClipped = true;
        Insets clip = parent.getBlipClip();
        if (clip == null) {
            isClipped = false;
            clip = new Insets(0,0,0,0);
        }        
        
        int iw = img.getWidth();
        int ih = img.getHeight();

        Rectangle anchor = parent.getLogicalAnchor2D().getBounds();

        double cw = (100000-clip.left-clip.right) / 100000.0;
        double ch = (100000-clip.top-clip.bottom) / 100000.0;
        double sx = anchor.getWidth()/(iw*cw);
        double sy = anchor.getHeight()/(ih*ch);
        double tx = anchor.getX()-(iw*sx*clip.left/100000.0);
        double ty = anchor.getY()-(ih*sy*clip.top/100000.0);
        AffineTransform at = new AffineTransform(sx, 0, 0, sy, tx, ty) ;

        Shape clipOld = graphics.getClip();
        if (isClipped) graphics.clip(anchor.getBounds2D());
        graphics.drawRenderedImage(img, at);
        graphics.setClip(clipOld);
    }

}
