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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.zip.ZipEntry;

import org.apache.poi.openxml4j.util.ZipSecureFile.ThresholdInputStream;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * Provides a way to get at all the ZipEntries
 *  from a ZipInputStream, as many times as required.
 * Allows a ZipInputStream to be treated much like
 *  a ZipFile, for a price in terms of memory.
 * Be sure to call {@link #close()} as soon as you're
 *  done, to free up that memory!
 */
public class ZipInputStreamZipEntrySource implements ZipEntrySource {
    private static POILogger logger = POILogFactory.getLogger(ZipInputStreamZipEntrySource.class);
	private ArrayList<FakeZipEntry> zipEntries;
	
	/**
	 * Reads all the entries from the ZipInputStream 
	 *  into memory, and closes the source stream.
	 * We'll then eat lots of memory, but be able to
	 *  work with the entries at-will.
	 */
	public ZipInputStreamZipEntrySource(ThresholdInputStream inp) throws IOException {
		zipEntries = new ArrayList<FakeZipEntry>();
		
		boolean going = true;
		while(going) {
			ZipEntry zipEntry = inp.getNextEntry();
			if(zipEntry == null) {
				going = false;
			} else {
				FakeZipEntry entry = FakeZipEntry.STRATEGY.createFakeZipEntry(zipEntry, inp);
				inp.closeEntry();
				
				zipEntries.add(entry);
			}
		}
		inp.close();
	}

	public Enumeration<? extends ZipEntry> getEntries() {
		return new EntryEnumerator();
	}
	
	public InputStream getInputStream(ZipEntry zipEntry) {
	    assert (zipEntry instanceof FakeZipEntry);
		FakeZipEntry entry = (FakeZipEntry)zipEntry;
		return entry.getInputStream();
	}
	
	public void close() {
	    if(zipEntries != null) {
    	    for(FakeZipEntry zipEntry : zipEntries) {
    	        try {
    	            zipEntry.close();
    	        } catch(Throwable t) {
    	            logger.log(POILogger.WARN, "Cannot close zip entry", t);
    	        }
    	    }
            // Free the memory
            zipEntries = null;
	    }
	}
	
	/**
	 * Why oh why oh why are Iterator and Enumeration
	 *  still not compatible?
	 */
	private class EntryEnumerator implements Enumeration<ZipEntry> {
		private Iterator<? extends ZipEntry> iterator;
		
		private EntryEnumerator() {
			iterator = zipEntries.iterator();
		}
		
		public boolean hasMoreElements() {
			return iterator.hasNext();
		}

		public ZipEntry nextElement() {
			return iterator.next();
		}
	}
}
