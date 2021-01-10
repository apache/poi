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

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.poi.hmef.HMEFMessage;
import org.apache.poi.hmef.attribute.MAPIAttribute;
import org.apache.poi.hmef.attribute.TNEFAttribute;
import org.apache.poi.hmef.attribute.TNEFProperty;
import org.apache.poi.hsmf.datatypes.MAPIProperty;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.util.LittleEndian;
import org.junit.jupiter.api.Test;

class HMEFFileHandler extends AbstractFileHandler {

	@Override
	public void handleExtracting(File file) throws Exception {
		FileMagic fm = FileMagic.valueOf(file);
		if (fm == FileMagic.OLE2) {
			super.handleExtracting(file);
		}
	}

	@Override
    public void handleFile(InputStream stream, String path) throws Exception {
		HMEFMessage msg = new HMEFMessage(stream);

		// there are test-files that have no body...
		String[] HTML_BODY = {
			"Testing TNEF Message", "TNEF test message with attachments", "Test"
		};
		String bodyStr;
		if(Arrays.asList(HTML_BODY).contains(msg.getSubject())) {
			MAPIAttribute bodyHtml = msg.getMessageMAPIAttribute(MAPIProperty.BODY_HTML);
			assertNotNull(bodyHtml);
			bodyStr = new String(bodyHtml.getData(), getEncoding(msg));
		} else {
			bodyStr = msg.getBody();
		}
		assertNotNull( bodyStr, "Body is not set" );
		assertNotNull( msg.getSubject(), "Subject is not set" );
	}

	// a test-case to test this locally without executing the full TestAllFiles
	@Test
	void test() throws Exception {
	    String path = "test-data/hmef/quick-winmail.dat";
		try (InputStream stream = new FileInputStream(path)) {
			handleFile(stream, path);
		}
	}

	private String getEncoding(HMEFMessage tnefDat) {
		TNEFAttribute oemCP = tnefDat.getMessageAttribute(TNEFProperty.ID_OEMCODEPAGE);
		MAPIAttribute cpId = tnefDat.getMessageMAPIAttribute(MAPIProperty.INTERNET_CPID);
		int codePage = 1252;
		if (oemCP != null) {
			codePage = LittleEndian.getInt(oemCP.getData());
		} else if (cpId != null) {
			codePage =  LittleEndian.getInt(cpId.getData());
		}
		switch (codePage) {
			// see http://en.wikipedia.org/wiki/Code_page for more
			case 1252: return "Windows-1252";
			case 20127: return "US-ASCII";
			default: return "cp"+codePage;
		}
	}

}
