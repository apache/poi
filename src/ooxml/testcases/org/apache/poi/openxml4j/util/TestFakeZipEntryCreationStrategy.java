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

package org.apache.poi.openxml4j.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.util.zip.ZipEntry;

import org.apache.poi.util.IOUtils;
import org.apache.poi.util.TempFile;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestFakeZipEntryCreationStrategy {
    
    private int fileCount = 0;
    
    @Before
    public void setUp() {
        fileCount = 0;
        FakeZipEntry.setFakeZipEntryCreationStrategy(new TempFileFakeZipEntryCreationStrategy());
    }

    @After
    public void tearDown() {
        FakeZipEntry.setFakeZipEntryCreationStrategy(new DefaultFakeZipEntryCreationStrategy());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testSetNullStrategy() throws Exception {
        FakeZipEntry.setFakeZipEntryCreationStrategy(null);
    }
    
    @Test
    public void testCustomStrategy() throws Exception {
        byte[] bytes = new byte[16];
        new Random().nextBytes(bytes);
        assertTrue(FakeZipEntry.STRATEGY instanceof TempFileFakeZipEntryCreationStrategy);
        FakeZipEntry zipEntry = FakeZipEntry.STRATEGY.createFakeZipEntry(new ZipEntry("name"), new ByteArrayInputStream(bytes));
        assertEquals(1, fileCount);
        assertNotNull(zipEntry);
        assertNotNull(zipEntry.getInputStream());
    }
    
    private class TempFileFakeZipEntryCreationStrategy implements FakeZipEntryCreationStrategy {

        public FakeZipEntry createFakeZipEntry(ZipEntry entry, InputStream inputStream) throws IOException {
            return new TempFileZipEntry(entry, inputStream);
        }    
    }
    
    private class TempFileZipEntry extends FakeZipEntry {
        private File file;
        
        public TempFileZipEntry(ZipEntry entry, InputStream inp) throws IOException {
            super(entry.getName());
            fileCount++;
            file = TempFile.createTempFile("fakezipentry", ".tmp");
            FileOutputStream fos = new FileOutputStream(file);
            try {
                byte[] buffer = new byte[4096];
                int read = 0;
                while( (read = inp.read(buffer)) != -1 ) {
                    fos.write(buffer, 0, read);
                }
            } finally {
                IOUtils.closeQuietly(fos);
            }
        }
        
        public InputStream getInputStream() throws IOException {
            return new FileInputStream(file);
        }

        public void close() throws IOException {
            if(file != null) {
                file.delete();
                file = null;
            }
        }
    }
}
