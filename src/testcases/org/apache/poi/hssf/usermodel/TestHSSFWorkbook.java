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

import static org.apache.poi.POITestCase.assertContains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.poi.POIDataSamples;
import org.apache.poi.ddf.EscherBSERecord;
import org.apache.poi.hpsf.ClassID;
import org.apache.poi.hssf.HSSFITestDataProvider;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.OldExcelFormatException;
import org.apache.poi.hssf.model.HSSFFormulaParser;
import org.apache.poi.hssf.model.InternalSheet;
import org.apache.poi.hssf.model.InternalWorkbook;
import org.apache.poi.hssf.record.CFRuleRecord;
import org.apache.poi.hssf.record.NameRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.RecordBase;
import org.apache.poi.hssf.record.RecordFormatException;
import org.apache.poi.hssf.record.WindowOneRecord;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.ptg.Area3DPtg;
import org.apache.poi.ss.usermodel.BaseTestWorkbook;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.TempFile;
import org.junit.Test;

import junit.framework.AssertionFailedError;

/**
 * Tests for {@link HSSFWorkbook}
 */
public final class TestHSSFWorkbook extends BaseTestWorkbook {
    private static final HSSFITestDataProvider _testDataProvider = HSSFITestDataProvider.instance;
    
    public TestHSSFWorkbook() {
        super(_testDataProvider);
    }

    /**
     * gives test code access to the {@link InternalWorkbook} within {@link HSSFWorkbook}
     */
    public static InternalWorkbook getInternalWorkbook(HSSFWorkbook wb) {
        return wb.getWorkbook();
    }

    /**
     * Tests for {@link HSSFWorkbook#isHidden()} etc
     * @throws IOException 
     */
    @Test
    public void hidden() throws IOException {
        HSSFWorkbook wb = new HSSFWorkbook();

        WindowOneRecord w1 = wb.getWorkbook().getWindowOne();

        assertEquals(false, wb.isHidden());
        assertEquals(false, w1.getHidden());

        wb.setHidden(true);
        assertEquals(true, wb.isHidden());
        assertEquals(true, w1.getHidden());

        HSSFWorkbook wbBack = HSSFTestDataSamples.writeOutAndReadBack(wb);
        w1 = wbBack.getWorkbook().getWindowOne();

        wbBack.setHidden(true);
        assertEquals(true, wbBack.isHidden());
        assertEquals(true, w1.getHidden());

        wbBack.setHidden(false);
        assertEquals(false, wbBack.isHidden());
        assertEquals(false, w1.getHidden());
        
        wbBack.close();
        wb.close();
    }

    @Test
    public void sheetClone() throws IOException {
        // First up, try a simple file
        HSSFWorkbook b = new HSSFWorkbook();
        assertEquals(0, b.getNumberOfSheets());
        b.createSheet("Sheet One");
        b.createSheet("Sheet Two");

        assertEquals(2, b.getNumberOfSheets());
        b.cloneSheet(0);
        assertEquals(3, b.getNumberOfSheets());

        // Now try a problem one with drawing records in it
        HSSFWorkbook bBack = HSSFTestDataSamples.openSampleWorkbook("SheetWithDrawing.xls");
        assertEquals(1, bBack.getNumberOfSheets());
        bBack.cloneSheet(0);
        assertEquals(2, bBack.getNumberOfSheets());
        
        bBack.close();
        b.close();
    }

    @Test
    public void readWriteWithCharts() throws IOException {
        HSSFSheet s;

        // Single chart, two sheets
        HSSFWorkbook b1 = HSSFTestDataSamples.openSampleWorkbook("44010-SingleChart.xls");
        assertEquals(2, b1.getNumberOfSheets());
        assertEquals("Graph2", b1.getSheetName(1));
        s = b1.getSheetAt(1);
        assertEquals(0, s.getFirstRowNum());
        assertEquals(8, s.getLastRowNum());

        // Has chart on 1st sheet??
        // FIXME
        assertNotNull(b1.getSheetAt(0).getDrawingPatriarch());
        assertNull(b1.getSheetAt(1).getDrawingPatriarch());
        assertFalse(b1.getSheetAt(0).getDrawingPatriarch().containsChart());
        b1.close();

        // We've now called getDrawingPatriarch() so
        //  everything will be all screwy
        // So, start again
        HSSFWorkbook b2 = HSSFTestDataSamples.openSampleWorkbook("44010-SingleChart.xls");

        HSSFWorkbook b3 = HSSFTestDataSamples.writeOutAndReadBack(b2);
        b2.close();

        assertEquals(2, b3.getNumberOfSheets());
        s = b3.getSheetAt(1);
        assertEquals(0, s.getFirstRowNum());
        assertEquals(8, s.getLastRowNum());
        b3.close();

        // Two charts, three sheets
        HSSFWorkbook b4 = HSSFTestDataSamples.openSampleWorkbook("44010-TwoCharts.xls");
        assertEquals(3, b4.getNumberOfSheets());

        s = b4.getSheetAt(1);
        assertEquals(0, s.getFirstRowNum());
        assertEquals(8, s.getLastRowNum());
        s = b4.getSheetAt(2);
        assertEquals(0, s.getFirstRowNum());
        assertEquals(8, s.getLastRowNum());

        // Has chart on 1st sheet??
        // FIXME
        assertNotNull(b4.getSheetAt(0).getDrawingPatriarch());
        assertNull(b4.getSheetAt(1).getDrawingPatriarch());
        assertNull(b4.getSheetAt(2).getDrawingPatriarch());
        assertFalse(b4.getSheetAt(0).getDrawingPatriarch().containsChart());
        b4.close();

        // We've now called getDrawingPatriarch() so
        //  everything will be all screwy
        // So, start again
        HSSFWorkbook b5 = HSSFTestDataSamples.openSampleWorkbook("44010-TwoCharts.xls");

        HSSFWorkbook b6 = HSSFTestDataSamples.writeOutAndReadBack(b5);
        b5.close();
        assertEquals(3, b6.getNumberOfSheets());

        s = b6.getSheetAt(1);
        assertEquals(0, s.getFirstRowNum());
        assertEquals(8, s.getLastRowNum());
        s = b6.getSheetAt(2);
        assertEquals(0, s.getFirstRowNum());
        assertEquals(8, s.getLastRowNum());
        b6.close();
    }

    @SuppressWarnings("deprecation")
    @Test
    public void selectedSheet_bug44523() throws IOException {
        HSSFWorkbook wb=new HSSFWorkbook();
        HSSFSheet sheet1 = wb.createSheet("Sheet1");
        HSSFSheet sheet2 = wb.createSheet("Sheet2");
        HSSFSheet sheet3 = wb.createSheet("Sheet3");
        HSSFSheet sheet4 = wb.createSheet("Sheet4");

        confirmActiveSelected(sheet1, true);
        confirmActiveSelected(sheet2, false);
        confirmActiveSelected(sheet3, false);
        confirmActiveSelected(sheet4, false);

        wb.setSelectedTab(1);

        // see Javadoc, in this case selected means "active"
        assertEquals(wb.getActiveSheetIndex(), wb.getSelectedTab());

        // Demonstrate bug 44525:
        // Well... not quite, since isActive + isSelected were also added in the same bug fix
        assertFalse("Identified bug 44523 a", sheet1.isSelected());
        wb.setActiveSheet(1);
        assertFalse("Identified bug 44523 b", sheet1.isActive());

        confirmActiveSelected(sheet1, false);
        confirmActiveSelected(sheet2, true);
        confirmActiveSelected(sheet3, false);
        confirmActiveSelected(sheet4, false);
        
        wb.close();
    }

    @SuppressWarnings("unused")
    @Test
    public void selectMultiple() throws IOException {
        HSSFWorkbook wb=new HSSFWorkbook();
        HSSFSheet sheet1 = wb.createSheet("Sheet1");
        HSSFSheet sheet2 = wb.createSheet("Sheet2");
        HSSFSheet sheet3 = wb.createSheet("Sheet3");
        HSSFSheet sheet4 = wb.createSheet("Sheet4");
        HSSFSheet sheet5 = wb.createSheet("Sheet5");
        HSSFSheet sheet6 = wb.createSheet("Sheet6");

        wb.setSelectedTabs(new int[] { 0, 2, 3});

        assertEquals(true, sheet1.isSelected());
        assertEquals(false, sheet2.isSelected());
        assertEquals(true, sheet3.isSelected());
        assertEquals(true, sheet4.isSelected());
        assertEquals(false, sheet5.isSelected());
        assertEquals(false, sheet6.isSelected());

        wb.setSelectedTabs(new int[] { 1, 3, 5});

        assertEquals(false, sheet1.isSelected());
        assertEquals(true, sheet2.isSelected());
        assertEquals(false, sheet3.isSelected());
        assertEquals(true, sheet4.isSelected());
        assertEquals(false, sheet5.isSelected());
        assertEquals(true, sheet6.isSelected());

        assertEquals(true, sheet1.isActive());
        assertEquals(false, sheet2.isActive());


        assertEquals(true, sheet1.isActive());
        assertEquals(false, sheet3.isActive());
        wb.setActiveSheet(2);
        assertEquals(false, sheet1.isActive());
        assertEquals(true, sheet3.isActive());

        if (false) { // helpful if viewing this workbook in excel:
            sheet1.createRow(0).createCell(0).setCellValue(new HSSFRichTextString("Sheet1"));
            sheet2.createRow(0).createCell(0).setCellValue(new HSSFRichTextString("Sheet2"));
            sheet3.createRow(0).createCell(0).setCellValue(new HSSFRichTextString("Sheet3"));
            sheet4.createRow(0).createCell(0).setCellValue(new HSSFRichTextString("Sheet4"));

            try {
                File fOut = TempFile.createTempFile("sheetMultiSelect", ".xls");
                FileOutputStream os = new FileOutputStream(fOut);
                wb.write(os);
                os.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        
        wb.close();
    }


    @Test
    public void activeSheetAfterDelete_bug40414() throws IOException {
        HSSFWorkbook wb=new HSSFWorkbook();
        HSSFSheet sheet0 = wb.createSheet("Sheet0");
        HSSFSheet sheet1 = wb.createSheet("Sheet1");
        HSSFSheet sheet2 = wb.createSheet("Sheet2");
        HSSFSheet sheet3 = wb.createSheet("Sheet3");
        HSSFSheet sheet4 = wb.createSheet("Sheet4");

        // confirm default activation/selection
        confirmActiveSelected(sheet0, true);
        confirmActiveSelected(sheet1, false);
        confirmActiveSelected(sheet2, false);
        confirmActiveSelected(sheet3, false);
        confirmActiveSelected(sheet4, false);

        wb.setActiveSheet(3);
        wb.setSelectedTab(3);

        confirmActiveSelected(sheet0, false);
        confirmActiveSelected(sheet1, false);
        confirmActiveSelected(sheet2, false);
        confirmActiveSelected(sheet3, true);
        confirmActiveSelected(sheet4, false);

        wb.removeSheetAt(3);
        // after removing the only active/selected sheet, another should be active/selected in its place
        assertTrue("identified bug 40414 a", sheet4.isSelected());
        assertTrue("identified bug 40414 b", sheet4.isActive());

        confirmActiveSelected(sheet0, false);
        confirmActiveSelected(sheet1, false);
        confirmActiveSelected(sheet2, false);
        confirmActiveSelected(sheet4, true);

        sheet3 = sheet4; // re-align local vars in this test case

        // Some more cases of removing sheets

        // Starting with a multiple selection, and different active sheet
        wb.setSelectedTabs(new int[] { 1, 3, });
        wb.setActiveSheet(2);
        confirmActiveSelected(sheet0, false, false);
        confirmActiveSelected(sheet1, false, true);
        confirmActiveSelected(sheet2, true,  false);
        confirmActiveSelected(sheet3, false, true);

        // removing a sheet that is not active, and not the only selected sheet
        wb.removeSheetAt(3);
        confirmActiveSelected(sheet0, false, false);
        confirmActiveSelected(sheet1, false, true);
        confirmActiveSelected(sheet2, true,  false);

        // removing the only selected sheet
        wb.removeSheetAt(1);
        confirmActiveSelected(sheet0, false, false);
        confirmActiveSelected(sheet2, true,  true);

        // The last remaining sheet should always be active+selected
        wb.removeSheetAt(1);
        confirmActiveSelected(sheet0, true,  true);
        
        wb.close();
    }

    private static void confirmActiveSelected(HSSFSheet sheet, boolean expected) {
        confirmActiveSelected(sheet, expected, expected);
    }


    private static void confirmActiveSelected(HSSFSheet sheet,
            boolean expectedActive, boolean expectedSelected) {
        assertEquals("active", expectedActive, sheet.isActive());
        assertEquals("selected", expectedSelected, sheet.isSelected());
    }

    /**
     * If Sheet.getSize() returns a different result to Sheet.serialize(), this will cause the BOF
     * records to be written with invalid offset indexes.  Excel does not like this, and such
     * errors are particularly hard to track down.  This test ensures that HSSFWorkbook throws
     * a specific exception as soon as the situation is detected. See bugzilla 45066
     * @throws IOException 
     */
    @Test
    public void sheetSerializeSizeMismatch_bug45066() throws IOException {
        HSSFWorkbook wb = new HSSFWorkbook();
        InternalSheet sheet = wb.createSheet("Sheet1").getSheet();
        List<RecordBase> sheetRecords = sheet.getRecords();
        // one way (of many) to cause the discrepancy is with a badly behaved record:
        sheetRecords.add(new BadlyBehavedRecord());
        // There is also much logic inside Sheet that (if buggy) might also cause the discrepancy
        try {
            wb.getBytes();
            fail("Identified bug 45066 a");
        } catch (IllegalStateException e) {
            // Expected badly behaved sheet record to cause exception
            assertTrue(e.getMessage().startsWith("Actual serialized sheet size"));
        }
        
        wb.close();
    }

    /**
     * Checks that us and HSSFName play nicely with named ranges
     *  that point to deleted sheets
     */
    @Test
    public void namesToDeleteSheets() throws IOException {
        HSSFWorkbook b = HSSFTestDataSamples.openSampleWorkbook("30978-deleted.xls");
        assertEquals(3, b.getNumberOfNames());

        // Sheet 2 is deleted
        assertEquals("Sheet1", b.getSheetName(0));
        assertEquals("Sheet3", b.getSheetName(1));

        Area3DPtg ptg;
        NameRecord nr;
        HSSFName n;

        /* ======= Name pointing to deleted sheet ====== */

        // First at low level
        nr = b.getWorkbook().getNameRecord(0);
        assertEquals("On2", nr.getNameText());
        assertEquals(0, nr.getSheetNumber());
        assertEquals(1, nr.getExternSheetNumber());
        assertEquals(1, nr.getNameDefinition().length);

        ptg = (Area3DPtg)nr.getNameDefinition()[0];
        assertEquals(1, ptg.getExternSheetIndex());
        assertEquals(0, ptg.getFirstColumn());
        assertEquals(0, ptg.getFirstRow());
        assertEquals(0, ptg.getLastColumn());
        assertEquals(2, ptg.getLastRow());

        // Now at high level
        n = b.getNameAt(0);
        assertEquals("On2", n.getNameName());
        assertEquals("", n.getSheetName());
        assertEquals("#REF!$A$1:$A$3", n.getRefersToFormula());


        /* ======= Name pointing to 1st sheet ====== */

        // First at low level
        nr = b.getWorkbook().getNameRecord(1);
        assertEquals("OnOne", nr.getNameText());
        assertEquals(0, nr.getSheetNumber());
        assertEquals(0, nr.getExternSheetNumber());
        assertEquals(1, nr.getNameDefinition().length);

        ptg = (Area3DPtg)nr.getNameDefinition()[0];
        assertEquals(0, ptg.getExternSheetIndex());
        assertEquals(0, ptg.getFirstColumn());
        assertEquals(2, ptg.getFirstRow());
        assertEquals(0, ptg.getLastColumn());
        assertEquals(3, ptg.getLastRow());

        // Now at high level
        n = b.getNameAt(1);
        assertEquals("OnOne", n.getNameName());
        assertEquals("Sheet1", n.getSheetName());
        assertEquals("Sheet1!$A$3:$A$4", n.getRefersToFormula());


        /* ======= Name pointing to 3rd sheet ====== */

        // First at low level
        nr = b.getWorkbook().getNameRecord(2);
        assertEquals("OnSheet3", nr.getNameText());
        assertEquals(0, nr.getSheetNumber());
        assertEquals(2, nr.getExternSheetNumber());
        assertEquals(1, nr.getNameDefinition().length);

        ptg = (Area3DPtg)nr.getNameDefinition()[0];
        assertEquals(2, ptg.getExternSheetIndex());
        assertEquals(0, ptg.getFirstColumn());
        assertEquals(0, ptg.getFirstRow());
        assertEquals(0, ptg.getLastColumn());
        assertEquals(1, ptg.getLastRow());

        // Now at high level
        n = b.getNameAt(2);
        assertEquals("OnSheet3", n.getNameName());
        assertEquals("Sheet3", n.getSheetName());
        assertEquals("Sheet3!$A$1:$A$2", n.getRefersToFormula());
        
        b.close();
    }

    /**
     * result returned by getRecordSize() differs from result returned by serialize()
     */
    private static final class BadlyBehavedRecord extends Record {
        public BadlyBehavedRecord() {
            //
        }
        @Override
		public short getSid() {
            return 0x777;
        }
        @Override
		public int serialize(int offset, byte[] data) {
            return 4;
        }
        @Override
		public int getRecordSize() {
            return 8;
        }
    }

    /**
     * The sample file provided with bug 45582 seems to have one extra byte after the EOFRecord
     */
    @Test
    public void extraDataAfterEOFRecord() {
        try {
            HSSFTestDataSamples.openSampleWorkbook("ex45582-22397.xls");
        } catch (RecordFormatException e) {
            if (e.getCause() instanceof LittleEndian.BufferUnderrunException) {
                throw new AssertionFailedError("Identified bug 45582");
            }
        }
    }

    /**
     * Test to make sure that NameRecord.getSheetNumber() is interpreted as a
     * 1-based sheet tab index (not a 1-based extern sheet index)
     */
    @Test
    public void findBuiltInNameRecord() throws IOException {
        // testRRaC has multiple (3) built-in name records
        // The second print titles name record has getSheetNumber()==4
        HSSFWorkbook wb1 = HSSFTestDataSamples.openSampleWorkbook("testRRaC.xls");
        NameRecord nr;
        assertEquals(3, wb1.getWorkbook().getNumNames());
        nr = wb1.getWorkbook().getNameRecord(2);
        // TODO - render full row and full column refs properly
        assertEquals("Sheet2!$A$1:$IV$1", HSSFFormulaParser.toFormulaString(wb1, nr.getNameDefinition())); // 1:1

        try {
          wb1.getSheetAt(3).setRepeatingRows(CellRangeAddress.valueOf("9:12"));
          wb1.getSheetAt(3).setRepeatingColumns(CellRangeAddress.valueOf("E:F"));
        } catch (RuntimeException e) {
            if (e.getMessage().equals("Builtin (7) already exists for sheet (4)")) {
                // there was a problem in the code which locates the existing print titles name record
                fail("Identified bug 45720b");
            }
            wb1.close();
            throw e;
        }
        HSSFWorkbook wb2 = HSSFTestDataSamples.writeOutAndReadBack(wb1);
        wb1.close();
        assertEquals(3, wb2.getWorkbook().getNumNames());
        nr = wb2.getWorkbook().getNameRecord(2);
        assertEquals("Sheet2!E:F,Sheet2!$A$9:$IV$12", HSSFFormulaParser.toFormulaString(wb2, nr.getNameDefinition())); // E:F,9:12
        wb2.close();
    }

    /**
     * Test that the storage clsid property is preserved
     */
    @Test
    public void bug47920() throws IOException {
        POIFSFileSystem fs1 = new POIFSFileSystem(POIDataSamples.getSpreadSheetInstance().openResourceAsStream("47920.xls"));
        HSSFWorkbook wb = new HSSFWorkbook(fs1);
        ClassID clsid1 = fs1.getRoot().getStorageClsid();

        ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
        wb.write(out);
        byte[] bytes = out.toByteArray();
        POIFSFileSystem fs2 = new POIFSFileSystem(new ByteArrayInputStream(bytes));
        ClassID clsid2 = fs2.getRoot().getStorageClsid();

        assertTrue(clsid1.equals(clsid2));
        
        fs2.close();
        wb.close();
        fs1.close();
    }
    
    /**
     * If we try to open an old (pre-97) workbook, we get a helpful
     *  Exception give to explain what we've done wrong
     */
    @Test
    public void helpfulExceptionOnOldFiles() throws Exception {
        InputStream excel4 = POIDataSamples.getSpreadSheetInstance().openResourceAsStream("testEXCEL_4.xls");
        try {
            new HSSFWorkbook(excel4).close();
            fail("Shouldn't be able to load an Excel 4 file");
        } catch (OldExcelFormatException e) {
            assertContains(e.getMessage(), "BIFF4");
        }
        excel4.close();
        
        InputStream excel5 = POIDataSamples.getSpreadSheetInstance().openResourceAsStream("testEXCEL_5.xls");
        try {
            new HSSFWorkbook(excel5).close();
            fail("Shouldn't be able to load an Excel 5 file");
        } catch (OldExcelFormatException e) {
            assertContains(e.getMessage(), "BIFF5");
        }
        excel5.close();
        
        InputStream excel95 = POIDataSamples.getSpreadSheetInstance().openResourceAsStream("testEXCEL_95.xls");
        try {
            new HSSFWorkbook(excel95).close();
            fail("Shouldn't be able to load an Excel 95 file");
        } catch (OldExcelFormatException e) {
            assertContains(e.getMessage(), "BIFF5");
        }
        excel95.close();
    }

    /**
     * Tests that we can work with both {@link POIFSFileSystem}
     *  and {@link NPOIFSFileSystem}
     */
    @Test
    public void differentPOIFS() throws Exception {
       // Open the two filesystems
       DirectoryNode[] files = new DirectoryNode[2];
       POIFSFileSystem poifsFileSystem = new POIFSFileSystem(HSSFTestDataSamples.openSampleFileStream("Simple.xls"));
       try {
           files[0] = poifsFileSystem.getRoot();
           NPOIFSFileSystem npoifsFileSystem = new NPOIFSFileSystem(HSSFTestDataSamples.getSampleFile("Simple.xls"));
           try {
               files[1] = npoifsFileSystem.getRoot();
               
               // Open without preserving nodes 
               for(DirectoryNode dir : files) {
                  HSSFWorkbook workbook = new HSSFWorkbook(dir, false);
                  HSSFSheet sheet = workbook.getSheetAt(0);
                  HSSFCell cell = sheet.getRow(0).getCell(0);
                  assertEquals("replaceMe", cell .getRichStringCellValue().getString());
                  
                  workbook.close();
               }
        
               // Now re-check with preserving
               for(DirectoryNode dir : files) {
                  HSSFWorkbook workbook = new HSSFWorkbook(dir, true);
                  HSSFSheet sheet = workbook.getSheetAt(0);
                  HSSFCell cell = sheet.getRow(0).getCell(0);
                  assertEquals("replaceMe", cell .getRichStringCellValue().getString());
                  
                  workbook.close();
               }
           } finally {
               npoifsFileSystem.close();
           }
       } finally {
           poifsFileSystem.close();
       }
    }

    @Test
    public void wordDocEmbeddedInXls() throws IOException {
       // Open the two filesystems
       DirectoryNode[] files = new DirectoryNode[2];
       POIFSFileSystem poifsFileSystem = new POIFSFileSystem(HSSFTestDataSamples.openSampleFileStream("WithEmbeddedObjects.xls"));
       try {
           files[0] = poifsFileSystem.getRoot();
           NPOIFSFileSystem npoifsFileSystem = new NPOIFSFileSystem(HSSFTestDataSamples.getSampleFile("WithEmbeddedObjects.xls"));
           try {
               files[1] = npoifsFileSystem.getRoot();
               
               // Check the embedded parts
               for(DirectoryNode root : files) {
                  HSSFWorkbook hw = new HSSFWorkbook(root, true);
                  List<HSSFObjectData> objects = hw.getAllEmbeddedObjects();
                  boolean found = false;
                  for (int i = 0; i < objects.size(); i++) {
                     HSSFObjectData embeddedObject = objects.get(i);
                     if (embeddedObject.hasDirectoryEntry()) {
                        DirectoryEntry dir = embeddedObject.getDirectory();
                        if (dir instanceof DirectoryNode) {
                           DirectoryNode dNode = (DirectoryNode)dir;
                           if (hasEntry(dNode,"WordDocument")) {
                              found = true;
                           }
                        }
                     }
                  }
                  assertTrue(found);
                  
                  hw.close();
               }
           } finally {
               npoifsFileSystem.close();
           }
       } finally {
           poifsFileSystem.close();
       }
    }

    /**
     * Checks that we can open a workbook with NPOIFS, and write it out
     *  again (via POIFS) and have it be valid
     * @throws IOException
     */
    @Test
    public void writeWorkbookFromNPOIFS() throws IOException {
       InputStream is = HSSFTestDataSamples.openSampleFileStream("WithEmbeddedObjects.xls");
       try {
           NPOIFSFileSystem fs = new NPOIFSFileSystem(is);
           try {
               // Start as NPOIFS
               HSSFWorkbook wb = new HSSFWorkbook(fs.getRoot(), true);
               assertEquals(3, wb.getNumberOfSheets());
               assertEquals("Root xls", wb.getSheetAt(0).getRow(0).getCell(0).getStringCellValue());

               // Will switch to POIFS
               HSSFWorkbook wbBack = HSSFTestDataSamples.writeOutAndReadBack(wb);
               assertEquals(3, wbBack.getNumberOfSheets());
               assertEquals("Root xls", wbBack.getSheetAt(0).getRow(0).getCell(0).getStringCellValue());
               wbBack.close();
               
               wb.close();
           } finally {
        	   fs.close();
           }
       } finally {
    	   is.close();
       }
    }

    @Test
    public void cellStylesLimit() throws IOException {
        HSSFWorkbook wb = new HSSFWorkbook();
        int numBuiltInStyles = wb.getNumCellStyles();
        int MAX_STYLES = 4030;
        int limit = MAX_STYLES - numBuiltInStyles;
        for(int i=0; i < limit; i++){
            /* HSSFCellStyle style =*/ wb.createCellStyle();
        }

        assertEquals(MAX_STYLES, wb.getNumCellStyles());
        try {
            /*HSSFCellStyle style =*/ wb.createCellStyle();
            fail("expected exception");
        } catch (IllegalStateException e){
            assertEquals("The maximum number of cell styles was exceeded. " +
                    "You can define up to 4000 styles in a .xls workbook", e.getMessage());
        }
        assertEquals(MAX_STYLES, wb.getNumCellStyles());
        
        wb.close();
    }

    @Test
    public void setSheetOrderHSSF() throws IOException{
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet s1 = wb.createSheet("first sheet");
        HSSFSheet s2 = wb.createSheet("other sheet");

        HSSFName name1 = wb.createName();
        name1.setNameName("name1");
        name1.setRefersToFormula("'first sheet'!D1");

        HSSFName name2 = wb.createName();
        name2.setNameName("name2");
        name2.setRefersToFormula("'other sheet'!C1");


        HSSFRow s1r1 = s1.createRow(2);
        HSSFCell c1 = s1r1.createCell(3);
        c1.setCellValue(30);
        HSSFCell c2 = s1r1.createCell(2);
        c2.setCellFormula("SUM('other sheet'!C1,'first sheet'!C1)");

        HSSFRow s2r1 = s2.createRow(0);
        HSSFCell c3 = s2r1.createCell(1);
        c3.setCellFormula("'first sheet'!D3");
        HSSFCell c4 = s2r1.createCell(2);
        c4.setCellFormula("'other sheet'!D3");

        // conditional formatting
        HSSFSheetConditionalFormatting sheetCF = s1.getSheetConditionalFormatting();

        HSSFConditionalFormattingRule rule1 = sheetCF.createConditionalFormattingRule(
                CFRuleRecord.ComparisonOperator.BETWEEN, "'first sheet'!D1", "'other sheet'!D1");

        HSSFConditionalFormattingRule [] cfRules = { rule1 };

        CellRangeAddress[] regions = {
            new CellRangeAddress(2, 4, 0, 0), // A3:A5
        };
        sheetCF.addConditionalFormatting(regions, cfRules);

        wb.setSheetOrder("other sheet", 0);

        // names
        assertEquals("'first sheet'!D1", wb.getName("name1").getRefersToFormula());
        assertEquals("'other sheet'!C1", wb.getName("name2").getRefersToFormula());

        // cells
        assertEquals("SUM('other sheet'!C1,'first sheet'!C1)", c2.getCellFormula());
        assertEquals("'first sheet'!D3", c3.getCellFormula());
        assertEquals("'other sheet'!D3", c4.getCellFormula());

        // conditional formatting
        HSSFConditionalFormatting cf = sheetCF.getConditionalFormattingAt(0);
        assertEquals("'first sheet'!D1", cf.getRule(0).getFormula1());
        assertEquals("'other sheet'!D1", cf.getRule(0).getFormula2());
        
        wb.close();
    }

    private boolean hasEntry(DirectoryNode dirNode, String entryName) {
       try {
           dirNode.getEntry(entryName);
           return true;
       } catch (FileNotFoundException e) {
           return false;
       }
   }

    @Test
    public void clonePictures() throws IOException {
        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("SimpleWithImages.xls");
        InternalWorkbook iwb = wb.getWorkbook();
        iwb.findDrawingGroup();

        for(int pictureIndex=1; pictureIndex <= 4; pictureIndex++){
            EscherBSERecord bse = iwb.getBSERecord(pictureIndex);
            assertEquals(1, bse.getRef());
        }

        wb.cloneSheet(0);
        for(int pictureIndex=1; pictureIndex <= 4; pictureIndex++){
            EscherBSERecord bse = iwb.getBSERecord(pictureIndex);
            assertEquals(2, bse.getRef());
        }

        wb.cloneSheet(0);
        for(int pictureIndex=1; pictureIndex <= 4; pictureIndex++){
            EscherBSERecord bse = iwb.getBSERecord(pictureIndex);
            assertEquals(3, bse.getRef());
        }
        
        wb.close();
    }

    @Test
    public void changeSheetNameWithSharedFormulas() throws IOException {
        changeSheetNameWithSharedFormulas("shared_formulas.xls");
    }

    // Should throw exception about invalid POIFSFileSystem
    @Test(expected=IllegalArgumentException.class)
    public void emptyDirectoryNode() throws IOException {
        POIFSFileSystem fs = new POIFSFileSystem();
        try {
            new HSSFWorkbook(fs).close();
        } finally {
            fs.close();
        }
    }

    @SuppressWarnings("deprecation")
    @Test
    public void selectedSheetShort() throws IOException {
        HSSFWorkbook wb=new HSSFWorkbook();
        
        HSSFSheet sheet1 = wb.createSheet("Sheet1");
        HSSFSheet sheet2 = wb.createSheet("Sheet2");
        HSSFSheet sheet3 = wb.createSheet("Sheet3");
        HSSFSheet sheet4 = wb.createSheet("Sheet4");

        confirmActiveSelected(sheet1, true);
        confirmActiveSelected(sheet2, false);
        confirmActiveSelected(sheet3, false);
        confirmActiveSelected(sheet4, false);

        wb.setSelectedTab((short)1);

        // see Javadoc, in this case selected means "active"
        assertEquals(wb.getActiveSheetIndex(), wb.getSelectedTab());

        // Demonstrate bug 44525:
        // Well... not quite, since isActive + isSelected were also added in the same bug fix
        assertFalse("Identified bug 44523 a", sheet1.isSelected());
        wb.setActiveSheet(1);
        assertFalse("Identified bug 44523 b", sheet1.isActive());

        confirmActiveSelected(sheet1, false);
        confirmActiveSelected(sheet2, true);
        confirmActiveSelected(sheet3, false);
        confirmActiveSelected(sheet4, false);

        assertEquals(0, wb.getFirstVisibleTab());
        wb.setDisplayedTab((short)2);
        assertEquals(2, wb.getFirstVisibleTab());
        assertEquals(2, wb.getDisplayedTab());

        wb.close();
    }

    @Test
    public void addSheetTwice() throws IOException {
        HSSFWorkbook wb=new HSSFWorkbook();
        HSSFSheet sheet1 = wb.createSheet("Sheet1");
        assertNotNull(sheet1);
        try {
            wb.createSheet("Sheet1");
            fail("Should fail if we add the same sheet twice");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("already contains a sheet of this name"));
        }
        
        wb.close();
    }

    @Test
    public void getSheetIndex() throws IOException {
        HSSFWorkbook wb=new HSSFWorkbook();
        HSSFSheet sheet1 = wb.createSheet("Sheet1");
        HSSFSheet sheet2 = wb.createSheet("Sheet2");
        HSSFSheet sheet3 = wb.createSheet("Sheet3");
        HSSFSheet sheet4 = wb.createSheet("Sheet4");

        assertEquals(0, wb.getSheetIndex(sheet1));
        assertEquals(1, wb.getSheetIndex(sheet2));
        assertEquals(2, wb.getSheetIndex(sheet3));
        assertEquals(3, wb.getSheetIndex(sheet4));

        // remove sheets
        wb.removeSheetAt(0);
        wb.removeSheetAt(2);

        // ensure that sheets are moved up and removed sheets are not found any more
        assertEquals(-1, wb.getSheetIndex(sheet1));
        assertEquals(0, wb.getSheetIndex(sheet2));
        assertEquals(1, wb.getSheetIndex(sheet3));
        assertEquals(-1, wb.getSheetIndex(sheet4));
        
        wb.close();
    }

    @SuppressWarnings("deprecation")
    @Test
    public void getExternSheetIndex() throws IOException {
        HSSFWorkbook wb=new HSSFWorkbook();
        wb.createSheet("Sheet1");
        wb.createSheet("Sheet2");

        assertEquals(0, wb.getExternalSheetIndex(0));
        assertEquals(1, wb.getExternalSheetIndex(1));

        assertEquals("Sheet1", wb.findSheetNameFromExternSheet(0));
        assertEquals("Sheet2", wb.findSheetNameFromExternSheet(1));
        //assertEquals(null, wb.findSheetNameFromExternSheet(2));

        assertEquals(0, wb.getSheetIndexFromExternSheetIndex(0));
        assertEquals(1, wb.getSheetIndexFromExternSheetIndex(1));
        assertEquals(-1, wb.getSheetIndexFromExternSheetIndex(2));
        assertEquals(-1, wb.getSheetIndexFromExternSheetIndex(100));
        
        wb.close();
    }

    @SuppressWarnings("deprecation")
    @Test
    public void sstString() throws IOException {
        HSSFWorkbook wb=new HSSFWorkbook();

        int sst = wb.addSSTString("somestring");
        assertEquals("somestring", wb.getSSTString(sst));
        //assertNull(wb.getSSTString(sst+1));
        
        wb.close();
    }

    @Test
    public void names() throws IOException {
        HSSFWorkbook wb=new HSSFWorkbook();

        try {
            wb.getNameAt(0);
            fail("Fails without any defined names");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("no defined names"));
        }

        HSSFName name = wb.createName();
        assertNotNull(name);

        assertNull(wb.getName("somename"));

        name.setNameName("myname");
        assertNotNull(wb.getName("myname"));

        assertEquals(0, wb.getNameIndex(name));
        assertEquals(0, wb.getNameIndex("myname"));

        try {
            wb.getNameAt(5);
            fail("Fails without any defined names");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("outside the allowable range"));
        }

        try {
            wb.getNameAt(-3);
            fail("Fails without any defined names");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("outside the allowable range"));
        }
        
        wb.close();
    }

    @Test
    public void testMethods() throws IOException {
        HSSFWorkbook wb=new HSSFWorkbook();
        wb.insertChartRecord();
        //wb.dumpDrawingGroupRecords(true);
        //wb.dumpDrawingGroupRecords(false);
        
        wb.close();
    }

    @Test
    public void writeProtection() throws IOException {
        HSSFWorkbook wb=new HSSFWorkbook();

        assertFalse(wb.isWriteProtected());

        wb.writeProtectWorkbook("mypassword", "myuser");
        assertTrue(wb.isWriteProtected());
        
//        OutputStream os = new FileOutputStream("/tmp/protected.xls");
//        try {
//            wb.write(os);
//        } finally {
//            os.close();
//        }

        wb.unwriteProtectWorkbook();
        assertFalse(wb.isWriteProtected());

        wb.close();
    }

    @Test
	public void bug50298() throws Exception {
		HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("50298.xls");

		assertSheetOrder(wb, "Invoice", "Invoice1", "Digest", "Deferred", "Received");

		HSSFSheet sheet = wb.cloneSheet(0);

		assertSheetOrder(wb, "Invoice", "Invoice1", "Digest", "Deferred", "Received", "Invoice (2)");

		wb.setSheetName(wb.getSheetIndex(sheet), "copy");

		assertSheetOrder(wb, "Invoice", "Invoice1", "Digest", "Deferred", "Received", "copy");

		wb.setSheetOrder("copy", 0);

		assertSheetOrder(wb, "copy", "Invoice", "Invoice1", "Digest", "Deferred", "Received");

		wb.removeSheetAt(0);

		assertSheetOrder(wb, "Invoice", "Invoice1", "Digest", "Deferred", "Received");

		// check that the overall workbook serializes with its correct size
		int expected = wb.getWorkbook().getSize();
		int written = wb.getWorkbook().serialize(0, new byte[expected*2]);

		assertEquals("Did not have the expected size when writing the workbook: written: " + written + ", but expected: " + expected,
				expected, written);

		HSSFWorkbook read = HSSFTestDataSamples.writeOutAndReadBack(wb);
		assertSheetOrder(read, "Invoice", "Invoice1", "Digest", "Deferred", "Received");
		read.close();
		wb.close();
	}

    @Test
	public void bug50298a() throws Exception {
		HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("50298.xls");

		assertSheetOrder(wb, "Invoice", "Invoice1", "Digest", "Deferred", "Received");

		HSSFSheet sheet = wb.cloneSheet(0);

		assertSheetOrder(wb, "Invoice", "Invoice1", "Digest", "Deferred", "Received", "Invoice (2)");

		wb.setSheetName(wb.getSheetIndex(sheet), "copy");

		assertSheetOrder(wb, "Invoice", "Invoice1", "Digest", "Deferred", "Received", "copy");

		wb.setSheetOrder("copy", 0);

		assertSheetOrder(wb, "copy", "Invoice", "Invoice1", "Digest", "Deferred", "Received");

		wb.removeSheetAt(0);

		assertSheetOrder(wb, "Invoice", "Invoice1", "Digest", "Deferred", "Received");

		wb.removeSheetAt(1);

		assertSheetOrder(wb, "Invoice", "Digest", "Deferred", "Received");

		wb.setSheetOrder("Digest", 3);

		assertSheetOrder(wb, "Invoice", "Deferred", "Received", "Digest");

		// check that the overall workbook serializes with its correct size
		int expected = wb.getWorkbook().getSize();
		int written = wb.getWorkbook().serialize(0, new byte[expected*2]);

		assertEquals("Did not have the expected size when writing the workbook: written: " + written + ", but expected: " + expected,
				expected, written);

		HSSFWorkbook read = HSSFTestDataSamples.writeOutAndReadBack(wb);
		assertSheetOrder(read, "Invoice", "Deferred", "Received", "Digest");
		read.close();
		wb.close();
	}
	
    @Test
	public void bug54500() throws Exception {
		String nameName = "AName";
		String sheetName = "ASheet";
		HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("54500.xls");

		assertSheetOrder(wb, "Sheet1", "Sheet2", "Sheet3");
		
		wb.createSheet(sheetName);

		assertSheetOrder(wb, "Sheet1", "Sheet2", "Sheet3", "ASheet");

		Name n = wb.createName();
		n.setNameName(nameName);
		n.setSheetIndex(3);
		n.setRefersToFormula(sheetName + "!A1");

		assertSheetOrder(wb, "Sheet1", "Sheet2", "Sheet3", "ASheet");
		assertEquals("ASheet!A1", wb.getName(nameName).getRefersToFormula());
		
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		wb.write(stream);

		assertSheetOrder(wb, "Sheet1", "Sheet2", "Sheet3", "ASheet");
		assertEquals("ASheet!A1", wb.getName(nameName).getRefersToFormula());

		wb.removeSheetAt(1);

		assertSheetOrder(wb, "Sheet1", "Sheet3", "ASheet");
		assertEquals("ASheet!A1", wb.getName(nameName).getRefersToFormula());

		ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
		wb.write(stream2);

		assertSheetOrder(wb, "Sheet1", "Sheet3", "ASheet");
		assertEquals("ASheet!A1", wb.getName(nameName).getRefersToFormula());

		HSSFWorkbook wb2 = new HSSFWorkbook(new ByteArrayInputStream(stream.toByteArray()));
		expectName(wb2, nameName, "ASheet!A1");
		HSSFWorkbook wb3 = new HSSFWorkbook(new ByteArrayInputStream(stream2.toByteArray()));
		expectName(wb3, nameName, "ASheet!A1");
		wb3.close();
		wb2.close();
		wb.close();
	}

	private void expectName(HSSFWorkbook wb, String name, String expect) {
		assertEquals(expect, wb.getName(name).getRefersToFormula());
	}

	@Test
	public void test49423() throws Exception
    {
		HSSFWorkbook workbook = HSSFTestDataSamples.openSampleWorkbook("49423.xls");
		
		boolean found = false;
        int numSheets = workbook.getNumberOfSheets();
        for (int i = 0; i < numSheets; i++) {
            HSSFSheet sheet = workbook.getSheetAt(i);
            List<HSSFShape> shapes = sheet.getDrawingPatriarch().getChildren();
            for(HSSFShape shape : shapes){
                HSSFAnchor anchor = shape.getAnchor();
    
                if(anchor instanceof HSSFClientAnchor){
                    // absolute coordinates
                    HSSFClientAnchor clientAnchor = (HSSFClientAnchor)anchor;
                    assertNotNull(clientAnchor);
                    //System.out.println(clientAnchor.getRow1() + "," + clientAnchor.getRow2());
                    found = true;
                } else if (anchor instanceof HSSFChildAnchor){
                    // shape is grouped and the anchor is expressed in the coordinate system of the group 
                    HSSFChildAnchor childAnchor = (HSSFChildAnchor)anchor;
                    assertNotNull(childAnchor);
                    //System.out.println(childAnchor.getDy1() + "," + childAnchor.getDy2());
                    found = true;
                }
            }
        }
        
        assertTrue("Should find some images via Client or Child anchors, but did not find any at all", found);
        workbook.close();
    }

    @Test
    public void testRewriteFileBug58480() throws IOException {
        final File file = TempFile.createTempFile("TestHSSFWorkbook", ".xls");

        try {
            // create new workbook
            {
                final Workbook workbook = new HSSFWorkbook();
                final Sheet sheet = workbook.createSheet("foo");
                final Row row = sheet.createRow(1);
                row.createCell(1).setCellValue("bar");
                
                writeAndCloseWorkbook(workbook, file);
            }
    
            // edit the workbook
            {
                NPOIFSFileSystem fs = new NPOIFSFileSystem(file, false);
                try {
                    DirectoryNode root = fs.getRoot();
                    final Workbook workbook = new HSSFWorkbook(root, true);
                    final Sheet sheet = workbook.getSheet("foo");
                    sheet.getRow(1).createCell(2).setCellValue("baz");
                    
                    writeAndCloseWorkbook(workbook, file);
                } finally {
                    fs.close();
                }
            }
        } finally {
            assertTrue(file.exists());
            assertTrue(file.delete());
        }
    }

    private void writeAndCloseWorkbook(Workbook workbook, File file)
    throws IOException {
        final ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        workbook.write(bytesOut);
        workbook.close();

        final byte[] byteArray = bytesOut.toByteArray();
        bytesOut.close();

        final FileOutputStream fileOut = new FileOutputStream(file);
        fileOut.write(byteArray);
        fileOut.close();

    }
    
    @Test
    @Override
    public void getSpreadsheetVersion() throws IOException {
        verifySpreadsheetVersion(SpreadsheetVersion.EXCEL97);
    }
}
