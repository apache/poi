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


import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests that {@link ExOleObjStg} works properly
 */
public final class TestExOleObjStg {

    // From a real file (embedded SWF control)
    // <ExOleObjStg info="16" type="4113" size="347" offset="4322" header="10 00 11 10 5B 01 00 00 ">....
    private static byte[] data;
    
    @BeforeClass
    public static void init() throws IOException {
        data = org.apache.poi.poifs.storage.RawDataUtil.decompress(
        "H4sIAAAAAAAAAAFjAZz+EAAREFsBAAAADgAAeJy7cF7wwcKNUg8Z0IAdAzPDv/+cDGxIYoxAzATjCDAwsEDF/"+
        "v3//x8kxAzE/0fBkAJBDPlAWMKgwODKkAekixgq0ZMCXiDGwAqPc1BayLtdcyl33XnBaTtcXINDUNUGvEgvK2"+
        "Y4ycgOZDswQsScgbaD7E0Fk8Uk2Q0CQgxMjMj+IVafAiPJVuEE5NhPTUCJ/aC8+xdLuvgHxaNgeAN65v8JZ1k"+
        "bQfmflWE0/1MTUGI/TB+sHBjN9yMLwNp0oLYbqD03GvcjC6SHpoUKABtkbzghmAPawudgkAGSaQw5DGUMBUAy"+
        "EVgvpAJrBz1gGV0OFCdOBR+QDGfIBJbtKcByvhzI4wbj//85GLiALA+gXDpDBlgtqKfhwxACl4e4AuYaYHeDI"+
        "RioEmSKI9C2HLhZyKqQ9fBilHXcwN4KN1wdM1Q1iJcINZGDgQfsJxC/GOib4Q8AvWU91AJ49g1jAQAA"
        );
    }

    @Test
    public void testRead() throws Exception {
        ExOleObjStg record = new ExOleObjStg(data, 0, data.length);
        assertEquals(RecordTypes.ExOleObjStg.typeID, record.getRecordType());

        int len = record.getDataLength();
        byte[] oledata = readAll(record.getData());
        assertEquals(len, oledata.length);

        POIFSFileSystem fs = new POIFSFileSystem(record.getData());
        assertTrue("Constructed POIFS from ExOleObjStg data", true);
        DocumentEntry doc = (DocumentEntry)fs.getRoot().getEntry("Contents");
        assertNotNull(doc);
        assertTrue("Fetched the Contents stream containing OLE properties", true);
        fs.close();
    }

    @Test
    public void testWrite() throws Exception {
        ExOleObjStg record = new ExOleObjStg(data, 0, data.length);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        record.writeOut(baos);
        byte[] b = baos.toByteArray();

        assertArrayEquals(data, b);
    }

    @Test
    public void testNewRecord() throws Exception {
        ExOleObjStg src = new ExOleObjStg(data, 0, data.length);
        byte[] oledata = readAll(src.getData());

        ExOleObjStg tgt = new ExOleObjStg();
        tgt.setData(oledata);


        assertEquals(src.getDataLength(), tgt.getDataLength());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        tgt.writeOut(out);
        byte[] b = out.toByteArray();

        assertEquals(data.length, b.length);
        assertArrayEquals(data, b);
    }

    private byte[] readAll(InputStream is) throws IOException {
        int pos;
        byte[] chunk = new byte[1024];
        ByteArrayOutputStream out = new  ByteArrayOutputStream();
        while((pos = is.read(chunk)) > 0){
            out.write(chunk, 0, pos);
        }
        return out.toByteArray();

    }
}
