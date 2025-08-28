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

package org.apache.poi.hssf;

import java.io.File;
import java.io.InputStream;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestHSSFParser {
    @Test
    void testXls() throws Exception {
        try (
                InputStream stream = HSSFTestDataSamples.openSampleFileStream("44010-SingleChart.xls");
                HSSFWorkbook wb = HSSFParser.parse(stream)
        ) {
            assertNotNull(wb);
            assertEquals(2, wb.getNumberOfSheets());
        }
    }

    @Test
    void testFailOnXlsx() throws Exception {
        try (InputStream stream = HSSFTestDataSamples.openSampleFileStream("github-321.xlsx")) {
            HSSFReadException hre = assertThrows(HSSFReadException.class, () -> HSSFParser.parse(stream));
            assertInstanceOf(OfficeXmlFileException.class, hre.getCause());
        }
    }
}
