/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */
package org.apache.poi.sl.draw;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.*;

import javax.imageio.ImageIO;

import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * For now this class renders only images supported by the javax.imageio.ImageIO
 * framework. Subclasses can override this class to support other formats, for
 * example, use Apache Batik to render WMF, PICT can be rendered using Apple QuickTime API for Java:
 *
 * <pre>
 * <code>
 * public class MyImageRendener extends ImageRendener {
 *     InputStream data;
 *
 *     public boolean drawImage(Graphics2D graphics,Rectangle2D anchor,Insets clip) {
 *         // draw image
 *       DataInputStream is = new DataInputStream(data);
 *       org.apache.batik.transcoder.wmf.tosvg.WMFRecordStore wmfStore =
 *               new org.apache.batik.transcoder.wmf.tosvg.WMFRecordStore();
 *       try {
 *           wmfStore.read(is);
 *       } catch (IOException e){
 *           return;
 *       }
 *
 *       float scale = (float)anchor.width/wmfStore.getWidthPixels();
 *
 *       org.apache.batik.transcoder.wmf.tosvg.WMFPainter painter =
 *               new org.apache.batik.transcoder.wmf.tosvg.WMFPainter(wmfStore, 0, 0, scale);
 *       graphics.translate(anchor.x, anchor.y);
 *       painter.paint(graphics);
 *     }
 *
 *     public void loadImage(InputStream data, String contentType) throws IOException {
 *         if ("image/wmf".equals(contentType)) {
 *             this.data = data;
 *             // use Apache Batik to handle WMF
 *         } else {
 *             super.loadImage(data,contentType);
 *         }
 *     }
 * }
 * </code>
 * </pre>
 *
 * and then pass this class to your instance of java.awt.Graphics2D:
 *
 * <pre>
 * <code>
 * graphics.setRenderingHint(Drawable.IMAGE_RENDERER, new MyImageRendener());
 * </code>
 * </pre>
 */
public class ImageRenderer {
    private final static POILogger LOG = POILogFactory.getLogger(ImageRenderer.class);
    
    protected BufferedImage img;

    /**
     * Load and buffer the image
     *
     * @param data the raw image stream
     * @param contentType the content type
     */
    public void loadImage(InputStream data, String contentType) throws IOException {
        img = convertBufferedImage(ImageIO.read(data), contentType);
    }

    /**
     * Load and buffer the image
     *
     * @param data the raw image stream
     * @param contentType the content type
     */
    public void loadImage(byte data[], String contentType) throws IOException {
        img = convertBufferedImage(ImageIO.read(new ByteArrayInputStream(data)), contentType);
    }

    /**
     * Add alpha channel to buffered image 
     */
    private static BufferedImage convertBufferedImage(BufferedImage img, String contentType) {
        if (img == null) {
            LOG.log(POILogger.WARN, "Content-type: "+contentType+" is not support. Image ignored.");
            return null;
        }
        
        BufferedImage bi = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics g = bi.getGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        return bi;
    }
    
    
    /**
     * @return the buffered image
     */
    public BufferedImage getImage() {
        return img;
    }

    /**
     * @return the dimension of the buffered image
     */
    public Dimension getDimension() {
        return (img == null)
            ? new Dimension(0,0)
            : new Dimension(img.getWidth(),img.getHeight());
    }

    /**
     * @param alpha the alpha [0..1] to be added to the image (possibly already containing an alpha channel)
     */
    public void setAlpha(double alpha) {
        if (img == null) return;

        Dimension dim = getDimension();
        BufferedImage newImg = new BufferedImage((int)dim.getWidth(), (int)dim.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = newImg.createGraphics();
        RescaleOp op = new RescaleOp(new float[]{1.0f, 1.0f, 1.0f, (float)alpha}, new float[]{0,0,0,0}, null);
        g.drawImage(img, op, 0, 0);
        g.dispose();
        
        img = newImg;
    }


    /**
     * Render picture data into the supplied graphics
     *
     * @return true if the picture data was successfully rendered
     */
    public boolean drawImage(
        Graphics2D graphics,
        Rectangle2D anchor) {
        return drawImage(graphics, anchor, null);
    }

    /**
     * Render picture data into the supplied graphics
     *
     * @return true if the picture data was successfully rendered
     */
    public boolean drawImage(
        Graphics2D graphics,
        Rectangle2D anchor,
        Insets clip) {
        if (img == null) return false;

        boolean isClipped = true;
        if (clip == null) {
            isClipped = false;
            clip = new Insets(0,0,0,0);
        }

        int iw = img.getWidth();
        int ih = img.getHeight();

        
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

        return true;
    }
}
