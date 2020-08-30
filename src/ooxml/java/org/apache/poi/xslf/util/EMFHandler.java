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
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collections;

import org.apache.poi.common.usermodel.GenericRecord;
import org.apache.poi.sl.draw.BitmapImageRenderer;
import org.apache.poi.sl.draw.DrawPictureShape;
import org.apache.poi.sl.draw.EmbeddedExtractor;
import org.apache.poi.sl.draw.EmbeddedExtractor.EmbeddedPart;
import org.apache.poi.sl.draw.ImageRenderer;
import org.apache.poi.sl.usermodel.PictureData;
import org.apache.poi.util.Internal;

@Internal
class EMFHandler extends MFProxy {
    private ImageRenderer imgr = null;
    private InputStream is;

    @Override
    public void parse(File file) throws IOException {
        // stream needs to be kept open until the instance is closed
        is = file.toURI().toURL().openStream();
        parse(is);
    }

    @Override
    public void parse(InputStream is) throws IOException {
        imgr = DrawPictureShape.getImageRenderer(null, getContentType());
        if (imgr instanceof BitmapImageRenderer) {
            throw new PPTX2PNG.NoScratchpadException();
        }

        // stream needs to be kept open
        imgr.loadImage(is, getContentType());

        if (ignoreParse) {
            try {
                imgr.getDimension();
            } catch (Exception e) {
//                if (!quite) {
//                    e.printStackTrace(System.err);
//                }
            }
        }
    }

    protected String getContentType() {
        return PictureData.PictureType.EMF.contentType;
    }

    @Override
    public Dimension2D getSize() {
        return imgr.getDimension();
    }

    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public void draw(Graphics2D ctx) {
        Dimension2D dim = getSize();
        imgr.drawImage(ctx, new Rectangle2D.Double(0, 0, dim.getWidth(), dim.getHeight()));
    }

    @Override
    public void close() throws IOException {
        if (is != null) {
            try {
                is.close();
            } finally {
                is = null;
            }
        }
    }

    @Override
    public GenericRecord getRoot() {
        return imgr.getGenericRecord();
    }

    @Override
    public Iterable<EmbeddedPart> getEmbeddings(int slideNo) {
        return (imgr instanceof EmbeddedExtractor)
            ? ((EmbeddedExtractor) imgr).getEmbeddings()
            : Collections.emptyList();
    }

    @Override
    void setDefaultCharset(Charset charset) {
        imgr.setDefaultCharset(charset);
    }
}
