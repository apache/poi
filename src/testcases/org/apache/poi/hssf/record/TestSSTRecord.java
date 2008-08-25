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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.util.LittleEndian;

/**
 * @author Marc Johnson (mjohnson at apache dot org)
 * @author Glen Stampoultzis (glens at apache.org)
 */

public final class TestSSTRecord extends TestCase {

    /**
     * test processContinueRecord
     */
    public void testProcessContinueRecord() {
//jmh        byte[] testdata = HexRead.readData( _test_file_path + File.separator + "BigSSTRecord" );
//jmh        byte[] input = new byte[testdata.length - 4];
//jmh
//jmh        System.arraycopy( testdata, 4, input, 0, input.length );
//jmh        SSTRecord record =
//jmh                new SSTRecord( LittleEndian.getShort( testdata, 0 ),
//jmh                        LittleEndian.getShort( testdata, 2 ), input );
//jmh        byte[] continueRecord = HexRead.readData( _test_file_path + File.separator + "BigSSTRecordCR" );
//jmh
//jmh        input = new byte[continueRecord.length - 4];
//jmh        System.arraycopy( continueRecord, 4, input, 0, input.length );
//jmh        record.processContinueRecord( input );
//jmh        assertEquals( 1464, record.getNumStrings() );
//jmh        assertEquals( 688, record.getNumUniqueStrings() );
//jmh        assertEquals( 688, record.countStrings() );
//jmh        byte[] ser_output = record.serialize();
//jmh        int offset = 0;
//jmh        short type = LittleEndian.getShort( ser_output, offset );
//jmh
//jmh        offset += LittleEndianConsts.SHORT_SIZE;
//jmh        short length = LittleEndian.getShort( ser_output, offset );
//jmh
//jmh        offset += LittleEndianConsts.SHORT_SIZE;
//jmh        byte[] recordData = new byte[length];
//jmh
//jmh        System.arraycopy( ser_output, offset, recordData, 0, length );
//jmh        offset += length;
//jmh        SSTRecord testRecord = new SSTRecord( type, length, recordData );
//jmh
//jmh        assertEquals( ContinueRecord.sid,
//jmh                LittleEndian.getShort( ser_output, offset ) );
//jmh        offset += LittleEndianConsts.SHORT_SIZE;
//jmh        length = LittleEndian.getShort( ser_output, offset );
//jmh        offset += LittleEndianConsts.SHORT_SIZE;
//jmh        byte[] cr = new byte[length];
//jmh
//jmh        System.arraycopy( ser_output, offset, cr, 0, length );
//jmh        offset += length;
//jmh        assertEquals( offset, ser_output.length );
//jmh        testRecord.processContinueRecord( cr );
//jmh        assertEquals( record, testRecord );
//jmh
//jmh        // testing based on new bug report
//jmh        testdata = HexRead.readData( _test_file_path + File.separator + "BigSSTRecord2" );
//jmh        input = new byte[testdata.length - 4];
//jmh        System.arraycopy( testdata, 4, input, 0, input.length );
//jmh        record = new SSTRecord( LittleEndian.getShort( testdata, 0 ),
//jmh                LittleEndian.getShort( testdata, 2 ), input );
//jmh        byte[] continueRecord1 = HexRead.readData( _test_file_path + File.separator + "BigSSTRecord2CR1" );
//jmh
//jmh        input = new byte[continueRecord1.length - 4];
//jmh        System.arraycopy( continueRecord1, 4, input, 0, input.length );
//jmh        record.processContinueRecord( input );
//jmh        byte[] continueRecord2 = HexRead.readData( _test_file_path + File.separator + "BigSSTRecord2CR2" );
//jmh
//jmh        input = new byte[continueRecord2.length - 4];
//jmh        System.arraycopy( continueRecord2, 4, input, 0, input.length );
//jmh        record.processContinueRecord( input );
//jmh        byte[] continueRecord3 = HexRead.readData( _test_file_path + File.separator + "BigSSTRecord2CR3" );
//jmh
//jmh        input = new byte[continueRecord3.length - 4];
//jmh        System.arraycopy( continueRecord3, 4, input, 0, input.length );
//jmh        record.processContinueRecord( input );
//jmh        byte[] continueRecord4 = HexRead.readData( _test_file_path + File.separator + "BigSSTRecord2CR4" );
//jmh
//jmh        input = new byte[continueRecord4.length - 4];
//jmh        System.arraycopy( continueRecord4, 4, input, 0, input.length );
//jmh        record.processContinueRecord( input );
//jmh        byte[] continueRecord5 = HexRead.readData( _test_file_path + File.separator + "BigSSTRecord2CR5" );
//jmh
//jmh        input = new byte[continueRecord5.length - 4];
//jmh        System.arraycopy( continueRecord5, 4, input, 0, input.length );
//jmh        record.processContinueRecord( input );
//jmh        byte[] continueRecord6 = HexRead.readData( _test_file_path + File.separator + "BigSSTRecord2CR6" );
//jmh
//jmh        input = new byte[continueRecord6.length - 4];
//jmh        System.arraycopy( continueRecord6, 4, input, 0, input.length );
//jmh        record.processContinueRecord( input );
//jmh        byte[] continueRecord7 = HexRead.readData( _test_file_path + File.separator + "BigSSTRecord2CR7" );
//jmh
//jmh        input = new byte[continueRecord7.length - 4];
//jmh        System.arraycopy( continueRecord7, 4, input, 0, input.length );
//jmh        record.processContinueRecord( input );
//jmh        assertEquals( 158642, record.getNumStrings() );
//jmh        assertEquals( 5249, record.getNumUniqueStrings() );
//jmh        assertEquals( 5249, record.countStrings() );
//jmh        ser_output = record.serialize();
//jmh        offset = 0;
//jmh        type = LittleEndian.getShort( ser_output, offset );
//jmh        offset += LittleEndianConsts.SHORT_SIZE;
//jmh        length = LittleEndian.getShort( ser_output, offset );
//jmh        offset += LittleEndianConsts.SHORT_SIZE;
//jmh        recordData = new byte[length];
//jmh        System.arraycopy( ser_output, offset, recordData, 0, length );
//jmh        offset += length;
//jmh        testRecord = new SSTRecord( type, length, recordData );
//jmh        for ( int count = 0; count < 7; count++ )
//jmh        {
//jmh            assertEquals( ContinueRecord.sid,
//jmh                    LittleEndian.getShort( ser_output, offset ) );
//jmh            offset += LittleEndianConsts.SHORT_SIZE;
//jmh            length = LittleEndian.getShort( ser_output, offset );
//jmh            offset += LittleEndianConsts.SHORT_SIZE;
//jmh            cr = new byte[length];
//jmh            System.arraycopy( ser_output, offset, cr, 0, length );
//jmh            testRecord.processContinueRecord( cr );
//jmh            offset += length;
//jmh        }
//jmh        assertEquals( offset, ser_output.length );
//jmh        assertEquals( record, testRecord );
//jmh        assertEquals( record.countStrings(), testRecord.countStrings() );
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
    public void testSimpleAddString()
    {
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

    public void testSimpleConstructor()
    {
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
     * main method to run the unit tests
     *
     * @param ignored_args
     */

    public static void main( String[] ignored_args ) {
        junit.textui.TestRunner.run( TestSSTRecord.class );
    }

    /**
     * Tests that workbooks with rich text that duplicates a non rich text cell can be read and written.
     */
    public void testReadWriteDuplicatedRichText1()
            throws Exception
    {
        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("duprich1.xls");
        HSSFSheet sheet = wb.getSheetAt( 1 );
        assertEquals( "01/05 (Wed)", sheet.getRow( 0 ).getCell(8 ).getStringCellValue() );
        assertEquals( "01/05 (Wed)", sheet.getRow( 1 ).getCell(8 ).getStringCellValue() );

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        wb.write( baos );

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

        wb.write( baos );
    }
}
