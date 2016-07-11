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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;

public class DefaultFakeZipEntryCreationStrategy implements FakeZipEntryCreationStrategy {

    public FakeZipEntry createFakeZipEntry(final ZipEntry entry, final InputStream inputStream) throws IOException {
        return new InMemoryZipEntry(entry, inputStream);
    }
    
    /**
     * So we can close the real zip entry and still
     *  effectively work with it.
     * Holds the (decompressed!) data in memory, so
     *  close this as soon as you can! 
     */
    static class InMemoryZipEntry extends FakeZipEntry {
        private byte[] data;
        
        public InMemoryZipEntry(ZipEntry entry, InputStream inp) throws IOException {
            super(entry.getName());
            
            // Grab the de-compressed contents for later
            ByteArrayOutputStream baos;

            long entrySize = entry.getSize();

            if (entrySize !=-1) {
                if (entrySize>=Integer.MAX_VALUE) {
                    throw new IOException("ZIP entry size is too large");
                }

                baos = new ByteArrayOutputStream((int) entrySize);
            } else {
                baos = new ByteArrayOutputStream();
            }

            byte[] buffer = new byte[4096];
            int read = 0;
            while( (read = inp.read(buffer)) != -1 ) {
                baos.write(buffer, 0, read);
            }
            
            data = baos.toByteArray();
        }
        
        public InputStream getInputStream() {
            return new ByteArrayInputStream(data);
        }

        public void close() throws IOException {
            data = null;
        }
    }

}
