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

package org.apache.poi.hwmf.usermodel;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.poi.hwmf.draw.HwmfDrawProperties;
import org.apache.poi.hwmf.draw.HwmfGraphics;
import org.apache.poi.hwmf.record.HwmfHeader;
import org.apache.poi.hwmf.record.HwmfPlaceableHeader;
import org.apache.poi.hwmf.record.HwmfRecord;
import org.apache.poi.hwmf.record.HwmfRecordType;
import org.apache.poi.hwmf.record.HwmfWindowing.WmfSetWindowExt;
import org.apache.poi.hwmf.record.HwmfWindowing.WmfSetWindowOrg;
import org.apache.poi.util.Dimension2DDouble;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndianInputStream;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.RecordFormatException;
import org.apache.poi.util.Units;

public class HwmfPicture {
    /** Max. record length - processing longer records will throw an exception */
    public static final int MAX_RECORD_LENGTH = 50_000_000;

    private static final POILogger logger = POILogFactory.getLogger(HwmfPicture.class);
    
    final List<HwmfRecord> records = new ArrayList<>();
    final HwmfPlaceableHeader placeableHeader;
    final HwmfHeader header;
    
    public HwmfPicture(InputStream inputStream) throws IOException {

        try (LittleEndianInputStream leis = new LittleEndianInputStream(inputStream)) {
            placeableHeader = HwmfPlaceableHeader.readHeader(leis);
            header = new HwmfHeader(leis);

            for (;;) {
                long recordSize;
                int recordFunction;
                try {
                    // recordSize in DWORDs
                    long recordSizeLong = leis.readUInt()*2;
                    if (recordSizeLong > Integer.MAX_VALUE) {
                        throw new RecordFormatException("record size can't be > "+Integer.MAX_VALUE);
                    } else if (recordSizeLong < 0L) {
                        throw new RecordFormatException("record size can't be < 0");
                    }
                    recordSize = (int)recordSizeLong;
                    recordFunction = leis.readShort();
                } catch (Exception e) {
                    logger.log(POILogger.ERROR, "unexpected eof - wmf file was truncated");
                    break;
                }
                // 4 bytes (recordSize) + 2 bytes (recordFunction)
                int consumedSize = 6;
                HwmfRecordType wrt = HwmfRecordType.getById(recordFunction);
                if (wrt == null) {
                    throw new IOException("unexpected record type: "+recordFunction);
                }
                if (wrt == HwmfRecordType.eof) {
                    break;
                }
                if (wrt.constructor == null) {
                    throw new IOException("unsupported record type: "+recordFunction);
                }

                final HwmfRecord wr = wrt.constructor.get();
                records.add(wr);

                consumedSize += wr.init(leis, recordSize, recordFunction);
                int remainingSize = (int)(recordSize - consumedSize);
                if (remainingSize < 0) {
                    throw new RecordFormatException("read too many bytes. record size: "+recordSize + "; comsumed size: "+consumedSize);
                } else if(remainingSize > 0) {
                    long skipped = IOUtils.skipFully(leis, remainingSize);
                    if (skipped != (long)remainingSize) {
                        throw new RecordFormatException("Tried to skip "+remainingSize + " but skipped: "+skipped);
                    }
                }
            }
        }
    }

    public List<HwmfRecord> getRecords() {
        return Collections.unmodifiableList(records);
    }

    public void draw(Graphics2D ctx) {
        Dimension2D dim = getSize();
        int width = Units.pointsToPixel(dim.getWidth());
        // keep aspect ratio for height
        int height = Units.pointsToPixel(dim.getHeight());
        Rectangle2D bounds = new Rectangle2D.Double(0,0,width,height);
        draw(ctx, bounds);
    }
    
    public void draw(Graphics2D ctx, Rectangle2D graphicsBounds) {
        final Shape clip = ctx.getClip();
        final AffineTransform at = ctx.getTransform();
        try {
            Rectangle2D wmfBounds = getBounds();
            Rectangle2D innerBounds = getInnnerBounds();
            if (innerBounds == null) {
                innerBounds = wmfBounds;
            }

            // scale output bounds to image bounds
            ctx.translate(graphicsBounds.getX(), graphicsBounds.getY());
            ctx.scale(graphicsBounds.getWidth()/wmfBounds.getWidth(), graphicsBounds.getHeight()/wmfBounds.getHeight());

            ctx.translate(-wmfBounds.getX(), -wmfBounds.getY());
            ctx.translate(innerBounds.getCenterX(), innerBounds.getCenterY());
            ctx.scale(wmfBounds.getWidth()/innerBounds.getWidth(), wmfBounds.getHeight()/innerBounds.getHeight());
            ctx.translate(-wmfBounds.getCenterX(), -wmfBounds.getCenterY());

            HwmfGraphics g = new HwmfGraphics(ctx, innerBounds);
            HwmfDrawProperties prop = g.getProperties();
            prop.setViewportOrg(innerBounds.getX(), innerBounds.getY());
            prop.setViewportExt(innerBounds.getWidth(), innerBounds.getHeight());

            int idx = 0;
            for (HwmfRecord r : records) {
                prop = g.getProperties();
                Shape propClip = prop.getClip();
                Shape ctxClip = ctx.getClip();
                if (!Objects.equals(propClip, ctxClip)) {
                    int a = 5;
                }
                r.draw(g);
                idx++;
            }
        } finally {
            ctx.setTransform(at);
            ctx.setClip(clip);
        }
    }

    /**
     * Returns the bounding box in device-independent units. Usually this is taken from the placeable header.
     *
     * @return the bounding box
     *
     * @throws RuntimeException if neither WmfSetWindowOrg/Ext nor the placeableHeader are set
     */
    public Rectangle2D getBounds() {
        if (placeableHeader != null) {
            return placeableHeader.getBounds();
        }
        Rectangle2D inner = getInnnerBounds();
        if (inner != null) {
            return inner;
        }
        throw new RuntimeException("invalid wmf file - window records are incomplete.");
    }

    /**
     * Returns the bounding box in device-independent units taken from the WmfSetWindowOrg/Ext records
     *
     * @return the bounding box or null, if the WmfSetWindowOrg/Ext records aren't set
     */
    public Rectangle2D getInnnerBounds() {
        WmfSetWindowOrg wOrg = null;
        WmfSetWindowExt wExt = null;
        for (HwmfRecord r : getRecords()) {
            if (r instanceof WmfSetWindowOrg) {
                wOrg = (WmfSetWindowOrg)r;
            } else if (r instanceof WmfSetWindowExt) {
                wExt = (WmfSetWindowExt)r;
            }
            if (wOrg != null && wExt != null) {
                return new Rectangle2D.Double(wOrg.getX(), wOrg.getY(), wExt.getSize().getWidth(), wExt.getSize().getHeight());
        }
        }
        return null;
    }


    public HwmfPlaceableHeader getPlaceableHeader() {
        return placeableHeader;
    }

    public HwmfHeader getHeader() {
        return header;
    }
    
    /**
     * Return the image size in points
     *
     * @return the image size in points
     */
    public Dimension2D getSize() {
        double inch = (placeableHeader == null) ? 1440 : placeableHeader.getUnitsPerInch();
        Rectangle2D bounds = getBounds();
        
        //coefficient to translate from WMF dpi to 72dpi
        double coeff = Units.POINT_DPI/inch;
        return new Dimension2DDouble(bounds.getWidth()*coeff, bounds.getHeight()*coeff);
    }

    public Iterable<HwmfEmbedded> getEmbeddings() {
        return () -> new HwmfEmbeddedIterator(HwmfPicture.this);
    }
}
