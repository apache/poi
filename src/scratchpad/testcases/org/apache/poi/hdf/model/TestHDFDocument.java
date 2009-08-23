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

package org.apache.poi.hdf.model;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;
import org.apache.poi.POIDataSamples;

/**
 * Class to test {@link HDFDocument} functionality
 *
 * @author Bob Otterberg
 */
public final class TestHDFDocument extends TestCase {
    private static final POIDataSamples _samples = POIDataSamples.getDocumentInstance();

    public void testStopJUnitComplainintAboutNoTests() {
        // TODO - fix these junits
    }

    /**
     * OBJECTIVE:  Test that HDF can read an empty document (empty.doc).<P>
     * SUCCESS:    HDF reads the document.  Matches values in their particular positions.<P>
     * FAILURE:    HDF does not read the document or excepts.  HDF cannot identify values
     *             in the document in their known positions.<P>
     */
    public void fixme_testEmpty() throws IOException {
        InputStream stream = _samples.openResourceAsStream("empty.doc");
        new HDFDocument(stream);
    }

    /**
     * OBJECTIVE:  Test that HDF can read an _very_ simple document (simple.doc).<P>
     * SUCCESS:    HDF reads the document.  Matches values in their particular positions.<P>
     * FAILURE:    HDF does not read the document or excepts.  HDF cannot identify values
     *             in the document in their known positions.<P>
     */
    public void fixme_testSimple() throws IOException {
        InputStream stream = _samples.openResourceAsStream("simple.doc");
        new HDFDocument(stream);
    }

    /**
     * OBJECTIVE:  Test that HDF can read a document containing a simple list (simple-list.doc).<P>
     * SUCCESS:    HDF reads the document.  Matches values in their particular positions.<P>
     * FAILURE:    HDF does not read the document or excepts.  HDF cannot identify values
     *             in the document in their known positions.<P>
     *
     */
    public void fixme_testSimpleList() throws IOException {
        InputStream stream = _samples.openResourceAsStream("simple-list.doc");
        new HDFDocument(stream);
    }

    /**
     * OBJECTIVE:  Test that HDF can read a document containing a simple table (simple-table.doc).<P>
     * SUCCESS:    HDF reads the document.  Matches values in their particular positions.<P>
     * FAILURE:    HDF does not read the document or excepts.  HDF cannot identify values
     *             in the document in their known positions.<P>
     */
    public void fixme_testSimpleTable() throws IOException {
        InputStream stream = _samples.openResourceAsStream("simple-table.doc");
        new HDFDocument(stream);
    }
}
