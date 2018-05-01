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
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.HPSFException;
import org.apache.poi.hpsf.MarkUnsupportedException;
import org.apache.poi.hpsf.NoPropertySetStreamException;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.PropertySetFactory;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hpsf.Variant;
import org.junit.Before;
import org.junit.Test;

/**
 * Test case for OLE2 files with empty properties.
 * An empty property's type is {@link Variant#VT_EMPTY}.
 */
public final class TestEmptyProperties {

    private static final POIDataSamples samples = POIDataSamples.getHPSFInstance();

    /**
     * This test file's summary information stream contains some empty properties.
     */
	private static final String POI_FS = "TestCorel.shw";

	private static final String[] POI_FILES = {
        "PerfectOffice_MAIN",
        SummaryInformation.DEFAULT_STREAM_NAME,
        "Main"
    };

	private List<POIFile> poiFiles;

    /**
     * <p>Read a the test file from the "data" directory.</p>
     *
     * @exception FileNotFoundException if the file containing the test data
     * does not exist
     * @exception IOException if an I/O exception occurs
     */
    @Before
    public void setUp() throws IOException {
        final File data = samples.getFile(POI_FS);
        poiFiles = Util.readPOIFiles(data);
    }

    /**
     * Checks the names of the files in the POI filesystem. They
     * are expected to be in a certain order.
     */
    @Test
    public void testReadFiles() {
        String[] expected = POI_FILES;
        for (int i = 0; i < expected.length; i++)
            assertEquals(poiFiles.get(i).getName(), expected[i]);
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
        Class<?>[] expected =  {
            NoPropertySetStreamException.class,
            SummaryInformation.class,
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
            assertEquals(o.getClass(), expected[i]);
        }
    }

    /**
     * <p>Tests the {@link PropertySet} methods. The test file has two
     * property sets: the first one is a {@link SummaryInformation},
     * the second one is a {@link DocumentSummaryInformation}.</p>
     *
     * @exception IOException if an I/O exception occurs
     * @exception HPSFException if an HPSF operation fails
     */
    @Test
    public void testPropertySetMethods() throws IOException, HPSFException {
        byte[] b = poiFiles.get(1).getBytes();
        PropertySet ps = PropertySetFactory.create(new ByteArrayInputStream(b));
        SummaryInformation s = (SummaryInformation) ps;
        assertNull(s.getTitle());
        assertNull(s.getSubject());
        assertNotNull(s.getAuthor());
        assertNull(s.getKeywords());
        assertNull(s.getComments());
        assertNotNull(s.getTemplate());
        assertNotNull(s.getLastAuthor());
        assertNotNull(s.getRevNumber());
        assertEquals(s.getEditTime(), 0);
        assertNull(s.getLastPrinted());
        assertNull(s.getCreateDateTime());
        assertNull(s.getLastSaveDateTime());
        assertEquals(s.getPageCount(), 0);
        assertEquals(s.getWordCount(), 0);
        assertEquals(s.getCharCount(), 0);
        assertNull(s.getThumbnail());
        assertNull(s.getApplicationName());
    }
}
