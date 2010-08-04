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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Collection of convenence chunks for standard parts of the MSG file attachment.
 */
public class AttachmentChunks implements ChunkGroup {
   public static final String PREFIX = "__attach_version1.0_#";
   
   /* String parts of Outlook Messages Attachments that are currently known */
   public static final int ATTACH_DATA          = 0x3701;
   // 0x3702 might be "attach encoding"
   public static final int ATTACH_EXTENSION     = 0x3703;
   public static final int ATTACH_FILENAME      = 0x3704;
   // 0x3705 might be "attach method"
   public static final int ATTACH_LONG_FILENAME = 0x3707;
   public static final int ATTACH_RENDERING_WMF = 0x3709;
   // 0x370B might be "rendering position"
   public static final int ATTACH_MIME_TAG      = 0x370E;

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
      switch(chunk.getChunkId()) {
      case ATTACH_DATA:
         if(chunk instanceof ByteChunk) {
             attachData = (ByteChunk)chunk;
         } else if(chunk instanceof DirectoryChunk) {
             attachmentDirectory = (DirectoryChunk)chunk;
         } else {
             System.err.println("Unexpected data chunk of type " + chunk);
         }
         break;
      case ATTACH_EXTENSION:
         attachExtension = (StringChunk)chunk;
         break;
      case ATTACH_FILENAME:
         attachFileName = (StringChunk)chunk;
         break;
      case ATTACH_LONG_FILENAME:
         attachLongFileName = (StringChunk)chunk;
         break;
      case ATTACH_MIME_TAG:
         attachMimeTag = (StringChunk)chunk;
         break;
      case ATTACH_RENDERING_WMF:
         attachRenderingWMF = (ByteChunk)chunk;
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
