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

package org.apache.poi.hssf.record.formula;

import junit.framework.TestCase;

/**
 * Tests for {@link SheetNameFormatter}
 * 
 * @author Josh Micich
 */
public final class TestSheetNameFormatter extends TestCase {

	private static void confirmFormat(String rawSheetName, String expectedSheetNameEncoding) {
		assertEquals(expectedSheetNameEncoding, SheetNameFormatter.format(rawSheetName));
	}

	/**
	 * Tests main public method 'format' 
	 */
	public void testFormat() {
		
		confirmFormat("abc", "abc");
		confirmFormat("123", "'123'");
		
		confirmFormat("my sheet", "'my sheet'"); // space
		confirmFormat("A:MEM", "'A:MEM'"); // colon

		confirmFormat("O'Brian", "'O''Brian'"); // single quote gets doubled
		
		
		confirmFormat("3rdTimeLucky", "'3rdTimeLucky'"); // digit in first pos
		confirmFormat("_", "_"); // plain underscore OK
		confirmFormat("my_3rd_sheet", "my_3rd_sheet"); // underscores and digits OK
		confirmFormat("A12220", "'A12220'"); 
		confirmFormat("TAXRETURN19980415", "TAXRETURN19980415"); 
	}
	
	public void testBooleanLiterals() {
		confirmFormat("TRUE", "'TRUE'");
		confirmFormat("FALSE", "'FALSE'");
		confirmFormat("True", "'True'");
		confirmFormat("fAlse", "'fAlse'");
		
		confirmFormat("Yes", "Yes");
		confirmFormat("No", "No");
	}
	
	private static void confirmCellNameMatch(String rawSheetName, boolean expected) {
		assertEquals(expected, SheetNameFormatter.nameLooksLikePlainCellReference(rawSheetName));
	}
	
	/**
	 * Tests functionality to determine whether a sheet name containing only letters and digits
	 * would look (to Excel) like a cell name.
	 */
	public void testLooksLikePlainCellReference() {
		
		confirmCellNameMatch("A1", true);
		confirmCellNameMatch("a111", true);
		confirmCellNameMatch("AA", false);
		confirmCellNameMatch("aa1", true);
		confirmCellNameMatch("A1A", false);
		confirmCellNameMatch("A1A1", false);
		confirmCellNameMatch("Sh3", false);
		confirmCellNameMatch("SALES20080101", false); // out of range
	}
	
	private static void confirmCellRange(String text, int numberOfPrefixLetters, boolean expected) {
		String prefix = text.substring(0, numberOfPrefixLetters);
		String suffix = text.substring(numberOfPrefixLetters);
		assertEquals(expected, SheetNameFormatter.cellReferenceIsWithinRange(prefix, suffix));
	}
	
	/**
	 * Tests exact boundaries for names that look very close to cell names (i.e. contain 1 or more
	 * letters followed by one or more digits).
	 */
	public void testCellRange() {
		confirmCellRange("A1", 1, true);
		confirmCellRange("a111", 1, true);
		confirmCellRange("A65536", 1, true);
		confirmCellRange("A65537", 1, false);
		confirmCellRange("iv1", 2, true);
		confirmCellRange("IW1", 2, false);
		confirmCellRange("AAA1", 3, false);
		confirmCellRange("a111", 1, true);
		confirmCellRange("Sheet1", 6, false);
		confirmCellRange("iV65536", 2, true);  // max cell in Excel 97-2003
		confirmCellRange("IW65537", 2, false);
	}
}
