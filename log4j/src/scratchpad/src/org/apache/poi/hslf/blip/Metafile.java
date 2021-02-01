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
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;

import org.apache.poi.hslf.usermodel.HSLFPictureData;
import org.apache.poi.util.LittleEndianInputStream;
import org.apache.poi.util.LittleEndianOutputStream;
import org.apache.poi.util.Units;

/**
 * Represents a metafile picture which can be one of the following types: EMF, WMF, or PICT.
 * A metafile is stored compressed using the ZIP deflate/inflate algorithm.
 */
public abstract class Metafile extends HSLFPictureData {

    /**
     *  A structure which represents a 34-byte header preceding the compressed metafile data
     */
    public static class Header{
        private static final int RECORD_LENGTH = 34;
        
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
        private int compression;

        /**
         * Reserved. Always 254.
         */
        private int filter = 254;

        public void read(byte[] data, int offset){
            @SuppressWarnings("resource")
            LittleEndianInputStream leis = new LittleEndianInputStream(
                new ByteArrayInputStream(data, offset, RECORD_LENGTH));
            
            wmfsize = leis.readInt();

            int left = leis.readInt();
            int top = leis.readInt();
            int right = leis.readInt();
            int bottom = leis.readInt();
            bounds.setBounds(left, top, right-left, bottom-top);

            int width = leis.readInt();
            int height = leis.readInt();
            size.setSize(width, height);

            zipsize = leis.readInt();
            compression = leis.readUByte();
            filter = leis.readUByte();
        }

        public void write(OutputStream out) throws IOException {
            @SuppressWarnings("resource")
            LittleEndianOutputStream leos = new LittleEndianOutputStream(out);
            
            //hmf
            leos.writeInt(wmfsize);
            //left
            leos.writeInt(bounds.x);
            //top
            leos.writeInt(bounds.y);
            //right
            leos.writeInt(bounds.x + bounds.width);
            //bottom
            leos.writeInt(bounds.y + bounds.height);
            //inch
            leos.writeInt(size.width);
            //inch
            leos.writeInt(size.height); 
            leos.writeInt(zipsize); 
            leos.writeByte(compression);
            leos.writeByte(filter);
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
