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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hpsf.ClassID;
import org.apache.poi.hpsf.ClassIDPredefined;
import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.HPSFException;
import org.apache.poi.hpsf.NoFormatIDException;
import org.apache.poi.hpsf.NoPropertySetStreamException;
import org.apache.poi.hpsf.Property;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.PropertySetFactory;
import org.apache.poi.hpsf.Section;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hpsf.UnsupportedVariantTypeException;
import org.apache.poi.hpsf.Variant;
import org.apache.poi.hpsf.VariantSupport;
import org.apache.poi.hpsf.WritingNotSupportedException;
import org.apache.poi.hpsf.wellknown.PropertyIDMap;
import org.apache.poi.poifs.eventfilesystem.POIFSReader;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderListener;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.DocumentNode;
import org.apache.poi.poifs.filesystem.DocumentOutputStream;
import org.apache.poi.poifs.filesystem.POIFSDocument;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.CodePageUtil;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.TempFile;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

/**
 * Tests HPSF's writing functionality
 */
class TestWrite {
    private static final POIDataSamples _samples = POIDataSamples.getHPSFInstance();
    private static final int CODEPAGE_DEFAULT = -1;

    private static final String POI_FS = "TestHPSFWritingFunctionality.doc";

    private static final String IMPROPER_DEFAULT_CHARSET_MESSAGE =
        "Your default character set is " + getDefaultCharsetName() +
        ". However, this testcase must be run in an environment " +
        "with a default character set supporting at least " +
        "8-bit-characters. You can achieve this by setting the " +
        "LANG environment variable to a proper value, e.g. " +
        "\"de_DE\".";

    /*
    private static String loggerBefore;

    @BeforeClass
    public static void setUpClass() {
        loggerBefore = System.getProperty("org.apache.poi.util.POILogger");

        // this test may fails in newer JDKs because of disallowed access if
        // properties are missing, make this visible
        System.setProperty("org.apache.poi.util.POILogger", CommonsLogger.class.getName());

        VariantSupport.setLogUnsupportedTypes(false);
    }

    @AfterClass
    public static void tearDownClass() {
        if(loggerBefore == null) {
            System.clearProperty("org.apache.poi.util.POILogger");
        } else {
            System.setProperty("org.apache.poi.util.POILogger", loggerBefore);
        }
    }
     */

    /**
     * Writes an empty property set to a POIFS and reads it back in.
     *
     * @exception IOException if an I/O exception occurs
     */
    @Test
    void withoutAFormatID() throws Exception {
        final File filename = TempFile.createTempFile(POI_FS, ".doc");

        /* Create a mutable property set with a section that does not have the
         * formatID set: */
        final PropertySet ps = new PropertySet();
        ps.clearSections();
        ps.addSection(new Section());

        /* Write it to a POIFS and the latter to disk: */
        try (OutputStream out = new FileOutputStream(filename);
             POIFSFileSystem poiFs = new POIFSFileSystem();
             ByteArrayOutputStream psStream = new ByteArrayOutputStream()) {
            assertThrows(NoFormatIDException.class, () -> ps.write(psStream));
            poiFs.createDocument(new ByteArrayInputStream(psStream.toByteArray()), SummaryInformation.DEFAULT_STREAM_NAME);
            poiFs.writeFilesystem(out);
        }
    }

    /**
     * Writes an empty property set to a POIFS and reads it back in.
     *
     * @exception IOException if an I/O exception occurs
     * @exception UnsupportedVariantTypeException if HPSF does not yet support
     * a variant type to be written
     */
    @Test
    void writeEmptyPropertySet()
    throws IOException, UnsupportedVariantTypeException {
        final File dataDir = _samples.getFile("");
        final File filename = new File(dataDir, POI_FS);
        filename.deleteOnExit();

        /* Create a mutable property set and write it to a POIFS: */
        try (OutputStream out = new FileOutputStream(filename);
            POIFSFileSystem poiFs = new POIFSFileSystem();
             ByteArrayOutputStream psStream = new ByteArrayOutputStream()) {
            final PropertySet ps = new PropertySet();
            final Section s = ps.getSections().get(0);
            s.setFormatID(SummaryInformation.FORMAT_ID);
            ps.write(psStream);
            poiFs.createDocument(new ByteArrayInputStream(psStream.toByteArray()), SummaryInformation.DEFAULT_STREAM_NAME);
            poiFs.writeFilesystem(out);
        }

        /* Read the POIFS: */
        final POIFSReader r = new POIFSReader();
        final List<PropertySet> psa = new ArrayList<>();

        r.registerListener(getListener(psa), SummaryInformation.DEFAULT_STREAM_NAME);
        r.read(filename);
        assertEquals(1, psa.size());
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
    void writeSimplePropertySet()
    throws IOException, UnsupportedVariantTypeException {
        final String AUTHOR = "Rainer Klute";
        final String TITLE = "Test Document";
        final File dataDir = _samples.getFile("");
        final File filename = new File(dataDir, POI_FS);
        filename.deleteOnExit();
        try (OutputStream out = new FileOutputStream(filename);
            POIFSFileSystem poiFs = new POIFSFileSystem()) {

            final PropertySet ps = new PropertySet();
            final Section si = new Section();
            si.setFormatID(SummaryInformation.FORMAT_ID);
            ps.clearSections();
            ps.addSection(si);

            final Property p = new Property();
            p.setID(PropertyIDMap.PID_AUTHOR);
            p.setType(Variant.VT_LPWSTR);
            p.setValue(AUTHOR);
            si.setProperty(p);
            si.setProperty(PropertyIDMap.PID_TITLE, Variant.VT_LPSTR, TITLE);

            poiFs.createDocument(ps.toInputStream(), SummaryInformation.DEFAULT_STREAM_NAME);
            poiFs.writeFilesystem(out);
        }

        /* Read the POIFS: */
        final List<PropertySet> psa = new ArrayList<>();
        final POIFSReader r = new POIFSReader();
        r.registerListener(getListener(psa), SummaryInformation.DEFAULT_STREAM_NAME);
        r.read(filename);
        assertEquals(1, psa.size());
        assertTrue(psa.get(0).isSummaryInformation());

        final Section s = psa.get(0).getSections().get(0);
        Object p1 = s.getProperty(PropertyIDMap.PID_AUTHOR);
        Object p2 = s.getProperty(PropertyIDMap.PID_TITLE);
        assertEquals(AUTHOR, p1);
        assertEquals(TITLE, p2);
    }



    /**
     * Writes a simple property set with two sections to a POIFS and reads it
     * back in.
     *
     * @exception IOException if an I/O exception occurs
     * @exception WritingNotSupportedException if HPSF does not yet support
     * a variant type to be written
     */
    @Test
    void writeTwoSections() throws WritingNotSupportedException, IOException {
        final String STREAM_NAME = "PropertySetStream";
        final String SECTION1 = "Section 1";
        final String SECTION2 = "Section 2";
        final ClassID FORMATID = ClassIDPredefined.EXCEL_V12.getClassID();

        final File dataDir = _samples.getFile("");
        final File filename = new File(dataDir, POI_FS);
        filename.deleteOnExit();

        try (OutputStream out = new FileOutputStream(filename);
            POIFSFileSystem poiFs = new POIFSFileSystem()) {
            final PropertySet ps = new PropertySet();
            ps.clearSections();

            final Section s1 = new Section();
            s1.setFormatID(FORMATID);
            s1.setProperty(2, SECTION1);
            ps.addSection(s1);

            final Section s2 = new Section();
            s2.setFormatID(FORMATID);
            s2.setProperty(2, SECTION2);
            ps.addSection(s2);

            poiFs.createDocument(ps.toInputStream(), STREAM_NAME);
            poiFs.writeFilesystem(out);
        }

        /* Read the POIFS: */
        final PropertySet[] psa = new PropertySet[1];
        final POIFSReader r = new POIFSReader();
        final POIFSReaderListener listener = (event) -> {
            assertDoesNotThrow(() -> psa[0] = PropertySetFactory.create(event.getStream()));
        };

        r.registerListener(listener,STREAM_NAME);
        r.read(filename);

        assertNotNull(psa[0]);
        Section s = (psa[0].getSections().get(0));
        assertEquals(s.getFormatID(), FORMATID);
        Object p = s.getProperty(2);
        assertEquals(SECTION1, p);
        s = (psa[0].getSections().get(1));
        p = s.getProperty(2);
        assertEquals(SECTION2, p);
    }

    private static POIFSReaderListener getListener(List<PropertySet> psa) {
        return event -> assertDoesNotThrow(() -> psa.add(PropertySetFactory.create(event.getStream())));
    }

    /**
     * Writes and reads back various variant types and checks whether the
     * stuff that has been read back equals the stuff that was written.
     */
    @Test
    void variantTypes() throws Exception {
        final int codepage = CODEPAGE_DEFAULT;
        Assumptions.assumeTrue(hasProperDefaultCharset(), IMPROPER_DEFAULT_CHARSET_MESSAGE);

        check(Variant.VT_EMPTY, null, codepage);
        check(Variant.VT_BOOL, Boolean.TRUE, codepage);
        check(Variant.VT_BOOL, Boolean.FALSE, codepage);
        check( Variant.VT_CF, new byte[] { 8, 0, 0, 0, 1, 0, 0, 0, 1, 2, 3, 4 }, codepage );
        check(Variant.VT_I4, 27, codepage);
        check(Variant.VT_I8, 28L, codepage);
        check(Variant.VT_R8, 29.0d, codepage);
        check(Variant.VT_I4, -27, codepage);
        check(Variant.VT_I8, -28L, codepage);
        check(Variant.VT_R8, -29.0d, codepage);
        check(Variant.VT_FILETIME, new Date(), codepage);
        check(Variant.VT_I4, Integer.MAX_VALUE, codepage);
        check(Variant.VT_I4, Integer.MIN_VALUE, codepage);
        check(Variant.VT_I8, Long.MAX_VALUE, codepage);
        check(Variant.VT_I8, Long.MIN_VALUE, codepage);
        check(Variant.VT_R8, Double.MAX_VALUE, codepage);
        check(Variant.VT_R8, Double.MIN_VALUE, codepage);
        checkString(Variant.VT_LPSTR, "\u00e4\u00f6\u00fc\u00df\u00c4\u00d6\u00dc", codepage);
        checkString(Variant.VT_LPWSTR, "\u00e4\u00f6\u00fc\u00df\u00c4\u00d6\u00dc", codepage);
    }



    /**
     * Writes and reads back strings using several different codepages and
     * checks whether the stuff that has been read back equals the stuff that
     * was written.
     */
    @Test
    void codepages() throws UnsupportedVariantTypeException, IOException
    {
        final int[] validCodepages = {CODEPAGE_DEFAULT, CodePageUtil.CP_UTF8, CodePageUtil.CP_UNICODE, CodePageUtil.CP_WINDOWS_1252};
        for (final int cp : validCodepages) {
            if (cp == -1 && !hasProperDefaultCharset())
            {
                System.err.println(IMPROPER_DEFAULT_CHARSET_MESSAGE +
                     " This testcase is skipped for the default codepage.");
                continue;
            }

            final long t = (cp == CodePageUtil.CP_UNICODE) ? Variant.VT_LPWSTR : Variant.VT_LPSTR;
            checkString(t, "\u00e4\u00f6\u00fc\u00c4\u00d6\u00dc\u00df", cp);
            if (cp == CodePageUtil.CP_UTF16 || cp == CodePageUtil.CP_UTF8) {
                check(t, "\u79D1\u5B78", cp);
            }
        }

        final int[] invalidCodepages = new int[] {0, 1, 2, 4711, 815};
        for (int cp : invalidCodepages) {
            assertThrows(UnsupportedEncodingException.class,
                () -> checkString(Variant.VT_LPSTR, "\u00e4\u00f6\u00fc\u00c4\u00d6\u00dc\u00df", cp),
                "UnsupportedEncodingException for codepage " + cp + " expected.");
        }

    }



    /**
     * Tests whether writing 8-bit characters to a Unicode property succeeds.
     */
    @Test
    void unicodeWrite8Bit() throws WritingNotSupportedException, IOException, NoPropertySetStreamException {
        final String TITLE = "This is a sample title";
        final PropertySet mps = new PropertySet();
        final Section ms = mps.getSections().get(0);
        ms.setFormatID(SummaryInformation.FORMAT_ID);
        final Property p = new Property();
        p.setID(PropertyIDMap.PID_TITLE);
        p.setType(Variant.VT_LPSTR);
        p.setValue(TITLE);
        ms.setProperty(p);

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

    private void checkString(final long variantType, final String value, final int codepage)
    throws UnsupportedVariantTypeException, IOException {
        for (int i=0; i<value.length(); i++) {
            check(variantType, value.substring(0, i), codepage);
        }
    }

    /**
     * Writes a property and reads it back in.
     *
     * @param variantType The property's variant type.
     * @param value The property's value.
     * @param codepage The codepage to use for writing and reading.
     * @throws UnsupportedVariantTypeException if the variant is not supported.
     * @throws IOException if an I/O exception occurs.
     */
    private void check(final long variantType, final Object value, final int codepage)
    throws UnsupportedVariantTypeException, IOException
    {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        VariantSupport.write(out, variantType, value, codepage);
        out.close();
        final byte[] b = out.toByteArray();
        final Object objRead =
            VariantSupport.read(b, 0, b.length + LittleEndianConsts.INT_SIZE, variantType, codepage);
        if (objRead instanceof byte[]) {
            assertArrayEquals((byte[])value, (byte[])objRead);
        } else if (value != null && !value.equals(objRead)) {
            assertEquals(value, objRead);
        }
    }

    /**
     * <p>Tests writing and reading back a proper dictionary.</p>
     */
    @Test
    void dictionary() throws IOException, HPSFException {
        final File copy = TempFile.createTempFile("Test-HPSF", "ole2");
        copy.deleteOnExit();

        /* Write: */
        final OutputStream out = new FileOutputStream(copy);
        final POIFSFileSystem poiFs = new POIFSFileSystem();
        final PropertySet ps1 = new PropertySet();
        final Section s = ps1.getSections().get(0);
        final Map<Long,String> m = new HashMap<>(3, 1.0f);
        m.put(1L, "String 1");
        m.put(2L, "String 2");
        m.put(3L, "String 3");
        s.setDictionary(m);
        s.setFormatID(DocumentSummaryInformation.FORMAT_ID[0]);
        int codepage = CodePageUtil.CP_UNICODE;
        s.setProperty(PropertyIDMap.PID_CODEPAGE, Variant.VT_I2, codepage);
        poiFs.createDocument(ps1.toInputStream(), "Test");
        poiFs.writeFilesystem(out);
        poiFs.close();
        out.close();

        /* Read back: */
        final List<POIFile> psf = Util.readPropertySets(copy);
        assertEquals(1, psf.size());
        final byte[] bytes = psf.get(0).getBytes();
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
     * Tests that when using POIFS, we can do an in-place write
     *  without needing to stream in + out the whole kitchen sink
     */
    @Test
    void inPlacePOIFSWrite() throws Exception {
        // We need to work on a File for in-place changes, so create a temp one
        final File copy = TempFile.createTempFile("Test-HPSF", "ole2");
        copy.deleteOnExit();

        // Copy a test file over to our temp location
        try (FileOutputStream out = new FileOutputStream(copy);
             InputStream inp = _samples.openResourceAsStream("TestShiftJIS.doc")) {
            IOUtils.copy(inp, out);
        }

        // Open the copy in read/write mode
        try (POIFSFileSystem fs = new POIFSFileSystem(copy, false)) {
            DirectoryEntry root = fs.getRoot();

            // Read the properties in there
            DocumentNode sinfDoc = (DocumentNode) root.getEntry(SummaryInformation.DEFAULT_STREAM_NAME);
            DocumentNode dinfDoc = (DocumentNode) root.getEntry(DocumentSummaryInformation.DEFAULT_STREAM_NAME);

            InputStream sinfStream = new DocumentInputStream(sinfDoc);
            SummaryInformation sinf = (SummaryInformation) PropertySetFactory.create(sinfStream);
            sinfStream.close();
            assertEquals(131077, sinf.getOSVersion());

            InputStream dinfStream = new DocumentInputStream(dinfDoc);
            DocumentSummaryInformation dinf = (DocumentSummaryInformation) PropertySetFactory.create(dinfStream);
            dinfStream.close();
            assertEquals(131077, dinf.getOSVersion());


            // Check they start as we expect
            assertEquals("Reiichiro Hori", sinf.getAuthor());
            assertEquals("Microsoft Word 9.0", sinf.getApplicationName());
            assertEquals("\u7b2c1\u7ae0", sinf.getTitle());

            assertEquals("", dinf.getCompany());
            assertNull(dinf.getManager());


            // Do an in-place replace via an InputStream
            assertNotNull(sinfDoc);
            assertNotNull(dinfDoc);

            new POIFSDocument(sinfDoc).replaceContents(sinf.toInputStream());
            new POIFSDocument(dinfDoc).replaceContents(dinf.toInputStream());


            // Check it didn't get changed
            sinfDoc = (DocumentNode) root.getEntry(SummaryInformation.DEFAULT_STREAM_NAME);
            dinfDoc = (DocumentNode) root.getEntry(DocumentSummaryInformation.DEFAULT_STREAM_NAME);

            InputStream sinfStream2 = new DocumentInputStream(sinfDoc);
            sinf = (SummaryInformation) PropertySetFactory.create(sinfStream2);
            sinfStream2.close();
            assertEquals(131077, sinf.getOSVersion());

            InputStream dinfStream2 = new DocumentInputStream(dinfDoc);
            dinf = (DocumentSummaryInformation) PropertySetFactory.create(dinfStream2);
            dinfStream2.close();
            assertEquals(131077, dinf.getOSVersion());
        }

        // Start again!
        try (FileOutputStream out = new FileOutputStream(copy);
                     InputStream inp = _samples.openResourceAsStream("TestShiftJIS.doc")) {
            IOUtils.copy(inp, out);
        }

        try (POIFSFileSystem fs = new POIFSFileSystem(copy, false)) {
            DirectoryEntry root = fs.getRoot();

            // Read the properties in once more
            DocumentNode sinfDoc = (DocumentNode)root.getEntry(SummaryInformation.DEFAULT_STREAM_NAME);
            DocumentNode dinfDoc = (DocumentNode)root.getEntry(DocumentSummaryInformation.DEFAULT_STREAM_NAME);

            InputStream sinfStream3 = new DocumentInputStream(sinfDoc);
            SummaryInformation sinf = (SummaryInformation)PropertySetFactory.create(sinfStream3);
            sinfStream3.close();
            assertEquals(131077, sinf.getOSVersion());

            InputStream dinfStream3 = new DocumentInputStream(dinfDoc);
            DocumentSummaryInformation dinf = (DocumentSummaryInformation)PropertySetFactory.create(dinfStream3);
            dinfStream3.close();
            assertEquals(131077, dinf.getOSVersion());


            // Have them write themselves in-place with no changes, as an OutputStream
            OutputStream soufStream = new DocumentOutputStream(sinfDoc);
            sinf.write(soufStream);
            soufStream.close();
            OutputStream doufStream = new DocumentOutputStream(dinfDoc);
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

            InputStream sinfStream4 = new DocumentInputStream(sinfDoc);
            byte[] sinfData = IOUtils.toByteArray(sinfStream4);
            sinfStream4.close();
            InputStream dinfStream4 = new DocumentInputStream(dinfDoc);
            byte[] dinfData = IOUtils.toByteArray(dinfStream4);
            dinfStream4.close();
            assertThat(sinfBytes.toByteArray(), equalTo(sinfData));
            assertThat(dinfBytes.toByteArray(), equalTo(dinfData));


            // Read back in as-is
            InputStream sinfStream5 = new DocumentInputStream(sinfDoc);
            sinf = (SummaryInformation)PropertySetFactory.create(sinfStream5);
            sinfStream5.close();
            assertEquals(131077, sinf.getOSVersion());

            InputStream dinfStream5 = new DocumentInputStream(dinfDoc);
            dinf = (DocumentSummaryInformation)PropertySetFactory.create(dinfStream5);
            dinfStream5.close();
            assertEquals(131077, dinf.getOSVersion());

            assertEquals("Reiichiro Hori", sinf.getAuthor());
            assertEquals("Microsoft Word 9.0", sinf.getApplicationName());
            assertEquals("\u7b2c1\u7ae0", sinf.getTitle());

            assertEquals("", dinf.getCompany());
            assertNull(dinf.getManager());


            // Now alter a few of them
            sinf.setAuthor("Changed Author");
            sinf.setTitle("Le titre \u00e9tait chang\u00e9");
            dinf.setManager("Changed Manager");


            // Save this into the filesystem
            OutputStream soufStream2 = new DocumentOutputStream(sinfDoc);
            sinf.write(soufStream2);
            soufStream2.close();
            OutputStream doufStream2 = new DocumentOutputStream(dinfDoc);
            dinf.write(doufStream2);
            doufStream2.close();


            // Read them back in again
            sinfDoc = (DocumentNode)root.getEntry(SummaryInformation.DEFAULT_STREAM_NAME);
            InputStream sinfStream6 = new DocumentInputStream(sinfDoc);
            sinf = (SummaryInformation)PropertySetFactory.create(sinfStream6);
            sinfStream6.close();
            assertEquals(131077, sinf.getOSVersion());

            dinfDoc = (DocumentNode)root.getEntry(DocumentSummaryInformation.DEFAULT_STREAM_NAME);
            InputStream dinfStream6 = new DocumentInputStream(dinfDoc);
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
        }

        try (POIFSFileSystem fs = new POIFSFileSystem(copy)) {
            DirectoryEntry root = fs.getRoot();

            // Re-check on load
            DocumentNode sinfDoc = (DocumentNode) root.getEntry(SummaryInformation.DEFAULT_STREAM_NAME);
            InputStream sinfStream7 = new DocumentInputStream(sinfDoc);
            SummaryInformation sinf = (SummaryInformation) PropertySetFactory.create(sinfStream7);
            sinfStream7.close();
            assertEquals(131077, sinf.getOSVersion());

            DocumentNode dinfDoc = (DocumentNode) root.getEntry(DocumentSummaryInformation.DEFAULT_STREAM_NAME);
            InputStream dinfStream7 = new DocumentInputStream(dinfDoc);
            DocumentSummaryInformation dinf = (DocumentSummaryInformation) PropertySetFactory.create(dinfStream7);
            dinfStream7.close();
            assertEquals(131077, dinf.getOSVersion());

            assertEquals("Changed Author", sinf.getAuthor());
            assertEquals("Microsoft Word 9.0", sinf.getApplicationName());
            assertEquals("Le titre \u00e9tait chang\u00e9", sinf.getTitle());

            assertEquals("", dinf.getCompany());
            assertEquals("Changed Manager", dinf.getManager());
        }

        // Tidy up
        assertTrue(copy.delete());
    }


    /**
     * Tests writing and reading back a proper dictionary with an invalid
     * codepage. (HPSF writes Unicode dictionaries only.)
     */
    @Test
    void dictionaryWithInvalidCodepage() throws IOException, HPSFException {
        final File copy = TempFile.createTempFile("Test-HPSF", "ole2");
        copy.deleteOnExit();

        /* Write: */

        final PropertySet ps1 = new PropertySet();
        final Section s = ps1.getSections().get(0);
        final Map<Long,String> m = new HashMap<>(3, 1.0f);
        m.put(1L, "String 1");
        m.put(2L, "String 2");
        m.put(3L, "String 3");

        try (OutputStream out = new FileOutputStream(copy);
             POIFSFileSystem poiFs = new POIFSFileSystem()) {
            s.setDictionary(m);
            s.setFormatID(DocumentSummaryInformation.FORMAT_ID[0]);
            int codepage = 12345;
            s.setProperty(PropertyIDMap.PID_CODEPAGE, Variant.VT_I2, codepage);
            assertThrows(UnsupportedEncodingException.class, () -> poiFs.createDocument(ps1.toInputStream(), "Test"));
            poiFs.writeFilesystem(out);
        }
    }

    /**
     * <p>Returns the display name of the default character set.</p>
     *
     * @return the display name of the default character set.
     */
    private static String getDefaultCharsetName() {
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
    private boolean hasProperDefaultCharset() {
        final String charSetName = System.getProperty("file.encoding");
        final Charset charSet = Charset.forName(charSetName);
        return charSet.newEncoder().canEncode('\u00e4');
    }
}
