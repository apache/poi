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

package org.apache.poi.hpsf.wellknown;

import org.apache.poi.hpsf.ClassID;
import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.util.Internal;
import org.apache.poi.util.Removal;

/**
 * This classed used to map section format IDs to {@link PropertyIDMap PropertyIDMaps},
 * but there's no way to use custom PropertyIDMaps.<p>
 * 
 * It is only kept for its ClassIDs until removal.
 * 
 * @deprecated in 4.0.0, there's no way to create custom PropertyIDMaps, therefore
 *   this class is obsolete
 */
@Internal
@Deprecated
@Removal(version="4.2.0")
public class SectionIDMap {

    /**
     * The SummaryInformation's section's format ID.
     * @deprecated use {@link SummaryInformation#FORMAT_ID}
     */
    @Deprecated
    public static final ClassID SUMMARY_INFORMATION_ID = SummaryInformation.FORMAT_ID;

    /**
     * The DocumentSummaryInformation's first and second sections' format ID.
     * @deprecated use {@link DocumentSummaryInformation#FORMAT_ID}
     */
    @Deprecated
    public static final ClassID[] DOCUMENT_SUMMARY_INFORMATION_ID = DocumentSummaryInformation.FORMAT_ID;

    /**
     * A property without a known name is described by this string.
     * @deprecated use {@link PropertyIDMap#UNDEFINED}
     */
    @Deprecated
    public static final String UNDEFINED = PropertyIDMap.UNDEFINED;
}
