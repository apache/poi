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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.StringUtil;

/**
 * Collection of convenience chunks for the NameID part of an outlook file
 */
public final class NameIdChunks implements ChunkGroup {
    public static final String NAME = "__nameid_version1.0";

    public static final String PS_MAPI = "00020328-0000-0000-C000-000000000046";
    public static final String PS_PUBLIC_STRINGS = "00020329-0000-0000-C000-000000000046";
    public static final String PSETID_Common = "00062008-0000-0000-C000-000000000046";
    public static final String PSETID_Address = "00062004-0000-0000-C000-000000000046";
    public static final String PS_INTERNET_HEADERS = "00020386-0000-0000-C000-000000000046";
    public static final String PSETID_Appointment = "00062002-0000-0000-C000-000000000046";
    public static final String PSETID_Meeting = "6ED8DA90-450B-101B-98DA-00AA003F1305";
    public static final String PSETID_Log = "0006200A-0000-0000-C000-000000000046";
    public static final String PSETID_Messaging = "41F28F13-83F4-4114-A584-EEDB5A6B0BFF";
    public static final String PSETID_Note = "0006200E-0000-0000-C000-000000000046";
    public static final String PSETID_PostRss = "00062041-0000-0000-C000-000000000046";
    public static final String PSETID_Task = "00062003-0000-0000-C000-000000000046";
    public static final String PSETID_UnifiedMessaging = "4442858E-A9E3-4E80-B900-317A210CC15B";
    public static final String PSETID_AirSync = "71035549-0739-4DCB-9163-00F0580DBBDF";
    public static final String PSETID_Sharing = "00062040-0000-0000-C000-000000000046";
    public static final String PSETID_XmlExtractedEntities = "23239608-685D-4732-9C55-4C95CB4E8E33";
    public static final String PSETID_Attachment = "96357F7F-59E1-47D0-99A7-46515C183B54";

    private ByteChunk guidStream;
    private ByteChunk entryStream;
    private ByteChunk stringStream;

    /** Holds all the chunks that were found. */
    private List<Chunk> allChunks = new ArrayList<>();

    public Chunk[] getAll() {
        return allChunks.toArray(new Chunk[0]);
    }

    @Override
    public Chunk[] getChunks() {
        return getAll();
    }

    /**
     * Called by the parser whenever a chunk is found.
     */
    @Override
    public void record(Chunk chunk) {
        if (chunk.getType() == Types.BINARY) {
            if (chunk.getChunkId() == 2) {
                guidStream = (ByteChunk)chunk;
            }
            else if (chunk.getChunkId() == 3) {
                entryStream = (ByteChunk)chunk;
            }
            else if (chunk.getChunkId() == 4) {
                stringStream = (ByteChunk)chunk;
            }
        }
        allChunks.add(chunk);
    }

    /**
     * Used to flag that all the chunks of the NameID have now been located.
     */
    @Override
    public void chunksComplete() {
        // Currently, we don't need to do anything special once
        // all the chunks have been located
    }

    private static void swap(byte[] array, int i, int j) {
        byte tmp = array[i];
        array[i] = array[j];
        array[j] = tmp;
    }

    private static final int[] CRC32_TABLE_0x04C11DB7 = {
        0x00000000, 0x77073096, 0xEE0E612C, 0x990951BA, 0x076DC419,
        0x706AF48F, 0xE963A535, 0x9E6495A3, 0x0EDB8832, 0x79DCB8A4,
        0xE0D5E91E, 0x97D2D988, 0x09B64C2B, 0x7EB17CBD, 0xE7B82D07,
        0x90BF1D91, 0x1DB71064, 0x6AB020F2, 0xF3B97148, 0x84BE41DE,
        0x1ADAD47D, 0x6DDDE4EB, 0xF4D4B551, 0x83D385C7, 0x136C9856,
        0x646BA8C0, 0xFD62F97A, 0x8A65C9EC, 0x14015C4F, 0x63066CD9,
        0xFA0F3D63, 0x8D080DF5, 0x3B6E20C8, 0x4C69105E, 0xD56041E4,
        0xA2677172, 0x3C03E4D1, 0x4B04D447, 0xD20D85FD, 0xA50AB56B,
        0x35B5A8FA, 0x42B2986C, 0xDBBBC9D6, 0xACBCF940, 0x32D86CE3,
        0x45DF5C75, 0xDCD60DCF, 0xABD13D59, 0x26D930AC, 0x51DE003A,
        0xC8D75180, 0xBFD06116, 0x21B4F4B5, 0x56B3C423, 0xCFBA9599,
        0xB8BDA50F, 0x2802B89E, 0x5F058808, 0xC60CD9B2, 0xB10BE924,
        0x2F6F7C87, 0x58684C11, 0xC1611DAB, 0xB6662D3D, 0x76DC4190,
        0x01DB7106, 0x98D220BC, 0xEFD5102A, 0x71B18589, 0x06B6B51F,
        0x9FBFE4A5, 0xE8B8D433, 0x7807C9A2, 0x0F00F934, 0x9609A88E,
        0xE10E9818, 0x7F6A0DBB, 0x086D3D2D, 0x91646C97, 0xE6635C01,
        0x6B6B51F4, 0x1C6C6162, 0x856530D8, 0xF262004E, 0x6C0695ED,
        0x1B01A57B, 0x8208F4C1, 0xF50FC457, 0x65B0D9C6, 0x12B7E950,
        0x8BBEB8EA, 0xFCB9887C, 0x62DD1DDF, 0x15DA2D49, 0x8CD37CF3,
        0xFBD44C65, 0x4DB26158, 0x3AB551CE, 0xA3BC0074, 0xD4BB30E2,
        0x4ADFA541, 0x3DD895D7, 0xA4D1C46D, 0xD3D6F4FB, 0x4369E96A,
        0x346ED9FC, 0xAD678846, 0xDA60B8D0, 0x44042D73, 0x33031DE5,
        0xAA0A4C5F, 0xDD0D7CC9, 0x5005713C, 0x270241AA, 0xBE0B1010,
        0xC90C2086, 0x5768B525, 0x206F85B3, 0xB966D409, 0xCE61E49F,
        0x5EDEF90E, 0x29D9C998, 0xB0D09822, 0xC7D7A8B4, 0x59B33D17,
        0x2EB40D81, 0xB7BD5C3B, 0xC0BA6CAD, 0xEDB88320, 0x9ABFB3B6,
        0x03B6E20C, 0x74B1D29A, 0xEAD54739, 0x9DD277AF, 0x04DB2615,
        0x73DC1683, 0xE3630B12, 0x94643B84, 0x0D6D6A3E, 0x7A6A5AA8,
        0xE40ECF0B, 0x9309FF9D, 0x0A00AE27, 0x7D079EB1, 0xF00F9344,
        0x8708A3D2, 0x1E01F268, 0x6906C2FE, 0xF762575D, 0x806567CB,
        0x196C3671, 0x6E6B06E7, 0xFED41B76, 0x89D32BE0, 0x10DA7A5A,
        0x67DD4ACC, 0xF9B9DF6F, 0x8EBEEFF9, 0x17B7BE43, 0x60B08ED5,
        0xD6D6A3E8, 0xA1D1937E, 0x38D8C2C4, 0x4FDFF252, 0xD1BB67F1,
        0xA6BC5767, 0x3FB506DD, 0x48B2364B, 0xD80D2BDA, 0xAF0A1B4C,
        0x36034AF6, 0x41047A60, 0xDF60EFC3, 0xA867DF55, 0x316E8EEF,
        0x4669BE79, 0xCB61B38C, 0xBC66831A, 0x256FD2A0, 0x5268E236,
        0xCC0C7795, 0xBB0B4703, 0x220216B9, 0x5505262F, 0xC5BA3BBE,
        0xB2BD0B28, 0x2BB45A92, 0x5CB36A04, 0xC2D7FFA7, 0xB5D0CF31,
        0x2CD99E8B, 0x5BDEAE1D, 0x9B64C2B0, 0xEC63F226, 0x756AA39C,
        0x026D930A, 0x9C0906A9, 0xEB0E363F, 0x72076785, 0x05005713,
        0x95BF4A82, 0xE2B87A14, 0x7BB12BAE, 0x0CB61B38, 0x92D28E9B,
        0xE5D5BE0D, 0x7CDCEFB7, 0x0BDBDF21, 0x86D3D2D4, 0xF1D4E242,
        0x68DDB3F8, 0x1FDA836E, 0x81BE16CD, 0xF6B9265B, 0x6FB077E1,
        0x18B74777, 0x88085AE6, 0xFF0F6A70, 0x66063BCA, 0x11010B5C,
        0x8F659EFF, 0xF862AE69, 0x616BFFD3, 0x166CCF45, 0xA00AE278,
        0xD70DD2EE, 0x4E048354, 0x3903B3C2, 0xA7672661, 0xD06016F7,
        0x4969474D, 0x3E6E77DB, 0xAED16A4A, 0xD9D65ADC, 0x40DF0B66,
        0x37D83BF0, 0xA9BCAE53, 0xDEBB9EC5, 0x47B2CF7F, 0x30B5FFE9,
        0xBDBDF21C, 0xCABAC28A, 0x53B39330, 0x24B4A3A6, 0xBAD03605,
        0xCDD70693, 0x54DE5729, 0x23D967BF, 0xB3667A2E, 0xC4614AB8,
        0x5D681B02, 0x2A6F2B94, 0xB40BBE37, 0xC30C8EA1, 0x5A05DF1B,
        0x2D02EF8D
    };

    /**
     * Calculates the CRC32 of the given bytes (conforms to RFC 1510, SSH-1).
     * The CRC32 calculation is similar to the standard one as demonstrated in RFC 1952,
     * but with the inversion (before and after the calculation) omitted.
     * <ul>
     * <li>poly:    0x04C11DB7</li>
     * <li>init:    0x00000000</li>
     * <li>xor:     0x00000000</li>
     * <li>revin:   true</li>
     * <li>revout:  true</li>
     * <li>check:   0x2DFD2D88 (CRC32 of "123456789")</li>
     * </ul>
     * @param buf the byte array to calculate CRC32 on
     * @param off the offset within buf at which the CRC32 calculation will start
     * @param len the number of bytes on which to calculate the CRC32
     * @return the CRC32 value (unsigned 32-bit integer stored in a long).
     */
    static private long calculateCRC32(byte[] buf, int off, int len) {
        int c = 0;
        int end = off + len;
        for (int i = off; i < end; i++) {
          c = CRC32_TABLE_0x04C11DB7[(c ^ buf[i]) & 0xFF] ^ (c >>> 8);
        }
        long result = c;
        if (result < 0) {
          result = result - 0xFFFFFFFF00000000L;
        }
        return result;
    }
    
    /**
     * Get property tag id by property set GUID and string name or numerical name from named properties mapping
     * @param guid Property set GUID in registry format without brackets.
     *             May be one of the PS_* or PSETID_* constants
     * @param name Property name in case of string named property
     * @param id   Property id in case of numerical named property
     * @return Property tag which can be matched with {@link org.apache.poi.hsmf.datatypes.MAPIProperty#id}
     *         or 0 if the property could not be found.
     * 
     */
    public long GetPropertyTag(String guid, String name, long id) {
        if (guidStream != null && entryStream != null && stringStream != null && guid != null) {
            byte[] entryStreamBytes = entryStream.getValue();
            byte[] stringStreamBytes = stringStream.getValue();
            byte[] guidStreamBytes = guidStream.getValue();
            int entryStreamEntryCount = entryStreamBytes != null ? entryStreamBytes.length / 8 : 0;
            for (int i = 0; i < entryStreamEntryCount; i++) {
                long nameIdentifierStringOffset = LittleEndian.getUInt(entryStreamBytes, i * 8);
                int propertyIndex = LittleEndian.getUShort(entryStreamBytes, i * 8 + 6);
                int guidIndex = LittleEndian.getUShort(entryStreamBytes, i * 8 + 4);
                int propertyKind = guidIndex & 0x01;
                guidIndex = guidIndex >> 1;
                //
                // fetch and match property GUID
                //
                String propertyGUID = null;
                if (guidIndex == 1) {
                    //
                    // predefined GUID
                    //
                    propertyGUID = PS_MAPI;
                } else if (guidIndex == 2) {
                    //
                    // predefined GUID
                    //
                    propertyGUID = PS_PUBLIC_STRINGS;
                } else if (guidIndex >= 3) {
                    //
                    // GUID from guid stream
                    //
                    int guidIndexOffset = (guidIndex - 3) * 0x10;
                    if (guidStreamBytes.length >= guidIndexOffset + 0x10) {
                        byte[] b = Arrays.copyOfRange(guidStreamBytes, guidIndexOffset, guidIndexOffset + 0x10);
                        swap(b, 0, 3);
                        swap(b, 1, 2);
                        swap(b, 4, 5);
                        swap(b, 6, 7);
                        ByteBuffer bb = ByteBuffer.wrap(b);
                        long high = bb.getLong();
                        long low = bb.getLong();
                        UUID uuid = new UUID(high, low);
                        propertyGUID = uuid.toString();
                    }
                }
                //
                // match GUID
                //
                if (guid.equalsIgnoreCase(propertyGUID)) {
                    //
                    // fetch property name / stream ID
                    //
                    String propertyName = null;
                    long propertyNameCRC32 = -1;
                    long streamID;
                    if (propertyKind == 1) {
                        //
                        // string named property
                        //
                        if (stringStreamBytes.length > nameIdentifierStringOffset) {
                            long nameLength = LittleEndian.getUInt(stringStreamBytes, (int) nameIdentifierStringOffset);
                            if (stringStreamBytes.length >= nameIdentifierStringOffset + 4 + nameLength) {
                                byte[] n = Arrays.copyOfRange(stringStreamBytes, (int) nameIdentifierStringOffset + 4,
                                        (int) nameIdentifierStringOffset + 4 + (int) nameLength);
                                propertyName = new String(n, StringUtil.UTF16LE);
                                if (guid.equalsIgnoreCase(PS_INTERNET_HEADERS)) {
                                    n = propertyName.toLowerCase().getBytes(StringUtil.UTF16LE);
                                }
                                propertyNameCRC32 = calculateCRC32(n, 0, n.length);
                            }
                        }
                        streamID = 0x1000 + (propertyNameCRC32 ^ ((guidIndex << 1) | 1)) % 0x1F;
                    } else {
                        //
                        // numerical named property
                        //
                        streamID = 0x1000 + (nameIdentifierStringOffset ^ (guidIndex << 1)) % 0x1F;
                    }
                    //
                    // match name/id
                    //
                    boolean matches = false;
                    //
                    // property set GUID matches
                    //
                    if (name != null) {
                        //
                        // match property by name
                        //
                        matches = propertyKind == 1 && name.equals(propertyName);
                    } else if (id >= 0) {
                        //
                        // match property by id
                        //
                        matches = propertyKind == 0 && id == nameIdentifierStringOffset;
                    }
                    if (matches) {
                        //
                        // find property index in matching stream entry
                        //
                        if (propertyKind == 1 && propertyNameCRC32 < 0) {
                            //
                            // skip stream entry matching and return tag from property index from entry stream
                            // this code should not be reached
                            //
                            return 0x8000 + propertyIndex;
                        }
                        long propertyTag = 0;
                        for (Chunk chunk : allChunks) {
                            if (chunk.getType() == Types.BINARY && chunk.getChunkId() == streamID) {
                                ByteChunk matchChunk = (ByteChunk) chunk;
                                byte[] matchChunkBytes = matchChunk.getValue();
                                int matchChunkEntryCount = matchChunkBytes != null ? matchChunkBytes.length / 8 : 0;
                                for (int m = 0; m < matchChunkEntryCount; m++) {
                                    long nameIdentifierStringCRC32 = LittleEndian.getUInt(matchChunkBytes, m * 8);
                                    int matchPropertyIndex = LittleEndian.getUShort(matchChunkBytes, m * 8 + 6);
                                    int matchGuidIndex = LittleEndian.getUShort(matchChunkBytes, m * 8 + 4);
                                    int matchPropertyKind = matchGuidIndex & 0x01;
                                    matchGuidIndex = matchGuidIndex >> 1;
                                    if (matchPropertyKind == 0) {
                                        if (nameIdentifierStringCRC32 == nameIdentifierStringOffset) {
                                            propertyTag = 0x8000 + matchPropertyIndex;
                                            break;
                                        }
                                    } else {
                                        if (nameIdentifierStringCRC32 == propertyNameCRC32) {
                                            propertyTag = 0x8000 + matchPropertyIndex;
                                            break;
                                        }
                                    }
                                }
                                break;
                            }
                        }
                        return propertyTag;
                    }
                }
            }
        }
        return 0;
    }
}
