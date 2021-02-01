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

package org.apache.poi.hssf.record.common;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.apache.poi.hssf.record.ContinueRecord;
import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.hssf.record.SSTRecord;
import org.apache.poi.hssf.record.cont.ContinuableRecordOutput;
import org.apache.poi.hssf.usermodel.HSSFOptimiser;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.util.LittleEndianByteArrayInputStream;
import org.apache.poi.util.LittleEndianByteArrayOutputStream;
import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.LittleEndianInput;
import org.apache.poi.util.LittleEndianInputStream;
import org.apache.poi.util.LittleEndianOutputStream;
import org.apache.poi.util.StringUtil;
import org.junit.jupiter.api.Test;

/**
 * Tests that {@link UnicodeString} record size calculates correctly.  The record size
 * is used when serializing {@link SSTRecord}s.
 *
 * @author Jason Height (jheight at apache.org)
 */
final class TestUnicodeString {
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

    @Test
    void smallStringSize() {
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
        FormatRun r = new FormatRun((short)0,(short)1);
        s.addFormatRun(r);
        FormatRun r2 = new FormatRun((short)2,(short)2);
        s.addFormatRun(r2);
        confirmSize(17, s);

        //Test a uncompressed small string that has rich text formatting
        s.setString(STR_16_BIT);
        s.setOptionFlags((byte)0x9);
        confirmSize(21, s);

        //Test a compressed small string that has rich text and extended text
        s.setString("Test");
        s.setOptionFlags((byte)0xC);
        confirmSize(17, s);

        // Extended phonetics data
        // Minimum size is 14
        // Also adds 4 bytes to hold the length
        s.setExtendedRst(
              new ExtRst()
        );
        confirmSize(35, s);

        //Test a uncompressed small string that has rich text and extended text
        s.setString(STR_16_BIT);
        s.setOptionFlags((byte)0xD);
        confirmSize(39, s);

        s.setExtendedRst(null);
        confirmSize(21, s);
    }

    @Test
    void perfectStringSize() {
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

    @Test
    void perfectRichStringSize() {
      //Test a rich text string
      UnicodeString s = makeUnicodeString(MAX_DATA_SIZE-2-1-8-2);
      s.addFormatRun(new FormatRun((short)1,(short)0));
      s.addFormatRun(new FormatRun((short)2,(short)1));
      s.setOptionFlags((byte)0x8);
      confirmSize(MAX_DATA_SIZE, s);

      //Test an uncompressed rich text string
      //Note that we can only ever get to a maximum size of 8227 since an uncompressed
      //string is writing double bytes.
      s = makeUnicodeString((MAX_DATA_SIZE-2-1-8-2)/2, true);
      s.addFormatRun(new FormatRun((short)1,(short)0));
      s.addFormatRun(new FormatRun((short)2,(short)1));
      s.setOptionFlags((byte)0x9);
      confirmSize(MAX_DATA_SIZE-1, s);
    }

    @Test
    void continuedStringSize() {
      //Test a basic string
      UnicodeString s = makeUnicodeString(MAX_DATA_SIZE-2-1+20);
      confirmSize(MAX_DATA_SIZE+4+1+20, s);
    }

    /** Tests that a string size calculation that fits neatly in two records, the second being a continue*/
    @Test
    void perfectContinuedStringSize() {
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

    @Test
    void formatRun() {
       FormatRun fr = new FormatRun((short)4, (short)0x15c);
       assertEquals(4, fr.getCharacterPos());
       assertEquals(0x15c, fr.getFontIndex());

       ByteArrayOutputStream baos = new ByteArrayOutputStream();
       LittleEndianOutputStream out = new LittleEndianOutputStream(baos);

       fr.serialize(out);

       byte[] b = baos.toByteArray();
       assertEquals(4, b.length);
       assertEquals(4, b[0]);
       assertEquals(0, b[1]);
       assertEquals(0x5c, b[2]);
       assertEquals(0x01, b[3]);

       LittleEndianInputStream inp = new LittleEndianInputStream(
             new ByteArrayInputStream(b)
       );
       fr = new FormatRun(inp);
       assertEquals(4, fr.getCharacterPos());
       assertEquals(0x15c, fr.getFontIndex());
    }

    @Test
    void extRstFromEmpty() {
       ExtRst ext = new ExtRst();

       assertEquals(0, ext.getNumberOfRuns());
       assertEquals(0, ext.getFormattingFontIndex());
       assertEquals(0, ext.getFormattingOptions());
       assertEquals("", ext.getPhoneticText());
       assertEquals(0, ext.getPhRuns().length);
       assertEquals(10, ext.getDataSize()); // Excludes 4 byte header

       ByteArrayOutputStream baos = new ByteArrayOutputStream();
       LittleEndianOutputStream out = new LittleEndianOutputStream(baos);
       ContinuableRecordOutput cout = new ContinuableRecordOutput(out, 0xffff);

       ext.serialize(cout);
       cout.writeContinue();

       byte[] b = baos.toByteArray();
       assertEquals(20, b.length);

       // First 4 bytes from the outputstream
       assertEquals(-1, b[0]);
       assertEquals(-1, b[1]);
       assertEquals(14, b[2]);
       assertEquals(0, b[3]);

       // Reserved
       assertEquals(1, b[4]);
       assertEquals(0, b[5]);
       // Data size
       assertEquals(10, b[6]);
       assertEquals(0, b[7]);
       // Font*2
       assertEquals(0, b[8]);
       assertEquals(0, b[9]);
       assertEquals(0, b[10]);
       assertEquals(0, b[11]);
       // 0 Runs
       assertEquals(0, b[12]);
       assertEquals(0, b[13]);
       // Size=0, *2
       assertEquals(0, b[14]);
       assertEquals(0, b[15]);
       assertEquals(0, b[16]);
       assertEquals(0, b[17]);

       // Last 2 bytes from the outputstream
       assertEquals(ContinueRecord.sid, b[18]);
       assertEquals(0, b[19]);


       // Load in again and re-test
       byte[] data = Arrays.copyOfRange(b, 4, 4+14);
       LittleEndianInputStream inp = new LittleEndianInputStream(new ByteArrayInputStream(data));
       ext = new ExtRst(inp, data.length);

       assertEquals(0, ext.getNumberOfRuns());
       assertEquals(0, ext.getFormattingFontIndex());
       assertEquals(0, ext.getFormattingOptions());
       assertEquals("", ext.getPhoneticText());
       assertEquals(0, ext.getPhRuns().length);
    }

    @Test
    void extRstFromData() {
       byte[] data = new byte[] {
               1, 0, 0x0C, 0,
               0, 0, 0x37, 0,
               0, 0,
               0, 0, 0, 0,
               0, 0 // Cruft at the end, as found from real files
       };
       assertEquals(16, data.length);

       LittleEndianInputStream inp = new LittleEndianInputStream(
             new ByteArrayInputStream(data)
       );
       ExtRst ext = new ExtRst(inp, data.length);
       assertEquals(0x0c, ext.getDataSize()); // Excludes 4 byte header

       assertEquals(0, ext.getNumberOfRuns());
       assertEquals(0x37, ext.getFormattingOptions());
       assertEquals(0, ext.getFormattingFontIndex());
       assertEquals("", ext.getPhoneticText());
       assertEquals(0, ext.getPhRuns().length);
    }

    @Test
    void corruptExtRstDetection() {
       byte[] data = new byte[] {
             0x79, 0x79, 0x11, 0x11,
             0x22, 0x22, 0x33, 0x33,
       };
       assertEquals(8, data.length);

       LittleEndianInputStream inp = new LittleEndianInputStream(
             new ByteArrayInputStream(data)
       );
       ExtRst ext = new ExtRst(inp, data.length);

       // Will be empty
       assertEquals(ext, new ExtRst());

       // If written, will be the usual size
       assertEquals(10, ext.getDataSize()); // Excludes 4 byte header

       // Is empty
       assertEquals(0, ext.getNumberOfRuns());
       assertEquals(0, ext.getFormattingOptions());
       assertEquals(0, ext.getFormattingFontIndex());
       assertEquals("", ext.getPhoneticText());
       assertEquals(0, ext.getPhRuns().length);
    }

    @Test
    void extRstEqualsAndHashCode() {
        byte[] buf = new byte[200];
        LittleEndianByteArrayOutputStream bos = new LittleEndianByteArrayOutputStream(buf, 0);
        String str = "\u1d02\u1d12\u1d22";
        bos.writeShort(1);
        bos.writeShort(5*LittleEndianConsts.SHORT_SIZE+str.length()*2+3*LittleEndianConsts.SHORT_SIZE+2); // data size
        bos.writeShort(0x4711);
        bos.writeShort(0x0815);
        bos.writeShort(1);
        bos.writeShort(str.length());
        bos.writeShort(str.length());
        StringUtil.putUnicodeLE(str, bos);
        bos.writeShort(1);
        bos.writeShort(1);
        bos.writeShort(3);
        bos.writeShort(42);

        LittleEndianInput in = new LittleEndianByteArrayInputStream(buf, 0, bos.getWriteIndex());
        ExtRst extRst1 = new ExtRst(in, bos.getWriteIndex());
        in = new LittleEndianByteArrayInputStream(buf, 0, bos.getWriteIndex());
        ExtRst extRst2 = new ExtRst(in, bos.getWriteIndex());

        assertEquals(extRst1, extRst2);
        assertEquals(extRst1.hashCode(), extRst2.hashCode());
    }

    @Test
    void unicodeStringsNullPointer() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {

            Sheet sheet = wb.createSheet("styles");
            Row row = sheet.createRow(0);
            Cell cell = row.createCell(0);

            CellStyle style = wb.createCellStyle();
            style.setFont(wb.createFont());
            cell.setCellStyle(style);

            cell.setCellValue("test");

            assertDoesNotThrow(() -> HSSFOptimiser.optimiseFonts(wb));
        }
    }

    @Test
    void copyExtRst() {
        ExtRst ext = new ExtRst();

        assertEquals(0, ext.getNumberOfRuns());
        assertEquals(0, ext.getFormattingFontIndex());
        assertEquals(0, ext.getFormattingOptions());
        assertEquals("", ext.getPhoneticText());
        assertEquals(0, ext.getPhRuns().length);
        assertEquals(10, ext.getDataSize()); // Excludes 4 byte header

        ExtRst copied = ext.copy();

        assertEquals(0, copied.getNumberOfRuns());
        assertEquals(0, copied.getFormattingFontIndex());
        assertEquals(0, copied.getFormattingOptions());
        assertEquals("", copied.getPhoneticText());
        assertEquals(0, copied.getPhRuns().length);
        assertEquals(10, copied.getDataSize());
    }

    @Test
    void copyExtRstFromData() {
        byte[] data = new byte[]{
                1, 0, 0x0C, 0,
                0, 0, 0x37, 0,
                0, 0,
                0, 0, 0, 0,
                0, 0 // Cruft at the end, as found from real files
        };
        assertEquals(16, data.length);

        LittleEndianInputStream inp = new LittleEndianInputStream(
                new ByteArrayInputStream(data)
        );
        ExtRst ext = new ExtRst(inp, data.length);
        assertEquals(0x0c, ext.getDataSize()); // Excludes 4 byte header

        assertEquals(0, ext.getNumberOfRuns());
        assertEquals(0x37, ext.getFormattingOptions());
        assertEquals(0, ext.getFormattingFontIndex());
        assertEquals("", ext.getPhoneticText());
        assertEquals(0, ext.getPhRuns().length);

        ExtRst copied = ext.copy();
        assertEquals(10, copied.getDataSize()); // Excludes 4 byte header

        assertEquals(0, copied.getNumberOfRuns());
        assertEquals(0x37, copied.getFormattingOptions());
        assertEquals(0, copied.getFormattingFontIndex());
        assertEquals("", copied.getPhoneticText());
        assertEquals(0, copied.getPhRuns().length);
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
      StringBuilder b = new StringBuilder(numChars);
        int charBase = is16Bit ? 0x8A00 : 'A';
        for (int i = 0; i < numChars; i++) {
            char ch = (char) ((i % 16) + charBase);
            b.append(ch);
        }
        return makeUnicodeString(b.toString());
    }
}
