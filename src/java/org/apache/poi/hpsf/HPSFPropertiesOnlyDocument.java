package org.apache.poi.hpsf;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.POIDocument;
import org.apache.poi.poifs.filesystem.EntryUtils;
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

    /**
     * Write out, with any properties changes, but nothing else
     */
    public void write(OutputStream out) throws IOException {
        POIFSFileSystem fs = new POIFSFileSystem();

        // For tracking what we've written out, so far
        List<String> excepts = new ArrayList<String>(1);

        // Write out our HPFS properties, with any changes
        writeProperties(fs, excepts);
        
        // Copy over everything else unchanged
        EntryUtils.copyNodes(directory, fs.getRoot(), excepts);
        
        // Save the resultant POIFSFileSystem to the output stream
        fs.writeFilesystem(out);
    }
}