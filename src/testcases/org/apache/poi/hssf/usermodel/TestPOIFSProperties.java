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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.hpsf.*;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.HexDump;
import org.junit.jupiter.api.Test;

/**
 * Old-style setting of POIFS properties doesn't work with POI 3.0.2
 */
class TestPOIFSProperties {
    private static final String title = "Testing POIFS properties";

    @Test
    void testFail() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        { // read the workbook, adjust the SummaryInformation and write the data to a byte array
            POIFSFileSystem fs = openFileSystem();

            HSSFWorkbook wb = new HSSFWorkbook(fs);

            //set POIFS properties after constructing HSSFWorkbook
            //(a piece of code that used to work up to POI 3.0.2)
            setTitle(fs);

            //save the workbook and read the property
            wb.write(out);
            out.close();
            wb.close();
        }

        // process the byte array
        checkFromByteArray(out.toByteArray());
    }

    @Test
    void testOK() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        { // read the workbook, adjust the SummaryInformation and write the data to a byte array
            POIFSFileSystem fs = openFileSystem();

            //set POIFS properties before constructing HSSFWorkbook
            setTitle(fs);

            HSSFWorkbook wb = new HSSFWorkbook(fs);

            wb.write(out);
            out.close();
            wb.close();
        }

        // process the byte array
        checkFromByteArray(out.toByteArray());
    }

    private POIFSFileSystem openFileSystem() throws IOException {
        InputStream is = HSSFTestDataSamples.openSampleFileStream("Simple.xls");
        POIFSFileSystem fs = new POIFSFileSystem(is);
        is.close();
        return fs;
    }

    private void setTitle(POIFSFileSystem fs) throws NoPropertySetStreamException, MarkUnsupportedException, IOException, WritingNotSupportedException {
        SummaryInformation summary1 = (SummaryInformation) PropertySetFactory.create(fs.createDocumentInputStream(SummaryInformation.DEFAULT_STREAM_NAME));
        assertNotNull(summary1);

        summary1.setTitle(title);
        //write the modified property back to POIFS
        fs.getRoot().getEntry(SummaryInformation.DEFAULT_STREAM_NAME).delete();
        fs.createDocument(summary1.toInputStream(), SummaryInformation.DEFAULT_STREAM_NAME);

        // check that the information was added successfully to the filesystem object
        SummaryInformation summaryCheck = (SummaryInformation) PropertySetFactory.create(fs.createDocumentInputStream(SummaryInformation.DEFAULT_STREAM_NAME));
        assertNotNull(summaryCheck);
    }

    private void checkFromByteArray(byte[] bytes) throws IOException, NoPropertySetStreamException, MarkUnsupportedException {
        // on some environments in CI we see strange failures, let's verify that the size is exactly right
        // this can be removed again after the problem is identified
        assertEquals(5120, bytes.length, "Had: " + HexDump.toHex(bytes));

        POIFSFileSystem fs2 = new POIFSFileSystem(new ByteArrayInputStream(bytes));
        SummaryInformation summary2 = (SummaryInformation) PropertySetFactory.create(fs2.createDocumentInputStream(SummaryInformation.DEFAULT_STREAM_NAME));
        assertNotNull(summary2);

        assertEquals(title, summary2.getTitle());
        fs2.close();
    }
}
