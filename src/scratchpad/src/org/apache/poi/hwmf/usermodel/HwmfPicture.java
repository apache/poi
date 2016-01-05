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
import org.apache.poi.util.LittleEndianInputStream;

public class HwmfPicture {
    final List<HwmfRecord> records = new ArrayList<HwmfRecord>();
    final HwmfPlaceableHeader placeableHeader;
    final HwmfHeader header;
    
    public HwmfPicture(InputStream inputStream) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(inputStream, 10000);
        LittleEndianInputStream leis = new LittleEndianInputStream(bis);
        placeableHeader = HwmfPlaceableHeader.readHeader(leis);
        header = new HwmfHeader(leis);
        
        for (;;) {
            // recordSize in DWORDs
            long recordSize = leis.readUInt()*2;
            int recordFunction = leis.readShort();
            // 4 bytes (recordSize) + 2 bytes (recordFunction)
            int consumedSize = 6;
            HwmfRecordType wrt = HwmfRecordType.getById(recordFunction);
            if (wrt == null) {
                throw new IOException("unexpected record type: "+recordFunction);
            }
            if (wrt == HwmfRecordType.eof) break;
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
            assert(remainingSize >= 0);
            if (remainingSize > 0) {
            	// skip size in loops, because not always all bytes are skipped in one call 
                for (int i=remainingSize; i>0; i-=leis.skip(i));
            }
        }
    }

    public List<HwmfRecord> getRecords() {
        return Collections.unmodifiableList(records);
    }

    public void draw(Graphics2D ctx) {
        AffineTransform at = ctx.getTransform();
        try {
            HwmfGraphics g = new HwmfGraphics(ctx, getBounds());
            for (HwmfRecord r : records)  {
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
}
