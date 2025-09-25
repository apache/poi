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

import org.apache.poi.xslf.usermodel.XSLFColor;
import org.apache.poi.xwpf.XWPFTestDataSamples;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.drawingml.x2006.main.CTColor;

import java.awt.*;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class TestXWPFTheme {

    @Test
    void testRead() throws IOException {
        try (XWPFDocument docx = XWPFTestDataSamples.openSampleDocument("sample.docx")) {
            XWPFTheme theme = docx.getTheme();
            assertEquals("Office Theme", theme.getName());
            assertEquals("Cambria", theme.getMajorFont());
            assertEquals("Calibri", theme.getMinorFont());
            CTColor accent1 = theme.getCTColor("accent1");
            XSLFColor color = new XSLFColor(accent1, null, null, null);
            assertEquals(new Color(79, 129, 189), color.getColor());
        }
    }
}
