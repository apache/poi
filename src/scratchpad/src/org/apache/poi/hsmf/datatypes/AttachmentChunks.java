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

import static org.apache.poi.hsmf.datatypes.MAPIProperty.ATTACH_CONTENT_ID;
import static org.apache.poi.hsmf.datatypes.MAPIProperty.ATTACH_DATA;
import static org.apache.poi.hsmf.datatypes.MAPIProperty.ATTACH_EXTENSION;
import static org.apache.poi.hsmf.datatypes.MAPIProperty.ATTACH_FILENAME;
import static org.apache.poi.hsmf.datatypes.MAPIProperty.ATTACH_LONG_FILENAME;
import static org.apache.poi.hsmf.datatypes.MAPIProperty.ATTACH_MIME_TAG;
import static org.apache.poi.hsmf.datatypes.MAPIProperty.ATTACH_RENDERING;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.poi.hsmf.MAPIMessage;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * Collection of convenience chunks for standard parts of the MSG file
 * attachment.
 */
public class AttachmentChunks implements ChunkGroup {
    private static final POILogger LOG = POILogFactory.getLogger(AttachmentChunks.class);
    public static final String PREFIX = "__attach_version1.0_#";

    private ByteChunk attachData;
    private StringChunk attachExtension;
    private StringChunk attachFileName;
    private StringChunk attachLongFileName;
    private StringChunk attachMimeTag;
    private DirectoryChunk attachmentDirectory;
    private StringChunk attachContentId;

    /**
     * This is in WMF Format. You'll probably want to pass it to Apache Batik to
     * turn it into a SVG that you can then display.
     */
    public ByteChunk attachRenderingWMF;

    /**
     * What the POIFS name of this attachment is.
     */
    private String poifsName;

    /** Holds all the chunks that were found. */
    private List<Chunk> allChunks = new ArrayList<>();

    public AttachmentChunks(String poifsName) {
        this.poifsName = poifsName;
    }

    /**
     * Is this Attachment an embedded MAPI message?
     */
    public boolean isEmbeddedMessage() {
        return (attachmentDirectory != null);
    }

    /**
     * Returns the embedded MAPI message, if the attachment is an embedded
     * message, or null otherwise
     */
    public MAPIMessage getEmbeddedMessage() throws IOException {
        if (attachmentDirectory != null) {
            return attachmentDirectory.getAsEmbededMessage();
        }
        return null;
    }

    /**
     * Returns the embedded object, if the attachment is an object based
     * embedding (image, document etc), or null if it's an embedded message
     */
    public byte[] getEmbeddedAttachmentObject() {
        if (attachData != null) {
            return attachData.getValue();
        }
        return null;
    }

    public Chunk[] getAll() {
        return allChunks.toArray(new Chunk[allChunks.size()]);
    }

    @Override
    public Chunk[] getChunks() {
        return getAll();
    }

    public String getPOIFSName() {
        return poifsName;
    }

    /**
     * @return the ATTACH_DATA chunk
     */
    public ByteChunk getAttachData() {
        return attachData;
    }

    /**
     * @return the attachment extension
     */
    public StringChunk getAttachExtension() {
        return attachExtension;
    }

    /**
     * @return the attachment (short) filename
     */
    public StringChunk getAttachFileName() {
        return attachFileName;
    }

    /**
     * @return the attachment (long) filename
     */
    public StringChunk getAttachLongFileName() {
        return attachLongFileName;
    }

    /**
     * @return the attachment mimetag
     */
    public StringChunk getAttachMimeTag() {
        return attachMimeTag;
    }

    /**
     * @return the attachment directory
     */
    public DirectoryChunk getAttachmentDirectory() {
        return attachmentDirectory;
    }

    /**
     * @return the attachment preview bytes
     */
    public ByteChunk getAttachRenderingWMF() {
        return attachRenderingWMF;
    }

    /**
     * @return the attachment content ID
     */
    public StringChunk getAttachContentId() {
        return attachContentId;
    }

    /**
     * Called by the parser whenever a chunk is found.
     */
    @Override
    public void record(Chunk chunk) {
        // TODO: add further members for other properties like:
        // - ATTACH_ADDITIONAL_INFO
        // - ATTACH_CONTENT_BASE
        // - ATTACH_CONTENT_LOCATION
        // - ATTACH_DISPOSITION
        // - ATTACH_ENCODING
        // - ATTACH_FLAGS
        // - ATTACH_LONG_PATHNAME
        // - ATTACH_SIZE
        final int chunkId = chunk.getChunkId();
        if (chunkId == ATTACH_DATA.id) {
            if (chunk instanceof ByteChunk) {
                attachData = (ByteChunk) chunk;
            } else if (chunk instanceof DirectoryChunk) {
                attachmentDirectory = (DirectoryChunk) chunk;
            } else {
                LOG.log(POILogger.ERROR, "Unexpected data chunk of type " + chunk.getEntryName());
            }
        } else if (chunkId == ATTACH_EXTENSION.id) {
            attachExtension = (StringChunk) chunk;
        } else if (chunkId == ATTACH_FILENAME.id) {
            attachFileName = (StringChunk) chunk;
        } else if (chunkId == ATTACH_LONG_FILENAME.id) {
            attachLongFileName = (StringChunk) chunk;
        } else if (chunkId == ATTACH_MIME_TAG.id) {
            attachMimeTag = (StringChunk) chunk;
        } else if (chunkId == ATTACH_RENDERING.id) {
            attachRenderingWMF = (ByteChunk) chunk;
        } else if (chunkId == ATTACH_CONTENT_ID.id) {
            attachContentId = (StringChunk) chunk;
        } else {
            LOG.log(POILogger.WARN, "Currently unsupported attachment chunk property will be ignored. " + chunk.getEntryName());
        }

        // And add to the main list
        allChunks.add(chunk);
    }

    /**
     * Used to flag that all the chunks of the attachment have now been located.
     */
    @Override
    public void chunksComplete() {
        // Currently, we don't need to do anything special once
        // all the chunks have been located
    }

    /**
     * Orders by the attachment number.
     */
    public static class AttachmentChunksSorter
    implements Comparator<AttachmentChunks>, Serializable {
        @Override
        public int compare(AttachmentChunks a, AttachmentChunks b) {
            return a.poifsName.compareTo(b.poifsName);
        }
    }
}
