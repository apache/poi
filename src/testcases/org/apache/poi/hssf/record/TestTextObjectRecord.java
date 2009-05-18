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

import java.io.ByteArrayInputStream;
import java.util.Arrays;

import junit.framework.TestCase;

import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.hssf.record.formula.RefPtg;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.util.HexRead;
import org.apache.poi.util.LittleEndian;

/**
 * Tests that serialization and deserialization of the TextObjectRecord .
 * Test data taken directly from a real Excel file.
 *
 * @author Yegor Kozlov
 */
public final class TestTextObjectRecord extends TestCase {

    private static final byte[] simpleData = HexRead.readFromString(
        "B6 01 12 00 " +
        "12 02 00 00 00 00 00 00" +
        "00 00 0D 00 08 00    00 00" +
        "00 00 " +
        "3C 00 0E 00 " +
        "00 48 65 6C 6C 6F 2C 20 57 6F 72 6C 64 21 " +
        "3C 00 08 " +
        "00 0D 00 00 00 00 00 00 00"
    );


    public void testRead() {

        RecordInputStream is =TestcaseRecordInputStream.create(simpleData);
        TextObjectRecord record = new TextObjectRecord(is);

        assertEquals(TextObjectRecord.sid, record.getSid());
        assertEquals(TextObjectRecord.HORIZONTAL_TEXT_ALIGNMENT_LEFT_ALIGNED, record.getHorizontalTextAlignment());
        assertEquals(TextObjectRecord.VERTICAL_TEXT_ALIGNMENT_TOP, record.getVerticalTextAlignment());
        assertEquals(TextObjectRecord.TEXT_ORIENTATION_NONE, record.getTextOrientation());
        assertEquals("Hello, World!", record.getStr().getString());
    }

    public void testWrite() {
        HSSFRichTextString str = new HSSFRichTextString("Hello, World!");

        TextObjectRecord record = new TextObjectRecord();
        record.setStr(str);
        record.setHorizontalTextAlignment( TextObjectRecord.HORIZONTAL_TEXT_ALIGNMENT_LEFT_ALIGNED );
        record.setVerticalTextAlignment( TextObjectRecord.VERTICAL_TEXT_ALIGNMENT_TOP );
        record.setTextLocked( true );
        record.setTextOrientation( TextObjectRecord.TEXT_ORIENTATION_NONE );

        byte [] ser = record.serialize();
        assertEquals(ser.length , simpleData.length);

        assertTrue(Arrays.equals(simpleData, ser));

        //read again
        RecordInputStream is = TestcaseRecordInputStream.create(simpleData);
        record = new TextObjectRecord(is);
    }

    /**
     * Zero {@link ContinueRecord}s follow a {@link TextObjectRecord} if the text is empty
     */
    public void testWriteEmpty() {
        HSSFRichTextString str = new HSSFRichTextString("");

        TextObjectRecord record = new TextObjectRecord();
        record.setStr(str);

        byte [] ser = record.serialize();

        int formatDataLen = LittleEndian.getUShort(ser, 16);
        assertEquals("formatDataLength", 0, formatDataLen);

        assertEquals(22, ser.length); // just the TXO record

        //read again
        RecordInputStream is = TestcaseRecordInputStream.create(ser);
        record = new TextObjectRecord(is);
        assertEquals(0, record.getStr().length());
    }

    /**
     * Test that TextObjectRecord serializes logs records properly.
     */
    public void testLongRecords() {
        int[] length = {1024, 2048, 4096, 8192, 16384}; //test against strings of different length
        for (int i = 0; i < length.length; i++) {
            StringBuffer buff = new StringBuffer(length[i]);
            for (int j = 0; j < length[i]; j++) {
                buff.append("x");
            }
            HSSFRichTextString str = new HSSFRichTextString(buff.toString());

            TextObjectRecord obj = new TextObjectRecord();
            obj.setStr(str);

            byte [] data = obj.serialize();
            RecordInputStream is = new RecordInputStream(new ByteArrayInputStream(data));
            is.nextRecord();
            TextObjectRecord record = new TextObjectRecord(is);
            str = record.getStr();

            assertEquals(buff.length(), str.length());
            assertEquals(buff.toString(), str.getString());
        }
    }

    /**
     * Test cloning
     */
    public void testClone() {
        String text = "Hello, World";
        HSSFRichTextString str = new HSSFRichTextString(text);

        TextObjectRecord obj = new TextObjectRecord();
        obj.setStr( str );


        TextObjectRecord cloned = (TextObjectRecord)obj.clone();
        assertEquals(obj.getRecordSize(), cloned.getRecordSize());
        assertEquals(obj.getHorizontalTextAlignment(), cloned.getHorizontalTextAlignment());
        assertEquals(obj.getStr().getString(), cloned.getStr().getString());

        //finally check that the serialized data is the same
        byte[] src = obj.serialize();
        byte[] cln = cloned.serialize();
        assertTrue(Arrays.equals(src, cln));
    }

    /** similar to {@link #simpleData} but with link formula at end of TXO rec*/
    private static final byte[] linkData = HexRead.readFromString(
            "B6 01 " + // TextObjectRecord.sid
            "1E 00 " + // size 18
            "44 02 02 00 00 00 00 00" +
            "00 00 " +
            "02 00 " + // strLen 2
            "10 00 " + // 16 bytes for 2 format runs
            "00 00 00 00 " +

            "05 00 " +          // formula size
            "D4 F0 8A 03 " +    // unknownInt
            "24 01 00 13 C0 " + //tRef(T2)
            "13 " +             // ??

            "3C 00 " + // ContinueRecord.sid
            "03 00 " + // size 3
            "00 " + // unicode compressed
            "41 42 " + // 'AB'
            "3C 00 " + // ContinueRecord.sid
            "10 00 " + // size 16
            "00 00 18 00 00 00 00 00 " +
            "02 00 00 00 00 00 00 00 "
        );


    public void testLinkFormula() {
        RecordInputStream is = new RecordInputStream(new ByteArrayInputStream(linkData));
        is.nextRecord();
        TextObjectRecord rec = new TextObjectRecord(is);

        Ptg ptg = rec.getLinkRefPtg();
        assertNotNull(ptg);
        assertEquals(RefPtg.class, ptg.getClass());
        RefPtg rptg = (RefPtg) ptg;
        assertEquals("T2", rptg.toFormulaString());

        byte [] data2 = rec.serialize();
        assertEquals(linkData.length, data2.length);
        assertTrue(Arrays.equals(linkData, data2));
    }
}
