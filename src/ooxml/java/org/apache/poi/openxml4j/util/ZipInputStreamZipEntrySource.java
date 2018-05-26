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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;

import org.apache.commons.collections4.IteratorUtils;

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
	
	/**
	 * Reads all the entries from the ZipInputStream 
	 *  into memory, and closes the source stream.
	 * We'll then eat lots of memory, but be able to
	 *  work with the entries at-will.
	 */
	public ZipInputStreamZipEntrySource(ZipArchiveThresholdInputStream inp) throws IOException {
		for (;;) {
			final ZipEntry zipEntry = inp.getNextEntry();
			if (zipEntry == null) {
				break;
			}
			zipEntries.put(zipEntry.getName(), new ZipArchiveFakeEntry(zipEntry, inp));
		}
		inp.close();
	}

	@Override
	public Enumeration<? extends ZipEntry> getEntries() {
		return IteratorUtils.asEnumeration(zipEntries.values().iterator());
	}

	@Override
	public InputStream getInputStream(ZipEntry zipEntry) {
	    assert (zipEntry instanceof ZipArchiveFakeEntry);
		return ((ZipArchiveFakeEntry)zipEntry).getInputStream();
	}

	@Override
	public void close() {
		// Free the memory
		zipEntries.clear();
	}

	@Override
	public boolean isClosed() {
	    return zipEntries.isEmpty();
	}

	@Override
	public ZipEntry getEntry(final String path) {
		final String normalizedPath = path.replace('\\', '/');
		final ZipEntry ze = zipEntries.get(normalizedPath);
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
