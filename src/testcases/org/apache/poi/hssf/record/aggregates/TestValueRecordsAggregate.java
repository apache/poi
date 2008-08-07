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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.CRC32;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.record.BlankRecord;
import org.apache.poi.hssf.record.FormulaRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.SharedFormulaRecord;
import org.apache.poi.hssf.record.UnknownRecord;
import org.apache.poi.hssf.record.WindowOneRecord;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class TestValueRecordsAggregate extends TestCase
{
    private static final String ABNORMAL_SHARED_FORMULA_FLAG_TEST_FILE = "AbnormalSharedFormulaFlag.xls";
    ValueRecordsAggregate valueRecord = new ValueRecordsAggregate();

    /**
     * Make sure the shared formula DOESNT makes it to the FormulaRecordAggregate when being parsed
     * as part of the value records
     */
    public void testSharedFormula()
    {
        List records = new ArrayList();
        records.add( new FormulaRecord() );
        records.add( new SharedFormulaRecord() );

        valueRecord.construct( 0, records );
        Iterator iterator = valueRecord.getIterator();
        Record record = (Record) iterator.next();
        assertNotNull( "Row contains a value", record );
        assertTrue( "First record is a FormulaRecordsAggregate", ( record instanceof FormulaRecordAggregate ) );
        //Ensure that the SharedFormulaRecord has been converted
        assertFalse( "SharedFormulaRecord is null", iterator.hasNext() );

    }

    public void testUnknownRecordsIgnored()
    {
        List records = testData();
        valueRecord.construct( 0, records );
        Iterator iterator = valueRecord.getIterator();
        Record record1 = (Record) iterator.next();
        Record record2 = (Record) iterator.next();
        assertNotNull( "No record found", record1 );
        assertNotNull( "No record found", record2 );
        assertFalse( iterator.hasNext() );

    }

    private List testData(){
        List records = new ArrayList();
        FormulaRecord formulaRecord = new FormulaRecord();
        UnknownRecord unknownRecord = new UnknownRecord();
        BlankRecord blankRecord = new BlankRecord();
        WindowOneRecord windowOneRecord = new WindowOneRecord();
        formulaRecord.setRow( 1 );
        formulaRecord.setColumn( (short) 1 );
        blankRecord.setRow( 2 );
        blankRecord.setColumn( (short) 2 );
        records.add( formulaRecord );
        records.add( unknownRecord );
        records.add( blankRecord );
        records.add( windowOneRecord );
        return records;
    }

    public void testInsertCell()
            throws Exception
    {
        Iterator iterator = valueRecord.getIterator();
        assertFalse( iterator.hasNext() );

        BlankRecord blankRecord = newBlankRecord();
        valueRecord.insertCell( blankRecord );
        iterator = valueRecord.getIterator();
        assertTrue( iterator.hasNext() );
    }

    public void testRemoveCell() {
        BlankRecord blankRecord1 = newBlankRecord();
        valueRecord.insertCell( blankRecord1 );
        BlankRecord blankRecord2 = newBlankRecord();
        valueRecord.removeCell( blankRecord2 );
        Iterator iterator = valueRecord.getIterator();
        assertFalse( iterator.hasNext() );

        // removing an already empty cell just falls through
        valueRecord.removeCell( blankRecord2 );
    }

    public void testGetPhysicalNumberOfCells() throws Exception
    {
        assertEquals(0, valueRecord.getPhysicalNumberOfCells());
        BlankRecord blankRecord1 = newBlankRecord();
        valueRecord.insertCell( blankRecord1 );
        assertEquals(1, valueRecord.getPhysicalNumberOfCells());
        valueRecord.removeCell( blankRecord1 );
        assertEquals(0, valueRecord.getPhysicalNumberOfCells());
    }

    public void testGetFirstCellNum() throws Exception
    {
        assertEquals( -1, valueRecord.getFirstCellNum() );
        valueRecord.insertCell( newBlankRecord( 2, 2 ) );
        assertEquals( 2, valueRecord.getFirstCellNum() );
        valueRecord.insertCell( newBlankRecord( 3, 3 ) );
        assertEquals( 2, valueRecord.getFirstCellNum() );

        // Note: Removal doesn't currently reset the first column.  It probably should but it doesn't.
        valueRecord.removeCell( newBlankRecord( 2, 2 ) );
        assertEquals( 2, valueRecord.getFirstCellNum() );
    }

    public void testGetLastCellNum() throws Exception
    {
        assertEquals( -1, valueRecord.getLastCellNum() );
        valueRecord.insertCell( newBlankRecord( 2, 2 ) );
        assertEquals( 2, valueRecord.getLastCellNum() );
        valueRecord.insertCell( newBlankRecord( 3, 3 ) );
        assertEquals( 3, valueRecord.getLastCellNum() );

        // Note: Removal doesn't currently reset the last column.  It probably should but it doesn't.
        valueRecord.removeCell( newBlankRecord( 3, 3 ) );
        assertEquals( 3, valueRecord.getLastCellNum() );

    }

    public void testSerialize() throws Exception
    {
        byte[] actualArray = new byte[36];
        byte[] expectedArray = new byte[]
        {
            (byte)0x06, (byte)0x00, (byte)0x16, (byte)0x00,
            (byte)0x01, (byte)0x00, (byte)0x01, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x01, (byte)0x02,
            (byte)0x06, (byte)0x00, (byte)0x02, (byte)0x00,
            (byte)0x02, (byte)0x00, (byte)0x00, (byte)0x00,
        };
        List records = testData();
        valueRecord.construct( 0, records );
        int bytesWritten = valueRecord.serializeCellRow(1, 0, actualArray );
        bytesWritten += valueRecord.serializeCellRow(2, bytesWritten, actualArray );
        assertEquals( 36, bytesWritten );
        for (int i = 0; i < 36; i++)
            assertEquals( expectedArray[i], actualArray[i] );
    }

    public static void main( String[] args )
    {
        System.out.println( "Testing org.apache.poi.hssf.record.aggregates.TestValueRecordAggregate" );
        junit.textui.TestRunner.run( TestValueRecordsAggregate.class );
    }

    private BlankRecord newBlankRecord()
    {
        return newBlankRecord( 2, 2 );
    }

    private BlankRecord newBlankRecord( int col, int row)
    {
        BlankRecord blankRecord = new BlankRecord();
        blankRecord.setRow( row );
        blankRecord.setColumn( (short) col );
        return blankRecord;
    }

    /**
     * Sometimes the 'shared formula' flag (<tt>FormulaRecord.isSharedFormula()</tt>) is set when 
     * there is no corresponding SharedFormulaRecord available. SharedFormulaRecord definitions do
     * not span multiple sheets.  They are are only defined within a sheet, and thus they do not 
     * have a sheet index field (only row and column range fields).<br/>
     * So it is important that the code which locates the SharedFormulaRecord for each 
     * FormulaRecord does not allow matches across sheets.</br> 
     * 
     * Prior to bugzilla 44449 (Feb 2008), POI <tt>ValueRecordsAggregate.construct(int, List)</tt> 
     * allowed <tt>SharedFormulaRecord</tt>s to be erroneously used across sheets.  That incorrect
     * behaviour is shown by this test.<p/>
     * 
     * <b>Notes on how to produce the test spreadsheet</b>:</p>
     * The setup for this test (AbnormalSharedFormulaFlag.xls) is rather fragile, insomuchas 
     * re-saving the file (either with Excel or POI) clears the flag.<br/>
     * <ol>
     * <li>A new spreadsheet was created in Excel (File | New | Blank Workbook).</li>
     * <li>Sheet3 was deleted.</li>
     * <li>Sheet2!A1 formula was set to '="second formula"', and fill-dragged through A1:A8.</li>
     * <li>Sheet1!A1 formula was set to '="first formula"', and also fill-dragged through A1:A8.</li>
     * <li>Four rows on Sheet1 "5" through "8" were deleted ('delete rows' alt-E D, not 'clear' Del).</li>
     * <li>The spreadsheet was saved as AbnormalSharedFormulaFlag.xls.</li>
     * </ol>
     * Prior to the row delete action the spreadsheet has two <tt>SharedFormulaRecord</tt>s. One 
     * for each sheet. To expose the bug, the shared formulas have been made to overlap.<br/>
     * The row delete action (as described here) seems to to delete the 
     * <tt>SharedFormulaRecord</tt> from Sheet1 (but not clear the 'shared formula' flags.<br/>
     * There are other variations on this theme to create the same effect.  
     * 
     */
    public void testSpuriousSharedFormulaFlag() {
        
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
        if(!cellFormula.equals("\"first formula\"")) {
            throw new RuntimeException("Something else wrong with this test case");
        }
        
        // but the problem is observable in rows 2,3,4 
        cellFormula = getFormulaFromFirstCell(s, 1); // row "2"
        if(cellFormula.equals("\"second formula\"")) {
            throw new AssertionFailedError("found bug 44449 (Wrong SharedFormulaRecord was used).");
        }
        if(!cellFormula.equals("\"first formula\"")) {
            throw new RuntimeException("Something else wrong with this test case");
        }
    }
    private static String getFormulaFromFirstCell(HSSFSheet s, int rowIx) {
        return s.getRow(rowIx).getCell((short)0).getCellFormula();
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

}
