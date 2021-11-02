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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LocaleUtil;

import static org.apache.logging.log4j.util.Unbox.box;

/**
 * Factor class to create the appropriate chunks, which
 *  needs the version of the file to process the chunk header
 *  and trailer areas.
 * Makes use of chunks_parse_cmds.tbl from vsdump to be able
 *  to process the chunk value area
 */
public final class ChunkFactory {

    //arbitrarily selected; may need to increase
    private static final int DEFAULT_MAX_RECORD_LENGTH = 1_000_000;
    private static int MAX_RECORD_LENGTH = DEFAULT_MAX_RECORD_LENGTH;

    /** The version of the currently open document */
    private int version;
    /**
     * Key is a Chunk's type, value is an array of its CommandDefinitions
     */
    private final Map<Integer, CommandDefinition[]> chunkCommandDefinitions =
            new HashMap<>();
    /**
     * What the name is of the chunk table definitions file?
     * This file comes from the scratchpad resources directory.
     */
    private static final String chunkTableName =
        "/org/apache/poi/hdgf/chunks_parse_cmds.tbl";

    /** For logging problems we spot with the file */
    private static final Logger LOG = LogManager.getLogger(ChunkFactory.class);

    /**
     * @param length the max record length allowed for ChunkFactory
     */
    public static void setMaxRecordLength(int length) {
        MAX_RECORD_LENGTH = length;
    }

    /**
     * @return the max record length allowed for ChunkFactory
     */
    public static int getMaxRecordLength() {
        return MAX_RECORD_LENGTH;
    }

    public ChunkFactory(int version) throws IOException {
        this.version = version;

        processChunkParseCommands();
    }

    /**
     * Open chunks_parse_cmds.tbl and process it, to get the definitions
     *  of all the different possible chunk commands.
     */
    private void processChunkParseCommands() throws IOException {
        try (InputStream cpd = ChunkFactory.class.getResourceAsStream(chunkTableName)) {
            if(cpd == null) {
                throw new IllegalStateException("Unable to find HDGF chunk definition on the classpath - " + chunkTableName);
            }

            try (BufferedReader inp = new BufferedReader(new InputStreamReader(cpd, LocaleUtil.CHARSET_1252))) {
                String line;
                while ((line = inp.readLine()) != null) {
                    if (line.isEmpty() || "# \t".contains(line.substring(0, 1))) {
                        continue;
                    }

                    // Start xxx
                    if (!line.matches("^start [0-9]+$")) {
                        throw new IllegalStateException("Expecting start xxx, found " + line);
                    }
                    int chunkType = Integer.parseInt(line.substring(6));
                    ArrayList<CommandDefinition> defsL = new ArrayList<>();

                    // Data entries
                    while ((line = inp.readLine()) != null) {
                        if (line.startsWith("end")) {
                            break;
                        }
                        StringTokenizer st = new StringTokenizer(line, " ");
                        int defType = Integer.parseInt(st.nextToken());
                        int offset = Integer.parseInt(st.nextToken());
                        String name = st.nextToken("\uffff").substring(1);

                        CommandDefinition def = new CommandDefinition(defType, offset, name);
                        defsL.add(def);
                    }

                    CommandDefinition[] defs = defsL.toArray(new CommandDefinition[0]);

                    // Add to the map
                    chunkCommandDefinitions.put(chunkType, defs);
                }
            }
        }
    }

    public int getVersion() { return version; }

    /**
     * Creates the appropriate chunk at the given location.
     *
     * @param data the chunk bytes
     * @param offset the offset into the chunk bytes array to start reading from
     *
     * @return the new Chunk
     */
    public Chunk createChunk(byte[] data, int offset) {
        // Create the header
        ChunkHeader header =
            ChunkHeader.createChunkHeader(version, data, offset);
        // Sanity check
        if(header.getLength() < 0) {
            throw new IllegalArgumentException("Found a chunk with a negative length, which isn't allowed");
        }

        // How far up to look
        int endOfDataPos = offset + header.getLength() + header.getSizeInBytes();

        // Check we have enough data, and tweak the header size
        //  as required
        if(endOfDataPos > data.length) {
            LOG.atWarn().log("Header called for {} bytes, but that would take us past the end of the data!", box(header.getLength()));

            endOfDataPos = data.length;
            header.setLength(data.length - offset - header.getSizeInBytes());

            if(header.hasTrailer()) {
                header.setLength(header.getLength() - 8);
                endOfDataPos  -= 8;
            }
            if(header.hasSeparator()) {
                header.setLength(header.getLength() - 4);
                endOfDataPos  -= 4;
            }
        }


        // Create the trailer and separator, if required
        ChunkTrailer trailer = null;
        ChunkSeparator separator = null;
        if(header.hasTrailer()) {
            if(endOfDataPos <= data.length-8) {
                trailer = new ChunkTrailer(
                    data, endOfDataPos);
                endOfDataPos += 8;
            } else {
                LOG.atError().log("Header claims a length to {} there's then no space for the trailer in the data ({})", box(endOfDataPos),box(data.length));
            }
        }
        if(header.hasSeparator()) {
            if(endOfDataPos <= data.length-4) {
                separator = new ChunkSeparator(
                        data, endOfDataPos);
            } else {
                LOG.atError().log("Header claims a length to {} there's then no space for the separator in the data ({})", box(endOfDataPos),box(data.length));
            }
        }

        // Now, create the chunk
        byte[] contents = IOUtils.safelyClone(data, offset+header.getSizeInBytes(), header.getLength(), MAX_RECORD_LENGTH);
        Chunk chunk = new Chunk(header, trailer, separator, contents);

        // Feed in the stuff from  chunks_parse_cmds.tbl
        CommandDefinition[] defs = chunkCommandDefinitions.get(header.getType());
        if (defs == null) {
            defs = new CommandDefinition[0];
        }
        chunk.setCommandDefinitions(defs);

        // Now get the chunk to process its commands
        chunk.processCommands();

        // All done
        return chunk;
    }

    /**
     * The definition of a Command, which a chunk may hold.
     * The Command holds the value, this describes it.
     */
    public static class CommandDefinition {
        private int type;
        private int offset;
        private String name;
        public CommandDefinition(int type, int offset, String name) {
            this.type = type;
            this.offset = offset;
            this.name = name;
        }

        public String getName() {
            return name;
        }
        public int getOffset() {
            return offset;
        }
        public int getType() {
            return type;
        }
    }
}
