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
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Username-aware subclass of {@link DefaultTempFileCreationStrategy}
 * that avoids permission issues when deploying applications with multiple users on the same server.
 * Other than adding the username to the temporary directory, all other behavior is the same as the superclass.
 *
 * @since POI 5.4.0
 */
public class UserNameAwareTempFileCreationStrategy extends DefaultTempFileCreationStrategy {

    /**
     * JVM property for the current username.
     */
    private static final String JAVA_PROP_USER_NAME = "user.name";

    @Override
    protected Path getPOIFilesDirectoryPath() throws IOException {
        final String tmpDir = getJavaIoTmpDir();
        String poifilesDir = POIFILES;
        // Make the default temporary directory contain the username to avoid permission issues
        // when deploying applications on the same server with multiple users
        String username = System.getProperty(JAVA_PROP_USER_NAME);
        if (null != username && !username.isEmpty()) {
            poifilesDir += "_" + username;
        }
        return Paths.get(tmpDir, poifilesDir);
    }

}
