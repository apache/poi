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

package org.apache.poi.hemf.usermodel;


import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

import org.apache.poi.hemf.draw.HemfGraphics;
import org.apache.poi.hemf.record.emf.HemfHeader;
import org.apache.poi.hemf.record.emf.HemfRecord;
import org.apache.poi.hemf.record.emf.HemfRecordIterator;
import org.apache.poi.hemf.record.emf.HemfWindowing;
import org.apache.poi.hwmf.usermodel.HwmfEmbedded;
import org.apache.poi.util.Dimension2DDouble;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndianInputStream;
import org.apache.poi.util.Units;

/**
 * Read-only EMF extractor.  Lots remain
 */
@Internal
public class HemfPicture implements Iterable<HemfRecord> {
    private final LittleEndianInputStream stream;
    private final List<HemfRecord> records = new ArrayList<>();
    private boolean isParsed = false;

    public HemfPicture(InputStream is) throws IOException {
        this(new LittleEndianInputStream(is));
    }

    public HemfPicture(LittleEndianInputStream is) throws IOException {
        stream = is;
    }

    public HemfHeader getHeader() {
        return (HemfHeader)getRecords().get(0);
    }

    public List<HemfRecord> getRecords() {
        if (!isParsed) {
            // in case the (first) parsing throws an exception, we can provide the
            // records up to that point
            isParsed = true;
            HemfHeader[] header = new HemfHeader[1];
            new HemfRecordIterator(stream).forEachRemaining(r -> {
                if (r instanceof HemfHeader) {
                    header[0] = (HemfHeader) r;
                }
                r.setHeader(header[0]);
                records.add(r);
            });
        }
        return records;
    }

    @Override
    public Iterator<HemfRecord> iterator() {
        return getRecords().iterator();
    }

    @Override
    public Spliterator<HemfRecord> spliterator() {
        return getRecords().spliterator();
    }

    @Override
    public void forEach(Consumer<? super HemfRecord> action) {
        getRecords().forEach(action);
    }

    /**
     * Returns the bounding box in device-independent units. Usually this is taken from the placeable header.
     *
     * @return the bounding box
     */
    public Rectangle2D getBounds() {
        HemfHeader header = (HemfHeader)getRecords().get(0);
        Rectangle2D dim = header.getFrameRectangle();
        double x = dim.getX(), y = dim.getY();
        double width = dim.getWidth(), height = dim.getHeight();
        if (dim.isEmpty() || Math.rint(width) == 0 || Math.rint(height) == 0) {
            for (HemfRecord r : getRecords()) {
                if (r instanceof HemfWindowing.EmfSetWindowExtEx) {
                    HemfWindowing.EmfSetWindowExtEx extEx = (HemfWindowing.EmfSetWindowExtEx)r;
                    Dimension2D d = extEx.getSize();
                    width = d.getWidth();
                    height = d.getHeight();
                    // keep searching - sometimes there's another record
                }
                if (r instanceof HemfWindowing.EmfSetWindowOrgEx) {
                    HemfWindowing.EmfSetWindowOrgEx orgEx = (HemfWindowing.EmfSetWindowOrgEx)r;
                    x = orgEx.getX();
                    y = orgEx.getY();
                }
            }
        }

        return new Rectangle2D.Double(x, y, width, height);
    }

    /**
     * Return the image size in points
     *
     * @return the image size in points
     */
    public Dimension2D getSize() {
        final Rectangle2D bounds = getBounds();

        if (bounds.isEmpty()) {
            return new Dimension2DDouble(100,100);
        }

        final double coeff = (double) Units.EMU_PER_CENTIMETER / Units.EMU_PER_POINT / 10.;
        double width = Math.abs(bounds.getWidth()*coeff);
        double height = Math.abs(bounds.getHeight()*coeff);

        return new Dimension2DDouble(width, height);
    }

    private static double minX(Rectangle2D bounds) {
        return Math.min(bounds.getMinX(), bounds.getMaxX());
    }

    private static double minY(Rectangle2D bounds) {
        return Math.min(bounds.getMinY(), bounds.getMaxY());
    }

    public void draw(Graphics2D ctx, Rectangle2D graphicsBounds) {
        final HemfHeader header = (HemfHeader)getRecords().get(0);

        final Shape clip = ctx.getClip();
        final AffineTransform at = ctx.getTransform();
        try {
            Rectangle2D emfBounds = header.getBoundsRectangle();

            // scale output bounds to image bounds
            ctx.translate(minX(graphicsBounds), minY(graphicsBounds));
            ctx.scale(graphicsBounds.getWidth()/emfBounds.getWidth(), graphicsBounds.getHeight()/emfBounds.getHeight());
            ctx.translate(-minX(emfBounds), -minY(emfBounds));

            int idx = 0;
            HemfGraphics g = new HemfGraphics(ctx, emfBounds);
            for (HemfRecord r : getRecords()) {
                try {
                    g.draw(r);
                } catch (RuntimeException ignored) {

                }
                idx++;
            }
        } finally {
            ctx.setTransform(at);
            ctx.setClip(clip);
        }
    }

    public Iterable<HwmfEmbedded> getEmbeddings() {
        return () -> new HemfEmbeddedIterator(HemfPicture.this);
    }
}
