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
package org.apache.poi.hssf.model;

import static org.apache.poi.poifs.storage.RawDataUtil.decompress;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.poi.POIDataSamples;
import org.apache.poi.ddf.DefaultEscherRecordFactory;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherDggRecord;
import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.ddf.EscherRecordFactory;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.record.ContinueRecord;
import org.apache.poi.hssf.record.DrawingRecord;
import org.apache.poi.hssf.record.EOFRecord;
import org.apache.poi.hssf.record.EscherAggregate;
import org.apache.poi.hssf.record.NoteRecord;
import org.apache.poi.hssf.record.ObjRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.RecordBase;
import org.apache.poi.hssf.record.RecordFactory;
import org.apache.poi.hssf.record.TextObjectRecord;
import org.apache.poi.hssf.record.WindowTwoRecord;
import org.apache.poi.hssf.record.aggregates.RowRecordsAggregate;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFTestHelper;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TestDrawingAggregate {
    /**
     *  information about drawing aggregate in a worksheet
     */
    private static class DrawingAggregateInfo {
        /**
         * start and end indices of the aggregate in the worksheet stream
         */
        private int startRecordIndex, endRecordIndex;
        /**
         * the records being aggregated
         */
        private List<RecordBase> aggRecords;

        /**
         * @return aggregate info or null if the sheet does not contain drawing objects
         */
        static DrawingAggregateInfo get(HSSFSheet sheet){
            DrawingAggregateInfo info = null;
            InternalSheet isheet = HSSFTestHelper.getSheetForTest(sheet);
            List<RecordBase> records = isheet.getRecords();
            for(int i = 0; i < records.size(); i++){
                RecordBase rb = records.get(i);
                if((rb instanceof DrawingRecord) && info == null) {
                    info = new DrawingAggregateInfo();
                    info.startRecordIndex = i;
                    info.endRecordIndex = i;
                } else if (info != null && (
                        rb instanceof DrawingRecord
                                || rb instanceof ObjRecord
                                || rb instanceof TextObjectRecord
                                || rb instanceof ContinueRecord
                                || rb instanceof NoteRecord
                )){
                    info.endRecordIndex = i;
                } else {
                    if(rb instanceof EscherAggregate)
                        throw new IllegalStateException("Drawing data already aggregated. " +
                                "You should cal this method before the first invocation of HSSFSheet#getDrawingPatriarch()");
                    if (info != null) break;
                }
            }
            if(info != null){
                info.aggRecords = new ArrayList<>(
                        records.subList(info.startRecordIndex, info.endRecordIndex + 1));
            }
            return info;
        }

        /**
         * @return the raw data being aggregated
         */
        byte[] getRawBytes(){
            UnsynchronizedByteArrayOutputStream out = UnsynchronizedByteArrayOutputStream.builder().get();
            for (RecordBase rb : aggRecords) {
                Record r = (org.apache.poi.hssf.record.Record) rb;
                try {
                    out.write(r.serialize());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return out.toByteArray();
        }
    }

    public static Stream<Arguments> samples() {
        String property = System.getProperty(POIDataSamples.TEST_PROPERTY, "test-data");
        File testData = new File(property, "spreadsheet");
        if (!testData.exists()) {
            testData = new File("../" + property, "spreadsheet");
        }

        File[] files = testData.listFiles((dir, name) -> name.endsWith(".xls"));
        assertNotNull(files, "Need to find files in test-data path, had path: " + testData);
        return Stream.of(files).
                filter(file -> !file.getName().equals("clusterfuzz-testcase-minimized-POIHSSFFuzzer-5285517825277952.xls")).
                map(Arguments::of);
    }

    /**
     * test that we correctly read and write drawing aggregates in all .xls files in POI test samples.
     * iterate over all sheets, aggregate drawing records (if there are any)
     * and remember information about the aggregated data.
     * Then serialize the workbook, read back and assert that the aggregated data is preserved.
     *
     * The assertion is strict meaning that the drawing data before and after save must be equal.
     */
    @ParameterizedTest
    @MethodSource("samples")
    void testAllTestSamples(File file) throws IOException {
        boolean ignoreParse = true;
        try (HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook(file.getName())) {
            ignoreParse = false;

            // map aggregate info by sheet index
            Map<Integer, DrawingAggregateInfo> aggs = new HashMap<>();
            for (int i = 0; i < wb.getNumberOfSheets(); i++){
                HSSFSheet sheet = wb.getSheetAt(i);
                DrawingAggregateInfo info = DrawingAggregateInfo.get(sheet);
                if(info != null) {
                    aggs.put(i, info);
                    HSSFPatriarch p = sheet.getDrawingPatriarch();

                    // compare aggregate.serialize() with raw bytes from the record stream
                    EscherAggregate agg = HSSFTestHelper.getEscherAggregate(p);

                    byte[] dgBytes1 = info.getRawBytes();
                    byte[] dgBytes2 = agg.serialize();

                    assertEquals(dgBytes1.length, dgBytes2.length, "different size of raw data ande aggregate.serialize()");
                    assertArrayEquals(dgBytes1, dgBytes2, "raw drawing data (" + dgBytes1.length + " bytes) and aggregate.serialize() are different.");
                }
            }

            if(aggs.size() != 0){
                try (HSSFWorkbook wb2 = HSSFTestDataSamples.writeOutAndReadBack(wb)) {
                    for (int i = 0; i < wb2.getNumberOfSheets(); i++) {
                        DrawingAggregateInfo info1 = aggs.get(i);
                        if (info1 != null) {
                            HSSFSheet sheet2 = wb2.getSheetAt(i);
                            DrawingAggregateInfo info2 = DrawingAggregateInfo.get(sheet2);
                            byte[] dgBytes1 = info1.getRawBytes();
                            byte[] dgBytes2 = info2.getRawBytes();
                            assertEquals(dgBytes1.length, dgBytes2.length, "different size of drawing data before and after save");
                            assertArrayEquals(dgBytes1, dgBytes2, "drawing data (" + dgBytes1.length + " bytes) before and after save is different.");
                        }
                    }
                }
            }

        } catch (Throwable e) {
            // don't bother about files we cannot read - they are different bugs
            if (!ignoreParse) {
                throw e;
            }
        }
    }

    /**
     * when reading incomplete data ensure that the serialized bytes match the source
     */
    @Test
    void testIncompleteData() throws IOException {
        //EscherDgContainer and EscherSpgrContainer length exceeds the actual length of the data
        String data =
            "H4sIAAAAAAAAAGWOOw7CQAxE32YTsSRIWSgQJSUloqSm5g4ICURBg+iBK3APGi6wBWeh9xGYbEps2WON"+
            "P+OWwpYeIsECMFC8S2jxNvMdlrYQ5xha5N8K6ryHdir6+avwOer5l3hq2NPYWuWN0n1dIsgfbgshuSj1"+
            "+2eqbvLdxQ0ndhy5KJ/lc1ZZK9okY5X/gSbrHZTH1vE/ozagTcwAAAA=";
        byte[] dgBytes = decompress(data);

        List<EscherRecord> records = new ArrayList<>();
        EscherRecordFactory recordFactory = new DefaultEscherRecordFactory();
        int pos = 0;
        while (pos < dgBytes.length) {
            EscherRecord r = recordFactory.createRecord(dgBytes, pos);
            int bytesRead = r.fillFields(dgBytes, pos, recordFactory);
            records.add(r);
            pos += bytesRead;
        }
        assertEquals(dgBytes.length, pos, "data was not fully read");

        // serialize to byte array
        UnsynchronizedByteArrayOutputStream out = UnsynchronizedByteArrayOutputStream.builder().get();
        for(EscherRecord r : records) {
            out.write(r.serialize());
        }
        assertArrayEquals(dgBytes, out.toByteArray());
    }

    /**
     * TODO: figure out why it fails with "RecordFormatException: 0 bytes written but getRecordSize() reports 80"
     */
    @Test
    void testFailing() throws IOException {
        try (HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("15573.xls")) {
            HSSFSheet sh = wb.getSheetAt(0);
            HSSFPatriarch dp = sh.getDrawingPatriarch();
            assertNotNull(dp);

            try (HSSFWorkbook wb2 = HSSFTestDataSamples.writeOutAndReadBack(wb)) {
                HSSFSheet sh2 = wb2.getSheetAt(0);
                HSSFPatriarch dp2 = sh2.getDrawingPatriarch();
                assertNotNull(dp2);
            }
        }
    }

    private static byte[] toByteArray(List<RecordBase> records) {
        UnsynchronizedByteArrayOutputStream out = UnsynchronizedByteArrayOutputStream.builder().get();
        for (RecordBase rb : records) {
            Record r = (org.apache.poi.hssf.record.Record) rb;
            try {
                out.write(r.serialize());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return out.toByteArray();
    }

    @Test
    void testSolverContainerMustBeSavedDuringSerialization() throws IOException{
        try (HSSFWorkbook wb1 = HSSFTestDataSamples.openSampleWorkbook("SolverContainerAfterSPGR.xls")) {
            HSSFSheet sh = wb1.getSheetAt(0);
            InternalSheet ish = HSSFTestHelper.getSheetForTest(sh);
            List<RecordBase> records = ish.getRecords();
            // records to be aggregated
            List<RecordBase> dgRecords = records.subList(19, 22);
            byte[] dgBytes = toByteArray(dgRecords);
            sh.getDrawingPatriarch();
            EscherAggregate agg = (EscherAggregate) ish.findFirstRecordBySid(EscherAggregate.sid);
            assertNotNull(agg);
            assertEquals(3, agg.getEscherRecords().get(0).getChildRecords().size());
            assertEquals(EscherContainerRecord.SOLVER_CONTAINER, agg.getEscherRecords().get(0).getChild(2).getRecordId());
            try (HSSFWorkbook wb2 = HSSFTestDataSamples.writeOutAndReadBack(wb1)) {
                sh = wb2.getSheetAt(0);
                sh.getDrawingPatriarch();
                ish = HSSFTestHelper.getSheetForTest(sh);
                agg = (EscherAggregate) ish.findFirstRecordBySid(EscherAggregate.sid);
                assertNotNull(agg);
                assertEquals(3, agg.getEscherRecords().get(0).getChildRecords().size());
                assertEquals(EscherContainerRecord.SOLVER_CONTAINER, agg.getEscherRecords().get(0).getChild(2).getRecordId());


                // collect drawing records into a byte buffer.
                agg = (EscherAggregate) ish.findFirstRecordBySid(EscherAggregate.sid);
                assertNotNull(agg);
                byte[] dgBytesAfterSave = agg.serialize();
                assertEquals(dgBytes.length, dgBytesAfterSave.length, "different size of drawing data before and after save");
                assertArrayEquals(dgBytes, dgBytesAfterSave, "drawing data before and after save is different");
            }
        }
    }

    @Test
    void testFileWithTextbox() throws IOException{
        try (HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("text.xls")) {
            HSSFSheet sh = wb.getSheetAt(0);
            InternalSheet ish = HSSFTestHelper.getSheetForTest(sh);
            List<RecordBase> records = ish.getRecords();
            // records to be aggregated
            List<RecordBase> dgRecords = records.subList(19, 23);
            byte[] dgBytes = toByteArray(dgRecords);
            sh.getDrawingPatriarch();

            // collect drawing records into a byte buffer.
            EscherAggregate agg = (EscherAggregate) ish.findFirstRecordBySid(EscherAggregate.sid);
            assertNotNull(agg);
            byte[] dgBytesAfterSave = agg.serialize();
            assertEquals(dgBytes.length, dgBytesAfterSave.length, "different size of drawing data before and after save");
            assertArrayEquals(dgBytes, dgBytesAfterSave, "drawing data before and after save is different");
        }
    }

    @Test
    void testFileWithCharts() throws IOException {
        try (HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("49581.xls")) {
            HSSFSheet sh = wb.getSheetAt(0);
            InternalSheet ish = HSSFTestHelper.getSheetForTest(sh);
            List<RecordBase> records = ish.getRecords();
            // records to be aggregated
            List<RecordBase> dgRecords = records.subList(19, 21);
            byte[] dgBytes = toByteArray(dgRecords);
            sh.getDrawingPatriarch();

            // collect drawing records into a byte buffer.
            EscherAggregate agg = (EscherAggregate) ish.findFirstRecordBySid(EscherAggregate.sid);
            assertNotNull(agg);
            byte[] dgBytesAfterSave = agg.serialize();
            assertEquals(dgBytes.length, dgBytesAfterSave.length, "different size of drawing data before and after save");
            for (int i = 0; i < dgBytes.length; i++) {
                if (dgBytes[i] != dgBytesAfterSave[i]) {
                    System.out.println("pos = " + i);
                }
            }
            assertArrayEquals(dgBytes, dgBytesAfterSave, "drawing data before and after save is different");
        }
    }

    /**
     * test reading drawing aggregate from a test file from Bugzilla 45129
     */
    @Test
    void test45129() throws IOException {
        try (HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("45129.xls")) {
            HSSFSheet sh = wb.getSheetAt(0);

            InternalWorkbook iworkbook = HSSFTestHelper.getWorkbookForTest(wb);
            InternalSheet isheet = HSSFTestHelper.getSheetForTest(sh);

            List<RecordBase> records = isheet.getRecords();

            // the sheet's drawing is not aggregated
            assertEquals(394, records.size(), "wrong size of sheet records stream");
            // the last record before the drawing block
            assertTrue(records.get(18) instanceof RowRecordsAggregate,
                "records.get(18) is expected to be RowRecordsAggregate but was " + records.get(18).getClass().getSimpleName());

            // records to be aggregated
            List<RecordBase> dgRecords = records.subList(19, 389);
            // collect drawing records into a byte buffer.
            byte[] dgBytes = toByteArray(dgRecords);

            for (RecordBase rb : dgRecords) {
                Record r = (org.apache.poi.hssf.record.Record) rb;
                short sid = r.getSid();
                // we expect that drawing block consists of either
                // DrawingRecord or ContinueRecord or ObjRecord or TextObjectRecord
                assertTrue(
                        sid == DrawingRecord.sid ||
                                sid == ContinueRecord.sid ||
                                sid == ObjRecord.sid ||
                                sid == TextObjectRecord.sid);
            }

            // the first record after the drawing block
            assertTrue(records.get(389) instanceof WindowTwoRecord, "records.get(389) is expected to be Window2");

            // aggregate drawing records.
            // The subrange [19, 388] is expected to be replaced with a EscherAggregate object
            DrawingManager2 drawingManager = iworkbook.findDrawingGroup();
            int loc = isheet.aggregateDrawingRecords(drawingManager, false);
            EscherAggregate agg = (EscherAggregate) records.get(loc);

            assertEquals(25, records.size(), "wrong size of the aggregated sheet records stream");
            assertTrue(records.get(18) instanceof RowRecordsAggregate,
                "records.get(18) is expected to be RowRecordsAggregate but was " + records.get(18).getClass().getSimpleName());
            assertTrue(records.get(19) instanceof EscherAggregate,
                "records.get(19) is expected to be EscherAggregate but was " + records.get(19).getClass().getSimpleName());
            assertTrue(records.get(20) instanceof WindowTwoRecord,
                "records.get(20) is expected to be Window2 but was " + records.get(20).getClass().getSimpleName());

            byte[] dgBytesAfterSave = agg.serialize();
            assertEquals(dgBytes.length, dgBytesAfterSave.length, "different size of drawing data before and after save");
            assertArrayEquals(dgBytes, dgBytesAfterSave, "drawing data before and after save is different");
        }
    }

    /**
     * Try to check file with such record sequence
     * ...
     * DrawingRecord
     * ContinueRecord
     * ObjRecord | TextObjRecord
     * ...
     */
    @Test
    void testSerializeDrawingBigger8k() throws IOException {
        try (HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("DrawingContinue.xls")) {
            InternalWorkbook iworkbook = HSSFTestHelper.getWorkbookForTest(wb);
            HSSFSheet sh = wb.getSheetAt(0);
            InternalSheet isheet = HSSFTestHelper.getSheetForTest(sh);


            List<RecordBase> records = isheet.getRecords();

            // the sheet's drawing is not aggregated
            assertEquals(32, records.size(), "wrong size of sheet records stream");
            // the last record before the drawing block
            assertTrue(records.get(18) instanceof RowRecordsAggregate,
                "records.get(18) is expected to be RowRecordsAggregate but was " + records.get(18).getClass().getSimpleName());

            // records to be aggregated
            List<RecordBase> dgRecords = records.subList(19, 26);
            for (RecordBase rb : dgRecords) {
                Record r = (org.apache.poi.hssf.record.Record) rb;
                short sid = r.getSid();
                // we expect that drawing block consists of either
                // DrawingRecord or ContinueRecord or ObjRecord or TextObjectRecord
                assertTrue(
                        sid == DrawingRecord.sid ||
                                sid == ContinueRecord.sid ||
                                sid == ObjRecord.sid ||
                                sid == NoteRecord.sid ||
                                sid == TextObjectRecord.sid);
            }
            // collect drawing records into a byte buffer.
            byte[] dgBytes = toByteArray(dgRecords);

            // the first record after the drawing block
            assertTrue(records.get(26) instanceof WindowTwoRecord, "records.get(26) is expected to be Window2");

            // aggregate drawing records.
            // The subrange [19, 38] is expected to be replaced with a EscherAggregate object
            DrawingManager2 drawingManager = iworkbook.findDrawingGroup();
            int loc = isheet.aggregateDrawingRecords(drawingManager, false);
            EscherAggregate agg = (EscherAggregate) records.get(loc);

            assertEquals(26, records.size(), "wrong size of the aggregated sheet records stream");
            assertTrue(records.get(18) instanceof RowRecordsAggregate,
                "records.get(18) is expected to be RowRecordsAggregate but was " + records.get(18).getClass().getSimpleName());
            assertTrue(records.get(19) instanceof EscherAggregate,
                "records.get(19) is expected to be EscherAggregate but was " + records.get(19).getClass().getSimpleName());
            assertTrue(records.get(20) instanceof WindowTwoRecord,
                "records.get(20) is expected to be Window2 but was " + records.get(20).getClass().getSimpleName());

            byte[] dgBytesAfterSave = agg.serialize();
            assertEquals(dgBytes.length, dgBytesAfterSave.length, "different size of drawing data before and after save");
            assertArrayEquals(dgBytes, dgBytesAfterSave, "drawing data before and after save is different");
        }
    }


    @Test
    void testSerializeDrawingBigger8k_noAggregation() throws IOException {
        try (HSSFWorkbook wb1 = HSSFTestDataSamples.openSampleWorkbook("DrawingContinue.xls")) {
            InternalSheet isheet = HSSFTestHelper.getSheetForTest(wb1.getSheetAt(0));
            List<RecordBase> records = isheet.getRecords();

            try (HSSFWorkbook wb2 = HSSFTestDataSamples.writeOutAndReadBack(wb1)) {
                InternalSheet isheet2 = HSSFTestHelper.getSheetForTest(wb2.getSheetAt(0));
                List<RecordBase> records2 = isheet2.getRecords();

                assertEquals(records.size(), records2.size());
                for (int i = 0; i < records.size(); i++) {
                    RecordBase r1 = records.get(i);
                    RecordBase r2 = records2.get(i);
                    assertSame(r1.getClass(), r2.getClass());
                    assertEquals(r1.getRecordSize(), r2.getRecordSize());
                    if (r1 instanceof Record) {
                        assertEquals(((org.apache.poi.hssf.record.Record) r1).getSid(), ((org.apache.poi.hssf.record.Record) r2).getSid());
                        assertArrayEquals(((org.apache.poi.hssf.record.Record) r1).serialize(), ((org.apache.poi.hssf.record.Record) r2).serialize());
                    }
                }
            }
        }
    }

    @Test
    void testSerializeDrawingWithComments() throws IOException {
        try (HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("DrawingAndComments.xls")) {
            HSSFSheet sh = wb.getSheetAt(0);
            InternalWorkbook iworkbook = HSSFTestHelper.getWorkbookForTest(wb);
            InternalSheet isheet = HSSFTestHelper.getSheetForTest(sh);

            List<RecordBase> records = isheet.getRecords();

            // the sheet's drawing is not aggregated
            assertEquals(46, records.size(), "wrong size of sheet records stream");
            // the last record before the drawing block
            assertTrue(records.get(18) instanceof RowRecordsAggregate,
                "records.get(18) is expected to be RowRecordsAggregate but was " + records.get(18).getClass().getSimpleName());

            // records to be aggregated
            List<RecordBase> dgRecords = records.subList(19, 39);
            for (RecordBase rb : dgRecords) {
                Record r = (org.apache.poi.hssf.record.Record) rb;
                short sid = r.getSid();
                // we expect that drawing block consists of either
                // DrawingRecord or ContinueRecord or ObjRecord or TextObjectRecord
                assertTrue(
                        sid == DrawingRecord.sid ||
                                sid == ContinueRecord.sid ||
                                sid == ObjRecord.sid ||
                                sid == NoteRecord.sid ||
                                sid == TextObjectRecord.sid);
            }
            // collect drawing records into a byte buffer.
            byte[] dgBytes = toByteArray(dgRecords);

            // the first record after the drawing block
            assertTrue(records.get(39) instanceof WindowTwoRecord, "records.get(39) is expected to be Window2");

            // aggregate drawing records.
            // The subrange [19, 38] is expected to be replaced with a EscherAggregate object
            DrawingManager2 drawingManager = iworkbook.findDrawingGroup();
            int loc = isheet.aggregateDrawingRecords(drawingManager, false);
            EscherAggregate agg = (EscherAggregate) records.get(loc);

            assertEquals(27, records.size(), "wrong size of the aggregated sheet records stream");
            assertTrue(records.get(18) instanceof RowRecordsAggregate,
                    "records.get(18) is expected to be RowRecordsAggregate but was " + records.get(18).getClass().getSimpleName());
            assertTrue(records.get(19) instanceof EscherAggregate,
                "records.get(19) is expected to be EscherAggregate but was " + records.get(19).getClass().getSimpleName());
            assertTrue(records.get(20) instanceof WindowTwoRecord,
                "records.get(20) is expected to be Window2 but was " + records.get(20).getClass().getSimpleName());

            byte[] dgBytesAfterSave = agg.serialize();
            assertEquals(dgBytes.length, dgBytesAfterSave.length, "different size of drawing data before and after save");
            assertArrayEquals(dgBytes, dgBytesAfterSave, "drawing data before and after save is different");
        }
    }


    @Test
    void testFileWithPictures() throws IOException {
        try (HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("ContinueRecordProblem.xls")) {
            HSSFSheet sh = wb.getSheetAt(0);

            InternalWorkbook iworkbook = HSSFTestHelper.getWorkbookForTest(wb);
            InternalSheet isheet = HSSFTestHelper.getSheetForTest(sh);

            List<RecordBase> records = isheet.getRecords();

            // the sheet's drawing is not aggregated
            assertEquals(315, records.size(), "wrong size of sheet records stream");
            // the last record before the drawing block
            assertTrue(records.get(21) instanceof RowRecordsAggregate,
                "records.get(21) is expected to be RowRecordsAggregate but was " + records.get(21).getClass().getSimpleName());

            // records to be aggregated
            List<RecordBase> dgRecords = records.subList(22, 300);
            for (RecordBase rb : dgRecords) {
                Record r = (org.apache.poi.hssf.record.Record) rb;
                short sid = r.getSid();
                // we expect that drawing block consists of either
                // DrawingRecord or ContinueRecord or ObjRecord or TextObjectRecord
                assertTrue(
                        sid == DrawingRecord.sid ||
                                sid == ContinueRecord.sid ||
                                sid == ObjRecord.sid ||
                                sid == TextObjectRecord.sid);
            }
            // collect drawing records into a byte buffer.
            byte[] dgBytes = toByteArray(dgRecords);

            // the first record after the drawing block
            assertTrue(records.get(300) instanceof WindowTwoRecord, "records.get(300) is expected to be Window2");

            // aggregate drawing records.
            // The subrange [19, 299] is expected to be replaced with a EscherAggregate object
            DrawingManager2 drawingManager = iworkbook.findDrawingGroup();
            int loc = isheet.aggregateDrawingRecords(drawingManager, false);
            EscherAggregate agg = (EscherAggregate) records.get(loc);

            assertEquals(38, records.size(), "wrong size of the aggregated sheet records stream");
            assertTrue(records.get(21) instanceof RowRecordsAggregate,
                "records.get(21) is expected to be RowRecordsAggregate but was " + records.get(21).getClass().getSimpleName());
            assertTrue(records.get(22) instanceof EscherAggregate,
                "records.get(22) is expected to be EscherAggregate but was " + records.get(22).getClass().getSimpleName());
            assertTrue(records.get(23) instanceof WindowTwoRecord,
                "records.get(23) is expected to be Window2 but was " + records.get(23).getClass().getSimpleName());

            byte[] dgBytesAfterSave = agg.serialize();
            assertEquals(dgBytes.length, dgBytesAfterSave.length, "different size of drawing data before and after save");
            assertArrayEquals(dgBytes, dgBytesAfterSave, "drawing data before and after save is different");
        }
    }

    @Test
    void testUnhandledContinue() throws IOException {
        String data =
            "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";

        byte[] dgBytes = decompress(data);
        List<org.apache.poi.hssf.record.Record> dgRecords = RecordFactory.createRecords(new ByteArrayInputStream(dgBytes));
        assertEquals(20, dgRecords.size());

        int[] expectedSids = {
            DrawingRecord.sid, ObjRecord.sid,
            DrawingRecord.sid, TextObjectRecord.sid,
            DrawingRecord.sid, ObjRecord.sid,
            DrawingRecord.sid, TextObjectRecord.sid,
            DrawingRecord.sid, ObjRecord.sid,
            DrawingRecord.sid, TextObjectRecord.sid,
            DrawingRecord.sid, ObjRecord.sid,
            DrawingRecord.sid, TextObjectRecord.sid,
            ContinueRecord.sid, ObjRecord.sid,
            ContinueRecord.sid, TextObjectRecord.sid
        };

        int[] actualSids = dgRecords.stream().mapToInt(Record::getSid).toArray();
        assertArrayEquals(expectedSids, actualSids, "unexpected record.sid");

        DrawingManager2 drawingManager = new DrawingManager2(new EscherDggRecord());

        // create a dummy sheet consisting of our test data
        InternalSheet sheet = InternalSheet.createSheet();
        List<RecordBase> records = sheet.getRecords();
        records.clear();
        records.addAll(dgRecords);
        records.add(EOFRecord.instance);


        sheet.aggregateDrawingRecords(drawingManager, false);
        assertEquals(2, records.size(), "drawing was not fully aggregated");
        assertTrue(records.get(0) instanceof EscherAggregate, "expected EscherAggregate");
        assertTrue(records.get(1) instanceof EOFRecord, "expected EOFRecord");
        EscherAggregate agg = (EscherAggregate) records.get(0);

        byte[] dgBytesAfterSave = agg.serialize();
        assertEquals(dgBytes.length, dgBytesAfterSave.length, "different size of drawing data before and after save");
        assertArrayEquals(dgBytes, dgBytesAfterSave, "drawing data before and after save is different");
    }

    @Test
    void testUnhandledContinue2() throws IOException {
        String data =
            "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";

        byte[] dgBytes = decompress(data);
        List<org.apache.poi.hssf.record.Record> dgRecords = RecordFactory.createRecords(new ByteArrayInputStream(dgBytes));
        assertEquals(14, dgRecords.size());

        int[] expectedSids = {
            DrawingRecord.sid, ObjRecord.sid,
            DrawingRecord.sid, ObjRecord.sid,
            DrawingRecord.sid, ObjRecord.sid,
            DrawingRecord.sid, ObjRecord.sid,
            ContinueRecord.sid, ObjRecord.sid,
            ContinueRecord.sid, ObjRecord.sid,
            ContinueRecord.sid, ObjRecord.sid
        };

        int[] actualSids = dgRecords.stream().mapToInt(Record::getSid).toArray();
        assertArrayEquals(expectedSids, actualSids, "unexpected record.sid");

        DrawingManager2 drawingManager = new DrawingManager2(new EscherDggRecord());

        // create a dummy sheet consisting of our test data
        InternalSheet sheet = InternalSheet.createSheet();
        List<RecordBase> records = sheet.getRecords();
        records.clear();
        records.addAll(dgRecords);
        records.add(EOFRecord.instance);

        sheet.aggregateDrawingRecords(drawingManager, false);
        assertEquals(2, records.size(), "drawing was not fully aggregated");
        assertTrue(records.get(0) instanceof EscherAggregate, "expected EscherAggregate");
        assertTrue(records.get(1) instanceof EOFRecord, "expected EOFRecord");

        EscherAggregate agg = (EscherAggregate) records.get(0);

        byte[] dgBytesAfterSave = agg.serialize();
        assertEquals(dgBytes.length, dgBytesAfterSave.length, "different size of drawing data before and after save");
        assertArrayEquals(dgBytes, dgBytesAfterSave, "drawing data brefpore and after save is different");
    }
}
