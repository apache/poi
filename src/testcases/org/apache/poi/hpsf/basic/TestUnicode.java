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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.HPSFException;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.PropertySetFactory;
import org.apache.poi.hpsf.Section;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.util.CodePageUtil;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests whether Unicode string can be read from a DocumentSummaryInformation.
 */
public class TestUnicode {

    static final String POI_FS = "TestUnicode.xls";
    static final String[] POI_FILES =  {
        DocumentSummaryInformation.DEFAULT_STREAM_NAME,
    };
    File data;
    POIFile[] poiFiles;


    /**
     * Read a the test file from the "data" directory.
     *
     * @exception FileNotFoundException if the file to be read does not exist.
     * @exception IOException if any other I/O exception occurs
     */
    @Before
    public void setUp() {
        POIDataSamples samples = POIDataSamples.getHPSFInstance();
        data = samples.getFile(POI_FS);
    }



    /**
     * Tests the {@link PropertySet} methods. The test file has two
     * property set: the first one is a {@link SummaryInformation},
     * the second one is a {@link DocumentSummaryInformation}.
     *
     * @exception IOException if an I/O exception occurs
     * @exception HPSFException if an HPSF exception occurs
     */
    @Test
    public void testPropertySetMethods() throws IOException, HPSFException {
        POIFile poiFile = Util.readPOIFiles(data, POI_FILES).get(0);
        byte[] b = poiFile.getBytes();
        PropertySet ps = PropertySetFactory.create(new ByteArrayInputStream(b));
        assertTrue(ps.isDocumentSummaryInformation());
        assertEquals(ps.getSectionCount(), 2);
        Section s = ps.getSections().get(1);
        assertEquals(s.getProperty(1), CodePageUtil.CP_UTF16);
        assertEquals(s.getProperty(2), -96070278);
        assertEquals(s.getProperty(3), "MCon_Info zu Office bei Schreiner");
        assertEquals(s.getProperty(4), "petrovitsch@schreiner-online.de");
        assertEquals(s.getProperty(5), "Petrovitsch, Wilhelm");
    }
}
