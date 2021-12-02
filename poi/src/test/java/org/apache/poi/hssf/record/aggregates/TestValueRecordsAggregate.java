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

package org.apache.poi.hssf.record.aggregates;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.CRC32;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.model.RecordStream;
import org.apache.poi.hssf.model.RowBlocksReader;
import org.apache.poi.hssf.record.BlankRecord;
import org.apache.poi.hssf.record.CellValueRecordInterface;
import org.apache.poi.hssf.record.FormulaRecord;
import org.apache.poi.hssf.record.MulBlankRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.SharedFormulaRecord;
import org.apache.poi.hssf.record.WindowTwoRecord;
import org.apache.poi.hssf.record.aggregates.RecordAggregate.RecordVisitor;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.util.HexRead;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ValueRecordsAggregate}
 */
final class TestValueRecordsAggregate {
    private static final String ABNORMAL_SHARED_FORMULA_FLAG_TEST_FILE = "AbnormalSharedFormulaFlag.xls";
    private final ValueRecordsAggregate valueRecord = new ValueRecordsAggregate();

    private List<CellValueRecordInterface> getValueRecords() {
        List<CellValueRecordInterface> list = new ArrayList<>();
        for ( CellValueRecordInterface rec : valueRecord ) {
            list.add(rec);
        }
        return Collections.unmodifiableList(list);
    }

    /**
     * Make sure the shared formula DOESNT makes it to the FormulaRecordAggregate when being parsed
     * as part of the value records
     */
    @Test
    void testSharedFormula() {
        List<org.apache.poi.hssf.record.Record> records = new ArrayList<>();
        records.add(new FormulaRecord());
        records.add(new SharedFormulaRecord());
        records.add(new WindowTwoRecord());

        constructValueRecord(records);
        List<CellValueRecordInterface> cvrs = getValueRecords();
        //Ensure that the SharedFormulaRecord has been converted
        assertEquals(1, cvrs.size());

        CellValueRecordInterface record = cvrs.get(0);
        assertNotNull( record, "Row contains a value" );
        assertTrue( ( record instanceof FormulaRecordAggregate ), "First record is a FormulaRecordsAggregate");
    }

    private void constructValueRecord(List<org.apache.poi.hssf.record.Record> records) {
        RowBlocksReader rbr = new RowBlocksReader(new RecordStream(records, 0));
        SharedValueManager sfrh = rbr.getSharedFormulaManager();
        RecordStream rs = rbr.getPlainRecordStream();
        while(rs.hasNext()) {
            Record rec = rs.getNext();
            valueRecord.construct((CellValueRecordInterface)rec, rs, sfrh);
        }
    }

    private static List<org.apache.poi.hssf.record.Record> testData() {
        List<org.apache.poi.hssf.record.Record> records = new ArrayList<>();
        FormulaRecord formulaRecord = new FormulaRecord();
        BlankRecord blankRecord = new BlankRecord();
        formulaRecord.setRow(1);
        formulaRecord.setColumn((short) 1);
        blankRecord.setRow(2);
        blankRecord.setColumn((short) 2);
        records.add(formulaRecord);
        records.add(blankRecord);
        records.add(new WindowTwoRecord());
        return records;
    }

    @Test
    void testInsertCell() {
        assertEquals(0, getValueRecords().size());

        BlankRecord blankRecord = newBlankRecord();
        valueRecord.insertCell( blankRecord );
        assertEquals(1, getValueRecords().size());
    }

    @Test
    void testRemoveCell() {
        BlankRecord blankRecord1 = newBlankRecord();
        valueRecord.insertCell( blankRecord1 );
        BlankRecord blankRecord2 = newBlankRecord();
        valueRecord.removeCell( blankRecord2 );
        assertEquals(0, getValueRecords().size());

        // removing an already empty cell just falls through
        valueRecord.removeCell( blankRecord2 );
    }

    @Test
    void testGetPhysicalNumberOfCells() {
        assertEquals(0, valueRecord.getPhysicalNumberOfCells());
        BlankRecord blankRecord1 = newBlankRecord();
        valueRecord.insertCell( blankRecord1 );
        assertEquals(1, valueRecord.getPhysicalNumberOfCells());
        valueRecord.removeCell( blankRecord1 );
        assertEquals(0, valueRecord.getPhysicalNumberOfCells());
    }

    @Test
    void testGetFirstCellNum() {
        assertEquals( -1, valueRecord.getFirstCellNum() );
        valueRecord.insertCell( newBlankRecord( 2, 2 ) );
        assertEquals( 2, valueRecord.getFirstCellNum() );
        valueRecord.insertCell( newBlankRecord( 3, 3 ) );
        assertEquals( 2, valueRecord.getFirstCellNum() );

        // Note: Removal doesn't currently reset the first column.  It probably should but it doesn't.
        valueRecord.removeCell( newBlankRecord( 2, 2 ) );
        assertEquals( 2, valueRecord.getFirstCellNum() );
    }

    @Test
    void testGetLastCellNum() {
        assertEquals( -1, valueRecord.getLastCellNum() );
        valueRecord.insertCell( newBlankRecord( 2, 2 ) );
        assertEquals( 2, valueRecord.getLastCellNum() );
        valueRecord.insertCell( newBlankRecord( 3, 3 ) );
        assertEquals( 3, valueRecord.getLastCellNum() );

        // Note: Removal doesn't currently reset the last column.  It probably should but it doesn't.
        valueRecord.removeCell( newBlankRecord( 3, 3 ) );
        assertEquals( 3, valueRecord.getLastCellNum() );

    }


    private static final class SerializerVisitor implements RecordVisitor {
        private final byte[] _buf;
        private int _writeIndex;
        public SerializerVisitor(byte[] buf) {
            _buf = buf;
            _writeIndex = 0;

        }
        @Override
        public void visitRecord(org.apache.poi.hssf.record.Record r) {
            r.serialize(_writeIndex, _buf);
            _writeIndex += r.getRecordSize();
        }
        public int getWriteIndex() {
            return _writeIndex;
        }
    }

    @Test
    void testSerialize() {
        byte[] expectedArray = HexRead.readFromString(""
                + "06 00 16 00 " // Formula
                + "01 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
                + "01 02 06 00 " // Blank
                + "02 00 02 00 00 00");
        byte[] actualArray = new byte[expectedArray.length];
        List<org.apache.poi.hssf.record.Record> records = testData();
        constructValueRecord(records);

        SerializerVisitor sv = new SerializerVisitor(actualArray);
        valueRecord.visitCellsForRow(1, sv);
        valueRecord.visitCellsForRow(2, sv);
        assertEquals(actualArray.length, sv.getWriteIndex());
        assertArrayEquals(expectedArray, actualArray);
    }

    private static BlankRecord newBlankRecord() {
        return newBlankRecord( 2, 2 );
    }

    private static BlankRecord newBlankRecord(int col, int row) {
        BlankRecord blankRecord = new BlankRecord();
        blankRecord.setRow( row );
        blankRecord.setColumn( (short) col );
        return blankRecord;
    }

    /**
     * Sometimes the 'shared formula' flag ({@code FormulaRecord.isSharedFormula()}) is set when
     * there is no corresponding SharedFormulaRecord available. SharedFormulaRecord definitions do
     * not span multiple sheets.  They are only defined within a sheet, and thus they do not
     * have a sheet index field (only row and column range fields).<br>
     * So it is important that the code which locates the SharedFormulaRecord for each
     * FormulaRecord does not allow matches across sheets.<p>
     *
     * Prior to bugzilla 44449 (Feb 2008), POI {@code ValueRecordsAggregate.construct(int, List)}
     * allowed {@code SharedFormulaRecord}s to be erroneously used across sheets.  That incorrect
     * behaviour is shown by this test.<p>
     *
     * <b>Notes on how to produce the test spreadsheet</b>:</p>
     * The setup for this test (AbnormalSharedFormulaFlag.xls) is rather fragile, insomuchas
     * re-saving the file (either with Excel or POI) clears the flag.<br>
     * <ol>
     * <li>A new spreadsheet was created in Excel (File | New | Blank Workbook).</li>
     * <li>Sheet3 was deleted.</li>
     * <li>Sheet2!A1 formula was set to '="second formula"', and fill-dragged through A1:A8.</li>
     * <li>Sheet1!A1 formula was set to '="first formula"', and also fill-dragged through A1:A8.</li>
     * <li>Four rows on Sheet1 "5" through "8" were deleted ('delete rows' alt-E D, not 'clear' Del).</li>
     * <li>The spreadsheet was saved as AbnormalSharedFormulaFlag.xls.</li>
     * </ol>
     * Prior to the row delete action the spreadsheet has two {@code SharedFormulaRecord}s. One
     * for each sheet. To expose the bug, the shared formulas have been made to overlap.<br>
     * The row delete action (as described here) seems to to delete the
     * {@code SharedFormulaRecord} from Sheet1 (but not clear the 'shared formula' flags.<br>
     * There are other variations on this theme to create the same effect.
     *
     */
    @Test
    void testSpuriousSharedFormulaFlag() throws Exception {

        long actualCRC = getFileCRC(HSSFTestDataSamples.openSampleFileStream(ABNORMAL_SHARED_FORMULA_FLAG_TEST_FILE));
        long expectedCRC = 2277445406L;
        if(actualCRC != expectedCRC) {
            System.err.println("Expected crc " + expectedCRC  + " but got " + actualCRC);
            throw failUnexpectedTestFileChange();
        }
        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook(ABNORMAL_SHARED_FORMULA_FLAG_TEST_FILE);

        HSSFSheet s = wb.getSheetAt(0); // Sheet1

        String cellFormula;
        cellFormula = getFormulaFromFirstCell(s, 0); // row "1"
        // the problem is not observable in the first row of the shared formula
        assertEquals("\"first formula\"", cellFormula, "Something else wrong with this test case");

        // but the problem is observable in rows 2,3,4
        cellFormula = getFormulaFromFirstCell(s, 1); // row "2"
        assertNotEquals("\"second formula\"", cellFormula, "found bug 44449 (Wrong SharedFormulaRecord was used).");

        assertEquals("\"first formula\"", cellFormula, "Something else wrong with this test case");

        wb.close();
    }
    private static String getFormulaFromFirstCell(HSSFSheet s, int rowIx) {
        return s.getRow(rowIx).getCell(0).getCellFormula();
    }

    /**
     * If someone opened this particular test file in Excel and saved it, the peculiar condition
     * which causes the target bug would probably disappear.  This test would then just succeed
     * regardless of whether the fix was present.  So a CRC check is performed to make it less easy
     * for that to occur.
     */
    private static RuntimeException failUnexpectedTestFileChange() {
        String msg = "Test file '" + ABNORMAL_SHARED_FORMULA_FLAG_TEST_FILE + "' has changed.  "
            + "This junit may not be properly testing for the target bug.  "
            + "Either revert the test file or ensure that the new version "
            + "has the right characteristics to test the target bug.";
        // A breakpoint in ValueRecordsAggregate.handleMissingSharedFormulaRecord(FormulaRecord)
        // should get hit during parsing of Sheet1.
        // If the test spreadsheet is created as directed, this condition should occur.
        // It is easy to upset the test spreadsheet (for example re-saving will destroy the
        // peculiar condition we are testing for).
        throw new RuntimeException(msg);
    }

    /**
     * gets a CRC checksum for the content of a file
     */
    private static long getFileCRC(InputStream is) {
        CRC32 crc = new CRC32();
        byte[] buf = new byte[2048];
        try {
            while(true) {
                int bytesRead = is.read(buf);
                if(bytesRead < 1) {
                    break;
                }
                crc.update(buf, 0, bytesRead);
            }
            is.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return crc.getValue();
    }

    @Test
    void testRemoveNewRow_bug46312() throws IOException {
        // To make bug occur, rowIndex needs to be >= ValueRecordsAggregate.records.length
        int rowIndex = 30;

        ValueRecordsAggregate vra = new ValueRecordsAggregate();
        // bug 46312 - Specified rowIndex 30 is outside the allowable range (0..30)
        assertDoesNotThrow(() -> vra.removeAllCellsValuesForRow(rowIndex));

        // same bug as demonstrated through usermodel API
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet();
            HSSFRow row = sheet.createRow(rowIndex);

            // must not add any cells to the new row if we want to see the bug
            // row.createCell(0); // this causes ValueRecordsAggregate.records to auto-extend
            assertDoesNotThrow(() -> sheet.createRow(rowIndex));
        }
    }

    /**
     * Tests various manipulations of blank cells, to make sure that {@link MulBlankRecord}s
     * are use appropriately
     */
    @Test
    void testMultipleBlanks() {
        BlankRecord brA2 = newBlankRecord(0, 1);
        BlankRecord brB2 = newBlankRecord(1, 1);
        BlankRecord brC2 = newBlankRecord(2, 1);
        BlankRecord brD2 = newBlankRecord(3, 1);
        BlankRecord brE2 = newBlankRecord(4, 1);
        BlankRecord brB3 = newBlankRecord(1, 2);
        BlankRecord brC3 = newBlankRecord(2, 2);

        valueRecord.insertCell(brA2);
        valueRecord.insertCell(brB2);
        valueRecord.insertCell(brD2);
        confirmMulBlank(3, 1, 1);

        valueRecord.insertCell(brC3);
        confirmMulBlank(4, 1, 2);

        valueRecord.insertCell(brB3);
        valueRecord.insertCell(brE2);
        confirmMulBlank(6, 3, 0);

        valueRecord.insertCell(brC2);
        confirmMulBlank(7, 2, 0);

        valueRecord.removeCell(brA2);
        confirmMulBlank(6, 2, 0);

        valueRecord.removeCell(brC2);
        confirmMulBlank(5, 2, 1);

        valueRecord.removeCell(brC3);
        confirmMulBlank(4, 1, 2);
    }

    private void confirmMulBlank(int expectedTotalBlankCells,
            int expectedNumberOfMulBlankRecords, int expectedNumberOfSingleBlankRecords) {
        // assumed row ranges set-up by caller:
        final int firstRow = 1;
        final int lastRow = 2;


        final class BlankStats {
            public int countBlankCells;
            public int countMulBlankRecords;
            public int countSingleBlankRecords;
        }

        final BlankStats bs = new BlankStats();
        RecordVisitor rv = r -> {
            if (r instanceof MulBlankRecord) {
                MulBlankRecord mbr = (MulBlankRecord) r;
                bs.countMulBlankRecords++;
                bs.countBlankCells += mbr.getNumColumns();
            } else if (r instanceof BlankRecord) {
                bs.countSingleBlankRecords++;
                bs.countBlankCells++;
            }
        };

        for (int rowIx = firstRow; rowIx <=lastRow; rowIx++) {
            if (valueRecord.rowHasCells(rowIx)) {
                valueRecord.visitCellsForRow(rowIx, rv);
            }
        }
        assertEquals(expectedTotalBlankCells, bs.countBlankCells);
        assertEquals(expectedNumberOfMulBlankRecords, bs.countMulBlankRecords);
        assertEquals(expectedNumberOfSingleBlankRecords, bs.countSingleBlankRecords);
    }
}
