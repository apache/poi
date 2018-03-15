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

package org.apache.poi.hemf.record;

import java.awt.Rectangle;
import java.io.IOException;

import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianInputStream;

/**
 * Extracts the full header from EMF files.
 * @see org.apache.poi.sl.image.ImageHeaderEMF
 */
@Internal
public class HemfHeader implements HemfRecord {

    private static final int MAX_RECORD_LENGTH = 1_000_000;


    private Rectangle boundsRectangle;
    private Rectangle frameRectangle;
    private long bytes;
    private long records;
    private int handles;
    private long nDescription;
    private long offDescription;
    private long nPalEntries;
    private boolean hasExtension1;
    private long cbPixelFormat;
    private long offPixelFormat;
    private long bOpenGL;
    private boolean hasExtension2;
    private long micrometersX;
    private long micrometersY;

    public Rectangle getBoundsRectangle() {
        return boundsRectangle;
    }

    public Rectangle getFrameRectangle() {
        return frameRectangle;
    }

    public long getBytes() {
        return bytes;
    }

    public long getRecords() {
        return records;
    }

    public int getHandles() {
        return handles;
    }

    public long getnDescription() {
        return nDescription;
    }

    public long getOffDescription() {
        return offDescription;
    }

    public long getnPalEntries() {
        return nPalEntries;
    }

    public boolean isHasExtension1() {
        return hasExtension1;
    }

    public long getCbPixelFormat() {
        return cbPixelFormat;
    }

    public long getOffPixelFormat() {
        return offPixelFormat;
    }

    public long getbOpenGL() {
        return bOpenGL;
    }

    public boolean isHasExtension2() {
        return hasExtension2;
    }

    public long getMicrometersX() {
        return micrometersX;
    }

    public long getMicrometersY() {
        return micrometersY;
    }

    @Override
    public String toString() {
        return "HemfHeader{" +
                "boundsRectangle=" + boundsRectangle +
                ", frameRectangle=" + frameRectangle +
                ", bytes=" + bytes +
                ", records=" + records +
                ", handles=" + handles +
                ", nDescription=" + nDescription +
                ", offDescription=" + offDescription +
                ", nPalEntries=" + nPalEntries +
                ", hasExtension1=" + hasExtension1 +
                ", cbPixelFormat=" + cbPixelFormat +
                ", offPixelFormat=" + offPixelFormat +
                ", bOpenGL=" + bOpenGL +
                ", hasExtension2=" + hasExtension2 +
                ", micrometersX=" + micrometersX +
                ", micrometersY=" + micrometersY +
                '}';
    }

    @Override
    public HemfRecordType getRecordType() {
        return HemfRecordType.header;
    }

    @Override
    public long init(LittleEndianInputStream leis, long recordId, long recordSize) throws IOException {
        if (recordId != 1L) {
            throw new IOException("Not a valid EMF header. Record type:"+recordId);
        }
        //read the record--id and size (2 bytes) have already been read
        byte[] data = IOUtils.safelyAllocate(recordSize, MAX_RECORD_LENGTH);
        IOUtils.readFully(leis, data);

        int offset = 0;

        //bounds
        int boundsLeft = LittleEndian.getInt(data, offset); offset += LittleEndian.INT_SIZE;
        int boundsTop = LittleEndian.getInt(data, offset); offset += LittleEndian.INT_SIZE;
        int boundsRight = LittleEndian.getInt(data, offset); offset += LittleEndian.INT_SIZE;
        int boundsBottom = LittleEndian.getInt(data, offset); offset += LittleEndian.INT_SIZE;
        boundsRectangle = new Rectangle(boundsLeft, boundsTop,
                boundsRight - boundsLeft, boundsBottom - boundsTop);

        int frameLeft = LittleEndian.getInt(data, offset); offset += LittleEndian.INT_SIZE;
        int frameTop = LittleEndian.getInt(data, offset); offset += LittleEndian.INT_SIZE;
        int frameRight = LittleEndian.getInt(data, offset); offset += LittleEndian.INT_SIZE;
        int frameBottom = LittleEndian.getInt(data, offset); offset += LittleEndian.INT_SIZE;
        frameRectangle = new Rectangle(frameLeft, frameTop,
                frameRight - frameLeft, frameBottom - frameTop);

        long recordSignature = LittleEndian.getInt(data, offset); offset += LittleEndian.INT_SIZE;
        if (recordSignature != 0x464D4520) {
            throw new IOException("bad record signature: " + recordSignature);
        }

        long version = LittleEndian.getInt(data, offset); offset += LittleEndian.INT_SIZE;
        //According to the spec, MSOffice doesn't pay attention to this value.
        //It _should_ be 0x00010000
        bytes = LittleEndian.getUInt(data, offset); offset += LittleEndian.INT_SIZE;
        records = LittleEndian.getUInt(data, offset); offset += LittleEndian.INT_SIZE;
        handles = LittleEndian.getUShort(data, offset);offset += LittleEndian.SHORT_SIZE;
        offset += LittleEndian.SHORT_SIZE;//reserved
        nDescription = LittleEndian.getUInt(data, offset); offset += LittleEndian.INT_SIZE;
        offDescription = LittleEndian.getUInt(data, offset); offset += LittleEndian.INT_SIZE;
        nPalEntries = LittleEndian.getUInt(data, offset); offset += LittleEndian.INT_SIZE;

        //should be skips
        offset += 8;//device
        offset += 8;//millimeters


        if (recordSize+8 >= 100) {
            hasExtension1 = true;
            cbPixelFormat = LittleEndian.getUInt(data, offset); offset += LittleEndian.INT_SIZE;
            offPixelFormat = LittleEndian.getUInt(data, offset); offset += LittleEndian.INT_SIZE;
            bOpenGL= LittleEndian.getUInt(data, offset); offset += LittleEndian.INT_SIZE;
        }

        if (recordSize+8 >= 108) {
            hasExtension2 = true;
            micrometersX = LittleEndian.getUInt(data, offset); offset += LittleEndian.INT_SIZE;
            micrometersY = LittleEndian.getUInt(data, offset); offset += LittleEndian.INT_SIZE;
        }
        return recordSize;
    }
}
