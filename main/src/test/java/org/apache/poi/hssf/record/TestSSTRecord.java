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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.record.common.UnicodeString;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.storage.RawDataUtil;
import org.apache.poi.util.HexRead;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LocaleUtil;
import org.junit.jupiter.api.Test;

final class TestSSTRecord {

    /**
     * decodes hexdump files and concatenates the results
     * @param hexDumpFileNames names of sample files in the hssf test data directory
     */
    private static byte[] concatHexDumps(String... hexDumpFileNames) throws IOException {
        int nFiles = hexDumpFileNames.length;
        ByteArrayOutputStream baos = new ByteArrayOutputStream(nFiles * 8228);
        for (String sampleFileName : hexDumpFileNames) {
            try (InputStream is = HSSFTestDataSamples.openSampleFileStream(sampleFileName)) {
                BufferedReader br = new BufferedReader(new InputStreamReader(is, LocaleUtil.CHARSET_1252));

                while (true) {
                    String line = br.readLine();
                    if (line == null) {
                        break;
                    }
                    baos.write(HexRead.readFromString(line));
                }
            }
        }

        return baos.toByteArray();
    }

    /**
     * @param rawData serialization of one {@link SSTRecord} and zero or more {@link ContinueRecord}s
     */
    private static SSTRecord createSSTFromRawData(byte[] rawData) {
        RecordInputStream in = new RecordInputStream(new ByteArrayInputStream(rawData));
        in.nextRecord();
        SSTRecord result = new SSTRecord(in);
        assertEquals(0, in.remaining());
        assertFalse(in.hasNextRecord());
        return result;
    }

    /**
     * SST is often split over several {@link ContinueRecord}s
     */
    @Test
    void testContinuedRecord() throws IOException {
        byte[] origData;
        SSTRecord record;
        byte[] ser_output;

        origData = concatHexDumps("BigSSTRecord", "BigSSTRecordCR");
        record = createSSTFromRawData(origData);
        assertEquals( 1464, record.getNumStrings() );
        assertEquals( 688, record.getNumUniqueStrings() );
        assertEquals( 688, record.countStrings() );
        ser_output = record.serialize();
        assertArrayEquals(origData, ser_output);

        // testing based on new bug report
        origData = concatHexDumps("BigSSTRecord2", "BigSSTRecord2CR1", "BigSSTRecord2CR2", "BigSSTRecord2CR3",
                "BigSSTRecord2CR4", "BigSSTRecord2CR5", "BigSSTRecord2CR6", "BigSSTRecord2CR7");
        record = createSSTFromRawData(origData);


        assertEquals( 158642, record.getNumStrings() );
        assertEquals( 5249, record.getNumUniqueStrings() );
        assertEquals( 5249, record.countStrings() );
        ser_output = record.serialize();
//        if (false) { // set true to observe make sure areSameSSTs() is working
//            ser_output[11000] = 'X';
//        }

        SSTRecord rec2 = createSSTFromRawData(ser_output);
        assertRecordEquals(record, rec2);

//        if (false) {
//            // TODO - trivial differences in ContinueRecord break locations
//            // Sample data should be checked against what most recent Excel version produces.
//            // maybe tweaks are required in ContinuableRecordOutput
//            assertArrayEquals(origData, ser_output);
//        }
    }

    /**
     * Test capability of handling mondo big strings
     */
    @Test
    void testHugeStrings() {
        SSTRecord record = new SSTRecord();
        byte[][] bstrings =
                {
                    new byte[9000], new byte[7433], new byte[9002],
                    new byte[16998]
                };
        UnicodeString[] strings = new UnicodeString[bstrings.length];
        int total_length = 0;

        for ( int k = 0; k < bstrings.length; k++ )
        {
            Arrays.fill( bstrings[k], (byte) ( 'a' + k ) );
            strings[k] = new UnicodeString( new String(bstrings[k], LocaleUtil.CHARSET_1252) );
            record.addString( strings[k] );
            total_length += 3 + bstrings[k].length;
        }

        // add overhead of SST record
        total_length += 8;

        // add overhead of broken strings
        total_length += 4;

        // add overhead of six records
        total_length += ( 6 * 4 );
        byte[] content = new byte[record.getRecordSize()];

        record.serialize( 0, content );
        assertEquals( total_length, content.length );

        //Deserialize the record.
        RecordInputStream recStream = new RecordInputStream(new ByteArrayInputStream(content));
        recStream.nextRecord();
        record = new SSTRecord(recStream);

        assertEquals( strings.length, record.getNumStrings() );
        assertEquals( strings.length, record.getNumUniqueStrings() );
        assertEquals( strings.length, record.countStrings() );
        for ( int k = 0; k < strings.length; k++ )
        {
            assertEquals( strings[k], record.getString( k ) );
        }
        record = new SSTRecord();
        bstrings[1] = new byte[bstrings[1].length - 1];
        for ( int k = 0; k < bstrings.length; k++ )
        {
            if ( ( bstrings[k].length % 2 ) == 1 )
            {
                Arrays.fill( bstrings[k], (byte) ( 'a' + k ) );
                strings[k] = new UnicodeString( new String(bstrings[k], LocaleUtil.CHARSET_1252) );
            }
            else
            {
                char[] data = new char[bstrings[k].length / 2];

                Arrays.fill( data, (char) ( '\u2122' + k ) );
                strings[k] = new UnicodeString(new String( data ));
            }
            record.addString( strings[k] );
        }
        content = new byte[record.getRecordSize()];
        record.serialize( 0, content );
        total_length--;
        assertEquals( total_length, content.length );

        recStream = new RecordInputStream(new ByteArrayInputStream(content));
        recStream.nextRecord();
        record = new SSTRecord(recStream);

        assertEquals( strings.length, record.getNumStrings() );
        assertEquals( strings.length, record.getNumUniqueStrings() );
        assertEquals( strings.length, record.countStrings() );
        for ( int k = 0; k < strings.length; k++ )
        {
            assertEquals( strings[k], record.getString( k ) );
        }
    }

    /**
     * test SSTRecord boundary conditions
     */
    @Test
    void testSSTRecordBug() {
        // create an SSTRecord and write a certain pattern of strings
        // to it ... then serialize it and verify the content
        SSTRecord record = new SSTRecord();

        // the record will start with two integers, then this string
        // ... that will eat up 16 of the 8224 bytes that the record
        // can hold
        record.addString( new UnicodeString("Hello") );

        // now we have an additional 8208 bytes, which is an exact
        // multiple of 16 bytes
        long testvalue = 1000000000000L;

        for ( int k = 0; k < 2000; k++ )
        {
            record.addString( new UnicodeString(String.valueOf( testvalue++ )) );
        }
        byte[] content = new byte[record.getRecordSize()];

        record.serialize( 0, content );
        assertEquals(8224, LittleEndian.getShort(content, 2));
        assertEquals(ContinueRecord.sid, LittleEndian.getShort(content, 8228));
        assertEquals(8224, LittleEndian.getShort(content, 8228+2));
        assertEquals( (byte) 13, content[4 + 8228] );
        assertEquals(ContinueRecord.sid, LittleEndian.getShort(content, 2*8228));
        assertEquals(8224, LittleEndian.getShort(content, 8228*2+2));
        assertEquals( (byte) 13, content[4 + 8228 * 2] );
        assertEquals(ContinueRecord.sid, LittleEndian.getShort(content, 3*8228));
        assertEquals( (byte) 13, content[4 + 8228 * 3] );
    }

    /**
     * test simple addString
     */
    @Test
    void testSimpleAddString() {
        SSTRecord record = new SSTRecord();
        UnicodeString s1 = new UnicodeString("Hello world");

        // \u2122 is the encoding of the trademark symbol ...
        UnicodeString s2 = new UnicodeString("Hello world\u2122");

        assertEquals( 0, record.addString( s1 ) );
        assertEquals( s1, record.getString( 0 ) );
        assertEquals( 1, record.countStrings() );
        assertEquals( 1, record.getNumStrings() );
        assertEquals( 1, record.getNumUniqueStrings() );
        assertEquals( 0, record.addString( s1 ) );
        assertEquals( s1, record.getString( 0 ) );
        assertEquals( 1, record.countStrings() );
        assertEquals( 2, record.getNumStrings() );
        assertEquals( 1, record.getNumUniqueStrings() );
        assertEquals( 1, record.addString( s2 ) );
        assertEquals( s2, record.getString( 1 ) );
        assertEquals( 2, record.countStrings() );
        assertEquals( 3, record.getNumStrings() );
        assertEquals( 2, record.getNumUniqueStrings() );
        Iterator<UnicodeString> iter = record.getStrings();

        while ( iter.hasNext() ) {
            UnicodeString ucs = iter.next();

            if ( ucs.equals( s1 ) )
            {
                assertEquals( (byte) 0, ucs.getOptionFlags() );
            }
            else if ( ucs.equals( s2 ) )
            {
                assertEquals( (byte) 1, ucs.getOptionFlags() );
            }
            else
            {
                fail( "cannot match string: " + ucs.getString() );
            }
        }
    }

    /**
     * test simple constructor
     */
    @Test
    void testSimpleConstructor() {
        SSTRecord record = new SSTRecord();

        assertEquals( 0, record.getNumStrings() );
        assertEquals( 0, record.getNumUniqueStrings() );
        assertEquals( 0, record.countStrings() );
        byte[] output = record.serialize();
        byte[] expected = {
            (byte) record.getSid(), (byte) ( record.getSid() >> 8 ),
            (byte) 8, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
            (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0
        };

        assertArrayEquals(expected, output);
    }

    /**
     * Tests that workbooks with rich text that duplicates a non rich text cell can be read and written.
     */
    @Test
    void testReadWriteDuplicatedRichText1() throws Exception {
        try (HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("duprich1.xls")) {
            HSSFSheet sheet = wb.getSheetAt(1);
            assertEquals("01/05 (Wed)", sheet.getRow(0).getCell(8).getStringCellValue());
            assertEquals("01/05 (Wed)", sheet.getRow(1).getCell(8).getStringCellValue());

            HSSFTestDataSamples.writeOutAndReadBack(wb).close();
        }

            // test the second file.
        try (HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("duprich2.xls")) {
            HSSFSheet sheet = wb.getSheetAt(0);
            int row = 0;
            assertEquals("Testing", sheet.getRow(row++).getCell(0).getStringCellValue());
            assertEquals("rich", sheet.getRow(row++).getCell(0).getStringCellValue());
            assertEquals("text", sheet.getRow(row++).getCell(0).getStringCellValue());
            assertEquals("strings", sheet.getRow(row++).getCell(0).getStringCellValue());
            assertEquals("Testing", sheet.getRow(row++).getCell(0).getStringCellValue());
            assertEquals("Testing", sheet.getRow(row).getCell(0).getStringCellValue());

            HSSFTestDataSamples.writeOutAndReadBack(wb).close();
        }
    }


    /**
     * deep comparison of two SST records
     */
    private static void assertRecordEquals(SSTRecord expected, SSTRecord actual){
        assertEquals(expected.getNumStrings(), actual.getNumStrings(), "number of strings");
        assertEquals(expected.getNumUniqueStrings(), actual.getNumUniqueStrings(), "number of unique strings");
        assertEquals(expected.countStrings(), actual.countStrings(), "count of strings");

        Iterator<UnicodeString> iterA = expected.getStrings();
        Iterator<UnicodeString> iterB = actual.getStrings();
        while (iterA.hasNext()) {
            assertEquals(iterA.next(), iterB.next());
        }
    }

    @Test
    void test50779_1() throws IOException {
        byte[] bytes = RawDataUtil.decompress("H4sIAAAAAAAAAL3aO2gVQRQG4HO5XkEZ8AESi4DBKoXFP+8IVioSvahcLxKC" +
          "iRIRc9FEUZExCL4IYnyQqIi9SEATFUHRTkSQaBFsrEQLG8EiFgGrCM6uIGiff5c5e3ZY9l8W9mt2FqSjoyEixTBSW5kPm7EV29CNHa" +
          "hjJ3ajgSb2oge92Id+HMAADuEwWjiCoxjCMI7hOE7iNM7gLM7hPC7gIi5hFJdxBWO4imu4jhsYxy3cBqTYtVRESU28/NnqMFJZ/Fgj" +
          "lBjLiXGcGM+JCZyYyInp4sRsZMTo8sshxGhODEUBzVFAcxTQHAU0RwHNUUBzFNAcBQxHAcNRwHAUMBwFDEcBw1HAcBQwHAUMRwHDUc" +
          "ByFLAcBSxHActRwHIUsBwFLEcBy1HAchSwHAUcRwHHUcBxFHAcBRxHAcdRwHEUcBwFHEcBx1HAcxTwHAU8RwHPUcBzFPAcBTxHAc9R" +
          "wHMU8BwFAkeBwFEgcBQIHAUCR4HAUSBwFAgcBQJHgcBRIHIUiBwFIkeByFEgchSIHAUiR4HIUSBmBdZJ7aWItDXTcHNiz925lkoP+u" +
          "oHVRof+dmnUrXVOajSQs/YKZVODE7v+jWxpbl9VKX9I929n/tVSndmb6pUkWfl//Tl5ZN/whtM4T7eYRL38BBf8R1PMZ9nfuBt2X3E" +
          "E7zAl7KfwWs8Lrvn+YpXf2cn8Qjfcp3ZJI1KvuZDOT+F95jO9yn6opstu+IvflWW5lEVVR5XybJc2/JZVdplRa7rZXWunbIm1w2yVp" +
          "bkN9yee9Kyg5gp/HfZAW3FQ1ce/694+A14Ha5/eSEAAA==");

        RecordInputStream in = TestcaseRecordInputStream.create(bytes);
        assertEquals(SSTRecord.sid, in.getSid());
        SSTRecord src = new SSTRecord(in);
        assertEquals(81, src.getNumStrings());

        byte[] serialized = src.serialize();

        in = TestcaseRecordInputStream.create(serialized);
        assertEquals(SSTRecord.sid, in.getSid());
        SSTRecord dst = new SSTRecord(in);
        assertEquals(81, dst.getNumStrings());

        assertRecordEquals(src, dst);
    }

    @Test
    void test50779_2() throws IOException {
        byte[] bytes = RawDataUtil.decompress("H4sIAAAAAAAAAL3Uu2sVQRjG4e9wOKKw4gUkKSyCVQqLd2d2ZhOwiiLqQSUe" +
          "JIgRUcQc1ERUZBXBG0GMFxIiaC8S8I6FWIoIop2NlWBhI1hoIVhFcM6Cgn+Avx3m2+HbXd5hYJ9FGxgYNbPedNYY0SZt1hZtU1vbtV" +
          "Oj6mi3xrRHe7VP+3VAh3RYXR3RUR3TpKZ0XCd1Wmd0Tud1QRd1SZc1rSu6qhld03Xd0E3Nal63JOuNnIlxTIxnYgomJjAxkYkpmZgh" +
          "JmaYiMnrPweIQRTIGQVyRoGcUSBnFMgZBXJGgZxRIGcUcIwCjlHAMQo4RgHHKOAYBRyjgGMUcIwCjlHAMwp4RgHPKOAZBTyjgGcU8I" +
          "wCnlHAMwp4RoGCUaBgFCgYBQpGgYJRoGAUKBgFCkaBglGgYBQIjAKBUSAwCgRGgcAoEBgFAqNAYBQIjAKBUSAyCkRGgcgoEBkFIqNA" +
          "ZBSIjAKRUSAyCkRGgZJRoGQUKBkFSkaBklGgZBQoGQVKRoEyKdBvrdtm1tepJjtzu+5862bV/fH2wayaPftzPKua3cGJrFocmzmVVS" +
          "cmHu34Nbexs3U6qxo2b6105kttfRof9VoPdU/vtKC7eqDP+qpn+pE63/WmXn3QU73Qp3r9Vq/0pF49T2+8/Ntd0GN9SbX3/H3dSxuz" +
          "pi1Js2lZfV9ly1Lt22DLG6nTtLW2ItV1tjrVQVuTqrPWyvTZ/z+7YettoXcIfy4oeijNf6Pb+g0SIvVzNSEAAA==");

        RecordInputStream in = TestcaseRecordInputStream.create(bytes);
        assertEquals(SSTRecord.sid, in.getSid());
        SSTRecord src = new SSTRecord(in);
        assertEquals(81, src.getNumStrings());

        byte[] serialized = src.serialize();

        in = TestcaseRecordInputStream.create(serialized);
        assertEquals(SSTRecord.sid, in.getSid());
        SSTRecord dst = new SSTRecord(in);
        assertEquals(81, dst.getNumStrings());

        assertRecordEquals(src, dst);
    }

    @Test
    void test57456() {
        byte[] bytes = HexRead.readFromString("FC, 00, 08, 00, 00, 00, 00, 00, E1, 06, 00, 00");
        RecordInputStream in = TestcaseRecordInputStream.create(bytes);
        assertEquals(SSTRecord.sid, in.getSid());
        SSTRecord src = new SSTRecord(in);
        assertEquals(0, src.getNumStrings());
        assertEquals(0, src.getNumUniqueStrings());

    }
}
