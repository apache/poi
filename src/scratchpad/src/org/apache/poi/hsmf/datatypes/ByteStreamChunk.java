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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.poi.util.IOUtils;

import org.apache.poi.hsmf.datatypes.Types.MAPIType;

/**
 * A Chunk that holds a stream to binary data, normally unparsed. Generally as we know how
 * to make sense of the contents, we create a new Chunk class and add a special
 * case in the parser for them.
 */

public class ByteStreamChunk extends Chunk implements AutoCloseable {
    private InputStream value;
    boolean streamclosed;
    byte[] bytevalue;

    /**
     * Creates a Byte Stream Chunk.
     */
    public ByteStreamChunk(String namePrefix, int chunkId, MAPIType type) {
        super(namePrefix, chunkId, type);
    }

    /**
     * Create a Byte Stream Chunk, with the specified type.
     */
    public ByteStreamChunk(int chunkId, MAPIType type) {
        super(chunkId, type);
    }

    @Override
    public void readValue(InputStream value) throws IOException {
        this.value = value;
    }

    @Override
    public void writeValue(OutputStream out) throws IOException {
        if (bytevalue != null || streamclosed || value == null) {
            if (bytevalue != null) {
                out.write(bytevalue);
            }
        }
        else {
            value.reset();
            IOUtils.copy(value, out);
        }
    }

    /**
     * Get bytes directly.
     */
    public byte[] getValue() throws IOException {
        if (value != null && bytevalue == null && !streamclosed) {
            try (InputStream valueres = value) {
                valueres.reset();
                bytevalue = IOUtils.toByteArray(valueres);
            } finally {
                streamclosed = true;
                value = null;
            }
        }
        return bytevalue;
    }

    /**
     * Set bytes directly.
     */
    public void setValue(byte[] value) {
        this.bytevalue = value;
        this.value = new ByteArrayInputStream(value);
    }

    /**
     * Closes the underlying stream.
     */
    public void close() throws IOException {
        if (value != null) {
            value.close();
        }
    }

    /**
     * Returns the data in a debug-friendly string format
     */
    @Override
    public String toString() {
        try {
            return toDebugFriendlyString(getValue());
        } catch (Exception e) {
            return e.toString();
        }
    }

    /**
     * Formats the byte array in a debug-friendly way, showing all of a short
     * array, and the start of a longer one.
     */
    protected static String toDebugFriendlyString(byte[] value) {
        if (value == null) {
            return "(Null Byte Array)";
        }

        StringBuffer text = new StringBuffer();
        text.append("Bytes len=").append(value.length);
        text.append(" [");

        int limit = Math.min(value.length, 16);
        if (value.length > 16) {
            limit = 12;
        }
        for (int i = 0; i < limit; i++) {
            if (i > 0) {
                text.append(',');
            }
            text.append(value[i]);
        }
        if (value.length > 16) {
            text.append(",....");
        }
        text.append("]");
        return text.toString();
    }

    /**
     * Returns the data, formatted as a string assuming it was a non-unicode
     * string. If your data isn't in fact stored as basically ASCII, don't
     * expect this to return much of any sense....
     * 
     * @return the data formatted as a string
     */
    public String getAs7bitString() throws IOException {
        return StringChunk.parseAs7BitData(getValue());
    }
}
