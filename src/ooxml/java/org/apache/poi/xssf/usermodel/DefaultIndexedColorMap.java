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
package org.apache.poi.xssf.usermodel;

import org.apache.poi.hssf.util.HSSFColor;

/**
 * Uses the legacy colors defined in HSSF for index lookups
 */
public class DefaultIndexedColorMap implements IndexedColorMap {

    /**
     * @see org.apache.poi.xssf.usermodel.IndexedColorMap#getRGB(int)
     */
    public byte[] getRGB(int index) {
        return getDefaultRGB(index);
    }

    /**
     * @param index
     * @return RGB bytes from HSSF default color by index
     */
    public static byte[] getDefaultRGB(int index) {
        HSSFColor hssfColor = HSSFColor.getIndexHash().get(index);
        if (hssfColor == null) return null;
        short[] rgbShort = hssfColor.getTriplet();
        return new byte[] {(byte) rgbShort[0], (byte) rgbShort[1], (byte) rgbShort[2]};
    }

}
