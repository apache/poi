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

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hdgf.chunks.ChunkFactory.CommandDefinition;
import org.apache.poi.util.LittleEndian;

import static org.apache.logging.log4j.util.Unbox.box;

/**
 * Base of all chunks, which hold data, flags etc
 */
public final class Chunk {
    /** For logging warnings about the structure of the file */
    private static final Logger LOG = LogManager.getLogger(Chunk.class);

    /**
     * The contents of the chunk, excluding the header,
     * trailer and separator
     */
    private final byte[] contents;
    private final ChunkHeader header;
    /** May be null */
    private final ChunkTrailer trailer;
    /** May be null */
    private final ChunkSeparator separator;
    /** The possible different commands we can hold */
    private CommandDefinition[] commandDefinitions;
    /** The command+value pairs we hold */
    private Command[] commands;
    /* The blocks (if any) we hold */
    //private Block[] blocks
    /** The name of the chunk, as found from the commandDefinitions */
    private String name;

    public Chunk(ChunkHeader header, ChunkTrailer trailer, ChunkSeparator separator, byte[] contents) {
        this.header = header;
        this.trailer = trailer;
        this.separator = separator;
        this.contents = contents.clone();
    }

    public byte[] _getContents() {
        return contents;
    }

    public ChunkHeader getHeader() {
        return header;
    }

    /**
     * Gets the separator between this chunk and the next, if it exists
     *
     * @return the separator
     */
    public ChunkSeparator getSeparator() {
        return separator;
    }

    /**
     * Gets the trailer for this chunk, if it exists
     *
     * @return the trailer
     */
    public ChunkTrailer getTrailer() {
        return trailer;
    }

    /**
     * Gets the command definitions, which define and describe much
     *  of the data held by the chunk.
     *
     * @return the command definitions
     */
    @SuppressWarnings("unused")
    public CommandDefinition[] getCommandDefinitions() {
        return commandDefinitions;
    }

    void setCommandDefinitions(CommandDefinition[] commandDefinitions) {
        this.commandDefinitions = commandDefinitions;
    }

    public Command[] getCommands() {
        return commands;
    }

    /**
     * Get the name of the chunk, as found from the CommandDefinitions
     *
     * @return the name of the chunk
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the size of the chunk, including any
     *  headers, trailers and separators.
     *
     * @return the size of the chunk
     */
    public int getOnDiskSize() {
        int size = header.getSizeInBytes() + contents.length;
        if(trailer != null) {
            size += trailer.getTrailerData().length;
        }
        if(separator != null) {
            size += separator.separatorData.length;
        }
        return size;
    }

    /**
     * Uses our CommandDefinitions to process the commands
     *  our chunk type has, and figure out the
     *  values for them.
     */
    void processCommands() {
        if(commandDefinitions == null) {
            throw new IllegalStateException("You must supply the command definitions before calling processCommands!");
        }

        // Loop over the definitions, building the commands
        //  and getting their values
        ArrayList<Command> commandList = new ArrayList<>();
        for(CommandDefinition cdef : commandDefinitions) {
            int type = cdef.getType();
            int offset = cdef.getOffset();

            // Handle virtual commands
            if(type == 10) {
                name = cdef.getName();
                continue;
            } else if(type == 18) {
                continue;
            }


            // Build the appropriate command for the type
            Command command;
            if(type == 11 || type == 21) {
                command = new BlockOffsetCommand(cdef);
            } else {
                command = new Command(cdef);
            }

            // Bizarrely, many of the offsets are from the start of the
            //  header, not from the start of the chunk body
            switch(type) {
            case 0: case 1: case 2: case 3: case 4: case 5: case 6: case 7:
            case 11: case 21:
            case 12: case 16: case 17: case 28: case 29:
                // Offset is from start of chunk (case 18 has been taken care of above)
                break;
            default:
                // Offset is from start of header!
                if(offset >= 19) {
                    offset -= 19;
                }
            }

            // Check we seem to have enough data
            if(offset >= contents.length) {
                LOG.atWarn().log("Command offset {} past end of data at {}", box(offset),box(contents.length));
                continue;
            }

            try {
                // Process
                switch(type) {
                // Types 0->7 = a flag at bit 0->7
                case 0: case 1: case 2: case 3: case 4: case 5: case 6: case 7:
                    command.value = ((contents[offset] >>> type) & 1) == 1;
                    break;
                case 8:
                    command.value = contents[offset];
                    break;
                case 9:
                    command.value = LittleEndian.getDouble(contents, offset);
                    break;
                case 12:
                    // A Little Endian String
                    // Starts 8 bytes into the data segment
                    // Ends at end of data, or 00 00

                    // Ensure we have enough data
                    if(contents.length < 8) {
                        command.value = "";
                        break;
                    }

                    // Find the end point
                    int startsAt = 8;
                    int endsAt = startsAt;
                    for(int j=startsAt; j<contents.length-1 && endsAt == startsAt; j++) {
                        if(contents[j] == 0 && contents[j+1] == 0) {
                            endsAt = j;
                        }
                    }
                    if(endsAt == startsAt) {
                        endsAt = contents.length;
                    }

                    int strLen = endsAt - startsAt;
                    command.value = new String(contents, startsAt, strLen, header.getChunkCharset());
                    break;
                case 25:
                    command.value = LittleEndian.getShort(contents, offset);
                    break;
                case 26:
                    command.value = LittleEndian.getInt(contents, offset);
                    break;

                // Types 11 and 21 hold the offset to the blocks
                case 11: case 21:
                    if(offset < contents.length - 3) {
                        int bOffset = (int)LittleEndian.getUInt(contents, offset);
                        BlockOffsetCommand bcmd = (BlockOffsetCommand)command;
                        bcmd.setOffset(bOffset);
                    }
                    break;

                default:
                    LOG.atInfo().log("Command of type {} not processed!", box(type));
                }
            }
            catch (Exception e) {
                LOG.atError().withThrowable(e).log("Unexpected error processing command, ignoring and continuing. Command: {}", command);
            }

            // Add to the array
            commandList.add(command);
        }

        // Save the commands we liked the look of
        this.commands = commandList.toArray(
                new Command[0]);

        // Now build up the blocks, if we had a command that tells
        //  us where a block is
    }

    /**
     * A command in the visio file. In order to make things fun,
     *  all the chunk actually stores is the value of the command.
     * You have to have your own lookup table to figure out what
     *  the commands are based on the chunk type.
     */
    public static class Command {
        protected Object value;
        private CommandDefinition definition;

        private Command(CommandDefinition definition, Object value) {
            this.definition = definition;
            this.value = value;
        }
        private Command(CommandDefinition definition) {
            this(definition, null);
        }

        public CommandDefinition getDefinition() { return definition; }
        public Object getValue() { return value; }
    }
    /*
     * A special kind of command that is an artificat of how we
     *  process CommandDefinitions, and so doesn't actually exist
     *  in the chunk
     */
//  public static class VirtualCommand extends Command {
//      private VirtualCommand(CommandDefinition definition) {
//          super(definition);
//      }
//  }
    /**
     * A special kind of command that holds the offset to
     *  a block
     */
    private static final class BlockOffsetCommand extends Command {
        private BlockOffsetCommand(CommandDefinition definition) {
            super(definition, null);
        }
        private void setOffset(int offset) {
            value = offset;
        }
    }
}
