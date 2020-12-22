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

package org.apache.poi.xwpf.usermodel;

import org.apache.poi.xwpf.XWPFTestDataSamples;
import org.junit.Test;
import java.io.IOException;
import static org.junit.Assert.assertEquals;

public class TestColumn {
    @Test
    public void testAddNewCol() throws IOException {
        XWPFDocument doc = new XWPFDocument();
        XWPFTable table = doc.createTable(2, 4);
        table.addNewCol();

        int expectedNumberOfColumns = 5;
        table.tableRows.forEach(row -> assertEquals(expectedNumberOfColumns, row.getTableCells().size()));
        doc.close();
    }

    @Test
    public void testAddNewColWithEmptyTable() throws IOException {
        XWPFDocument doc = new XWPFDocument();
        XWPFTable table = doc.createTable(0, 0);
        table.removeRow(0);
        table.addNewCol();

        int expectedNumberOfColumnsInRow1 = 1;
        int actualNumberOfColumnsInRow1 = table.tableRows.get(0).getTableCells().size();
        assertEquals(expectedNumberOfColumnsInRow1, actualNumberOfColumnsInRow1);
        doc.close();
    }

    @Test
    public void testAddNewColWithDocx() throws Exception {
        try (XWPFDocument doc = XWPFTestDataSamples
                .openSampleDocument("TestTableColumns.docx")) {
            XWPFTable table = doc.getTables().get(0);
            table.addNewCol();

            int expectedNumberOfColumns = 5;
            table.tableRows.forEach(row -> assertEquals(expectedNumberOfColumns, row.getTableCells().size()));
        }
    }

    @Test
    public void testAddNewColWhenRowsHaveDifferentNumbersOfColumnsWithDocx() throws Exception {
        try (XWPFDocument doc = XWPFTestDataSamples
                .openSampleDocument("TestTableColumns.docx")) {
            XWPFTable table = doc.getTables().get(1);
            table.addNewCol();

            int expectedNumberOfColumnsInRow1 = 5;
            int actualNumberOfColumnsInRow1 = table.tableRows.get(0).getTableCells().size();
            assertEquals(expectedNumberOfColumnsInRow1, actualNumberOfColumnsInRow1);

            int expectedNumberOfColumnsInRow2 = 4;
            int actualNumberOfColumnsInRow2 = table.tableRows.get(1).getTableCells().size();
            assertEquals(expectedNumberOfColumnsInRow2, actualNumberOfColumnsInRow2);
        }
    }
}
