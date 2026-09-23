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

package org.apache.poi.xssf.model;

import static org.apache.poi.ooxml.POIXMLTypeLoader.DEFAULT_XML_OPTIONS;
import static org.apache.poi.xssf.usermodel.XSSFRelation.NS_SPREADSHEETML;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.util.Internal;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRst;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSst;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.SstDocument;

/**
 * Table of strings shared across all sheets in a workbook.
 * <p>
 * A workbook may contain thousands of cells containing string (non-numeric) data. Furthermore this data is very
 * likely to be repeated across many rows or columns. The goal of implementing a single string table that is shared
 * across the workbook is to improve performance in opening and saving the file by only reading and writing the
 * repetitive information once.
 * </p>
 * <p>
 * Consider for example a workbook summarizing information for cities within various countries. There may be a
 * column for the name of the country, a column for the name of each city in that country, and a column
 * containing the data for each city. In this case the country name is repetitive, being duplicated in many cells.
 * In many cases the repetition is extensive, and a tremendous savings is realized by making use of a shared string
 * table when saving the workbook. When displaying text in the spreadsheet, the cell table will just contain an
 * index into the string table as the value of a cell, instead of the full string.
 * </p>
 * <p>
 * The shared string table contains all the necessary information for displaying the string: the text, formatting
 * properties, and phonetic properties (for East Asian languages).
 * </p>
 */
public class SharedStringsTable extends POIXMLDocumentPart implements SharedStrings, Closeable {

    /**
     *  Array of individual string items in the Shared String table.
     */
    private final List<CTRst> strings = new ArrayList<>();

    /**
     *  Maps the XML fragments of the rich strings and their indexes in the <code>strings</code> arrays
     */
    private final Map<String, Integer> stmap = new HashMap<>();

    /**
     *  Maps plain strings and their indexes in the <code>strings</code> arrays.
     *  <p>
     *  Entries that hold nothing but a piece of text (see {@link #plainText(CTRst)}) can be keyed
     *  by that text, which saves serialising them to XML just to be able to look them up. This map
     *  is kept separate from {@link #stmap} so that the raw text can never collide with the XML
     *  fragment of a rich string.
     */
    private final Map<String, Integer> plainmap = new HashMap<>();

    /**
     * An integer representing the total count of strings in the workbook. This count does not
     * include any numbers, it counts only the total of text strings in the workbook.
     */
    protected int count;

    /**
     * An integer representing the total count of unique strings in the Shared String Table.
     * A string is unique even if it is a copy of another string, but has different formatting applied
     * at the character level.
     */
    protected int uniqueCount;

    private SstDocument _sstDoc;

    private static final XmlOptions options = new XmlOptions();
    static {
        options.setSaveInner();
        options.setSaveAggressiveNamespaces();
        options.setUseDefaultNamespace(true);
        options.setSaveImplicitNamespaces(Collections.singletonMap("", NS_SPREADSHEETML));
    }

    public SharedStringsTable() {
        super();
        _sstDoc = SstDocument.Factory.newInstance();
        _sstDoc.addNewSst();
    }

    /**
     * @since 3.14-Beta1
     */
    public SharedStringsTable(PackagePart part) throws IOException {
        super(part);
        try (InputStream stream = part.getInputStream()) {
            readFrom(stream);
        }
    }

    /**
     * Read this shared strings table from an XML file.
     *
     * @param is The input stream containing the XML document.
     * @throws IOException if an error occurs while reading.
     */
    public void readFrom(InputStream is) throws IOException {
        try {
            int cnt = 0;
            _sstDoc = SstDocument.Factory.parse(is, DEFAULT_XML_OPTIONS);
            CTSst sst = _sstDoc.getSst();
            count = Math.toIntExact(sst.getCount());
            uniqueCount = Math.toIntExact(sst.getUniqueCount());
            //noinspection deprecation
            for (CTRst st : sst.getSiArray()) {
                String plain = plainText(st);
                if (plain != null) {
                    plainmap.put(plain, cnt);
                } else {
                    stmap.put(xmlText(st), cnt);
                }
                strings.add(st);
                cnt++;
            }
        } catch (XmlException e) {
            throw new IOException("unable to parse shared strings table", e);
        }
    }

    protected String xmlText(CTRst st) {
        return st.xmlText(options);
    }

    /**
     * Returns the text of the given entry if the entry can safely be identified by its plain text
     * alone, or <code>null</code> if it has to be identified by its serialised XML fragment.
     * <p>
     * Only entries that consist of nothing but a piece of text qualify, ie no formatting runs and
     * no phonetic information. Text with leading or trailing whitespace is excluded too, because
     * for such entries the <code>xml:space="preserve"</code> attribute is significant but is not
     * part of the text itself.
     *
     * @param st the entry to check
     * @return the text to key the entry by, or <code>null</code> if the XML fragment has to be used
     */
    private static String plainText(CTRst st) {
        if (!st.isSetT() || st.sizeOfRArray() != 0 || st.sizeOfRPhArray() != 0 || st.isSetPhoneticPr()) {
            return null;
        }
        String text = st.getT();
        if (text == null || text.isEmpty()) {
            return text;
        }
        if (Character.isWhitespace(text.charAt(0)) || Character.isWhitespace(text.charAt(text.length() - 1))) {
            return null;
        }
        return text;
    }

    /**
     * Return a string item by index
     *
     * @param idx index of item to return.
     * @return the item at the specified position in this Shared String table.
     */
    @Override
    public RichTextString getItemAt(int idx) {
        return new XSSFRichTextString(strings.get(idx));
    }

    /**
     * Return an integer representing the total count of strings in the workbook. This count does not
     * include any numbers, it counts only the total of text strings in the workbook.
     *
     * @return the total count of strings in the workbook
     */
    @Override
    public int getCount(){
        return count;
    }

    /**
     * Returns an integer representing the total count of unique strings in the Shared String Table.
     * A string is unique even if it is a copy of another string, but has different formatting applied
     * at the character level.
     *
     * @return the total count of unique strings in the workbook
     */
    @Override
    public int getUniqueCount() {
        return uniqueCount;
    }

    /**
     * Add an entry to this Shared String table (a new value is appended to the end).
     *
     * <p>
     * If the Shared String table already contains this <code>CTRst</code> bean, its index is returned.
     * Otherwise a new entry is aded.
     * </p>
     *
     * @param st the entry to add
     * @return index the index of added entry
     */
    @Internal
    int addEntry(CTRst st) {
        // plain strings can be keyed by their text, which avoids serialising them to XML
        String plain = plainText(st);
        Map<String, Integer> map = plain != null ? plainmap : stmap;
        String s = plain != null ? plain : xmlText(st);
        count++;
        Integer existing = map.get(s);
        if (existing != null) {
            return existing;
        }

        uniqueCount++;
        //create a CTRst bean attached to this SstDocument and copy the argument CTRst into it
        CTRst newSt = _sstDoc.getSst().addNewSi();
        newSt.set(st);
        int idx = strings.size();
        map.put(s, idx);
        strings.add(newSt);
        return idx;
    }

    /**
     * Add an entry to this Shared String table (a new value is appended to the end).
     *
     * <p>
     * If the Shared String table already contains this string entry, its index is returned.
     * Otherwise a new entry is added.
     * </p>
     *
     * @param string the entry to add
     * @since 4.0.0
     * @return index the index of added entry
     */
    public int addSharedStringItem(RichTextString string) {
        if (string instanceof XSSFRichTextString richTextString) {
            return addEntry(richTextString.getCTRst());
        } else {
            throw new IllegalArgumentException("Only XSSFRichTextString argument is supported");
        }
    }

    /**
     * Provide access to the strings in the SharedStringsTable
     *
     * @return list of shared string instances
     */
    public List<RichTextString> getSharedStringItems() {
        ArrayList<RichTextString> items = new ArrayList<>();
        for (CTRst rst : strings) {
            items.add(new XSSFRichTextString(rst));
        }
        return Collections.unmodifiableList(items);
    }

    /**
     * Write this table out as XML.
     *
     * @param out The stream to write to.
     * @throws IOException if an error occurs while writing.
     */
    public void writeTo(OutputStream out) throws IOException {
        XmlOptions xmlOptions = new XmlOptions(DEFAULT_XML_OPTIONS);
        // the following two lines turn off writing CDATA
        // see Bugzilla 48936
        xmlOptions.setSaveCDataLengthThreshold(1000000);
        xmlOptions.setSaveCDataEntityCountThreshold(-1);

        //re-create the sst table every time saving a workbook
        CTSst sst = _sstDoc.getSst();
        sst.setCount(count);
        sst.setUniqueCount(uniqueCount);

        _sstDoc.save(out, xmlOptions);
    }

    @Override
    protected void commit() throws IOException {
        PackagePart part = getPackagePart();
        try (OutputStream out = part.getOutputStream()) {
            writeTo(out);
        }
    }

    /**
     * Close any open resources, like temp files. This method is called by <code>XSSFWorkbook#close()</code>.
     * <p>
     *     This implementation is empty but subclasses may need to implement some logic.
     * </p>
     *
     * @since 4.0.0
     * @throws IOException if an error occurs while closing.
     */
    @Override
    public void close() throws IOException {}
}
