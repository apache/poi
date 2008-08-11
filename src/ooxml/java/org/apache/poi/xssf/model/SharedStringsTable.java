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
import java.util.LinkedList;

import org.apache.poi.ss.usermodel.SharedStringSource;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRst;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSst;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.SstDocument;


/**
 * Table of strings shared across all sheets in a workbook.
 * 
 * @version $Id$
 */
public class SharedStringsTable implements SharedStringSource, XSSFModel {

    private final LinkedList<String> strings = new LinkedList<String>();
    private SstDocument doc;
    
    /**
     * Create a new SharedStringsTable, by reading it 
     *  from the InputStream of a PackagePart.
     * 
     * @param is The input stream containing the XML document.
     * @throws IOException if an error occurs while reading.
     */
    public SharedStringsTable(InputStream is) throws IOException {
        readFrom(is);
    }
    /**
     * Create a new, empty SharedStringsTable
     */
    public SharedStringsTable() {
    	doc = SstDocument.Factory.newInstance();
    }

    /**
     * Read this shared strings table from an XML file.
     * 
     * @param is The input stream containing the XML document.
     * @throws IOException if an error occurs while reading.
     */
    public void readFrom(InputStream is) throws IOException {
        try {
            doc = SstDocument.Factory.parse(is);
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
     * For unit testing only!
     */
    public int _getNumberOfStrings() {
    	return strings.size();
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
        options.setUseDefaultNamespace();
        
        // Requests use of whitespace for easier reading
        options.setSavePrettyPrint();

        SstDocument doc = SstDocument.Factory.newInstance(options);
        CTSst sst = doc.addNewSst();
        sst.setCount(strings.size());
        sst.setUniqueCount(strings.size());
        for (String s : strings) {
            sst.addNewSi().setT(s);
        }
        doc.save(out, options);
    }
}
