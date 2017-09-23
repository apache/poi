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

import java.io.IOException;

import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndian;

/**
 * Represents a DIB picture data in a PPT file
 */
public final class DIB extends Bitmap {

    //arbitrarily selected; may need to increase
    private static final int MAX_RECORD_LENGTH = 1_000_000;

    /**
     * Size of the BITMAPFILEHEADER structure preceding the actual DIB bytes
     */
    private static final int HEADER_SIZE = 14;

    @Override
    public PictureType getType(){
        return PictureType.DIB;
    }

    /**
     * DIB signature is {@code 0x7A80} or {@code 0x7A90}
     *
     * @return DIB signature ({@code 0x7A80} or {@code 0x7A90})
     */
    public int getSignature(){
        return (getUIDInstanceCount() == 1 ? 0x7A80 : 0x7A90);
    }

    /**
     * Sets the DIB signature - either {@code 0x7A80} or {@code 0x7A90}
     */
    public void setSignature(int signature) {
        switch (signature) {
            case 0x7A80:
                setUIDInstanceCount(1);
                break;
            case 0x7A90:
                setUIDInstanceCount(2);
                break;
            default:
                throw new IllegalArgumentException(signature+" is not a valid instance/signature value for DIB");
        }        
    }    
    
    @Override
    public byte[] getData(){
        return addBMPHeader ( super.getData() );
    }

    public static byte[] addBMPHeader(byte[] data){
        // bitmap file-header, corresponds to a
        // Windows  BITMAPFILEHEADER structure
        // (For more information, consult the Windows API Programmer's reference )
        byte[] header = new byte[HEADER_SIZE];
        //Specifies the file type. It must be set to the signature word BM (0x4D42) to indicate bitmap.
        LittleEndian.putInt(header, 0, 0x4D42);

        // read the size of the image and calculate the overall file size
        // and the offset where the bitmap starts
        int imageSize = LittleEndian.getInt(data, 0x22 - HEADER_SIZE);
        int fileSize = data.length + HEADER_SIZE;
        int offset = fileSize - imageSize;
        
		// specifies the size, in bytes, of the bitmap file - must add the length of the header
        LittleEndian.putInt(header, 2, fileSize); 
        // Reserved; set to zero
        LittleEndian.putInt(header, 6, 0);
        // the offset, i.e. starting address, of the byte where the bitmap data can be found
        LittleEndian.putInt(header, 10, offset);
        
        //DIB data is the header + dib bytes
        byte[] dib = IOUtils.safelyAllocate(header.length + data.length, MAX_RECORD_LENGTH);
        System.arraycopy(header, 0, dib, 0, header.length);
        System.arraycopy(data, 0, dib, header.length, data.length);

        return dib;
    }

    @Override
    public void setData(byte[] data) throws IOException {
        //cut off the bitmap file-header
        byte[] dib = IOUtils.safelyAllocate(data.length-HEADER_SIZE, data.length);
        System.arraycopy(data, HEADER_SIZE, dib, 0, dib.length);
        super.setData(dib);
    }
}
