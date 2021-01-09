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

package org.apache.poi.hemf.hemfplus.extractor;


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hemf.record.emf.HemfComment.EmfComment;
import org.apache.poi.hemf.record.emf.HemfComment.EmfCommentDataPlus;
import org.apache.poi.hemf.record.emf.HemfRecord;
import org.apache.poi.hemf.record.emfplus.HemfPlusHeader;
import org.apache.poi.hemf.record.emfplus.HemfPlusRecord;
import org.apache.poi.hemf.record.emfplus.HemfPlusRecordType;
import org.apache.poi.hemf.usermodel.HemfPicture;
import org.junit.jupiter.api.Test;

public class TestHemfPlusExtractor {

    @Test
    void testBasic() throws Exception {
        //test header
        EmfCommentDataPlus emfPlus = getCommentRecord("SimpleEMF_windows.emf", 0);
        List<HemfPlusRecord> records = emfPlus.getRecords();
        assertEquals(1, records.size());
        assertEquals(HemfPlusRecordType.header, records.get(0).getEmfPlusRecordType());

        HemfPlusHeader header = (HemfPlusHeader)records.get(0);
        assertEquals(240, header.getLogicalDpiX());
        assertEquals(240, header.getLogicalDpiY());
        assertEquals(1, header.getFlags());
        assertEquals(1, header.getEmfPlusFlags());



        //test that the HemfCommentEMFPlus record at offset 1
        //contains 6 HemfCommentEMFPlus records within it
        List<HemfPlusRecordType> expected = new ArrayList<>();
        expected.add(HemfPlusRecordType.setPixelOffsetMode);
        expected.add(HemfPlusRecordType.setAntiAliasMode);
        expected.add(HemfPlusRecordType.setCompositingQuality);
        expected.add(HemfPlusRecordType.setPageTransform);
        expected.add(HemfPlusRecordType.setInterpolationMode);
        expected.add(HemfPlusRecordType.getDC);

        emfPlus = getCommentRecord("SimpleEMF_windows.emf", 1);
        records = emfPlus.getRecords();
        assertEquals(expected.size(), records.size());

        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), records.get(i).getEmfPlusRecordType());
        }
    }


    private EmfCommentDataPlus getCommentRecord(String testFileName, int recordIndex) throws Exception {
        try (InputStream is = POIDataSamples.getSpreadSheetInstance().openResourceAsStream(testFileName)) {
            HemfPicture ex = new HemfPicture(is);
            int i = 0;
            for (HemfRecord record : ex) {
                if (record instanceof EmfComment && i++ == recordIndex) {
                    EmfComment commentRecord = (EmfComment)record;
                    return (EmfCommentDataPlus) commentRecord.getCommentData();
                }
            }
        }
        return null;
    }
}
