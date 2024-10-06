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

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class UserNameAwareTempFileCreationStrategyTest {

    @Test
    void getPOIFilesDirectoryPath() throws IOException {
        UserNameAwareTempFileCreationStrategy strategy = new UserNameAwareTempFileCreationStrategy();
        String tmpDir = System.getProperty("java.io.tmpdir");
        String username = System.getProperty("user.name");
        String expectedPath = Paths.get(tmpDir, "poifiles_" + username).toString();

        Path actualPath = strategy.getPOIFilesDirectoryPath();

        assertEquals(expectedPath, actualPath.toString());
    }

}
