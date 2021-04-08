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
package org.apache.poi.ss.extractor;

/**
 * Common interface for Excel text extractors, covering
 *  HSSF and XSSF
 */
public interface ExcelExtractor {
    /**
     * Should sheet names be included?
     * Default is true
     *
     * @param includeSheetNames {@code true} if the sheet names should be included
     */
    void setIncludeSheetNames(boolean includeSheetNames);

    /**
     * Should we return the formula itself, and not the result it produces?
     * Default is false
     *
     * @param formulasNotResults {@code true} if the formula itself is returned
     */
    void setFormulasNotResults(boolean formulasNotResults);

    /**
     * Should headers and footers be included in the output?
     * Default is true
     *
     * @param includeHeadersFooters {@code true} if headers and footers should be included
     */
    void setIncludeHeadersFooters(boolean includeHeadersFooters);

    /**
     * Should cell comments be included?
     * Default is false
     *
     * @param includeCellComments {@code true} if cell comments should be included
     */
    void setIncludeCellComments(boolean includeCellComments);

    /**
     * Retrieves the text contents of the file
     *
     * @return the text contents of the file
     */
    String getText();
}
