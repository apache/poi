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

import org.apache.poi.util.HexRead;

import junit.framework.TestCase;

/**
 * Tests that records size calculates correctly.
 *
 * @author Jason Height (jheight at apache.org)
 */
public final class TestUnicodeString extends TestCase {


    public void testSmallStringSize() {
        //Test a basic string
        UnicodeString s = makeUnicodeString("Test");
        UnicodeString.UnicodeRecordStats stats = new UnicodeString.UnicodeRecordStats();
        s.getRecordSize(stats);
        assertEquals(7, stats.recordSize);

        //Test a small string that is uncompressed
        s.setOptionFlags((byte)0x01);
        stats = new UnicodeString.UnicodeRecordStats();
        s.getRecordSize(stats);
        assertEquals(11, stats.recordSize);

        //Test a compressed small string that has rich text formatting
        s.setOptionFlags((byte)0x8);
        UnicodeString.FormatRun r = new UnicodeString.FormatRun((short)0,(short)1);
        s.addFormatRun(r);
        UnicodeString.FormatRun r2 = new UnicodeString.FormatRun((short)2,(short)2);
        s.addFormatRun(r2);
        stats = new UnicodeString.UnicodeRecordStats();
        s.getRecordSize(stats);
        assertEquals(17, stats.recordSize);

        //Test a uncompressed small string that has rich text formatting
        s.setOptionFlags((byte)0x9);
        stats = new UnicodeString.UnicodeRecordStats();
        s.getRecordSize(stats);
        assertEquals(21, stats.recordSize);

        //Test a compressed small string that has rich text and extended text
        s.setOptionFlags((byte)0xC);
        s.setExtendedRst(new byte[]{(byte)0x1,(byte)0x2,(byte)0x3,(byte)0x4,(byte)0x5});
        stats = new UnicodeString.UnicodeRecordStats();
        s.getRecordSize(stats);
        assertEquals(26, stats.recordSize);

        //Test a uncompressed small string that has rich text and extended text
        s.setOptionFlags((byte)0xD);
        stats = new UnicodeString.UnicodeRecordStats();
        s.getRecordSize(stats);
        assertEquals(30, stats.recordSize);
    }

    public void testPerfectStringSize() {
      //Test a basic string
      UnicodeString s = makeUnicodeString(SSTRecord.MAX_RECORD_SIZE-2-1);
      UnicodeString.UnicodeRecordStats stats = new UnicodeString.UnicodeRecordStats();
      s.getRecordSize(stats);
      assertEquals(SSTRecord.MAX_RECORD_SIZE, stats.recordSize);

      //Test an uncompressed string
      //Note that we can only ever get to a maximim size of 8227 since an uncompressed
      //string is writing double bytes.
      s = makeUnicodeString((SSTRecord.MAX_RECORD_SIZE-2-1)/2);
      s.setOptionFlags((byte)0x1);
      stats = new UnicodeString.UnicodeRecordStats();
      s.getRecordSize(stats);
      assertEquals(SSTRecord.MAX_RECORD_SIZE-1, stats.recordSize);
    }

    public void testPerfectRichStringSize() {
      //Test a rich text string
      UnicodeString s = makeUnicodeString(SSTRecord.MAX_RECORD_SIZE-2-1-8-2);
      s.addFormatRun(new UnicodeString.FormatRun((short)1,(short)0));
      s.addFormatRun(new UnicodeString.FormatRun((short)2,(short)1));
      UnicodeString.UnicodeRecordStats stats = new UnicodeString.UnicodeRecordStats();
      s.setOptionFlags((byte)0x8);
      s.getRecordSize(stats);
      assertEquals(SSTRecord.MAX_RECORD_SIZE, stats.recordSize);

      //Test an uncompressed rich text string
      //Note that we can only ever get to a maximim size of 8227 since an uncompressed
      //string is writing double bytes.
      s = makeUnicodeString((SSTRecord.MAX_RECORD_SIZE-2-1-8-2)/2);
      s.addFormatRun(new UnicodeString.FormatRun((short)1,(short)0));
      s.addFormatRun(new UnicodeString.FormatRun((short)2,(short)1));
      s.setOptionFlags((byte)0x9);
      stats = new UnicodeString.UnicodeRecordStats();
      s.getRecordSize(stats);
      assertEquals(SSTRecord.MAX_RECORD_SIZE-1, stats.recordSize);
    }

    public void testContinuedStringSize() {
      //Test a basic string
      UnicodeString s = makeUnicodeString(SSTRecord.MAX_RECORD_SIZE-2-1+20);
      UnicodeString.UnicodeRecordStats stats = new UnicodeString.UnicodeRecordStats();
      s.getRecordSize(stats);
      assertEquals(SSTRecord.MAX_RECORD_SIZE+4+1+20, stats.recordSize);
    }

    /** Tests that a string size calculation that fits neatly in two records, the second being a continue*/
    public void testPerfectContinuedStringSize() {
      //Test a basic string
      int strSize = SSTRecord.MAX_RECORD_SIZE*2;
      //String overhead
      strSize -= 3;
      //Continue Record overhead
      strSize -= 4;
      //Continue Record additional byte overhead
      strSize -= 1;
      UnicodeString s = makeUnicodeString(strSize);
      UnicodeString.UnicodeRecordStats stats = new UnicodeString.UnicodeRecordStats();
      s.getRecordSize(stats);
      assertEquals(SSTRecord.MAX_RECORD_SIZE*2, stats.recordSize);
    }




    private static UnicodeString makeUnicodeString( String s )
    {
      UnicodeString st = new UnicodeString(s);
      st.setOptionFlags((byte)0);
      return st;
    }

    private static UnicodeString makeUnicodeString( int numChars) {
      StringBuffer b = new StringBuffer(numChars);
      for (int i=0;i<numChars;i++) {
        b.append(i%10);
      }
      return makeUnicodeString(b.toString());
    }
}
