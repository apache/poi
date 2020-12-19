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
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.poi.hslf.usermodel.HSLFPictureData;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Units;

/**
 * Represents a bitmap picture data:  JPEG or PNG.
 * The data is not compressed and the exact file content is written in the stream.
 */
public abstract class Bitmap extends HSLFPictureData {

    @Override
    public byte[] getData(){
        byte[] rawdata = getRawData();
        int prefixLen = 16*getUIDInstanceCount()+1;
        return IOUtils.safelyClone(rawdata, prefixLen, rawdata.length-prefixLen, rawdata.length);
    }

    @Override
    public void setData(byte[] data) throws IOException {
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
        setRawData(rawData);
    }

    @Override
    public Dimension getImageDimension() {
        try {
            BufferedImage bi = ImageIO.read(new ByteArrayInputStream(getData()));
            return new Dimension(
                (int)Units.pixelToPoints(bi.getWidth()),
                (int)Units.pixelToPoints(bi.getHeight())
            );
        } catch (IOException e) {
            return new Dimension(200,200);
        }
    }
}
