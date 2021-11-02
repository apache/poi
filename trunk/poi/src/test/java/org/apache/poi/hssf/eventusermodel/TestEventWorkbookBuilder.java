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

package org.apache.poi.hssf.eventusermodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.eventusermodel.EventWorkbookBuilder.SheetRecordCollectingListener;
import org.apache.poi.hssf.model.HSSFFormulaParser;
import org.apache.poi.hssf.model.InternalWorkbook;
import org.apache.poi.hssf.record.FormulaRecord;
import org.apache.poi.hssf.usermodel.HSSFEvaluationWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.formula.ptg.Ref3DPtg;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link EventWorkbookBuilder}
 */
final class TestEventWorkbookBuilder {
    private final List<FormulaRecord> fRecs = new ArrayList<>();
    private SheetRecordCollectingListener listener;

    @BeforeEach
    void setUp() throws IOException {
        HSSFRequest req = new HSSFRequest();
        fRecs.clear();
        listener = new SheetRecordCollectingListener(this::addFormulaRecord);
        req.addListenerForAllRecords(listener);

        HSSFEventFactory factory = new HSSFEventFactory();
        try (InputStream is = HSSFTestDataSamples.openSampleFileStream("3dFormulas.xls");
            POIFSFileSystem fs = new POIFSFileSystem(is)) {
            factory.processWorkbookEvents(req, fs);
        }
    }

    private void addFormulaRecord(org.apache.poi.hssf.record.Record r) {
        if (r instanceof FormulaRecord) {
            fRecs.add((FormulaRecord)r);
        }
    }

    @Test
    void testBasics() {
        assertNotNull(listener.getSSTRecord());
        assertNotNull(listener.getBoundSheetRecords());
        assertNotNull(listener.getExternSheetRecords());
    }

    @Test
    void testGetStubWorkbooks() {
        assertNotNull(listener.getStubWorkbook());
        assertNotNull(listener.getStubHSSFWorkbook());
    }

    @Test
    void testContents() {
        assertEquals(2, listener.getSSTRecord().getNumStrings());
        assertEquals(3, listener.getBoundSheetRecords().length);
        assertEquals(1, listener.getExternSheetRecords().length);

        assertEquals(3, listener.getStubWorkbook().getNumSheets());

        InternalWorkbook ref = listener.getStubWorkbook();
        assertEquals("Sh3", ref.findSheetFirstNameFromExternSheet(0));
        assertEquals("Sheet1", ref.findSheetFirstNameFromExternSheet(1));
        assertEquals("S2", ref.findSheetFirstNameFromExternSheet(2));
    }

    @Test
    void testFormulas() {

        // Check our formula records
        assertEquals(6, fRecs.size());

        InternalWorkbook stubWB = listener.getStubWorkbook();
        assertNotNull(stubWB);
        HSSFWorkbook stubHSSF = listener.getStubHSSFWorkbook();
        assertNotNull(stubHSSF);

        // Check these stubs have the right stuff on them
        assertEquals("Sheet1", stubWB.getSheetName(0));
        assertEquals("Sheet1", stubHSSF.getSheetName(0));
        assertEquals("S2",     stubWB.getSheetName(1));
        assertEquals("S2",     stubHSSF.getSheetName(1));
        assertEquals("Sh3",    stubWB.getSheetName(2));
        assertEquals("Sh3",    stubHSSF.getSheetName(2));

        // Check we can get the formula without breaking
        for (FormulaRecord fRec : fRecs) {
            HSSFFormulaParser.toFormulaString(stubHSSF, fRec.getParsedExpression());
        }

        // Peer into just one formula, and check that
        //  all the ptgs give back the right things
        Ptg[] ptgs = fRecs.get(0).getParsedExpression();
        assertEquals(1, ptgs.length);
        assertTrue(ptgs[0] instanceof Ref3DPtg);

        Ref3DPtg ptg = (Ref3DPtg)ptgs[0];
        HSSFEvaluationWorkbook book = HSSFEvaluationWorkbook.create(stubHSSF);
        assertEquals("Sheet1!A1", ptg.toFormulaString(book));


        // Now check we get the right formula back for
        //  a few sample ones
        FormulaRecord fr;

        // Sheet 1 A2 is on same sheet
        fr = fRecs.get(0);
        assertEquals(1, fr.getRow());
        assertEquals(0, fr.getColumn());
        assertEquals("Sheet1!A1", HSSFFormulaParser.toFormulaString(stubHSSF, fr.getParsedExpression()));

        // Sheet 1 A5 is to another sheet
        fr = fRecs.get(3);
        assertEquals(4, fr.getRow());
        assertEquals(0, fr.getColumn());
        assertEquals("'S2'!A1", HSSFFormulaParser.toFormulaString(stubHSSF, fr.getParsedExpression()));

        // Sheet 1 A7 is to another sheet, range
        fr = fRecs.get(5);
        assertEquals(6, fr.getRow());
        assertEquals(0, fr.getColumn());
        assertEquals("SUM(Sh3!A1:A4)", HSSFFormulaParser.toFormulaString(stubHSSF, fr.getParsedExpression()));


        // Now, load via Usermodel and re-check
        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("3dFormulas.xls");
        assertEquals("Sheet1!A1", wb.getSheetAt(0).getRow(1).getCell(0).getCellFormula());
        assertEquals("SUM(Sh3!A1:A4)", wb.getSheetAt(0).getRow(6).getCell(0).getCellFormula());
    }
}
