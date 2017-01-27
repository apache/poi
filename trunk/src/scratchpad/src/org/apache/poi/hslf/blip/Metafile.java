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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;

import org.apache.poi.hslf.usermodel.HSLFPictureData;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.Units;

/**
 * Represents a metafile picture which can be one of the following types: EMF, WMF, or PICT.
 * A metafile is stored compressed using the ZIP deflate/inflate algorithm.
 *
 * @author Yegor Kozlov
 */
public abstract class Metafile extends HSLFPictureData {

    /**
     *  A structure which represents a 34-byte header preceding the compressed metafile data
     *
     * @author Yegor Kozlov
     */
    public static class Header{

        /**
         * size of the original file
         */
        private int wmfsize;

        /**
         * Boundary of the metafile drawing commands
         */
        private final Rectangle bounds = new Rectangle();

        /**
         *  Size of the metafile in EMUs
         */
        private final Dimension size = new Dimension();

        /**
         * size of the compressed metafile data
         */
        private int zipsize;

        /**
         * Reserved. Always 0.
         */
        private int compression = 0;

        /**
         * Reserved. Always 254.
         */
        private int filter = 254;

        public void read(byte[] data, int offset){
            int pos = offset;
            wmfsize = LittleEndian.getInt(data, pos);   pos += LittleEndian.INT_SIZE;

            int left = LittleEndian.getInt(data, pos); pos += LittleEndian.INT_SIZE;
            int top = LittleEndian.getInt(data, pos); pos += LittleEndian.INT_SIZE;
            int right = LittleEndian.getInt(data, pos); pos += LittleEndian.INT_SIZE;
            int bottom = LittleEndian.getInt(data, pos); pos += LittleEndian.INT_SIZE;

            bounds.setBounds(left, top, right-left, bottom-top);
            int width = LittleEndian.getInt(data, pos); pos += LittleEndian.INT_SIZE;
            int height = LittleEndian.getInt(data, pos); pos += LittleEndian.INT_SIZE;

            size.setSize(width, height);

            zipsize = LittleEndian.getInt(data, pos); pos += LittleEndian.INT_SIZE;

            compression = LittleEndian.getUByte(data, pos); pos++;
            filter = LittleEndian.getUByte(data, pos); pos++;
        }

        public void write(OutputStream out) throws IOException {
            byte[] header = new byte[34];
            int pos = 0;
            LittleEndian.putInt(header, pos, wmfsize); pos += LittleEndian.INT_SIZE; //hmf

            LittleEndian.putInt(header, pos, bounds.x); pos += LittleEndian.INT_SIZE; //left
            LittleEndian.putInt(header, pos, bounds.y); pos += LittleEndian.INT_SIZE; //top
            LittleEndian.putInt(header, pos, bounds.x + bounds.width); pos += LittleEndian.INT_SIZE; //right
            LittleEndian.putInt(header, pos, bounds.y + bounds.height); pos += LittleEndian.INT_SIZE; //bottom
            LittleEndian.putInt(header, pos, size.width); pos += LittleEndian.INT_SIZE; //inch
            LittleEndian.putInt(header, pos, size.height); pos += LittleEndian.INT_SIZE; //inch
            LittleEndian.putInt(header, pos, zipsize); pos += LittleEndian.INT_SIZE; //inch

            header[pos] = 0; pos ++;
            header[pos] = (byte)filter; pos ++;

            out.write(header);
        }

        public int getSize(){
            return 34;
        }
        
        public int getWmfSize() {
            return wmfsize;
        }
        
        protected void setWmfSize(int wmfSize) {
            this.wmfsize = wmfSize;
        }
        
        protected void setZipSize(int zipSize) {
            this.zipsize = zipSize;
        }

        public Rectangle getBounds() {
            return (Rectangle)bounds.clone();
        }
        
        protected void setBounds(Rectangle bounds) {
            this.bounds.setBounds(bounds);
        }
        
        protected void setDimension(Dimension size) {
            this.size.setSize(size);
        }
    }

    protected static byte[] compress(byte[] bytes, int offset, int length) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DeflaterOutputStream  deflater = new DeflaterOutputStream( out );
        deflater.write(bytes, offset, length);
        deflater.close();
        return out.toByteArray();
    }

    @Override
    public Dimension getImageDimension() {
        int prefixLen = 16*getUIDInstanceCount();
        Header header = new Header();
        header.read(getRawData(), prefixLen);
        return new Dimension(
            (int)Math.round(Units.toPoints((long)header.size.getWidth())),
            (int)Math.round(Units.toPoints((long)header.size.getHeight()))
        );
    }
}
