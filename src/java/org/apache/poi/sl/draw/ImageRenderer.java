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

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

/**
 * Classes can implement this interfaces to support other formats, for
 * example, use Apache Batik to render WMF, PICT can be rendered using Apple QuickTime API for Java:
 *
 * <pre>
 * <code>
 * public class MyImageRendener implements ImageRendener {
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
public interface ImageRenderer {
    /**
     * Determines if this image renderer implementation supports the given contentType
     * @param contentType the image content type
     * @return if the content type is supported
     */
    boolean canRender(String contentType);

    /**
     * Load and buffer the image
     *
     * @param data the raw image stream
     * @param contentType the content type
     */
    void loadImage(InputStream data, String contentType) throws IOException;

    /**
     * Load and buffer the image
     *
     * @param data the raw image bytes
     * @param contentType the content type
     */
    void loadImage(byte[] data, String contentType) throws IOException;

    /**
     * @return the dimension of the buffered image
     */
    Dimension getDimension();

    /**
     * @param alpha the alpha [0..1] to be added to the image (possibly already containing an alpha channel)
     */
    void setAlpha(double alpha);

    /**
     * @return the image as buffered image
     */
    BufferedImage getImage();

    /**
     * @param dim the dimension in pixels of the returned image
     * @return the image as buffered image
     * 
     * @since POI 3.15-beta2
     */
    BufferedImage getImage(Dimension dim);
    
    /**
     * Render picture data into the supplied graphics
     *
     * @return true if the picture data was successfully rendered
     */
    boolean drawImage(Graphics2D graphics, Rectangle2D anchor);

    /**
     * Render picture data into the supplied graphics
     *
     * @return true if the picture data was successfully rendered
     */
    boolean drawImage(Graphics2D graphics, Rectangle2D anchor, Insets clip);
}