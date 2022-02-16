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

import static org.apache.poi.util.TempFile.JAVA_IO_TMPDIR;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;

/**
 * Default implementation of the {@link TempFileCreationStrategy} used by {@link TempFile}:
 * Files are collected into one directory and by default are deleted on exit from the VM.
 * Files may be manually deleted by user prior to JVM exit.
 * Files can be kept by defining the system property {@link #DELETE_FILES_ON_EXIT}.
 *
 * Each file is registered for deletion with the JVM and the temporary directory is not deleted
 * after the JVM exits. Files that are created in the poifiles directory outside
 * the control of DefaultTempFileCreationStrategy are not deleted.
 * See {@link TempFileCreationStrategy} for better strategies for long-running
 * processes or limited temporary storage.
 */
public class DefaultTempFileCreationStrategy implements TempFileCreationStrategy {
    public static final String POIFILES = "poifiles";

    /** To use files.deleteOnExit after clean JVM exit, set the <code>-Dpoi.delete.tmp.files.on.exit</code> JVM property */
    public static final String DELETE_FILES_ON_EXIT = "poi.delete.tmp.files.on.exit";

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
     * @see Files#createTempFile(Path, String, String, FileAttribute[]) 
     */
    public DefaultTempFileCreationStrategy(File dir) {
        this.dir = dir;
    }

    private void createPOIFilesDirectory() throws IOException {
        // Identify and create our temp dir, if needed
        // The directory is not deleted, even if it was created by this TempFileCreationStrategy
        if (dir == null) {
            String tmpDir = System.getProperty(JAVA_IO_TMPDIR);
            if (tmpDir == null) {
                throw new IOException("Systems temporary directory not defined - set the -D"+JAVA_IO_TMPDIR+" jvm property!");
            }
            dir = new File(tmpDir, POIFILES);
        }

        createTempDirectory(dir);
    }

    /**
     * Attempt to create a directory, including any necessary parent directories.
     * Does nothing if directory already exists.
     * The method is synchronized to ensure that multiple threads don't try to create the directory at the same time.
     *
     * @param directory  the directory to create
     * @throws IOException if unable to create temporary directory or it is not a directory
     */
    private synchronized void createTempDirectory(File directory) throws IOException {
        // create directory if it doesn't exist
        final boolean dirExists = (directory.exists() || directory.mkdirs());

        if (!dirExists) {
            throw new IOException("Could not create temporary directory '" + directory + "'");
        }
        else if (!directory.isDirectory()) {
            throw new IOException("Could not create temporary directory. '" + directory + "' exists but is not a directory.");
        }
    }

    @Override
    public File createTempFile(String prefix, String suffix) throws IOException {
        // Identify and create our temp dir, if needed
        createPOIFilesDirectory();

        // Generate a unique new filename
        File newFile = Files.createTempFile(dir.toPath(), prefix, suffix).toFile();

        // Set the delete on exit flag, but only when explicitly disabled
        if (System.getProperty(DELETE_FILES_ON_EXIT) != null) {
            newFile.deleteOnExit();
        }

        // All done
        return newFile;
    }

    /* (non-JavaDoc) Created directory path is <JAVA_IO_TMPDIR>/poifiles/prefix0123456789 */
    @Override
    public File createTempDirectory(String prefix) throws IOException {
        // Identify and create our temp dir, if needed
        createPOIFilesDirectory();

        // Generate a unique new filename
        // FIXME: Java 7+: use java.nio.file.Files#createTempDirectory
        final long n = RandomSingleton.getInstance().nextLong();
        File newDirectory = new File(dir, prefix + Long.toString(n));
        createTempDirectory(newDirectory);

        //this method appears to be only used in tests, so it is probably ok to use deleteOnExit
        newDirectory.deleteOnExit();

        // All done
        return newDirectory;
    }
}
