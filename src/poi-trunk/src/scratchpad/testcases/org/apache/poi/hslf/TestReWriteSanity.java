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
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hslf.record.CurrentUserAtom;
import org.apache.poi.hslf.record.PersistPtrHolder;
import org.apache.poi.hslf.record.Record;
import org.apache.poi.hslf.record.UserEditAtom;
import org.apache.poi.hslf.usermodel.HSLFSlideShowImpl;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests that HSLFSlideShow writes the powerpoint bit of data back out
 *  in a sane manner - i.e. records end up in the right place
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public final class TestReWriteSanity {
    // HSLFSlideShow primed on the test data
    private HSLFSlideShowImpl ss;
    // POIFS primed on the test data
    private POIFSFileSystem pfs;

    @Before
    public void setUp() throws Exception {
        POIDataSamples slTests = POIDataSamples.getSlideShowInstance();
        pfs = new POIFSFileSystem(slTests.openResourceAsStream("basic_test_ppt_file.ppt"));
        ss = new HSLFSlideShowImpl(pfs);
    }
    
    @After
    public void tearDown() throws Exception {
        pfs.close();
        ss.close();
    }

    @Test
    public void testUserEditAtomsRight() throws Exception {
        // Write out to a byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ss.write(baos);

        // Build an input stream of it
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

        // Create a new one from that
        HSLFSlideShowImpl wss = new HSLFSlideShowImpl(bais);

        // Find the location of the PersistPtrIncrementalBlocks and
        // UserEditAtoms
        Record[] r = wss.getRecords();
        Map<Integer,Record> pp = new HashMap<>();
        Map<Integer,Object> ue = new HashMap<>();
        ue.put(Integer.valueOf(0),Integer.valueOf(0)); // Will show 0 if first
        int pos = 0;
        int lastUEPos = -1;

        for (final Record rec : r) {
            if(rec instanceof PersistPtrHolder) {
                pp.put(Integer.valueOf(pos), rec);
            }
            if(rec instanceof UserEditAtom) {
                ue.put(Integer.valueOf(pos), rec);
                lastUEPos = pos;
            }

            ByteArrayOutputStream bc = new ByteArrayOutputStream();
            rec.writeOut(bc);
            pos += bc.size();
        }

        // Check that the UserEditAtom's point to right stuff
        for (final Record rec : r) {
            if(rec instanceof UserEditAtom) {
                UserEditAtom uea = (UserEditAtom)rec;
                int luPos = uea.getLastUserEditAtomOffset();
                int ppPos = uea.getPersistPointersOffset();

                assertContains(ue, Integer.valueOf(luPos));
                assertContains(pp, Integer.valueOf(ppPos));
            }
        }

        // Check that the CurrentUserAtom points to the right UserEditAtom
        CurrentUserAtom cua = wss.getCurrentUserAtom();
        int listedUEPos = (int)cua.getCurrentEditOffset();
        assertEquals(lastUEPos,listedUEPos);
        
        wss.close();
    }
}
