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

import org.apache.logging.log4j.Logger;
import org.apache.poi.logging.PoiLogManager;

import static org.apache.poi.util.TempFile.JAVA_IO_TMPDIR;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipal;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Default implementation of the {@link TempFileCreationStrategy} used by {@link TempFile}:
 * Files are collected into one directory.
 * Files may be manually deleted by user prior to JVM exit.
 * You can define the system property {@link #DELETE_FILES_ON_EXIT} if you want to
 * delete any stray files on clean JVM exit (any non-empty value means add the deleteOnExit flag).
 *
 * The POI code should tidy up temp files when it no longer needs them.
 * The temporary directory is not deleted after the JVM exits.
 * Files that are created in the poifiles directory outside
 * the control of DefaultTempFileCreationStrategy are not deleted.
 * See {@link TempFileCreationStrategy} for better strategies for long-running
 * processes or limited temporary storage.
 */
public class DefaultTempFileCreationStrategy implements TempFileCreationStrategy {
    /** Name of POI files directory in temporary directory. */
    public static final String POIFILES = "poifiles";

    /** To use files.deleteOnExit after clean JVM exit, set the <code>-Dpoi.delete.tmp.files.on.exit</code> JVM property */
    public static final String DELETE_FILES_ON_EXIT = "poi.delete.tmp.files.on.exit";

    private static final Logger logger =
            PoiLogManager.getLogger(DefaultTempFileCreationStrategy.class);

    /** The directory where the temporary files will be created (<code>null</code> to use the default directory). */
    private volatile File dir;

    /** The directory where that was passed to the constructor (<code>null</code> to use the default directory). */
    private final File initDir;

    /** The lock to make dir initialized only once. */
    private final Lock dirLock = new ReentrantLock();

    // File permissions that are applied as best effort
    private final Set<PosixFilePermission> posixRWFilePermissions = createPosixRWFilePermissions();
    private final Set<PosixFilePermission> posixRWXFilePermissions = createPosixRWXFilePermissions();

    /**
     * Creates the strategy so that it creates the temporary files in the default directory.
     *
     * @see File#createTempFile(String, String)
     */
    public DefaultTempFileCreationStrategy() {
        this(null);
    }

    /**
     * Creates the strategy allowing to set a custom directory for the temporary files.
     * <p>
     *     If you provide a non-null dir as input, it must be a directory (if it already exists).
     *     Since POI 5.5.0, this is checked at construction time. In previous versions, it was checked
     *     at the first call to {@link #createTempFile(String, String)} or {@link #createTempDirectory(String)}.
     * </p>
     *
     * @param dir The directory where the temporary files will be created (<code>null</code> to use the default directory).
     * @throws IllegalArgumentException if the provided directory does not exist or is not a directory
     * @see Files#createTempFile(Path, String, String, FileAttribute[]) 
     */
    public DefaultTempFileCreationStrategy(File dir) {
        this.initDir = dir;
        this.dir = dir;
        if (dir != null && dir.exists() && !dir.isDirectory()) {
            throw new IllegalArgumentException("The provided file is not a directory: " + dir);
        }
    }

    @Override
    public File createTempFile(String prefix, String suffix) throws IOException {
        // Identify and create our temp dir, if needed
        createPOIFilesDirectoryIfNecessary();

        // Generate a unique new filename
        File newFile;
        if (posixRWFilePermissions == null) {
            newFile = createTempFileFallback(prefix, suffix);
        } else {
            try {
                // Try POSIX permissions first (owner read/write only)
                Path p = Files.createTempFile(dir.toPath(), prefix, suffix,
                        PosixFilePermissions.asFileAttribute(posixRWFilePermissions));
                newFile = p.toFile();
            } catch (UnsupportedOperationException | IOException e) {
                newFile = createTempFileFallback(prefix, suffix);
            }
        }

        // Set the delete on exit flag if sys prop is set
        if (System.getProperty(DELETE_FILES_ON_EXIT) != null) {
            newFile.deleteOnExit();
        }

        // All done
        return newFile;
    }

    // POSIX not supported (e.g., Windows) or failed: fall back to creating normally
    private File createTempFileFallback(String prefix, String suffix) throws IOException {
        File newFile = Files.createTempFile(dir.toPath(), prefix, suffix).toFile();
        setOwnerOnlyFilePermissions(newFile, false);
        return newFile;
    }

    /* (non-JavaDoc) Created directory path is <JAVA_IO_TMPDIR>/poifiles/prefix0123456789 */
    @Override
    public File createTempDirectory(String prefix) throws IOException {
        // Identify and create our temp dir, if needed
        createPOIFilesDirectoryIfNecessary();

        // Generate a unique new filename
        File newDirectory;
        if (posixRWXFilePermissions == null) {
            newDirectory = createTempDirFallback(prefix);
        } else {
            try {
                Path p = Files.createTempDirectory(dir.toPath(), prefix,
                        PosixFilePermissions.asFileAttribute(posixRWXFilePermissions));
                newDirectory = p.toFile();
            } catch (UnsupportedOperationException | IOException e) {
                newDirectory = createTempDirFallback(prefix);
            }
        }

        //this method appears to be only used in tests, so it is probably ok to use deleteOnExit
        newDirectory.deleteOnExit();

        // All done
        return newDirectory;
    }

    // POSIX not supported (e.g., Windows) or failed: fall back to creating normally
    private File createTempDirFallback(String prefix) throws IOException {
        File newDirectory = Files.createTempDirectory(dir.toPath(), prefix).toFile();
        setOwnerOnlyFilePermissions(newDirectory, true);
        return newDirectory;
    }

    protected String getJavaIoTmpDir() throws IOException {
        final String tmpDir = System.getProperty(JAVA_IO_TMPDIR);
        if (tmpDir == null) {
            throw new IOException("System's temporary directory not defined - set the -D" + JAVA_IO_TMPDIR + " jvm property!");
        }
       return tmpDir;
    }

    protected Path getPOIFilesDirectoryPath() throws IOException {
        if (initDir == null) {
            return Path.of(getJavaIoTmpDir(), POIFILES);
        } else {
            return initDir.toPath();
        }
    }

    // Create our temp dir only once by double-checked locking
    // The directory is not deleted, even if it was created by this TempFileCreationStrategy
    private void createPOIFilesDirectoryIfNecessary() throws IOException {
        // First make sure we recreate the directory if it was not somehow removed by a third party
        if (dir != null && !dir.exists()) {
            dir = null;
        }
        if (dir == null) {
            dirLock.lock();
            try {
                if (dir == null) {
                    final Path dirPath = getPOIFilesDirectoryPath();
                    File fileDir = dirPath.toFile();
                    File newDir;
                    if (fileDir.exists()) {
                        if (!fileDir.isDirectory()) {
                            throw new IOException("Could not create temporary directory. '" + fileDir + "' exists but is not a directory.");
                        }
                        newDir = fileDir;
                    } else {
                        newDir = Files.createDirectories(dirPath).toFile();
                        try {
                            // attempt to restrict directory perms to owner only
                            try {
                                Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rwx------");
                                Files.setPosixFilePermissions(newDir.toPath(), perms);
                            } catch (UnsupportedOperationException | IOException e) {
                                // fallback: use File API to set owner-only flags where supported
                                newDir.setReadable(false, false);
                                newDir.setWritable(false, false);
                                newDir.setExecutable(false, false);
                                newDir.setReadable(true, true);
                                newDir.setWritable(true, true);
                                newDir.setExecutable(true, true);
                            }
                        } catch (Exception ignored) {
                            // best-effort only
                        }
                    }
                    // a directory passed to the constructor was chosen by the caller, but the default
                    // directory has a fixed name in the shared temporary directory
                    dir = (initDir == null) ? checkDirectoryOwner(newDir) : newDir;
                }
            } finally {
                dirLock.unlock();
            }
        }
    }

    /**
     * Another user could have created the directory (or a symbolic link to one) before us,
     * and would then be able to replace the temporary files in it.
     * So only use it, if it belongs to the user that creates the files in it.
     *
     * @return the directory with symbolic links resolved, so that it can't be redirected later
     * @throws IOException if the directory belongs to another user
     */
    private static File checkDirectoryOwner(final File fileDir) throws IOException {
        final Path dirPath = fileDir.toPath().toRealPath();
        if (Files.getFileAttributeView(dirPath, PosixFileAttributeView.class) == null) {
            return dirPath.toFile();
        }
        // the owner of a new file is the current user - this avoids looking up the user name
        final Path probe = Files.createTempFile(dirPath, "poi-owner", ".tmp");
        try {
            final UserPrincipal owner = Files.getOwner(dirPath);
            final UserPrincipal user = Files.getOwner(probe);
            if (!owner.equals(user)) {
                throw new IOException("Could not use temporary directory '" + dirPath + "' as it is owned by '" +
                        owner.getName() + "' and not by '" + user.getName() + "' - remove it or use a TempFileCreationStrategy with a different directory");
            }
        } finally {
            Files.deleteIfExists(probe);
        }
        return dirPath.toFile();
    }

    private static void setOwnerOnlyFilePermissions(final File file, final boolean executable) {
        try {
            file.setReadable(true, true);
            file.setWritable(true, true);
            file.setExecutable(executable, true);
        } catch (Exception ignore) {
            // best-effort only
        }

    }

    private static Set<PosixFilePermission> createPosixRWFilePermissions() {
        try {
            return PosixFilePermissions.fromString("rw-------");
        } catch (Exception e) {
            logger.warn("Failed to init the PosixFilePermissions, continuing with weaker permissions", e);
            return null;
        }
    }

    private static Set<PosixFilePermission> createPosixRWXFilePermissions() {
        try {
            return PosixFilePermissions.fromString("rwx------");
        } catch (Exception e) {
            logger.warn("Failed to init the PosixFilePermissions, continuing with weaker permissions", e);
            return null;
        }
    }

}
