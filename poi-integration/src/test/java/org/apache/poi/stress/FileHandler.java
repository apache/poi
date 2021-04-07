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
package org.apache.poi.stress;

import java.io.File;
import java.io.InputStream;

/**
 * Base interface for the various file types that are
 * used in the stress testing.
 */
public interface FileHandler {
	/**
	 * The FileHandler receives a stream ready for reading the
	 * file and should handle the content that is provided and
	 * try to read and interpret the data.
	 *
	 * Closing is handled by the framework outside this call.
	 *
	 * @param stream The input stream to read the file from.
	 * @param path the relative path to the file
	 * @throws Exception If an error happens in the file-specific handler
	 */
	void handleFile(InputStream stream, String path) throws Exception;

	/**
	 * Ensures that extracting text from the given file
	 * is returning some text.
	 */
	void handleExtracting(File file) throws Exception;

	/**
	 * Allows to perform some additional work, e.g. run
	 * some of the example applications
	 */
	void handleAdditional(File file) throws Exception;
}
