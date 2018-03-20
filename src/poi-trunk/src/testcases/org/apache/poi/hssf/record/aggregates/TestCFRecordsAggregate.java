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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.hssf.model.RecordStream;
import org.apache.poi.hssf.record.CFHeaderBase;
import org.apache.poi.hssf.record.CFHeaderRecord;
import org.apache.poi.hssf.record.CFRule12Record;
import org.apache.poi.hssf.record.CFRuleBase;
import org.apache.poi.hssf.record.CFRuleBase.ComparisonOperator;
import org.apache.poi.hssf.record.CFRuleRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.RecordFactory;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.LittleEndian;

/**
 * Tests the serialization and deserialization of the CFRecordsAggregate
 * class works correctly.  
 */
@SuppressWarnings("resource")
public final class TestCFRecordsAggregate extends TestCase {
    public void testCFRecordsAggregate() {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet();

        List<Record> recs = new ArrayList<>();
        CFHeaderBase header = new CFHeaderRecord();
        CFRuleBase rule1 = CFRuleRecord.create(sheet, "7");
        CFRuleBase rule2 = CFRuleRecord.create(sheet, ComparisonOperator.BETWEEN, "2", "5");
        CFRuleBase rule3 = CFRuleRecord.create(sheet, ComparisonOperator.GE, "100", null);
        header.setNumberOfConditionalFormats(3);
        CellRangeAddress[] cellRanges = {
                new CellRangeAddress(0,1,0,0),
                new CellRangeAddress(0,1,2,2),
        };
        header.setCellRanges(cellRanges);
        recs.add(header);
        recs.add(rule1);
        recs.add(rule2);
        recs.add(rule3);
        CFRecordsAggregate record = CFRecordsAggregate.createCFAggregate(new RecordStream(recs, 0));

        // Serialize
        byte [] serializedRecord = new byte[record.getRecordSize()];
        record.serialize(0, serializedRecord);
        InputStream in = new ByteArrayInputStream(serializedRecord);

        //Parse
        recs = RecordFactory.createRecords(in);

        // Verify
        assertNotNull(recs);
        assertEquals(4, recs.size());

        header = (CFHeaderRecord)recs.get(0);
        rule1 = (CFRuleRecord)recs.get(1);
        assertNotNull(rule1);
        rule2 = (CFRuleRecord)recs.get(2);
        assertNotNull(rule2);
        rule3 = (CFRuleRecord)recs.get(3);
        assertNotNull(rule3);
        cellRanges = header.getCellRanges();

        assertEquals(2, cellRanges.length);
        assertEquals(3, header.getNumberOfConditionalFormats());
        assertFalse(header.getNeedRecalculation());

        record = CFRecordsAggregate.createCFAggregate(new RecordStream(recs, 0));

        record = record.cloneCFAggregate();

        assertNotNull(record.getHeader());
        assertEquals(3,record.getNumberOfRules());

        header = record.getHeader();
        rule1 = record.getRule(0);
        assertNotNull(rule1);
        rule2 = record.getRule(1);
        assertNotNull(rule2);
        rule3 = record.getRule(2);
        assertNotNull(rule3);
        cellRanges = header.getCellRanges();

        assertEquals(2, cellRanges.length);
        assertEquals(3, header.getNumberOfConditionalFormats());
        assertFalse(header.getNeedRecalculation());
    }

    /**
     * Make sure that the CF Header record is properly updated with the number of rules
     */
    public void testNRules() {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet();
        CellRangeAddress[] cellRanges = {
                new CellRangeAddress(0,1,0,0),
                new CellRangeAddress(0,1,2,2),
        };
        CFRuleRecord[] rules = {
                CFRuleRecord.create(sheet, "7"),
                CFRuleRecord.create(sheet, ComparisonOperator.BETWEEN, "2", "5"),
        };
        CFRecordsAggregate agg = new CFRecordsAggregate(cellRanges, rules);
        byte[] serializedRecord = new byte[agg.getRecordSize()];
        agg.serialize(0, serializedRecord);

        int nRules = LittleEndian.getUShort(serializedRecord, 4);
        if (nRules == 0) {
            throw new AssertionFailedError("Identified bug 45682 b");
        }
        assertEquals(rules.length, nRules);
    }
    
    public void testCantMixTypes() {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet();
        CellRangeAddress[] cellRanges = {
                new CellRangeAddress(0,1,0,0),
                new CellRangeAddress(0,1,2,2),
        };
        CFRuleBase[] rules = {
                CFRuleRecord.create(sheet, "7"),
                CFRule12Record.create(sheet, ComparisonOperator.BETWEEN, "2", "5"),
        };
        try {
            new CFRecordsAggregate(cellRanges, rules);
            fail("Shouldn't be able to mix between types");
        } catch (IllegalArgumentException e) {
            // expected here
        }
        
        
        rules = new CFRuleBase[] { CFRuleRecord.create(sheet, "7") };
        CFRecordsAggregate agg = new CFRecordsAggregate(cellRanges, rules);
        assertTrue(agg.getHeader().getNeedRecalculation());
        
        try {
            agg.addRule(CFRule12Record.create(sheet, "7"));
            fail("Shouldn't be able to mix between types");
        } catch (IllegalArgumentException e) {
            // expected here
        }
    }
}
