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

package org.apache.poi.hsmf.parsers;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.apache.poi.hsmf.datatypes.AttachmentChunks;
import org.apache.poi.hsmf.datatypes.ByteChunk;
import org.apache.poi.hsmf.datatypes.ByteChunkDeferred;
import org.apache.poi.hsmf.datatypes.Chunk;
import org.apache.poi.hsmf.datatypes.ChunkGroup;
import org.apache.poi.hsmf.datatypes.Chunks;
import org.apache.poi.hsmf.datatypes.DirectoryChunk;
import org.apache.poi.hsmf.datatypes.MAPIProperty;
import org.apache.poi.hsmf.datatypes.MessagePropertiesChunk;
import org.apache.poi.hsmf.datatypes.MessageSubmissionChunk;
import org.apache.poi.hsmf.datatypes.NameIdChunks;
import org.apache.poi.hsmf.datatypes.PropertiesChunk;
import org.apache.poi.hsmf.datatypes.RecipientChunks;
import org.apache.poi.hsmf.datatypes.StoragePropertiesChunk;
import org.apache.poi.hsmf.datatypes.StringChunk;
import org.apache.poi.hsmf.datatypes.Types;
import org.apache.poi.hsmf.datatypes.Types.MAPIType;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.DocumentNode;
import org.apache.poi.poifs.filesystem.Entry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * Processes a POIFS of a .msg file into groups of Chunks, such as
 * core data, attachment #1 data, attachment #2 data, recipient
 * data and so on.
 */
public final class POIFSChunkParser {
    private static final POILogger LOG = POILogFactory.getLogger(POIFSChunkParser.class);

    private POIFSChunkParser() {}

    public static ChunkGroup[] parse(POIFSFileSystem fs) {
        return parse(fs.getRoot());
    }

    public static ChunkGroup[] parse(DirectoryNode node) {
        Chunks mainChunks = new Chunks();

        ArrayList<ChunkGroup> groups = new ArrayList<>();
        groups.add(mainChunks);

        // Find our top level children
        // Note - we don't handle children of children yet, as
        //  there doesn't seem to be any use of that in Outlook
        for (Entry entry : node) {
            if (entry instanceof DirectoryNode) {
                DirectoryNode dir = (DirectoryNode) entry;
                ChunkGroup group = null;

                // Do we know what to do with it?
                if (dir.getName().startsWith(AttachmentChunks.PREFIX)) {
                    group = new AttachmentChunks(dir.getName());
                }
                if (dir.getName().startsWith(NameIdChunks.NAME)) {
                    group = new NameIdChunks();
                }
                if (dir.getName().startsWith(RecipientChunks.PREFIX)) {
                    group = new RecipientChunks(dir.getName());
                }

                if (group != null) {
                    processChunks(dir, group);
                    groups.add(group);
                }
            }
        }

        // Now do the top level chunks
        processChunks(node, mainChunks);

        // All chunks are now processed, have the ChunkGroup
        // match up variable-length properties and their chunks
        for (ChunkGroup group : groups) {
            group.chunksComplete();
        }

        // Finish
        return groups.toArray(new ChunkGroup[0]);
    }

    /**
     * Creates all the chunks for a given Directory, but
     * doesn't recurse or descend
     */
    private static void processChunks(DirectoryNode node, ChunkGroup grouping) {
        final Map<Integer, MultiChunk> multiChunks = new TreeMap<>();

        for (Entry entry : node) {
            if (entry instanceof DocumentNode ||
                (entry instanceof DirectoryNode && entry.getName().endsWith(Types.DIRECTORY.asFileEnding()))) {
                process(entry, grouping, multiChunks);
            }
        }

        // Finish up variable length multivalued properties
        multiChunks.entrySet().stream()
            .flatMap(me -> me.getValue().getChunks().values().stream())
            .filter(Objects::nonNull)
            .forEach(grouping::record);
    }

    /**
     * Creates a chunk, and gives it to its parent group
     */
    private static void process(Entry entry, ChunkGroup grouping, Map<Integer, MultiChunk> multiChunks) {
        final String entryName = entry.getName();
        boolean[] isMultiValued = { false };

        // Is it a properties chunk? (They have special names)
        Chunk chunk = (PropertiesChunk.NAME.equals(entryName))
            ? readPropertiesChunk(grouping, entry)
            : readPrimitiveChunk(entry, isMultiValued, multiChunks);

        if (chunk == null) {
            return;
        }

        if (entry instanceof DocumentNode) {
            try (DocumentInputStream inp = new DocumentInputStream((DocumentNode) entry)) {
                chunk.readValue(inp);
            } catch (IOException e) {
                LOG.log(POILogger.ERROR, "Error reading from part ", entry.getName(), e);
            }
        }

        if (!isMultiValued[0]) {
            // multi value chunks will be grouped later, in the correct order
            grouping.record(chunk);
        }
    }

    private static Chunk readPropertiesChunk(ChunkGroup grouping, Entry entry) {
        if (grouping instanceof Chunks) {
            // These should be the properties for the message itself
            boolean isEmbedded = entry.getParent() != null && entry.getParent().getParent() != null;
            return new MessagePropertiesChunk(grouping, isEmbedded);
        } else {
            // Will be properties on an attachment or recipient
            return new StoragePropertiesChunk(grouping);
        }
    }

    private static Chunk readPrimitiveChunk(Entry entry, boolean[] isMultiValue, Map<Integer, MultiChunk> multiChunks) {
        final String entryName = entry.getName();
        final int splitAt = entryName.lastIndexOf('_');

        // Check it's a regular chunk
        if (entryName.length() < 9 || splitAt == -1) {
            // Name in the wrong format
            return null;
        }

        // Split it into its parts
        final String namePrefix = entryName.substring(0, splitAt + 1);
        final String ids = entryName.substring(splitAt + 1);

        // Make sure we got what we expected, should be of
        // the form __<name>_<id><type>
        if (namePrefix.equals("Olk10SideProps") || namePrefix.equals("Olk10SideProps_")) {
            // This is some odd Outlook 2002 thing, skip
            return null;
        } else if (splitAt > entryName.length() - 8) {
            // Underscores not the right place, something's wrong
            throw new IllegalArgumentException("Invalid chunk name " + entryName);
        }

        // Now try to turn it into id + type
        final int chunkId, typeId;
        try {
            chunkId = Integer.parseInt(ids.substring(0, 4), 16);
            int tid = Integer.parseInt(ids.substring(4, 8), 16);
            isMultiValue[0] = (tid & Types.MULTIVALUED_FLAG) != 0;
            typeId = tid & ~Types.MULTIVALUED_FLAG;
        } catch (NumberFormatException e) {
            // Name in the wrong format
            return null;
        }

        MAPIType type = Types.getById(typeId);
        if (type == null) {
            type = Types.createCustom(typeId);
        }

        // Special cases based on the ID
        if (chunkId == MAPIProperty.MESSAGE_SUBMISSION_ID.id) {
            return new MessageSubmissionChunk(namePrefix, chunkId, type);
        } else if (type == Types.BINARY && chunkId == MAPIProperty.ATTACH_DATA.id) {
            ByteChunkDeferred bcd = new ByteChunkDeferred(namePrefix, chunkId, type);
            if (entry instanceof DocumentNode) {
                bcd.readValue((DocumentNode) entry);
            }
            return bcd;
        } else {
            // Nothing special about this ID
            // So, do the usual thing which is by type
            if (isMultiValue[0]) {
                return readMultiValue(namePrefix, ids, chunkId, entry, type, multiChunks);
            } else {
                if (type == Types.DIRECTORY && entry instanceof DirectoryNode) {
                    return new DirectoryChunk((DirectoryNode) entry, namePrefix, chunkId, type);
                } else if (type == Types.BINARY) {
                    return new ByteChunk(namePrefix, chunkId, type);
                } else if (type == Types.ASCII_STRING || type == Types.UNICODE_STRING) {
                    return new StringChunk(namePrefix, chunkId, type);
                }
                // Type of an unsupported type! Skipping...
                LOG.log(POILogger.WARN, "UNSUPPORTED PROP TYPE ", entryName);
                return null;
            }
        }
    }


    private static Chunk readMultiValue(String namePrefix, String ids, int chunkId, Entry entry, MAPIType type,
                                       Map<Integer, MultiChunk> multiChunks) {
        long multiValueIdx = -1;
        if (ids.contains("-")) {
            String mvidxstr = ids.substring(ids.lastIndexOf('-') + 1);
            try {
                multiValueIdx = Long.parseLong(mvidxstr) & 0xFFFFFFFFL;
            } catch (NumberFormatException ignore) {
                LOG.log(POILogger.WARN, "Can't read multi value idx from entry ", entry.getName());
            }
        }

        final MultiChunk mc = multiChunks.computeIfAbsent(chunkId, k -> new MultiChunk());
        if (multiValueIdx == -1) {
            return new ByteChunk(chunkId, Types.BINARY) {
                @Override
                public void readValue(InputStream value) throws IOException {
                    super.readValue(value);
                    mc.setLength(getValue().length / 4);
                }
            };
        } else {
            final Chunk chunk;
            if (type == Types.BINARY) {
                chunk = new ByteChunk(namePrefix, chunkId, type);
            } else if (type == Types.ASCII_STRING || type == Types.UNICODE_STRING) {
                chunk = new StringChunk(namePrefix, chunkId, type);
            } else {
                // Type of an unsupported multivalued type! Skipping...
                LOG.log(POILogger.WARN, "Unsupported multivalued prop type for entry ", entry.getName());
                return null;
            }
            mc.addChunk((int) multiValueIdx, chunk);
            return chunk;
        }
    }

    private static class MultiChunk {
        private int length = -1;
        private final Map<Integer,Chunk> chunks = new TreeMap<>();

        @SuppressWarnings("unused")
        int getLength() {
            return length;
        }

        void setLength(int length) {
            this.length = length;
        }

        void addChunk(int multiValueIdx, Chunk value) {
            chunks.put(multiValueIdx, value);
        }

        Map<Integer, Chunk> getChunks() {
            return chunks;
        }
    }
}
