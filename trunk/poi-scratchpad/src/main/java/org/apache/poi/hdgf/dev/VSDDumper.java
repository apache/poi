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

package org.apache.poi.hdgf.dev;

import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;

import org.apache.poi.hdgf.HDGFDiagram;
import org.apache.poi.hdgf.chunks.Chunk;
import org.apache.poi.hdgf.chunks.Chunk.Command;
import org.apache.poi.hdgf.pointers.Pointer;
import org.apache.poi.hdgf.streams.ChunkStream;
import org.apache.poi.hdgf.streams.PointerContainingStream;
import org.apache.poi.hdgf.streams.Stream;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * Developer helper class to dump out the pointer+stream structure
 *  of a Visio file
 */
public final class VSDDumper {
    static final String tabs = "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t";

    private final PrintStream ps;
    private final HDGFDiagram hdgf;
    VSDDumper(PrintStream ps, HDGFDiagram hdgf) {
        this.ps = ps;
        this.hdgf = hdgf;
    }

    public static void main(String[] args) throws Exception {
        if(args.length == 0) {
            System.err.println("Use:");
            System.err.println("  VSDDumper <filename>");
            System.exit(1);
        }

        try (POIFSFileSystem poifs = new POIFSFileSystem(new File(args[0]));
             HDGFDiagram hdgf = new HDGFDiagram(poifs)) {
            PrintStream ps = System.out;
            ps.println("Opened " + args[0]);
            VSDDumper vd = new VSDDumper(ps, hdgf);
            vd.dumpFile();
        }
    }

    public void dumpFile() {
        dumpVal("Claimed document size", hdgf.getDocumentSize(), 0);
        ps.println();
        dumpStream(hdgf.getTrailerStream(), 0);
    }

    private void dumpStream(Stream stream, int indent) {
        Pointer ptr = stream.getPointer();
        dumpVal("Stream at", ptr.getOffset(), indent);
        dumpVal("Type is", ptr.getType(), indent+1);
        dumpVal("Format is", ptr.getFormat(), indent+1);
        dumpVal("Length is", ptr.getLength(), indent+1);
        if(ptr.destinationCompressed()) {
            dumpVal("DC.Length is", stream._getContentsLength(), indent+1);
        }
        dumpVal("Compressed is", ptr.destinationCompressed(), indent+1);
        dumpVal("Stream is", stream.getClass().getName(), indent+1);

        byte[] db = stream._getStore()._getContents();
        String ds = (db.length >= 8) ? Arrays.toString(db) : "[]";
        dumpVal("First few bytes are", ds, indent+1);

        if (stream instanceof PointerContainingStream) {
            Stream[] streams = ((PointerContainingStream) stream).getPointedToStreams();
            dumpVal("Nbr of children", streams.length, indent+1);

            for(Stream s : streams) {
                dumpStream(s, indent+1);
            }
        }
        if(stream instanceof ChunkStream) {
            Chunk[] chunks = ((ChunkStream) stream).getChunks();
            dumpVal("Nbr of chunks", chunks.length, indent+1);

            for(Chunk chunk : chunks) {
                dumpChunk(chunk, indent+1);
            }
        }
    }

    private void dumpChunk(Chunk chunk, int indent) {
        dumpVal(chunk.getName(), "", indent);
        dumpVal("Length is", chunk._getContents().length, indent);
        dumpVal("OD Size is", chunk.getOnDiskSize(), indent);
        dumpVal("T / S is", chunk.getTrailer() + " / " + chunk.getSeparator(), indent);
        Command[] commands = chunk.getCommands();
        dumpVal("Nbr of commands", commands.length, indent);
        for(Command command : commands) {
            dumpVal(command.getDefinition().getName(), ""+command.getValue(), indent+1);
        }
    }

    private void dumpVal(String label, long value, int indent) {
        ps.print(tabs.substring(0,indent));
        ps.print(label);
        ps.print('\t');
        ps.print(value);
        ps.print(" (0x");
        ps.print(Long.toHexString(value));
        ps.println(")");
    }

    private void dumpVal(String label, boolean value, int indent) {
        dumpVal(label, Boolean.toString(value), indent);
    }

    private void dumpVal(String label, String value, int indent) {
        ps.print(tabs.substring(0,indent));
        ps.print(label);
        ps.print('\t');
        ps.println(value);
    }
}
