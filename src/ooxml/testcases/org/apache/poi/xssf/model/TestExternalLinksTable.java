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

package org.apache.poi.xssf.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;

import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

public final class TestExternalLinksTable {
    @Test
    void none() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("SampleSS.xlsx")) {
            assertNotNull(wb.getExternalLinksTable());
            assertEquals(0, wb.getExternalLinksTable().size());
        }
    }

    @Test
    void basicRead() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("ref-56737.xlsx")) {
            assertNotNull(wb.getExternalLinksTable());
            assertEquals(1, wb.getExternalLinksTable().size());

            ExternalLinksTable links = wb.getExternalLinksTable().get(0);
            assertEquals(3, links.getSheetNames().size());
            assertEquals(2, links.getDefinedNames().size());

            assertEquals("Uses", links.getSheetNames().get(0));
            assertEquals("Defines", links.getSheetNames().get(1));
            assertEquals("56737", links.getSheetNames().get(2));

            Name name = links.getDefinedNames().get(0);
            assertEquals("NR_Global_B2", name.getNameName());
            assertEquals(-1, name.getSheetIndex());
            assertNull(name.getSheetName());
            assertEquals("'Defines'!$B$2", name.getRefersToFormula());

            name = links.getDefinedNames().get(1);
            assertEquals("NR_To_A1", name.getNameName());
            assertEquals(1, name.getSheetIndex());
            assertEquals("Defines", name.getSheetName());
            assertEquals("'Defines'!$A$1", name.getRefersToFormula());

            assertEquals("56737.xlsx", links.getLinkedFileName());
        }
    }

    @Test
    void basicReadWriteRead() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("ref-56737.xlsx")) {
            Name name = wb.getExternalLinksTable().get(0).getDefinedNames().get(1);
            name.setNameName("Testing");
            name.setRefersToFormula("$A$1");

            XSSFWorkbook wbBack = XSSFTestDataSamples.writeOutAndReadBack(wb);
            assertEquals(1, wbBack.getExternalLinksTable().size());
            ExternalLinksTable links = wbBack.getExternalLinksTable().get(0);

            name = links.getDefinedNames().get(0);
            assertEquals("NR_Global_B2", name.getNameName());
            assertEquals(-1, name.getSheetIndex());
            assertNull(name.getSheetName());
            assertEquals("'Defines'!$B$2", name.getRefersToFormula());

            name = links.getDefinedNames().get(1);
            assertEquals("Testing", name.getNameName());
            assertEquals(1, name.getSheetIndex());
            assertEquals("Defines", name.getSheetName());
            assertEquals("$A$1", name.getRefersToFormula());
        }
    }

    @Test
    void readWithReferencesToTwoExternalBooks() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("ref2-56737.xlsx")) {
            assertNotNull(wb.getExternalLinksTable());
            assertEquals(2, wb.getExternalLinksTable().size());

            // Check the first one, links to 56737.xlsx
            ExternalLinksTable links = wb.getExternalLinksTable().get(0);
            assertEquals("56737.xlsx", links.getLinkedFileName());
            assertEquals(3, links.getSheetNames().size());
            assertEquals(2, links.getDefinedNames().size());

            assertEquals("Uses", links.getSheetNames().get(0));
            assertEquals("Defines", links.getSheetNames().get(1));
            assertEquals("56737", links.getSheetNames().get(2));

            Name name = links.getDefinedNames().get(0);
            assertEquals("NR_Global_B2", name.getNameName());
            assertEquals(-1, name.getSheetIndex());
            assertNull(name.getSheetName());
            assertEquals("'Defines'!$B$2", name.getRefersToFormula());

            name = links.getDefinedNames().get(1);
            assertEquals("NR_To_A1", name.getNameName());
            assertEquals(1, name.getSheetIndex());
            assertEquals("Defines", name.getSheetName());
            assertEquals("'Defines'!$A$1", name.getRefersToFormula());


            // Check the second one, links to 56737.xls, slightly differently
            links = wb.getExternalLinksTable().get(1);
            assertEquals("56737.xls", links.getLinkedFileName());
            assertEquals(2, links.getSheetNames().size());
            assertEquals(2, links.getDefinedNames().size());

            assertEquals("Uses", links.getSheetNames().get(0));
            assertEquals("Defines", links.getSheetNames().get(1));

            name = links.getDefinedNames().get(0);
            assertEquals("NR_Global_B2", name.getNameName());
            assertEquals(-1, name.getSheetIndex());
            assertNull(name.getSheetName());
            assertEquals("'Defines'!$B$2", name.getRefersToFormula());

            name = links.getDefinedNames().get(1);
            assertEquals("NR_To_A1", name.getNameName());
            assertEquals(1, name.getSheetIndex());
            assertEquals("Defines", name.getSheetName());
            assertEquals("'Defines'!$A$1", name.getRefersToFormula());
        }
    }
}
