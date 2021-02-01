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

package org.apache.poi.sl.usermodel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * Common interface for OLE shapes, i.e. shapes linked to embedded documents
 * 
 * @since POI 4.0.0
 */
public interface ObjectData {
    
    /**
     * Gets an input stream which returns the binary of the embedded data.
     *
     * @return the input stream which will contain the binary of the embedded data.
     */
    InputStream getInputStream() throws IOException;

    
    /**
     * @return the object data as stream (for writing)
     */
    OutputStream getOutputStream() throws IOException;
    
    /**
     * Convenience method to get the embedded data as byte array.
     *
     * @return the embedded data.
     */
    default byte[] getBytes() throws IOException {
        try (InputStream is = getInputStream()) {
            return IOUtils.toByteArray(is);
        }
    }
    
    /**
     * @return does this ObjectData have an associated POIFS Directory Entry?
     * (Not all do, those that don't have a data portion)
     */
    default boolean hasDirectoryEntry() {
        try (final InputStream is = FileMagic.prepareToCheckMagic(getInputStream())) {
            FileMagic fm = FileMagic.valueOf(is);
            return fm == FileMagic.OLE2;
        } catch (IOException e) {
            POILogger LOG = POILogFactory.getLogger(ObjectData.class);
            LOG.log(POILogger.WARN, "Can't determine filemagic of ole stream", e);
            return false;
        }
    }

    /**
     * Gets the object data. Only call for ones that have
     * data though. See {@link #hasDirectoryEntry()}.
     * The caller has to close the corresponding POIFSFileSystem
     *
     * @return the object data as an OLE2 directory.
     * @throws IOException if there was an error reading the data.
     */
    @SuppressWarnings("resource")
    default DirectoryEntry getDirectory() throws IOException {
        try (final InputStream is = getInputStream()) {
            return new POIFSFileSystem(is).getRoot();
        }
    }

    /**
     * @return the OLE2 Class Name of the object
     */
    String getOLE2ClassName();

    /**
     * @return a filename suggestion - inspecting/interpreting the Directory object probably gives a better result
     */
    String getFileName();

}
