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

import java.util.Locale;

import org.apache.poi.hpsf.ClassID;
import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.SummaryInformation;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * <p>Tests ClassID structure.</p>
 *
 * @author Michael Zalewski (zalewski@optonline.net)
 */
public final class TestClassID {

    /**
     * Various tests of overridden .equals()
     */
    @Test
    public void testEquals() {
        ClassID clsidTest1 = new ClassID(
              new byte[] {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
                          0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10}
            , 0
        );
        ClassID clsidTest2 = new ClassID(
              new byte[] {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
                          0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10}
            , 0
        );
        ClassID clsidTest3 = new ClassID(
              new byte[] {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
                          0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x11 }
            , 0
        );
        assertEquals(clsidTest1, clsidTest1);
        assertEquals(clsidTest1, clsidTest2);
        assertNotEquals(clsidTest1, clsidTest3);
        assertNotEquals(null, clsidTest1);
    }
    
    /**
     * Try to write to a buffer that is too small. This should
     *   throw an Exception
     */
    @Test
    public void testWriteArrayStoreException() {
        ClassID clsidTest = new ClassID(
              new byte[] {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
                          0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10}
            , 0
        );
        boolean bExceptionOccurred = false;
        try
        {
            clsidTest.write(new byte[15], 0);
        }
        catch (Exception e)
        {
            bExceptionOccurred = true;
        }
        assertTrue(bExceptionOccurred);

        bExceptionOccurred = false;
        try
        {
            clsidTest.write(new byte[16], 1);
        }
        catch (Exception e)
        {
            bExceptionOccurred = true;
        }
        assertTrue(bExceptionOccurred);

        // These should work without throwing an Exception
        bExceptionOccurred = false;
        try
        {
            clsidTest.write(new byte[16], 0);
            clsidTest.write(new byte[17], 1);
        }
        catch (Exception e)
        {
            bExceptionOccurred = true;
        }
        assertFalse(bExceptionOccurred);
    }

    /**
     * <p>Tests the {@link PropertySet} methods. The test file has two
     * property set: the first one is a {@link SummaryInformation},
     * the second one is a {@link DocumentSummaryInformation}.</p>
     */
    @Test
    public void testClassID() {
        ClassID clsidTest = new ClassID(
              new byte[] {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
                          0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10}
            , 0
        );
        assertEquals(clsidTest.toString().toUpperCase(Locale.ROOT),
                            "{04030201-0605-0807-090A-0B0C0D0E0F10}"
        );
    }
}
