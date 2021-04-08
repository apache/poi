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

package org.apache.poi.ss.formula;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link SheetNameFormatter}
 *
 * @author Josh Micich
 */
final class TestSheetNameFormatter {
	/**
	 * Tests main public method 'format'
	 */
	@Test
	void testFormat() {

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

		confirmFormat(null, "#REF");
	}

	private static void confirmFormat(String rawSheetName, String expectedSheetNameEncoding) {
		// test all variants

		assertEquals(expectedSheetNameEncoding, SheetNameFormatter.format(rawSheetName));

		StringBuilder sb = new StringBuilder();
		SheetNameFormatter.appendFormat(sb, rawSheetName);
		assertEquals(expectedSheetNameEncoding, sb.toString());

		sb = new StringBuilder();
		SheetNameFormatter.appendFormat((Appendable)sb, rawSheetName);
		assertEquals(expectedSheetNameEncoding, sb.toString());

		StringBuffer sbf = new StringBuffer();
		//noinspection deprecation
		SheetNameFormatter.appendFormat(sbf, rawSheetName);
		assertEquals(expectedSheetNameEncoding, sbf.toString());
	}

	@Test
	void testFormatWithWorkbookName() {

		confirmFormat("abc", "abc", "[abc]abc");
		confirmFormat("abc", "123", "'[abc]123'");

		confirmFormat("abc", "my sheet", "'[abc]my sheet'"); // space
		confirmFormat("abc", "A:MEM", "'[abc]A:MEM'"); // colon

		confirmFormat("abc", "O'Brian", "'[abc]O''Brian'"); // single quote gets doubled

		confirmFormat("abc", "3rdTimeLucky", "'[abc]3rdTimeLucky'"); // digit in first pos
		confirmFormat("abc", "_", "[abc]_"); // plain underscore OK
		confirmFormat("abc", "my_3rd_sheet", "[abc]my_3rd_sheet"); // underscores and digits OK
		confirmFormat("abc", "A12220", "'[abc]A12220'");
		confirmFormat("abc", "TAXRETURN19980415", "[abc]TAXRETURN19980415");

		confirmFormat("abc", null, "[abc]#REF");
		confirmFormat(null, "abc", "[#REF]abc");
		confirmFormat(null, null, "[#REF]#REF");
	}

	private static void confirmFormat(String workbookName, String rawSheetName, String expectedSheetNameEncoding) {
		// test all variants

		StringBuilder sb = new StringBuilder();
		SheetNameFormatter.appendFormat(sb, workbookName, rawSheetName);
		assertEquals(expectedSheetNameEncoding, sb.toString());

		sb = new StringBuilder();
		SheetNameFormatter.appendFormat((Appendable)sb, workbookName, rawSheetName);
		assertEquals(expectedSheetNameEncoding, sb.toString());

		StringBuffer sbf = new StringBuffer();
		//noinspection deprecation
		SheetNameFormatter.appendFormat(sbf, workbookName, rawSheetName);
		assertEquals(expectedSheetNameEncoding, sbf.toString());
	}

	@Test
	void testFormatException() {
		Appendable mock = new Appendable() {
			@Override
			public Appendable append(CharSequence csq) throws IOException {
				throw new IOException("Test exception");
			}

			@Override
			public Appendable append(CharSequence csq, int start, int end) throws IOException {
				throw new IOException("Test exception");
			}

			@Override
			public Appendable append(char c) throws IOException {
				throw new IOException("Test exception");
			}
		};

		assertThrows(RuntimeException.class, () -> SheetNameFormatter.appendFormat(mock, null, null));
		assertThrows(RuntimeException.class, () -> SheetNameFormatter.appendFormat(mock, null));
	}

	@Test
	void testBooleanLiterals() {
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
	@Test
	void testLooksLikePlainCellReference() {

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
	@Test
	void testCellRange() {
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
