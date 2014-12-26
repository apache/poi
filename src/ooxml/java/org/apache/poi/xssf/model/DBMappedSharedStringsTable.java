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

import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.util.TempFile;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRst;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSst;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.SstDocument;

import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * SharedStringsTable With Map DB implementation
 * </p>
 *
 */
public class DBMappedSharedStringsTable extends SharedStringsTable implements AutoCloseable{

    /**
     * Maps strings and their indexes in the <code>recordVsIndexBasedSTMap</code> map db
     */
    private DB recordVsIndexMapDB;
    private HTreeMap<String, Integer> recordVsIndexBasedSTMap; //string vs index map to lookup existing record in stTable
    /**
     * Maps strings and their indexes in the <code>recordVsIndexBasedSTMap</code> map db
     */
    private DB indexVsRecordMapDB;
    private HTreeMap<Integer, String> indexVsRecordBasedSTMap; //index vs string map to retrieve record with index

    private final File temp_shared_string_file;

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

    private final static XmlOptions options = new XmlOptions();
    private final static XmlOptions out_options = new XmlOptions();


    static {
        options.put(XmlOptions.SAVE_INNER);
        options.put(XmlOptions.SAVE_AGGRESSIVE_NAMESPACES);
        options.put(XmlOptions.SAVE_USE_DEFAULT_NAMESPACE);
        options.setSaveImplicitNamespaces(Collections.singletonMap("", "http://schemas.openxmlformats.org/spreadsheetml/2006/main"));

        out_options.setLoadSubstituteNamespaces(Collections.singletonMap("", "http://schemas.openxmlformats.org/spreadsheetml/2006/main"));   //TODO add options if required
    }

    public DBMappedSharedStringsTable() {
        super();
        temp_shared_string_file = createTempFile("poi-shared-string-table", ".xml");
        initMapDbBasedSharedStringTableMap();
    }

    private File createTempFile(String prefix, String suffix) {
        try {
            return TempFile.createTempFile(prefix, suffix);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't create required temp file", e);
        }
    }

    public DBMappedSharedStringsTable(PackagePart part, PackageRelationship rel) throws IOException {
        super(part, rel);//TODO needs to be commented out whiler reading
        temp_shared_string_file = createTempFile("poi-shared-string-table", ".xml");
        initMapDbBasedSharedStringTableMap();
        readFrom(part.getInputStream());
    }

    public FileInputStream getSharedStringInputStream() throws IOException {
        return new FileInputStream(temp_shared_string_file);
    }

    public FileOutputStream getSharedStringsTableOutputStream() throws IOException {
        return new FileOutputStream(temp_shared_string_file);
    }

    public File getTemp_shared_string_file() {
        return temp_shared_string_file;
    }

    private void initMapDbBasedSharedStringTableMap() {
        initRecordVsIndexBasedMapDB();
        initIndexVsRecordBasedMapDB();
    }

    private void initRecordVsIndexBasedMapDB() {
        File mapDbFile = createTempFile(new BigInteger(130, new SecureRandom()).toString(32), "");//creating random name file to store map db
        recordVsIndexMapDB = DBMaker.newFileDB(mapDbFile)
                .transactionDisable()
                .cacheHardRefEnable()
                .cacheSize(65536)
                .deleteFilesAfterClose()
                .mmapFileEnablePartial()
                .closeOnJvmShutdown().make();
        recordVsIndexBasedSTMap = recordVsIndexMapDB.createHashMap(new BigInteger(130, new SecureRandom()).toString(32)).make();
    }

    private void initIndexVsRecordBasedMapDB() {
        File mapDb2File = createTempFile(new BigInteger(130, new SecureRandom()).toString(32), "");//creating random name file to store map db
        indexVsRecordMapDB = DBMaker.newFileDB(mapDb2File)
                .transactionDisable()
                .cacheDisable() //caching not required indexVsRecordBasedSTMap will be used to write all existing values
                .deleteFilesAfterClose()
                .mmapFileEnablePartial()
                .closeOnJvmShutdown().make();
        indexVsRecordBasedSTMap = indexVsRecordMapDB.createHashMap(new BigInteger(130, new SecureRandom()).toString(32)).make();
    }

    /**
     * Read this shared strings table from an XML file.
     *
     * @param is The input stream containing the XML document.
     * @throws java.io.IOException if an error occurs while reading.
     */
    @SuppressWarnings("deprecation") //YK: getXYZArray() array accessors are deprecated in xmlbeans with JDK 1.5 support
    public void readFrom(InputStream is) throws IOException {
        try {
            int cnt = 0;
            _sstDoc = SstDocument.Factory.parse(is);
            CTSst sst = _sstDoc.getSst();
            count = (int) sst.getCount();
            uniqueCount = (int) sst.getUniqueCount();
            for (CTRst st : sst.getSiArray()) {
                String key = getKey(st);
                recordVsIndexBasedSTMap.put(key, cnt);
                indexVsRecordBasedSTMap.put(cnt, key);
                cnt++;
            }
        } catch (XmlException e) {
            throw new IOException(e.getLocalizedMessage());
        }
    }

    private String getKey(CTRst st) {
        return st.xmlText(options);
    }

    /**
     * Return a string item by index
     *
     * @param idx index of item to return.
     * @return the item at the specified position in this Shared String table.
     */
    public CTRst getEntryAt(int idx) {
        try {
            return CTRst.Factory.parse(indexVsRecordBasedSTMap.get(idx), out_options);
        } catch (XmlException e) {
            throw new RuntimeException("Error Parsing xmlText from SSTable");
        }
    }

    /**
     * Return an integer representing the total count of strings in the workbook. This count does not
     * include any numbers, it counts only the total of text strings in the workbook.
     *
     * @return the total count of strings in the workbook
     */
    public int getCount() {
        return count;
    }

    /**
     * Returns an integer representing the total count of unique strings in the Shared String Table.
     * A string is unique even if it is a copy of another string, but has different formatting applied
     * at the character level.
     *
     * @return the total count of unique strings in the workbook
     */
    public int getUniqueCount() {
        return uniqueCount;
    }

    /**
     * Add an entry to this Shared String table (a new value is appened to the end).
     * <p/>
     * <p>
     * If the Shared String table already contains this <code>CTRst</code> bean, its index is returned.
     * Otherwise a new entry is aded.
     * </p>
     *
     * @param st the entry to add
     * @return index the index of added entry
     */
    public int addEntry(CTRst st) {
        String s = getKey(st);
        count++;
        if (recordVsIndexBasedSTMap.containsKey(s)) {
            return recordVsIndexBasedSTMap.get(s);
        }
        //new unique record
        recordVsIndexBasedSTMap.put(s, uniqueCount);
        indexVsRecordBasedSTMap.put(uniqueCount, s);
        return uniqueCount++;
    }
    /**
     * Provide low-level access to the underlying array of CTRst beans
     *
     * @return array of CTRst beans
     */
    public List<CTRst> getItems() {
        List<CTRst> beans = new ArrayList<CTRst>();
        for (int i = 0; i < uniqueCount; i++) {
            beans.add(getEntryAt(i));
        }
        return beans;
    }

    /**
     * Write this table out as XML.
     *
     * @param out The stream to write to.
     * @throws java.io.IOException if an error occurs while writing.
     */
    public void writeTo(OutputStream out) throws IOException {
        //re-create the sst table every time saving a workbook at the end after adding all record using map DB
        try {
            Writer writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
            addDefaultXmlOptions(writer);
            if (uniqueCount != 0) {
                addStringItems(writer);
                addEndDocument(writer);
            }
            writer.flush();
        } catch (XMLStreamException e) {
            throw new RuntimeException("Couldn't write to SharedStringsTable", e);
        }
    }

    private void addDefaultXmlOptions(Writer writer) throws XMLStreamException, IOException {
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
        String isNoSIElements = uniqueCount == 0 ? "/" : "";
        writer.write("<sst count=\"" + count + "\" uniqueCount=\"" + uniqueCount + "\" xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"" + isNoSIElements + ">");
    }

    private void addStringItems(Writer writer) throws XMLStreamException, IOException {
        for (int i = 0; i < uniqueCount; i++) {
            String s = indexVsRecordBasedSTMap.get(i);
            writer.write("<si>");
            writer.write(s);
            writer.write("</si>");
        }
    }

    private void addEndDocument(Writer writer) throws XMLStreamException, IOException {
        writer.write("</sst>");
    }

    @Override
    protected void commit() throws IOException {
       // createDefaultSSTTableXml();
        FileOutputStream sharedStringOutputStream = getSharedStringsTableOutputStream();
        writeTo(sharedStringOutputStream);
        sharedStringOutputStream.close();
    }

    private void createDefaultSSTTableXml() throws IOException {         //Todo, check if needed to create default one
        _sstDoc = SstDocument.Factory.newInstance();
        PackagePart part = getPackagePart();
        OutputStream out = part.getOutputStream();
        _sstDoc.save(out, options);
        out.close();
    }

    @Override
    public void close() throws Exception {
        recordVsIndexBasedSTMap.clear();
        indexVsRecordBasedSTMap.clear();
        recordVsIndexMapDB.close();
        indexVsRecordMapDB.close();
    }
}
