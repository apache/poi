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

package org.apache.poi.hssf.usermodel;

import junit.framework.Assert;
import org.apache.poi.hssf.model.Sheet;
import org.apache.poi.hssf.model.Workbook;
import org.apache.poi.hssf.record.*;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.util.List;

/**
 * Designed to check wither the records written actually make sense.
 */
public class SanityChecker
        extends Assert
{
    private class CheckRecord
    {
        Class record;
        char occurance;  // 1 = one time, M = many times

        public CheckRecord(Class record, char occurance)
        {
            this.record = record;
            this.occurance = occurance;
        }

        public Class getRecord()
        {
            return record;
        }

        public char getOccurance()
        {
            return occurance;
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
        new CheckRecord(BoundSheetRecord.class, '1'),   // Is this right?
        new CheckRecord(CountryRecord.class, '1'),
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
        new CheckRecord(HeaderRecord.class, '1'),
        new CheckRecord(FooterRecord.class, '1'),
        new CheckRecord(HCenterRecord.class, '1'),
        new CheckRecord(VCenterRecord.class, '1'),
        new CheckRecord(PrintSetupRecord.class, '1'),
        new CheckRecord(DefaultColWidthRecord.class, '1'),
        new CheckRecord(DimensionsRecord.class, '1'),
        new CheckRecord(WindowTwoRecord.class, '1'),
        new CheckRecord(SelectionRecord.class, '1'),
        new CheckRecord(EOFRecord.class, '1')
    };

    public void checkWorkbookRecords(Workbook workbook)
    {
        List records = workbook.getRecords();
        assertTrue(records.get(0) instanceof BOFRecord);
        assertTrue(records.get(records.size() - 1) instanceof EOFRecord);

        checkRecordOrder(records, workbookRecords);
    }

    public void checkSheetRecords(Sheet sheet)
    {
        List records = sheet.getRecords();
        assertTrue(records.get(0) instanceof BOFRecord);
        assertTrue(records.get(records.size() - 1) instanceof EOFRecord);

        checkRecordOrder(records, sheetRecords);
    }

    public void checkHSSFWorkbook(HSSFWorkbook wb)
    {
        checkWorkbookRecords(wb.getWorkbook());
        for (int i = 0; i < wb.getNumberOfSheets(); i++)
            checkSheetRecords(wb.getSheetAt(i).getSheet());

    }

    private void checkRecordOrder(List records, CheckRecord[] check)
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
                }
                checkIndex++;
            }
            if (checkIndex >= check.length)
                return;
        }
        fail("Could not find required record: " + check[checkIndex]);
    }

}
