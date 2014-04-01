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

import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.POILogger;
import org.apache.poi.hslf.model.Picture;
import org.apache.poi.hslf.model.Shape;
import org.apache.poi.hslf.exceptions.HSLFException;

import java.io.*;
import java.util.zip.InflaterInputStream;

/**
 * Represents a WMF (Windows Metafile) picture data.
 *
 * @author Yegor Kozlov
 */
public final class WMF extends Metafile {

    /**
     * Extract compressed WMF data from a ppt
     */
    public byte[] getData(){
        try {
            byte[] rawdata = getRawData();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream is = new ByteArrayInputStream( rawdata );
            Header header = new Header();
            header.read(rawdata, CHECKSUM_SIZE);
            is.skip(header.getSize() + CHECKSUM_SIZE);

            AldusHeader aldus = new AldusHeader();
            aldus.left = header.bounds.x;
            aldus.top = header.bounds.y;
            aldus.right = header.bounds.x + header.bounds.width;
            aldus.bottom = header.bounds.y + header.bounds.height;
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

    public void setData(byte[] data) throws IOException {
        int pos = 0;
        AldusHeader aldus = new AldusHeader();
        aldus.read(data, pos);
        pos += aldus.getSize();

        byte[] compressed = compress(data, pos, data.length-pos);

        Header header = new Header();
        header.wmfsize = data.length - aldus.getSize();
        header.bounds = new java.awt.Rectangle((short)aldus.left, (short)aldus.top, (short)aldus.right-(short)aldus.left, (short)aldus.bottom-(short)aldus.top);
        //coefficient to translate from WMF dpi to 96pdi
        int coeff = 96*Shape.EMU_PER_POINT/aldus.inch;
        header.size = new java.awt.Dimension(header.bounds.width*coeff, header.bounds.height*coeff);
        header.zipsize = compressed.length;

        byte[] checksum = getChecksum(data);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(checksum);
        header.write(out);
        out.write(compressed);

        setRawData(out.toByteArray());
    }

    /**
     * We are of type <code>Picture.WMF</code>
     */
    public int getType(){
        return Picture.WMF;
    }

    /**
     * WMF signature is <code>0x2160</code>
     */
    public int getSignature(){
        return 0x2160;
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
    public class AldusHeader{
        public static final int APMHEADER_KEY = 0x9AC6CDD7;

        public int handle;
        public int left, top, right, bottom;
        public int inch = 72; //default resolution is 72 dpi
        public int reserved;
        public int checksum;

        public void read(byte[] data, int offset){
            int pos = offset;
            int key = LittleEndian.getInt(data, pos); pos += LittleEndian.INT_SIZE; //header key
            if (key != APMHEADER_KEY) throw new HSLFException("Not a valid WMF file");

            handle = LittleEndian.getUShort(data, pos); pos += LittleEndian.SHORT_SIZE;
            left = LittleEndian.getUShort(data, pos); pos += LittleEndian.SHORT_SIZE;
            top = LittleEndian.getUShort(data, pos); pos += LittleEndian.SHORT_SIZE;
            right = LittleEndian.getUShort(data, pos); pos += LittleEndian.SHORT_SIZE;
            bottom = LittleEndian.getUShort(data, pos); pos += LittleEndian.SHORT_SIZE;

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
            int checksum = 0;
            checksum ^=  (APMHEADER_KEY & 0x0000FFFF);
            checksum ^= ((APMHEADER_KEY & 0xFFFF0000) >> 16);
            checksum ^= left;
            checksum ^= top;
            checksum ^= right;
            checksum ^= bottom;
            checksum ^= inch;
            return checksum;
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

        public int getSize(){
            return 22;
        }
    }

}
