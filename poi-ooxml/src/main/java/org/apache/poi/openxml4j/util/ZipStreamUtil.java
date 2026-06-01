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

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.poi.util.Internal;

/**
 * Small utility to iterate a {@link ZipArchiveThresholdInputStream} and
 * execute a {@link ZipEntryProcessor} for every entry. Kept intentionally
 * minimal and annotated {@link Internal} so maintainers can review it as
 * an internal helper rather than a public API.
 */
@Internal
public final class ZipStreamUtil {

    private ZipStreamUtil() {
        // utility
    }

    public static void streamEntries(ZipArchiveThresholdInputStream zisThreshold, ZipEntryProcessor processor) throws IOException {
        // Iterate entries using package-private getNextEntry() which preserves
        // ZipSecureFile enforcement (entry count, size and inflate ratio checks).
        ZipArchiveEntry ze;
        while ((ze = zisThreshold.getNextEntry()) != null) {
            processor.process(ze, zisThreshold);
        }
    }
}
