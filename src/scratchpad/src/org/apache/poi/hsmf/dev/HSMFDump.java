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

package org.apache.poi.hsmf.dev;

import java.io.FileInputStream;
import java.io.IOException;

import org.apache.poi.hsmf.datatypes.Chunk;
import org.apache.poi.hsmf.datatypes.ChunkGroup;
import org.apache.poi.hsmf.datatypes.MAPIAttribute;
import org.apache.poi.hsmf.datatypes.Types;
import org.apache.poi.hsmf.parsers.POIFSChunkParser;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * Dumps out the chunk details, and where possible contents
 */
public class HSMFDump {
   private POIFSFileSystem fs;
   public HSMFDump(POIFSFileSystem fs) {
      this.fs = fs;
   }
   
   public void dump() throws IOException {
      ChunkGroup[] chunkGroups = POIFSChunkParser.parse(fs);
      for(ChunkGroup chunks : chunkGroups) {
         System.out.println(chunks.getClass().getSimpleName());
         for(Chunk chunk : chunks.getChunks()) {
            MAPIAttribute attr = MAPIAttribute.get(chunk.getChunkId());
            
            String idName = attr.id + " - " + attr.name;
            if(attr == MAPIAttribute.UNKNOWN) {
               idName = chunk.getChunkId() + " - (unknown)";
            }
            
            System.out.println(
                  "   " + idName + " - " + 
                  Types.asName(chunk.getType())
            );
            System.out.println(
                  "       " + chunk.toString()
            );
         }
         System.out.println();
      }
   }
   
   public static void main(String[] args) throws Exception {
      for(String file : args) {
         POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(file));
         HSMFDump dump = new HSMFDump(fs);
         dump.dump();
      }
   }
}
