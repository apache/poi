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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hpsf.ClassID;
import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.Filetime;
import org.apache.poi.hpsf.HPSFException;
import org.apache.poi.hpsf.MarkUnsupportedException;
import org.apache.poi.hpsf.NoPropertySetStreamException;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.PropertySetFactory;
import org.apache.poi.hpsf.Section;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hpsf.wellknown.PropertyIDMap;
import org.junit.Before;
import org.junit.Test;

/**
 * <p>Tests the basic HPSF functionality.</p>
 */
public final class TestBasic {

    private static final POIDataSamples samples = POIDataSamples.getHPSFInstance();

    private static final String[] POI_FILES = {
        SummaryInformation.DEFAULT_STREAM_NAME,
        DocumentSummaryInformation.DEFAULT_STREAM_NAME,
        "WordDocument",
        "\001CompObj",
        "1Table"
    };
    private static final int BYTE_ORDER   = 0xfffe;
    private static final int FORMAT       = 0x0000;
    private static final int OS_VERSION   = 0x00020A04;
    private static final ClassID CLASS_ID = new ClassID("{00000000-0000-0000-0000-000000000000}");
    private static final int[] SECTION_COUNT = {1, 2};
    private static final boolean[] IS_SUMMARY_INFORMATION = {true, false};
    private static final boolean[] IS_DOCUMENT_SUMMARY_INFORMATION = {false, true};

    private List<POIFile> poiFiles;


    /**
     * <p>Read a the test file from the "data" directory.</p>
     *
     * @exception FileNotFoundException if the file to be read does not exist.
     * @exception IOException if any other I/O exception occurs.
     */
    @Before
    public void setUp() throws IOException {
        final File data = samples.getFile("TestGermanWord90.doc");
        poiFiles = Util.readPOIFiles(data);
    }

    /**
     * <p>Checks the names of the files in the POI filesystem. They
     * are expected to be in a certain order.</p>
     */
    @Test
    public void testReadFiles() {
        String[] expected = POI_FILES;
        for (int i = 0; i < expected.length; i++) {
            assertEquals(poiFiles.get(i).getName(), expected[i]);
        }
    }

    /**
     * <p>Tests whether property sets can be created from the POI
     * files in the POI file system. This test case expects the first
     * file to be a {@link SummaryInformation}, the second file to be
     * a {@link DocumentSummaryInformation} and the rest to be no
     * property sets. In the latter cases a {@link
     * NoPropertySetStreamException} will be thrown when trying to
     * create a {@link PropertySet}.</p>
     *
     * @exception IOException if an I/O exception occurs.
     *
     * @exception UnsupportedEncodingException if a character encoding is not
     * supported.
     */
    @Test
    public void testCreatePropertySets()
    throws UnsupportedEncodingException, IOException {
        Class<?>[] expected = {
            SummaryInformation.class,
            DocumentSummaryInformation.class,
            NoPropertySetStreamException.class,
            NoPropertySetStreamException.class,
            NoPropertySetStreamException.class
        };
        for (int i = 0; i < expected.length; i++) {
            InputStream in = new ByteArrayInputStream(poiFiles.get(i).getBytes());
            Object o;
            try {
                o = PropertySetFactory.create(in);
            } catch (NoPropertySetStreamException ex) {
                o = ex;
            } catch (MarkUnsupportedException ex) {
                o = ex;
            }
            in.close();
            assertEquals(expected[i], o.getClass());
        }
    }

    /**
     * <p>Tests the {@link PropertySet} methods. The test file has two
     * property sets: the first one is a {@link SummaryInformation},
     * the second one is a {@link DocumentSummaryInformation}.</p>
     *
     * @exception IOException if an I/O exception occurs
     * @exception HPSFException if any HPSF exception occurs
     */
    @Test
    public void testPropertySetMethods() throws IOException, HPSFException {
        /* Loop over the two property sets. */
        for (int i = 0; i < 2; i++) {
            byte[] b = poiFiles.get(i).getBytes();
            PropertySet ps = PropertySetFactory.create(new ByteArrayInputStream(b));
            assertEquals(BYTE_ORDER, ps.getByteOrder());
            assertEquals(FORMAT, ps.getFormat());
            assertEquals(OS_VERSION, ps.getOSVersion());
            assertEquals(CLASS_ID, ps.getClassID());
            assertEquals(SECTION_COUNT[i], ps.getSectionCount());
            assertEquals(IS_SUMMARY_INFORMATION[i], ps.isSummaryInformation());
            assertEquals(IS_DOCUMENT_SUMMARY_INFORMATION[i], ps.isDocumentSummaryInformation());
        }
    }

    /**
     * <p>Tests the {@link Section} methods. The test file has two
     * property sets: the first one is a {@link SummaryInformation},
     * the second one is a {@link DocumentSummaryInformation}.</p>
     *
     * @exception IOException if an I/O exception occurs
     * @exception HPSFException if any HPSF exception occurs
     */
    @Test
    public void testSectionMethods() throws IOException, HPSFException {
        InputStream is = new ByteArrayInputStream(poiFiles.get(0).getBytes());
        final SummaryInformation si = (SummaryInformation)PropertySetFactory.create(is);
        final List<Section> sections = si.getSections();
        final Section s = sections.get(0);
        assertEquals(s.getFormatID(), SummaryInformation.FORMAT_ID);
        assertNotNull(s.getProperties());
        assertEquals(17, s.getPropertyCount());
        assertEquals("Titel", s.getProperty(PropertyIDMap.PID_TITLE));
        assertEquals(1764, s.getSize());
    }

    @Test
    public void bug52117LastPrinted() throws IOException, HPSFException {
        File f = samples.getFile("TestBug52117.doc");
        POIFile poiFile = Util.readPOIFiles(f, new String[]{POI_FILES[0]}).get(0);
        InputStream in = new ByteArrayInputStream(poiFile.getBytes());
        SummaryInformation si = (SummaryInformation)PropertySetFactory.create(in);
        Date lastPrinted = si.getLastPrinted();
        long editTime = si.getEditTime();
        assertTrue(Filetime.isUndefined(lastPrinted));
        assertEquals(1800000000L, editTime);
    }

    @Test
    public void bug61809() throws IOException, HPSFException {
        InputStream is_si = new ByteArrayInputStream(poiFiles.get(0).getBytes());
        final SummaryInformation si = (SummaryInformation)PropertySetFactory.create(is_si);
        final Section s_si = si.getSections().get(0);

        assertEquals("PID_TITLE", s_si.getPIDString(PropertyIDMap.PID_TITLE));
        assertEquals(PropertyIDMap.UNDEFINED, s_si.getPIDString(4711));

        InputStream is_dsi = new ByteArrayInputStream(poiFiles.get(1).getBytes());
        final DocumentSummaryInformation dsi = (DocumentSummaryInformation)PropertySetFactory.create(is_dsi);
        final Section s_dsi = dsi.getSections().get(0);

        assertEquals("PID_MANAGER", s_dsi.getPIDString(PropertyIDMap.PID_MANAGER));
    }
}
