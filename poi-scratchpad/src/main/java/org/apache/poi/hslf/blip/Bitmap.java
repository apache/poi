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
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.apache.commons.io.input.UnsynchronizedByteArrayInputStream;
import org.apache.poi.ddf.EscherBSERecord;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.hslf.usermodel.HSLFPictureData;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Internal;
import org.apache.poi.util.Removal;
import org.apache.poi.util.Units;

/**
 * Represents a bitmap picture data:  JPEG or PNG.
 * The data is not compressed and the exact file content is written in the stream.
 */
public abstract class Bitmap extends HSLFPictureData {

    /**
     * @deprecated Use {@link HSLFSlideShow#addPicture(byte[], org.apache.poi.sl.usermodel.PictureData.PictureType)} or one of its overloads to create new
     *             {@link Bitmap}. This API led to detached {@link Bitmap} instances (See Bugzilla
     *             46122) and prevented adding additional functionality.
     */
    @Deprecated
    @Removal(version = "5.3")
    public Bitmap() {
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
    protected Bitmap(EscherContainerRecord recordContainer, EscherBSERecord bse) {
        super(recordContainer, bse);
    }

    @Override
    public byte[] getData(){
        byte[] rawdata = getRawData();
        int prefixLen = 16*getUIDInstanceCount()+1;
        return IOUtils.safelyClone(rawdata, prefixLen, rawdata.length-prefixLen, rawdata.length);
    }

    @Override
    protected byte[] formatImageForSlideshow(byte[] data) {
        byte[] checksum = getChecksum(data);
        byte[] rawData = new byte[checksum.length * getUIDInstanceCount() + 1 + data.length];
        int offset = 0;

        System.arraycopy(checksum, 0, rawData, offset, checksum.length);
        offset += checksum.length;

        if (getUIDInstanceCount() == 2) {
            System.arraycopy(checksum, 0, rawData, offset, checksum.length);
            offset += checksum.length;
        }

        offset++;
        System.arraycopy(data, 0, rawData, offset, data.length);
        return rawData;
    }

    @Override
    public Dimension getImageDimension() {
        try (InputStream is = new UnsynchronizedByteArrayInputStream(getData())){
            BufferedImage bi = ImageIO.read(is);
            return new Dimension(
                (int)Units.pixelToPoints(bi.getWidth()),
                (int)Units.pixelToPoints(bi.getHeight())
            );
        } catch (IOException e) {
            return new Dimension(200,200);
        }
    }
}
