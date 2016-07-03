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
import java.security.SecureRandom;

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
    
    /**
     * Default implementation of the {@link TempFileCreationStrategy} used by {@link TempFile}:
     * Files are collected into one directory and by default are deleted on exit from the VM. 
     * Files can be kept by defining the system property <code>poi.keep.tmp.files</code>.
     */
    public static class DefaultTempFileCreationStrategy implements TempFileCreationStrategy {
        
        /** The directory where the temporary files will be created (<code>null</code> to use the default directory). */
        private File dir;
        
        /**
         * Creates the strategy so that it creates the temporary files in the default directory.
         * 
         * @see File#createTempFile(String, String)
         */
        public DefaultTempFileCreationStrategy() {
            this(null);
        }
        
        /**
         * Creates the strategy allowing to set the  
         *
         * @param dir The directory where the temporary files will be created (<code>null</code> to use the default directory).
         * 
         * @see File#createTempFile(String, String, File)
         */
        public DefaultTempFileCreationStrategy(File dir) {
            this.dir = dir;
        }
        
        private void createPOIFilesDirectory() throws IOException {
            // Identify and create our temp dir, if needed
            // The directory is not deleted, even if it was created by this TempFleCreationStrategy
            if (dir == null) {
                String tmpDir = System.getProperty(JAVA_IO_TMPDIR);
                if (tmpDir == null) {
                    throw new IOException("Systems temporary directory not defined - set the -D"+JAVA_IO_TMPDIR+" jvm property!");
                }
                dir = new File(tmpDir, "poifiles");
            }
            
            createTempDirectory(dir);
        }
        
        private void createTempDirectory(File directory) throws IOException {
            if (!(directory.exists() || directory.mkdirs()) || !directory.isDirectory()) {
                throw new IOException("Could not create temporary directory '" + directory + "'");
            }
        }
        
        @Override
        public File createTempFile(String prefix, String suffix) throws IOException {
            // Identify and create our temp dir, if needed
            createPOIFilesDirectory();
            
            // Generate a unique new filename 
            File newFile = File.createTempFile(prefix, suffix, dir);

            // Set the delete on exit flag, unless explicitly disabled
            if (System.getProperty("poi.keep.tmp.files") == null) {
                newFile.deleteOnExit();
            }

            // All done
            return newFile;
        }
        
        private static final SecureRandom random = new SecureRandom();
        @Override
        public File createTempDirectory(String prefix) throws IOException {
            // Identify and create our temp dir, if needed
            createPOIFilesDirectory();
            
            // Generate a unique new filename
            // FIXME: Java 7+: use java.nio.Files#createTempDirectory
            final long n = random.nextLong();
            File newDirectory = new File(dir, prefix + Long.toString(n));
            createTempDirectory(newDirectory);

            // Set the delete on exit flag, unless explicitly disabled
            if (System.getProperty("poi.keep.tmp.files") == null) {
                newDirectory.deleteOnExit();
            }

            // All done
            return newDirectory;
        }
    }
}
