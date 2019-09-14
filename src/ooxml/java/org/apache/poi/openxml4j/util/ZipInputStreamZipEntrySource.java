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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;

/**
 * Provides a way to get at all the ZipEntries
 *  from a ZipInputStream, as many times as required.
 * Allows a ZipInputStream to be treated much like
 *  a ZipFile, for a price in terms of memory.
 * Be sure to call {@link #close()} as soon as you're
 *  done, to free up that memory!
 */
public class ZipInputStreamZipEntrySource implements ZipEntrySource {
	private final Map<String, ZipArchiveFakeEntry> zipEntries = new HashMap<>();

	private InputStream streamToClose;

	/**
	 * Reads all the entries from the ZipInputStream 
	 *  into memory, and don't close (since POI 4.0.1) the source stream.
	 * We'll then eat lots of memory, but be able to
	 *  work with the entries at-will.
	 */
	public ZipInputStreamZipEntrySource(ZipArchiveThresholdInputStream inp) throws IOException {
		for (;;) {
			final ZipArchiveEntry zipEntry = inp.getNextEntry();
			if (zipEntry == null) {
				break;
			}
			zipEntries.put(zipEntry.getName(), new ZipArchiveFakeEntry(zipEntry, inp));
		}

		streamToClose = inp;
	}

	@Override
	public Enumeration<? extends ZipArchiveEntry> getEntries() {
		return Collections.enumeration(zipEntries.values());
	}

	@Override
	public InputStream getInputStream(ZipArchiveEntry zipEntry) {
	    assert (zipEntry instanceof ZipArchiveFakeEntry);
		return ((ZipArchiveFakeEntry)zipEntry).getInputStream();
	}

	@Override
	public void close() throws IOException {
		// Free the memory
		zipEntries.clear();

		streamToClose.close();
	}

	@Override
	public boolean isClosed() {
	    return zipEntries.isEmpty();
	}

	@Override
	public ZipArchiveEntry getEntry(final String path) {
		final String normalizedPath = path.replace('\\', '/');
		final ZipArchiveEntry ze = zipEntries.get(normalizedPath);
		if (ze != null) {
			return ze;
		}

		for (final Map.Entry<String, ZipArchiveFakeEntry> fze : zipEntries.entrySet()) {
			if (normalizedPath.equalsIgnoreCase(fze.getKey())) {
				return fze.getValue();
			}
		}

		return null;
	}
}
