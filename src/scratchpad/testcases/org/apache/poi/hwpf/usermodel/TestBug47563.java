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
package org.apache.poi.hwpf.usermodel;

import junit.framework.TestCase;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.HWPFTestDataSamples;

/**
 * Bug 47563 - Exception when working with table 
 */
public class TestBug47563 extends TestCase {

	public void test() throws Exception {
		test(1, 5);
		test(1, 6);
		test(5, 1);
		test(6, 1);
		test(2, 2);
		test(3, 2);
		test(2, 3);
		test(3, 3);
	}

	private void test(int rows, int columns) throws Exception {
		// POI apparently can't create a document from scratch,
		// so we need an existing empty dummy document
		HWPFDocument doc = HWPFTestDataSamples.openSampleFile("empty.doc");

		Range range = doc.getRange();
		range.sanityCheck();

		Table table = range.insertTableBefore((short) columns, rows);
		table.sanityCheck();

		for (int rowIdx = 0; rowIdx < table.numRows(); rowIdx++) {
			TableRow row = table.getRow(rowIdx);
			row.sanityCheck();

			System.out.println("row " + rowIdx);
			for (int colIdx = 0; colIdx < row.numCells(); colIdx++) {
				TableCell cell = row.getCell(colIdx);
				cell.sanityCheck();

				System.out.println("column " + colIdx + ", num paragraphs "
						+ cell.numParagraphs());

				Paragraph par = cell.getParagraph(0);
				par.sanityCheck();

				par.insertBefore("" + (rowIdx * row.numCells() + colIdx));
				par.sanityCheck();
				
				row.sanityCheck();
				table.sanityCheck();
				range.sanityCheck();

			}
		}

		String text = range.text();
		int mustBeAfter = 0;
		for (int i = 0; i < rows * columns; i++) {
			int next = text.indexOf(Integer.toString(i), mustBeAfter);
			assertTrue("Test with " + rows + "/" + columns + ": Should not find " + i + " but found it at " + next + " in " + text, 
					next != -1);
			mustBeAfter = next;
		}
	}
}
