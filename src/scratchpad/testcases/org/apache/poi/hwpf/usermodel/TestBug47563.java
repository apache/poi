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

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.HWPFTestDataSamples;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Bug 47563 - Exception when working with table 
 */
@RunWith(Parameterized.class)
public class TestBug47563 {

	@Parameterized.Parameter()
	public int rows;
	@Parameterized.Parameter(1)
	public int columns;

	@Parameterized.Parameters(name="rows: {0}, columns: {1}")
	public static Collection<Object[]> data() {
		List<Object[]> data = new ArrayList<>();

		data.add(new Object[] {1, 5});
		data.add(new Object[] {1, 6});
		data.add(new Object[] {5, 1});
		data.add(new Object[] {6, 1});
		data.add(new Object[] {2, 2});
		data.add(new Object[] {3, 2});
		data.add(new Object[] {2, 3});
		data.add(new Object[] {3, 3});

		return data;
	}

	@Test
	public void test() throws Exception {
		System.out.println();
		System.out.println("Testing with rows: " + rows + ", columns: " + columns);

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
			assertTrue("Test with " + rows + "/" + columns + ": Should not find " + i + " but found it at " + next + " with " + mustBeAfter + " in " + text + "\n" +
							text.indexOf(Integer.toString(i), mustBeAfter),
					next != -1);
			mustBeAfter = next;
		}
	}
}
