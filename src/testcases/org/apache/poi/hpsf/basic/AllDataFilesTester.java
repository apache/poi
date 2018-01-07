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

package org.apache.poi.hpsf.basic;

import org.apache.poi.POIDataSamples;

import java.io.File;
import java.io.FileFilter;
import java.util.logging.Logger;

/**
 * <p>Processes a test method for all OLE2 files in the HPSF test data
 * directory. Well, this class does not check whether a file is an OLE2 file but
 * rather whether its name begins with "Test".</p>
 */
public class AllDataFilesTester {
    private static final POIDataSamples _samples = POIDataSamples.getHPSFInstance();

    /**
     * <p>Interface specifying how to run a test on a single file.</p>
     */
    public interface TestTask
    {
        /**
         * <p>Executes a test on a single file.</p>
         *
         * @param file the file
         * @throws Throwable if the method throws anything.
         */
        void runTest(File file) throws Throwable;
    }

    /**
     * <p>Tests the simplified custom properties.</p>
     *
     * @param task the task to execute
     * @throws Throwable 
     */
    public void runTests(final TestTask task) throws Throwable
    {
        POIDataSamples _samples = POIDataSamples.getHPSFInstance();
        final File dataDir = _samples.getFile("");
        final File[] docs = dataDir.listFiles(new FileFilter()
        {
            @Override
            public boolean accept(final File file)
            {
                return file.isFile() && file.getName().startsWith("Test");
            }});
        for (final File doc : docs) {
            final Logger logger = Logger.getLogger(getClass().getName());
            logger.info("Processing file \" " + doc + "\".");

            /* Execute the test task. */
            task.runTest(doc);
        }
    }

}
