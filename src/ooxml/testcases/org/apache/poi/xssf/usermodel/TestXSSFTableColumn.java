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

package org.apache.poi.xssf.usermodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.List;

import org.apache.poi.xssf.XSSFTestDataSamples;
import org.junit.Test;

public final class TestXSSFTableColumn {

    @Test
    public void testGetColumnName() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples
                .openSampleWorkbook("CustomXMLMappings-complex-type.xlsx")) {
            XSSFTable table = wb.getTable("Tabella2");

            List<XSSFTableColumn> tableColumns = table.getColumns();

            assertEquals("ID", tableColumns.get(0).getName());
            assertEquals("Unmapped Column", tableColumns.get(1).getName());
            assertEquals("SchemaRef", tableColumns.get(2).getName());
            assertEquals("Namespace", tableColumns.get(3).getName());

        }
    }

    @Test
    public void testGetColumnIndex() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples
                .openSampleWorkbook("CustomXMLMappings-complex-type.xlsx")) {
            XSSFTable table = wb.getTable("Tabella2");

            List<XSSFTableColumn> tableColumns = table.getColumns();

            assertEquals(0, tableColumns.get(0).getColumnIndex());
            assertEquals(1, tableColumns.get(1).getColumnIndex());
            assertEquals(2, tableColumns.get(2).getColumnIndex());
            assertEquals(3, tableColumns.get(3).getColumnIndex());

        }
    }

    @Test
    public void testGetXmlColumnPrs() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples
                .openSampleWorkbook("CustomXMLMappings-complex-type.xlsx")) {
            XSSFTable table = wb.getTable("Tabella2");

            List<XSSFTableColumn> tableColumns = table.getColumns();

            assertNotNull(tableColumns.get(0).getXmlColumnPr());
            assertNull(tableColumns.get(1).getXmlColumnPr()); // unmapped column
            assertNotNull(tableColumns.get(2).getColumnIndex());
            assertNotNull(tableColumns.get(3).getColumnIndex());

        }
    }
}
