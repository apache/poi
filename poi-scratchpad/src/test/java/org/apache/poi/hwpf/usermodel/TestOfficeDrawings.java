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

import static org.apache.poi.hwpf.HWPFTestDataSamples.openSampleFile;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.poi.ddf.EscherComplexProperty;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherOptRecord;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.util.StringUtil;
import org.junit.jupiter.api.Test;

/**
 * Test cases for {@link OfficeDrawing} and {@link OfficeDrawingsImpl} classes.
 */
public class TestOfficeDrawings {
    /**
     * Tests watermark text extraction
     */
    @Test
    void testWatermark() throws Exception {
        try (HWPFDocument hwpfDocument = openSampleFile("watermark.doc")) {
            OfficeDrawing drawing = hwpfDocument.getOfficeDrawingsHeaders().getOfficeDrawings().iterator().next();
            EscherContainerRecord escherContainerRecord = drawing.getOfficeArtSpContainer();

            EscherOptRecord officeArtFOPT = escherContainerRecord.getChildById((short) 0xF00B);
            EscherComplexProperty gtextUNICODE = officeArtFOPT.lookup(0x00c0);

            String text = StringUtil.getFromUnicodeLE(gtextUNICODE.getComplexData());
            assertEquals("DRAFT CONTRACT\0", text);
        }
    }
}
