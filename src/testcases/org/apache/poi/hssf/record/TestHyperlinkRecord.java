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

/**
 * Test HyperlinkRecord
 *
 * @author Nick Burch
 * @author Yegor Kozlov
 */
public final class TestHyperlinkRecord extends TestCase {

    //link to http://www.lakings.com/
    private static final byte[] data1 = { 0x02, 0x00,    //First row of the hyperlink
                     0x02, 0x00,    //Last row of the hyperlink
                     0x00, 0x00,    //First column of the hyperlink
                     0x00, 0x00,    //Last column of the hyperlink

                     //16-byte GUID. Seems to be always the same. Does not depend on the hyperlink type
                     (byte)0xD0, (byte)0xC9, (byte)0xEA, 0x79, (byte)0xF9, (byte)0xBA, (byte)0xCE, 0x11,
                     (byte)0x8C, (byte)0x82, 0x00, (byte)0xAA, 0x00, 0x4B, (byte)0xA9, 0x0B,

                    0x02, 0x00, 0x00, 0x00, //integer, always 2

                    // flags. Define the type of the hyperlink:
                    // HyperlinkRecord.HLINK_URL | HyperlinkRecord.HLINK_ABS | HyperlinkRecord.HLINK_LABEL
                    0x17, 0x00, 0x00, 0x00,

                    0x08, 0x00, 0x00, 0x00, //length of the label including the trailing '\0'

                    //label:
                    0x4D, 0x00, 0x79, 0x00, 0x20, 0x00, 0x4C, 0x00, 0x69, 0x00, 0x6E, 0x00, 0x6B, 0x00, 0x00, 0x00,

                    //16-byte link moniker: HyperlinkRecord.URL_MONIKER
                    (byte)0xE0, (byte)0xC9, (byte)0xEA, 0x79, (byte)0xF9, (byte)0xBA, (byte)0xCE,  0x11,
                    (byte)0x8C, (byte)0x82, 0x00, (byte)0xAA, 0x00, 0x4B, (byte)0xA9, 0x0B,

                    //count of bytes in the address including the tail
                    0x48, 0x00, 0x00, 0x00, //integer

                    //the actual link, terminated by '\u0000'
                    0x68, 0x00, 0x74, 0x00, 0x74, 0x00, 0x70, 0x00, 0x3A, 0x00, 0x2F, 0x00,
                    0x2F, 0x00, 0x77, 0x00, 0x77, 0x00, 0x77, 0x00, 0x2E, 0x00, 0x6C, 0x00,
                    0x61, 0x00, 0x6B, 0x00, 0x69, 0x00, 0x6E, 0x00, 0x67, 0x00, 0x73, 0x00,
                    0x2E, 0x00, 0x63, 0x00, 0x6F, 0x00, 0x6D, 0x00, 0x2F, 0x00, 0x00, 0x00,

                    //standard 24-byte tail of a URL link. Seems to always be the same for all URL HLINKs
                    0x79, 0x58, (byte)0x81, (byte)0xF4, 0x3B, 0x1D, 0x7F, 0x48, (byte)0xAF, 0x2C,
                    (byte)0x82, 0x5D, (byte)0xC4, (byte)0x85, 0x27, 0x63, 0x00, 0x00, 0x00,
                    0x00, (byte)0xA5, (byte)0xAB, 0x00, 0x00};

    //link to a file in the current directory: link1.xls
    private static final byte[] data2 =  {0x00, 0x00,
                     0x00, 0x00,
                     0x00, 0x00,
                     0x00, 0x00,
                     //16-bit GUID. Seems to be always the same. Does not depend on the hyperlink type
                     (byte)0xD0, (byte)0xC9, (byte)0xEA, 0x79, (byte)0xF9, (byte)0xBA, (byte)0xCE, 0x11,
                     (byte)0x8C, (byte)0x82, 0x00, (byte)0xAA, 0x00, 0x4B, (byte)0xA9, 0x0B,

                     0x02, 0x00, 0x00, 0x00,    //integer, always 2

                     0x15, 0x00, 0x00, 0x00,    //options: HyperlinkRecord.HLINK_URL | HyperlinkRecord.HLINK_LABEL

                     0x05, 0x00, 0x00, 0x00,    //length of the label
                     //label
                     0x66, 0x00, 0x69, 0x00, 0x6C, 0x00, 0x65, 0x00, 0x00, 0x00,

                     //16-byte link moniker: HyperlinkRecord.FILE_MONIKER
                     0x03, 0x03, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte)0xC0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x46,

                     0x00, 0x00,    //level
                     0x0A, 0x00, 0x00, 0x00,    //length of the path )

                     //path to the file (plain ISO-8859 bytes, NOT UTF-16LE!)
                     0x6C, 0x69, 0x6E, 0x6B, 0x31, 0x2E, 0x78, 0x6C, 0x73, 0x00,

                     //standard 24-byte tail of a file link
                     (byte)0xFF, (byte)0xFF, (byte)0xAD, (byte)0xDE, 0x00, 0x00, 0x00, 0x00,
                     0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                     0x00, 0x00, 0x00, 0x00,
                     
                     0x00, 0x00, 0x00, 0x00, // length of address link field
                     };

    // mailto:ebgans@mail.ru?subject=Hello,%20Ebgans!
    private static final byte[] data3 = {0x01, 0x00,
                    0x01, 0x00,
                    0x00, 0x00,
                    0x00, 0x00,

                    //16-bit GUID. Seems to be always the same. Does not depend on the hyperlink type
                    (byte)0xD0, (byte)0xC9, (byte)0xEA, 0x79, (byte)0xF9, (byte)0xBA, (byte)0xCE, 0x11,
                    (byte)0x8C, (byte)0x82, 0x00, (byte)0xAA, 0x00, 0x4B, (byte)0xA9, 0x0B,

                    0x02, 0x00, 0x00, 0x00, //integer, always 2

                    0x17, 0x00, 0x00, 0x00,  //options: HyperlinkRecord.HLINK_URL | HyperlinkRecord.HLINK_ABS | HyperlinkRecord.HLINK_LABEL

                    0x06, 0x00, 0x00, 0x00,     //length of the label
                    0x65, 0x00, 0x6D, 0x00, 0x61, 0x00, 0x69, 0x00, 0x6C, 0x00, 0x00, 0x00, //label

                    //16-byte link moniker: HyperlinkRecord.URL_MONIKER
                    (byte)0xE0, (byte)0xC9, (byte)0xEA, 0x79, (byte)0xF9, (byte)0xBA, (byte)0xCE, 0x11,
                    (byte)0x8C, (byte)0x82, 0x00, (byte)0xAA, 0x00, 0x4B, (byte)0xA9, 0x0B,

                    //length of the address including the tail.
                    0x76, 0x00, 0x00, 0x00,

                    //the address is terminated by '\u0000'
                    0x6D, 0x00, 0x61, 0x00, 0x69, 0x00, 0x6C, 0x00, 0x74, 0x00, 0x6F, 0x00,
                    0x3A, 0x00, 0x65, 0x00, 0x62, 0x00, 0x67, 0x00, 0x61, 0x00, 0x6E, 0x00,
                    0x73, 0x00, 0x40, 0x00, 0x6D, 0x00, 0x61, 0x00, 0x69, 0x00, 0x6C, 0x00,
                    0x2E, 0x00, 0x72, 0x00, 0x75, 0x00, 0x3F, 0x00, 0x73, 0x00, 0x75, 0x00,
                    0x62, 0x00, 0x6A, 0x00, 0x65, 0x00, 0x63, 0x00, 0x74, 0x00, 0x3D, 0x00,
                    0x48, 0x00, 0x65, 0x00, 0x6C, 0x00, 0x6C, 0x00, 0x6F, 0x00, 0x2C, 0x00,
                    0x25, 0x00, 0x32, 0x00, 0x30, 0x00, 0x45, 0x00, 0x62, 0x00, 0x67, 0x00,
                    0x61, 0x00, 0x6E, 0x00, 0x73, 0x00, 0x21, 0x00, 0x00, 0x00,

                    //standard 24-byte tail of a URL link
                    0x79, 0x58, (byte)0x81, (byte)0xF4, 0x3B, 0x1D, 0x7F, 0x48, (byte)0xAF, (byte)0x2C,
                    (byte)0x82, 0x5D, (byte)0xC4, (byte)0x85, 0x27, 0x63, 0x00, 0x00, 0x00,
                    0x00, (byte)0xA5, (byte)0xAB, 0x00, 0x00
    };

    //link to a place in worksheet: Sheet1!A1
    byte[] data4 = {0x03, 0x00,
                    0x03, 0x00,
                    0x00, 0x00,
                    0x00, 0x00,

                    //16-bit GUID. Seems to be always the same. Does not depend on the hyperlink type
                    (byte)0xD0, (byte)0xC9, (byte)0xEA, 0x79, (byte)0xF9, (byte)0xBA, (byte)0xCE, 0x11,
                    (byte)0x8C, (byte)0x82, 0x00, (byte)0xAA, 0x00, 0x4B, (byte)0xA9, 0x0B,

                    0x02, 0x00, 0x00, 0x00, //integer, always 2

                    0x1C, 0x00, 0x00, 0x00, //flags: HyperlinkRecord.HLINK_LABEL | HyperlinkRecord.HLINK_PLACE

                    0x06, 0x00, 0x00, 0x00, //length of the label

                    0x70, 0x00, 0x6C, 0x00, 0x61, 0x00, 0x63, 0x00, 0x65, 0x00, 0x00, 0x00, //label

                    0x0A, 0x00, 0x00, 0x00, //length of the document link including trailing zero

                    //link: Sheet1!A1
                    0x53, 0x00, 0x68, 0x00, 0x65, 0x00, 0x65, 0x00, 0x74, 0x00, 0x31, 0x00, 0x21,
                    0x00, 0x41, 0x00, 0x31, 0x00, 0x00, 0x00};

    private void confirmGUID(byte[] expectedGuid, byte[] actualGuid) {
		assertTrue(Arrays.equals(expectedGuid, actualGuid));
	}
    public void testReadURLLink(){
        RecordInputStream is = TestcaseRecordInputStream.create(HyperlinkRecord.sid, data1);
        HyperlinkRecord link = new HyperlinkRecord(is);
        assertEquals(2, link.getFirstRow());
        assertEquals(2, link.getLastRow());
        assertEquals(0, link.getFirstColumn());
        assertEquals(0, link.getLastColumn());
        confirmGUID(HyperlinkRecord.STD_MONIKER, link.getGuid());
        confirmGUID(HyperlinkRecord.URL_MONIKER, link.getMoniker());
        assertEquals(2, link.getLabelOptions());
        int opts = HyperlinkRecord.HLINK_URL | HyperlinkRecord.HLINK_ABS | HyperlinkRecord.HLINK_LABEL;
        assertEquals(0x17, opts);
        assertEquals(opts, link.getLinkOptions());
        assertEquals(0, link.getFileOptions());

        assertEquals("My Link", link.getLabel());
        assertEquals("http://www.lakings.com/", link.getAddress());
    }

    public void testReadFileLink(){
        RecordInputStream is = TestcaseRecordInputStream.create(HyperlinkRecord.sid, data2);
        HyperlinkRecord link = new HyperlinkRecord(is);
        assertEquals(0, link.getFirstRow());
        assertEquals(0, link.getLastRow());
        assertEquals(0, link.getFirstColumn());
        assertEquals(0, link.getLastColumn());
        confirmGUID(HyperlinkRecord.STD_MONIKER, link.getGuid());
        confirmGUID(HyperlinkRecord.FILE_MONIKER, link.getMoniker());
        assertEquals(2, link.getLabelOptions());
        int opts = HyperlinkRecord.HLINK_URL | HyperlinkRecord.HLINK_LABEL;
        assertEquals(0x15, opts);
        assertEquals(opts, link.getLinkOptions());

        assertEquals("file", link.getLabel());
        assertEquals("link1.xls", link.getAddress());
    }

    public void testReadEmailLink(){
        RecordInputStream is = TestcaseRecordInputStream.create(HyperlinkRecord.sid, data3);
        HyperlinkRecord link = new HyperlinkRecord(is);
        assertEquals(1, link.getFirstRow());
        assertEquals(1, link.getLastRow());
        assertEquals(0, link.getFirstColumn());
        assertEquals(0, link.getLastColumn());
        confirmGUID(HyperlinkRecord.STD_MONIKER, link.getGuid());
        confirmGUID(HyperlinkRecord.URL_MONIKER, link.getMoniker());
        assertEquals(2, link.getLabelOptions());
        int opts = HyperlinkRecord.HLINK_URL | HyperlinkRecord.HLINK_ABS | HyperlinkRecord.HLINK_LABEL;
        assertEquals(0x17, opts);
        assertEquals(opts, link.getLinkOptions());

        assertEquals("email", link.getLabel());
        assertEquals("mailto:ebgans@mail.ru?subject=Hello,%20Ebgans!", link.getAddress());
    }

    public void testReadDocumentLink(){
        RecordInputStream is = TestcaseRecordInputStream.create(HyperlinkRecord.sid, data4);
        HyperlinkRecord link = new HyperlinkRecord(is);
        assertEquals(3, link.getFirstRow());
        assertEquals(3, link.getLastRow());
        assertEquals(0, link.getFirstColumn());
        assertEquals(0, link.getLastColumn());
        confirmGUID(HyperlinkRecord.STD_MONIKER, link.getGuid());
        assertEquals(2, link.getLabelOptions());
        int opts = HyperlinkRecord.HLINK_LABEL | HyperlinkRecord.HLINK_PLACE;
        assertEquals(0x1C, opts);
        assertEquals(opts, link.getLinkOptions());

        assertEquals("place", link.getLabel());
        assertEquals("Sheet1!A1", link.getAddress());
    }

    private void serialize(byte[] data){
        RecordInputStream is = TestcaseRecordInputStream.create(HyperlinkRecord.sid, data);
        HyperlinkRecord link = new HyperlinkRecord(is);
        byte[] bytes1 = link.serialize();
        is = TestcaseRecordInputStream.create(bytes1);
        link = new HyperlinkRecord(is);
        byte[] bytes2 = link.serialize();
        assertEquals(bytes1.length, bytes2.length);
        assertTrue(Arrays.equals(bytes1, bytes2));
    }

    public void testSerialize(){
        serialize(data1);
        serialize(data2);
        serialize(data3);
        serialize(data4);
    }

    public void testCreateURLRecord() {
        HyperlinkRecord link = new HyperlinkRecord();
        link.newUrlLink();
        link.setFirstRow((short)2);
        link.setLastRow((short)2);
        link.setLabel("My Link");
        link.setAddress("http://www.lakings.com/");

        byte[] tmp = link.serialize();
        byte[] ser = new byte[tmp.length-4];
        System.arraycopy(tmp, 4, ser, 0, ser.length);
        assertEquals(data1.length, ser.length);
        assertTrue(Arrays.equals(data1, ser));
    }

    public void testCreateFileRecord() {
        HyperlinkRecord link = new HyperlinkRecord();
        link.newFileLink();
        link.setFirstRow((short)0);
        link.setLastRow((short)0);
        link.setLabel("file");
        link.setAddress("link1.xls");

        byte[] tmp = link.serialize();
        byte[] ser = new byte[tmp.length-4];
        System.arraycopy(tmp, 4, ser, 0, ser.length);
        assertEquals(data2.length, ser.length);
        assertTrue(Arrays.equals(data2, ser));
    }

    public void testCreateDocumentRecord() {
        HyperlinkRecord link = new HyperlinkRecord();
        link.newDocumentLink();
        link.setFirstRow((short)3);
        link.setLastRow((short)3);
        link.setLabel("place");
        link.setAddress("Sheet1!A1");

        byte[] tmp = link.serialize();
        byte[] ser = new byte[tmp.length-4];
        System.arraycopy(tmp, 4, ser, 0, ser.length);
        assertEquals(data4.length, ser.length);
        assertTrue(Arrays.equals(data4, ser));
    }

    public void testCreateEmailtRecord() {
        HyperlinkRecord link = new HyperlinkRecord();
        link.newUrlLink();
        link.setFirstRow((short)1);
        link.setLastRow((short)1);
        link.setLabel("email");
        link.setAddress("mailto:ebgans@mail.ru?subject=Hello,%20Ebgans!");

        byte[] tmp = link.serialize();
        byte[] ser = new byte[tmp.length-4];
        System.arraycopy(tmp, 4, ser, 0, ser.length);
        assertEquals(data3.length, ser.length);
        assertTrue(Arrays.equals(data3, ser));
    }

    public void testClone() {
        byte[][] data = {data1, data2, data3, data4};
        for (int i = 0; i < data.length; i++) {
            RecordInputStream is = TestcaseRecordInputStream.create(HyperlinkRecord.sid, data[i]);
            HyperlinkRecord link = new HyperlinkRecord(is);
            HyperlinkRecord clone = (HyperlinkRecord)link.clone();
            assertTrue(Arrays.equals(link.serialize(), clone.serialize()));
        }

    }
}
