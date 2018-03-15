
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


package org.apache.poi.hssf.usermodel;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.apache.poi.hssf.model.InternalSheet;
import org.apache.poi.hssf.model.InternalWorkbook;
import org.apache.poi.hssf.record.BOFRecord;
import org.apache.poi.hssf.record.BackupRecord;
import org.apache.poi.hssf.record.BookBoolRecord;
import org.apache.poi.hssf.record.BoundSheetRecord;
import org.apache.poi.hssf.record.CalcModeRecord;
import org.apache.poi.hssf.record.CodepageRecord;
import org.apache.poi.hssf.record.CountryRecord;
import org.apache.poi.hssf.record.DSFRecord;
import org.apache.poi.hssf.record.DateWindow1904Record;
import org.apache.poi.hssf.record.DefaultColWidthRecord;
import org.apache.poi.hssf.record.DefaultRowHeightRecord;
import org.apache.poi.hssf.record.DeltaRecord;
import org.apache.poi.hssf.record.DimensionsRecord;
import org.apache.poi.hssf.record.EOFRecord;
import org.apache.poi.hssf.record.ExtSSTRecord;
import org.apache.poi.hssf.record.ExtendedFormatRecord;
import org.apache.poi.hssf.record.ExternSheetRecord;
import org.apache.poi.hssf.record.FnGroupCountRecord;
import org.apache.poi.hssf.record.FontRecord;
import org.apache.poi.hssf.record.FormatRecord;
import org.apache.poi.hssf.record.GridsetRecord;
import org.apache.poi.hssf.record.GutsRecord;
import org.apache.poi.hssf.record.HideObjRecord;
import org.apache.poi.hssf.record.InterfaceEndRecord;
import org.apache.poi.hssf.record.InterfaceHdrRecord;
import org.apache.poi.hssf.record.IterationRecord;
import org.apache.poi.hssf.record.MMSRecord;
import org.apache.poi.hssf.record.NameRecord;
import org.apache.poi.hssf.record.PasswordRev4Record;
import org.apache.poi.hssf.record.PrecisionRecord;
import org.apache.poi.hssf.record.PrintGridlinesRecord;
import org.apache.poi.hssf.record.PrintHeadersRecord;
import org.apache.poi.hssf.record.ProtectRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.RecordBase;
import org.apache.poi.hssf.record.RefModeRecord;
import org.apache.poi.hssf.record.RefreshAllRecord;
import org.apache.poi.hssf.record.SSTRecord;
import org.apache.poi.hssf.record.SaveRecalcRecord;
import org.apache.poi.hssf.record.SelectionRecord;
import org.apache.poi.hssf.record.StyleRecord;
import org.apache.poi.hssf.record.SupBookRecord;
import org.apache.poi.hssf.record.TabIdRecord;
import org.apache.poi.hssf.record.UseSelFSRecord;
import org.apache.poi.hssf.record.WSBoolRecord;
import org.apache.poi.hssf.record.WindowOneRecord;
import org.apache.poi.hssf.record.WindowProtectRecord;
import org.apache.poi.hssf.record.WindowTwoRecord;
import org.apache.poi.hssf.record.WriteAccessRecord;
import org.apache.poi.hssf.record.aggregates.PageSettingsBlock;

/**
 * Designed to check wither the records written actually make sense.
 */
public class SanityChecker {
    static class CheckRecord
    {
        Class<? extends RecordBase> record;
        char occurance;  // 1 = one time, M = 1..many times, * = 0..many, 0 = optional
        private final boolean together;

        public CheckRecord( Class<? extends RecordBase> record, char occurance )
        {
            this(record, occurance, true);
        }

        /**
         * @param record        The record type to check
         * @param occurance     The occurance 1 = occurs once, M = occurs many times
         * @param together
         */
        public CheckRecord(Class<? extends RecordBase> record, char occurance, boolean together)
        {
            this.record = record;
            this.occurance = occurance;
            this.together = together;
        }

        public Class<? extends RecordBase> getRecord()
        {
            return record;
        }

        public char getOccurance()
        {
            return occurance;
        }

        public boolean isRequired()
        {
            return occurance == '1' || occurance == 'M';
        }

        public boolean isOptional()
        {
            return occurance == '0' || occurance == '*';
        }

        public boolean isTogether()
        {
            return together;
        }

        public boolean isMany()
        {
            return occurance == '*' || occurance == 'M';
        }

        public int match( List<? extends RecordBase> records, int recordIdx )
        {
            int firstRecord = findFirstRecord(records, getRecord(), recordIdx);
            if (isRequired())
            {
                return matchRequired( firstRecord, records, recordIdx );
            }
            return matchOptional( firstRecord, records, recordIdx );
        }

        private int matchOptional( int firstRecord, List<? extends RecordBase> records, int recordIdx )
        {
            if (firstRecord == -1)
            {
                return recordIdx;
            }

            return matchOneOrMany( records, firstRecord );
        }

        private int matchRequired( int firstRecord, List<? extends RecordBase> records, int recordIdx )
        {
            if (firstRecord == -1)
            {
                fail("Manditory record missing or out of order: " + record);
            }

            return matchOneOrMany( records, firstRecord );
        }

        private int matchOneOrMany( List<? extends RecordBase> records, int recordIdx )
        {
            if (isZeroOrOne())
            {
                // check no other records
                if (findFirstRecord(records, getRecord(), recordIdx+1) != -1)
                    fail("More than one record matched for " + getRecord().getName());
            }
            else if (isZeroToMany())
            {
                if (together)
                {
                    int nextIdx = findFirstRecord(records, record, recordIdx+1);
                    while (nextIdx != -1)
                    {
                        if (nextIdx - 1 != recordIdx)
                            fail("Records are not together " + record.getName());
                        recordIdx = nextIdx;
                        nextIdx = findFirstRecord(records, record, recordIdx+1);
                    }
                }
            }
            return recordIdx+1;
        }

        private boolean isZeroToMany()
        {
            return occurance == '*' || occurance == 'M';
        }

        private boolean isZeroOrOne()
        {
            return occurance == '0' || occurance == '1';
        }
    }

    CheckRecord[] workbookRecords = new CheckRecord[] {
        new CheckRecord(BOFRecord.class, '1'),
        new CheckRecord(InterfaceHdrRecord.class, '1'),
        new CheckRecord(MMSRecord.class, '1'),
        new CheckRecord(InterfaceEndRecord.class, '1'),
        new CheckRecord(WriteAccessRecord.class, '1'),
        new CheckRecord(CodepageRecord.class, '1'),
        new CheckRecord(DSFRecord.class, '1'),
        new CheckRecord(TabIdRecord.class, '1'),
        new CheckRecord(FnGroupCountRecord.class, '1'),
        new CheckRecord(WindowProtectRecord.class, '1'),
        new CheckRecord(ProtectRecord.class, '1'),
        new CheckRecord(PasswordRev4Record.class, '1'),
        new CheckRecord(WindowOneRecord.class, '1'),
        new CheckRecord(BackupRecord.class, '1'),
        new CheckRecord(HideObjRecord.class, '1'),
        new CheckRecord(DateWindow1904Record.class, '1'),
        new CheckRecord(PrecisionRecord.class, '1'),
        new CheckRecord(RefreshAllRecord.class, '1'),
        new CheckRecord(BookBoolRecord.class, '1'),
        new CheckRecord(FontRecord.class, 'M'),
        new CheckRecord(FormatRecord.class, 'M'),
        new CheckRecord(ExtendedFormatRecord.class, 'M'),
        new CheckRecord(StyleRecord.class, 'M'),
        new CheckRecord(UseSelFSRecord.class, '1'),
        new CheckRecord(BoundSheetRecord.class, 'M'),
        new CheckRecord(CountryRecord.class, '1'),
        new CheckRecord(SupBookRecord.class, '0'),
        new CheckRecord(ExternSheetRecord.class, '0'),
        new CheckRecord(NameRecord.class, '*'),
        new CheckRecord(SSTRecord.class, '1'),
        new CheckRecord(ExtSSTRecord.class, '1'),
        new CheckRecord(EOFRecord.class, '1'),
    };

    CheckRecord[] sheetRecords = new CheckRecord[] {
        new CheckRecord(BOFRecord.class, '1'),
        new CheckRecord(CalcModeRecord.class, '1'),
        new CheckRecord(RefModeRecord.class, '1'),
        new CheckRecord(IterationRecord.class, '1'),
        new CheckRecord(DeltaRecord.class, '1'),
        new CheckRecord(SaveRecalcRecord.class, '1'),
        new CheckRecord(PrintHeadersRecord.class, '1'),
        new CheckRecord(PrintGridlinesRecord.class, '1'),
        new CheckRecord(GridsetRecord.class, '1'),
        new CheckRecord(GutsRecord.class, '1'),
        new CheckRecord(DefaultRowHeightRecord.class, '1'),
        new CheckRecord(WSBoolRecord.class, '1'),
        new CheckRecord(PageSettingsBlock.class, '1'),
        new CheckRecord(DefaultColWidthRecord.class, '1'),
        new CheckRecord(DimensionsRecord.class, '1'),
        new CheckRecord(WindowTwoRecord.class, '1'),
        new CheckRecord(SelectionRecord.class, '1'),
        new CheckRecord(EOFRecord.class, '1')
    };

    private void checkWorkbookRecords(InternalWorkbook workbook)
    {
        List<Record> records = workbook.getRecords();
        assertTrue(records.get(0) instanceof BOFRecord);
        assertTrue(records.get(records.size() - 1) instanceof EOFRecord);

        checkRecordOrder(records, workbookRecords);
//        checkRecordsTogether(records, workbookRecords);
    }

    private void checkSheetRecords(InternalSheet sheet) {
        List<RecordBase> records = sheet.getRecords();
        assertTrue(records.get(0) instanceof BOFRecord);
        assertTrue(records.get(records.size() - 1) instanceof EOFRecord);

        checkRecordOrder(records, sheetRecords);
//        checkRecordsTogether(records, sheetRecords);
    }

    public void checkHSSFWorkbook(HSSFWorkbook wb)
    {
        checkWorkbookRecords(wb.getWorkbook());
        for (int i = 0; i < wb.getNumberOfSheets(); i++)
            checkSheetRecords(wb.getSheetAt(i).getSheet());

    }

    /*
    private void checkRecordsTogether(List records, CheckRecord[] check)
    {
        for ( int checkIdx = 0; checkIdx < check.length; checkIdx++ )
        {
            int recordIdx = findFirstRecord(records, check[checkIdx].getRecord());
            boolean notFoundAndRecordRequired = (recordIdx == -1 && check[checkIdx].isRequired());
            if (notFoundAndRecordRequired)
            {
                fail("Expected to find record of class " + check.getClass() + " but did not");
            }
            else if (recordIdx >= 0)
            {
                if (check[checkIdx].isMany())
                {
                    // Skip records that are together
                    while (recordIdx < records.size() && check[checkIdx].getRecord().isInstance(records.get(recordIdx)))
                        recordIdx++;
                }

                // Make sure record does not occur in remaining records (after the next)
                recordIdx++;
                for (int recordIdx2 = recordIdx; recordIdx2 < records.size(); recordIdx2++)
                {
                    if (check[checkIdx].getRecord().isInstance(records.get(recordIdx2)))
                        fail("Record occurs scattered throughout record chain:\n" + records.get(recordIdx2));
                }
            }
        }
    } */

    /* package */ static int findFirstRecord( List<? extends RecordBase> records, Class<? extends RecordBase> record, int startIndex )
    {
        for (int i = startIndex; i < records.size(); i++)
        {
            if (record.getName().equals(records.get(i).getClass().getName()))
                return i;
        }
        return -1;
    }

    void checkRecordOrder(List<? extends RecordBase> records, CheckRecord[] check)
    {
        int recordIdx = 0;
        for (CheckRecord element : check) {
            recordIdx = element.match(records, recordIdx);
        }
    }

    /*
    void checkRecordOrder(List records, CheckRecord[] check)
    {
        int checkIndex = 0;
        for (int recordIndex = 0; recordIndex < records.size(); recordIndex++)
        {
            Record record = (Record) records.get(recordIndex);
            if (check[checkIndex].getRecord().isInstance(record))
            {
                if (check[checkIndex].getOccurance() == 'M')
                {
                    // skip over duplicate records if multiples are allowed
                    while (recordIndex+1 < records.size() && check[checkIndex].getRecord().isInstance(records.get(recordIndex+1)))
                        recordIndex++;
//                    lastGoodMatch = recordIndex;
                }
                else if (check[checkIndex].getOccurance() == '1')
                {
                    // Check next record to make sure there's not more than one
                    if (recordIndex != records.size() - 1)
                    {
                        if (check[checkIndex].getRecord().isInstance(records.get(recordIndex+1)))
                        {
                            fail("More than one occurance of record found:\n" + records.get(recordIndex).toString());
                        }
                    }
//                    lastGoodMatch = recordIndex;
                }
//                else if (check[checkIndex].getOccurance() == '0')
//                {
//
//                }
                checkIndex++;
            }
            if (checkIndex >= check.length)
                return;
        }
        fail("Could not find required record: " + check[checkIndex]);
    } */

}
