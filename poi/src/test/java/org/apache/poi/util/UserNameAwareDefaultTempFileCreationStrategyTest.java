package org.apache.poi.util;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class UserNameAwareDefaultTempFileCreationStrategyTest {

    @Test
    void getPOIFilesDirectoryPath() throws IOException {
        UserNameAwareDefaultTempFileCreationStrategy strategy = new UserNameAwareDefaultTempFileCreationStrategy();
        String tmpDir = System.getProperty("java.io.tmpdir");
        String username = System.getProperty("user.name");
        String expectedPath = Paths.get(tmpDir, "poifiles_" + username).toString();

        Path actualPath = strategy.getPOIFilesDirectoryPath();

        assertEquals(expectedPath, actualPath.toString());
    }

}
