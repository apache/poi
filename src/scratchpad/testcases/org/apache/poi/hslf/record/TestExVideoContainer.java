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

package org.apache.poi.hslf.record;


import junit.framework.TestCase;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;

/**
 * Tests that {@link HeadersFootersAtom} works properly
 *
 * @author Yegor Kozlov
 */
public final class TestExVideoContainer extends TestCase {

    // From a real file
    private byte[] data = new byte[]{
            0x0F, 0x00, 0x05, 0x10, (byte) 0x9E, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x04, 0x10, 0x08, 0x00, 0x00, 0x00,
            0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, (byte)0xBA, 0x0F, (byte)0x86, 0x00, 0x00, 0x00,
            0x44, 0x00, 0x3A, 0x00, 0x5C, 0x00, 0x70, 0x00, 0x72, 0x00, 0x6F, 0x00, 0x6A, 0x00, 0x65, 0x00,
            0x63, 0x00, 0x74, 0x00, 0x73, 0x00, 0x5C, 0x00, 0x53, 0x00, 0x63, 0x00, 0x68, 0x00, 0x75, 0x00,
            0x6C, 0x00, 0x65, 0x00, 0x72, 0x00, 0x41, 0x00, 0x47, 0x00, 0x5C, 0x00, 0x6D, 0x00, 0x63, 0x00,
            0x6F, 0x00, 0x6D, 0x00, 0x5F, 0x00, 0x76, 0x00, 0x5F, 0x00, 0x31, 0x00, 0x5F, 0x00, 0x30, 0x00,
            0x5F, 0x00, 0x34, 0x00, 0x5C, 0x00, 0x76, 0x00, 0x69, 0x00, 0x65, 0x00, 0x77, 0x00, 0x5C, 0x00,
            0x64, 0x00, 0x61, 0x00, 0x74, 0x00, 0x61, 0x00, 0x5C, 0x00, 0x74, 0x00, 0x65, 0x00, 0x73, 0x00,
            0x74, 0x00, 0x73, 0x00, 0x5C, 0x00, 0x69, 0x00, 0x6D, 0x00, 0x61, 0x00, 0x67, 0x00, 0x65, 0x00,
            0x73, 0x00, 0x5C, 0x00, 0x63, 0x00, 0x61, 0x00, 0x72, 0x00, 0x64, 0x00, 0x73, 0x00, 0x2E, 0x00,
            0x6D, 0x00, 0x70, 0x00, 0x67, 0x00};




    public void testRead() {
        ExVideoContainer record = new ExVideoContainer(data, 0, data.length);
        assertEquals(RecordTypes.ExVideoContainer.typeID, record.getRecordType());

        ExMediaAtom exMedia = record.getExMediaAtom();
        assertEquals(1, exMedia.getObjectId());
        assertNotNull(exMedia);
        assertFalse(exMedia.getFlag(ExMediaAtom.fLoop));
        assertFalse(exMedia.getFlag(ExMediaAtom.fNarration));
        assertFalse(exMedia.getFlag(ExMediaAtom.fRewind));

        CString path = record.getPathAtom();
        assertNotNull(exMedia);
        assertEquals("D:\\projects\\SchulerAG\\mcom_v_1_0_4\\view\\data\\tests\\images\\cards.mpg", path.getText());
    }

    public void testWrite() throws Exception {
        ExVideoContainer record = new ExVideoContainer(data, 0, data.length);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        record.writeOut(baos);
        byte[] b = baos.toByteArray();

        assertTrue(Arrays.equals(data, b));
    }

    public void testNewRecord() throws Exception {
        ExVideoContainer record = new ExVideoContainer();
        record.getExMediaAtom().setObjectId(1);
        record.getPathAtom().setText("D:\\projects\\SchulerAG\\mcom_v_1_0_4\\view\\data\\tests\\images\\cards.mpg");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        record.writeOut(baos);
        byte[] b = baos.toByteArray();

        assertTrue(Arrays.equals(data, b));
    }
}
