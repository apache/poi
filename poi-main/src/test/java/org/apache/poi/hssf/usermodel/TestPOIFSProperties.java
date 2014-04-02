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

package org.apache.poi.hssf.usermodel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import junit.framework.TestCase;

import org.apache.poi.hpsf.PropertySetFactory;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * Old-style setting of POIFS properties doesn't work with POI 3.0.2
 *
 * @author Yegor Kozlov
 */
public class TestPOIFSProperties extends TestCase{

    private static final String title = "Testing POIFS properties";

    public void testFail() throws Exception {
        InputStream is = HSSFTestDataSamples.openSampleFileStream("Simple.xls");
        POIFSFileSystem fs = new POIFSFileSystem(is);

        HSSFWorkbook wb = new HSSFWorkbook(fs);

        //set POIFS properties after constructing HSSFWorkbook
        //(a piece of code that used to work up to POI 3.0.2)
        SummaryInformation summary1 = (SummaryInformation)PropertySetFactory.create(fs.createDocumentInputStream(SummaryInformation.DEFAULT_STREAM_NAME));
        summary1.setTitle(title);
        //write the modified property back to POIFS
        fs.getRoot().getEntry(SummaryInformation.DEFAULT_STREAM_NAME).delete();
        fs.createDocument(summary1.toInputStream(), SummaryInformation.DEFAULT_STREAM_NAME);

        //save the workbook and read the property
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        out.close();

        POIFSFileSystem fs2 = new POIFSFileSystem(new ByteArrayInputStream(out.toByteArray()));
        SummaryInformation summary2 = (SummaryInformation)PropertySetFactory.create(fs2.createDocumentInputStream(SummaryInformation.DEFAULT_STREAM_NAME));

        //failing assertion
        assertEquals(title, summary2.getTitle());
    }

    public void testOK() throws Exception {
        InputStream is = HSSFTestDataSamples.openSampleFileStream("Simple.xls");
        POIFSFileSystem fs = new POIFSFileSystem(is);

        //set POIFS properties before constructing HSSFWorkbook
        SummaryInformation summary1 = (SummaryInformation)PropertySetFactory.create(fs.createDocumentInputStream(SummaryInformation.DEFAULT_STREAM_NAME));
        summary1.setTitle(title);

        fs.getRoot().getEntry(SummaryInformation.DEFAULT_STREAM_NAME).delete();
        fs.createDocument(summary1.toInputStream(), SummaryInformation.DEFAULT_STREAM_NAME);

        HSSFWorkbook wb = new HSSFWorkbook(fs);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        out.close();

        //read the property
        POIFSFileSystem fs2 = new POIFSFileSystem(new ByteArrayInputStream(out.toByteArray()));
        SummaryInformation summary2 = (SummaryInformation)PropertySetFactory.create(fs2.createDocumentInputStream(SummaryInformation.DEFAULT_STREAM_NAME));
        assertEquals(title, summary2.getTitle());

    }
}
