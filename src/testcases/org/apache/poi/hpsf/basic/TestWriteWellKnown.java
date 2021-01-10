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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hpsf.CustomProperties;
import org.apache.poi.hpsf.CustomProperty;
import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.NoPropertySetStreamException;
import org.apache.poi.hpsf.Property;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.PropertySetFactory;
import org.apache.poi.hpsf.Section;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hpsf.UnexpectedPropertySetTypeException;
import org.apache.poi.hpsf.Variant;
import org.apache.poi.hpsf.VariantSupport;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.util.TempFile;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * <p>Tests HPSF's high-level writing functionality for the well-known property
 * set "SummaryInformation" and "DocumentSummaryInformation".</p>
 */
class TestWriteWellKnown {

    private static final String POI_FS = "TestWriteWellKnown.doc";

    @BeforeAll
    public static void setUp() {
        VariantSupport.setLogUnsupportedTypes(false);
    }

    private static final String P_APPLICATION_NAME = "ApplicationName";
    private static final String P_AUTHOR = "Author";
    private static final int    P_CHAR_COUNT = 4712;
    private static final String P_COMMENTS = "Comments";
    private static final Date   P_CREATE_DATE_TIME;
    private static final long   P_EDIT_TIME = 4713 * 1000 * 10;
    private static final String P_KEYWORDS = "Keywords";
    private static final String P_LAST_AUTHOR = "LastAuthor";
    private static final Date   P_LAST_PRINTED;
    private static final Date   P_LAST_SAVE_DATE_TIME;
    private static final int    P_PAGE_COUNT = 4714;
    private static final String P_REV_NUMBER = "RevNumber";
    private static final int    P_SECURITY = 1;
    private static final String P_SUBJECT = "Subject";
    private static final String P_TEMPLATE = "Template";
    // FIXME (byte array properties not yet implemented): static final byte[] P_THUMBNAIL = new byte[123];
    private static final String P_TITLE = "Title";
    private static final int    P_WORD_COUNT = 4715;

    private static final int     P_BYTE_COUNT = 4716;
    private static final String  P_CATEGORY = "Category";
    private static final String  P_COMPANY = "Company";
    // FIXME (byte array properties not yet implemented): static final byte[]  P_DOCPARTS = new byte[123];
    // FIXME (byte array properties not yet implemented): static final byte[]  P_HEADING_PAIR = new byte[123];
    private static final int     P_HIDDEN_COUNT = 4717;
    private static final int     P_LINE_COUNT = 4718;
    private static final boolean P_LINKS_DIRTY = true;
    private static final String  P_MANAGER = "Manager";
    private static final int     P_MM_CLIP_COUNT = 4719;
    private static final int     P_NOTE_COUNT = 4720;
    private static final int     P_PAR_COUNT = 4721;
    private static final String  P_PRESENTATION_FORMAT = "PresentationFormat";
    private static final boolean P_SCALE = false;
    private static final int     P_SLIDE_COUNT = 4722;
    private static final Date    now = new Date();

    private static final Integer POSITIVE_INTEGER = 2222;
    private static final Long POSITIVE_LONG = 3333L;
    private static final Double POSITIVE_DOUBLE = 4444d;
    private static final Integer NEGATIVE_INTEGER = 2222;
    private static final Long NEGATIVE_LONG = 3333L;
    private static final Double NEGATIVE_DOUBLE = 4444d;

    private static final Integer MAX_INTEGER = Integer.MAX_VALUE;
    private static final Integer MIN_INTEGER = Integer.MIN_VALUE;
    private static final Long MAX_LONG = Long.MAX_VALUE;
    private static final Long MIN_LONG = Long.MIN_VALUE;
    private static final Double MAX_DOUBLE = Double.MAX_VALUE;
    private static final Double MIN_DOUBLE = Double.MIN_VALUE;

    static {
        Calendar cal = LocaleUtil.getLocaleCalendar(2000, 6, 6, 6, 6, 6);
        P_CREATE_DATE_TIME = cal.getTime();
        cal.set(2001, 7, 7, 7, 7, 7);
        P_LAST_PRINTED = cal.getTime();
        cal.set(2002, 8, 8, 8, 8, 8);
        P_LAST_SAVE_DATE_TIME = cal.getTime();
    }

    /**
     * <p>This test method test the writing of properties in the well-known
     * property set streams "SummaryInformation" and
     * "DocumentSummaryInformation" by performing the following steps:</p>
     *
     * <ol>
     *
     * <li><p>Read a test document <em>doc1</em> into a POI filesystem.</p></li>
     *
     * <li><p>Read the summary information stream and the document summary
     * information stream from the POI filesystem.</p></li>
     *
     * <li><p>Write all properties supported by HPSF to the summary
     * information (e.g. author, edit date, application name) and to the
     * document summary information (e.g. company, manager).</p></li>
     *
     * <li><p>Write the summary information stream and the document summary
     * information stream to the POI filesystem.</p></li>
     *
     * <li><p>Write the POI filesystem to a (temporary) file <em>doc2</em>
     * and close the latter.</p></li>
     *
     * <li><p>Open <em>doc2</em> for reading and check summary information
     * and document summary information. All properties written before must be
     * found in the property streams of <em>doc2</em> and have the correct
     * values.</p></li>
     *
     * <li><p>Remove all properties supported by HPSF from the summary
     * information (e.g. author, edit date, application name) and from the
     * document summary information (e.g. company, manager).</p></li>
     *
     * <li><p>Write the summary information stream and the document summary
     * information stream to the POI filesystem.</p></li>
     *
     * <li><p>Write the POI filesystem to a (temporary) file <em>doc3</em>
     * and close the latter.</p></li>
     *
     * <li><p>Open <em>doc3</em> for reading and check summary information
     * and document summary information. All properties removed before must not
     * be found in the property streams of <em>doc3</em>.</p></li> </ol>
     *
     * @throws IOException if some I/O error occurred.
     */
    @Test
    void testWriteWellKnown() throws Exception {
        POIDataSamples _samples = POIDataSamples.getHPSFInstance();

        final File doc1 = TempFile.createTempFile("POI_HPSF_Test1.", ".tmp");
        final File doc2 = TempFile.createTempFile("POI_HPSF_Test2.", ".tmp");
        final File doc3 = TempFile.createTempFile("POI_HPSF_Test3.", ".tmp");

        FileInputStream fis = new FileInputStream(_samples.getFile(POI_FS));
        FileOutputStream fos = new FileOutputStream(doc1);
        IOUtils.copy(fis, fos);
        fos.close();
        fis.close();

        CustomProperties cps1 = write1stFile(doc1, doc2);
        CustomProperties cps2 = write2ndFile(doc2, doc3);
        write3rdFile(doc3);

        assertEquals(cps1, cps2);
    }

    /*
     * Write all properties supported by HPSF to the summary information
     * (e.g. author, edit date, application name) and to the document
     * summary information (e.g. company, manager).
     */
    private static CustomProperties write1stFile(File fileIn, File fileOut) throws Exception {
        /* Read a test document <em>doc1</em> into a POI filesystem. */
        POIFSFileSystem poifs = new POIFSFileSystem(fileIn, false);

        /*
         * Read the summary information stream and the document summary
         * information stream from the POI filesystem.
         *
         * Please note that the result consists of SummaryInformation and
         * DocumentSummaryInformation instances which are in memory only. To
         * make them permanent they have to be written to a POI filesystem
         * explicitly (overwriting the former contents). Then the POI filesystem
         * should be saved to a file.
         */
        SummaryInformation si = getSummaryInformation(poifs);
        DocumentSummaryInformation dsi = getDocumentSummaryInformation(poifs);

        si.setApplicationName(P_APPLICATION_NAME);
        si.setAuthor(P_AUTHOR);
        si.setCharCount(P_CHAR_COUNT);
        si.setComments(P_COMMENTS);
        si.setCreateDateTime(P_CREATE_DATE_TIME);
        si.setEditTime(P_EDIT_TIME);
        si.setKeywords(P_KEYWORDS);
        si.setLastAuthor(P_LAST_AUTHOR);
        si.setLastPrinted(P_LAST_PRINTED);
        si.setLastSaveDateTime(P_LAST_SAVE_DATE_TIME);
        si.setPageCount(P_PAGE_COUNT);
        si.setRevNumber(P_REV_NUMBER);
        si.setSecurity(P_SECURITY);
        si.setSubject(P_SUBJECT);
        si.setTemplate(P_TEMPLATE);
        // FIXME (byte array properties not yet implemented): si.setThumbnail(P_THUMBNAIL);
        si.setTitle(P_TITLE);
        si.setWordCount(P_WORD_COUNT);

        dsi.setByteCount(P_BYTE_COUNT);
        dsi.setCategory(P_CATEGORY);
        dsi.setCompany(P_COMPANY);
        // FIXME (byte array properties not yet implemented): dsi.setDocparts(P_DOCPARTS);
        // FIXME (byte array properties not yet implemented): dsi.setHeadingPair(P_HEADING_PAIR);
        dsi.setHiddenCount(P_HIDDEN_COUNT);
        dsi.setLineCount(P_LINE_COUNT);
        dsi.setLinksDirty(P_LINKS_DIRTY);
        dsi.setManager(P_MANAGER);
        dsi.setMMClipCount(P_MM_CLIP_COUNT);
        dsi.setNoteCount(P_NOTE_COUNT);
        dsi.setParCount(P_PAR_COUNT);
        dsi.setPresentationFormat(P_PRESENTATION_FORMAT);
        dsi.setScale(P_SCALE);
        dsi.setSlideCount(P_SLIDE_COUNT);

        CustomProperties cps = dsi.getCustomProperties();
        assertNull(cps);
        cps = new CustomProperties();
        cps.put("Schl\u00fcssel \u00e4",    "Wert \u00e4");
        cps.put("Schl\u00fcssel \u00e4\u00f6",   "Wert \u00e4\u00f6");
        cps.put("Schl\u00fcssel \u00e4\u00f6\u00fc",  "Wert \u00e4\u00f6\u00fc");
        cps.put("Schl\u00fcssel \u00e4\u00f6\u00fc\u00d6", "Wert \u00e4\u00f6\u00fc\u00d6");
        cps.put("positive_Integer", POSITIVE_INTEGER);
        cps.put("positive_Long", POSITIVE_LONG);
        cps.put("positive_Double", POSITIVE_DOUBLE);
        cps.put("negative_Integer", NEGATIVE_INTEGER);
        cps.put("negative_Long", NEGATIVE_LONG);
        cps.put("negative_Double", NEGATIVE_DOUBLE);
        cps.put("Boolean", Boolean.TRUE);
        cps.put("Date", now);
        cps.put("max_Integer", MAX_INTEGER);
        cps.put("min_Integer", MIN_INTEGER);
        cps.put("max_Long", MAX_LONG);
        cps.put("min_Long", MIN_LONG);
        cps.put("max_Double", MAX_DOUBLE);
        cps.put("min_Double", MIN_DOUBLE);

        // Check the keys went in
        assertTrue(cps.containsKey("Schl\u00fcssel \u00e4"));
        assertTrue(cps.containsKey("Boolean"));

        // Check the values went in
        assertEquals("Wert \u00e4", cps.get("Schl\u00fcssel \u00e4"));
        assertEquals(Boolean.TRUE, cps.get("Boolean"));
        assertTrue(cps.containsValue(Boolean.TRUE));
        assertTrue(cps.containsValue("Wert \u00e4"));

        // Check that things that aren't in aren't in
        assertFalse(cps.containsKey("False Boolean"));
        assertFalse(cps.containsValue(Boolean.FALSE));

        // Save as our custom properties
        dsi.setCustomProperties(cps);


        /* Write the summary information stream and the document summary
         * information stream to the POI filesystem. */
        si.write(poifs.getRoot(), SummaryInformation.DEFAULT_STREAM_NAME);
        dsi.write(poifs.getRoot(), DocumentSummaryInformation.DEFAULT_STREAM_NAME);

        /* Write the POI filesystem to a (temporary) file <em>doc2</em>
         * and close the latter. */
        OutputStream out = new FileOutputStream(fileOut);
        poifs.writeFilesystem(out);
        out.close();
        poifs.close();

        return cps;
    }

    /*
     * Open <em>doc2</em> for reading and check summary information and
     * document summary information. All properties written before must be
     * found in the property streams of <em>doc2</em> and have the correct
     * values.
     */
    private static CustomProperties write2ndFile(File fileIn, File fileOut) throws Exception {
        POIFSFileSystem poifs = new POIFSFileSystem(fileIn, false);
        SummaryInformation si = getSummaryInformation(poifs);
        DocumentSummaryInformation dsi = getDocumentSummaryInformation(poifs);

        assertEquals(P_APPLICATION_NAME, si.getApplicationName());
        assertEquals(P_AUTHOR, si.getAuthor());
        assertEquals(P_CHAR_COUNT, si.getCharCount());
        assertEquals(P_COMMENTS, si.getComments());
        assertEquals(P_CREATE_DATE_TIME, si.getCreateDateTime());
        assertEquals(P_EDIT_TIME, si.getEditTime());
        assertEquals(P_KEYWORDS, si.getKeywords());
        assertEquals(P_LAST_AUTHOR, si.getLastAuthor());
        assertEquals(P_LAST_PRINTED, si.getLastPrinted());
        assertEquals(P_LAST_SAVE_DATE_TIME, si.getLastSaveDateTime());
        assertEquals(P_PAGE_COUNT, si.getPageCount());
        assertEquals(P_REV_NUMBER, si.getRevNumber());
        assertEquals(P_SECURITY, si.getSecurity());
        assertEquals(P_SUBJECT, si.getSubject());
        assertEquals(P_TEMPLATE, si.getTemplate());
        // FIXME (byte array properties not yet implemented): assertEquals(P_THUMBNAIL, si.getThumbnail());
        assertEquals(P_TITLE, si.getTitle());
        assertEquals(P_WORD_COUNT, si.getWordCount());

        assertEquals(P_BYTE_COUNT, dsi.getByteCount());
        assertEquals(P_CATEGORY, dsi.getCategory());
        assertEquals(P_COMPANY, dsi.getCompany());
        // FIXME (byte array properties not yet implemented): assertEquals(P_, dsi.getDocparts());
        // FIXME (byte array properties not yet implemented): assertEquals(P_, dsi.getHeadingPair());
        assertEquals(P_HIDDEN_COUNT, dsi.getHiddenCount());
        assertEquals(P_LINE_COUNT, dsi.getLineCount());
        assertEquals(P_LINKS_DIRTY, dsi.getLinksDirty());
        assertEquals(P_MANAGER, dsi.getManager());
        assertEquals(P_MM_CLIP_COUNT, dsi.getMMClipCount());
        assertEquals(P_NOTE_COUNT, dsi.getNoteCount());
        assertEquals(P_PAR_COUNT, dsi.getParCount());
        assertEquals(P_PRESENTATION_FORMAT, dsi.getPresentationFormat());
        assertEquals(P_SCALE, dsi.getScale());
        assertEquals(P_SLIDE_COUNT, dsi.getSlideCount());

        final CustomProperties cps = dsi.getCustomProperties();
        assertNotNull(cps);
        assertNull(cps.get("No value available"));
        assertEquals("Wert \u00e4", cps.get("Schl\u00fcssel \u00e4"));
        assertEquals("Wert \u00e4\u00f6", cps.get("Schl\u00fcssel \u00e4\u00f6"));
        assertEquals("Wert \u00e4\u00f6\u00fc", cps.get("Schl\u00fcssel \u00e4\u00f6\u00fc"));
        assertEquals("Wert \u00e4\u00f6\u00fc\u00d6", cps.get("Schl\u00fcssel \u00e4\u00f6\u00fc\u00d6"));
        assertEquals(POSITIVE_INTEGER, cps.get("positive_Integer"));
        assertEquals(POSITIVE_LONG, cps.get("positive_Long"));
        assertEquals(POSITIVE_DOUBLE, cps.get("positive_Double"));
        assertEquals(NEGATIVE_INTEGER, cps.get("negative_Integer"));
        assertEquals(NEGATIVE_LONG, cps.get("negative_Long"));
        assertEquals(NEGATIVE_DOUBLE, cps.get("negative_Double"));
        assertEquals(Boolean.TRUE, cps.get("Boolean"));
        assertEquals(now, cps.get("Date"));
        assertEquals(MAX_INTEGER, cps.get("max_Integer"));
        assertEquals(MIN_INTEGER, cps.get("min_Integer"));
        assertEquals(MAX_LONG, cps.get("max_Long"));
        assertEquals(MIN_LONG, cps.get("min_Long"));
        assertEquals(MAX_DOUBLE, cps.get("max_Double"));
        assertEquals(MIN_DOUBLE, cps.get("min_Double"));

        /* Remove all properties supported by HPSF from the summary
         * information (e.g. author, edit date, application name) and from the
         * document summary information (e.g. company, manager). */
        si.removeApplicationName();
        si.removeAuthor();
        si.removeCharCount();
        si.removeComments();
        si.removeCreateDateTime();
        si.removeEditTime();
        si.removeKeywords();
        si.removeLastAuthor();
        si.removeLastPrinted();
        si.removeLastSaveDateTime();
        si.removePageCount();
        si.removeRevNumber();
        si.removeSecurity();
        si.removeSubject();
        si.removeTemplate();
        si.removeThumbnail();
        si.removeTitle();
        si.removeWordCount();

        dsi.removeByteCount();
        dsi.removeCategory();
        dsi.removeCompany();
        dsi.removeCustomProperties();
        dsi.removeDocparts();
        dsi.removeHeadingPair();
        dsi.removeHiddenCount();
        dsi.removeLineCount();
        dsi.removeLinksDirty();
        dsi.removeManager();
        dsi.removeMMClipCount();
        dsi.removeNoteCount();
        dsi.removeParCount();
        dsi.removePresentationFormat();
        dsi.removeScale();
        dsi.removeSlideCount();

        // Write the summary information stream and the document summary
        // information stream to the POI filesystem.
        si.write(poifs.getRoot(), SummaryInformation.DEFAULT_STREAM_NAME);
        dsi.write(poifs.getRoot(), DocumentSummaryInformation.DEFAULT_STREAM_NAME);

        // Write the POI filesystem to a (temporary) file doc3 and close the latter.
        FileOutputStream out = new FileOutputStream(fileOut);
        poifs.writeFilesystem(out);
        out.close();
        poifs.close();

        return cps;
    }

    /*
     * Open {@code doc3} for reading and check summary information
     * and document summary information. All properties removed before must not
     * be found in the property streams of {@code doc3}.
     */
    private static void write3rdFile(File fileIn) throws Exception {
        POIFSFileSystem poifs = new POIFSFileSystem(fileIn, false);
        SummaryInformation si = getSummaryInformation(poifs);
        DocumentSummaryInformation dsi = getDocumentSummaryInformation(poifs);

        assertNull(si.getApplicationName());
        assertNull(si.getAuthor());
        assertEquals(0, si.getCharCount());
        assertTrue(si.wasNull());
        assertNull(si.getComments());
        assertNull(si.getCreateDateTime());
        assertEquals(0, si.getEditTime());
        assertTrue(si.wasNull());
        assertNull(si.getKeywords());
        assertNull(si.getLastAuthor());
        assertNull(si.getLastPrinted());
        assertNull(si.getLastSaveDateTime());
        assertEquals(0, si.getPageCount());
        assertTrue(si.wasNull());
        assertNull(si.getRevNumber());
        assertEquals(0, si.getSecurity());
        assertTrue(si.wasNull());
        assertNull(si.getSubject());
        assertNull(si.getTemplate());
        assertNull(si.getThumbnail());
        assertNull(si.getTitle());
        assertEquals(0, si.getWordCount());
        assertTrue(si.wasNull());

        assertEquals(0, dsi.getByteCount());
        assertTrue(dsi.wasNull());
        assertNull(dsi.getCategory());
        assertNull(dsi.getCustomProperties());
        // FIXME (byte array properties not yet implemented): assertNull(dsi.getDocparts());
        // FIXME (byte array properties not yet implemented): assertNull(dsi.getHeadingPair());
        assertEquals(0, dsi.getHiddenCount());
        assertTrue(dsi.wasNull());
        assertEquals(0, dsi.getLineCount());
        assertTrue(dsi.wasNull());
        assertFalse(dsi.getLinksDirty());
        assertTrue(dsi.wasNull());
        assertNull(dsi.getManager());
        assertEquals(0, dsi.getMMClipCount());
        assertTrue(dsi.wasNull());
        assertEquals(0, dsi.getNoteCount());
        assertTrue(dsi.wasNull());
        assertEquals(0, dsi.getParCount());
        assertTrue(dsi.wasNull());
        assertNull(dsi.getPresentationFormat());
        assertFalse(dsi.getScale());
        assertTrue(dsi.wasNull());
        assertEquals(0, dsi.getSlideCount());
        assertTrue(dsi.wasNull());
        poifs.close();
    }

    private static SummaryInformation getSummaryInformation(POIFSFileSystem poifs) throws Exception {
        DocumentInputStream dis = poifs.createDocumentInputStream(SummaryInformation.DEFAULT_STREAM_NAME);
        PropertySet ps = new PropertySet(dis);
        SummaryInformation si = new SummaryInformation(ps);
        dis.close();
        return si;
    }

    static DocumentSummaryInformation getDocumentSummaryInformation(POIFSFileSystem poifs)
    throws IOException, NoPropertySetStreamException, UnexpectedPropertySetTypeException {
        if (!poifs.getRoot().hasEntry(DocumentSummaryInformation.DEFAULT_STREAM_NAME)) {
            return null;
        }

        DocumentInputStream dis = poifs.createDocumentInputStream(DocumentSummaryInformation.DEFAULT_STREAM_NAME);
        PropertySet ps = new PropertySet(dis);
        DocumentSummaryInformation dsi = new DocumentSummaryInformation(ps);
        dis.close();
        return dsi;
    }

    /**
     * Tests basic custom property features.
     */
    @Test
    void testCustomerProperties()
    {
        final String KEY = "Schl\u00fcssel \u00e4";
        final String VALUE_1 = "Wert 1";
        final String VALUE_2 = "Wert 2";

        CustomProperty cp;
        CustomProperties cps = new CustomProperties();
        assertEquals(0, cps.size());

        /* After adding a custom property the size must be 1 and it must be
         * possible to extract the custom property from the map. */
        cps.put(KEY, VALUE_1);
        assertEquals(1, cps.size());
        Object v1 = cps.get(KEY);
        assertEquals(VALUE_1, v1);

        /* After adding a custom property with the same name the size must still
         * be one. */
        cps.put(KEY, VALUE_2);
        assertEquals(1, cps.size());
        Object v2 = cps.get(KEY);
        assertEquals(VALUE_2, v2);

        /* Removing the custom property must return the remove property and
         * reduce the size to 0. */
        cp = cps.remove(KEY);
        assertEquals(KEY, cp.getName());
        assertEquals(VALUE_2, cp.getValue());
        assertEquals(0, cps.size());
    }



    /**
     * Tests reading custom properties from a section including reading
     * custom properties which are not pure.
     */
    @Test
    void testGetCustomerProperties()
    {
        final int ID_1 = 2;
        final int ID_2 = 3;
        final String NAME_1 = "Schl\u00fcssel \u00e4";
        final String VALUE_1 = "Wert 1";
        final Map<Long,String> dictionary = new HashMap<>();

        DocumentSummaryInformation dsi = PropertySetFactory.newDocumentSummaryInformation();
        CustomProperties cps;
        Section s;

        /* A document summary information set stream by default does have custom properties. */
        cps = dsi.getCustomProperties();
        assertNull(cps);

        /* Test an empty custom properties set. */
        s = new Section();
        s.setFormatID(DocumentSummaryInformation.FORMAT_ID[1]);
        // s.setCodepage(CodePageUtil.CP_UNICODE);
        dsi.addSection(s);
        cps = dsi.getCustomProperties();
        assertEquals(0, cps.size());

        /* Add a custom property. */
        Property p = new Property();
        p.setID(ID_1);
        p.setType(Variant.VT_LPWSTR);
        p.setValue(VALUE_1);
        s.setProperty(p);
        dictionary.put((long) ID_1, NAME_1);
        s.setDictionary(dictionary);
        cps = dsi.getCustomProperties();
        assertEquals(1, cps.size());
        assertTrue(cps.isPure());

        /* Add another custom property. */
        s.setProperty(ID_2, Variant.VT_LPWSTR, VALUE_1);
        dictionary.put((long) ID_2, NAME_1);
        s.setDictionary(dictionary);
        cps = dsi.getCustomProperties();
        assertEquals(1, cps.size());
        assertFalse(cps.isPure());
    }
}
