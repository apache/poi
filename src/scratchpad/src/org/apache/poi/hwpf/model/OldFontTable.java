/* ====================================================================
     Licensed to the Apache Software Foundation (ASF) under one or more
     contributor license agreements.    See the NOTICE file distributed with
     this work for additional information regarding copyright ownership.
     The ASF licenses this file to You under the Apache License, Version 2.0
     (the "License"); you may not use this file except in compliance with
     the License.    You may obtain a copy of the License at

             http://www.apache.org/licenses/LICENSE-2.0

     Unless required by applicable law or agreed to in writing, software
     distributed under the License is distributed on an "AS IS" BASIS,
     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     See the License for the specific language governing permissions and
     limitations under the License.
==================================================================== */

package org.apache.poi.hwpf.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * Font table for Word 6.0
 */
@Internal
public final class OldFontTable {
    private final static POILogger _logger = POILogFactory.getLogger(OldFontTable.class);

    // added extra facilitator members
    // FFN structure containing strings of font names
    private final OldFfn[] _fontNames;

    public OldFontTable(byte[] buf, int offset, int length) {
        //length is stored at the index section in the table
        //and it is recorded in the first short.


        List<OldFfn> ffns = new ArrayList<>();
        int fontTableLength = LittleEndian.getShort(buf, offset);

        int endOfTableOffset = offset + length;
        int startOffset = offset + LittleEndianConsts.SHORT_SIZE;//first short should == length!

        while (true) {
            OldFfn oldFfn = OldFfn.build(buf, startOffset, endOfTableOffset);
            if (oldFfn == null) {
                break;
            }
            ffns.add(oldFfn);
            startOffset += oldFfn.getLength();

        }
        _fontNames = ffns.toArray(new OldFfn[0]);
    }


    public OldFfn[] getFontNames() {
        return _fontNames;
    }


    public String getMainFont(int chpFtc) {
        if (chpFtc >= _fontNames.length) {
            _logger.log(POILogger.INFO, "Mismatch in chpFtc with stringCount");
            return null;
        }

        return _fontNames[chpFtc].getMainFontName();
    }

    @Override
    public String toString() {
        return "OldFontTable{" +
                "_fontNames=" + Arrays.toString(_fontNames) +
                '}';
    }
}
