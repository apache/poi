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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.HWPFTestDataSamples;
import org.apache.poi.util.HexDump;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Bug 47563 - Exception when working with table
 */
public class TestBug47563 {


	public static Stream<Arguments> data() {
		List<Arguments> data = new ArrayList<>();

		data.add(Arguments.of( 1, 5 ));
		data.add(Arguments.of( 1, 6 ));
		data.add(Arguments.of( 5, 1 ));
		data.add(Arguments.of( 6, 1 ));
		data.add(Arguments.of( 2, 2 ));
		data.add(Arguments.of( 3, 2 ));
		data.add(Arguments.of( 2, 3 ));
		data.add(Arguments.of( 3, 3 ));

		return data.stream();
	}

	@ParameterizedTest
	@MethodSource("data")
	void test(int rows, int columns) throws Exception {
		// POI apparently can't create a document from scratch,
		// so we need an existing empty dummy document
		try (HWPFDocument doc = HWPFTestDataSamples.openSampleFile("empty.doc")) {
			Range range = doc.getRange();
			range.sanityCheck();

			Table table = range.insertTableBefore((short) columns, rows);
			table.sanityCheck();

			for (int rowIdx = 0; rowIdx < table.numRows(); rowIdx++) {
				TableRow row = table.getRow(rowIdx);
				row.sanityCheck();

				for (int colIdx = 0; colIdx < row.numCells(); colIdx++) {
					TableCell cell = row.getCell(colIdx);
					cell.sanityCheck();

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
			String textBytes = HexDump.toHex(text.getBytes(StandardCharsets.UTF_8));
			int mustBeAfter = 0;
			for (int i = 0; i < rows * columns; i++) {
				int next = text.indexOf(Integer.toString(i), mustBeAfter);
				assertTrue( next != -1, "Test with " + rows + "/" + columns + ": Should find " + i +
					" but did not find it (" + next + ") with " + mustBeAfter + " in " + textBytes + "\n" + next);
				mustBeAfter = next;
			}
		}
	}
}
