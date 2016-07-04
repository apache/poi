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
 * Interface used by the {@link TempFile} utility class to create temporary files.
 * 
 * Classes that implement a TempFileCreationStrategy attempt to handle the cleanup
 * of temporary files.
 * 
 * Examples include:
 * <ul>
 *   <li>{@link DefaultTempFileCreationStrategy} deletes temporary files when
 *       the JVM exits.
 *       This may not be suitable for long-running applications that never
 *       shut down the JVM since the list of registered files and disk space
 *       usage would grow for as long as the JVM is running.
 *       You may wish to implement your own strategy that meets the needs of
 *       your situation.
 *   </li>
 *   <li>A strategy that keeps the <code>n</code> most-recent files, discarding
 *       older files on a first-in, first-out basis.
 *       A java.util.Deque or org.apache.commons.collections4.queue.CircularFifoQueue
 *       may be helpful for achieving this.
 *   </li>
 *   <li>A strategy that keeps track of every temporary file that has been
 *       created by the class or instance and provides a method to explicitly
 *       delete the temporary files in the reverse order that they were created.
 *       This is the same as DefaultTempFileCreationStrategy, except the strategy
 *       class would maintain the list of files to delete rather than or in
 *       addition to {@link java.io.DeleteOnExitHook} maintaining the list, and
 *       the files could be deleted before the JVM exit.
 *   </li>
 *   <li>A strategy that creates a directory that is deleted on JVM exit.
 *       Any files inside the directory do not need to be registered since the
 *       entire directory will be deleted at exit.
 *       This could be dangerous if files were added to the temporary directory
 *       outside of this TempFileCreationStrategy's control.
 *       This could be accomplished with {@link #createTempDirectory(String)} and
 *       creating regular (unregistered) files in the temp directory.
 *   </li>
 * </ul>
 * 
 */
public interface TempFileCreationStrategy {
    /**
     * Creates a new and empty temporary file.
     *
     * @param prefix The prefix to be used to generate the name of the temporary file.
     * @param suffix The suffix to be used to generate the name of the temporary file.
     * 
     * @return The path to the newly created and empty temporary file.
     * 
     * @throws IOException If no temporary file could be created.
     */
    File createTempFile(String prefix, String suffix) throws IOException;
    
    /**
     * Creates a new and empty temporary directory.
     *
     * @param prefix The directory name to be used to generate the name of the temporary directory.
     * 
     * @return The path to the newly created and empty temporary directory.
     * 
     * @throws IOException If no temporary directory could be created.
     * 
     * @since POI 3.15 beta 3.
     */
    File createTempDirectory(String prefix) throws IOException;
}
