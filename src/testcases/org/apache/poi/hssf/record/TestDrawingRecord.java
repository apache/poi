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

import static org.junit.Assert.assertArrayEquals;

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
        assertEquals(2, rec.size());
        assertTrue(rec.get(0) instanceof DrawingRecord);
        assertTrue(rec.get(1) instanceof ContinueRecord);

        assertArrayEquals(data1, ((DrawingRecord)rec.get(0)).getRecordData());
        assertArrayEquals(data2, ((ContinueRecord)rec.get(1)).getData());

    }

}
