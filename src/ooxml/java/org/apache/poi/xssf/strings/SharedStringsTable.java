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

package org.apache.poi.xssf.strings;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;

import org.apache.poi.ss.usermodel.SharedStringSource;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.openxml4j.opc.PackagePart;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRst;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSst;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.SstDocument;


/**
 * Table of strings shared across all sheets in a workbook.
 * 
 * FIXME: I don't like having a dependency on PackagePart (from OpenXML4J) in model classes.
 * I'd rather let Workbook keep track of all part-document relationships and keep all other
 * classes clean. -- Ugo
 * 
 * @version $Id$
 */
public class SharedStringsTable implements SharedStringSource {

    private final LinkedList<String> strings = new LinkedList<String>();
    
    private PackagePart part;
   
    /**
     * Create a new SharedStringsTable by reading it from a PackagePart.
     * 
     * @param part The PackagePart to read.
     * @throws IOException if an error occurs while reading.
     */
    public SharedStringsTable(PackagePart part) throws IOException {
        this.part = part;
        InputStream is = part.getInputStream();
        try {
            readFrom(is);
        } finally {
            if (is != null) is.close();
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
            SstDocument doc = SstDocument.Factory.parse(is);
            for (CTRst rst : doc.getSst().getSiArray()) {
                strings.add(rst.getT());
            }
        } catch (XmlException e) {
            throw new IOException(e.getLocalizedMessage());
        }
    }

    public String getSharedStringAt(int idx) {
        return strings.get(idx);
    }

    public synchronized int putSharedString(String s) {
        if (strings.contains(s)) {
            return strings.indexOf(s);
        }
        strings.add(s);
        return strings.size() - 1;
    }

    /**
     * Save this table to its own PackagePart.
     * 
     * @throws IOException if an error occurs while writing.
     */
    public void save() throws IOException {
        OutputStream out = this.part.getOutputStream();
        try {
            writeTo(out);
        } finally {
            out.close();
        }
    }

    /**
     * Write this table out as XML.
     * 
     * @param out The stream to write to.
     * @throws IOException if an error occurs while writing.
     */
    public void writeTo(OutputStream out) throws IOException {
        XmlOptions options = new XmlOptions();
        options.setSaveOuter();
        SstDocument doc = SstDocument.Factory.newInstance(options);
        CTSst sst = doc.addNewSst();
        sst.setCount(strings.size());
        sst.setUniqueCount(strings.size());
        for (String s : strings) {
            sst.addNewSi().setT(s);
        }
        doc.save(out);
    }
}
