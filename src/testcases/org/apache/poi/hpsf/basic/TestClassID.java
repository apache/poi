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

package org.apache.poi.hpsf.basic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.Locale;

import org.apache.poi.hpsf.ClassID;
import org.apache.poi.hpsf.ClassIDPredefined;
import org.junit.Test;

/**
 * Tests ClassID structure.
 */
public final class TestClassID {

    private static final byte[] BUF16 = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16};

    /**
     * Various tests of overridden .equals()
     */
    @Test
    public void testEquals() {
        ClassID clsidTest1 = new ClassID(BUF16, 0);
        ClassID clsidTest2 = new ClassID(BUF16, 0);
        byte[] buf2 = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,17};
        ClassID clsidTest3 = new ClassID(buf2, 0);
        assertEquals(clsidTest1, clsidTest1);
        assertEquals(clsidTest1, clsidTest2);
        assertNotEquals(clsidTest1, clsidTest3);
        assertNotEquals(null, clsidTest1);
    }

    /**
     * Try to write to a buffer that is too small. This should
     *   throw an Exception
     */
    @Test(expected = ArrayStoreException.class)
    public void testWriteArrayStoreException1() {
        new ClassID(BUF16, 0).write(new byte[15], 0);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testWriteArrayStoreException2() {
        new ClassID(BUF16, 0).write(new byte[16], 1);
    }

    @Test
    public void testWriteArrayStoreException3() {
        ClassID clsidTest = new ClassID(BUF16, 0);
        clsidTest.write(new byte[16], 0);
        clsidTest.write(new byte[17], 1);
    }

    @Test
    public void testClassID() {
        ClassID clsidTest = new ClassID(BUF16, 0);
        assertEquals("{04030201-0605-0807-090A-0B0C0D0E0F10}", clsidTest.toString());
    }

    @Test
    public void checkUUIDConversion() {
        String exp = "EABCECDB-CC1C-4A6F-B4E3-7F888A5ADFC8";
        ClassID clsId = ClassIDPredefined.EXCEL_V14_ODS.getClassID();
        assertEquals(exp, clsId.toUUIDString());
        assertEquals(exp, clsId.toUUID().toString().toUpperCase(Locale.ROOT));
    }
}
