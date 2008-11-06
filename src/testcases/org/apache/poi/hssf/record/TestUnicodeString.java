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

import org.apache.poi.hssf.record.cont.ContinuableRecordOutput;

/**
 * Tests that {@link UnicodeString} record size calculates correctly.  The record size
 * is used when serializing {@link SSTRecord}s.
 *
 * @author Jason Height (jheight at apache.org)
 */
public final class TestUnicodeString extends TestCase {
    private static final int MAX_DATA_SIZE = RecordInputStream.MAX_RECORD_DATA_SIZE;

    /** a 4 character string requiring 16 bit encoding */
    private static final String STR_16_BIT = "A\u591A\u8A00\u8A9E";

    private static void confirmSize(int expectedSize, UnicodeString s) {
        confirmSize(expectedSize, s, 0);
    }
    /**
     * Note - a value of zero for <tt>amountUsedInCurrentRecord</tt> would only ever occur just
     * after a {@link ContinueRecord} had been started.  In the initial {@link SSTRecord} this 
     * value starts at 8 (for the first {@link UnicodeString} written).  In general, it can be
     * any value between 0 and {@link #MAX_DATA_SIZE}
     */
    private static void confirmSize(int expectedSize, UnicodeString s, int amountUsedInCurrentRecord) {
        ContinuableRecordOutput out = ContinuableRecordOutput.createForCountingOnly();
        out.writeContinue();
        for(int i=amountUsedInCurrentRecord; i>0; i--) {
            out.writeByte(0);
        }
        int size0 = out.getTotalSize();
        s.serialize(out);
        int size1 = out.getTotalSize();
        int actualSize = size1-size0;
        assertEquals(expectedSize, actualSize);
    }

    public void testSmallStringSize() {
        //Test a basic string
        UnicodeString s = makeUnicodeString("Test");
        confirmSize(7, s);

        //Test a small string that is uncompressed
        s = makeUnicodeString(STR_16_BIT);
        s.setOptionFlags((byte)0x01);
        confirmSize(11, s);

        //Test a compressed small string that has rich text formatting
        s.setString("Test");
        s.setOptionFlags((byte)0x8);
        UnicodeString.FormatRun r = new UnicodeString.FormatRun((short)0,(short)1);
        s.addFormatRun(r);
        UnicodeString.FormatRun r2 = new UnicodeString.FormatRun((short)2,(short)2);
        s.addFormatRun(r2);
        confirmSize(17, s);

        //Test a uncompressed small string that has rich text formatting
        s.setString(STR_16_BIT);
        s.setOptionFlags((byte)0x9);
        confirmSize(21, s);

        //Test a compressed small string that has rich text and extended text
        s.setString("Test");
        s.setOptionFlags((byte)0xC);
        s.setExtendedRst(new byte[]{(byte)0x1,(byte)0x2,(byte)0x3,(byte)0x4,(byte)0x5});
        confirmSize(26, s);

        //Test a uncompressed small string that has rich text and extended text
        s.setString(STR_16_BIT);
        s.setOptionFlags((byte)0xD);
        confirmSize(30, s);
    }

    public void testPerfectStringSize() {
      //Test a basic string
      UnicodeString s = makeUnicodeString(MAX_DATA_SIZE-2-1);
      confirmSize(MAX_DATA_SIZE, s);

      //Test an uncompressed string
      //Note that we can only ever get to a maximim size of 8227 since an uncompressed
      //string is writing double bytes.
      s = makeUnicodeString((MAX_DATA_SIZE-2-1)/2, true);
      s.setOptionFlags((byte)0x1);
      confirmSize(MAX_DATA_SIZE-1, s);
    }

    public void testPerfectRichStringSize() {
      //Test a rich text string
      UnicodeString s = makeUnicodeString(MAX_DATA_SIZE-2-1-8-2);
      s.addFormatRun(new UnicodeString.FormatRun((short)1,(short)0));
      s.addFormatRun(new UnicodeString.FormatRun((short)2,(short)1));
      s.setOptionFlags((byte)0x8);
      confirmSize(MAX_DATA_SIZE, s);

      //Test an uncompressed rich text string
      //Note that we can only ever get to a maximum size of 8227 since an uncompressed
      //string is writing double bytes.
      s = makeUnicodeString((MAX_DATA_SIZE-2-1-8-2)/2, true);
      s.addFormatRun(new UnicodeString.FormatRun((short)1,(short)0));
      s.addFormatRun(new UnicodeString.FormatRun((short)2,(short)1));
      s.setOptionFlags((byte)0x9);
      confirmSize(MAX_DATA_SIZE-1, s);
    }

    public void testContinuedStringSize() {
      //Test a basic string
      UnicodeString s = makeUnicodeString(MAX_DATA_SIZE-2-1+20);
      confirmSize(MAX_DATA_SIZE+4+1+20, s);
    }

    /** Tests that a string size calculation that fits neatly in two records, the second being a continue*/
    public void testPerfectContinuedStringSize() {
      //Test a basic string
      int strSize = MAX_DATA_SIZE*2;
      //String overhead
      strSize -= 3;
      //Continue Record overhead
      strSize -= 4;
      //Continue Record additional byte overhead
      strSize -= 1;
      UnicodeString s = makeUnicodeString(strSize);
      confirmSize(MAX_DATA_SIZE*2, s);
    }


    private static UnicodeString makeUnicodeString(String s) {
      UnicodeString st = new UnicodeString(s);
      st.setOptionFlags((byte)0);
      return st;
    }

    private static UnicodeString makeUnicodeString(int numChars) {
        return makeUnicodeString(numChars, false);
    }
    /**
     * @param is16Bit if <code>true</code> the created string will have characters > 0x00FF
     * @return a string of the specified number of characters
     */
    private static UnicodeString makeUnicodeString(int numChars, boolean is16Bit) {
      StringBuffer b = new StringBuffer(numChars);
      int charBase = is16Bit ? 0x8A00 : 'A';
      for (int i=0;i<numChars;i++) {
        char ch = (char) ((i%16)+charBase);
        b.append(ch);
      }
      return makeUnicodeString(b.toString());
    }
}
