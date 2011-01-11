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

import static org.apache.poi.hsmf.datatypes.MAPIAttribute.ATTACH_ADDITIONAL_INFO;
import static org.apache.poi.hsmf.datatypes.MAPIAttribute.ATTACH_CONTENT_BASE;
import static org.apache.poi.hsmf.datatypes.MAPIAttribute.ATTACH_CONTENT_LOCATION;
import static org.apache.poi.hsmf.datatypes.MAPIAttribute.ATTACH_DATA;
import static org.apache.poi.hsmf.datatypes.MAPIAttribute.ATTACH_DISPOSITION;
import static org.apache.poi.hsmf.datatypes.MAPIAttribute.ATTACH_ENCODING;
import static org.apache.poi.hsmf.datatypes.MAPIAttribute.ATTACH_EXTENSION;
import static org.apache.poi.hsmf.datatypes.MAPIAttribute.ATTACH_FILENAME;
import static org.apache.poi.hsmf.datatypes.MAPIAttribute.ATTACH_FLAGS;
import static org.apache.poi.hsmf.datatypes.MAPIAttribute.ATTACH_LONG_FILENAME;
import static org.apache.poi.hsmf.datatypes.MAPIAttribute.ATTACH_LONG_PATHNAME;
import static org.apache.poi.hsmf.datatypes.MAPIAttribute.ATTACH_MIME_TAG;
import static org.apache.poi.hsmf.datatypes.MAPIAttribute.ATTACH_RENDERING;
import static org.apache.poi.hsmf.datatypes.MAPIAttribute.ATTACH_SIZE;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Collection of convenence chunks for standard parts of the MSG file attachment.
 */
public class AttachmentChunks implements ChunkGroup {
   public static final String PREFIX = "__attach_version1.0_#";
   
   public ByteChunk attachData;
   public StringChunk attachExtension;
   public StringChunk attachFileName;
   public StringChunk attachLongFileName;
   public StringChunk attachMimeTag;
   public DirectoryChunk attachmentDirectory;
   
   /** 
    * This is in WMF Format. You'll probably want to pass it
    *  to Apache Batik to turn it into a SVG that you can
    *  then display. 
    */
   public ByteChunk attachRenderingWMF;
   
   /**
    * What the POIFS name of this attachment is.
    */
   private String poifsName;

   /** Holds all the chunks that were found. */
   private List<Chunk> allChunks = new ArrayList<Chunk>();

   
   public AttachmentChunks(String poifsName) {
      this.poifsName = poifsName;
   }
   
   public Chunk[] getAll() {
      return allChunks.toArray(new Chunk[allChunks.size()]);
   }
   public Chunk[] getChunks() {
      return getAll();
   }
   
   public String getPOIFSName() {
      return poifsName;
   }
   
   /**
    * Called by the parser whenever a chunk is found.
    */
   public void record(Chunk chunk) {
      if(chunk.getChunkId() == ATTACH_ADDITIONAL_INFO.id) {
         // TODO
      }
      else if(chunk.getChunkId() == ATTACH_CONTENT_BASE.id) {
         // TODO
      }
      else if(chunk.getChunkId() == ATTACH_CONTENT_LOCATION.id) {
         // TODO
      }
      else if(chunk.getChunkId() == ATTACH_DATA.id) {
         if(chunk instanceof ByteChunk) {
             attachData = (ByteChunk)chunk;
         } else if(chunk instanceof DirectoryChunk) {
             attachmentDirectory = (DirectoryChunk)chunk;
         } else {
             System.err.println("Unexpected data chunk of type " + chunk);
         }
      }
      else if(chunk.getChunkId() == ATTACH_DISPOSITION.id) {
         // TODO
      }
      else if(chunk.getChunkId() == ATTACH_ENCODING.id) {
         // TODO
      }
      else if(chunk.getChunkId() == ATTACH_EXTENSION.id) {
         attachExtension = (StringChunk)chunk;
      }
      else if(chunk.getChunkId() == ATTACH_FILENAME.id) {
         attachFileName = (StringChunk)chunk;
      }
      else if(chunk.getChunkId() == ATTACH_FLAGS.id) {
         // TODO
      }
      else if(chunk.getChunkId() == ATTACH_LONG_FILENAME.id) {
         attachLongFileName = (StringChunk)chunk;
      }
      else if(chunk.getChunkId() == ATTACH_LONG_PATHNAME.id) {
         // TODO
      }
      else if(chunk.getChunkId() == ATTACH_MIME_TAG.id) {
         attachMimeTag = (StringChunk)chunk;
      }
      else if(chunk.getChunkId() == ATTACH_RENDERING.id) {
         attachRenderingWMF = (ByteChunk)chunk;
      }
      else if(chunk.getChunkId() == ATTACH_SIZE.id) {
         // TODO
      }

      // And add to the main list
      allChunks.add(chunk);
   }
   
   /**
    * Orders by the attachment number.
    */
   public static class AttachmentChunksSorter implements Comparator<AttachmentChunks> {
      public int compare(AttachmentChunks a, AttachmentChunks b) {
         return a.poifsName.compareTo(b.poifsName);
      }
   }
}
