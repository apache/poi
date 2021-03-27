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

package org.apache.poi.xslf.util;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

import javax.imageio.ImageIO;

import org.apache.poi.sl.draw.Drawable;
import org.apache.poi.util.Internal;

@Internal
public class BitmapFormat implements OutputFormat {
    private final String format;
    private BufferedImage img;
    private Graphics2D graphics;

    public BitmapFormat(String format) {
        this.format = format;
    }

    @Override
    public Graphics2D addSlide(double width, double height) {
        int type;
        switch (format) {
            case "png":
            case "gif":
                type = BufferedImage.TYPE_INT_ARGB;
                break;
            default:
                type = BufferedImage.TYPE_INT_RGB;
                break;
        }
        img = new BufferedImage((int)width, (int)height, type);
        graphics = img.createGraphics();
        graphics.setRenderingHint(Drawable.BUFFERED_IMAGE, new WeakReference<>(img));
        return graphics;
    }

    @Override
    public void writeSlide(MFProxy proxy, File outFile) throws IOException {
        if (!"null".equals(format)) {
            ImageIO.write(img, format, outFile);
        }
    }

    @Override
    public void close() throws IOException {
        if (graphics != null) {
            graphics.dispose();
            img.flush();
        }
    }
}
