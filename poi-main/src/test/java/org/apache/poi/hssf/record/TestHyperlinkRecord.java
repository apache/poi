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

import java.util.Arrays;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.hssf.record.HyperlinkRecord.GUID;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.HexRead;
import org.apache.poi.util.LittleEndianByteArrayInputStream;
import org.apache.poi.util.LittleEndianByteArrayOutputStream;

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
    private static final byte[] data4 = {0x03, 0x00,
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

	private static final byte[] dataLinkToWorkbook = HexRead.readFromString("01 00 01 00 01 00 01 00 " +
			"D0 C9 EA 79 F9 BA CE 11 8C 82 00 AA 00 4B A9 0B " +
			"02 00 00 00 " +
			"1D 00 00 00 " + // options: LABEL | PLACE | FILE_OR_URL
			// label: "My Label"
			"09 00 00 00 " +
			"4D 00 79 00 20 00 4C 00 61 00 62 00 65 00 6C 00 00 00 " +
			"03 03 00 00 00 00 00 00 C0 00 00 00 00 00 00 46 " + // file GUID
			"00 00 " + // file options
			// shortFileName: "YEARFR~1.XLS"
			"0D 00 00 00 " +
			"59 45 41 52 46 52 7E 31 2E 58 4C 53 00 " +
			// FILE_TAIL - unknown byte sequence
			"FF FF AD DE 00 00 00 00 " +
			"00 00 00 00 00 00 00 00 " +
			"00 00 00 00 00 00 00 00 " +
			// field len, char data len
			"2E 00 00 00 " +
			"28 00 00 00 " +
			"03 00 " + // unknown ushort
			// _address: "yearfracExamples.xls"
			"79 00 65 00 61 00 72 00 66 00 72 00 61 00 63 00 " +
			"45 00 78 00 61 00 6D 00 70 00 6C 00 65 00 73 00 " +
			"2E 00 78 00 6C 00 73 00 " +
			// textMark: "Sheet1!B6"
			"0A 00 00 00 " +
			"53 00 68 00 65 00 65 00 74 00 31 00 21 00 42 00 " +
			"36 00 00 00");

	private static final byte[] dataTargetFrame = HexRead.readFromString("0E 00 0E 00 00 00 00 00 " +
			"D0 C9 EA 79 F9 BA CE 11  8C 82 00 AA 00 4B A9 0B " +
			"02 00 00 00 " +
			"83 00 00 00 " + // options: TARGET_FRAME | ABS | FILE_OR_URL
			// targetFrame: "_blank"
			"07 00 00 00 " +
			"5F 00 62 00 6C 00 61 00 6E 00 6B 00 00 00 " +
			// url GUID
			"E0 C9 EA 79 F9 BA CE 11 8C 82 00 AA 00 4B A9 0B " +
			// address: "http://www.regnow.com/softsell/nph-softsell.cgi?currency=USD&item=7924-37"
			"94 00 00 00 " +
			"68 00 74 00 74 00 70 00 3A 00 2F 00 2F 00 77 00 " +
			"77 00 77 00 2E 00 72 00 65 00 67 00 6E 00 6F 00 " +
			"77 00 2E 00 63 00 6F 00 6D 00 2F 00 73 00 6F 00 " +
			"66 00 74 00 73 00 65 00 6C 00 6C 00 2F 00 6E 00 " +
			"70 00 68 00 2D 00 73 00 6F 00 66 00 74 00 73 00 " +
			"65 00 6C 00 6C 00 2E 00 63 00 67 00 69 00 3F 00 " +
			"63 00 75 00 72 00 72 00 65 00 6E 00 63 00 79 00 " +
			"3D 00 55 00 53 00 44 00 26 00 69 00 74 00 65 00 " +
			"6D 00 3D 00 37 00 39 00 32 00 34 00 2D 00 33 00 " +
			"37 00 00 00");


	private static final byte[] dataUNC = HexRead.readFromString("01 00 01 00 01 00 01 00 " +
			"D0 C9 EA 79 F9 BA CE 11 8C 82 00 AA 00 4B A9 0B " +
			"02 00 00 00 " +
			"1F 01 00 00 " + // options: UNC_PATH | LABEL | TEXT_MARK | ABS | FILE_OR_URL
			"09 00 00 00 " + // label: "My Label"
			"4D 00 79 00 20 00 6C 00 61 00 62 00 65 00 6C 00 00 00 " +
			// note - no moniker GUID
			"27 00 00 00 " +  // "\\\\MyServer\\my-share\\myDir\\PRODNAME.xls"
			"5C 00 5C 00 4D 00 79 00 53 00 65 00 72 00 76 00 " +
			"65 00 72 00 5C 00 6D 00 79 00 2D 00 73 00 68 00 " +
			"61 00 72 00 65 00 5C 00 6D 00 79 00 44 00 69 00 " +
			"72 00 5C 00 50 00 52 00 4F 00 44 00 4E 00 41 00 " +
			"4D 00 45 00 2E 00 78 00 6C 00 73 00 00 00 " +

			"0C 00 00 00 " + // textMark: PRODNAME!C2
			"50 00 52 00 4F 00 44 00 4E 00 41 00 4D 00 45 00 21 00 " +
			"43 00 32 00 00 00");


    /**
     * From Bugzilla 47498
     */
    private static byte[] data_47498 = {
            0x02, 0x00, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, (byte)0xD0, (byte)0xC9,
            (byte)0xEA, 0x79, (byte)0xF9, (byte)0xBA, (byte)0xCE, 0x11, (byte)0x8C,
            (byte)0x82, 0x00, (byte)0xAA, 0x00, 0x4B, (byte)0xA9, 0x0B, 0x02, 0x00,
            0x00, 0x00, 0x15, 0x00, 0x00, 0x00, 0x04, 0x00, 0x00, 0x00, 0x50, 0x00,
            0x44, 0x00, 0x46, 0x00, 0x00, 0x00, (byte)0xE0, (byte)0xC9, (byte)0xEA,
            0x79, (byte)0xF9, (byte)0xBA, (byte)0xCE, (byte)0x11, (byte)0x8C, (byte)0x82,
            0x00, (byte)0xAA, 0x00, 0x4B, (byte)0xA9, 0x0B, 0x28, 0x00, 0x00, 0x00,
            0x74, 0x00, 0x65, 0x00, 0x73, 0x00, 0x74, 0x00, 0x66, 0x00, 0x6F, 0x00,
            0x6C, 0x00, 0x64, 0x00, 0x65, 0x00, 0x72, 0x00, 0x2F, 0x00, 0x74, 0x00,
            0x65, 0x00, 0x73, 0x00, 0x74, 0x00, 0x2E, 0x00, 0x50, 0x00, 0x44, 0x00,
            0x46, 0x00, 0x00, 0x00};


    private void confirmGUID(GUID expectedGuid, GUID actualGuid) {
		assertEquals(expectedGuid, actualGuid);
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
        assertEquals("link1.xls", link.getShortFilename());
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
        assertEquals("Sheet1!A1", link.getTextMark());
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
        link.setShortFilename("link1.xls");

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
        link.setTextMark("Sheet1!A1");

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

	public void testReserializeTargetFrame() {
		RecordInputStream in = TestcaseRecordInputStream.create(HyperlinkRecord.sid, dataTargetFrame);
		HyperlinkRecord hr = new HyperlinkRecord(in);
		byte[] ser = hr.serialize();
		TestcaseRecordInputStream.confirmRecordEncoding(HyperlinkRecord.sid, dataTargetFrame, ser);
	}


	public void testReserializeLinkToWorkbook() {

		RecordInputStream in = TestcaseRecordInputStream.create(HyperlinkRecord.sid, dataLinkToWorkbook);
		HyperlinkRecord hr = new HyperlinkRecord(in);
		byte[] ser = hr.serialize();
		TestcaseRecordInputStream.confirmRecordEncoding(HyperlinkRecord.sid, dataLinkToWorkbook, ser);
		if ("YEARFR~1.XLS".equals(hr.getAddress())) {
			throw new AssertionFailedError("Identified bug in reading workbook link");
		}
		assertEquals("yearfracExamples.xls", hr.getAddress());
	}

	public void testReserializeUNC() {

		RecordInputStream in = TestcaseRecordInputStream.create(HyperlinkRecord.sid, dataUNC);
		HyperlinkRecord hr = new HyperlinkRecord(in);
		byte[] ser = hr.serialize();
		TestcaseRecordInputStream.confirmRecordEncoding(HyperlinkRecord.sid, dataUNC, ser);
		try {
			hr.toString();
		} catch (NullPointerException e) {
			throw new AssertionFailedError("Identified bug with option URL and UNC set at same time");
		}
	}
	
	public void testGUID() {
		GUID g;
		g = GUID.parse("3F2504E0-4F89-11D3-9A0C-0305E82C3301");
		confirmGUID(g, 0x3F2504E0, 0x4F89, 0x11D3, 0x9A0C0305E82C3301L);
		assertEquals("3F2504E0-4F89-11D3-9A0C-0305E82C3301", g.formatAsString());

		g = GUID.parse("13579BDF-0246-8ACE-0123-456789ABCDEF");
		confirmGUID(g, 0x13579BDF, 0x0246, 0x8ACE, 0x0123456789ABCDEFL);
		assertEquals("13579BDF-0246-8ACE-0123-456789ABCDEF", g.formatAsString());

		byte[] buf = new byte[16];
		g.serialize(new LittleEndianByteArrayOutputStream(buf, 0));
		String expectedDump = "[DF, 9B, 57, 13, 46, 02, CE, 8A, 01, 23, 45, 67, 89, AB, CD, EF]";
		assertEquals(expectedDump, HexDump.toHex(buf));

		// STD Moniker
		g = createFromStreamDump("[D0, C9, EA, 79, F9, BA, CE, 11, 8C, 82, 00, AA, 00, 4B, A9, 0B]");
		assertEquals("79EAC9D0-BAF9-11CE-8C82-00AA004BA90B", g.formatAsString());
		// URL Moniker
		g = createFromStreamDump("[E0, C9, EA, 79, F9, BA, CE, 11, 8C, 82, 00, AA, 00, 4B, A9, 0B]");
		assertEquals("79EAC9E0-BAF9-11CE-8C82-00AA004BA90B", g.formatAsString());
		// File Moniker
		g = createFromStreamDump("[03, 03, 00, 00, 00, 00, 00, 00, C0, 00, 00, 00, 00, 00, 00, 46]");
		assertEquals("00000303-0000-0000-C000-000000000046", g.formatAsString());
	}

	private static GUID createFromStreamDump(String s) {
		return new GUID(new LittleEndianByteArrayInputStream(HexRead.readFromString(s)));
	}

	private void confirmGUID(GUID g, int d1, int d2, int d3, long d4) {
		assertEquals(new String(HexDump.intToHex(d1)), new String(HexDump.intToHex(g.getD1())));
		assertEquals(new String(HexDump.shortToHex(d2)), new String(HexDump.shortToHex(g.getD2())));
		assertEquals(new String(HexDump.shortToHex(d3)), new String(HexDump.shortToHex(g.getD3())));
		assertEquals(new String(HexDump.longToHex(d4)), new String(HexDump.longToHex(g.getD4())));
	}

    public void test47498(){
        RecordInputStream is = TestcaseRecordInputStream.create(HyperlinkRecord.sid, data_47498);
        HyperlinkRecord link = new HyperlinkRecord(is);
        assertEquals(2, link.getFirstRow());
        assertEquals(2, link.getLastRow());
        assertEquals(0, link.getFirstColumn());
        assertEquals(0, link.getLastColumn());
        confirmGUID(HyperlinkRecord.STD_MONIKER, link.getGuid());
        confirmGUID(HyperlinkRecord.URL_MONIKER, link.getMoniker());
        assertEquals(2, link.getLabelOptions());
        int opts = HyperlinkRecord.HLINK_URL | HyperlinkRecord.HLINK_LABEL;
        assertEquals(opts, link.getLinkOptions());
        assertEquals(0, link.getFileOptions());

        assertEquals("PDF", link.getLabel());
        assertEquals("testfolder/test.PDF", link.getAddress());

        byte[] ser = link.serialize();
        TestcaseRecordInputStream.confirmRecordEncoding(HyperlinkRecord.sid, data_47498, ser);
    }
}
