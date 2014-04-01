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

import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.hslf.usermodel.PictureData;
import org.apache.poi.hslf.blip.Bitmap;
import org.apache.poi.util.POILogger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

/**
 * Background shape
 *
 * @author Yegor Kozlov
 */
public final class Background extends Shape {

    protected Background(EscherContainerRecord escherRecord, Shape parent) {
        super(escherRecord, parent);
    }

    protected EscherContainerRecord createSpContainer(boolean isChild) {
        return null;
    }

    public void draw(Graphics2D graphics) {
        Fill f = getFill();
        Dimension pg = getSheet().getSlideShow().getPageSize();
        Rectangle anchor = new Rectangle(0, 0, pg.width, pg.height);
        switch (f.getFillType()) {
            case Fill.FILL_SOLID:
                Color color = f.getForegroundColor();
                graphics.setPaint(color);
                graphics.fill(anchor);
                break;
            case Fill.FILL_PICTURE:
                PictureData data = f.getPictureData();
                if (data instanceof Bitmap) {
                    BufferedImage img = null;
                    try {
                        img = ImageIO.read(new ByteArrayInputStream(data.getData()));
                    } catch (Exception e) {
                        logger.log(POILogger.WARN, "ImageIO failed to create image. image.type: " + data.getType());
                        return;
                    }
                    Image scaledImg = img.getScaledInstance(anchor.width, anchor.height, Image.SCALE_SMOOTH);
                    graphics.drawImage(scaledImg, anchor.x, anchor.y, null);

                }
                break;
            default:
                logger.log(POILogger.WARN, "unsuported fill type: " + f.getFillType());
                break;
        }
    }
}
