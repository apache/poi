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
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.InflaterInputStream;

import org.apache.commons.io.input.UnsynchronizedByteArrayInputStream;
import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.poi.ddf.EscherBSERecord;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.hslf.exceptions.HSLFException;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.sl.image.ImageHeaderWMF;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Internal;
import org.apache.poi.util.Removal;
import org.apache.poi.util.Units;

/**
 * Represents a WMF (Windows Metafile) picture data.
 */
public final class WMF extends Metafile {

    /**
     * @deprecated Use {@link HSLFSlideShow#addPicture(byte[], org.apache.poi.sl.usermodel.PictureData.PictureType)} or one of its overloads to create new
     *             WMF. This API led to detached WMF instances (See Bugzilla
     *             46122) and prevented adding additional functionality.
     */
    @Deprecated
    @Removal(version = "5.3")
    public WMF() {
        this(new EscherContainerRecord(), new EscherBSERecord());
    }

    /**
     * Creates a new instance.
     *
     * @param recordContainer Record tracking all pictures. Should be attached to the slideshow that this picture is
     *                        linked to.
     * @param bse Record referencing this picture. Should be attached to the slideshow that this picture is linked to.
     */
    @Internal
    public WMF(EscherContainerRecord recordContainer, EscherBSERecord bse) {
        super(recordContainer, bse);
    }

    @Override
    public byte[] getData(){
        byte[] rawdata = getRawData();
        try (InputStream is = new UnsynchronizedByteArrayInputStream(rawdata)) {


            Header header = new Header();
            header.read(rawdata, CHECKSUM_SIZE*getUIDInstanceCount());
            long skipLen = header.getSize() + (long)CHECKSUM_SIZE*getUIDInstanceCount();
            long skipped = IOUtils.skipFully(is, skipLen);
            assert(skipped == skipLen);

            ImageHeaderWMF aldus = new ImageHeaderWMF(header.getBounds());
            UnsynchronizedByteArrayOutputStream out = new UnsynchronizedByteArrayOutputStream();
            aldus.write(out);

            try (InflaterInputStream inflater = new InflaterInputStream( is )) {
                IOUtils.copy(inflater, out);
            }
            return out.toByteArray();
        } catch (IOException e){
            throw new HSLFException(e);
        }
    }

    @Override
    protected byte[] formatImageForSlideshow(byte[] data) {
        int pos = 0;
        ImageHeaderWMF nHeader = new ImageHeaderWMF(data, pos);
        pos += nHeader.getLength();

        byte[] compressed = compress(data, pos, data.length-pos);

        Header header = new Header();
        header.setWmfSize(data.length - nHeader.getLength());
        header.setBounds(nHeader.getBounds());
        Dimension nDim = nHeader.getSize();
        header.setDimension(new Dimension(Units.toEMU(nDim.getWidth()), Units.toEMU(nDim.getHeight())));
        header.setZipSize(compressed.length);

        byte[] checksum = getChecksum(data);
        byte[] rawData = new byte[checksum.length * getUIDInstanceCount() + header.getSize() + compressed.length];
        int offset = 0;

        System.arraycopy(checksum, 0, rawData, offset, checksum.length);
        offset += checksum.length;

        if (getUIDInstanceCount() == 2) {
            System.arraycopy(checksum, 0, rawData, offset, checksum.length);
            offset += checksum.length;
        }

        header.write(rawData, offset);
        offset += header.getSize();
        System.arraycopy(compressed, 0, rawData, offset, compressed.length);

        return rawData;
    }

    @Override
    public PictureType getType(){
        return PictureType.WMF;
    }

    /**
     * WMF signature is either {@code 0x2160} or {@code 0x2170}
     */
    @Override
    public int getSignature(){
        return (getUIDInstanceCount() == 1 ? 0x2160 : 0x2170);
    }

    /**
     * Sets the WMF signature - either {@code 0x2160} or {@code 0x2170}
     */
    @Override
    public void setSignature(int signature) {
        switch (signature) {
            case 0x2160:
                setUIDInstanceCount(1);
                break;
            case 0x2170:
                setUIDInstanceCount(2);
                break;
            default:
                throw new IllegalArgumentException(signature+" is not a valid instance/signature value for WMF");
        }
    }
}
