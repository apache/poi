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
            new HemfRecordIterator(stream).forEachRemaining(records::add);
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
     * Return the image size in points
     *
     * @return the image size in points
     */
    public Dimension2D getSize() {
        HemfHeader header = (HemfHeader)getRecords().get(0);
        final double coeff = (double) Units.EMU_PER_CENTIMETER / Units.EMU_PER_POINT / 10.;
        Rectangle2D dim = header.getFrameRectangle();
        double width = dim.getWidth(), height = dim.getHeight();
        if (dim.isEmpty() || Math.rint(width*coeff) == 0 || Math.rint(height*coeff) == 0) {
            for (HemfRecord r : getRecords()) {
                if (r instanceof HemfWindowing.EmfSetWindowExtEx) {
                    Dimension2D d = ((HemfWindowing.EmfSetWindowExtEx)r).getSize();
                    width = d.getWidth();
                    height = d.getHeight();
                    // keep searching - sometimes there's another record
                }
            }
        }

        if (Math.rint(width*coeff) == 0 || Math.rint(height*coeff) == 0) {
            width = 100;
            height = 100;
        }

        return new Dimension2DDouble(width*coeff, height*coeff);
    }

    public void draw(Graphics2D ctx, Rectangle2D graphicsBounds) {
        HemfHeader header = (HemfHeader)getRecords().get(0);

        AffineTransform at = ctx.getTransform();
        try {
            Rectangle2D emfBounds = header.getBoundsRectangle();
            ctx.translate(graphicsBounds.getCenterX()-emfBounds.getCenterX(), graphicsBounds.getCenterY()-emfBounds.getCenterY());

            // scale output bounds to image bounds
            ctx.translate(emfBounds.getCenterX(), emfBounds.getCenterY());
            ctx.scale(graphicsBounds.getWidth()/emfBounds.getWidth(), graphicsBounds.getHeight()/emfBounds.getHeight());
            ctx.translate(-emfBounds.getCenterX(), -emfBounds.getCenterY());

            int idx = 0;
            HemfGraphics g = new HemfGraphics(ctx, emfBounds);
            for (HemfRecord r : getRecords()) {
                g.draw(r);
                idx++;
            }
        } finally {
            ctx.setTransform(at);
        }
    }

}
