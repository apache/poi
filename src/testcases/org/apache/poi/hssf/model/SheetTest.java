package org.apache.poi.hssf.model;

import junit.framework.TestCase;

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

	public void testGetMergedRegionAt()
	{
		//TODO
	}

	public void testGetNumMergedRegions()
	{
		//TODO
	}

}



