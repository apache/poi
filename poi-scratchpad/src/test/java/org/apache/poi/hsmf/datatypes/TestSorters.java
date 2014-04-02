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

import java.util.Arrays;

import org.apache.poi.hsmf.datatypes.AttachmentChunks.AttachmentChunksSorter;
import org.apache.poi.hsmf.datatypes.RecipientChunks.RecipientChunksSorter;

import junit.framework.TestCase;

/**
 * Checks that the sorters on the chunk groups order
 *  chunks properly.
 */
public final class TestSorters extends TestCase {
   public void testAttachmentChunksSorter() {
      AttachmentChunks[] chunks;
      
      // Simple
      chunks = new AttachmentChunks[] {
            new AttachmentChunks("__attach_version1.0_#00000001"),
            new AttachmentChunks("__attach_version1.0_#00000000"),
      };
      Arrays.sort(chunks, new AttachmentChunksSorter());
      assertEquals("__attach_version1.0_#00000000", chunks[0].getPOIFSName());
      assertEquals("__attach_version1.0_#00000001", chunks[1].getPOIFSName());
      
      // Lots, with gaps
      chunks = new AttachmentChunks[] {
            new AttachmentChunks("__attach_version1.0_#00000101"),
            new AttachmentChunks("__attach_version1.0_#00000001"),
            new AttachmentChunks("__attach_version1.0_#00000002"),
            new AttachmentChunks("__attach_version1.0_#00000005"),
            new AttachmentChunks("__attach_version1.0_#00000026"),
            new AttachmentChunks("__attach_version1.0_#00000000"),
            new AttachmentChunks("__attach_version1.0_#000000AB"),
      };
      Arrays.sort(chunks, new AttachmentChunksSorter());
      assertEquals("__attach_version1.0_#00000000", chunks[0].getPOIFSName());
      assertEquals("__attach_version1.0_#00000001", chunks[1].getPOIFSName());
      assertEquals("__attach_version1.0_#00000002", chunks[2].getPOIFSName());
      assertEquals("__attach_version1.0_#00000005", chunks[3].getPOIFSName());
      assertEquals("__attach_version1.0_#00000026", chunks[4].getPOIFSName());
      assertEquals("__attach_version1.0_#000000AB", chunks[5].getPOIFSName());
      assertEquals("__attach_version1.0_#00000101", chunks[6].getPOIFSName());
   }
   
   public void testRecipientChunksSorter() {
      RecipientChunks[] chunks;
      
      // Simple
      chunks = new RecipientChunks[] {
            new RecipientChunks("__recip_version1.0_#00000001"),
            new RecipientChunks("__recip_version1.0_#00000000"),
      };
      Arrays.sort(chunks, new RecipientChunksSorter());
      assertEquals(0, chunks[0].recipientNumber);
      assertEquals(1, chunks[1].recipientNumber);
      
      // Lots, with gaps
      chunks = new RecipientChunks[] {
            new RecipientChunks("__recip_version1.0_#00020001"),
            new RecipientChunks("__recip_version1.0_#000000FF"),
            new RecipientChunks("__recip_version1.0_#00000205"),
            new RecipientChunks("__recip_version1.0_#00000001"),
            new RecipientChunks("__recip_version1.0_#00000005"),
            new RecipientChunks("__recip_version1.0_#00000009"),
            new RecipientChunks("__recip_version1.0_#00000404"),
            new RecipientChunks("__recip_version1.0_#00000000"),
      };
      Arrays.sort(chunks, new RecipientChunksSorter());
      assertEquals(0, chunks[0].recipientNumber);
      assertEquals(1, chunks[1].recipientNumber);
      assertEquals(5, chunks[2].recipientNumber);
      assertEquals(9, chunks[3].recipientNumber);
      assertEquals(0xFF, chunks[4].recipientNumber);
      assertEquals(0x205, chunks[5].recipientNumber);
      assertEquals(0x404, chunks[6].recipientNumber);
      assertEquals(0x20001, chunks[7].recipientNumber);
   }
}
