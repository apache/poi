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

import junit.framework.TestCase;

import java.util.List;
import java.util.ArrayList;

import org.apache.poi.util.BinaryTree;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianConsts;

/**
 * Tests that records size calculates correctly.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public class TestSSTRecordSizeCalculator
        extends TestCase
{
    private static final String SMALL_STRING = "Small string";
    private static final int COMPRESSED_PLAIN_STRING_OVERHEAD = 3;
    private List recordLengths;
    private BinaryTree strings;
    private static final int OPTION_FIELD_SIZE = 1;

    public TestSSTRecordSizeCalculator( String s )
    {
        super( s );
    }

    public void testBasic()
            throws Exception
    {
        strings.put(new Integer(0), makeUnicodeString(SMALL_STRING));
        SSTRecordSizeCalculator calculator = new SSTRecordSizeCalculator(new SSTSerializer(recordLengths, strings, 1, 1));
        assertEquals(SSTRecord.SST_RECORD_OVERHEAD + COMPRESSED_PLAIN_STRING_OVERHEAD + SMALL_STRING.length(), calculator.getRecordSize());
    }

    public void testBigStringAcrossUnicode()
            throws Exception
    {
        String bigString = new String(new char[SSTRecord.MAX_DATA_SPACE + 100]);
        strings.put(new Integer(0), makeUnicodeString(bigString));
        SSTRecordSizeCalculator calculator = new SSTRecordSizeCalculator(new SSTSerializer(recordLengths, strings, 1, 1));
        assertEquals(SSTRecord.SST_RECORD_OVERHEAD
                + COMPRESSED_PLAIN_STRING_OVERHEAD
                + SSTRecord.MAX_DATA_SPACE
                + SSTRecord.STD_RECORD_OVERHEAD
                + OPTION_FIELD_SIZE
                + 100,
                calculator.getRecordSize());

    }

    public void testPerfectFit()
            throws Exception
    {
        String perfectFit = new String(new char[SSTRecord.MAX_DATA_SPACE - COMPRESSED_PLAIN_STRING_OVERHEAD]);
        strings.put(new Integer(0), makeUnicodeString(perfectFit));
        SSTRecordSizeCalculator calculator = new SSTRecordSizeCalculator(new SSTSerializer(recordLengths, strings, 1, 1));
        assertEquals(SSTRecord.SST_RECORD_OVERHEAD
                + SSTRecord.MAX_DATA_SPACE,
                calculator.getRecordSize());
    }

    public void testSecondStringStartsOnNewContinuation()
            throws Exception
    {
        String perfectFit = new String(new char[SSTRecord.MAX_DATA_SPACE - COMPRESSED_PLAIN_STRING_OVERHEAD]);
        strings.put(new Integer(0), makeUnicodeString(perfectFit));
        strings.put(new Integer(1), makeUnicodeString(SMALL_STRING));
        SSTRecordSizeCalculator calculator = new SSTRecordSizeCalculator(new SSTSerializer(recordLengths, strings, 1, 1));
        assertEquals(SSTRecord.SST_RECORD_OVERHEAD
                + SSTRecord.MAX_DATA_SPACE
                // second string
                + SSTRecord.STD_RECORD_OVERHEAD
                + COMPRESSED_PLAIN_STRING_OVERHEAD
                + SMALL_STRING.length(),
                calculator.getRecordSize());
    }


    public void setUp()
    {
        recordLengths = new ArrayList();
        strings = new BinaryTree();
    }


    private UnicodeString makeUnicodeString( String s )
    {
        int length = SSTRecord.STRING_MINIMAL_OVERHEAD + s.length();
        byte[] unicodeStringBuffer = new byte[length];
        LittleEndian.putUShort( unicodeStringBuffer, 0, s.length() );
        int offset = LittleEndianConsts.SHORT_SIZE;
        unicodeStringBuffer[offset++] = 0;
        System.arraycopy( s.getBytes(), 0, unicodeStringBuffer, offset, s.length() );
        return new UnicodeString( UnicodeString.sid, (short) unicodeStringBuffer.length, unicodeStringBuffer );
    }

}
