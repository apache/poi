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
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;

import org.apache.commons.io.input.UnsynchronizedByteArrayInputStream;
import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.poi.ddf.EscherBSERecord;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.hslf.usermodel.HSLFPictureData;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianInputStream;
import org.apache.poi.util.LittleEndianOutputStream;
import org.apache.poi.util.Units;

/**
 * Represents a metafile picture which can be one of the following types: EMF, WMF, or PICT.
 * A metafile is stored compressed using the ZIP deflate/inflate algorithm.
 */
public abstract class Metafile extends HSLFPictureData {

    /**
     * Creates a new instance.
     *
     * @param recordContainer Record tracking all pictures. Should be attached to the slideshow that this picture is
     *                        linked to.
     * @param bse Record referencing this picture. Should be attached to the slideshow that this picture is linked to.
     */
    @Internal
    protected Metafile(EscherContainerRecord recordContainer, EscherBSERecord bse) {
        super(recordContainer, bse);
    }

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

        public void read(byte[] data, int offset) {
            try (
                    LittleEndianInputStream leis = new LittleEndianInputStream(
                            new UnsynchronizedByteArrayInputStream(data, offset, RECORD_LENGTH))
            ) {
                wmfsize = leis.readInt();

                int left = leis.readInt();
                int top = leis.readInt();
                int right = leis.readInt();
                int bottom = leis.readInt();
                bounds.setBounds(left, top, right - left, bottom - top);

                int width = leis.readInt();
                int height = leis.readInt();
                size.setSize(width, height);

                zipsize = leis.readInt();
                compression = leis.readUByte();
                filter = leis.readUByte();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
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

        void write(byte[] destination, int offset) {
            //hmf
            LittleEndian.putInt(destination, offset, wmfsize);
            offset += 4;

            //left
            LittleEndian.putInt(destination, offset, bounds.x);
            offset += 4;

            //top
            LittleEndian.putInt(destination, offset, bounds.y);
            offset += 4;

            //right
            LittleEndian.putInt(destination, offset, bounds.x + bounds.width);
            offset += 4;

            //bottom
            LittleEndian.putInt(destination, offset, bounds.y + bounds.height);
            offset += 4;

            //inch
            LittleEndian.putInt(destination, offset, size.width);
            offset += 4;

            //inch
            LittleEndian.putInt(destination, offset, size.height);
            offset += 4;

            LittleEndian.putInt(destination, offset, zipsize);
            offset += 4;

            destination[offset] = (byte) compression;
            offset++;

            destination[offset] = (byte) filter;
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

    protected static byte[] compress(byte[] bytes, int offset, int length) {
        UnsynchronizedByteArrayOutputStream out = new UnsynchronizedByteArrayOutputStream();
        try (DeflaterOutputStream deflater = new DeflaterOutputStream(out)) {
            deflater.write(bytes, offset, length);
        } catch (IOException ignored) {
            // IOException won't get thrown by the DeflaterOutputStream in this configuration because:
            //  1. UnsynchronizedByteArrayOutputStream doesn't throw an IOException during writes.
            //  2. The DeflaterOutputStream is not finished until we're done writing.
            throw new AssertionError("Won't happen", ignored);
        }
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
