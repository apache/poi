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

package org.apache.poi.extractor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.Entry;
import org.apache.poi.poifs.filesystem.FileMagic;

public interface ExtractorProvider {
    boolean accepts(FileMagic fm);

    /**
     * Create Extractor via file
     * @param file the file
     * @param password the password or {@code null} if not encrypted
     * @return the extractor
     * @throws IOException if file can't be read or parsed
     */
    POITextExtractor create(File file, String password) throws IOException;

    /**
     * Create Extractor via InputStream
     * @param inputStream the stream
     * @param password the password or {@code null} if not encrypted
     * @return the extractor
     * @throws IOException if stream can't be read or parsed
     */
    POITextExtractor create(InputStream inputStream, String password) throws IOException;

    /**
     * Create Extractor from POIFS node
     * @param poifsDir the node
     * @param password the password or {@code null} if not encrypted
     * @return the extractor
     * @throws IOException if node can't be parsed
     * @throws IllegalStateException if processing fails for some other reason,
     *              e.g. missing JCE Unlimited Strength Jurisdiction Policy files
     *              while handling encrypted files.
     */
    POITextExtractor create(DirectoryNode poifsDir, String password) throws IOException;

    /**
     * Returns an array of text extractors, one for each of
     *  the embedded documents in the file (if there are any).
     * If there are no embedded documents, you'll get back an
     *  empty array. Otherwise, you'll get one open
     *  {@link POITextExtractor} for each embedded file.
     *
     * @param ext the extractor holding the directory to start parsing
     * @param dirs a list to be filled with directory references holding embedded
     * @param nonPOIFS a list to be filled with streams which aren't based on POIFS entries
     *
     * @throws IOException when the format specific extraction fails because of invalid entires
     */
    default void identifyEmbeddedResources(POIOLE2TextExtractor ext, List<Entry> dirs, List<InputStream> nonPOIFS) throws IOException {
        throw new IllegalArgumentException("Error checking for Scratchpad embedded resources");
    }

}
