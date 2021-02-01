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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.util.Internal;

/**
 * This is a very thin shim to gather number formats from styles.bin
 * files.
 *
 * @since 3.16-beta3
 */
@Internal
public class XSSFBStylesTable extends XSSFBParser {

    private final SortedMap<Short, String> numberFormats = new TreeMap<>();
    private final List<Short> styleIds = new ArrayList<>();

    private boolean inCellXFS;
    private boolean inFmts;
    public XSSFBStylesTable(InputStream is) throws IOException {
        super(is);
        parse();
    }

    String getNumberFormatString(int idx) {
        short numberFormatIdx = getNumberFormatIndex(idx);
        if (numberFormats.containsKey(numberFormatIdx)) {
            return numberFormats.get(numberFormatIdx);
        }

        return BuiltinFormats.getBuiltinFormat(numberFormatIdx);
    }

    short getNumberFormatIndex(int idx) {
        return styleIds.get(idx);
    }

    @Override
    public void handleRecord(int recordType, byte[] data) throws XSSFBParseException {
        XSSFBRecordType type = XSSFBRecordType.lookup(recordType);
        switch (type) {
            case BrtBeginCellXFs:
                inCellXFS = true;
                break;
            case BrtEndCellXFs:
                inCellXFS = false;
                break;
            case BrtXf:
                if (inCellXFS) {
                    handleBrtXFInCellXF(data);
                }
                break;
            case BrtBeginFmts:
                inFmts = true;
                break;
            case BrtEndFmts:
                inFmts = false;
                break;
            case BrtFmt:
                if (inFmts) {
                    handleFormat(data);
                }
                break;

        }
    }

    private void handleFormat(byte[] data) {
        int ifmt = data[0] & 0xFF;
        if (ifmt > Short.MAX_VALUE) {
            throw new POIXMLException("Format id must be a short");
        }
        StringBuilder sb = new StringBuilder();
        XSSFBUtils.readXLWideString(data, 2, sb);
        String fmt = sb.toString();
        numberFormats.put((short)ifmt, fmt);
    }

    private void handleBrtXFInCellXF(byte[] data) {
        int ifmtOffset = 2;
        //int ifmtLength = 2;

        //numFmtId in xml terms
        int ifmt = data[ifmtOffset] & 0xFF;//the second byte is ignored
        styleIds.add((short)ifmt);
    }
}
