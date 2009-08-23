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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.HPSFException;
import org.apache.poi.hpsf.MarkUnsupportedException;
import org.apache.poi.hpsf.NoPropertySetStreamException;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.PropertySetFactory;
import org.apache.poi.hpsf.Section;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hpsf.wellknown.SectionIDMap;
import org.apache.poi.POIDataSamples;

/**
 * <p>Tests the basic HPSF functionality.</p>
 *
 * @author Rainer Klute (klute@rainer-klute.de)
 */
public final class TestBasic extends TestCase {

    private static final String POI_FS = "TestGermanWord90.doc";
    private static final String[] POI_FILES = new String[]
        {
            "\005SummaryInformation",
            "\005DocumentSummaryInformation",
            "WordDocument",
            "\001CompObj",
            "1Table"
        };
    private static final int BYTE_ORDER = 0xfffe;
    private static final int FORMAT     = 0x0000;
    private static final int OS_VERSION = 0x00020A04;
    private static final byte[] CLASS_ID =
        {
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00
        };
    private static final int[] SECTION_COUNT =
        {1, 2};
    private static final boolean[] IS_SUMMARY_INFORMATION =
        {true, false};
    private static final boolean[] IS_DOCUMENT_SUMMARY_INFORMATION =
        {false, true};

    private POIFile[] poiFiles;


    /**
     * <p>Read a the test file from the "data" directory.</p>
     *
     * @exception FileNotFoundException if the file to be read does not exist.
     * @exception IOException if any other I/O exception occurs.
     */
    public void setUp() throws IOException
    {
        POIDataSamples samples = POIDataSamples.getHPSFInstance();
        final File data = samples.getFile(POI_FS);
        poiFiles = Util.readPOIFiles(data);
    }

    /**
     * <p>Checks the names of the files in the POI filesystem. They
     * are expected to be in a certain order.</p>
     */
    public void testReadFiles()
    {
        String[] expected = POI_FILES;
        for (int i = 0; i < expected.length; i++)
            Assert.assertEquals(poiFiles[i].getName(), expected[i]);
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
    public void testCreatePropertySets()
    throws UnsupportedEncodingException, IOException
    {
        Class[] expected = new Class[]
            {
                SummaryInformation.class,
                DocumentSummaryInformation.class,
                NoPropertySetStreamException.class,
                NoPropertySetStreamException.class,
                NoPropertySetStreamException.class
            };
        for (int i = 0; i < expected.length; i++)
        {
            InputStream in = new ByteArrayInputStream(poiFiles[i].getBytes());
            Object o;
            try
            {
                o = PropertySetFactory.create(in);
            }
            catch (NoPropertySetStreamException ex)
            {
                o = ex;
            }
            catch (MarkUnsupportedException ex)
            {
                o = ex;
            }
            in.close();
            Assert.assertEquals(expected[i], o.getClass());
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
    public void testPropertySetMethods() throws IOException, HPSFException
    {
        /* Loop over the two property sets. */
        for (int i = 0; i < 2; i++)
        {
            byte[] b = poiFiles[i].getBytes();
            PropertySet ps =
                PropertySetFactory.create(new ByteArrayInputStream(b));
            Assert.assertEquals(ps.getByteOrder(), BYTE_ORDER);
            Assert.assertEquals(ps.getFormat(), FORMAT);
            Assert.assertEquals(ps.getOSVersion(), OS_VERSION);
            Assert.assertEquals(new String(ps.getClassID().getBytes()),
                                new String(CLASS_ID));
            Assert.assertEquals(ps.getSectionCount(), SECTION_COUNT[i]);
            Assert.assertEquals(ps.isSummaryInformation(),
                                IS_SUMMARY_INFORMATION[i]);
            Assert.assertEquals(ps.isDocumentSummaryInformation(),
                                IS_DOCUMENT_SUMMARY_INFORMATION[i]);
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
    public void testSectionMethods() throws IOException, HPSFException
    {
        final SummaryInformation si = (SummaryInformation)
            PropertySetFactory.create(new ByteArrayInputStream
                (poiFiles[0].getBytes()));
        final List sections = si.getSections();
        final Section s = (Section) sections.get(0);
        Assert.assertTrue(org.apache.poi.hpsf.Util.equal
            (s.getFormatID().getBytes(), SectionIDMap.SUMMARY_INFORMATION_ID));
        Assert.assertNotNull(s.getProperties());
        Assert.assertEquals(17, s.getPropertyCount());
        Assert.assertEquals("Titel", s.getProperty(2));
        Assert.assertEquals(1748, s.getSize());
    }
}
