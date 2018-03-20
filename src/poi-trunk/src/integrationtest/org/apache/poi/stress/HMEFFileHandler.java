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
package org.apache.poi.stress;

import static org.junit.Assert.assertNotNull;

import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.poi.hmef.HMEFMessage;
import org.apache.poi.hmef.attribute.MAPIAttribute;
import org.apache.poi.hmef.attribute.MAPIStringAttribute;
import org.junit.Test;

public class HMEFFileHandler extends AbstractFileHandler {

	@Override
    public void handleFile(InputStream stream, String path) throws Exception {
		HMEFMessage msg = new HMEFMessage(stream);
		
		// list all properties
		StringBuilder props = new StringBuilder();
		for(MAPIAttribute att : msg.getMessageMAPIAttributes()) {
			props.append(att.getType()).append(": ").append(MAPIStringAttribute.getAsString( att)).append("\n");
		}
		
		// there are two test-files that have no body...
		if(!msg.getSubject().equals("Testing TNEF Message") && !msg.getSubject().equals("TNEF test message with attachments")) {
    		assertNotNull("Had: " + msg.getBody() + ", " + msg.getSubject() + ", " + msg.getAttachments() + ": " + props,
    				msg.getBody());
		}
		assertNotNull("Had: " + msg.getBody() + ", " + msg.getSubject() + ", " + msg.getAttachments() + ": " + props,
				msg.getSubject());
	}
	
	// a test-case to test this locally without executing the full TestAllFiles
	@Test
	public void test() throws Exception {
	    String path = "test-data/hmef/quick-winmail.dat";
		try (InputStream stream = new FileInputStream(path)) {
			handleFile(stream, path);
		}
	}
}
