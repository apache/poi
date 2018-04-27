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

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.poi.hwmf.draw.HwmfGraphics;
import org.apache.poi.hwmf.record.HwmfHeader;
import org.apache.poi.hwmf.record.HwmfPlaceableHeader;
import org.apache.poi.hwmf.record.HwmfRecord;
import org.apache.poi.hwmf.record.HwmfRecordType;
import org.apache.poi.hwmf.record.HwmfWindowing.WmfSetWindowExt;
import org.apache.poi.hwmf.record.HwmfWindowing.WmfSetWindowOrg;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndianInputStream;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.RecordFormatException;
import org.apache.poi.util.Units;

public class HwmfPicture {
    private static final POILogger logger = POILogFactory.getLogger(HwmfPicture.class);
    
    final List<HwmfRecord> records = new ArrayList<>();
    final HwmfPlaceableHeader placeableHeader;
    final HwmfHeader header;
    
    public HwmfPicture(InputStream inputStream) throws IOException {

        try (BufferedInputStream bis = new BufferedInputStream(inputStream, 10000);
             LittleEndianInputStream leis = new LittleEndianInputStream(bis)) {
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
                if (wrt.clazz == null) {
                    throw new IOException("unsupported record type: "+recordFunction);
                }

                HwmfRecord wr;
                try {
                    wr = wrt.clazz.newInstance();
                    records.add(wr);
                } catch (Exception e) {
                    throw (IOException)new IOException("can't create wmf record").initCause(e);
                }

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
        Dimension dim = getSize();
        int width = Units.pointsToPixel(dim.getWidth());
        // keep aspect ratio for height
        int height = Units.pointsToPixel(dim.getHeight());
        Rectangle2D bounds = new Rectangle2D.Double(0,0,width,height);
        draw(ctx, bounds);
    }
    
    public void draw(Graphics2D ctx, Rectangle2D graphicsBounds) {
        AffineTransform at = ctx.getTransform();
        try {
            Rectangle2D wmfBounds = getBounds();
            // scale output bounds to image bounds
            ctx.translate(graphicsBounds.getX(), graphicsBounds.getY());
            ctx.scale(graphicsBounds.getWidth()/wmfBounds.getWidth(), graphicsBounds.getHeight()/wmfBounds.getHeight());
            
            HwmfGraphics g = new HwmfGraphics(ctx, wmfBounds);
            for (HwmfRecord r : records) {
                r.draw(g);
            }
        } finally {
            ctx.setTransform(at);
        }
    }

    /**
     * Returns the bounding box in device-independent units. Usually this is taken from the placeable header.
     * 
     * @return the bounding box
     */
    public Rectangle2D getBounds() {
        if (placeableHeader != null) {
            return placeableHeader.getBounds();
        } else {
            WmfSetWindowOrg wOrg = null;
            WmfSetWindowExt wExt = null;
            for (HwmfRecord r : getRecords()) {
                if (wOrg != null && wExt != null) {
                    break;
                }
                if (r instanceof WmfSetWindowOrg) {
                    wOrg = (WmfSetWindowOrg)r;
                } else if (r instanceof WmfSetWindowExt) {
                    wExt = (WmfSetWindowExt)r;
                }
            }
            if (wOrg == null || wExt == null) {
                throw new RuntimeException("invalid wmf file - window records are incomplete.");
            }
            return new Rectangle2D.Double(wOrg.getX(), wOrg.getY(), wExt.getWidth(), wExt.getHeight());
        }        
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
    public Dimension getSize() {
        double inch = (placeableHeader == null) ? 1440 : placeableHeader.getUnitsPerInch();
        Rectangle2D bounds = getBounds();
        
        //coefficient to translate from WMF dpi to 72dpi
        double coeff = Units.POINT_DPI/inch;
        return new Dimension((int)Math.round(bounds.getWidth()*coeff), (int)Math.round(bounds.getHeight()*coeff));
    }
}
