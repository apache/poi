
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

import junit.framework.TestCase;

import org.apache.poi.util.IntMapper;

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
//    private List recordLengths;
    private IntMapper strings;
    private static final int OPTION_FIELD_SIZE = 1;

    public TestSSTRecordSizeCalculator( String s )
    {
        super( s );
    }

    public void testBasic()
            throws Exception
    {
        strings.add(makeUnicodeString(SMALL_STRING));
        SSTRecordSizeCalculator calculator = new SSTRecordSizeCalculator(strings);
        assertEquals(SSTRecord.SST_RECORD_OVERHEAD + COMPRESSED_PLAIN_STRING_OVERHEAD + SMALL_STRING.length(),
                calculator.getRecordSize());
    }

    public void testBigStringAcrossUnicode()
            throws Exception
    {
        String bigString = new String(new char[SSTRecord.MAX_DATA_SPACE + 100]);
        strings.add(makeUnicodeString(bigString));
        SSTRecordSizeCalculator calculator = new SSTRecordSizeCalculator(strings);
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
        strings.add(makeUnicodeString(perfectFit));
        SSTRecordSizeCalculator calculator = new SSTRecordSizeCalculator(strings);
        assertEquals(SSTRecord.SST_RECORD_OVERHEAD
                + COMPRESSED_PLAIN_STRING_OVERHEAD
                + perfectFit.length(),
                calculator.getRecordSize());
    }

    public void testJustOversized()
            throws Exception
    {
        String tooBig = new String(new char[SSTRecord.MAX_DATA_SPACE - COMPRESSED_PLAIN_STRING_OVERHEAD + 1]);
        strings.add(makeUnicodeString(tooBig));
        SSTRecordSizeCalculator calculator = new SSTRecordSizeCalculator(strings);
        assertEquals(SSTRecord.SST_RECORD_OVERHEAD
                + COMPRESSED_PLAIN_STRING_OVERHEAD
                + tooBig.length() - 1
                // continue record
                + SSTRecord.STD_RECORD_OVERHEAD
                + OPTION_FIELD_SIZE
                + 1,
                calculator.getRecordSize());

    }

    public void testSecondStringStartsOnNewContinuation()
            throws Exception
    {
        String perfectFit = new String(new char[SSTRecord.MAX_DATA_SPACE - COMPRESSED_PLAIN_STRING_OVERHEAD]);
        strings.add(makeUnicodeString(perfectFit));
        strings.add(makeUnicodeString(SMALL_STRING));
        SSTRecordSizeCalculator calculator = new SSTRecordSizeCalculator(strings);
        assertEquals(SSTRecord.SST_RECORD_OVERHEAD
                + SSTRecord.MAX_DATA_SPACE
                // second string
                + SSTRecord.STD_RECORD_OVERHEAD
                + COMPRESSED_PLAIN_STRING_OVERHEAD
                + SMALL_STRING.length(),
                calculator.getRecordSize());
    }

    public void testHeaderCrossesNormalContinuePoint()
            throws Exception
    {
        String almostPerfectFit = new String(new char[SSTRecord.MAX_DATA_SPACE - COMPRESSED_PLAIN_STRING_OVERHEAD - 2]);
        strings.add(makeUnicodeString(almostPerfectFit));
        String oneCharString = new String(new char[1]);
        strings.add(makeUnicodeString(oneCharString));
        SSTRecordSizeCalculator calculator = new SSTRecordSizeCalculator(strings);
        assertEquals(SSTRecord.SST_RECORD_OVERHEAD
                + COMPRESSED_PLAIN_STRING_OVERHEAD
                + almostPerfectFit.length()
                // second string
                + SSTRecord.STD_RECORD_OVERHEAD
                + COMPRESSED_PLAIN_STRING_OVERHEAD
                + oneCharString.length(),
                calculator.getRecordSize());

    }


    public void setUp()
    {
        strings = new IntMapper();
    }


    private UnicodeString makeUnicodeString( String s )
    {
      UnicodeString st = new UnicodeString(s);
      st.setOptionFlags((byte)0);
      return st;
    }

}
