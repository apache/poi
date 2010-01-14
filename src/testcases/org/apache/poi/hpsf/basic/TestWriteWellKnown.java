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

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.TestCase;

import org.apache.poi.hpsf.CustomProperties;
import org.apache.poi.hpsf.CustomProperty;
import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.MarkUnsupportedException;
import org.apache.poi.hpsf.MutableProperty;
import org.apache.poi.hpsf.MutableSection;
import org.apache.poi.hpsf.NoPropertySetStreamException;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.PropertySetFactory;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hpsf.UnexpectedPropertySetTypeException;
import org.apache.poi.hpsf.Variant;
import org.apache.poi.hpsf.VariantSupport;
import org.apache.poi.hpsf.WritingNotSupportedException;
import org.apache.poi.hpsf.wellknown.SectionIDMap;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.POIDataSamples;
import org.apache.poi.util.TempFile;

/**
 * <p>Tests HPSF's high-level writing functionality for the well-known property
 * set "SummaryInformation" and "DocumentSummaryInformation".</p>
 *
 * @author Rainer Klute
 *     <a href="mailto:klute@rainer-klute.de">klute@rainer-klute.de</a>
 */
public class TestWriteWellKnown extends TestCase {

    private static final String POI_FS = "TestWriteWellKnown.doc";


    /**
     * @see TestCase#setUp()
     */
    public void setUp()
    {
        VariantSupport.setLogUnsupportedTypes(false);
    }



    /**
     * <p>This test method checks whether DocumentSummary information streams
     * can be read. This is done by opening all "Test*" files in the 'poifs' directrory
     * pointed to by the "POI.testdata.path" system property, trying to extract
     * the document summary information stream in the root directory and calling
     * its get... methods.</p>
     */
    public void testReadDocumentSummaryInformation()
            throws FileNotFoundException, IOException,
            NoPropertySetStreamException, MarkUnsupportedException,
            UnexpectedPropertySetTypeException
    {
        POIDataSamples _samples = POIDataSamples.getHPSFInstance();
        final File dataDir = _samples.getFile("");
        final File[] docs = dataDir.listFiles(new FileFilter()
        {
            public boolean accept(final File file)
            {
                return file.isFile() && file.getName().startsWith("Test");
            }});
        for (int i = 0; i < docs.length; i++)
        {
            final File doc = docs[i];

            /* Read a test document <em>doc</em> into a POI filesystem. */
            final POIFSFileSystem poifs = new POIFSFileSystem(new FileInputStream(doc));
            final DirectoryEntry dir = poifs.getRoot();
            DocumentEntry dsiEntry = null;
            try
            {
                dsiEntry = (DocumentEntry) dir.getEntry(DocumentSummaryInformation.DEFAULT_STREAM_NAME);
            }
            catch (FileNotFoundException ex)
            {
                /*
                 * A missing document summary information stream is not an error
                 * and therefore silently ignored here.
                 */
            }

            /*
             * If there is a document summry information stream, read it from
             * the POI filesystem.
             */
            if (dsiEntry != null)
            {
                final DocumentInputStream dis = new DocumentInputStream(dsiEntry);
                final PropertySet ps = new PropertySet(dis);
                final DocumentSummaryInformation dsi = new DocumentSummaryInformation(ps);

                /* Execute the get... methods. */
                dsi.getByteCount();
                dsi.getByteOrder();
                dsi.getCategory();
                dsi.getCompany();
                dsi.getCustomProperties();
                // FIXME dsi.getDocparts();
                // FIXME dsi.getHeadingPair();
                dsi.getHiddenCount();
                dsi.getLineCount();
                dsi.getLinksDirty();
                dsi.getManager();
                dsi.getMMClipCount();
                dsi.getNoteCount();
                dsi.getParCount();
                dsi.getPresentationFormat();
                dsi.getScale();
                dsi.getSlideCount();
            }
        }
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
     * @throws MarkUnsupportedException
     * @throws NoPropertySetStreamException
     * @throws UnexpectedPropertySetTypeException
     * @throws WritingNotSupportedException
     */
    public void testWriteWellKnown() throws IOException,
            NoPropertySetStreamException, MarkUnsupportedException,
            UnexpectedPropertySetTypeException, WritingNotSupportedException
    {
        POIDataSamples _samples = POIDataSamples.getHPSFInstance();
        final File dataDir = _samples.getFile("");
        final File doc1 = new File(dataDir, POI_FS);

        /* Read a test document <em>doc1</em> into a POI filesystem. */
        POIFSFileSystem poifs = new POIFSFileSystem(new FileInputStream(doc1));
        DirectoryEntry dir = poifs.getRoot();
        DocumentEntry siEntry = (DocumentEntry) dir.getEntry(SummaryInformation.DEFAULT_STREAM_NAME);
        DocumentEntry dsiEntry = (DocumentEntry) dir.getEntry(DocumentSummaryInformation.DEFAULT_STREAM_NAME);

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
        DocumentInputStream dis = new DocumentInputStream(siEntry);
        PropertySet ps = new PropertySet(dis);
        SummaryInformation si = new SummaryInformation(ps);
        dis = new DocumentInputStream(dsiEntry);
        ps = new PropertySet(dis);
        DocumentSummaryInformation dsi = new DocumentSummaryInformation(ps);

        /*
         * Write all properties supported by HPSF to the summary information
         * (e.g. author, edit date, application name) and to the document
         * summary information (e.g. company, manager).
         */
        Calendar cal = new GregorianCalendar();
        cal.set(2000, 6, 6, 6, 6, 6);
        final long time1 = cal.getTimeInMillis();
        cal.set(2001, 7, 7, 7, 7, 7);
        final long time2 = cal.getTimeInMillis();
        cal.set(2002, 8, 8, 8, 8, 8);
        final long time3 = cal.getTimeInMillis();

        int nr = 4711;
        final String P_APPLICATION_NAME = "ApplicationName";
        final String P_AUTHOR = "Author";
        final int    P_CHAR_COUNT = ++nr;
        final String P_COMMENTS = "Comments";
        final Date   P_CREATE_DATE_TIME = new Date(time1);
        final long   P_EDIT_TIME = ++nr * 1000 * 10;
        final String P_KEYWORDS = "Keywords";
        final String P_LAST_AUTHOR = "LastAuthor";
        final Date   P_LAST_PRINTED = new Date(time2);
        final Date   P_LAST_SAVE_DATE_TIME = new Date(time3);
        final int    P_PAGE_COUNT = ++nr;
        final String P_REV_NUMBER = "RevNumber";
        final int    P_SECURITY = 1;
        final String P_SUBJECT = "Subject";
        final String P_TEMPLATE = "Template";
        // FIXME (byte array properties not yet implemented): final byte[] P_THUMBNAIL = new byte[123];
        final String P_TITLE = "Title";
        final int    P_WORD_COUNT = ++nr;

        final int     P_BYTE_COUNT = ++nr;
        final String  P_CATEGORY = "Category";
        final String  P_COMPANY = "Company";
        // FIXME (byte array properties not yet implemented): final byte[]  P_DOCPARTS = new byte[123];
        // FIXME (byte array properties not yet implemented): final byte[]  P_HEADING_PAIR = new byte[123];
        final int     P_HIDDEN_COUNT = ++nr;
        final int     P_LINE_COUNT = ++nr;
        final boolean P_LINKS_DIRTY = true;
        final String  P_MANAGER = "Manager";
        final int     P_MM_CLIP_COUNT = ++nr;
        final int     P_NOTE_COUNT = ++nr;
        final int     P_PAR_COUNT = ++nr;
        final String  P_PRESENTATION_FORMAT = "PresentationFormat";
        final boolean P_SCALE = false;
        final int     P_SLIDE_COUNT = ++nr;
        final Date    now = new Date();

        final Integer POSITIVE_INTEGER = new Integer(2222);
        final Long POSITIVE_LONG = new  Long(3333);
        final Double POSITIVE_DOUBLE = new  Double(4444);
        final Integer NEGATIVE_INTEGER = new Integer(2222);
        final Long NEGATIVE_LONG = new  Long(3333);
        final Double NEGATIVE_DOUBLE = new  Double(4444);

        final Integer MAX_INTEGER = new Integer(Integer.MAX_VALUE);
        final Integer MIN_INTEGER = new Integer(Integer.MIN_VALUE);
        final Long MAX_LONG = new Long(Long.MAX_VALUE);
        final Long MIN_LONG = new Long(Long.MIN_VALUE);
        final Double MAX_DOUBLE = new Double(Double.MAX_VALUE);
        final Double MIN_DOUBLE = new Double(Double.MIN_VALUE);

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

        CustomProperties customProperties = dsi.getCustomProperties();
        if (customProperties == null)
            customProperties = new CustomProperties();
        customProperties.put("Schl\u00fcssel \u00e4",    "Wert \u00e4");
        customProperties.put("Schl\u00fcssel \u00e4\u00f6",   "Wert \u00e4\u00f6");
        customProperties.put("Schl\u00fcssel \u00e4\u00f6\u00fc",  "Wert \u00e4\u00f6\u00fc");
        customProperties.put("Schl\u00fcssel \u00e4\u00f6\u00fc\u00d6", "Wert \u00e4\u00f6\u00fc\u00d6");
        customProperties.put("positive_Integer", POSITIVE_INTEGER);
        customProperties.put("positive_Long", POSITIVE_LONG);
        customProperties.put("positive_Double", POSITIVE_DOUBLE);
        customProperties.put("negative_Integer", NEGATIVE_INTEGER);
        customProperties.put("negative_Long", NEGATIVE_LONG);
        customProperties.put("negative_Double", NEGATIVE_DOUBLE);
        customProperties.put("Boolean", Boolean.TRUE);
        customProperties.put("Date", now);
        customProperties.put("max_Integer", MAX_INTEGER);
        customProperties.put("min_Integer", MIN_INTEGER);
        customProperties.put("max_Long", MAX_LONG);
        customProperties.put("min_Long", MIN_LONG);
        customProperties.put("max_Double", MAX_DOUBLE);
        customProperties.put("min_Double", MIN_DOUBLE);
        
        // Check the keys went in
        assertTrue(customProperties.containsKey("Schl\u00fcssel \u00e4"));
        assertTrue(customProperties.containsKey("Boolean"));
        
        // Check the values went in
        assertEquals("Wert \u00e4", customProperties.get("Schl\u00fcssel \u00e4"));
        assertEquals(Boolean.TRUE, customProperties.get("Boolean"));
        assertTrue(customProperties.containsValue(Boolean.TRUE));
        assertTrue(customProperties.containsValue("Wert \u00e4"));
        
        // Check that things that aren't in aren't in
        assertFalse(customProperties.containsKey("False Boolean"));
        assertFalse(customProperties.containsValue(Boolean.FALSE));

        // Save as our custom properties
        dsi.setCustomProperties(customProperties);

        
        /* Write the summary information stream and the document summary
         * information stream to the POI filesystem. */
        si.write(dir, siEntry.getName());
        dsi.write(dir, dsiEntry.getName());

        /* Write the POI filesystem to a (temporary) file <em>doc2</em>
         * and close the latter. */
        final File doc2 = TempFile.createTempFile("POI_HPSF_Test.", ".tmp");
        doc2.deleteOnExit();
        OutputStream out = new FileOutputStream(doc2);
        poifs.writeFilesystem(out);
        out.close();

        /*
         * Open <em>doc2</em> for reading and check summary information and
         * document summary information. All properties written before must be
         * found in the property streams of <em>doc2</em> and have the correct
         * values.
         */
        poifs = new POIFSFileSystem(new FileInputStream(doc2));
        dir = poifs.getRoot();
        siEntry = (DocumentEntry) dir.getEntry(SummaryInformation.DEFAULT_STREAM_NAME);
        dsiEntry = (DocumentEntry) dir.getEntry(DocumentSummaryInformation.DEFAULT_STREAM_NAME);

        dis = new DocumentInputStream(siEntry);
        ps = new PropertySet(dis);
        si = new SummaryInformation(ps);
        dis = new DocumentInputStream(dsiEntry);
        ps = new PropertySet(dis);
        dsi = new DocumentSummaryInformation(ps);

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
        assertEquals(customProperties, cps);
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

        /*
         * <li><p>Write the summary information stream and the document summary
         * information stream to the POI filesystem. */
        si.write(dir, siEntry.getName());
        dsi.write(dir, dsiEntry.getName());

        /*
         * <li><p>Write the POI filesystem to a (temporary) file <em>doc3</em>
         * and close the latter. */
        final File doc3 = TempFile.createTempFile("POI_HPSF_Test.", ".tmp");
        doc3.deleteOnExit();
        out = new FileOutputStream(doc3);
        poifs.writeFilesystem(out);
        out.close();

        /*
         * Open <em>doc3</em> for reading and check summary information
         * and document summary information. All properties removed before must not
         * be found in the property streams of <em>doc3</em>.
         */
        poifs = new POIFSFileSystem(new FileInputStream(doc3));
        dir = poifs.getRoot();
        siEntry = (DocumentEntry) dir.getEntry(SummaryInformation.DEFAULT_STREAM_NAME);
        dsiEntry = (DocumentEntry) dir.getEntry(DocumentSummaryInformation.DEFAULT_STREAM_NAME);

        dis = new DocumentInputStream(siEntry);
        ps = new PropertySet(dis);
        si = new SummaryInformation(ps);
        dis = new DocumentInputStream(dsiEntry);
        ps = new PropertySet(dis);
        dsi = new DocumentSummaryInformation(ps);

        assertEquals(null, si.getApplicationName());
        assertEquals(null, si.getAuthor());
        assertEquals(0, si.getCharCount());
        assertTrue(si.wasNull());
        assertEquals(null, si.getComments());
        assertEquals(null, si.getCreateDateTime());
        assertEquals(0, si.getEditTime());
        assertTrue(si.wasNull());
        assertEquals(null, si.getKeywords());
        assertEquals(null, si.getLastAuthor());
        assertEquals(null, si.getLastPrinted());
        assertEquals(null, si.getLastSaveDateTime());
        assertEquals(0, si.getPageCount());
        assertTrue(si.wasNull());
        assertEquals(null, si.getRevNumber());
        assertEquals(0, si.getSecurity());
        assertTrue(si.wasNull());
        assertEquals(null, si.getSubject());
        assertEquals(null, si.getTemplate());
        assertEquals(null, si.getThumbnail());
        assertEquals(null, si.getTitle());
        assertEquals(0, si.getWordCount());
        assertTrue(si.wasNull());

        assertEquals(0, dsi.getByteCount());
        assertTrue(dsi.wasNull());
        assertEquals(null, dsi.getCategory());
        assertEquals(null, dsi.getCustomProperties());
        // FIXME (byte array properties not yet implemented): assertEquals(null, dsi.getDocparts());
        // FIXME (byte array properties not yet implemented): assertEquals(null, dsi.getHeadingPair());
        assertEquals(0, dsi.getHiddenCount());
        assertTrue(dsi.wasNull());
        assertEquals(0, dsi.getLineCount());
        assertTrue(dsi.wasNull());
        assertEquals(false, dsi.getLinksDirty());
        assertTrue(dsi.wasNull());
        assertEquals(null, dsi.getManager());
        assertEquals(0, dsi.getMMClipCount());
        assertTrue(dsi.wasNull());
        assertEquals(0, dsi.getNoteCount());
        assertTrue(dsi.wasNull());
        assertEquals(0, dsi.getParCount());
        assertTrue(dsi.wasNull());
        assertEquals(null, dsi.getPresentationFormat());
        assertEquals(false, dsi.getScale());
        assertTrue(dsi.wasNull());
        assertEquals(0, dsi.getSlideCount());
        assertTrue(dsi.wasNull());
    }



    /**
     * <p>Tests the simplified custom properties by reading them from the
     * available test files.</p>
     *
     * @throws Throwable if anything goes wrong.
     */
    public void testReadCustomPropertiesFromFiles() throws Throwable
    {
        final AllDataFilesTester.TestTask task = new AllDataFilesTester.TestTask()
        {
            public void runTest(final File file) throws FileNotFoundException,
                    IOException, NoPropertySetStreamException,
                    MarkUnsupportedException,
                    UnexpectedPropertySetTypeException
            {
                /* Read a test document <em>doc</em> into a POI filesystem. */
                final POIFSFileSystem poifs = new POIFSFileSystem(new FileInputStream(file));
                final DirectoryEntry dir = poifs.getRoot();
                DocumentEntry dsiEntry = null;
                try
                {
                    dsiEntry = (DocumentEntry) dir.getEntry(DocumentSummaryInformation.DEFAULT_STREAM_NAME);
                }
                catch (FileNotFoundException ex)
                {
                    /*
                     * A missing document summary information stream is not an error
                     * and therefore silently ignored here.
                     */
                }

                /*
                 * If there is a document summry information stream, read it from
                 * the POI filesystem, else create a new one.
                 */
                DocumentSummaryInformation dsi;
                if (dsiEntry != null)
                {
                    final DocumentInputStream dis = new DocumentInputStream(dsiEntry);
                    final PropertySet ps = new PropertySet(dis);
                    dsi = new DocumentSummaryInformation(ps);
                }
                else
                    dsi = PropertySetFactory.newDocumentSummaryInformation();
                final CustomProperties cps = dsi.getCustomProperties();

                if (cps == null)
                    /* The document does not have custom properties. */
                    return;

                for (final Iterator i = cps.entrySet().iterator(); i.hasNext();)
                {
                    final Map.Entry e = (Entry) i.next();
                    final CustomProperty cp = (CustomProperty) e.getValue();
                    cp.getName();
                    cp.getValue();
                }
            }
        };

        POIDataSamples _samples = POIDataSamples.getHPSFInstance();
        final File dataDir = _samples.getFile("");
        final File[] docs = dataDir.listFiles(new FileFilter()
        {
            public boolean accept(final File file)
            {
                return file.isFile() && file.getName().startsWith("Test");
            }
        });

        for (int i = 0; i < docs.length; i++)
        {
            task.runTest(docs[i]);
        }
    }



    /**
     * <p>Tests basic custom property features.</p>
     */
    public void testCustomerProperties()
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
        cp = (CustomProperty) cps.remove(KEY);
        assertEquals(KEY, cp.getName());
        assertEquals(VALUE_2, cp.getValue());
        assertEquals(0, cps.size());
    }



    /**
     * <p>Tests reading custom properties from a section including reading
     * custom properties which are not pure.</p>
     */
    public void testGetCustomerProperties()
    {
        final int ID_1 = 2;
        final int ID_2 = 3;
        final String NAME_1 = "Schl\u00fcssel \u00e4";
        final String VALUE_1 = "Wert 1";
        final Map dictionary = new HashMap();

        DocumentSummaryInformation dsi = PropertySetFactory.newDocumentSummaryInformation();
        CustomProperties cps;
        MutableSection s;

        /* A document summary information set stream by default does have custom properties. */
        cps = dsi.getCustomProperties();
        assertEquals(null, cps);

        /* Test an empty custom properties set. */
        s = new MutableSection();
        s.setFormatID(SectionIDMap.DOCUMENT_SUMMARY_INFORMATION_ID[1]);
        // s.setCodepage(Constants.CP_UNICODE);
        dsi.addSection(s);
        cps = dsi.getCustomProperties();
        assertEquals(0, cps.size());

        /* Add a custom property. */
        MutableProperty p = new MutableProperty();
        p.setID(ID_1);
        p.setType(Variant.VT_LPWSTR);
        p.setValue(VALUE_1);
        s.setProperty(p);
        dictionary.put(Long.valueOf(ID_1), NAME_1);
        s.setDictionary(dictionary);
        cps = dsi.getCustomProperties();
        assertEquals(1, cps.size());
        assertTrue(cps.isPure());

        /* Add another custom property. */
        s.setProperty(ID_2, Variant.VT_LPWSTR, VALUE_1);
        dictionary.put(Long.valueOf(ID_2), NAME_1);
        s.setDictionary(dictionary);
        cps = dsi.getCustomProperties();
        assertEquals(1, cps.size());
        assertFalse(cps.isPure());
    }
}
