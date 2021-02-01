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

package org.apache.poi.sl.image;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.RecordFormatException;

public final class ImageHeaderPNG {

    private static final int MAGIC_OFFSET = 16;

    private byte[] data;

    /**
     * @param data The raw image data
     */
    public ImageHeaderPNG(byte[] data) {
        this.data = data;
    }

    /**
     * PNG created on MAC may have a 16-byte prefix which prevents successful reading.
     * @return the trimmed PNG data
     */
    public byte[] extractPNG() {
        //
        //Just cut it off!.
        try (InputStream is = new ByteArrayInputStream(data)) {
            if (is.skip(MAGIC_OFFSET) == MAGIC_OFFSET && FileMagic.valueOf(is) == FileMagic.PNG) {
                return IOUtils.toByteArray(is);
            }
        } catch (IOException e) {
            throw new RecordFormatException("Unable to parse PNG header", e);
        }

        return data;
    }
}
