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

package org.apache.poi.hpbf.model;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.input.UnsynchronizedByteArrayInputStream;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.Entry;
import org.apache.poi.util.IOUtils;

/**
 * Parent class of all HPBF sub-parts, handling
 *  the fiddly reading in / writing out bits
 *  for all of them.
 */
public abstract class HPBFPart {
    private byte[] data;
    private final String[] path;

    /**
     * @param path  the path to the part, eg Contents or Quill, QuillSub, CONTENTS
     */
    public HPBFPart(DirectoryNode baseDir, String[] path) throws IOException {
        this.path = path;

        DirectoryNode dir = getDir(baseDir, path);
        String name = path[path.length-1];

        if (!dir.hasEntryCaseInsensitive(name)) {
            throw new IllegalArgumentException("File invalid - failed to find document entry '" + name + "'");
        }

        // Grab the data from the part stream
        try (InputStream is = dir.createDocumentInputStream(name)) {
            data = IOUtils.toByteArray(is);
        }
    }

    private static DirectoryNode getDir(DirectoryNode baseDir, String[] path) {
        DirectoryNode dir = baseDir;
        for(int i=0; i<path.length-1; i++) {
            try {
                Entry entry = dir.getEntry(path[i]);
                if (!(entry instanceof DirectoryNode)) {
                    throw new IllegalArgumentException("Had unexpected type of entry for path: " + path[i] + ": " + entry);
                }
                dir = (DirectoryNode) entry;
            } catch (FileNotFoundException e) {
                throw new IllegalArgumentException("File invalid - failed to find directory entry '"
                        + path[i] + "': " + e);
            }
        }
        return dir;
    }

    public void writeOut(DirectoryNode baseDir) throws IOException {
        String[] path = getPath();

        // Ensure that all parent directories exist
        DirectoryNode dir = baseDir;
        for(int i=0; i<path.length-1; i++) {
            try {
                dir = (DirectoryNode)dir.getEntryCaseInsensitive(path[i]);
            } catch(FileNotFoundException e) {
                dir.createDirectory(path[i]);
            }
        }

        // Update the byte array with the latest data
        generateData();

        // Write out
        try (UnsynchronizedByteArrayInputStream bais = UnsynchronizedByteArrayInputStream.builder().setByteArray(data).get()) {
            dir.createDocument(path[path.length-1], bais);
        }
    }

    /**
     * Called just before writing out, to trigger
     *  the data byte array to be updated with the
     *  latest contents.
     */
    protected abstract void generateData();

    /**
     * Returns the raw data that makes up
     *  this document part.
     */
    public final byte[] getData() {
        return data;
    }

    protected final void setData(byte[] data) {
        this.data = data.clone();
    }

    /**
     * Returns
     */
    public final String[] getPath() {
        return path;
    }
}
