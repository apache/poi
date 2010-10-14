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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.apache.poi.POIXMLDocumentPart;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRst;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSst;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.SstDocument;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;


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
 *
 * @author Nick Birch
 * @author Yegor Kozlov
 */
public class SharedStringsTable extends POIXMLDocumentPart {

    /**
     *  Array of individual string items in the Shared String table.
     */
    private final List<CTRst> strings = new ArrayList<CTRst>();

    /**
     *  Maps strings and their indexes in the <code>strings</code> arrays
     */
    private final Map<String, Integer> stmap = new HashMap<String, Integer>();

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

    private SstDocument _sstDoc;

    public SharedStringsTable() {
        super();
        _sstDoc = SstDocument.Factory.newInstance();
        _sstDoc.addNewSst();
    }

    public SharedStringsTable(PackagePart part, PackageRelationship rel) throws IOException {
        super(part, rel);
        readFrom(part.getInputStream());
    }

    /**
     * Read this shared strings table from an XML file.
     * 
     * @param is The input stream containing the XML document.
     * @throws IOException if an error occurs while reading.
     */
    @SuppressWarnings("deprecation") //YK: getXYZArray() array accessors are deprecated in xmlbeans with JDK 1.5 support
    public void readFrom(InputStream is) throws IOException {
        try {
            int cnt = 0;
            _sstDoc = SstDocument.Factory.parse(is);
            CTSst sst = _sstDoc.getSst();
            count = (int)sst.getCount();
            uniqueCount = (int)sst.getUniqueCount();
            for (CTRst st : sst.getSiArray()) {
                stmap.put(st.toString(), cnt);
                strings.add(st);
                cnt++;
            }
        } catch (XmlException e) {
            throw new IOException(e.getLocalizedMessage());
        }
    }

    /**
     * Return a string item by index
     *
     * @param idx index of item to return.
     * @return the item at the specified position in this Shared String table.
     */
    public CTRst getEntryAt(int idx) {
        return strings.get(idx);
    }

    /**
     * Return an integer representing the total count of strings in the workbook. This count does not
     * include any numbers, it counts only the total of text strings in the workbook.
     *
     * @return the total count of strings in the workbook
     */
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
    public int getUniqueCount(){
        return uniqueCount;
    }

    /**
     * Add an entry to this Shared String table (a new value is appened to the end).
     *
     * <p>
     * If the Shared String table already contains this <code>CTRst</code> bean, its index is returned.
     * Otherwise a new entry is aded.
     * </p>
     *
     * @param st the entry to add
     * @return index the index of added entry
     */
    public int addEntry(CTRst st) {
        String s = st.toString();
        count++;
        if (stmap.containsKey(s)) {
            return stmap.get(s);
        }

        uniqueCount++;
        //create a CTRst bean attached to this SstDocument and copy the argument CTRst into it
        CTRst newSt = _sstDoc.getSst().addNewSi();
        newSt.set(st);
        int idx = strings.size();
        stmap.put(s, idx);
        strings.add(newSt);
        return idx;
    }
    /**
     * Provide low-level access to the underlying array of CTRst beans
     *
     * @return array of CTRst beans
     */
    public List<CTRst> getItems() {
        return strings;
    }

    /**
     * Write this table out as XML.
     * 
     * @param out The stream to write to.
     * @throws IOException if an error occurs while writing.
     */
    public void writeTo(OutputStream out) throws IOException {
        XmlOptions options = new XmlOptions(DEFAULT_XML_OPTIONS);
        // the following two lines turn off writing CDATA
        // see Bugzilla 48936
        options.setSaveCDataLengthThreshold(1000000);
        options.setSaveCDataEntityCountThreshold(-1);

        //re-create the sst table every time saving a workbook
        CTSst sst = _sstDoc.getSst();
        sst.setCount(count);
        sst.setUniqueCount(uniqueCount);

        _sstDoc.save(out, options);
    }

    @Override
    protected void commit() throws IOException {
        PackagePart part = getPackagePart();
        OutputStream out = part.getOutputStream();
        writeTo(out);
        out.close();
    }
}
