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

package org.apache.poi.hmef;

import junit.framework.TestCase;

import org.apache.poi.POIDataSamples;

public final class TestAttachments extends TestCase {
   private static final POIDataSamples _samples = POIDataSamples.getHMEFInstance();

   /**
    * Check the file is as we expect
    */
	public void testCounts() throws Exception {
      HMEFMessage msg = new HMEFMessage(
            _samples.openResourceAsStream("quick-winmail.dat")
      );
      
      // Should have 5 attachments
      assertEquals(5, msg.getAttachments().size());
	}
   
	/**
	 * Check some basic bits about the attachments 
	 */
   public void testBasicAttachments() throws Exception {
      // TODO
   }
   
   /**
    * Query the attachments in detail, and check we see
    *  the right values for key things
    */
   public void testAttachmentDetails() throws Exception {
      // TODO
   }

   /**
    * Ensure the attachment contents come back as they should do 
    */
   public void testAttachmentContents() throws Exception {
      // TODO
   }
}
