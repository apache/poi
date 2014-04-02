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
