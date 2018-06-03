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

import static org.apache.commons.collections4.IteratorUtils.asIterable;
import static org.apache.commons.collections4.IteratorUtils.asIterator;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

/**
 * A ZipEntrySource wrapper around a ZipFile.
 * Should be as low in terms of memory as a
 *  normal ZipFile implementation is.
 */
public class ZipFileZipEntrySource implements ZipEntrySource {
   private ZipFile zipArchive;
   public ZipFileZipEntrySource(ZipFile zipFile) {
      this.zipArchive = zipFile;
   }

   @Override
   public void close() throws IOException {
      if(zipArchive != null) {
         zipArchive.close();
      }
      zipArchive = null;
   }

   @Override
   public boolean isClosed() {
       return (zipArchive == null);
   }

   @Override
   public Enumeration<? extends ZipArchiveEntry> getEntries() {
      if (zipArchive == null)
         throw new IllegalStateException("Zip File is closed");
      
      return zipArchive.getEntries();
   }

   @Override
   public InputStream getInputStream(ZipArchiveEntry entry) throws IOException {
      if (zipArchive == null)
         throw new IllegalStateException("Zip File is closed");
      
      return zipArchive.getInputStream(entry);
   }

   @Override
   public ZipArchiveEntry getEntry(final String path) {
      String normalizedPath = path.replace('\\', '/');

      final ZipArchiveEntry entry = zipArchive.getEntry(normalizedPath);
      if (entry != null) {
         return entry;
      }

      // the opc spec allows case-insensitive filename matching (see #49609)
      for (final ZipArchiveEntry ze : asIterable(asIterator(zipArchive.getEntries()))) {
         if (normalizedPath.equalsIgnoreCase(ze.getName().replace('\\','/'))) {
            return ze;
         }
      }

      return null;
   }
}
