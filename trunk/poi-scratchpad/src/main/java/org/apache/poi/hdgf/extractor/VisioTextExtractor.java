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

package org.apache.poi.hdgf.extractor;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.extractor.POIOLE2TextExtractor;
import org.apache.poi.hdgf.HDGFDiagram;
import org.apache.poi.hdgf.chunks.Chunk;
import org.apache.poi.hdgf.chunks.Chunk.Command;
import org.apache.poi.hdgf.streams.ChunkStream;
import org.apache.poi.hdgf.streams.PointerContainingStream;
import org.apache.poi.hdgf.streams.Stream;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * Class to find all the text in a Visio file, and return it.
 * Can operate on the command line (outputs to stdout), or
 *  can return the text for you (example: for use with Lucene).
 */
public final class VisioTextExtractor implements POIOLE2TextExtractor {
    private HDGFDiagram hdgf;
    private boolean doCloseFilesystem = true;

    public VisioTextExtractor(HDGFDiagram hdgf) {
        this.hdgf = hdgf;
    }
    public VisioTextExtractor(POIFSFileSystem fs) throws IOException {
        this(fs.getRoot());
    }

    public VisioTextExtractor(DirectoryNode dir) throws IOException {
        this(new HDGFDiagram(dir));
    }

    public VisioTextExtractor(InputStream inp) throws IOException {
        this(new POIFSFileSystem(inp));
    }

    /**
     * Locates all the text entries in the file, and returns their
     *  contents.
     *
     * @return An array of each Text item in the document
     */
    public String[] getAllText() {
        List<String> text = new ArrayList<>();
        for(Stream stream : hdgf.getTopLevelStreams()) {
            findText(stream, text);
        }
        return text.toArray(new String[0]);
    }
    private void findText(Stream stream, List<String> text) {
        if(stream instanceof PointerContainingStream) {
            PointerContainingStream ps = (PointerContainingStream)stream;
            for(final Stream substream : ps.getPointedToStreams()) {
                findText(substream, text);
            }
        }
        if(stream instanceof ChunkStream) {
            ChunkStream cs = (ChunkStream)stream;
            for(final Chunk chunk : cs.getChunks()) {
                if(chunk != null &&
                        chunk.getName() != null &&
                        "Text".equals(chunk.getName()) &&
                        chunk.getCommands().length > 0) {

                    // First command
                    Command cmd = chunk.getCommands()[0];
                    if(cmd != null && cmd.getValue() != null) {
                        // Capture the text, as long as it isn't
                        //  simply an empty string
                        String str = cmd.getValue().toString();
                        if (!(str.isEmpty() || "\n".equals(str))) {
                            text.add( str );
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns the textual contents of the file.
     * Each textual object's text will be separated
     *  by a newline
     *
     * @return All text contained in this document, separated by <code>\n</code>
     */
    @Override
    public String getText() {
        StringBuilder text = new StringBuilder();
        for(String t : getAllText()) {
            text.append(t);
            if(!t.endsWith("\r") && !t.endsWith("\n")) {
                text.append('\n');
            }
        }
        return text.toString();
    }

    @Override
    public HDGFDiagram getDocument() {
        return hdgf;
    }

    @Override
    public void setCloseFilesystem(boolean doCloseFilesystem) {
        this.doCloseFilesystem = doCloseFilesystem;
    }

    @Override
    public boolean isCloseFilesystem() {
        return doCloseFilesystem;
    }

    @Override
    public HDGFDiagram getFilesystem() {
        return hdgf;
    }
}
