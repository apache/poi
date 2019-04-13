package org.apache.poi.poifs.filesystem;

import org.apache.poi.poifs.nio.FileBackedDataSource;
import org.apache.poi.util.Beta;
import org.apache.poi.util.TempFile;

import java.io.File;
import java.io.IOException;

/**
 * An experimental POIFSFileSystem to support the encryption of large files
 *
 * @since 4.1.1
 */
@Beta
public class TempFilePOIFSFileSystem extends POIFSFileSystem {
    File tempFile;

    protected void createNewDataSource() {
        try {
            tempFile = TempFile.createTempFile("poifs", ".tmp");
            _data = new FileBackedDataSource(tempFile, false);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create data source", e);
        }
    }

    public void close() throws IOException {
        tempFile.delete();
        super.close();
    }

}
