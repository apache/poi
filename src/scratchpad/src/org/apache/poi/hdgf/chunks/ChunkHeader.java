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

import org.apache.poi.util.LittleEndian;

/**
 * A chunk header
 */
public abstract class ChunkHeader {
	protected int type;
	protected int id;
	protected int length;
	protected int unknown1;

	/**
	 * Creates the appropriate ChunkHeader for the Chunk Header at
	 *  the given location, for the given document version.
	 */
	public static ChunkHeader createChunkHeader(int documentVersion, byte[] data, int offset) {
		if(documentVersion >= 6) {
			ChunkHeaderV6 ch;
			if(documentVersion > 6) {
				ch = new ChunkHeaderV11();
			} else {
				ch = new ChunkHeaderV6();
			}
			ch.type = (int)LittleEndian.getUInt(data, offset + 0);
			ch.id   = (int)LittleEndian.getUInt(data, offset + 4);
			ch.unknown1 = (int)LittleEndian.getUInt(data, offset + 8);
			ch.length   = (int)LittleEndian.getUInt(data, offset + 12);
			ch.unknown2 = LittleEndian.getShort(data, offset + 16);
			ch.unknown3 = (short)LittleEndian.getUnsignedByte(data, offset + 18);

			return ch;
		} else if(documentVersion == 5 || documentVersion == 4) {
			ChunkHeaderV4V5 ch = new ChunkHeaderV4V5();

			ch.type = LittleEndian.getShort(data, offset + 0);
			ch.id   = LittleEndian.getShort(data, offset + 2);
			ch.unknown2 = (short)LittleEndian.getUnsignedByte(data, offset + 4);
			ch.unknown3 = (short)LittleEndian.getUnsignedByte(data, offset + 5);
			ch.unknown1 = LittleEndian.getShort(data, offset + 6);
			ch.length   = (int)LittleEndian.getUInt(data, offset + 8);

			return ch;
		} else {
			throw new IllegalArgumentException("Visio files with versions below 4 are not supported, yours was " + documentVersion);
		}
	}

	/**
	 * Returns the size of a chunk header for the given document version.
	 */
	public static int getHeaderSize(int documentVersion) {
		if(documentVersion > 6) {
			return ChunkHeaderV11.getHeaderSize();
		} else if(documentVersion == 6) {
			return ChunkHeaderV6.getHeaderSize();
		} else {
			return ChunkHeaderV4V5.getHeaderSize();
		}
	}

	public abstract int getSizeInBytes();
	public abstract boolean hasTrailer();
	public abstract boolean hasSeparator();

	/**
	 * Returns the ID/IX of the chunk
	 */
	public int getId() {
		return id;
	}
	/**
	 * Returns the length of the trunk, excluding the length
	 *  of the header, trailer or separator.
	 */
	public int getLength() {
		return length;
	}
	/**
	 * Returns the type of the chunk, which affects the
	 *  mandatory information
	 */
	public int getType() {
		return type;
	}
	public int getUnknown1() {
		return unknown1;
	}
}
