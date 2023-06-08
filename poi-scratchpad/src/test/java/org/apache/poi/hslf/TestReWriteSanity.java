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

package org.apache.poi.hslf;


import static org.apache.poi.POITestCase.assertContains;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.poi.POIDataSamples;
import org.apache.poi.hslf.record.CurrentUserAtom;
import org.apache.poi.hslf.record.PersistPtrHolder;
import org.apache.poi.hslf.record.Record;
import org.apache.poi.hslf.record.UserEditAtom;
import org.apache.poi.hslf.usermodel.HSLFSlideShowImpl;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests that HSLFSlideShow writes the powerpoint bit of data back out
 *  in a sane manner - i.e. records end up in the right place
 */
public final class TestReWriteSanity {
    // HSLFSlideShow primed on the test data
    private HSLFSlideShowImpl ss;
    // POIFS primed on the test data
    private POIFSFileSystem pfs;

    @BeforeEach
    void setUp() throws Exception {
        POIDataSamples slTests = POIDataSamples.getSlideShowInstance();
        pfs = new POIFSFileSystem(slTests.openResourceAsStream("basic_test_ppt_file.ppt"));
        ss = new HSLFSlideShowImpl(pfs);
    }

    @AfterEach
    void tearDown() throws Exception {
        pfs.close();
        ss.close();
    }

    @Test
    void testUserEditAtomsRight() throws Exception {
        // Write out to a byte array
        UnsynchronizedByteArrayOutputStream baos = UnsynchronizedByteArrayOutputStream.builder().get();
        ss.write(baos);

        // Create a new one from that
        try (HSLFSlideShowImpl wss = new HSLFSlideShowImpl(baos.toInputStream())) {

            // Find the location of the PersistPtrIncrementalBlocks and
            // UserEditAtoms
            Record[] r = wss.getRecords();
            Map<Integer, Record> pp = new HashMap<>();
            Map<Integer, Object> ue = new HashMap<>();
            ue.put(0, 0); // Will show 0 if first
            int lastUEPos = -1;

            CountingOutputStream cos = new CountingOutputStream(NullOutputStream.INSTANCE);
            for (final Record rec : r) {
                int pos = cos.getCount();
                if (rec instanceof PersistPtrHolder) {
                    pp.put(pos, rec);
                }
                if (rec instanceof UserEditAtom) {
                    ue.put(pos, rec);
                    lastUEPos = pos;
                }

                rec.writeOut(cos);
            }

            // Check that the UserEditAtom's point to right stuff
            for (final Record rec : r) {
                if (rec instanceof UserEditAtom) {
                    UserEditAtom uea = (UserEditAtom) rec;
                    int luPos = uea.getLastUserEditAtomOffset();
                    int ppPos = uea.getPersistPointersOffset();

                    assertContains(ue, luPos);
                    assertContains(pp, ppPos);
                }
            }

            // Check that the CurrentUserAtom points to the right UserEditAtom
            CurrentUserAtom cua = wss.getCurrentUserAtom();
            int listedUEPos = (int) cua.getCurrentEditOffset();
            assertEquals(lastUEPos, listedUEPos);
        }
    }
}
