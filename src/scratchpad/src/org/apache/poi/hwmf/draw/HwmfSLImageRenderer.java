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

package org.apache.poi.hwmf.draw;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.hwmf.usermodel.HwmfPicture;
import org.apache.poi.sl.draw.DrawPictureShape;
import org.apache.poi.sl.draw.ImageRenderer;
import org.apache.poi.sl.usermodel.PictureData;
import org.apache.poi.util.Units;

/**
 * Helper class which is instantiated by {@link DrawPictureShape}
 * via reflection
 */
public class HwmfSLImageRenderer implements ImageRenderer {
    HwmfPicture image;
    double alpha;
    
    @Override
    public void loadImage(InputStream data, String contentType) throws IOException {
        if (!PictureData.PictureType.WMF.contentType.equals(contentType)) {
            throw new IOException("Invalid picture type");
        }
        image = new HwmfPicture(data);
    }

    @Override
    public void loadImage(byte[] data, String contentType) throws IOException {
        if (!PictureData.PictureType.WMF.contentType.equals(contentType)) {
            throw new IOException("Invalid picture type");
        }
        image = new HwmfPicture(new ByteArrayInputStream(data));
    }

    @Override
    public Dimension getDimension() {
        int width = 0, height = 0;
        if (image != null) {
            Dimension dim = image.getSize();
            width = Units.pointsToPixel(dim.getWidth());
            // keep aspect ratio for height
            height = Units.pointsToPixel(dim.getHeight());
        }
        return new Dimension(width, height);
    }

    @Override
    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    @Override
    public BufferedImage getImage() {
        return getImage(getDimension());
    }

    @Override
    public BufferedImage getImage(Dimension dim) {
        if (image == null) {
            return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB); 
        }
        
        BufferedImage bufImg = new BufferedImage((int)dim.getWidth(), (int)dim.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bufImg.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        image.draw(g, new Rectangle2D.Double(0,0,dim.getWidth(),dim.getHeight()));
        g.dispose();
        
        if (alpha != 0) {
            BufferedImage newImg = new BufferedImage((int)dim.getWidth(), (int)dim.getHeight(), BufferedImage.TYPE_INT_ARGB);
            g = newImg.createGraphics();
            RescaleOp op = new RescaleOp(new float[]{1.0f, 1.0f, 1.0f, (float)alpha}, new float[]{0,0,0,0}, null);
            g.drawImage(bufImg, op, 0, 0);
            g.dispose();
            bufImg = newImg;
        }
        
        return bufImg;
    }
    
    @Override
    public boolean drawImage(Graphics2D graphics, Rectangle2D anchor) {
        return drawImage(graphics, anchor, null);
    }

    @Override
    public boolean drawImage(Graphics2D graphics, Rectangle2D anchor, Insets clip) {
        if (image == null) {
            return false;
        } else {
            image.draw(graphics, anchor);
            return true;
        }
    }
}
