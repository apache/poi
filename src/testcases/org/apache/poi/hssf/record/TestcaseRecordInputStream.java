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

import junit.framework.Assert;

import org.apache.poi.util.LittleEndian;

/**
 * A Record Input Stream derivative that makes access to byte arrays used in the
 * test cases work a bit easier.
 * <p> Creates the sream and moves to the first record.
 *
 * @author Jason Height (jheight at apache.org)
 */
public class TestcaseRecordInputStream
        extends RecordInputStream
{
    /**
     * Convenience constructor
     */
    public TestcaseRecordInputStream(int sid, byte[] data)
    {
      super(new ByteArrayInputStream(mergeDataAndSid((short)sid, (short)data.length, data)));
      nextRecord();
    }
    public TestcaseRecordInputStream(short sid, short length, byte[] data)
    {
      super(new ByteArrayInputStream(mergeDataAndSid(sid, length, data)));
      nextRecord();
    }

    public static byte[] mergeDataAndSid(short sid, short length, byte[] data) {
      byte[] result = new byte[data.length + 4];
      LittleEndian.putShort(result, 0, sid);
      LittleEndian.putShort(result, 2, length);
      System.arraycopy(data, 0, result, 4, data.length);
      return result;
    }
    /**
     * Confirms data sections are equal
     * @param expectedData - just raw data (without sid or size short ints)
     * @param actualRecordBytes this includes 4 prefix bytes (sid & size)
     */
    public static void confirmRecordEncoding(int expectedSid, byte[] expectedData, byte[] actualRecordBytes) {
        int expectedDataSize = expectedData.length;
        Assert.assertEquals(actualRecordBytes.length - 4, expectedDataSize);
        Assert.assertEquals(expectedSid, LittleEndian.getShort(actualRecordBytes, 0));
        Assert.assertEquals(expectedDataSize, LittleEndian.getShort(actualRecordBytes, 2));
        for (int i = 0; i < expectedDataSize; i++)
            Assert.assertEquals("At offset " + i, expectedData[i], actualRecordBytes[i+4]);
        
    }
}
