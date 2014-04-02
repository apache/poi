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

package org.apache.poi.hssf.record;


import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.util.HexRead;

import junit.framework.TestCase;

/**
 * Tests the serialization and deserialization of the TextObjectBaseRecord
 * class works correctly.  Test data taken directly from a real
 * Excel file.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public final class TestTextObjectBaseRecord extends TestCase {
	/** data for one TXO rec and two continue recs */
    private static final byte[] data = HexRead.readFromString(
        "B6 01 " + // TextObjectRecord.sid
        "12 00 " + // size 18
        "44 02 02 00 00 00 00 00" +
        "00 00 " +
        "02 00 " + // strLen 2
        "10 00 " + // 16 bytes for 2 format runs
        "00 00" +
        "00 00 " +
        "3C 00 " + // ContinueRecord.sid
        "03 00 " + // size 3
        "00 " + // unicode compressed
        "41 42 " + // 'AB'
        "3C 00 " + // ContinueRecord.sid
        "10 00 " + // size 16
        "00 00 18 00 00 00 00 00 " +
        "02 00 00 00 00 00 00 00 "
    );


    public void testLoad() {
        RecordInputStream in = TestcaseRecordInputStream.create(data);
        TextObjectRecord record = new TextObjectRecord(in);

        assertEquals(TextObjectRecord.HORIZONTAL_TEXT_ALIGNMENT_CENTERED, record.getHorizontalTextAlignment());
        assertEquals(TextObjectRecord.VERTICAL_TEXT_ALIGNMENT_JUSTIFY, record.getVerticalTextAlignment());
        assertEquals(true, record.isTextLocked());
        assertEquals(TextObjectRecord.TEXT_ORIENTATION_ROT_RIGHT, record.getTextOrientation());

        assertEquals(49, record.getRecordSize() );
    }

    public void testStore()
    {
        TextObjectRecord record = new TextObjectRecord();


        HSSFRichTextString str = new HSSFRichTextString("AB");
        str.applyFont(0, 2, (short)0x0018);
        str.applyFont(2, 2, (short)0x0320);

        record.setHorizontalTextAlignment(TextObjectRecord.HORIZONTAL_TEXT_ALIGNMENT_CENTERED);
        record.setVerticalTextAlignment(TextObjectRecord.VERTICAL_TEXT_ALIGNMENT_JUSTIFY);
        record.setTextLocked(true);
        record.setTextOrientation(TextObjectRecord.TEXT_ORIENTATION_ROT_RIGHT);
        record.setStr(str);

        byte [] recordBytes = record.serialize();
        assertEquals(recordBytes.length, data.length);
        for (int i = 0; i < data.length; i++)
            assertEquals("At offset " + i, data[i], recordBytes[i]);
    }
}
