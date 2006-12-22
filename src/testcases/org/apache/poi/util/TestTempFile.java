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

import junit.framework.TestCase;

import java.io.File;
import java.io.FileWriter;

/**
 * @author Glen Stampoultzis
 */
public class TestTempFile extends TestCase {
    TempFile tempFile;

    public void testCreateTempFile()
            throws Exception
    {
        File tempFile = TempFile.createTempFile("test", ".txt");
        FileWriter w = new FileWriter(tempFile);
        w.write("testing");
        w.close();
        assertTrue(tempFile.exists());
        assertEquals("poifiles", tempFile.getParentFile().getName());

        // Can't think of a good way to check whether a file is actually deleted since it would require the VM to stop.
    }
}