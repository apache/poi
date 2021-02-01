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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.InflaterInputStream;

import org.apache.poi.hslf.exceptions.HSLFException;
import org.apache.poi.sl.image.ImageHeaderWMF;
import org.apache.poi.util.IOUtils;
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
            header.read(rawdata, CHECKSUM_SIZE*getUIDInstanceCount());
            long skipLen = header.getSize() + (long)CHECKSUM_SIZE*getUIDInstanceCount();
            long skipped = IOUtils.skipFully(is, skipLen);
            assert(skipped == skipLen);

            ImageHeaderWMF aldus = new ImageHeaderWMF(header.getBounds());
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
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(checksum);
        if (getUIDInstanceCount() == 2) {
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
        return (getUIDInstanceCount() == 1 ? 0x2160 : 0x2170);
    }

    /**
     * Sets the WMF signature - either {@code 0x2160} or {@code 0x2170}
     */
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
