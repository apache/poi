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

package org.apache.poi.ss.usermodel;

import java.io.IOException;

import org.apache.poi.poifs.filesystem.DirectoryEntry;

/**
 * Common interface for OLE shapes, i.e. shapes linked to embedded documents
 * 
 * @since POI 3.16-beta2
 */
public interface ObjectData extends SimpleShape {
    /**
     * @return the data portion, for an ObjectData that doesn't have an associated POIFS Directory Entry
     */
    byte[] getObjectData() throws IOException;

    /**
     * @return does this ObjectData have an associated POIFS Directory Entry?
     * (Not all do, those that don't have a data portion)
     */
    boolean hasDirectoryEntry();

    /**
     * Gets the object data. Only call for ones that have
     * data though. See {@link #hasDirectoryEntry()}.
     * The caller has to close the corresponding POIFSFileSystem
     *
     * @return the object data as an OLE2 directory.
     * @throws IOException if there was an error reading the data.
     */
    DirectoryEntry getDirectory() throws IOException;

    /**
     * @return the OLE2 Class Name of the object
     */
    String getOLE2ClassName();

    /**
     * @return a filename suggestion - inspecting/interpreting the Directory object probably gives a better result
     */
    String getFileName();

    /**
     * @return the preview picture
     */
    PictureData getPictureData();

    default String getContentType() {
        return "binary/octet-stream";
    }
}
