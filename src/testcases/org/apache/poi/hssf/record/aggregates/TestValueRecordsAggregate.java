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

import junit.framework.TestCase;
import org.apache.poi.hssf.record.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TestValueRecordsAggregate extends TestCase
{
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

    public void testRemoveCell()
            throws Exception
    {
        BlankRecord blankRecord1 = newBlankRecord();
        valueRecord.insertCell( blankRecord1 );
        BlankRecord blankRecord2 = newBlankRecord();
        valueRecord.removeCell( blankRecord2 );
        Iterator iterator = valueRecord.getIterator();
        assertFalse( iterator.hasNext() );

        // removing an already empty cell just falls through
        valueRecord.removeCell( blankRecord2 );

        // even trying to remove null just falls through silently.
        valueRecord.removeCell( null );

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

    public void testGetRecordSize() throws Exception
    {
        List records = testData();
        valueRecord.construct( 0, records );
        assertEquals( 36, valueRecord.getRecordSize() );
    }

    public void testClone() throws Exception
    {
        List records = testData();
        valueRecord.construct( 0, records );
        valueRecord = (ValueRecordsAggregate) valueRecord.clone();
        assertEquals( 36, valueRecord.getRecordSize() );
    }

}
