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

package org.apache.poi.hpsf.basic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.HPSFException;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.PropertySetFactory;
import org.apache.poi.hpsf.Section;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.util.CodePageUtil;
import org.junit.jupiter.api.Test;

/**
 * Tests whether Unicode string can be read from a DocumentSummaryInformation.
 */
class TestUnicode {
    private static final POIDataSamples samples = POIDataSamples.getHPSFInstance();

    /**
     * Tests the {@link PropertySet} methods. The test file has two
     * property set: the first one is a {@link SummaryInformation},
     * the second one is a {@link DocumentSummaryInformation}.
     *
     * @throws IOException if an I/O exception occurs
     * @throws HPSFException if an HPSF exception occurs
     */
    @Test
    void testPropertySetMethods() throws IOException, HPSFException {
        final String POI_FS = "TestUnicode.xls";
        final String[] POI_FILES = { DocumentSummaryInformation.DEFAULT_STREAM_NAME };

        File data = samples.getFile(POI_FS);

        POIFile poiFile = Util.readPOIFiles(data, POI_FILES).get(0);
        byte[] b = poiFile.getBytes();
        PropertySet ps = PropertySetFactory.create(new ByteArrayInputStream(b));
        assertTrue(ps.isDocumentSummaryInformation());
        assertEquals(2, ps.getSectionCount());
        Section s = ps.getSections().get(1);
        assertEquals(CodePageUtil.CP_UTF16, s.getProperty(1));
        assertEquals(-96070278, s.getProperty(2));
        assertEquals("MCon_Info zu Office bei Schreiner", s.getProperty(3));
        assertEquals("petrovitsch@schreiner-online.de", s.getProperty(4));
        assertEquals("Petrovitsch, Wilhelm", s.getProperty(5));
    }
}
