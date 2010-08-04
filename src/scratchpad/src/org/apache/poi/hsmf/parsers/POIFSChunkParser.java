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
import java.util.ArrayList;

import org.apache.poi.hsmf.datatypes.AttachmentChunks;
import org.apache.poi.hsmf.datatypes.ByteChunk;
import org.apache.poi.hsmf.datatypes.Chunk;
import org.apache.poi.hsmf.datatypes.ChunkGroup;
import org.apache.poi.hsmf.datatypes.Chunks;
import org.apache.poi.hsmf.datatypes.DirectoryChunk;
import org.apache.poi.hsmf.datatypes.MessageSubmissionChunk;
import org.apache.poi.hsmf.datatypes.NameIdChunks;
import org.apache.poi.hsmf.datatypes.RecipientChunks;
import org.apache.poi.hsmf.datatypes.StringChunk;
import org.apache.poi.hsmf.datatypes.Types;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.DocumentNode;
import org.apache.poi.poifs.filesystem.Entry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * Processes a POIFS of a .msg file into groups of Chunks, such as
 * core data, attachment #1 data, attachment #2 data, recipient
 * data and so on.
 */
public final class POIFSChunkParser {
   public static ChunkGroup[] parse(POIFSFileSystem fs) throws IOException {
      return parse(fs.getRoot());
   }
   public static ChunkGroup[] parse(DirectoryNode node) throws IOException {
      Chunks mainChunks = new Chunks();
      
      ArrayList<ChunkGroup> groups = new ArrayList<ChunkGroup>();
      groups.add(mainChunks);

      // Find our top level children
      // Note - we don't handle children of children yet, as
      //  there doesn't seem to be any use of that in Outlook
      for(Entry entry : node) {
         if(entry instanceof DirectoryNode) {
            DirectoryNode dir = (DirectoryNode)entry;
            ChunkGroup group = null;
            
            // Do we know what to do with it?
            if(dir.getName().startsWith(AttachmentChunks.PREFIX)) {
               group = new AttachmentChunks(dir.getName());
            }
            if(dir.getName().startsWith(NameIdChunks.PREFIX)) {
               group = new NameIdChunks();
            }
            if(dir.getName().startsWith(RecipientChunks.PREFIX)) {
               group = new RecipientChunks(dir.getName());
            }
            
            if(group != null) {
               processChunks(dir, group);
               groups.add(group);
            } else {
               // Unknown directory, skip silently
            }
         }
      }
      
      // Now do the top level chunks
      processChunks(node, mainChunks);
      
      // Finish
      return groups.toArray(new ChunkGroup[groups.size()]);
   }
   
   /**
    * Creates all the chunks for a given Directory, but
    *  doesn't recurse or descend 
    */
   protected static void processChunks(DirectoryNode node, ChunkGroup grouping) {
      for(Entry entry : node) {
         if(entry instanceof DocumentNode) {
            process(entry, grouping);
         } else if(entry instanceof DirectoryNode) {
             if(entry.getName().endsWith(Types.asFileEnding(Types.DIRECTORY))) {
                 process(entry, grouping);
             }
         }
      }
   }
   
   /**
    * Creates a chunk, and gives it to its parent group 
    */
   protected static void process(Entry entry, ChunkGroup grouping) {
      String entryName = entry.getName();
      
      if(entryName.length() < 9) {
         // Name in the wrong format
         return;
      }
      if(entryName.indexOf('_') == -1) {
         // Name in the wrong format
         return;
      }
      
      // Split it into its parts
      int splitAt = entryName.lastIndexOf('_');
      if(splitAt == -1 || splitAt > (entryName.length()-8)) {
         throw new IllegalArgumentException("Invalid chunk name " + entryName);
      }
      
      // Now try to turn it into id + type
      String namePrefix = entryName.substring(0, splitAt+1);
      String ids = entryName.substring(splitAt+1);
      try {
         int chunkId = Integer.parseInt(ids.substring(0, 4), 16);
         int type    = Integer.parseInt(ids.substring(4, 8), 16);
         
         Chunk chunk = null;
         
         // Special cases based on the ID
         switch(chunkId) {
         case Chunks.SUBMISSION_ID_DATE:
            chunk = new MessageSubmissionChunk(namePrefix, chunkId, type);
            break;
         default:
            // Nothing special about this ID
            // So, do the usual thing which is by type
            switch(type) {
            case Types.BINARY:
               chunk = new ByteChunk(namePrefix, chunkId, type);
               break;
            case Types.DIRECTORY:
               if(entry instanceof DirectoryNode) {
                   chunk = new DirectoryChunk((DirectoryNode)entry, namePrefix, chunkId, type);
               }
               break;
            case Types.ASCII_STRING:
            case Types.UNICODE_STRING:
               chunk = new StringChunk(namePrefix, chunkId, type);
               break;
            }
         }
         
         if(chunk != null) {
             if(entry instanceof DocumentNode) {
                try {
                   DocumentInputStream inp = new DocumentInputStream((DocumentNode)entry);
                   chunk.readValue(inp);
                   grouping.record(chunk);
                } catch(IOException e) {
                   System.err.println("Error reading from part " + entry.getName() + " - " + e.toString());
                }
             } else {
                grouping.record(chunk);
             }
         }
      } catch(NumberFormatException e) {
         // Name in the wrong format
         return;
      }
   }
}
