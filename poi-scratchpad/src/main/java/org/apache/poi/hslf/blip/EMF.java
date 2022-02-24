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
import org.apache.poi.sl.image.ImageHeaderEMF;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Internal;
import org.apache.poi.util.Removal;
import org.apache.poi.util.Units;

/**
 * Represents EMF (Windows Enhanced Metafile) picture data.
 */
public final class EMF extends Metafile {

    /**
     * @deprecated Use {@link HSLFSlideShow#addPicture(byte[], org.apache.poi.sl.usermodel.PictureData.PictureType)} or one of its overloads to create new
     *             EMF. This API led to detached EMF instances (See Bugzilla
     *             46122) and prevented adding additional functionality.
     */
    @Deprecated
    @Removal(version = "5.3")
    public EMF() {
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
    public EMF(EscherContainerRecord recordContainer, EscherBSERecord bse) {
        super(recordContainer, bse);
    }

    @Override
    public byte[] getData(){
        byte[] rawdata = getRawData();
        Header header = new Header();
        header.read(rawdata, CHECKSUM_SIZE);

        try (
                InputStream is = new UnsynchronizedByteArrayInputStream(rawdata);
                InflaterInputStream inflater = new InflaterInputStream(is);
                UnsynchronizedByteArrayOutputStream out = new UnsynchronizedByteArrayOutputStream()
        ) {
            long len = IOUtils.skipFully(is,header.getSize() + (long)CHECKSUM_SIZE);
            assert(len == header.getSize() + CHECKSUM_SIZE);

            IOUtils.copy(inflater, out);

            return out.toByteArray();
        } catch (IOException e){
            throw new HSLFException(e);
        }
    }

    @Override
    protected byte[] formatImageForSlideshow(byte[] data) {
        byte[] compressed = compress(data, 0, data.length);

        ImageHeaderEMF nHeader = new ImageHeaderEMF(data, 0);

        Header header = new Header();
        header.setWmfSize(data.length);
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
        return PictureType.EMF;
    }

    /**
     * EMF signature is {@code 0x3D40} or {@code 0x3D50}
     *
     * @return EMF signature ({@code 0x3D40} or {@code 0x3D50})
     */
    @Override
    public int getSignature(){
        return (getUIDInstanceCount() == 1 ? 0x3D40 : 0x3D50);
    }

    /**
     * Sets the EMF signature - either {@code 0x3D40} or {@code 0x3D50}
     */
    @Override
    public void setSignature(int signature) {
        switch (signature) {
            case 0x3D40:
                setUIDInstanceCount(1);
                break;
            case 0x3D50:
                setUIDInstanceCount(2);
                break;
            default:
                throw new IllegalArgumentException(signature+" is not a valid instance/signature value for EMF");
        }
    }
}
