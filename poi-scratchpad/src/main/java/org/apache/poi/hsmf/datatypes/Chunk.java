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

package org.apache.poi.hsmf.datatypes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

import org.apache.poi.hsmf.datatypes.Types.MAPIType;
import org.apache.poi.util.StringUtil;

public abstract class Chunk {
    public static final String DEFAULT_NAME_PREFIX = "__substg1.0_";

    private final int chunkId;
    private final MAPIType type;
    private final String namePrefix;

    protected Chunk(String namePrefix, int chunkId, MAPIType type) {
        this.namePrefix = namePrefix;
        this.chunkId = chunkId;
        this.type = type;
    }

    protected Chunk(int chunkId, MAPIType type) {
        this(DEFAULT_NAME_PREFIX, chunkId, type);
    }

    /**
     * Gets the id of this chunk
     */
    public int getChunkId() {
        return this.chunkId;
    }

    /**
     * Gets the numeric type of this chunk.
     */
    public MAPIType getType() {
        return this.type;
    }

    /**
     * Creates a string to use to identify this chunk in the POI file system
     * object.
     */
    public String getEntryName() {
        String type = this.type.asFileEnding();

        StringBuilder chunkId = new StringBuilder(Integer.toHexString(this.chunkId));
        int need0count = 4 - chunkId.length();
        if (need0count > 0) {
            chunkId.insert(0, StringUtil.repeat('0', need0count));
        }

        return this.namePrefix
            + chunkId.toString().toUpperCase(Locale.ROOT)
            + type.toUpperCase(Locale.ROOT);
    }

    /**
     * Writes the value of this chunk back out again.
     */
    public abstract void writeValue(OutputStream out) throws IOException;

    /**
     * Reads the value of this chunk using an InputStream
     */
    public abstract void readValue(InputStream value) throws IOException;
}
