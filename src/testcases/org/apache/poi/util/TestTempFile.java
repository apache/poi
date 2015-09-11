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
package org.apache.poi.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.Test;

/**
 * @author Glen Stampoultzis
 */
public class TestTempFile {
    @Test
    public void testCreateTempFile()
            throws Exception
    {
        File tempFile = TempFile.createTempFile("test", ".txt");
        FileOutputStream fos = new FileOutputStream(tempFile);
        fos.write(1);
        fos.close();
        assertTrue(tempFile.exists());
        assertEquals("poifiles", tempFile.getParentFile().getName());

        // Can't think of a good way to check whether a file is actually deleted since it would require the VM to stop.
    }
    
    @Test
    public void testConstructor() {
        // can currently be constructed...
        new TempFile();
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testSetTempFileCreationStrategy() throws IOException {
        TempFile.setTempFileCreationStrategy(new TempFile.DefaultTempFileCreationStrategy());
        
        File file1 = TempFile.createTempFile("TestTempFile", ".tst");
        File file2 = TempFile.createTempFile("TestTempFile", ".tst");
        assertFalse(file1.equals(file2));
        assertNotNull(file2);
        assertTrue(file2.delete());
        assertNotNull(file1);
        assertTrue(file1.delete());
        
        TempFile.setTempFileCreationStrategy(null);
    }
}
