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


import org.apache.poi.poifs.filesystem.FileMagic;

import java.util.Arrays;

public final class ImageHeaderPNG {

    private static final int MAGIC_OFFSET = 16;

    private final byte[] data;

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
        if (data.length >= MAGIC_OFFSET) {
            byte[] newData = Arrays.copyOfRange(data, MAGIC_OFFSET, data.length);
            if (FileMagic.valueOf(newData) == FileMagic.PNG) {
                return newData;
            }
        }

        return data;
    }
}
