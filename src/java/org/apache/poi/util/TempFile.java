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
import java.util.Random;

/**
 * Interface for creating temporary files.  Collects them all into one directory.
 *
 * @author Glen Stampoultzis
 */
public final class TempFile {
    private static File dir;
    private static final Random rnd = new Random();

    /**
     * Creates a temporary file.  Files are collected into one directory and by default are
     * deleted on exit from the VM.  Files can be kept by defining the system property
     * <code>poi.keep.tmp.files</code>.
     * <p>
     * Don't forget to close all files or it might not be possible to delete them.
     */
    public static File createTempFile(String prefix, String suffix) {
        if (dir == null)
        {
            dir = new File(System.getProperty("java.io.tmpdir"), "poifiles");
            dir.mkdir();
            if (System.getProperty("poi.keep.tmp.files") == null)
                dir.deleteOnExit();
        }

        File newFile = new File(dir, prefix + rnd.nextInt() + suffix);
        if (System.getProperty("poi.keep.tmp.files") == null)
            newFile.deleteOnExit();
        return newFile;
    }
}
