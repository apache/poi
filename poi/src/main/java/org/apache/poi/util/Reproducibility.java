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

package org.apache.poi.util;

import java.io.IOException;

import org.apache.commons.io.function.IORunnable;

/**
 * Helper class for allowing to produce so called
 * "reproducible" output.
 *
 * I.e. multiple runs of the same steps should
 * produce the same byte-by-byte result.
 *
 * This usually means that among other "randomness"
 * timestamp should be avoided.
 *
 * This class provides a few useful bits to allow Apache POI to produce
 * reproducible binary files.
 *
 * See https://reproducible-builds.org/ for more details.
 */
public class Reproducibility {
    // Add some support for reproducible output files
    // if SOURCE_DATE_EPOCH is set, we use timestamp "0" for
    // entries in Zip files
    // See https://reproducible-builds.org/docs/source-date-epoch/
    // for the specification of SOURCE_DATE_EPOCH
    private static boolean IS_SOURCE_DATE_EPOCH =
            System.getenv("SOURCE_DATE_EPOCH") != null;

    /**
     * Check if the environment variable SOURCE_DATE_EPOCH is set.
     *
     * @return True if set, false otherwise
     */
    public static boolean isSourceDateEpoch() {
        return IS_SOURCE_DATE_EPOCH;
    }

    /**
     * Execute a runnable with SOURCE_DATE_EPOCH set.
     *
     * This is mostly only used in tests to check reproducibility
     * of documents.
     *
     * @param r A runnable which executes the wanted steps with
     *          SOURCE_DATE_EPOCH defined
     *
     * @throws IOException if executing the runnable throws an IOException
     * @throws RuntimeException if executing the runnable throws a RuntimeException
     */
    public static void runWithSourceDateEpoch(IORunnable r) throws IOException {
        boolean before = IS_SOURCE_DATE_EPOCH;
        IS_SOURCE_DATE_EPOCH = true;
        try {
            r.run();
        } finally {
            IS_SOURCE_DATE_EPOCH = before;
        }
    }
}
