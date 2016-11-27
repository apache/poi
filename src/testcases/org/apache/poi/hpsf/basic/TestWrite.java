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

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hpsf.ClassID;
import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.HPSFException;
import org.apache.poi.hpsf.IllegalPropertySetDataException;
import org.apache.poi.hpsf.MutableProperty;
import org.apache.poi.hpsf.MutablePropertySet;
import org.apache.poi.hpsf.MutableSection;
import org.apache.poi.hpsf.NoFormatIDException;
import org.apache.poi.hpsf.NoPropertySetStreamException;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.PropertySetFactory;
import org.apache.poi.hpsf.ReadingNotSupportedException;
import org.apache.poi.hpsf.Section;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hpsf.UnsupportedVariantTypeException;
import org.apache.poi.hpsf.Variant;
import org.apache.poi.hpsf.VariantSupport;
import org.apache.poi.hpsf.WritingNotSupportedException;
import org.apache.poi.hpsf.wellknown.PropertyIDMap;
import org.apache.poi.hpsf.wellknown.SectionIDMap;
import org.apache.poi.poifs.eventfilesystem.POIFSReader;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderEvent;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderListener;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DocumentNode;
import org.apache.poi.poifs.filesystem.NDocumentInputStream;
import org.apache.poi.poifs.filesystem.NDocumentOutputStream;
import org.apache.poi.poifs.filesystem.NPOIFSDocument;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.CodePageUtil;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.TempFile;
import org.junit.Before;
import org.junit.Test;

/**
 * <p>Tests HPSF's writing functionality.</p>
 */
public class TestWrite
{
    private static final POIDataSamples _samples = POIDataSamples.getHPSFInstance();

    static final String POI_FS = "TestHPSFWritingFunctionality.doc";

    static final int BYTE_ORDER = 0xfffe;
    static final int FORMAT     = 0x0000;
    static final int OS_VERSION = 0x00020A04;
    static final int[] SECTION_COUNT = {1, 2};
    static final boolean[] IS_SUMMARY_INFORMATION = {true, false};
    static final boolean[] IS_DOCUMENT_SUMMARY_INFORMATION = {false, true};

    final String IMPROPER_DEFAULT_CHARSET_MESSAGE =
        "Your default character set is " + getDefaultCharsetName() +
        ". However, this testcase must be run in an environment " +
        "with a default character set supporting at least " +
        "8-bit-characters. You can achieve this by setting the " +
        "LANG environment variable to a proper value, e.g. " +
        "\"de_DE\".";

    POIFile[] poiFiles;

    @Before
    public void setUp()
    {
        VariantSupport.setLogUnsupportedTypes(false);
    }

    /**
     * <p>Writes an empty property set to a POIFS and reads it back
     * in.</p>
     *
     * @exception IOException if an I/O exception occurs
     */
    @Test(expected=NoFormatIDException.class)
    public void withoutAFormatID() throws Exception
    {
        final File filename = TempFile.createTempFile(POI_FS, ".doc");

        /* Create a mutable property set with a section that does not have the
         * formatID set: */
        final OutputStream out = new FileOutputStream(filename);
        final POIFSFileSystem poiFs = new POIFSFileSystem();
        final MutablePropertySet ps = new MutablePropertySet();
        ps.clearSections();
        ps.addSection(new MutableSection());

        /* Write it to a POIFS and the latter to disk: */
        try {
            final ByteArrayOutputStream psStream = new ByteArrayOutputStream();
            ps.write(psStream);
            psStream.close();
            final byte[] streamData = psStream.toByteArray();
            poiFs.createDocument(new ByteArrayInputStream(streamData),
                                 SummaryInformation.DEFAULT_STREAM_NAME);
            poiFs.writeFilesystem(out);
        } finally {
            poiFs.close();
            out.close();
        }
    }



    /**
     * <p>Writes an empty property set to a POIFS and reads it back
     * in.</p>
     *
     * @exception IOException if an I/O exception occurs
     * @exception UnsupportedVariantTypeException if HPSF does not yet support
     * a variant type to be written
     */
    @Test
    public void writeEmptyPropertySet()
        throws IOException, UnsupportedVariantTypeException
    {
        final File dataDir = _samples.getFile("");
        final File filename = new File(dataDir, POI_FS);
        filename.deleteOnExit();

        /* Create a mutable property set and write it to a POIFS: */
        final OutputStream out = new FileOutputStream(filename);
        final POIFSFileSystem poiFs = new POIFSFileSystem();
        final MutablePropertySet ps = new MutablePropertySet();
        final MutableSection s = (MutableSection) ps.getSections().get(0);
        s.setFormatID(SectionIDMap.SUMMARY_INFORMATION_ID);

        final ByteArrayOutputStream psStream = new ByteArrayOutputStream();
        ps.write(psStream);
        psStream.close();
        final byte[] streamData = psStream.toByteArray();
        poiFs.createDocument(new ByteArrayInputStream(streamData),
                             SummaryInformation.DEFAULT_STREAM_NAME);
        poiFs.writeFilesystem(out);
        poiFs.close();
        out.close();

        /* Read the POIFS: */
        final POIFSReader r = new POIFSReader();
        r.registerListener(new MyPOIFSReaderListener(),
                           SummaryInformation.DEFAULT_STREAM_NAME);
        FileInputStream stream = new FileInputStream(filename);
        try {
            r.read(stream);
        } finally {
            stream.close();
        }
    }



    /**
     * <p>Writes a simple property set with a SummaryInformation section to a
     * POIFS and reads it back in.</p>
     *
     * @exception IOException if an I/O exception occurs
     * @exception UnsupportedVariantTypeException if HPSF does not yet support
     * a variant type to be written
     */
    @Test
    public void writeSimplePropertySet()
        throws IOException, UnsupportedVariantTypeException
    {
        final String AUTHOR = "Rainer Klute";
        final String TITLE = "Test Document";
        final File dataDir = _samples.getFile("");
        final File filename = new File(dataDir, POI_FS);
        filename.deleteOnExit();
        final OutputStream out = new FileOutputStream(filename);
        final POIFSFileSystem poiFs = new POIFSFileSystem();

        final MutablePropertySet ps = new MutablePropertySet();
        final MutableSection si = new MutableSection();
        si.setFormatID(SectionIDMap.SUMMARY_INFORMATION_ID);
        ps.clearSections();
        ps.addSection(si);

        final MutableProperty p = new MutableProperty();
        p.setID(PropertyIDMap.PID_AUTHOR);
        p.setType(Variant.VT_LPWSTR);
        p.setValue(AUTHOR);
        si.setProperty(p);
        si.setProperty(PropertyIDMap.PID_TITLE, Variant.VT_LPSTR, TITLE);

        poiFs.createDocument(ps.toInputStream(),
                             SummaryInformation.DEFAULT_STREAM_NAME);
        poiFs.writeFilesystem(out);
        poiFs.close();
        out.close();

        /* Read the POIFS: */
        final PropertySet[] psa = new PropertySet[1];
        final POIFSReader r = new POIFSReader();
        r.registerListener(new POIFSReaderListener()
            {
                @Override
                public void processPOIFSReaderEvent
                    (final POIFSReaderEvent event)
                {
                    try
                    {
                        psa[0] = PropertySetFactory.create(event.getStream());
                    }
                    catch (Exception ex)
                    {
                        fail(org.apache.poi.hpsf.Util.toString(ex));
                    }
                }

            },
            SummaryInformation.DEFAULT_STREAM_NAME);
        
        InputStream stream = new FileInputStream(filename);
        try {
            r.read(stream);
        } finally {
            stream.close();
        }
        assertNotNull(psa[0]);
        assertTrue(psa[0].isSummaryInformation());

        final Section s = (psa[0].getSections().get(0));
        Object p1 = s.getProperty(PropertyIDMap.PID_AUTHOR);
        Object p2 = s.getProperty(PropertyIDMap.PID_TITLE);
        assertEquals(AUTHOR, p1);
        assertEquals(TITLE, p2);
    }



    /**
     * <p>Writes a simple property set with two sections to a POIFS and reads it
     * back in.</p>
     *
     * @exception IOException if an I/O exception occurs
     * @exception WritingNotSupportedException if HPSF does not yet support
     * a variant type to be written
     */
    @Test
    public void writeTwoSections()
        throws WritingNotSupportedException, IOException
    {
        final String STREAM_NAME = "PropertySetStream";
        final String SECTION1 = "Section 1";
        final String SECTION2 = "Section 2";

        final File dataDir = _samples.getFile("");
        final File filename = new File(dataDir, POI_FS);
        filename.deleteOnExit();
        final OutputStream out = new FileOutputStream(filename);

        final POIFSFileSystem poiFs = new POIFSFileSystem();
        final MutablePropertySet ps = new MutablePropertySet();
        ps.clearSections();

        final ClassID formatID = new ClassID();
        formatID.setBytes(new byte[]{0, 1,  2,  3,  4,  5,  6,  7,
                                     8, 9, 10, 11, 12, 13, 14, 15});
        final MutableSection s1 = new MutableSection();
        s1.setFormatID(formatID);
        s1.setProperty(2, SECTION1);
        ps.addSection(s1);

        final MutableSection s2 = new MutableSection();
        s2.setFormatID(formatID);
        s2.setProperty(2, SECTION2);
        ps.addSection(s2);

        poiFs.createDocument(ps.toInputStream(), STREAM_NAME);
        poiFs.writeFilesystem(out);
        poiFs.close();
        out.close();

        /* Read the POIFS: */
        final PropertySet[] psa = new PropertySet[1];
        final POIFSReader r = new POIFSReader();
        r.registerListener(new POIFSReaderListener()
            {
                @Override
                public void processPOIFSReaderEvent
                    (final POIFSReaderEvent event)
                {
                    try
                    {
                        psa[0] = PropertySetFactory.create(event.getStream());
                    }
                    catch (Exception ex)
                    {
                        throw new RuntimeException(ex);
                    }
                }
            },
            STREAM_NAME);
        FileInputStream stream = new FileInputStream(filename);
        try {
            r.read(stream);
        } finally {
            stream.close();
        }
        assertNotNull(psa[0]);
        Section s = (psa[0].getSections().get(0));
        assertEquals(s.getFormatID(), formatID);
        Object p = s.getProperty(2);
        assertEquals(SECTION1, p);
        s = (psa[0].getSections().get(1));
        p = s.getProperty(2);
        assertEquals(SECTION2, p);
    }



    static class MyPOIFSReaderListener implements POIFSReaderListener
    {
        @Override
        public void processPOIFSReaderEvent(final POIFSReaderEvent event)
        {
            try
            {
                PropertySetFactory.create(event.getStream());
            }
            catch (Exception ex)
            {
                fail(org.apache.poi.hpsf.Util.toString(ex));
            }
        }
    }



    private static final int CODEPAGE_DEFAULT = -1;
    private static final int CODEPAGE_1252 = 1252;
    private static final int CODEPAGE_UTF8 = CodePageUtil.CP_UTF8;
    private static final int CODEPAGE_UTF16 = CodePageUtil.CP_UTF16;



    /**
     * <p>Writes and reads back various variant types and checks whether the
     * stuff that has been read back equals the stuff that was written.</p>
     */
    @Test
    public void variantTypes()
    {
        Throwable t = null;
        final int codepage = CODEPAGE_DEFAULT;
        if (!hasProperDefaultCharset())
        {
            System.err.println(IMPROPER_DEFAULT_CHARSET_MESSAGE +
                " This testcase is skipped.");
            return;
        }

        try
        {
            check(Variant.VT_EMPTY, null, codepage);
            check(Variant.VT_BOOL, Boolean.TRUE, codepage);
            check(Variant.VT_BOOL, Boolean.FALSE, codepage);
            check( Variant.VT_CF, new byte[] { 8, 0, 0, 0, 1, 0, 0, 0, 1, 2, 3,
                    4 }, codepage );
            check(Variant.VT_I4, Integer.valueOf(27), codepage);
            check(Variant.VT_I8, Long.valueOf(28), codepage);
            check(Variant.VT_R8, new Double(29.0), codepage);
            check(Variant.VT_I4, Integer.valueOf(-27), codepage);
            check(Variant.VT_I8, Long.valueOf(-28), codepage);
            check(Variant.VT_R8, new Double(-29.0), codepage);
            check(Variant.VT_FILETIME, new Date(), codepage);
            check(Variant.VT_I4, new Integer(Integer.MAX_VALUE), codepage);
            check(Variant.VT_I4, new Integer(Integer.MIN_VALUE), codepage);
            check(Variant.VT_I8, new Long(Long.MAX_VALUE), codepage);
            check(Variant.VT_I8, new Long(Long.MIN_VALUE), codepage);
            check(Variant.VT_R8, new Double(Double.MAX_VALUE), codepage);
            check(Variant.VT_R8, new Double(Double.MIN_VALUE), codepage);

            check(Variant.VT_LPSTR,
                  "", codepage);
            check(Variant.VT_LPSTR,
                  "\u00e4", codepage);
            check(Variant.VT_LPSTR,
                  "\u00e4\u00f6", codepage);
            check(Variant.VT_LPSTR,
                  "\u00e4\u00f6\u00fc", codepage);
            check(Variant.VT_LPSTR,
                  "\u00e4\u00f6\u00fc\u00df", codepage);
            check(Variant.VT_LPSTR,
                  "\u00e4\u00f6\u00fc\u00df\u00c4", codepage);
            check(Variant.VT_LPSTR,
                  "\u00e4\u00f6\u00fc\u00df\u00c4\u00d6", codepage);
            check(Variant.VT_LPSTR,
                  "\u00e4\u00f6\u00fc\u00df\u00c4\u00d6\u00dc", codepage);

            check(Variant.VT_LPWSTR,
                  "", codepage);
            check(Variant.VT_LPWSTR,
                  "\u00e4", codepage);
            check(Variant.VT_LPWSTR,
                  "\u00e4\u00f6", codepage);
            check(Variant.VT_LPWSTR,
                  "\u00e4\u00f6\u00fc", codepage);
            check(Variant.VT_LPWSTR,
                  "\u00e4\u00f6\u00fc\u00df", codepage);
            check(Variant.VT_LPWSTR,
                  "\u00e4\u00f6\u00fc\u00df\u00c4", codepage);
            check(Variant.VT_LPWSTR,
                  "\u00e4\u00f6\u00fc\u00df\u00c4\u00d6", codepage);
            check(Variant.VT_LPWSTR,
                  "\u00e4\u00f6\u00fc\u00df\u00c4\u00d6\u00dc", codepage);
        }
        catch (Exception ex)
        {
            t = ex;
        }
        catch (Error ex)
        {
            t = ex;
        }
        if (t != null)
            fail(org.apache.poi.hpsf.Util.toString(t));
    }



    /**
     * <p>Writes and reads back strings using several different codepages and
     * checks whether the stuff that has been read back equals the stuff that
     * was written.</p>
     */
    @Test
    public void codepages()
    {
        Throwable thr = null;
        final int[] validCodepages = new int[]
            {CODEPAGE_DEFAULT, CODEPAGE_UTF8, CODEPAGE_UTF16, CODEPAGE_1252};
        for (final int cp : validCodepages) {
            if (cp == -1 && !hasProperDefaultCharset())
            {
                System.err.println(IMPROPER_DEFAULT_CHARSET_MESSAGE +
                     " This testcase is skipped for the default codepage.");
                continue;
            }

            final long t = cp == CODEPAGE_UTF16 ? Variant.VT_LPWSTR
                                                : Variant.VT_LPSTR;
            try
            {
                check(t, "", cp);
                check(t, "\u00e4", cp);
                check(t, "\u00e4\u00f6", cp);
                check(t, "\u00e4\u00f6\u00fc", cp);
                check(t, "\u00e4\u00f6\u00fc\u00c4", cp);
                check(t, "\u00e4\u00f6\u00fc\u00c4\u00d6", cp);
                check(t, "\u00e4\u00f6\u00fc\u00c4\u00d6\u00dc", cp);
                check(t, "\u00e4\u00f6\u00fc\u00c4\u00d6\u00dc\u00df", cp);
                if (cp == CodePageUtil.CP_UTF16 || cp == CodePageUtil.CP_UTF8)
                    check(t, "\u79D1\u5B78", cp);
            }
            catch (Exception ex)
            {
                thr = ex;
            }
            catch (Error ex)
            {
                thr = ex;
            }
            if (thr != null)
                fail(org.apache.poi.hpsf.Util.toString(thr) +
                     " with codepage " + cp);
        }

        final int[] invalidCodepages = new int[] {0, 1, 2, 4711, 815};
        for (int cp : invalidCodepages) {
            final long type = cp == CODEPAGE_UTF16 ? Variant.VT_LPWSTR
                                                   : Variant.VT_LPSTR;
            try
            {
                check(type, "", cp);
                check(type, "\u00e4", cp);
                check(type, "\u00e4\u00f6", cp);
                check(type, "\u00e4\u00f6\u00fc", cp);
                check(type, "\u00e4\u00f6\u00fc\u00c4", cp);
                check(type, "\u00e4\u00f6\u00fc\u00c4\u00d6", cp);
                check(type, "\u00e4\u00f6\u00fc\u00c4\u00d6\u00dc", cp);
                check(type, "\u00e4\u00f6\u00fc\u00c4\u00d6\u00dc\u00df", cp);
                fail("UnsupportedEncodingException for codepage " + cp +
                     " expected.");
            }
            catch (UnsupportedEncodingException ex)
            {
                /* This is the expected behaviour. */
            }
            catch (Exception ex)
            {
                thr = ex;
            }
            catch (Error ex)
            {
                thr = ex;
            }
            if (thr != null)
                fail(org.apache.poi.hpsf.Util.toString(thr));
        }

    }



    /**
     * <p>Tests whether writing 8-bit characters to a Unicode property
     * succeeds.</p>
     */
    @Test
    public void unicodeWrite8Bit()
    {
        final String TITLE = "This is a sample title";
        final MutablePropertySet mps = new MutablePropertySet();
        final MutableSection ms = (MutableSection) mps.getSections().get(0);
        ms.setFormatID(SectionIDMap.SUMMARY_INFORMATION_ID);
        final MutableProperty p = new MutableProperty();
        p.setID(PropertyIDMap.PID_TITLE);
        p.setType(Variant.VT_LPSTR);
        p.setValue(TITLE);
        ms.setProperty(p);

        Throwable t = null;
        try
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            mps.write(out);
            out.close();
            byte[] bytes = out.toByteArray();

            PropertySet psr = new PropertySet(bytes);
            assertTrue(psr.isSummaryInformation());
            Section sr = psr.getSections().get(0);
            String title = (String) sr.getProperty(PropertyIDMap.PID_TITLE);
            assertEquals(TITLE, title);
        }
        catch (WritingNotSupportedException e)
        {
            t = e;
        }
        catch (IOException e)
        {
            t = e;
        }
        catch (NoPropertySetStreamException e)
        {
            t = e;
        }
        if (t != null)
            fail(t.getMessage());
    }



    /**
     * <p>Writes a property and reads it back in.</p>
     *
     * @param variantType The property's variant type.
     * @param value The property's value.
     * @param codepage The codepage to use for writing and reading.
     * @throws UnsupportedVariantTypeException if the variant is not supported.
     * @throws IOException if an I/O exception occurs.
     * @throws ReadingNotSupportedException
     * @throws UnsupportedEncodingException
     */
    private void check(final long variantType, final Object value,
                       final int codepage)
        throws UnsupportedVariantTypeException, IOException,
               ReadingNotSupportedException, UnsupportedEncodingException
    {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        VariantSupport.write(out, variantType, value, codepage);
        out.close();
        final byte[] b = out.toByteArray();
        final Object objRead =
            VariantSupport.read(b, 0, b.length + LittleEndian.INT_SIZE,
                                variantType, codepage);
        if (objRead instanceof byte[])
        {
            byte[] valueB = (byte[])value;
            byte[] readB = (byte[])objRead;
            if (valueB.length != readB.length)
               fail("Byte arrays are different length - expected " + valueB.length +
                     " but found " + readB.length);

            final int diff = diff(valueB, readB);
            if (diff >= 0)
                fail("Byte arrays are different. First different byte is at " +
                     "index " + diff + ".");
        }
        else
            if (value != null && !value.equals(objRead))
            {
                fail("Expected: \"" + value + "\" but was: \"" + objRead +
                     "\". Codepage: " + codepage +
                     (codepage == -1 ?
                      " (" + System.getProperty("file.encoding") + ")." : "."));
            }
            else
                assertEquals(value, objRead);
    }



    /**
     * <p>Compares two byte arrays.</p>
     *
     * @param a The first byte array
     * @param b The second byte array
     * @return The index of the first byte that is different. If the byte arrays
     * are equal, -1 is returned.
     */
    private int diff(final byte[] a, final byte[] b)
    {
        final int min = Math.min(a.length, b.length);
        for (int i = 0; i < min; i++)
            if (a[i] != b[i])
                return i;
        if (a.length != b.length)
            return min;
        return -1;
    }



    /**
     * <p>This test method does a write and read back test with all POI
     * filesystems in the "data" directory by performing the following
     * actions for each file:</p>
     *
     * <ul>
     *
     * <li><p>Read its property set streams.</p></li>
     *
     * <li><p>Create a new POI filesystem containing the origin file's
     * property set streams.</p></li>
     *
     * <li><p>Read the property set streams from the POI filesystem just
     * created.</p></li>
     *
     * <li><p>Compare each property set stream with the corresponding one from
     * the origin file and check whether they are equal.</p></li>
     *
     * </ul>
     * @throws IOException 
     */
    @Test
    public void recreate() throws IOException
    {
        final File dataDir = _samples.getFile("");
        final File[] fileList = dataDir.listFiles(new FileFilter()
            {
                @Override
                public boolean accept(final File f)
                {
                    return f.getName().startsWith("Test") && TestReadAllFiles.checkExclude(f);
                }
            });
        for (final File file : fileList) {
            try {
                testRecreate(file);
            } catch (Exception e) {
                throw new IOException("While handling file " + file, e);
            }
        }
    }



    /**
     * <p>Performs the check described in {@link #recreate()} for a single
     * POI filesystem.</p>
     *
     * @param f the POI filesystem to check
     * @throws IOException 
     * @throws HPSFException
     */
    private void testRecreate(final File f) throws IOException, HPSFException
    {
        /* Read the POI filesystem's property set streams: */
        final POIFile[] psf1 = Util.readPropertySets(f);

        /* Create a new POI filesystem containing the origin file's
         * property set streams: */
        final File copy = TempFile.createTempFile(f.getName(), "");
        copy.deleteOnExit();
        final OutputStream out = new FileOutputStream(copy);
        final POIFSFileSystem poiFs = new POIFSFileSystem();
        for (POIFile file : psf1) {
            final InputStream in =
                new ByteArrayInputStream(file.getBytes());
            final PropertySet psIn = PropertySetFactory.create(in);
            final MutablePropertySet psOut = new MutablePropertySet(psIn);
            final ByteArrayOutputStream psStream =
                new ByteArrayOutputStream();
            psOut.write(psStream);
            psStream.close();
            final byte[] streamData = psStream.toByteArray();
            poiFs.createDocument(new ByteArrayInputStream(streamData),
                                 file.getName());
            poiFs.writeFilesystem(out);
        }
        poiFs.close();
        out.close();


        /* Read the property set streams from the POI filesystem just
         * created. */
        final POIFile[] psf2 = Util.readPropertySets(copy);
        for (int i = 0; i < psf2.length; i++)
        {
            final byte[] bytes1 = psf1[i].getBytes();
            final byte[] bytes2 = psf2[i].getBytes();
            final InputStream in1 = new ByteArrayInputStream(bytes1);
            final InputStream in2 = new ByteArrayInputStream(bytes2);
            final PropertySet ps1 = PropertySetFactory.create(in1);
            final PropertySet ps2 = PropertySetFactory.create(in2);

            /* Compare the property set stream with the corresponding one
             * from the origin file and check whether they are equal. */
            assertEquals("Equality for file " + f.getName(), ps1, ps2);
        }
    }



    /**
     * <p>Tests writing and reading back a proper dictionary.</p>
     * @throws IOException 
     * @throws HPSFException 
     */
    @Test
    public void dictionary() throws IOException, HPSFException
    {
        final File copy = TempFile.createTempFile("Test-HPSF", "ole2");
        copy.deleteOnExit();

        /* Write: */
        final OutputStream out = new FileOutputStream(copy);
        final POIFSFileSystem poiFs = new POIFSFileSystem();
        final MutablePropertySet ps1 = new MutablePropertySet();
        final MutableSection s = (MutableSection) ps1.getSections().get(0);
        final Map<Long,String> m = new HashMap<Long,String>(3, 1.0f);
        m.put(Long.valueOf(1), "String 1");
        m.put(Long.valueOf(2), "String 2");
        m.put(Long.valueOf(3), "String 3");
        s.setDictionary(m);
        s.setFormatID(SectionIDMap.DOCUMENT_SUMMARY_INFORMATION_ID[0]);
        int codepage = CodePageUtil.CP_UNICODE;
        s.setProperty(PropertyIDMap.PID_CODEPAGE, Variant.VT_I2,
                      Integer.valueOf(codepage));
        poiFs.createDocument(ps1.toInputStream(), "Test");
        poiFs.writeFilesystem(out);
        poiFs.close();
        out.close();

        /* Read back: */
        final POIFile[] psf = Util.readPropertySets(copy);
        assertEquals(1, psf.length);
        final byte[] bytes = psf[0].getBytes();
        final InputStream in = new ByteArrayInputStream(bytes);
        final PropertySet ps2 = PropertySetFactory.create(in);

        /* Check if the result is a DocumentSummaryInformation stream, as
         * specified. */
        assertTrue(ps2.isDocumentSummaryInformation());

        /* Compare the property set stream with the corresponding one
         * from the origin file and check whether they are equal. */
        assertEquals(ps1, ps2);
    }

    /**
     * Tests that when using NPOIFS, we can do an in-place write
     *  without needing to stream in + out the whole kitchen sink
     */
    @Test
    public void inPlaceNPOIFSWrite() throws Exception {
        NPOIFSFileSystem fs = null;
        DirectoryEntry root = null;
        DocumentNode sinfDoc = null;
        DocumentNode dinfDoc = null;
        SummaryInformation sinf = null;
        DocumentSummaryInformation dinf = null;
        
        // We need to work on a File for in-place changes, so create a temp one
        final File copy = TempFile.createTempFile("Test-HPSF", "ole2");
        copy.deleteOnExit();
        
        // Copy a test file over to our temp location
        InputStream inp = _samples.openResourceAsStream("TestShiftJIS.doc");
        FileOutputStream out = new FileOutputStream(copy);
        IOUtils.copy(inp, out);
        inp.close();
        out.close();
        
        
        // Open the copy in read/write mode
        fs = new NPOIFSFileSystem(copy, false);
        root = fs.getRoot();
        
        
        // Read the properties in there
        sinfDoc = (DocumentNode)root.getEntry(SummaryInformation.DEFAULT_STREAM_NAME);
        dinfDoc = (DocumentNode)root.getEntry(DocumentSummaryInformation.DEFAULT_STREAM_NAME);

        InputStream sinfStream = new NDocumentInputStream(sinfDoc);
        sinf = (SummaryInformation)PropertySetFactory.create(sinfStream);
        sinfStream.close();
        assertEquals(131077, sinf.getOSVersion());
        
        InputStream dinfStream = new NDocumentInputStream(dinfDoc);
        dinf = (DocumentSummaryInformation)PropertySetFactory.create(dinfStream);
        dinfStream.close();
        assertEquals(131077, dinf.getOSVersion());
        
        
        // Check they start as we expect
        assertEquals("Reiichiro Hori", sinf.getAuthor());
        assertEquals("Microsoft Word 9.0", sinf.getApplicationName());
        assertEquals("\u7b2c1\u7ae0", sinf.getTitle());
        
        assertEquals("", dinf.getCompany());
        assertEquals(null, dinf.getManager());
        
        
        // Do an in-place replace via an InputStream
        new NPOIFSDocument(sinfDoc).replaceContents(sinf.toInputStream());
        new NPOIFSDocument(dinfDoc).replaceContents(dinf.toInputStream());
        
        
        // Check it didn't get changed
        sinfDoc = (DocumentNode)root.getEntry(SummaryInformation.DEFAULT_STREAM_NAME);
        dinfDoc = (DocumentNode)root.getEntry(DocumentSummaryInformation.DEFAULT_STREAM_NAME);
        
        InputStream sinfStream2 = new NDocumentInputStream(sinfDoc);
        sinf = (SummaryInformation)PropertySetFactory.create(sinfStream2);
        sinfStream2.close();
        assertEquals(131077, sinf.getOSVersion());
        
        InputStream dinfStream2 = new NDocumentInputStream(dinfDoc);
        dinf = (DocumentSummaryInformation)PropertySetFactory.create(dinfStream2);
        dinfStream2.close();
        assertEquals(131077, dinf.getOSVersion());

        
        // Start again!
        fs.close();
        inp = _samples.openResourceAsStream("TestShiftJIS.doc");
        out = new FileOutputStream(copy);
        IOUtils.copy(inp, out);
        inp.close();
        out.close();
        
        fs = new NPOIFSFileSystem(copy, false);
        root = fs.getRoot();
        
        // Read the properties in once more
        sinfDoc = (DocumentNode)root.getEntry(SummaryInformation.DEFAULT_STREAM_NAME);
        dinfDoc = (DocumentNode)root.getEntry(DocumentSummaryInformation.DEFAULT_STREAM_NAME);

        InputStream sinfStream3 = new NDocumentInputStream(sinfDoc);
        sinf = (SummaryInformation)PropertySetFactory.create(sinfStream3);
        sinfStream3.close();
        assertEquals(131077, sinf.getOSVersion());
        
        InputStream dinfStream3 = new NDocumentInputStream(dinfDoc);
        dinf = (DocumentSummaryInformation)PropertySetFactory.create(dinfStream3);
        dinfStream3.close();
        assertEquals(131077, dinf.getOSVersion());
        
        
        // Have them write themselves in-place with no changes, as an OutputStream
        OutputStream soufStream = new NDocumentOutputStream(sinfDoc);
        sinf.write(soufStream);
        soufStream.close();
        OutputStream doufStream = new NDocumentOutputStream(dinfDoc);
        dinf.write(doufStream);
        doufStream.close();
        
        // And also write to some bytes for checking
        ByteArrayOutputStream sinfBytes = new ByteArrayOutputStream();
        sinf.write(sinfBytes);
        ByteArrayOutputStream dinfBytes = new ByteArrayOutputStream();
        dinf.write(dinfBytes);
        
        
        // Check that the filesystem can give us back the same bytes
        sinfDoc = (DocumentNode)root.getEntry(SummaryInformation.DEFAULT_STREAM_NAME);
        dinfDoc = (DocumentNode)root.getEntry(DocumentSummaryInformation.DEFAULT_STREAM_NAME);

        InputStream sinfStream4 = new NDocumentInputStream(sinfDoc);
        byte[] sinfData = IOUtils.toByteArray(sinfStream4);
        sinfStream4.close();
        InputStream dinfStream4 = new NDocumentInputStream(dinfDoc);
        byte[] dinfData = IOUtils.toByteArray(dinfStream4);
        dinfStream4.close();
        assertThat(sinfBytes.toByteArray(), equalTo(sinfData));
        assertThat(dinfBytes.toByteArray(), equalTo(dinfData));

        
        // Read back in as-is
        InputStream sinfStream5 = new NDocumentInputStream(sinfDoc);
        sinf = (SummaryInformation)PropertySetFactory.create(sinfStream5);
        sinfStream5.close();
        assertEquals(131077, sinf.getOSVersion());
        
        InputStream dinfStream5 = new NDocumentInputStream(dinfDoc);
        dinf = (DocumentSummaryInformation)PropertySetFactory.create(dinfStream5);
        dinfStream5.close();
        assertEquals(131077, dinf.getOSVersion());
        
        assertEquals("Reiichiro Hori", sinf.getAuthor());
        assertEquals("Microsoft Word 9.0", sinf.getApplicationName());
        assertEquals("\u7b2c1\u7ae0", sinf.getTitle());
        
        assertEquals("", dinf.getCompany());
        assertEquals(null, dinf.getManager());
        

        // Now alter a few of them
        sinf.setAuthor("Changed Author");
        sinf.setTitle("Le titre \u00e9tait chang\u00e9");
        dinf.setManager("Changed Manager");
        
        
        // Save this into the filesystem
        OutputStream soufStream2 = new NDocumentOutputStream(sinfDoc);
        sinf.write(soufStream2);
        soufStream2.close();
        OutputStream doufStream2 = new NDocumentOutputStream(dinfDoc);
        dinf.write(doufStream2);
        doufStream2.close();
        
        
        // Read them back in again
        sinfDoc = (DocumentNode)root.getEntry(SummaryInformation.DEFAULT_STREAM_NAME);
        InputStream sinfStream6 = new NDocumentInputStream(sinfDoc);
        sinf = (SummaryInformation)PropertySetFactory.create(sinfStream6);
        sinfStream6.close();
        assertEquals(131077, sinf.getOSVersion());
        
        dinfDoc = (DocumentNode)root.getEntry(DocumentSummaryInformation.DEFAULT_STREAM_NAME);
        InputStream dinfStream6 = new NDocumentInputStream(dinfDoc);
        dinf = (DocumentSummaryInformation)PropertySetFactory.create(dinfStream6);
        dinfStream6.close();
        assertEquals(131077, dinf.getOSVersion());

        assertEquals("Changed Author", sinf.getAuthor());
        assertEquals("Microsoft Word 9.0", sinf.getApplicationName());
        assertEquals("Le titre \u00e9tait chang\u00e9", sinf.getTitle());
        
        assertEquals("", dinf.getCompany());
        assertEquals("Changed Manager", dinf.getManager());

        
        // Close the whole filesystem, and open it once more
        fs.writeFilesystem();
        fs.close();
        
        fs = new NPOIFSFileSystem(copy);
        root = fs.getRoot();
        
        // Re-check on load
        sinfDoc = (DocumentNode)root.getEntry(SummaryInformation.DEFAULT_STREAM_NAME);
        InputStream sinfStream7 = new NDocumentInputStream(sinfDoc);
        sinf = (SummaryInformation)PropertySetFactory.create(sinfStream7);
        sinfStream7.close();
        assertEquals(131077, sinf.getOSVersion());
        
        dinfDoc = (DocumentNode)root.getEntry(DocumentSummaryInformation.DEFAULT_STREAM_NAME);
        InputStream dinfStream7 = new NDocumentInputStream(dinfDoc);
        dinf = (DocumentSummaryInformation)PropertySetFactory.create(dinfStream7);
        dinfStream7.close();
        assertEquals(131077, dinf.getOSVersion());

        assertEquals("Changed Author", sinf.getAuthor());
        assertEquals("Microsoft Word 9.0", sinf.getApplicationName());
        assertEquals("Le titre \u00e9tait chang\u00e9", sinf.getTitle());
        
        assertEquals("", dinf.getCompany());
        assertEquals("Changed Manager", dinf.getManager());
        
        
        // Tidy up
        fs.close();
        copy.delete();
    }


    /**
     * <p>Tests writing and reading back a proper dictionary with an invalid
     * codepage. (HPSF writes Unicode dictionaries only.)</p>
     * @throws IOException 
     * @throws HPSFException 
     */
    @Test(expected=IllegalPropertySetDataException.class)
    public void dictionaryWithInvalidCodepage() throws IOException, HPSFException
    {
        final File copy = TempFile.createTempFile("Test-HPSF", "ole2");
        copy.deleteOnExit();
        
        /* Write: */
        final OutputStream out = new FileOutputStream(copy);
        
        final POIFSFileSystem poiFs = new POIFSFileSystem();
        final MutablePropertySet ps1 = new MutablePropertySet();
        final MutableSection s = (MutableSection) ps1.getSections().get(0);
        final Map<Long,String> m = new HashMap<Long, String>(3, 1.0f);
        m.put(Long.valueOf(1), "String 1");
        m.put(Long.valueOf(2), "String 2");
        m.put(Long.valueOf(3), "String 3");

        try {
            s.setDictionary(m);
            s.setFormatID(SectionIDMap.DOCUMENT_SUMMARY_INFORMATION_ID[0]);
            int codepage = 12345;
            s.setProperty(PropertyIDMap.PID_CODEPAGE, Variant.VT_I2,
                          Integer.valueOf(codepage));
            poiFs.createDocument(ps1.toInputStream(), "Test");
            poiFs.writeFilesystem(out);
        } finally {
            poiFs.close();
            out.close();
        }
    }



    /**
     * <p>Returns the display name of the default character set.</p>
     *
     * @return the display name of the default character set.
     */
    private String getDefaultCharsetName()
    {
        final String charSetName = System.getProperty("file.encoding");
        final Charset charSet = Charset.forName(charSetName);
        return charSet.displayName(Locale.ROOT);
    }



    /**
     * <p>In order to execute tests with characters beyond US-ASCII, this
     * method checks whether the application is runing in an environment
     * where the default character set is 16-bit-capable.</p>
     *
     * @return <code>true</code> if the default character set is 16-bit-capable,
     * else <code>false</code>.
     */
    private boolean hasProperDefaultCharset()
    {
        final String charSetName = System.getProperty("file.encoding");
        final Charset charSet = Charset.forName(charSetName);
        return charSet.newEncoder().canEncode('\u00e4');
    }
}
