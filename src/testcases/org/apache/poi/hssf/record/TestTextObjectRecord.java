
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

import junit.framework.*;

import java.util.Arrays;
import java.util.List;
import java.io.ByteArrayInputStream;

import org.apache.poi.hssf.usermodel.HSSFRichTextString;

/**
 * Tests that serialization and deserialization of the TextObjectRecord .
 * Test data taken directly from a real Excel file.
 *
 * @author Yegor Kozlov
 */
public class TestTextObjectRecord extends TestCase {

    byte[] data = {(byte)0xB6, 0x01, 0x12, 0x00, 0x12, 0x02, 0x00, 0x00, 0x00, 0x00,
                   0x00, 0x00, 0x00, 0x00, 0x0D, 0x00, 0x08, 0x00, 0x00, 0x00, 0x00,
                   0x00, 0x3C, 0x00, 0x1B, 0x00, 0x01, 0x48, 0x00, 0x65, 0x00, 0x6C,
                   0x00, 0x6C, 0x00, 0x6F, 0x00, 0x2C, 0x00, 0x20, 0x00, 0x57, 0x00,
                   0x6F, 0x00, 0x72, 0x00, 0x6C, 0x00, 0x64, 0x00, 0x21, 0x00, 0x3C,
                   0x00, 0x08, 0x00, 0x0D, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };


    public void testRead()
            throws Exception
    {


        RecordInputStream is = new RecordInputStream(new ByteArrayInputStream(data));
        is.nextRecord();
        TextObjectRecord record = new TextObjectRecord(is);

        assertEquals(TextObjectRecord.sid, record.getSid());
        record.validateSid(TextObjectRecord.sid);
        assertEquals(TextObjectRecord.HORIZONTAL_TEXT_ALIGNMENT_LEFT_ALIGNED, record.getHorizontalTextAlignment());
        assertEquals(TextObjectRecord.VERTICAL_TEXT_ALIGNMENT_TOP, record.getVerticalTextAlignment());
        assertEquals(TextObjectRecord.TEXT_ORIENTATION_NONE, record.getTextOrientation());
        assertEquals(0, record.getReserved7());
        assertEquals("Hello, World!", record.getStr().getString());

    }

    public void testWrite()
    {
        HSSFRichTextString str = new HSSFRichTextString("Hello, World!");

        TextObjectRecord record = new TextObjectRecord();
        int frLength = ( str.numFormattingRuns() + 1 ) * 8;
        record.setFormattingRunLength( (short) frLength );
        record.setTextLength( (short) str.length() );
        record.setStr( str );
        record.setHorizontalTextAlignment( TextObjectRecord.HORIZONTAL_TEXT_ALIGNMENT_LEFT_ALIGNED );
        record.setVerticalTextAlignment( TextObjectRecord.VERTICAL_TEXT_ALIGNMENT_TOP );
        record.setTextLocked( true );
        record.setTextOrientation( TextObjectRecord.TEXT_ORIENTATION_NONE );
        record.setReserved7( 0 );

        byte [] ser = record.serialize();
        //assertEquals(ser.length , data.length);

        //assertTrue(Arrays.equals(data, ser));

        //read again
        RecordInputStream is = new RecordInputStream(new ByteArrayInputStream(data));
        is.nextRecord();
        record = new TextObjectRecord(is);

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
            int frLength = ( str.numFormattingRuns() + 1 ) * 8;
            obj.setFormattingRunLength( (short) frLength );
            obj.setTextLength( (short) str.length() );
            obj.setStr( str );

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
        int frLength = ( str.numFormattingRuns() + 1 ) * 8;
        obj.setFormattingRunLength( (short) frLength );
        obj.setTextLength( (short) str.length() );
        obj.setReserved1(true);
        obj.setReserved2((short)2);
        obj.setReserved3((short)3);
        obj.setReserved4((short)4);
        obj.setReserved5((short)5);
        obj.setReserved6((short)6);
        obj.setReserved7((short)7);
        obj.setStr( str );


        TextObjectRecord cloned = (TextObjectRecord)obj.clone();
        assertEquals(obj.getReserved2(), cloned.getReserved2());
        assertEquals(obj.getReserved3(), cloned.getReserved3());
        assertEquals(obj.getReserved4(), cloned.getReserved4());
        assertEquals(obj.getReserved5(), cloned.getReserved5());
        assertEquals(obj.getReserved6(), cloned.getReserved6());
        assertEquals(obj.getReserved7(), cloned.getReserved7());
        assertEquals(obj.getRecordSize(), cloned.getRecordSize());
        assertEquals(obj.getOptions(), cloned.getOptions());
        assertEquals(obj.getHorizontalTextAlignment(), cloned.getHorizontalTextAlignment());
        assertEquals(obj.getFormattingRunLength(), cloned.getFormattingRunLength());
        assertEquals(obj.getStr().getString(), cloned.getStr().getString());

        //finally check that the serialized data is the same
        byte[] src = obj.serialize();
        byte[] cln = cloned.serialize();
        assertTrue(Arrays.equals(src, cln));
    }
}
