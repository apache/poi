package org.apache.poi.hpsf;

import java.io.OutputStream;

import org.apache.poi.POIDocument;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * A version of {@link POIDocument} which allows access to the
 *  HPSF Properties, but no other document contents.
 * Normally used when you want to read or alter the Document Properties,
 *  without affecting the rest of the file
 */
public class HPSFPropertiesOnlyDocument extends POIDocument {
    public HPSFPropertiesOnlyDocument(NPOIFSFileSystem fs) {
        super(fs.getRoot());
    }
    public HPSFPropertiesOnlyDocument(POIFSFileSystem fs) {
        super(fs);
    }

    public void write(OutputStream out) {
        throw new IllegalStateException("Unable to write, only for properties!");
    }
}