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
import java.io.EOFException;
import java.io.IOException;
import java.util.zip.InflaterInputStream;

import org.apache.poi.hslf.exceptions.HSLFException;
import org.apache.poi.sl.image.ImageHeaderPICT;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.Units;

/**
 * Represents Macintosh PICT picture data.
 */
public final class PICT extends Metafile {
    private static final POILogger LOG = POILogFactory.getLogger(PICT.class);


    @Override
    public byte[] getData(){
        byte[] rawdata = getRawData();
        try {
            byte[] macheader = new byte[512];
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write(macheader);
            int pos = CHECKSUM_SIZE*getUIDInstanceCount();
            byte[] pict = read(rawdata, pos);
            out.write(pict);
            return out.toByteArray();
        } catch (IOException e){
            throw new HSLFException(e);
        }
    }

    private byte[] read(byte[] data, int pos) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        Header header = new Header();
        header.read(data, pos);
        long bs_exp = (long)pos + header.getSize();
        long bs_act = IOUtils.skipFully(bis, bs_exp);
        if (bs_exp != bs_act) {
            throw new EOFException();
        }
        byte[] chunk = new byte[4096];
        ByteArrayOutputStream out = new ByteArrayOutputStream(header.getWmfSize());
        try (InflaterInputStream inflater = new InflaterInputStream(bis)) {
            int count;
            while ((count = inflater.read(chunk)) >= 0) {
                out.write(chunk, 0, count);
                // PICT zip-stream can be erroneous, so we clear the array to determine
                // the maximum of read bytes, after the inflater crashed
                bytefill(chunk, (byte) 0);
            }
        } catch (Exception e) {
            int lastLen;
            for (lastLen = chunk.length - 1; lastLen >= 0 && chunk[lastLen] == 0; lastLen--) ;
            if (++lastLen > 0) {
                if (header.getWmfSize() > out.size()) {
                    // sometimes the wmfsize is smaller than the amount of already successfully read bytes
                    // in this case we take the lastLen as-is, otherwise we truncate it to the given size
                    lastLen = Math.min(lastLen, header.getWmfSize() - out.size());
                }
                out.write(chunk, 0, lastLen);
            }
            // End of picture marker for PICT is 0x00 0xFF
            LOG.log(POILogger.ERROR, "PICT zip-stream is invalid, read as much as possible. Uncompressed length of header: ", header.getWmfSize(), " / Read bytes: ", out.size(), e);
        }
        return out.toByteArray();
    }

    @Override
    public void setData(byte[] data) throws IOException {
        // skip the first 512 bytes - they are MAC specific crap
        final int nOffset = ImageHeaderPICT.PICT_HEADER_OFFSET;
        ImageHeaderPICT nHeader = new ImageHeaderPICT(data, nOffset);

        Header header = new Header();
        int wmfSize = data.length - nOffset;
        header.setWmfSize(wmfSize);
        byte[] compressed = compress(data, nOffset, wmfSize);
        header.setZipSize(compressed.length);
        header.setBounds(nHeader.getBounds());
        Dimension nDim = nHeader.getSize();
        header.setDimension(new Dimension(Units.toEMU(nDim.getWidth()), Units.toEMU(nDim.getHeight())));

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
        return PictureType.PICT;
    }

    /**
     * PICT signature is {@code 0x5420} or {@code 0x5430}
     *
     * @return PICT signature ({@code 0x5420} or {@code 0x5430})
     */
    public int getSignature(){
        return (getUIDInstanceCount() == 1 ? 0x5420 : 0x5430);
    }

    /**
     * Sets the PICT signature - either {@code 0x5420} or {@code 0x5430}
     */
    public void setSignature(int signature) {
        switch (signature) {
            case 0x5420:
                setUIDInstanceCount(1);
                break;
            case 0x5430:
                setUIDInstanceCount(2);
                break;
            default:
                throw new IllegalArgumentException(signature+" is not a valid instance/signature value for PICT");
        }
    }


    /*
     * initialize a smaller piece of the array and use the System.arraycopy
     * call to fill in the rest of the array in an expanding binary fashion
     */
    private static void bytefill(byte[] array, byte value) {
        // http://stackoverflow.com/questions/9128737/fastest-way-to-set-all-values-of-an-array
        int len = array.length;

        if (len > 0){
            array[0] = value;
        }

        for (int i = 1; i < len; i += i) {
            System.arraycopy(array, 0, array, i, Math.min(len - i, i));
        }
    }
}
