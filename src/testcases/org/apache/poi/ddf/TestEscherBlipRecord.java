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

package org.apache.poi.ddf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import junit.framework.TestCase;
import org.apache.poi.POIDataSamples;

/**
 * Test read/serialize of escher blip records
 *
 * @author Yegor Kozlov
 */
public final class TestEscherBlipRecord extends TestCase {
    private static final POIDataSamples _samples = POIDataSamples.getDDFInstance();

    //test reading/serializing of a PNG blip
    public void testReadPNG() {
        //provided in bug-44886
        byte[] data = _samples.readFile("Container.dat");

        EscherContainerRecord record = new EscherContainerRecord();
        record.fillFields(data, 0, new DefaultEscherRecordFactory());
        EscherContainerRecord bstore = (EscherContainerRecord)record.getChild(1);
        EscherBSERecord bse1 = (EscherBSERecord)bstore.getChild(0);
        assertEquals(EscherBSERecord.BT_PNG, bse1.getBlipTypeWin32());
        assertEquals(EscherBSERecord.BT_PNG, bse1.getBlipTypeMacOS());
        assertTrue(Arrays.equals(new byte[]{
            0x65, 0x07, 0x4A, (byte)0x8D, 0x3E, 0x42, (byte)0x8B, (byte)0xAC,
            0x1D, (byte)0x89, 0x35, 0x4F, 0x48, (byte)0xFA, 0x37, (byte)0xC2
        }, bse1.getUid()));
        assertEquals(255, bse1.getTag());
        assertEquals(32308, bse1.getSize());

        EscherBitmapBlip blip1 = (EscherBitmapBlip)bse1.getBlipRecord();
        assertEquals(0x6E00, blip1.getOptions());
        assertEquals(EscherBitmapBlip.RECORD_ID_PNG, blip1.getRecordId());
        assertTrue(Arrays.equals(new byte[]{
            0x65, 0x07, 0x4A, (byte)0x8D, 0x3E, 0x42, (byte)0x8B, (byte)0xAC,
            0x1D, (byte)0x89, 0x35, 0x4F, 0x48, (byte)0xFA, 0x37, (byte)0xC2
        }, blip1.getUID()));

        //serialize and read again
        byte[] ser = bse1.serialize();
        EscherBSERecord bse2 = new EscherBSERecord();
        bse2.fillFields(ser, 0, new DefaultEscherRecordFactory());
        assertEquals(bse1.getRecordId(), bse2.getRecordId());
        assertEquals(bse1.getBlipTypeWin32(), bse2.getBlipTypeWin32());
        assertEquals(bse1.getBlipTypeMacOS(), bse2.getBlipTypeMacOS());
        assertTrue(Arrays.equals(bse1.getUid(), bse2.getUid()));
        assertEquals(bse1.getTag(), bse2.getTag());
        assertEquals(bse1.getSize(), bse2.getSize());

        EscherBitmapBlip blip2 = (EscherBitmapBlip)bse1.getBlipRecord();
        assertEquals(blip1.getOptions(), blip2.getOptions());
        assertEquals(blip1.getRecordId(), blip2.getRecordId());
        assertEquals(blip1.getUID(), blip2.getUID());

        assertTrue(Arrays.equals(blip1.getPicturedata(), blip1.getPicturedata()));
    }

    //test reading/serializing of a PICT metafile
    public void testReadPICT() {
        //provided in bug-44886
        byte[] data = _samples.readFile("Container.dat");

        EscherContainerRecord record = new EscherContainerRecord();
        record.fillFields(data, 0, new DefaultEscherRecordFactory());
        EscherContainerRecord bstore = (EscherContainerRecord)record.getChild(1);
        EscherBSERecord bse1 = (EscherBSERecord)bstore.getChild(1);
        //System.out.println(bse1);
        assertEquals(EscherBSERecord.BT_WMF, bse1.getBlipTypeWin32());
        assertEquals(EscherBSERecord.BT_PICT, bse1.getBlipTypeMacOS());
        assertTrue(Arrays.equals(new byte[]{
            (byte)0xC7, 0x15, 0x69, 0x2D, (byte)0xE5, (byte)0x89, (byte)0xA3, 0x6F,
            0x66, 0x03, (byte)0xD6, 0x24, (byte)0xF7, (byte)0xDB, 0x1D, 0x13
        }, bse1.getUid()));
        assertEquals(255, bse1.getTag());
        assertEquals(1133, bse1.getSize());

        EscherMetafileBlip blip1 = (EscherMetafileBlip)bse1.getBlipRecord();
        assertEquals(0x5430, blip1.getOptions());
        assertEquals(EscherMetafileBlip.RECORD_ID_PICT, blip1.getRecordId());
        assertTrue(Arrays.equals(new byte[]{
            0x57, 0x32, 0x7B, (byte)0x91, 0x23, 0x5D, (byte)0xDB, 0x36,
            0x7A, (byte)0xDB, (byte)0xFF, 0x17, (byte)0xFE, (byte)0xF3, (byte)0xA7, 0x05
        }, blip1.getUID()));
        assertTrue(Arrays.equals(new byte[]{
            (byte)0xC7, 0x15, 0x69, 0x2D, (byte)0xE5, (byte)0x89, (byte)0xA3, 0x6F,
            0x66, 0x03, (byte)0xD6, 0x24, (byte)0xF7, (byte)0xDB, 0x1D, 0x13
        }, blip1.getPrimaryUID()));

        //serialize and read again
        byte[] ser = bse1.serialize();
        EscherBSERecord bse2 = new EscherBSERecord();
        bse2.fillFields(ser, 0, new DefaultEscherRecordFactory());
        assertEquals(bse1.getRecordId(), bse2.getRecordId());
        assertEquals(bse1.getOptions(), bse2.getOptions());
        assertEquals(bse1.getBlipTypeWin32(), bse2.getBlipTypeWin32());
        assertEquals(bse1.getBlipTypeMacOS(), bse2.getBlipTypeMacOS());
        assertTrue(Arrays.equals(bse1.getUid(), bse2.getUid()));
        assertEquals(bse1.getTag(), bse2.getTag());
        assertEquals(bse1.getSize(), bse2.getSize());

        EscherMetafileBlip blip2 = (EscherMetafileBlip)bse1.getBlipRecord();
        assertEquals(blip1.getOptions(), blip2.getOptions());
        assertEquals(blip1.getRecordId(), blip2.getRecordId());
        assertEquals(blip1.getUID(), blip2.getUID());
        assertEquals(blip1.getPrimaryUID(), blip2.getPrimaryUID());

        assertTrue(Arrays.equals(blip1.getPicturedata(), blip1.getPicturedata()));
    }

    //integral test: check that the read-write-read round trip is consistent
    public void testContainer() {
        byte[] data = _samples.readFile("Container.dat");

        EscherContainerRecord record = new EscherContainerRecord();
        record.fillFields(data, 0, new DefaultEscherRecordFactory());

        byte[] ser = record.serialize();
        assertTrue(Arrays.equals(data, ser));
    }

    private byte[] read(File file) {
        byte[] data = new byte[(int)file.length()];
        try {
            FileInputStream is = new FileInputStream(file);
            is.read(data);
            is.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return data;
    }

    /**
     * The test data was created from pl031405.xls attached to Bugzilla #47143
     */
    public void test47143() {
        byte[] data = _samples.readFile("47143.dat");
        EscherBSERecord bse = new EscherBSERecord();
        bse.fillFields(data, 0, new DefaultEscherRecordFactory());
        bse.toString(); //assert that toString() works
        assertTrue(bse.getBlipRecord() instanceof EscherMetafileBlip);

        EscherMetafileBlip blip = (EscherMetafileBlip)bse.getBlipRecord();
        blip.toString(); //assert that toString() works
        byte[] remaining = blip.getRemainingData();
        assertNotNull(remaining);

        byte[] ser = bse.serialize();  //serialize and assert against the source data
        assertTrue(Arrays.equals(data, ser));
    }
}
