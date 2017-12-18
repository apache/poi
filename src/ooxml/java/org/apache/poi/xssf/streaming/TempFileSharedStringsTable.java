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

package org.apache.poi.xssf.streaming;

import java.io.*;
import java.util.*;

import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.util.Removal;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRst;

import static org.apache.poi.xssf.usermodel.XSSFRelation.NS_SPREADSHEETML;

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
class TempFileSharedStringsTable extends SharedStringsTable {

    /**
     *  Array of individual string items in the Shared String table.
     */
    private final List<CTRst> strings = new ArrayList<>();

    /**
     *  Maps strings and their indexes in the <code>strings</code> arrays
     */
    private final Map<String, Integer> stmap = new HashMap<>();

    /**
     * An integer representing the total count of strings in the workbook. This count does not
     * include any numbers, it counts only the total of text strings in the workbook.
     */
    private int count;

    /**
     * An integer representing the total count of unique strings in the Shared String Table.
     * A string is unique even if it is a copy of another string, but has different formatting applied
     * at the character level.
     */
    private int uniqueCount;

    private static final XmlOptions options = new XmlOptions();
    static {
        options.put( XmlOptions.SAVE_INNER );
     	options.put( XmlOptions.SAVE_AGGRESSIVE_NAMESPACES );
     	options.put( XmlOptions.SAVE_USE_DEFAULT_NAMESPACE );
        options.setSaveImplicitNamespaces(Collections.singletonMap("", NS_SPREADSHEETML));
    }

    public TempFileSharedStringsTable() {
        super();
    }

    /**
     * Read this shared strings table from an XML file.
     * 
     * @param is The input stream containing the XML document.
     * @throws IOException if an error occurs while reading.
     */
    @Override
    public void readFrom(InputStream is) throws IOException {
        throw new UnsupportedOperationException("TempFileSharedStringsTable only supports writing of SXSSF workbooks");
    }

    private String xmlText(CTRst st) {
        return st.xmlText(options);
    }

    /**
     * Return a string item by index
     *
     * @param idx index of item to return.
     * @return the item at the specified position in this Shared String table.
     * @deprecated use <code>getItemAt(int idx)</code> instead
     */
    @Removal(version = "4.2")
    @Override
    public CTRst getEntryAt(int idx) {
        return strings.get(idx);
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
    public int getUniqueCount(){
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
     * @deprecated use <code>addSharedStringItem(RichTextString string)</code> instead
     */
    @Removal(version = "4.2") //make private in 4.2
    @Override
    public int addEntry(CTRst st) {
        String s = xmlText(st);
        count++;
        if (stmap.containsKey(s)) {
            return stmap.get(s);
        }

        uniqueCount++;
        int idx = strings.size();
        stmap.put(s, idx);
        strings.add(st);
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
     * @since POI 4.0.0
     * @return index the index of added entry
     */
    @Override
    public int addSharedStringItem(RichTextString string) {
        if(!(string instanceof XSSFRichTextString)){
            throw new IllegalArgumentException("Only XSSFRichTextString argument is supported");
        }
        return addEntry(((XSSFRichTextString) string).getCTRst());
    }

    /**
     * Provide low-level access to the underlying array of CTRst beans
     *
     * @return array of CTRst beans
     * @deprecated use <code>getSharedStringItems</code> instead
     */
    @Removal(version = "4.2")
    @Override
    public List<CTRst> getItems() {
        throw new UnsupportedOperationException("TempFileSharedStringsTable only supports writing of SXSSF workbooks");
    }

    /**
     * Provide access to the strings in the SharedStringsTable
     *
     * @return list of shared string instances
     */
    @Override
    public List<RichTextString> getSharedStringItems() {
        throw new UnsupportedOperationException("TempFileSharedStringsTable only supports writing of SXSSF workbooks");
    }

    /**
     * Write this table out as XML.
     * 
     * @param out The stream to write to.
     * @throws IOException if an error occurs while writing.
     */
    @Override
    public void writeTo(OutputStream out) throws IOException {
        Writer writer = new BufferedWriter(
                new OutputStreamWriter(out, "UTF-8"));
        try {
            writer.write("<sst count=\"");
            writer.write(Integer.toString(count));
            writer.write("\" uniqueCount=\"");
            writer.write(Integer.toString(uniqueCount));
            writer.write("\" xmlns=\"");
            writer.write(NS_SPREADSHEETML);
            writer.write("\">");
            for (CTRst rst : strings) {
                writer.write("<si>");
                writer.write(xmlText(rst));
                writer.write("</si>");
            }
            writer.write("</sst>");
        } finally {
            // do not close; let calling code close the output stream
            writer.flush();
        }
    }
}
