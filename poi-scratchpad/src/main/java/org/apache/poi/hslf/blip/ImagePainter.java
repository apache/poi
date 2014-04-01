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

import org.apache.poi.hslf.model.Picture;
import org.apache.poi.hslf.usermodel.PictureData;

import java.awt.*;

/**
 * A common interface for objects that can render ppt picture data.
 * <p>
 * Subclasses can redefine it and use third-party libraries for actual rendering,
 * for example, Bitmaps can be rendered using javax.imageio.* , WMF can be rendered using Apache Batik,
 * PICT can be rendered using Apple QuickTime API for Java, etc.
 * </p>
 *
 * A typical usage is as follows:
 * <code>
 * public WMFPaiter implements ImagePainter{
 *   public void paint(Graphics2D graphics, PictureData pict, Picture parent){
 *       DataInputStream is = new DataInputStream(new ByteArrayInputStream(pict.getData()));
 *       org.apache.batik.transcoder.wmf.tosvg.WMFRecordStore wmfStore =
 *               new org.apache.batik.transcoder.wmf.tosvg.WMFRecordStore();
 *       try {
 *           wmfStore.read(is);
 *       } catch (IOException e){
 *           return;
 *       }
 *
 *       Rectangle anchor = parent.getAnchor();
 *       float scale = (float)anchor.width/wmfStore.getWidthPixels();
 *
 *       org.apache.batik.transcoder.wmf.tosvg.WMFPainter painter =
 *               new org.apache.batik.transcoder.wmf.tosvg.WMFPainter(wmfStore, 0, 0, scale);
 *       graphics.translate(anchor.x, anchor.y);
 *       painter.paint(graphics);
 *   }
 * }
 * PictureData.setImagePainter(Picture.WMF, new WMFPaiter());
 * ...
 * </code>
 * Subsequent calls of Slide.draw(Graphics gr) will use WMFPaiter for WMF images.
 *
 * @author  Yegor Kozlov.
 */
public interface ImagePainter {

    /**
     * Paints the specified picture data
     *
     * @param graphics  the graphics to paintb into
     * @param pict      the data to paint
     * @param parent    the shapes that owns the picture data
     */
    public void paint(Graphics2D graphics, PictureData pict, Picture parent);
}
