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

import java.io.File;
import java.io.IOException;

/**
 * Interface for creating temporary files. Collects them all into one directory by default.
 */
public final class TempFile {
    /** The strategy used by {@link #createTempFile(String, String)} to create the temporary files. */
    private static TempFileCreationStrategy strategy = new DefaultTempFileCreationStrategy();

    /** Define a constant for this property as it is sometimes mistypes as "tempdir" otherwise */
    public static final String JAVA_IO_TMPDIR = "java.io.tmpdir";
    
    private TempFile() {
        // no instances of this class
    }

    /**
     * Configures the strategy used by {@link #createTempFile(String, String)} to create the temporary files.
     *
     * @param strategy The new strategy to be used to create the temporary files.
     * 
     * @throws IllegalArgumentException When the given strategy is <code>null</code>.
     */
    public static void setTempFileCreationStrategy(TempFileCreationStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("strategy == null");
        }
        TempFile.strategy = strategy;
    }
    
    /**
     * Creates a new and empty temporary file. By default, files are collected into one directory and are
     * deleted on exit from the VM, although they can be kept by defining the system property
     * <code>poi.keep.tmp.files</code> (see {@link DefaultTempFileCreationStrategy}).
     * <p>
     * Don't forget to close all files or it might not be possible to delete them.
     *
     * @param prefix The prefix to be used to generate the name of the temporary file.
     * @param suffix The suffix to be used to generate the name of the temporary file.
     * 
     * @return The path to the newly created and empty temporary file.
     * 
     * @throws IOException If no temporary file could be created.
     */
    public static File createTempFile(String prefix, String suffix) throws IOException {
        return strategy.createTempFile(prefix, suffix);
    }
    
    public static File createTempDirectory(String name) throws IOException {
        return strategy.createTempDirectory(name);
    }
}
