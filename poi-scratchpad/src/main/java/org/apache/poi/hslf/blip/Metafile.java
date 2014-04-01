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
import org.apache.poi.hslf.usermodel.PictureData;

import java.awt.*;
import java.io.*;
import java.util.zip.DeflaterOutputStream;

/**
 * Represents a metafile picture which can be one of the following types: EMF, WMF, or PICT.
 * A metafile is stored compressed using the ZIP deflate/inflate algorithm.
 *
 * @author Yegor Kozlov
 */
public abstract class Metafile extends PictureData {

    /**
     *  A structure which represents a 34-byte header preceeding the compressed metafile data
     *
     * @author Yegor Kozlov
     */
    public static class Header{

        /**
         * size of the original file
         */
        public int wmfsize;

        /**
         * Boundary of the metafile drawing commands
         */
        public Rectangle bounds;

        /**
         *  Size of the metafile in EMUs
         */
        public Dimension size;

        /**
         * size of the compressed metafile data
         */
        public int zipsize;

        /**
         * Reserved. Always 0.
         */
        public int compression;

        /**
         * Reserved. Always 254.
         */
        public int filter = 254;

        public void read(byte[] data, int offset){
            int pos = offset;
            wmfsize = LittleEndian.getInt(data, pos);   pos += LittleEndian.INT_SIZE;

            int left = LittleEndian.getInt(data, pos); pos += LittleEndian.INT_SIZE;
            int top = LittleEndian.getInt(data, pos); pos += LittleEndian.INT_SIZE;
            int right = LittleEndian.getInt(data, pos); pos += LittleEndian.INT_SIZE;
            int bottom = LittleEndian.getInt(data, pos); pos += LittleEndian.INT_SIZE;

            bounds = new Rectangle(left, top, right-left, bottom-top);
            int width = LittleEndian.getInt(data, pos); pos += LittleEndian.INT_SIZE;
            int height = LittleEndian.getInt(data, pos); pos += LittleEndian.INT_SIZE;

            size = new Dimension(width, height);

            zipsize = LittleEndian.getInt(data, pos); pos += LittleEndian.INT_SIZE;

            compression = LittleEndian.getUnsignedByte(data, pos); pos++;
            filter = LittleEndian.getUnsignedByte(data, pos); pos++;
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
    }

    protected byte[] compress(byte[] bytes, int offset, int length) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DeflaterOutputStream  deflater = new DeflaterOutputStream( out );
        deflater.write(bytes, offset, length);
        deflater.close();
        return out.toByteArray();
    }
}
