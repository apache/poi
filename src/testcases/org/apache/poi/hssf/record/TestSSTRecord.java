
/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
        

package org.apache.poi.hssf.record;

import junit.framework.TestCase;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.util.HexRead;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianConsts;

import java.io.*;
import java.util.Arrays;
import java.util.Iterator;

/**
 * @author Marc Johnson (mjohnson at apache dot org)
 * @author Glen Stampoultzis (glens at apache.org)
 */

public class TestSSTRecord
        extends TestCase
{
    private String _test_file_path;
    private static final String _test_file_path_property = "HSSF.testdata.path";

    /**
     * Creates new TestSSTRecord
     *
     * @param name
     */

    public TestSSTRecord( String name )
    {
        super( name );
        _test_file_path = System.getProperty( _test_file_path_property );
    }

    /**
     * test processContinueRecord
     *
     * @exception IOException
     */

    public void testProcessContinueRecord()
            throws IOException
    {
        byte[] testdata = HexRead.readData( _test_file_path + File.separator + "BigSSTRecord" );
        byte[] input = new byte[testdata.length - 4];

        System.arraycopy( testdata, 4, input, 0, input.length );
        SSTRecord record =
                new SSTRecord( LittleEndian.getShort( testdata, 0 ),
                        LittleEndian.getShort( testdata, 2 ), input );
        byte[] continueRecord = HexRead.readData( _test_file_path + File.separator + "BigSSTRecordCR" );

        input = new byte[continueRecord.length - 4];
        System.arraycopy( continueRecord, 4, input, 0, input.length );
        record.processContinueRecord( input );
        assertEquals( 1464, record.getNumStrings() );
        assertEquals( 688, record.getNumUniqueStrings() );
        assertEquals( 688, record.countStrings() );
        byte[] ser_output = record.serialize();
        int offset = 0;
        short type = LittleEndian.getShort( ser_output, offset );

        offset += LittleEndianConsts.SHORT_SIZE;
        short length = LittleEndian.getShort( ser_output, offset );

        offset += LittleEndianConsts.SHORT_SIZE;
        byte[] recordData = new byte[length];

        System.arraycopy( ser_output, offset, recordData, 0, length );
        offset += length;
        SSTRecord testRecord = new SSTRecord( type, length, recordData );

        assertEquals( ContinueRecord.sid,
                LittleEndian.getShort( ser_output, offset ) );
        offset += LittleEndianConsts.SHORT_SIZE;
        length = LittleEndian.getShort( ser_output, offset );
        offset += LittleEndianConsts.SHORT_SIZE;
        byte[] cr = new byte[length];

        System.arraycopy( ser_output, offset, cr, 0, length );
        offset += length;
        assertEquals( offset, ser_output.length );
        testRecord.processContinueRecord( cr );
        assertEquals( record, testRecord );

        // testing based on new bug report
        testdata = HexRead.readData( _test_file_path + File.separator + "BigSSTRecord2" );
        input = new byte[testdata.length - 4];
        System.arraycopy( testdata, 4, input, 0, input.length );
        record = new SSTRecord( LittleEndian.getShort( testdata, 0 ),
                LittleEndian.getShort( testdata, 2 ), input );
        byte[] continueRecord1 = HexRead.readData( _test_file_path + File.separator + "BigSSTRecord2CR1" );

        input = new byte[continueRecord1.length - 4];
        System.arraycopy( continueRecord1, 4, input, 0, input.length );
        record.processContinueRecord( input );
        byte[] continueRecord2 = HexRead.readData( _test_file_path + File.separator + "BigSSTRecord2CR2" );

        input = new byte[continueRecord2.length - 4];
        System.arraycopy( continueRecord2, 4, input, 0, input.length );
        record.processContinueRecord( input );
        byte[] continueRecord3 = HexRead.readData( _test_file_path + File.separator + "BigSSTRecord2CR3" );

        input = new byte[continueRecord3.length - 4];
        System.arraycopy( continueRecord3, 4, input, 0, input.length );
        record.processContinueRecord( input );
        byte[] continueRecord4 = HexRead.readData( _test_file_path + File.separator + "BigSSTRecord2CR4" );

        input = new byte[continueRecord4.length - 4];
        System.arraycopy( continueRecord4, 4, input, 0, input.length );
        record.processContinueRecord( input );
        byte[] continueRecord5 = HexRead.readData( _test_file_path + File.separator + "BigSSTRecord2CR5" );

        input = new byte[continueRecord5.length - 4];
        System.arraycopy( continueRecord5, 4, input, 0, input.length );
        record.processContinueRecord( input );
        byte[] continueRecord6 = HexRead.readData( _test_file_path + File.separator + "BigSSTRecord2CR6" );

        input = new byte[continueRecord6.length - 4];
        System.arraycopy( continueRecord6, 4, input, 0, input.length );
        record.processContinueRecord( input );
        byte[] continueRecord7 = HexRead.readData( _test_file_path + File.separator + "BigSSTRecord2CR7" );

        input = new byte[continueRecord7.length - 4];
        System.arraycopy( continueRecord7, 4, input, 0, input.length );
        record.processContinueRecord( input );
        assertEquals( 158642, record.getNumStrings() );
        assertEquals( 5249, record.getNumUniqueStrings() );
        assertEquals( 5249, record.countStrings() );
        ser_output = record.serialize();
        offset = 0;
        type = LittleEndian.getShort( ser_output, offset );
        offset += LittleEndianConsts.SHORT_SIZE;
        length = LittleEndian.getShort( ser_output, offset );
        offset += LittleEndianConsts.SHORT_SIZE;
        recordData = new byte[length];
        System.arraycopy( ser_output, offset, recordData, 0, length );
        offset += length;
        testRecord = new SSTRecord( type, length, recordData );
        for ( int count = 0; count < 7; count++ )
        {
            assertEquals( ContinueRecord.sid,
                    LittleEndian.getShort( ser_output, offset ) );
            offset += LittleEndianConsts.SHORT_SIZE;
            length = LittleEndian.getShort( ser_output, offset );
            offset += LittleEndianConsts.SHORT_SIZE;
            cr = new byte[length];
            System.arraycopy( ser_output, offset, cr, 0, length );
            testRecord.processContinueRecord( cr );
            offset += length;
        }
        assertEquals( offset, ser_output.length );
        assertEquals( record, testRecord );
        assertEquals( record.countStrings(), testRecord.countStrings() );
    }

    /**
     * Test capability of handling mondo big strings
     *
     * @exception IOException
     */

    public void testHugeStrings()
            throws IOException
    {
        SSTRecord record = new SSTRecord();
        byte[][] bstrings =
                {
                    new byte[9000], new byte[7433], new byte[9002],
                    new byte[16998]
                };
        String[] strings = new String[bstrings.length];
        int total_length = 0;

        for ( int k = 0; k < bstrings.length; k++ )
        {
            Arrays.fill( bstrings[k], (byte) ( 'a' + k ) );
            strings[k] = new String( bstrings[k] );
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
        for ( int index = 0; index != content.length; )
        {
            short record_type = LittleEndian.getShort( content, index );

            index += LittleEndianConsts.SHORT_SIZE;
            short record_length = LittleEndian.getShort( content, index );

            index += LittleEndianConsts.SHORT_SIZE;
            byte[] data = new byte[record_length];

            System.arraycopy( content, index, data, 0, record_length );
            index += record_length;
            if ( record_type == SSTRecord.sid )
            {
                record = new SSTRecord( record_type, record_length, data );
            }
            else
            {
                record.processContinueRecord( data );
            }
        }
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
                strings[k] = new String( bstrings[k] );
            }
            else
            {
                char[] data = new char[bstrings[k].length / 2];

                Arrays.fill( data, (char) ( '\u2122' + k ) );
                strings[k] = new String( data );
            }
            record.addString( strings[k] );
        }
        content = new byte[record.getRecordSize()];
        record.serialize( 0, content );
        total_length--;
        assertEquals( total_length, content.length );
        for ( int index = 0; index != content.length; )
        {
            short record_type = LittleEndian.getShort( content, index );

            index += LittleEndianConsts.SHORT_SIZE;
            short record_length = LittleEndian.getShort( content, index );

            index += LittleEndianConsts.SHORT_SIZE;
            byte[] data = new byte[record_length];

            System.arraycopy( content, index, data, 0, record_length );
            index += record_length;
            if ( record_type == SSTRecord.sid )
            {
                record = new SSTRecord( record_type, record_length, data );
            }
            else
            {
                record.processContinueRecord( data );
            }
        }
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
     *
     * @exception IOException
     */
    public void testSSTRecordBug()
            throws IOException
    {

        // create an SSTRecord and write a certain pattern of strings
        // to it ... then serialize it and verify the content
        SSTRecord record = new SSTRecord();

        // the record will start with two integers, then this string
        // ... that will eat up 16 of the 8224 bytes that the record
        // can hold
        record.addString( "Hello" );

        // now we have an additional 8208 bytes, which is an exact
        // multiple of 16 bytes
        long testvalue = 1000000000000L;

        for ( int k = 0; k < 2000; k++ )
        {
            record.addString( String.valueOf( testvalue++ ) );
        }
        byte[] content = new byte[record.getRecordSize()];

        record.serialize( 0, content );
        assertEquals( (byte) 13, content[4 + 8228] );
        assertEquals( (byte) 13, content[4 + 8228 * 2] );
        assertEquals( (byte) 13, content[4 + 8228 * 3] );
    }

    /**
     * test simple addString
     */
    public void testSimpleAddString()
    {
        SSTRecord record = new SSTRecord();
        String s1 = "Hello world";

        // \u2122 is the encoding of the trademark symbol ...
        String s2 = "Hello world\u2122";

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

            if ( ucs.getString().equals( s1 ) )
            {
                assertEquals( (byte) 0, ucs.getOptionFlags() );
            }
            else if ( ucs.getString().equals( s2 ) )
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
     * test reader constructor
     *
     * @exception IOException
     */

    public void testReaderConstructor()
            throws IOException
    {
        byte[] testdata = HexRead.readData( _test_file_path + File.separator + "BigSSTRecord" );
        byte[] input = new byte[testdata.length - 4];

        System.arraycopy( testdata, 4, input, 0, input.length );
        SSTRecord record = new SSTRecord( LittleEndian.getShort( testdata, 0 ),
                LittleEndian.getShort( testdata, 2 ),
                input );

        assertEquals( 1464, record.getNumStrings() );
        assertEquals( 688, record.getNumUniqueStrings() );
        assertEquals( 492, record.countStrings() );
//jmh        assertEquals( 1, record.getDeserializer().getContinuationExpectedChars() );
        assertEquals( "Consolidated B-24J Liberator The Dragon & His Tai",
                record.getDeserializer().getUnfinishedString() );
//        assertEquals( 52, record.getDeserializer().getTotalLength() );
//        assertEquals( 3, record.getDeserializer().getStringDataOffset() );
        assertTrue( !record.getDeserializer().isWideChar() );
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
        assertEquals( 0, record.getDeserializer().getContinuationCharsRead() );
        assertEquals( "", record.getDeserializer().getUnfinishedString() );
//        assertEquals( 0, record.getDeserializer().getTotalLength() );
//        assertEquals( 0, record.getDeserializer().getStringDataOffset() );
        assertTrue( !record.getDeserializer().isWideChar() );
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

    public static void main( String[] ignored_args )
    {
        System.out.println( "Testing hssf.record.SSTRecord functionality" );
        junit.textui.TestRunner.run( TestSSTRecord.class );
    }

    /**
     * Tests that workbooks with rich text that duplicates a non rich text cell can be read and written.
     */
    public void testReadWriteDuplicatedRichText1()
            throws Exception
    {
        File file = new File( _test_file_path + File.separator + "duprich1.xls" );
        InputStream stream = new FileInputStream( file );
        HSSFWorkbook wb = new HSSFWorkbook( stream );
        stream.close();
        HSSFSheet sheet = wb.getSheetAt( 1 );
        assertEquals( "01/05 (Wed) ", sheet.getRow( 0 ).getCell( (short) 8 ).getStringCellValue() );
        assertEquals( "01/05 (Wed)", sheet.getRow( 1 ).getCell( (short) 8 ).getStringCellValue() );

        file = File.createTempFile( "testout", "xls" );
        FileOutputStream outStream = new FileOutputStream( file );
        wb.write( outStream );
        outStream.close();
        file.delete();

        // test the second file.
        file = new File( _test_file_path + File.separator + "duprich2.xls" );
        stream = new FileInputStream( file );
        wb = new HSSFWorkbook( stream );
        stream.close();
        sheet = wb.getSheetAt( 0 );
        int row = 0;
        assertEquals( "Testing ", sheet.getRow( row++ ).getCell( (short) 0 ).getStringCellValue() );
        assertEquals( "rich", sheet.getRow( row++ ).getCell( (short) 0 ).getStringCellValue() );
        assertEquals( "text", sheet.getRow( row++ ).getCell( (short) 0 ).getStringCellValue() );
        assertEquals( "strings", sheet.getRow( row++ ).getCell( (short) 0 ).getStringCellValue() );
        assertEquals( "Testing  ", sheet.getRow( row++ ).getCell( (short) 0 ).getStringCellValue() );
        assertEquals( "Testing", sheet.getRow( row++ ).getCell( (short) 0 ).getStringCellValue() );

//        file = new File("/tryme.xls");
        file = File.createTempFile( "testout", ".xls" );
        outStream = new FileOutputStream( file );
        wb.write( outStream );
        outStream.close();
        file.delete();
    }

}
