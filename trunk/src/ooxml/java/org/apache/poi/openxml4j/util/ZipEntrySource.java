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
import java.util.zip.ZipEntry;

/**
 * An Interface to make getting the different bits
 *  of a Zip File easy.
 * Allows you to get at the ZipEntries, without
 *  needing to worry about ZipFile vs ZipInputStream
 *  being annoyingly very different.
 */
public interface ZipEntrySource {
	/**
	 * Returns an Enumeration of all the Entries
	 */
	public Enumeration<? extends ZipEntry> getEntries();
	
	/**
	 * Returns an InputStream of the decompressed 
	 *  data that makes up the entry
	 */
	public InputStream getInputStream(ZipEntry entry) throws IOException;
	
	/**
	 * Indicates we are done with reading, and 
	 *  resources may be freed
	 */
	public void close() throws IOException;
}
