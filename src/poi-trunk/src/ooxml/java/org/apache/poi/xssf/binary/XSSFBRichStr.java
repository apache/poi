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

package org.apache.poi.xssf.binary;

import org.apache.poi.util.Internal;

/**
 * @since 3.16-beta3
 */
@Internal
class XSSFBRichStr {

    public static XSSFBRichStr build(byte[] bytes, int offset) throws XSSFBParseException {
        byte first = bytes[offset];
        boolean dwSizeStrRunExists = (first >> 7 & 1) == 1;//first bit == 1?
        boolean phoneticExists = (first >> 6 & 1) == 1;//second bit == 1?
        StringBuilder sb = new StringBuilder();

        int read = XSSFBUtils.readXLWideString(bytes, offset+1, sb);
        //TODO: parse phonetic strings.
        return new XSSFBRichStr(sb.toString(), "");
    }

    private final String string;
    private final String phoneticString;

    XSSFBRichStr(String string, String phoneticString) {
        this.string = string;
        this.phoneticString = phoneticString;
    }

    public String getString() {
        return string;
    }
}
