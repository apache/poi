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


import static java.lang.Math.abs;
import static org.apache.poi.hemf.draw.HemfGraphics.EmfRenderState.EMFPLUS_ONLY;
import static org.apache.poi.hemf.draw.HemfGraphics.EmfRenderState.EMF_ONLY;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.poi.common.usermodel.GenericRecord;
import org.apache.poi.hemf.draw.HemfGraphics;
import org.apache.poi.hemf.record.emf.HemfComment;
import org.apache.poi.hemf.record.emf.HemfHeader;
import org.apache.poi.hemf.record.emf.HemfRecord;
import org.apache.poi.hemf.record.emf.HemfRecordIterator;
import org.apache.poi.hwmf.usermodel.HwmfCharsetAware;
import org.apache.poi.hwmf.usermodel.HwmfEmbedded;
import org.apache.poi.sl.draw.Drawable;
import org.apache.poi.util.Dimension2DDouble;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndianInputStream;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.util.Units;

/**
 * Read-only EMF extractor.  Lots remain
 */
@Internal
public class HemfPicture implements Iterable<HemfRecord>, GenericRecord {
    private final LittleEndianInputStream stream;
    private final List<HemfRecord> records = new ArrayList<>();
    private boolean isParsed = false;
    private Charset defaultCharset = LocaleUtil.CHARSET_1252;

    public HemfPicture(InputStream is) {
        this(new LittleEndianInputStream(is));
    }

    public HemfPicture(LittleEndianInputStream is) {
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
                if (r instanceof HwmfCharsetAware) {
                    ((HwmfCharsetAware)r).setCharsetProvider(this::getDefaultCharset);
                }
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
     * Returns the bounding box in device-independent units - usually this is in .01 millimeter units
     *
     * @return the bounding box in device-independent units
     */
    public Rectangle2D getBounds() {
        Rectangle2D dim = getHeader().getFrameRectangle();
        boolean isInvalid = ReluctantRectangle2D.isEmpty(dim);
        if (isInvalid) {
            Rectangle2D lastDim = new ReluctantRectangle2D();
            getInnerBounds(lastDim, new ReluctantRectangle2D());
            if (!lastDim.isEmpty()) {
                return lastDim;
            }
        }
        return dim;
    }

    public void getInnerBounds(Rectangle2D window, Rectangle2D viewport) {
        HemfGraphics.EmfRenderState[] renderState = { HemfGraphics.EmfRenderState.INITIAL };
        for (HemfRecord r : getRecords()) {
            if (
                (renderState[0] == EMF_ONLY && r instanceof HemfComment.EmfComment) ||
                (renderState[0] == EMFPLUS_ONLY && !(r instanceof HemfComment.EmfComment))
            ) {
                continue;
            }

            try {
                r.calcBounds(window, viewport, renderState);
            } catch (RuntimeException ignored) {
            }

            if (!window.isEmpty() && !viewport.isEmpty()) {
                break;
            }
        }
    }

    /**
     * Return the image bounds in points
     *
     * @return the image bounds in points
     */
    public Rectangle2D getBoundsInPoints() {
        return Units.pixelToPoints(getHeader().getBoundsRectangle());
    }

    /**
     * Return the image size in points
     *
     * @return the image size in points
     */
    public Dimension2D getSize() {
        final Rectangle2D b = getBoundsInPoints();
        return new Dimension2DDouble(abs(b.getWidth()), abs(b.getHeight()));
    }
    public void draw(Graphics2D ctx, Rectangle2D graphicsBounds) {
        final Shape clip = ctx.getClip();
        final AffineTransform at = ctx.getTransform();
        try {
            Rectangle2D emfBounds = getHeader().getBoundsRectangle();
            Rectangle2D winBounds = new ReluctantRectangle2D();
            Rectangle2D viewBounds = new ReluctantRectangle2D();
            getInnerBounds(winBounds, viewBounds);

            Boolean forceHeader = (Boolean)ctx.getRenderingHint(Drawable.EMF_FORCE_HEADER_BOUNDS);
            if (forceHeader == null) {
                forceHeader = false;
            }
            // this is a compromise ... sometimes winBounds are totally off :(
            // but mostly they fit better than the header bounds
            Rectangle2D b =
                !viewBounds.isEmpty() && !forceHeader
                ? viewBounds
                : !winBounds.isEmpty() && !forceHeader
                ? winBounds
                : emfBounds;

            ctx.translate(graphicsBounds.getCenterX(), graphicsBounds.getCenterY());
            ctx.scale(
                graphicsBounds.getWidth()/b.getWidth(),
                graphicsBounds.getHeight()/b.getHeight()
            );
            ctx.translate(-b.getCenterX(),-b.getCenterY());

            HemfGraphics g = new HemfGraphics(ctx, b);

            int idx=0;
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

    @Override
    public List<? extends GenericRecord> getGenericChildren() {
        return getRecords();
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return null;
    }

    public void setDefaultCharset(Charset defaultCharset) {
        this.defaultCharset = defaultCharset;
    }

    public Charset getDefaultCharset() {
        return defaultCharset;
    }


    private static class ReluctantRectangle2D extends Rectangle2D.Double {
        private boolean offsetSet = false;
        private boolean rangeSet = false;

        public ReluctantRectangle2D() {
            super(-1,-1,0,0);
        }

        @Override
        public void setRect(double x, double y, double w, double h) {
            if (offsetSet && rangeSet) {
                return;
            }
            super.setRect(
                offsetSet ? this.x : x,
                offsetSet ? this.y : y,
                rangeSet ? this.width : w,
                rangeSet ? this.height : h);
            offsetSet |= (x != -1 || y != -1);
            rangeSet |= (w != 0 || h != 0);
        }

        @Override
        public boolean isEmpty() {
            return isEmpty(this);
        }

        public static boolean isEmpty(Rectangle2D r) {
            double w = Math.rint(r.getWidth());
            double h = Math.rint(r.getHeight());
            return
                (w <= 0.0) || (h <= 0.0) ||
                (r.getX() == -1 && r.getY() == -1) ||
                // invalid emf bound have sometimes 1,1 as dimension
                (w == 1 && h == 1);
        }
    }
}
