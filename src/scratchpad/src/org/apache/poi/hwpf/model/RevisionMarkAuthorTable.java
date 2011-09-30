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

package org.apache.poi.hwpf.model;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.poi.hwpf.model.io.HWPFOutputStream;
import org.apache.poi.util.Internal;

/**
 * String table containing the names of authors of revision marks, e-mails and
 * comments in this document.
 * 
 * @author Ryan Lauck
 */
@Internal
public final class RevisionMarkAuthorTable {
	/**
	 * Array of entries.
	 */
	private String[] entries;

	/**
	 * Constructor to read the table from the table stream.
	 * 
	 * @param tableStream the table stream.
	 * @param offset the offset into the byte array.
	 * @param size the size of the table in the byte array.
	 */
    public RevisionMarkAuthorTable( byte[] tableStream, int offset, int size )
            throws IOException
    {
        entries = SttbUtils.readSttbfRMark( tableStream, offset );
    }

	/**
	 * Gets the entries. The returned list cannot be modified.
	 * 
	 * @return the list of entries.
	 */
	public List<String> getEntries() {
		return Collections.unmodifiableList(Arrays.asList(entries));
	}
	
	/**
	 * Get an author by its index.  Returns null if it does not exist.
	 * 
	 * @return the revision mark author
	 */
	public String getAuthor(int index) {
		String auth = null;
		if(index >= 0 && index < entries.length) {
			auth = entries[index];
		}
		return auth;
	}
	
	/**
	 * Gets the number of entries.
	 * 
	 * @return the number of entries.
	 */
	public int getSize() {
		return entries.length;
	}

	/**
	 * Writes this table to the table stream.
	 * 
	 * @param tableStream  the table stream to write to.
	 * @throws IOException  if an error occurs while writing.
	 */
    public void writeTo( HWPFOutputStream tableStream ) throws IOException
    {
        SttbUtils.writeSttbfRMark( entries, tableStream );
    }

}
