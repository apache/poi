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

import java.io.FileInputStream;
import java.io.IOException;

import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.POIDataSamples;

import junit.framework.TestCase;

/**
 * Tests to verify that the chunk parser works properly
 */
public final class TestPOIFSChunkParser extends TestCase {
   private POIDataSamples samples;

	public TestPOIFSChunkParser() throws IOException {
        samples = POIDataSamples.getHSMFInstance();
	}
	
   public void testFindsRecips() throws IOException {
      
   }
   
	public void testFindsAttachments() throws IOException {
	   POIFSFileSystem with = new POIFSFileSystem(
	         new FileInputStream(samples.getFile("attachment_test_msg.msg"))
	   );
      POIFSFileSystem without = new POIFSFileSystem(
            new FileInputStream(samples.getFile("simple_test_msg.msg"))
      );
      
      // Check details on the one with
	      
	   // One with, from the top
	   
	   // One without, from the top
	}
}
