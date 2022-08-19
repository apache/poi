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
package org.apache.poi.hdgf.chunks;

import java.nio.charset.Charset;

import org.apache.poi.hdgf.exceptions.OldVisioFormatException;
import org.apache.poi.util.LittleEndian;

/**
 * A chunk header
 */
public abstract class ChunkHeader {
    private int type;
    private int id;
    private int length;
    private int unknown1;

    /**
     * Creates the appropriate ChunkHeader for the Chunk Header at
     *  the given location, for the given document version.
     *
     * @param documentVersion the documentVersion - 4 and higher is supported
     * @param data the chunk data
     * @param offset the start offset in the chunk data
     * @return the ChunkHeader
     */
    public static ChunkHeader createChunkHeader(int documentVersion, byte[] data, int offset) {
        if(documentVersion >= 6) {
            ChunkHeaderV6 ch;
            if(documentVersion > 6) {
                ch = new ChunkHeaderV11();
            } else {
                ch = new ChunkHeaderV6();
            }
            ch.setType((int) LittleEndian.getUInt(data, offset));
            ch.setId((int) LittleEndian.getUInt(data, offset + 4));
            ch.setUnknown1((int) LittleEndian.getUInt(data, offset + 8));
            ch.setLength((int) LittleEndian.getUInt(data, offset + 12));
            ch.setUnknown2(LittleEndian.getShort(data, offset + 16));
            ch.setUnknown3(LittleEndian.getUByte(data, offset + 18));

            return ch;
        } else if(documentVersion == 5 || documentVersion == 4) {
            ChunkHeaderV4V5 ch = new ChunkHeaderV4V5();

            ch.setType(LittleEndian.getShort(data, offset));
            ch.setId(LittleEndian.getShort(data, offset + 2));
            ch.setUnknown2(LittleEndian.getUByte(data, offset + 4));
            ch.setUnknown3(LittleEndian.getUByte(data, offset + 5));
            ch.setUnknown1(LittleEndian.getShort(data, offset + 6));
            ch.setLength(Math.toIntExact(LittleEndian.getUInt(data, offset + 8)));

            return ch;
        } else {
            throw new OldVisioFormatException("Visio files with versions below 4 are not supported, yours was " + documentVersion);
        }
    }

    /**
     * Returns the size of a chunk header for the given document version.
     *
     * @param documentVersion the documentVersion - 4 and higher is supported
     *
     * @return the header size
     */
    public static int getHeaderSize(int documentVersion) {
        return documentVersion >= 6 ? ChunkHeaderV6.getHeaderSize() : ChunkHeaderV4V5.getHeaderSize();
    }

    public abstract int getSizeInBytes();
    public abstract boolean hasTrailer();
    public abstract boolean hasSeparator();
    public abstract Charset getChunkCharset();

    /**
     * @return the ID/IX of the chunk
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the length of the trunk, excluding the length
     *  of the header, trailer or separator.
     *
     * @return the length of the trunk
     */
    public int getLength() {
        return length;
    }

    /**
     * Returns the type of the chunk, which affects the
     *  mandatory information
     *
     * @return the type of the chunk
     */
    public int getType() {
        return type;
    }

    public int getUnknown1() {
        return unknown1;
    }

    void setType(int type) {
        this.type = type;
    }

    void setId(int id) {
        this.id = id;
    }

    void setLength(int length) {
        this.length = length;
    }

    void setUnknown1(int unknown1) {
        this.unknown1 = unknown1;
    }
}
