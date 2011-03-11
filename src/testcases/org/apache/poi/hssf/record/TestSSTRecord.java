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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Iterator;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.record.common.UnicodeString;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.util.HexRead;
import org.apache.poi.util.LittleEndian;

/**
 * @author Marc Johnson (mjohnson at apache dot org)
 * @author Glen Stampoultzis (glens at apache.org)
 */
public final class TestSSTRecord extends TestCase {

    /**
     * decodes hexdump files and concatenates the results
     * @param hexDumpFileNames names of sample files in the hssf test data directory
     */
    private static byte[] concatHexDumps(String... hexDumpFileNames) {
        int nFiles = hexDumpFileNames.length;
        ByteArrayOutputStream baos = new ByteArrayOutputStream(nFiles * 8228);
        for (int i = 0; i < nFiles; i++) {
            String sampleFileName = hexDumpFileNames[i];
            InputStream is = HSSFTestDataSamples.openSampleFileStream(sampleFileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            try {
                while (true) {
                    String line = br.readLine();
                    if (line == null) {
                        break;
                    }
                    baos.write(HexRead.readFromString(line));
                }
                is.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
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
        assertTrue(!in.hasNextRecord());
        return result;
    }

    /**
     * SST is often split over several {@link ContinueRecord}s
     */
    public void testContinuedRecord() {
        byte[] origData;
        SSTRecord record;
        byte[] ser_output;

        origData = concatHexDumps("BigSSTRecord", "BigSSTRecordCR");
        record = createSSTFromRawData(origData);
        assertEquals( 1464, record.getNumStrings() );
        assertEquals( 688, record.getNumUniqueStrings() );
        assertEquals( 688, record.countStrings() );
        ser_output = record.serialize();
        assertTrue(Arrays.equals(origData, ser_output));

        // testing based on new bug report
        origData = concatHexDumps("BigSSTRecord2", "BigSSTRecord2CR1", "BigSSTRecord2CR2", "BigSSTRecord2CR3",
                "BigSSTRecord2CR4", "BigSSTRecord2CR5", "BigSSTRecord2CR6", "BigSSTRecord2CR7");
        record = createSSTFromRawData(origData);


        assertEquals( 158642, record.getNumStrings() );
        assertEquals( 5249, record.getNumUniqueStrings() );
        assertEquals( 5249, record.countStrings() );
        ser_output = record.serialize();
        if (false) { // set true to observe make sure areSameSSTs() is working
            ser_output[11000] = 'X';
        }

        SSTRecord rec2 = createSSTFromRawData(ser_output);
        if (!areSameSSTs(record, rec2)) {
            throw new AssertionFailedError("large SST re-serialized incorrectly");
        }
        if (false) {
            // TODO - trivial differences in ContinueRecord break locations
            // Sample data should be checked against what most recent Excel version produces.
            // maybe tweaks are required in ContinuableRecordOutput
            assertTrue(Arrays.equals(origData, ser_output));
        }
    }

    private boolean areSameSSTs(SSTRecord a, SSTRecord b) {

        if (a.getNumStrings() != b.getNumStrings()) {
            return false;
        }
        int nElems = a.getNumUniqueStrings();
        if (nElems != b.getNumUniqueStrings()) {
            return false;
        }
        for(int i=0; i<nElems; i++) {
            if (!a.getString(i).equals(b.getString(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Test capability of handling mondo big strings
     *
     * @exception IOException
     */

    public void testHugeStrings() {
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
            strings[k] = new UnicodeString( new String(bstrings[k]) );
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
                strings[k] = new UnicodeString( new String(bstrings[k]) );
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
    public void testSSTRecordBug() {
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
    public void testSimpleAddString() {
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
        Iterator iter = record.getStrings();

        while ( iter.hasNext() )
        {
            UnicodeString ucs = (UnicodeString) iter.next();

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
    public void testSimpleConstructor() {
        SSTRecord record = new SSTRecord();

        assertEquals( 0, record.getNumStrings() );
        assertEquals( 0, record.getNumUniqueStrings() );
        assertEquals( 0, record.countStrings() );
        byte[] output = record.serialize();
        byte[] expected =
                {
                    (byte) record.getSid(), (byte) ( record.getSid() >> 8 ),
                    (byte) 8, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
                    (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0
                };

        assertEquals( expected.length, output.length );
        for ( int k = 0; k < expected.length; k++ )
        {
            assertEquals( String.valueOf( k ), expected[k], output[k] );
        }
    }

    /**
     * Tests that workbooks with rich text that duplicates a non rich text cell can be read and written.
     */
    public void testReadWriteDuplicatedRichText1() {
        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("duprich1.xls");
        HSSFSheet sheet = wb.getSheetAt( 1 );
        assertEquals( "01/05 (Wed)", sheet.getRow( 0 ).getCell(8 ).getStringCellValue() );
        assertEquals( "01/05 (Wed)", sheet.getRow( 1 ).getCell(8 ).getStringCellValue() );

        HSSFTestDataSamples.writeOutAndReadBack(wb);

        // test the second file.
        wb = HSSFTestDataSamples.openSampleWorkbook("duprich2.xls");
        sheet = wb.getSheetAt( 0 );
        int row = 0;
        assertEquals( "Testing", sheet.getRow( row++ ).getCell(0 ).getStringCellValue() );
        assertEquals( "rich", sheet.getRow( row++ ).getCell(0 ).getStringCellValue() );
        assertEquals( "text", sheet.getRow( row++ ).getCell(0 ).getStringCellValue() );
        assertEquals( "strings", sheet.getRow( row++ ).getCell(0 ).getStringCellValue() );
        assertEquals( "Testing", sheet.getRow( row++ ).getCell(0 ).getStringCellValue() );
        assertEquals( "Testing", sheet.getRow( row++ ).getCell(0 ).getStringCellValue() );

        HSSFTestDataSamples.writeOutAndReadBack(wb);
    }

    /**
     * hex dump from UnicodeStringFailCase1.xls atatched to Bugzilla 50779
     */
    private static final String data_50779_1 =
            //Offset=0x00000612(1554) recno=71 sid=0x00FC size=0x2020(8224)
            "      FC 00 20 20 51 00 00 00 51 00 00 00 32 00" +
            "05 10 00 00 00 42 30 44 30 46 30 48 30 4A 30 4B" +
            "30 4D 30 4F 30 51 30 53 30 55 30 57 30 59 30 5B" +
            "30 5D 30 5F 30 61 30 64 30 66 30 68 30 6A 30 6B" +
            "30 6C 30 6D 30 6E 30 6F 30 72 30 75 30 78 30 7B" +
            "30 7E 30 7F 30 80 30 81 30 82 30 84 30 86 30 88" +
            "30 89 30 8A 30 8B 30 8C 30 8D 30 8F 30 92 30 93" +
            "30 30 00 30 00 30 00 31 00 01 00 0C 00 05 00 35" +
            "00 00 00 00 00 00 00 4B 30 32 00 01 42 30 44 30" +
            "46 30 48 30 4A 30 4B 30 4D 30 4F 30 51 30 53 30" +
            "55 30 57 30 59 30 5B 30 5D 30 5F 30 61 30 64 30" +
            "66 30 68 30 6A 30 6B 30 6C 30 6D 30 6E 30 6F 30" +
            "72 30 75 30 78 30 7B 30 7E 30 7F 30 80 30 81 30" +
            "82 30 84 30 86 30 88 30 89 30 8A 30 8B 30 8C 30" +
            "8D 30 8F 30 92 30 93 30 30 00 30 00 30 00 32 00" +
            "32 00 01 42 30 44 30 46 30 48 30 4A 30 4B 30 4D" +
            "30 4F 30 51 30 53 30 55 30 57 30 59 30 5B 30 5D" +
            "30 5F 30 61 30 64 30 66 30 68 30 6A 30 6B 30 6C" +
            "30 6D 30 6E 30 6F 30 72 30 75 30 78 30 7B 30 7E" +
            "30 7F 30 80 30 81 30 82 30 84 30 86 30 88 30 89" +
            "30 8A 30 8B 30 8C 30 8D 30 8F 30 92 30 93 30 30" +
            "00 30 00 30 00 33 00 32 00 01 42 30 44 30 46 30" +
            "48 30 4A 30 4B 30 4D 30 4F 30 51 30 53 30 55 30" +
            "57 30 59 30 5B 30 5D 30 5F 30 61 30 64 30 66 30" +
            "68 30 6A 30 6B 30 6C 30 6D 30 6E 30 6F 30 72 30" +
            "75 30 78 30 7B 30 7E 30 7F 30 80 30 81 30 82 30" +
            "84 30 86 30 88 30 89 30 8A 30 8B 30 8C 30 8D 30" +
            "8F 30 92 30 93 30 30 00 30 00 30 00 34 00 32 00" +
            "01 42 30 44 30 46 30 48 30 4A 30 4B 30 4D 30 4F" +
            "30 51 30 53 30 55 30 57 30 59 30 5B 30 5D 30 5F" +
            "30 61 30 64 30 66 30 68 30 6A 30 6B 30 6C 30 6D" +
            "30 6E 30 6F 30 72 30 75 30 78 30 7B 30 7E 30 7F" +
            "30 80 30 81 30 82 30 84 30 86 30 88 30 89 30 8A" +
            "30 8B 30 8C 30 8D 30 8F 30 92 30 93 30 30 00 30" +
            "00 30 00 35 00 32 00 01 42 30 44 30 46 30 48 30" +
            "4A 30 4B 30 4D 30 4F 30 51 30 53 30 55 30 57 30" +
            "59 30 5B 30 5D 30 5F 30 61 30 64 30 66 30 68 30" +
            "6A 30 6B 30 6C 30 6D 30 6E 30 6F 30 72 30 75 30" +
            "78 30 7B 30 7E 30 7F 30 80 30 81 30 82 30 84 30" +
            "86 30 88 30 89 30 8A 30 8B 30 8C 30 8D 30 8F 30" +
            "92 30 93 30 30 00 30 00 30 00 36 00 32 00 01 42" +
            "30 44 30 46 30 48 30 4A 30 4B 30 4D 30 4F 30 51" +
            "30 53 30 55 30 57 30 59 30 5B 30 5D 30 5F 30 61" +
            "30 64 30 66 30 68 30 6A 30 6B 30 6C 30 6D 30 6E" +
            "30 6F 30 72 30 75 30 78 30 7B 30 7E 30 7F 30 80" +
            "30 81 30 82 30 84 30 86 30 88 30 89 30 8A 30 8B" +
            "30 8C 30 8D 30 8F 30 92 30 93 30 30 00 30 00 30" +
            "00 37 00 32 00 01 42 30 44 30 46 30 48 30 4A 30" +
            "4B 30 4D 30 4F 30 51 30 53 30 55 30 57 30 59 30" +
            "5B 30 5D 30 5F 30 61 30 64 30 66 30 68 30 6A 30" +
            "6B 30 6C 30 6D 30 6E 30 6F 30 72 30 75 30 78 30" +
            "7B 30 7E 30 7F 30 80 30 81 30 82 30 84 30 86 30" +
            "88 30 89 30 8A 30 8B 30 8C 30 8D 30 8F 30 92 30" +
            "93 30 30 00 30 00 30 00 38 00 32 00 01 42 30 44" +
            "30 46 30 48 30 4A 30 4B 30 4D 30 4F 30 51 30 53" +
            "30 55 30 57 30 59 30 5B 30 5D 30 5F 30 61 30 64" +
            "30 66 30 68 30 6A 30 6B 30 6C 30 6D 30 6E 30 6F" +
            "30 72 30 75 30 78 30 7B 30 7E 30 7F 30 80 30 81" +
            "30 82 30 84 30 86 30 88 30 89 30 8A 30 8B 30 8C" +
            "30 8D 30 8F 30 92 30 93 30 30 00 30 00 30 00 39" +
            "00 32 00 01 42 30 44 30 46 30 48 30 4A 30 4B 30" +
            "4D 30 4F 30 51 30 53 30 55 30 57 30 59 30 5B 30" +
            "5D 30 5F 30 61 30 64 30 66 30 68 30 6A 30 6B 30" +
            "6C 30 6D 30 6E 30 6F 30 72 30 75 30 78 30 7B 30" +
            "7E 30 7F 30 80 30 81 30 82 30 84 30 86 30 88 30" +
            "89 30 8A 30 8B 30 8C 30 8D 30 8F 30 92 30 93 30" +
            "30 00 30 00 31 00 30 00 32 00 01 42 30 44 30 46" +
            "30 48 30 4A 30 4B 30 4D 30 4F 30 51 30 53 30 55" +
            "30 57 30 59 30 5B 30 5D 30 5F 30 61 30 64 30 66" +
            "30 68 30 6A 30 6B 30 6C 30 6D 30 6E 30 6F 30 72" +
            "30 75 30 78 30 7B 30 7E 30 7F 30 80 30 81 30 82" +
            "30 84 30 86 30 88 30 89 30 8A 30 8B 30 8C 30 8D" +
            "30 8F 30 92 30 93 30 30 00 30 00 31 00 31 00 32" +
            "00 01 42 30 44 30 46 30 48 30 4A 30 4B 30 4D 30" +
            "4F 30 51 30 53 30 55 30 57 30 59 30 5B 30 5D 30" +
            "5F 30 61 30 64 30 66 30 68 30 6A 30 6B 30 6C 30" +
            "6D 30 6E 30 6F 30 72 30 75 30 78 30 7B 30 7E 30" +
            "7F 30 80 30 81 30 82 30 84 30 86 30 88 30 89 30" +
            "8A 30 8B 30 8C 30 8D 30 8F 30 92 30 93 30 30 00" +
            "30 00 31 00 32 00 32 00 01 42 30 44 30 46 30 48" +
            "30 4A 30 4B 30 4D 30 4F 30 51 30 53 30 55 30 57" +
            "30 59 30 5B 30 5D 30 5F 30 61 30 64 30 66 30 68" +
            "30 6A 30 6B 30 6C 30 6D 30 6E 30 6F 30 72 30 75" +
            "30 78 30 7B 30 7E 30 7F 30 80 30 81 30 82 30 84" +
            "30 86 30 88 30 89 30 8A 30 8B 30 8C 30 8D 30 8F" +
            "30 92 30 93 30 30 00 30 00 31 00 33 00 32 00 01" +
            "42 30 44 30 46 30 48 30 4A 30 4B 30 4D 30 4F 30" +
            "51 30 53 30 55 30 57 30 59 30 5B 30 5D 30 5F 30" +
            "61 30 64 30 66 30 68 30 6A 30 6B 30 6C 30 6D 30" +
            "6E 30 6F 30 72 30 75 30 78 30 7B 30 7E 30 7F 30" +
            "80 30 81 30 82 30 84 30 86 30 88 30 89 30 8A 30" +
            "8B 30 8C 30 8D 30 8F 30 92 30 93 30 30 00 30 00" +
            "31 00 34 00 32 00 01 42 30 44 30 46 30 48 30 4A" +
            "30 4B 30 4D 30 4F 30 51 30 53 30 55 30 57 30 59" +
            "30 5B 30 5D 30 5F 30 61 30 64 30 66 30 68 30 6A" +
            "30 6B 30 6C 30 6D 30 6E 30 6F 30 72 30 75 30 78" +
            "30 7B 30 7E 30 7F 30 80 30 81 30 82 30 84 30 86" +
            "30 88 30 89 30 8A 30 8B 30 8C 30 8D 30 8F 30 92" +
            "30 93 30 30 00 30 00 31 00 35 00 32 00 01 42 30" +
            "44 30 46 30 48 30 4A 30 4B 30 4D 30 4F 30 51 30" +
            "53 30 55 30 57 30 59 30 5B 30 5D 30 5F 30 61 30" +
            "64 30 66 30 68 30 6A 30 6B 30 6C 30 6D 30 6E 30" +
            "6F 30 72 30 75 30 78 30 7B 30 7E 30 7F 30 80 30" +
            "81 30 82 30 84 30 86 30 88 30 89 30 8A 30 8B 30" +
            "8C 30 8D 30 8F 30 92 30 93 30 30 00 30 00 31 00" +
            "36 00 32 00 01 42 30 44 30 46 30 48 30 4A 30 4B" +
            "30 4D 30 4F 30 51 30 53 30 55 30 57 30 59 30 5B" +
            "30 5D 30 5F 30 61 30 64 30 66 30 68 30 6A 30 6B" +
            "30 6C 30 6D 30 6E 30 6F 30 72 30 75 30 78 30 7B" +
            "30 7E 30 7F 30 80 30 81 30 82 30 84 30 86 30 88" +
            "30 89 30 8A 30 8B 30 8C 30 8D 30 8F 30 92 30 93" +
            "30 30 00 30 00 31 00 37 00 32 00 01 42 30 44 30" +
            "46 30 48 30 4A 30 4B 30 4D 30 4F 30 51 30 53 30" +
            "55 30 57 30 59 30 5B 30 5D 30 5F 30 61 30 64 30" +
            "66 30 68 30 6A 30 6B 30 6C 30 6D 30 6E 30 6F 30" +
            "72 30 75 30 78 30 7B 30 7E 30 7F 30 80 30 81 30" +
            "82 30 84 30 86 30 88 30 89 30 8A 30 8B 30 8C 30" +
            "8D 30 8F 30 92 30 93 30 30 00 30 00 31 00 38 00" +
            "32 00 01 42 30 44 30 46 30 48 30 4A 30 4B 30 4D" +
            "30 4F 30 51 30 53 30 55 30 57 30 59 30 5B 30 5D" +
            "30 5F 30 61 30 64 30 66 30 68 30 6A 30 6B 30 6C" +
            "30 6D 30 6E 30 6F 30 72 30 75 30 78 30 7B 30 7E" +
            "30 7F 30 80 30 81 30 82 30 84 30 86 30 88 30 89" +
            "30 8A 30 8B 30 8C 30 8D 30 8F 30 92 30 93 30 30" +
            "00 30 00 31 00 39 00 32 00 01 42 30 44 30 46 30" +
            "48 30 4A 30 4B 30 4D 30 4F 30 51 30 53 30 55 30" +
            "57 30 59 30 5B 30 5D 30 5F 30 61 30 64 30 66 30" +
            "68 30 6A 30 6B 30 6C 30 6D 30 6E 30 6F 30 72 30" +
            "75 30 78 30 7B 30 7E 30 7F 30 80 30 81 30 82 30" +
            "84 30 86 30 88 30 89 30 8A 30 8B 30 8C 30 8D 30" +
            "8F 30 92 30 93 30 30 00 30 00 32 00 30 00 32 00" +
            "01 42 30 44 30 46 30 48 30 4A 30 4B 30 4D 30 4F" +
            "30 51 30 53 30 55 30 57 30 59 30 5B 30 5D 30 5F" +
            "30 61 30 64 30 66 30 68 30 6A 30 6B 30 6C 30 6D" +
            "30 6E 30 6F 30 72 30 75 30 78 30 7B 30 7E 30 7F" +
            "30 80 30 81 30 82 30 84 30 86 30 88 30 89 30 8A" +
            "30 8B 30 8C 30 8D 30 8F 30 92 30 93 30 30 00 30" +
            "00 32 00 31 00 32 00 01 42 30 44 30 46 30 48 30" +
            "4A 30 4B 30 4D 30 4F 30 51 30 53 30 55 30 57 30" +
            "59 30 5B 30 5D 30 5F 30 61 30 64 30 66 30 68 30" +
            "6A 30 6B 30 6C 30 6D 30 6E 30 6F 30 72 30 75 30" +
            "78 30 7B 30 7E 30 7F 30 80 30 81 30 82 30 84 30" +
            "86 30 88 30 89 30 8A 30 8B 30 8C 30 8D 30 8F 30" +
            "92 30 93 30 30 00 30 00 32 00 32 00 32 00 01 42" +
            "30 44 30 46 30 48 30 4A 30 4B 30 4D 30 4F 30 51" +
            "30 53 30 55 30 57 30 59 30 5B 30 5D 30 5F 30 61" +
            "30 64 30 66 30 68 30 6A 30 6B 30 6C 30 6D 30 6E" +
            "30 6F 30 72 30 75 30 78 30 7B 30 7E 30 7F 30 80" +
            "30 81 30 82 30 84 30 86 30 88 30 89 30 8A 30 8B" +
            "30 8C 30 8D 30 8F 30 92 30 93 30 30 00 30 00 32" +
            "00 33 00 32 00 01 42 30 44 30 46 30 48 30 4A 30" +
            "4B 30 4D 30 4F 30 51 30 53 30 55 30 57 30 59 30" +
            "5B 30 5D 30 5F 30 61 30 64 30 66 30 68 30 6A 30" +
            "6B 30 6C 30 6D 30 6E 30 6F 30 72 30 75 30 78 30" +
            "7B 30 7E 30 7F 30 80 30 81 30 82 30 84 30 86 30" +
            "88 30 89 30 8A 30 8B 30 8C 30 8D 30 8F 30 92 30" +
            "93 30 30 00 30 00 32 00 34 00 32 00 01 42 30 44" +
            "30 46 30 48 30 4A 30 4B 30 4D 30 4F 30 51 30 53" +
            "30 55 30 57 30 59 30 5B 30 5D 30 5F 30 61 30 64" +
            "30 66 30 68 30 6A 30 6B 30 6C 30 6D 30 6E 30 6F" +
            "30 72 30 75 30 78 30 7B 30 7E 30 7F 30 80 30 81" +
            "30 82 30 84 30 86 30 88 30 89 30 8A 30 8B 30 8C" +
            "30 8D 30 8F 30 92 30 93 30 30 00 30 00 32 00 35" +
            "00 32 00 01 42 30 44 30 46 30 48 30 4A 30 4B 30" +
            "4D 30 4F 30 51 30 53 30 55 30 57 30 59 30 5B 30" +
            "5D 30 5F 30 61 30 64 30 66 30 68 30 6A 30 6B 30" +
            "6C 30 6D 30 6E 30 6F 30 72 30 75 30 78 30 7B 30" +
            "7E 30 7F 30 80 30 81 30 82 30 84 30 86 30 88 30" +
            "89 30 8A 30 8B 30 8C 30 8D 30 8F 30 92 30 93 30" +
            "30 00 30 00 32 00 36 00 32 00 01 42 30 44 30 46" +
            "30 48 30 4A 30 4B 30 4D 30 4F 30 51 30 53 30 55" +
            "30 57 30 59 30 5B 30 5D 30 5F 30 61 30 64 30 66" +
            "30 68 30 6A 30 6B 30 6C 30 6D 30 6E 30 6F 30 72" +
            "30 75 30 78 30 7B 30 7E 30 7F 30 80 30 81 30 82" +
            "30 84 30 86 30 88 30 89 30 8A 30 8B 30 8C 30 8D" +
            "30 8F 30 92 30 93 30 30 00 30 00 32 00 37 00 32" +
            "00 01 42 30 44 30 46 30 48 30 4A 30 4B 30 4D 30" +
            "4F 30 51 30 53 30 55 30 57 30 59 30 5B 30 5D 30" +
            "5F 30 61 30 64 30 66 30 68 30 6A 30 6B 30 6C 30" +
            "6D 30 6E 30 6F 30 72 30 75 30 78 30 7B 30 7E 30" +
            "7F 30 80 30 81 30 82 30 84 30 86 30 88 30 89 30" +
            "8A 30 8B 30 8C 30 8D 30 8F 30 92 30 93 30 30 00" +
            "30 00 32 00 38 00 32 00 01 42 30 44 30 46 30 48" +
            "30 4A 30 4B 30 4D 30 4F 30 51 30 53 30 55 30 57" +
            "30 59 30 5B 30 5D 30 5F 30 61 30 64 30 66 30 68" +
            "30 6A 30 6B 30 6C 30 6D 30 6E 30 6F 30 72 30 75" +
            "30 78 30 7B 30 7E 30 7F 30 80 30 81 30 82 30 84" +
            "30 86 30 88 30 89 30 8A 30 8B 30 8C 30 8D 30 8F" +
            "30 92 30 93 30 30 00 30 00 32 00 39 00 32 00 01" +
            "42 30 44 30 46 30 48 30 4A 30 4B 30 4D 30 4F 30" +
            "51 30 53 30 55 30 57 30 59 30 5B 30 5D 30 5F 30" +
            "61 30 64 30 66 30 68 30 6A 30 6B 30 6C 30 6D 30" +
            "6E 30 6F 30 72 30 75 30 78 30 7B 30 7E 30 7F 30" +
            "80 30 81 30 82 30 84 30 86 30 88 30 89 30 8A 30" +
            "8B 30 8C 30 8D 30 8F 30 92 30 93 30 30 00 30 00" +
            "33 00 30 00 32 00 01 42 30 44 30 46 30 48 30 4A" +
            "30 4B 30 4D 30 4F 30 51 30 53 30 55 30 57 30 59" +
            "30 5B 30 5D 30 5F 30 61 30 64 30 66 30 68 30 6A" +
            "30 6B 30 6C 30 6D 30 6E 30 6F 30 72 30 75 30 78" +
            "30 7B 30 7E 30 7F 30 80 30 81 30 82 30 84 30 86" +
            "30 88 30 89 30 8A 30 8B 30 8C 30 8D 30 8F 30 92" +
            "30 93 30 30 00 30 00 33 00 31 00 32 00 01 42 30" +
            "44 30 46 30 48 30 4A 30 4B 30 4D 30 4F 30 51 30" +
            "53 30 55 30 57 30 59 30 5B 30 5D 30 5F 30 61 30" +
            "64 30 66 30 68 30 6A 30 6B 30 6C 30 6D 30 6E 30" +
            "6F 30 72 30 75 30 78 30 7B 30 7E 30 7F 30 80 30" +
            "81 30 82 30 84 30 86 30 88 30 89 30 8A 30 8B 30" +
            "8C 30 8D 30 8F 30 92 30 93 30 30 00 30 00 33 00" +
            "32 00 32 00 01 42 30 44 30 46 30 48 30 4A 30 4B" +
            "30 4D 30 4F 30 51 30 53 30 55 30 57 30 59 30 5B" +
            "30 5D 30 5F 30 61 30 64 30 66 30 68 30 6A 30 6B" +
            "30 6C 30 6D 30 6E 30 6F 30 72 30 75 30 78 30 7B" +
            "30 7E 30 7F 30 80 30 81 30 82 30 84 30 86 30 88" +
            "30 89 30 8A 30 8B 30 8C 30 8D 30 8F 30 92 30 93" +
            "30 30 00 30 00 33 00 33 00 32 00 01 42 30 44 30" +
            "46 30 48 30 4A 30 4B 30 4D 30 4F 30 51 30 53 30" +
            "55 30 57 30 59 30 5B 30 5D 30 5F 30 61 30 64 30" +
            "66 30 68 30 6A 30 6B 30 6C 30 6D 30 6E 30 6F 30" +
            "72 30 75 30 78 30 7B 30 7E 30 7F 30 80 30 81 30" +
            "82 30 84 30 86 30 88 30 89 30 8A 30 8B 30 8C 30" +
            "8D 30 8F 30 92 30 93 30 30 00 30 00 33 00 34 00" +
            "32 00 01 42 30 44 30 46 30 48 30 4A 30 4B 30 4D" +
            "30 4F 30 51 30 53 30 55 30 57 30 59 30 5B 30 5D" +
            "30 5F 30 61 30 64 30 66 30 68 30 6A 30 6B 30 6C" +
            "30 6D 30 6E 30 6F 30 72 30 75 30 78 30 7B 30 7E" +
            "30 7F 30 80 30 81 30 82 30 84 30 86 30 88 30 89" +
            "30 8A 30 8B 30 8C 30 8D 30 8F 30 92 30 93 30 30" +
            "00 30 00 33 00 35 00 32 00 01 42 30 44 30 46 30" +
            "48 30 4A 30 4B 30 4D 30 4F 30 51 30 53 30 55 30" +
            "57 30 59 30 5B 30 5D 30 5F 30 61 30 64 30 66 30" +
            "68 30 6A 30 6B 30 6C 30 6D 30 6E 30 6F 30 72 30" +
            "75 30 78 30 7B 30 7E 30 7F 30 80 30 81 30 82 30" +
            "84 30 86 30 88 30 89 30 8A 30 8B 30 8C 30 8D 30" +
            "8F 30 92 30 93 30 30 00 30 00 33 00 36 00 32 00" +
            "01 42 30 44 30 46 30 48 30 4A 30 4B 30 4D 30 4F" +
            "30 51 30 53 30 55 30 57 30 59 30 5B 30 5D 30 5F" +
            "30 61 30 64 30 66 30 68 30 6A 30 6B 30 6C 30 6D" +
            "30 6E 30 6F 30 72 30 75 30 78 30 7B 30 7E 30 7F" +
            "30 80 30 81 30 82 30 84 30 86 30 88 30 89 30 8A" +
            "30 8B 30 8C 30 8D 30 8F 30 92 30 93 30 30 00 30" +
            "00 33 00 37 00 32 00 01 42 30 44 30 46 30 48 30" +
            "4A 30 4B 30 4D 30 4F 30 51 30 53 30 55 30 57 30" +
            "59 30 5B 30 5D 30 5F 30 61 30 64 30 66 30 68 30" +
            "6A 30 6B 30 6C 30 6D 30 6E 30 6F 30 72 30 75 30" +
            "78 30 7B 30 7E 30 7F 30 80 30 81 30 82 30 84 30" +
            "86 30 88 30 89 30 8A 30 8B 30 8C 30 8D 30 8F 30" +
            "92 30 93 30 30 00 30 00 33 00 38 00 32 00 01 42" +
            "30 44 30 46 30 48 30 4A 30 4B 30 4D 30 4F 30 51" +
            "30 53 30 55 30 57 30 59 30 5B 30 5D 30 5F 30 61" +
            "30 64 30 66 30 68 30 6A 30 6B 30 6C 30 6D 30 6E" +
            "30 6F 30 72 30 75 30 78 30 7B 30 7E 30 7F 30 80" +
            "30 81 30 82 30 84 30 86 30 88 30 89 30 8A 30 8B" +
            "30 8C 30 8D 30 8F 30 92 30 93 30 30 00 30 00 33" +
            "00 39 00 32 00 01 42 30 44 30 46 30 48 30 4A 30" +
            "4B 30 4D 30 4F 30 51 30 53 30 55 30 57 30 59 30" +
            "5B 30 5D 30 5F 30 61 30 64 30 66 30 68 30 6A 30" +
            "6B 30 6C 30 6D 30 6E 30 6F 30 72 30 75 30 78 30" +
            "7B 30 7E 30 7F 30 80 30 81 30 82 30 84 30 86 30" +
            "88 30 89 30 8A 30 8B 30 8C 30 8D 30 8F 30 92 30" +
            "93 30 30 00 30 00 34 00 30 00 32 00 01 42 30 44" +
            "30 46 30 48 30 4A 30 4B 30 4D 30 4F 30 51 30 53" +
            "30 55 30 57 30 59 30 5B 30 5D 30 5F 30 61 30 64" +
            "30 66 30 68 30 6A 30 6B 30 6C 30 6D 30 6E 30 6F" +
            "30 72 30 75 30 78 30 7B 30 7E 30 7F 30 80 30 81" +
            "30 82 30 84 30 86 30 88 30 89 30 8A 30 8B 30 8C" +
            "30 8D 30 8F 30 92 30 93 30 30 00 30 00 34 00 31" +
            "00 32 00 01 42 30 44 30 46 30 48 30 4A 30 4B 30" +
            "4D 30 4F 30 51 30 53 30 55 30 57 30 59 30 5B 30" +
            "5D 30 5F 30 61 30 64 30 66 30 68 30 6A 30 6B 30" +
            "6C 30 6D 30 6E 30 6F 30 72 30 75 30 78 30 7B 30" +
            "7E 30 7F 30 80 30 81 30 82 30 84 30 86 30 88 30" +
            "89 30 8A 30 8B 30 8C 30 8D 30 8F 30 92 30 93 30" +
            "30 00 30 00 34 00 32 00 32 00 01 42 30 44 30 46" +
            "30 48 30 4A 30 4B 30 4D 30 4F 30 51 30 53 30 55" +
            "30 57 30 59 30 5B 30 5D 30 5F 30 61 30 64 30 66" +
            "30 68 30 6A 30 6B 30 6C 30 6D 30 6E 30 6F 30 72" +
            "30 75 30 78 30 7B 30 7E 30 7F 30 80 30 81 30 82" +
            "30 84 30 86 30 88 30 89 30 8A 30 8B 30 8C 30 8D" +
            "30 8F 30 92 30 93 30 30 00 30 00 34 00 33 00 32" +
            "00 01 42 30 44 30 46 30 48 30 4A 30 4B 30 4D 30" +
            "4F 30 51 30 53 30 55 30 57 30 59 30 5B 30 5D 30" +
            "5F 30 61 30 64 30 66 30 68 30 6A 30 6B 30 6C 30" +
            "6D 30 6E 30 6F 30 72 30 75 30 78 30 7B 30 7E 30" +
            "7F 30 80 30 81 30 82 30 84 30 86 30 88 30 89 30" +
            "8A 30 8B 30 8C 30 8D 30 8F 30 92 30 93 30 30 00" +
            "30 00 34 00 34 00 32 00 01 42 30 44 30 46 30 48" +
            "30 4A 30 4B 30 4D 30 4F 30 51 30 53 30 55 30 57" +
            "30 59 30 5B 30 5D 30 5F 30 61 30 64 30 66 30 68" +
            "30 6A 30 6B 30 6C 30 6D 30 6E 30 6F 30 72 30 75" +
            "30 78 30 7B 30 7E 30 7F 30 80 30 81 30 82 30 84" +
            "30 86 30 88 30 89 30 8A 30 8B 30 8C 30 8D 30 8F" +
            "30 92 30 93 30 30 00 30 00 34 00 35 00 32 00 01" +
            "42 30 44 30 46 30 48 30 4A 30 4B 30 4D 30 4F 30" +
            "51 30 53 30 55 30 57 30 59 30 5B 30 5D 30 5F 30" +
            "61 30 64 30 66 30 68 30 6A 30 6B 30 6C 30 6D 30" +
            "6E 30 6F 30 72 30 75 30 78 30 7B 30 7E 30 7F 30" +
            "80 30 81 30 82 30 84 30 86 30 88 30 89 30 8A 30" +
            "8B 30 8C 30 8D 30 8F 30 92 30 93 30 30 00 30 00" +
            "34 00 36 00 32 00 01 42 30 44 30 46 30 48 30 4A" +
            "30 4B 30 4D 30 4F 30 51 30 53 30 55 30 57 30 59" +
            "30 5B 30 5D 30 5F 30 61 30 64 30 66 30 68 30 6A" +
            "30 6B 30 6C 30 6D 30 6E 30 6F 30 72 30 75 30 78" +
            "30 7B 30 7E 30 7F 30 80 30 81 30 82 30 84 30 86" +
            "30 88 30 89 30 8A 30 8B 30 8C 30 8D 30 8F 30 92" +
            "30 93 30 30 00 30 00 34 00 37 00 32 00 01 42 30" +
            "44 30 46 30 48 30 4A 30 4B 30 4D 30 4F 30 51 30" +
            "53 30 55 30 57 30 59 30 5B 30 5D 30 5F 30 61 30" +
            "64 30 66 30 68 30 6A 30 6B 30 6C 30 6D 30 6E 30" +
            "6F 30 72 30 75 30 78 30 7B 30 7E 30 7F 30 80 30" +
            "81 30 82 30 84 30 86 30 88 30 89 30 8A 30 8B 30" +
            "8C 30 8D 30 8F 30 92 30 93 30 30 00 30 00 34 00" +
            "38 00 32 00 01 42 30 44 30 46 30 48 30 4A 30 4B" +
            "30 4D 30 4F 30 51 30 53 30 55 30 57 30 59 30 5B" +
            "30 5D 30 5F 30 61 30 64 30 66 30 68 30 6A 30 6B" +
            "30 6C 30 6D 30 6E 30 6F 30 72 30 75 30 78 30 7B" +
            "30 7E 30 7F 30 80 30 81 30 82 30 84 30 86 30 88" +
            "30 89 30 8A 30 8B 30 8C 30 8D 30 8F 30 92 30 93" +
            "30 30 00 30 00 34 00 39 00 32 00 01 42 30 44 30" +
            "46 30 48 30 4A 30 4B 30 4D 30 4F 30 51 30 53 30" +
            "55 30 57 30 59 30 5B 30 5D 30 5F 30 61 30 64 30" +
            "66 30 68 30 6A 30 6B 30 6C 30 6D 30 6E 30 6F 30" +
            "72 30 75 30 78 30 7B 30 7E 30 7F 30 80 30 81 30" +
            "82 30 84 30 86 30 88 30 89 30 8A 30 8B 30 8C 30" +
            "8D 30 8F 30 92 30 93 30 30 00 30 00 35 00 30 00" +
            "32 00 01 42 30 44 30 46 30 48 30 4A 30 4B 30 4D" +
            "30 4F 30 51 30 53 30 55 30 57 30 59 30 5B 30 5D" +
            "30 5F 30 61 30 64 30 66 30 68 30 6A 30 6B 30 6C" +
            "30 6D 30 6E 30 6F 30 72 30 75 30 78 30 7B 30 7E" +
            "30 7F 30 80 30 81 30 82 30 84 30 86 30 88 30 89" +
            "30 8A 30 8B 30 8C 30 8D 30 8F 30 92 30 93 30 30" +
            "00 30 00 35 00 31 00 32 00 01 42 30 44 30 46 30" +
            "48 30 4A 30 4B 30 4D 30 4F 30 51 30 53 30 55 30" +
            "57 30 59 30 5B 30 5D 30 5F 30 61 30 64 30 66 30" +
            "68 30 6A 30 6B 30 6C 30 6D 30 6E 30 6F 30 72 30" +
            "75 30 78 30 7B 30 7E 30 7F 30 80 30 81 30 82 30" +
            "84 30 86 30 88 30 89 30 8A 30 8B 30 8C 30 8D 30" +
            "8F 30 92 30 93 30 30 00 30 00 35 00 32 00 32 00" +
            "01 42 30 44 30 46 30 48 30 4A 30 4B 30 4D 30 4F" +
            "30 51 30 53 30 55 30 57 30 59 30 5B 30 5D 30 5F" +
            "30 61 30 64 30 66 30 68 30 6A 30 6B 30 6C 30 6D" +
            "30 6E 30 6F 30 72 30 75 30 78 30 7B 30 7E 30 7F" +
            "30 80 30 81 30 82 30 84 30 86 30 88 30 89 30 8A" +
            "30 8B 30 8C 30 8D 30 8F 30 92 30 93 30 30 00 30" +
            "00 35 00 33 00 32 00 01 42 30 44 30 46 30 48 30" +
            "4A 30 4B 30 4D 30 4F 30 51 30 53 30 55 30 57 30" +
            "59 30 5B 30 5D 30 5F 30 61 30 64 30 66 30 68 30" +
            "6A 30 6B 30 6C 30 6D 30 6E 30 6F 30 72 30 75 30" +
            "78 30 7B 30 7E 30 7F 30 80 30 81 30 82 30 84 30" +
            "86 30 88 30 89 30 8A 30 8B 30 8C 30 8D 30 8F 30" +
            "92 30 93 30 30 00 30 00 35 00 34 00 32 00 01 42" +
            "30 44 30 46 30 48 30 4A 30 4B 30 4D 30 4F 30 51" +
            "30 53 30 55 30 57 30 59 30 5B 30 5D 30 5F 30 61" +
            "30 64 30 66 30 68 30 6A 30 6B 30 6C 30 6D 30 6E" +
            "30 6F 30 72 30 75 30 78 30 7B 30 7E 30 7F 30 80" +
            "30 81 30 82 30 84 30 86 30 88 30 89 30 8A 30 8B" +
            "30 8C 30 8D 30 8F 30 92 30 93 30 30 00 30 00 35" +
            "00 35 00 32 00 01 42 30 44 30 46 30 48 30 4A 30" +
            "4B 30 4D 30 4F 30 51 30 53 30 55 30 57 30 59 30" +
            "5B 30 5D 30 5F 30 61 30 64 30 66 30 68 30 6A 30" +
            "6B 30 6C 30 6D 30 6E 30 6F 30 72 30 75 30 78 30" +
            "7B 30 7E 30 7F 30 80 30 81 30 82 30 84 30 86 30" +
            "88 30 89 30 8A 30 8B 30 8C 30 8D 30 8F 30 92 30" +
            "93 30 30 00 30 00 35 00 36 00 32 00 01 42 30 44" +
            "30 46 30 48 30 4A 30 4B 30 4D 30 4F 30 51 30 53" +
            "30 55 30 57 30 59 30 5B 30 5D 30 5F 30 61 30 64" +
            "30 66 30 68 30 6A 30 6B 30 6C 30 6D 30 6E 30 6F" +
            "30 72 30 75 30 78 30 7B 30 7E 30 7F 30 80 30 81" +
            "30 82 30 84 30 86 30 88 30 89 30 8A 30 8B 30 8C" +
            "30 8D 30 8F 30 92 30 93 30 30 00 30 00 35 00 37" +
            "00 32 00 01 42 30 44 30 46 30 48 30 4A 30 4B 30" +
            "4D 30 4F 30 51 30 53 30 55 30 57 30 59 30 5B 30" +
            "5D 30 5F 30 61 30 64 30 66 30 68 30 6A 30 6B 30" +
            "6C 30 6D 30 6E 30 6F 30 72 30 75 30 78 30 7B 30" +
            "7E 30 7F 30 80 30 81 30 82 30 84 30 86 30 88 30" +
            "89 30 8A 30 8B 30 8C 30 8D 30 8F 30 92 30 93 30" +
            "30 00 30 00 35 00 38 00 32 00 01 42 30 44 30 46" +
            "30 48 30 4A 30 4B 30 4D 30 4F 30 51 30 53 30 55" +
            "30 57 30 59 30 5B 30 5D 30 5F 30 61 30 64 30 66" +
            "30 68 30 6A 30 6B 30 6C 30 6D 30 6E 30 6F 30 72" +
            "30 75 30 78 30 7B 30 7E 30 7F 30 80 30 81 30 82" +
            "30 84 30 86 30 88 30 89 30 8A 30 8B 30 8C 30 8D" +
            "30 8F 30 92 30 93 30 30 00 30 00 35 00 39 00 32" +
            "00 01 42 30 44 30 46 30 48 30 4A 30 4B 30 4D 30" +
            "4F 30 51 30 53 30 55 30 57 30 59 30 5B 30 5D 30" +
            "5F 30 61 30 64 30 66 30 68 30 6A 30 6B 30 6C 30" +
            "6D 30 6E 30 6F 30 72 30 75 30 78 30 7B 30 7E 30" +
            "7F 30 80 30 81 30 82 30 84 30 86 30 88 30 89 30" +
            "8A 30 8B 30 8C 30 8D 30 8F 30 92 30 93 30 30 00" +
            "30 00 36 00 30 00 32 00 01 42 30 44 30 46 30 48" +
            "30 4A 30 4B 30 4D 30 4F 30 51 30 53 30 55 30 57" +
            "30 59 30 5B 30 5D 30 5F 30 61 30 64 30 66 30 68" +
            "30 6A 30 6B 30 6C 30 6D 30 6E 30 6F 30 72 30 75" +
            "30 78 30 7B 30 7E 30 7F 30 80 30 81 30 82 30 84" +
            "30 86 30 88 30 89 30 8A 30 8B 30 8C 30 8D 30 8F" +
            "30 92 30 93 30 30 00 30 00 36 00 31 00 32 00 01" +
            "42 30 44 30 46 30 48 30 4A 30 4B 30 4D 30 4F 30" +
            "51 30 53 30 55 30 57 30 59 30 5B 30 5D 30 5F 30" +
            "61 30 64 30 66 30 68 30 6A 30 6B 30 6C 30 6D 30" +
            "6E 30 6F 30 72 30 75 30 78 30 7B 30 7E 30 7F 30" +
            "80 30 81 30 82 30 84 30 86 30 88 30 89 30 8A 30" +
            "8B 30 8C 30 8D 30 8F 30 92 30 93 30 30 00 30 00" +
            "36 00 32 00 32 00 01 42 30 44 30 46 30 48 30 4A" +
            "30 4B 30 4D 30 4F 30 51 30 53 30 55 30 57 30 59" +
            "30 5B 30 5D 30 5F 30 61 30 64 30 66 30 68 30 6A" +
            "30 6B 30 6C 30 6D 30 6E 30 6F 30 72 30 75 30 78" +
            "30 7B 30 7E 30 7F 30 80 30 81 30 82 30 84 30 86" +
            "30 88 30 89 30 8A 30 8B 30 8C 30 8D 30 8F 30 92" +
            "30 93 30 30 00 30 00 36 00 33 00 32 00 01 42 30" +
            "44 30 46 30 48 30 4A 30 4B 30 4D 30 4F 30 51 30" +
            "53 30 55 30 57 30 59 30 5B 30 5D 30 5F 30 61 30" +
            "64 30 66 30 68 30 6A 30 6B 30 6C 30 6D 30 6E 30" +
            "6F 30 72 30 75 30 78 30 7B 30 7E 30 7F 30 80 30" +
            "81 30 82 30 84 30 86 30 88 30 89 30 8A 30 8B 30" +
            "8C 30 8D 30 8F 30 92 30 93 30 30 00 30 00 36 00" +
            "34 00 32 00 01 42 30 44 30 46 30 48 30 4A 30 4B" +
            "30 4D 30 4F 30 51 30 53 30 55 30 57 30 59 30 5B" +
            "30 5D 30 5F 30 61 30 64 30 66 30 68 30 6A 30 6B" +
            "30 6C 30 6D 30 6E 30 6F 30 72 30 75 30 78 30 7B" +
            "30 7E 30 7F 30 80 30 81 30 82 30 84 30 86 30 88" +
            "30 89 30 8A 30 8B 30 8C 30 8D 30 8F 30 92 30 93" +
            "30 30 00 30 00 36 00 35 00 32 00 01 42 30 44 30" +
            "46 30 48 30 4A 30 4B 30 4D 30 4F 30 51 30 53 30" +
            "55 30 57 30 59 30 5B 30 5D 30 5F 30 61 30 64 30" +
            "66 30 68 30 6A 30 6B 30 6C 30 6D 30 6E 30 6F 30" +
            "72 30 75 30 78 30 7B 30 7E 30 7F 30 80 30 81 30" +
            "82 30 84 30 86 30 88 30 89 30 8A 30 8B 30 8C 30" +
            "8D 30 8F 30 92 30 93 30 30 00 30 00 36 00 36 00" +
            "32 00 01 42 30 44 30 46 30 48 30 4A 30 4B 30 4D" +
            "30 4F 30 51 30 53 30 55 30 57 30 59 30 5B 30 5D" +
            "30 5F 30 61 30 64 30 66 30 68 30 6A 30 6B 30 6C" +
            "30 6D 30 6E 30 6F 30 72 30 75 30 78 30 7B 30 7E" +
            "30 7F 30 80 30 81 30 82 30 84 30 86 30 88 30 89" +
            "30 8A 30 8B 30 8C 30 8D 30 8F 30 92 30 93 30 30" +
            "00 30 00 36 00 37 00 32 00 01 42 30 44 30 46 30" +
            "48 30 4A 30 4B 30 4D 30 4F 30 51 30 53 30 55 30" +
            "57 30 59 30 5B 30 5D 30 5F 30 61 30 64 30 66 30" +
            "68 30 6A 30 6B 30 6C 30 6D 30 6E 30 6F 30 72 30" +
            "75 30 78 30 7B 30 7E 30 7F 30 80 30 81 30 82 30" +
            "84 30 86 30 88 30 89 30 8A 30 8B 30 8C 30 8D 30" +
            "8F 30 92 30 93 30 30 00 30 00 36 00 38 00 32 00" +
            "01 42 30 44 30 46 30 48 30 4A 30 4B 30 4D 30 4F" +
            "30 51 30 53 30 55 30 57 30 59 30 5B 30 5D 30 5F" +
            "30 61 30 64 30 66 30 68 30 6A 30 6B 30 6C 30 6D" +
            "30 6E 30 6F 30 72 30 75 30 78 30 7B 30 7E 30 7F" +
            "30 80 30 81 30 82 30 84 30 86 30 88 30 89 30 8A" +
            "30 8B 30 8C 30 8D 30 8F 30 92 30 93 30 30 00 30" +
            "00 36 00 39 00 32 00 01 42 30 44 30 46 30 48 30" +
            "4A 30 4B 30 4D 30 4F 30 51 30 53 30 55 30 57 30" +
            "59 30 5B 30 5D 30 5F 30 61 30 64 30 66 30 68 30" +
            "6A 30 6B 30 6C 30 6D 30 6E 30 6F 30 72 30 75 30" +
            "78 30 7B 30 7E 30 7F 30 80 30 81 30 82 30 84 30" +
            "86 30 88 30 89 30 8A 30 8B 30 8C 30 8D 30 8F 30" +
            "92 30 93 30 30 00 30 00 37 00 30 00 32 00 01 42" +
            "30 44 30 46 30 48 30 4A 30 4B 30 4D 30 4F 30 51" +
            "30 53 30 55 30 57 30 59 30 5B 30 5D 30 5F 30 61" +
            "30 64 30 66 30 68 30 6A 30 6B 30 6C 30 6D 30 6E" +
            "30 6F 30 72 30 75 30 78 30 7B 30 7E 30 7F 30 80" +
            "30 81 30 82 30 84 30 86 30 88 30 89 30 8A 30 8B" +
            "30 8C 30 8D 30 8F 30 92 30 93 30 30 00 30 00 37" +
            "00 31 00 32 00 01 42 30 44 30 46 30 48 30 4A 30" +
            "4B 30 4D 30 4F 30 51 30 53 30 55 30 57 30 59 30" +
            "5B 30 5D 30 5F 30 61 30 64 30 66 30 68 30 6A 30" +
            "6B 30 6C 30 6D 30 6E 30 6F 30 72 30 75 30 78 30" +
            "7B 30 7E 30 7F 30 80 30 81 30 82 30 84 30 86 30" +
            "88 30 89 30 8A 30 8B 30 8C 30 8D 30 8F 30 92 30" +
            "93 30 30 00 30 00 37 00 32 00 32 00 01 42 30 44" +
            "30 46 30 48 30 4A 30 4B 30 4D 30 4F 30 51 30 53" +
            "30 55 30 57 30 59 30 5B 30 5D 30 5F 30 61 30 64" +
            "30 66 30 68 30 6A 30 6B 30 6C 30 6D 30 6E 30 6F" +
            "30 72 30 75 30 78 30 7B 30 7E 30 7F 30 80 30 81" +
            "30 82 30 84 30 86 30 88 30 89 30 8A 30 8B 30 8C" +
            "30 8D 30 8F 30 92 30 93 30 30 00 30 00 37 00 33" +
            "00 32 00 01 42 30 44 30 46 30 48 30 4A 30 4B 30" +
            "4D 30 4F 30 51 30 53 30 55 30 57 30 59 30 5B 30" +
            "5D 30 5F 30 61 30 64 30 66 30 68 30 6A 30 6B 30" +
            "6C 30 6D 30 6E 30 6F 30 72 30 75 30 78 30 7B 30" +
            "7E 30 7F 30 80 30 81 30 82 30 84 30 86 30 88 30" +
            "89 30 8A 30 8B 30 8C 30 8D 30 8F 30 92 30 93 30" +
            "30 00 30 00 37 00 34 00 32 00 01 42 30 44 30 46" +
            "30 48 30 4A 30 4B 30 4D 30 4F 30 51 30 53 30 55" +
            "30 57 30 59 30 5B 30 5D 30 5F 30 61 30 64 30 66" +
            "30 68 30 6A 30 6B 30 6C 30 6D 30 6E 30 6F 30 72" +
            "30 75 30 78 30 7B 30 7E 30 7F 30 80 30 81 30 82" +
            "30 84 30 86 30 88 30 89 30 8A 30 8B 30 8C 30 8D" +
            "30 8F 30 92 30 93 30 30 00 30 00 37 00 35 00 32" +
            "00 01 42 30 44 30 46 30 48 30 4A 30 4B 30 4D 30" +
            "4F 30 51 30 53 30 55 30 57 30 59 30 5B 30 5D 30" +
            "5F 30 61 30 64 30 66 30 68 30 6A 30 6B 30 6C 30" +
            "6D 30 6E 30 6F 30 72 30 75 30 78 30 7B 30 7E 30" +
            "7F 30 80 30 81 30 82 30 84 30 86 30 88 30 89 30" +
            "8A 30 8B 30 8C 30 8D 30 8F 30 92 30 93 30 30 00" +
            "30 00 37 00 36 00 32 00 01 42 30 44 30 46 30 48" +
            "30 4A 30 4B 30 4D 30 4F 30 51 30 53 30 55 30 57" +
            "30 59 30 5B 30 5D 30 5F 30 61 30 64 30 66 30 68" +
            "30 6A 30 6B 30 6C 30 6D 30 6E 30 6F 30 72 30 75" +
            "30 78 30 7B 30 7E 30 7F 30 80 30 81 30 82 30 84" +
            "30 86 30 88 30 89 30 8A 30 8B 30 8C 30 8D 30 8F" +
            "30 92 30 93 30 30 00 30 00 37 00 37 00 32 00 01" +
            "42 30 44 30 46 30 48 30 4A 30 4B 30 4D 30 4F 30" +
            "51 30 53 30 55 30 57 30 59 30 5B 30 5D 30 5F 30" +
            "61 30 64 30 66 30 68 30 6A 30 6B 30 6C 30 6D 30" +
            "6E 30 6F 30 72 30 75 30 78 30 7B 30 7E 30 7F 30" +
            "80 30 81 30 82 30 84 30 86 30 88 30 89 30 8A 30" +
            "8B 30 8C 30 8D 30 8F 30 92 30 93 30 30 00 30 00" +
            "37 00 38 00 1F 00 05 B8 00 00 00 17 53 77 6D 53" +
            "90 52 97 EE 68 0C 77 A9 5C 4B 62 0C 77 8F 79 F6" +
            "5C 0C 77 03 68 28 67 0C 77 FC 57 89 73 0C 77 71" +
            "67 AC 4E FD 90 43 53 49 84 0C 77 5E 79 48 59 DD" +
            "5D 0C 77 77 95 CE 91 0C 77 01 00 B4 00 05 00 35" +
            "00 0A 00 37 00 37 00 DB 30 C3 30 AB 30 A4 30 C9" +
            "30 A6 30 A2 30 AA 30 E2 30 EA 30 B1 30 F3 30 A4" +
            "30 EF 30 C6 30 B1 30 F3 30 D5 30 AF 30 B7 30 DE" +
            "30 B1 30 F3 30 C8 30 C1 30 AE 30 B1 30 F3 30 B5" +
            "30 A4 30 BF 30 DE 30 B1 30 F3 30 C8 30 A6 30 AD" +
            "30 E7 30 A6 30 C8                              " +

            // Offset=0x00002636(9782) recno=72 sid=0x003C size=0x0151(337)
            "                  3C 00 51 01 30 C1 30 D0 30 B1" +
            "30 F3 30 AB 30 CA 30 AC 30 EF 30 B1 30 F3 30 CA" +
            "30 AC 30 CE 30 B1 30 F3 30 00 00 00 00 03 00 06" +
            "00 03 00 03 00 0C 00 06 00 03 00 11 00 09 00 03" +
            "00 17 00 0C 00 03 00 1C 00 0F 00 03 00 22 00 12" +
            "00 03 00 28 00 15 00 03 00 2C 00 18 00 04 00 32" +
            "00 1C 00 03 00 32 00 05 10 00 00 00 42 30 44 30" +
            "46 30 48 30 4A 30 4B 30 4D 30 4F 30 51 30 53 30" +
            "55 30 57 30 59 30 5B 30 5D 30 5F 30 61 30 64 30" +
            "66 30 68 30 6A 30 6B 30 6C 30 6D 30 6E 30 6F 30" +
            "72 30 75 30 78 30 7B 30 7E 30 7F 30 80 30 81 30" +
            "82 30 84 30 86 30 88 30 89 30 8A 30 8B 30 8C 30" +
            "8D 30 8F 30 92 30 93 30 30 00 30 00 37 00 39 00" +
            "01 00 0C 00 05 00 35 00 00 00 00 00 00 00 00 00" +
            "32 00 05 10 00 00 00 42 30 44 30 46 30 48 30 4A" +
            "30 4B 30 4D 30 4F 30 51 30 53 30 55 30 57 30 59" +
            "30 5B 30 5D 30 5F 30 61 30 64 30 66 30 68 30 6A" +
            "30 6B 30 6C 30 6D 30 6E 30 6F 30 72 30 75 30 78" +
            "30 7B 30 7E 30 7F 30 80 30 81 30 82 30 84 30 86" +
            "30 88 30 89 30 8A 30 8B 30 8C 30 8D 30 8F 30 92" +
            "30 93 30 30 00 30 00 38 00 30 00 01 00 0C 00 05" +
            "00 35 00 00 00 00 00 00 00 4B 30               ";


    /**
     * hex dump from UnicodeStringFailCase2.xls atatched to Bugzilla 50779
     */
    private static final String data_50779_2 =
            //"Offset=0x00000612(1554) recno=71 sid=0x00FC size=0x2020(8224)\n" +
            "      FC 00 20 20 51 00 00 00 51 00 00 00 32 00" +
            "01 42 30 44 30 46 30 48 30 4A 30 4B 30 4D 30 4F" +
            "30 51 30 53 30 55 30 57 30 59 30 5B 30 5D 30 5F" +
            "30 61 30 64 30 66 30 68 30 6A 30 6B 30 6C 30 6D" +
            "30 6E 30 6F 30 72 30 75 30 78 30 7B 30 7E 30 7F" +
            "30 80 30 81 30 82 30 84 30 86 30 88 30 89 30 8A" +
            "30 8B 30 8C 30 8D 30 8F 30 92 30 93 30 30 00 30" +
            "00 30 00 31 00 32 00 01 42 30 44 30 46 30 48 30" +
            "4A 30 4B 30 4D 30 4F 30 51 30 53 30 55 30 57 30" +
            "59 30 5B 30 5D 30 5F 30 61 30 64 30 66 30 68 30" +
            "6A 30 6B 30 6C 30 6D 30 6E 30 6F 30 72 30 75 30" +
            "78 30 7B 30 7E 30 7F 30 80 30 81 30 82 30 84 30" +
            "86 30 88 30 89 30 8A 30 8B 30 8C 30 8D 30 8F 30" +
            "92 30 93 30 30 00 30 00 30 00 32 00 32 00 01 42" +
            "30 44 30 46 30 48 30 4A 30 4B 30 4D 30 4F 30 51" +
            "30 53 30 55 30 57 30 59 30 5B 30 5D 30 5F 30 61" +
            "30 64 30 66 30 68 30 6A 30 6B 30 6C 30 6D 30 6E" +
            "30 6F 30 72 30 75 30 78 30 7B 30 7E 30 7F 30 80" +
            "30 81 30 82 30 84 30 86 30 88 30 89 30 8A 30 8B" +
            "30 8C 30 8D 30 8F 30 92 30 93 30 30 00 30 00 30" +
            "00 33 00 32 00 01 42 30 44 30 46 30 48 30 4A 30" +
            "4B 30 4D 30 4F 30 51 30 53 30 55 30 57 30 59 30" +
            "5B 30 5D 30 5F 30 61 30 64 30 66 30 68 30 6A 30" +
            "6B 30 6C 30 6D 30 6E 30 6F 30 72 30 75 30 78 30" +
            "7B 30 7E 30 7F 30 80 30 81 30 82 30 84 30 86 30" +
            "88 30 89 30 8A 30 8B 30 8C 30 8D 30 8F 30 92 30" +
            "93 30 30 00 30 00 30 00 34 00 32 00 01 42 30 44" +
            "30 46 30 48 30 4A 30 4B 30 4D 30 4F 30 51 30 53" +
            "30 55 30 57 30 59 30 5B 30 5D 30 5F 30 61 30 64" +
            "30 66 30 68 30 6A 30 6B 30 6C 30 6D 30 6E 30 6F" +
            "30 72 30 75 30 78 30 7B 30 7E 30 7F 30 80 30 81" +
            "30 82 30 84 30 86 30 88 30 89 30 8A 30 8B 30 8C" +
            "30 8D 30 8F 30 92 30 93 30 30 00 30 00 30 00 35" +
            "00 32 00 01 42 30 44 30 46 30 48 30 4A 30 4B 30" +
            "4D 30 4F 30 51 30 53 30 55 30 57 30 59 30 5B 30" +
            "5D 30 5F 30 61 30 64 30 66 30 68 30 6A 30 6B 30" +
            "6C 30 6D 30 6E 30 6F 30 72 30 75 30 78 30 7B 30" +
            "7E 30 7F 30 80 30 81 30 82 30 84 30 86 30 88 30" +
            "89 30 8A 30 8B 30 8C 30 8D 30 8F 30 92 30 93 30" +
            "30 00 30 00 30 00 36 00 32 00 01 42 30 44 30 46" +
            "30 48 30 4A 30 4B 30 4D 30 4F 30 51 30 53 30 55" +
            "30 57 30 59 30 5B 30 5D 30 5F 30 61 30 64 30 66" +
            "30 68 30 6A 30 6B 30 6C 30 6D 30 6E 30 6F 30 72" +
            "30 75 30 78 30 7B 30 7E 30 7F 30 80 30 81 30 82" +
            "30 84 30 86 30 88 30 89 30 8A 30 8B 30 8C 30 8D" +
            "30 8F 30 92 30 93 30 30 00 30 00 30 00 37 00 32" +
            "00 01 42 30 44 30 46 30 48 30 4A 30 4B 30 4D 30" +
            "4F 30 51 30 53 30 55 30 57 30 59 30 5B 30 5D 30" +
            "5F 30 61 30 64 30 66 30 68 30 6A 30 6B 30 6C 30" +
            "6D 30 6E 30 6F 30 72 30 75 30 78 30 7B 30 7E 30" +
            "7F 30 80 30 81 30 82 30 84 30 86 30 88 30 89 30" +
            "8A 30 8B 30 8C 30 8D 30 8F 30 92 30 93 30 30 00" +
            "30 00 30 00 38 00 32 00 01 42 30 44 30 46 30 48" +
            "30 4A 30 4B 30 4D 30 4F 30 51 30 53 30 55 30 57" +
            "30 59 30 5B 30 5D 30 5F 30 61 30 64 30 66 30 68" +
            "30 6A 30 6B 30 6C 30 6D 30 6E 30 6F 30 72 30 75" +
            "30 78 30 7B 30 7E 30 7F 30 80 30 81 30 82 30 84" +
            "30 86 30 88 30 89 30 8A 30 8B 30 8C 30 8D 30 8F" +
            "30 92 30 93 30 30 00 30 00 30 00 39 00 32 00 01" +
            "42 30 44 30 46 30 48 30 4A 30 4B 30 4D 30 4F 30" +
            "51 30 53 30 55 30 57 30 59 30 5B 30 5D 30 5F 30" +
            "61 30 64 30 66 30 68 30 6A 30 6B 30 6C 30 6D 30" +
            "6E 30 6F 30 72 30 75 30 78 30 7B 30 7E 30 7F 30" +
            "80 30 81 30 82 30 84 30 86 30 88 30 89 30 8A 30" +
            "8B 30 8C 30 8D 30 8F 30 92 30 93 30 30 00 30 00" +
            "31 00 30 00 32 00 01 42 30 44 30 46 30 48 30 4A" +
            "30 4B 30 4D 30 4F 30 51 30 53 30 55 30 57 30 59" +
            "30 5B 30 5D 30 5F 30 61 30 64 30 66 30 68 30 6A" +
            "30 6B 30 6C 30 6D 30 6E 30 6F 30 72 30 75 30 78" +
            "30 7B 30 7E 30 7F 30 80 30 81 30 82 30 84 30 86" +
            "30 88 30 89 30 8A 30 8B 30 8C 30 8D 30 8F 30 92" +
            "30 93 30 30 00 30 00 31 00 31 00 32 00 01 42 30" +
            "44 30 46 30 48 30 4A 30 4B 30 4D 30 4F 30 51 30" +
            "53 30 55 30 57 30 59 30 5B 30 5D 30 5F 30 61 30" +
            "64 30 66 30 68 30 6A 30 6B 30 6C 30 6D 30 6E 30" +
            "6F 30 72 30 75 30 78 30 7B 30 7E 30 7F 30 80 30" +
            "81 30 82 30 84 30 86 30 88 30 89 30 8A 30 8B 30" +
            "8C 30 8D 30 8F 30 92 30 93 30 30 00 30 00 31 00" +
            "32 00 32 00 01 42 30 44 30 46 30 48 30 4A 30 4B" +
            "30 4D 30 4F 30 51 30 53 30 55 30 57 30 59 30 5B" +
            "30 5D 30 5F 30 61 30 64 30 66 30 68 30 6A 30 6B" +
            "30 6C 30 6D 30 6E 30 6F 30 72 30 75 30 78 30 7B" +
            "30 7E 30 7F 30 80 30 81 30 82 30 84 30 86 30 88" +
            "30 89 30 8A 30 8B 30 8C 30 8D 30 8F 30 92 30 93" +
            "30 30 00 30 00 31 00 33 00 32 00 01 42 30 44 30" +
            "46 30 48 30 4A 30 4B 30 4D 30 4F 30 51 30 53 30" +
            "55 30 57 30 59 30 5B 30 5D 30 5F 30 61 30 64 30" +
            "66 30 68 30 6A 30 6B 30 6C 30 6D 30 6E 30 6F 30" +
            "72 30 75 30 78 30 7B 30 7E 30 7F 30 80 30 81 30" +
            "82 30 84 30 86 30 88 30 89 30 8A 30 8B 30 8C 30" +
            "8D 30 8F 30 92 30 93 30 30 00 30 00 31 00 34 00" +
            "32 00 01 42 30 44 30 46 30 48 30 4A 30 4B 30 4D" +
            "30 4F 30 51 30 53 30 55 30 57 30 59 30 5B 30 5D" +
            "30 5F 30 61 30 64 30 66 30 68 30 6A 30 6B 30 6C" +
            "30 6D 30 6E 30 6F 30 72 30 75 30 78 30 7B 30 7E" +
            "30 7F 30 80 30 81 30 82 30 84 30 86 30 88 30 89" +
            "30 8A 30 8B 30 8C 30 8D 30 8F 30 92 30 93 30 30" +
            "00 30 00 31 00 35 00 32 00 01 42 30 44 30 46 30" +
            "48 30 4A 30 4B 30 4D 30 4F 30 51 30 53 30 55 30" +
            "57 30 59 30 5B 30 5D 30 5F 30 61 30 64 30 66 30" +
            "68 30 6A 30 6B 30 6C 30 6D 30 6E 30 6F 30 72 30" +
            "75 30 78 30 7B 30 7E 30 7F 30 80 30 81 30 82 30" +
            "84 30 86 30 88 30 89 30 8A 30 8B 30 8C 30 8D 30" +
            "8F 30 92 30 93 30 30 00 30 00 31 00 36 00 32 00" +
            "01 42 30 44 30 46 30 48 30 4A 30 4B 30 4D 30 4F" +
            "30 51 30 53 30 55 30 57 30 59 30 5B 30 5D 30 5F" +
            "30 61 30 64 30 66 30 68 30 6A 30 6B 30 6C 30 6D" +
            "30 6E 30 6F 30 72 30 75 30 78 30 7B 30 7E 30 7F" +
            "30 80 30 81 30 82 30 84 30 86 30 88 30 89 30 8A" +
            "30 8B 30 8C 30 8D 30 8F 30 92 30 93 30 30 00 30" +
            "00 31 00 37 00 32 00 01 42 30 44 30 46 30 48 30" +
            "4A 30 4B 30 4D 30 4F 30 51 30 53 30 55 30 57 30" +
            "59 30 5B 30 5D 30 5F 30 61 30 64 30 66 30 68 30" +
            "6A 30 6B 30 6C 30 6D 30 6E 30 6F 30 72 30 75 30" +
            "78 30 7B 30 7E 30 7F 30 80 30 81 30 82 30 84 30" +
            "86 30 88 30 89 30 8A 30 8B 30 8C 30 8D 30 8F 30" +
            "92 30 93 30 30 00 30 00 31 00 38 00 32 00 01 42" +
            "30 44 30 46 30 48 30 4A 30 4B 30 4D 30 4F 30 51" +
            "30 53 30 55 30 57 30 59 30 5B 30 5D 30 5F 30 61" +
            "30 64 30 66 30 68 30 6A 30 6B 30 6C 30 6D 30 6E" +
            "30 6F 30 72 30 75 30 78 30 7B 30 7E 30 7F 30 80" +
            "30 81 30 82 30 84 30 86 30 88 30 89 30 8A 30 8B" +
            "30 8C 30 8D 30 8F 30 92 30 93 30 30 00 30 00 31" +
            "00 39 00 32 00 01 42 30 44 30 46 30 48 30 4A 30" +
            "4B 30 4D 30 4F 30 51 30 53 30 55 30 57 30 59 30" +
            "5B 30 5D 30 5F 30 61 30 64 30 66 30 68 30 6A 30" +
            "6B 30 6C 30 6D 30 6E 30 6F 30 72 30 75 30 78 30" +
            "7B 30 7E 30 7F 30 80 30 81 30 82 30 84 30 86 30" +
            "88 30 89 30 8A 30 8B 30 8C 30 8D 30 8F 30 92 30" +
            "93 30 30 00 30 00 32 00 30 00 32 00 01 42 30 44" +
            "30 46 30 48 30 4A 30 4B 30 4D 30 4F 30 51 30 53" +
            "30 55 30 57 30 59 30 5B 30 5D 30 5F 30 61 30 64" +
            "30 66 30 68 30 6A 30 6B 30 6C 30 6D 30 6E 30 6F" +
            "30 72 30 75 30 78 30 7B 30 7E 30 7F 30 80 30 81" +
            "30 82 30 84 30 86 30 88 30 89 30 8A 30 8B 30 8C" +
            "30 8D 30 8F 30 92 30 93 30 30 00 30 00 32 00 31" +
            "00 32 00 01 42 30 44 30 46 30 48 30 4A 30 4B 30" +
            "4D 30 4F 30 51 30 53 30 55 30 57 30 59 30 5B 30" +
            "5D 30 5F 30 61 30 64 30 66 30 68 30 6A 30 6B 30" +
            "6C 30 6D 30 6E 30 6F 30 72 30 75 30 78 30 7B 30" +
            "7E 30 7F 30 80 30 81 30 82 30 84 30 86 30 88 30" +
            "89 30 8A 30 8B 30 8C 30 8D 30 8F 30 92 30 93 30" +
            "30 00 30 00 32 00 32 00 32 00 01 42 30 44 30 46" +
            "30 48 30 4A 30 4B 30 4D 30 4F 30 51 30 53 30 55" +
            "30 57 30 59 30 5B 30 5D 30 5F 30 61 30 64 30 66" +
            "30 68 30 6A 30 6B 30 6C 30 6D 30 6E 30 6F 30 72" +
            "30 75 30 78 30 7B 30 7E 30 7F 30 80 30 81 30 82" +
            "30 84 30 86 30 88 30 89 30 8A 30 8B 30 8C 30 8D" +
            "30 8F 30 92 30 93 30 30 00 30 00 32 00 33 00 32" +
            "00 01 42 30 44 30 46 30 48 30 4A 30 4B 30 4D 30" +
            "4F 30 51 30 53 30 55 30 57 30 59 30 5B 30 5D 30" +
            "5F 30 61 30 64 30 66 30 68 30 6A 30 6B 30 6C 30" +
            "6D 30 6E 30 6F 30 72 30 75 30 78 30 7B 30 7E 30" +
            "7F 30 80 30 81 30 82 30 84 30 86 30 88 30 89 30" +
            "8A 30 8B 30 8C 30 8D 30 8F 30 92 30 93 30 30 00" +
            "30 00 32 00 34 00 32 00 01 42 30 44 30 46 30 48" +
            "30 4A 30 4B 30 4D 30 4F 30 51 30 53 30 55 30 57" +
            "30 59 30 5B 30 5D 30 5F 30 61 30 64 30 66 30 68" +
            "30 6A 30 6B 30 6C 30 6D 30 6E 30 6F 30 72 30 75" +
            "30 78 30 7B 30 7E 30 7F 30 80 30 81 30 82 30 84" +
            "30 86 30 88 30 89 30 8A 30 8B 30 8C 30 8D 30 8F" +
            "30 92 30 93 30 30 00 30 00 32 00 35 00 32 00 01" +
            "42 30 44 30 46 30 48 30 4A 30 4B 30 4D 30 4F 30" +
            "51 30 53 30 55 30 57 30 59 30 5B 30 5D 30 5F 30" +
            "61 30 64 30 66 30 68 30 6A 30 6B 30 6C 30 6D 30" +
            "6E 30 6F 30 72 30 75 30 78 30 7B 30 7E 30 7F 30" +
            "80 30 81 30 82 30 84 30 86 30 88 30 89 30 8A 30" +
            "8B 30 8C 30 8D 30 8F 30 92 30 93 30 30 00 30 00" +
            "32 00 36 00 32 00 01 42 30 44 30 46 30 48 30 4A" +
            "30 4B 30 4D 30 4F 30 51 30 53 30 55 30 57 30 59" +
            "30 5B 30 5D 30 5F 30 61 30 64 30 66 30 68 30 6A" +
            "30 6B 30 6C 30 6D 30 6E 30 6F 30 72 30 75 30 78" +
            "30 7B 30 7E 30 7F 30 80 30 81 30 82 30 84 30 86" +
            "30 88 30 89 30 8A 30 8B 30 8C 30 8D 30 8F 30 92" +
            "30 93 30 30 00 30 00 32 00 37 00 32 00 01 42 30" +
            "44 30 46 30 48 30 4A 30 4B 30 4D 30 4F 30 51 30" +
            "53 30 55 30 57 30 59 30 5B 30 5D 30 5F 30 61 30" +
            "64 30 66 30 68 30 6A 30 6B 30 6C 30 6D 30 6E 30" +
            "6F 30 72 30 75 30 78 30 7B 30 7E 30 7F 30 80 30" +
            "81 30 82 30 84 30 86 30 88 30 89 30 8A 30 8B 30" +
            "8C 30 8D 30 8F 30 92 30 93 30 30 00 30 00 32 00" +
            "38 00 32 00 01 42 30 44 30 46 30 48 30 4A 30 4B" +
            "30 4D 30 4F 30 51 30 53 30 55 30 57 30 59 30 5B" +
            "30 5D 30 5F 30 61 30 64 30 66 30 68 30 6A 30 6B" +
            "30 6C 30 6D 30 6E 30 6F 30 72 30 75 30 78 30 7B" +
            "30 7E 30 7F 30 80 30 81 30 82 30 84 30 86 30 88" +
            "30 89 30 8A 30 8B 30 8C 30 8D 30 8F 30 92 30 93" +
            "30 30 00 30 00 32 00 39 00 32 00 01 42 30 44 30" +
            "46 30 48 30 4A 30 4B 30 4D 30 4F 30 51 30 53 30" +
            "55 30 57 30 59 30 5B 30 5D 30 5F 30 61 30 64 30" +
            "66 30 68 30 6A 30 6B 30 6C 30 6D 30 6E 30 6F 30" +
            "72 30 75 30 78 30 7B 30 7E 30 7F 30 80 30 81 30" +
            "82 30 84 30 86 30 88 30 89 30 8A 30 8B 30 8C 30" +
            "8D 30 8F 30 92 30 93 30 30 00 30 00 33 00 30 00" +
            "32 00 01 42 30 44 30 46 30 48 30 4A 30 4B 30 4D" +
            "30 4F 30 51 30 53 30 55 30 57 30 59 30 5B 30 5D" +
            "30 5F 30 61 30 64 30 66 30 68 30 6A 30 6B 30 6C" +
            "30 6D 30 6E 30 6F 30 72 30 75 30 78 30 7B 30 7E" +
            "30 7F 30 80 30 81 30 82 30 84 30 86 30 88 30 89" +
            "30 8A 30 8B 30 8C 30 8D 30 8F 30 92 30 93 30 30" +
            "00 30 00 33 00 31 00 32 00 01 42 30 44 30 46 30" +
            "48 30 4A 30 4B 30 4D 30 4F 30 51 30 53 30 55 30" +
            "57 30 59 30 5B 30 5D 30 5F 30 61 30 64 30 66 30" +
            "68 30 6A 30 6B 30 6C 30 6D 30 6E 30 6F 30 72 30" +
            "75 30 78 30 7B 30 7E 30 7F 30 80 30 81 30 82 30" +
            "84 30 86 30 88 30 89 30 8A 30 8B 30 8C 30 8D 30" +
            "8F 30 92 30 93 30 30 00 30 00 33 00 32 00 32 00" +
            "01 42 30 44 30 46 30 48 30 4A 30 4B 30 4D 30 4F" +
            "30 51 30 53 30 55 30 57 30 59 30 5B 30 5D 30 5F" +
            "30 61 30 64 30 66 30 68 30 6A 30 6B 30 6C 30 6D" +
            "30 6E 30 6F 30 72 30 75 30 78 30 7B 30 7E 30 7F" +
            "30 80 30 81 30 82 30 84 30 86 30 88 30 89 30 8A" +
            "30 8B 30 8C 30 8D 30 8F 30 92 30 93 30 30 00 30" +
            "00 33 00 33 00 32 00 01 42 30 44 30 46 30 48 30" +
            "4A 30 4B 30 4D 30 4F 30 51 30 53 30 55 30 57 30" +
            "59 30 5B 30 5D 30 5F 30 61 30 64 30 66 30 68 30" +
            "6A 30 6B 30 6C 30 6D 30 6E 30 6F 30 72 30 75 30" +
            "78 30 7B 30 7E 30 7F 30 80 30 81 30 82 30 84 30" +
            "86 30 88 30 89 30 8A 30 8B 30 8C 30 8D 30 8F 30" +
            "92 30 93 30 30 00 30 00 33 00 34 00 32 00 01 42" +
            "30 44 30 46 30 48 30 4A 30 4B 30 4D 30 4F 30 51" +
            "30 53 30 55 30 57 30 59 30 5B 30 5D 30 5F 30 61" +
            "30 64 30 66 30 68 30 6A 30 6B 30 6C 30 6D 30 6E" +
            "30 6F 30 72 30 75 30 78 30 7B 30 7E 30 7F 30 80" +
            "30 81 30 82 30 84 30 86 30 88 30 89 30 8A 30 8B" +
            "30 8C 30 8D 30 8F 30 92 30 93 30 30 00 30 00 33" +
            "00 35 00 32 00 01 42 30 44 30 46 30 48 30 4A 30" +
            "4B 30 4D 30 4F 30 51 30 53 30 55 30 57 30 59 30" +
            "5B 30 5D 30 5F 30 61 30 64 30 66 30 68 30 6A 30" +
            "6B 30 6C 30 6D 30 6E 30 6F 30 72 30 75 30 78 30" +
            "7B 30 7E 30 7F 30 80 30 81 30 82 30 84 30 86 30" +
            "88 30 89 30 8A 30 8B 30 8C 30 8D 30 8F 30 92 30" +
            "93 30 30 00 30 00 33 00 36 00 32 00 01 42 30 44" +
            "30 46 30 48 30 4A 30 4B 30 4D 30 4F 30 51 30 53" +
            "30 55 30 57 30 59 30 5B 30 5D 30 5F 30 61 30 64" +
            "30 66 30 68 30 6A 30 6B 30 6C 30 6D 30 6E 30 6F" +
            "30 72 30 75 30 78 30 7B 30 7E 30 7F 30 80 30 81" +
            "30 82 30 84 30 86 30 88 30 89 30 8A 30 8B 30 8C" +
            "30 8D 30 8F 30 92 30 93 30 30 00 30 00 33 00 37" +
            "00 32 00 01 42 30 44 30 46 30 48 30 4A 30 4B 30" +
            "4D 30 4F 30 51 30 53 30 55 30 57 30 59 30 5B 30" +
            "5D 30 5F 30 61 30 64 30 66 30 68 30 6A 30 6B 30" +
            "6C 30 6D 30 6E 30 6F 30 72 30 75 30 78 30 7B 30" +
            "7E 30 7F 30 80 30 81 30 82 30 84 30 86 30 88 30" +
            "89 30 8A 30 8B 30 8C 30 8D 30 8F 30 92 30 93 30" +
            "30 00 30 00 33 00 38 00 32 00 01 42 30 44 30 46" +
            "30 48 30 4A 30 4B 30 4D 30 4F 30 51 30 53 30 55" +
            "30 57 30 59 30 5B 30 5D 30 5F 30 61 30 64 30 66" +
            "30 68 30 6A 30 6B 30 6C 30 6D 30 6E 30 6F 30 72" +
            "30 75 30 78 30 7B 30 7E 30 7F 30 80 30 81 30 82" +
            "30 84 30 86 30 88 30 89 30 8A 30 8B 30 8C 30 8D" +
            "30 8F 30 92 30 93 30 30 00 30 00 33 00 39 00 32" +
            "00 01 42 30 44 30 46 30 48 30 4A 30 4B 30 4D 30" +
            "4F 30 51 30 53 30 55 30 57 30 59 30 5B 30 5D 30" +
            "5F 30 61 30 64 30 66 30 68 30 6A 30 6B 30 6C 30" +
            "6D 30 6E 30 6F 30 72 30 75 30 78 30 7B 30 7E 30" +
            "7F 30 80 30 81 30 82 30 84 30 86 30 88 30 89 30" +
            "8A 30 8B 30 8C 30 8D 30 8F 30 92 30 93 30 30 00" +
            "30 00 34 00 30 00 32 00 01 42 30 44 30 46 30 48" +
            "30 4A 30 4B 30 4D 30 4F 30 51 30 53 30 55 30 57" +
            "30 59 30 5B 30 5D 30 5F 30 61 30 64 30 66 30 68" +
            "30 6A 30 6B 30 6C 30 6D 30 6E 30 6F 30 72 30 75" +
            "30 78 30 7B 30 7E 30 7F 30 80 30 81 30 82 30 84" +
            "30 86 30 88 30 89 30 8A 30 8B 30 8C 30 8D 30 8F" +
            "30 92 30 93 30 30 00 30 00 34 00 31 00 32 00 01" +
            "42 30 44 30 46 30 48 30 4A 30 4B 30 4D 30 4F 30" +
            "51 30 53 30 55 30 57 30 59 30 5B 30 5D 30 5F 30" +
            "61 30 64 30 66 30 68 30 6A 30 6B 30 6C 30 6D 30" +
            "6E 30 6F 30 72 30 75 30 78 30 7B 30 7E 30 7F 30" +
            "80 30 81 30 82 30 84 30 86 30 88 30 89 30 8A 30" +
            "8B 30 8C 30 8D 30 8F 30 92 30 93 30 30 00 30 00" +
            "34 00 32 00 32 00 01 42 30 44 30 46 30 48 30 4A" +
            "30 4B 30 4D 30 4F 30 51 30 53 30 55 30 57 30 59" +
            "30 5B 30 5D 30 5F 30 61 30 64 30 66 30 68 30 6A" +
            "30 6B 30 6C 30 6D 30 6E 30 6F 30 72 30 75 30 78" +
            "30 7B 30 7E 30 7F 30 80 30 81 30 82 30 84 30 86" +
            "30 88 30 89 30 8A 30 8B 30 8C 30 8D 30 8F 30 92" +
            "30 93 30 30 00 30 00 34 00 33 00 32 00 01 42 30" +
            "44 30 46 30 48 30 4A 30 4B 30 4D 30 4F 30 51 30" +
            "53 30 55 30 57 30 59 30 5B 30 5D 30 5F 30 61 30" +
            "64 30 66 30 68 30 6A 30 6B 30 6C 30 6D 30 6E 30" +
            "6F 30 72 30 75 30 78 30 7B 30 7E 30 7F 30 80 30" +
            "81 30 82 30 84 30 86 30 88 30 89 30 8A 30 8B 30" +
            "8C 30 8D 30 8F 30 92 30 93 30 30 00 30 00 34 00" +
            "34 00 32 00 01 42 30 44 30 46 30 48 30 4A 30 4B" +
            "30 4D 30 4F 30 51 30 53 30 55 30 57 30 59 30 5B" +
            "30 5D 30 5F 30 61 30 64 30 66 30 68 30 6A 30 6B" +
            "30 6C 30 6D 30 6E 30 6F 30 72 30 75 30 78 30 7B" +
            "30 7E 30 7F 30 80 30 81 30 82 30 84 30 86 30 88" +
            "30 89 30 8A 30 8B 30 8C 30 8D 30 8F 30 92 30 93" +
            "30 30 00 30 00 34 00 35 00 32 00 01 42 30 44 30" +
            "46 30 48 30 4A 30 4B 30 4D 30 4F 30 51 30 53 30" +
            "55 30 57 30 59 30 5B 30 5D 30 5F 30 61 30 64 30" +
            "66 30 68 30 6A 30 6B 30 6C 30 6D 30 6E 30 6F 30" +
            "72 30 75 30 78 30 7B 30 7E 30 7F 30 80 30 81 30" +
            "82 30 84 30 86 30 88 30 89 30 8A 30 8B 30 8C 30" +
            "8D 30 8F 30 92 30 93 30 30 00 30 00 34 00 36 00" +
            "32 00 01 42 30 44 30 46 30 48 30 4A 30 4B 30 4D" +
            "30 4F 30 51 30 53 30 55 30 57 30 59 30 5B 30 5D" +
            "30 5F 30 61 30 64 30 66 30 68 30 6A 30 6B 30 6C" +
            "30 6D 30 6E 30 6F 30 72 30 75 30 78 30 7B 30 7E" +
            "30 7F 30 80 30 81 30 82 30 84 30 86 30 88 30 89" +
            "30 8A 30 8B 30 8C 30 8D 30 8F 30 92 30 93 30 30" +
            "00 30 00 34 00 37 00 32 00 01 42 30 44 30 46 30" +
            "48 30 4A 30 4B 30 4D 30 4F 30 51 30 53 30 55 30" +
            "57 30 59 30 5B 30 5D 30 5F 30 61 30 64 30 66 30" +
            "68 30 6A 30 6B 30 6C 30 6D 30 6E 30 6F 30 72 30" +
            "75 30 78 30 7B 30 7E 30 7F 30 80 30 81 30 82 30" +
            "84 30 86 30 88 30 89 30 8A 30 8B 30 8C 30 8D 30" +
            "8F 30 92 30 93 30 30 00 30 00 34 00 38 00 32 00" +
            "01 42 30 44 30 46 30 48 30 4A 30 4B 30 4D 30 4F" +
            "30 51 30 53 30 55 30 57 30 59 30 5B 30 5D 30 5F" +
            "30 61 30 64 30 66 30 68 30 6A 30 6B 30 6C 30 6D" +
            "30 6E 30 6F 30 72 30 75 30 78 30 7B 30 7E 30 7F" +
            "30 80 30 81 30 82 30 84 30 86 30 88 30 89 30 8A" +
            "30 8B 30 8C 30 8D 30 8F 30 92 30 93 30 30 00 30" +
            "00 34 00 39 00 32 00 01 42 30 44 30 46 30 48 30" +
            "4A 30 4B 30 4D 30 4F 30 51 30 53 30 55 30 57 30" +
            "59 30 5B 30 5D 30 5F 30 61 30 64 30 66 30 68 30" +
            "6A 30 6B 30 6C 30 6D 30 6E 30 6F 30 72 30 75 30" +
            "78 30 7B 30 7E 30 7F 30 80 30 81 30 82 30 84 30" +
            "86 30 88 30 89 30 8A 30 8B 30 8C 30 8D 30 8F 30" +
            "92 30 93 30 30 00 30 00 35 00 30 00 32 00 01 42" +
            "30 44 30 46 30 48 30 4A 30 4B 30 4D 30 4F 30 51" +
            "30 53 30 55 30 57 30 59 30 5B 30 5D 30 5F 30 61" +
            "30 64 30 66 30 68 30 6A 30 6B 30 6C 30 6D 30 6E" +
            "30 6F 30 72 30 75 30 78 30 7B 30 7E 30 7F 30 80" +
            "30 81 30 82 30 84 30 86 30 88 30 89 30 8A 30 8B" +
            "30 8C 30 8D 30 8F 30 92 30 93 30 30 00 30 00 35" +
            "00 31 00 32 00 01 42 30 44 30 46 30 48 30 4A 30" +
            "4B 30 4D 30 4F 30 51 30 53 30 55 30 57 30 59 30" +
            "5B 30 5D 30 5F 30 61 30 64 30 66 30 68 30 6A 30" +
            "6B 30 6C 30 6D 30 6E 30 6F 30 72 30 75 30 78 30" +
            "7B 30 7E 30 7F 30 80 30 81 30 82 30 84 30 86 30" +
            "88 30 89 30 8A 30 8B 30 8C 30 8D 30 8F 30 92 30" +
            "93 30 30 00 30 00 35 00 32 00 32 00 01 42 30 44" +
            "30 46 30 48 30 4A 30 4B 30 4D 30 4F 30 51 30 53" +
            "30 55 30 57 30 59 30 5B 30 5D 30 5F 30 61 30 64" +
            "30 66 30 68 30 6A 30 6B 30 6C 30 6D 30 6E 30 6F" +
            "30 72 30 75 30 78 30 7B 30 7E 30 7F 30 80 30 81" +
            "30 82 30 84 30 86 30 88 30 89 30 8A 30 8B 30 8C" +
            "30 8D 30 8F 30 92 30 93 30 30 00 30 00 35 00 33" +
            "00 32 00 01 42 30 44 30 46 30 48 30 4A 30 4B 30" +
            "4D 30 4F 30 51 30 53 30 55 30 57 30 59 30 5B 30" +
            "5D 30 5F 30 61 30 64 30 66 30 68 30 6A 30 6B 30" +
            "6C 30 6D 30 6E 30 6F 30 72 30 75 30 78 30 7B 30" +
            "7E 30 7F 30 80 30 81 30 82 30 84 30 86 30 88 30" +
            "89 30 8A 30 8B 30 8C 30 8D 30 8F 30 92 30 93 30" +
            "30 00 30 00 35 00 34 00 32 00 01 42 30 44 30 46" +
            "30 48 30 4A 30 4B 30 4D 30 4F 30 51 30 53 30 55" +
            "30 57 30 59 30 5B 30 5D 30 5F 30 61 30 64 30 66" +
            "30 68 30 6A 30 6B 30 6C 30 6D 30 6E 30 6F 30 72" +
            "30 75 30 78 30 7B 30 7E 30 7F 30 80 30 81 30 82" +
            "30 84 30 86 30 88 30 89 30 8A 30 8B 30 8C 30 8D" +
            "30 8F 30 92 30 93 30 30 00 30 00 35 00 35 00 32" +
            "00 01 42 30 44 30 46 30 48 30 4A 30 4B 30 4D 30" +
            "4F 30 51 30 53 30 55 30 57 30 59 30 5B 30 5D 30" +
            "5F 30 61 30 64 30 66 30 68 30 6A 30 6B 30 6C 30" +
            "6D 30 6E 30 6F 30 72 30 75 30 78 30 7B 30 7E 30" +
            "7F 30 80 30 81 30 82 30 84 30 86 30 88 30 89 30" +
            "8A 30 8B 30 8C 30 8D 30 8F 30 92 30 93 30 30 00" +
            "30 00 35 00 36 00 32 00 01 42 30 44 30 46 30 48" +
            "30 4A 30 4B 30 4D 30 4F 30 51 30 53 30 55 30 57" +
            "30 59 30 5B 30 5D 30 5F 30 61 30 64 30 66 30 68" +
            "30 6A 30 6B 30 6C 30 6D 30 6E 30 6F 30 72 30 75" +
            "30 78 30 7B 30 7E 30 7F 30 80 30 81 30 82 30 84" +
            "30 86 30 88 30 89 30 8A 30 8B 30 8C 30 8D 30 8F" +
            "30 92 30 93 30 30 00 30 00 35 00 37 00 32 00 01" +
            "42 30 44 30 46 30 48 30 4A 30 4B 30 4D 30 4F 30" +
            "51 30 53 30 55 30 57 30 59 30 5B 30 5D 30 5F 30" +
            "61 30 64 30 66 30 68 30 6A 30 6B 30 6C 30 6D 30" +
            "6E 30 6F 30 72 30 75 30 78 30 7B 30 7E 30 7F 30" +
            "80 30 81 30 82 30 84 30 86 30 88 30 89 30 8A 30" +
            "8B 30 8C 30 8D 30 8F 30 92 30 93 30 30 00 30 00" +
            "35 00 38 00 32 00 01 42 30 44 30 46 30 48 30 4A" +
            "30 4B 30 4D 30 4F 30 51 30 53 30 55 30 57 30 59" +
            "30 5B 30 5D 30 5F 30 61 30 64 30 66 30 68 30 6A" +
            "30 6B 30 6C 30 6D 30 6E 30 6F 30 72 30 75 30 78" +
            "30 7B 30 7E 30 7F 30 80 30 81 30 82 30 84 30 86" +
            "30 88 30 89 30 8A 30 8B 30 8C 30 8D 30 8F 30 92" +
            "30 93 30 30 00 30 00 35 00 39 00 32 00 01 42 30" +
            "44 30 46 30 48 30 4A 30 4B 30 4D 30 4F 30 51 30" +
            "53 30 55 30 57 30 59 30 5B 30 5D 30 5F 30 61 30" +
            "64 30 66 30 68 30 6A 30 6B 30 6C 30 6D 30 6E 30" +
            "6F 30 72 30 75 30 78 30 7B 30 7E 30 7F 30 80 30" +
            "81 30 82 30 84 30 86 30 88 30 89 30 8A 30 8B 30" +
            "8C 30 8D 30 8F 30 92 30 93 30 30 00 30 00 36 00" +
            "30 00 32 00 01 42 30 44 30 46 30 48 30 4A 30 4B" +
            "30 4D 30 4F 30 51 30 53 30 55 30 57 30 59 30 5B" +
            "30 5D 30 5F 30 61 30 64 30 66 30 68 30 6A 30 6B" +
            "30 6C 30 6D 30 6E 30 6F 30 72 30 75 30 78 30 7B" +
            "30 7E 30 7F 30 80 30 81 30 82 30 84 30 86 30 88" +
            "30 89 30 8A 30 8B 30 8C 30 8D 30 8F 30 92 30 93" +
            "30 30 00 30 00 36 00 31 00 32 00 01 42 30 44 30" +
            "46 30 48 30 4A 30 4B 30 4D 30 4F 30 51 30 53 30" +
            "55 30 57 30 59 30 5B 30 5D 30 5F 30 61 30 64 30" +
            "66 30 68 30 6A 30 6B 30 6C 30 6D 30 6E 30 6F 30" +
            "72 30 75 30 78 30 7B 30 7E 30 7F 30 80 30 81 30" +
            "82 30 84 30 86 30 88 30 89 30 8A 30 8B 30 8C 30" +
            "8D 30 8F 30 92 30 93 30 30 00 30 00 36 00 32 00" +
            "32 00 01 42 30 44 30 46 30 48 30 4A 30 4B 30 4D" +
            "30 4F 30 51 30 53 30 55 30 57 30 59 30 5B 30 5D" +
            "30 5F 30 61 30 64 30 66 30 68 30 6A 30 6B 30 6C" +
            "30 6D 30 6E 30 6F 30 72 30 75 30 78 30 7B 30 7E" +
            "30 7F 30 80 30 81 30 82 30 84 30 86 30 88 30 89" +
            "30 8A 30 8B 30 8C 30 8D 30 8F 30 92 30 93 30 30" +
            "00 30 00 36 00 33 00 32 00 01 42 30 44 30 46 30" +
            "48 30 4A 30 4B 30 4D 30 4F 30 51 30 53 30 55 30" +
            "57 30 59 30 5B 30 5D 30 5F 30 61 30 64 30 66 30" +
            "68 30 6A 30 6B 30 6C 30 6D 30 6E 30 6F 30 72 30" +
            "75 30 78 30 7B 30 7E 30 7F 30 80 30 81 30 82 30" +
            "84 30 86 30 88 30 89 30 8A 30 8B 30 8C 30 8D 30" +
            "8F 30 92 30 93 30 30 00 30 00 36 00 34 00 32 00" +
            "01 42 30 44 30 46 30 48 30 4A 30 4B 30 4D 30 4F" +
            "30 51 30 53 30 55 30 57 30 59 30 5B 30 5D 30 5F" +
            "30 61 30 64 30 66 30 68 30 6A 30 6B 30 6C 30 6D" +
            "30 6E 30 6F 30 72 30 75 30 78 30 7B 30 7E 30 7F" +
            "30 80 30 81 30 82 30 84 30 86 30 88 30 89 30 8A" +
            "30 8B 30 8C 30 8D 30 8F 30 92 30 93 30 30 00 30" +
            "00 36 00 35 00 32 00 01 42 30 44 30 46 30 48 30" +
            "4A 30 4B 30 4D 30 4F 30 51 30 53 30 55 30 57 30" +
            "59 30 5B 30 5D 30 5F 30 61 30 64 30 66 30 68 30" +
            "6A 30 6B 30 6C 30 6D 30 6E 30 6F 30 72 30 75 30" +
            "78 30 7B 30 7E 30 7F 30 80 30 81 30 82 30 84 30" +
            "86 30 88 30 89 30 8A 30 8B 30 8C 30 8D 30 8F 30" +
            "92 30 93 30 30 00 30 00 36 00 36 00 32 00 01 42" +
            "30 44 30 46 30 48 30 4A 30 4B 30 4D 30 4F 30 51" +
            "30 53 30 55 30 57 30 59 30 5B 30 5D 30 5F 30 61" +
            "30 64 30 66 30 68 30 6A 30 6B 30 6C 30 6D 30 6E" +
            "30 6F 30 72 30 75 30 78 30 7B 30 7E 30 7F 30 80" +
            "30 81 30 82 30 84 30 86 30 88 30 89 30 8A 30 8B" +
            "30 8C 30 8D 30 8F 30 92 30 93 30 30 00 30 00 36" +
            "00 37 00 32 00 01 42 30 44 30 46 30 48 30 4A 30" +
            "4B 30 4D 30 4F 30 51 30 53 30 55 30 57 30 59 30" +
            "5B 30 5D 30 5F 30 61 30 64 30 66 30 68 30 6A 30" +
            "6B 30 6C 30 6D 30 6E 30 6F 30 72 30 75 30 78 30" +
            "7B 30 7E 30 7F 30 80 30 81 30 82 30 84 30 86 30" +
            "88 30 89 30 8A 30 8B 30 8C 30 8D 30 8F 30 92 30" +
            "93 30 30 00 30 00 36 00 38 00 32 00 01 42 30 44" +
            "30 46 30 48 30 4A 30 4B 30 4D 30 4F 30 51 30 53" +
            "30 55 30 57 30 59 30 5B 30 5D 30 5F 30 61 30 64" +
            "30 66 30 68 30 6A 30 6B 30 6C 30 6D 30 6E 30 6F" +
            "30 72 30 75 30 78 30 7B 30 7E 30 7F 30 80 30 81" +
            "30 82 30 84 30 86 30 88 30 89 30 8A 30 8B 30 8C" +
            "30 8D 30 8F 30 92 30 93 30 30 00 30 00 36 00 39" +
            "00 32 00 01 42 30 44 30 46 30 48 30 4A 30 4B 30" +
            "4D 30 4F 30 51 30 53 30 55 30 57 30 59 30 5B 30" +
            "5D 30 5F 30 61 30 64 30 66 30 68 30 6A 30 6B 30" +
            "6C 30 6D 30 6E 30 6F 30 72 30 75 30 78 30 7B 30" +
            "7E 30 7F 30 80 30 81 30 82 30 84 30 86 30 88 30" +
            "89 30 8A 30 8B 30 8C 30 8D 30 8F 30 92 30 93 30" +
            "30 00 30 00 37 00 30 00 32 00 01 42 30 44 30 46" +
            "30 48 30 4A 30 4B 30 4D 30 4F 30 51 30 53 30 55" +
            "30 57 30 59 30 5B 30 5D 30 5F 30 61 30 64 30 66" +
            "30 68 30 6A 30 6B 30 6C 30 6D 30 6E 30 6F 30 72" +
            "30 75 30 78 30 7B 30 7E 30 7F 30 80 30 81 30 82" +
            "30 84 30 86 30 88 30 89 30 8A 30 8B 30 8C 30 8D" +
            "30 8F 30 92 30 93 30 30 00 30 00 37 00 31 00 32" +
            "00 01 42 30 44 30 46 30 48 30 4A 30 4B 30 4D 30" +
            "4F 30 51 30 53 30 55 30 57 30 59 30 5B 30 5D 30" +
            "5F 30 61 30 64 30 66 30 68 30 6A 30 6B 30 6C 30" +
            "6D 30 6E 30 6F 30 72 30 75 30 78 30 7B 30 7E 30" +
            "7F 30 80 30 81 30 82 30 84 30 86 30 88 30 89 30" +
            "8A 30 8B 30 8C 30 8D 30 8F 30 92 30 93 30 30 00" +
            "30 00 37 00 32 00 32 00 01 42 30 44 30 46 30 48" +
            "30 4A 30 4B 30 4D 30 4F 30 51 30 53 30 55 30 57" +
            "30 59 30 5B 30 5D 30 5F 30 61 30 64 30 66 30 68" +
            "30 6A 30 6B 30 6C 30 6D 30 6E 30 6F 30 72 30 75" +
            "30 78 30 7B 30 7E 30 7F 30 80 30 81 30 82 30 84" +
            "30 86 30 88 30 89 30 8A 30 8B 30 8C 30 8D 30 8F" +
            "30 92 30 93 30 30 00 30 00 37 00 33 00 32 00 01" +
            "42 30 44 30 46 30 48 30 4A 30 4B 30 4D 30 4F 30" +
            "51 30 53 30 55 30 57 30 59 30 5B 30 5D 30 5F 30" +
            "61 30 64 30 66 30 68 30 6A 30 6B 30 6C 30 6D 30" +
            "6E 30 6F 30 72 30 75 30 78 30 7B 30 7E 30 7F 30" +
            "80 30 81 30 82 30 84 30 86 30 88 30 89 30 8A 30" +
            "8B 30 8C 30 8D 30 8F 30 92 30 93 30 30 00 30 00" +
            "37 00 34 00 32 00 01 42 30 44 30 46 30 48 30 4A" +
            "30 4B 30 4D 30 4F 30 51 30 53 30 55 30 57 30 59" +
            "30 5B 30 5D 30 5F 30 61 30 64 30 66 30 68 30 6A" +
            "30 6B 30 6C 30 6D 30 6E 30 6F 30 72 30 75 30 78" +
            "30 7B 30 7E 30 7F 30 80 30 81 30 82 30 84 30 86" +
            "30 88 30 89 30 8A 30 8B 30 8C 30 8D 30 8F 30 92" +
            "30 93 30 30 00 30 00 37 00 35 00 32 00 01 42 30" +
            "44 30 46 30 48 30 4A 30 4B 30 4D 30 4F 30 51 30" +
            "53 30 55 30 57 30 59 30 5B 30 5D 30 5F 30 61 30" +
            "64 30 66 30 68 30 6A 30 6B 30 6C 30 6D 30 6E 30" +
            "6F 30 72 30 75 30 78 30 7B 30 7E 30 7F 30 80 30" +
            "81 30 82 30 84 30 86 30 88 30 89 30 8A 30 8B 30" +
            "8C 30 8D 30 8F 30 92 30 93 30 30 00 30 00 37 00" +
            "36 00 32 00 01 42 30 44 30 46 30 48 30 4A 30 4B" +
            "30 4D 30 4F 30 51 30 53 30 55 30 57 30 59 30 5B" +
            "30 5D 30 5F 30 61 30 64 30 66 30 68 30 6A 30 6B" +
            "30 6C 30 6D 30 6E 30 6F 30 72 30 75 30 78 30 7B" +
            "30 7E 30 7F 30 80 30 81 30 82 30 84 30 86 30 88" +
            "30 89 30 8A 30 8B 30 8C 30 8D 30 8F 30 92 30 93" +
            "30 30 00 30 00 37 00 37 00 32 00 01 42 30 44 30" +
            "46 30 48 30 4A 30 4B 30 4D 30 4F 30 51 30 53 30" +
            "55 30 57 30 59 30 5B 30 5D 30 5F 30 61 30 64 30" +
            "66 30 68 30 6A 30 6B 30 6C 30 6D 30 6E 30 6F 30" +
            "72 30 75 30 78 30 7B 30 7E 30 7F 30 80 30 81 30" +
            "82 30 84 30 86 30 88 30 89 30 8A 30 8B 30 8C 30" +
            "8D 30 8F 30 92 30 93 30 30 00 30 00 37 00 38 00" +
            "18 00 05 96 00 00 00 17 53 77 6D 53 90 52 97 EE" +
            "68 0C 77 A9 5C 4B 62 0C 77 8F 79 F6 5C 0C 77 03" +
            "68 28 67 0C 77 FC 57 89 73 0C 77 71 67 AC 4E FD" +
            "90 43 53 49 84 0C 77 01 00 92 00 05 00 35 00 08" +
            "00 2C 00 2C 00 DB 30 C3 30 AB 30 A4 30 C9 30 A6" +
            "30 A2 30 AA 30 E2 30 EA 30 B1 30 F3 30 A4 30 EF" +
            "30 C6 30 B1 30 F3 30 D5 30 AF 30 B7 30 DE 30 B1" +
            "30 F3 30 C8 30 C1 30 AE 30 B1 30 F3 30 B5 30 A4" +
            "30 BF 30 DE 30 B1 30 F3 30 C8 30 A6 30 AD 30 E7" +
            "30 A6 30 C8 30 C1 30 D0 30 B1 30 F3 30 00 00 00" +
            "00 03 00 06 00 03 00 03 00 0C 00 06 00 03 00 11" +
            "00 09 00 03 00 17                              " +

            //Offset=0x00002636(9782) recno=72 sid=0x003C size=0x010D(269)
            "                  3C 00 0D 01 00 0C 00 03 00 1C" +
            "00 0F 00 03 00 22 00 12 00 03 00 28 00 15 00 03" +
            "00 32 00 05 10 00 00 00 42 30 44 30 46 30 48 30" +
            "4A 30 4B 30 4D 30 4F 30 51 30 53 30 55 30 57 30" +
            "59 30 5B 30 5D 30 5F 30 61 30 64 30 66 30 68 30" +
            "6A 30 6B 30 6C 30 6D 30 6E 30 6F 30 72 30 75 30" +
            "78 30 7B 30 7E 30 7F 30 80 30 81 30 82 30 84 30" +
            "86 30 88 30 89 30 8A 30 8B 30 8C 30 8D 30 8F 30" +
            "92 30 93 30 30 00 30 00 37 00 39 00 01 00 0C 00" +
            "05 00 35 00 00 00 00 00 00 00 00 00 32 00 05 10" +
            "00 00 00 42 30 44 30 46 30 48 30 4A 30 4B 30 4D" +
            "30 4F 30 51 30 53 30 55 30 57 30 59 30 5B 30 5D" +
            "30 5F 30 61 30 64 30 66 30 68 30 6A 30 6B 30 6C" +
            "30 6D 30 6E 30 6F 30 72 30 75 30 78 30 7B 30 7E" +
            "30 7F 30 80 30 81 30 82 30 84 30 86 30 88 30 89" +
            "30 8A 30 8B 30 8C 30 8D 30 8F 30 92 30 93 30 30" +
            "00 30 00 38 00 30 00 01 00 0C 00 05 00 35 00 00" +
            "00 00 00 00 00 4B 30                           ";


    /**
     * deep comparison of two SST records
     */
    public static void assertEquals(SSTRecord expected, SSTRecord actual){
        assertEquals("number of strings", expected.getNumStrings(), actual.getNumStrings());
        assertEquals("number of unique strings", expected.getNumUniqueStrings(), actual.getNumUniqueStrings());
        assertEquals("count of strings", expected.countStrings(), actual.countStrings());
        for ( int k = 0; k < expected.countStrings(); k++ ) {
            org.apache.poi.hssf.record.common.UnicodeString us1 = expected.getString(k);
            org.apache.poi.hssf.record.common.UnicodeString us2 = actual.getString(k);

            assertTrue("String at idx=" + k, us1.equals(us2));
        }
    }


    public void test50779_1(){
        byte[] bytes = HexRead.readFromString(data_50779_1);

        RecordInputStream in = TestcaseRecordInputStream.create(bytes);
        assertEquals(SSTRecord.sid, in.getSid());
        SSTRecord src = new SSTRecord(in);
        assertEquals(81, src.getNumStrings());

        byte[] serialized = src.serialize();

        in = TestcaseRecordInputStream.create(serialized);
        assertEquals(SSTRecord.sid, in.getSid());
        SSTRecord dst = new SSTRecord(in);
        assertEquals(81, dst.getNumStrings());

        assertEquals(src, dst);
    }

    public void test50779_2() {
        byte[] bytes = HexRead.readFromString(data_50779_2);

        RecordInputStream in = TestcaseRecordInputStream.create(bytes);
        assertEquals(SSTRecord.sid, in.getSid());
        SSTRecord src = new SSTRecord(in);
        assertEquals(81, src.getNumStrings());

        byte[] serialized = src.serialize();

        in = TestcaseRecordInputStream.create(serialized);
        assertEquals(SSTRecord.sid, in.getSid());
        SSTRecord dst = new SSTRecord(in);
        assertEquals(81, dst.getNumStrings());

        assertEquals(src, dst);
    }

}
