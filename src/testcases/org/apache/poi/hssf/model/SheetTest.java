package org.apache.poi.hssf.model;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.poi.hssf.record.ColumnInfoRecord;
import org.apache.poi.hssf.record.MergeCellsRecord;
import org.apache.poi.hssf.record.RowRecord;
import org.apache.poi.hssf.record.StringRecord;

/**
 * @author Tony Poppleton
 */
public class SheetTest extends TestCase
{
	/**
	 * Constructor for SheetTest.
	 * @param arg0
	 */
	public SheetTest(String arg0)
	{
		super(arg0);
	}
	
	public void testAddMergedRegion()
	{
		Sheet sheet = Sheet.createSheet();
		int regionsToAdd = 4096;
		int startRecords = sheet.getRecords().size();
		
		//simple test that adds a load of regions
		for (int n = 0; n < regionsToAdd; n++)
		{
			int index = sheet.addMergedRegion(0, (short) 0, 1, (short) 1);
			assertTrue("Merged region index expected to be " + n + " got " + index, index == n);
		}
		
		//test all the regions were indeed added 
		assertTrue(sheet.getNumMergedRegions() == regionsToAdd);
		
		//test that the regions were spread out over the appropriate number of records
		int recordsAdded    = sheet.getRecords().size() - startRecords;
		int recordsExpected = regionsToAdd/1027;
		if ((regionsToAdd % 1027) != 0)
			recordsExpected++;
		assertTrue("The " + regionsToAdd + " merged regions should have been spread out over " + recordsExpected + " records, not " + recordsAdded, recordsAdded == recordsExpected);
	}

	public void testRemoveMergedRegion()
	{
		Sheet sheet = Sheet.createSheet();
		int regionsToAdd = 4096;
		
		for (int n = 0; n < regionsToAdd; n++)
			sheet.addMergedRegion(0, (short) 0, 1, (short) 1);
			
		int records = sheet.getRecords().size();
		
		//remove a third from the beginning
		for (int n = 0; n < regionsToAdd/3; n++)
		{
			sheet.removeMergedRegion(0); 
			//assert they have been deleted
			assertTrue("Num of regions should be " + (regionsToAdd - n - 1) + " not " + sheet.getNumMergedRegions(), sheet.getNumMergedRegions() == regionsToAdd - n - 1);
		}
		
		//assert any record removing was done
		int recordsRemoved = (regionsToAdd/3)/1027; //doesn't work for particular values of regionsToAdd
		assertTrue("Expected " + recordsRemoved + " record to be removed from the starting " + records + ".  Currently there are " + sheet.getRecords().size() + " records", records - sheet.getRecords().size() == recordsRemoved);
	}

	/**
	 * Bug: 22922 (Reported by Xuemin Guan)
	 * <p>
	 * Remove mergedregion fails when a sheet loses records after an initial CreateSheet
	 * fills up the records.
	 *
	 */
	public void testMovingMergedRegion() {
		List records = new ArrayList();
		
		MergeCellsRecord merged = new MergeCellsRecord();
		merged.addArea(0, (short)0, 1, (short)2);
		records.add(new RowRecord());
		records.add(new RowRecord());
		records.add(new RowRecord());
		records.add(merged);
		
		Sheet sheet = Sheet.createSheet(records, 0);
		sheet.records.remove(0);
		
		//stub object to throw off list INDEX operations
		sheet.removeMergedRegion(0);
		assertEquals("Should be no more merged regions", 0, sheet.getNumMergedRegions());
	}

	public void testGetMergedRegionAt()
	{
		//TODO
	}

	public void testGetNumMergedRegions()
	{
		//TODO
	}

	public void testGetCellWidth()
	{
		try{
			Sheet sheet = Sheet.createSheet();
			ColumnInfoRecord nci = ( ColumnInfoRecord ) sheet.createColInfo();
	
			//prepare test model
			nci.setFirstColumn((short)5);
			nci.setLastColumn((short)10);
			nci.setColumnWidth((short)100);
			Field f = Sheet.class.getDeclaredField("columnSizes");
			f.setAccessible(true);
			List columnSizes = new ArrayList();
			f.set(sheet,columnSizes);
			columnSizes.add(nci);
			sheet.records.add(1 + sheet.dimsloc, nci);
			sheet.dimsloc++;
	
			assertEquals((short)100,sheet.getColumnWidth((short)5));
			assertEquals((short)100,sheet.getColumnWidth((short)6));
			assertEquals((short)100,sheet.getColumnWidth((short)7));
			assertEquals((short)100,sheet.getColumnWidth((short)8));
			assertEquals((short)100,sheet.getColumnWidth((short)9));
			assertEquals((short)100,sheet.getColumnWidth((short)10));

			sheet.setColumnWidth((short)6,(short)200);

			assertEquals((short)100,sheet.getColumnWidth((short)5));
			assertEquals((short)200,sheet.getColumnWidth((short)6));
			assertEquals((short)100,sheet.getColumnWidth((short)7));
			assertEquals((short)100,sheet.getColumnWidth((short)8));
			assertEquals((short)100,sheet.getColumnWidth((short)9));
			assertEquals((short)100,sheet.getColumnWidth((short)10));
			

		}
		catch(Exception e){e.printStackTrace();fail(e.getMessage());}

	}

	/**
	 * Makes sure all rows registered for this sheet are aggregated, they were being skipped
	 *
	 */
	public void testRowAggregation() {
		List records = new ArrayList();
		RowRecord row = new RowRecord();
		row.setRowNumber(0);		
		records.add(row);
		
		row = new RowRecord();
		row.setRowNumber(1);
		records.add(row);

		records.add(new StringRecord());
		
		row = new RowRecord();
		row.setRowNumber(2);
		records.add(row);
		
		
		Sheet sheet = Sheet.createSheet(records, 0);
		assertNotNull("Row [2] was skipped", sheet.getRow(2));
		
	}

}



