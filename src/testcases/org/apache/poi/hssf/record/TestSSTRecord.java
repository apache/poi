
/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.poi.hssf.record;

import org.apache.poi.util.*;

import junit.framework.*;

import java.io.*;

import java.util.*;

/**
 * @author Marc Johnson (mjohnson at apache dot org)
 */

public class TestSSTRecord
    extends TestCase
{
    private String              _test_file_path;
    private static final String _test_file_path_property =
        "HSSF.testdata.path";

    /**
     * Creates new TestSSTRecord
     *
     * @param name
     */

    public TestSSTRecord(String name)
    {
        super(name);
        _test_file_path = System.getProperty(_test_file_path_property);
    }

    /**
     * test processContinueRecord
     *
     * @exception IOException
     */

    public void testProcessContinueRecord()
        throws IOException
    {
        byte[] testdata = readTestData("BigSSTRecord");
        byte[] input    = new byte[ testdata.length - 4 ];

        System.arraycopy(testdata, 4, input, 0, input.length);
        SSTRecord record         =
            new SSTRecord(LittleEndian.getShort(testdata, 0),
                          LittleEndian.getShort(testdata, 2), input);
        byte[]    continueRecord = readTestData("BigSSTRecordCR");

        input = new byte[ continueRecord.length - 4 ];
        System.arraycopy(continueRecord, 4, input, 0, input.length);
        record.processContinueRecord(input);
        assertEquals(1464, record.getNumStrings());
        assertEquals(688, record.getNumUniqueStrings());
        assertEquals(688, record.countStrings());
        byte[] ser_output = record.serialize();
        int    offset     = 0;
        short  type       = LittleEndian.getShort(ser_output, offset);

        offset += LittleEndianConsts.SHORT_SIZE;
        short length = LittleEndian.getShort(ser_output, offset);

        offset += LittleEndianConsts.SHORT_SIZE;
        byte[] recordData = new byte[ length ];

        System.arraycopy(ser_output, offset, recordData, 0, length);
        offset += length;
        SSTRecord testRecord = new SSTRecord(type, length, recordData);

        assertEquals(ContinueRecord.sid,
                     LittleEndian.getShort(ser_output, offset));
        offset += LittleEndianConsts.SHORT_SIZE;
        length = LittleEndian.getShort(ser_output, offset);
        offset += LittleEndianConsts.SHORT_SIZE;
        byte[] cr = new byte[ length ];

        System.arraycopy(ser_output, offset, cr, 0, length);
        offset += length;
        assertEquals(offset, ser_output.length);
        testRecord.processContinueRecord(cr);
        assertEquals(record, testRecord);

        // testing based on new bug report
        testdata = readTestData("BigSSTRecord2");
        input    = new byte[ testdata.length - 4 ];
        System.arraycopy(testdata, 4, input, 0, input.length);
        record = new SSTRecord(LittleEndian.getShort(testdata, 0),
                               LittleEndian.getShort(testdata, 2), input);
        byte[] continueRecord1 = readTestData("BigSSTRecord2CR1");

        input = new byte[ continueRecord1.length - 4 ];
        System.arraycopy(continueRecord1, 4, input, 0, input.length);
        record.processContinueRecord(input);
        byte[] continueRecord2 = readTestData("BigSSTRecord2CR2");

        input = new byte[ continueRecord2.length - 4 ];
        System.arraycopy(continueRecord2, 4, input, 0, input.length);
        record.processContinueRecord(input);
        byte[] continueRecord3 = readTestData("BigSSTRecord2CR3");

        input = new byte[ continueRecord3.length - 4 ];
        System.arraycopy(continueRecord3, 4, input, 0, input.length);
        record.processContinueRecord(input);
        byte[] continueRecord4 = readTestData("BigSSTRecord2CR4");

        input = new byte[ continueRecord4.length - 4 ];
        System.arraycopy(continueRecord4, 4, input, 0, input.length);
        record.processContinueRecord(input);
        byte[] continueRecord5 = readTestData("BigSSTRecord2CR5");

        input = new byte[ continueRecord5.length - 4 ];
        System.arraycopy(continueRecord5, 4, input, 0, input.length);
        record.processContinueRecord(input);
        byte[] continueRecord6 = readTestData("BigSSTRecord2CR6");

        input = new byte[ continueRecord6.length - 4 ];
        System.arraycopy(continueRecord6, 4, input, 0, input.length);
        record.processContinueRecord(input);
        byte[] continueRecord7 = readTestData("BigSSTRecord2CR7");

        input = new byte[ continueRecord7.length - 4 ];
        System.arraycopy(continueRecord7, 4, input, 0, input.length);
        record.processContinueRecord(input);
        assertEquals(158642, record.getNumStrings());
        assertEquals(5249, record.getNumUniqueStrings());
        assertEquals(5249, record.countStrings());
        ser_output = record.serialize();
        offset     = 0;
        type       = LittleEndian.getShort(ser_output, offset);
        offset     += LittleEndianConsts.SHORT_SIZE;
        length     = LittleEndian.getShort(ser_output, offset);
        offset     += LittleEndianConsts.SHORT_SIZE;
        recordData = new byte[ length ];
        System.arraycopy(ser_output, offset, recordData, 0, length);
        offset     += length;
        testRecord = new SSTRecord(type, length, recordData);
        for (int count = 0; count < 7; count++)
        {
            assertEquals(ContinueRecord.sid,
                         LittleEndian.getShort(ser_output, offset));
            offset += LittleEndianConsts.SHORT_SIZE;
            length = LittleEndian.getShort(ser_output, offset);
            offset += LittleEndianConsts.SHORT_SIZE;
            cr     = new byte[ length ];
            System.arraycopy(ser_output, offset, cr, 0, length);
            testRecord.processContinueRecord(cr);
            offset += length;
        }
        assertEquals(offset, ser_output.length);
        assertEquals(record, testRecord);
    }

    /**
     * Test capability of handling mondo big strings
     *
     * @exception IOException
     */

    public void testHugeStrings()
        throws IOException
    {
        SSTRecord record       = new SSTRecord();
        byte[][]  bstrings     =
        {
            new byte[ 9000 ], new byte[ 7433 ], new byte[ 9002 ],
            new byte[ 16998 ]
        };
        String[]  strings      = new String[ bstrings.length ];
        int       total_length = 0;

        for (int k = 0; k < bstrings.length; k++)
        {
            Arrays.fill(bstrings[ k ], ( byte ) ('a' + k));
            strings[ k ] = new String(bstrings[ k ]);
            record.addString(strings[ k ]);
            total_length += 3 + bstrings[ k ].length;
        }

        // add overhead of SST record
        total_length += 8;

        // add overhead of broken strings
        total_length += 4;

        // add overhead of six records
        total_length += (6 * 4);
        byte[] content = new byte[ record.getRecordSize() ];

        record.serialize(0, content);
        assertEquals(total_length, content.length);
        for (int index = 0; index != content.length; )
        {
            short record_type = LittleEndian.getShort(content, index);

            index += LittleEndianConsts.SHORT_SIZE;
            short record_length = LittleEndian.getShort(content, index);

            index += LittleEndianConsts.SHORT_SIZE;
            byte[] data = new byte[ record_length ];

            System.arraycopy(content, index, data, 0, record_length);
            index += record_length;
            if (record_type == SSTRecord.sid)
            {
                record = new SSTRecord(record_type, record_length, data);
            }
            else
            {
                record.processContinueRecord(data);
            }
        }
        assertEquals(strings.length, record.getNumStrings());
        assertEquals(strings.length, record.getNumUniqueStrings());
        assertEquals(strings.length, record.countStrings());
        for (int k = 0; k < strings.length; k++)
        {
            assertEquals(strings[ k ], record.getString(k));
        }
        record        = new SSTRecord();
        bstrings[ 1 ] = new byte[ bstrings[ 1 ].length - 1 ];
        for (int k = 0; k < bstrings.length; k++)
        {
            if ((bstrings[ k ].length % 2) == 1)
            {
                Arrays.fill(bstrings[ k ], ( byte ) ('a' + k));
                strings[ k ] = new String(bstrings[ k ]);
            }
            else
            {
                char[] data = new char[ bstrings[ k ].length / 2 ];

                Arrays.fill(data, ( char ) ('\u2122' + k));
                strings[ k ] = new String(data);
            }
            record.addString(strings[ k ]);
        }
        content = new byte[ record.getRecordSize() ];
        record.serialize(0, content);
        total_length--;
        assertEquals(total_length, content.length);
        for (int index = 0; index != content.length; )
        {
            short record_type = LittleEndian.getShort(content, index);

            index += LittleEndianConsts.SHORT_SIZE;
            short record_length = LittleEndian.getShort(content, index);

            index += LittleEndianConsts.SHORT_SIZE;
            byte[] data = new byte[ record_length ];

            System.arraycopy(content, index, data, 0, record_length);
            index += record_length;
            if (record_type == SSTRecord.sid)
            {
                record = new SSTRecord(record_type, record_length, data);
            }
            else
            {
                record.processContinueRecord(data);
            }
        }
        assertEquals(strings.length, record.getNumStrings());
        assertEquals(strings.length, record.getNumUniqueStrings());
        assertEquals(strings.length, record.countStrings());
        for (int k = 0; k < strings.length; k++)
        {
            assertEquals(strings[ k ], record.getString(k));
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
        record.addString("Hello");

        // now we have an additional 8208 bytes, which is an exact
        // multiple of 16 bytes
        long testvalue = 1000000000000L;

        for (int k = 0; k < 2000; k++)
        {
            record.addString(String.valueOf(testvalue++));
        }
        byte[] content = new byte[ record.getRecordSize() ];

        record.serialize(0, content);
        assertEquals(( byte ) 13, content[ 4 + 8228 ]);
        assertEquals(( byte ) 13, content[ 4 + 8228 * 2 ]);
        assertEquals(( byte ) 13, content[ 4 + 8228 * 3 ]);
    }

    /**
     * test simple addString
     */

    public void testSimpleAddString()
    {
        SSTRecord record = new SSTRecord();
        String    s1     = "Hello world";

        // \u2122 is the encoding of the trademark symbol ...
        String    s2     = "Hello world\u2122";

        assertEquals(0, record.addString(s1));
        assertEquals(s1, record.getString(0));
        assertEquals(1, record.countStrings());
        assertEquals(1, record.getNumStrings());
        assertEquals(1, record.getNumUniqueStrings());
        assertEquals(0, record.addString(s1));
        assertEquals(s1, record.getString(0));
        assertEquals(1, record.countStrings());
        assertEquals(2, record.getNumStrings());
        assertEquals(1, record.getNumUniqueStrings());
        assertEquals(1, record.addString(s2));
        assertEquals(s2, record.getString(1));
        assertEquals(2, record.countStrings());
        assertEquals(3, record.getNumStrings());
        assertEquals(2, record.getNumUniqueStrings());
        Iterator iter = record.getStrings();

        while (iter.hasNext())
        {
            UnicodeString ucs = ( UnicodeString ) iter.next();

            if (ucs.getString().equals(s1))
            {
                assertEquals(( byte ) 0, ucs.getOptionFlags());
            }
            else if (ucs.getString().equals(s2))
            {
                assertEquals(( byte ) 1, ucs.getOptionFlags());
            }
            else
            {
                fail("cannot match string: " + ucs.getString());
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
        byte[] testdata = readTestData("BigSSTRecord");
        byte[] input    = new byte[ testdata.length - 4 ];

        System.arraycopy(testdata, 4, input, 0, input.length);
        SSTRecord record = new SSTRecord(LittleEndian.getShort(testdata, 0),
                                         LittleEndian.getShort(testdata, 2),
                                         input);

        assertEquals(1464, record.getNumStrings());
        assertEquals(688, record.getNumUniqueStrings());
        assertEquals(492, record.countStrings());
        assertEquals(1, record.getExpectedChars());
        assertEquals("Consolidated B-24J Liberator The Dragon & His Tai",
                     record.getUnfinishedString());
        assertEquals(52, record.getTotalLength());
        assertEquals(3, record.getStringDataOffset());
        assertTrue(!record.isWideChar());
    }

    /**
     * test simple constructor
     */

    public void testSimpleConstructor()
    {
        SSTRecord record = new SSTRecord();

        assertEquals(0, record.getNumStrings());
        assertEquals(0, record.getNumUniqueStrings());
        assertEquals(0, record.countStrings());
        assertEquals(0, record.getExpectedChars());
        assertEquals("", record.getUnfinishedString());
        assertEquals(0, record.getTotalLength());
        assertEquals(0, record.getStringDataOffset());
        assertTrue(!record.isWideChar());
        byte[] output   = record.serialize();
        byte[] expected =
        {
            ( byte ) record.getSid(), ( byte ) (record.getSid() >> 8),
            ( byte ) 8, ( byte ) 0, ( byte ) 0, ( byte ) 0, ( byte ) 0,
            ( byte ) 0, ( byte ) 0, ( byte ) 0, ( byte ) 0, ( byte ) 0
        };

        assertEquals(expected.length, output.length);
        for (int k = 0; k < expected.length; k++)
        {
            assertEquals(String.valueOf(k), expected[ k ], output[ k ]);
        }
    }

    /**
     * main method to run the unit tests
     *
     * @param ignored_args
     */

    public static void main(String [] ignored_args)
    {
        System.out.println("Testing hssf.record.SSTRecord functionality");
        junit.textui.TestRunner.run(TestSSTRecord.class);
    }

    private byte [] readTestData(String filename)
        throws IOException
    {
        File            file           = new File(_test_file_path
                                                  + File.separator
                                                  + filename);
        FileInputStream stream         = new FileInputStream(file);
        int             characterCount = 0;
        byte            b              = ( byte ) 0;
        List            bytes          = new ArrayList();
        boolean         done           = false;

        while (!done)
        {
            int count = stream.read();

            switch (count)
            {

                case '0' :
                case '1' :
                case '2' :
                case '3' :
                case '4' :
                case '5' :
                case '6' :
                case '7' :
                case '8' :
                case '9' :
                    b <<= 4;
                    b += ( byte ) (count - '0');
                    characterCount++;
                    if (characterCount == 2)
                    {
                        bytes.add(new Byte(b));
                        characterCount = 0;
                        b              = ( byte ) 0;
                    }
                    break;

                case 'A' :
                case 'B' :
                case 'C' :
                case 'D' :
                case 'E' :
                case 'F' :
                    b <<= 4;
                    b += ( byte ) (count + 10 - 'A');
                    characterCount++;
                    if (characterCount == 2)
                    {
                        bytes.add(new Byte(b));
                        characterCount = 0;
                        b              = ( byte ) 0;
                    }
                    break;

                case 'a' :
                case 'b' :
                case 'c' :
                case 'd' :
                case 'e' :
                case 'f' :
                    b <<= 4;
                    b += ( byte ) (count + 10 - 'a');
                    characterCount++;
                    if (characterCount == 2)
                    {
                        bytes.add(new Byte(b));
                        characterCount = 0;
                        b              = ( byte ) 0;
                    }
                    break;

                case -1 :
                    done = true;
                    break;

                default :
                    break;
            }
        }
        stream.close();
        Byte[] polished = ( Byte [] ) bytes.toArray(new Byte[ 0 ]);
        byte[] rval     = new byte[ polished.length ];

        for (int j = 0; j < polished.length; j++)
        {
            rval[ j ] = polished[ j ].byteValue();
        }
        return rval;
    }
}
