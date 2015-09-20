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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.InflaterInputStream;

import org.apache.poi.hslf.exceptions.HSLFException;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.Units;

/**
 * Represents a WMF (Windows Metafile) picture data.
 */
public final class WMF extends Metafile {

    @Override
    public byte[] getData(){
        try {
            byte[] rawdata = getRawData();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream is = new ByteArrayInputStream( rawdata );
            Header header = new Header();
            header.read(rawdata, CHECKSUM_SIZE*uidInstanceCount);
            is.skip(header.getSize() + CHECKSUM_SIZE*uidInstanceCount);

            NativeHeader aldus = new NativeHeader(header.bounds);
            aldus.write(out);

            InflaterInputStream inflater = new InflaterInputStream( is );
            byte[] chunk = new byte[4096];
            int count;
            while ((count = inflater.read(chunk)) >=0 ) {
                out.write(chunk,0,count);
            }
            inflater.close();
            return out.toByteArray();
        } catch (IOException e){
            throw new HSLFException(e);
        }
    }

    @Override
    public void setData(byte[] data) throws IOException {
        int pos = 0;
        NativeHeader nHeader = new NativeHeader(data, pos);
        pos += nHeader.getLength();

        byte[] compressed = compress(data, pos, data.length-pos);

        Header header = new Header();
        header.wmfsize = data.length - nHeader.getLength();
        header.bounds = new Rectangle((short)nHeader.left, (short)nHeader.top, (short)nHeader.right-(short)nHeader.left, (short)nHeader.bottom-(short)nHeader.top);
        Dimension nDim = nHeader.getSize();
        header.size = new Dimension(Units.toEMU(nDim.getWidth()), Units.toEMU(nDim.getHeight()));
        header.zipsize = compressed.length;

        byte[] checksum = getChecksum(data);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (int i=0; i<uidInstanceCount; i++) {
            out.write(checksum);
        }
        header.write(out);
        out.write(compressed);

        setRawData(out.toByteArray());
    }

    @Override
    public PictureType getType(){
        return PictureType.WMF;
    }

    /**
     * WMF signature is either {@code 0x2160} or {@code 0x2170}
     */
    public int getSignature(){
        return (uidInstanceCount == 1 ? 0x2160 : 0x2170);
    }

    /**
     * Sets the WMF signature - either {@code 0x2160} or {@code 0x2170}
     */
    public void setSignature(int signature) {
        switch (signature) {
            case 0x2160:
                uidInstanceCount = 1;
                break;
            case 0x2170:
                uidInstanceCount = 2;
                break;
            default:
                throw new IllegalArgumentException(signature+" is not a valid instance/signature value for WMF");
        }
    }

    /**
     * Aldus Placeable Metafile header - 22 byte structure before WMF data.
     * <ul>
     *  <li>int Key;               Magic number (always 9AC6CDD7h)
     *  <li>short  Handle;         Metafile HANDLE number (always 0)
     *  <li>short Left;            Left coordinate in metafile units
     *  <li>short Top;             Top coordinate in metafile units
     *  <li>short Right;           Right coordinate in metafile units
     *  <li>short Bottom;          Bottom coordinate in metafile units
     *  <li>short  Inch;           Number of metafile units per inch
     *  <li>int Reserved;          Reserved (always 0)
     *  <li>short  Checksum;       Checksum value for previous 10 shorts
     * </ul>
     */
    @SuppressWarnings("unused")
    public static class NativeHeader {
        public static final int APMHEADER_KEY = 0x9AC6CDD7;
        private static POILogger logger = POILogFactory.getLogger(NativeHeader.class);

        private final int handle;
        private final int left, top, right, bottom;

        /**
         * The number of logical units per inch used to represent the image.
         * This value can be used to scale an image. By convention, an image is
         * considered to be recorded at 1440 logical units (twips) per inch. 
         * Thus, a value of 720 specifies that the image SHOULD be rendered at
         * twice its normal size, and a value of 2880 specifies that the image
         * SHOULD be rendered at half its normal size.
         */
        private final int inch; 
        private final int reserved;
        private int checksum;
        
        public NativeHeader(Rectangle dim) {
            handle = 0;
            left = dim.x;
            top = dim.y;
            right = dim.x + dim.width;
            bottom = dim.y + dim.height;
            inch = Units.POINT_DPI; //default resolution is 72 dpi
            reserved = 0;
        }

        public NativeHeader(byte[] data, int pos) {
            int key = LittleEndian.getInt(data, pos); pos += LittleEndian.INT_SIZE; //header key
            if (key != APMHEADER_KEY) {
                logger.log(POILogger.WARN, "WMF file doesn't contain a placeable header - ignore parsing");
                handle = 0;
                left = 0;
                top = 0;
                right = 200;
                bottom = 200;
                inch = Units.POINT_DPI; //default resolution is 72 dpi
                reserved = 0;
                return;
            }

            handle = LittleEndian.getUShort(data, pos); pos += LittleEndian.SHORT_SIZE;
            left = LittleEndian.getShort(data, pos); pos += LittleEndian.SHORT_SIZE;
            top = LittleEndian.getShort(data, pos); pos += LittleEndian.SHORT_SIZE;
            right = LittleEndian.getShort(data, pos); pos += LittleEndian.SHORT_SIZE;
            bottom = LittleEndian.getShort(data, pos); pos += LittleEndian.SHORT_SIZE;

            inch = LittleEndian.getUShort(data, pos); pos += LittleEndian.SHORT_SIZE;
            reserved = LittleEndian.getInt(data, pos); pos += LittleEndian.INT_SIZE;

            checksum = LittleEndian.getShort(data, pos); pos += LittleEndian.SHORT_SIZE;
            if (checksum != getChecksum()){
                logger.log(POILogger.WARN, "WMF checksum does not match the header data");
            }
        }

        /**
         * Returns a checksum value for the previous 10 shorts in the header.
         * The checksum is calculated by XORing each short value to an initial value of 0:
         */
        public int getChecksum(){
            int cs = 0;
            cs ^=  (APMHEADER_KEY & 0x0000FFFF);
            cs ^= ((APMHEADER_KEY & 0xFFFF0000) >> 16);
            cs ^= left;
            cs ^= top;
            cs ^= right;
            cs ^= bottom;
            cs ^= inch;
            return cs;
        }

        public void write(OutputStream out) throws IOException {
            byte[] header = new byte[22];
            int pos = 0;
            LittleEndian.putInt(header, pos, APMHEADER_KEY); pos += LittleEndian.INT_SIZE; //header key
            LittleEndian.putUShort(header, pos, 0); pos += LittleEndian.SHORT_SIZE; //hmf
            LittleEndian.putUShort(header, pos, left); pos += LittleEndian.SHORT_SIZE; //left
            LittleEndian.putUShort(header, pos, top); pos += LittleEndian.SHORT_SIZE; //top
            LittleEndian.putUShort(header, pos, right); pos += LittleEndian.SHORT_SIZE; //right
            LittleEndian.putUShort(header, pos, bottom); pos += LittleEndian.SHORT_SIZE; //bottom
            LittleEndian.putUShort(header, pos, inch); pos += LittleEndian.SHORT_SIZE; //inch
            LittleEndian.putInt(header, pos, 0); pos += LittleEndian.INT_SIZE;  //reserved

            checksum = getChecksum();
            LittleEndian.putUShort(header, pos, checksum);

            out.write(header);
        }

        public Dimension getSize() {
            //coefficient to translate from WMF dpi to 72dpi
            double coeff = ((double)Units.POINT_DPI)/inch;
            return new Dimension((int)Math.round((right-left)*coeff), (int)Math.round((bottom-top)*coeff));
        }
        
        public int getLength(){
            return 22;
        }
    }
}
