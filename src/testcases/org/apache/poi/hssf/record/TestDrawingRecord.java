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

package org.apache.poi.hssf.record;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

public final class TestDrawingRecord extends TestCase {

    /**
     * Check that RecordFactoryInputStream properly handles continued DrawingRecords
     * See Bugzilla #47548
     */
    public void testReadContinued() throws IOException {

        //simulate a continues drawing record
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        //main part
        DrawingRecord dg = new DrawingRecord();
        byte[] data1 = new byte[8224];
        Arrays.fill(data1, (byte)1);
        dg.setData(data1);
        out.write(dg.serialize());

        //continued part
        byte[] data2 = new byte[4048];
        Arrays.fill(data2, (byte)2);
        ContinueRecord cn = new ContinueRecord(data2);
        out.write(cn.serialize());

        List<Record> rec = RecordFactory.createRecords(new ByteArrayInputStream(out.toByteArray()));
        assertEquals(1, rec.size());
        assertTrue(rec.get(0) instanceof DrawingRecord);

        //DrawingRecord.getData() should return concatenated data1 and data2
        byte[] tmp = new byte[data1.length + data2.length];
        System.arraycopy(data1, 0, tmp, 0, data1.length);
        System.arraycopy(data2, 0, tmp, data1.length, data2.length);

        DrawingRecord dg2 = (DrawingRecord)rec.get(0);
        assertEquals(data1.length + data2.length, dg2.getData().length);
        assertTrue(Arrays.equals(tmp, dg2.getData()));

    }

}
