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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.io.TempDir;

class TestThreadLocalTempFile {
    @AfterEach
    void tearDown() {
        TempFile.setTempFileCreationStrategy(new DefaultTempFileCreationStrategy());
    }

    @RepeatedTest(2) // Repeat it to ensure testing the case
    void testThreadLocalStrategy(@TempDir Path tmpDir) {
        Path rootDir = tmpDir.toAbsolutePath().normalize();
        Path globalTmpDir = rootDir.resolve("global-tmp-dir");
        Path localTmpDir1 = rootDir.resolve("local-tmp-dir1");
        Path localTmpDir2 = rootDir.resolve("local-tmp-dir2");

        TempFile.setTempFileCreationStrategy(new DefaultTempFileCreationStrategy(globalTmpDir.toFile()));
        assertTempFileIn(globalTmpDir);

        String result = TempFile.withStrategy(new DefaultTempFileCreationStrategy(localTmpDir1.toFile()), () -> {
            assertTempFileIn(localTmpDir1);
            String nestedResult = TempFile.withStrategy(new DefaultTempFileCreationStrategy(localTmpDir2.toFile()), () -> {
                assertTempFileIn(localTmpDir2);
                return "nested-test-result";
            });
            assertTempFileIn(localTmpDir1);
            return "my-test-result-" + nestedResult;
        });
        assertTempFileIn(globalTmpDir);
        assertEquals("my-test-result-nested-test-result", result);
    }

    private static void assertTempFileIn(Path expectedDir) {
        Path tempFile;
        try {
            tempFile = TempFile.createTempFile("tmp-prefix", ".tmp").toPath();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        assertTrue(tempFile.startsWith(expectedDir), tempFile.toString());
    }
}
