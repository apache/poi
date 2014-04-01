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

import org.apache.poi.hslf.usermodel.PictureData;

import java.io.IOException;
import java.io.ByteArrayOutputStream;

/**
 * Represents a bitmap picture data:  JPEG or PNG.
 * The data is not compressed and the exact file content is written in the stream.
 *
 * @author Yegor Kozlov
 */
public abstract  class Bitmap extends PictureData {

    public byte[] getData(){
        byte[] rawdata = getRawData();
        byte[] imgdata = new byte[rawdata.length-17];
        System.arraycopy(rawdata, 17, imgdata, 0, imgdata.length);
        return imgdata;
    }

    public void setData(byte[] data) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] checksum = getChecksum(data);
        out.write(checksum);
        out.write(0);
        out.write(data);

        setRawData(out.toByteArray());
    }
}
