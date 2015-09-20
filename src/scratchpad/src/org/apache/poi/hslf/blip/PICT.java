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

import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.InflaterInputStream;

import org.apache.poi.hslf.exceptions.HSLFException;
import org.apache.poi.util.Units;

/**
 * Represents Macintosh PICT picture data.
 */
public final class PICT extends Metafile {
    public static class NativeHeader {
        /**
         * skip the first 512 bytes - they are MAC specific crap
         */
        public static final int PICT_HEADER_OFFSET = 512;
        
        public static final double DEFAULT_RESOLUTION = Units.POINT_DPI;
        
        private static final byte V2_HEADER[] = {
            0x00, 0x11,       // v2 version opcode
            0x02, (byte)0xFF, // version number of new picture
            0x0C, 0x00,        // header opcode
            (byte)0xFF, (byte)0xFE, 0x00, 0x00 // pic size dummy
        };

        public final Rectangle bounds;
        public final double hRes, vRes;
        
        public NativeHeader(byte data[], int offset) {
            // http://mirrors.apple2.org.za/apple.cabi.net/Graphics/PICT.and_QT.INFO/PICT.file.format.TI.txt

            // low order 16 bits of picture size - can be ignored
            offset += 2;
            // rectangular bounding box of picture, at 72 dpi
            // rect : 8 bytes (top, left, bottom, right: integer)
            int y1 = readUnsignedShort(data, offset); offset += 2;
            int x1 = readUnsignedShort(data, offset); offset += 2;
            int y2 = readUnsignedShort(data, offset); offset += 2;
            int x2 = readUnsignedShort(data, offset); offset += 2;

            // check for version 2 ... otherwise we don't read any further
            boolean isV2 = true;
            for (byte b : V2_HEADER) {
                if (b != data[offset++]) {
                    isV2 = false;
                    break;
                }
            }
            
            if (isV2) {
                // 4 bytes - fixed, horizontal resolution (dpi) of source data
                hRes = readFixedPoint(data, offset); offset += 4;
                // 4 bytes - fixed, vertical resolution (dpi) of source data
                vRes = readFixedPoint(data, offset); offset += 4;
            } else {
                hRes = DEFAULT_RESOLUTION;
                vRes = DEFAULT_RESOLUTION;
            }
            
            bounds = new Rectangle(x1,y1,x2-x1,y2-y1);
        }

        public Dimension getSize() {
            int height = (int)Math.round(bounds.height*DEFAULT_RESOLUTION/vRes);
            int width = (int)Math.round(bounds.width*DEFAULT_RESOLUTION/hRes);
            return new Dimension(width, height);
        }

        private static int readUnsignedShort(byte data[], int offset) {
            int b0 = data[offset] & 0xFF;
            int b1 = data[offset+1] & 0xFF;
            return b0 << 8 | b1;
        }

        private static double readFixedPoint(byte data[], int offset) {
            int b0 = data[offset] & 0xFF;
            int b1 = data[offset+1] & 0xFF;
            int b2 = data[offset+2] & 0xFF;
            int b3 = data[offset+3] & 0xFF;
            int i = (b0 << 24) | (b1 << 16) | (b2 << 8) | b3;
            return i / (double)0x10000;
        }
    }
    
    @Override
    public byte[] getData(){
        byte[] rawdata = getRawData();
        try {
            byte[] macheader = new byte[512];
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write(macheader);
            int pos = CHECKSUM_SIZE*uidInstanceCount;
            byte[] pict = read(rawdata, pos);
            out.write(pict);
            return out.toByteArray();
        } catch (IOException e){
            throw new HSLFException(e);
        }
    }

    private byte[] read(byte[] data, int pos) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        Header header = new Header();
        header.read(data, pos);
        bis.skip(pos + header.getSize());
        InflaterInputStream inflater = new InflaterInputStream( bis );
        byte[] chunk = new byte[4096];
        int count;
        while ((count = inflater.read(chunk)) >=0 ) {
            out.write(chunk,0,count);
        }
        inflater.close();
        return out.toByteArray();
    }

    @Override
    public void setData(byte[] data) throws IOException {
        // skip the first 512 bytes - they are MAC specific crap
        final int nOffset = NativeHeader.PICT_HEADER_OFFSET;
        NativeHeader nHeader = new NativeHeader(data, nOffset);
        
        Header header = new Header();
        header.wmfsize = data.length - nOffset;
        byte[] compressed = compress(data, nOffset, header.wmfsize);
        header.zipsize = compressed.length;
        header.bounds = nHeader.bounds;
        Dimension nDim = nHeader.getSize();
        header.size = new Dimension(Units.toEMU(nDim.getWidth()), Units.toEMU(nDim.getHeight()));

        byte[] checksum = getChecksum(data);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(checksum);
        if (uidInstanceCount == 2) {
            out.write(checksum);
        }
        header.write(out);
        out.write(compressed);

        setRawData(out.toByteArray());
    }

    @Override
    public PictureType getType(){
        return PictureType.PICT;
    }

    /**
     * PICT signature is {@code 0x5420} or {@code 0x5430}
     *
     * @return PICT signature ({@code 0x5420} or {@code 0x5430})
     */
    public int getSignature(){
        return (uidInstanceCount == 1 ? 0x5420 : 0x5430);
    }

    /**
     * Sets the PICT signature - either {@code 0x5420} or {@code 0x5430}
     */
    public void setSignature(int signature) {
        switch (signature) {
            case 0x5420:
                uidInstanceCount = 1;
                break;
            case 0x5430:
                uidInstanceCount = 2;
                break;
            default:
                throw new IllegalArgumentException(signature+" is not a valid instance/signature value for PICT");
        }        
    }
}
